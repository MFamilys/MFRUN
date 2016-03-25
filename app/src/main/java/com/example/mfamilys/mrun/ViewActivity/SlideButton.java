package com.example.mfamilys.mrun.ViewActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SlideButton extends Button {
    private boolean isSlide = false;

    public SlideButton(Context context, AttributeSet attrs, int defstyle) {
        super(context, attrs, defstyle);
    }

    public SlideButton(Context context) {
        super(context);
    }

    public SlideButton(Context context, AttributeSet attrs) {

        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_UP){
            isSlide=false;
        }
        else if(event.getAction()==MotionEvent.ACTION_DOWN){
            isSlide=true;
        }
        return true;
    }
    public boolean handleActivityEvent(MotionEvent activityEvent){
        boolean result=false;
        if(isSlide){
            //当用户长按结束
            if(activityEvent.getAction()==MotionEvent.ACTION_UP){
               //已经滑动到尾部
                if(this.getLeft()+this.getWidth()/2>((FrameLayout)this.getParent().getParent()).getWidth()-this.getWidth()){
                    LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)this.getLayoutParams();
                    lp.leftMargin=0;
                    this.setLayoutParams(lp);
                    isSlide=false;
                    result=true;
                }
                //没滑动到底部动画返回
                else{
                    TranslateAnimation trans=new TranslateAnimation(
                            Animation.ABSOLUTE,0,Animation.ABSOLUTE,-this.getLeft(),
                            Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0
                    );
                    trans.setDuration(100);
                    //设置动画改变的速率
                    trans.setInterpolator(new AccelerateInterpolator());
                    trans.setInterpolator(new Interpolator() {
                        @Override
                        public float getInterpolation(float v) {
                            return 0;
                        }
                    });
                    trans.setAnimationListener(new SlidingAnimationListener(this));
                    startAnimation(trans);
                }
            }
            //还在拖动的过程中,手还没放开(Action_down or Action_move)
            else if(activityEvent.getAction()==MotionEvent.ACTION_MOVE){
                LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)getLayoutParams();
                lp.leftMargin=(int)(activityEvent.getX()-
                        ((FrameLayout)this.getParent().getParent()).getLeft())-this.getWidth()/2;
                if(lp.leftMargin>0 && lp.leftMargin<((FrameLayout) this.getParent().getParent()).getWidth()-this.getWidth()){
                    setLayoutParams(lp);
                }
            }

        }
        return result;
    }

    private static class SlidingAnimationListener implements Animation.AnimationListener{
        private SlideButton but;
        public SlidingAnimationListener(SlideButton button){
            this.but=button;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            rePosition();
            but.isSlide=false;
            but.clearAnimation();
        }
        private void rePosition(){
            LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)but.getLayoutParams();
            lp.leftMargin=0;
            but.setLayoutParams(lp);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationStart(Animation animation) {

        }
    }
}
