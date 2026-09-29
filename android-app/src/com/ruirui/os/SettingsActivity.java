package com.ruirui.os;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);draw();}
    private void draw(){
        LinearLayout root=Ui.root(this); Ui.header(root,this,this,"设置","Settings  ·  让锐锐 OS 按你的习惯工作");
        section(root,"连接与设备","CONNECTIONS & DEVICE");
        systemItem(root,"网络和互联网","Wi-Fi、移动网络与热点","network","◔");
        systemItem(root,"已连接的设备","蓝牙与配对设备","device","⌁");
        systemItem(root,"显示和触控","亮度、字体、屏幕超时","display","☼");
        systemItem(root,"声音和振动","音量、铃声与触感","sound","♫");
        systemItem(root,"通知","应用通知和锁屏提醒","notifications","◉");
        systemItem(root,"应用","默认应用、权限与应用信息","apps","▦");
        systemItem(root,"存储空间","查看设备空间使用情况","storage","▤");
        section(root,"锐锐 AI","RUIRUI INTELLIGENCE");
        item(root,"模型与权限","Model, API & permissions","配置兼容接口、模型名称和 AI 系统能力",AssistantActivity.class,"✧");
        item(root,"终端","Terminal","在设备上运行命令并查看结果",TerminalActivity.class,"⌘");
        section(root,"个性化","PERSONALIZATION");
        item(root,Ui.english(this)?(Ui.dark(this)?"Appearance · Dark":"Appearance · Light"):(Ui.dark(this)?"外观 · 深色":"外观 · 浅色"),"Appearance","流光玻璃 · 点击切换主题",null,"◐");
        item(root,Ui.english(this)?"Language":"语言","Language","简体中文 · English",null,"文");
        section(root,"隐私与系统","PRIVACY & SYSTEM");
        systemItem(root,"安全和隐私","屏幕锁定、隐私控制与安全更新","security","◇");
        systemItem(root,"电池","电量与耗电情况","battery","▰");
        systemItem(root,"系统设置","语言、手势、重置和更新","system","⚙");
        systemItem(root,"关于锐锐 OS","Android 14 · Google APIs · Emulator","about","ⓘ");
        Ui.gap(root,this,14); root.addView(Ui.text(this,"马上就好。",13,0xFF7E8AA3,false));
        Ui.page(this,this,root);
    }
    private void section(LinearLayout r,String zh,String en){Ui.gap(r,this,8);r.addView(Ui.text(this,zh,13,0xFF71809A,true));r.addView(Ui.text(this,en,9,0xFFA0AAB9,true));Ui.gap(r,this,10);}
    private void item(LinearLayout parent,String a,String b,String c,Class<?> target,String symbol){
        LinearLayout card=Ui.glass(this,15);LinearLayout row=Ui.row(this);
        TextView icon=Ui.text(this,symbol,19,0xFF607FBE,true);icon.setGravity(17);icon.setBackground(Ui.shape(0xFFE9F0FF,0,15));
        row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));LinearLayout words=Ui.column(this);words.setPadding(Ui.dp(this,12),0,0,0);
        words.addView(Ui.text(this,a,16,0xFF253550,true));words.addView(Ui.text(this,b,10,0xFF8894A8,false));
        row.addView(words,new LinearLayout.LayoutParams(0,-2,1));row.addView(Ui.text(this,"›",24,0xFF9CA8BB,false));card.addView(row);Ui.gap(card,this,9);
        card.addView(Ui.text(this,c,12,0xFF77859D,false));card.setOnClickListener(v->{if(target!=null)startActivity(new Intent(this,target));else if(a.startsWith("外观")||a.startsWith("Appearance")){boolean next=!Ui.dark(this);getSharedPreferences("ui",0).edit().putBoolean("dark",next).apply();recreate();}else if(a.equals("语言")||a.equals("Language")){boolean next=!Ui.english(this);getSharedPreferences("ui",0).edit().putBoolean("english",next).apply();recreate();}});Ui.addCard(parent,card,this,10);
    }
    private void systemItem(LinearLayout parent,String title,String sub,String category,String symbol){
        LinearLayout card=Ui.glass(this,14);LinearLayout row=Ui.row(this);TextView icon=Ui.text(this,symbol,19,0xFF607FBE,true);icon.setGravity(17);icon.setBackground(Ui.shape(0xFFE9F0FF,0,15));row.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
        LinearLayout words=Ui.column(this);words.setPadding(Ui.dp(this,12),0,0,0);words.addView(Ui.text(this,title,15,0xFF253550,true));words.addView(Ui.text(this,sub,11,0xFF8894A8,false));row.addView(words,new LinearLayout.LayoutParams(0,-2,1));row.addView(Ui.text(this,"›",23,0xFF9CA8BB,false));card.addView(row);
        card.setOnClickListener(v->startActivity(new Intent(this,SystemSettingsActivity.class).putExtra("category",category)));Ui.addCard(parent,card,this,8);
    }
}
