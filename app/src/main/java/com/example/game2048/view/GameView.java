package com.example.game2048.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Toast;

import com.example.game2048.activity.Game;
import com.example.game2048.bean.GameItem;
import com.example.game2048.config.Config;
import java.util.ArrayList;

/**
 * Created by 金文韬 on 2017/10/22.
 */

public class GameView extends GridLayout implements View.OnTouchListener {
    private int mTarget;            //目标分数
    private int mScoreHistory;      //历史分数
    private int mGameLines;         //矩阵规格参数
    private GameItem[][] mGameMatrix; //游戏1矩阵
    private int[][] mGameMatrixHistory; //游戏历史矩阵
    private ArrayList<Integer> mCalList;  //存储方块的数组
    private ArrayList<Point> mBlanks;       //存储空格的数组
    //历史高分记录
    private int mHighScore;
    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;
    private int mKeyItemNum = -1;

    public GameView(Context context) {
        super(context);
        mTarget=Config.mSp.getInt(Config.KEY_GAME_GOAL, 2048);
        initGameMatrix();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGameMatrix();
    }
    /**
     * 初始化View
     */
    private void initGameMatrix() {
        removeAllViews();
        mScoreHistory = 0;
        Config.mGameLines = Config.mSp.getInt(Config.KEY_GAME_LINES, 4);
        mGameLines = Config.mGameLines;
        mGameMatrix = new GameItem[mGameLines][mGameLines];
        mGameMatrixHistory = new int[mGameLines][mGameLines];
        mCalList = new ArrayList<Integer>();
        mBlanks = new ArrayList<Point>();
        mHighScore = Config.mSp.getInt(Config.KEY_HIGH_SCORE, 0);
        setColumnCount(mGameLines);
        setRowCount(mGameLines);
        //添加触模
        setOnTouchListener(this);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();   // DisplayMetrics metrics = new DisplayMetrics();getWindowManager().getDefaultDisplay().getMetrics(metrics);
        display.getMetrics(metrics);
        Config.mItemSize = metrics.widthPixels/Config.mGameLines;
        Log.d("syso", "手机分辨率: ="+metrics.widthPixels+"*"+metrics.heightPixels);
        initGameView(Config.mItemSize);
    }

