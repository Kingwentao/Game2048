package com.example.game2048.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.game2048.R;
import com.example.game2048.config.Config;

public class ConfigPreference extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnGameLines;
    private TextView mTvGoal;
    private Button mBtnBack;
    private Button mBtnDone;
    private String[] mGameLinesList;
    private String[] mGameGoalList;
    private AlertDialog.Builder mBuilder;
    private String[] mGameStandrand;      //游戏难度
    String mLine;
    private String mStandard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_config);
        initView();
    }

    private void initView() {
        mBtnGameLines = (Button) findViewById(R.id.btn_gamelines);
        mTvGoal = (TextView) findViewById(R.id.tv_goal);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnDone = (Button) findViewById(R.id.btn_done);
        mBtnGameLines.setOnClickListener(this);
        //mBtnGoal.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mBtnDone.setOnClickListener(this);
        mBtnGameLines.setText(Config.mSp.getString(Config.GAME_STANDARD, "简单"));
        mTvGoal.setText(" " + Config.mSp.getInt(Config.KEY_GAME_GOAL,1024));
        mGameLinesList = new String[]{"4", "4", "5"};
        mGameStandrand=new String[]{"简单","一般","困难"};
        mGameGoalList = new String[]{"1024", "2048", "4096"};
    }

    //保存设置
    private void saveConfig(){
        SharedPreferences.Editor editor=Config.mSp.edit();
        try{
            editor.putInt(Config.KEY_GAME_LINES,Integer.parseInt(mLine));
            Log.d("syso", "mStandard=: "+mStandard);
            editor.putString(Config.GAME_STANDARD,mStandard);
            editor.putInt(Config.KEY_GAME_GOAL, Integer.parseInt(mTvGoal.getText().toString()));
        }catch (Exception e){
            Toast.makeText(this,"没有改变游戏难度",Toast.LENGTH_SHORT).show();
        }finally {
            editor.commit();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_gamelines:
                mBuilder=new AlertDialog.Builder(this);
                mBuilder.setTitle("选择难度");
                mBuilder.setItems(mGameStandrand, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBtnGameLines.setText(mGameStandrand[i]);
                        mStandard=mGameStandrand[i];
                        mLine=mGameLinesList[i];
                        mTvGoal.setText(mGameGoalList[i]);;
                    }
                }).create().show();
                break;
            case R.id.tv_goal:
                mBuilder = new AlertDialog.Builder(this);
                mBuilder.setTitle("设置完成游戏目标分数");
                mBuilder.setItems(mGameGoalList,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTvGoal.setText(mGameGoalList[which]);
                            }
                        });
                mBuilder.create().show();
                break;
            case R.id.btn_back:
                this.finish();
                break;
            case R.id.btn_done:
                saveConfig();
                setResult(RESULT_OK);
                this.finish();
                break;
            default:
                break;
        }
        }
    }
