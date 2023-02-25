import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final int INCOMING_BYTES = 12; // We know beforehand how many bytes we should receive each read cycle. In this, we should only receive one byte for each RGB value.
    private static SerialPort m_arduino;
    private static RGB m_rgbValues = new RGB(0, 0, 0);
    private static Object mutexObject;
    private final static String ARDUINO_PORT = "COM6";

    public static void main(String[] args) throws InterruptedException {
        m_arduino = SerialPort.getCommPort(ARDUINO_PORT);
        mutexObject = new Object();

        MainWindow mainWindow = new MainWindow();
        mainWindow.setContentPane(mainWindow.mainPanel);
        mainWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setTitle("A Color Sensor");
        mainWindow.setVisible(true);
        System.out.println(m_arduino.getDescriptivePortName());

        while(true) {
            m_arduino.openPort();
            if(m_arduino.bytesAvailable() >= 12) {
                byte[] incomingBytes = new byte[INCOMING_BYTES];
                m_arduino.readBytes(incomingBytes, INCOMING_BYTES);
                int green = (incomingBytes[3]&0xff)|((incomingBytes[4]&0xff)<<8);
                int blue = (incomingBytes[6]&0xff)|((incomingBytes[7]&0xff)<<8);
                int red = (incomingBytes[9]&0xff)|(((incomingBytes[10]&0xff)<<8));

                if(red > blue && red > green && red < 210) {
                    red += 75;
                }

                synchronized(mutexObject) {
                    m_rgbValues = new RGB(red, green, blue);
                }

            }
            System.out.print("RED: ");
            System.out.println(getRGBValues().R);
            System.out.print("GREEN: ");
            System.out.println(getRGBValues().G);
            System.out.print("BLUE: ");
            System.out.println(getRGBValues().B);

            if(getRGBValues().R >= 255 || getRGBValues().G >= 255 || getRGBValues().B >= 255) {
                int scale = Math.max(getRGBValues().R, Math.max(getRGBValues().G, getRGBValues().B));
                int nR = (getRGBValues().R/scale * 254);
                int nG = (getRGBValues().G/scale * 254);
                int nB = (getRGBValues().B/scale * 254);
                mainWindow.rgbValues.setText("RED: " + Integer.toString(nR) + " | GREEN: " + Integer.toString(nG) + " | BLUE: " + Integer.toString(nB));
                mainWindow.readColor.setBackground(new Color(nR, nG, nB));
                continue;
            }
            mainWindow.readColor.setBackground(new Color(getRGBValues().R, getRGBValues().G, getRGBValues().B));
            mainWindow.rgbValues.setText("RED: " + Integer.toString(getRGBValues().R) + " | GREEN: " + Integer.toString(getRGBValues().G) + " | BLUE: " + Integer.toString(getRGBValues().B));

        }


    }

    public static RGB getRGBValues() {
        synchronized(mutexObject) {
            return m_rgbValues;
        }
    }

    public static class RGB {
        public int R = 0;
        public int G = 0;
        public int B = 0;

        public RGB(int iR, int iG, int iB) {
            R = iR;
            G = iG;
            B = iB;
        }
    }
}
