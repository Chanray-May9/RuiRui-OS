package com.ruirui.os;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class Ui {
    private static boolean dark;
    private static final java.util.Map<String,String> EN=new java.util.HashMap<>();
    private static final java.util.Map<String,String> ZH=new java.util.HashMap<>();
    static {
        String[][] pairs={
            {"早上好，锐锐","Good morning, RuiRui"},{"一切交给锐锐","Leave it to RuiRui"},{"你的 AI 助手，随时准备好。\nYour AI companion is almost ready.","Your AI assistant is ready to help.\nAlways by your side."},{"模型尚未连接","Model not connected"},{"常用","Everyday"},{"设置","Settings"},{"终端","Terminal"},{"AI 助手","AI assistant"},{"相册","Photos"},{"马上就好，锐锐正在把熟悉的一切准备好。","Almost there. RuiRui is getting things ready."},
            {"让锐锐 OS 按你的习惯工作","Make RuiRui OS work your way"},{"锐锐 AI","RuiRui AI"},{"模型与权限","Model & permissions"},{"配置兼容接口、模型名称和 AI 系统能力","Configure a compatible API, model and system access"},{"在设备上运行命令并查看结果","Run commands on this device and view the results"},{"个性化","Personalization"},{"外观","Appearance"},{"流光玻璃 · 点击切换主题","Fluid glass · tap to switch theme"},{"语言","Language"},{"简体中文 · English","Simplified Chinese · English"},{"系统","System"},{"关于锐锐 OS","About RuiRui OS"},{"Android 14 · Google APIs · Emulator","Android 14 · Google APIs · Emulator"},{"PERSONALIZATION","PERSONALIZATION"},{"SYSTEM","SYSTEM"},{"RUIRUI INTELLIGENCE","RUIRUI INTELLIGENCE"},
            {"模型与授权","Model & permissions"},{"把喜欢的模型接进锐锐 OS","Connect your favorite model to RuiRui OS"},{"支持 OpenAI 兼容接口。填写基准 URL、密钥与模型名称；需要时补充兼容参数。","Supports OpenAI-compatible APIs. Enter the base URL, key and model name; add provider options if needed."},{"基准 URL  ·  Base URL","Base URL"},{"API 密钥  ·  API Key","API key"},{"模型名称  ·  Model","Model name"},{"兼容配置 JSON  ·  Optional","Provider options (JSON)"},{"保存配置  ·  Save","Save configuration"},{"已保存，马上就好。","Saved. Almost ready."},{"AI 系统权限","AI system permissions"},{"完全控制模式只需在这里确认一次。启用后，锐锐可通过 Windows 控制桥使用模拟器 root 执行系统命令，不再逐项询问。","Confirm full-control mode here once. RuiRui can then run system commands through the Windows bridge with emulator root, without asking each time."},{"完全控制  ·  Full control","Full control"},{"系统通道：正在检查… · checking","System bridge: checking…"},{"系统通道：root 可用 · root available","System bridge: root available"},{"系统通道：未连接 · not connected","System bridge: not connected"},{"开始和锐锐说话  ·  Chat","Chat with RuiRui"},
            {"锐锐终端  /  Android shell  ·  Windows ADB bridge","RuiRui Terminal  /  Android shell  ·  Windows ADB bridge"},{"提示：完全控制授权后，命令可通过 root 执行。","With full control enabled, commands run through the root bridge."},{"输入命令…","Enter a command…"},{"运行","Run"},{"发消息，或描述你想完成的操作…","Message RuiRui or describe a task…"},{"发送","Send"},{"和锐锐聊聊","Chat with RuiRui"},{"AI 助手  ·  Chat with RuiRui","AI assistant  ·  Chat with RuiRui"},{"没有找到应用","No apps found"},{"所有应用","All apps"},{"搜索应用","Search apps"},{"向上滑动打开应用抽屉","Swipe up for all apps"},{"连接与设备","Connections & device"},{"网络和互联网","Network & internet"},{"Wi-Fi、移动网络与热点","Wi-Fi, mobile network and hotspot"},{"已连接的设备","Connected devices"},{"蓝牙与配对设备","Bluetooth and paired devices"},{"显示和触控","Display & touch"},{"亮度、字体、屏幕超时","Brightness, font and screen timeout"},{"声音和振动","Sound & vibration"},{"音量、铃声与触感","Volume, ringtones and haptics"},{"通知","Notifications"},{"应用通知和锁屏提醒","App notifications and lock-screen alerts"},{"应用","Apps"},{"默认应用、权限与应用信息","Default apps, permissions and app info"},{"存储空间","Storage"},{"查看设备空间使用情况","See how device storage is used"},{"隐私与系统","Privacy & system"},{"安全和隐私","Security & privacy"},{"屏幕锁定、隐私控制与安全更新","Screen lock, privacy controls and security updates"},{"电池","Battery"},{"电量与耗电情况","Battery level and usage"},{"系统设置","System settings"},{"语言、手势、重置和更新","Language, gestures, reset and updates"},{"马上就好。","Almost ready."}
        };
        for(String[] p:pairs) EN.put(p[0],p[1]);
        String[][] appNames={{"Chrome","Chrome"},{"Camera","相机"},{"Phone","电话"},{"Messages","信息"},{"Photos","照片"},{"Contacts","联系人"},{"Calendar","日历"},{"Maps","地图"},{"Clock","时钟"},{"Drive","云端硬盘"},{"Files","文件"},{"Gmail","邮件"},{"Google","谷歌"},{"Settings","系统设置"},{"YouTube","YouTube"},{"YT Music","YouTube 音乐"},{"TMobile","SIM 卡工具"}};
        for(String[] p:appNames){ZH.put(p[0],p[1]);if(!p[0].equals(p[1]))EN.put(p[1],p[0]);}
    }
    private Ui() {}
    static boolean dark(Context c) { return c.getSharedPreferences("ui",0).getBoolean("dark",false); }
    static boolean english(Context c) { return c.getSharedPreferences("ui",0).getBoolean("english",false); }
    static String tr(Context c,String s) { return english(c)?EN.getOrDefault(s,s):s; }
    static String appLabel(Context c,String s) { return english(c)?EN.getOrDefault(s,s):ZH.getOrDefault(s,s); }
    static int dp(Context c, float n) { return (int)(n * c.getResources().getDisplayMetrics().density + 0.5f); }
    static GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(dark?surfaceColor(color):color); d.setCornerRadius(radius);
        if (stroke != 0) d.setStroke(1, dark?strokeColor(stroke):stroke); return d;
    }
    static LinearLayout column(Context c) { LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); return l; }
    static LinearLayout row(Context c) { LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    static TextView text(Context c, String s, float size, int color, boolean bold) {
        TextView v = new TextView(c); v.setText(tr(c,s)); v.setTextSize(size); v.setTextColor(textColor(color));
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); v.setIncludeFontPadding(false); return v;
    }
    static LinearLayout root(Context c) {
        dark=dark(c);
        LinearLayout r = column(c); r.setPadding(dp(c, 22), dp(c, 20), dp(c, 22), dp(c, 24));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                dark?new int[]{0xFF111827,0xFF202A3D,0xFF211F35}:new int[]{0xFFF9FAFF, 0xFFEAF1FF, 0xFFF3EEFF}); r.setBackground(bg); return r;
    }
    static LinearLayout glass(Context c, int pad) {
        LinearLayout l = column(c); l.setPadding(dp(c,pad),dp(c,pad),dp(c,pad),dp(c,pad));
        l.setBackground(shape(dark?0xE5283348:0xEFFFFFFF, dark?0x48FFFFFF:0xB8FFFFFF, 25)); l.setElevation(dp(c,3)); return l;
    }
    static void gap(LinearLayout l, Context c, int height) {
        View v = new View(c); l.addView(v, new LinearLayout.LayoutParams(1, dp(c,height)));
    }
    static void addCard(LinearLayout parent, View card, Context c, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin = dp(c,bottom); parent.addView(card,p);
    }
    static ScrollView scroll(Context c, LinearLayout content) {
        ScrollView s = new ScrollView(c); s.setFillViewport(true); s.setClipToPadding(false);
        s.addView(content); return s;
    }
    static void page(Context c, android.app.Activity a, LinearLayout content) {
        boolean night=dark(c);a.getWindow().setStatusBarColor(Color.TRANSPARENT); a.getWindow().setNavigationBarColor(night?0xFF191F2D:0xFFF2F4FF);
        a.getWindow().getDecorView().setSystemUiVisibility(night?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        a.setContentView(scroll(c,content));
    }
    static TextView pill(Context c, String s) {
        TextView v=text(c,s,12,0xFF536A99,true); v.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));
        v.setBackground(shape(0xFFE9F0FF,0,24)); return v;
    }
    static void header(LinearLayout root, Context c, android.app.Activity a, String title, String subtitle) {
        TextView back=text(c,"‹",36,0xFF33476E,false); back.setOnClickListener(v->a.finish());
        root.addView(back,new LinearLayout.LayoutParams(-1,dp(c,44)));
        root.addView(text(c,title,30,0xFF192844,true)); gap(root,c,8);
        root.addView(text(c,subtitle,14,0xFF687895,false)); gap(root,c,22);
    }
    static int foreground(Context c){return dark(c)?0xFFE8EEF9:0xFF253550;}
    static int hintColor(Context c){return dark(c)?0xFF9EACC5:0xFF8793A7;}
    private static int textColor(int color){if(!dark||color==Color.WHITE)return color;double l=luma(color);if(l<0.22)return 0xFFE8EEF9;if(l<0.42)return 0xFFC1CCE0;if(l>0.90)return 0xFF2B3850;return color;}
    private static int surfaceColor(int color){double l=luma(color);if(l>0.88){int a=(color>>>24)&255;return (a<<24)|0x00263145;}if(l>0.68){int a=(color>>>24)&255;return (a<<24)|0x002E3A50;}return color;}
    private static int strokeColor(int color){return luma(color)>0.85?0x42FFFFFF:color;}
    private static double luma(int c){double r=((c>>16)&255)/255.0,g=((c>>8)&255)/255.0,b=(c&255)/255.0;return 0.2126*r+0.7152*g+0.0722*b;}
}
