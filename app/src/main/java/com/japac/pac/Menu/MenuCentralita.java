package com.japac.pac.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.pdf.PRAcroForm;
import com.japac.pac.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MenuCentralita extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {

            setupCamera(width, height);
            conectarCamara();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            mCameraDevice = cameraDevice;
            if (grabando) {
                try {
                    crearVideoNombreArchivo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            } else {
                startPreview();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

            cameraDevice.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

            cameraDevice.close();
            mCameraDevice = null;

        }
    };

    private String id;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;
    private int mTotalRotation;
    private MediaRecorder mMediaRecorder;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private File mVideoCarpeta;
    private String mVideoNombreArchivo;
    private Button camarabtn;
    private boolean grabando, subiendo;
    private static SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() / (long) rhs.getWidth() * rhs.getHeight());
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_centralita);

        firebaseFirestore = firebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        almacen = FirebaseStorage.getInstance();
        almacenRef = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
        id = mAuth.getCurrentUser().getUid();
        createVideoCarpeta();

        mMediaRecorder = new MediaRecorder();

        mTextureView = (TextureView) findViewById(R.id.textureView);
        camarabtn = (Button) findViewById(R.id.btnReg);
        camarabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {

            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            conectarCamara();

        } else {

            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        }


    }

    @Override
    protected void onPause() {

        cerrarCamara();
        stopBackgroundThread();
        super.onPause();


    }

    @Override
    public void onWindowFocusChanged(boolean foco) {
        super.onWindowFocusChanged(foco);
        View decorView = getWindow().getDecorView();
        if (foco) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void setupCamera(int width, int height) {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {

            for (String cameraId : cameraManager.getCameraIdList()) {

                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {

                    continue;

                } else if (cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {

                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                    int totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                    mTotalRotation = totalRotation;
                    boolean swapRotation = totalRotation == 90 || totalRotation == 270;
                    int rotatedWidth = width;
                    int rotatedHeight = height;
                    if (swapRotation) {
                        rotatedWidth = height;
                        rotatedHeight = width;
                    }
                    mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                    mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                    mCameraId = cameraId;
                    Log.d("MenuCentralita", cameraId);
                    return;

                }

            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void conectarCamara() {

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

                }

            } else {

                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);


            }
            new CountDownTimer(28800000,  1800000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    DateTimeZone zone = DateTimeZone.forID("Europe/London");
                    DateTime now = DateTime.now(zone);
                    Integer hour = now.getHourOfDay();
                    Calendar calendar = Calendar.getInstance();
                    int weekday = calendar.get(Calendar.DAY_OF_WEEK);
                    DateFormatSymbols dfs = new DateFormatSymbols();
                    if (!dfs.getWeekdays()[weekday].equals("Saturday") || !dfs.getWeekdays()[weekday].equals("Sunday")) {

                        if(hour >= 8 && hour <= 16){



                            if(hour >= 16){

                            }

                        }
                    }

                }
                @Override
                public void onFinish() {
                    start();
                }
            }.start();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Jornada()) {

                    }
                }
            }, 1800000);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }

    private void startPreview() {

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void startRecord() {
        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void cerrarCamara() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    private void startBackgroundThread() {

        mBackgroundHandlerThread = new HandlerThread("camara");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

    }

    private void stopBackgroundThread() {

        mBackgroundHandlerThread.quitSafely();
        try {

            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {

        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;

    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private boolean Jornada() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime now = DateTime.now(zone);
        Integer hour = now.getHourOfDay();
        Boolean hora = ((hour >= 8) && (hour <= 16));
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        DateFormatSymbols dfs = new DateFormatSymbols();
        if (dfs.getWeekdays()[weekday].equals("Saturday") || dfs.getWeekdays()[weekday].equals("Sunday")) {
            hora = false;
        }
        return hora;
    }

    private void createVideoCarpeta() {

        File videoCarpeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        mVideoCarpeta = new File(videoCarpeta, "camaraPAC");
        if (!mVideoCarpeta.exists()) {

            mVideoCarpeta.mkdirs();

        }

    }

    private File crearVideoNombreArchivo() throws IOException {

        String timestamp = new SimpleDateFormat("ddMMyyyy_HH:mm").format(new Date());
        File videoArchivo = File.createTempFile("video", ".mp4", mVideoCarpeta);
        mVideoNombreArchivo = videoArchivo.getAbsolutePath();
        return videoArchivo;

    }

    private void checkWriteStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    crearVideoNombreArchivo();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            startRecord();
            mMediaRecorder.start();
            grabando = true;
        } else {
            try {
                crearVideoNombreArchivo();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
            grabando = true;
        }

    }

    private void setupMediaRecorder() throws IOException {

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoNombreArchivo);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();

    }

}
