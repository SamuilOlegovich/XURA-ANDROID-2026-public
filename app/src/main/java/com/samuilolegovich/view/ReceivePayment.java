package com.samuilolegovich.view;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

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

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран получения платежа: показывает классический XRPL-адрес кошелька в виде текста
 * и QR-кода с градиентной раскраской, позволяет скопировать адрес в буфер обмена
 * или поделиться адресом вместе с изображением QR-кода через системный диалог "Поделиться".
 */
@AndroidEntryPoint
public class ReceivePayment extends BaseActivity {

    @Inject WalletRepository repository;
    public static final String RECEIVE_PAYMENT_CLASS = ".ReceivePayment";

    private String ADDRESS_COPIED_TO_PHONE_BUFFER;

    private ClipboardManager clipboardManager;
    private ClipData clipData;

    private String classicAddress;

    private TextView receivePaymentTextViewTow;
    private TextView receivePaymentTextView;
    private ImageView qrCode;
    private TextView address;
    private View copy;
    private View share;
    private Bitmap qrBitmap;



    /** Инициализирует экран: разметка, View, локализация, слушатели, адрес кошелька и QR-код. */
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



    /** Находит и сохраняет ссылки на View разметки экрана. */
    private void setButtons() {
        receivePaymentTextViewTow = (TextView) findViewById(R.id.receive_payment_text_view_tow);
        receivePaymentTextView = (TextView) findViewById(R.id.receive_payment_text_view);
        qrCode = (ImageView) findViewById(R.id.qr_code);
        address = (TextView) findViewById(R.id.address);
        copy = findViewById(R.id.copy_linc);
        share = findViewById(R.id.share_linc);
    }


    /** Загружает локализованные строки для заголовков экрана и сообщения о копировании адреса. */
    private void setLanguage() {
        ADDRESS_COPIED_TO_PHONE_BUFFER = getString(R.string.address_copied_to_phone_buffer);
        receivePaymentTextViewTow.setText(R.string.your_address);
        receivePaymentTextView.setText(R.string.request_xrp);
    }


    /** Получает классический XRPL-адрес текущего кошелька из репозитория и отображает его на экране. */
    private void setAddress() {
        classicAddress = repository.getClassicAddress();
        address.setText(classicAddress);
    }


    /** Назначает обработчики кнопок копирования адреса в буфер обмена и отправки адреса с QR-кодом через "Поделиться". */
    private void listeners() {
        View root = findViewById(android.R.id.content);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        copy.setOnClickListener(v -> {
            pulse(v);
            clipData = ClipData.newPlainText("text", classicAddress);
            clipboardManager.setPrimaryClip(clipData);
            showSnackbar(root, ADDRESS_COPIED_TO_PHONE_BUFFER, SnackbarType.INFO);
        });

        share.setOnClickListener(v -> {
            pulse(v);
            shareAddressWithQr();
        });
    }

    /** Сохраняет сгенерированный QR-код во временный файл и открывает системный диалог "Поделиться" с адресом и изображением QR. */
    private void shareAddressWithQr() {
        try {
            File imagesDir = new File(getCacheDir(), "images");
            imagesDir.mkdirs();
            File qrFile = new File(imagesDir, "xura_qr.png");
            try (FileOutputStream out = new FileOutputStream(qrFile)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Uri qrUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", qrFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_TEXT, classicAddress);
            intent.putExtra(Intent.EXTRA_STREAM, qrUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, classicAddress));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Кодирует адрес кошелька в QR-код через ZXing и раскрашивает закодированные пиксели градиентом вместо однотонного чёрного. */
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

            qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            qrBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            qrCode.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Находит для параметра t (0..1) две соседние опорные точки градиента и возвращает смешанный между ними цвет. */
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

    /** Линейно смешивает два ARGB-цвета с коэффициентом r (0 — чистый c1, 1 — чистый c2). */
    private int blendColors(int c1, int c2, float r) {
        float inv = 1f - r;
        int a = (int)(((c1 >> 24) & 0xFF) * inv + ((c2 >> 24) & 0xFF) * r);
        int red = (int)(((c1 >> 16) & 0xFF) * inv + ((c2 >> 16) & 0xFF) * r);
        int g = (int)(((c1 >> 8)  & 0xFF) * inv + ((c2 >> 8)  & 0xFF) * r);
        int b = (int)(( c1        & 0xFF) * inv + ( c2        & 0xFF) * r);
        return (a << 24) | (red << 16) | (g << 8) | b;
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
