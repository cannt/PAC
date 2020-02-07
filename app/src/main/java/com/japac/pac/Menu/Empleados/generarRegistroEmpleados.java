package com.japac.pac.Menu.Empleados;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.japac.pac.Menu.ViewPagers.Menu;
import com.japac.pac.PDF.TemplatePDF;
import com.japac.pac.R;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class generarRegistroEmpleados extends Fragment {

    private FloatingActionButton botonReg;

    FirebaseFirestore mDb;

    private String SHAREempleado, SHAREano, SHAREmes, cif, ano1, mes1, mesnu, id, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba;

    private FirebaseAuth mAuth;

    private View mAnoMes;

    FirebaseStorage almacen;
    StorageReference almacenRef;

    private boolean next = true, end = false, emailShare;

    private ArrayAdapter<String> anoAdapter;

    private Spinner anoMesSpinner;

    private CountDownTimer timerPDF;

    private File folder, localFile, fileShare;

    public generarRegistroEmpleados() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View RootView = inflater.inflate(R.layout.fragment_generar_registro_empleados, container, false);
        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getCurrentUser().getUid();
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
                    botonReg = (FloatingActionButton) RootView.findViewById(R.id.ic_regis);
                    botonReg.setScaleType(ImageView.ScaleType.CENTER);
                    botonReg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            leerRegistro();
                        }
                    });
                    botonReg.setVisibility(View.VISIBLE);

                }
            }
        });


        return RootView;

    }

    private void leerRegistro() {
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
                            Menu.cargando(true);
                            touch(true);
                            final String nif = documentSnapshot1.getString("NIF");
                            final String naf = documentSnapshot1.getString("NAF");
                            String idreg = documentSnapshot1.getString("id");
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
                                            final List<String> ansL = Arrays.asList(ans.split("\\s*,\\s*"));
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    final String idEm = documentSnapshot.getString("id");
                                                    try {

                                                        localFile = File.createTempFile("firma", "jpg");
                                                        almacenRef.child(empresa + "/Firmas/" + nombre + "/" + idEm + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                Menu.cargando(false);
                                                                touch(false);
                                                                elegirFechasAños(ansL, "Empleado", nombre, nif, naf, idEm);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                Menu.cargando(false);
                                                                touch(false);
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        Menu.cargando(false);
                                                        touch(false);
                                                    }

                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Menu.cargando(false);
                                    touch(false);
                                    Toast.makeText(getContext(), "El empleado " + nombre + " no a registrado ninguna jornada todavia", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

    }

    private void elegirFechasAños(List<String> años, final String roles1, final String empleado, final String nif, final String naf, final String id1) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el año");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
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
        anoAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, años);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final Button botonSiguiente = mAnoMes.findViewById(R.id.btn1);
        final Button botonCancelar = mAnoMes.findViewById(R.id.btn2);
        final AlertDialog.Builder añoEle = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        añoEle
                .setView(mAnoMes);
        final AlertDialog dialogoAñoEle = añoEle.create();
        botonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection(roles1)
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
                                List<String> msL = Arrays.asList(ms.split("\\s*,\\s*"));
                                Menu.cargando(false);
                                touch(false);
                                elegirFechasMeses(msL, roles1, empleado, ano1, nif, naf, id1);
                                dialogoAñoEle.dismiss();

                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAñoEle.dismiss();
            }
        });
        dialogoAñoEle.setCanceledOnTouchOutside(false);
        dialogoAñoEle.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                anoMesSpinner.setEnabled(true);
                botonSiguiente.setEnabled(true);
                botonCancelar.setEnabled(true);
                Menu.cargando(false);
                touch(false);
            }
        });
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mAnoMes);
            mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
            dialogoAñoEle.show();
        } else {
            dialogoAñoEle.show();
        }
    }

    private void elegirFechasMeses(List<String> meses, final String roles1, final String empleado, final String ano3, final String nif, final String naf, final String id1) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el mes");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        anoMesSpinner = (Spinner) mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mes1 = adapterView.getItemAtPosition(i).toString();
                mesnu = adapterView.getItemAtPosition(i).toString();
                if (mes1.equals("01")) {
                    mes1 = "Enero";
                } else if (mes1.equals("02")) {
                    mes1 = "Febrero";
                } else if (mes1.equals("03")) {
                    mes1 = "Marzo";
                } else if (mes1.equals("04")) {
                    mes1 = "Abril";
                } else if (mes1.equals("05")) {
                    mes1 = "Mayo";
                } else if (mes1.equals("06")) {
                    mes1 = "Junio";
                } else if (mes1.equals("07")) {
                    mes1 = "Julio";
                } else if (mes1.equals("08")) {
                    mes1 = "Agosto";
                } else if (mes1.equals("09")) {
                    mes1 = "Septiembre";
                } else if (mes1.equals("10")) {
                    mes1 = "Octubre";
                } else if (mes1.equals("11")) {
                    mes1 = "Nobiembre";
                } else if (mes1.equals("12")) {
                    mes1 = "Diciembre";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, meses);
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
                Menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection(roles1)
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
                                    Menu.cargando(false);
                                    touch(false);
                                    String dia = documentSnapshotd.getString("dias");
                                    List<String> diL = Arrays.asList(dia.split("\\s*,\\s*"));
                                    creacionPdf(diL, empleado, roles1, mesnu, ano3, empresa, id1, nif, naf);
                                    dialogoMesEle.dismiss();
                                }
                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Menu.cargando(false);
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

    private void creacionPdf(final List<String> diasList, final String empl, final String rolT, String me, final String anoT, String empr, final String idT, String NIF, String NAF) {
        Menu.cargando(true);
        touch(true);
        final TemplatePDF templatePDF = new TemplatePDF(getContext());
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
                                .collection(rolT)
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
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        horas.add(document.getId());
                                    }
                                }
                                String horaEn = horas.get(0);
                                String horaSa = horas.get(horas.size() - 1);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                                Long diferencia;
                                try {
                                    Date dateEn = simpleDateFormat.parse(horaEn);
                                    Date dateSa = simpleDateFormat.parse(horaSa);
                                    diferencia = dateSa.getTime() - dateEn.getTime();
                                    int days = (int) (diferencia / (1000 * 60 * 60 * 24));
                                    int hours = (int) ((diferencia - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                                    int mindif = (int) (diferencia - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                                    int horasExtras = 0;
                                    int minExtras = 0;

                                    if (hours >= 8) {
                                        if (hours > 8) {
                                            horasExtras = hours - 8;
                                        } else {
                                            horasExtras = 0;
                                        }
                                        if (mindif > 0) {
                                            minExtras = mindif;
                                        } else if (mindif == 0) {
                                            minExtras = 0;
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
                                        Menu.cargando(false);
                                        touch(false);
                                        Menu.datos(folder, fileShare, SHAREempleado, mes1, SHAREano, SHAREmes, empresa, emailAn);
                                    }
                                    next = true;
                                    i[0]++;
                                } catch (ParseException e) {
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

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
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
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else if (!touch) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

}
