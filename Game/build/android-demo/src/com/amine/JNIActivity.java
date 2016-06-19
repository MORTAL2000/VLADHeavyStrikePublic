
package com.amine;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.io.File;
import android.util.Log;
import com.android.vending.util.*;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class JNIActivity extends Activity 
{
    JNIView mView;
	IabHelper mIAB;
	private static String TAG = "VLAD";
	public boolean mIsTrial = false;
	public boolean mPreviousIsTrial = false;
	public String m_IABError = "";

    @Override protected void onCreate(Bundle icicle)
	{
        super.onCreate(icicle);
			
		String HeartOfTarrasque = "";
		HeartOfTarrasque += "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmr74NZfDV7yOihth2wEfoU3zOEnF8K";
		HeartOfTarrasque += "c6uUquNFNqIw/CW+07fhYkrPNMjCo8tN/drOnbK6+A7M5crYCfW9jFx6deSQFle1TsMQ7+LevC";
		HeartOfTarrasque += "wwuLxei8OtmM7mMPzLzXcyT2UndDcCvrHmVmTDOjGPxxD7cNffLpC3cVCo+7qG7JnRkhV+VUZ6";
		HeartOfTarrasque += "elS8LRASnn7bVrPUw2sMA8M+JmeSdhb7omngP6VdFCRBoH5QIPuHj5XHkbheHiuJH3RPMKsIuH";
		HeartOfTarrasque += "w3SZZx1TU0s4iVRNRFFAvEUgG6VkmA9iBC6lh2z7WvRQckB8AmpoSqE/bKkwr0sMel0TilGvIc";
		HeartOfTarrasque += "5VXBixlSNliAyJDQIDAQAB";
		mIAB = new IabHelper(this, HeartOfTarrasque);
		mIAB.startSetup(new IabHelper.OnIabSetupFinishedListener()
		{
			public void onIabSetupFinished(IabResult result)
			{
				if(!result.isSuccess())
				{
					Log.e("VLAD", "Problem setting up In-app Billing: " + result);
					if(mIAB != null)
					{
						mIAB.dispose();
						mIAB = null;
					}
					return;
				}
				
				if(mIAB == null)
				{
					Log.e("VLAD", "In-app Billing helper was disposed of unexpectedly");
				}
				else
				{
					Log.d("VLAD", "In-app Billing Initialized");
					mIAB.queryInventoryAsync(mGotInventoryListener);
				}
			}
		});
		
        mView = new JNIView(getApplication());
		mView.sActivity = this;
		setContentView(mView);
		String internaPath = getFilesDir().getAbsolutePath();
		File externalDir = getExternalFilesDir(null);
		String externalPath = (externalDir != null) ? externalDir.getAbsolutePath() : new String("");		
		JNILib.init(this, getAssets(), internaPath, externalPath);
    }
	
	// Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
	{
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
		{
            Log.d("VLAD", "Query inventory finished.");            
            if (mIAB == null)
			{
				Log.e("VLAD", "In-app Billing helper was disposed of unexpectedly");
				return;
			}
            
            if (result.isFailure())
			{
                Log.e("VLAD", "Inventory query failed: " + result);
				if(mIAB != null)
				{
					mIAB.dispose();
					mIAB = null;
				}
                return;
            }

            Log.d("VLAD", "Query inventory was successful.");
            Purchase fullGame = inventory.getPurchase("com.frantic.vlad.full_game");
            mIsTrial = (fullGame == null);
            Log.d("VLAD", "mIsTrial: " + mIsTrial);	
        }
    };
	
	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
	{
		public void onIabPurchaseFinished(IabResult result, Purchase purchase)
		{
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
				
			if(mIAB == null)
			{
				Log.e("VLAD", "In-app Billing helper was disposed of unexpectedly");
				return;
			}
	
			if(result.isFailure())
			{
				m_IABError = "" + result;
				Log.e("VLAD", m_IABError);
				
				// no need to show a user dialog, google play already displays its own dialog
				//JNIActivity.this.runOnUiThread(new Runnable()
				//{
					//public void run() 
					//{
						//AlertDialog.Builder builder1 = new AlertDialog.Builder(JNIActivity.this);
						//builder1.setMessage(m_IABError);
						//builder1.setCancelable(true);
						//builder1.setPositiveButton("OK", 
						//new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int id) { dialog.cancel(); } });
						//AlertDialog alert11 = builder1.create();
						//alert11.show();
					//}
				//});
				
				return;
			}	
	
			Log.d(TAG, "Purchase successful.");
			boolean bFound = purchase.getSku().equals(mProductToPurchase);
			Log.d(TAG, "mProductToPurchase found: " + bFound);
			mIsTrial = false;
		}
	};
	
	String mProductToPurchase = "";
	public void purchase(String productID)
	{
		if(mIAB != null)
		{
			mProductToPurchase = productID;
			mIAB.flagEndAsync();
			mIAB.launchPurchaseFlow(this, productID, 10001, mPurchaseFinishedListener, "");
		}
		else
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.frantic.vlad")));
		}		
	}
	
	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

		if(mIAB != null && mIAB.handleActivityResult(requestCode, resultCode, data))
		{
			Log.d(TAG, "onActivityResult handled by IabHelper.");
			return;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override protected void onDestroy()
	{
		if(mIAB != null)
		{
			mIAB.dispose();
			mIAB = null;
		}
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
