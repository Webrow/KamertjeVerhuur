package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Rowan on 14-12-2014.
 */
public class GridView extends View {

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

    public GridView(Context context, Player[] players) {
        super(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        //Inits
        this.width = size.x;
        this.height = size.y;
        this.wallcolor = -11952534;
        this.startGridX = width / 10;
        this.startGridY = height / 6;
        this.players = players;
        this.basebitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.edits = new Canvas(basebitmap);

        //Paints
        dotPaint.setColor(Color.parseColor("#499e6a"));
        dotPaint.setStyle(Paint.Style.FILL);

        gridPaint.setColor(Color.parseColor("#499e6a"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(12);

        background.setColor(Color.parseColor("#bce3cc"));
        background.setStyle(Paint.Style.FILL);

        //Init Grid
        createGrid(edits);

        //Redraw Canvas
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
                validateTouch(event.getX(), event.getY());
            }
        }
        invalidate();
        return true;
    }

    //Validate touch checks whoose turn it is and draws a wall whenever a wall is pressed.
    //After touching a wall a squarecheck is done to see if all 4 walls are standing
    //If so the walls get colored in the color of the player.
    //After this the scorehandler gets called which updates the score, and eventually
    //the turnhandler is raised which decided wether its the next players turn.
    //Afterthe max score has been reached ( 36 ) finishbattle is raised.
    //A scoreboard shows who won the match.
    public void validateTouch(float x, float y) {
        int currentplayer = 0;

        for( int i = 0; i < players.length; i++){
            if (players[i].myTurn)
                currentplayer = i;
        }

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
                        turnHandler();
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
                        turnHandler();
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

    //Create scoreboard, show who wins.
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

    //Class for sorting scores.
    class PlayerComparator implements Comparator<Player> {
        public int compare(Player p1, Player p2) {
            return p1.getScore() < p2.getScore() ? -1
                    : p1.getScore() > p2.getScore() ? 1 : 0;
        }
    }

    //Check whoose turn it is, and sets an indicator
    public void turnHandler(){
        int currentplayer = 0, nextplayer = 0;
        int i;
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

    //Handles scores
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

    //Draws the playingfield
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
}