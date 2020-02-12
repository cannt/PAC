package com.japac.pac.auth;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.japac.pac.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class registrar extends AppCompatActivity {

    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String confirma, empresas, roles, cod, snombre, semail, sconfEmail, scontrasena, sconfContrasena, scodigoEmpresa, snif, snaf;

    private EditText nombre, email, confEmail, contrasena, confContrasena, codigoEmpresa, codigoEmpleado, nif, naf;

    private FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;

    private ProgressBar progressBar;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        progressBar = findViewById(R.id.progressbar);
        view = findViewById(R.id.viewGrey);

        if (compruebapermisos()) {

            mAuth = FirebaseAuth.getInstance();

            firebaseFirestore = FirebaseFirestore.getInstance();

            Button entrar = findViewById(R.id.btnEntrar);
            Button registrar = findViewById(R.id.btnRegistrar);

            nombre = findViewById(R.id.regNombre);
            email = findViewById(R.id.regEmail);
            confEmail = findViewById(R.id.confEmail);
            contrasena = findViewById(R.id.regContraseña);
            confContrasena = findViewById(R.id.confContraseña);
            codigoEmpresa = findViewById(R.id.regCodigoEmpresa);
            nif = findViewById(R.id.regNif);
            naf = findViewById(R.id.regNaf);

            entrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(com.japac.pac.auth.registrar.this, login.class);
                    startActivity(intent);
                }
            });

            registrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cargando(true);
                    snombre = nombre.getText().toString();
                    semail = email.getText().toString();
                    sconfEmail = confEmail.getText().toString();
                    scontrasena = contrasena.getText().toString();
                    sconfContrasena = confContrasena.getText().toString();
                    scodigoEmpresa = codigoEmpresa.getText().toString();
                    snif = nif.getText().toString();
                    snaf = naf.getText().toString();
                    if (snombre.isEmpty() || semail.isEmpty() || sconfEmail.isEmpty() || scontrasena.isEmpty() || sconfContrasena.isEmpty() || scodigoEmpresa.isEmpty() || snif.isEmpty() || snaf.isEmpty()) {
                        if (snombre.isEmpty()) {
                            nombre.setError("Escriba su nombre");
                        }
                        if (semail.isEmpty()) {
                            email.setError("Escriba un email");
                        }
                        if (sconfEmail.isEmpty()) {
                            confEmail.setError("Repita el email");
                        } else if (!semail.equals(sconfEmail)) {
                            confEmail.setError("El email no coincide");
                        }
                        if (scontrasena.isEmpty()) {
                            contrasena.setError("Escriba una contraseña");
                        }
                        if (sconfContrasena.isEmpty()) {
                            confContrasena.setError("Repita la contraseña");
                        } else if (!scontrasena.equals(sconfContrasena)) {
                            confContrasena.setError("La contraseña no coincide");
                        }
                        if (scodigoEmpresa.isEmpty()) {
                            codigoEmpresa.setError("Escriba el codigo de su empresa");
                        }
                        if (snif.isEmpty()) {
                            nif.setError("Escriba su NIF");
                        }
                        if (snaf.isEmpty()) {
                            naf.setError("Escriba su NAF");
                        }
                        cargando(false);
                    } else if (contrasena.length() >= 6) {
                        if (!semail.equals(sconfEmail) || !scontrasena.equals(sconfContrasena)) {
                            if (!semail.equals(sconfEmail)) {
                                confEmail.setError("Los emails no coinciden");
                            }
                            if (!scontrasena.equals(sconfContrasena)) {
                                confContrasena.setError("Las contraseñas no coinciden");
                            }
                            cargando(false);
                        } else if (semail.contentEquals(sconfEmail) && scontrasena.contentEquals(sconfContrasena)) {
                            codigoEmpresaCheck();
                        }
                    } else {
                        contrasena.setError("La contraseña debe ser de al menos 6 caracteres");
                        cargando(false);
                    }

                }
            });
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

    private void codigoEmpresaCheck() {

        firebaseFirestore.collection("Codigos").document(scodigoEmpresa).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    codigoEmpleadoCheck();
                } else {
                    codigoEmpresa.setError("El codigo de empresa no existe");
                    cargando(false);
                }
            }
        });
    }

    private void codigoEmpleadoCheck() {
        cargando(false);
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Codigo de empleado");

        final EditText codEmpleado = new EditText(this);

        alerta.setView(codEmpleado)
                .setCancelable(false)
                .setPositiveButton("Validar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        cargando(true);
                        cod = codEmpleado.getText().toString();

                        firebaseFirestore.collection("Codigos").document(scodigoEmpresa).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    empresas = Objects.requireNonNull(documentSnapshot.get("Empresa")).toString();
                                    roles();
                                    confirma = Objects.requireNonNull(documentSnapshot.get(snombre)).toString();
                                    codConfirma();
                                }
                            }
                        });
                    }
                });
        alerta.show();
    }

    private void roles() {
        if (cod.charAt(4) == 'E') {
            roles = "Empleado";
        } else if (cod.charAt(4) == 'a') {
            roles = "Administrador";
        } else {
            Toast.makeText(registrar.this, "ROL NO VALIDO", Toast.LENGTH_SHORT).show();
            cargando(false);
        }
    }

    private void codConfirma() {
        if (cod.equals(confirma)) {
            privacyPolicy();

        } else {
            codigoEmpleadoCheck();
            cargando(false);
            Toast.makeText(registrar.this, "EL CODIGO NO COINCIDE", Toast.LENGTH_SHORT).show();

        }
    }

    private void privacyPolicy() {
        final AlertDialog.Builder pP = new AlertDialog.Builder(registrar.this);
        pP.setMessage("Politica de privacidad")
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Leer", null)
                .setNeutralButton("Cancelar", null);
        final AlertDialog dialogoQuien = pP.create();
        dialogoQuien.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positivoYo = dialogoQuien.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoOtro = dialogoQuien.getButton(AlertDialog.BUTTON_NEGATIVE);
                Button neutroC = dialogoQuien.getButton(AlertDialog.BUTTON_NEUTRAL);
                positivoYo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registrarUsuario();
                        dialogoQuien.dismiss();

                    }
                });

                negativoOtro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);

                    }
                });

                neutroC.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        System.exit(0);
                        dialogoQuien.dismiss();
                    }
                });
            }
        });
        dialogoQuien.setCanceledOnTouchOutside(false);
        dialogoQuien.show();
    }

    private void registrarUsuario() {

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), contrasena.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    final Map<String, Object> map = new HashMap<>();
                    map.put("empresa", empresas);
                    map.put("rol", roles);
                    map.put("nombre", snombre);
                    map.put("email", semail);
                    map.put("codigo empresa", scodigoEmpresa);
                    map.put("codigo empleado", cod);
                    map.put("comprobar", "no");
                    map.put("id", id);
                    map.put("notificacion", false);
                    map.put("NIF", snif);
                    map.put("NAF", snaf);
                    map.put("DESACTIVADO", false);
                    map.put("Dias libres", null);
                    map.put("Dias libres solicitados", null);
                    if (roles.equals("Administrador")) {
                        map.put("jefe", "todo");
                    } else if (roles.equals("Empleado")) {
                        map.put("jefe", "no");
                    }
                    final Map<String, String> mapF = new HashMap<>();
                    mapF.put("años", "");
                    mapF.put("meses", "");
                    mapF.put("dias", "");
                    firebaseFirestore.collection("Todas las ids").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseFirestore.collection("Empresas").document(empresas).collection(roles).document(snombre).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseFirestore.collection("Empresas").document(empresas).collection(roles).document(snombre).collection("Registro").document("años,meses,dias").set(mapF).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            final Map<String, Object> mapDias = new HashMap<>();
                                            mapDias.put("Dias libres", null);
                                            mapDias.put("Dias libres solicitados", null);
                                            mapDias.put("Nombre", snombre);
                                            firebaseFirestore.collection("Empresas").document(empresas).collection("Dias libres solicitados").document(snombre).set(mapDias).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mapDias.put("aceptado", false);
                                                    mapDias.put("asignado", false);
                                                    mapDias.put("rechazado", false);
                                                    mapDias.put("eliminado", false);
                                                    firebaseFirestore.collection("Empresas").document(empresas).collection("Dias libres").document(snombre).set(mapDias).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            InputMethodManager inputManager = (InputMethodManager)
                                                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                                                            Objects.requireNonNull(inputManager).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                                                                    InputMethodManager.HIDE_NOT_ALWAYS);
                                                            cargando(false);
                                                            startActivity(new Intent(registrar.this, firma.class));
                                                            finish();
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
                    Toast.makeText(registrar.this, "Este email ya esta en uso", Toast.LENGTH_SHORT).show();
                    cargando(false);
                }
            }
        });


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
            registrar.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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
            registrar.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}

