package com.dany.nxjbt;

import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

//Data sender thread used to send Uktrasonic data
public class DataSenderThread extends Thread {

    private int data = -1;
    //Get the sensor at port 1
    private UltrasonicSensor usSensor = new UltrasonicSensor(SensorPort.S1);
    
    //Constructor
    public DataSenderThread() {
    }

    @Override
    public void run() {
        
        
        //Continious loop as long as a connection is made
        while (main.btComm.getConnectionMade()) {
            //Gets the sensor data
            data = usSensor.getDistance();
            //Send data
            main.btComm.writeMessage(data);
            //Display it on LCD
            LCD.drawString("" + data + " ", 0, 7);
            LCD.refresh();
            //Sleep thread
            try {
                Thread.sleep(300);
            } catch (Exception e) {
            }
        }
    }
}
