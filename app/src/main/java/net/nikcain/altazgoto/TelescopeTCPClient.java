package net.nikcain.altazgoto;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class TelescopeTCPClient {
    public static final String LOG_TAG = "TelescopeTCPClient";
    String addr = "192.168.1.102";
    int port = 22222;
    enum direction {
        up,
        down,
        left,
        right
    }

    String SendHTTPPOST(String jsonstring)
    {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(addr);
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e(LOG_TAG, "SendHTTPPOST: "+ e.getMessage());
            return "";
        }
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            try(OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = jsonstring.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        }
        catch(IOException i)
        {
            Log.e(LOG_TAG, "SendHTTPPOST: "+ i.getMessage());
        }
        finally{
            urlConnection.disconnect();
        }
        return "";
    }
    void SendTarget(targets target)
    {
        String js = "{\"DEC\": "+String.valueOf(target.dec)+",\"RS\": "+String.valueOf(target.ra)+"}";
        String resp = SendHTTPPOST(js);
    }

    void SetCalibration(boolean setting)
    {
        String js = "{\"Calibration\": "+ setting +"}";
        String resp = SendHTTPPOST(js);
    }

    void SetTracking(boolean tracking)
    {
        String js = "{\"Tracking\": "+ tracking +"}";
        String resp = SendHTTPPOST(js);
    }

    void Move(direction dir)
    {
        String js="{\"Move\": \"";
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
    }
}
