package com.sonhoai.sonho.catchthefruits;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private TextView tapToStar;
    private TextView scoreLabel;
    private ImageView box, box_bg, black, orange, pink;

    LinearLayout linearLayout;
    RelativeLayout quitGame;

    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private int boxY;
    private int orangeX, orangeY, pinkX, pinkY, blackX, blackY;

    private boolean action_flg = false;
    private boolean start_flg = false;

    private int frameHieght, boxSize;

    private int screenWidth;
    private int screenHeight;


    private int score = 0;

    private int speedBox, speedOrange, speedBlack, speedPink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();


        box_bg.setVisibility(View.VISIBLE);
        box.setVisibility(View.INVISIBLE);
        scoreLabel.setVisibility(View.INVISIBLE);

        //get screen size
        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //xác định kích thước màn hình hiện tại
        screenWidth = size.x;
        screenHeight = size.y;

        //điều chỉnh tốc độ khung hình fps trên 1 giây
        speedBox = Math.round(screenHeight / 60F);
        speedOrange = Math.round(screenWidth / 60F);
        speedPink = Math.round(screenWidth / 36F);
        speedBlack = Math.round(screenWidth / 45F);

        orange.setX(-80);
        orange.setY(-80);
        pink.setX(-80);
        pink.setY(-80);
        black.setX(-80);
        black.setY(-80);
    }

    private void init() {
        audioManager = new AudioManager(getApplicationContext());

        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        tapToStar = (TextView) findViewById(R.id.starLabel);
        box = (ImageView) findViewById(R.id.box);
        box_bg = (ImageView) findViewById(R.id.box_bg);
        black = (ImageView) findViewById(R.id.black);
        orange = (ImageView) findViewById(R.id.orange);
        pink = (ImageView) findViewById(R.id.pink);
        quitGame = (RelativeLayout) findViewById(R.id.quitGame);

        linearLayout = (LinearLayout) findViewById(R.id.activity_main);
    }

    public void changePost() {
        hitCheck();
        //orange
        orangeX -= speedOrange;
        //nếu bé hơn trục x, qua tới âm, thì set về mặc định
        if (orangeX < 0) {
            orangeX = screenWidth + 20;
            //random vị trí y, từ trên xuống dưới
            orangeY = (int) Math.floor(Math.random() * (frameHieght - orange.getHeight()));
        }
        orange.setX(orangeX);
        orange.setY(orangeY);
        //black
        blackX -= speedBlack;
        if (blackX < 0) {
            blackX = screenWidth + 5;
            blackY = (int) Math.floor(Math.random() * (frameHieght - black.getHeight()));
        }
        black.setX(blackX);
        black.setY(blackY);
        pinkX -= speedPink;
        if (pinkX < 0) {
            pinkX = screenWidth + 500;
            pinkY = (int) Math.floor(Math.random() * (frameHieght - pink.getHeight()));
        }
        pink.setX(pinkX);
        pink.setY(pinkY);

        //nếu có chạm vào, thì do box đi lên, co nghĩa là tiền về 0,0
        if (action_flg == true) {
            //touch
            boxY -= speedBox;
        } else {
            //release
            boxY += speedBox;
        }

        //phần phía trên
        if (boxY < 0) boxY = 0;
        //nếu mà vị trí y của box qua khỏi kích thước màn hình thì đặt lại vị trí y là tọa độ 0, và y là chiều cao - boxsize
        //phần phía dưới
        if (boxY > frameHieght - boxSize) boxY = frameHieght - boxSize;
        box.setY(boxY);
        scoreLabel.setText("Score: " + score);
    }

    public void hitCheck() {
        //orange
        int orangeCenterX = orangeX + orange.getWidth() / 2;
        int orangeCenterY = orangeY + orange.getHeight() / 2;
        ///check ở góc 1/4 phía trên trái hoặc 1/4 dưới trái
        if (0 <= orangeCenterX && orangeCenterX <= boxSize
                &&
                boxY <= orangeCenterY && orangeCenterY <= boxY + boxSize) {

            score += 5;
            orangeX = -10;
            audioManager.playHitSound();
        }
        //pink
        int pinkCenterX = pinkX + pink.getWidth() / 2;
        int pinkCenterY = pinkY + pink.getHeight() / 2;
        // 0 <= orangeCenterX <= boxWidth;
        //boxY <= orangeCenterY <= boxY + boxHeight;
        if (0 <= pinkCenterX && pinkCenterX <= boxSize
                &&
                boxY <= pinkCenterY && pinkCenterY <= boxY + boxSize) {

            score += 10;
            pinkX = -10;
            audioManager.playHitSound();
        }
        int blackCenterX = blackX + black.getWidth() / 2;
        int blackCenterY = blackY + black.getHeight() / 2;
        // 0 <= orangeCenterX <= boxWidth;
        //boxY <= orangeCenterY <= boxY + boxHeight;
        if (0 <= blackCenterX && blackCenterX <= boxSize
                &&
                boxY <= blackCenterY && blackCenterY <= boxY + boxSize) {
            //game over
            timer.cancel();
            timer = null;
            audioManager.playOverSound();
            showGameOver();
        }
    }

    public void showGameOver() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);

            }
        });

        int hightScoreLabel;
        SharedPreferences settings = getSharedPreferences("GAME_DATA", Context.MODE_PRIVATE);
        int hightScore = settings.getInt("HIGHT_SCORE", 0);
        if (score > hightScore) {
            hightScoreLabel = score;
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("HIGHT_SCORE", score);
            editor.commit();
        } else {
            hightScoreLabel = hightScore;
        }
        dialog.setTitle("Game Over");
        dialog.setMessage("Your Score: " + score + "\nHight Score: " + hightScore);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();


    }

    public boolean onTouchEvent(MotionEvent me) {
        box.setVisibility(View.VISIBLE);
        if (!start_flg) {

            start_flg = true;

            //do chỉ khi có tương tác thì mới kiểm tra dc cái kích thước, vị trí
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame);
            //set chiều cao mặc định
            frameHieght = frameLayout.getHeight();

            //lấy ra vị trí y
            boxY = (int) box.getY();
            boxSize = box.getHeight();

            scoreLabel.setVisibility(View.VISIBLE);
            tapToStar.setVisibility(View.GONE);
            box_bg.setVisibility(View.GONE);
            quitGame.setVisibility(View.GONE);


            //game loop
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            changePost();
                        }
                    });
                }
            }, 0, 20);

        } else {
            if (me.getAction() == MotionEvent.ACTION_DOWN) {
                action_flg = true;
                audioManager.playFlyrSound();
            } else if (me.getAction() == MotionEvent.ACTION_UP) {
                action_flg = false;
            }
        }

        return true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Log.i("LOGGG", "NAHANA");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Bạn có muốn thoát?");
            builder.setMessage("Nhấn OK để thoát khỏi màn hình trò chơi");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    timer.cancel();
                    finish();
                }
            });
            builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void quitGame(View view) {
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
