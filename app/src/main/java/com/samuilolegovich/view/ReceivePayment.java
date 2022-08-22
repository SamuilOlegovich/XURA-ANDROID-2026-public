package com.samuilolegovich.view;

import static com.samuilolegovich.view.RestoreOrCreateNewWallet.RESTORE_OR_NEW_WALLET_CLASS;

import static java.lang.Integer.valueOf;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.zxing.WriterException;
import com.samuilolegovich.R;
import com.samuilolegovich.enums.StringEnum;
import com.samuilolegovich.utils.Cipher;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;

import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

// тут страница с данными для получения платежа плюс куар код
public class ReceivePayment extends AppCompatActivity {
    public static final String RECEIVE_PAYMENT_CLASS = ".ReceivePayment";

    private ClipboardManager clipboardManager;
    private Animation animTranslate;
    private ClipData clipData;

    private String classicAddress;

    private ImageView qrCode;
    private TextView address;
    private TextView copy;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_payment);
        setButtons();
        listeners();
        setAddress();
        setQrCode();
    }



    private void setButtons() {
        qrCode = (ImageView) findViewById(R.id.qr_code);
        address = (TextView) findViewById(R.id.address);
        copy = (TextView) findViewById(R.id.copy);
    }

    private void setAddress() {
        classicAddress = PaymentAndSocketManagerXRPL.getInstances().getClassicAddress(true);
        address.setText(classicAddress);
    }

    private void listeners() {
        animTranslate = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        copy.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(animTranslate);
                        clipData = ClipData.newPlainText("text", classicAddress);
                        clipboardManager.setPrimaryClip(clipData);
                        makeToast("ADDRESS COPIED TO PHONE BUFFER");
                    }
                }
        );
    }

    private void setQrCode() {
        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(classicAddress, null, QRGContents.Type.TEXT, 800);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(500188);
        qrCode.setImageBitmap(qrgEncoder.getBitmap());
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
