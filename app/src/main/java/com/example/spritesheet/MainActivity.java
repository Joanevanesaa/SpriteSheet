package com.example.spritesheet;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;



public class MainActivity extends AppCompatActivity {
    GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    static class GameView extends SurfaceView implements Runnable {
        Thread gameThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;

        Canvas canvas;
        Paint paint;

        long fps;
        private long timeThisFrame;
        Bitmap bitmapGirl;

        boolean isMoving = false;
        boolean forward = false;

        float walkSpeedPerSecond = 250;
        float GirlXPosition = 20;
        private int frameWidth = 50;
        private int frameHeight = 100;
        // How many frames are there on the sprite sheet?
        private int frameCount = 8;
        // Start at the first frame - where else?
        private int currentFrame = 0;
        // What time was it when we last changed frames
        private long lastFrameChangeTime = 0;
        // How long should each frame last
        private int frameLengthInMilliseconds = 100;
        //rectangle to define area of spritesheet
        private Rect frameToDraw = new Rect(0, 0, frameWidth, frameHeight);
        //rect to define area on which to draw
        RectF whereToDraw = new RectF(GirlXPosition, 0, GirlXPosition + frameWidth, frameHeight);

        private long getLastFrameChangeTime = 0;
        private int getFrameLengthInMilliseconds = 100;

        public GameView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();

            bitmapGirl = BitmapFactory.decodeResource(this.getResources(), R.drawable.girl);
            bitmapGirl = Bitmap.createScaledBitmap(bitmapGirl, frameWidth * frameCount, frameHeight, false);

        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();
                update();
                draw(startFrameTime);

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void getCurrentFrame() {
            long time = System.currentTimeMillis();
            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                    frameToDraw.left = currentFrame * frameWidth;
                    frameToDraw.right = frameToDraw.left + frameWidth;
                }
            }
        }

        public void back() {
            Matrix matrix = new Matrix();
            matrix.preScale(-1.0f, 1.0f);
            Bitmap bInput = bitmapGirl;
            bitmapGirl = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);
        }

        public void update() {
            if (isMoving) {
                if (GirlXPosition > (getScreenWidth() - 100) || GirlXPosition < 0) {
                    forward = true;
                    if (forward) {
                        back();
                        forward = false;
                    }
                    walkSpeedPerSecond = -walkSpeedPerSecond;
                }
                if (forward) {
                    back();
                    forward = false;
                }
                GirlXPosition = GirlXPosition + (walkSpeedPerSecond / fps);

            }
        }

        void draw(long startFrameTime) {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 220, 200, 189));

                paint.setColor(Color.argb(255, 67, 67, 67));

                paint.setTextSize(45);

                canvas.drawText("FPS : " + fps, 20, 200, paint);
                canvas.drawText("Height : " + frameHeight + "  Width : " + frameWidth, 20, 240, paint);
                canvas.drawText("Girl Height : " + bitmapGirl.getHeight() + "   Ninja Width : " + bitmapGirl.getHeight(), 20, 280, paint);
                canvas.drawText("Girl X Position : " + startFrameTime, 20, 320, paint);

                whereToDraw.set((int) GirlXPosition, 20, (int) GirlXPosition + frameWidth, frameHeight);
                getCurrentFrame();
                canvas.drawBitmap(bitmapGirl, frameToDraw, whereToDraw, paint);
                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error : ", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;

                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}
