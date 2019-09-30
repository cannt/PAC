package com.japac.pac.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.japac.pac.Menu.MenuJefeDeObra;
import com.japac.pac.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirmaConfirma extends AppCompatActivity {
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firma_confirma);
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
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("Desde", FieldValue.delete());
                                updates.put("obraR", FieldValue.delete());
                                updates.put("fechaR", FieldValue.delete());
                                updates.put("horaR", FieldValue.delete());
                                updates.put("saR", FieldValue.delete());
                                firebaseFirestore.collection("Todas las ids").document(id).set(updates, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        final Map<String, Object> maps = new HashMap<>();
                                        maps.put("Firmando", false);
                                        firebaseFirestore.collection("Todas las ids").document(id).set(maps, SetOptions.merge());
                                        cargandoloNO();
                                        setResult(2);
                                        finish();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            finishAfterTransition();
                                        }
                                    }
                                });
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
                            String desde = documentSnapshot.getString("Desde");
                            firmaRef = almacenRef
                                    .child(documentSnapshot.getString("empresa"))
                                    .child("Registros desde " + desde)
                                    .child(documentSnapshot.getString("nombre"))
                                    .child(documentSnapshot.get("obraR").toString())
                                    .child(documentSnapshot.get("fechaR").toString())
                                    .child(documentSnapshot.get("horaR").toString())
                                    .child(documentSnapshot.getString("saR") + " de " + documentSnapshot.getString("nombre") + " en la obra " + documentSnapshot.get("obraR").toString() + " desde la cuenta de " + desde + " el dia " + documentSnapshot.get("fechaR").toString() + " a las " + documentSnapshot.get("fechaR").toString() + ".jpg");
                            final Map<String, Object> mapf = new HashMap<>();
                            mapf.put("Firmando", true);
                            firebaseFirestore.collection("Todas las ids").document(id).set(mapf, SetOptions.merge());
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
