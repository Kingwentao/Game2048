package com.example.game2048.config;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Created by 金文韬 on 2017/10/22.
 */

public class Config extends Application {
    //SP对象
    public static SharedPreferences mSp;
    //Item的宽高
    public static int mItemSize;
    //游戏目标分数
    public int mGameGoal;
    //GameView行列数
    public static int mGameLines;
    //记录分数
    public static int SCORE=0;
    //记录难度
    public static String mStandard;
    public static String KEY_HIGH_SCORE ="KEY_HIGHSCORE" ;
    public static String KEY_GAME_LINES = "KEY_GAMELINES";
    public static String KEY_GAME_GOAL = "KEY_GAMEGOAL";
    public static String SP_HIGH_SCORE="SP_HIGHSCORE";
    public static String GAME_STANDARD="SP_STANDARD";
    @Override
    public void onCreate() {
        super.onCreate();
        mSp=getSharedPreferences(SP_HIGH_SCORE,0);   //设置文件名及存储方式
        mGameLines=mSp.getInt(KEY_GAME_LINES,4);
        mGameGoal=mSp.getInt(KEY_GAME_GOAL,1024);
        mStandard=mSp.getString(GAME_STANDARD,"简单");
        mItemSize=0;
    }
}
