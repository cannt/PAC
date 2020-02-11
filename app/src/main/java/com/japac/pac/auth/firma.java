package com.japac.pac.auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.japac.pac.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class firma extends AppCompatActivity {
    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SignaturePad firma;

    private Button btnfirmar, btnborrar;

    private String id;

    private FirebaseAuth mAuth;

    private FirebaseFirestore firebaseFirestore;

    private StorageReference almacenRef;
    private StorageReference firmaRef;
    private StorageReference firmasImagenRef;

    private ProgressBar progressBar;
    
    private View view;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_firma);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        view = (View) findViewById(R.id.viewGrey);
        
        
        firebaseFirestore = FirebaseFirestore.getInstance();

        btnborrar = (Button) findViewById(R.id.btnBorrarCon);
        btnfirmar = (Button) findViewById(R.id.btnFirmarCon);

        firma = (SignaturePad) findViewById(R.id.firmaCon);
        firma.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                btnborrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cargando(true);
                        firma.clear();
                        cargando(false);
                    }
                });
                btnfirmar.setOnClickListener(new View.OnClickListener() {
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
                                startActivity(new Intent(com.japac.pac.auth.firma.this, login.class));
                                finish();
                                finishAfterTransition();
                            }
                        });

                    }
                });
            }

            @Override
            public void onSigned() {

            }

            @Override
            public void onClear() {

            }
        });

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
            com.japac.pac.auth.firma.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
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
            com.japac.pac.auth.firma.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mAuth = FirebaseAuth.getInstance();
        FirebaseStorage almacen = FirebaseStorage.getInstance();
        almacenRef = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
        id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.contains("empresa")) {
                    if (compruebapermisos()) {
                        if (mAuth.getCurrentUser() != null) {
                            firmaRef = almacenRef.child(Objects.requireNonNull(documentSnapshot.getString("empresa"))).child("Firmas").child(Objects.requireNonNull(documentSnapshot.getString("nombre"))).child(id + ".jpg");
                            firmasImagenRef = almacenRef.child("firmas/" + id + ".jpg");
                        }
                    }
                }
            }
        });
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

}
