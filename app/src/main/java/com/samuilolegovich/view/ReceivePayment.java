package com.samuilolegovich.view;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.wallet.model.PaymentManager.PaymentAndSocketManagerXRPL;


import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;



// тут страница с данными для получения платежа плюс куар код
public class ReceivePayment extends AppCompatActivity {
    public static final String RECEIVE_PAYMENT_CLASS = ".ReceivePayment";

    private String ADDRESS_COPIED_TO_PHONE_BUFFER;

    private ClipboardManager clipboardManager;
    private Animation animTranslate;
    private ClipData clipData;

    private String classicAddress;

    private TextView receivePaymentTextViewTow;
    private TextView receivePaymentTextView;
    private ImageView qrCode;
    private TextView address;
    private TextView copy;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.receive_payment);
        setButtons();
        setLanguage();
        listeners();
        setAddress();
        setQrCode();
    }



    private void setButtons() {
        receivePaymentTextViewTow = (TextView) findViewById(R.id.receive_payment_text_view_tow);
        receivePaymentTextView = (TextView) findViewById(R.id.receive_payment_text_view);
        qrCode = (ImageView) findViewById(R.id.qr_code);
        address = (TextView) findViewById(R.id.address);
        copy = (TextView) findViewById(R.id.copy_linc);
    }


    private void setLanguage() {
        ADDRESS_COPIED_TO_PHONE_BUFFER = getString(R.string.addres_copied_to_phone_buffer);
        receivePaymentTextViewTow.setText(R.string.your_address);
        receivePaymentTextView.setText(R.string.requesr_xrp);
        copy.setText(R.string.copy);
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
                        makeToast(ADDRESS_COPIED_TO_PHONE_BUFFER);
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
