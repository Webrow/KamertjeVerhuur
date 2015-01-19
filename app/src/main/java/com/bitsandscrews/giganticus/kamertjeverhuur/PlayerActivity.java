package com.bitsandscrews.giganticus.kamertjeverhuur;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class PlayerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Button btn2 = (Button) findViewById(R.id.btn_p2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OfflineActivity.class);
                int amountplayers = 2;
                intent.putExtra("playercount", amountplayers);
                startActivity(intent);
            }
        });

        Button btn3 = (Button) findViewById(R.id.btn_p3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OfflineActivity.class);
                int amountplayers = 3;
                intent.putExtra("playercount", amountplayers);
                startActivity(intent);
            }
        });

        Button btn4 = (Button) findViewById(R.id.btn_p4);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), OfflineActivity.class);
                int amountplayers = 4;
                intent.putExtra("playercount", amountplayers);
                startActivity(intent);
            }
        });
    }
}
