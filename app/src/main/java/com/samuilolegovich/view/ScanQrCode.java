package com.samuilolegovich.view;

import static com.samuilolegovich.view.SendPayment.SEND_PAYMENT;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.samuilolegovich.MainActivity;
import com.samuilolegovich.R;
import com.samuilolegovich.qrScaner.QRCodeDecoder;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;



public class ScanQrCode extends AppCompatActivity {
    public static final String SCAN_QR_CODE_CLASS = ".ScanQrCode";

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private final int REQUEST_CODE_PERMISSIONS = 5555;
    private final int SUSPENSION_TIME = 2000;

    private ImageCapture imageCapture;
    private PreviewView mPreviewView;

    public boolean isProcess;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.MAIN_ACTIVITY.setLocale();
        setContentView(R.layout.scan_code_page);
        setButtons();
        performCheck();
    }



    private void performCheck() {
        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    private void setButtons() {

        mPreviewView = (PreviewView) findViewById(R.id.camera);
    }


    // при сканировании кода запускаем тред и через время все обнуляется и он опять сканирует,
    // в нашем случаи надо сделать чтобы через это время он установил адрес сосканированного
    // поля в нужное нам поле и закрыл страницу
    public void qrCodeHandler(String qrCodeText) {
        Context context = this;
        runOnUiThread(() -> Toast.makeText(context, qrCodeText, Toast.LENGTH_LONG).show());
        SEND_PAYMENT.setAddress(qrCodeText);
        onBackPressed();

//        new Thread(() -> {
//            try {
//                Thread.sleep(SUSPENSION_TIME);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            isProcess = false;
//            SendPayment.ADDRESS = qrCodeText;
//            onBackPressed();
//        }).start();
    }


    private void bindPreview(@NotNull ProcessCameraProvider cameraProvider) {
        // выбираем заднюю камеру устройства
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // анализатор изображения
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), new QRCodeDecoder(this));
        ImageCapture.Builder builder = new ImageCapture.Builder();

//        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
//        if (hdrImageCaptureExtender.isExtensionAvaible(cameraSelector)) {
//            hdrImageCaptureExtender.enableExtension(cameraSelector);
//        }

        // создаем предпросмотр
        Preview preview = new Preview.Builder().build();
        // инициализируем захват изображения
        imageCapture = builder.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();
        // пердпросмотру устанавливаем провайдер поверхности - созданый на экземпляре предпросмотрщика
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        // привязываем жизненый цикл поставщика камеры передав в метод нашу активити
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
    }


    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {

            }
        } , ContextCompat.getMainExecutor(this));
    }


    // все ли необходимые разрешения установлены пользователем
    private boolean allPermissionGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                this.finish();
            }
        }
    }


    private void goToAnotherPage(String namePage) {
        // класс для перехода на другую страницу
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    // при нажатии на кнопку назад будем возвращаться назад
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
