package com.sonhoai.sonho.catchthefruits;

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

public class GameActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private TextView tapToStar, scoreLabel;
    private ImageView player, box_bg, boom, fruitA, fruitB;
    LinearLayout linearLayout;
    RelativeLayout quitGame;
    private Handler handler = new Handler();
    private Timer timer = new Timer();
    private int playerY, fruitAX, fruitAY, fruitBX, fruitBY, boomX, boomY;
    private boolean action_flg = false;
    private boolean start_flg = false;
    private int frameHieght, boxSize,screenWidth, screenHeight;
    private int score = 0, speedPlayer, speedFruitA, speedBoom, speedFruitB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        box_bg.setVisibility(View.VISIBLE);
        player.setVisibility(View.INVISIBLE);
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
        speedPlayer = Math.round(screenHeight / 50F);
        speedFruitA = Math.round(screenWidth / 60F);
        speedFruitB = Math.round(screenWidth / 36F);
        speedBoom = Math.round(screenWidth / 50F);

        fruitA.setX(-80);
        fruitA.setY(-80);
        fruitB.setX(-80);
        fruitB.setY(-80);
        boom.setX(-80);
        boom.setY(-80);
    }

    private void init() {
        audioManager = new AudioManager(getApplicationContext());

        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        tapToStar = (TextView) findViewById(R.id.starLabel);
        player = (ImageView) findViewById(R.id.box);
        box_bg = (ImageView) findViewById(R.id.box_bg);
        boom = (ImageView) findViewById(R.id.black);
        fruitA = (ImageView) findViewById(R.id.orange);
        fruitB = (ImageView) findViewById(R.id.pink);
        quitGame = (RelativeLayout) findViewById(R.id.quitGame);

        linearLayout = (LinearLayout) findViewById(R.id.activity_main);
    }

    public void changePosition() {
        hitCheck();
        //orange
        fruitAX -= speedFruitA;
        //nếu bé hơn trục x, qua tới âm, thì set về mặc định
        if (fruitAX < -fruitAX) {
            fruitAX = screenWidth + 20;
            //random vị trí y, từ trên xuống dưới
            fruitAY = (int) Math.floor(Math.random() * (frameHieght - fruitA.getHeight()));
        }
        fruitA.setX(fruitAX);
        fruitA.setY(fruitAY);
        //black
        boomX -= speedBoom;
        if (boomX < -boomX) {
            boomX = screenWidth + 5;
            boomY = (int) Math.floor(Math.random() * (frameHieght - boom.getHeight()));
        }
        boom.setX(boomX);
        boom.setY(boomY);
        fruitBX -= speedFruitB;
        if (fruitBX < 0) {
            fruitBX = screenWidth + 500;
            fruitBY = (int) Math.floor(Math.random() * (frameHieght - fruitB.getHeight()));
        }
        fruitB.setX(fruitBX);
        fruitB.setY(fruitBY);

        //nếu có chạm vào, thì do box đi lên, co nghĩa là tiền về 0,0
        if (action_flg == true) {
            //touch
            playerY -= speedPlayer;
        } else {
            //release
            playerY += speedPlayer;
        }

        //phần phía trên
        if (playerY < 0) playerY = 0;
        //nếu mà vị trí y của box qua khỏi kích thước màn hình thì đặt lại vị trí y là tọa độ 0, và y là chiều cao - boxsize
        //phần phía dưới
        if (playerY > frameHieght - boxSize) playerY = frameHieght - boxSize;
        player.setY(playerY);
        scoreLabel.setText("Score: " + score);
    }

    public void hitCheck() {
        //orange
        int orangeCenterX = fruitAX + fruitA.getWidth() / 2;
        int orangeCenterY = fruitAY + fruitA.getHeight() / 2;
        ///check ở góc 1/4 phía trên trái hoặc 1/4 dưới trái
        if (0 <= orangeCenterX && orangeCenterX <= boxSize
                &&
                playerY <= orangeCenterY && orangeCenterY <= playerY + boxSize) {

            score += 5;
            fruitAX = -10;
            audioManager.playHitSound();
        }
        //pink
        int pinkCenterX = fruitBX + fruitB.getWidth() / 2;
        int pinkCenterY = fruitBY + fruitB.getHeight() / 2;
        // 0 <= orangeCenterX <= boxWidth;
        //boxY <= orangeCenterY <= boxY + boxHeight;
        if (0 <= pinkCenterX && pinkCenterX <= boxSize
                &&
                playerY <= pinkCenterY && pinkCenterY <= playerY + boxSize) {

            score += 10;
            fruitBX = -10;
            audioManager.playHitSound();
        }
        int blackCenterX = boomX + boom.getWidth() / 2;
        int blackCenterY = boomY + boom.getHeight() / 2;
        // 0 <= orangeCenterX <= boxWidth;
        //boxY <= orangeCenterY <= boxY + boxHeight;
        if (0 <= blackCenterX && blackCenterX <= boxSize
                &&
                playerY <= blackCenterY && blackCenterY <= playerY + boxSize) {
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
        player.setVisibility(View.VISIBLE);
        if (!start_flg) {
            start_flg = true;
            //audioManager.playBgSound();
            //do chỉ khi có tương tác thì mới kiểm tra dc cái kích thước, vị trí
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame);
            //set chiều cao mặc định
            frameHieght = frameLayout.getHeight();

            //lấy ra vị trí y
            playerY = (int) player.getY();
            boxSize = player.getHeight();

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
                            changePosition();
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
            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
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
