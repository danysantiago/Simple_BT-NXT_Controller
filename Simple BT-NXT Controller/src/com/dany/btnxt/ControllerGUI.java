package com.dany.btnxt;

import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

public class ControllerGUI extends Activity{

	public static int BT_REQUEST_CODE = 5;

	//Our BT Communication Class
	public static BT_Comm btComm = new BT_Comm();

	//Views in the GUI
	protected Button upButton;
	protected Button leftupButton;
	protected Button rightupButton;
	protected Button leftButton;
	protected Button rightButton;
	protected Button downButton;
	protected SeekBar spdSeekBar;
	protected ListView console;
	protected Dialog progressDialog;

	//Byte message (default it as a STOP command)
	protected byte msg = BT_Comm.STOP;
	protected byte motorSpd = 40;
	
	public static final int OBSTACLE_WARNING_VALUE = 30;

	protected boolean connectionMade = false;

	//Threads
	AsyncTask msgThread;
	AsyncTask setSpdThread;
	AsyncTask connectNXTThread;
	AsyncTask dataReciveThread;

	//Console string array
	ArrayList<String> consoleText = new ArrayList<String>();

	//Called when creating activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//Sets for the screen not to sleep (un-enabling Android OS to kill app while on sleep)
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Foward button and listener
		upButton = (Button) findViewById(R.id.up_button);
		upButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});
		
		leftupButton = (Button) findViewById(R.id.leftfoward_button);
		leftupButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});
		
		rightupButton = (Button) findViewById(R.id.rightfoward_button);
		rightupButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});

		//Left button and listener
		leftButton = (Button) findViewById(R.id.left_button);
		leftButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});

		//Right button and listener
		rightButton = (Button) findViewById(R.id.right_button);
		rightButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});

		//Down button and listener
		downButton = (Button) findViewById(R.id.down_button);
		downButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buttonTouchListener(v, event);
				return false;
			}
		});

		//Seekbar (Speed) listener
		spdSeekBar = (SeekBar) findViewById(R.id.speed_bar);
		spdSeekBar.setProgress(motorSpd); //Sets the seek bar at default speed
		spdSeekBar.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//One seekbar release start set speed thread
				if (event.getAction() == MotionEvent.ACTION_UP){
					if(connectionMade){
						int progress = spdSeekBar.getProgress();
						Log.d("DEBUG", "Seek bar progress: " + progress);
						motorSpd = (byte) (progress);
						if(setSpdThread != null)
							setSpdThread.cancel(true);
						setSpdThread = new SetSpd_AsyncTask().execute();
					}
				}

				return false;
			}

		});

		//Console setup
		console = (ListView) findViewById(R.id.mainconsole_listview);
		console.setDivider(null);

		//Start intent requesting bluetooth power
		Intent requestBT = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
		startActivityForResult(requestBT, BT_REQUEST_CODE);

		//Start data receiver thread
		dataReciveThread = new DataReciver_AsyncTask().execute();


	}

	//Called when an intent returns with a result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BT_REQUEST_CODE){ //Result received from BT request
			if(resultCode == RESULT_OK){
				consoleText.add("Android Bluetooth Enabled");
				//Sets the adapter to our BT comm class
				btComm.setAdapterBT(BluetoothAdapter.getDefaultAdapter());
				updateConsole();
			} else if (resultCode == RESULT_CANCELED){
				//Finish app if bluetooth not permitted
				Toast.makeText(this, "App closed because Bluetooth was denied", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	//Called when activity is killed
	@Override
	public void onDestroy(){
		//Send close comm msg to NXT
		msg = BT_Comm.CLOSE_COMM;

		new SendMSG_AsyncTask().execute();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Kill threads on closing
		if(connectNXTThread != null)
			connectNXTThread.cancel(true);
		if(dataReciveThread != null)
			dataReciveThread.cancel(true);

		//Close Android comm
		btComm.closeConnection();

		super.onDestroy();
	}


	//Button listener method
	public void buttonTouchListener(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d("DEBUG","Pressing button");

			if (v == upButton){
				msg = BT_Comm.MOVE_FOWARD;
			} else if (v == leftupButton){
				msg = BT_Comm.MOVE_FOWARD_LEFT;
			} else if (v == rightupButton){
				msg = BT_Comm.MOVE_FOWARD_RIGHT;
			} else if (v == leftButton){
				msg = BT_Comm.ROTATE_LEFT;
			} else if (v == rightButton){
				msg = BT_Comm.ROTATE_RIGHT;
			} else if (v == downButton){
				msg = BT_Comm.MOVE_BACKWARD;
			}
			
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			msg = BT_Comm.STOP;
			
		}
		
		//First cancel any previous msg thread (makes comm more responsive)
		if(msgThread != null)
			msgThread.cancel(true);
		
		//Start send msg thread based on msg settled
		msgThread = new SendMSG_AsyncTask().execute();


	}

	//Method called when the activity starts to create option menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	//Method called when option menu is about to be drawn
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//Dependion on connection disable/enable connect item
		if(connectionMade)
			menu.findItem(R.id.connect_item).setEnabled(false);
		else
			menu.findItem(R.id.connect_item).setEnabled(true);

		return true;
	}

	//Method called when a menu item is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.clearconsole_item) {
			consoleText.clear();
			consoleText.add("Console Text Cleared");
			updateConsole();
		} else if (item.getItemId() == R.id.connect_item){ //Connect item			
			//If thread already running cancel and start new one
			if(connectNXTThread != null)
				connectNXTThread.cancel(true);
			connectNXTThread = new ConnectNXT_AsyncTask().execute();
		} else if (item.getItemId() == R.id.test_item){ //Connect item			
			Intent i = new Intent(ControllerGUI.this, TestTouchPad.class);
			startActivity(i);
		}

		return true;
	}

	//Out update console method
	public void updateConsole(){
		if (consoleText.size() > 13){
			consoleText.remove(0);
		}
		//Draws the xonsoleText array of string into the listview
		console.setAdapter(new ArrayAdapter<String>(this,R.layout.small_text_listview, consoleText));
	}

	//Connect NXT Thread
	private class ConnectNXT_AsyncTask extends AsyncTask<URL, Integer, Long> {

		//This method is called on pre execution of thread
		@Override
		protected void onPreExecute() {
			//A progress dialog is shown while connecting
			consoleText.add("Connecting to NXT: " + btComm.getNXT_MACAdress());
			progressDialog = ProgressDialog.show(ControllerGUI.this, "","Connecting. Please wait...", true);
			progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					consoleText.add("Connection Canceled");
					updateConsole();

					connectNXTThread.cancel(true);	
				}
			});
			progressDialog.setCancelable(true);
			progressDialog.show();
			updateConsole();
		}

		//The main thread execution
		@Override
		protected Long doInBackground(URL... params) {
			connectionMade = btComm.connectToNXTs();

			return null;
		}

		//This is called once the thread has finished (with or without error)
		@Override
		protected void onPostExecute(Long result) {
			if(connectionMade){
				consoleText.add("Connection Sucessful");
			} else {
				consoleText.add("Connection Unsucessful, try again");
			}

			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			updateConsole();

		}
	}

	//Send message to NXT thread
	private class SendMSG_AsyncTask extends AsyncTask<URL, Integer, Long> {

		@Override
		protected Long doInBackground(URL... params) {
			if(connectionMade){
				try {
					btComm.writeMessage(msg);
				} catch (Exception e) {

				}
			}
			return null;
		}
	}

	//Send/set speed to NXT thread
	private class SetSpd_AsyncTask extends AsyncTask<URL, Integer, Long>{

		@Override
		protected Long doInBackground(URL... params) {

			Log.d("DEBUG","Starting Speed Set");
			try {
				btComm.writeMessage(BT_Comm.SET_MOTOR_SPD);
				btComm.writeMessage(motorSpd);
				consoleText.add("Speed updated: " + motorSpd*9 + "rpm");
			} catch (Exception e) {
				Log.d("DEBUG", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Long result) {
			updateConsole();
		}
	}

	//Get data thread (Ultrasonic data)
	private class DataReciver_AsyncTask extends AsyncTask<URL, Integer, Long>{
		private int rmsg = -1;

		@Override
		protected Long doInBackground(URL... params) {

			while (true){

				if(connectionMade){
					try{
						rmsg = btComm.readMessage();
						Log.d("DEBUG","Ultrasonic data: " + rmsg);
						publishProgress(); //Calls onProgressUpdate
						Thread.sleep(100);
					} catch (Exception e) {
						Log.d("DEBUG", e.getMessage());
					}
				}
			}
		}

		//onProgress update handles UI updates (Outer threads cant)
		@Override
		protected void onProgressUpdate(Integer... progress) {
			if(rmsg <= OBSTACLE_WARNING_VALUE && rmsg != -1){
				consoleText.add("WARNING: Obstacle proximity: ~" + rmsg +"cm");

			} else if (consoleText.get(consoleText.size() - 1).substring(0, 7).equals("WARNING")){
				consoleText.add("Obstacle no longer a danger");
			}

			updateConsole();
		}

	}

}
