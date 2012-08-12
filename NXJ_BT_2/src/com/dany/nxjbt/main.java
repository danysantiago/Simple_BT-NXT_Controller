package com.dany.nxjbt;

import lejos.nxt.*;

public class main {

    public static BT_Comm btComm = new BT_Comm();
    private static DataSenderThread dst;

    //Escape button listener
    public static void main(String[] args) throws InterruptedException {

        Button.ESCAPE.addButtonListener(new ButtonListener() {

            public void buttonPressed(Button b) {
            }

            public void buttonReleased(Button b) {
                System.exit(1);
            }
        });
        
        //Creates data sender thread
        dst = new DataSenderThread();
        
        //LCD class is used to draw in the NTX's LCD
        LCD.clear();
        LCD.drawString("Waiting for Comm", 0, 0);
        LCD.drawString("My MAC Address:", 0, 2);
        LCD.drawString(btComm.getMacAddress(), 0, 3);
        LCD.refresh();

        //Open BT and wait for connection
        btComm.openBTConnection();
  
        LCD.clear();
        LCD.drawString("Connected", 0, 0);
        LCD.refresh();
        
        //Start data sender thread (Ultrasonic data)
        dst.start();

        //loop to read data as long as there is a connection  
        while (btComm.getConnectionMade()) {

            int msg = -1;
            
            //Reads data
            msg = btComm.readMessage();
            
            //Displays info in LCD
            LCD.clear();
            LCD.drawString("Message Recived:", 0, 0);
            LCD.drawInt(msg, 0, 1);
            LCD.drawString("Motor Speed:", 0, 3);
            LCD.drawString(Motor.A.getSpeed() + " - " + Motor.B.getSpeed(), 0, 4);
            LCD.drawString("Ultrasonic data:", 0, 6);
        }

        LCD.clear();
        LCD.drawString("Closing...", 0, 0);
        LCD.refresh();

        Thread.sleep(1000);

        System.exit(1);
    }
}
