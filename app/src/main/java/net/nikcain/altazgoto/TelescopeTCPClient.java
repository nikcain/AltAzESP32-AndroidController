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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelescopeTCPClient {
    public static final String LOG_TAG = "TelescopeTCPClient";
    String addr = "http://192.168.1.33";
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
                    Log.e(LOG_TAG, "SendHTTPPOST: "+ e.getMessage());
                    isConnected = false;
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
                        isConnected = false;
                        Log.e(LOG_TAG, "get output stream: "+ e.getMessage());
                        throw new RuntimeException(e);
                    }

                    isConnected = true;
                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.i(LOG_TAG, response.toString());
                    }
                    isConnected = true;
                }
                catch(IOException i)
                {
                    Log.e(LOG_TAG, "SendHTTPPOST: "+ i.getMessage());
                    //isConnected = false;
                } catch (RuntimeException e) {
                    Log.e(LOG_TAG, "runtime: "+ e.getMessage());
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
                        //UI Thread work here
                        //String statusjson = "{\"Tracking\":false,\"Calibrating\":false,\"DateTimeSet\":true, \"targetRA\":1.234,\"targetDEC\":5.6789,\"currentRA\":0.1111,\"currentDEC\":0.2222}";
                        status = response.toString();
                        try {
                            if (!status.equals("")) {
                                JSONObject jObject = new JSONObject(status);
                                model.getUiState().getValue().tracking = jObject.getBoolean("Tracking");
                                model.getUiState().getValue().calibrating = jObject.getBoolean("Calibrating");
                                model.getUiState().getValue().currentRA = jObject.getDouble("currentRA");
                                model.getUiState().getValue().currentDEC = jObject.getDouble("currentDEC");
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
            }
        });

    }
    void SendTarget(targets target)
    {
        String js = "{\"messageType\": \"SetTarget\",\"message\": ";
        js += "{\"DEC\": "+ target.dec +",\"RA\": "+ target.ra +"}";
        js += "}";
        SendHTTPPOST(js);
    }

    void SetCalibration(boolean setting)
    {
        String js = "{\"messageType\": \"SetCalibration\",\"message\": ";
        js += "{\"Calibration\": "+ setting +"}";
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
        String js = "{";
        js += "\"year\":" + Calendar.getInstance().get(Calendar.YEAR) + ",";
        js += "\"month\":" + Calendar.getInstance().get(Calendar.MONTH) + ",";
        js += "\"day\":" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ",";
        js += "\"hour\":" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ",";
        js += "\"minutes\":" + Calendar.getInstance().get(Calendar.MINUTE) + ",";
        js += "\"seconds\":" + Calendar.getInstance().get(Calendar.SECOND) + "}";
        SendHTTPPOST(js);
    }
}
