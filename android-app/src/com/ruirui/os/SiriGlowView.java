package com.ruirui.os;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.animation.LinearInterpolator;

final class SiriGlowView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final float density;
    private final Path frame=new Path();private ValueAnimator animator;private float phase=0;
    SiriGlowView(Context c){super(c);density=getResources().getDisplayMetrics().density;setLayerType(View.LAYER_TYPE_HARDWARE,null);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();animator=ValueAnimator.ofFloat(0,1);animator.setDuration(2400);animator.setRepeatCount(ValueAnimator.INFINITE);animator.setInterpolator(new LinearInterpolator());animator.addUpdateListener(a->{phase=(float)a.getAnimatedValue();invalidate();});animator.start();}
    @Override protected void onDetachedFromWindow(){if(animator!=null)animator.cancel();super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();if(w<1||h<1)return;
        float edge=5*density;RectF r=new RectF(edge,edge,w-edge,h-edge);frame.reset();frame.addRoundRect(r,Math.min(46*density,Math.min(w,h)*.18f),Math.min(46*density,Math.min(w,h)*.18f),Path.Direction.CW);
        int[] colors={Color.rgb(91,190,255),Color.rgb(180,151,255),Color.rgb(111,225,224),Color.rgb(119,157,255),Color.rgb(91,190,255)};float[] stops={0,.27f,.52f,.77f,1};
        SweepGradient sweep=new SweepGradient(w/2,h/2,colors,stops);Matrix rotation=new Matrix();rotation.setRotate(phase*360,w/2,h/2);sweep.setLocalMatrix(rotation);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setShader(sweep);paint.setPathEffect(null);
        paint.setStrokeWidth(9*density);paint.setAlpha(32);c.drawPath(frame,paint);
        paint.setStrokeWidth(2.1f*density);paint.setAlpha(220);c.drawPath(frame,paint);paint.setShader(null);
    }
}
