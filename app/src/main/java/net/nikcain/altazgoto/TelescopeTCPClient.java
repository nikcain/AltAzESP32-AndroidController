package net.nikcain.altazgoto;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelescopeTCPClient {
    public static final String LOG_TAG = "TelescopeTCPClient";
    String addr = "http://192.168.4.1";
    boolean isConnected = false;
    String status = "";
    AppViewModel model;

    public TelescopeTCPClient(AppViewModel _model) {
        model = _model;
    }

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
                    Log.e(LOG_TAG, "SendHTTPPOST1: "+ e.getMessage());
                    isConnected = false;
                    model.setDebugText("no connection");
                    return;
                }
                try {
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestProperty("Accept-Encoding", "identity");

                    urlConnection.connect();
                    try(OutputStream os = urlConnection.getOutputStream()) {
                        byte[] input = jsonstring.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    } catch (IOException e) {
                        isConnected = false;
                        Log.e(LOG_TAG, "get output stream: "+ e.getMessage());
                        model.setDebugText("error on send command");
                        throw new RuntimeException(e);
                    }

                    if (urlConnection.getResponseCode() == 200) { isConnected = true; }
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
                    Log.e(LOG_TAG, "SendHTTPPOST2: "+ i.getMessage());
                    //model.setDebugText("SendHTTPPOST2: "+ i.getMessage());
                    //isConnected = false;
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "runtime: "+ e.getMessage());
                    model.setDebugText("runtime: "+ e.getMessage());
                    isConnected = false;
                    //throw new RuntimeException(e);
                    return;
                }
                finally{
                    urlConnection.disconnect();
                    //isConnected = false;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        status = response.toString();
                        try {
                            if (!status.equals("")) {
                                JSONObject jObject = new JSONObject(status);
                                model.getUiState().getValue().tracking = jObject.getBoolean("Tracking");
                                model.getUiState().getValue().currentAlt = jObject.getDouble("currentAlt");
                                model.getUiState().getValue().currentAz = jObject.getDouble("currentAz");

                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
            }
        });

    }
    void SendTarget(calibrationstars target)
    {
        String js = "{\"messageType\": \"SetTarget\",\"message\": ";
        js += "{\"DEC\": "+ target.dec +",\"RA\": "+ target.ra +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void SendTarget(targets target)
    {
        String js = "{\"messageType\": \"SetTarget\",\"message\": ";
        js += "{\"DEC\": "+ target.dec +",\"RA\": "+ target.ra +"}";
        js += "}";
        SendHTTPPOST(js);
    }
    void SendAltAzTarget(double alt, double az)
    {
        String js = "{\"messageType\": \"SetAltAz\",\"message\": ";
        js += "{\"ALT\": "+ alt +",\"AZ\": "+ az +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void SetTracking(boolean tracking)
    {
        String js = "{\"messageType\": \"SetTracking\",\"message\": ";
        js += "{\"Tracking\": "+ tracking +"}";
        js += "}";
        Log.i(LOG_TAG, "settracking " + js);
        SendHTTPPOST(js);
    }

    void Reset()
    {
        String js = "{\"messageType\": \"Reset\"}";
        SendHTTPPOST(js);
    }

    void Stop()
    {
        String js = "{\"messageType\": \"Stop\"}";
        SendHTTPPOST(js);
    }
    void Move(direction dir)
    {
        String js = "{\"messageType\": \"Move\",\"message\": ";
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

    void GetStatus()
    {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");

        String js = "{";
        js += "\"year\":" + Calendar.getInstance(timeZone).get(Calendar.YEAR) + ",";
        js += "\"month\":" + (1 + Calendar.getInstance(timeZone).get(Calendar.MONTH)) + ",";
        js += "\"day\":" + Calendar.getInstance(timeZone).get(Calendar.DAY_OF_MONTH) + ",";
        js += "\"hour\":" + Calendar.getInstance(timeZone).get(Calendar.HOUR_OF_DAY) + ",";
        js += "\"minutes\":" + Calendar.getInstance(timeZone).get(Calendar.MINUTE) + ",";
        js += "\"seconds\":" + Calendar.getInstance(timeZone).get(Calendar.SECOND) + "}";
        SendHTTPPOST(js);
    }
}
