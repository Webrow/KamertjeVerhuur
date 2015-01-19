package com.bitsandscrews.giganticus.kamertjeverhuur;

/**
 * Created by Rowan on 11-12-2014.
 */
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

public class Player{

    public int pcolors[] = {Color.parseColor("#7979FF"), Color.parseColor("#FF79E1"), Color.parseColor("#DB9900"), Color.parseColor("#25A0C5")};
    String playername;
    int playernumber;
    int score;
    int color;
    Paint paint = new Paint();
    Paint painttext = new Paint();
    Boolean myTurn;

    public Player(int playernum) {
        playernumber = playernum;
        setColor(pcolors[playernum]);
        setPlayerName(playernumber);
        score = 0;
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        painttext.setColor(color);
        painttext.setStyle(Paint.Style.STROKE);
        painttext.setTextSize(60);
        painttext.setAntiAlias(true);
        painttext.setStrokeWidth(3);

        this.myTurn = false;
        if (playernum == 0) {
            this.myTurn = true;
        }
    }

    public void setColor(int pcolor){
        this.color = pcolor;
    }

    public void setPlayerName(int playernumber){
        int number = playernumber + 1;
        playername = "P" +number;
    }

    public String getPlayerName(){
        return playername;
    }

    public void setScore(){
        score += 1;
    }

    public String getScoreString(){
        String text = getPlayerName() +" acquired only : " +getScore() +" PTS.";
        return text;
    }

    public int getScore(){
        return score;
    }

    public int getColor(){
        return color;
    }

}

