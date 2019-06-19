package com.japac.pac.Menu;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.Auth.Login;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.R;
import com.japac.pac.Servicios.FueraDeHora;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Calendar;

import javax.annotation.Nullable;

public class MenuEmpleado extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {

    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    FirebaseFirestore firebaseFirestore;
    CollectionReference geoFirestoreRef;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private Double latitudDetectada, longitudDetectada, latitudGuardada, longitudGuardada, distan;

    private Button Iniciar, Finalizar, Cerrar;

    private String empresa, IoF, fecha, año, mes, dia, hora, entrada_salida, nombre, roles, obra, codigoEmpresa, id, comp, obcomp, obcomprueba, trayecto;

    private ArrayAdapter<String> obraAdapter;

    private Spinner obraSpinner;

    private LocalizacionUsuario mLocalizarUsuario;

    private FirebaseAuth mAuth;

    private ImageView logo;

    private FusedLocationProviderClient mProovedor;

    private int spinnerPosition;

    private ProgressBar cargando;

    private GeoPoint geoPointLocalizayo;

    private static final int ERROR_DIALOGO_PEDIR = 9001;

    private boolean cerrar = false, finali = false, trayectoBo = false;

    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_empleado);
        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoEmpleado);

        if (Jornada()) {
            if (compruebapermisos() && isServicesOK()) {

                Iniciar = (Button) findViewById(R.id.btnIniciarJornada);
                Finalizar = (Button) findViewById(R.id.btnFinalizarJornada);
                Cerrar = (Button) findViewById(R.id.btnCerrar);

                logo = (ImageView) findViewById(R.id.logoEmpleado);


                firebaseFirestore = FirebaseFirestore.getInstance();


                mAuth = FirebaseAuth.getInstance();

                almacen = FirebaseStorage.getInstance();
                almacenRef = almacen.getReference();


                id = mAuth.getCurrentUser().getUid();
                comp = "";
                primero();
                almacenRef.child(empresa + "/" + "Logo/" + "Logo" + codigoEmpresa + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Picasso.get().load(uri).into(logo);
                    }
                });
                Cerrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(MenuEmpleado.this, Login.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });


                Iniciar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comp.equals("iniciada")) {
                            Toast.makeText(MenuEmpleado.this, "Ya existe una jornada iniciada, finalizala primero", Toast.LENGTH_SHORT).show();
                        } else if (comp.equals("finalizada") || comp.equals("no")) {
                            entrada_salida = "Entrada";
                            leerGeo();
                        }
                    }
                });

                Finalizar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comp.contentEquals("finalizada") || comp.contentEquals("no")) {
                            Toast.makeText(MenuEmpleado.this, "Debes iniciar primero una jornada", Toast.LENGTH_SHORT).show();
                        } else if (comp.contentEquals("iniciada")) {
                            entrada_salida = "Salida";
                            leerGeo();
                        }
                    }
                });
                overridePendingTransition(0, 0);
            }

        } else if (!Jornada()) {
            startActivity(new Intent(MenuEmpleado.this, FueraDeHora.class));
            finish();
        }
    }

    public boolean Jornada() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime now = DateTime.now(zone);
        Integer hour = now.getHourOfDay();
        Boolean hora = ((hour >= 7) && (hour < 17));
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        DateFormatSymbols dfs = new DateFormatSymbols();
        if(dfs.getWeekdays()[weekday].equals("Saturday")|| dfs.getWeekdays()[weekday].equals( "Sunday")){
            hora = false;
        }
        if (FueraDeHora.returnAcepta()) {
            Intent intentSE = new Intent(MenuEmpleado.this, FueraDeHora.class);
            stopService(intentSE);
            hora = true;
        }
        return hora;
    }

    public void crearCanalDeNotificaciones() {
        if (comp.equals("iniciada")) {
            IoF = "Has iniciado una jornada en " + obcomprueba;
        } else if (comp.equals("finalizada") || comp.equals("no")) {
            IoF = "Has finalizado la jornada en " + obcomprueba;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("obras", nombre, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(IoF);
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
                .setContentText(IoF)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(IoF))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId("obras")
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(8991, builder.build());
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MenuEmpleado.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MenuEmpleado.this, available, ERROR_DIALOGO_PEDIR);
        } else {
            Toast.makeText(this, "Mapas no funciona", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    private void detalles() {
        if (mLocalizarUsuario == null) {
            mLocalizarUsuario = new LocalizacionUsuario();
            mLocalizarUsuario.setId(id);
            mLocalizarUsuario.setNombre(nombre);
            localizacion();
        }
    }


    private void guardarLocalizacion() {
        if (mLocalizarUsuario != null) {
            DocumentReference locationRef = firebaseFirestore
                    .collection("Empresas")
                    .document(empresa)
                    .collection("Localizaciones")
                    .document(nombre);
            locationRef.set(mLocalizarUsuario);
        }

    }

    private void localizacion() {
        mProovedor = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (compruebapermisos()) {
                final Task localizacion = mProovedor.getLastLocation();
                localizacion.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location locacizacionActual = (Location) task.getResult();
                            geoPointLocalizayo = new GeoPoint(locacizacionActual.getLatitude(), locacizacionActual.getLongitude());
                            mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                            mLocalizarUsuario.setTimestamp(null);
                            guardarLocalizacion();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }

    }

    private boolean compruebapermisos() {
        int resultado;
        List<String> listaPermisosNecesarios = new ArrayList<>();
        for (String perm : permisos) {
            resultado = ContextCompat.checkSelfPermission(this, perm);
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                listaPermisosNecesarios.add(perm);
            }
        }
        if (!listaPermisosNecesarios.isEmpty()) {
            ActivityCompat.requestPermissions(this, listaPermisosNecesarios.toArray(new String[listaPermisosNecesarios.size()]), Permisos);
            return false;
        }
        return true;
    }

    private void primero() {
        cargandoloSI();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Toast.makeText(MenuEmpleado.this, "EXISTE ID", Toast.LENGTH_SHORT).show();
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    comp = documentSnapshot.getString("comprobar");
                    empresa = documentSnapshot.getString("empresa");
                    nombre = documentSnapshot.getString("nombre");
                    roles = documentSnapshot.getString("rol");
                    if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                        obcomprueba = documentSnapshot.getString("obra");
                        if (comp.equals("iniciada")) {
                            crearCanalDeNotificaciones();
                        }
                    }

                    if (comp.equals("iniciada")) {
                        Cerrar.setEnabled(false);
                        Iniciar.setEnabled(false);
                        Finalizar.setEnabled(true);
                        cerrar = true;
                    } else if (comp.equals("no") || comp.equals("finalizada")) {
                        if (comp.equals("finalizada")) {
                            finali = true;
                        } else if (comp.equals("no")) {
                            finali = false;
                        }
                        Finalizar.setEnabled(false);
                        Iniciar.setEnabled(true);
                        Cerrar.setEnabled(true);
                        cerrar = false;
                    }
                    registration = firebaseFirestore.collection("Empresas").document(empresa).collection("Obras").addSnapshotListener(MenuEmpleado.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                if (doc.get("jefe") != null) {
                                    if(doc.get("jefe").equals(nombre)){
                                        salirAjefe();
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    firestore();
                    logo.setVisibility(View.INVISIBLE);
                    Cerrar.setVisibility(View.INVISIBLE);
                    almacenRef.child(empresa + "/" + "Logo/" + "Logo" + codigoEmpresa + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(logo);
                            logo.setVisibility(View.VISIBLE);
                            Cerrar.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            logo.setVisibility(View.VISIBLE);
                            Cerrar.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Toast.makeText(MenuEmpleado.this, "NO EXISTE ID", Toast.LENGTH_SHORT).show();
                }
                cargandoloNO();
            }
        });
        overridePendingTransition(0, 0);

    }

    private void salirAjefe(){
        startActivity(new Intent(MenuEmpleado.this, Login.class));
        finish();
        registration.remove();
    }

    private void firestore() {
        geoFirestoreRef = firebaseFirestore.collection("Empresas").document(empresa).collection("Obras");
        final List<String> obs = new ArrayList<String>();
        geoFirestoreRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        obs.add(obran);
                    }
                    obraSpinner = (Spinner) findViewById(R.id.spinnerObra);
                    obraAdapter = new ArrayAdapter<String>(MenuEmpleado.this, android.R.layout.simple_spinner_item, obs);
                    obraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    obraSpinner.setAdapter(obraAdapter);
                    firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                obcomprueba = documentSnapshot.getString("obra");
                                obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                            }
                        }
                    });
                    obraSpinner.setOnItemSelectedListener(MenuEmpleado.this);
                } else if (!task.isSuccessful()) {
                    obs.add("SIN OBRAS");
                }
                detalles();
            }
        });
    }

    private int leeObras(Spinner spinner, String obraselecionada) {

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(obraselecionada)) {
                return i;
            }
        }
        return 0;
    }

    private void leerGeo() {

        firebaseFirestore.collection("Empresas").document(empresa).collection("Obras").document(obra).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                GeoPoint geopointGuardado = documentSnapshot.getGeoPoint("geoPoint");
                latitudGuardada = geopointGuardado.getLatitude();
                longitudGuardada = geopointGuardado.getLongitude();
                latitudDetectada = geoPointLocalizayo.getLatitude();
                longitudDetectada = geoPointLocalizayo.getLongitude();
                distan = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(latitudGuardada, longitudGuardada));
                if (Double.compare(distan, 50.0) <= 0) {
                    if (entrada_salida.equals("Entrada")) {
                        comp = "iniciada";
                        firebaseFirestore.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                trayecto = documentSnapshot.getString("marca temporal");
                                                                if (trayecto != null) {
                                                                    String fechaGuardada = "Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                            " del " + trayecto.charAt(19) + trayecto.charAt(20) +
                                                                            " de " + trayecto.charAt(25) + trayecto.charAt(26) + trayecto.charAt(27) + trayecto.charAt(28);
                                                                    DateFormat fechaF = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
                                                                    String fechaAhora = "Iniciado el " + fechaF.format(Calendar.getInstance().getTime());
                                                                    if (fechaAhora.equals(fechaGuardada)) {
                                                                        trayectoBo = true;
                                                                        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                                                                        String horaAhora = hourFormat.format(Calendar.getInstance().getTime());
                                                                        trayecto = trayecto + fechaAhora.replace("Iniciado el ", " ") + " a las " + horaAhora;
                                                                    }
                                                                }
                                                                spinnerPosition = obraAdapter.getPosition(obra);
                                                                obraSpinner.setSelection(spinnerPosition);
                                                                enviajornada();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else if (entrada_salida.equals("Salida")) {
                        compruebaObra();
                    }
                } else if (Double.compare(distan, 50.0) > 0) {
                    Toast.makeText(MenuEmpleado.this, "No te encuentras dentro de la obra seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void compruebaObra() {

        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                obcomp = documentSnapshot.getString("obra");
                if (documentSnapshot.exists()) {
                    if (obra.equals(obcomp)) {
                        comp = "finalizada";
                        firebaseFirestore.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        DateFormat dayFormat = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
                                                        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                                                        fecha = dayFormat.format(Calendar.getInstance().getTime());
                                                        hora = hourFormat.format(Calendar.getInstance().getTime());
                                                        trayecto = "Iniciado el " + fecha + " a las " + hora + " y finalizado el ";
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("marca temporal", trayecto).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                enviajornada();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(MenuEmpleado.this, "No has iniciado jornada en esta obra", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        overridePendingTransition(0, 0);
    }

    private void enviajornada() {
        if (entrada_salida.equals("Entrada")) {
            Cerrar.setEnabled(false);
            Iniciar.setEnabled(false);
            Finalizar.setEnabled(true);
        } else if (entrada_salida.equals("Salida")) {
            crearCanalDeNotificaciones();
            Finalizar.setEnabled(false);
            Iniciar.setEnabled(true);
            Cerrar.setEnabled(true);

        }
        DateFormat dfecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat daño = new SimpleDateFormat("yyyy");
        DateFormat dmes = new SimpleDateFormat("MM");
        DateFormat ddia = new SimpleDateFormat("dd");
        fecha = dfecha.format(Calendar.getInstance().getTime());
        año = daño.format(Calendar.getInstance().getTime());
        mes = dmes.format(Calendar.getInstance().getTime());
        dia = ddia.format(Calendar.getInstance().getTime());

        DateFormat dhora = new SimpleDateFormat("HH:mm:ss");
        hora = dhora.format(Calendar.getInstance().getTime());

        final Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("Entrada o Salida", entrada_salida);
        map.put("obra", obra);
        map.put("rol", roles);
        map.put("fecha", fecha);
        map.put("hora", hora);
        map.put("UID", id);
        if (trayectoBo) {
            trayectoBo = false;
            map.put("Trayecto desde " + obcomp + " hasta " + obra, trayecto);
        }
        firebaseFirestore.collection("Empresas").document(empresa).collection("Registro").document(año).collection(mes).document(dia).collection(hora).document(nombre).set(map);
        overridePendingTransition(0, 0);
        primero();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        obra = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Permisos: {
                if (grantResults.length > 0) {
                    String permisosDenegados = "";
                    for (String per : permissions) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permisosDenegados += "\n" + per;
                        }
                    }
                    recreate();
                }
                return;
            }
        }
    }

    private void cargandoloSI() {
        cargando.setVisibility(View.VISIBLE);
        Iniciar.setEnabled(false);
        Finalizar.setEnabled(false);
        Cerrar.setEnabled(false);
    }

    private void cargandoloNO() {
        cargando.setVisibility(View.INVISIBLE);
        if (cerrar) {
            Iniciar.setEnabled(false);
            Cerrar.setEnabled(false);
            Finalizar.setEnabled(true);
        } else if (!cerrar) {
            if (finali) {
                Iniciar.setEnabled(true);
                Cerrar.setEnabled(true);
                Finalizar.setEnabled(false);
            } else if (!finali) {
                Iniciar.setEnabled(true);
                Cerrar.setEnabled(true);
                Finalizar.setEnabled(true);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitudDetectada = location.getLatitude();
        longitudDetectada = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

