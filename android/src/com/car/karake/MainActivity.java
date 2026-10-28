package com.car.karake;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    private static final String ACTION_PLAY_EXTERIOR_SOUND = "com.ecarx.action.PLAY_EXTERIOR_SOUND";
    private static final int AUDIO_FORMAT = 2;
    private static final float AUDIO_GAIN = 8.0f;
    private static final int BUFFER_SIZE = 192;
    private static final int EXTERIOR_BUFFER_SIZE = 512;
    private static final int EXTERIOR_CHANNEL_CONFIG = 12;
    private static final int EXTERIOR_INPUT_CHANNEL = 16;
    private static final int EXTERIOR_SAMPLE_RATE = 48000;
    private static final int EXTERIOR_SPEAKER_DEVICE_ID = 14;
    private static final String GITEE_REPO_URL = "https://gitee.com/elliot-wu/CarKaraoke";
    private static final boolean IS_Z10_VERSION = false;
    private static final float LOWPASS_CUTOFF = 3000.0f;
    private static final int MAX_LOG_LINES = 0;
    private static final int REQUEST_OVERLAY_PERMISSION = 300;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_STORAGE_PERMISSION = 400;
    private static final int SAMPLE_RATE = 48000;
    private static final String TAG = "CarKaraoke";
    private static final String VERSION_CHECK_URL = "https://gitee.com/api/v5/repos/elliot-wu/CarKaraoke/releases/latest";
    private static final float VOLUME_BOOST = 1.2f;
    private AudioRecord audioRecord;
    private Handler audioStateCheckHandler;
    private Runnable audioStateCheckRunnable;
    private AudioTrack audioTrack;
    private AudioDeviceInfo cachedExteriorDevice;
    private TextView currentStatusView;
    private TextView dateView;
    private float[] delayBuffer;
    private int delayBufferSize;
    private TextView delayLabel;
    private SeekBar delaySeekBar;
    private TextView delayValueView;
    private TextView echoLabel;
    private SeekBar echoSeekBar;
    private TextView echoValueView;
    private AudioManager exteriorAudioManager;
    private AudioFocusRequest exteriorFocusRequest;
    private TextView gainLabel;
    private SeekBar gainSeekBar;
    private TextView gainValueView;
    private boolean isDarkMode;
    private AudioManager karaokeAudioManager;
    private AudioFocusRequest karaokeFocusRequest;
    private AudioDeviceInfo karaokePreferredDevice;
    private Button mainToggleButton;
    private TextView permissionStatusView;
    private Thread recordingThread;
    private SharedPreferences sharedPreferences;
    private LinearLayout statusCard;
    private View statusIndicator;
    private TextView statusView;
    private LinearLayout toggleContainer;
    private View toggleKnob;
    private Button viewLogsButton;
    private boolean isInstalling = IS_Z10_VERSION;
    private String APK_DOWNLOAD_URL = "";
    private int activeSampleRate = 48000;
    private int CHANNEL_CONFIG = EXTERIOR_INPUT_CHANNEL;
    private float[] lowpassHistory = new float[AUDIO_FORMAT];
    private int[] channelConfigs = {EXTERIOR_INPUT_CHANNEL, EXTERIOR_CHANNEL_CONFIG};
    private String[] channelNames = {"单声道", "立体声"};
    private int currentChannelIndex = MAX_LOG_LINES;
    private int outputChannelConfig = 4;
    private boolean autoStartKaraoke = true;
    private boolean updateNoRemind = IS_Z10_VERSION;
    private boolean isRecording = IS_Z10_VERSION;
    private byte[] monoToStereoBuffer = new byte[384];
    private boolean isFX11Vehicle = IS_Z10_VERSION;
    private int delayTime = MAX_LOG_LINES;
    private float echoLevel = 0.0f;
    private float gainLevel = 15.0f;
    private StringBuilder logs = new StringBuilder();
    private BroadcastReceiver stopKaraokeReceiver = new BroadcastReceiver() { // from class: com.car.karake.MainActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws IllegalStateException {
            Log.i(MainActivity.TAG, "收到停止K歌广播");
            MainActivity.this.addLog(MainActivity.TAG, "收到停止K歌广播");
            if (MainActivity.this.isRecording) {
                MainActivity.this.stopAudioTest();
            }
        }
    };
    private int delayBufferIndex = MAX_LOG_LINES;
    private int exteriorWriteLogCounter = MAX_LOG_LINES;
    private int exteriorWriteErrorCount = MAX_LOG_LINES;

    /* JADX INFO: Access modifiers changed from: private */
    public void addLog(String str, String str2) {
        this.logs.append(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date()) + " [" + str + "] " + str2 + "\n");
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) throws IllegalStateException {
        int color;
        final int color2;
        int color3;
        int color4;
        int color5;
        int color6;
        String str;
        final int i;
        super.onCreate(bundle);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.car.karake.MainActivity.2
            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable th) {
                String str2 = "应用崩溃 - " + th.getMessage() + "\n";
                StackTraceElement[] stackTrace = th.getStackTrace();
                int length = stackTrace.length;
                for (int i2 = MainActivity.MAX_LOG_LINES; i2 < length; i2++) {
                    str2 = str2 + "\tat " + stackTrace[i2].toString() + "\n";
                }
                MainActivity.this.addLog("CRASH", str2);
                Log.e(MainActivity.TAG, str2);
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(thread, th);
            }
        });
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        addLog(TAG, "应用启动，初始化音频功能");
        Log.i(TAG, "应用启动，初始化音频功能");
        Intent intent = getIntent();
        if (intent != null && "INSTALL_COMPLETE".equals(intent.getAction())) {
            Log.i(TAG, "收到安装完成回调，跳过更新检查");
            new Handler().postDelayed(new Runnable() { // from class: com.car.karake.MainActivity.3
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.checkInstallationStatus();
                }
            }, 2000L);
        }
        this.sharedPreferences = getSharedPreferences("CarKaraokeConfig", MAX_LOG_LINES);
        loadConfig();
        checkPermissions();
        if ((intent == null || !"INSTALL_COMPLETE".equals(intent.getAction())) && !this.updateNoRemind) {
            checkForUpdates();
        }
        this.isDarkMode = (getResources().getConfiguration().uiMode & 48) == 32 ? true : IS_Z10_VERSION;
        if (Build.VERSION.SDK_INT >= 17) {
            getApplicationContext().registerComponentCallbacks(new ComponentCallbacks() { // from class: com.car.karake.MainActivity.4
                @Override // android.content.ComponentCallbacks
                public void onConfigurationChanged(Configuration configuration) {
                    boolean z = (configuration.uiMode & 48) == 32 ? true : MainActivity.IS_Z10_VERSION;
                    if (z != MainActivity.this.isDarkMode) {
                        MainActivity.this.isDarkMode = z;
                        MainActivity.this.recreate();
                    }
                }

                @Override // android.content.ComponentCallbacks
                public void onLowMemory() {
                }
            });
        }
        if (this.isDarkMode) {
            color = Color.parseColor("#1e293b");
            color2 = Color.parseColor("#334155");
            color3 = Color.parseColor("#ffffff");
            color4 = Color.parseColor("#94a3b8");
            color5 = Color.parseColor("#3b82f6");
            color6 = Color.parseColor("#22c55e");
            Color.parseColor("#ef4444");
        } else {
            color = Color.parseColor("#f8fafc");
            color2 = Color.parseColor("#ffffff");
            color3 = Color.parseColor("#1e293b");
            color4 = Color.parseColor("#94a3b8");
            color5 = Color.parseColor("#3b82f6");
            color6 = Color.parseColor("#22c55e");
            Color.parseColor("#ef4444");
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#3b82f6"));
            if (this.isDarkMode) {
                getWindow().getDecorView().setSystemUiVisibility(256);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(256);
            }
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        if (Build.VERSION.SDK_INT >= 21 && this.isDarkMode) {
            linearLayout.setBackground(new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[]{Color.parseColor("#1a1c20"), Color.parseColor("#0a0a0c")}));
        } else {
            linearLayout.setBackgroundColor(color);
        }
        linearLayout.setPadding(32, 16, 32, 16);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(MAX_LOG_LINES);
        linearLayout2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout2.setWeightSum(1.0f);
        if (!this.isDarkMode) {
            linearLayout2.setBackgroundColor(Color.parseColor("#f8fafc"));
            linearLayout2.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL);
        }
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 0.6f));
        linearLayout3.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(MAX_LOG_LINES);
        linearLayout4.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        TextView textView = new TextView(this);
        textView.setText("无麦K歌");
        textView.setTextSize(33.6f);
        textView.setTypeface(null, 1);
        textView.setTextColor(color3);
        TextView textView2 = new TextView(this);
        textView2.setText("v2.0.1 横屏适配");
        textView2.setTextSize(16.0f);
        textView2.setTextColor(color4);
        textView2.setPadding(EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES);
        TextView textView3 = new TextView(this);
        textView3.setText("轻松K歌");
        textView3.setTextSize(16.0f);
        textView3.setTextColor(color4);
        linearLayout4.addView(textView);
        linearLayout4.addView(textView2);
        linearLayout3.addView(linearLayout4);
        linearLayout3.addView(textView3);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(1);
        linearLayout5.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 0.4f));
        linearLayout5.setGravity(5);
        linearLayout5.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES);
        this.dateView = new TextView(this);
        this.dateView.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(new Date()));
        this.dateView.setTextSize(14.4f);
        this.dateView.setTextColor(color4);
        LinearLayout linearLayout6 = new LinearLayout(this);
        linearLayout6.setOrientation(MAX_LOG_LINES);
        linearLayout6.setGravity(EXTERIOR_INPUT_CHANNEL);
        this.statusIndicator = new View(this);
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(EXTERIOR_CHANNEL_CONFIG, EXTERIOR_CHANNEL_CONFIG);
        marginLayoutParams.setMargins(MAX_LOG_LINES, MAX_LOG_LINES, 8, MAX_LOG_LINES);
        this.statusIndicator.setLayoutParams(marginLayoutParams);
        this.statusIndicator.setBackgroundColor(color6);
        this.statusIndicator.setPadding(4, 4, 4, 4);
        this.currentStatusView = new TextView(this);
        this.currentStatusView.setText("服务已就绪");
        this.currentStatusView.setTextSize(19.2f);
        this.currentStatusView.setTextColor(color3);
        linearLayout6.addView(this.statusIndicator);
        linearLayout6.addView(this.currentStatusView);
        linearLayout5.addView(this.dateView);
        linearLayout5.addView(linearLayout6);
        linearLayout2.addView(linearLayout3);
        linearLayout2.addView(linearLayout5);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(-1, MAX_LOG_LINES, 1.0f));
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(true);
        LinearLayout linearLayout7 = new LinearLayout(this);
        linearLayout7.setOrientation(1);
        linearLayout7.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout7.setPadding(MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES, 48);
        LinearLayout linearLayout8 = new LinearLayout(this);
        linearLayout8.setOrientation(1);
        linearLayout8.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout8.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL);
        final LinearLayout linearLayout9 = new LinearLayout(this);
        linearLayout9.setOrientation(MAX_LOG_LINES);
        linearLayout9.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout9.setWeightSum(2.0f);
        linearLayout9.setPadding(24, 24, 24, 24);
        if (this.isDarkMode) {
            linearLayout9.setElevation(AUDIO_GAIN);
        } else {
            linearLayout9.setElevation(4.0f);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (this.isDarkMode) {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(color2);
                gradientDrawable.setCornerRadius(24.0f);
                linearLayout9.setBackground(gradientDrawable);
                str = "#ffffff";
            } else {
                GradientDrawable gradientDrawable2 = new GradientDrawable();
                gradientDrawable2.setColor(color2);
                gradientDrawable2.setCornerRadius(24.0f);
                str = "#ffffff";
                gradientDrawable2.setStroke(1, Color.parseColor("#e2e8f0"));
                linearLayout9.setBackground(gradientDrawable2);
            }
            linearLayout9.setOnTouchListener(new View.OnTouchListener() { // from class: com.car.karake.MainActivity.5
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 0) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout9.setElevation(12.0f);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        GradientDrawable gradientDrawable3 = new GradientDrawable();
                        gradientDrawable3.setColor(color2);
                        gradientDrawable3.setCornerRadius(24.0f);
                        gradientDrawable3.setStroke(1, Color.parseColor("#93c5fd"));
                        linearLayout9.setBackground(gradientDrawable3);
                        linearLayout9.setElevation(6.0f);
                        return MainActivity.IS_Z10_VERSION;
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout9.setElevation(MainActivity.AUDIO_GAIN);
                        } else {
                            linearLayout9.setElevation(4.0f);
                        }
                        if (!MainActivity.this.isDarkMode) {
                            GradientDrawable gradientDrawable4 = new GradientDrawable();
                            gradientDrawable4.setColor(color2);
                            gradientDrawable4.setCornerRadius(24.0f);
                            gradientDrawable4.setStroke(1, Color.parseColor("#e2e8f0"));
                            linearLayout9.setBackground(gradientDrawable4);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        return MainActivity.IS_Z10_VERSION;
                    }
                    return MainActivity.IS_Z10_VERSION;
                }
            });
        } else {
            str = "#ffffff";
            linearLayout9.setBackgroundColor(color2);
        }
        LinearLayout linearLayout10 = new LinearLayout(this);
        linearLayout10.setOrientation(1);
        linearLayout10.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        TextView textView4 = new TextView(this);
        textView4.setText("主控开关");
        textView4.setTextSize(22.4f);
        textView4.setTypeface(null, 1);
        textView4.setTextColor(color3);
        TextView textView5 = new TextView(this);
        textView5.setText("唤醒车载音响系统，进入K歌模式");
        textView5.setTextSize(16.0f);
        textView5.setTextColor(color4);
        linearLayout10.addView(textView4);
        linearLayout10.addView(textView5);
        this.toggleContainer = new LinearLayout(this);
        this.toggleContainer.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, 80, 1.0f));
        this.toggleContainer.setOrientation(MAX_LOG_LINES);
        this.toggleContainer.setGravity(EXTERIOR_INPUT_CHANNEL);
        this.toggleContainer.setPadding(24, MAX_LOG_LINES, 24, MAX_LOG_LINES);
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable3 = new GradientDrawable();
            if (this.isDarkMode) {
                gradientDrawable3.setColor(Color.parseColor("#9ca3af"));
            } else {
                gradientDrawable3.setColor(Color.parseColor("#dbeafe"));
            }
            gradientDrawable3.setCornerRadius(32.0f);
            this.toggleContainer.setBackground(gradientDrawable3);
        } else if (this.isDarkMode) {
            this.toggleContainer.setBackgroundColor(Color.parseColor("#9ca3af"));
        } else {
            this.toggleContainer.setBackgroundColor(Color.parseColor("#dbeafe"));
        }
        this.toggleContainer.setElevation(4.0f);
        this.mainToggleButton = new Button(this);
        this.mainToggleButton.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        this.mainToggleButton.setBackgroundColor(MAX_LOG_LINES);
        this.mainToggleButton.setElevation(0.0f);
        this.mainToggleButton.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES);
        this.toggleKnob = new View(this);
        this.toggleKnob.setLayoutParams(new ViewGroup.LayoutParams(56, 56));
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable4 = new GradientDrawable();
            gradientDrawable4.setColor(Color.parseColor(str));
            gradientDrawable4.setCornerRadius(32.0f);
            this.toggleKnob.setBackground(gradientDrawable4);
        } else {
            this.toggleKnob.setBackgroundColor(Color.parseColor(str));
        }
        this.toggleKnob.setElevation(4.0f);
        this.mainToggleButton.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) throws IllegalStateException {
                if (MainActivity.this.isRecording) {
                    MainActivity.this.stopAudioTest();
                } else {
                    MainActivity.this.startAudioTest();
                }
            }
        });
        this.toggleContainer.addView(this.toggleKnob);
        this.toggleContainer.addView(this.mainToggleButton);
        linearLayout9.addView(linearLayout10);
        linearLayout9.addView(this.toggleContainer);
        final LinearLayout linearLayout11 = new LinearLayout(this);
        linearLayout11.setOrientation(1);
        linearLayout11.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout11.setPadding(24, 24, 24, 24);
        if (this.isDarkMode) {
            linearLayout11.setElevation(AUDIO_GAIN);
        } else {
            linearLayout11.setElevation(4.0f);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (this.isDarkMode) {
                GradientDrawable gradientDrawable5 = new GradientDrawable();
                gradientDrawable5.setColor(color2);
                gradientDrawable5.setCornerRadius(24.0f);
                linearLayout11.setBackground(gradientDrawable5);
            } else {
                GradientDrawable gradientDrawable6 = new GradientDrawable();
                gradientDrawable6.setColor(color2);
                gradientDrawable6.setCornerRadius(24.0f);
                gradientDrawable6.setStroke(1, Color.parseColor("#e2e8f0"));
                linearLayout11.setBackground(gradientDrawable6);
            }
            linearLayout11.setOnTouchListener(new View.OnTouchListener() { // from class: com.car.karake.MainActivity.7
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 0) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout11.setElevation(12.0f);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        GradientDrawable gradientDrawable7 = new GradientDrawable();
                        gradientDrawable7.setColor(color2);
                        gradientDrawable7.setCornerRadius(24.0f);
                        gradientDrawable7.setStroke(1, Color.parseColor("#93c5fd"));
                        linearLayout11.setBackground(gradientDrawable7);
                        linearLayout11.setElevation(6.0f);
                        return MainActivity.IS_Z10_VERSION;
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout11.setElevation(MainActivity.AUDIO_GAIN);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        linearLayout11.setElevation(4.0f);
                        GradientDrawable gradientDrawable8 = new GradientDrawable();
                        gradientDrawable8.setColor(color2);
                        gradientDrawable8.setCornerRadius(24.0f);
                        gradientDrawable8.setStroke(1, Color.parseColor("#e2e8f0"));
                        linearLayout11.setBackground(gradientDrawable8);
                        return MainActivity.IS_Z10_VERSION;
                    }
                    return MainActivity.IS_Z10_VERSION;
                }
            });
        } else {
            linearLayout11.setBackgroundColor(color2);
        }
        TextView textView6 = new TextView(this);
        textView6.setText("音效参数");
        textView6.setTextSize(20.8f);
        textView6.setTypeface(null, 1);
        textView6.setTextColor(color3);
        textView6.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 16);
        LinearLayout linearLayout12 = new LinearLayout(this);
        linearLayout12.setOrientation(1);
        linearLayout12.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout12.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 16);
        LinearLayout linearLayout13 = new LinearLayout(this);
        linearLayout13.setOrientation(MAX_LOG_LINES);
        linearLayout13.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout13.setWeightSum(2.0f);
        TextView textView7 = new TextView(this);
        textView7.setText("音效增幅");
        textView7.setTextSize(17.6f);
        textView7.setTextColor(color4);
        textView7.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.gainValueView = new TextView(this);
        this.gainValueView.setText(((int) ((this.gainLevel * 100.0f) / 50.0f)) + "%");
        this.gainValueView.setTextSize(17.6f);
        this.gainValueView.setTextColor(color5);
        this.gainValueView.setTypeface(Typeface.MONOSPACE);
        int i2 = color3;
        this.gainValueView.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.gainValueView.setGravity(5);
        linearLayout13.addView(textView7);
        linearLayout13.addView(this.gainValueView);
        this.gainSeekBar = createCustomSeekBar(this, this.isDarkMode);
        this.gainSeekBar.setMax(100);
        this.gainSeekBar.setProgress((int) ((this.gainLevel * 100.0f) / 50.0f));
        linearLayout12.addView(linearLayout13);
        linearLayout12.addView(this.gainSeekBar);
        LinearLayout linearLayout14 = new LinearLayout(this);
        linearLayout14.setOrientation(1);
        linearLayout14.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout14.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 16);
        LinearLayout linearLayout15 = new LinearLayout(this);
        linearLayout15.setOrientation(MAX_LOG_LINES);
        linearLayout15.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout15.setWeightSum(2.0f);
        TextView textView8 = new TextView(this);
        textView8.setText("混响回声");
        textView8.setTextSize(17.6f);
        textView8.setTextColor(color4);
        textView8.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.echoValueView = new TextView(this);
        this.echoValueView.setText(((int) (this.echoLevel * 100.0f)) + "%");
        this.echoValueView.setTextSize(17.6f);
        this.echoValueView.setTextColor(color5);
        this.echoValueView.setTypeface(Typeface.MONOSPACE);
        int i3 = color2;
        this.echoValueView.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.echoValueView.setGravity(5);
        linearLayout15.addView(textView8);
        linearLayout15.addView(this.echoValueView);
        this.echoSeekBar = createCustomSeekBar(this, this.isDarkMode);
        this.echoSeekBar.setMax(100);
        this.echoSeekBar.setProgress((int) (this.echoLevel * 100.0f));
        linearLayout14.addView(linearLayout15);
        linearLayout14.addView(this.echoSeekBar);
        LinearLayout linearLayout16 = new LinearLayout(this);
        linearLayout16.setOrientation(1);
        linearLayout16.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        LinearLayout linearLayout17 = new LinearLayout(this);
        linearLayout17.setOrientation(MAX_LOG_LINES);
        linearLayout17.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout17.setWeightSum(2.0f);
        TextView textView9 = new TextView(this);
        textView9.setText("信号延迟");
        textView9.setTextSize(17.6f);
        textView9.setTextColor(color4);
        textView9.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.delayValueView = new TextView(this);
        this.delayValueView.setText(this.delayTime + "ms");
        this.delayValueView.setTextSize(17.6f);
        this.delayValueView.setTextColor(color5);
        this.delayValueView.setTypeface(Typeface.MONOSPACE);
        int i4 = color4;
        this.delayValueView.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.delayValueView.setGravity(5);
        linearLayout17.addView(textView9);
        linearLayout17.addView(this.delayValueView);
        this.delaySeekBar = createCustomSeekBar(this, this.isDarkMode);
        this.delaySeekBar.setMax(100);
        this.delaySeekBar.setProgress(this.delayTime);
        linearLayout16.addView(linearLayout17);
        linearLayout16.addView(this.delaySeekBar);
        linearLayout11.addView(textView6);
        linearLayout11.addView(linearLayout12);
        linearLayout11.addView(linearLayout14);
        linearLayout11.addView(linearLayout16);
        linearLayout8.addView(linearLayout9);
        linearLayout8.addView(createVerticalSpacer(24));
        linearLayout8.addView(linearLayout11);
        LinearLayout linearLayout18 = new LinearLayout(this);
        linearLayout18.setOrientation(MAX_LOG_LINES);
        linearLayout18.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout18.setGravity(17);
        linearLayout18.setPadding(MAX_LOG_LINES, 16, MAX_LOG_LINES, MAX_LOG_LINES);
        Button button = new Button(this);
        button.setText("保存方案");
        button.setTextSize(17.6f);
        if (this.isDarkMode) {
            button.setTextColor(Color.parseColor(str));
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable7 = new GradientDrawable();
                gradientDrawable7.setColor(color5);
                gradientDrawable7.setCornerRadius(24.0f);
                button.setBackground(gradientDrawable7);
                i = i3;
            } else {
                button.setBackgroundColor(color5);
                i = i3;
            }
        } else {
            button.setTextColor(color5);
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable8 = new GradientDrawable();
                i = i3;
                gradientDrawable8.setColor(i);
                gradientDrawable8.setCornerRadius(24.0f);
                gradientDrawable8.setStroke(1, Color.parseColor("#e2e8f0"));
                button.setBackground(gradientDrawable8);
            } else {
                i = i3;
                button.setBackgroundColor(i);
            }
        }
        if (this.isDarkMode) {
            button.setElevation(2.0f);
        } else {
            button.setElevation(2.0f);
        }
        button.setPadding(32, EXTERIOR_CHANNEL_CONFIG, 32, EXTERIOR_CHANNEL_CONFIG);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = new ViewGroup.MarginLayoutParams(-2, -2);
        marginLayoutParams2.setMargins(MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES);
        button.setLayoutParams(marginLayoutParams2);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.saveConfig();
            }
        });
        Button button2 = new Button(this);
        button2.setText("恢复默认");
        button2.setTextSize(17.6f);
        if (this.isDarkMode) {
            button2.setTextColor(Color.parseColor(str));
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable9 = new GradientDrawable();
                gradientDrawable9.setColor(color5);
                gradientDrawable9.setCornerRadius(24.0f);
                button2.setBackground(gradientDrawable9);
            } else {
                button2.setBackgroundColor(color5);
            }
        } else {
            button2.setTextColor(color5);
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable10 = new GradientDrawable();
                gradientDrawable10.setColor(i);
                gradientDrawable10.setCornerRadius(24.0f);
                gradientDrawable10.setStroke(1, Color.parseColor("#e2e8f0"));
                button2.setBackground(gradientDrawable10);
            } else {
                button2.setBackgroundColor(i);
            }
        }
        if (this.isDarkMode) {
            button2.setElevation(2.0f);
        } else {
            button2.setElevation(2.0f);
        }
        button2.setPadding(32, EXTERIOR_CHANNEL_CONFIG, 32, EXTERIOR_CHANNEL_CONFIG);
        button2.setLayoutParams(new ViewGroup.MarginLayoutParams(-2, -2));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.9
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.resetToDefaults();
            }
        });
        linearLayout18.addView(button);
        linearLayout18.addView(button2);
        linearLayout8.addView(linearLayout18);
        LinearLayout linearLayout19 = new LinearLayout(this);
        linearLayout19.setOrientation(1);
        linearLayout19.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout19.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 24);
        final LinearLayout linearLayout20 = new LinearLayout(this);
        linearLayout20.setOrientation(1);
        linearLayout20.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout20.setPadding(32, 32, 32, 32);
        if (this.isDarkMode) {
            linearLayout20.setElevation(AUDIO_GAIN);
        } else {
            linearLayout20.setElevation(4.0f);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (this.isDarkMode) {
                GradientDrawable gradientDrawable11 = new GradientDrawable();
                gradientDrawable11.setColor(i);
                gradientDrawable11.setCornerRadius(24.0f);
                linearLayout20.setBackground(gradientDrawable11);
            } else {
                GradientDrawable gradientDrawable12 = new GradientDrawable();
                gradientDrawable12.setColor(i);
                gradientDrawable12.setCornerRadius(24.0f);
                gradientDrawable12.setStroke(1, Color.parseColor("#e2e8f0"));
                linearLayout20.setBackground(gradientDrawable12);
            }
            linearLayout20.setOnTouchListener(new View.OnTouchListener() { // from class: com.car.karake.MainActivity.10
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 0) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout20.setElevation(12.0f);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        GradientDrawable gradientDrawable13 = new GradientDrawable();
                        gradientDrawable13.setColor(i);
                        gradientDrawable13.setCornerRadius(24.0f);
                        gradientDrawable13.setStroke(1, Color.parseColor("#93c5fd"));
                        linearLayout20.setBackground(gradientDrawable13);
                        linearLayout20.setElevation(6.0f);
                        return MainActivity.IS_Z10_VERSION;
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout20.setElevation(MainActivity.AUDIO_GAIN);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        linearLayout20.setElevation(4.0f);
                        GradientDrawable gradientDrawable14 = new GradientDrawable();
                        gradientDrawable14.setColor(i);
                        gradientDrawable14.setCornerRadius(24.0f);
                        gradientDrawable14.setStroke(1, Color.parseColor("#e2e8f0"));
                        linearLayout20.setBackground(gradientDrawable14);
                        return MainActivity.IS_Z10_VERSION;
                    }
                    return MainActivity.IS_Z10_VERSION;
                }
            });
        } else {
            linearLayout20.setBackgroundColor(i);
        }
        TextView textView10 = new TextView(this);
        textView10.setText("常规设置");
        textView10.setTextSize(20.8f);
        textView10.setTypeface(null, 1);
        textView10.setTextColor(i2);
        textView10.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 32);
        LinearLayout linearLayout21 = new LinearLayout(this);
        linearLayout21.setOrientation(MAX_LOG_LINES);
        linearLayout21.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout21.setWeightSum(2.0f);
        linearLayout21.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 32);
        LinearLayout linearLayout22 = new LinearLayout(this);
        linearLayout22.setOrientation(MAX_LOG_LINES);
        linearLayout22.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        LinearLayout linearLayout23 = new LinearLayout(this);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = new ViewGroup.MarginLayoutParams(40, 40);
        marginLayoutParams3.setMargins(MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES);
        linearLayout23.setLayoutParams(marginLayoutParams3);
        linearLayout23.setGravity(17);
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable13 = new GradientDrawable();
            if (this.isDarkMode) {
                gradientDrawable13.setColor(Color.parseColor("#9ca3af"));
            } else {
                gradientDrawable13.setColor(Color.parseColor("#dbeafe"));
            }
            gradientDrawable13.setCornerRadius(12.0f);
            linearLayout23.setBackground(gradientDrawable13);
        } else if (this.isDarkMode) {
            linearLayout23.setBackgroundColor(Color.parseColor("#9ca3af"));
        } else {
            linearLayout23.setBackgroundColor(Color.parseColor("#dbeafe"));
        }
        TextView textView11 = new TextView(this);
        textView11.setText("▶");
        textView11.setTextSize(17.6f);
        if (this.isDarkMode) {
            textView11.setTextColor(Color.parseColor(str));
        } else {
            textView11.setTextColor(Color.parseColor("#3b82f6"));
        }
        linearLayout23.addView(textView11);
        LinearLayout linearLayout24 = new LinearLayout(this);
        linearLayout24.setOrientation(1);
        linearLayout24.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        TextView textView12 = new TextView(this);
        textView12.setText("自动运行");
        textView12.setTextSize(17.6f);
        textView12.setTextColor(i2);
        TextView textView13 = new TextView(this);
        textView13.setText("软件开启时自动开始K歌");
        textView13.setTextSize(14.4f);
        textView13.setTextColor(i4);
        linearLayout24.addView(textView12);
        linearLayout24.addView(textView13);
        linearLayout22.addView(linearLayout23);
        linearLayout22.addView(linearLayout24);
        Switch r5 = new Switch(this);
        r5.setChecked(this.autoStartKaraoke);
        r5.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        r5.setGravity(5);
        r5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.car.karake.MainActivity.11
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.autoStartKaraoke = z;
                Log.i(MainActivity.TAG, "自动开始K歌设置为: " + MainActivity.this.autoStartKaraoke);
                MainActivity.this.saveConfig();
            }
        });
        customizeSwitch(r5);
        linearLayout21.addView(linearLayout22);
        linearLayout21.addView(r5);
        LinearLayout linearLayout25 = new LinearLayout(this);
        linearLayout25.setOrientation(MAX_LOG_LINES);
        linearLayout25.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout25.setWeightSum(2.0f);
        LinearLayout linearLayout26 = new LinearLayout(this);
        linearLayout26.setOrientation(MAX_LOG_LINES);
        linearLayout26.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        LinearLayout linearLayout27 = new LinearLayout(this);
        ViewGroup.MarginLayoutParams marginLayoutParams4 = new ViewGroup.MarginLayoutParams(40, 40);
        marginLayoutParams4.setMargins(MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES);
        linearLayout27.setLayoutParams(marginLayoutParams4);
        linearLayout27.setGravity(17);
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable14 = new GradientDrawable();
            if (this.isDarkMode) {
                gradientDrawable14.setColor(Color.parseColor("#9ca3af"));
            } else {
                gradientDrawable14.setColor(Color.parseColor("#fed7aa"));
            }
            gradientDrawable14.setCornerRadius(12.0f);
            linearLayout27.setBackground(gradientDrawable14);
        } else if (this.isDarkMode) {
            linearLayout27.setBackgroundColor(Color.parseColor("#9ca3af"));
        } else {
            linearLayout27.setBackgroundColor(Color.parseColor("#fed7aa"));
        }
        TextView textView14 = new TextView(this);
        textView14.setText("🔇");
        textView14.setTextSize(17.6f);
        if (this.isDarkMode) {
            textView14.setTextColor(Color.parseColor(str));
        } else {
            textView14.setTextColor(Color.parseColor("#f59e0b"));
        }
        linearLayout27.addView(textView14);
        LinearLayout linearLayout28 = new LinearLayout(this);
        linearLayout28.setOrientation(1);
        linearLayout28.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        TextView textView15 = new TextView(this);
        textView15.setText("勿扰模式");
        textView15.setTextSize(17.6f);
        textView15.setTextColor(i2);
        TextView textView16 = new TextView(this);
        textView16.setText("关闭更新及版本提醒");
        textView16.setTextSize(14.4f);
        textView16.setTextColor(i4);
        linearLayout28.addView(textView15);
        linearLayout28.addView(textView16);
        linearLayout26.addView(linearLayout27);
        linearLayout26.addView(linearLayout28);
        Switch r7 = new Switch(this);
        r7.setChecked(this.updateNoRemind);
        r7.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        r7.setGravity(5);
        r7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.car.karake.MainActivity.12
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MainActivity.this.updateNoRemind = z;
                Log.i(MainActivity.TAG, "更新不再提示设置为: " + MainActivity.this.updateNoRemind);
                MainActivity.this.saveConfig();
            }
        });
        customizeSwitch(r7);
        linearLayout25.addView(linearLayout26);
        linearLayout25.addView(r7);
        linearLayout20.addView(textView10);
        linearLayout20.addView(linearLayout21);
        linearLayout20.addView(linearLayout25);
        LinearLayout linearLayout29 = new LinearLayout(this);
        linearLayout29.setOrientation(MAX_LOG_LINES);
        linearLayout29.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout29.setWeightSum(2.0f);
        linearLayout29.setPadding(MAX_LOG_LINES, 24, MAX_LOG_LINES, MAX_LOG_LINES);
        LinearLayout linearLayout30 = new LinearLayout(this);
        linearLayout30.setOrientation(MAX_LOG_LINES);
        linearLayout30.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        LinearLayout linearLayout31 = new LinearLayout(this);
        ViewGroup.MarginLayoutParams marginLayoutParams5 = new ViewGroup.MarginLayoutParams(40, 40);
        marginLayoutParams5.setMargins(MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES);
        linearLayout31.setLayoutParams(marginLayoutParams5);
        linearLayout31.setGravity(17);
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable15 = new GradientDrawable();
            if (this.isDarkMode) {
                gradientDrawable15.setColor(Color.parseColor("#93c5fd"));
            } else {
                gradientDrawable15.setColor(Color.parseColor("#dbeafe"));
            }
            gradientDrawable15.setCornerRadius(12.0f);
            linearLayout31.setBackground(gradientDrawable15);
        }
        TextView textView17 = new TextView(this);
        textView17.setText("📄");
        textView17.setTextSize(17.6f);
        if (this.isDarkMode) {
            textView17.setTextColor(Color.parseColor(str));
        } else {
            textView17.setTextColor(Color.parseColor("#2563eb"));
        }
        linearLayout31.addView(textView17);
        LinearLayout linearLayout32 = new LinearLayout(this);
        linearLayout32.setOrientation(1);
        linearLayout32.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        TextView textView18 = new TextView(this);
        textView18.setText("运行日志");
        textView18.setTextSize(17.6f);
        textView18.setTextColor(i2);
        TextView textView19 = new TextView(this);
        textView19.setText("查看应用启动以来的日志记录");
        textView19.setTextSize(14.4f);
        textView19.setTextColor(i4);
        linearLayout32.addView(textView18);
        linearLayout32.addView(textView19);
        linearLayout30.addView(linearLayout31);
        linearLayout30.addView(linearLayout32);
        this.viewLogsButton = new Button(this);
        this.viewLogsButton.setText("查看日志");
        this.viewLogsButton.setTextSize(16.0f);
        if (this.isDarkMode) {
            this.viewLogsButton.setTextColor(Color.parseColor(str));
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable16 = new GradientDrawable();
                gradientDrawable16.setColor(Color.parseColor("#3b82f6"));
                gradientDrawable16.setCornerRadius(16.0f);
                this.viewLogsButton.setBackground(gradientDrawable16);
            }
        } else {
            this.viewLogsButton.setTextColor(Color.parseColor("#2563eb"));
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable17 = new GradientDrawable();
                gradientDrawable17.setColor(Color.parseColor("#dbeafe"));
                gradientDrawable17.setCornerRadius(16.0f);
                gradientDrawable17.setStroke(1, Color.parseColor("#bfdbfe"));
                this.viewLogsButton.setBackground(gradientDrawable17);
            }
        }
        this.viewLogsButton.setElevation(2.0f);
        this.viewLogsButton.setPadding(24, 8, 24, 8);
        this.viewLogsButton.setLayoutParams(new LinearLayout.LayoutParams(MAX_LOG_LINES, -2, 1.0f));
        this.viewLogsButton.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.13
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showLogDialog();
            }
        });
        linearLayout29.addView(linearLayout30);
        linearLayout29.addView(this.viewLogsButton);
        linearLayout20.addView(linearLayout29);
        final LinearLayout linearLayout33 = new LinearLayout(this);
        linearLayout33.setOrientation(1);
        linearLayout33.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout33.setPadding(32, 32, 32, 32);
        if (this.isDarkMode) {
            linearLayout33.setElevation(AUDIO_GAIN);
        } else {
            linearLayout33.setElevation(4.0f);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (this.isDarkMode) {
                GradientDrawable gradientDrawable18 = new GradientDrawable();
                gradientDrawable18.setColor(i);
                gradientDrawable18.setCornerRadius(24.0f);
                linearLayout33.setBackground(gradientDrawable18);
            } else {
                GradientDrawable gradientDrawable19 = new GradientDrawable();
                gradientDrawable19.setColor(Color.parseColor("#3b82f6"));
                gradientDrawable19.setCornerRadius(24.0f);
                linearLayout33.setBackground(gradientDrawable19);
            }
            linearLayout33.setOnTouchListener(new View.OnTouchListener() { // from class: com.car.karake.MainActivity.14
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 0) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout33.setElevation(12.0f);
                        } else {
                            linearLayout33.setElevation(6.0f);
                        }
                        if (!MainActivity.this.isDarkMode) {
                            GradientDrawable gradientDrawable20 = new GradientDrawable();
                            gradientDrawable20.setColor(Color.parseColor("#2563eb"));
                            gradientDrawable20.setCornerRadius(24.0f);
                            linearLayout33.setBackground(gradientDrawable20);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        return MainActivity.IS_Z10_VERSION;
                    }
                    if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                        if (MainActivity.this.isDarkMode) {
                            linearLayout33.setElevation(MainActivity.AUDIO_GAIN);
                        } else {
                            linearLayout33.setElevation(4.0f);
                        }
                        if (!MainActivity.this.isDarkMode) {
                            GradientDrawable gradientDrawable21 = new GradientDrawable();
                            gradientDrawable21.setColor(Color.parseColor("#3b82f6"));
                            gradientDrawable21.setCornerRadius(24.0f);
                            linearLayout33.setBackground(gradientDrawable21);
                            return MainActivity.IS_Z10_VERSION;
                        }
                        return MainActivity.IS_Z10_VERSION;
                    }
                    return MainActivity.IS_Z10_VERSION;
                }
            });
        } else if (this.isDarkMode) {
            linearLayout33.setBackgroundColor(i);
        } else {
            linearLayout33.setBackgroundColor(Color.parseColor("#3b82f6"));
        }
        LinearLayout linearLayout34 = new LinearLayout(this);
        linearLayout34.setOrientation(1);
        linearLayout34.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout34.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES);
        linearLayout34.setElevation(AUDIO_GAIN);
        TextView textView20 = new TextView(this);
        textView20.setText("支持开发者");
        textView20.setTextSize(20.8f);
        textView20.setTypeface(null, 1);
        if (this.isDarkMode) {
            textView20.setTextColor(i2);
        } else {
            textView20.setTextColor(Color.parseColor(str));
        }
        TextView textView21 = new TextView(this);
        textView21.setText("邪恶银渐层@小红书 / 先天BUG圣体@懂车帝");
        textView21.setTextSize(14.0f);
        if (this.isDarkMode) {
            textView21.setTextColor(i4);
        } else {
            textView21.setTextColor(Color.parseColor("#dbeafe"));
        }
        textView21.setPadding(MAX_LOG_LINES, 8, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL);
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(REQUEST_OVERLAY_PERMISSION, REQUEST_OVERLAY_PERMISSION);
        layoutParams.gravity = 17;
        layoutParams.setMargins(MAX_LOG_LINES, 8, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        try {
            int identifier = getResources().getIdentifier("donatee", "drawable", getPackageName());
            if (identifier != 0) {
                imageView.setImageResource(identifier);
            }
        } catch (Exception e) {
            Log.e(TAG, "加载赞赏码图片失败: " + e.getMessage());
        }
        TextView textView22 = new TextView(this);
        textView22.setText("扫码支持，感谢您的认可！");
        textView22.setTextSize(14.0f);
        textView22.setGravity(17);
        if (this.isDarkMode) {
            textView22.setTextColor(i4);
        } else {
            textView22.setTextColor(Color.parseColor("#dbeafe"));
        }
        Button button3 = new Button(this);
        button3.setText("检查版本");
        button3.setTextSize(14.0f);
        if (this.isDarkMode) {
            button3.setTextColor(color5);
            button3.setBackgroundColor(MAX_LOG_LINES);
        } else {
            button3.setTextColor(Color.parseColor("#1e40af"));
            if (Build.VERSION.SDK_INT >= 21) {
                GradientDrawable gradientDrawable20 = new GradientDrawable();
                gradientDrawable20.setColor(Color.parseColor("#dbeafe"));
                gradientDrawable20.setCornerRadius(12.0f);
                button3.setBackground(gradientDrawable20);
            }
        }
        button3.setPadding(24, 8, 24, 8);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 17;
        layoutParams2.setMargins(MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL, MAX_LOG_LINES, MAX_LOG_LINES);
        button3.setLayoutParams(layoutParams2);
        button3.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.15
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.checkForUpdates();
            }
        });
        linearLayout34.addView(textView20);
        linearLayout34.addView(textView21);
        linearLayout34.addView(imageView);
        linearLayout34.addView(textView22);
        linearLayout34.addView(button3);
        linearLayout33.addView(linearLayout34);
        linearLayout19.addView(linearLayout20);
        linearLayout19.addView(createVerticalSpacer(32));
        linearLayout19.addView(linearLayout33);
        // Independent scrolling keeps the main controls reachable on short screens.
        linearLayout7.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout7.setPadding(0, 8, 0, 0);
        linearLayout7.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1.0f));
        ScrollView controlsScroll = new ScrollView(this);
        controlsScroll.setFillViewport(true);
        LinearLayout.LayoutParams controlsParams = new LinearLayout.LayoutParams(0, -1, 1.25f);
        controlsParams.setMargins(0, 0, 24, 0);
        controlsScroll.setLayoutParams(controlsParams);
        controlsScroll.addView(linearLayout8);
        ScrollView settingsScroll = new ScrollView(this);
        settingsScroll.setFillViewport(true);
        settingsScroll.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.0f));
        settingsScroll.addView(linearLayout19);
        linearLayout7.addView(controlsScroll);
        linearLayout7.addView(settingsScroll);
        linearLayout.addView(linearLayout2);
        LinearLayout linearLayout35 = new LinearLayout(this);
        linearLayout35.setOrientation(1);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(MAX_LOG_LINES, 8, MAX_LOG_LINES, MAX_LOG_LINES);
        linearLayout35.setLayoutParams(layoutParams3);
        linearLayout35.setPadding(EXTERIOR_INPUT_CHANNEL, EXTERIOR_CHANNEL_CONFIG, EXTERIOR_INPUT_CHANNEL, EXTERIOR_CHANNEL_CONFIG);
        if (Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable21 = new GradientDrawable();
            gradientDrawable21.setColor(i);
            gradientDrawable21.setCornerRadius(12.0f);
            if (!this.isDarkMode) {
                gradientDrawable21.setStroke(1, Color.parseColor("#e2e8f0"));
            }
            linearLayout35.setBackground(gradientDrawable21);
        }
        TextView textView23 = new TextView(this);
        textView23.setText("权限状态");
        textView23.setTextSize(16.0f);
        textView23.setTextColor(i4);
        this.permissionStatusView = new TextView(this);
        this.permissionStatusView.setTextSize(14.4f);
        this.permissionStatusView.setTextColor(i2);
        this.permissionStatusView.setPadding(MAX_LOG_LINES, 8, MAX_LOG_LINES, MAX_LOG_LINES);
        linearLayout35.addView(textView23);
        linearLayout35.addView(this.permissionStatusView);
        linearLayout19.addView(linearLayout35, 1);
        linearLayout.addView(linearLayout7);
        this.delaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.car.karake.MainActivity.16
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i5, boolean z) {
                MainActivity.this.delayTime = i5;
                MainActivity.this.delayValueView.setText(MainActivity.this.delayTime + "ms");
                Log.i(MainActivity.TAG, "延迟时间设置为: " + MainActivity.this.delayTime + "ms");
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.echoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.car.karake.MainActivity.17
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i5, boolean z) {
                MainActivity.this.echoLevel = i5 / 100.0f;
                MainActivity.this.echoValueView.setText(i5 + "%");
                Log.i(MainActivity.TAG, "回声强度设置为: " + MainActivity.this.echoLevel);
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.gainSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // from class: com.car.karake.MainActivity.18
            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onProgressChanged(SeekBar seekBar, int i5, boolean z) {
                MainActivity.this.gainLevel = (i5 * 50.0f) / 100.0f;
                MainActivity.this.gainValueView.setText(i5 + "%");
                Log.i(MainActivity.TAG, "总增益设置为: " + MainActivity.this.gainLevel + "倍");
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override // android.widget.SeekBar.OnSeekBarChangeListener
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setContentView(linearLayout);
        refreshPermissionStatusView();
        this.toggleContainer.post(new Runnable() { // from class: com.car.karake.MainActivity.19
            @Override // java.lang.Runnable
            public void run() {
                LinearLayout linearLayout36 = new LinearLayout(MainActivity.this);
                linearLayout36.setLayoutParams(new LinearLayout.LayoutParams(MainActivity.MAX_LOG_LINES, 80, 1.0f));
                linearLayout36.setOrientation(MainActivity.MAX_LOG_LINES);
                linearLayout36.setGravity(MainActivity.EXTERIOR_INPUT_CHANNEL);
                linearLayout36.setPadding(24, MainActivity.MAX_LOG_LINES, 24, MainActivity.MAX_LOG_LINES);
                if (Build.VERSION.SDK_INT >= 21) {
                    GradientDrawable gradientDrawable22 = new GradientDrawable();
                    if (MainActivity.this.isDarkMode) {
                        gradientDrawable22.setColor(Color.parseColor("#9ca3af"));
                    } else {
                        gradientDrawable22.setColor(Color.parseColor("#dbeafe"));
                    }
                    gradientDrawable22.setCornerRadius(32.0f);
                    linearLayout36.setBackground(gradientDrawable22);
                } else if (MainActivity.this.isDarkMode) {
                    linearLayout36.setBackgroundColor(Color.parseColor("#9ca3af"));
                } else {
                    linearLayout36.setBackgroundColor(Color.parseColor("#dbeafe"));
                }
                View view = new View(MainActivity.this);
                view.setLayoutParams(new ViewGroup.LayoutParams(64, 64));
                if (Build.VERSION.SDK_INT >= 21) {
                    GradientDrawable gradientDrawable23 = new GradientDrawable();
                    gradientDrawable23.setColor(Color.parseColor("#ffffff"));
                    gradientDrawable23.setCornerRadius(32.0f);
                    view.setBackground(gradientDrawable23);
                } else {
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                }
                view.setElevation(4.0f);
                linearLayout36.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.19.1
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view2) throws IllegalStateException {
                        if (MainActivity.this.isRecording) {
                            MainActivity.this.stopAudioTest();
                        } else {
                            MainActivity.this.startAudioTest();
                        }
                    }
                });
                linearLayout36.addView(view);
                MainActivity.this.toggleContainer = linearLayout36;
                MainActivity.this.toggleKnob = view;
                linearLayout9.removeViewAt(1);
                linearLayout9.addView(linearLayout36);
                MainActivity.this.toggleKnob.setTranslationX(0.0f);
            }
        });
        startDateUpdater();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.car.karake.STOP_KARAOKE");
        registerReceiver(this.stopKaraokeReceiver, intentFilter);
        warmupAudioSystem();
        Log.i(TAG, "UI初始化完成，准备开始音频测试");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startDateUpdater() {
        new Handler().postDelayed(new Runnable() { // from class: com.car.karake.MainActivity.20
            @Override // java.lang.Runnable
            public void run() {
                if (MainActivity.this.dateView != null) {
                    MainActivity.this.dateView.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(new Date()));
                }
                MainActivity.this.startDateUpdater();
            }
        }, 60000L);
    }

    private void customizeSwitch(Switch r4) {
        // Preserve the existing listener that saves the setting.
        int[][] states = {new int[]{android.R.attr.state_checked}, new int[]{}};
        int[] colors = {Color.parseColor("#3b82f6"), Color.parseColor("#6b7280")};
        r4.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
        r4.setTrackTintList(new ColorStateList(states, colors));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAudioTest() throws IllegalStateException {
        int i;
        int minBufferSize;
        int i2;
        int i3;
        AudioDeviceInfo routedDevice;
        if (checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
            Log.e(TAG, "录音权限未获取，请检查权限设置");
            addLog(TAG, "录音权限未获取，请检查权限设置");
            Toast.makeText(this, "录音权限未获取，请检查权限设置", 1).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("权限不足");
            builder.setMessage("K歌需要录音权限；未授予不影响其他功能使用。\n\n请在系统设置中授予录音权限：\n设置 -> 应用 -> 无麦K歌 -> 权限 -> 录音");
            builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.22
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    try {
                        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
                        MainActivity.this.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "无法打开设置页面", MainActivity.MAX_LOG_LINES).show();
                    }
                }
            });
            builder.setNegativeButton("取消", (DialogInterface.OnClickListener) null);
            builder.show();
            return;
        }
        if (this.isRecording) {
            addLog(TAG, "已经在K歌中，无需重复开始");
            Log.i(TAG, "已经在K歌中，无需重复开始");
            return;
        }
        addLog(TAG, "开始K歌，初始化AudioRecord和AudioTrack");
        Log.i(TAG, "开始K歌，初始化AudioRecord和AudioTrack");
        if (this.currentStatusView != null) {
            this.currentStatusView.setText("正在K歌...");
            this.currentStatusView.setTextColor(Color.parseColor("#3b82f6"));
        }
        if (this.statusIndicator != null) {
            this.statusIndicator.setBackgroundColor(Color.parseColor("#ef4444"));
        }
        if (this.toggleContainer != null && Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(Color.parseColor("#3b82f6"));
            gradientDrawable.setCornerRadius(32.0f);
            this.toggleContainer.setBackground(gradientDrawable);
        }
        if (this.toggleKnob != null) {
            this.toggleKnob.animate().translationX((this.toggleContainer.getWidth() - this.toggleKnob.getWidth()) - 48).setDuration(300L).setStartDelay(0L).start();
        }
        Toast.makeText(this, "无麦K歌已打开", MAX_LOG_LINES).show();
        if (this.isFX11Vehicle) {
            Log.i(TAG, "✓ FX11车型：强制请求音频焦点，防止音频流被系统停止");
            addLog(TAG, "✓ FX11车型：强制请求音频焦点");
        } else {
            Log.i(TAG, "使用智能音频处理，允许其他音乐继续播放");
        }
        int i4 = this.outputChannelConfig;
        int minBufferSize2 = AudioRecord.getMinBufferSize(48000, this.CHANNEL_CONFIG, AUDIO_FORMAT);
        int minBufferSize3 = AudioTrack.getMinBufferSize(48000, i4, AUDIO_FORMAT);
        if (minBufferSize3 <= 0 && i4 == 4) {
            Log.w(TAG, "单声道输出不支持，切换为立体声输出");
            addLog(TAG, "单声道输出不支持，切换为立体声输出");
            minBufferSize3 = AudioTrack.getMinBufferSize(48000, EXTERIOR_CHANNEL_CONFIG, AUDIO_FORMAT);
            i4 = EXTERIOR_CHANNEL_CONFIG;
        }
        if (minBufferSize2 <= 0 || minBufferSize3 <= 0) {
            Log.w(TAG, "48k不支持，回退到44100Hz");
            addLog(TAG, "48k不支持，回退到44100Hz");
            int minBufferSize4 = AudioRecord.getMinBufferSize(44100, this.CHANNEL_CONFIG, AUDIO_FORMAT);
            int minBufferSize5 = AudioTrack.getMinBufferSize(44100, i4, AUDIO_FORMAT);
            if (minBufferSize5 <= 0 && i4 == 4) {
                i = minBufferSize4;
                minBufferSize = AudioTrack.getMinBufferSize(44100, EXTERIOR_CHANNEL_CONFIG, AUDIO_FORMAT);
                i3 = EXTERIOR_CHANNEL_CONFIG;
                i2 = 44100;
            } else {
                i = minBufferSize4;
                minBufferSize = minBufferSize5;
                i2 = 44100;
                i3 = i4;
            }
        } else {
            i = minBufferSize2;
            minBufferSize = minBufferSize3;
            i2 = 48000;
            i3 = i4;
        }
        if (i <= 0 || minBufferSize <= 0) {
            int i5 = i2;
            Log.e(TAG, "音频参数不支持: sampleRate=" + i5 + ", recordBuffer=" + i + ", playBuffer=" + minBufferSize);
            addLog(TAG, "音频参数不支持，无法启动K歌");
            return;
        }
        this.activeSampleRate = i2;
        try {
            try {
                Log.i(TAG, "系统音频延迟: " + ((AudioManager) getSystemService("audio")).getProperty((String) AudioManager.class.getField("PROPERTY_OUTPUT_LATENCY").get(null)) + "ms");
            } catch (Exception e) {
                Log.i(TAG, "系统不支持获取音频延迟信息");
            }
            int iMax = Math.max(BUFFER_SIZE, i);
            int i6 = i2;
            this.audioRecord = new AudioRecord(1, i6, this.CHANNEL_CONFIG, AUDIO_FORMAT, iMax);
            Log.i(TAG, "AudioRecord初始化成功，使用低延迟设置，缓冲区: " + iMax + " bytes");
            addLog(TAG, "AudioRecord初始化成功，缓冲区: " + iMax + " bytes");
            try {
                int iMax2 = Math.max(BUFFER_SIZE, minBufferSize);
                this.audioTrack = new AudioTrack(3, i6, EXTERIOR_CHANNEL_CONFIG, AUDIO_FORMAT, iMax2, 1);
                Log.i(TAG, "AudioTrack初始化成功（使用简单构造方法），使用音乐流从主扬声器播放");
                addLog(TAG, "AudioTrack初始化成功（简单构造方法）");
                try {
                    if (Build.VERSION.SDK_INT >= 26) {
                        AudioTrack.class.getMethod("setPerformanceMode", Integer.TYPE).invoke(this.audioTrack, Integer.valueOf(AudioTrack.class.getField("PERFORMANCE_MODE_LOW_LATENCY").getInt(null)));
                        Log.i(TAG, "AudioTrack初始化成功，低延迟模式已启用");
                    }
                } catch (Exception e2) {
                    Log.i(TAG, "AudioTrack初始化成功，系统不支持低延迟模式: " + e2.getMessage());
                }
                String str = "立体声";
                Log.i(TAG, "AudioTrack初始化成功，输出声道: " + (i3 == EXTERIOR_CHANNEL_CONFIG ? "立体声" : "单声道") + "，缓冲区: " + iMax2 + " bytes");
                if (i3 != EXTERIOR_CHANNEL_CONFIG) {
                    str = "单声道";
                }
                addLog(TAG, "AudioTrack初始化成功，输出声道: " + str + "，缓冲区: " + iMax2 + " bytes");
                if (this.isFX11Vehicle) {
                    try {
                        this.audioTrack.setVolume(1.0f);
                        Log.i(TAG, "FX11 AudioTrack音量已设置为最大: 1.0");
                        addLog(TAG, "FX11 音量设置: 最大");
                        AudioManager audioManager = (AudioManager) getSystemService("audio");
                        if (audioManager != null) {
                            int streamType = this.audioTrack.getStreamType();
                            int streamMaxVolume = audioManager.getStreamMaxVolume(streamType);
                            int streamVolume = audioManager.getStreamVolume(streamType);
                            Log.i(TAG, "FX11 系统音量检查: 当前=" + streamVolume + ", 最大=" + streamMaxVolume + ", 流类型=" + streamType);
                            addLog(TAG, "FX11 系统音量: " + streamVolume + "/" + streamMaxVolume);
                            double d = streamVolume;
                            double d2 = streamMaxVolume;
                            Double.isNaN(d2);
                            if (d < 0.3d * d2 && streamMaxVolume > 0) {
                                Double.isNaN(d2);
                                int i7 = (int) (d2 * 0.8d);
                                audioManager.setStreamVolume(streamType, i7, 1);
                                Log.i(TAG, "FX11 系统音量已提升到: " + i7 + "/" + streamMaxVolume);
                                addLog(TAG, "FX11 系统音量提升: " + i7);
                                Toast.makeText(this, "已自动调整音量到" + i7, MAX_LOG_LINES).show();
                            }
                        }
                        int i8 = Build.VERSION.SDK_INT;
                    } catch (Exception e3) {
                        Log.e(TAG, "FX11 音频配置失败: " + e3.getMessage());
                    }
                }
                this.delayBufferSize = (int) ((this.activeSampleRate * this.delayTime) / 1000.0f);
                if (this.delayTime > 0) {
                    this.delayBuffer = new float[this.delayBufferSize];
                    this.delayBufferIndex = MAX_LOG_LINES;
                } else {
                    this.delayBuffer = null;
                    this.delayBufferSize = MAX_LOG_LINES;
                }
                if (this.isFX11Vehicle && Build.VERSION.SDK_INT >= 26) {
                    try {
                        this.karaokeAudioManager = (AudioManager) getSystemService("audio");
                        this.karaokeFocusRequest = new AudioFocusRequest.Builder(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(1).setContentType(AUDIO_FORMAT).setFlags(1).build()).setAcceptsDelayedFocusGain(IS_Z10_VERSION).setWillPauseWhenDucked(IS_Z10_VERSION).setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() { // from class: com.car.karake.MainActivity.25
                            @Override // android.media.AudioManager.OnAudioFocusChangeListener
                            public void onAudioFocusChange(int i9) {
                                Log.i(MainActivity.TAG, "FX11 K歌音频焦点变化: " + i9);
                                MainActivity.this.addLog(MainActivity.TAG, "FX11 K歌音频焦点变化: " + i9);
                                switch (i9) {
                                    case -3:
                                    case -2:
                                        Log.i(MainActivity.TAG, "FX11 音频焦点临时丢失，降低音量");
                                        if (MainActivity.this.audioTrack != null) {
                                            try {
                                                MainActivity.this.audioTrack.setVolume(0.3f);
                                                break;
                                            } catch (Exception e4) {
                                                Log.e(MainActivity.TAG, "降低音量失败: " + e4.getMessage());
                                                return;
                                            }
                                        }
                                        break;
                                    case -1:
                                        Log.w(MainActivity.TAG, "FX11 音频焦点永久丢失，停止K歌");
                                        MainActivity.this.addLog(MainActivity.TAG, "FX11 音频焦点永久丢失");
                                        MainActivity.this.runOnUiThread(new Runnable() { // from class: com.car.karake.MainActivity.25.1
                                            @Override // java.lang.Runnable
                                            public void run() throws IllegalStateException {
                                                MainActivity.this.stopAudioTest();
                                            }
                                        });
                                        break;
                                    case 1:
                                        Log.i(MainActivity.TAG, "FX11 重新获得音频焦点，恢复音量");
                                        if (MainActivity.this.audioTrack != null) {
                                            try {
                                                MainActivity.this.audioTrack.setVolume(1.0f);
                                                break;
                                            } catch (Exception e5) {
                                                Log.e(MainActivity.TAG, "恢复音量失败: " + e5.getMessage());
                                                return;
                                            }
                                        }
                                        break;
                                }
                            }
                        }).build();
                        int iRequestAudioFocus = this.karaokeAudioManager.requestAudioFocus(this.karaokeFocusRequest);
                        Log.i(TAG, "FX11 K歌音频焦点请求: " + (iRequestAudioFocus == 1 ? "✓ 成功" : "✗ 失败"));
                        addLog(TAG, "FX11 K歌音频焦点: " + (iRequestAudioFocus == 1 ? "成功" : "失败"));
                        if (iRequestAudioFocus != 1) {
                            Log.e(TAG, "FX11 无法获得音频焦点，K歌可能无声音");
                            addLog(TAG, "FX11 警告：未获得音频焦点");
                            Toast.makeText(this, "音频焦点获取失败，可能影响播放", 1).show();
                        }
                    } catch (Exception e4) {
                        Log.e(TAG, "FX11 音频焦点请求失败: " + e4.getMessage());
                        addLog(TAG, "FX11 音频焦点请求异常: " + e4.getMessage());
                        e4.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "使用智能音频处理，允许其他音乐继续播放");
                }
                this.isRecording = true;
                this.audioRecord.startRecording();
                if (this.isFX11Vehicle && Build.VERSION.SDK_INT >= 23 && this.karaokePreferredDevice != null) {
                    this.audioTrack.setPreferredDevice(this.karaokePreferredDevice);
                    Log.i(TAG, "FX11 play()前再次设置设备路由");
                }
                this.audioTrack.play();
                if (this.isFX11Vehicle) {
                    try {
                        int i9 = this.activeSampleRate / 10;
                        int i10 = i9 * AUDIO_FORMAT;
                        byte[] bArr = new byte[i10];
                        for (int i11 = MAX_LOG_LINES; i11 < i9; i11++) {
                            double d3 = i11;
                            Double.isNaN(d3);
                            double d4 = d3 * 2764.601535159018d;
                            Double.isNaN(this.activeSampleRate);
                            short sSin = (short) (Math.sin(d4 / this.activeSampleRate) * 0.3d * 32767.0d);
                            int i12 = i11 * AUDIO_FORMAT;
                            bArr[i12] = (byte) (sSin & 255);
                            bArr[i12 + 1] = (byte) ((sSin >> 8) & 255);
                        }
                        int iWrite = this.audioTrack.write(bArr, MAX_LOG_LINES, i10);
                        Log.i(TAG, "FX11 测试音已写入，激活音频路径: " + iWrite + " bytes");
                        addLog(TAG, "FX11 测试音已写入: " + iWrite + "字节");
                        if (Build.VERSION.SDK_INT >= 23 && (routedDevice = this.audioTrack.getRoutedDevice()) != null) {
                            Log.i(TAG, "FX11 测试音后路由设备: ID=" + routedDevice.getId() + ", 地址=" + (routedDevice.getAddress() != null ? routedDevice.getAddress() : ""));
                        }
                    } catch (Exception e5) {
                        Log.w(TAG, "FX11 测试音写入失败: " + e5.getMessage());
                    }
                }
                if (this.isFX11Vehicle) {
                    final AudioTrack audioTrack = this.audioTrack;
                    if (this.audioStateCheckHandler == null) {
                        this.audioStateCheckHandler = new Handler(Looper.getMainLooper());
                    }
                    this.audioStateCheckRunnable = new Runnable() { // from class: com.car.karake.MainActivity.26
                        private int consecutiveStoppedCount = MainActivity.MAX_LOG_LINES;
                        private int restartAttempts = MainActivity.MAX_LOG_LINES;

                        @Override // java.lang.Runnable
                        public void run() throws IllegalStateException {
                            if (audioTrack != null && MainActivity.this.isRecording) {
                                int playState = audioTrack.getPlayState();
                                if (playState != 3) {
                                    this.consecutiveStoppedCount++;
                                    Log.w(MainActivity.TAG, "FX11 检测到AudioTrack非播放状态: " + playState + " (连续" + this.consecutiveStoppedCount + "次)");
                                    if (this.consecutiveStoppedCount >= MainActivity.AUDIO_FORMAT && this.restartAttempts < 3) {
                                        this.restartAttempts++;
                                        Log.w(MainActivity.TAG, "FX11 尝试重启AudioTrack (第" + this.restartAttempts + "次尝试)");
                                        MainActivity.this.addLog(MainActivity.TAG, "FX11 重启AudioTrack 第" + this.restartAttempts + "次");
                                        try {
                                            audioTrack.stop();
                                            Thread.sleep(100L);
                                            audioTrack.play();
                                            this.consecutiveStoppedCount = MainActivity.MAX_LOG_LINES;
                                            Log.i(MainActivity.TAG, "FX11 AudioTrack重启成功");
                                            MainActivity.this.addLog(MainActivity.TAG, "FX11 AudioTrack重启成功");
                                        } catch (Exception e6) {
                                            Log.e(MainActivity.TAG, "FX11 重启AudioTrack失败: " + e6.getMessage());
                                            MainActivity.this.addLog(MainActivity.TAG, "FX11 重启失败: " + e6.getMessage());
                                        }
                                    } else if (this.restartAttempts >= 3) {
                                        Log.e(MainActivity.TAG, "FX11 AudioTrack重启次数过多，停止K歌");
                                        MainActivity.this.addLog(MainActivity.TAG, "FX11 重启失败过多次，停止");
                                        MainActivity.this.runOnUiThread(new Runnable() { // from class: com.car.karake.MainActivity.26.1
                                            @Override // java.lang.Runnable
                                            public void run() throws IllegalStateException {
                                                MainActivity.this.stopAudioTest();
                                                Toast.makeText(MainActivity.this, "音频播放异常，已自动停止", 1).show();
                                            }
                                        });
                                        return;
                                    }
                                } else {
                                    if (this.consecutiveStoppedCount > 0) {
                                        Log.i(MainActivity.TAG, "FX11 AudioTrack播放状态恢复正常");
                                    }
                                    this.consecutiveStoppedCount = MainActivity.MAX_LOG_LINES;
                                }
                                if (MainActivity.this.isRecording && MainActivity.this.audioStateCheckHandler != null) {
                                    MainActivity.this.audioStateCheckHandler.postDelayed(this, 1000L);
                                }
                            }
                        }
                    };
                    this.audioStateCheckHandler.postDelayed(this.audioStateCheckRunnable, 1000L);
                    Log.i(TAG, "FX11 音频状态监听已启动，每秒检查一次");
                    addLog(TAG, "FX11 音频状态监听已启动");
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(this)) {
                        Log.i(TAG, "启动悬浮窗服务");
                        startService(new Intent(this, (Class<?>) FloatingWindowService.class));
                    } else {
                        Log.i(TAG, "悬浮窗权限未授予，不影响K歌使用");
                        Toast.makeText(this, "悬浮窗未授予不影响K歌使用", MAX_LOG_LINES).show();
                    }
                }
                Log.i(TAG, "开始录制和播放音频，延迟缓冲区大小: " + this.delayBufferSize);
                this.recordingThread = new Thread(new Runnable() { // from class: com.car.karake.MainActivity.27
                    @Override // java.lang.Runnable
                    public void run() {
                        int i13;
                        byte[] bArr2;
                        int iWrite2;
                        boolean z;
                        Log.i(MainActivity.TAG, "录音线程已启动，开始初始化音频缓冲区");
                        byte[] bArr3 = new byte[MainActivity.BUFFER_SIZE];
                        long jCurrentTimeMillis = System.currentTimeMillis();
                        Log.i(MainActivity.TAG, "录音线程初始化完成，开始处理音频数据");
                        long jNanoTime = 0;
                        long j = 0;
                        long jNanoTime2 = 0;
                        long j2 = 0;
                        while (MainActivity.this.isRecording) {
                            try {
                                long jNanoTime3 = System.nanoTime();
                                long j3 = jCurrentTimeMillis;
                                int i14 = MainActivity.this.audioRecord.read(bArr3, MainActivity.MAX_LOG_LINES, MainActivity.BUFFER_SIZE);
                                jNanoTime += System.nanoTime() - jNanoTime3;
                                if (i14 > 0) {
                                    j++;
                                    if (MainActivity.this.isFX11Vehicle && j % 100 == 0) {
                                        int i15 = MainActivity.MAX_LOG_LINES;
                                        while (true) {
                                            if (i15 < Math.min(i14, 100)) {
                                                if (Math.abs((int) ((short) ((bArr3[i15] & 255) | (bArr3[i15 + 1] << 8)))) <= 100) {
                                                    i15 += MainActivity.AUDIO_FORMAT;
                                                } else {
                                                    z = MainActivity.IS_Z10_VERSION;
                                                    break;
                                                }
                                            } else {
                                                z = true;
                                                break;
                                            }
                                        }
                                        if (z) {
                                            Log.w(MainActivity.TAG, "FX11 警告：检测到静音输入！可能麦克风被禁用或音量为0");
                                            MainActivity.this.addLog(MainActivity.TAG, "FX11 警告：检测到静音输入");
                                        }
                                    }
                                    long jNanoTime4 = System.nanoTime();
                                    MainActivity.this.processAudio(bArr3, i14);
                                    jNanoTime2 += System.nanoTime() - jNanoTime4;
                                    if (MainActivity.this.CHANNEL_CONFIG != MainActivity.EXTERIOR_INPUT_CHANNEL) {
                                        i13 = i14;
                                        bArr2 = bArr3;
                                    } else {
                                        int length = MainActivity.this.monoToStereoBuffer.length;
                                        i13 = i14 * MainActivity.AUDIO_FORMAT;
                                        if (length < i13) {
                                            MainActivity.this.monoToStereoBuffer = new byte[i13];
                                        }
                                        for (int i16 = MainActivity.MAX_LOG_LINES; i16 < i14; i16 += MainActivity.AUDIO_FORMAT) {
                                            byte[] bArr4 = MainActivity.this.monoToStereoBuffer;
                                            int i17 = i16 * MainActivity.AUDIO_FORMAT;
                                            bArr4[i17] = bArr3[i16];
                                            int i18 = i16 + 1;
                                            MainActivity.this.monoToStereoBuffer[i17 + 1] = bArr3[i18];
                                            MainActivity.this.monoToStereoBuffer[i17 + MainActivity.AUDIO_FORMAT] = bArr3[i16];
                                            MainActivity.this.monoToStereoBuffer[i17 + 3] = bArr3[i18];
                                        }
                                        bArr2 = MainActivity.this.monoToStereoBuffer;
                                    }
                                    long jNanoTime5 = System.nanoTime();
                                    if (Build.VERSION.SDK_INT < 23 || (iWrite2 = MainActivity.this.audioTrack.write(bArr2, MainActivity.MAX_LOG_LINES, i13, 1)) == -3 || iWrite2 < 0) {
                                        MainActivity.this.audioTrack.write(bArr2, MainActivity.MAX_LOG_LINES, i13);
                                    }
                                    long jNanoTime6 = (System.nanoTime() - jNanoTime5) + j2;
                                    if (j % 10 == 0 || System.currentTimeMillis() - j3 > 1000) {
                                        double d5 = jNanoTime;
                                        double d6 = j;
                                        Double.isNaN(d5);
                                        Double.isNaN(d6);
                                        double d7 = (d5 / d6) / 1000.0d;
                                        double d8 = jNanoTime2;
                                        Double.isNaN(d8);
                                        Double.isNaN(d6);
                                        double d9 = (d8 / d6) / 1000.0d;
                                        double d10 = jNanoTime6;
                                        Double.isNaN(d10);
                                        Double.isNaN(d6);
                                        double d11 = (d10 / d6) / 1000.0d;
                                        double d12 = d7 + d9 + d11;
                                        Log.i(MainActivity.TAG, "音频处理性能统计 - 读取: " + String.format("%.2f", Double.valueOf(d7)) + "μs, 处理: " + String.format("%.2f", Double.valueOf(d9)) + "μs, 写入: " + String.format("%.2f", Double.valueOf(d11)) + "μs, 总帧时间: " + String.format("%.2f", Double.valueOf(d12)) + "μs, 帧率: " + j);
                                        jCurrentTimeMillis = System.currentTimeMillis();
                                        jNanoTime = 0;
                                        j = 0;
                                        jNanoTime2 = 0;
                                        j2 = 0;
                                    } else {
                                        j2 = jNanoTime6;
                                        jCurrentTimeMillis = j3;
                                    }
                                } else {
                                    Log.w(MainActivity.TAG, "从麦克风读取音频数据失败，读取字节数: " + i14);
                                    jCurrentTimeMillis = j3;
                                }
                            } catch (Exception e6) {
                                Log.e(MainActivity.TAG, "音频处理失败: " + e6.getMessage());
                                e6.printStackTrace();
                            }
                        }
                        Log.i(MainActivity.TAG, "录音线程已停止");
                    }
                });
                Process.setThreadPriority(-19);
                this.recordingThread.setPriority(10);
                this.recordingThread.start();
                Log.i(TAG, "音频测试线程启动，优先级已设置为最高");
            } catch (Exception e6) {
                String str2 = "音频输出初始化失败: " + e6.getMessage();
                Log.e(TAG, str2);
                addLog(TAG, str2);
                if (this.audioRecord != null) {
                    this.audioRecord.release();
                }
                final String message = e6.getMessage();
                runOnUiThread(new Runnable() { // from class: com.car.karake.MainActivity.24
                    @Override // java.lang.Runnable
                    public void run() {
                        Toast.makeText(MainActivity.this, "音频输出初始化失败，请重启应用", 1).show();
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                        builder2.setTitle("初始化失败");
                        builder2.setMessage("音频输出初始化失败\n\n可能原因：\n1. 音频系统配置不支持\n2. 音频设备被占用\n3. 系统资源不足\n\n错误信息: " + message);
                        builder2.setPositiveButton("确定", (DialogInterface.OnClickListener) null);
                        builder2.show();
                    }
                });
            }
        } catch (Exception e7) {
            String str3 = "麦克风初始化失败: " + e7.getMessage();
            Log.e(TAG, str3);
            addLog(TAG, str3);
            runOnUiThread(new Runnable() { // from class: com.car.karake.MainActivity.23
                @Override // java.lang.Runnable
                public void run() {
                    Toast.makeText(MainActivity.this, "麦克风初始化失败，请检查权限或重启应用", 1).show();
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                    builder2.setTitle("初始化失败");
                    builder2.setMessage("麦克风初始化失败\n\n可能原因：\n1. 录音权限未授予\n2. 麦克风被其他应用占用\n3. 系统音频配置不支持\n\n错误信息: " + e7.getMessage());
                    builder2.setPositiveButton("确定", (DialogInterface.OnClickListener) null);
                    builder2.show();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopAudioTest() throws IllegalStateException {
        addLog(TAG, "停止K歌");
        Log.i(TAG, "停止K歌");
        Toast.makeText(this, "无麦K歌已关闭", MAX_LOG_LINES).show();
        this.isRecording = IS_Z10_VERSION;
        if (this.isFX11Vehicle && this.audioStateCheckHandler != null && this.audioStateCheckRunnable != null) {
            this.audioStateCheckHandler.removeCallbacks(this.audioStateCheckRunnable);
            this.audioStateCheckRunnable = null;
            Log.i(TAG, "FX11 音频状态监听已停止");
        }
        if (this.isFX11Vehicle && Build.VERSION.SDK_INT >= 26 && this.karaokeFocusRequest != null && this.karaokeAudioManager != null) {
            try {
                this.karaokeAudioManager.abandonAudioFocusRequest(this.karaokeFocusRequest);
                this.karaokeFocusRequest = null;
                Log.i(TAG, "FX11 K歌音频焦点已释放");
                addLog(TAG, "FX11 K歌音频焦点已释放");
            } catch (Exception e) {
                Log.e(TAG, "FX11 释放音频焦点失败: " + e.getMessage());
            }
        }
        if (this.currentStatusView != null) {
            this.currentStatusView.setText("服务已就绪");
            if (this.isDarkMode) {
                this.currentStatusView.setTextColor(Color.parseColor("#ffffff"));
            } else {
                this.currentStatusView.setTextColor(Color.parseColor("#1e293b"));
            }
        }
        if (this.statusIndicator != null) {
            this.statusIndicator.setBackgroundColor(Color.parseColor("#22c55e"));
        }
        if (this.toggleContainer != null && Build.VERSION.SDK_INT >= 21) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            if (this.isDarkMode) {
                gradientDrawable.setColor(Color.parseColor("#9ca3af"));
            } else {
                gradientDrawable.setColor(Color.parseColor("#dbeafe"));
            }
            gradientDrawable.setCornerRadius(32.0f);
            this.toggleContainer.setBackground(gradientDrawable);
        }
        if (this.toggleKnob != null) {
            this.toggleKnob.animate().translationX(0.0f).setDuration(300L).setStartDelay(0L).start();
        }
        Log.i(TAG, "停止悬浮窗服务");
        stopService(new Intent(this, (Class<?>) FloatingWindowService.class));
        try {
            if (this.recordingThread != null) {
                try {
                    this.recordingThread.join(5000L);
                    if (this.recordingThread.isAlive()) {
                        Log.w(TAG, "线程停止超时，强制中断");
                        this.recordingThread.interrupt();
                    }
                    Log.i(TAG, "K歌线程已停止");
                } catch (InterruptedException e2) {
                    Log.e(TAG, "线程停止失败: " + e2.getMessage());
                }
            }
            if (this.audioRecord != null) {
                this.audioRecord.stop();
                this.audioRecord.release();
                this.audioRecord = null;
                Log.i(TAG, "AudioRecord已释放");
            }
            if (this.audioTrack != null) {
                this.audioTrack.stop();
                this.audioTrack.release();
                this.audioTrack = null;
                Log.i(TAG, "AudioTrack已释放");
            }
        } finally {
            this.recordingThread = null;
        }
    }

    private void updateDelayBuffer() {
        int i = (int) ((this.activeSampleRate * this.delayTime) / 1000.0f);
        if (this.delayBufferSize != i) {
            this.delayBufferSize = i;
            this.delayBuffer = new float[this.delayBufferSize];
            this.delayBufferIndex = MAX_LOG_LINES;
            Log.i(TAG, "延迟缓冲区已更新，新大小: " + this.delayBufferSize);
        }
    }

    private float lowpassFilter(float f, int i) {
        float f2 = 1.0f / this.activeSampleRate;
        this.lowpassHistory[i] = this.lowpassHistory[i] + ((f2 / (5.3051644E-5f + f2)) * (f - this.lowpassHistory[i]));
        return this.lowpassHistory[i];
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void processAudio(byte[] bArr, int i) {
        boolean z = (this.delayTime <= 0 || this.delayBuffer == null || this.delayBufferSize <= 0) ? IS_Z10_VERSION : true;
        if (z) {
            updateDelayBuffer();
        }
        for (int i2 = MAX_LOG_LINES; i2 < i; i2 += AUDIO_FORMAT) {
            int i3 = i2 + 1;
            float f = (short) ((bArr[i2] & 255) | (bArr[i3] << 8));
            float fLowpassFilter = lowpassFilter(f, MAX_LOG_LINES);
            if (z) {
                int length = ((this.delayBufferIndex - this.delayBufferSize) + this.delayBuffer.length) % this.delayBuffer.length;
                if (length < 0) {
                    length += this.delayBuffer.length;
                }
                fLowpassFilter += this.delayBuffer[length] * this.echoLevel;
                this.delayBuffer[this.delayBufferIndex] = f;
                this.delayBufferIndex = (this.delayBufferIndex + 1) % this.delayBuffer.length;
            }
            float f2 = fLowpassFilter * this.gainLevel;
            if (f2 > 32767.0f) {
                f2 = 32766.9f;
            }
            if (f2 < -32768.0f) {
                f2 = -32767.9f;
            }
            short s = (short) f2;
            bArr[i2] = (byte) (s & 255);
            bArr[i3] = (byte) ((s >> 8) & 255);
        }
    }

    private void listAllAudioDevices() {
        if (Build.VERSION.SDK_INT >= 23) {
            AudioDeviceInfo[] devices = ((AudioManager) getSystemService("audio")).getDevices(AUDIO_FORMAT);
            Log.i(TAG, "=== 可用音频输出设备列表 ===");
            Log.i(TAG, "设备总数: " + devices.length);
            for (int i = MAX_LOG_LINES; i < devices.length; i++) {
                AudioDeviceInfo audioDeviceInfo = devices[i];
                String string = audioDeviceInfo.getProductName() != null ? audioDeviceInfo.getProductName().toString() : "unknown";
                String address = audioDeviceInfo.getAddress() != null ? audioDeviceInfo.getAddress() : "none";
                int type = audioDeviceInfo.getType();
                String audioDeviceTypeName = getAudioDeviceTypeName(type);
                int[] sampleRates = audioDeviceInfo.getSampleRates();
                int[] channelMasks = audioDeviceInfo.getChannelMasks();
                Integer numValueOf = Integer.valueOf(i);
                Integer numValueOf2 = Integer.valueOf(type);
                String string2 = "default";
                String string3 = (sampleRates == null || sampleRates.length <= 0) ? "default" : Arrays.toString(sampleRates);
                if (channelMasks != null && channelMasks.length > 0) {
                    string2 = Arrays.toString(channelMasks);
                }
                Log.i(TAG, String.format("设备[%d]: 名称=%s, 地址=%s, 类型=%d(%s), 采样率=%s, 声道=%s", numValueOf, string, address, numValueOf2, audioDeviceTypeName, string3, string2));
            }
            Log.i(TAG, "=== 音频设备列表结束 ===");
        }
    }

    private String getAudioDeviceTypeName(int i) {
        switch (i) {
            case 1:
                return "EARPIECE";
            case AUDIO_FORMAT /* 2 */:
                return "BUILTIN_SPEAKER";
            case 3:
                return "WIRED_HEADSET";
            case 4:
                return "WIRED_HEADPHONES";
            case 5:
                return "LINE_ANALOG";
            case 6:
                return "LINE_DIGITAL";
            case 7:
                return "BLUETOOTH_SCO";
            case 8:
                return "BLUETOOTH_A2DP";
            case 9:
                return "HDMI";
            case 10:
                return "HDMI_ARC";
            case 11:
                return "USB_DEVICE";
            case EXTERIOR_CHANNEL_CONFIG /* 12 */:
                return "USB_ACCESSORY";
            case 13:
                return "DOCK";
            case EXTERIOR_SPEAKER_DEVICE_ID /* 14 */:
                return "FM";
            case 15:
                return "BUILTIN_MIC";
            case EXTERIOR_INPUT_CHANNEL /* 16 */:
                return "FM_TUNER";
            case 17:
                return "TV_TUNER";
            case 18:
                return "TELEPHONY";
            case 19:
                return "AUX_LINE";
            case 20:
                return "IP";
            case 21:
                return "BUS";
            case 22:
                return "USB_HEADSET";
            case 23:
                return "HEARING_AID";
            case 24:
                return "BUILTIN_SPEAKER_SAFE";
            case 25:
                return "REMOTE_SUBMIX";
            case 26:
                return "BLE_HEADSET";
            case 27:
                return "BLE_SPEAKER";
            case 28:
                return "ECHO_REFERENCE";
            case 29:
                return "HDMI_EARC";
            case 30:
                return "BLE_BROADCAST";
            default:
                return "UNKNOWN(" + i + ")";
        }
    }

    private AudioAttributes buildExteriorAudioAttributes(int i) {
        return new AudioAttributes.Builder().setUsage(i).setContentType(4).setFlags(1).build();
    }

    private AudioAttributes buildKaraokeAudioAttributes() {
        return new AudioAttributes.Builder().setUsage(1).setContentType(AUDIO_FORMAT).build();
    }

    private AudioDeviceInfo findInteriorOutputDevice(AudioManager audioManager) {
        String address;
        AudioDeviceInfo audioDeviceInfo;
        String lowerCase;
        int type;
        AudioDeviceInfo audioDeviceInfo2 = null;
        if (Build.VERSION.SDK_INT < 23 || audioManager == null) {
            return null;
        }
        AudioDeviceInfo[] devices = audioManager.getDevices(AUDIO_FORMAT);
        int length = devices.length;
        int i = MAX_LOG_LINES;
        while (true) {
            if (i < length) {
                audioDeviceInfo = devices[i];
                lowerCase = audioDeviceInfo.getProductName() != null ? audioDeviceInfo.getProductName().toString().toLowerCase() : "";
                address = audioDeviceInfo.getAddress() != null ? audioDeviceInfo.getAddress().toLowerCase() : "";
                type = audioDeviceInfo.getType();
                if (address.contains("bus12_aauto_media_out") || ((address.contains("bus12") && address.contains("aauto")) || lowerCase.contains("fx11"))) {
                    this.isFX11Vehicle = true;
                    Log.i(TAG, "✓ 检测到FX11车型，将使用专用音频配置");
                    addLog(TAG, "✓ 检测到FX11车型");
                }
                if (!address.contains("outside") && !address.contains("outside_out") && !lowerCase.contains("outside")) {
                    if (address.contains("media") || address.contains("bus0") || address.contains("bus12") || address.contains("audio_zone_1") || address.contains("system_sound") || address.contains("aauto") || lowerCase.contains("media") || lowerCase.contains("speaker") || lowerCase.contains("system")) {
                        break;
                    }
                    if (audioDeviceInfo2 == null && type == AUDIO_FORMAT) {
                        audioDeviceInfo2 = audioDeviceInfo;
                    }
                }
                i++;
            } else {
                if (audioDeviceInfo2 != null) {
                    String string = audioDeviceInfo2.getProductName() != null ? audioDeviceInfo2.getProductName().toString() : "";
                    address = audioDeviceInfo2.getAddress() != null ? audioDeviceInfo2.getAddress() : "";
                    Log.i(TAG, "使用内置扬声器作为车内播放设备: 名称=" + string + ", 地址=" + address);
                    addLog(TAG, "使用内置扬声器作为车内播放设备: 名称=" + string + ", 地址=" + address);
                }
                return audioDeviceInfo2;
            }
        }
        Log.i(TAG, "找到车内播放设备: 名称=" + lowerCase + ", 地址=" + address + ", 类型=" + type);
        addLog(TAG, "找到车内播放设备: 名称=" + lowerCase + ", 地址=" + address + ", 类型=" + type);
        return audioDeviceInfo;
    }

    private void toggleExteriorSound() {
        Log.i(TAG, "车外喊话功能已在此版本中移除");
        Toast.makeText(this, "车外喊话功能已移除", MAX_LOG_LINES).show();
    }

    private void startExteriorSound() {
        Log.i(TAG, "车外喊话功能已在V1.0.3中移除");
        Toast.makeText(this, "车外喊话功能已移除", MAX_LOG_LINES).show();
    }

    private void stopExteriorSound() {
        Log.i(TAG, "车外喊话功能已在V1.0.3中移除");
    }

    private void warmupAudioSystem() throws IllegalStateException {
        Log.i(TAG, "预热音频系统...");
        try {
            AudioTrack audioTrack = new AudioTrack(3, 48000, 4, AUDIO_FORMAT, 384, 1);
            audioTrack.play();
            audioTrack.write(new byte[256], MAX_LOG_LINES, 256);
            audioTrack.stop();
            audioTrack.release();
            Log.i(TAG, "音频系统预热完成");
        } catch (Exception e) {
            Log.w(TAG, "音频预热失败: " + e.getMessage());
        }
    }

    private void loadConfig() {
        Log.i(TAG, "加载保存的配置");
        this.currentChannelIndex = this.sharedPreferences.getInt("currentChannelIndex", MAX_LOG_LINES);
        this.CHANNEL_CONFIG = this.channelConfigs[this.currentChannelIndex];
        this.delayTime = this.sharedPreferences.getInt("delayTime", MAX_LOG_LINES);
        this.echoLevel = this.sharedPreferences.getFloat("echoLevel", 0.0f);
        this.gainLevel = this.sharedPreferences.getFloat("gainLevel", 15.0f);
        this.autoStartKaraoke = this.sharedPreferences.getBoolean("autoStartKaraoke", IS_Z10_VERSION);
        this.updateNoRemind = this.sharedPreferences.getBoolean("updateNoRemind", IS_Z10_VERSION);
        Log.i(TAG, "配置加载完成: 声道=" + this.channelNames[this.currentChannelIndex] + ", 延迟=" + this.delayTime + "ms, 回声=" + ((int) (this.echoLevel * 100.0f)) + "%, 增益=" + this.gainLevel + "倍");
        Log.i(TAG, "自动开始K歌: " + this.autoStartKaraoke + ", 更新不再提示: " + this.updateNoRemind);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveConfig() {
        Log.i(TAG, "保存当前配置");
        SharedPreferences.Editor editorEdit = this.sharedPreferences.edit();
        editorEdit.putInt("currentChannelIndex", this.currentChannelIndex);
        editorEdit.putInt("delayTime", this.delayTime);
        editorEdit.putFloat("echoLevel", this.echoLevel);
        editorEdit.putFloat("gainLevel", this.gainLevel);
        editorEdit.putBoolean("autoStartKaraoke", this.autoStartKaraoke);
        editorEdit.putBoolean("updateNoRemind", this.updateNoRemind);
        editorEdit.apply();
        Log.i(TAG, "配置保存完成: 声道=" + this.channelNames[this.currentChannelIndex] + ", 延迟=" + this.delayTime + "ms, 回声=" + ((int) (this.echoLevel * 100.0f)) + "%, 增益=" + this.gainLevel + "倍");
        Log.i(TAG, "自动开始K歌: " + this.autoStartKaraoke + ", 更新不再提示: " + this.updateNoRemind);
        Toast.makeText(this, "配置已保存", MAX_LOG_LINES).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void resetToDefaults() {
        this.delayTime = MAX_LOG_LINES;
        this.echoLevel = 0.0f;
        this.gainLevel = 15.0f;
        this.currentChannelIndex = MAX_LOG_LINES;
        this.CHANNEL_CONFIG = this.channelConfigs[this.currentChannelIndex];
        this.autoStartKaraoke = true;
        this.updateNoRemind = IS_Z10_VERSION;
        if (this.delaySeekBar != null) {
            this.delaySeekBar.setProgress(this.delayTime);
        }
        if (this.echoSeekBar != null) {
            this.echoSeekBar.setProgress((int) (this.echoLevel * 100.0f));
        }
        if (this.gainSeekBar != null) {
            this.gainSeekBar.setProgress((int) ((this.gainLevel * 100.0f) / 50.0f));
        }
        if (this.delayLabel != null) {
            this.delayLabel.setText("延迟: " + this.delayTime + "ms");
        }
        if (this.echoLabel != null) {
            this.echoLabel.setText("回声: " + ((int) (this.echoLevel * 100.0f)) + "%");
        }
        if (this.gainLabel != null) {
            this.gainLabel.setText("增幅: " + String.format("%.1f", Float.valueOf(this.gainLevel)) + "倍");
        }
        Log.i(TAG, "已恢复默认音频设置");
        Log.i(TAG, "已恢复默认配置选项: 自动开始K歌=" + this.autoStartKaraoke + ", 更新不再提示=" + this.updateNoRemind);
        this.delayBuffer = null;
        this.delayBufferSize = MAX_LOG_LINES;
        this.delayBufferIndex = MAX_LOG_LINES;
    }

    private void showDonateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("支持开发者");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(32, 32, 32, 32);
        linearLayout.setGravity(17);
        TextView textView = new TextView(this);
        textView.setText("本软件由小红书 @邪恶银渐层 制作\n专为吉利竖屏车型适配的版本\n\n如果您认为该软件对您有用\n欢迎您赞赏我～谢谢您！");
        textView.setTextSize(16.0f);
        textView.setGravity(17);
        if (this.isDarkMode) {
            textView.setTextColor(Color.parseColor("#e5e7eb"));
        } else {
            textView.setTextColor(Color.parseColor("#374151"));
        }
        textView.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, 24);
        linearLayout.addView(textView);
        try {
            ImageView imageView = new ImageView(this);
            int identifier = getResources().getIdentifier("donatee", "drawable", getPackageName());
            if (identifier != 0) {
                imageView.setImageResource(identifier);
            } else {
                TextView textView2 = new TextView(this);
                textView2.setText("(请将 donatee.jpg 放入项目的 drawable 文件夹)");
                textView2.setTextSize(12.0f);
                textView2.setTextColor(Color.parseColor("#9ca3af"));
                textView2.setGravity(17);
                linearLayout.addView(textView2);
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(600, 600);
            layoutParams.gravity = 17;
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            linearLayout.addView(imageView);
        } catch (Exception e) {
            Log.e(TAG, "加载捐赠二维码失败: " + e.getMessage());
        }
        builder.setView(linearLayout);
        builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.28
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void showAboutDialog() {
        int i = getResources().getConfiguration().uiMode;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于车机无麦K歌");
        builder.setMessage("- 版本号：2.0.0 吉利竖屏车型版\n\n- 作者信息：由邪恶银渐层@小红书、先天BUG圣体@懂车帝制作 \n\n- 问题反馈：Elliotwu77@163.com");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.29
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override // android.app.Activity
    protected void onDestroy() throws IllegalStateException {
        super.onDestroy();
        stopAudioTest();
        try {
            unregisterReceiver(this.stopKaraokeReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "广播接收器未注册或已注销");
        }
        Log.i(TAG, "应用销毁，音频资源已释放");
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) throws IllegalStateException {
        super.onNewIntent(intent);
        if (intent != null && "INSTALL_COMPLETE".equals(intent.getAction())) {
            Log.i(TAG, "收到安装完成回调(onNewIntent)，跳过更新检查");
            new Handler().postDelayed(new Runnable() { // from class: com.car.karake.MainActivity.30
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.checkInstallationStatus();
                }
            }, 2000L);
        } else if (intent != null && intent.getBooleanExtra("STOP_KARAOKE", IS_Z10_VERSION)) {
            Log.i(TAG, "收到来自悬浮窗的停止K歌请求");
            stopAudioTest();
        }
    }

    private void refreshPermissionStatusView() {
        if (this.permissionStatusView == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("录音：").append(checkSelfPermission("android.permission.RECORD_AUDIO") == 0 ? "已授予" : "未授予").append("\n");
        sb.append("存储：").append(checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") == 0 ? "已授予" : "未授予").append("\n");
        sb.append("悬浮窗：").append((Build.VERSION.SDK_INT < 23 || !Settings.canDrawOverlays(this)) ? "未授予" : "已授予");
        this.permissionStatusView.setText(sb.toString());
    }

    private void checkPermissions() {
        Log.i(TAG, "检查权限");
        if (checkSelfPermission("android.permission.RECORD_AUDIO") != 0) {
            Log.w(TAG, "录音权限未授予，正在请求");
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            Log.i(TAG, "录音权限已授予");
        }
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0 || checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
            Log.w(TAG, "存储权限未授予，正在请求");
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, REQUEST_STORAGE_PERMISSION);
        } else {
            Log.i(TAG, "存储权限已授予");
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Log.w(TAG, "悬浮窗权限未授予，正在请求");
                startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + getPackageName())), REQUEST_OVERLAY_PERMISSION);
            } else {
                Log.i(TAG, "悬浮窗权限已授予");
            }
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        boolean z;
        super.onRequestPermissionsResult(i, strArr, iArr);
        refreshPermissionStatusView();
        if (i == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (iArr.length > 0 && iArr[MAX_LOG_LINES] == 0) {
                Log.i(TAG, "录音权限已授予");
                return;
            } else {
                Log.i(TAG, "录音权限未授予，不影响其他使用；K歌需录音权限");
                Toast.makeText(this, "未授予录音权限不影响其他使用；开始K歌需授予录音权限", MAX_LOG_LINES).show();
                return;
            }
        }
        if (i == REQUEST_STORAGE_PERMISSION) {
            int length = iArr.length;
            int i2 = MAX_LOG_LINES;
            while (true) {
                if (i2 >= length) {
                    z = true;
                    break;
                } else if (iArr[i2] == 0) {
                    i2++;
                } else {
                    z = IS_Z10_VERSION;
                    break;
                }
            }
            if (z) {
                Log.i(TAG, "存储权限已授予");
            } else {
                Log.i(TAG, "部分或全部存储权限未授予，不影响K歌使用");
                Toast.makeText(this, "存储权限未授予，不影响K歌使用", MAX_LOG_LINES).show();
            }
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == REQUEST_OVERLAY_PERMISSION) {
            refreshPermissionStatusView();
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    Log.i(TAG, "悬浮窗权限已授予");
                } else {
                    Log.i(TAG, "悬浮窗权限未授予，不影响其他使用");
                    Toast.makeText(this, "悬浮窗未授予不影响使用", MAX_LOG_LINES).show();
                }
            }
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        refreshPermissionStatusView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkForUpdates() {
        Log.i(TAG, "检查应用更新");
        new CheckUpdateTask().execute(new Void[MAX_LOG_LINES]);
    }

    private class CheckUpdateTask extends AsyncTask<Void, Void, String[]> {
        private CheckUpdateTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public String[] doInBackground(Void... voidArr)  {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MainActivity.VERSION_CHECK_URL).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(5000);
                if (httpURLConnection.getResponseCode() == MainActivity.REQUEST_RECORD_AUDIO_PERMISSION) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    while (true) {
                        String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    }
                    bufferedReader.close();
                    String strTrim = sb.toString().trim();
                    Log.i(MainActivity.TAG, "=== Gitee API响应开始 ===");
                    Log.i(MainActivity.TAG, strTrim);
                    Log.i(MainActivity.TAG, "=== Gitee API响应结束 ===");
                    Log.i(MainActivity.TAG, "开始解析版本号和下载URL");
                    String versionFromJson = MainActivity.this.parseVersionFromJson(strTrim);
                    String downloadUrlFromJson = MainActivity.this.parseDownloadUrlFromJson(strTrim);
                    Log.i(MainActivity.TAG, "解析结果 - 版本号: " + versionFromJson + ", 下载URL: " + downloadUrlFromJson);
                    if (versionFromJson != null && downloadUrlFromJson != null) {
                        MainActivity.this.APK_DOWNLOAD_URL = downloadUrlFromJson;
                        return new String[]{versionFromJson, downloadUrlFromJson};
                    }
                    return null;
                }
                return null;
            } catch (Exception e) {
                Log.e(MainActivity.TAG, "检查更新失败: " + e.getMessage());
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(String[] strArr) {
            if (strArr != null && strArr.length == MainActivity.AUDIO_FORMAT) {
                String str = strArr[MainActivity.MAX_LOG_LINES];
                String str2 = strArr[1];
                Log.i(MainActivity.TAG, "最新版本: " + str);
                Log.i(MainActivity.TAG, "下载URL: " + str2);
                if (MainActivity.this.isNewVersionAvailable("2.0.0", str)) {
                    Log.i(MainActivity.TAG, "发现新版本: " + str);
                    MainActivity.this.showUpdateDialog(str);
                    return;
                } else {
                    Log.i(MainActivity.TAG, "当前已是最新版本");
                    Toast.makeText(MainActivity.this, "当前已是最新版本", MainActivity.MAX_LOG_LINES).show();
                    return;
                }
            }
            Log.w(MainActivity.TAG, "无法获取最新版本信息");
            Toast.makeText(MainActivity.this, "检查更新失败，请稍后重试", MainActivity.MAX_LOG_LINES).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String parseVersionFromJson(String str) {
        try {
            int iIndexOf = str.indexOf("\"tag_name\":");
            if (iIndexOf != -1) {
                int iIndexOf2 = str.indexOf("\"", iIndexOf + 10);
                int i = iIndexOf2 + 1;
                int iIndexOf3 = str.indexOf("\"", i);
                if (iIndexOf2 != -1 && iIndexOf3 != -1) {
                    String strSubstring = str.substring(i, iIndexOf3);
                    if (strSubstring.startsWith("v")) {
                        strSubstring = strSubstring.substring(1);
                    }
                    Log.i(TAG, "解析到版本号: " + strSubstring);
                    return strSubstring;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "解析版本号失败: " + e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String parseDownloadUrlFromJson(String str) {
        int iIndexOf;
        try {
            int iIndexOf2 = str.indexOf("\"assets\":");
            if (iIndexOf2 != -1 && (iIndexOf = str.indexOf("\"browser_download_url\":", iIndexOf2)) != -1) {
                int iIndexOf3 = str.indexOf("\"", iIndexOf + 22);
                int i = iIndexOf3 + 1;
                int iIndexOf4 = str.indexOf("\"", i);
                if (iIndexOf3 != -1 && iIndexOf4 != -1) {
                    return str.substring(i, iIndexOf4);
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "解析下载URL失败: " + e.getMessage());
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isNewVersionAvailable(String str, String str2) {
        try {
            String[] strArrSplit = str.split("\\.");
            String[] strArrSplit2 = str2.split("\\.");
            int i = MAX_LOG_LINES;
            while (i < Math.max(strArrSplit.length, strArrSplit2.length)) {
                int i2 = i < strArrSplit.length ? Integer.parseInt(strArrSplit[i]) : MAX_LOG_LINES;
                int i3 = i < strArrSplit2.length ? Integer.parseInt(strArrSplit2[i]) : MAX_LOG_LINES;
                if (i3 > i2) {
                    return true;
                }
                if (i3 < i2) {
                    return IS_Z10_VERSION;
                }
                i++;
            }
        } catch (Exception e) {
            Log.e(TAG, "版本比较失败: " + e.getMessage());
        }
        return IS_Z10_VERSION;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUpdateDialog(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发现新版本");
        builder.setMessage("当前版本: 2.0.0\n最新版本: " + str + "\n\n是否更新到最新版本？");
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.31
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                MainActivity.this.downloadAndInstallApk();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.32
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton("不再提示", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.33
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.updateNoRemind = true;
                MainActivity.this.saveConfig();
                dialogInterface.dismiss();
                Toast.makeText(MainActivity.this, "已设置为不再提示更新", MainActivity.MAX_LOG_LINES).show();
            }
        });
        builder.create().show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void downloadAndInstallApk() {
        Log.i(TAG, "开始下载APK");
        new DownloadApkTask().execute(new Void[MAX_LOG_LINES]);
    }

    private class DownloadApkTask extends AsyncTask<Void, Integer, File> {
        private TextView updateStatusView;

        private DownloadApkTask() {
        }

        @Override // android.os.AsyncTask
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "开始下载新版本...", MainActivity.MAX_LOG_LINES).show();
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public File doInBackground(Void... voidArr)  {
            try {
                URLConnection uRLConnectionOpenConnection = new URL(MainActivity.this.APK_DOWNLOAD_URL).openConnection();
                uRLConnectionOpenConnection.connect();
                int contentLength = uRLConnectionOpenConnection.getContentLength();
                InputStream inputStream = uRLConnectionOpenConnection.getInputStream();
                File file = new File(MainActivity.this.getExternalFilesDir(null), "download");
                if (!file.exists()) {
                    file.mkdirs();
                }
                File file2 = new File(file, "CarKaraoke.apk");
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                byte[] bArr = new byte[1024];
                int i = MainActivity.MAX_LOG_LINES;
                while (true) {
                    int i2 = inputStream.read(bArr);
                    if (i2 != -1) {
                        fileOutputStream.write(bArr, MainActivity.MAX_LOG_LINES, i2);
                        i += i2;
                        publishProgress(Integer.valueOf((i * 100) / contentLength));
                    } else {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();
                        return file2;
                    }
                }
            } catch (Exception e) {
                Log.e(MainActivity.TAG, "下载APK失败: " + e.getMessage());
                return null;
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            Log.i(MainActivity.TAG, "下载进度: " + numArr[MainActivity.MAX_LOG_LINES] + "%");
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(File file) {
            if (file == null || !file.exists()) {
                Log.e(MainActivity.TAG, "APK下载失败");
                Toast.makeText(MainActivity.this, "APK下载失败", MainActivity.MAX_LOG_LINES).show();
            } else {
                Log.i(MainActivity.TAG, "APK下载完成: " + file.getAbsolutePath());
                MainActivity.this.showDownloadCompleteDialog(file.getAbsolutePath());
            }
        }
    }

    private void installApk(File file)  {
        File file2;
        int iCreateSession;
        PackageInstaller.Session sessionOpenSession;
        OutputStream outputStreamOpenWrite;
        Log.i(TAG, "开始安装APK");
        Log.i(TAG, "APK文件路径: " + file.getAbsolutePath());
        Log.i(TAG, "APK文件存在: " + file.exists());
        Log.i(TAG, "APK文件大小: " + file.length() + " 字节");
        this.isInstalling = true;
        Log.i(TAG, "=== 开始安装流程 ===");
        Log.i(TAG, "当前应用版本: 1.0.6");
        Log.i(TAG, "尝试安装的APK: " + file.getName());
        int i = MAX_LOG_LINES;
        try {
            Log.i(TAG, "尝试使用PackageInstaller API安装APK");
            PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(1);
            sessionParams.setAppPackageName("com.car.karake");
            iCreateSession = packageInstaller.createSession(sessionParams);
            Log.i(TAG, "创建安装会话成功，sessionId: " + iCreateSession);
            sessionOpenSession = packageInstaller.openSession(iCreateSession);
            Log.i(TAG, "打开安装会话成功");
            outputStreamOpenWrite = sessionOpenSession.openWrite("package", 0L, file.length());
            Log.i(TAG, "打开输出流成功");
            file2 = file;
            FileInputStream fileInputStream = new FileInputStream(file2);
            byte[] bArr = new byte[65536];
            long j = 0;
            while (true) {
                int i2 = fileInputStream.read(bArr);
                if (i2 != -1) {
                    outputStreamOpenWrite.write(bArr, i, i2);
                    j += i2;
                    Log.i(TAG, "写入APK数据: " + j + " / " + file2.length() + " 字节");
                    i = MAX_LOG_LINES;
                } else {
                    fileInputStream.close();
                    outputStreamOpenWrite.flush();
                    outputStreamOpenWrite.close();
                    Log.i(TAG, "APK文件写入完成");
                    sessionOpenSession.commit(createIntentSender(iCreateSession));
                    Log.i(TAG, "PackageInstaller安装请求提交成功");
                    Toast.makeText(this, "正在安装更新...", 1).show();
                    new Handler().postDelayed(new Runnable() { // from class: com.car.karake.MainActivity.34
                        @Override // java.lang.Runnable
                        public void run() {
                            MainActivity.this.checkInstallationStatus();
                        }
                    }, 10000L);
                    return;
                }
            }
        } catch (Exception e) {
            file2 = file;
            Log.e(TAG, "PackageInstaller安装失败: " + e.getMessage(), e);
            Toast.makeText(this, "安装失败: " + e.getMessage(), MAX_LOG_LINES).show();
            Log.i(TAG, "安装流程已简化，用户需使用应用管家手动安装");
            Toast.makeText(this, "请使用应用管家安装更新", MAX_LOG_LINES).show();
            Toast.makeText(this, "APK文件路径: " + file2.getAbsolutePath(), 1).show();
            this.isInstalling = IS_Z10_VERSION;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkInstallationStatus() {
        try {
            try {
                PackageInfo packageInfo = getPackageManager().getPackageInfo("com.car.karake", MAX_LOG_LINES);
                String str = packageInfo.versionName;
                Log.i(TAG, "当前安装版本: " + str + " (code: " + packageInfo.versionCode + ")");
                Log.i(TAG, "更新前版本: 2.0.0");
                if (isNewVersionAvailable("2.0.0", str)) {
                    Log.i(TAG, "安装成功，版本已更新到: " + str);
                    Toast.makeText(this, "安装成功，版本已更新到: " + str, MAX_LOG_LINES).show();
                    Log.i(TAG, "安装成功，应用已更新，请手动打开新版本");
                } else {
                    Log.w(TAG, "安装可能失败，当前版本仍为: " + str);
                    Toast.makeText(this, "安装可能失败，当前版本仍为: " + str, MAX_LOG_LINES).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "检查安装状态失败: " + e.getMessage());
            }
        } finally {
            this.isInstalling = IS_Z10_VERSION;
        }
    }

    private void restartApp() {
        Log.i(TAG, "重启应用");
        try {
            final Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(getPackageName());
            launchIntentForPackage.addFlags(335544320);
            new Handler().postDelayed(new Runnable() { // from class: com.car.karake.MainActivity.35
                @Override // java.lang.Runnable
                public void run() {
                    MainActivity.this.startActivity(launchIntentForPackage);
                    MainActivity.this.finish();
                    System.exit(MainActivity.MAX_LOG_LINES);
                }
            }, 1000L);
        } catch (Exception e) {
            Log.e(TAG, "重启应用失败: " + e.getMessage());
        }
    }

    private IntentSender createIntentSender(int i) {
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.setAction("INSTALL_COMPLETE");
        intent.putExtra("SESSION_ID", i);
        return PendingIntent.getActivity(this, i, intent, 134217728).getIntentSender();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDownloadCompleteDialog(String str) {
        Log.i(TAG, "显示下载完成提示对话框");
        int i = getResources().getConfiguration().uiMode;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("下载完成");
        builder.setMessage("已下载至" + str + "\n\n请使用\"应用管家\"安装更新！\n\n不会更新请看小红书邪恶银渐层更新教程更新！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.36
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
        Log.i(TAG, "下载完成提示对话框显示完成");
    }

    private Button createCustomCard(String str, int i, int i2, int i3, boolean z) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(20.0f);
        button.setTextColor(i2);
        button.setTypeface(null, 1);
        button.setGravity(17);
        button.setLayoutParams(new LinearLayout.LayoutParams(-1, MAX_LOG_LINES, 1.0f));
        button.setPadding(24, 24, 24, 24);
        button.setAllCaps(IS_Z10_VERSION);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(16.0f);
        gradientDrawable.setStroke(AUDIO_FORMAT, Color.parseColor(z ? "#333333" : "#E0E0E0"));
        button.setBackground(gradientDrawable);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.car.karake.MainActivity.37
            @Override // android.view.View.OnClickListener
            public void onClick(final View view) {
                view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(150L).withEndAction(new Runnable() { // from class: com.car.karake.MainActivity.37.1
                    @Override // java.lang.Runnable
                    public void run() {
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150L).start();
                    }
                }).start();
            }
        });
        return button;
    }

    private LinearLayout createStatusCard(String str, int i, int i2, int i3, boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(17);
        linearLayout.setPadding(32, 32, 32, 32);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, MAX_LOG_LINES, 1.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(16.0f);
        gradientDrawable.setStroke(AUDIO_FORMAT, Color.parseColor(z ? "#333333" : "#E0E0E0"));
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(this);
        textView.setText("K歌状态");
        textView.setTextSize(18.0f);
        textView.setTextColor(i2);
        textView.setTypeface(null, 1);
        textView.setPadding(MAX_LOG_LINES, MAX_LOG_LINES, MAX_LOG_LINES, EXTERIOR_INPUT_CHANNEL);
        textView.setGravity(17);
        this.statusView = new TextView(this);
        this.statusView.setText(str);
        this.statusView.setTextSize(24.0f);
        this.statusView.setTextColor(i2);
        this.statusView.setTypeface(null, 1);
        this.statusView.setGravity(17);
        linearLayout.addView(textView);
        linearLayout.addView(this.statusView);
        return linearLayout;
    }

    private void updateStatusCard(boolean z, boolean z2) {
        String str = z2 ? "K歌中" : "等待开始K歌";
        int color = Color.parseColor(z2 ? "#34DBA1" : "#C4BBF3");
        Color.parseColor(z ? "#FFFFFF" : "#000000");
        this.statusView.setText(str);
        ((GradientDrawable) this.statusCard.getBackground()).setColor(color);
    }

    private LinearLayout createControlCard(String str, int i, int i2, boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(32, 32, 32, 32);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, MAX_LOG_LINES, 1.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(16.0f);
        gradientDrawable.setStroke(AUDIO_FORMAT, Color.parseColor(z ? "#333333" : "#E0E0E0"));
        linearLayout.setBackground(gradientDrawable);
        return linearLayout;
    }

    private LinearLayout createConfigCard(String str, boolean z, int i, int i2, int i3, boolean z2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(MAX_LOG_LINES);
        linearLayout.setGravity(EXTERIOR_INPUT_CHANNEL);
        linearLayout.setPadding(32, 32, 32, 32);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, MAX_LOG_LINES, 1.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i3);
        gradientDrawable.setCornerRadius(16.0f);
        gradientDrawable.setStroke(AUDIO_FORMAT, Color.parseColor(z2 ? "#333333" : "#E0E0E0"));
        linearLayout.setBackground(gradientDrawable);
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(20.0f);
        textView.setTextColor(i2);
        textView.setTypeface(null, 1);
        textView.setGravity(17);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 1.0f));
        Switch r8 = new Switch(this);
        r8.setChecked(z);
        int color = z ? Color.parseColor("#34DBA1") : Color.parseColor("#C4BBF3");
        int color2 = z ? Color.parseColor("#34DBA1") : Color.parseColor("#C4BBF3");
        if (Build.VERSION.SDK_INT >= 17) {
            r8.setTrackTintList(ColorStateList.valueOf(color));
            r8.setThumbTintList(ColorStateList.valueOf(color2));
        }
        linearLayout.addView(textView);
        linearLayout.addView(r8);
        return linearLayout;
    }

    private SeekBar createCustomSeekBar(Context context, boolean z) {
        SeekBar seekBar = new SeekBar(context);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(-1, 48));
        if (Build.VERSION.SDK_INT >= 21) {
            int color = Color.parseColor("#00C8FF");
            seekBar.setThumbTintList(ColorStateList.valueOf(color));
            seekBar.setProgressTintList(ColorStateList.valueOf(color));
            seekBar.setSecondaryProgressTintList(ColorStateList.valueOf(color));
        }
        return seekBar;
    }

    private View createVerticalSpacer(int i) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, i));
        return view;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLogDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 无麦K歌应用日志 ===\n\n");
        sb.append("当前状态：\n");
        sb.append("- 录音状态: " + (this.isRecording ? "正在录音" : "未录音") + "\n");
        sb.append("- 自动开始K歌: " + this.autoStartKaraoke + "\n");
        sb.append("- 更新不再提示: " + this.updateNoRemind + "\n\n");
        sb.append("音频设置：\n");
        sb.append("- 延迟时间: " + this.delayTime + "ms\n");
        sb.append("- 回声强度: " + ((int) (this.echoLevel * 100.0f)) + "%\n");
        sb.append("- 增幅倍数: " + this.gainLevel + "倍\n");
        sb.append("- 当前声道: " + this.channelNames[this.currentChannelIndex] + "\n");
        sb.append("- 输出声道: 单声道\n\n");
        sb.append("系统信息：\n");
        sb.append("- Android版本: " + Build.VERSION.RELEASE + "\n");
        sb.append("- 应用版本: 2.0.0\n\n");
        sb.append("运行日志：\n");
        if (this.logs.length() == 0) {
            sb.append("(暂无日志)");
        } else {
            sb.append(this.logs.toString());
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("应用日志");
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(sb.toString());
        textView.setTextSize(12.0f);
        textView.setPadding(EXTERIOR_INPUT_CHANNEL, EXTERIOR_INPUT_CHANNEL, EXTERIOR_INPUT_CHANNEL, EXTERIOR_INPUT_CHANNEL);
        textView.setTextColor(Color.parseColor("#000000"));
        textView.setTypeface(Typeface.MONOSPACE);
        scrollView.addView(textView);
        builder.setView(scrollView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: com.car.karake.MainActivity.38
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
