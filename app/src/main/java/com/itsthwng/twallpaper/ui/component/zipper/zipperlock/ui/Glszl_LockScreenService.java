package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import com.itsthwng.twallpaper.R;
import com.itsthwng.twallpaper.local.LocalData;
import com.itsthwng.twallpaper.local.LocalStorage;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ZipLockMainActivity;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_actionLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Layers.Glszl_lockerLayer;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView.Glszl_IndicatorDots;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView.PinLockListener;
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_pinLockView.Glszl_PinLockView;

import java.util.Locale;

public class Glszl_LockScreenService extends Service {
    public static boolean IsPreview = false;
    public static int currentWallpaperId = -1;
    public static int currentWallpaperBgId = -1;
    public static int currentZipperId = -1;
    public static int currentChainId = -1;
    public static int currentFontId = -1;
    public static Glszl_LockScreenService f10546cc;
    public static boolean lockStarted;
    static SoundPool soundPool;
    public String Passcode = "";
    public RelativeLayout PasswordHolder;
    public EditText PasswordInput;
    IntentFilter batteryLevelFilter;
    BroadcastReceiver batteryLevelReceiver;
    boolean batteryRegistred = false;
    int beep;
    public ConstraintLayout errorMessage;
    public ConstraintLayout forgotPassword;
    public CardView forrgot_pass_dialog;
    public ImageView forrgot_pass_dialog_cancel;
    public ImageView forrgot_pass_dialog_ok;
    public TextView forrgot_pass_dialog_question;
    public EditText forrgot_pass_input;
    public Glszl_IndicatorDots indicatorDots;
    public RelativeLayout mOverlay;
    public Glszl_PinLockView f14469pinLockView;
    public boolean showForrgotDialog = false;
    boolean soundActive = false;
    Handler svsHandler;
    boolean vibrateActive = false;
    Vibrator vibrator;

    public Glszl_LockScreenService() {
    }

