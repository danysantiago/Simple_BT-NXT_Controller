package com.dany.nxjbt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lejos.nxt.Motor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.nxt.comm.NXTConnection;

//Bluetooth communication class to handle bt stuff
public class BT_Comm {

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
    
    public static final byte SEND_ULTRA_DATA = 20;
    
    public static final byte CLOSE_COMM = 21;
    
    public static final byte CUSTOM_MOVE = 25;
    
    //Stores local MAC Address
    private String localAddress = Bluetooth.getLocalAddress();
    
    private BTConnection btc;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    private Boolean connectionMade = false;
    
    private int motorSpeed = 360;

    public String getMacAddress() {
        return localAddress;
    }
    
    //Opens BT Connection
    public boolean openBTConnection() {

        try {
            //Connection is open and is set to lisent until connected
            btc = Bluetooth.waitForConnection();
            btc.setIOMode(NXTConnection.RAW);
            
            //Opens the input and output streams
            dis = btc.openDataInputStream();
            dos = btc.openDataOutputStream();

            connectionMade = true;
        } catch (Exception e) {
            //Error handler
            System.out.println(e.getMessage());
            connectionMade = false;
            return false;
        }

        return true;
    }
    
    //Closes BT Connection
    public void closeBTConnection() {
        try {
            dis.close();
            dos.close();
            connectionMade = false;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    //Read msg
    public int readMessage() {
        byte rMSG = -1;
        try {
            //Read
            rMSG = dis.readByte();
            //Decode
            decodeMSG(rMSG);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            connectionMade = false;
            return -1;
        }

        return rMSG; //returns msg read
    }
    
    //Write msg
    public boolean writeMessage(int msg) {

        try {
            //Write
            dos.write(msg);
            //flush to stream
            dos.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }
    
    //Get connection mode
    public boolean getConnectionMade() {
        return connectionMade;
    }
    
    //Decode msg method
    public void decodeMSG(Byte msg) throws IOException {
        //Motor Movement Functions
//        int currentSpeedA = Motor.A.getSpeed();
//        int currentSpeedB = Motor.B.getSpeed();
        
        if (msg == MOVE_FOWARD) {
            Motor.A.backward();
            Motor.B.backward();
        } else if (msg == MOVE_FOWARD_LEFT) { 
            Motor.A.backward();
            Motor.B.setSpeed(motorSpeed/2);
            Motor.B.backward();
            
        } else if (msg == MOVE_FOWARD_RIGHT) {
            Motor.A.setSpeed(motorSpeed/2);
            Motor.A.backward();
            Motor.B.backward();
            
        } else if (msg == ROTATE_LEFT) {
            Motor.A.backward();
            Motor.B.forward();
        } else if (msg == ROTATE_RIGHT) {
            Motor.A.forward();
            Motor.B.backward();
        } else if (msg == MOVE_BACKWARD) {
            Motor.A.forward();
            Motor.B.forward();
        } else if (msg == STOP) {
            Motor.A.setSpeed(motorSpeed);
            Motor.B.setSpeed(motorSpeed);
            Motor.A.stop();
            Motor.B.stop();
        }

        //Motor set Speed Function
        if (msg == SET_MOTOR_SPD) {

            Byte rMsg = 40;

            rMsg = dis.readByte();

            int spd = 9 * rMsg;
            motorSpeed = spd;

            Motor.A.setSpeed(spd);
            Motor.B.setSpeed(spd);

        }
        
        if (msg == CUSTOM_MOVE) {
            int motorB = dis.read();
            int motorA = dis.read();
            Motor.A.setSpeed(motorA*9);
            Motor.B.setSpeed(motorB*9);
            Motor.A.backward();
            Motor.B.backward();
        }
        
        //Connection close msg
        if (msg == CLOSE_COMM) {
            closeBTConnection();
        }
    }
}
