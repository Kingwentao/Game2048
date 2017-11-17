package com.example.game2048.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.example.game2048.R;

public class SplishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splish_layout);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                Intent intent=new Intent(SplishActivity.this,Game.class);
                Thread.sleep(3000);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    finish();
                }
            }
        }).start();

}
}
