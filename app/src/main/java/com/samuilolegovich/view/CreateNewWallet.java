package com.samuilolegovich.view;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.asyncAndRun.asyncTask.CreateNewWalletAsync;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.wallet.repository.WalletRepository;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.samuilolegovich.view.CheckingNewWallet.CHECKING_NEW_WALLET_CLASS;



// тут выводим данные по новому кошельку
public class CreateNewWallet extends BaseActivity {
    public static final String CREATE_NEW_WALLET_CLASS = ".CreateNewWallet";

    private String ADDRESS_COPIED_TO_PHONE_BUFFER;

    private ClipboardManager clipboardManager;
    private ClipData clipData;

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Animation animTranslate;

    private boolean isNewWallet;

    private String seedString;

    private TextView createNewWalletText;
    private TextView copy;
    private TextView seed;
    private TextView next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_wallet);
        setButtons();
        setLanguage();
        listeners();
        isNewWallet = createNewWallet();
    }



    private void setButtons() {
        createNewWalletText = (TextView) findViewById(R.id.create_new_wallet_text_view);
        seed = (TextView) findViewById(R.id.seed_field);
        next = (TextView) findViewById(R.id.next_link);
        copy = (TextView) findViewById(R.id.copy_linc);

    }


    private void setLanguage() {
        ADDRESS_COPIED_TO_PHONE_BUFFER = getString(R.string.addres_copied_to_phone_buffer);
        createNewWalletText.setText(R.string.lead_the_seed);
        seed.setText(R.string.wrong_restart_please);
        next.setText(R.string.next);
        copy.setText(R.string.copy);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        if (isNewWallet) {
                            MainActivity.START_FLAG = false;
                            WalletRepository.getInstance().loadBalance();
                            goToAnotherPage(CHECKING_NEW_WALLET_CLASS);
                        } else {
                            isNewWallet = createNewWallet();
                        }
                    }
                }
        );

        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

        copy.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        clipData = ClipData.newPlainText("text", seedString);
                        clipboardManager.setPrimaryClip(clipData);
                        makeToast(ADDRESS_COPIED_TO_PHONE_BUFFER);
                    }
                }
        );
    }


    @SuppressLint("HardwareIds")
    private void setPreSeed(String newSeed) {
        preferences = getSharedPreferences(StringEnum.APP_PREFERENCES.getValue(),
                Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.putString(StringEnum.APP_PREFERENCES_PRE_SEED.getValue(),
                Cipher.encryptString(newSeed,
                        preferences.getString(StringEnum.APP_PREFERENCES_SALT.getValue(),
                                ""),
                                Settings.Secure.getString(getContentResolver(),
                                        Settings.Secure.ANDROID_ID)));
        editor.apply();
    }


    // запустить менеджер и создать новый кошелек
    private boolean createNewWallet() {
        AsyncTask<String, Void, Map<String, String>> asyncTask = new CreateNewWalletAsync().execute("");
        Map<String, String> map = null;

        try {
            map = asyncTask.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (map != null && map.containsKey("Seed")) {
            seedString = map.get("Seed");
            seed.setText(seedString);
            setPreSeed(seedString);
            return true;
        }
        return false;
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
