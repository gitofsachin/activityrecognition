package com.example.sachin.activitymonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.util.Log;

public class FileOperations
{
    public FileOperations()
    {
    }

    public Boolean write(String fname, String fcontent)
    {
        try
        {
            String fpath = "/sdcard/"+fname+".txt";
            Log.d("Entering write","Success");
            File file = new File(fpath);
            // If file does not exists, then create it
            if (!file.exists())
            {
                file.createNewFile();
            }
            else
            {
                file.delete();
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean append(String fname, String fcontent)
    {
        try
        {
            String fpath = "/sdcard/"+fname+".txt";
            File file = new File(fpath);
            // If file does not exists, then create it
            if (!file.exists())
            {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fcontent);
            bw.close();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public String read(String fname)
    {
        BufferedReader br = null;
        String response = null;
        try
        {
            StringBuffer output = new StringBuffer();
            //StringBuffer firstline = new StringBuffer();
            String fpath = "/sdcard/"+fname+".txt";
            br = new BufferedReader(new FileReader(fpath));
            String line = "";
            line=br.readLine();
            line="";
            while ((line = br.readLine()) != null)
            {
                output.append(line +"\n");
            }
            response = output.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    public void moveFile() throws IOException {
        File newFile = new File("/sdcard/SANA", "Accelero.txt");
        File file=new File("/sdcard/Accelerometer.txt");
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }
}
