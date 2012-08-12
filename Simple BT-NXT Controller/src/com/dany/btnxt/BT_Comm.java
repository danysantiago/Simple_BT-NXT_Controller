package com.dany.btnxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BT_Comm {
	
	//Our Comm Dialect
	public static final byte MOVE_FOWARD = 10;
	public static final byte MOVE_FOWARD_LEFT = 11;
	public static final byte MOVE_FOWARD_RIGHT = 12;
    public static final byte ROTATE_LEFT = 13;
    public static final byte ROTATE_RIGHT = 14;
    public static final byte MOVE_BACKWARD = 15;
    public static final byte MOVE_BACKWARD_LEFT = 16;
    public static final byte MOVE_BACKWARD_RIGHT = 17;
    public static final byte STOP = 18;
    
    public static final byte SET_MOTOR_SPD = 19;
    
    public static final byte REQUEST_ULTRA_DATA = 20;
    
    public static final byte CLOSE_COMM = 21;
    
    public static final byte CUSTOM_MOVE = 25;

	//target NXT MAC Address
	private String NXT_MACAddress = "00:16:53:0A:E5:FF";

	private BluetoothAdapter btLocalAdapter;
	private BluetoothSocket btSocket;
	private BluetoothDevice btDevice_NXT;
	private boolean connectionMade = false;
	
	public String getNXT_MACAdress(){
		return NXT_MACAddress;
	}
	
	public boolean getConnectionMade(){
		return connectionMade;
	}
	
	//gets the Bluetooth Adapter
	public BluetoothAdapter getAdapterBT(){
		return btLocalAdapter;
	}

	//sets the Bluetooth Adapter
	public void setAdapterBT(BluetoothAdapter adapter){
		btLocalAdapter = adapter;
	}

	//connect to both NXTs
	public boolean connectToNXTs(){

		//get the BluetoothDevice of the NXT
		btDevice_NXT = btLocalAdapter.getRemoteDevice(NXT_MACAddress);

		//try to connect to the nxt
		try {
			//Start RFCOMM with UUID: Bluetooth Serial Port Profile (SPP)
			btSocket = btDevice_NXT.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));

			btSocket.connect();

			connectionMade = true;

		} catch (Exception e) {
			Log.d("DEBUG","BT Comm Error:: " + e.getMessage());
			connectionMade = false;
		}
		return connectionMade;

	}
	
	//Closes connection in socket
	public void closeConnection(){
		if (btSocket != null)
			
			try {
				btSocket.close();
			} catch (IOException e) {
				Log.d("DEBUG","BT Comm Error:: " + e.getMessage());
			}
	}
	
	//Write message to NXT
	public void writeMessage(byte msg){
		OutputStream mmOutStream;
		
		try{
			mmOutStream = btSocket.getOutputStream();
			mmOutStream.write(msg);
			mmOutStream.flush();
		} catch (Exception e){
			Log.d("DEBUG","BT Comm Error:: " + e.getMessage());
		}
	}
	
	//Read message from NXT
	public int readMessage(){
		InputStream mmInputStream;
		InputStreamReader mmInputStreamReader;
		int msg;
		
		try {
			mmInputStream = btSocket.getInputStream();
			mmInputStreamReader = new InputStreamReader(mmInputStream);
			msg = mmInputStreamReader.read();
			return msg;
		} catch (Exception e){
			Log.d("DEBUG","BT Comm Error: " + e.getMessage());
			return -1;
		}
	}

}
