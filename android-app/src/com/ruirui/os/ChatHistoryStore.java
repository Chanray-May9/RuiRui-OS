package com.ruirui.os;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class ChatHistoryStore {
    private static final String FILE="ruirui-chat-history.json";
    static synchronized JSONArray all(Context c){try{File f=new File(c.getFilesDir(),FILE);if(!f.exists())return new JSONArray();return new JSONArray(new String(Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8));}catch(Exception e){return new JSONArray();}}
    static synchronized JSONObject get(Context c,String id){JSONArray a=all(c);for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&id.equals(o.optString("id")))return o;}return null;}
    static synchronized String create(Context c){String id=java.util.UUID.randomUUID().toString();JSONArray a=all(c);try{a.put(new JSONObject().put("id",id).put("updated",System.currentTimeMillis()).put("messages",new JSONArray()));save(c,a);}catch(Exception ignored){}return id;}
    static synchronized void append(Context c,String id,String role,String text){if(text==null||text.trim().isEmpty())return;JSONArray a=all(c);try{JSONObject s=find(a,id);if(s==null){s=new JSONObject().put("id",id).put("updated",System.currentTimeMillis()).put("messages",new JSONArray());a.put(s);}JSONArray m=s.optJSONArray("messages");if(m==null){m=new JSONArray();s.put("messages",m);}m.put(new JSONObject().put("role",role).put("text",text).put("time",System.currentTimeMillis()));s.put("updated",System.currentTimeMillis());if(s.optString("title").isEmpty()&&"user".equals(role))s.put("title",text.length()>44?text.substring(0,44)+"…":text);save(c,a);}catch(Exception ignored){}}
    static synchronized JSONArray conversation(Context c,String id){try{JSONObject s=get(c,id);JSONArray v=s==null?null:s.optJSONArray("conversation");return v==null?new JSONArray():new JSONArray(v.toString());}catch(Exception e){return new JSONArray();}}
    static synchronized void saveConversation(Context c,String id,JSONArray conversation){JSONArray a=all(c);try{JSONObject s=find(a,id);if(s==null){s=new JSONObject().put("id",id).put("updated",System.currentTimeMillis()).put("messages",new JSONArray());a.put(s);}s.put("conversation",new JSONArray(conversation.toString()));s.put("updated",System.currentTimeMillis());save(c,a);}catch(Exception ignored){}}
    private static JSONObject find(JSONArray a,String id){for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&id.equals(o.optString("id")))return o;}return null;}
    private static void save(Context c,JSONArray a){try{File f=new File(c.getFilesDir(),FILE),tmp=new File(c.getFilesDir(),FILE+".tmp");Files.write(tmp.toPath(),a.toString().getBytes(StandardCharsets.UTF_8));if(!tmp.renameTo(f)){Files.copy(tmp.toPath(),f.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);tmp.delete();}}catch(Exception ignored){}}
    private ChatHistoryStore(){}
}
