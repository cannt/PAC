package com.japac.pac.Auth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Firma extends AppCompatActivity {
    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private SignaturePad firma;

    private Button btnfirmar, btnborrar;

    private String id;

    FirebaseAuth mAuth;

    FirebaseFirestore firebaseFirestore;

    FirebaseStorage almacen;
    StorageReference almacenRef;
    StorageReference firmaRef;
    StorageReference firmasImagenRef;

    private ProgressBar cargando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_firma);
        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoFirmaConfi);

        firebaseFirestore = firebaseFirestore.getInstance();

        btnborrar = (Button) findViewById(R.id.btnBorrarCon);
        btnfirmar = (Button) findViewById(R.id.btnFirmarCon);

        firma = (SignaturePad) findViewById(R.id.firmaCon);
        firma.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                btnborrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cargandoloSI();
                        firma.clear();
                        cargandoloNO();
                    }
                });
                btnfirmar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cargandoloSI();
                        Bitmap firmaImagen = firma.getSignatureBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        firmaImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();
                        UploadTask uploadTask = firmaRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                cargandoloNO();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                cargandoloNO();
                                startActivity(new Intent(Firma.this, Login.class));
                                finish();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    finishAfterTransition();
                                }
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

    @Override
    protected void onStart() {
        super.onStart();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mAuth = FirebaseAuth.getInstance();
        almacen = FirebaseStorage.getInstance();
        almacenRef = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
        id = mAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.contains("empresa")) {
                    if (compruebapermisos()) {
                        if (mAuth.getCurrentUser() != null) {
                            firmaRef = almacenRef.child(documentSnapshot.getString("empresa")).child("Firmas").child(documentSnapshot.getString("nombre")).child(id + ".jpg");
                            firmasImagenRef = almacenRef.child("firmas/" + id + ".jpg");
                        }
                    }
                }
            }
        });
    }

    private void cargandoloSI() {
        cargando.setVisibility(View.VISIBLE);
        btnborrar.setEnabled(false);
        btnfirmar.setEnabled(false);
    }

    private void cargandoloNO() {
        btnborrar.setEnabled(true);
        btnfirmar.setEnabled(true);
        cargando.setVisibility(View.INVISIBLE);
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
