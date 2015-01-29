package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.RelativeLayout;


public class OfflineActivity extends Activity {

    public int amountplayers;
    private Player[] players;
    GridView canvasview;
    private Point[] gridpoints;
    private Point[] walls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        //Create the players
        initPlayers();

        canvasview = new GridView(this, players);
        gridpoints = canvasview.gridpoints;
        walls = canvasview.wallpoints;

        //testing
        RelativeLayout myLayout = new RelativeLayout(this);
        myLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        myLayout.setBackgroundColor(Color.parseColor("#bce3cc"));

        //Create walls
        initWalls(gridpoints);

        myLayout.addView(canvasview);

        this.setContentView(myLayout);
    }

    public void initWalls(Point[] walls) {
        this.walls = canvasview.wallpoints;
    }

    public void initPlayers(){
        //debundle playerdata
        Bundle bundle = getIntent().getExtras();
        amountplayers = bundle.getInt("playercount");

        players = new Player[amountplayers];

        for(int i = 0;i < players.length; i++) {
            int pnumber = i;
            players[i] = new Player(pnumber);
        }
    }

}
