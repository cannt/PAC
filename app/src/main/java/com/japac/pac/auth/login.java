package com.japac.pac.auth;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.japac.pac.R;
import com.japac.pac.servicios.servicioLocalizacion;
import com.japac.pac.menu.menu;
import com.japac.pac.servicios.snackbarDS;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {


    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private EditText email, contrasena;

    private TextView pPt;

    private Button entrar, registrar;

    private View mDosBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String semail = "", scontrasena = "";

    private ProgressBar progressBar;

    private View view, mDos;

    private Snackbar snackbar;

    private static final int ERROR_DIALOGO_PEDIR = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progressbar);
        view = findViewById(R.id.viewGrey);
        entrar = findViewById(R.id.btnEntrar);
        registrar = findViewById(R.id.btnRegistrar);
        snackbar = Snackbar.make(findViewById(R.id.viewSnack), "Bienvenido", 5000)
                .setActionTextColor(Color.WHITE);
        cargando(true);
        if (compruebapermisos() && isServicesOK()) {
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationServices.getFusedLocationProviderClient(login.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(login.this)
                                    .removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                mAuth = FirebaseAuth.getInstance();
                                firebaseFirestore = FirebaseFirestore.getInstance();
                                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                                        .build();
                                firebaseFirestore.setFirestoreSettings(settings);

                                email = findViewById(R.id.logEmail);
                                contrasena = findViewById(R.id.logContraseña);
                                pPt = findViewById(R.id.PrivacyPolicy);

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
                                        cargando(true);
                                        semail = email.getText().toString();
                                        scontrasena = contrasena.getText().toString();

                                        if (!semail.isEmpty() && !scontrasena.isEmpty()) {

                                            loginUsuario();

                                        } else if (semail.isEmpty()) {
                                            email.setError("Introduzca su email");
                                            cargando(false);
                                            if (scontrasena.isEmpty()) {
                                                contrasena.setError("Introduzca su contraseña");
                                                cargando(false);
                                            }

                                        } else {
                                            contrasena.setError("Introduzca su contraseña");
                                            cargando(false);
                                            if (semail.isEmpty()) {
                                                email.setError("Introduzca su email");
                                                cargando(false);
                                            }
                                        }
                                    }
                                });
                                cargando(false);
                            }
                        }
                    }, Looper.getMainLooper());
        }

    }


    private boolean Jornada() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTime now = DateTime.now(zone);
        int hour = now.getHourOfDay();
        boolean hora = ((hour >= 7) && (hour < 17));
        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        DateFormatSymbols dfs = new DateFormatSymbols();
        if (dfs.getWeekdays()[weekday].equals("Saturday") || dfs.getWeekdays()[weekday].equals("Sunday")) {
            hora = false;
        }
        if (!hora) {
            final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
            if (sharedPreferences.contains("acepta")) {
                if (sharedPreferences.getBoolean("acepta", false)) {
                    hora = true;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                }
            }
        }
        return hora;
    }

    private boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(login.this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(login.this, available, ERROR_DIALOGO_PEDIR);
            dialog.show();
        } else {
            snackbar.setText("Mapas no funciona");
            TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextSize(10);
            snackbarDS.configSnackbar(this, snackbar);
            snackbar.show();

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

        mAuth.signInWithEmailAndPassword(semail, scontrasena).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    menuRoles();
                } else {
                    snackbar.setText("No se pudo iniciar sesion, compruebe los datos");
                    TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextSize(10);
                    snackbarDS.configSnackbar(login.this, snackbar);
                    snackbar.show();
                    cargando(false);
                }

            }
        });
    }

    private void menuRoles() {
        cargando(true);
        final String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if(documentSnapshot.getString("rol").equals("Empleado")){
                        final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
                        Boolean desac = documentSnapshot.getBoolean("desactivado");
                        if (!desac) {
                            if(Jornada()){
                                startLocationService();
                                cargando(false);
                                startActivity(new Intent(login.this, menu.class));
                                finish();
                            }else if (!Jornada()) {
                                final TextView myMsgtitle = new TextView(login.this);
                                myMsgtitle.setText("Esta fuera de horario laboral\n¿Desea continuar de todas formas?");
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);
                                myMsgtitle.setPadding(2,2,2,2);
                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                final Button btnCont = mDosBtn.findViewById(R.id.btn1);
                                btnCont.setText("Continuar");
                                final Button btnSal = mDosBtn.findViewById(R.id.btn2);
                                btnSal.setText("Salir");
                                final AlertDialog.Builder fuera = new AlertDialog.Builder(login.this)
                                        .setCustomTitle(myMsgtitle)
                                        .setView(mDosBtn);
                                final AlertDialog dialogoFuera = fuera.create();
                                btnCont.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("acepta", true);
                                        editor.apply();
                                        login.this.recreate();
                                    }
                                });
                                btnSal.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        finish();
                                        System.exit(0);
                                    }
                                });
                                dialogoFuera.setCanceledOnTouchOutside(false);
                                dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        btnCont.setEnabled(true);
                                        btnSal.setEnabled(true);
                                        cargando(false);
                                        final CharSequence negativeButtonText = btnCont.getText();
                                        new CountDownTimer(10000, 100) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                                btnCont.setText(String.format(
                                                        Locale.getDefault(), "%s (%d)",
                                                        negativeButtonText,
                                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                                                ));
                                            }

                                            @Override
                                            public void onFinish() {
                                                if ((dialogoFuera).isShowing()) {
                                                    dialogoFuera.dismiss();
                                                    finish();
                                                    System.exit(0);
                                                }
                                            }
                                        }.start();
                                    }
                                });
                                if (mDosBtn.getParent() != null) {
                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                    dialogoFuera.show();
                                } else {
                                    dialogoFuera.show();
                                }
                            }
                        } else {
                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                            final TextView myMsgtitle = new TextView(login.this);
                            myMsgtitle.setText("Usuario actualmente desactivado\nContacte con el responsable de su empresa");
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);
                            myMsgtitle.setPadding(2, 2, 2, 2);
                            final AlertDialog.Builder registroBu = new AlertDialog.Builder(Objects.requireNonNull(login.this))
                                    .setCustomTitle(myMsgtitle)
                                    .setView(mDos);
                            final Button btnCon = mDos.findViewById(R.id.btn1);
                            btnCon.setText("Reintentar");
                            final Button btnSal = mDos.findViewById(R.id.btn2);
                            btnSal.setText("Salir");
                            final AlertDialog dialogoRegistro = registroBu.create();
                            btnCon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    email.getText().clear();
                                    contrasena.getText().clear();
                                    mAuth.signOut();
                                    login.this.recreate();
                                    dialogoRegistro.dismiss();
                                }
                            });
                            btnSal.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoRegistro.dismiss();
                                    email.getText().clear();
                                    contrasena.getText().clear();
                                    mAuth.signOut();
                                    ((ActivityManager) login.this.getSystemService(ACTIVITY_SERVICE))
                                            .clearApplicationUserData();
                                }
                            });
                            dialogoRegistro.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCon.setEnabled(true);
                                    btnSal.setEnabled(true);
                                    cargando(false);
                                }
                            });
                            dialogoRegistro.setCanceledOnTouchOutside(false);
                            if (mDos.getParent() != null) {
                                ((ViewGroup) mDos.getParent()).removeView(mDos);
                                mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                dialogoRegistro.show();
                            } else {
                                dialogoRegistro.show();
                            }
                        }
                    }else if(documentSnapshot.getString("rol").equals("Administrador")){
                        startLocationService();
                        cargando(false);
                        startActivity(new Intent(login.this, menu.class));
                        finish();
                    }
                } else {
                    cargando(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                cargando(false);
            }
        });
    }

    private void registro() {

        Intent intent = new Intent(this, com.japac.pac.auth.registrar.class);
        startActivity(intent);

    }

    private void usuario() {
        if (mAuth.getCurrentUser() != null) {
            menuRoles();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x1) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    recreate();
                    break;
                case Activity.RESULT_CANCELED:
                    finish();
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Permisos) {
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

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, servicioLocalizacion.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                login.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
            if ("com.japac.pac.Servicios.servicioLocalizacion".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void cargando(Boolean carg) {

        if (carg) {
            if (progressBar.getProgress() != 25) {
                progressBar.setProgress(25);
            }
            if (progressBar.getVisibility() != View.VISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
            login.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            if (progressBar.getProgress() != 100) {
                progressBar.setProgress(100);
            }
            if (progressBar.getVisibility() != View.INVISIBLE) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
            login.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}