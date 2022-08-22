package com.samuilolegovich.qrScaner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.samuilolegovich.view.ScanQrCode;

import java.util.List;

public class QRCodeDecoder implements ImageAnalysis.Analyzer {
    private BarcodeScannerOptions options;
    private BarcodeScanner scanner;
    private Context context;


    public QRCodeDecoder(Context context) {
        this.options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        this.scanner = BarcodeScanning.getClient(options);
        this.context = context;

    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        // получаем изобраение
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = image.getImage();
        // если оно не равно нулл - получаем угол наклона изображения.
        if (mediaImage != null) {
            int rotationDeg = image.getImageInfo().getRotationDegrees();
            InputImage iImage = InputImage.fromMediaImage(mediaImage, rotationDeg);

            Task<List<Barcode>> result = scanner.process(iImage);
            // слушатель успешного получения кода с изображения
            result.addOnSuccessListener(barcode -> {
                 if (barcode.size() > 0) {
                     Barcode.UrlBookmark urlBookmark = barcode.get(0).getUrl();
                     String url = null;
                     try {
                         url = urlBookmark.getUrl();
                     } catch (Exception e) {
                         url = barcode.get(0).getDisplayValue();
                     }
                     // проверяем состояние системы через контекст -
                     // не обрабатывает ли она предыдцщий найденый код
                     // и провери декодирован ли текущай
                     if (!((ScanQrCode) context).isProcess &&  url != null) {
                         // устанавливаем систему в состояние обработки найденого текста и вызываем обработчик куар кодов
                         ((ScanQrCode) context).isProcess = true;
                         ((ScanQrCode) context).qrCodeHandler(url);
                     }
                 }
                 image.close();
            });
            result.addOnFailureListener(e -> image.close());

        }


    }
}
