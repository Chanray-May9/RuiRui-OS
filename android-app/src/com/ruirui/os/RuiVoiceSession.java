package com.ruirui.os;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.service.voice.VoiceInteractionSession;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

final class RuiVoiceSession extends VoiceInteractionSession {
    RuiVoiceSession(android.content.Context c){super(c);}
    @Override public View onCreateContentView(){
        android.content.Context c=getContext();FrameLayout full=new FrameLayout(c);full.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout card=Ui.glass(c,18);card.setBackground(Ui.shape(0xF5FFFFFF,0xC8FFFFFF,28));card.setElevation(Ui.dp(c,18));
        LinearLayout row=Ui.row(c);TextView orb=Ui.text(c,"✦",19,0xFF668CCC,true);orb.setGravity(Gravity.CENTER);orb.setBackground(Ui.shape(0xFFE8F2FF,0,20));row.addView(orb,new LinearLayout.LayoutParams(Ui.dp(c,42),Ui.dp(c,42)));LinearLayout labels=Ui.column(c);labels.setPadding(Ui.dp(c,11),0,0,0);labels.addView(Ui.text(c,"你好，我是锐锐",17,0xFF253651,true));labels.addView(Ui.text(c,"说说你想做什么",12,0xFF7888A0,false));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));TextView close=Ui.text(c,"×",23,0xFF71809A,false);close.setGravity(Gravity.CENTER);row.addView(close,new LinearLayout.LayoutParams(Ui.dp(c,36),Ui.dp(c,42)));close.setOnClickListener(v->finish());card.addView(row);Ui.gap(card,c,13);
        EditText prompt=new EditText(c);prompt.setSingleLine(true);prompt.setTextSize(14);prompt.setHint("发消息给锐锐…");prompt.setTextColor(0xFF243653);prompt.setHintTextColor(0xFF8793A7);prompt.setPadding(Ui.dp(c,14),0,Ui.dp(c,14),0);prompt.setBackground(Ui.shape(0xFFF5F8FE,0xFFDDE5F2,17));card.addView(prompt,new LinearLayout.LayoutParams(-1,Ui.dp(c,48)));Ui.gap(card,c,10);
        TextView send=Ui.text(c,"打开锐锐聊天",14,Color.WHITE,true);send.setGravity(Gravity.CENTER);send.setBackground(Ui.shape(0xFF617FC1,0,16));card.addView(send,new LinearLayout.LayoutParams(-1,Ui.dp(c,46)));send.setOnClickListener(v->openChat(prompt.getText().toString()));prompt.setOnEditorActionListener((v,a,e)->{openChat(prompt.getText().toString());return true;});
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);cp.leftMargin=Ui.dp(c,17);cp.rightMargin=Ui.dp(c,17);cp.bottomMargin=Ui.dp(c,34);full.addView(card,cp);return full;
    }
    private void openChat(String text){Intent i=new Intent(getContext(),ChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);if(text!=null&&!text.trim().isEmpty())i.putExtra("initial_prompt",text.trim());startAssistantActivity(i);finish();}
}
