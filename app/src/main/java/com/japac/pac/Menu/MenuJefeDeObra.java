package com.japac.pac.Menu;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.Auth.Login;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class MenuJefeDeObra extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {

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

    private Button btnRegistroJornada, Cerrar;

    private String JefeDeObra,IoF, empresa, fecha, año, mes, dia, hora, entrada_salida, nombre, roles, obra, codigoEmpresa, codigoEmpleado, id, comp, obcomp, obcomprueba,emailAn, nombreAm, trayecto;

    private ArrayAdapter<String> obraAdapter, obraJefeAdapter;

    private Spinner obraSpinner;

    private GeoPoint geoPointLocalizayo;

    private FirebaseAuth mAuth;

    private int spinnerPosition;

    private View mLoginDialog, mSpinner;

    private TextView Bien, eresObras;

    private boolean otro = false, cerrar = false, trayectoBo = false;

    private ProgressBar cargando;

    private ImageView logo;

    private static final int ERROR_DIALOGO_PEDIR = 9001;

    private LocalizacionUsuario mLocalizarUsuario;

    private FusedLocationProviderClient mProovedor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_jefe_de_obra);

        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoJefe);

        if (compruebapermisos() == true) {
            if(isServicesOK()){

                logo = (ImageView) findViewById(R.id.logoJefeDeObra);
                btnRegistroJornada = (Button) findViewById(R.id.btnRegistrarJornadas);
                Cerrar = (Button) findViewById(R.id.btnCerrar);

                firebaseFirestore = FirebaseFirestore.getInstance();

                mAuth = FirebaseAuth.getInstance();

                almacen = FirebaseStorage.getInstance();
                almacenRef = almacen.getReference();

                id = mAuth.getCurrentUser().getUid();
                comp = "no";

                Bien = (TextView) findViewById(R.id.BienvenidoX);
                eresObras = (TextView) findViewById(R.id.eresDe);

                primero();

                Cerrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(MenuJefeDeObra.this, Login.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
                if (comp.isEmpty() == true) {
                    firebaseFirestore.collection("Todas las ids").document(id).update("comprobar", "no");
                }


                btnRegistroJornada.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dQuien();
                    }
                });
                overridePendingTransition(0, 0);
            }
        }
    }


    public void crearCanalDeNotificaciones(){
        if(comp.equals("iniciada")){
            IoF = "Has iniciado una jornada en " + obcomprueba;
        }else if(comp.equals("finalizada") || comp.equals("no")){
            IoF = "Has finalizado la jornada en " + obcomprueba;
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("obras", nombre, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(IoF);
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            mostrarNotificacion();
        }
    }

    public void mostrarNotificacion(){
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
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    comp = documentSnapshot.getString("comprobar");
                    if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no" ) {
                        obcomprueba = documentSnapshot.getString("obra");
                        if (comp.equals("iniciada")) {
                            crearCanalDeNotificaciones();
                        }
                    }
                    if (!otro) {
                        nombreAm = documentSnapshot.getString("nombre");
                        emailAn = documentSnapshot.getString("email");
                    }
                    Toast.makeText(MenuJefeDeObra.this, "EXISTE ID", Toast.LENGTH_SHORT).show();
                    codigoEmpleado = documentSnapshot.getString("codigo empleado");
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    empresa = documentSnapshot.getString("empresa");
                    nombre = documentSnapshot.getString("nombre");
                    roles = documentSnapshot.getString("rol");
                    JefeDeObra = documentSnapshot.getString("jefe");
                    if(comp.equals("iniciada")){
                        Cerrar.setEnabled(false);
                        cerrar = true;
                    }else if(comp.equals("no") || comp.equals("finalizada")){
                        Cerrar.setEnabled(true);
                        cerrar = false;
                    }
                    Bien.setText("Bienvenido de nuevo " + nombreAm);

                    firestore();
                    almacenRef.child(empresa + "/" + "Logo/" + "Logo" + codigoEmpresa + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Picasso.get().load(uri).into(logo);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                        }
                    });
                } else {

                    Toast.makeText(MenuJefeDeObra.this, "NO EXISTE ID", Toast.LENGTH_SHORT).show();
                }
                cargandoloNO();
            }
        });

        overridePendingTransition(0, 0);

    }

    private void firestore() {

        geoFirestoreRef = firebaseFirestore.collection("Empresas").document(empresa).collection("Obras");
        final List<String> obs = new ArrayList<String>();
        final List<String> oJeF = new ArrayList<String>();
        geoFirestoreRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        String JFob = document.getString("jefe");
                        obs.add(obran);
                        if (nombreAm.equals(JFob)) {
                            oJeF.add(obran);
                        }

                    }
                    mSpinner = getLayoutInflater().inflate(R.layout.spinner_dialogo, null);
                    obraSpinner = (Spinner) mSpinner.findViewById(R.id.spinnerObra);

                    obraAdapter = new ArrayAdapter<String>(MenuJefeDeObra.this, android.R.layout.simple_spinner_item, obs);
                    obraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    obraJefeAdapter = new ArrayAdapter<String>(MenuJefeDeObra.this, android.R.layout.simple_spinner_item, oJeF);
                    obraJefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                obcomprueba = documentSnapshot.getString("obra");
                                obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                            }
                        }
                    });
                    eresObras.setText("Eres jefe de obra de : " + oJeF);
                    mLoginDialog = getLayoutInflater().inflate(R.layout.login_dialogo, null, false);
                    obraSpinner.setOnItemSelectedListener(MenuJefeDeObra.this);
                } else if (!task.isSuccessful()) {
                    obs.add("SIN OBRAS");
                }
                detalles();
            }
        });


    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MenuJefeDeObra.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MenuJefeDeObra.this, available, ERROR_DIALOGO_PEDIR);
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
                                                                if(trayecto!=null){
                                                                    String fechaGuardada ="Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                            " del " + trayecto.charAt(19)+trayecto.charAt(20) +
                                                                            " de " + trayecto.charAt(25)+trayecto.charAt(26)+trayecto.charAt(27)+trayecto.charAt(28);
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
                    Toast.makeText(MenuJefeDeObra.this, "No te encuentras dentro de la obra seleccionada", Toast.LENGTH_SHORT).show();
                    dRegistrar();
                }
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
                    } else {

                        Toast.makeText(MenuJefeDeObra.this, "No has iniciado jornada en esta obra", Toast.LENGTH_SHORT).show();
                        dRegistrar();
                    }
                }
            }
        });

        overridePendingTransition(0, 0);
    }

    private void enviajornada() {

        if(entrada_salida.equals("salida")){
            crearCanalDeNotificaciones();
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
        map.put("hora",hora);
        map.put("UID", id);
        if (otro) {
            map.put("iniciado por", nombreAm);
        }
        if (trayectoBo) {
            trayectoBo = false;
            map.put("Trayecto desde " + obcomp + " hasta " + obra, trayecto);
        }
        firebaseFirestore.collection("Empresas").document(empresa).collection("Registro").document(año).collection(mes).document(dia).collection(hora).document(nombre).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (otro) {
                        dConfirma();
                    }
                }
            }
        });
        primero();
        overridePendingTransition(0, 0);
    }

    private void dQuien() {
        final AlertDialog.Builder Quien = new AlertDialog.Builder(MenuJefeDeObra.this);
        Quien.setMessage("¿Quien quiere registrar la jornada?")
                .setPositiveButton(nombre, null)
                .setNegativeButton("Otro empleado de " + empresa, null)
                .setNeutralButton("Cancelar", null);
        final AlertDialog dialogoQuien = Quien.create();
        dialogoQuien.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positivoYo = (Button) dialogoQuien.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoOtro = (Button) dialogoQuien.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button neutroC = (Button) dialogoQuien.getButton(AlertDialog.BUTTON_NEUTRAL);
                positivoYo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoQuien.dismiss();
                        dRegistrar();
                    }
                });

                negativoOtro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoQuien.dismiss();
                        dLogin();
                    }
                });

                neutroC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoQuien.dismiss();
                    }
                });
            }
        });
        dialogoQuien.setCanceledOnTouchOutside(false);
        dialogoQuien.show();
    }

    private void dRegistrar() {

        if (otro) {
            obraSpinner.setAdapter(obraJefeAdapter);
        } else if (!otro) {
            obraSpinner.setAdapter(obraAdapter);
        }

        final AlertDialog.Builder registroJornada = new AlertDialog.Builder(MenuJefeDeObra.this);
        registroJornada.setTitle(nombre);
        registroJornada.setMessage("Seleccione la obra")
                .setNeutralButton("Iniciar", null)
                .setNegativeButton("Finalizar", null)
                .setPositiveButton("Cancelar", null);
        registroJornada.setView(obraSpinner);
        final AlertDialog dialogoRegistro = registroJornada.create();
        dialogoRegistro.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button neutroInicia = dialogoRegistro.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button negativoFinaliza = dialogoRegistro.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button positivoSale = dialogoRegistro.getButton(AlertDialog.BUTTON_POSITIVE);
                neutroInicia.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comp.equals("iniciada")) {
                            Toast.makeText(MenuJefeDeObra.this, "Ya existe una jornada iniciada, finalizala primero", Toast.LENGTH_SHORT).show();
                        } else if (comp.equals("finalizada") || comp.equals("no")) {
                            entrada_salida = "Entrada";
                            dialogoRegistro.dismiss();
                            leerGeo();
                        }
                    }
                });
                negativoFinaliza.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (comp.contentEquals("finalizada") || comp.contentEquals("no")) {
                            Toast.makeText(MenuJefeDeObra.this, "Debes iniciar primero una jornada", Toast.LENGTH_SHORT).show();
                        } else if (comp.contentEquals("iniciada")) {
                            entrada_salida = "Salida";
                            dialogoRegistro.dismiss();
                            leerGeo();
                        }
                    }
                });
                positivoSale.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (otro) {
                            dialogoRegistro.dismiss();
                            dConfirma();
                        } else {
                            dialogoRegistro.dismiss();
                        }
                    }
                });
            }
        });
        dialogoRegistro.setCanceledOnTouchOutside(false);
        if (obraSpinner.getParent() != null) {
            ((ViewGroup) obraSpinner.getParent()).removeView(obraSpinner);
            firestore();
            dialogoRegistro.show();
        } else {
            dialogoRegistro.show();
        }
    }

    private void dLogin() {
        final AlertDialog.Builder Login = new AlertDialog.Builder(MenuJefeDeObra.this);
        final EditText semail = mLoginDialog.findViewById(R.id.emailDialogo);
        final EditText scontraseña = mLoginDialog.findViewById(R.id.contraseñaDialogo);
        Login
                .setView(mLoginDialog)
                .setPositiveButton("Acceder", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoLogin = Login.create();
        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positivoAccede = (Button) dialogoLogin.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negaticoCancela = (Button) dialogoLogin.getButton(AlertDialog.BUTTON_NEGATIVE);
                positivoAccede.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String emailNu = semail.getText().toString();
                        final String contraseñaNu = scontraseña.getText().toString();
                        if (!emailNu.isEmpty() && !contraseñaNu.isEmpty()) {
                            mAuth.signOut();
                            mAuth.signInWithEmailAndPassword(emailNu, contraseñaNu).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        id = mAuth.getCurrentUser().getUid();
                                        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    Toast.makeText(MenuJefeDeObra.this, "EXISTE ID", Toast.LENGTH_SHORT).show();
                                                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                                                    comp = documentSnapshot.getString("comprobar");
                                                    empresa = documentSnapshot.getString("empresa");
                                                    nombre = documentSnapshot.getString("nombre");
                                                    roles = documentSnapshot.getString("rol");
                                                    if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                                        obcomprueba = documentSnapshot.getString("obra");
                                                    }
                                                    otro = true;
                                                    dialogoLogin.dismiss();

                                                    dRegistrar();
                                                } else {

                                                    Toast.makeText(MenuJefeDeObra.this, "NO EXISTE ID", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {

                                        Toast.makeText(MenuJefeDeObra.this, "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        } else if (emailNu.isEmpty()) {
                            semail.setError("Introduzca su email");

                            if (contraseñaNu.isEmpty()) {
                                scontraseña.setError("Introduzca su contraseña");

                            }

                        } else if (contraseñaNu.isEmpty()) {
                            scontraseña.setError("Introduzca su contraseña");

                            if (emailNu.isEmpty()) {
                                semail.setError("Introduzca su email");

                            }
                        }
                    }
                });
                negaticoCancela.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoLogin.dismiss();
                    }
                });
            }
        });
        dialogoLogin.setCanceledOnTouchOutside(false);
        if (mLoginDialog.getParent() != null) {
            ((ViewGroup) mLoginDialog.getParent()).removeView(mLoginDialog);
            mLoginDialog = getLayoutInflater().inflate(R.layout.login_dialogo, null, false);

            dialogoLogin.show();

        } else {

            dialogoLogin.show();
        }
    }

    private void dConfirma() {
        mAuth.signOut();
        final AlertDialog.Builder Confirma = new AlertDialog.Builder(MenuJefeDeObra.this);
        final EditText semail = mLoginDialog.findViewById(R.id.emailDialogo);
        semail.setEnabled(false);
        semail.setText(emailAn);
        final EditText scontraseña = mLoginDialog.findViewById(R.id.contraseñaDialogo);
        Confirma
                .setView(mLoginDialog)
                .setTitle(nombreAm + " confirme la operación")
                .setPositiveButton("Confirmar", null);
        final AlertDialog dialogoConfirma = Confirma.create();
        dialogoConfirma.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positvoConfir = (Button) dialogoConfirma.getButton(AlertDialog.BUTTON_POSITIVE);
                positvoConfir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String contraseñaAn = scontraseña.getText().toString();
                        if (!contraseñaAn.isEmpty()) {

                            mAuth.signInWithEmailAndPassword(emailAn, contraseñaAn).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    id = mAuth.getCurrentUser().getUid();
                                    otro = false;
                                    dialogoConfirma.dismiss();

                                    primero();
                                }

                            });
                        } else if (contraseñaAn.isEmpty()) {
                            scontraseña.setError("Introduzca la contraseña");
                        }
                    }
                });
            }
        });
        dialogoConfirma.setCanceledOnTouchOutside(false);
        if (mLoginDialog.getParent() != null) {
            ((ViewGroup) mLoginDialog.getParent()).removeView(mLoginDialog);
            mLoginDialog = getLayoutInflater().inflate(R.layout.login_dialogo, null, false);
            dialogoConfirma.show();

        } else {

            dialogoConfirma.show();
        }
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
        btnRegistroJornada.setEnabled(false);
        Cerrar.setEnabled(false);
    }

    private void cargandoloNO() {
        btnRegistroJornada.setEnabled(true);
        if(cerrar){
            Cerrar.setEnabled(false);
        }else if(!cerrar){
            Cerrar.setEnabled(true);
        }
        cargando.setVisibility(View.INVISIBLE);
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
