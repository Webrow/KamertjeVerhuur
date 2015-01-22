package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rowan on 14-12-2014.
 */
public class MultiGridView extends View {

    private Display display;
    private Paint gridPaint = new Paint();
    private Paint dotPaint = new Paint();
    private Paint wallPaint = new Paint();
    Paint background = new Paint();
    public int colums = 6;
    private int rows = 6;
    private int height;
    private int width;
    public int startGridX;
    public int startGridY;
    public int wallsize = 150;
    private Canvas canvas = new Canvas();
    public Canvas edits;
    public Point[] gridpoints = new Point[(rows+1)*(colums+1)];
    public Point[] wallpoints = new Point[((rows-1)*(colums))+((rows)*(colums-1))];
    private Bitmap basebitmap;
    private Bitmap currentBitmap;
    private Player[] players;
    private int wallcolor;
    private int max_score;
    private String sendBuffer = null;
    private Boolean readySendBuffer = null;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;
    private Boolean mRun;
    private String mServerMessage;
    private Boolean mRecieved = false;
    private Socket gameConnection = new Socket();
    private Context context;
    private Boolean finished = false;
    private String nickname;
    private String roomname;
    private int currentplayer;
    private int myplayernumber;

    public MultiGridView(Context context, Player[] players, String nickname, String roomname) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.width = size.x;
        this.height = size.y;
        this.wallcolor = -11952534;
        this.context = context;
        this.roomname = roomname;
        this.nickname = nickname;

        this.startGridX = width / 10;
        this.startGridY = height / 6;

        this.players = players;
        dotPaint.setColor(Color.parseColor("#499e6a"));
        dotPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(Color.parseColor("#499e6a"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(12);

        background.setColor(Color.parseColor("#bce3cc"));
        background.setStyle(Paint.Style.FILL);
        System.out.println("Im here");
        this.basebitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.edits = new Canvas(basebitmap);
        createGrid(edits);
        System.out.println("Im here");
        new Thread(new ClientThread()).start();

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:{
                int endX = startGridX + (colums*wallsize);
                int endY = startGridY + (rows*wallsize);
                if ((event.getX() > startGridX) && (event.getX() < endX)){
                    if ((event.getY() > startGridY) && (event.getY() < endY)) {
                        String y = Float.toString(event.getY());
                        String x = Float.toString(event.getX());
                        String message = "600" + roomname + "-" + nickname + "-" + x + "|" + y;
                        sender_handler(message);
                    }
                }
            }
        }
        invalidate();
        return true;
    }

