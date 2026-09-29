package com.ruirui.os;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.*;
final class RuiBridge {
    private static final String HOST="http://10.0.2.2:18765";
    private RuiBridge(){}
    private static JSONObject request(String path,JSONObject body)throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL(HOST+path).openConnection();
        try{c.setConnectTimeout(4000);c.setReadTimeout(40000);if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
        int status=c.getResponseCode();InputStream stream=status>=200&&status<300?c.getInputStream():c.getErrorStream();ByteArrayOutputStream bytes=new ByteArrayOutputStream();if(stream!=null)try(InputStream in=stream){in.transferTo(bytes);}JSONObject result=new JSONObject(bytes.toString(StandardCharsets.UTF_8));if(status<200||status>=300)throw new Exception(result.optString("error","系统通道 HTTP "+status));return result;}finally{c.disconnect();}
    }
    static boolean ready(){try{return get("/health").optBoolean("root",false);}catch(Exception e){return false;}}
    static boolean authorize(boolean enabled){try{return request(enabled?"/authorize":"/deauthorize",new JSONObject()).optBoolean("ok",false);}catch(Exception e){return false;}}
    static JSONObject execute(String command,boolean once)throws Exception{return request(once?"/run-once":"/run",new JSONObject().put("command",command).put("approved",once));}
    static String run(String command)throws Exception{JSONObject r=execute(command,false);return r.optString("stdout",r.optString("output",""))+r.optString("stderr","")+"\n(exit "+r.optInt("exitCode",-1)+")";}
    static JSONObject get(String path)throws Exception{return request(path,null);}
    static String state(String key)throws Exception{return get("/state?key="+java.net.URLEncoder.encode(key,StandardCharsets.UTF_8)).optString("value");}
    static boolean setting(String key,String value){try{return request("/setting",new JSONObject().put("key",key).put("value",value)).optBoolean("ok",false);}catch(Exception e){return false;}}
}
