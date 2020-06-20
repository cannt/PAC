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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.japac.pac.R;
import com.japac.pac.servicios.snackbarDS;

import java.io.ByteArrayOutputStream;
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

    private FirebaseStorage almacen;
    private StorageReference almacenRef;

    private CountDownTimer timer;

    private StorageReference firmaRef;

    private ProgressBar progressBar;

    private View mFirmar, mTresBtn;

    private Snackbar snackbar;

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
            snackbar = Snackbar.make(findViewById(R.id.viewSnack), "Bienvenido", 5000)
                    .setActionTextColor(Color.WHITE);
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

        firebaseFirestore.collection("Codigos").document(scodigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("Codigo de empresa").equals(scodigoEmpresa)) {
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
                                    empresas = documentSnapshot.get("Empresa").toString();
                                    if (cod.charAt(4) == 'E') {
                                        roles = "Empleado";
                                    } else if (cod.charAt(4) == 'a') {
                                        roles = "Administrador";
                                    }
                                    confirma = documentSnapshot.get(snombre).toString();
                                    codConfirma();
                                }
                            }
                        });
                    }
                });
        alerta.show();
    }

    private void codConfirma() {
        if (cod.equals(confirma)) {
            privacyPolicy();
        } else {
            codigoEmpleadoCheck();
            cargando(false);
            snackbar.setText("El codigo " + cod + " no coincide con ninguna empresa");
            TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextSize(10);
            snackbarDS.configSnackbar(registrar.this, snackbar);
            snackbar.show();

        }
    }

    private void privacyPolicy() {
        final TextView titulo2 = new TextView(this);
        titulo2.setText("Politica de privacidad");
        titulo2.setGravity(Gravity.CENTER_HORIZONTAL);
        mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnRechazar = mTresBtn.findViewById(R.id.btn1);
        btnRechazar.setText("Rechazar");
        final Button btnAcept = mTresBtn.findViewById(R.id.btn2);
        btnAcept.setText("Aceptar");
        final Button btnLeer = mTresBtn.findViewById(R.id.Cancelar);
        btnLeer.setText("Leer");
        final AlertDialog.Builder pP = new AlertDialog.Builder(registrar.this);
        pP
                .setView(mTresBtn)
                .setCustomTitle(titulo2);
        final AlertDialog dialogoQuien = pP.create();
        btnAcept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
                dialogoQuien.dismiss();

            }
        });

        btnLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });

        btnRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
                dialogoQuien.dismiss();
            }
        });
        dialogoQuien.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                btnAcept.setEnabled(true);
                btnLeer.setEnabled(true);
                btnRechazar.setEnabled(true);

            }
        });
        dialogoQuien.setCanceledOnTouchOutside(false);
        if (mTresBtn.getParent() != null) {
            ((ViewGroup) mTresBtn.getParent()).removeView(mTresBtn);
            mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoQuien.show();
        } else {
            dialogoQuien.show();
        }
    }

    private void registrarUsuario() {

        mAuth.createUserWithEmailAndPassword(email.getText().toString(), contrasena.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final String id = mAuth.getCurrentUser().getUid();
                    final Map<String, Object> map = new HashMap<>();
                    map.put("empresa", empresas);
                    map.put("rol", roles);
                    map.put("nombre", snombre);
                    map.put("email", semail);
                    map.put("codigo empresa", scodigoEmpresa);
                    map.put("codigo empleado", cod);
                    map.put("comprobar", "no");
                    map.put("id", id);
                    map.put("NIF", snif);
                    map.put("NAF", snaf);
                    if (roles.equals("Empleado")) {
                        map.put("desactivado", false);
                    }
                    map.put("Dias libres", null);
                    map.put("Dias libres solicitados", null);
                    if (roles.equals("Administrador")) {
                        map.put("jefe", "todo");
                    } else if (roles.equals("Empleado")) {
                        map.put("jefe", "no");
                    }
                    firebaseFirestore.collection("Todas las ids").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseFirestore.collection("Empresas").document(empresas).collection(roles).document(snombre).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    final Map<String, Object> mapDias = new HashMap<>();
                                    mapDias.put("Dias libres", null);
                                    mapDias.put("Dias libres solicitados", null);
                                    mapDias.put("nombre", snombre);
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
                                                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                                            InputMethodManager.HIDE_NOT_ALWAYS);
                                                    cargando(false);
                                                    firmar();
                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    snackbar.setText("Este email ya esta en uso");
                    TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextSize(10);
                    snackbarDS.configSnackbar(registrar.this, snackbar);
                    snackbar.show();
                    cargando(false);
                }
            }
        });


    }

    private void firmar() {
        cargando(true);
        final TextView myMsgtitle = new TextView(registrar.this);
        myMsgtitle.setText(snombre + " debe firmar para confirmar la operación");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mFirmar = getLayoutInflater().inflate(R.layout.dialogo_firmar, null, false);
        final AlertDialog.Builder Firmar = new AlertDialog.Builder(this);
        final SignaturePad firma = mFirmar.findViewById(R.id.firmaCon2);
        final Button botonFirm = mFirmar.findViewById(R.id.btn1);
        final Button botonBor = mFirmar.findViewById(R.id.btn2);
        Firmar
                .setCustomTitle(myMsgtitle)
                .setView(mFirmar);
        final AlertDialog dialogoFirmar = Firmar.create();
        firma.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                botonFirm.setEnabled(true);
                botonBor.setEnabled(true);
            }

            @Override
            public void onSigned() {
                botonFirm.setEnabled(true);
                botonBor.setEnabled(true);
            }

            @Override
            public void onClear() {
                botonFirm.setEnabled(false);
                botonBor.setEnabled(false);
            }
        });
        botonFirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargando(true);
                Bitmap firmaImagen = firma.getSignatureBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                firmaImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = firmaRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        cargando(false);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        cargando(false);
                        startActivity(new Intent(registrar.this, login.class));
                        finish();
                        finishAfterTransition();
                    }
                });

            }
        });
        botonBor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargando(true);
                firma.clear();
                cargando(false);
            }
        });
        dialogoFirmar.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                almacen = FirebaseStorage.getInstance();
                almacenRef = almacen.getReference();
                mAuth = FirebaseAuth.getInstance();
                FirebaseStorage almacen = FirebaseStorage.getInstance();
                almacenRef = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
                mAuth.signInWithEmailAndPassword(semail, scontrasena).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        firebaseFirestore.collection("Todas las ids").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.contains("empresa")) {
                                    if (compruebapermisos()) {
                                        if (mAuth.getCurrentUser() != null) {
                                            firmaRef = almacenRef.child(documentSnapshot.getString("empresa")).child("Firmas").child(documentSnapshot.getString("nombre")).child(mAuth.getCurrentUser().getUid() + ".jpg");
                                            cargando(false);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        dialogoFirmar.setCanceledOnTouchOutside(false);
        if (mFirmar.getParent() != null) {
            ((ViewGroup) mFirmar.getParent()).removeView(mFirmar);
            mFirmar = getLayoutInflater().inflate(R.layout.dialogo_firmar, null, false);

            dialogoFirmar.show();

        } else {

            dialogoFirmar.show();
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


