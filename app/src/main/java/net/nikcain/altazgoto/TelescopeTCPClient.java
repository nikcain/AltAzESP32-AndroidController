package net.nikcain.altazgoto;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelescopeTCPClient {
    public static final String LOG_TAG = "TelescopeTCPClient";
    String addr = "http://192.168.1.222";

    String status = "";
    enum direction {
        up,
        down,
        left,
        right
    }

    void SendHTTPPOST(String jsonstring)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                StringBuilder response = new StringBuilder();
                //Background work here

                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(addr);
                    urlConnection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "SendHTTPPOST: "+ e.getMessage());
                    return;// "";
                }
                try {
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Accept-Encoding", "identity");

                    try(OutputStream os = urlConnection.getOutputStream()) {
                        byte[] input = jsonstring.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "get output stream: "+ e.getMessage());
                        throw new RuntimeException(e);
                    }

                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.i(LOG_TAG, response.toString());
                    }
                }
                catch(IOException i)
                {
                    Log.e(LOG_TAG, "SendHTTPPOST: "+ i.getMessage());
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "runtime: "+ e.getMessage());
                    throw new RuntimeException(e);
                }
                finally{
                    urlConnection.disconnect();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //UI Thread work here
                        status = response.toString();
                    }
                });
            }
        });

    }
    void SendTarget(targets target)
    {
        String js = "{\"messagetype\": \"SetTarget\",\"message\": ";
        js += "{\"DEC\": "+ target.dec +",\"RA\": "+ target.ra +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void SetCalibration(boolean setting)
    {
        String js = "{\"messagetype\": \"SetCalibration\",\"message\": ";
        js += "{\"Calibration\": "+ setting +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void SetTracking(boolean tracking)
    {
        String js = "{\"messagetype\": \"SetTracking\",\"message\": ";
        js += "{\"Tracking\": "+ tracking +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void Reset()
    {
        String js = "{\"messagetype\": \"Reset\"}";
        SendHTTPPOST(js);
    }

    void Stop()
    {
        String js = "{\"messagetype\": \"Stop\"}";
        SendHTTPPOST(js);
    }
    void Move(direction dir)
    {
        String js = "{\"messagetype\": \"Move\",\"message\": ";
        js += "{\"Move\": \"";
        switch(dir)
        {
            case up:
                js += "up";
                break;
            case down:
                js += "down";
                break;
            case left:
                js += "left";
                break;
            case right:
                js += "right";
                break;
        }
        js += "\"}";
        js += "}";
        SendHTTPPOST(js);
    }
}
