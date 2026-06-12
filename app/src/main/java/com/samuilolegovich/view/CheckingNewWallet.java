package com.samuilolegovich.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.samuilolegovich.BaseActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.PrefsHelper;
import com.samuilolegovich.utils.SecureSeedStorage;
import com.samuilolegovich.wallet.repository.WalletRepository;

import javax.inject.Inject;

import static com.samuilolegovich.view.Referral.REFERRAL_CLASS;
import dagger.hilt.android.AndroidEntryPoint;




// тут мы сверим информаци о новом кошельке, правильно ли ее записал юзер
@AndroidEntryPoint
public class CheckingNewWallet extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String CHECKING_NEW_WALLET_CLASS = ".CheckingNewWallet";

    private Animation animTranslate;

    private TextView checkingNewWalletText;
    private EditText seed;
    private TextView next;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.checking_new_wallet);
        setButtons();
        setLanguage();
        listeners();
    }



    private void setButtons() {
        checkingNewWalletText = (TextView) findViewById(R.id.checking_new_wallet_text);
        seed = (EditText) findViewById(R.id.password_field);
        next = (TextView) findViewById(R.id.next_link);
    }


    private void setLanguage() {
        checkingNewWalletText.setText(R.string.lead_the_seed);
        next.setText(R.string.next);
    }


    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String seedOne = getPreSeed();
                        String seedTwo = seed.getText().toString();
                        v.startAnimation(animTranslate);

                        if (seedOne.equals(seedTwo)) {
                            setSeed(seedOne);
                            MainActivity.START_FLAG = false;
                            repository.loadBalance();
                            goToAnotherPage(REFERRAL_CLASS);
                        } else {
                            seed.setText("");
                            makeToast(StringEnum.SEED_DOES_NOT_MATCH.getValue());
                        }
                    }
                }
        );
    }


    private void makeToast(String massage) {
        Toast toast = Toast.makeText(getApplicationContext(), massage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0,110);   // import android.view.Gravity;
        toast.show();
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    private String getPreSeed() {
        return SecureSeedStorage.load(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_PRE_SEED.getValue());
    }


    private void setSeed(String newSeed) {
        SecureSeedStorage.save(PrefsHelper.get(this), StringEnum.APP_PREFERENCES_SEED.getValue(), newSeed);
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
