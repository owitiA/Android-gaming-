package com.owitia.retrosquash;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
    //


    Canvas canvas;
    SquashCourtView courtView;

    //entire logic of the game
    // sound

    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    int sample3 = -1;
    int sample4 = -1;


    //pixeling and drawing
    Display display;
    int screenWidth;
    int screenHeight;

    //Game objects
    int racketWidth;
    int racketHeight;
    Point racketPosition;
    int ballWidth;
    Point ballPosition;


    // racket movements

    boolean racketIsMovingLeft;
    boolean racketIsMovingRight;

    //ball

    boolean ballIsMovingLeft;
    boolean ballIsMovingRight;
    boolean ballIsMovingUp;
    boolean ballIsMovingDown;

    //stats and score
    int score;
    int lives;
    int fps;
    long lastFrameTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_main);
        courtView = new SquashCourtView(this);
        setContentView(courtView);


        // int maxStreams =10;
        //  int srcQuality = 10;
        int priority = 0;

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sample1 = soundPool.load(this, R.raw.sample1, priority);
        sample2 = soundPool.load(this, R.raw.sample2, priority);
        sample3 = soundPool.load(this, R.raw.sample3, priority);
        sample4 = soundPool.load(this, R.raw.sample4, priority);


        //Init display
        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;

        //game objects position
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 8;
        racketHeight = 10;

        ballWidth = screenWidth / 35; //used arbitary division
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;

        score = 0;
        lives = 3;

    }

    @Override
    protected void onResume() {
        super.onResume();
        courtView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        courtView.pause();

    }

    class SquashCourtView extends SurfaceView implements Runnable {
        Thread logicThread;
        SurfaceHolder holder;
        volatile boolean playingSquash;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            holder = getHolder();
            paint = new Paint();
            ballIsMovingDown = true;

            //send the ball in random direction
            Random randomNumber = new Random();
            int ballDirection = randomNumber.nextInt(3);

            switch (ballDirection) {
                case 0:
                    ballIsMovingLeft = true;
                    ballIsMovingRight = false;
                    break;

                case 1:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = true;
                    break;

                case 2:
                    ballIsMovingLeft = false;
                    ballIsMovingRight = false;
                    break;
            }

        }

        @Override
        public void run() {
            while (playingSquash) {
                UpdateLogic();
                drawCourt();
                controlFPS();

            }
        }

        //update logic
        private void UpdateLogic() {

            //racket movement
            if (racketIsMovingRight) {
                if (racketPosition.x + (racketWidth / 2) < screenWidth) {
                    racketPosition.x += 20;
                }
            }
            if (racketIsMovingLeft) {
                if (racketPosition.x - (racketWidth / 2) >= 0) {
                    racketPosition.x -= 20;
                }
            }


            //detect collisions
            //right
            if (ballPosition.x + ballWidth > screenWidth) {
                ballIsMovingRight = true;
                ballIsMovingLeft = false;
                soundPool.play(sample1, 1f, 1f, 0, 0, 1);
            }
            //left
            if (ballPosition.x < 0) {
                ballIsMovingRight = true;
                ballIsMovingLeft = false;
                soundPool.play(sample2, 1f, 1f, 0, 0, 1);
            }
            //bottom

            if (ballPosition.y > screenHeight - ballWidth) {
                lives -= 1;
                if (lives == 0) {
                    lives = 3;
                    score = 0;
                    soundPool.play(sample4, 1f, 1f, 0, 0, 1);
                }
                //return ball to top
                ballPosition.y = 1 + ballWidth;
                Random randomNumber = new Random();
                //int startX = randomNumber.nextInt(screenWidth - ballWidth) + 1;
                //ballPosition.x = startX + ballWidth;

                int ballDirection = randomNumber.nextInt(3);
                switch (ballDirection) {
                    case 0:
                        ballIsMovingLeft = true;
                        ballIsMovingRight = false;
                        break;

                    case 1:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = true;
                        break;

                    case 2:
                        ballIsMovingLeft = false;
                        ballIsMovingRight = false;
                        break;
                }

            }


            //top
            if (ballPosition.y <= 0) {
                ballIsMovingDown = true;
                ballIsMovingUp = false;
                ballPosition.y = 1;
                soundPool.play(sample2, 1f, 1f, 0, 0, 1);

            }
            //move ball
            if (ballIsMovingDown) {
                ballPosition.y += 6;
            }
            if (ballIsMovingUp) {
                ballPosition.y -= 10;
            }

            if (ballIsMovingLeft) {
                ballPosition.x -= 12;
            }

            if (ballIsMovingRight) {
                ballPosition.x += 12;
            }
            // has ball hit Racket
            if (ballPosition.y + ballWidth >= (racketPosition.y - racketHeight / 2)) {
                int halfRacket = racketWidth / 2;
                if (ballPosition.x + ballWidth > (racketPosition.x - halfRacket) &&
                        ballPosition.x - ballWidth < (racketPosition.x + halfRacket)) {
                    //rebound ball
                    soundPool.play(sample3, 1f, 1f, 0, 0, 1);
                    score++;
                    ballIsMovingUp = true;
                    ballIsMovingDown = false;

                    if (ballPosition.x >= racketPosition.x) {
                        ballIsMovingRight = true;
                        ballIsMovingLeft = false;
                    } else {
                        ballIsMovingRight = false;
                        ballIsMovingLeft = true;
                    }
                }
            }
        }


        //control fps

        private void controlFPS() {
            long timeThisFrame = System.currentTimeMillis() - lastFrameTime;
            long timeToSleep = 15 - timeThisFrame;
            if (timeThisFrame > 0) {
                fps = (int) (1000 / timeThisFrame);
            }
            if (timeToSleep > 0) {
                try {
                    Thread.sleep(timeToSleep);

                } catch (InterruptedException ex) {
                }
            }
            lastFrameTime = System.currentTimeMillis();

        }

        public void resume() {
            playingSquash = true;
            logicThread = new Thread(this);
            logicThread.start();
        }

        public void pause() {
            playingSquash = false;
            try {
                logicThread.join();
            } catch (InterruptedException e) {
            }
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()& MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getX() >= screenWidth / 2) {
                        racketIsMovingRight = true;
                        racketIsMovingLeft = false;
                    } else {
                        racketIsMovingRight = false;
                        racketIsMovingLeft = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    racketIsMovingRight = false;
                    racketIsMovingLeft = false;
                    break;
            }
            return true;

        }

        private void drawCourt() {
            if (!holder.getSurface().isValid()) {
                return;
            }
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            //title
            paint.setColor(Color.WHITE);
            paint.setTextSize(25);
            String title = "Score: " + score + " lives" + " fps" + fps;
            canvas.drawText(title, 20, 20, paint);

            //racket
            int left = racketPosition.x - (racketWidth / 2);
            int top = racketPosition.y - (racketHeight / 2);
            int right = racketPosition.x + (racketWidth / 2);
            int bottom = racketPosition.y + (racketHeight / 2);

            canvas.drawRect(left, top, right, bottom, paint);

            canvas.drawCircle(ballPosition.x, ballPosition.y, ballWidth, paint);

            holder.unlockCanvasAndPost(canvas);

        }
    }


}



