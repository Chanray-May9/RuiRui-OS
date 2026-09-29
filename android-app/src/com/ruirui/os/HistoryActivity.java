package com.ruirui.os;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import org.json.*;

public class HistoryActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.root(this);Ui.header(root,this,this,"聊天历史","本机保存 · 仅此设备可见");ScrollView scroll=new ScrollView(this);LinearLayout list=Ui.column(this);scroll.addView(list);JSONArray sessions=ChatHistoryStore.all(this);if(sessions.length()==0){TextView empty=Ui.text(this,"还没有聊天记录\n和锐锐聊过的内容会显示在这里。",15,Ui.hintColor(this),false);empty.setGravity(Gravity.CENTER);list.addView(empty,new LinearLayout.LayoutParams(-1,Ui.dp(this,240)));}else{java.util.ArrayList<JSONObject> sorted=new java.util.ArrayList<>();for(int i=0;i<sessions.length();i++){JSONObject o=sessions.optJSONObject(i);if(o!=null&&o.optJSONArray("messages")!=null&&o.optJSONArray("messages").length()>0)sorted.add(o);}sorted.sort((x,y)->Long.compare(y.optLong("updated"),x.optLong("updated")));for(JSONObject o:sorted)addSession(list,o);}root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));Ui.page(this,this,root);}
    private void addSession(LinearLayout list,JSONObject s){LinearLayout card=Ui.glass(this,16);String id=s.optString("id");JSONArray m=s.optJSONArray("messages");JSONObject last=m==null?null:m.optJSONObject(Math.max(0,m.length()-1));String title=s.optString("title","聊天记录");TextView heading=Ui.text(this,title,16,Ui.foreground(this),true);TextView detail=Ui.text(this,(last==null?"":last.optString("text",""))+"\n"+android.text.format.DateFormat.format("yyyy-MM-dd  HH:mm",s.optLong("updated")),12,Ui.hintColor(this),false);detail.setMaxLines(3);card.addView(heading);Ui.gap(card,this,7);card.addView(detail);card.setOnClickListener(v->{android.content.Intent i=new android.content.Intent(this,ChatActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP|android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("session_id",id);startActivity(i);finish();});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=Ui.dp(this,12);list.addView(card,p);}
}
