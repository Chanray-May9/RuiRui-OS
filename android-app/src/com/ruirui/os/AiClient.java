package com.ruirui.os;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

final class AiClient {
    interface StreamListener { void onText(String text); }
    static final class Config {
        String url,key,model,extra,headers,effort; int timeout;
        Config(String u,String k,String m,String e,String h,int t){this(u,k,m,e,h,t,"none");}
        Config(String u,String k,String m,String e,String h,int t,String reasoning){url=u.trim();key=k;model=m.trim();extra=e.trim();headers=h.trim();timeout=t;effort=reasoning;}
        static Config load(SharedPreferences p){return new Config(p.getString("url",""),p.getString("key",""),p.getString("model",""),p.getString("extra",""),p.getString("headers",""),p.getInt("timeout",90),p.getString("effort","none"));}
        String endpoint() throws Exception {
            String endpoint=url.replaceAll("/+$","");URL parsed=new URL(endpoint);
            if(!parsed.getProtocol().equals("https")&&!parsed.getProtocol().equals("http"))throw new Exception("URL 必须以 https:// 或 http:// 开头");
            if(parsed.getHost().isEmpty()||parsed.getUserInfo()!=null||parsed.getQuery()!=null||parsed.getRef()!=null)throw new Exception("请填写不含账号、查询参数或片段的接口 URL");
            return endpoint.endsWith("/chat/completions")?endpoint:endpoint+"/chat/completions";
        }
        void validate() throws Exception {
            endpoint();if(model.isEmpty())throw new Exception("请填写模型名称");if(timeout<10||timeout>300)throw new Exception("超时应为 10–300 秒");
            if(!extra.isEmpty())new JSONObject(extra);
            if(!headers.isEmpty()){JSONObject h=new JSONObject(headers);Iterator<String> it=h.keys();while(it.hasNext()){String n=it.next();Object v=h.get(n);if(!(v instanceof String)||n.contains("\n")||v.toString().contains("\n")||v.toString().contains("\r"))throw new Exception("请求头必须是字符串，不能包含换行");}}
            if(!effort.equals("none")&&!effort.equals("low")&&!effort.equals("medium")&&!effort.equals("high"))throw new Exception("思维强度只能选低、中或高");
        }
    }
    private final Config config;private volatile HttpURLConnection current;
    AiClient(Config c){config=c;}void cancel(){HttpURLConnection c=current;if(c!=null)c.disconnect();}
    String request(JSONArray messages)throws Exception{return request(messages,null);}
    String request(JSONArray messages,StreamListener listener)throws Exception {
        config.validate();JSONObject body=config.extra.isEmpty()?new JSONObject():new JSONObject(config.extra);
        body.put("model",config.model);body.put("messages",messages);boolean streaming=listener!=null;body.put("stream",streaming);
        if(!config.effort.equals("none"))body.put("reasoning_effort",config.effort);
        HttpURLConnection c=(HttpURLConnection)new URL(config.endpoint()).openConnection();current=c;
        try{
            c.setRequestMethod("POST");c.setConnectTimeout(20000);c.setReadTimeout(config.timeout*1000);c.setDoOutput(true);
            c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept",streaming?"text/event-stream, application/json":"application/json");
            if(!config.key.isEmpty())c.setRequestProperty("Authorization","Bearer "+config.key);
            if(!config.headers.isEmpty()){JSONObject h=new JSONObject(config.headers);Iterator<String> it=h.keys();while(it.hasNext()){String n=it.next();c.setRequestProperty(n,h.getString(n));}}
            try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}
            int status=c.getResponseCode();InputStream stream=status>=200&&status<300?c.getInputStream():c.getErrorStream();
            if(status<200||status>=300){String raw=readLimited(stream,1024*1024);String detail="";try{Object err=new JSONObject(raw).opt("error");if(err instanceof JSONObject)detail=((JSONObject)err).optString("message","");else if(err instanceof String)detail=(String)err;}catch(Exception ignored){}if(!config.key.isEmpty())detail=detail.replace(config.key,"[密钥已隐藏]");throw new Exception("HTTP "+status+" "+detail.substring(0,Math.min(300,detail.length())));}
            if(stream==null)throw new Exception("接口未返回响应内容");
            String type=c.getContentType();String raw;
            if(streaming&&type!=null&&type.toLowerCase().contains("text/event-stream"))raw=readEvents(stream,listener);
            else {raw=readLimited(stream,4*1024*1024);if(streaming&&listener!=null)listener.onText(raw);}
            JSONObject response;
            try{response=new JSONObject(raw);}catch(Exception e){if(streaming)return raw;throw new Exception("接口返回的不是 OpenAI Chat Completions JSON");}
            JSONArray choices=response.optJSONArray("choices");if(choices==null||choices.length()==0){if(streaming&&listener!=null&&raw.length()>0)return raw;throw new Exception("接口没有返回 choices；请使用 Chat Completions 兼容接口");}
            JSONObject message=choices.getJSONObject(0).getJSONObject("message");Object content=message.opt("content");
            if(content instanceof String&&!((String)content).trim().isEmpty())return(String)content;
            if(content instanceof JSONArray){StringBuilder result=new StringBuilder();JSONArray parts=(JSONArray)content;for(int i=0;i<parts.length();i++)result.append(parts.getJSONObject(i).optString("text",""));if(result.length()>0)return result.toString();}
            throw new Exception("模型返回了空内容，请检查模型和兼容参数");
        }finally{current=null;c.disconnect();}
    }
    private String readEvents(InputStream input,StreamListener listener)throws Exception{
        StringBuilder result=new StringBuilder();BufferedReader reader=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8));String line;
        while((line=reader.readLine())!=null){if(!line.startsWith("data:"))continue;String data=line.substring(5).trim();if(data.isEmpty())continue;if(data.equals("[DONE]"))break;
            JSONObject event;try{event=new JSONObject(data);}catch(Exception e){continue;}
            JSONObject error=event.optJSONObject("error");if(error!=null)throw new Exception(error.optString("message","模型流式响应失败"));
            JSONArray choices=event.optJSONArray("choices");if(choices==null||choices.length()==0)continue;JSONObject choice=choices.optJSONObject(0);if(choice==null)continue;
            JSONObject delta=choice.optJSONObject("delta");if(delta==null)delta=choice.optJSONObject("message");if(delta==null)continue;Object content=delta.opt("content");String piece="";
            if(content instanceof String)piece=(String)content;else if(content instanceof JSONArray){JSONArray parts=(JSONArray)content;for(int i=0;i<parts.length();i++)piece+=parts.optJSONObject(i)==null?"":parts.optJSONObject(i).optString("text","");}
            if(!piece.isEmpty()){result.append(piece);listener.onText(result.toString());}
        }
        if(result.length()==0)throw new Exception("模型的流式响应没有文本内容");return result.toString();
    }
    private static String readLimited(InputStream input,int max)throws Exception{if(input==null)return"";ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=input){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new Exception("模型响应过大，请缩短输出");out.write(b,0,n);}}return out.toString(StandardCharsets.UTF_8);}
    static JSONObject action(String content)throws Exception{
        String clean=content.trim().replaceAll("^```(?:json)?\\s*","").replaceAll("\\s*```$","");
        if(!clean.startsWith("{"))return new JSONObject().put("message",content).put("commands",new JSONArray());
        JSONObject obj;try{obj=new JSONObject(clean);}catch(Exception e){throw new Exception("模型操作 JSON 不完整，尚未执行任何命令");}
        JSONArray commands=obj.optJSONArray("commands");if(obj.has("commands")&&commands==null)throw new Exception("commands 必须是字符串数组，尚未执行");if(commands==null)commands=new JSONArray();
        if(commands.length()>8)throw new Exception("单步命令过多，请拆分任务");for(int i=0;i<commands.length();i++){Object cmd=commands.get(i);if(!(cmd instanceof String)||((String)cmd).trim().isEmpty()||((String)cmd).length()>12000)throw new Exception("模型命令格式无效，尚未执行");}
        return new JSONObject().put("message",obj.optString("message","")).put("commands",commands);
    }
}
