package com.japac.pac.Servicios;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.japac.pac.Auth.Login;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.R;

public class ServicioLocalizacion extends Service {

    private GeoPoint geopointGuardado, geoPointLocalizayo;
    private Double latitudDetectada, longitudDetectada, latitudGuardada, longitudGuardada, distan;
    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;
    private final static long FASTEST_INTERVAL = 2000;
    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    private LocalizacionUsuario mLocalizarUsuario;
    private String id, nombre, empresa, comprobar, obra;
    private Boolean hora;

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

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

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
            mAuth = FirebaseAuth.getInstance();
            id = mAuth.getCurrentUser().getUid();
            mDb = FirebaseFirestore.getInstance();
            mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot0) {
                    if (documentSnapshot0.exists()) {
                        empresa = documentSnapshot0.getString("empresa");
                        nombre = documentSnapshot0.getString("nombre");
                        mLocalizarUsuario = new LocalizacionUsuario();
                        mLocalizarUsuario.setGeoPoint(geoPoint);
                        mLocalizarUsuario.setId(id);
                        mLocalizarUsuario.setNombre(nombre);
                        mLocalizarUsuario.setTimestamp(null);
                        comprobar = documentSnapshot0.getString("comprobar");
                        obra = documentSnapshot0.getString("obra");
                        DocumentReference locationRef = FirebaseFirestore.getInstance()
                                .collection("Empresas")
                                .document(empresa).collection("Localizaciones").document(nombre);
                        locationRef.set(mLocalizarUsuario).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                /*if (comprobar.equals("iniciada") && obra != null) {
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                latitudGuardada = task.getResult().getGeoPoint("geoPoint").getLatitude();
                                                longitudGuardada = task.getResult().getGeoPoint("geoPoint").getLongitude();
                                                mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                        geoPointLocalizayo = documentSnapshot2.getGeoPoint("geoPoint");
                                                        latitudDetectada = geoPointLocalizayo.getLatitude();
                                                        longitudDetectada = geoPointLocalizayo.getLongitude();
                                                        distan = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(latitudGuardada, longitudGuardada));
                                                        if (Double.compare(distan, 100.0) > 0) {
                                                            crearCanalDeNotificaciones();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }*/
                            }
                        });
                    }
                }
            });
        } catch (NullPointerException e) {
            stopSelf();
        }

    }

    public void crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("obras", nombre, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Te estas alejando de la obra " + obra + " sin finalizar la jornada");
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            mostrarNotificacion();
        }
    }

    public void mostrarNotificacion() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "obras")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(nombre)
                .setContentText("Te estas alejando de la obra " + obra + " sin finalizar la jornada")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Te estas alejando de la obra " + obra + " sin finalizar la jornada"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId("obras")
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(8991, builder.build());
    }
}
