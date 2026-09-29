package com.ruirui.os;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TerminalActivity extends Activity {
    private TextView output;private EditText command;
    @Override public void onCreate(Bundle b){super.onCreate(b);draw();}
    private void draw(){
        LinearLayout root=Ui.root(this);Ui.header(root,this,this,"终端","Terminal  ·  命令在设备本机执行");
        LinearLayout card=Ui.glass(this,14);TextView notice=Ui.text(this,"锐锐终端  /  Android shell  ·  Windows ADB bridge",12,0xFF68805F,true);card.addView(notice);Ui.gap(card,this,12);
        ScrollView log=new ScrollView(this);log.setBackground(Ui.shape(0xFFF3F6FC,0xFFE1E7F1,16));output=Ui.text(this,"$ _",12,0xFF344966,false);output.setTypeface(android.graphics.Typeface.MONOSPACE);output.setPadding(Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12),Ui.dp(this,12));log.addView(output);card.addView(log,new LinearLayout.LayoutParams(-1,Ui.dp(this,390)));Ui.gap(card,this,12);
        LinearLayout row=Ui.row(this);command=new EditText(this);command.setSingleLine(true);command.setTextSize(13);command.setHint(Ui.tr(this,"输入命令…"));command.setTypeface(android.graphics.Typeface.MONOSPACE);command.setTextColor(Ui.foreground(this));command.setHintTextColor(Ui.hintColor(this));command.setBackground(Ui.shape(0xFFF7F9FE,0xFFDDE5F2,14));command.setPadding(Ui.dp(this,12),0,Ui.dp(this,12),0);row.addView(command,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));
        TextView run=Ui.text(this,"运行",13,0xFFFFFFFF,true);run.setGravity(Gravity.CENTER);run.setBackground(Ui.shape(0xFF617FC1,0,14));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(Ui.dp(this,66),Ui.dp(this,48));rp.leftMargin=Ui.dp(this,8);row.addView(run,rp);run.setOnClickListener(v->execute());command.setOnEditorActionListener((v,a,e)->{execute();return true;});card.addView(row);Ui.gap(card,this,10);card.addView(Ui.text(this,"提示：完全控制授权后，命令可通过 root 执行。",11,0xFF8793A7,false));Ui.addCard(root,card,this,0);Ui.page(this,this,root);
    }
    private void execute(){String cmd=command.getText().toString().trim();if(cmd.isEmpty())return;command.setText("");((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(command.getWindowToken(),0);new Thread(()->{String result;try{boolean full=getSharedPreferences("ai-provider",0).getBoolean("full",false);if(full){RuiBridge.authorize(true);try{result=RuiBridge.run(cmd);}catch(Exception ex){Process p=new ProcessBuilder("su","-c",cmd).redirectErrorStream(true).start();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();p.getInputStream().transferTo(out);p.waitFor();result=out.toString();}}else{Process p=new ProcessBuilder("sh","-c",cmd).redirectErrorStream(true).start();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();p.getInputStream().transferTo(out);p.waitFor();result=out.toString();}}catch(Exception e){result=e.toString();}final String r=result;runOnUiThread(()->output.append("\n$ "+cmd+"\n"+(r.isEmpty()?"(完成)":r)+"\n$ _"));}).start();}
}