    public void drawTouch(float x, float y) {
        int currentplayer = 0;

        Boolean isHorizontal = false;
        for (int i = 0; i < wallpoints.length; i++) {
            int medianx = 1000;
            int mediany = 1000;
            if (wallpoints[i] != null){
                medianx = Math.abs((wallpoints[i].x - (int) x) / 2);
                mediany = Math.abs((wallpoints[i].y - (int) y) / 2);
            }
            if ((medianx < 25) && (mediany < 25)) {
                if((wallpoints[i].y - (wallsize/2) - startGridY) % wallsize == 0){
                    isHorizontal = true;
                    edits.drawLine(wallpoints[i].x, wallpoints[i].y - (wallsize / 2), wallpoints[i].x, wallpoints[i].y + (wallsize / 2), gridPaint);
                    int whatsquare = checkSquare(wallpoints[i], isHorizontal);
                    //links
                    if (whatsquare == 0) {
                        edits.drawRect(wallpoints[i].x - wallsize, wallpoints[i].y - (wallsize / 2), wallpoints[i].x, wallpoints[i].y + (wallsize / 2), players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - wallsize, wallpoints[i].y - (wallsize / 2), wallpoints[i].x, wallpoints[i].y + (wallsize / 2), gridPaint);
                        scoreHandler(players[currentplayer], 1);
                    }
                    //rechts
                    if (whatsquare == 1) {
                        edits.drawRect(wallpoints[i].x, wallpoints[i].y - (wallsize / 2), wallpoints[i].x + wallsize, wallpoints[i].y + (wallsize / 2), players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x, wallpoints[i].y - (wallsize / 2), wallpoints[i].x + wallsize, wallpoints[i].y + (wallsize / 2), gridPaint);
                        scoreHandler(players[currentplayer], 1);
                    }
                    if (whatsquare == 2) {
                        edits.drawRect(wallpoints[i].x, wallpoints[i].y - (wallsize / 2), wallpoints[i].x + wallsize, wallpoints[i].y + (wallsize / 2), players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x, wallpoints[i].y - (wallsize / 2), wallpoints[i].x + wallsize, wallpoints[i].y + (wallsize / 2), gridPaint);
                        edits.drawRect(wallpoints[i].x - wallsize, wallpoints[i].y - (wallsize / 2), wallpoints[i].x, wallpoints[i].y + (wallsize / 2), players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - wallsize, wallpoints[i].y - (wallsize / 2), wallpoints[i].x, wallpoints[i].y + (wallsize / 2), gridPaint);
                        scoreHandler(players[currentplayer], 2);
                    }
                    //geen
                    if (whatsquare == -1){
                        //TODO PINGBACK ik ben klaar
                        String message = "555" +roomname +"-" +nickname;
                        sender_handler(message);
                        //turnHandler(players[currentplayer]);
                    }
                    wallpoints[i] = null;
                    break;
                }
                if((wallpoints[i].x - (wallsize/2) - startGridX) % wallsize == 0){
                    isHorizontal = false;
                    edits.drawLine(wallpoints[i].x - (wallsize / 2), wallpoints[i].y, wallpoints[i].x + (wallsize / 2), wallpoints[i].y, gridPaint);
                    int whatsquare = checkSquare(wallpoints[i], isHorizontal);
                    //bottom
                    if (whatsquare == 0) {
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y, wallpoints[i].x + (wallsize / 2), wallpoints[i].y + wallsize, players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y, wallpoints[i].x + (wallsize / 2), wallpoints[i].y + wallsize, gridPaint);
                        scoreHandler(players[currentplayer], 1);
                    }
                    //top
                    if (whatsquare == 1) {
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y - wallsize, wallpoints[i].x + (wallsize / 2), wallpoints[i].y, players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y - wallsize, wallpoints[i].x + (wallsize / 2), wallpoints[i].y, gridPaint);
                        scoreHandler(players[currentplayer], 1);
                    }
                    if (whatsquare == 2) {
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y - wallsize, wallpoints[i].x + (wallsize / 2), wallpoints[i].y, players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y - wallsize, wallpoints[i].x + (wallsize / 2), wallpoints[i].y, gridPaint);
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y, wallpoints[i].x + (wallsize / 2), wallpoints[i].y + wallsize, players[currentplayer].paint);
                        edits.drawRect(wallpoints[i].x - (wallsize / 2), wallpoints[i].y, wallpoints[i].x + (wallsize / 2), wallpoints[i].y + wallsize, gridPaint);
                        scoreHandler(players[currentplayer], 2);
                    }
                    //geen
                    if (whatsquare == -1){
                        //TODO PINGBACK ik ben klaar
                        String message = "555" +roomname +"-" +nickname;
                        sender_handler(message);
                        //turnHandler(players[currentplayer]);
                    }
                    wallpoints[i] = null;
                    break;
                }
            }
        }
        if (max_score >= rows*colums){
            Context context = getContext();
            Intent i = new Intent(context, MainActivity.class);
            context.startActivity(i);
        }

        max_score = 0;
        for (int j = 0; j < players.length; j++){
            max_score += players[j].score;
            if (max_score == rows * colums){
                finishBattle();
                break;
            }
        }
    }

    public void finishBattle() {
        Player[] results;
        results = players;

        Arrays.sort(results, new PlayerComparator());
        Collections.reverse(Arrays.asList(results));

        int winner = 0;
        int highscore = 0;
        for (int k = 0; k < results.length; k++){
            while (highscore <= results[k].score){
                highscore = results[k].score;
                winner ++;
                break;
            }
        }
        winner = winner - 1;
        int i;
        for ( i = 0; i <= winner; i++){
            String text1 = results[i].getPlayerName() + " wins with: " +results[i].score +" PTS!";
            edits.drawText(text1, (width / 5) + 50, startGridY*4 + 10 + (100 * i), results[i].painttext);
        }

        int linespacing = startGridY*4 + 10 + (i * 100);
        int counter = 0;
        for ( int l = 0; l < results.length; l++){
            if (results[l].score < results[winner].score){
                edits.drawText(results[l].getScoreString(), (width / 8) + 80, linespacing + (100 * counter), results[l].painttext);
                counter++;
            }
        }
    }

    class PlayerComparator implements Comparator<Player> {
        public int compare(Player p1, Player p2) {
            return p1.getScore() < p2.getScore() ? -1
                    : p1.getScore() > p2.getScore() ? 1 : 0;
        }
    }

    public void turnHandler(Player player){
        int currentplayer = 0, nextplayer = 0;
        int i, next;
        for(i = 0; i < players.length; i++){
            if (players[i].myTurn) {
                currentplayer = players[i].playernumber;
                players[i].myTurn = false;
            }
            nextplayer = currentplayer + 1;
        }
        if (nextplayer < players.length) {
            players[nextplayer].myTurn = true;
        }
        else {
            nextplayer = 0;
            players[0].myTurn = true;
        }
        edits.drawRect(0, 110 ,width, startGridY - 20, background);
        edits.drawText("^", 100 + (nextplayer * 250), 210, players[nextplayer].painttext);
    }

    public void scoreHandler(Player player, int amount){

        edits.drawRect(0, 0,width, 100, background);

        player.score += amount;

        for (int i = 0; i < players.length; i++){

            int padmodx = 50 + (i * 250);
            edits.drawText(players[i].getPlayerName() + " = " + players[i].score, padmodx, 100, players[i].painttext);
        }
    }


    //left 0 = linkermuur , 1 = rechtermuur, -1 = geensquare
    public int checkSquare(Point wall, Boolean horizontal){

        int leftwall[] = new int[4];
        int rightwall[] = new int[4];
        int bottomwall[] = new int[4];
        int topwall[] = new int[4];
        int result = -1;
        boolean flagleft = true;
        boolean flagright = true;
        boolean flagbottom = true;
        boolean flagtop = true;
        if (horizontal){
            leftwall[0] = basebitmap.getPixel(wall.x - wallsize, wall.y);
            leftwall[1] = basebitmap.getPixel(wall.x - (wallsize/2), wall.y - (wallsize/2));
            leftwall[2] = basebitmap.getPixel(wall.x, wall.y);
            leftwall[3] = basebitmap.getPixel(wall.x - (wallsize/2), wall.y + (wallsize/2));
            for (int i = 0; i < leftwall.length; i++){
                if (leftwall[i] != wallcolor)
                    flagleft = false;
            }
            rightwall[0] = basebitmap.getPixel(wall.x + wallsize, wall.y);
            rightwall[1] = basebitmap.getPixel(wall.x + (wallsize/2), wall.y + (wallsize/2));
            rightwall[2] = basebitmap.getPixel(wall.x, wall.y);
            rightwall[3] = basebitmap.getPixel(wall.x + (wallsize/2), wall.y - (wallsize/2));
            for (int i = 0; i < rightwall.length; i++){
                if (rightwall[i] != wallcolor)
                    flagright = false;
            }
            if (flagleft) result = 0;
            if (flagright) result = 1;
            if (flagright && flagleft) result = 2;

            return result;
        }
        if (!horizontal){
            bottomwall[0] = basebitmap.getPixel(wall.x - (wallsize/2), wall.y + (wallsize/2));
            bottomwall[1] = basebitmap.getPixel(wall.x, wall.y);
            bottomwall[2] = basebitmap.getPixel(wall.x + (wallsize/2), wall.y + (wallsize/2));
            bottomwall[3] = basebitmap.getPixel(wall.x, wall.y + wallsize);
            for (int i = 0; i < bottomwall.length; i++){
                if (bottomwall[i] != wallcolor)
                    flagbottom = false;
            }
            topwall[0] = basebitmap.getPixel(wall.x - (wallsize/2), wall.y - (wallsize/2));
            topwall[1] = basebitmap.getPixel(wall.x, wall.y - wallsize);
            topwall[2] = basebitmap.getPixel(wall.x + (wallsize/2), wall.y - (wallsize/2));
            topwall[3] = basebitmap.getPixel(wall.x, wall.y);
            for (int i = 0; i < topwall.length; i++){
                if (topwall[i] != wallcolor)
                    flagtop = false;
            }
            if (flagbottom) result = 0;
            if (flagtop) result = 1;
            if (flagtop && flagbottom) result = 2;

            return result;
        }

        return -1;
    }

    public void createGrid(Canvas edits){
        for (int i = 0; i < players.length; i++){
            int padmodx = 50 + (i * 250);
            edits.drawText(players[i].getPlayerName() + " = " + players[i].score, padmodx, 100, players[i].painttext);
        }

        edits.drawRect(0, 110 ,width, startGridY - 20, background);
        edits.drawText("^", 100, 210, players[0].painttext);

        edits.drawRect(startGridX, startGridY, startGridX + (rows * wallsize), startGridY + (colums * wallsize), gridPaint);
        int i, j, x, y;
        int pointarray = 0, wallarray = 0 ;
        for (i = 0; i < colums + 1; i++) {
            for (j = 0; j < rows + 1; j++) {
                x = startGridX + (wallsize * i);
                y = startGridY + (wallsize * j);
                edits.drawCircle(x, y, 12, dotPaint);
                gridpoints[pointarray] = new Point();
                gridpoints[pointarray].set(x, y);
                pointarray++;
            }
        }

        int startx, stopx, starty, stopy;
        for (i = 0; i < colums; i++) {
            for (j = 0; j < rows - 1; j++) {
                startx = (startGridX + (wallsize * i));
                stopx = (startGridX + (wallsize * i) + wallsize);
                starty =  (startGridY + (wallsize)) + (wallsize * j);
                stopy = (startGridY + (wallsize)) + (wallsize * j);
                edits.drawLine(startx, starty, stopx, stopy, wallPaint);
                x = (startGridX + (wallsize/2)) + (wallsize * i);
                y = (startGridY + (wallsize)) + (wallsize * j);
                //edits.drawCircle(x, y, 12, wallPaint);
                wallpoints[wallarray] = new Point();
                wallpoints[wallarray].set(x, y);
                wallarray++;
            }
        }

        for (i = 0; i < colums - 1; i++) {
            for (j = 0; j < rows; j++) {
                startx = (startGridX + (wallsize)) + (wallsize * i);
                stopx = (startGridX + (wallsize)) + (wallsize * i);
                starty =  (startGridY + (wallsize * j));
                stopy = (startGridY + (wallsize)) + (wallsize * j);
                edits.drawLine(startx, starty, stopx, stopy, wallPaint);
                x = (startGridX + (wallsize)) + (wallsize * i);
                y = (startGridY + (wallsize/2)) + (wallsize * j);
                //edits.drawCircle(x, y, 12, wallPaint);
                wallpoints[wallarray] = new Point();
                wallpoints[wallarray].set(x, y);
                wallarray++;
            }
        }

    }
    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(basebitmap, 0, 0, null);
    }

    class ClientThread implements Runnable {
        public void run() {
            mRun = true;

            try {
                if(!gameConnection.isConnected()) {
                    gameConnection = new Socket("46.4.112.245", 8097);
                    sendBuffer = "118" +roomname +"-" +nickname;
                    readySendBuffer = true;
                }
                //here you must put your computer's IP address.
                //InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                //Log.e("TCP Client", "C: Connecting...");

                //create a socket to make the connection with the server
                //Socket socket = new Socket(serverAddr, SERVER_PORT);

                try {
                    Log.i("Debug", "inside try catch");
                    System.out.println("We are trying to send something");
                    //sends the message to the server
                    mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(gameConnection.getOutputStream())), true);

                    //receives the message which the server sends back
                    mBufferIn = new BufferedReader(new InputStreamReader(gameConnection.getInputStream()));
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

    private void eventThread() {
        ((Activity)context).runOnUiThread(new Thread(new Runnable() {
            public void run() {
                if (mRecieved) {
                    message_handler(mServerMessage);
                    mRecieved = false;
                    new Thread(new ClientThread()).start();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }

    private void sender_handler(String sendMessage){
        readySendBuffer = true;
        sendBuffer = sendMessage;
        new Thread(new ClientThread()).start();
    }

    private void message_handler(String mServerMessage){

        if (mServerMessage.startsWith("603")){
            //TODO Draw something, not their turn!
            edits.drawRect(0, 110 ,width, startGridY - 20, background);
            edits.drawText("Wait for your turn", 100, 210, players[myplayernumber].painttext);
            invalidate();
        }
        if (mServerMessage.startsWith("602")){
            //TODO It is your turn!
            //TODO Draw something on screen
            edits.drawRect(0, 110 ,width, startGridY - 20, background);
            edits.drawText("It is your turn", 100, 210, players[myplayernumber].painttext);
            invalidate();
        }
        if (mServerMessage.startsWith("601")){
            String message = mServerMessage.substring(3);
            String[] coordinates = message.split("\\|");
            System.out.println(coordinates[0]);
            System.out.println(coordinates[1]);
            Float xtouch = Float.parseFloat(coordinates[0]);
            Float ytouch = Float.parseFloat(coordinates[1]);
            drawTouch(xtouch, ytouch);
            invalidate();
        }
        if (mServerMessage.startsWith("604")){
            //TODO Player has disconnected, game is ended.
        }
        if (mServerMessage.startsWith("599")){
            //TODO Welcome, your number =
            String number = mServerMessage.substring(3);
            this.myplayernumber = Integer.parseInt(number);
        }
        if (mServerMessage.startsWith("598")){
            //TODO Room not ready
            edits.drawRect(0, 110 ,width, startGridY - 20, background);
            edits.drawText("Room is not full yet", 100, 210, players[myplayernumber].painttext);
            invalidate();
        }
        if (mServerMessage.startsWith("597")){
            //TODO Room ready
            edits.drawRect(0, 110 ,width, startGridY - 20, background);
            edits.drawText("Room is not full yet", 100, 210, players[myplayernumber].painttext);
            invalidate();
        }
    }
}