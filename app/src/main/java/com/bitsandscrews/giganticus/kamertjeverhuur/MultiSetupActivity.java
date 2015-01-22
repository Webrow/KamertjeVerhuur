package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MultiSetupActivity extends Activity {

    private Boolean mRecieved = false;
    private String roomName;
    private EditText room;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button join;
    private int amountplayers;
    private Socket socket = new Socket();
    private Boolean next_activity = false;
    private Boolean validRoom;
    private BufferedReader infromServer;
    private String recvBuffer;
    private Boolean startGame;
    private String sendBuffer;
    private Boolean readySendBuffer;
    private Boolean readyWarningBuffer;
    private String warningBuffer;
    private EditText nickField;
    private String nickname;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private Boolean mRun;
    private String mServerMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multisetup);
        this.amountplayers = 0;
        this.next_activity = false;
        this.validRoom = false;
        this.room = (EditText) findViewById(R.id.roomname);
        this.nickField = (EditText) findViewById(R.id.nickname);
        this.roomName = " ";
        this.nickname = " ";
        this.recvBuffer = "";
        this.mRun = false;

        new Thread(new ClientThread()).start();
        eventThread();

        room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                room.setBackgroundColor(Color.WHITE);
                join.setBackgroundColor(Color.parseColor("#499e6a"));
                join.setText("Check Roomname");
            }
        });

        this.btn2 = (Button) findViewById(R.id.p2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setBackgroundColor(Color.parseColor("#8EFEB9"));
                btn3.setBackgroundColor(Color.parseColor("#499e6a"));
                btn4.setBackgroundColor(Color.parseColor("#499e6a"));
                amountplayers = 2;
            }
        });

        this.btn3 = (Button) findViewById(R.id.p3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn3.setBackgroundColor(Color.parseColor("#8EFEB9"));
                btn2.setBackgroundColor(Color.parseColor("#499e6a"));
                btn4.setBackgroundColor(Color.parseColor("#499e6a"));
                amountplayers = 3;
            }
        });

        this.btn4 = (Button) findViewById(R.id.p4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn4.setBackgroundColor(Color.parseColor("#8EFEB9"));
                btn2.setBackgroundColor(Color.parseColor("#499e6a"));
                btn3.setBackgroundColor(Color.parseColor("#499e6a"));
                amountplayers = 4;
            }
        });

        this.join = (Button) findViewById(R.id.joincreate);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomName = room.getText().toString();
                nickname = nickField.getText().toString();
                sendBuffer = "101" +roomName +"-" +nickname +"-" +amountplayers;
                readySendBuffer = true;
                new Thread(new ClientThread()).start();
            }
        });
    }


    class ClientThread implements Runnable {
        public void run() {

            while (true && !next_activity) {
                mRun = true;

                try {
                    if(!socket.isConnected()) {
                        socket = new Socket("46.4.112.245", 8097);
                    }
                    //here you must put your computer's IP address.
                    //InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                    //Log.e("TCP Client", "C: Connecting...");

                    //create a socket to make the connection with the server
                    //Socket socket = new Socket(serverAddr, SERVER_PORT);

                    try {
                        Log.i("Debug", "inside try catch");
                        //sends the message to the server
                        mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        //receives the message which the server sends back
                        mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        // send login name
                        //sendMessage(Constants.LOGIN_NAME + PreferencesManager.getInstance().getUserName());
                        //sendMessage("Hi");
                        //in this while the client listens for the messages sent by the server

                        if ((sendBuffer != null) && readySendBuffer) {
                            mBufferOut.println(sendBuffer);
                            mBufferOut.flush();
                            sendBuffer = "";
                            readySendBuffer = false;
                        }

                        mServerMessage = mBufferIn.readLine();
                        if (mServerMessage != null) {
                            //call the method messageReceived from MyActivity class
                            mRecieved = true;
                            eventThread();
                        }

                        Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

                    } catch (Exception e) {

                        Log.e("TCP", "S: Error", e);

                    }

                } catch (Exception e) {

                    Log.e("TCP", "C: Error", e);

                }
            }
        }
    }

    private void eventThread() {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                if (mRecieved && !next_activity) {
                    message_handler(mServerMessage);
                    mRecieved = false;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    public void message_handler(String recvBuffer){

        if(recvBuffer.startsWith("100")){
            startGame = true;
            join.setText("Room created!");
            join.setBackgroundColor(Color.GREEN);
            gameLauncher(findViewById(R.id.roomname));
        }
        if(recvBuffer.startsWith("103")){
            startGame = true;
            join.setText("Joining session!");
            join.setBackgroundColor(Color.GREEN);
            this.amountplayers = Integer.parseInt(recvBuffer.substring(3));
            gameLauncher(findViewById(R.id.roomname));
        }
        if(recvBuffer.startsWith("403")){
            warningBuffer = "Room Busy";
            System.out.println(warningBuffer);
            readyWarningBuffer = true;
            warningThread();
        }
        if(recvBuffer.startsWith("404")){
            warningBuffer = "Select players!";
            readyWarningBuffer = true;
            warningThread();
        }
    }

    private void warningThread() {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                while (readyWarningBuffer && !next_activity) {
                    room.setBackgroundColor(Color.RED);
                    room.setText(warningBuffer);
                    readyWarningBuffer = false;
                    join.setBackgroundColor(Color.GREEN);
                    join.setText("CHECK ROOMNAME");
                    readyWarningBuffer = false;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    private void gameLauncher(View v){
        Intent intent = new Intent(v.getContext(), MultiplayerActivity.class);
        if (roomName == ""){
            room.setHint("Fill in Roomname!");
        }
        if (startGame){
            intent.putExtra("roomname", roomName);
            intent.putExtra("nickname", nickname);
            intent.putExtra("playercount", amountplayers);
            next_activity = true;
            startActivity(intent);
        }
    }
}
