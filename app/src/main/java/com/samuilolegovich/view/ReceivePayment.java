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

import com.samuilolegovich.BaseActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.wallet.repository.WalletRepository;


import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.util.HashMap;
import java.util.Map;



// тут страница с данными для получения платежа плюс куар код
public class ReceivePayment extends BaseActivity {
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
        classicAddress = WalletRepository.getInstance().getClassicAddress();
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
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix matrix = new MultiFormatWriter().encode(classicAddress, BarcodeFormat.QR_CODE, 600, 600, hints);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];

            int[] gradColors = {0xFF00D4FF, 0xFF4040F0, 0xFF9020D0, 0xFFD02090, 0xFFFFB000};
            float[] gradPos   = {0f, 0.25f, 0.5f, 0.75f, 1f};

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        float t = (float)(x + y) / (float)(width + height);
                        pixels[y * width + x] = interpolateGradient(gradColors, gradPos, t);
                    } else {
                        pixels[y * width + x] = 0x00000000;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            qrCode.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int interpolateGradient(int[] colors, float[] positions, float t) {
        if (t <= positions[0]) return colors[0];
        if (t >= positions[positions.length - 1]) return colors[colors.length - 1];
        for (int i = 0; i < positions.length - 1; i++) {
            if (t >= positions[i] && t <= positions[i + 1]) {
                float f = (t - positions[i]) / (positions[i + 1] - positions[i]);
                return blendColors(colors[i], colors[i + 1], f);
            }
        }
        return colors[colors.length - 1];
    }

    private int blendColors(int c1, int c2, float r) {
        float inv = 1f - r;
        int a = (int)(((c1 >> 24) & 0xFF) * inv + ((c2 >> 24) & 0xFF) * r);
        int red = (int)(((c1 >> 16) & 0xFF) * inv + ((c2 >> 16) & 0xFF) * r);
        int g = (int)(((c1 >> 8)  & 0xFF) * inv + ((c2 >> 8)  & 0xFF) * r);
        int b = (int)(( c1        & 0xFF) * inv + ( c2        & 0xFF) * r);
        return (a << 24) | (red << 16) | (g << 8) | b;
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
