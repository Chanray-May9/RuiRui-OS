package com.ruirui.os;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

/** The existing Siri-style status pill, expanded in place for follow-up instructions. */
final class SiriFloatingView extends LinearLayout {
    private final AiActivityService service;
    private final TextView status;
    private final EditText input;
    private final LinearLayout composer;
    private boolean expanded=false;
    SiriFloatingView(AiActivityService service,String initial){super(service);this.service=service;setOrientation(VERTICAL);setPadding(Ui.dp(service,10),Ui.dp(service,7),Ui.dp(service,10),Ui.dp(service,7));setBackground(Ui.shape(0xF5F8FBFF,0xBBDDE7FA,24));setElevation(Ui.dp(service,12));
        LinearLayout top=Ui.row(service);TextView orb=Ui.text(service,"✦",16,0xFF739FE0,true);orb.setGravity(Gravity.CENTER);orb.setBackground(Ui.shape(0xFFE8F2FF,0,20));top.addView(orb,new LinearLayout.LayoutParams(Ui.dp(service,30),Ui.dp(service,30)));status=Ui.text(service,initial,12,0xFF344A68,true);status.setSingleLine(true);status.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams st=new LinearLayout.LayoutParams(-1,Ui.dp(service,30));st.leftMargin=Ui.dp(service,9);top.addView(status,st);addView(top,new LayoutParams(-1,Ui.dp(service,30)));top.setOnClickListener(v->toggle());
        composer=Ui.row(service);composer.setVisibility(GONE);composer.setPadding(0,Ui.dp(service,7),0,0);input=new EditText(service);input.setSingleLine(true);input.setTextSize(13);input.setTextColor(0xFF253550);input.setHintTextColor(0xFF8793A7);input.setPadding(Ui.dp(service,12),0,Ui.dp(service,8),0);input.setBackground(Ui.shape(0xFFF5F8FE,0xFFDDE5F2,16));composer.addView(input,new LayoutParams(0,Ui.dp(service,42),1));TextView send=Ui.text(service,"↑",19,Color.WHITE,true);send.setGravity(Gravity.CENTER);send.setBackground(Ui.shape(0xFF617FC1,0,16));LayoutParams sp=new LayoutParams(Ui.dp(service,48),Ui.dp(service,42));sp.leftMargin=Ui.dp(service,7);composer.addView(send,sp);addView(composer,new LayoutParams(-1,-2));send.setOnClickListener(v->submit());input.setOnEditorActionListener((v,a,e)->{submit();return true;});setFocusableInTouchMode(true);
    }
    void setStatus(String text){status.setText(text==null||text.trim().isEmpty()?"锐锐正在思考":text);}
    private void toggle(){expanded=!expanded;composer.setVisibility(expanded?VISIBLE:GONE);service.setFloatingFocusable(expanded);service.updateFloatingLayout();if(expanded){input.requestFocus();input.postDelayed(()->{InputMethodManager imm=(InputMethodManager)service.getSystemService(Context.INPUT_METHOD_SERVICE);if(imm!=null)imm.showSoftInput(input,InputMethodManager.SHOW_IMPLICIT);},120);}else hideKeyboard();}
    private void submit(){String text=input.getText().toString().trim();if(text.isEmpty())return;input.setText("");ChatActivity.addSupplement(service,text);expanded=false;composer.setVisibility(GONE);hideKeyboard();service.setFloatingFocusable(false);service.updateFloatingLayout();}
    private void hideKeyboard(){InputMethodManager imm=(InputMethodManager)service.getSystemService(Context.INPUT_METHOD_SERVICE);if(imm!=null)imm.hideSoftInputFromWindow(input.getWindowToken(),0);input.clearFocus();}
}
