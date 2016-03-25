package com.example.mfamilys.mrun.ViewActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.example.mfamilys.mrun.R;

public class SlideButtonActivity extends Activity {

    private SlideButton mSlideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setTheme(android.R.style.Animation_Dialog);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slide_button_layout);
        mSlideButton=(SlideButton)this.findViewById(R.id.SlideButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mSlideButton.handleActivityEvent(event)){
            Toast.makeText(this,"touch",Toast.LENGTH_LONG);
        }
        return super.onTouchEvent(event);
    }
}
