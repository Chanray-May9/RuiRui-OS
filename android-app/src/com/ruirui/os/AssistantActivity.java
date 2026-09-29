package com.ruirui.os;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class AssistantActivity extends Activity {
    private android.content.SharedPreferences prefs;
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("ai-provider",0);draw();}
    private EditText field(LinearLayout box,String label,String key,String hint,int type){
        box.addView(Ui.text(this,label,13,0xFF576987,true));Ui.gap(box,this,7);
        EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(14);e.setHint(Ui.tr(this,hint));e.setInputType(type);e.setText(prefs.getString(key,""));
        e.setPadding(Ui.dp(this,14),Ui.dp(this,11),Ui.dp(this,14),Ui.dp(this,11));e.setTextColor(Ui.foreground(this));e.setHintTextColor(Ui.hintColor(this));e.setBackground(Ui.shape(0xFFF7F9FE,0xFFDDE5F2,14));box.addView(e,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));Ui.gap(box,this,15);return e;
    }
    private void draw(){
        LinearLayout root=Ui.root(this);Ui.header(root,this,this,"锐锐 AI","模型与授权  ·  Provider & permissions");
        LinearLayout info=Ui.glass(this,17);info.addView(Ui.text(this,"把喜欢的模型接进锐锐 OS",17,0xFF243653,true));Ui.gap(info,this,7);
        info.addView(Ui.text(this,"支持 OpenAI 兼容接口。填写基准 URL、密钥与模型名称；需要时补充兼容参数。",13,0xFF75829A,false));Ui.addCard(root,info,this,14);
        LinearLayout form=Ui.glass(this,17);
        EditText url=field(form,"基准 URL  ·  Base URL","url","https://api.example.com/v1",33);
        EditText key=field(form,"API 密钥  ·  API Key","key","sk-…",129);
        EditText model=field(form,"模型名称  ·  Model","model","gpt-4o-mini",33);
        EditText headers=field(form,"自定义请求头 JSON · Optional","headers","{}",1);
        EditText timeout=field(form,"超时秒数 · 10–300","timeoutText","90",2);timeout.setText(String.valueOf(prefs.getInt("timeout",90)));
        EditText extra=field(form,"兼容配置 JSON  ·  Optional","extra","{\"temperature\":0.7}",1);
        TextView save=Ui.text(this,"保存配置  ·  Save",15,0xFFFFFFFF,true);save.setGravity(Gravity.CENTER);save.setBackground(Ui.shape(0xFF617FC1,0,16));
        Spinner effort=new Spinner(this);String[] effortLabels={"off（不发送思维参数）","low","medium","high"};String[] effortIds={"none","low","medium","high"};effort.setAdapter(new android.widget.ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,effortLabels));effort.setSelection(Math.max(0,java.util.Arrays.asList(effortIds).indexOf(prefs.getString("effort","none"))));form.addView(Ui.text(this,"思维强度 · Reasoning effort",13,0xFF576987,true));form.addView(effort,new LinearLayout.LayoutParams(-1,Ui.dp(this,46)));Ui.gap(form,this,12);form.addView(save,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));
        save.setOnClickListener(v->{try{AiClient.Config config=new AiClient.Config(url.getText().toString(),key.getText().toString(),model.getText().toString(),extra.getText().toString(),headers.getText().toString(),Integer.parseInt(timeout.getText().toString()),effortIds[effort.getSelectedItemPosition()]);config.validate();prefs.edit().putString("url",config.url).putString("key",config.key).putString("model",config.model).putString("extra",config.extra).putString("headers",config.headers).putInt("timeout",config.timeout).putString("effort",config.effort).apply();android.widget.Toast.makeText(this,"配置已保存",0).show();}catch(Exception ex){new AlertDialog.Builder(this).setTitle("请检查配置").setMessage(ex.getMessage()).setPositiveButton("知道了",null).show();}});
        Ui.gap(form,this,10);TextView check=Ui.text(this,"检查模型连接",14,0xFF536C9E,true);form.addView(check);check.setOnClickListener(v->{check.setEnabled(false);check.setText("正在连接…");new Thread(()->{String result;try{AiClient.Config config=new AiClient.Config(url.getText().toString(),key.getText().toString(),model.getText().toString(),extra.getText().toString(),headers.getText().toString(),Integer.parseInt(timeout.getText().toString()),effortIds[effort.getSelectedItemPosition()]);String reply=new AiClient(config).request(new org.json.JSONArray().put(new org.json.JSONObject().put("role","user").put("content","这是连接检查，只回复连接成功，不执行任何操作。")));result="模型已响应："+reply.substring(0,Math.min(200,reply.length()));}catch(Exception ex){result="连接失败："+ex.getMessage();}String shown=result;runOnUiThread(()->{check.setEnabled(true);check.setText("检查模型连接");new AlertDialog.Builder(this).setTitle("连接结果").setMessage(shown).setPositiveButton("知道了",null).show();});}).start();});
        Ui.addCard(root,form,this,18);
        LinearLayout auth=Ui.glass(this,17);auth.addView(Ui.text(this,"AI 系统权限",17,0xFF243653,true));Ui.gap(auth,this,7);
        auth.addView(Ui.text(this,"普通模式会确认每批系统操作。完全控制模式只需在这里确认一次。启用后，锐锐可通过 Windows 控制桥使用模拟器 root 执行系统命令，不再逐项询问。",13,0xFF75829A,false));Ui.gap(auth,this,13);
        Switch full=new Switch(this);full.setText(Ui.tr(this,"完全控制  ·  Full control"));full.setTextSize(14);full.setTextColor(Ui.foreground(this));full.setChecked(prefs.getBoolean("full",false));auth.addView(full);
        full.setOnCheckedChangeListener((button,on)->{if(!on){prefs.edit().putBoolean("full",false).apply();new Thread(()->RuiBridge.authorize(false)).start();return;}if(prefs.getBoolean("full-authorized",false)){prefs.edit().putBoolean("full",true).apply();new Thread(()->RuiBridge.authorize(true)).start();return;}new AlertDialog.Builder(this).setTitle("把系统交给锐锐？").setMessage("确认后，本机 AI 服务可通过 root 执行系统命令。以后不会重复询问。请确认你了解这个授权。\n\nEnable once; no per-action prompts afterward.").setNegativeButton("暂不",(d,w)->full.setChecked(false)).setPositiveButton("授权锐锐",(d,w)->{prefs.edit().putBoolean("full",true).putBoolean("full-authorized",true).apply();new Thread(()->RuiBridge.authorize(true)).start();}).setOnCancelListener(d->full.setChecked(false)).show();});
        Ui.gap(auth,this,11);TextView status=Ui.text(this,"系统通道：正在检查… · checking",12,0xFF9A7F51,false);auth.addView(status);
        if(prefs.getBoolean("full",false))new Thread(()->RuiBridge.authorize(true)).start();
        new Thread(()->{boolean ok=RuiBridge.ready();runOnUiThread(()->{status.setText("系统通道："+(ok?"root 可用 · root available":"未连接 · not connected"));status.setTextColor(ok?0xFF4A8B71:0xFF9A7F51);});}).start();
        Ui.addCard(root,auth,this,16);

        LinearLayout gesture=Ui.glass(this,17);gesture.addView(Ui.text(this,"长按电源键打开锐锐",17,0xFF243653,true));Ui.gap(gesture,this,7);gesture.addView(Ui.text(this,"把锐锐设为系统语音助手后，在支持该手势的手机上即可从锁屏或其他应用唤出悬浮窗口。部分品牌需要再到系统的“电源键 / 手势”设置里选择助手。",13,0xFF75829A,false));Ui.gap(gesture,this,12);TextView setAssistant=Ui.text(this,"设为系统语音助手",14,0xFFFFFFFF,true);setAssistant.setGravity(Gravity.CENTER);setAssistant.setBackground(Ui.shape(0xFF617FC1,0,16));gesture.addView(setAssistant,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));setAssistant.setOnClickListener(v->{if(android.os.Build.VERSION.SDK_INT>=29){android.app.role.RoleManager rm=(android.app.role.RoleManager)getSystemService(ROLE_SERVICE);if(rm!=null&&rm.isRoleAvailable(android.app.role.RoleManager.ROLE_ASSISTANT))startActivityForResult(rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_ASSISTANT),83);else new AlertDialog.Builder(this).setMessage("此系统没有提供默认助手选择入口。请在系统设置中选择锐锐作为数字助理应用。") .setPositiveButton("知道了",null).show();}else startActivity(new android.content.Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS));});TextView power=Ui.text(this,"查看系统手势设置",13,0xFF536C9E,true);gesture.addView(power);power.setOnClickListener(v->{try{startActivity(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception ignored){}});Ui.addCard(root,gesture,this,16);

        Ui.page(this,this,root);
    }
}



