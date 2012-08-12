package com.dany.btnxt;

import java.net.URL;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TestTouchPad extends Activity{
	
	protected TouchPadView pad;
	
	protected byte motorA = 0;
	protected byte motorB = 0;
	
	protected float mX=0;
	protected float mY=0;
	protected float mTrueX=0;
	protected float mTrueY=0;
	
	protected byte MSG = -1;
	
	protected AsyncTask customMoveMSG_Thread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.testtouchpad_layout);
		
		pad = (TouchPadView) findViewById(R.id.myTouchPad);
		pad.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				padTouchListener(v, event);
				return false;
			}
		});
		
		
		
	}

	protected void padTouchListener(View v, MotionEvent event) {
		Log.d("DEBUG",event.toString());
		if(v == pad){
			
			double moveDif = Math.abs(Math.sqrt((event.getX() - mTrueX)*(event.getX() - mTrueX) + (event.getY() - mTrueY)*(event.getY() - mTrueY)));
			Log.d("DEBUG", "" + moveDif);
			if( moveDif > 20){
				mTrueX = event.getX();
				mX = mTrueX;
				mTrueY = event.getY();
				mY = mTrueY;
				
				if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
					if(event.getX() < 200){
						mX = 200 - mX;
						mY = 200 - mY;
						motorA = (byte) Math.floor(mY/2);
						motorB = (byte) Math.floor(Math.sqrt(mX*mX + mY*mY)/2);

					} else if (event.getX() == 200){
						
						motorA = (byte) Math.floor(mX/2);
						motorB = (byte) Math.floor((200 - mY)/2);

					} else if (event.getX() > 200){
						mX = mX - 200;
						mY = 200 - mY;
						motorA = (byte) Math.floor(Math.sqrt(mX*mX + mY*mY)/2);
						motorB = (byte) Math.floor(mY/2);

					}
					
					if(motorA > 100){motorA=100;}
					if(motorB > 100){motorB=100;}
					if(motorA < 0){motorA=0;}
					if(motorB < 0){motorB=0;}
					Log.e("DEBUG","Motors " + motorA + ", " + motorB);
					MSG = BT_Comm.CUSTOM_MOVE;
				}
				
			}
			
			if (event.getAction() == MotionEvent.ACTION_UP) {
				MSG = BT_Comm.STOP;
			}
			
			if(customMoveMSG_Thread != null)
				customMoveMSG_Thread.cancel(true);
			customMoveMSG_Thread = new CustomeMoveMSG_AsyncTask().execute();
			
			
			
		}
		
	}
	
	private class CustomeMoveMSG_AsyncTask extends AsyncTask<URL, Integer, Long> {

		@Override
		protected Long doInBackground(URL... params) {
			if(ControllerGUI.btComm.getConnectionMade()){
				try {
					if(MSG != BT_Comm.STOP){
						ControllerGUI.btComm.writeMessage(MSG);
						ControllerGUI.btComm.writeMessage(motorA);
						ControllerGUI.btComm.writeMessage(motorB);
					} else {
						ControllerGUI.btComm.writeMessage(BT_Comm.STOP);
					}
				} catch (Exception e) {

				}
			}
			return null;
		}
	}
	
	

}
