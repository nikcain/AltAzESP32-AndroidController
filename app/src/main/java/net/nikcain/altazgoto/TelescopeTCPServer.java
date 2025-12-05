package net.nikcain.altazgoto;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class SocketHandler extends Thread {

    public static final String LOG_TAG = "TelescopeTCPServer";
    public static final int BUFFER_SIZE = 2048;
    private Socket socket;
    AppViewModel model;
    SocketHandler(Socket cs, AppViewModel md) { socket = cs; model = md;}

    private BufferedReader in = null;

    public void run()
    {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int charsRead = 0;
        char[] buffer = new char[BUFFER_SIZE];

        while (true) {
            try {
                if (!((charsRead = in.read(buffer)) != -1)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            StringBuilder message = new StringBuilder();
            message.append(new String(buffer).substring(0, charsRead));
            Log.e(LOG_TAG, "recv : "+ message);

            model.setDebugText(String.valueOf(message));
        }
    }
}

public class TelescopeTCPServer {
    AppViewModel model;

    public TelescopeTCPServer(AppViewModel _model){
        model = _model;
    }

    void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                    ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(5432);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (;;) {
                    SocketHandler socketHander = null;
                    try {
                        socketHander = new SocketHandler(serverSocket.accept(), model);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    socketHander.start();
                }
            }
        });
/*
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
*/
    }
}
