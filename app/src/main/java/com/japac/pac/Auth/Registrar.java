package com.japac.pac.Auth;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class Registrar extends AppCompatActivity {

    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String confirma, empresas, roles, cod, snombre, semail, sconfEmail, scontraseña, sconfContraseña,scodigoEmpresa;

    private EditText nombre, email, confEmail, contraseña, confContraseña, codigoEmpresa, codigoEmpleado;

    private Button entrar, registrar;

    FirebaseFirestore firebaseFirestore;

    FirebaseAuth mAuth;

    private ProgressBar cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);
        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoRegistro);

        if (compruebapermisos()) {

            mAuth = FirebaseAuth.getInstance();

            firebaseFirestore = firebaseFirestore.getInstance();

            entrar = (Button) findViewById(R.id.btnEntrar);
            registrar = (Button) findViewById(R.id.btnRegistrar);

            nombre = (EditText) findViewById(R.id.regNombre);
            email = (EditText) findViewById(R.id.regEmail);
            confEmail = (EditText) findViewById(R.id.confEmail);
            contraseña = (EditText) findViewById(R.id.regContraseña);
            confContraseña = (EditText) findViewById(R.id.confContraseña);
            codigoEmpresa = (EditText) findViewById(R.id.regCodigoEmpresa);


            entrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });

            registrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cargandoloSI();
                    snombre = nombre.getText().toString();
                    semail = email.getText().toString();
                    sconfEmail = confEmail.getText().toString();
                    scontraseña = contraseña.getText().toString();
                    sconfContraseña = confContraseña.getText().toString();
                    scodigoEmpresa = codigoEmpresa.getText().toString();

                    if (snombre.isEmpty() || semail.isEmpty() || sconfEmail.isEmpty() || scontraseña.isEmpty() || sconfContraseña.isEmpty() || scodigoEmpresa.isEmpty()) {
                        if (snombre.isEmpty()) {
                            nombre.setError("Escriba su nombre");
                        }
                        if (semail.isEmpty()) {
                            email.setError("Escriba un email");
                        }
                        if (sconfEmail.isEmpty()) {
                            confEmail.setError("Repita el email");
                        } else if (semail.equals(sconfEmail) == false) {
                            confEmail.setError("El email no coincide");
                        }
                        if (scontraseña.isEmpty()) {
                            contraseña.setError("Escriba una contraseña");
                        }
                        if (sconfContraseña.isEmpty()) {
                            confContraseña.setError("Repita la contraseña");
                        } else if (scontraseña.equals(sconfContraseña) == false) {
                            confContraseña.setError("La contraseña no coincide");
                        }
                        if (scodigoEmpresa.isEmpty()) {
                            codigoEmpresa.setError("Escriba el codigo de su empresa");
                        }
                        cargandoloNO();
                    } else if (contraseña.length() >= 6) {
                        if (semail.equals(sconfEmail) == false || scontraseña.equals(sconfContraseña) == false) {
                            if (semail.equals(sconfEmail) == false) {
                                confEmail.setError("Los emails no coinciden");
                            }
                            if (scontraseña.equals(sconfContraseña) == false) {
                                confContraseña.setError("Las contraseñas no coinciden");
                            }
                            cargandoloNO();
                        } else if (semail.contentEquals(sconfEmail) == true && scontraseña.contentEquals(sconfContraseña) == true) {
                            codigoEmpresaCheck();
                        }
                    } else {
                        contraseña.setError("La contraseña debe ser de al menos 6 caracteres");
                        cargandoloNO();
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

    public void codigoEmpresaCheck() {

        firebaseFirestore.collection("Codigos").document(scodigoEmpresa).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    codigoEmpleadoCheck();
                } else {
                    codigoEmpresa.setError("El codigo de empresa no existe");
                    cargandoloNO();
                }
            }
        });
    }

    public void codigoEmpleadoCheck() {
        cargandoloNO();
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Codigo de empleado");

        final EditText codEmpleado = new EditText(this);

        alerta.setView(codEmpleado)
                .setCancelable(false)
                .setPositiveButton("Validar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        cargandoloSI();
                        cod = codEmpleado.getText().toString();

                        firebaseFirestore.collection("Codigos").document(scodigoEmpresa).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    empresas = documentSnapshot.get("Empresa").toString();
                                    roles();
                                    confirma = documentSnapshot.get(snombre).toString();
                                    codConfirma();
                                }
                            }
                        });
                    }
                });
        alerta.show();
    }

    public void roles() {
        if (cod.charAt(4) == 'E') {
            roles = "Empleado";
        } else if (cod.charAt(4) == 'a') {
            roles = "Administrador";
        } else {
            Toast.makeText(Registrar.this, "ROL NO VALIDO", Toast.LENGTH_SHORT).show();
            cargandoloNO();
        }
    }

    public void codConfirma() {
        if (cod.equals(confirma) == true) {
            Toast.makeText(Registrar.this, "EXISTE EL EMPLEADO", Toast.LENGTH_SHORT).show();
            registrarUsuario();

        } else if (cod.equals(confirma) == false) {
            codigoEmpleadoCheck();
            cargandoloNO();
            Toast.makeText(Registrar.this, "EL CODIGO NO COINCIDE", Toast.LENGTH_SHORT).show();

        }
    }

    private void registrarUsuario() {

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), contraseña.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String id = mAuth.getCurrentUser().getUid();
                    final Map<String, Object> map = new HashMap<>();
                    map.put("empresa", empresas);
                    map.put("rol", roles);
                    map.put("nombre", nombre.getText().toString());
                    map.put("email", email.getText().toString());
                    map.put("codigo empresa", codigoEmpresa.getText().toString());
                    map.put("codigo empleado", cod);
                    map.put("comprobar", "no");
                    map.put("id", id);
                    map.put("fuera de jornada", false);
                    if (roles.equals("Administrador")) {
                        map.put("jefe", "todo");
                    } else if (roles.equals("Empleado")) {
                        map.put("jefe", "no");
                    }

                    firebaseFirestore.collection("Todas las ids").document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseFirestore.collection("Empresas").document(empresas).collection(roles).document(nombre.getText().toString()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            InputMethodManager inputManager = (InputMethodManager)
                                                    getSystemService(Context.INPUT_METHOD_SERVICE);
                                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                                    InputMethodManager.HIDE_NOT_ALWAYS);
                                            cargandoloNO();
                                            startActivity(new Intent(Registrar.this, Firma.class));
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(Registrar.this, "Este email ya esta en uso", Toast.LENGTH_SHORT).show();
                    cargandoloNO();
                }
            }
        });


    }

    public void login() {

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

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
}

