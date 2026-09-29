package com.ruirui.os;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextClock;
import android.widget.ViewFlipper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeActivity extends Activity {
    private ViewFlipper pages;
    private TextView dots;
    private GestureDetector gestures;
    private static final String[] PINNED={"com.google.android.dialer","com.google.android.apps.messaging","com.android.chrome","com.android.camera2","com.google.android.apps.photos","com.google.android.contacts","com.google.android.calendar","com.google.android.apps.maps"};
    @Override public void onCreate(Bundle state){super.onCreate(state);RuiWallpaper.applyDefault(this);draw();}
    private void draw(){
        boolean night=Ui.dark(this);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);getWindow().setNavigationBarColor(night?0xFF171D2B:0xFFF4F5FA);
        getWindow().getDecorView().setSystemUiVisibility(night?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=Ui.root(this);root.setPadding(Ui.dp(this,22),Ui.dp(this,10),Ui.dp(this,22),Ui.dp(this,12));
        try{Drawable wallpaper=WallpaperManager.getInstance(this).getDrawable();if(wallpaper!=null)root.setBackground(wallpaper);}catch(Exception ignored){}
        LinearLayout top=Ui.glass(this,20);top.setBackground(Ui.shape(night?0x88202B40:0xBFFFFFFF,night?0x40FFFFFF:0xB8FFFFFF,28));
        LinearLayout brand=Ui.row(this);LinearLayout identity=Ui.column(this);
        identity.addView(Ui.text(this,"锐锐 OS",13,0xFF405779,true));identity.addView(Ui.text(this,Ui.english(this)?"PERSONAL MOBILE SYSTEM":"个人移动系统",9,0xFF71809A,true));
        brand.addView(identity,new LinearLayout.LayoutParams(0,-2,1));brand.addView(Ui.text(this,new SimpleDateFormat(Ui.english(this)?"EEE, MMM d":"M月d日  EEEE",Ui.english(this)?Locale.ENGLISH:Locale.SIMPLIFIED_CHINESE).format(new Date()),13,0xFF63728C,false));top.addView(brand);
        Ui.gap(top,this,12);
        TextClock time=new TextClock(this);time.setFormat24Hour("HH:mm");time.setFormat12Hour("h:mm");time.setTextSize(50);time.setTextColor(Ui.dark(this)?0xFFE8EEF9:0xFF263752);time.setFormat24Hour("HH:mm");top.addView(time);
        Ui.addCard(root,top,this,15);

        pages=new ViewFlipper(this);
        TranslateAnimation outLeft=new TranslateAnimation(1,0,1,-1,1,0,1,0);outLeft.setDuration(190);
        TranslateAnimation inRight=new TranslateAnimation(1,1,1,0,1,0,1,0);inRight.setDuration(190);
        TranslateAnimation outRight=new TranslateAnimation(1,0,1,1,1,0,1,0);outRight.setDuration(190);
        TranslateAnimation inLeft=new TranslateAnimation(1,-1,1,0,1,0,1,0);inLeft.setDuration(190);
        pages.setOutAnimation(outLeft);pages.setInAnimation(inRight);
        pages.addView(homePage(primaryApps()));pages.addView(homePage(secondaryApps()));
        root.addView(pages,new LinearLayout.LayoutParams(-1,0,1));
        dots=Ui.text(this,"●     ○",13,0xFF526B92,true);dots.setGravity(Gravity.CENTER);dots.setPadding(0,Ui.dp(this,6),0,Ui.dp(this,10));root.addView(dots);
        TextView drawerHint=Ui.text(this,"向上滑动打开应用抽屉",10,0xFF75839B,false);drawerHint.setGravity(Gravity.CENTER);LinearLayout.LayoutParams hint=new LinearLayout.LayoutParams(-1,Ui.dp(this,18));hint.bottomMargin=Ui.dp(this,3);root.addView(drawerHint,hint);
        LinearLayout dock=Ui.row(this);dock.setGravity(Gravity.CENTER);dock.setPadding(Ui.dp(this,8),Ui.dp(this,7),Ui.dp(this,8),Ui.dp(this,7));dock.setBackground(Ui.shape(night?0xA828354C:0xCFFFFFFF,night?0x48FFFFFF:0xAAFFFFFF,30));
        String[] dockPkgs={PINNED[0],PINNED[1],PINNED[2],PINNED[3]};
        for(String pkg:dockPkgs){AppEntry app=packageEntry(pkg);if(app!=null)dock.addView(iconCell(app,false),new LinearLayout.LayoutParams(0,Ui.dp(this,64),1));}
        root.addView(dock,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
        gestures=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onFling(MotionEvent start,MotionEvent end,float vx,float vy){
                float dx=end.getX()-start.getX(),dy=end.getY()-start.getY();
                if(Math.abs(dx)>Ui.dp(HomeActivity.this,65)&&Math.abs(dx)>Math.abs(dy)*1.25f){if(dx<0)nextPage();else previousPage();return true;}
                if(dy < -Ui.dp(HomeActivity.this,80)&&Math.abs(dy)>Math.abs(dx)){startActivity(new Intent(HomeActivity.this,AppDrawerActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);return true;}
                return false;
            }
        });
    }
    @Override public boolean dispatchTouchEvent(MotionEvent event){if(gestures!=null)gestures.onTouchEvent(event);return super.dispatchTouchEvent(event);}
    private void nextPage(){if(pages.getDisplayedChild()==0){TranslateAnimation in=new TranslateAnimation(1,1,1,0,1,0,1,0);TranslateAnimation out=new TranslateAnimation(1,0,1,-1,1,0,1,0);in.setDuration(190);out.setDuration(190);pages.setInAnimation(in);pages.setOutAnimation(out);pages.showNext();dots.setText("○     ●");}}
    private void previousPage(){if(pages.getDisplayedChild()==1){TranslateAnimation in=new TranslateAnimation(1,-1,1,0,1,0,1,0);TranslateAnimation out=new TranslateAnimation(1,0,1,1,1,0,1,0);in.setDuration(190);out.setDuration(190);pages.setInAnimation(in);pages.setOutAnimation(out);pages.showPrevious();dots.setText("●     ○");}}
    private List<AppEntry> primaryApps(){
        ArrayList<AppEntry> list=new ArrayList<>();
        AppEntry assistant=new AppEntry("锐锐 AI","✧",0xFF6989CF,()->startActivity(new Intent(this,ChatActivity.class)));
        AppEntry settings=new AppEntry(Ui.tr(this,"设置"),"⚙",0xFF7D8EAB,()->startActivity(new Intent(this,SettingsActivity.class)));
        AppEntry terminal=new AppEntry("终端","⌘",0xFF5F779E,()->startActivity(new Intent(this,TerminalActivity.class)));
        AppEntry[] pins={assistant,settings,packageEntry(PINNED[2]),packageEntry(PINNED[3]),packageEntry(PINNED[0]),packageEntry(PINNED[1]),packageEntry(PINNED[4]),packageEntry(PINNED[5])};
        for(AppEntry app:pins)if(app!=null)list.add(app);return list;
    }
    private List<AppEntry> secondaryApps(){
        ArrayList<AppEntry> list=new ArrayList<>();Set<String> used=new HashSet<>();for(String p:PINNED)used.add(p);
        used.add(getPackageName());Intent query=new Intent(Intent.ACTION_MAIN);query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> entries=getPackageManager().queryIntentActivities(query,PackageManager.MATCH_ALL);
        java.text.Collator collator=java.text.Collator.getInstance(Ui.english(this)?Locale.ENGLISH:Locale.SIMPLIFIED_CHINESE);
        entries.sort((a,b)->collator.compare(a.loadLabel(getPackageManager()).toString(),b.loadLabel(getPackageManager()).toString()));
        for(ResolveInfo r:entries){String pkg=r.activityInfo.packageName;if(used.add(pkg))list.add(new AppEntry(Ui.appLabel(this,r.loadLabel(getPackageManager()).toString()),r.loadIcon(getPackageManager()),()->launchPackage(pkg)));}
        list.add(new AppEntry("锐锐 AI","✧",0xFF6989CF,()->startActivity(new Intent(this,ChatActivity.class))));
        list.add(new AppEntry(Ui.tr(this,"设置"),"⚙",0xFF7D8EAB,()->startActivity(new Intent(this,SettingsActivity.class))));
        list.add(new AppEntry("终端","⌘",0xFF5F779E,()->startActivity(new Intent(this,TerminalActivity.class))));return list;
    }
    private LinearLayout homePage(List<AppEntry> apps){
        LinearLayout page=Ui.column(this);page.setGravity(Gravity.TOP);Ui.gap(page,this,18);
        for(int start=0;start<apps.size();start+=4){LinearLayout row=Ui.row(this);row.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);for(int n=start;n<Math.min(start+4,apps.size());n++)row.addView(iconCell(apps.get(n),true),new LinearLayout.LayoutParams(0,Ui.dp(this,92),1));page.addView(row);Ui.gap(page,this,10);}
        return page;
    }
    private View iconCell(AppEntry app,boolean label){
        LinearLayout box=Ui.column(this);box.setGravity(Gravity.CENTER_HORIZONTAL);box.setPadding(Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,3),Ui.dp(this,4));
        FrameLayout iconBg=new FrameLayout(this);iconBg.setBackground(Ui.shape(0x66FFFFFF,0x44FFFFFF,22));
        if(app.icon!=null){ImageView image=new ImageView(this);image.setImageDrawable(app.icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Ui.dp(this,43),Ui.dp(this,43),Gravity.CENTER);iconBg.addView(image,p);}
        else{TextView glyph=Ui.text(this,app.glyph,25,app.tint,true);glyph.setGravity(Gravity.CENTER);iconBg.addView(glyph,new FrameLayout.LayoutParams(-1,-1));}
        box.addView(iconBg,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
        if(label){TextView name=Ui.text(this,app.name,11,Ui.dark(this)?0xFFF0F3FA:0xFF293953,false);name.setGravity(Gravity.CENTER);name.setMaxLines(1);name.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=Ui.dp(this,5);box.addView(name,p);}
        box.setOnClickListener(v->app.action.run());return box;
    }
    private AppEntry packageEntry(String packageName){
        try{PackageManager pm=getPackageManager();Intent launch=pm.getLaunchIntentForPackage(packageName);if(launch==null)return null;CharSequence label=pm.getApplicationLabel(pm.getApplicationInfo(packageName,0));Drawable icon=pm.getApplicationIcon(packageName);return new AppEntry(Ui.appLabel(this,label.toString()),icon,()->startActivity(launch));}catch(Exception ignored){return null;}
    }
    private void launchPackage(String pkg){AppEntry app=packageEntry(pkg);if(app!=null)app.action.run();}
    static final class AppEntry{final String name,glyph;final int tint;final Drawable icon;final Runnable action;AppEntry(String n,String g,int c,Runnable r){name=n;glyph=g;tint=c;icon=null;action=r;}AppEntry(String n,Drawable i,Runnable r){name=n;glyph="";tint=0;icon=i;action=r;}}
}

