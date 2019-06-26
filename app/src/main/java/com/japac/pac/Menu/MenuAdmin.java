package com.japac.pac.Menu;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.common.collect.Iterables;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.Auth.Login;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.util.Calendar;

public class MenuAdmin extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final int Permisos = 8991;

    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    FirebaseFirestore firebaseFirestore;

    CollectionReference geoFirestoreRef, geoFirestoreRef2;

    StorageReference almacenFirmas;

    FirebaseStorage almacen;

    StorageReference almacenRef;

    private TextView pPt;

    private Double latitudDetectada, longitudDetectada, latitudGuardada, longitudGuardada, distan;

    private Button btnRegistroJornada, btnObras, Cerrar, btnEmpleados, btnRegistro;

    private String empleado, rolE, roll = "Empleado", IoF, mañaOtard, emailElim, idElim, sa, ultimo1, codigoEmpleado, codigoEmpleadoChech, codigo, letras1, letras2, letras3, snombre, empresa, fecha, jefeElim, trayecto, mes1, ano1, mes, dia, hora, entrada_salida, nombre, roles, obra, codigoEmpresa, id, comp, obcomp, obcomprueba, nombreAm, emailAn, jefes;

    private ArrayAdapter<String> obraAdapter, jefeAdapter, anoAdapter;

    private Spinner obraSpinner, jefeSpinner, anoMesSpinner;

    private ImageView logo;

    private FirebaseAuth mAuth;

    private View mSpinner, mLoginDialog, mNombres, mAnoMes;

    private List<String> obs, jfs;

    private int spinnerPosition;

    private LocalizacionUsuario mLocalizarUsuario;

    private EditText nom;

    static final String patron = "0123456789BCDGHIKLMNOPQRSTUVWXYZbcdghiklmnopqrstuvwxyz";

    static SecureRandom aleatorio = new SecureRandom();

    private boolean otro = false, cerrar = false, trayectoBo = false;

    private ProgressBar cargando;

    private Uri filePath;

    private FusedLocationProviderClient mProovedor;

    private static final int ERROR_DIALOGO_PEDIR = 9001;

    private final int PICK_IMAGE_REQUEST = 71;

    private GeoPoint geoPointLocalizayo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_admin);

        cargando = new ProgressBar(this);
        cargando = (ProgressBar) findViewById(R.id.cargandoAdmin);

        if (compruebapermisos() && isServicesOK()) {
            btnRegistroJornada = (Button) findViewById(R.id.btnRegistrarJornadas);
            btnObras = (Button) findViewById(R.id.btnObras);
            Cerrar = (Button) findViewById(R.id.btnCerrar);
            btnEmpleados = (Button) findViewById(R.id.btnAdmEmpleados);
            btnRegistro = (Button) findViewById(R.id.pdfVer);
            pPt = (TextView) findViewById(R.id.PrivacyPolicy);

            logo = (ImageView) findViewById(R.id.logoAdmin);

            firebaseFirestore = FirebaseFirestore.getInstance();

            mAuth = FirebaseAuth.getInstance();

            almacen = FirebaseStorage.getInstance();
            almacenRef = almacen.getReference();

            id = mAuth.getCurrentUser().getUid();
            comp = "no";
            mLoginDialog = getLayoutInflater().inflate(R.layout.login_dialogo, null, false);
            cargandoloSI();
            primero();

            pPt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });

            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickFromGallery();
                }
            });

            Cerrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(MenuAdmin.this, Login.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0, 0);
                }
            });


            btnRegistroJornada.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dQuien();
                }
            });

            btnObras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MenuAdmin.this, MapaActivity.class);
                    startActivity(intent);
                }
            });

            btnEmpleados.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dAdministrarEmpleados();
                }
            });
            btnRegistro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leerRegistro();
                }
            });
            overridePendingTransition(0, 0);
        }
    }

    public void leerRegistro() {
        jfs.add(nombre);
        final AlertDialog.Builder registroLeer = new AlertDialog.Builder(MenuAdmin.this)
                .setTitle("¿De que empleado desea generar el documento?");
        jefeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                empleado = adapterView.getItemAtPosition(i).toString();
                firebaseFirestore.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains(empleado)) {
                            codigoEmpleadoChech = documentSnapshot.getString(empleado);
                            if (codigoEmpleadoChech.charAt(0) == 'J' && codigoEmpleadoChech.charAt(1) == 'e' && codigoEmpleadoChech.charAt(2) == 'F') {
                                if (codigoEmpleadoChech.charAt(7) == 'E') {
                                    rolE = "Empleado";
                                } else if (codigoEmpleadoChech.charAt(7) == 'a') {
                                    rolE = "Administrador";
                                }
                            } else if (codigoEmpleadoChech.charAt(4) == 'E') {
                                rolE = "Empleado";
                            } else if (codigoEmpleadoChech.charAt(4) == 'a') {
                                rolE = "Administrador";
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        registroLeer
                .setView(mNombres)
                .setPositiveButton("Generar", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoRegistroLeer = registroLeer.create();
        dialogoRegistroLeer.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positivoAc = (Button) dialogoRegistroLeer.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCa = (Button) dialogoRegistroLeer.getButton(AlertDialog.BUTTON_NEGATIVE);

                positivoAc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firebaseFirestore.collection("Empresas").document(empresa).collection(rolE).document(empleado).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    final String nif = documentSnapshot.getString("NIF");
                                    firebaseFirestore.collection("Empresas").document(empresa).collection(rolE).document(empleado).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String ans = documentSnapshot.getString("años");
                                            List<String> ansL = Arrays.asList(ans.split("\\s*,\\s*"));
                                            elegirFechasAños(ansL, rolE, empleado, nif);
                                            dialogoRegistroLeer.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MenuAdmin.this, "El empleado " + empleado + " no a registrado ninguna jornada todavia", Toast.LENGTH_LONG).show();
                                            jfs.remove(nombre);
                                            dialogoRegistroLeer.dismiss();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                negativoCa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoRegistroLeer.dismiss();
                    }
                });
            }
        });
        dialogoRegistroLeer.setCanceledOnTouchOutside(false);
        if (mNombres.getParent() != null) {
            ((ViewGroup) mNombres.getParent()).removeView(mNombres);
            firestoreNombres();
            dialogoRegistroLeer.show();
        } else {
            dialogoRegistroLeer.show();
        }

    }

    private void elegirFechasAños(List<String> años, final String roles, final String empleado, final String nif) {
        jfs.remove(nombre);
        mAnoMes = getLayoutInflater().inflate(R.layout.spinner_dialogo, null, false);
        anoMesSpinner = (Spinner) mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ano1 = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<String>(MenuAdmin.this, android.R.layout.simple_spinner_item, años);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final AlertDialog.Builder añoEle = new AlertDialog.Builder(MenuAdmin.this)
                .setTitle("Eliga un año");
        añoEle
                .setView(mAnoMes)
                .setPositiveButton("Siguiente", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(empleado).collection("Registro").document("AÑOS").collection(ano1).document("MESES").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String ms = documentSnapshot.getString("meses");
                                List<String> msL = Arrays.asList(ms.split("\\s*,\\s*"));
                                elegirFechasMeses(msL, roles, empleado, ano1, nif);
                                dialogInterface.dismiss();

                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialogoAñoEle = añoEle.create();
        dialogoAñoEle.setCanceledOnTouchOutside(false);
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mNombres);
            firestoreNombres();
            dialogoAñoEle.show();
        } else {
            dialogoAñoEle.show();
        }
    }

    private void elegirFechasMeses(List<String> meses, final String roles, final String empleado, final String ano, String nif) {
        mAnoMes = getLayoutInflater().inflate(R.layout.spinner_dialogo, null, false);
        anoMesSpinner = (Spinner) mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mes1 = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<String>(MenuAdmin.this, android.R.layout.simple_spinner_item, meses);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final AlertDialog.Builder mesEle = new AlertDialog.Builder(MenuAdmin.this)
                .setTitle("Eliga el mes");
        jefeSpinner.setOnItemSelectedListener(this);
        mesEle
                .setView(mAnoMes)
                .setPositiveButton("Siguiente", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(empleado).collection("Registro").document("AÑOS").collection(ano).document("MESES").collection(mes1).document("DIAS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String ds = documentSnapshot.getString("dias");
                                List<String> dsL = Arrays.asList(ds.split("\\s*,\\s*"));
                                PdfDocument pdfDocument = new PdfDocument();
                                PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
                                PdfDocument.Page myPage = pdfDocument.startPage(myPageInfo);

                                Paint myPaint = new Paint();

                                String myString = "hola";
                                int x = 10, y = 25;
                                myPage.getCanvas().drawText(myString, x, y, myPaint);
                                pdfDocument.finishPage(myPage);

                                String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/pdf.pdf";
                                File myFile = new File(myFilePath);
                                Toast.makeText(MenuAdmin.this, myFilePath, Toast.LENGTH_LONG).show();

                                try {
                                    pdfDocument.writeTo(new FileOutputStream(myFile));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                pdfDocument.close();
                                dialogInterface.dismiss();

                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        final AlertDialog dialogoMesEle = mesEle.create();
        dialogoMesEle.setCanceledOnTouchOutside(false);
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mNombres);
            firestoreNombres();
            dialogoMesEle.show();
        } else {
            dialogoMesEle.show();
        }
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

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MenuAdmin.this);

        if (available == ConnectionResult.SUCCESS) {

            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MenuAdmin.this, available, ERROR_DIALOGO_PEDIR);
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
                    public void onComplete(@NonNull Task task2) {
                        if (task2.isSuccessful()) {
                            Location locacizacionActual = (Location) task2.getResult();
                            geoPointLocalizayo = new GeoPoint(locacizacionActual.getLatitude(), locacizacionActual.getLongitude());
                            mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                            mLocalizarUsuario.setTimestamp(null);
                            guardarLocalizacion();
                        } else {

                        }
                    }
                });
                cargandoloNO();
            }
        } catch (SecurityException e) {

        }

    }

    private void pickFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cargandoloSI();
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                logo.setImageBitmap(bitmap);
                uploadMetodo();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadMetodo() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Subiendo...");
            progressDialog.show();

            StorageReference ref = almacenRef.child(empresa + "/" + "Logo/" + "Logo" + codigoEmpresa + ".jpg");
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MenuAdmin.this, "Subida", Toast.LENGTH_SHORT).show();
                            firebaseFirestore.collection("Empresas").document(empresa).update("logo", true);
                            firebaseFirestore.collection("Empresas").document(empresa).update("filepath", filePath.toString());
                            cargandoloNO();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MenuAdmin.this, "Fallo " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            cargandoloNO();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Subida al " + (int) progress + "%");
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

    private void primero() {
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    comp = documentSnapshot.getString("comprobar");
                    empresa = documentSnapshot.getString("empresa");
                    nombre = documentSnapshot.getString("nombre");
                    roles = documentSnapshot.getString("rol");
                    if (!otro) {
                        nombreAm = documentSnapshot.getString("nombre");
                        emailAn = documentSnapshot.getString("email");
                    }
                    codigoEmpleado = documentSnapshot.getString("codigo empleado");
                    if (documentSnapshot.contains("obra")) {
                        obcomprueba = documentSnapshot.getString("obra");
                        if (comp.equals("iniciada")) {
                            crearCanalDeNotificaciones();
                        }
                    }
                    if (comp.equals("iniciada")) {
                        Cerrar.setEnabled(false);
                        cerrar = true;
                    } else if (comp.equals("no") || comp.equals("finalizada")) {
                        Cerrar.setEnabled(true);
                        cerrar = false;
                    }
                    if (comp.isEmpty()) {
                        firebaseFirestore.collection("Todas las ids").document(id).update("comprobar", "no");
                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", "no");
                    }
                    if (otro) {
                        dRegistrar();
                    }
                    firestoreObras();
                    logo.setVisibility(View.INVISIBLE);
                    btnEmpleados.setVisibility(View.INVISIBLE);
                    btnObras.setVisibility(View.INVISIBLE);
                    btnRegistroJornada.setVisibility(View.INVISIBLE);
                    Cerrar.setVisibility(View.INVISIBLE);
                    almacenRef.child(empresa + "/" + "Logo/" + "Logo" + codigoEmpresa + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(logo);
                            logo.setVisibility(View.VISIBLE);
                            btnEmpleados.setVisibility(View.VISIBLE);
                            btnObras.setVisibility(View.VISIBLE);
                            btnRegistroJornada.setVisibility(View.VISIBLE);
                            Cerrar.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            logo.setVisibility(View.VISIBLE);
                            btnEmpleados.setVisibility(View.VISIBLE);
                            btnObras.setVisibility(View.VISIBLE);
                            btnRegistroJornada.setVisibility(View.VISIBLE);
                            Cerrar.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Toast.makeText(MenuAdmin.this, "NO EXISTE ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
        overridePendingTransition(0, 0);

    }

    private void firestoreObras() {
        obs = new ArrayList<String>();
        firebaseFirestore.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        obs.add(obran);

                    }
                    mSpinner = getLayoutInflater().inflate(R.layout.spinner_dialogo, null, false);
                    obraSpinner = (Spinner) mSpinner.findViewById(R.id.spinnerObra);

                    obraSpinner.setOnItemSelectedListener(MenuAdmin.this);
                    obraAdapter = new ArrayAdapter<String>(MenuAdmin.this, android.R.layout.simple_spinner_item, obs);
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
                    mLoginDialog = getLayoutInflater().inflate(R.layout.login_dialogo, null, false);
                } else if (!task.isSuccessful()) {
                    obs.add("SIN OBRAS");
                }
            }
        });
        firestoreNombres();
    }

    private int leeObras(Spinner spinner, String obraselecionada) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(obraselecionada)) {
                return i;
            }
        }
        return 0;
    }

    private void firestoreNombres() {
        geoFirestoreRef = firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado");
        geoFirestoreRef2 = firebaseFirestore.collection("Empresas").document(empresa).collection("Administrador");
        jfs = new ArrayList<String>();
        geoFirestoreRef2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String jefe = document.getString("nombre");
                        jfs.add(jefe);
                    }
                    geoFirestoreRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                            if (task2.isSuccessful()) {
                                for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                    String jefe = document2.getString("nombre");
                                    jfs.add(jefe);
                                }
                                jfs.remove(nombreAm);
                            }
                        }
                    });
                    mNombres = getLayoutInflater().inflate(R.layout.spinner_dialogo, null, false);
                    jefeSpinner = (Spinner) mNombres.findViewById(R.id.spinnerObra);
                    jefeSpinner.setOnItemSelectedListener(MenuAdmin.this);
                    jefeAdapter = new ArrayAdapter<String>(MenuAdmin.this, android.R.layout.simple_spinner_item, jfs);
                    jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    jefeSpinner.setAdapter(jefeAdapter);
                } else if (!task.isSuccessful()) {
                    jfs.add("sin empleados");
                }
            }
        });
        detalles();
    }

    private void dQuien() {
        final AlertDialog.Builder Quien = new AlertDialog.Builder(MenuAdmin.this);
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
        final AlertDialog.Builder registroJornada = new AlertDialog.Builder(MenuAdmin.this);
        registroJornada.setTitle(nombre);
        registroJornada.setMessage("Seleccione la obra")
                .setNeutralButton("Iniciar", null)
                .setNegativeButton("Finalizar", null)
                .setPositiveButton("Cancelar", null);
        registroJornada.setView(mSpinner);
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
                            Toast.makeText(MenuAdmin.this, "Ya existe una jornada iniciada, finalizala primero", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(MenuAdmin.this, "Debes iniciar primero una jornada", Toast.LENGTH_SHORT).show();
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
        if (mSpinner.getParent() != null) {
            ((ViewGroup) mSpinner.getParent()).removeView(mSpinner);
            firestoreObras();
        }
        dialogoRegistro.show();

    }

    private void dLogin() {
        final AlertDialog.Builder Login = new AlertDialog.Builder(MenuAdmin.this);
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
                                        otro = true;
                                        dialogoLogin.dismiss();
                                        primero();
                                    } else {
                                        Toast.makeText(MenuAdmin.this, "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                                        dialogoLogin.show();
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
                    Toast.makeText(MenuAdmin.this, "No te encuentras dentro de la obra seleccionada", Toast.LENGTH_SHORT).show();
                    dRegistrar();
                }
            }
        });
    }

    private void compruebaObra() {
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    obcomp = documentSnapshot.getString("obra");
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
                        Toast.makeText(MenuAdmin.this, "No has iniciado jornada en esta obra", Toast.LENGTH_SHORT).show();
                        dRegistrar();
                    }
                }
            }
        });
        overridePendingTransition(0, 0);
    }

    private void enviajornada() {
        if (entrada_salida.equals("salida")) {
            crearCanalDeNotificaciones();
        }
        DateFormat dfecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat daño = new SimpleDateFormat("yyyy");
        DateFormat dmes = new SimpleDateFormat("MM");
        DateFormat ddia = new SimpleDateFormat("dd");
        fecha = dfecha.format(Calendar.getInstance().getTime());
        ano1 = daño.format(Calendar.getInstance().getTime());
        mes = dmes.format(Calendar.getInstance().getTime());
        dia = ddia.format(Calendar.getInstance().getTime());

        DateFormat dhora = new SimpleDateFormat("HH:mm:ss");
        hora = dhora.format(Calendar.getInstance().getTime());

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 0 && timeOfDay < 12) {
            mañaOtard = "Mañana";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            mañaOtard = "Tarde";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            mañaOtard = "Tarde";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            mañaOtard = "Noche";
        }
        final Map<String, Object> map = new HashMap<>();
        map.put("nombre", nombre);
        map.put("Entrada o Salida", entrada_salida);
        map.put("obra", obra);
        map.put("rol", roles);
        map.put("fecha", fecha);
        map.put("hora", hora);
        map.put("Mañana o tarde", mañaOtard);
        map.put("UID", id);
        if (otro) {
            map.put("iniciado por", nombreAm);
        }
        if (trayectoBo) {
            trayectoBo = false;
            map.put("Trayecto desde " + obcomp + " hasta " + obra, trayecto);
        }
        final Map<String, String> mapA = new HashMap<>();
        final Map<String, String> mapM = new HashMap<>();
        final Map<String, String> mapD = new HashMap<>();
        firebaseFirestore.collection("Empresas").document(empresa).collection("Registro").document(ano1).collection(mes).document(dia).collection(hora).document(nombre).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains("años")) {
                            String a = documentSnapshot.getString("años");
                            if (a.isEmpty()) {
                                mapA.put("años", ano1);
                            } else {
                                if (!a.contains(ano1)) {
                                    mapA.put("años", a + ", " + ano1);
                                } else if (a.contains(ano1)) {
                                    mapA.put("años", ano1);

                                }
                            }
                            firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.contains("meses")) {
                                        String m = documentSnapshot.getString("meses");
                                        if (m.isEmpty()) {
                                            mapM.put("meses", mes);
                                        } else {
                                            if (!m.contains(mes)) {
                                                mapM.put("meses", m + ", " + mes);
                                            } else if (m.contains(mes)) {
                                                mapM.put("meses", mes);

                                            }
                                        }
                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.contains("dias")) {
                                                    String d = documentSnapshot.getString("dias");
                                                    if (d.isEmpty()) {
                                                        mapD.put("dias", dia);
                                                    } else {
                                                        if (!d.contains(dia)) {
                                                            mapD.put("dias", d + ", " + dia);
                                                        } else if (d.contains(dia)) {
                                                            mapD.put("dias", dia);

                                                        }
                                                    }
                                                } else {
                                                    mapD.put("dias", dia);
                                                }
                                            }
                                        });
                                    } else {
                                        mapM.put("meses", mes);
                                        mapD.put("dias", dia);
                                    }
                                }
                            });
                        } else {
                            mapA.put("años", ano1);
                            mapM.put("meses", mes);
                            mapD.put("dias", dia);
                        }
                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").set(mapA).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").set(mapM).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").set(mapD).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").collection(dia).document(dia).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (otro) {
                                                            otro = false;
                                                            dConfirma();
                                                        } else if (!otro) {
                                                            primero();
                                                        }
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
            }
        });
        overridePendingTransition(0, 0);
    }

    private void dConfirma() {
        mAuth.signOut();
        final AlertDialog.Builder Confirma = new AlertDialog.Builder(MenuAdmin.this);
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
                                    if (task.isSuccessful()) {
                                        id = mAuth.getCurrentUser().getUid();
                                        dialogoConfirma.dismiss();
                                        primero();
                                    } else if (task.isCanceled()) {
                                        dialogoConfirma.show();
                                    }
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

    private void dAdministrarEmpleados() {

        final AlertDialog.Builder obraAdministrarEmpledos = new AlertDialog.Builder(MenuAdmin.this)
                .setTitle("Menu de administracion de empleados");
        obraSpinner.setOnItemSelectedListener(MenuAdmin.this);
        obraAdministrarEmpledos
                .setPositiveButton("Generar empleado", null)
                .setNeutralButton("Eliminar empleado", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoAdministradorEmpleados = obraAdministrarEmpledos.create();
        dialogoAdministradorEmpleados.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positivoCrearEmp = dialogoAdministradorEmpleados.getButton(AlertDialog.BUTTON_POSITIVE);
                Button neutralElEm = dialogoAdministradorEmpleados.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button negativoCa = dialogoAdministradorEmpleados.getButton(AlertDialog.BUTTON_NEGATIVE);
                positivoCrearEmp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministradorEmpleados.dismiss();
                        dGeneraEmpleado();
                    }
                });

                neutralElEm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministradorEmpleados.dismiss();
                        dEliminarEmpleado();
                    }
                });

                negativoCa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministradorEmpleados.dismiss();
                    }
                });
            }
        });
        dialogoAdministradorEmpleados.setCanceledOnTouchOutside(false);
        dialogoAdministradorEmpleados.show();


    }

    private void dGeneraEmpleado() {

        final AlertDialog.Builder empleadoGen = new AlertDialog.Builder(MenuAdmin.this);
        View mCrearEmpleadoDialogo = getLayoutInflater().inflate(R.layout.generar_empleado, null);
        nom = mCrearEmpleadoDialogo.findViewById(R.id.nombreDialogo);
        final Switch emp = mCrearEmpleadoDialogo.findViewById(R.id.switchEmp);
        empleadoGen
                .setView(mCrearEmpleadoDialogo)
                .setTitle("Generador de empleados")
                .setPositiveButton("Crear", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoEmpleadoGen = empleadoGen.create();
        dialogoEmpleadoGen.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positivoCrea = (Button) dialogoEmpleadoGen.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCancela = (Button) dialogoEmpleadoGen.getButton(AlertDialog.BUTTON_NEGATIVE);
                emp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            letras2 = "a";
                        } else if (isChecked == false) {
                            letras2 = "E";
                        }
                    }
                });
                letras2 = "E";
                emp.setChecked(false);
                positivoCrea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoEmpleadoGen.dismiss();
                        letras1 = codigoRandom(4);
                        letras3 = codigoRandom(5) + codigoEmpleado.substring(codigoEmpleado.length() - 3);
                        snombre = nom.getText().toString();
                        if (!snombre.isEmpty()) {
                            if (snombre.length() > 3) {
                                if (jfs.isEmpty()) {
                                    codigo = letras1 + letras2 + letras3;
                                    firebaseFirestore.collection("Codigos").document(codigoEmpresa).update(snombre, codigo);
                                    firestoreNombres();
                                } else if (!jfs.isEmpty()) {
                                    String ultimo = Iterables.getLast(jfs);
                                    ultimo1 = Normalizer.normalize(ultimo, Normalizer.Form.NFD)
                                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                                            .toLowerCase();
                                    final String snombrea = Normalizer.normalize(snombre, Normalizer.Form.NFD)
                                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                                            .toLowerCase();
                                    for (String s : jfs) {
                                        sa = Normalizer.normalize(s, Normalizer.Form.NFD)
                                                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                                                .toLowerCase();
                                        if (sa.equals(snombrea)) {
                                            snombre = s;
                                            break;
                                        }
                                    }
                                    if (sa.equals(snombrea)) {
                                        firebaseFirestore.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.contains(snombre)) {
                                                    codigoEmpleadoChech = documentSnapshot.getString(snombre);
                                                    if (codigoEmpleadoChech.charAt(0) == 'J' && codigoEmpleadoChech.charAt(1) == 'e' && codigoEmpleadoChech.charAt(2) == 'F') {
                                                        if (codigoEmpleadoChech.charAt(7) == 'E') {
                                                            nom.setError(snombre + " ya es un empleado y jefe de obra de " + empresa);
                                                        } else if (codigoEmpleadoChech.charAt(7) == 'a') {
                                                            nom.setError(snombre + " ya es un administrador y jefe de obra de " + empresa);
                                                        }
                                                    } else if (codigoEmpleadoChech.charAt(4) == 'E') {
                                                        nom.setError(snombre + " ya es un empleado de " + empresa);
                                                    } else if (codigoEmpleadoChech.charAt(4) == 'a') {
                                                        nom.setError(snombre + " ya es un administrador y jefe de obra de " + empresa);
                                                    }
                                                }
                                            }
                                        });
                                    } else if (!sa.equals(snombrea)) {
                                        if (sa.equals(ultimo1)) {
                                            codigo = letras1 + letras2 + letras3;
                                            firebaseFirestore.collection("Codigos").document(codigoEmpresa).update(snombre, codigo);
                                            firestoreNombres();
                                        }
                                    }
                                }
                            } else {
                                nom.setError("Nombre muy pequeño");
                            }
                        } else {
                            nom.setError("Escriba un nombre");
                        }
                    }
                });
                negativoCancela.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoEmpleadoGen.dismiss();
                    }
                });
            }
        });
        dialogoEmpleadoGen.setCanceledOnTouchOutside(false);
        dialogoEmpleadoGen.show();

    }

    String codigoRandom(int largo) {
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++)
            sb.append(patron.charAt(aleatorio.nextInt(patron.length())));
        return sb.toString();

    }

    private void dEliminarEmpleado() {

        final AlertDialog.Builder EliminarEmpleado = new AlertDialog.Builder(MenuAdmin.this)
                .setTitle("¿Que empleado desea eliminar?");
        obraSpinner.setOnItemSelectedListener(MenuAdmin.this);
        EliminarEmpleado
                .setView(mNombres)
                .setPositiveButton("Siguiente", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoEliminarEmpleado = EliminarEmpleado.create();
        dialogoEliminarEmpleado.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positivoSig = (Button) dialogoEliminarEmpleado.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCance = (Button) dialogoEliminarEmpleado.getButton(AlertDialog.BUTTON_NEGATIVE);

                positivoSig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoEliminarEmpleado.dismiss();
                        if (jefes.equals(nombreAm)) {
                            Toast.makeText(MenuAdmin.this, nombreAm + " es esta cuenta, solo puede ser eliminada por otro Administrador", Toast.LENGTH_LONG).show();
                        } else if (!jefes.equals(nombreAm)) {
                            firebaseFirestore.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        roll = documentSnapshot.getString(jefes);
                                        if (roll.charAt(0) == 'J' && roll.charAt(1) == 'e' && roll.charAt(2) == 'F') {
                                            if (roll.charAt(7) == 'a') {
                                                roll = "Administrador";
                                                dElimEmpleado();
                                            } else if (roll.charAt(7) == 'E') {
                                                roll = "Empleado";
                                                dElimEmpleado();
                                            }
                                        } else {
                                            if (roll.charAt(4) == 'a') {
                                                roll = "Administrador";
                                                dElimEmpleado();
                                            } else if (roll.charAt(4) == 'E') {
                                                roll = "Empleado";
                                                dElimEmpleado();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                });

                negativoCance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoEliminarEmpleado.dismiss();
                    }
                });
            }
        });
        dialogoEliminarEmpleado.setCanceledOnTouchOutside(false);
        if (mNombres.getParent() != null) {
            ((ViewGroup) mNombres.getParent()).removeView(mNombres);
            firestoreNombres();
            dialogoEliminarEmpleado.show();
        } else {
            dialogoEliminarEmpleado.show();
        }


    }

    private void dElimEmpleado() {
        firebaseFirestore.collection("Empresas").document(empresa).collection(roll).document(jefes).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documento1 = task.getResult();
                    idElim = documento1.getString("id");
                    emailElim = documento1.getString("email");
                    jefeElim = documento1.getString("jefe");
                    almacenFirmas = FirebaseStorage.getInstance().getReference().child(empresa + "/Firmas/" + jefes + "/" + idElim + ".jpg");
                    almacenFirmas.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseFirestore.collection("Empresas").document(empresa).collection(roll).document(jefes).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseFirestore.collection("Todas las ids").document(idElim).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Codigos").document(codigoEmpresa).update(jefes, "ELIMINADO").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Localizaciones").document(jefes).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            String JFob = document.getString("jefe");
                                                                            String obran = document.getString("obra");
                                                                            if (jefes.equals(JFob)) {
                                                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Obras").document(obran).update("jefe", "no").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        firebaseFirestore.collection("Jefes").document(idElim).delete();
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    }
                                                                    firestoreNombres();
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
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        obra = parent.getItemAtPosition(position).toString();
        jefes = parent.getItemAtPosition(position).toString();
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
        btnObras.setEnabled(false);
        Cerrar.setEnabled(false);
        btnEmpleados.setEnabled(false);
    }

    private void cargandoloNO() {
        btnRegistroJornada.setEnabled(true);
        btnObras.setEnabled(true);
        if (cerrar) {
            Cerrar.setEnabled(false);
        } else if (!cerrar) {
            Cerrar.setEnabled(true);
        }
        btnEmpleados.setEnabled(true);
        cargando.setVisibility(View.INVISIBLE);
    }

}

