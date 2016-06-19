
package com.amine;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.io.File;
import android.util.Log;

public class JNIActivity extends Activity 
{
    JNIView mView;

    @Override protected void onCreate(Bundle icicle)
	{
        super.onCreate(icicle);
        mView = new JNIView(getApplication());
		mView.sActivity = this;
		setContentView(mView);
		String internaPath = getFilesDir().getAbsolutePath();
		File externalDir = getExternalFilesDir(null);
		String externalPath = (externalDir != null) ? externalDir.getAbsolutePath() : new String("");		
		JNILib.init(this, getAssets(), internaPath, externalPath);
    }
	
	@Override protected void onDestroy()
	{		
		JNILib.destroy();
		super.onDestroy();
	}
	
    @Override protected void onPause()
	{
		JNILib.pause();
        super.onPause();
        mView.onPause();
    }

    @Override protected void onResume()
	{
		JNILib.resume();
        super.onResume();
        mView.onResume();
    }
	
	public boolean onTouchEvent(MotionEvent event)
	{		
		int action = event.getAction();
		int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		if(pointerIndex == 0)
		{
			int actionType = action & MotionEvent.ACTION_MASK;
			boolean bPressed = (actionType == MotionEvent.ACTION_DOWN || actionType == MotionEvent.ACTION_MOVE);
			float x = event.getX(pointerIndex);
			float y = event.getY(pointerIndex);
			JNILib.setTouchState(x, y, bPressed);		
			return true;
		}
		return super.onTouchEvent(event);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			JNILib.setBackPressed(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			JNILib.setBackPressed(false);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
