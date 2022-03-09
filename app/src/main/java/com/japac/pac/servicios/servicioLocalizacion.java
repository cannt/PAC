package com.japac.pac.servicios;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.japac.pac.localizacion.localizacionUsuario;
import com.japac.pac.menu.menu;

import java.util.Objects;

public class servicioLocalizacion extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;
    private final static long FASTEST_INTERVAL = 2000;
    private FirebaseFirestore mDb;
    private localizacionUsuario mLocalizarUsuario;
    private String id;
    private static String nombre;
    private static String empresa;
    private String comprobar;
    private String obra;
    private static String rol;
    private Double distancia;
    private static servicioLocalizacion servicioLocalizacion;
    private static Boolean running = false;
    private static CountDownTimer timerFina;
    private static String estado;
    private Boolean des;

    public servicioLocalizacion() {
        Log.d("servicioLocalizacion", "ENTRA");
        servicioLocalizacion = this;
    }

    public static void setEstado(String estado) {
        com.japac.pac.servicios.servicioLocalizacion.estado = estado;
        if(empresa!=null && nombre!=null){
            FirebaseFirestore.getInstance()
                    .collection("Empresas")
                    .document(empresa).collection("Localizaciones " + rol).document(nombre).update("estado", estado);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("servicioLocalizacion onCreate", "ENTRA");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "mi_canal_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Mi Canal",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand", "ENTRA");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {
        Log.d("getLocation", "ENTRA");
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d("onLocationResult", "ENTRA");
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            Log.d("location", "ENTRA");
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            saveUserLocation(geoPoint);
                        }
                    }
                },
                Looper.myLooper());
    }

    private void saveUserLocation(final GeoPoint geoPoint) {
        try {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            id = mAuth.getCurrentUser().getUid();
            mDb = FirebaseFirestore.getInstance();
            mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(final DocumentSnapshot documentSnapshot0) {
                    if (documentSnapshot0.exists()) {
                        empresa = documentSnapshot0.getString("empresa");
                        nombre = documentSnapshot0.getString("nombre");
                        rol = documentSnapshot0.getString("rol");
                        mLocalizarUsuario = new localizacionUsuario();
                        mLocalizarUsuario.setGeoPoint(geoPoint);
                        mLocalizarUsuario.setId(id);
                        mLocalizarUsuario.setNombre(nombre);
                        mLocalizarUsuario.setTimestamp(null);
                        mLocalizarUsuario.setObra(documentSnapshot0.getString("obra"));
                        mLocalizarUsuario.setEstado(estado);
                        if(rol.equals("Empleado")){
                            des = documentSnapshot0.getBoolean("desactivado");
                            mLocalizarUsuario.setDesactivado(des);
                        }
                        comprobar = documentSnapshot0.getString("comprobar");
                        obra = documentSnapshot0.getString("obra");
                        final DocumentReference locationRef = FirebaseFirestore.getInstance()
                                .collection("Empresas")
                                .document(empresa).collection("Localizaciones " + rol).document(nombre);
                        locationRef.set(mLocalizarUsuario);
                        if(rol.equals("Administrador")){
                            locationRef.update("desactivado", FieldValue.delete());
                        }
                    }
                }
            });
        } catch (NullPointerException e) {
            stopSelf();
        }
    }

    private static void setTimer() {
        timerFina = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                running = true;
            }

            @Override
            public void onFinish() {
                if(empresa!=null && nombre!=null){
                    FirebaseFirestore.getInstance()
                            .collection("Empresas")
                            .document(empresa).collection("Localizaciones " + rol).document(nombre).update("estado", "offline full").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            running = false;
                            servicioLocalizacion.stopSelf();
                            menu.finishTask();
                        }
                    });
                }
            }
        };
    }

    public static void finaliza(Boolean fina) {
        if (!running && fina) {
            setTimer();
            timerFina.start();
        } else if (running && !fina) {
            timerFina.cancel();
            running = false;
        }

    }
}