    /**
     * @param cardSize 每个数字的高度
     */
    private void initGameView(int cardSize) {
        removeAllViews();
        GameItem card;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                card = new GameItem(getContext(), 0);
                addView(card, cardSize, cardSize);
                mGameMatrix[i][j] = card;
                mBlanks.add(new Point(i, j));
            }
        }
        //添加随机数字
        addRandomNum();
        addRandomNum();
    }

    /**
     * 添加随机数字
     */
    private void addRandomNum() {
        getBlanks();
        if (mBlanks.size() > 0) {
            int randomNum = (int) (Math.random() * mBlanks.size());
            Point randomPoint = mBlanks.get(randomNum);
            mGameMatrix[randomPoint.x][randomPoint.y].setNum(Math.random() > 0.2d ? 2 : 4);
            animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
        }
    }

    /**
     * 把空白处的位置添加到数组中
     */
    private void getBlanks() {
        mBlanks.clear();
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == 0) {
                    mBlanks.add(new Point(i, j));
                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                saveHistoryMatrix();
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                mEndX = (int) event.getX();
                mEndY = (int) event.getY();
                judgeDirection(mEndX - mStartX, mEndY - mStartY);
                if (isMoved()) {
                    addRandomNum();
                    Game.getGameActivity().setScore(Config.SCORE,0);
                }
               checkCompleted();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 检查游戏完成度
     */
    private void checkCompleted() {
        int result=checkNums();
        if (result==0){
            if (Config.SCORE>mHighScore){
                SharedPreferences.Editor edit=Config.mSp.edit();
                edit.putInt(Config.SP_HIGH_SCORE,Config.SCORE);
                edit.apply();
                Game.getGameActivity().setScore(Config.SCORE,1);
                Config.SCORE=0;
            }
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            builder.setTitle("您输了哦，请重新开始吧").setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startGame();
                }
            }).create().show();
            Config.SCORE=0;
        }else if (result==2){
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            builder.setTitle("完成此难度，双击666").setPositiveButton("重新游戏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startGame();
                }
            }).setNegativeButton("继续游戏", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor edit = Config.mSp.edit();
                    if (mTarget == 1024) {
                        edit.putInt(Config.KEY_GAME_GOAL, 2048);
                        mTarget = 2048;
                        Game.getGameActivity().setGoal(2048);
                    } else if (mTarget == 2048) {
                        edit.putInt(Config.KEY_GAME_GOAL, 4096);
                        mTarget = 4096;
                        Game.getGameActivity().setGoal(4096);
                    } else {
                        edit.putInt(Config.KEY_GAME_GOAL, 4096);
                        mTarget = 4096;
                        Game.getGameActivity().setGoal(4096);
                    }
                    edit.apply();
                }
            }).create().show();
            Config.SCORE=0;
        }
    }

    /**
     *  检测所有数字 看是否有满足条件的
     * @return 0:结束 1:正常 2:成功
     */
    private int checkNums() {
        getBlanks();
        if (mBlanks.size() == 0) {
            for (int i = 0; i < mGameLines; i++) {
                for (int j = 0; j < mGameLines; j++) {
                    if (j < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i][j + 1]
                                .getNum()) {
                            return 1;
                        }
                    }
                    if (i < mGameLines - 1) {
                        if (mGameMatrix[i][j].getNum() == mGameMatrix[i + 1][j]
                                .getNum()) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() == mTarget) {
                    return 2;
                }
            }
        }
        return 1;
    }
    /**
     * 判断滑动的方向
     * @param offsetX x轴方向的偏移量
     * @param offsetY y轴方向的偏移量
     */
    private void judgeDirection(int offsetX, int offsetY) {
        int density = getDeviceDensity();      //获取设备屏幕密度,像素的比例
        Log.d("density", "屏幕像素="+density);
        int slideDis = 5 * density;              //滑动边界
        int maxDis = 200 * density;                //滑动限制
        int superDix=350*density;                   //超级权限的条件
        boolean flagNormal = (Math.abs(offsetX) > slideDis
                || Math.abs(offsetY) > slideDis)
                && (Math.abs(offsetX) < maxDis
                && Math.abs(offsetY) < maxDis);
        boolean flagSurper = (Math.abs(offsetX) > superDix) || (Math.abs(offsetY) > superDix);
        if (flagNormal && !flagSurper) {
            if (Math.abs(offsetX) > Math.abs(offsetY)) {
                if (offsetX > slideDis) {
                    swipeRight();
                } else {
                    swipeLeft();
                }
            } else {
                if (offsetY > slideDis) {
                    swipeDown();
                } else {
                    swipeUp();
                }
            }
        }else if(flagSurper){
            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
            final EditText et=new EditText(getContext());
            builder.setTitle("请输入2048游戏数")
                    .setView(et)
                    .setPositiveButton("就赖皮一下哈", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (!TextUtils.isEmpty(et.getText())){
                                int superNum=Integer.parseInt(et.getText().toString());
                                addSuperNum(superNum);
                                if(!checkSuperNum(superNum)){
                                    Toast.makeText(getContext(),"不带这么赖皮的呦，请输入游戏数",Toast.LENGTH_SHORT).show();
                                }
                                checkCompleted();
                            }
                        }
                    })
                    .setNegativeButton("小样，我不需要",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        }
    }

    //添加超级权限数字
    public void addSuperNum(int num){
        if(checkSuperNum(num)){
            getBlanks();
            if(mBlanks.size()>0){
                int randomNum= (int) (Math.random()*mBlanks.size());
                Point randomPoint=mBlanks.get(randomNum);
                mGameMatrix[randomPoint.x][randomPoint.y].setNum(num);
                animCreate(mGameMatrix[randomPoint.x][randomPoint.y]);
            }
        }
    }

    //检查超级权限数字准确性
    private boolean checkSuperNum(int num) {
        boolean flag = (num == 2 || num == 4 || num == 8 || num == 16
                || num == 32 || num == 64 || num == 128 || num == 256
                || num == 512 || num == 1024);
        return flag;
    }

    //添加动画效果
    public  void animCreate(GameItem target){
        target.setAnimation(null);
        ScaleAnimation sa=new ScaleAnimation(0.1f,1,0.1f,1, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        sa.setDuration(100);
        target.getItemView().startAnimation(sa);
    }

    /**
     * 向上滑动
     */
    private void swipeUp() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(mCalList.get(j));
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(0);
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 向下滑动
     */
    private void swipeDown() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[j][i].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[j][i].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[m][i].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }

    /**
     * 向右滑动
     */
    private void swipeRight() {
        for (int i = mGameLines - 1; i >= 0; i--) {
            for (int j = mGameLines - 1; j >= 0; j--) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            // 改变Item值
            for (int j = 0; j < mGameLines - mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(0);
            }
            int index = mCalList.size() - 1;
            for (int m = mGameLines - mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(mCalList.get(index));
                index--;
            }
            // 重置行参数
            mKeyItemNum = -1;
            mCalList.clear();
            index = 0;
        }
    }

    /**
     * 向左滑动
     */
    private void swipeLeft() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                int currentNum = mGameMatrix[i][j].getNum();
                if (currentNum != 0) {
                    if (mKeyItemNum == -1) {
                        mKeyItemNum = currentNum;
                    } else {
                        if (mKeyItemNum == currentNum) {
                            mCalList.add(mKeyItemNum * 2);
                            Config.SCORE += mKeyItemNum * 2;
                            mKeyItemNum = -1;
                        } else {
                            mCalList.add(mKeyItemNum);
                            mKeyItemNum = currentNum;
                        }
                    }
                } else {
                    continue;
                }
            }
            if (mKeyItemNum != -1) {
                mCalList.add(mKeyItemNum);
            }
            for (int j = 0; j < mCalList.size(); j++) {
                mGameMatrix[i][j].setNum(mCalList.get(j));
            }
            for (int m = mCalList.size(); m < mGameLines; m++) {
                mGameMatrix[i][m].setNum(0);
            }
            mKeyItemNum = -1;
            mCalList.clear();
        }
    }

    /**
     * 判断是否移动，只需要和历史数组对比即可得到结果
     */
    public boolean isMoved() {
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                if (mGameMatrix[i][j].getNum() != mGameMatrixHistory[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getDeviceDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        return (int) metrics.density;
    }

    /**
     * 保存历史矩阵
     */
    private void saveHistoryMatrix() {
        mScoreHistory = Config.SCORE;
        for (int i = 0; i < mGameLines; i++) {
            for (int j = 0; j < mGameLines; j++) {
                mGameMatrixHistory[i][j] = mGameMatrix[i][j].getNum();
            }
        }
    }

    /**
     * 游戏开始
     */
    public void startGame(){
        initGameMatrix();
        initGameView(Config.mItemSize);
    }

    /**
     * 继续游戏
     */
    public void continueGame(){
        initGameMatrix();
        initGameView(Config.mItemSize);
    }

    /**
     * 撤销上次移动
     */
    public void revertGame(){
        int sum=0;
        for(int[] element:mGameMatrixHistory){
            for (int i:element){
                sum+=i;
            }
        }
        if(sum!=0){
            Game.getGameActivity().setScore(mScoreHistory,0);
            Config.SCORE=mScoreHistory;
            for (int i=0;i<mGameLines;i++){
                for (int j=0;j<mGameLines;j++){
                    mGameMatrix[i][j].setNum(mGameMatrixHistory[i][j]);
                }
            }
        }
    }
}
