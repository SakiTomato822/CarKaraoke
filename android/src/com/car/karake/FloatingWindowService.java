package com.car.karake;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/* loaded from: classes.dex */
public class FloatingWindowService extends Service {
    private static final String TAG = "CarKaraoke";
    private View floatingView;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "创建悬浮窗服务");
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        relativeLayout.setBackgroundColor(Color.argb(128, 255, 255, 255));
        relativeLayout.setPadding(8, 8, 8, 8);
        relativeLayout.setBackgroundDrawable(getDrawableWithRoundedCorners(80.0f, 120.0f));
        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        imageView.setLayoutParams(layoutParams);
        if ((getResources().getConfiguration().uiMode & 48) == 32) {
            int identifier = getResources().getIdentifier("maikefeng_2", "drawable", getPackageName());
            if (identifier > 0) {
                imageView.setImageResource(identifier);
            } else {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        } else {
            int identifier2 = getResources().getIdentifier("maikefeng", "drawable", getPackageName());
            if (identifier2 > 0) {
                imageView.setImageResource(identifier2);
            } else {
                imageView.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(0);
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.FloatingWindowService.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Log.i(FloatingWindowService.TAG, "悬浮窗停止按钮被点击");
                FloatingWindowService.this.sendBroadcast(new Intent("com.car.karake.STOP_KARAOKE"));
                Toast.makeText(FloatingWindowService.this.getApplicationContext(), "无麦K歌已关闭", 0).show();
                FloatingWindowService.this.stopSelf();
                Log.i(FloatingWindowService.TAG, "悬浮窗服务已停止");
            }
        });
        relativeLayout.addView(imageView);
        this.floatingView = relativeLayout;
        this.params = new WindowManager.LayoutParams(80, 80, Build.VERSION.SDK_INT >= 26 ? 2038 : 2002, 8, -3);
        this.params.gravity = 8388629;
        this.params.x = 0;
        this.params.y = 0;
        this.windowManager = (WindowManager) getSystemService("window");
        try {
            this.windowManager.addView(this.floatingView, this.params);
            Log.i(TAG, "悬浮窗已添加到WindowManager");
        } catch (Exception e) {
            Log.e(TAG, "添加悬浮窗失败: " + e.getMessage());
            Toast.makeText(this, "添加悬浮窗失败: " + e.getMessage(), 1).show();
            stopSelf();
        }
        this.floatingView.setOnTouchListener(new FloatingTouchListener());
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        if (this.floatingView != null && this.windowManager != null) {
            try {
                this.windowManager.removeView(this.floatingView);
                Log.i(TAG, "悬浮窗已从WindowManager移除");
            } catch (Exception e) {
                Log.e(TAG, "移除悬浮窗失败: " + e.getMessage());
            }
        }
    }

    private class FloatingTouchListener implements View.OnTouchListener {
        private float initialTouchX;
        private float initialTouchY;
        private int initialX;
        private int initialY;

        private FloatingTouchListener() {
        }

        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case 0:
                    this.initialX = FloatingWindowService.this.params.x;
                    this.initialY = FloatingWindowService.this.params.y;
                    this.initialTouchX = motionEvent.getRawX();
                    this.initialTouchY = motionEvent.getRawY();
                    break;
                case 2:
                    FloatingWindowService.this.params.x = this.initialX + ((int) (motionEvent.getRawX() - this.initialTouchX));
                    FloatingWindowService.this.params.y = this.initialY + ((int) (motionEvent.getRawY() - this.initialTouchY));
                    FloatingWindowService.this.windowManager.updateViewLayout(FloatingWindowService.this.floatingView, FloatingWindowService.this.params);
                    break;
            }
            return true;
        }
    }

    private ShapeDrawable getDrawableWithRoundedCorners(float f, float f2) {
        float[] fArr = new float[8];
        float fMin = Math.min(f, f2) / 2.0f;
        for (int i = 0; i < 8; i++) {
            fArr[i] = fMin;
        }
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(fArr, null, null));
        shapeDrawable.getPaint().setColor(Color.argb(128, 255, 255, 255));
        return shapeDrawable;
    }
}
