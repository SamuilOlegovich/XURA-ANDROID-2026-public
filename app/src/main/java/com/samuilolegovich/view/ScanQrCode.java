package com.samuilolegovich.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import com.samuilolegovich.BaseActivity;
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
import dagger.hilt.android.AndroidEntryPoint;




/**
 * Экран сканирования QR-кода через CameraX: запрашивает разрешение на камеру, показывает
 * предпросмотр и анализирует кадры через {@link QRCodeDecoder}; при успешном распознавании
 * передаёт адрес кошелька в {@link SendPayment} и закрывает экран.
 */
@AndroidEntryPoint
public class ScanQrCode extends BaseActivity {
    public static final String SCAN_QR_CODE_CLASS = ".ScanQrCode";

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    private final int REQUEST_CODE_PERMISSIONS = 5555;
    private final int SUSPENSION_TIME = 2000;

    private ImageCapture imageCapture;
    private PreviewView mPreviewView;
    private View root;

    public boolean isProcess;



    /** Инициализирует экран: разметка, View и проверка/запрос разрешения на использование камеры. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_code_page);
        root = findViewById(android.R.id.content);
        setButtons();
        performCheck();
    }



    /** Если разрешение на камеру уже выдано — запускает камеру, иначе запрашивает разрешение у пользователя. */
    private void performCheck() {
        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    /** Находит и сохраняет ссылку на View предпросмотра камеры. */
    private void setButtons() {

        mPreviewView = (PreviewView) findViewById(R.id.camera);
    }


    /**
     * Вызывается декодером {@link QRCodeDecoder} при успешном распознавании QR-кода:
     * при сканировании анализатор работает циклически и сам себя перезапускает через паузу,
     * поэтому здесь нужно успеть подставить распознанный адрес в поле отправки платежа
     * и сразу закрыть экран сканирования.
     */
    public void qrCodeHandler(String qrCodeText) {
        runOnUiThread(() -> showSnackbar(root, qrCodeText, SnackbarType.INFO));
        SendPayment.ADDRESS = qrCodeText;
        onBackPressed();
    }


    /** Настраивает и связывает с жизненным циклом активити: выбор задней камеры, анализатор изображения для распознавания QR и предпросмотр. */
    private void bindPreview(@NotNull ProcessCameraProvider cameraProvider) {
        // выбираем заднюю камеру устройства
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // анализатор изображения
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
        imageAnalysis.setAnalyzer(Executors.newFixedThreadPool(1), new QRCodeDecoder(this));
        ImageCapture.Builder builder = new ImageCapture.Builder();

        // создаем предпросмотр
        Preview preview = new Preview.Builder().build();
        // инициализируем захват изображения
        imageCapture = builder.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();
        // пердпросмотру устанавливаем провайдер поверхности - созданый на экземпляре предпросмотрщика
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        // привязываем жизненый цикл поставщика камеры передав в метод нашу активити
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);
    }


    /** Асинхронно получает провайдер камеры и привязывает к нему предпросмотр и анализатор QR-кода. */
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


    /** Проверяет, что все необходимые разрешения (доступ к камере) уже выданы пользователем. */
    private boolean allPermissionGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }


    /** Обрабатывает ответ пользователя на запрос разрешения камеры: запускает камеру при согласии, иначе закрывает экран. */
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


    /** Запускает Activity по имени её класса/действия. */
    private void goToAnotherPage(String namePage) {
        Intent intent = new Intent(namePage);
        startActivity(intent);
    }


    /** Не содержит дополнительной логики помимо стандартной обработки паузы активити. */
    @Override
    protected void onPause() {
        super.onPause();
    }


    /** Не содержит дополнительной логики помимо стандартной обработки возобновления активити. */
    @Override
    protected void onResume() {
        super.onResume();
    }


    /** Стандартная обработка нажатия "назад" без дополнительной логики. */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
