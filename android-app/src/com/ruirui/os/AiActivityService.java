package com.ruirui.os;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public final class AiActivityService extends Service {
    private static final String CHANNEL="ruirui_ai_activity";private WindowManager windows;private SiriGlowView glow;private SiriFloatingView floating;private WindowManager.LayoutParams floatingParams;
    static void start(Context context,String status){Intent i=new Intent(context,AiActivityService.class).setAction("start").putExtra("status",status);if(Build.VERSION.SDK_INT>=26)context.startForegroundService(i);else context.startService(i);}
    static void update(Context context,String status){context.startService(new Intent(context,AiActivityService.class).setAction("update").putExtra("status",status));}
    static void finish(Context context){context.startService(new Intent(context,AiActivityService.class).setAction("finish"));}
    @Override public void onCreate(){super.onCreate();windows=(WindowManager)getSystemService(WINDOW_SERVICE);createChannel();}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel(CHANNEL,"锐锐 AI 任务",NotificationManager.IMPORTANCE_LOW);ch.setDescription("AI 正在执行任务时显示状态");ch.setShowBadge(false);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch);}}
    private Notification notification(String status){Intent open=new Intent(this,ChatActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);PendingIntent pi=PendingIntent.getActivity(this,1,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("锐锐正在工作").setContentText(status).setContentIntent(pi).setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE).build();}
    @Override public int onStartCommand(Intent intent,int flags,int id){String action=intent==null?"start":intent.getAction();String status=intent==null?"锐锐正在思考":intent.getStringExtra("status");if("finish".equals(action)){removeGlow();stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return START_NOT_STICKY;}
        if(Build.VERSION.SDK_INT>=26)startForeground(41,notification(status));else startForeground(41,notification(status));
        if("start".equals(action))showGlow(status);else if(glow!=null){if(floating!=null)floating.setStatus(status);((android.app.NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(41,notification(status));}
        return START_NOT_STICKY;
    }
    private void showGlow(String status){if(!Settings.canDrawOverlays(this))return;if(glow==null){glow=new SiriGlowView(this);int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS|WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.START;p.setTitle("锐锐 AI 悬浮光效");if(Build.VERSION.SDK_INT>=28)p.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;try{windows.addView(glow,p);}catch(Exception e){glow=null;}}
        if(floating==null){floating=new SiriFloatingView(this,status);int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;floatingParams=new WindowManager.LayoutParams(Ui.dp(this,290),WindowManager.LayoutParams.WRAP_CONTENT,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);floatingParams.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;floatingParams.y=Ui.dp(this,26);floatingParams.softInputMode=android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;floatingParams.setTitle("锐锐 AI 悬浮助手");try{windows.addView(floating,floatingParams);}catch(Exception e){floating=null;floatingParams=null;}}}
    void setFloatingFocusable(boolean focus){if(floating==null||floatingParams==null)return;try{if(focus)floatingParams.flags&=~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;else floatingParams.flags|=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;windows.updateViewLayout(floating,floatingParams);}catch(Exception ignored){}}
    void updateFloatingLayout(){if(floating==null||floatingParams==null)return;try{windows.updateViewLayout(floating,floatingParams);}catch(Exception ignored){}}
    private void removeGlow(){if(floating!=null){try{windows.removeView(floating);}catch(Exception ignored){}floating=null;floatingParams=null;}if(glow!=null){try{windows.removeView(glow);}catch(Exception ignored){}glow=null;}}
    @Override public void onDestroy(){removeGlow();super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
