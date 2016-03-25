package com.example.mfamilys.mrun.ViewActivity;
import android.content.Context;
import android.util.AttributeSet;
import  android.widget.GridView;

/**
 * Created by mfamilys on 15-8-6.
 */
public class NoScollGridView extends  GridView{
    public NoScollGridView(Context context){
        super(context);
    }
    public NoScollGridView(Context context,AttributeSet attr){
        super(context,attr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandspec=MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE>>2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandspec);
    }
}
