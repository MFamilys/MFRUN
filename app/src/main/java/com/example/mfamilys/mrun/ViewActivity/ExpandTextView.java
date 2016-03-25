package com.example.mfamilys.mrun.ViewActivity;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mfamilys.mrun.R;

/**
 * Created by mfamilys on 15-8-3.
 */
public class ExpandTextView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = ExpandTextView.class.getSimpleName();
    //默认显示的行数
    private static final int MAX_COLLAPSED_LINES=8;
    //默认的动画持续时间
    private static final int DEFAULT_ANIM_DURATION=300;
    //默认的动画开始时的透明值
    private static final float DEFAULT_ANIM_ALPHA_START=0.7f;
    protected TextView mTV;
    private ImageButton mButton;
    private boolean mCollapsed=true;
    private int mCollapsedHeight;
    private int mTextHeightWithMaxLines;
    private int mMarginBetweenTxtAndBottonm;
    private int mMaxCollapsedLines;
    private int mAnimationDuration;
    private float mAnimAlphaStart;
    private Drawable mExpandDrawable;
    private Drawable mCollapseDrawable;
    //保存状态(稀疏数组)
    private SparseBooleanArray mCollapsedStatus;
    private int mPosition;
    //是否重置布局标志
    private boolean mRelayout;
    //动画加载中标志
    private boolean mAnimating;
    //回调标志(自定义)
    private OnExpandStateChangeListener mListener;


    //构造函数
    public ExpandTextView(Context context){
         this(context,null);
     }

    public ExpandTextView(Context context, AttributeSet attrs){
        super(context,attrs);
        init(attrs);
    }

    //限制只能垂直布局,不能水平
    public void setOrientation(int orienttation){
        if(orienttation==LinearLayout.HORIZONTAL){
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation" );
        }
        super.setOrientation(orienttation);
    }

    //文本框点击事件
    @Override
    public void onClick(View view) {
        //如果不显示按钮(即刚好铺满布局,直接返回)
        if (mButton.getVisibility() == View.GONE) {
            return;
        } else {
            //设置标志,每次相反
            mCollapsed = !mCollapsed;
            //设置图像
            mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
            //将某一个位置跟折叠状态对应起来
            if (mCollapsedStatus != null) {
                mCollapsedStatus.put(mPosition, mCollapsed);
            }
            Animation animation;
            if (mCollapsed) {
                animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
            } else {
                animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() +
                        mTextHeightWithMaxLines - mTV.getHeight());
            }
            //保持动画结束时的状态
            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //设置透明效果
                    applyAlphaAnimation(mTV, mAnimAlphaStart);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //清除动画防止applyTransformation()调用
                    clearAnimation();
                    //清楚动画标志
                    mAnimating = false;
                    if (mListener != null) {
                        mListener.OnExpandStateChanged(mTV, !mCollapsed);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //动画完成之后清楚动画防止占用内存
            clearAnimation();
            startAnimation(animation);
        }
    }
    //向下传送的触屏控制
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //当动画在进行时,中断所有的子控件触屏响应事件，完成后再向下传递
        return mAnimating;
    }
    //当完成子组件加载时
    @Override
    protected void onFinishInflate() {
        findView();
        super.onFinishInflate();
    }

    //加载组件的大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //当布局无需变化或者组件隐藏时,返回
        if(!mRelayout||getVisibility()==View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout=false;
        //最优情况下,无需显示按钮
        mButton.setVisibility(View.GONE);
        mTV.setMaxLines(Integer.MAX_VALUE);
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        //如果每一行都已经被折叠,直接返回
        if(mTV.getLineCount()<=mMaxCollapsedLines){
            return;
        }
        //保存实际文本高度
        mTextHeightWithMaxLines=getRealTextViewHeight(mTV);
        //必须折叠的情况
        if(mCollapsed){
            mTV.setMaxLines(mMaxCollapsedLines);
        }
        mButton.setVisibility(View.VISIBLE);
        //重新设置窗口大小
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        if(mCollapsed){
            mTV.post(new Runnable() {
                @Override
                public void run() {
                    mMarginBetweenTxtAndBottonm=getHeight()-mTV.getHeight();
                }
            });
            //保存高度
            mCollapsedHeight=getMeasuredHeight();
        }
    }
    /*
    **以下为自定义方法
     */

    public  void setOnExpandStateChangeListener(@Nullable OnExpandStateChangeListener listener){
        mListener=listener;
    }

    public void setText(@Nullable CharSequence text){
        mRelayout=true;
        mTV.setText(text);
        setVisibility(TextUtils.isEmpty(text)?View.GONE:View.VISIBLE);
    }

    public void setText(@Nullable CharSequence text,@NonNull SparseBooleanArray CollapsedStatus,int Position){
        mCollapsedStatus=CollapsedStatus;
        mPosition=Position;
        //取不到Postion就返回真(默认已折叠)
        boolean isCollapsed=mCollapsedStatus.get(Position,true);
        clearAnimation();
        mCollapsed=isCollapsed;
        mButton.setImageDrawable(mCollapsed?mExpandDrawable:mCollapseDrawable);
        //调用上面的函数
        setText(text);
        getLayoutParams().height= ViewGroup.LayoutParams.WRAP_CONTENT;
        //如果不适合的话通知父组件重画
        requestLayout();
    }

    @Nullable
    public CharSequence getText(){
        if(mTV == null){
            return "";
        }
        return mTV.getText();
    }

    private void init(AttributeSet attrs){
        //获得属性容器
        TypedArray typedArray=getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
        mMaxCollapsedLines=typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines,MAX_COLLAPSED_LINES);
        mAnimationDuration=typedArray.getInt(R.styleable.ExpandableTextView_animDuration,DEFAULT_ANIM_DURATION);
        mAnimAlphaStart=typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
        mExpandDrawable=typedArray.getDrawable(R.styleable.ExpandableTextView_expandDrawable);
        mCollapseDrawable=typedArray.getDrawable(R.styleable.ExpandableTextView_collapseDrawable);
        if(mExpandDrawable==null){
        mExpandDrawable=getDrawable(getContext(), R.drawable.ic_collapse_small_holo_light);
        }
        if(mCollapseDrawable==null){
            mCollapseDrawable=getDrawable(getContext(),R.drawable.ic_expand_small_holo_light);
        }
        typedArray.recycle();
        setOrientation(LinearLayout.VERTICAL);
        setVisibility(GONE);
    }

    private void findView(){
        mTV=(TextView)findViewById(R.id.expandable_text);
        mTV.setOnClickListener(this);
        mButton=(ImageButton)findViewById(R.id.expand_collapse);
        mButton.setImageDrawable(mCollapsed?mExpandDrawable:mCollapseDrawable);
        mButton.setOnClickListener(this);
    }


    private static void applyAlphaAnimation(View view,float alpha){
        AlphaAnimation alphaAnimation=new AlphaAnimation(alpha,alpha);
        alphaAnimation.setDuration(0);
        alphaAnimation.setFillAfter(true);
        view.startAnimation(alphaAnimation);
    }

    private static Drawable getDrawable(@NonNull Context context,@DrawableRes int resId){
        Resources resources=context.getResources();
        return resources.getDrawable(resId);
    }
    //返回文本框的实际高度(文本框高度＋上下边界高度)
    private static int getRealTextViewHeight(@NonNull TextView textView){
        int textHeight=textView.getLayout().getLineTop(textView.getLineCount());
        int padding =textView.getCompoundPaddingTop()+textView.getCompoundPaddingBottom();
        return textHeight+padding;
    }
    /*
    **以下是自定义类
     */
    class ExpandCollapseAnimation extends  Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            mTV.setMaxHeight(newHeight - mMarginBetweenTxtAndBottonm);
            if (Float.compare(mAnimAlphaStart, 1.0f) != 0) {
                applyAlphaAnimation(mTV, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
            }
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

        //回调函数
        public interface OnExpandStateChangeListener{
            void OnExpandStateChanged(TextView textView,boolean isExpanded);
        }
}
