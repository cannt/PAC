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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.localizacion.localizacionUsuario;
import com.japac.pac.menu.menu;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class servicioLocalizacion extends Service {
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;
    private final static long FASTEST_INTERVAL = 2000;
    private FirebaseFirestore mDb;
    private localizacionUsuario mLocalizarUsuario;
    private String id, nombre, empresa, comprobar, obra, rol;
    private Double distancia;
    private static servicioLocalizacion servicioLocalizacion;
    private static Boolean running = false;
    private static CountDownTimer timerFina;

    public servicioLocalizacion() {
        servicioLocalizacion = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "mi_canal_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Mi Canal",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {

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

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
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
            id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
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
                        comprobar = documentSnapshot0.getString("comprobar");
                        obra = documentSnapshot0.getString("obra");
                        final DocumentReference locationRef = FirebaseFirestore.getInstance()
                                .collection("Empresas")
                                .document(empresa).collection("Localizaciones").document(nombre);
                        locationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    GeoPoint geoPointAn = Objects.requireNonNull(task.getResult()).getGeoPoint("geoPoint");
                                    if (geoPoint != null && geoPointAn != null) {
                                        distancia = SphericalUtil.computeDistanceBetween(new LatLng(geoPointAn.getLatitude(), geoPointAn.getLongitude()), new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                                    }
                                    locationRef.set(mLocalizarUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            final String norm = Normalizer.normalize(nombre.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                                            if (rol.equals("Empleado")) {
                                                mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").document(norm).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (distancia != null) {
                                                            if (Objects.requireNonNull(task.getResult()).exists()) {
                                                                if (Double.compare(distancia, 200.0) >= 0) {
                                                                    Log.d("distancia", String.valueOf(distancia));
                                                                    final Map<String, Object> mapGeo = new HashMap<>();
                                                                    mapGeo.put("geoPoint", geoPoint);
                                                                    mapGeo.put("nombre", nombre);
                                                                    if (obra == null) {
                                                                        mapGeo.put("obra", null);
                                                                    } else {
                                                                        mapGeo.put("obra", obra);
                                                                    }
                                                                    String norm = Normalizer.normalize(nombre.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                                                                    mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").document(norm).set(mapGeo);

                                                                }
                                                            } else {
                                                                Log.d("distancia canceled", String.valueOf(distancia));
                                                                final Map<String, Object> mapGeo = new HashMap<>();
                                                                mapGeo.put("geoPoint", geoPoint);
                                                                mapGeo.put("nombre", nombre);
                                                                if (obra == null) {
                                                                    mapGeo.put("obra", null);
                                                                } else {
                                                                    mapGeo.put("obra", obra);
                                                                }
                                                                mapGeo.put("id", id);
                                                                String norm = Normalizer.normalize(nombre.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                                                                mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").document(norm).set(mapGeo);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } catch (NullPointerException e) {
            stopSelf();
        }
    }

    private static void setTimer() {
        timerFina = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                running = true;
                Log.d("tick", running.toString());
            }

            @Override
            public void onFinish() {
                running = false;
                Log.d("onFinish", running.toString());
                servicioLocalizacion.stopSelf();
                menu.finishTask();
            }
        };
    }

    public static void finaliza(Boolean fina) {
        Log.d("finaliza", fina.toString());
        if (!running && fina) {
            Log.d("timerFina", running.toString());
            Log.d("fina", fina.toString());
            setTimer();
            timerFina.start();
        } else if (running && !fina) {
            Log.d("timerFina", running.toString());
            Log.d("fina", fina.toString());
            timerFina.cancel();
            running = false;
        }
        Log.d("timerFina 2", running.toString());
        Log.d("fina 2", fina.toString());

    }
}
