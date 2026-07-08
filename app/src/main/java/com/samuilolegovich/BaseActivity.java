package com.samuilolegovich;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.AntiDebugDetector;
import com.samuilolegovich.utils.AudioHelper;
import com.samuilolegovich.utils.InactivityGuard;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SessionPin;
import com.samuilolegovich.utils.UiSoundPlayer;
import com.samuilolegovich.view.Lost;
import com.samuilolegovich.view.RouletteResult;
import com.samuilolegovich.view.SelectGame;
import com.samuilolegovich.view.Settings;
import com.samuilolegovich.view.SlotResult;
import com.samuilolegovich.view.Win;
import com.samuilolegovich.view.YourReferral;
import com.samuilolegovich.viewmodel.NavigationEvent;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.util.Locale;
import dagger.hilt.android.AndroidEntryPoint;
import android.widget.TextView;




/**
 * Базовый класс для всех Activity приложения.
 * Централизует общую для всех экранов логику: применение языка интерфейса,
 * автоблокировку по неактивности, анти-debug проверку, анимацию логотипа
 * и синхронизацию нижней навигации — чтобы не дублировать этот код в каждом экране.
 */
@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {

    /** Применяет сохранённый язык интерфейса до вызова super.onCreate, чтобы Activity сразу создавалась с нужной локалью. Запрещает скриншоты и отображение содержимого в switcher задач в production-сборке. */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        applyLocale();
        if (!BuildConfig.DEBUG) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
        // Разрешаем приложению рисовать за статус-баром и навигацией —
        // отступы управляются вручную через WindowInsets (см. setContentView ниже)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);

        WalletRepository.getInstance().getNavigationEventLiveData().observe(this, event -> {
            if (event == null) return;
            // Не открываем экраны результата поверх самих же экранов результата
            if (this instanceof Win || this instanceof Lost || this instanceof YourReferral
                    || this instanceof SlotResult || this instanceof RouletteResult) return;
            switch (event.type) {
                case NavigationEvent.LOST:
                    Lost.MASSAGE = event.message;
                    startActivity(new Intent(Lost.LOST_CLASS));
                    break;
                case NavigationEvent.WIN:
                    Win.MASSAGE = event.message;
                    startActivity(new Intent(Win.WIN_CLASS));
                    break;
                case NavigationEvent.YOUR_REFERRAL:
                    YourReferral.MASSAGE = event.message;
                    startActivity(new Intent(YourReferral.YOUR_REFERRAL_CLASS));
                    break;
                case NavigationEvent.SLOT_RESULT:
                    startActivity(new Intent(SlotResult.SLOT_RESULT_CLASS));
                    break;
                case NavigationEvent.ROULETTE_RESULT:
                    startActivity(new Intent(RouletteResult.ROULETTE_RESULT_CLASS));
                    break;
            }
            WalletRepository.getInstance().clearNavigationEvent();
        });
    }

    /**
     * Перехватывает установку разметки и применяет отступы для статус-бара,
     * выреза камеры и боковых insets. Bottom не трогаем глобально —
     * его обрабатывает BottomNavigationView через setupBottomNav().
     */
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets safe = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() |
                    WindowInsetsCompat.Type.displayCutout());
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return insets;
        });
    }

    /** При каждом возвращении экрана на передний план повторно выполняет проверки безопасности и синхронизацию UI. */
    @Override
    protected void onResume() {
        super.onResume();
        checkAutoLock();
        InactivityGuard.onForeground(); // сброс таймера ПОСЛЕ проверки, иначе блокировка не сработает
        syncBottomNavSelection();
        animateLogo();
        checkAntiDebug();
        updateGameModeBadge();
    }

    /**
     * Запускает в фоновом потоке проверку на отладчик/Frida.
     * Делается при каждом возврате экрана в foreground, а не только при запуске приложения,
     * так как инструменты динамического анализа могут быть подключены в любой момент.
     */
    private void checkAntiDebug() {
        new Thread(() -> {
            if (AntiDebugDetector.isDetected()) {
                runOnUiThread(this::showAntiDebugWarning);
            }
        }).start();
    }

    /** Показывает блокирующий диалог об обнаружении отладчика/Frida и закрывает приложение по нажатию кнопки. */
    private void showAntiDebugWarning() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle("Обнаружена попытка анализа приложения")
                .setMessage(
                        "На устройстве обнаружены признаки инструментов динамического анализа " +
                        "(отладчик или Frida).\n\n" +
                        "Такие инструменты позволяют перехватывать данные приложения в реальном " +
                        "времени, включая seed-фразу и подписываемые транзакции.\n\n" +
                        "Приложение будет закрыто.")
                .setCancelable(false)
                .setPositiveButton("Выйти", (d, w) -> finishAffinity())
                .show();
    }

    /** Анимирует появление логотипа XURA (масштаб + прозрачность) при возврате экрана на передний план. */
    private void animateLogo() {
        View logo = findViewById(R.id.logo_xura);
        if (logo == null) return;

        if (!Settings.isAnimationsEnabled(this)) {
            logo.setScaleX(1f);
            logo.setScaleY(1f);
            logo.setAlpha(1f);
            return;
        }

        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setAlpha(0f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(logo, "alpha",  0f, 1f);

        OvershootInterpolator overshoot = new OvershootInterpolator(1.3f);
        scaleX.setInterpolator(overshoot);
        scaleY.setInterpolator(overshoot);
        scaleX.setDuration(550);
        scaleY.setDuration(550);
        alpha.setDuration(350);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.start();
    }

    /**
     * Признак того, что экран не должен запускать автоблокировку.
     * Экраны ввода/смены пароля переопределяют это, чтобы не попасть в петлю
     * (иначе экран ввода пароля сам бы запрашивал ввод пароля).
     */
    protected boolean isLockExempt() {
        return false;
    }

    /** Проверяет, истёк ли таймаут неактивности, и при наличии установленного пароля перенаправляет на экран его ввода. */
    private void checkAutoLock() {
        if (isLockExempt() || !InactivityGuard.isLockRequired()) return;

        SharedPreferences prefs = PrefsHelper.get(this);
        String password = prefs.getString(StringEnum.APP_PREFERENCES_PASSWORD.getValue(), "");
        boolean hasPassword = !password.isEmpty()
                && !password.equalsIgnoreCase(StringEnum.APP_PREFERENCES_PASSWORD_NOT_INSTALLED.getValue());
        if (!hasPassword) return;

        InactivityGuard.reset();
        MainActivity.START_FLAG = true;
        SessionPin.clear();
        startActivity(new Intent(".EnterApplicationPassword"));
    }

    /**
     * Подключает обработчик нажатий нижней навигации, переключающий между основными вкладками.
     * Нужно вызывать из onCreate после setContentView в MainActivity, SelectGame и Settings.
     */
    protected void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) return;

        // Сдвигаем плавающий nav вверх на высоту системной навигационной панели,
        // чтобы он не перекрывался жестовой полосой или кнопками навигации
        ViewCompat.setOnApplyWindowInsetsListener(nav, (v, insets) -> {
            // content уже имеет bottom padding = navBarHeight (см. setContentView),
            // поэтому здесь достаточно только визуального отступа от края контента
            int baseMargin = (int) (16 * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseMargin;
            v.setLayoutParams(lp);
            return insets;
        });

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_wallet && !(this instanceof MainActivity)) {
                navigateToTab(MainActivity.MAIN_ACTIVITY_CLASS);
            } else if (id == R.id.nav_games && !(this instanceof SelectGame)) {
                navigateToTab(SelectGame.SELECT_GAME_CLASS);
            } else if (id == R.id.nav_settings && !(this instanceof Settings)) {
                navigateToTab(Settings.SETTINGS_CLASS);
            }
            return true;
        });
    }

    /**
     * Выставляет в нижней навигации пункт, соответствующий текущей Activity.
     * Вызывается в onResume — то есть после onRestoreInstanceState, поэтому
     * выставленное значение не будет перебито восстановлением состояния.
     */
    private void syncBottomNavSelection() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) return;

        int targetId;
        if (this instanceof MainActivity) {
            targetId = R.id.nav_wallet;
        } else if (this instanceof SelectGame) {
            targetId = R.id.nav_games;
        } else if (this instanceof Settings) {
            targetId = R.id.nav_settings;
        } else {
            return;
        }

        if (nav.getSelectedItemId() != targetId) {
            nav.setSelectedItemId(targetId);
        }
    }

    /** Переключается на Activity-вкладку по её action, не пересоздавая её, если она уже есть в стеке. */
    private void navigateToTab(String action) {
        Intent intent = new Intent(action);
        // Переиспользуем существующую Activity из стека без пересоздания
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        // Убираем анимацию перехода — это смена вкладки, не навигация вглубь
        overridePendingTransition(0, 0);
    }

    /** Все переходы вглубь — slide+fade справа. Вкладки переопределяют это через overridePendingTransition(0,0) после вызова. */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /** При возврате назад экран уезжает вправо, предыдущий въезжает слева. */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /** Виды Snackbar-уведомлений, влияющие на префикс-иконку и цвет текста. */
    public enum SnackbarType { SUCCESS, ERROR, INFO }

    /** Показывает Snackbar с цветом и префиксом, зависящими от типа уведомления (успех/ошибка/инфо). */
    protected void showSnackbar(View root, String message, SnackbarType type) {
        String prefix;
        int colorRes;
        switch (type) {
            case SUCCESS:
                prefix = "✓ ";
                colorRes = R.color.xura_cyan;
                break;
            case ERROR:
                prefix = "✗ ";
                colorRes = R.color.xura_error;
                break;
            default:
                prefix = "";
                colorRes = R.color.xura_text_primary;
                break;
        }
        Snackbar snackbar = Snackbar.make(root, prefix + message, Snackbar.LENGTH_LONG);
        TextView tv = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(ContextCompat.getColor(this, colorRes));
        snackbar.show();
    }

    /** Запускает короткую пульсирующую анимацию на View — используется для визуального отклика на действие пользователя. */
    protected void pulse(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_scale_pulse));
    }

    /** Лёгкий тап — навигация, «назад», выбор игры, правила. */
    protected void soundNav() {
        if (AudioHelper.isSoundEnabled(this)) UiSoundPlayer.get().nav();
        hapticTap();
    }

    /** Тинк монеты — чипы ставки, +/−, выбор числа/цвета, ячейка рулетки. */
    protected void soundSelect() {
        if (AudioHelper.isSoundEnabled(this)) UiSoundPlayer.get().select();
        hapticTap();
    }

    /** Тяжёлый удар — SPIN, BET, PlaceBet. */
    protected void soundAction() {
        if (AudioHelper.isSoundEnabled(this)) UiSoundPlayer.get().action();
        hapticTap();
    }

    protected boolean isHapticEnabled() {
        return PrefsHelper.get(this).getBoolean(StringEnum.APP_PREFERENCES_HAPTIC_ENABLED.getValue(), true);
    }

    protected void hapticTap() {
        if (!isHapticEnabled()) return;
        View root = getWindow().getDecorView();
        root.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    protected void hapticScroll() {
        if (!isHapticEnabled()) return;
        View root = getWindow().getDecorView();
        root.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    /** Обновляет бейдж режима игры (LIVE / TRIAL) если он присутствует в текущем layout. */
    private void updateGameModeBadge() {
        TextView badge = findViewById(R.id.tv_game_mode);
        if (badge == null) return;
        boolean isLive = Boolean.TRUE.equals(MainActivity.IS_REAL_GAME_MODE);
        badge.setText(isLive ? "● LIVE" : "○ TRIAL");
        badge.setTextColor(isLive ? 0xFF00C853 : 0xFFFFB000);
    }

    /** Читает сохранённую в настройках локаль и применяет её к ресурсам Activity до отрисовки UI. */
    private void applyLocale() {
        SharedPreferences prefs = PrefsHelper.get(this);
        String lang = prefs.getString(StringEnum.APP_PREFERENCES_LOCALE.getValue(), "en");
        Locale locale = new Locale(lang);

        // Синхронизируем статическое поле для кода, который читает MainActivity.newLocale
        MainActivity.newLocale = locale;

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
