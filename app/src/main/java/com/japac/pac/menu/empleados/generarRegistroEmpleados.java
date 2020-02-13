package com.japac.pac.menu.empleados;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.menu.menu;
import com.japac.pac.pdf.templatePDF;
import com.japac.pac.R;
import com.japac.pac.servicios.snackbarDS;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class generarRegistroEmpleados extends Fragment {

    private FloatingActionButton botonReg;

    private FirebaseFirestore mDb;

    private String SHAREempleado;
    private String SHAREano;
    private String SHAREmes;
    private String cif;
    private String ano1;
    private String mes1;
    private String mesnu;
    private String codigoEmpresa;
    private String comp;
    private String empresa;
    private String nombre;
    private String roles;
    private String nombreAm;
    private String emailAn;
    private String codigoEmpleado;
    private String obcomprueba;

    private View mAnoMes;
    private View viewGrey;

    private FirebaseStorage almacen;
    private StorageReference almacenRef;

    private boolean next = true, end = false, emailShare, ayuda = false;

    private FloatingActionButton icAyuda;

    private ImageView ayudaImage;

    private TextView ayudaTextView;
    private TextView ayudaTextoRegistro;
    private TextView ayudaTextSalir;

    private ArrayAdapter<String> anoAdapter;

    private Spinner anoMesSpinner;

    private Animation anim;

    private CountDownTimer timerPDF;
    private CountDownTimer timerBtn;

    private File folder, localFile, fileShare;

    public generarRegistroEmpleados() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_generar_registro_empleados, container, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mDb = FirebaseFirestore.getInstance();
        almacen = FirebaseStorage.getInstance();
        almacenRef = almacen.getReference();
        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    comp = documentSnapshot.getString("comprobar");
                    empresa = documentSnapshot.getString("empresa");
                    nombre = documentSnapshot.getString("nombre");
                    roles = documentSnapshot.getString("rol");
                    nombreAm = documentSnapshot.getString("nombre");
                    emailAn = documentSnapshot.getString("email");
                    codigoEmpleado = documentSnapshot.getString("codigo empleado");
                    cif = documentSnapshot.getString("cif");
                    if (documentSnapshot.contains("obra")) {
                        obcomprueba = documentSnapshot.getString("obra");
                    }
                    icAyuda = RootView.findViewById(R.id.ic_ayuda);
                    icAyuda.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!ayuda){
                                ayuda = true;
                                anim = new AlphaAnimation(0.0f, 1.0f);
                                anim.setDuration(400);
                                anim.setStartOffset(20);
                                anim.setRepeatMode(Animation.REVERSE);
                                anim.setRepeatCount(Animation.INFINITE);
                                viewGrey = RootView.findViewById(R.id.viewGrey4);
                                ayudaImage = RootView.findViewById(R.id.alerta_ayuda_Registro);
                                ayudaTextView = RootView.findViewById(R.id.ayuda_generar_registro);
                                ayudaTextoRegistro = RootView.findViewById(R.id.ayuda_texto_registro);
                                ayudaTextSalir = RootView.findViewById(R.id.ayuda_salir);
                                icAyuda.setAnimation(anim);
                                botonReg.setAnimation(anim);
                                ayudaImage.setVisibility(View.VISIBLE);
                                ayudaTextView.setVisibility(View.VISIBLE);
                                ayudaTextSalir.setVisibility(View.VISIBLE);
                                viewGrey.setVisibility(View.VISIBLE);
                            }else {
                                ayuda = false;
                                icAyuda.clearAnimation();
                                botonReg.clearAnimation();
                                ayudaImage.setVisibility(View.GONE);
                                ayudaTextView.setVisibility(View.GONE);
                                ayudaTextoRegistro.setVisibility(View.GONE);
                                ayudaTextSalir.setVisibility(View.GONE);
                                viewGrey.setVisibility(View.GONE);
                            }
                        }
                    });
                    botonReg = RootView.findViewById(R.id.ic_regis);
                    botonReg.setScaleType(ImageView.ScaleType.CENTER);
                    botonReg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!ayuda){
                                if(timerBtn==null){
                                    final long[] grados = {0};
                                    timerBtn  = new CountDownTimer(60000, 10) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            grados[0] = grados[0] + 1;
                                            final OvershootInterpolator interpolator = new OvershootInterpolator();
                                            ViewCompat.animate(botonReg).
                                                    rotation(grados[0]).
                                                    withLayer().
                                                    setDuration(0).
                                                    setInterpolator(interpolator).
                                                    start();
                                        }

                                        @Override
                                        public void onFinish() {
                                            timerBtn.start();
                                        }
                                    }.start();
                                }
                                leerRegistro();
                            }else{
                                if(ayudaTextoRegistro.getVisibility()==View.VISIBLE){
                                    botonReg.setAnimation(anim);
                                    ayudaTextoRegistro.setVisibility(View.GONE);
                                }else{
                                    botonReg.clearAnimation();
                                    ayudaTextoRegistro.setVisibility(View.VISIBLE);

                                }
                            }
                        }
                    });
                    botonReg.setVisibility(View.VISIBLE);

                }
            }
        });


        return RootView;

    }

    private void leerRegistro() {
        menu.cargando(true);
        touch(true);
        mDb
                .collection("Empresas")
                .document(empresa)
                .collection("Empleado")
                .document(nombre)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot1) {
                        if (documentSnapshot1.exists()) {
                            final String nif = documentSnapshot1.getString("NIF");
                            final String naf = documentSnapshot1.getString("NAF");
                            mDb
                                    .collection("Empresas")
                                    .document(empresa)
                                    .collection("Empleado")
                                    .document(nombre)
                                    .collection("Registro")
                                    .document("AÑOS")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                            String ans = documentSnapshot2.getString("años");
                                            final List<String> ansL = Arrays.asList(Objects.requireNonNull(ans).split("\\s*,\\s*"));
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    final String idEm = documentSnapshot.getString("id");
                                                    try {

                                                        localFile = File.createTempFile("firma", "jpg");
                                                        almacenRef.child(empresa + "/Firmas/" + nombre + "/" + idEm + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                menu.cargando(false);
                                                                touch(false);
                                                                elegirFechasAnos(ansL, nombre, nif, naf, idEm);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                if(timerBtn!=null){
                                                                    timerBtn.cancel();
                                                                    ViewCompat.animate(botonReg)
                                                                            .rotation(0.0F)
                                                                            .withLayer()
                                                                            .setDuration(300)
                                                                            .setInterpolator(new OvershootInterpolator(10.0F))
                                                                            .start();
                                                                }
                                                                menu.cargando(false);
                                                                touch(false);
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        if(timerBtn!=null){
                                                            timerBtn.cancel();
                                                            ViewCompat.animate(botonReg)
                                                                    .rotation(0.0F)
                                                                    .withLayer()
                                                                    .setDuration(300)
                                                                    .setInterpolator(new OvershootInterpolator(10.0F))
                                                                    .start();
                                                        }
                                                        e.printStackTrace();
                                                        menu.cargando(false);
                                                        touch(false);
                                                    }

                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if(timerBtn!=null){
                                        timerBtn.cancel();
                                        ViewCompat.animate(botonReg)
                                                .rotation(0.0F)
                                                .withLayer()
                                                .setDuration(300)
                                                .setInterpolator(new OvershootInterpolator(10.0F))
                                                .start();
                                    }
                                    menu.cargando(false);
                                    touch(false);
                                    menu.snackbar.setText("El empleado " + nombre + " no a registrado ninguna jornada todavia");
                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setTextSize(10);
                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                    menu.snackbar.show();
                                }
                            });
                        }
                    }
                });

    }

    @SuppressLint("InflateParams")
    private void elegirFechasAnos(List<String> anos, final String empleado, final String nif, final String naf, final String id1) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el año");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        anoMesSpinner = mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ano1 = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, anos);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final Button botonSiguiente = mAnoMes.findViewById(R.id.btn1);
        final Button botonCancelar = mAnoMes.findViewById(R.id.btn2);
        final AlertDialog.Builder anoEle = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        anoEle
                .setView(mAnoMes);
        final AlertDialog dialogoAnoEle = anoEle.create();
        botonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection("Empleado")
                        .document(empleado)
                        .collection("Registro")
                        .document("AÑOS")
                        .collection(ano1)
                        .document("MESES")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot3) {
                                String ms = documentSnapshot3.getString("meses");
                                List<String> msL = Arrays.asList(Objects.requireNonNull(ms).split("\\s*,\\s*"));
                                menu.cargando(false);
                                touch(false);
                                elegirFechasMeses(msL, empleado, ano1, nif, naf, id1);
                                dialogoAnoEle.dismiss();

                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerBtn!=null){
                    timerBtn.cancel();
                    ViewCompat.animate(botonReg)
                            .rotation(0.0F)
                            .withLayer()
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(10.0F))
                            .start();
                }
                dialogoAnoEle.dismiss();
            }
        });
        dialogoAnoEle.setCanceledOnTouchOutside(false);
        dialogoAnoEle.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                anoMesSpinner.setEnabled(true);
                botonSiguiente.setEnabled(true);
                botonCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mAnoMes);
            mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
            dialogoAnoEle.show();
        } else {
            dialogoAnoEle.show();
        }
    }

    private void elegirFechasMeses(List<String> meses, final String empleado, final String ano3, final String nif, final String naf, final String id1) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el mes");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        anoMesSpinner = mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mes1 = adapterView.getItemAtPosition(i).toString();
                mesnu = adapterView.getItemAtPosition(i).toString();
                switch (mes1) {
                    case "01":
                        mes1 = "Enero";
                        break;
                    case "02":
                        mes1 = "Febrero";
                        break;
                    case "03":
                        mes1 = "Marzo";
                        break;
                    case "04":
                        mes1 = "Abril";
                        break;
                    case "05":
                        mes1 = "Mayo";
                        break;
                    case "06":
                        mes1 = "Junio";
                        break;
                    case "07":
                        mes1 = "Julio";
                        break;
                    case "08":
                        mes1 = "Agosto";
                        break;
                    case "09":
                        mes1 = "Septiembre";
                        break;
                    case "10":
                        mes1 = "Octubre";
                        break;
                    case "11":
                        mes1 = "Nobiembre";
                        break;
                    case "12":
                        mes1 = "Diciembre";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), android.R.layout.simple_spinner_item, meses);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final Button botonSiguiente = mAnoMes.findViewById(R.id.btn1);
        final Button botonCancelar = mAnoMes.findViewById(R.id.btn2);
        final AlertDialog.Builder mesEle = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        mesEle
                .setView(mAnoMes);
        final AlertDialog dialogoMesEle = mesEle.create();
        botonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection("Empleado")
                        .document(empleado)
                        .collection("Registro")
                        .document("AÑOS")
                        .collection(ano3)
                        .document("MESES")
                        .collection(mesnu)
                        .document("DIAS")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshotd) {
                                if (documentSnapshotd.exists()) {
                                    menu.cargando(false);
                                    touch(false);
                                    String dia = documentSnapshotd.getString("dias");
                                    List<String> diL = Arrays.asList(Objects.requireNonNull(dia).split("\\s*,\\s*"));
                                    creacionPdf(diL, empleado, mesnu, ano3, empresa, id1, nif, naf);
                                    dialogoMesEle.dismiss();
                                }
                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timerBtn!=null){
                    timerBtn.cancel();
                    ViewCompat.animate(botonReg)
                            .rotation(0.0F)
                            .withLayer()
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(10.0F))
                            .start();
                }
                dialogoMesEle.dismiss();
            }
        });
        dialogoMesEle.setCanceledOnTouchOutside(false);
        dialogoMesEle.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                anoMesSpinner.setEnabled(true);
                botonSiguiente.setEnabled(true);
                botonCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mAnoMes);
            mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
            dialogoMesEle.show();
        } else {
            dialogoMesEle.show();
        }
    }

    private void creacionPdf(final List<String> diasList, final String empl, String me, final String anoT, String empr, final String idT, String NIF, String NAF) {
        menu.cargando(true);
        touch(true);
        final templatePDF templatePDF = new templatePDF();
        templatePDF.openDocument(empl, me, anoT);
        templatePDF.addMetaData(empr, empl, me, anoT);
        templatePDF.crearHeader(empr, empl, cif, NIF, NAF, mes1, anoT);
        final int[] i = {0};
        timerPDF = new CountDownTimer(999999999, 1000) {
            @Override
            public void onTick(long mmenushareillisUntilFinished) {

                if (next) {

                    if (i[0] == diasList.size()) {
                        i[0] = 0;
                        end = false;
                        timerPDF.cancel();
                    } else {

                        next = false;
                        final String diList = diasList.get(i[0]);

                        mDb.collection("Empresas")
                                .document(empresa)
                                .collection("Empleado")
                                .document(empl)
                                .collection("Registro")
                                .document("AÑOS")
                                .collection(anoT)
                                .document("MESES")
                                .collection(mesnu)
                                .document("DIAS")
                                .collection(diList).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> horas = new ArrayList<>();
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                        horas.add(document.getId());
                                    }
                                }
                                String horaEn = horas.get(0);
                                String horaSa = horas.get(horas.size() - 1);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                long diferencia;
                                try {
                                    Date dateEn = simpleDateFormat.parse(horaEn);
                                    Date dateSa = simpleDateFormat.parse(horaSa);
                                    diferencia = Objects.requireNonNull(dateSa).getTime() - Objects.requireNonNull(dateEn).getTime();
                                    int days = (int) (diferencia / (1000 * 60 * 60 * 24));
                                    int hours = (int) ((diferencia - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                                    int mindif = (int) (diferencia - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                                    int horasExtras = 0;
                                    int minExtras = 0;

                                    if (hours >= 8) {
                                        if (hours > 8) {
                                            horasExtras = hours - 8;
                                        }
                                        if (mindif > 0) {
                                            minExtras = mindif;
                                        }
                                    }
                                    if (horasExtras != 0) {
                                        hours = hours - horasExtras;
                                    }
                                    if (minExtras != 0) {
                                        mindif = mindif - minExtras;
                                    }
                                    int horasTotales = hours + horasExtras;
                                    int minutosTotales = mindif + minExtras;
                                    horaEn = horas.get(0);
                                    horaSa = horas.get(horas.size() - 1);
                                    if (i[0] == diasList.size() - 1) {
                                        end = true;
                                    }
                                    templatePDF.tablaMain(diList, horaEn, horaSa, hours + ":" + mindif, horasExtras + ":" + minExtras, horasTotales + ":" + minutosTotales, idT, localFile.getAbsolutePath(), end, anoT, empresa, empl, mesnu, almacen);
                                    if (end) {
                                        SHAREempleado = empl;
                                        SHAREano = anoT;
                                        SHAREmes = mesnu;
                                        if(timerBtn!=null){
                                            timerBtn.cancel();
                                            ViewCompat.animate(botonReg)
                                                    .rotation(0.0F)
                                                    .withLayer()
                                                    .setDuration(300)
                                                    .setInterpolator(new OvershootInterpolator(10.0F))
                                                    .start();
                                        }
                                        menu.cargando(false);
                                        touch(false);
                                        menu.datos(folder, fileShare, SHAREempleado, mes1, SHAREano, SHAREmes, empresa, emailAn);
                                    }
                                    next = true;
                                    i[0]++;
                                } catch (ParseException e) {
                                    if(timerBtn!=null){
                                        timerBtn.cancel();
                                        ViewCompat.animate(botonReg)
                                                .rotation(0.0F)
                                                .withLayer()
                                                .setDuration(300)
                                                .setInterpolator(new OvershootInterpolator(10.0F))
                                                .start();
                                    }
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

            }

            @Override
            public void onFinish() {

            }

        }.start();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory()) {
                for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                    deleteRecursive(child);
                }
            }
            if (fileOrDirectory != null) {
                fileOrDirectory.delete();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!emailShare) {
            deleteRecursive(folder);
        }
    }

    public void onPause() {
        super.onPause();
        if (!emailShare) {
            deleteRecursive(folder);
        }
        if(timerBtn!=null){
            timerBtn.cancel();
            ViewCompat.animate(botonReg)
                    .rotation(0.0F)
                    .withLayer()
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator(10.0F))
                    .start();
        }
    }

    public void onResume() {
        super.onResume();
        if (emailShare) {
            emailShare = false;
            deleteRecursive(folder);
        }
    }

    private void touch(Boolean touch) {
        if (touch) {
            Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

}
