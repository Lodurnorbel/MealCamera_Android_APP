package com.adolfo.android.mealcameraapp.ui;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.fotoapparat.Fotoapparat;
import io.fotoapparat.configuration.CameraConfiguration;
import io.fotoapparat.configuration.UpdateConfiguration;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.view.CameraView;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.selector.AspectRatioSelectorsKt.standardRatio;
import static io.fotoapparat.selector.FlashSelectorsKt.autoFlash;
import static io.fotoapparat.selector.FlashSelectorsKt.autoRedEye;
import static io.fotoapparat.selector.FlashSelectorsKt.off;
import static io.fotoapparat.selector.FlashSelectorsKt.torch;
import static io.fotoapparat.selector.FocusModeSelectorsKt.autoFocus;
import static io.fotoapparat.selector.FocusModeSelectorsKt.continuousFocusPicture;
import static io.fotoapparat.selector.FocusModeSelectorsKt.fixed;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;
import static io.fotoapparat.selector.LensPositionSelectorsKt.front;
import static io.fotoapparat.selector.PreviewFpsRangeSelectorsKt.highestFps;
import static io.fotoapparat.selector.ResolutionSelectorsKt.highestResolution;
import static io.fotoapparat.selector.SelectorsKt.firstAvailable;
import static io.fotoapparat.selector.SensorSensitivitySelectorsKt.highestSensorSensitivity;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.adolfo.android.mealcameraapp.R;
import com.adolfo.android.mealcameraapp.api.Utils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String LOGGING_TAG = "Camera Application";
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private CameraView cameraView;
    private View capture;
    private Fotoapparat fotoapparat;
    boolean activeCameraBack = true;

    private CameraConfiguration cameraConfiguration = CameraConfiguration
            .builder()
            .photoResolution(standardRatio(highestResolution()))
            .focusMode(firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),
                    fixed()
            ))
            .flash(firstAvailable(
                    autoRedEye(),
                    autoFlash(),
                    torch(),
                    off()
            ))
            .previewFpsRange(highestFps())
            .sensorSensitivity(highestSensorSensitivity())
            .frameProcessor(new SampleFrameProcessor())
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraView = findViewById(R.id.cameraView);
        capture = findViewById(R.id.capture);

        if(!hasNoPermissions()){
            cameraView.setVisibility(View.VISIBLE);
        } else {
            requestPermission();
        }

        fotoapparat = createFotoapparat();

        takePictureOnClick();
        switchCameraOnClick();
        toggleTorchOnSwitch();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(!hasNoPermissions()){
            fotoapparat.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!hasNoPermissions()) {
            fotoapparat.stop();
        }
    }

    //TOMADOR DE FOTOS
    private void takePictureOnClick() {
        capture.setOnClickListener(view -> takePicture());
    }

    private void takePicture() {
        PhotoResult photoResult = fotoapparat.takePicture();

        photoResult.saveToFile(new File(getExternalFilesDir("photos"), "photo.jpg"));

        photoResult.toBitmap(scaled(0.25f))
                .whenDone(bitmapPhoto -> {
                    if (bitmapPhoto == null) {
                        Log.e(LOGGING_TAG, "Couldn't capture photo.");
                        return;
                    }
                    ImageView imageView = findViewById(R.id.result);
                    ImageView imageView2 = findViewById(R.id.result2);

                    imageView.setImageBitmap(bitmapPhoto.bitmap);
                    imageView.setRotation(-bitmapPhoto.rotationDegrees);

                    Log.d("PHOTO", "Nueva foto creada");
                    Log.d("PHOTO", "Enviar a 10.0.2.2:5000 " + bitmapPhoto.toString());

                    Bitmap bitmap_test = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                            R.mipmap.image_test2);

                    Utils utils = new Utils();
                    //utils.sendImage(bitmapPhoto.bitmap, imageView2, findViewById(R.id.priceText));
                    utils.sendImage(bitmap_test, imageView2, findViewById(R.id.priceText));

                    imageView.setImageBitmap(bitmap_test);
                });
    }

    //ACTIVADOR DE FLASH
    private void toggleTorchOnSwitch() {
        SwitchCompat torchSwitch = findViewById(R.id.torchSwitch);

        torchSwitch.setOnCheckedChangeListener((compoundButton, b) -> fotoapparat.updateConfiguration(
                UpdateConfiguration.builder()
                        .flash(b ? torch() : off())
                        .build()
        ));
    }

    //ROTADOR DE CAMARA
    private void switchCameraOnClick() {
        View switchCameraButton = findViewById(R.id.switchCamera);
        boolean hasFrontCamera = fotoapparat.isAvailable(front());

        switchCameraButton.setVisibility((hasFrontCamera ? View.VISIBLE : View.GONE));

        if(hasFrontCamera){
            switchCameraOnClick(switchCameraButton);
        }
    }

    private void switchCameraOnClick(View switchCameraButton) {
        switchCameraButton.setOnClickListener((view -> {
            activeCameraBack = !activeCameraBack;
            fotoapparat.switchTo(activeCameraBack ? back() : front(), cameraConfiguration);
        }));
    }

    public boolean hasNoPermissions(){
        return //ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(this, permissions, 10);
    }

    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .frameProcessor(new SampleFrameProcessor())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(e -> {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                })
                .build();
    }


    private class SampleFrameProcessor implements FrameProcessor {
        @Override
        public void process(@NotNull Frame frame) {
            // Perform frame processing, if needed
        }
    }

    private class CheckUrl extends AsyncTask<URL, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(URL... urls) {
            try{
                URL myUrl = new URL("http://10.0.2.2:5000");
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(50);
                connection.connect();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}