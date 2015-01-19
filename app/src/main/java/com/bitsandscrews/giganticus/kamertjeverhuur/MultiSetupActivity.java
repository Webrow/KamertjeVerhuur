package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MultiSetupActivity extends Activity {

    private String roomName;
    private EditText room;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button checkroomname;
    private int amountplayers;
    private Socket socket;
    private Boolean next_activity;
    private Boolean validRoom;
    private BufferedReader infromServer;
    private String recvBuffer;
    private Boolean startGame;
    private String sendBuffer;
    private Boolean readySendBuffer;
    private Boolean readyWarningBuffer;
    private String warningBuffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multisetup);
        this.amountplayers = 0;
        this.next_activity = false;
        this.validRoom = false;

        new Thread(new ClientThread()).start();
        new Thread(new ListenThread()).start();

        this.room = (EditText) findViewById(R.id.roomname);
        this.roomName = room.getText().toString();

        this.btn2 = (Button) findViewById(R.id.p2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setBackgroundColor(Color.parseColor("8EFEB9"));
                amountplayers = 2;
            }
        });

        this.btn3 = (Button) findViewById(R.id.p3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setBackgroundColor(Color.parseColor("8EFEB9"));
                amountplayers = 3;
            }
        });

        this.btn4 = (Button) findViewById(R.id.p4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn2.setBackgroundColor(Color.parseColor("8EFEB9"));
                amountplayers = 4;
            }
        });

        this.checkroomname = (Button) findViewById(R.id.checkroomname);
        checkroomname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBuffer = "101" +roomName +"-" +amountplayers;
                readySendBuffer = true;
            }
        });

        Button join = (Button) findViewById(R.id.joincreate);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MultiGridView.class);
                if (roomName == ""){
                    room.setHint("Fill in Roomname!");
                }
                if (startGame){
                    if (validRoom){
                        intent.putExtra("roomname", roomName);
                        next_activity = true;
                        //startActivity(intent);
                    }
                }
            }
        });
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            if (socket.isBound()) {
                try {
                    if ((sendBuffer != null) && readySendBuffer) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                        out.println(sendBuffer);
                        sendBuffer = "";
                        readySendBuffer = false;
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            else {
                try {
                    socket = new Socket("46.4.112.245", 8097);
                    room.setBackgroundColor(Color.RED);
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    class ListenThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (socket.isBound()) {
                    try {
                        infromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()) );
                        if (infromServer.readLine() != null) {
                            if (recvBuffer == "")
                            recvBuffer = infromServer.readLine();
                            message_handler(recvBuffer);
                        }
                    } catch (UnknownHostException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    android.os.SystemClock.sleep(5000);
                }
            }
        }
    }

    public void message_handler(String recvBuffer){
        if(recvBuffer.startsWith("100")){
            startGame = true;
            checkroomname.setText("Room created!");
            checkroomname.setBackgroundColor(Color.GREEN);
        }
        if(recvBuffer.startsWith("403")){
            warningBuffer = "Room Busy";
            readyWarningBuffer = true;
            new Thread(new WarningThread()).start();

        }
        if(recvBuffer.startsWith("404")){
            warningBuffer = "Select players!";
            readyWarningBuffer = true;
            new Thread(new WarningThread()).start();

        }
    }

    class WarningThread implements Runnable {
        public void run() {
            while(readyWarningBuffer){
                checkroomname.setBackgroundColor(Color.RED);
                checkroomname.setText(warningBuffer);
                readyWarningBuffer = false;
                android.os.SystemClock.sleep(5000);
                checkroomname.setBackgroundColor(Color.GREEN);
                checkroomname.setText("CHECK ROOMNAME");
            }
        }
    }
}
