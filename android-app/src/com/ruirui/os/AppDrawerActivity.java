package com.ruirui.os;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppDrawerActivity extends Activity {
    private LinearLayout list;private ArrayList<Entry> entries;
    @Override public void onCreate(Bundle b){super.onCreate(b);draw();}
    private void draw(){
        boolean night=Ui.dark(this);getWindow().setNavigationBarColor(night?0xFF171D2B:0xFFF4F5FA);getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);getWindow().getDecorView().setSystemUiVisibility(night?0:android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        LinearLayout root=Ui.root(this);TextView back=Ui.text(this,"⌄",28,0xFF405779,true);back.setGravity(Gravity.CENTER);root.addView(back,new LinearLayout.LayoutParams(-1,Ui.dp(this,34)));back.setOnClickListener(v->finish());
        root.addView(Ui.text(this,Ui.tr(this,"所有应用"),29,0xFF1B2B47,true));Ui.gap(root,this,14);
        EditText search=new EditText(this);search.setSingleLine(true);search.setTextSize(15);search.setHint(Ui.tr(this,"搜索应用"));search.setTextColor(Ui.foreground(this));search.setHintTextColor(Ui.hintColor(this));search.setPadding(Ui.dp(this,16),0,Ui.dp(this,16),0);search.setBackground(Ui.shape(0xCCFFFFFF,0x55FFFFFF,24));root.addView(search,new LinearLayout.LayoutParams(-1,Ui.dp(this,52)));Ui.gap(root,this,12);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);list=Ui.column(this);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));entries=loadApps();showApps("");
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){showApps(s.toString());}public void afterTextChanged(Editable e){}});
        setContentView(root);
    }
    private ArrayList<Entry> loadApps(){
        PackageManager pm=getPackageManager();Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> resolved=pm.queryIntentActivities(q,PackageManager.MATCH_ALL);ArrayList<Entry> out=new ArrayList<>();
        for(ResolveInfo r:resolved){String pkg=r.activityInfo.packageName;if(pkg.equals(getPackageName()))continue;out.add(new Entry(Ui.appLabel(this,r.loadLabel(pm).toString()),r.loadIcon(pm),()->{Intent i=pm.getLaunchIntentForPackage(pkg);if(i!=null)startActivity(i);}));}
        out.add(new Entry(Ui.english(this)?"RuiRui AI":"锐锐 AI",null,()->startActivity(new Intent(this,ChatActivity.class))));
        out.add(new Entry(Ui.tr(this,"设置"),null,()->startActivity(new Intent(this,SettingsActivity.class))));
        out.add(new Entry(Ui.tr(this,"终端"),null,()->startActivity(new Intent(this,TerminalActivity.class))));
        Collator collator=Collator.getInstance(Ui.english(this)?Locale.ENGLISH:Locale.SIMPLIFIED_CHINESE);out.sort((a,b)->collator.compare(a.name,b.name));return out;
    }
    private void showApps(String filter){
        list.removeAllViews();ArrayList<Entry> shown=new ArrayList<>();for(Entry e:entries)if(e.name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))shown.add(e);
        for(int n=0;n<shown.size();n+=4){LinearLayout row=Ui.row(this);row.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);for(int i=n;i<Math.min(n+4,shown.size());i++)row.addView(cell(shown.get(i)),new LinearLayout.LayoutParams(0,Ui.dp(this,100),1));list.addView(row);}
        if(shown.isEmpty()){TextView empty=Ui.text(this,Ui.tr(this,"没有找到应用"),15,Ui.hintColor(this),false);empty.setGravity(Gravity.CENTER);list.addView(empty,new LinearLayout.LayoutParams(-1,Ui.dp(this,120)));}
    }
    private LinearLayout cell(Entry e){
        LinearLayout box=Ui.column(this);box.setGravity(Gravity.CENTER_HORIZONTAL);FrameLayout bg=new FrameLayout(this);bg.setBackground(Ui.shape(0x66FFFFFF,0x44FFFFFF,22));
        if(e.icon!=null){ImageView image=new ImageView(this);image.setImageDrawable(e.icon);image.setScaleType(ImageView.ScaleType.FIT_CENTER);bg.addView(image,new FrameLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,44),Gravity.CENTER));}
        else{String glyph=e.name.contains("AI")?"✧":e.name.contains("终端")||e.name.contains("Terminal")?"⌘":"⚙";TextView t=Ui.text(this,glyph,24,0xFF6B84B4,true);t.setGravity(Gravity.CENTER);bg.addView(t,new FrameLayout.LayoutParams(-1,-1));}
        box.addView(bg,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,58)));TextView label=Ui.text(this,e.name,11,Ui.dark(this)?0xFFF0F3FA:0xFF293953,false);label.setGravity(Gravity.CENTER);label.setMaxLines(1);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=Ui.dp(this,5);box.addView(label,p);box.setOnClickListener(v->e.action.run());return box;
    }
    private static final class Entry{final String name;final Drawable icon;final Runnable action;Entry(String n,Drawable i,Runnable r){name=n;icon=i;action=r;}}
}

