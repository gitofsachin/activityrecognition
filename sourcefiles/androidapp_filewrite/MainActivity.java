/************************************************************
 ACTIVITY MONITORING USING SMARTPHONE
 SACHIN S AND SAJNA REMI CLERE
 Indian Institute of Science, Bangalore

 Logs gyroscope and linear accelerometer data on tcp request
 and sends back to the request device through TCP
 ************************************************************/

package com.example.sachin.activitymonitor;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity
{
    public static int samplesperrequest=128;
    TextView info, infoip, msg;
    String message = "";
    ServerSocket serverSocket;


    static int numberofreadings;
    static int linaccnumberofreadings;
    static int gyrodone;
    static int linaccdone;
    static float [][]gyro=new float[samplesperrequest][3];
    static float [][]linacc=new float[samplesperrequest][3];
    static long []gyrotime= new long[samplesperrequest];
    static long []linacctime= new long[samplesperrequest];

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);

        FileOperations fop= new FileOperations();
        fop.write("Accelerometer","");
        fop.write("Gyroscope","");

        infoip.setText(getIpAddress());
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (serverSocket != null)
        {
            try
            {
                serverSocket.close();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread implements SensorEventListener
    {

        private SensorManager sensorManager;
        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            float [] values= event.values;
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE)
            {
                if(gyrodone==0)
                {
                    gyro[numberofreadings][0] = values[0];
                    gyro[numberofreadings][1] = values[1];
                    gyro[numberofreadings][2] = values[2];
                    gyrotime[numberofreadings] = event.timestamp;
                    numberofreadings = numberofreadings + 1;
                }
                if (numberofreadings == samplesperrequest)
                {
                    gyrodone=1;
                    if(linaccdone==1)//other sensors done with capturing
                    {
                        //sensorManager.unregisterListener(this);
                    }
                }

                FileOperations fop= new FileOperations();
                fop.append("Gyroscope",event.timestamp+" "+values[0]+" "+values[1]+" "+values[2]+",\n");
            }
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)
            {
                if(linaccdone==0)
                {
                    linacc[linaccnumberofreadings][0] = values[0];
                    linacc[linaccnumberofreadings][1] = values[1];
                    linacc[linaccnumberofreadings][2] = values[2];
                    linacctime[linaccnumberofreadings] = event.timestamp;
                    linaccnumberofreadings = linaccnumberofreadings + 1;
                }
                if (linaccnumberofreadings == samplesperrequest)
                {
                    linaccdone=1;
                    if(gyrodone==1)//other sensors done with capturing
                    {
                        //sensorManager.unregisterListener(this);
                    }
                }
                FileOperations fop= new FileOperations();
                fop.append("Accelerometer",event.timestamp+" "+values[0]+" "+values[1]+" "+values[2]+",\n");
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
        @Override
        public void run()
        {
            try
            {
                numberofreadings=0;
                gyrodone=1;
                linaccnumberofreadings=0;
                linaccdone=1;
                sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        SensorManager.SENSOR_DELAY_FASTEST);
                serverSocket = new ServerSocket(SocketServerPORT);
                MainActivity.this.runOnUiThread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        info.setText("Opened port : "
                                + serverSocket.getLocalPort());
                    }
                });

                while (true)
                {
                    Socket socket = serverSocket.accept();
                    count++;
                    message += "Request #" + count + " from " + socket.getInetAddress()
                            + ":" + socket.getPort() + "\n";

                    MainActivity.this.runOnUiThread(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            msg.setText(message);
                        }
                    });

                    SocketServerReplyThread socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();

                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private class SocketServerReplyThread extends Thread
    {
        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c)
        {
            hostThreadSocket = socket;
            cnt = c;
        }


        @Override
        public void run()
        {
            OutputStream outputStream;
            String userReply = "Request #" + cnt;
            String totalmsg;
            String msgReply;
            String msgReplylinacc;


            try
            {
                numberofreadings=0;
                gyrodone=0;
                linaccnumberofreadings=0;
                linaccdone=0;

                while(numberofreadings<samplesperrequest||linaccnumberofreadings<samplesperrequest)
                {
                    //Wait for sample capture
                }
                msgReply="";
                msgReplylinacc="";
                for(int i=0;i<samplesperrequest;i++)
                {
                    msgReply=msgReply+Long.toString(gyrotime[i])+" "+Arrays.toString(gyro[i]).replace("[", "").replace("]", "").replace(",","")+",\n";
                    msgReplylinacc=msgReplylinacc+Long.toString(linacctime[i])+" "+Arrays.toString(linacc[i]).replace("[", "").replace("]", "").replace(",","")+",\n";
                }
                Log.d("Length:","->"+msgReply.length());
                Log.d("Linacclength:", "->" + msgReplylinacc.length());
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);

                int length=msgReply.length()+msgReplylinacc.length();
                msgReply="size"+length+"sizend"+msgReply;

                //Uncomment below for viewing gyroscope and accelerometer values in debug window
                // Log.d("values",msgReply);
                //Log.d("Linaccval",msgReplylinacc);

                totalmsg=msgReply+msgReplylinacc;
                printStream.print(totalmsg);
                printStream.close();


                message += "Processed "+ samplesperrequest + " samples on " + userReply + "\n\n";

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        msg.setText(message);
                    }
                });

            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }

            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msg.setText(message);
                }
            });
        }

    }

    private String getIpAddress()
    {
        String ip = "";
        try
        {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements())
            {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements())
                {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress())
                    {
                        ip += "Device IP Address: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}

