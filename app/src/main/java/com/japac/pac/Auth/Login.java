package com.japac.pac.Auth;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.japac.pac.Menu.MenuAdmin;
import com.japac.pac.Menu.MenuEmpleado;
import com.japac.pac.Menu.MenuJefeDeObra;
import com.japac.pac.R;
import com.japac.pac.Servicios.FueraDeHora;
import com.japac.pac.Servicios.ServicioLocalizacion;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Login extends AppCompatActivity {


    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private EditText email, contraseña;

    private TextView pPt;

    private Button entrar, registrar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String semail = "", scontraseña = "", codigoEmpleado, sroles;

    private ProgressBar cargando;

    private static final int ERROR_DIALOGO_PEDIR = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoLogin);
        if (Jornada()) {
            if (compruebapermisos() && isServicesOK()) {
                GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API).build();
                googleApiClient.connect();

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(10000 / 2);

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                builder.setAlwaysShow(true);

                PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:

                                mAuth = FirebaseAuth.getInstance();
                                firebaseFirestore = firebaseFirestore.getInstance();

                                entrar = (Button) findViewById(R.id.btnEntrar);
                                registrar = (Button) findViewById(R.id.btnRegistrar);

                                email = (EditText) findViewById(R.id.logEmail);
                                contraseña = (EditText) findViewById(R.id.logContraseña);
                                pPt = (TextView) findViewById(R.id.PrivacyPolicy);

                                usuario();

                                pPt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(url));
                                        startActivity(i);
                                    }
                                });

                                registrar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        registro();
                                    }
                                });

                                entrar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cargandoloSI();
                                        semail = email.getText().toString();
                                        scontraseña = contraseña.getText().toString();

                                        if (!semail.isEmpty() && !scontraseña.isEmpty()) {

                                            loginUsuario();

                                        } else if (semail.isEmpty()) {
                                            email.setError("Introduzca su email");
                                            cargandoloNO();
                                            if (scontraseña.isEmpty()) {
                                                contraseña.setError("Introduzca su contraseña");
                                                cargandoloNO();
                                            }

                                        } else if (scontraseña.isEmpty()) {
                                            contraseña.setError("Introduzca su contraseña");
                                            cargandoloNO();
                                            if (semail.isEmpty()) {
                                                email.setError("Introduzca su email");
                                                cargandoloNO();
                                            }
                                        }
                                    }
                                });
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                try {
                                    status.startResolutionForResult(Login.this, 0x1);
                                } catch (IntentSender.SendIntentException e) {

                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                                break;
                        }
                    }
                });
            }
        } else if (!Jornada()) {
            startActivity(new Intent(Login.this, FueraDeHora.class));
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
        if (dfs.getWeekdays()[weekday].equals("Saturday") || dfs.getWeekdays()[weekday].equals("Sunday")) {
            hora = false;
        }
        if (FueraDeHora.returnAcepta()) {
            Intent intentSE = new Intent(Login.this, FueraDeHora.class);
            stopService(intentSE);
            hora = true;
        }
        return hora;
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Login.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Login.this, available, ERROR_DIALOGO_PEDIR);
        } else {
            Toast.makeText(this, "Mapas no funciona", Toast.LENGTH_SHORT).show();

        }
        return false;
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

    private void loginUsuario() {
        mAuth.signInWithEmailAndPassword(semail, scontraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    menuRoles();
                } else {
                    Toast.makeText(Login.this, "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                    cargandoloNO();
                }

            }
        });
    }

    private void menuRoles() {
        final String id = mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    startLocationService();
                    sroles = documentSnapshot.getString("rol");
                    if (sroles.equals("Administrador")) {
                        cargandoloNO();
                        startActivity(new Intent(Login.this, MenuAdmin.class));
                        finish();
                    } else if (sroles.equals("Empleado")) {
                        codigoEmpleado = documentSnapshot.getString("codigo empleado");
                        if (codigoEmpleado.length() > 14) {
                            cargandoloNO();
                            startActivity(new Intent(Login.this, MenuJefeDeObra.class));
                            finish();
                        } else {
                            cargandoloNO();
                            startActivity(new Intent(Login.this, MenuEmpleado.class));
                            finish();
                        }
                    }
                    if (FueraDeHora.returnAcepta()) {
                        firebaseFirestore.collection("Empresas").document(documentSnapshot.getString("empresa")).collection(documentSnapshot.getString("rol")).document(documentSnapshot.getString("nombre")).update("fuera de jornada", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id).update("fuera de jornada", true);
                                FueraDeHora.acepta = false;
                            }
                        });
                    } else if (!FueraDeHora.returnAcepta()) {
                        firebaseFirestore.collection("Empresas").document(documentSnapshot.getString("empresa")).collection(documentSnapshot.getString("rol")).document(documentSnapshot.getString("nombre")).update("fuera de jornada", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id).update("fuera de jornada", false);
                            }
                        });
                    }
                } else {
                    cargandoloNO();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                cargandoloNO();
            }
        });
    }

    private void cargandoloSI() {
        cargando.setVisibility(View.VISIBLE);
        entrar.setEnabled(false);
        registrar.setEnabled(false);
    }

    private void cargandoloNO() {
        entrar.setEnabled(true);
        registrar.setEnabled(true);
        cargando.setVisibility(View.INVISIBLE);
    }

    private void registro() {

        Intent intent = new Intent(this, Registrar.class);
        startActivity(intent);

    }

    public void usuario() {
        if (mAuth.getCurrentUser() != null) {
            cargandoloSI();
            menuRoles();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0x1:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        recreate();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        System.exit(0);
                        break;
                }
                break;
        }
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

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, ServicioLocalizacion.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                Login.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}