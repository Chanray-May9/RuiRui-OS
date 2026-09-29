package com.ruirui.os;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.BatteryManager;
import android.os.StatFs;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import java.io.File;
import java.util.List;

public class SystemSettingsActivity extends Activity {
    private String category;
    @Override public void onCreate(Bundle b){super.onCreate(b);category=getIntent().getStringExtra("category");draw();}
    private String title(){return switch(category==null?"system":category){case "network"->"网络和互联网";case "device"->"已连接的设备";case "display"->"显示和触控";case "sound"->"声音和振动";case "notifications"->"通知";case "apps"->"应用";case "storage"->"存储空间";case "security"->"安全和隐私";case "battery"->"电池";case "about"->"关于锐锐 OS";default->"系统设置";};}
    private void draw(){LinearLayout root=Ui.root(this);Ui.header(root,this,this,title(),"系统设置  ·  保留熟悉的操作方式");
        switch(category==null?"system":category){
            case "network"->{section(root,"网络连接","NETWORK");addSwitch(root,"Wi-Fi","连接无线网络","wifi",true);addSwitch(root,"飞行模式","暂时关闭无线连接","airplane",false);info(root,"网络面板","连接到其他无线网络时，打开 Wi-Fi 卡片并选择可用网络。",null);}
            case "device"->{section(root,"附近设备","NEARBY DEVICES");addSwitch(root,"蓝牙","连接耳机、手表和其他设备","bluetooth",false);info(root,"已配对设备","配对和连接设备的操作保持 Android 标准方式。",null);}
            case "display"->{section(root,"屏幕","DISPLAY");addSwitch(root,"深色外观","切换锐锐 OS 的浅色与深色主题","night",Ui.dark(this));slider(root,"屏幕亮度","brightness",0,255,128);section(root,"显示大小","DISPLAY SIZE");scaleButtons(root);}
            case "sound"->{section(root,"音量","VOLUME");AudioManager a=(AudioManager)getSystemService(AUDIO_SERVICE);slider(root,"媒体音量","volume",0,a.getStreamMaxVolume(AudioManager.STREAM_MUSIC),a.getStreamVolume(AudioManager.STREAM_MUSIC));slider(root,"铃声音量","ring_volume",0,a.getStreamMaxVolume(AudioManager.STREAM_RING),a.getStreamVolume(AudioManager.STREAM_RING));info(root,"振动反馈","音量调整立即应用到设备。",null);}
            case "notifications"->{section(root,"勿扰模式","DO NOT DISTURB");addSwitch(root,"勿扰模式","暂时静音通知和来电","dnd",false);info(root,"应用通知","应用级通知仍可在应用信息中逐个调整。",()->openAndroid("android.settings.NOTIFICATION_SETTINGS"));}
            case "apps"->drawApps(root);
            case "storage"->drawStorage(root);
            case "battery"->drawBattery(root);
            case "security"->{section(root,"设备安全","DEVICE SECURITY");info(root,"屏幕锁定","管理锁屏方式和设备保护。",()->openAndroid("android.settings.SECURITY_SETTINGS"));info(root,"应用权限","查看相机、麦克风、位置等权限。",()->openAndroid("android.settings.PRIVACY_SETTINGS"));info(root,"安全更新","Android 14 · Google APIs emulator image",null);}
            case "about"->{section(root,"设备信息","DEVICE INFORMATION");info(root,"锐锐 OS","版本 1.0 alpha · Android 14",null);info(root,"基础镜像","Google APIs x86_64 · API 34",null);info(root,"设备","Pixel 7 emulator profile",null);}
            default->{section(root,"系统偏好","SYSTEM PREFERENCES");info(root,"语言","简体中文与 English",()->toggleLanguage());info(root,"手势导航","使用屏幕底部手势返回桌面和切换应用。",null);info(root,"关于锐锐 OS","Android 14 · Google APIs · emulator",()->openAndroid("android.settings.DEVICE_INFO_SETTINGS"));}
        }
        Ui.page(this,this,root);
    }
    private void section(LinearLayout root,String zh,String en){Ui.gap(root,this,11);root.addView(Ui.text(this,zh+"  ·  "+en,12,0xFF70809B,true));Ui.gap(root,this,9);}
    private void info(LinearLayout root,String title,String subtitle,Runnable click){LinearLayout card=Ui.glass(this,15);card.addView(Ui.text(this,title,16,0xFF253550,true));Ui.gap(card,this,6);card.addView(Ui.text(this,subtitle,12,0xFF78869D,false));if(click!=null)card.setOnClickListener(v->click.run());Ui.addCard(root,card,this,10);}
    private void addSwitch(LinearLayout root,String title,String subtitle,String key,boolean initial){
        LinearLayout card=Ui.glass(this,15);LinearLayout row=Ui.row(this);LinearLayout words=Ui.column(this);words.addView(Ui.text(this,title,16,0xFF253550,true));words.addView(Ui.text(this,subtitle,11,0xFF8290A7,false));row.addView(words,new LinearLayout.LayoutParams(0,-2,1));Switch sw=new Switch(this);sw.setTextColor(Ui.foreground(this));row.addView(sw);card.addView(row);Ui.addCard(root,card,this,10);
        sw.setChecked(initial);final boolean[] loading={true};sw.setOnCheckedChangeListener((b,on)->{if(loading[0])return;new Thread(()->{boolean ok=RuiBridge.setting(key,on?"1":"0");runOnUiThread(()->{if(!ok){android.widget.Toast.makeText(this,"马上就好，正在连接系统服务。",0).show();sw.setChecked(!on);}else if(key.equals("night")){getSharedPreferences("ui",0).edit().putBoolean("dark",on).apply();recreate();}});}).start();});
        new Thread(()->{try{String v=RuiBridge.state(key);boolean actual=v.equals("1")||v.equalsIgnoreCase("true");runOnUiThread(()->{loading[0]=true;sw.setChecked(actual);loading[0]=false;});}catch(Exception ignored){runOnUiThread(()->loading[0]=false);}}).start();
    }
    private void slider(LinearLayout root,String title,String key,int min,int max,int initial){LinearLayout card=Ui.glass(this,15);card.addView(Ui.text(this,title,15,0xFF253550,true));SeekBar seek=new SeekBar(this);seek.setMax(Math.max(1,max-min));seek.setProgress(Math.max(0,Math.min(max-min,initial-min)));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,Ui.dp(this,48));p.topMargin=Ui.dp(this,6);card.addView(seek,p);TextView value=Ui.text(this,Integer.toString(initial),12,0xFF78869D,false);card.addView(value);final int[] current={initial};seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar b,int n,boolean user){current[0]=n+min;value.setText(Integer.toString(current[0]));}public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){int v=current[0];if(key.equals("volume")){((AudioManager)getSystemService(AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_MUSIC,v,0);}else if(key.equals("ring_volume")){((AudioManager)getSystemService(AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_RING,v,0);}else new Thread(()->RuiBridge.setting(key,Integer.toString(v))).start();}});Ui.addCard(root,card,this,10);}
    private void scaleButtons(LinearLayout root){LinearLayout row=Ui.row(this);for(String value:new String[]{"0.9","1.0","1.1","1.2"}){TextView b=Ui.pill(this,value+"×");b.setGravity(Gravity.CENTER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.rightMargin=Ui.dp(this,4);row.addView(b,p);b.setOnClickListener(v->new Thread(()->RuiBridge.setting("font",value)).start());}root.addView(row);Ui.gap(root,this,8);}
    private void drawApps(LinearLayout root){section(root,"已安装的应用","INSTALLED APPS");PackageManager pm=getPackageManager();Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> apps=pm.queryIntentActivities(q,PackageManager.MATCH_ALL);java.util.HashSet<String> seen=new java.util.HashSet<>();for(ResolveInfo app:apps){String pkg=app.activityInfo.packageName;if(pkg.equals(getPackageName())||!seen.add(pkg))continue;info(root,Ui.appLabel(this,app.loadLabel(pm).toString()),pkg,()->{Intent i=new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",Uri.parse("package:"+pkg));startActivity(i);});}}
    private void drawStorage(LinearLayout root){section(root,"设备空间","DEVICE STORAGE");try{StatFs s=new StatFs(new File("/data").getAbsolutePath());long total=s.getTotalBytes(),free=s.getAvailableBytes(),used=total-free;info(root,"已使用  "+size(used),"共  "+size(total)+"  ·  可用  "+size(free),null);}catch(Exception e){info(root,"存储空间","当前无法读取设备空间。",null);}info(root,"应用数据","在应用页面查看各应用的存储与缓存。",()->startActivity(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS")));}
    private void drawBattery(LinearLayout root){section(root,"电池状态","BATTERY STATUS");BatteryManager b=(BatteryManager)getSystemService(BATTERY_SERVICE);int capacity=b.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);info(root,capacity>=0?capacity+"%":"电量信息不可用","模拟器电池状态由设备设置管理。",null);}
    private String size(long n){return String.format(java.util.Locale.ROOT,"%.1f GB",n/(1024d*1024d*1024d));}
    private void toggleLanguage(){getSharedPreferences("ui",0).edit().putBoolean("english",!Ui.english(this)).apply();recreate();}
    private void openAndroid(String action){try{startActivity(new Intent(action));}catch(Exception ignored){}}
}
