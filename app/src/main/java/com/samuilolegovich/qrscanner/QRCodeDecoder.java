package com.samuilolegovich.qrscanner;

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

/**
 * Анализатор кадров камеры (CameraX ImageAnalysis), который на каждом кадре
 * пытается распознать QR-код через ML Kit и передаёт найденный URL/текст в ScanQrCode.
 */
public class QRCodeDecoder implements ImageAnalysis.Analyzer {
    private BarcodeScannerOptions options;
    private BarcodeScanner scanner;
    private Context context;


    /** Настраивает ML Kit сканер на распознавание только QR-кодов (без других форматов штрихкодов). */
    public QRCodeDecoder(Context context) {
        this.options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        this.scanner = BarcodeScanning.getClient(options);
        this.context = context;

    }

    /**
     * Вызывается CameraX на каждом новом кадре с камеры. Передаёт кадр в ML Kit на распознавание
     * QR-кода; если код найден и экран сканера не занят обработкой предыдущего результата —
     * передаёт найденный URL (или текст) в обработчик ScanQrCode.qrCodeHandler.
     */
    @Override
    public void analyze(@NonNull ImageProxy image) {
        // получаем изображение
        @SuppressLint("UnsafeOptInUsageError")
        Image mediaImage = image.getImage();
        // если оно не равно null — получаем угол наклона изображения
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
                     // проверяем состояние системы через контекст —
                     // не обрабатывает ли она предыдущий найденный код
                     // и проверен ли текущий
                     if (!((ScanQrCode) context).isProcess &&  url != null) {
                         // устанавливаем систему в состояние обработки найденного текста и вызываем обработчик QR-кодов
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