    public void onStart(Intent intent, int i) {
        super.onStart(intent, i);
        f10546cc = this;
        lockStarted = true;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        String str;
        f10546cc = this;
        this.svsHandler = new Handler();
        InitialWindowAndObjects();
        this.PasswordHolder = this.mOverlay.findViewById(R.id.passwordHolder);
        this.PasswordInput = this.mOverlay.findViewById(R.id.editText);
        this.f14469pinLockView = this.mOverlay.findViewById(R.id.pin_lock_view);
        this.indicatorDots = this.mOverlay.findViewById(R.id.indicator_dots);
        this.forgotPassword = this.mOverlay.findViewById(R.id.forgot_password);
        this.forrgot_pass_dialog = this.mOverlay.findViewById(R.id.forgot_password_dialog);
        this.forrgot_pass_dialog_question = this.mOverlay.findViewById(R.id.forgot_password_question);
        this.forrgot_pass_input = this.mOverlay.findViewById(R.id.forgot_password_input);
        this.forrgot_pass_dialog_cancel = this.mOverlay.findViewById(R.id.forgot_password_cancel);
        this.forrgot_pass_dialog_ok = this.mOverlay.findViewById(R.id.forgot_password_ok);
        this.errorMessage = this.mOverlay.findViewById(R.id.error_message);
        final Glszl_SharedPreferencisUtil sharedPreferencisUtil = new Glszl_SharedPreferencisUtil(f10546cc.getApplicationContext());
        this.f14469pinLockView.attachIndicatorDots(this.indicatorDots);
        if (sharedPreferencisUtil.getSecurityQuestionIsActive()) {
            int securityQIndex = sharedPreferencisUtil.getSequrityQIndex();
            if (securityQIndex == 0) {
                str = getApplicationContext().getResources().getString(R.string.question1);
            } else if (securityQIndex == 1) {
                str = getApplicationContext().getResources().getString(R.string.question2);
            } else if (securityQIndex == 2) {
                str = getApplicationContext().getResources().getString(R.string.question3);
            } else if (securityQIndex != 3) {
                str = getApplicationContext().getResources().getString(R.string.question1);
            } else {
                str = getApplicationContext().getResources().getString(R.string.question4);
            }
            this.forrgot_pass_dialog_question.setText(str);
            this.forgotPassword.setVisibility(View.VISIBLE);
        } else {
            this.forgotPassword.setVisibility(View.INVISIBLE);
        }
        this.forrgot_pass_dialog_ok.setOnClickListener(view -> {
            String forgotAnswer = forrgot_pass_input.getText().toString().toLowerCase().trim();
            if (forgotAnswer.equals(sharedPreferencisUtil.getSecurityQuestion().toLowerCase().trim())) {
                // Hide keyboard before closing dialog
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(forrgot_pass_input.getWindowToken(), 0);
                }
                UnlockFromPasswordCorect();
                showForrgotDialog = false;
                forgotPassword.setVisibility(View.VISIBLE);
                PasswordHolder.setVisibility(View.VISIBLE);
                indicatorDots.setVisibility(View.VISIBLE);
                forrgot_pass_dialog.setVisibility(View.INVISIBLE);
                forrgot_pass_input.setText("");
                return;
            }
            errorMessage.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> errorMessage.setVisibility(View.INVISIBLE), 1000);
        });
        this.forrgot_pass_dialog_cancel.setOnClickListener(view -> {
            // Hide keyboard before closing dialog
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(forrgot_pass_input.getWindowToken(), 0);
            }
            forrgot_pass_dialog.setVisibility(View.GONE);
            forgotPassword.setVisibility(View.VISIBLE);
            PasswordHolder.setVisibility(View.VISIBLE);
            indicatorDots.setVisibility(View.VISIBLE);
            showForrgotDialog = false;
            forrgot_pass_input.setText("");
            f14469pinLockView.updateDots();
        });
        this.forgotPassword.setOnClickListener(view -> {
            forgotPassword.setVisibility(View.INVISIBLE);
            PasswordHolder.setVisibility(View.INVISIBLE);
            indicatorDots.setVisibility(View.INVISIBLE);
            showForrgotDialog = true;
            forrgot_pass_dialog.setVisibility(View.VISIBLE);
            // Request focus and show keyboard for the security question input
            forrgot_pass_input.requestFocus();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(forrgot_pass_input, InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        });
        this.f14469pinLockView.setPinLockListener(new PinLockListener() {
            

            @Override 
            public void onComplete(String str) {
                if (sharedPreferencisUtil.getPin() == null) {
                    UnlockFromPasswordCorect();
                } else if (str.equals(sharedPreferencisUtil.getPin())) {
                    UnlockFromPasswordCorect();
                } else {
                    // Show toast and vibrate for incorrect PIN
                    showIncorrectPinFeedback();
                    f14469pinLockView.clearPin();
                    f14469pinLockView.clearPin();
                    f14469pinLockView.clearPin();
                    f14469pinLockView.clearPin();
                }
            }

            @Override 
            public void onEmpty() {
            }

            @Override 
            public void onPinChange(int i, String str) {
            }
        });
        this.PasswordInput.addTextChangedListener(new TextWatcher() {
            

            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                if (PasswordInput.getText().toString().equals(Passcode)) {
                    UnlockFromPasswordCorect();
                }
            }
        });
        String LoadPassword = Glszl_PasswordAdapter.LoadPassword(this);
        this.Passcode = LoadPassword;
        if (LoadPassword.isEmpty() || this.Passcode.isEmpty()) {
            Glszl_lockerLayer.heart_zipper_PasswordCorrect = !sharedPreferencisUtil.getPinIsActive();
        }
        
        // Always hide PIN screen initially to prevent flashing
        // The PIN screen will be shown by lockerLayer when zipper is dragged to unlock position
        this.PasswordHolder.setVisibility(View.GONE);
        this.indicatorDots.setVisibility(View.GONE);
        this.f14469pinLockView.setVisibility(View.GONE);
        this.forgotPassword.setVisibility(View.GONE);
        
        // Khởi tạo trước UI để hiển thị ngay
        if (this.mOverlay != null) {
            this.mOverlay.setAlpha(0f);
            this.mOverlay.animate().alpha(1f).setDuration(200).start();
        }
        
        // Handle preview mode
        if (IsPreview) {
            // In preview mode, only bypass PIN if PIN is not enabled
            if (!sharedPreferencisUtil.getPinIsActive()) {
                Glszl_lockerLayer.heart_zipper_PasswordCorrect = true;
            }
        }
        batteryLevel();
        soundPool = build(10, 3, 1);
        this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.beep = soundPool.load(this, R.raw.unzip, 1);
        this.soundActive = Glszl_AppAdapter.IsSoundActive(this);
        this.vibrateActive = Glszl_AppAdapter.IsVibrateActive(this);
        if (Build.VERSION.SDK_INT >= 26) {
            return Service.START_STICKY;
        }
        return super.onStartCommand(intent, i, i2);
    }

    public IBinder onBind(Intent intent) {
        f10546cc = this;
        return null;
    }

    public void UnlockFromPasswordCorect() {
        Glszl_lockerLayer.heart_zipper_PasswordCorrect = true;
        if (this.PasswordHolder != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.PasswordHolder.getWindowToken(), 0);
        }
        Glszl_lockerLayer.SlideDownAfterTime(300);
    }

    public void onCreate() {
        super.onCreate();
        InitialForeground();
    }

    public void PlayUnzipSound() {
        if (this.soundActive && soundPool != null) {
            // Play sound in background thread to prevent UI blocking
            new Thread(() -> {
                try {
                    soundPool.play(beep, 1.0f, 1.0f, 0, 0, 1.0f);
                } catch (Exception e) {
                    Log.e("PlayUnzipSound", "Error playing sound: " + e.getMessage());
                }
            }).start();
        }
        Vibrate();
    }

    private void InitialWindowAndObjects() {
        f10546cc = this;

        WindowManager.LayoutParams lp = getLayoutParams();

        // Critical: Set screen brightness to default to prevent screen wake
        lp.screenBrightness = -1.0f;

        lp.gravity = Gravity.TOP;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        lp.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        // Cho phép tràn vùng notch/cutout (API 28+)
        if (Build.VERSION.SDK_INT >= 28) {
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        try {
            Context localeContext = updateLocale(getApplicationContext());
            LayoutInflater inflater = (LayoutInflater) localeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mOverlay = (RelativeLayout) inflater.inflate(R.layout.zipper_lock_screen, null);

            // Quan trọng: tắt behavior tự chèn padding theo insets ở root
            mOverlay.setFitsSystemWindows(false);
            if (Build.VERSION.SDK_INT >= 29) {
                mOverlay.setOnApplyWindowInsetsListener((v, insets) -> {
                    // Consume tất cả insets để layout KHÔNG tự chèn padding dưới
                    return v.onApplyWindowInsets(new WindowInsets.Builder().build());
                });
            }

            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // ===== LẤY KÍCH THƯỚC FULL SCREEN =====
            if (Build.VERSION.SDK_INT >= 30) {
                // bounds đã là full; KHÔNG trừ system bars
                final WindowMetrics metrics = wm.getCurrentWindowMetrics();
                final Rect b = metrics.getBounds();
                lp.width  = b.width();
                lp.height = b.height();
            } else {
                // getRealMetrics => full; KHÔNG dùng getSize
                Display display = wm.getDefaultDisplay();
                DisplayMetrics dm = new DisplayMetrics();
                display.getRealMetrics(dm);
                lp.width  = dm.widthPixels;
                lp.height = dm.heightPixels;
            }

            wm.addView(this.mOverlay, lp);

            // Ẩn status/nav (immersive) cho overlay
            applyImmersiveForOverlay(mOverlay);

            // Nếu user vuốt hiện bars -> tự ẩn lại
            mOverlay.setOnSystemUiVisibilityChangeListener(visibility ->
                    mOverlay.postDelayed(() -> applyImmersiveForOverlay(mOverlay), 50)
            );

            // Đảm bảo root view không có margin/padding đáy vô tình
            if (mOverlay.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mOverlay.getLayoutParams();
                mlp.bottomMargin = mlp.topMargin = mlp.leftMargin = mlp.rightMargin = 0;
            }
            mOverlay.setPadding(0, 0, 0, 0);

            Glszl_GameAdapter.drawer = mOverlay.findViewById(R.id.drawer);
            Glszl_GameAdapter.StartGame(f10546cc);
        } catch (Exception e) {
            Log.i("Probleem a3chiri", "Probleem a3chiri: " + e.getMessage());
        }
    }

    private static WindowManager.LayoutParams getLayoutParams() {
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
    }


    private void applyImmersiveForOverlay(View v) {
        if (Build.VERSION.SDK_INT >= 30) {
            // Với overlay, dùng controller qua ViewCompat
            WindowInsetsController c = v.getWindowInsetsController();
            if (c == null) c = v.getRootWindowInsets() != null ? v.getWindowInsetsController() : null;

            if (c != null) {
                c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                // fallback dưới
                setLegacyUiFlags(v);
            }
        } else {
            setLegacyUiFlags(v);
        }
    }

    private void setLegacyUiFlags(View v) {
        v.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public void onDestroy() {
        f10546cc = null;
        if(IsPreview){
            if(currentWallpaperBgId != -1){
                Glszl_AppAdapter.SaveWallpaperBg(getApplicationContext(), currentWallpaperBgId);
                currentWallpaperBgId = -1;
            }
            if(currentWallpaperId != -1) {
                Glszl_AppAdapter.SaveWallpaper(getApplicationContext(), currentWallpaperId);
                currentWallpaperId = -1;
            }
            if(currentChainId != -1) {
                SaveChain("chain_index", currentChainId);
                Glszl_AppAdapter.SaveChain(getApplicationContext(), currentChainId);
                currentChainId = -1;
            }
            if(currentFontId != -1) {
                Glszl_AppAdapter.SaveFont(getApplicationContext(), currentFontId);
                currentFontId = -1;
            }
            if(currentZipperId != -1) {
                SaveChain("zipper_index", currentZipperId);
                Glszl_AppAdapter.SaveZipper(getApplicationContext(), currentZipperId);
                currentZipperId = -1;
            }

        }
        super.onDestroy();
        System.gc();
    }

    public void SaveChain(String str, Integer num) {
        SharedPreferences.Editor edit = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), 0).edit();
        edit.putInt(str, num).apply();
    }

    public static void Start(final Context context) {
        if (f10546cc != null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(new Intent(context, Glszl_LockScreenService.class));
            return;
        }
        new Thread() {


            public void run() {
                context.startService(new Intent(context, Glszl_LockScreenService.class));
            }
        }.start();
    }

    public SoundPool build(int i, int i2, int i3) {
        return new SoundPool.Builder().build();
    }

    public void Vibrate() {
        if (this.vibrateActive) {
            this.vibrator.vibrate(500);
        }
    }

    private void showIncorrectPinFeedback() {
        // Show toast message
        Toast.makeText(this, getString(R.string.pin_is_incorrect), Toast.LENGTH_SHORT).show();

        // Vibrate with error pattern (short pulse) - always vibrate for incorrect PIN regardless of settings
        if (this.vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create error vibration pattern for Android O+
                VibrationEffect vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
                this.vibrator.vibrate(vibrationEffect);
            } else {
                // Fallback for older Android versions
                this.vibrator.vibrate(100);
            }
        }
    }

    public void runOnUiThread(Runnable runnable) {
        this.svsHandler.post(runnable);
    }

    public void finish() {
        try {
            unregisterReceiver(this.batteryLevelReceiver);
        } catch (Exception e) {
            System.out.println("Finish error: " + e.getMessage());
        }
        Glszl_GameAdapter.close();
        ((WindowManager) f10546cc.getSystemService(Context.WINDOW_SERVICE)).removeViewImmediate(this.mOverlay);
        stopForeground(true);
        stopSelf();
        f10546cc = null;
        lockStarted = false;
    }

    private void batteryLevel() {
        this.batteryLevelReceiver = new BroadcastReceiver() {
            

            public void onReceive(Context context, Intent intent) {
                int i = -1;
                int intExtra = intent.getIntExtra("level", -1);
                int intExtra2 = intent.getIntExtra("scale", -1);
                if (intExtra >= 0 && intExtra2 > 0) {
                    i = (intExtra * 100) / intExtra2;
                }
                Glszl_actionLayer.FixBateryLevelWidth(i);
            }
        };
        if (!this.batteryRegistred) {
            IntentFilter intentFilter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
            this.batteryLevelFilter = intentFilter;
            try {
                registerReceiver(this.batteryLevelReceiver, intentFilter);
            } catch (Exception ignored) {
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    public void InitialForeground() {
        Intent intent = new Intent(this, ZipLockMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent activity = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getPackageName() + "123");
            builder.setSmallIcon(R.drawable.applogo).setContentTitle(getResources().getString(R.string.app_name)).setContentText(getString(R.string.lockscreen_service_running)).setContentIntent(activity).build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            builder.setChannelId(getPackageName() + "123");
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(getPackageName() + "123", "Test", 1);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(-16776961);
            notificationChannel.setSound(null, null);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // Sửa lỗi MissingForegroundServiceTypeException cho Android 14+
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(1337, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            } else {
                startForeground(1337, builder.build());
            }
        } else {
            startForeground(1337, new NotificationCompat.Builder(this).setSmallIcon(R.drawable.applogo).setContentTitle(getResources().getString(R.string.app_name)).setContentText(getString(R.string.lockscreen_service_running)).setContentIntent(activity).build());
        }
    }

    private Context updateLocale(Context context) {
        LocalStorage localStorage = new LocalData(context, "sharedPreferences");
        Resources resources = context.getResources();

        String tag = localStorage.getLangCode();
        Locale locale = Locale.forLanguageTag(tag);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        return context.createConfigurationContext(config);
    }
}
