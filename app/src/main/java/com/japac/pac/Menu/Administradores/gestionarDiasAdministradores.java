package com.japac.pac.Menu.Administradores;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.japac.pac.Menu.ViewPagers.Menu;
import com.japac.pac.R;
import com.japac.pac.Servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class gestionarDiasAdministradores extends Fragment {

    private FirebaseAuth mAuth;

    private FirebaseFirestore firebaseFirestore;

    private String id, empresa, nombre, empleSelec, otroMot, motiv;

    private CalendarPickerView calendarPickerView, calendarPickerView2;

    private ArrayList<Date> collectionDates = new ArrayList<>(31), collectionDates2 = new ArrayList<>(31);

    private TextView diasSolicTextV, diasSolicTextV2;

    private View mEmpleados, mCambMot, mTresBtn, mDosBtn, mJustificar;

    private Spinner empleadosSpinner;

    private ArrayAdapter<String> empleadoAdapter;

    private SlidingUpPanelLayout slidingLayout;

    private Boolean hayDias = true;

    private ImageView xpand;

    private ViewGroup root;

    public gestionarDiasAdministradores() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Menu.cargando(true);
        touch(true);
        root = (ViewGroup) inflater.inflate(R.layout.fragment_gestionar_dias, null, false);
        final Date hoy = new Date();
        final Calendar siguienteAño = Calendar.getInstance();
        siguienteAño.add(Calendar.YEAR, 1);
        mAuth = FirebaseAuth.getInstance();
        xpand = root.findViewById(R.id.btnXpand);
        diasSolicTextV2 = (TextView) root.findViewById(R.id.DiasSoliList2);
        diasSolicTextV = (TextView) root.findViewById(R.id.DiasSoliList);
        diasSolicTextV.setText("Dias libres pendientes de aprobación");
        diasSolicTextV2.setText("Dias libres asignados\nDezlizar para mas detalles");
        calendarPickerView = root.findViewById(R.id.calendar_view);
        calendarPickerView2 = root.findViewById(R.id.calendar_view2);
        calendarPickerView.init(hoy, siguienteAño.getTime());
        calendarPickerView2.init(hoy, siguienteAño.getTime());
        slidingLayout = (SlidingUpPanelLayout) root.findViewById(R.id.sliding_layout);
        slidingLayout.setDragView(diasSolicTextV2);
        diasSolicTextV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (Menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        Menu.setCambioDeFragmento(false);
                    } else if (!Menu.getCambioDeFragment()) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    }
                }
                if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    if (Menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        Menu.setCambioDeFragmento(false);
                    } else if (!Menu.getCambioDeFragment()) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }

            }
        });
        diasSolicTextV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    if (Menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        Menu.setCambioDeFragmento(false);
                    } else if (!Menu.getCambioDeFragment()) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }

                }
            }
        });
        xpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    if (Menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        Menu.setCambioDeFragmento(false);
                    } else if (!Menu.getCambioDeFragment()) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    xpand.setImageResource(R.drawable.ic_expand_up);
                } else if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    if (Menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        Menu.setCambioDeFragmento(false);
                    } else if (!Menu.getCambioDeFragment()) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                    }
                    xpand.setImageResource(R.drawable.ic_expand_down);
                }
            }
        });
        slidingLayout.setAnchorPoint(0.92f);
        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (!hayDias) {
                    xpand.setVisibility(View.INVISIBLE);
                    xpand.setClickable(false);
                    diasSolicTextV2.setText("No hay dias libres asignados por ahora");
                } else if (hayDias) {
                    if (xpand.getVisibility() == View.INVISIBLE) {
                        xpand.setVisibility(View.VISIBLE);
                        xpand.setClickable(true);
                    }
                    if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        diasSolicTextV2.setText("Dias libres asignados");
                        xpand.setImageResource(R.drawable.ic_expand_down);
                    }
                    if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        diasSolicTextV2.setText("Dias libres asignados\nDezlizar para ver mas detalles");
                        xpand.setImageResource(R.drawable.ic_expand_up);
                    }
                    if (previousState == SlidingUpPanelLayout.PanelState.COLLAPSED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                        diasSolicTextV2.setText("Dias libres asignados");
                        xpand.setImageResource(R.drawable.ic_expand_down);
                    }
                    if (previousState == SlidingUpPanelLayout.PanelState.ANCHORED && newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                        diasSolicTextV2.setText("Dias libres asignados\nDezlizar para ver mas detalles");
                        xpand.setImageResource(R.drawable.ic_expand_up);

                    }
                    if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                        if (Menu.getCambioDeFragment()) {
                            actualizarCalendarioAd();
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            Menu.setCambioDeFragmento(false);
                        } else if (!Menu.getCambioDeFragment()) {
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                    }
                }
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        id = mAuth.getCurrentUser().getUid();
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                empresa = documentSnapshot.getString("empresa");
                nombre = documentSnapshot.getString("nombre");
                actualizarCalendarioAd();
                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {

                            return;
                        }
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getDocument().exists()) {
                                switch (doc.getType()) {
                                    case ADDED:
                                    case MODIFIED:
                                        actualizarCalendarioAd();
                                        if (doc.getDocument().getBoolean("solicita") != null && doc.getDocument().getBoolean("solicita")) {
                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "El empleado " + doc.getDocument().getString("nombre") + " a solicitado un dia libre", 8000)
                                                    .setActionTextColor(Color.WHITE);
                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(9);
                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                            snackbar.show();
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(doc.getDocument().getString("nombre")).update("solicita", false);
                                            if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                                                if (Menu.getCambioDeFragment()) {
                                                    actualizarCalendarioAd();
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                    Menu.setCambioDeFragmento(false);
                                                } else if (!Menu.getCambioDeFragment()) {
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                }
                                            }
                                        }
                                        break;
                                    case REMOVED:
                                        actualizarCalendarioAd();
                                        break;
                                }
                            }
                        }
                    }
                });

            }
        });
        Menu.cargando(false);
        touch(false);
        return root;
    }

    private void dialogoLibresAd(final Date date) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        final String datefull = formato.format(date);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final ArrayList<String> contieneDias = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String diasS = document.getString("Dias libres");
                        if (diasS != null) {
                            if (diasS.contains(datefull)) {
                                contieneDias.add(document.getString("nombre"));
                            }
                        }
                    }
                    if (!contieneDias.isEmpty()) {
                        if (contieneDias.size() == 1) {
                            gestDiasAd(contieneDias.get(0), datefull);
                        } else if (contieneDias.size() > 1) {
                            mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                            empleadosSpinner = (Spinner) mEmpleados.findViewById(R.id.spinnerObra);
                            empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    empleSelec = parent.getItemAtPosition(position).toString();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            empleadoAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, contieneDias);
                            empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            empleadosSpinner.setAdapter(empleadoAdapter);
                            final TextView myMsgtitle = new TextView(getActivity());
                            myMsgtitle.setText("Dia " + datefull + " libre " + "actualmente asignado a los siguientes empleados" + contieneDias.get(0) + "\nSeleccione uno de la lista para gestionarlo");
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);
                            final Button btnSiguient = (Button) mEmpleados.findViewById(R.id.btn1);
                            final Button btnCance = (Button) mEmpleados.findViewById(R.id.btn2);
                            AlertDialog.Builder AdministrarDiasLibresAd = new AlertDialog.Builder(getContext())
                                    .setCustomTitle(myMsgtitle)
                                    .setView(mEmpleados);
                            final AlertDialog dialogoAdministrarDiasLibresAd = AdministrarDiasLibresAd.create();
                            btnCance.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoAdministrarDiasLibresAd.dismiss();

                                }
                            });

                            btnSiguient.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    gestDiasAd(empleSelec, datefull);
                                    dialogoAdministrarDiasLibresAd.dismiss();
                                }
                            });
                            dialogoAdministrarDiasLibresAd.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnSiguient.setEnabled(true);
                                    btnCance.setEnabled(true);
                                }
                            });
                            dialogoAdministrarDiasLibresAd.setCanceledOnTouchOutside(false);
                            if (mEmpleados.getParent() != null) {
                                ((ViewGroup) mEmpleados.getParent()).removeView(mEmpleados);
                                mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                dialogoAdministrarDiasLibresAd.show();
                            } else {
                                dialogoAdministrarDiasLibresAd.show();
                            }

                        }

                    } else {
                        final ArrayList<String> contieneDias2 = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String diasS = document.getString("Dias libres solicitados");
                            if (diasS != null) {
                                if (diasS.contains(datefull)) {
                                    contieneDias2.add(document.getString("nombre"));
                                }
                            }
                        }
                        if (!contieneDias2.isEmpty()) {
                            if (contieneDias2.size() == 1) {
                                dialogoAceptAdmin(contieneDias2.get(0), datefull);
                            } else if (contieneDias2.size() > 1) {
                                mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                empleadosSpinner = (Spinner) mEmpleados.findViewById(R.id.spinnerObra);
                                empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        empleSelec = parent.getItemAtPosition(position).toString();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                                empleadoAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, contieneDias2);
                                empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                empleadosSpinner.setAdapter(empleadoAdapter);
                                final TextView myMsgtitle = new TextView(getActivity());
                                myMsgtitle.setText("Empleados que quieren el dia " + datefull + " libre\nSeleccione uno de la lista para consultar sus motivos");
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);
                                final Button btnSiguient = (Button) mEmpleados.findViewById(R.id.btn1);
                                final Button btnCance = (Button) mEmpleados.findViewById(R.id.btn2);
                                AlertDialog.Builder AdministrarDiasLibresAd = new AlertDialog.Builder(getContext())
                                        .setCustomTitle(myMsgtitle)
                                        .setView(mEmpleados);
                                final AlertDialog dialogoAdministrarDiasLibresAd = AdministrarDiasLibresAd.create();
                                btnCance.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoAdministrarDiasLibresAd.dismiss();

                                    }
                                });

                                btnSiguient.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoAceptAdmin(empleSelec, datefull);
                                        dialogoAdministrarDiasLibresAd.dismiss();
                                    }
                                });
                                dialogoAdministrarDiasLibresAd.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        btnCance.setEnabled(true);
                                        btnSiguient.setEnabled(true);
                                    }
                                });
                                dialogoAdministrarDiasLibresAd.setCanceledOnTouchOutside(false);
                                if (mEmpleados.getParent() != null) {
                                    ((ViewGroup) mEmpleados.getParent()).removeView(mEmpleados);
                                    mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                    dialogoAdministrarDiasLibresAd.show();
                                } else {
                                    dialogoAdministrarDiasLibresAd.show();
                                }

                            }

                        } else {
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    final ArrayList<String> contieneDias3 = new ArrayList<>();
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            contieneDias3.add(document.getString("nombre"));
                                        }
                                        if (!contieneDias3.isEmpty()) {
                                            if (contieneDias3.size() == 1) {
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("¿Desea darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final Button btnAsigDia = (Button) mDosBtn.findViewById(R.id.btn1);
                                                final Button btnCancelar = (Button) mDosBtn.findViewById(R.id.btn2);
                                                AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(getContext())
                                                        .setCustomTitle(myMsgtitle)
                                                        .setView(mDosBtn);
                                                final AlertDialog dialogoAdministrarDiasLibresAd2 = AdministrarDiasLibresAd2.create();
                                                btnCancelar.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoAdministrarDiasLibresAd2.dismiss();

                                                    }
                                                });

                                                btnAsigDia.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoAdministrarDiasLibresAd2.dismiss();
                                                        final TextView titulo = new TextView(getActivity());
                                                        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                                                        titulo.setTextColor(Color.BLACK);
                                                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                                                        final String datefull = formato.format(date);
                                                        titulo.setText("¿Porque motivo deseas darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                        final Button btnVaca = (Button) mCambMot.findViewById(R.id.Vacas);
                                                        final Button btnBaja = (Button) mCambMot.findViewById(R.id.Baja);
                                                        final Button btnOtros = (Button) mCambMot.findViewById(R.id.Otros);
                                                        final Button btnCance = (Button) mCambMot.findViewById(R.id.Cancelar);
                                                        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                                                                .setCustomTitle(titulo)
                                                                .setView(mCambMot);
                                                        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                                                        btnVaca.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                asignariDiaLibreAd(contieneDias3.get(0), datefull + "V");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnBaja.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                asignariDiaLibreAd(contieneDias3.get(0), datefull + "B");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnOtros.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialogoAdministrarDiasLibres.dismiss();
                                                                mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                                                final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                                                                final EditText sJustificar = mJustificar.findViewById(R.id.justificaDialogo);
                                                                final Button btnJusti = (Button) mJustificar.findViewById(R.id.btn1);
                                                                Login
                                                                        .setTitle("Justifique el motivo")
                                                                        .setView(mJustificar);
                                                                final AlertDialog dialogoLogin = Login.create();
                                                                btnJusti.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        final String JustTexto = sJustificar.getText().toString();

                                                                        if (!JustTexto.isEmpty()) {

                                                                            otroMot = JustTexto;
                                                                            sJustificar.setHintTextColor(Color.GRAY);
                                                                            sJustificar.setError(null);
                                                                            asignariDiaLibreAd(contieneDias3.get(0), datefull + "O");
                                                                            dialogoLogin.dismiss();


                                                                        } else if (JustTexto.isEmpty()) {

                                                                            sJustificar.setHintTextColor(Color.RED);
                                                                            sJustificar.setError("No puede dejar este texto vacio");

                                                                        }
                                                                    }
                                                                });
                                                                dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                    @Override
                                                                    public void onShow(final DialogInterface dialog) {
                                                                        sJustificar.setEnabled(true);
                                                                        btnJusti.setEnabled(true);
                                                                    }
                                                                });
                                                                dialogoLogin.setCanceledOnTouchOutside(false);
                                                                if (mJustificar.getParent() != null) {
                                                                    ((ViewGroup) mJustificar.getParent()).removeView(mJustificar);
                                                                    mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                                                    dialogoLogin.show();

                                                                } else {
                                                                    dialogoLogin.show();
                                                                }
                                                            }
                                                        });
                                                        btnCance.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialogoAdministrarDiasLibres.dismiss();
                                                            }
                                                        });
                                                        dialogoAdministrarDiasLibres.setOnShowListener(new DialogInterface.OnShowListener() {
                                                            @Override
                                                            public void onShow(DialogInterface dialog) {

                                                                btnVaca.setEnabled(true);
                                                                btnBaja.setEnabled(true);
                                                                btnOtros.setEnabled(true);
                                                                btnCance.setEnabled(true);
                                                            }
                                                        });
                                                        dialogoAdministrarDiasLibres.setCanceledOnTouchOutside(false);
                                                        if (mCambMot.getParent() != null) {
                                                            ((ViewGroup) mCambMot.getParent()).removeView(mCambMot);
                                                            mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                            dialogoAdministrarDiasLibres.show();
                                                        } else {
                                                            dialogoAdministrarDiasLibres.show();
                                                        }
                                                    }
                                                });
                                                dialogoAdministrarDiasLibresAd2.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialog) {
                                                        btnAsigDia.setEnabled(true);
                                                        btnCancelar.setEnabled(true);

                                                    }
                                                });
                                                dialogoAdministrarDiasLibresAd2.setCanceledOnTouchOutside(false);
                                                if (mDosBtn.getParent() != null) {
                                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                    dialogoAdministrarDiasLibresAd2.show();
                                                } else {
                                                    dialogoAdministrarDiasLibresAd2.show();
                                                }

                                            } else if (contieneDias3.size() > 1) {
                                                mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                                empleadosSpinner = (Spinner) mEmpleados.findViewById(R.id.spinnerObra);
                                                empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        empleSelec = parent.getItemAtPosition(position).toString();
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                                empleadoAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, contieneDias3);
                                                empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                empleadosSpinner.setAdapter(empleadoAdapter);
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("¿Desea asignar el dia " + datefull + " libre? \nSeleccione un empleado de la lista primero");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
                                                final Button btnSiguiente = (Button) mEmpleados.findViewById(R.id.btn1);
                                                final Button btnCancelar = (Button) mEmpleados.findViewById(R.id.btn2);
                                                AlertDialog.Builder AdministrarDiasLibresAd = new AlertDialog.Builder(getContext())
                                                        .setCustomTitle(myMsgtitle)
                                                        .setView(mEmpleados);
                                                final AlertDialog dialogoAdministrarDiasLibresAd = AdministrarDiasLibresAd.create();
                                                btnCancelar.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoAdministrarDiasLibresAd.dismiss();

                                                    }
                                                });

                                                btnSiguiente.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoAdministrarDiasLibresAd.dismiss();
                                                        final TextView titulo = new TextView(getActivity());
                                                        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                                                        titulo.setTextColor(Color.BLACK);
                                                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                                                        final String datefull = formato.format(date);
                                                        titulo.setText("¿Porque motivo deseas darle el dia " + datefull + " libre a " + empleSelec + "?");
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                        final Button btnVaca = (Button) mCambMot.findViewById(R.id.Vacas);
                                                        final Button btnBaja = (Button) mCambMot.findViewById(R.id.Baja);
                                                        final Button btnOtros = (Button) mCambMot.findViewById(R.id.Otros);
                                                        final Button btnCance = (Button) mCambMot.findViewById(R.id.Cancelar);
                                                        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                                                                .setCustomTitle(titulo)
                                                                .setView(mCambMot);
                                                        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                                                        btnVaca.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                asignariDiaLibreAd(empleSelec, datefull + "V");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnBaja.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                asignariDiaLibreAd(empleSelec, datefull + "B");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnOtros.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                dialogoAdministrarDiasLibres.dismiss();
                                                                mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                                                final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                                                                final EditText sJustificar = mJustificar.findViewById(R.id.justificaDialogo);
                                                                final Button btnJust = (Button) mJustificar.findViewById(R.id.btn1);
                                                                Login
                                                                        .setTitle("Justifique el motivo")
                                                                        .setView(mJustificar);
                                                                final AlertDialog dialogoLogin = Login.create();
                                                                btnJust.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        final String JustTexto = sJustificar.getText().toString();

                                                                        if (!JustTexto.isEmpty()) {

                                                                            otroMot = JustTexto;
                                                                            sJustificar.setError(null);
                                                                            sJustificar.setHintTextColor(Color.GRAY);
                                                                            asignariDiaLibreAd(empleSelec, datefull + "O");
                                                                            dialogoLogin.dismiss();


                                                                        } else if (JustTexto.isEmpty()) {
                                                                            sJustificar.setError("No puede dejar este texto vacio");
                                                                            sJustificar.setHintTextColor(Color.RED);

                                                                        }
                                                                    }
                                                                });
                                                                dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                    @Override
                                                                    public void onShow(final DialogInterface dialog) {
                                                                        sJustificar.setEnabled(true);
                                                                        btnJust.setEnabled(true);
                                                                    }
                                                                });
                                                                dialogoLogin.setCanceledOnTouchOutside(false);
                                                                if (mJustificar.getParent() != null) {
                                                                    ((ViewGroup) mJustificar.getParent()).removeView(mJustificar);
                                                                    mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                                                    dialogoLogin.show();

                                                                } else {
                                                                    dialogoLogin.show();
                                                                }
                                                            }
                                                        });
                                                        btnCance.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialogoAdministrarDiasLibres.dismiss();
                                                            }
                                                        });
                                                        dialogoAdministrarDiasLibres.setOnShowListener(new DialogInterface.OnShowListener() {
                                                            @Override
                                                            public void onShow(DialogInterface dialog) {

                                                                btnVaca.setEnabled(true);
                                                                btnBaja.setEnabled(true);
                                                                btnOtros.setEnabled(true);
                                                                btnCance.setEnabled(true);
                                                            }
                                                        });
                                                        dialogoAdministrarDiasLibres.setCanceledOnTouchOutside(false);
                                                        if (mCambMot.getParent() != null) {
                                                            ((ViewGroup) mCambMot.getParent()).removeView(mCambMot);
                                                            mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                            dialogoAdministrarDiasLibres.show();
                                                        } else {
                                                            dialogoAdministrarDiasLibres.show();
                                                        }
                                                    }
                                                });
                                                dialogoAdministrarDiasLibresAd.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialog) {
                                                        btnSiguiente.setEnabled(true);
                                                        btnCancelar.setEnabled(true);
                                                    }
                                                });
                                                dialogoAdministrarDiasLibresAd.setCanceledOnTouchOutside(false);
                                                if (mEmpleados.getParent() != null) {
                                                    ((ViewGroup) mEmpleados.getParent()).removeView(mEmpleados);
                                                    mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                                    dialogoAdministrarDiasLibresAd.show();
                                                } else {
                                                    dialogoAdministrarDiasLibresAd.show();
                                                }

                                            }
                                        } else {
                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "No tienes ningun empleado registrado", 3000)
                                                    .setActionTextColor(Color.WHITE);
                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(12);
                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                            snackbar.show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void asignariDiaLibreAd(final String nombre2, final String fecha) {
        Menu.cargando(true);
        touch(true);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final String id2 = documentSnapshot.getString("id");
                String tip = documentSnapshot.getString("Dias libres solicitados");
                String fin = null;
                String aceptD = documentSnapshot.getString("Dias libres");
                final Map<String, Object> mapSolis = new HashMap<>();
                if (tip != null) {
                    final List<String> diasSotLista = Arrays.asList(tip.split("\\s*;\\s*"));
                    final List<String> dias2 = new ArrayList<>();
                    for (String ds : diasSotLista) {
                        if (!ds.contains(fecha)) {
                            dias2.add(ds);
                        }
                    }
                    for (String ds2 : dias2) {
                        if (fin == null) {
                            fin = ds2 + ";";
                        } else if (fin != null) {
                            fin = fin + ds2 + ";";
                        }
                    }
                    mapSolis.put("Dias libres solicitados", fin);
                }
                if (aceptD != null) {
                    aceptD = documentSnapshot.getString("Dias libres") + fecha + ";";
                } else {
                    aceptD = fecha + ";";
                }
                mapSolis.put("Dias libres", aceptD);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre2).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre2).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id2).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mapSolis.put("asignado", true);
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre2).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (fecha.contains("O")) {
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre2).update(fecha.replace("O", "").replaceAll("/", "-"), otroMot).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre2).update(fecha.replace("O", "").replaceAll("/", "-"), otroMot).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Dia libre " + fecha.replaceAll("O", "").replaceAll("V", "").replaceAll("B", "") + " asignado.", 3000)
                                                                            .setActionTextColor(Color.WHITE);
                                                                    TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                    tv.setTextSize(12);
                                                                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                                    snackbarDS.configSnackbar(getContext(), snackbar);
                                                                    snackbar.show();
                                                                    if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                                        if (Menu.getCambioDeFragment()) {
                                                                            actualizarCalendarioAd();
                                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                            Menu.setCambioDeFragmento(false);
                                                                        } else if (!Menu.getCambioDeFragment()) {
                                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                        }
                                                                    }
                                                                    Menu.cargando(false);
                                                                    touch(false);
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Dia libre " + fecha.replaceAll("O", "").replaceAll("V", "").replaceAll("B", "") + " asignado.", 3000)
                                                            .setActionTextColor(Color.WHITE);
                                                    TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(12);
                                                    tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                    snackbarDS.configSnackbar(getContext(), snackbar);
                                                    snackbar.show();
                                                    if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                        if (Menu.getCambioDeFragment()) {
                                                            actualizarCalendarioAd();
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                            Menu.setCambioDeFragmento(false);
                                                        } else if (!Menu.getCambioDeFragment()) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                        }
                                                    }
                                                    Menu.cargando(false);
                                                    touch(false);
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

    private void dialogoAceptAdmin(final String nombreEm, final String fechaText) {
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombreEm).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                String motiv2 = documentSnapshot.getString("Dias libres solicitados");
                final List<String> listaMotiv = Arrays.asList(motiv2.split("\\s*;\\s*"));
                for (String mot : listaMotiv) {
                    if (mot.contains(fechaText)) {
                        if (mot.contains("V")) {
                            dialogoAcReDia(nombreEm, "vacaciones.", fechaText);
                        } else if (mot.contains("B")) {
                            dialogoAcReDia(nombreEm, "baja laboral.", fechaText);
                        } else if (mot.contains("O")) {
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombreEm).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                    String camb = fechaText.replaceAll("/", "-");
                                    dialogoAcReDia(nombreEm, documentSnapshot.getString(camb), fechaText);
                                }
                            });
                        }
                        break;
                    }
                }
            }
        });
    }

    private void dialogoAcReDia(final String nom, String mot, final String fech) {

        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nom + " quiere el dia " + fech + " libre por " + mot);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnAceptar = (Button) mTresBtn.findViewById(R.id.btn1);
        btnAceptar.setText("Aceptar");
        final Button btnRechazar = (Button) mTresBtn.findViewById(R.id.btn2);
        btnRechazar.setText("Rechazar");
        final Button btnCance = (Button) mTresBtn.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mTresBtn);
        final AlertDialog dialogoAdministrarDiasLibresAd2 = AdministrarDiasLibresAd2.create();
        final String finalMotiv = mot;
        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);

            }
        });

        btnRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        final List<String> diasSotLista = Arrays.asList(tip.split("\\s*;\\s*"));
                        final List<String> dias2 = new ArrayList<>();
                        for (String ds : diasSotLista) {
                            if (!ds.contains(fech)) {
                                dias2.add(ds);
                            }
                        }
                        for (String ds2 : dias2) {
                            if (fin == null) {
                                fin = ds2 + ";";
                            } else if (fin != null) {
                                fin = fin + ds2 + ";";
                            }
                        }
                        final Map<String, Object> mapD = new HashMap<>();
                        mapD.put("Dias libres solicitados", fin);
                        firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id"))
                                .set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mapD.put("rechazado", true);
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nom).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "El dia " + fech + " ha sido rechazado", 8000)
                                                                .setActionTextColor(Color.WHITE);
                                                        TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(12);
                                                        snackbarDS.configSnackbar(getContext(), snackbar);
                                                        snackbar.show();
                                                        Menu.cargando(false);
                                                        touch(false);
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
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        String aceptD = documentSnapshot.getString("Dias libres");
                        String acept = null;
                        final List<String> diasSotLista = Arrays.asList(tip.split("\\s*;\\s*"));
                        final List<String> dias2 = new ArrayList<>();
                        for (String ds : diasSotLista) {
                            if (!ds.contains(fech)) {
                                dias2.add(ds);
                            } else {
                                acept = ds;
                            }
                        }
                        for (String ds2 : dias2) {
                            if (fin == null) {
                                fin = ds2 + ";";
                            } else if (fin != null) {
                                fin = fin + ds2 + ";";
                            }
                        }
                        final Map<String, Object> mapSolis = new HashMap<>();
                        mapSolis.put("Dias libres solicitados", fin);
                        if (acept != null) {
                            if (aceptD == null) {
                                acept = acept + ";";
                            } else if (aceptD != null) {
                                acept = aceptD + acept + ";";
                            }
                            mapSolis.put("Dias libres", acept);
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom)
                                    .set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id")).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mapSolis.put("aceptado", true);
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nom)
                                                            .set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "El dia " + fech + " ha sido aceptado", 8000)
                                                                    .setActionTextColor(Color.WHITE);
                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(12);
                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                            snackbar.show();
                                                            if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                                if (Menu.getCambioDeFragment()) {
                                                                    actualizarCalendarioAd();
                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                    Menu.setCambioDeFragmento(false);
                                                                } else if (!Menu.getCambioDeFragment()) {
                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                }
                                                            }
                                                            Menu.cargando(false);
                                                            touch(false);
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
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);
            }
        });
        dialogoAdministrarDiasLibresAd2.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnAceptar.setEnabled(true);
                btnRechazar.setEnabled(true);
                btnCance.setEnabled(true);
            }
        });
        dialogoAdministrarDiasLibresAd2.setCanceledOnTouchOutside(false);
        if (mTresBtn.getParent() != null) {
            ((ViewGroup) mTresBtn.getParent()).removeView(mTresBtn);
            mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoAdministrarDiasLibresAd2.show();
        } else {
            dialogoAdministrarDiasLibresAd2.show();
        }
    }

    private void gestDiasAd(final String nombre2, final String date) {

        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                motiv = documentSnapshot.getString("Dias libres");
                final List<String> listaMotiv = Arrays.asList(motiv.split("\\s*;\\s*"));
                for (String mot : listaMotiv) {
                    if (mot.contains(date)) {
                        if (mot.contains("V")) {
                            dialogoYaDia(nombre2, date, "vacaciones.");
                        } else if (mot.contains("B")) {
                            dialogoYaDia(nombre2, date, "baja laboral.");
                        } else if (mot.contains("O")) {
                            dialogoYaDia(nombre2, date, documentSnapshot.getString(date.replaceAll("/", "-")));
                        }
                        break;
                    }
                }
            }
        });

    }

    private void dialogoYaDia(final String nombre3, final String date, final String motivo) {

        mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnEliminar = (Button) mTresBtn.findViewById(R.id.btn1);
        btnEliminar.setText("Eliminar dia libre");
        final Button btnCambMot = (Button) mTresBtn.findViewById(R.id.btn2);
        final Button btnCance = (Button) mTresBtn.findViewById(R.id.Cancelar);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombre3 + " tiene el dia " + date + " libre por " + motivo);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mTresBtn);
        final AlertDialog dialogoAdministrarDiasLibresAd2 = AdministrarDiasLibresAd2.create();

        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);

            }
        });

        btnCambMot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDiasLibresAd2.dismiss();
                final TextView titulo = new TextView(getActivity());
                titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                titulo.setTextColor(Color.BLACK);
                titulo.setText("Seleccione el nuevo motivo para el dia " + date + " libre de " + nombre3);
                mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                final Button btnVaca = (Button) mCambMot.findViewById(R.id.Vacas);
                final Button btnBaja = (Button) mCambMot.findViewById(R.id.Baja);
                final Button btnOtros = (Button) mCambMot.findViewById(R.id.Otros);
                final Button btnCance = (Button) mCambMot.findViewById(R.id.Cancelar);
                AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                        .setCustomTitle(titulo)
                        .setView(mCambMot);
                final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                btnVaca.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministrarDiasLibres.dismiss();
                        motivNu(nombre3, date, "V");

                    }
                });
                btnBaja.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministrarDiasLibres.dismiss();
                        motivNu(nombre3, date, "B");

                    }
                });
                btnOtros.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialogoAdministrarDiasLibres.dismiss();
                        mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                        final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                        final EditText sJustificar = mJustificar.findViewById(R.id.justificaDialogo);
                        final Button btnJust = (Button) mJustificar.findViewById(R.id.btn1);
                        Login
                                .setTitle("Justifique el motivo")
                                .setView(mJustificar);
                        final AlertDialog dialogoLogin = Login.create();
                        btnJust.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String JustTexto = sJustificar.getText().toString();

                                if (!JustTexto.isEmpty()) {

                                    otroMot = JustTexto;
                                    sJustificar.setError(null);
                                    sJustificar.setHintTextColor(Color.GRAY);
                                    motivNu(nombre3, date, "O");
                                    dialogoLogin.dismiss();


                                } else if (JustTexto.isEmpty()) {
                                    sJustificar.setError("No puede dejar este texto vacio");
                                    sJustificar.setHintTextColor(Color.RED);

                                }
                            }
                        });
                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialog) {
                                sJustificar.setEnabled(true);
                                btnJust.setEnabled(true);
                            }
                        });
                        dialogoLogin.setCanceledOnTouchOutside(false);
                        if (mJustificar.getParent() != null) {
                            ((ViewGroup) mJustificar.getParent()).removeView(mJustificar);
                            mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                            dialogoLogin.show();

                        } else {
                            dialogoLogin.show();
                        }
                    }
                });
                btnCance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministrarDiasLibres.dismiss();
                    }
                });
                dialogoAdministrarDiasLibres.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {

                        btnVaca.setEnabled(true);
                        btnBaja.setEnabled(true);
                        btnOtros.setEnabled(true);
                        btnCance.setEnabled(true);
                    }
                });
                dialogoAdministrarDiasLibres.setCanceledOnTouchOutside(false);
                if (mCambMot.getParent() != null) {
                    ((ViewGroup) mCambMot.getParent()).removeView(mCambMot);
                    mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                    dialogoAdministrarDiasLibres.show();
                } else {
                    dialogoAdministrarDiasLibres.show();
                }
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                dialogoAdministrarDiasLibresAd2.dismiss();
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre3).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final String diasLibresAcept = documentSnapshot.getString("Dias libres");
                        String insertar = null;
                        insertar = diasLibresAcept.replace(date + "V;", "").replace(date + "B;", "").replace(date + "O;", "");
                        if (insertar.equals("")) {
                            insertar = null;
                        }
                        final String finalInsertar = insertar;
                        firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id")).update("Dias libres", insertar).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre3).update("Dias libres", finalInsertar).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre3).update("Dias libres", finalInsertar).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                final Map<String, Object> mapins = new HashMap<>();
                                                mapins.put("Dias libres", finalInsertar);
                                                mapins.put("eliminado", true);
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).set(mapins, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (!motivo.equals("V") || !motivo.equals("B")) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre3).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                                                if (Menu.getCambioDeFragment()) {
                                                                                    actualizarCalendarioAd();
                                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                                    Menu.setCambioDeFragmento(false);
                                                                                } else if (!Menu.getCambioDeFragment()) {
                                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                                }
                                                                            }
                                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Dia libre eliminado correctamente", 8000)
                                                                                    .setActionTextColor(Color.WHITE);
                                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                            tv.setTextSize(12);
                                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                                            snackbar.show();
                                                                            Menu.cargando(false);
                                                                            touch(false);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {

                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Dia libre eliminado correctamente", 8000)
                                                                    .setActionTextColor(Color.WHITE);
                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(12);
                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                            snackbar.show();
                                                            Menu.cargando(false);
                                                            touch(false);
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
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);
            }
        });
        dialogoAdministrarDiasLibresAd2.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCambMot.setEnabled(true);
                btnEliminar.setEnabled(true);
                btnCance.setEnabled(true);
            }
        });
        dialogoAdministrarDiasLibresAd2.setCanceledOnTouchOutside(false);
        if (mTresBtn.getParent() != null) {
            ((ViewGroup) mTresBtn.getParent()).removeView(mTresBtn);
            mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null, false);
            dialogoAdministrarDiasLibresAd2.show();

        } else {
            dialogoAdministrarDiasLibresAd2.show();
        }
    }

    private void motivNu(final String nombre4, final String fecha, final String motivo) {
        Menu.cargando(true);
        touch(true);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre4).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final List<String> listaMotiv = Arrays.asList(documentSnapshot.getString("Dias libres").split("\\s*;\\s*"));
                for (final String m : listaMotiv) {
                    if (m.contains(fecha)) {
                        firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id")).update("Dias libres", documentSnapshot.getString("Dias libres").replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre4).update("Dias libres", documentSnapshot.getString("Dias libres").replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update("Dias libres", documentSnapshot.getString("Dias libres").replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre4).update("Dias libres", documentSnapshot.getString("Dias libres").replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (motivo.equals("O")) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update(fecha.replaceAll("/", "-"), otroMot).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(fecha.replaceAll("/", "-"), otroMot).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Motivo del dia libre " + fecha + " actualizado", 8000)
                                                                                    .setActionTextColor(Color.WHITE);
                                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                            tv.setTextSize(10);
                                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                                            snackbar.show();
                                                                            Menu.cargando(false);
                                                                            touch(false);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {
                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Motivo del dia libre " + fecha + " actualizado", 8000)
                                                                    .setActionTextColor(Color.WHITE);
                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(10);
                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                            snackbar.show();
                                                            Menu.cargando(false);
                                                            touch(false);
                                                        }
                                                        if (!motivo.equals("O") && m.contains("O")) {
                                                            Menu.cargando(true);
                                                            touch(true);
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update(fecha.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre4).update(fecha.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Motivo del dia libre " + fecha + " actualizado", 8000)
                                                                                    .setActionTextColor(Color.WHITE);
                                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                            tv.setTextSize(10);
                                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                                            snackbar.show();
                                                                            Menu.cargando(false);
                                                                            touch(false);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {
                                                            Menu.cargando(true);
                                                            touch(true);
                                                            Snackbar snackbar = Snackbar.make(root.findViewById(R.id.viewSnack), "Motivo del dia libre " + fecha + " actualizado", 8000)
                                                                    .setActionTextColor(Color.WHITE);
                                                            TextView tv = (TextView) (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(10);
                                                            snackbarDS.configSnackbar(getContext(), snackbar);
                                                            snackbar.show();
                                                            Menu.cargando(false);
                                                            touch(false);
                                                        }
                                                        if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                            if (Menu.getCambioDeFragment()) {
                                                                actualizarCalendarioAd();
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                Menu.setCambioDeFragmento(false);
                                                            } else if (!Menu.getCambioDeFragment()) {
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
            }
        });

    }

    private void actualizarCalendarioAd() {
        Log.d("actualizarCalendarioAd", "entra");
        Menu.cargando(true);
        touch(true);
        final Date hoy = new Date();
        final Calendar siguienteAño = Calendar.getInstance();
        siguienteAño.add(Calendar.YEAR, 1);
        calendarPickerView.init(hoy, siguienteAño.getTime());
        if (collectionDates != null) {
            collectionDates.clear();
        }
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                try {
                    calendarPickerView.clearSelectedDates();
                    String dateS = new SimpleDateFormat("dd/MM/yyyy").format(date);
                    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(dateS);
                    Date currentTime = Calendar.getInstance().getTime();
                    if (date1.before(currentTime)) {
                        Toast.makeText(getActivity(), "Este dia ya ha pasado, seleccione un dia futuro", Toast.LENGTH_SHORT).show();
                    }else{
                        dialogoLibresAd(date);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
        calendarPickerView.clearHighlightedDates();
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (final QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.getString("Dias libres solicitados") != null) {
                        collectionDates.clear();
                        calendarPickerView.clearHighlightedDates();
                        final List<String> diasSoliLista = Arrays.asList(doc.getString("Dias libres solicitados").replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*"));
                        for (String ds : diasSoliLista) {
                            if (ds != null) {
                                try {
                                    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(ds);
                                    Date currentTime = Calendar.getInstance().getTime();
                                    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    String hoyFech = fmt.format(new Date());
                                    final String dateS = fmt.format(date1);
                                    if (date1.before(currentTime)) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(doc.getString("nombre")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                                                    String tip = documentSnapshot.getString("Dias libres solicitados");
                                                    String fin = null;
                                                    final Map<String, String> mapD = new HashMap<>();
                                                    final List<String> diasSotLista = Arrays.asList(tip.split("\\s*;\\s*"));
                                                    final List<String> dias2 = new ArrayList<>();
                                                    for (String ds : diasSotLista) {
                                                        if (!ds.contains(dateS)) {
                                                            dias2.add(ds);
                                                        }
                                                    }
                                                    for (String ds2 : dias2) {
                                                        if (fin == null) {
                                                            fin = ds2 + ";";
                                                        } else if (fin != null) {
                                                            fin = fin + ds2 + ";";
                                                        }
                                                    }

                                                    mapD.put("Dias libres solicitados", fin);
                                                    firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id"))
                                                            .set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(doc.getString("nombre")).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    if (documentSnapshot.getString("nombre") == null) {
                                                                        mapD.put("nombre", nombre);
                                                                    }
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(doc.getString("nombre")).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(doc.getString("nombre")).set(mapD, SetOptions.merge());
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    } else {
                                        collectionDates.add(date1);
                                        if (collectionDates != null) {
                                            calendarPickerView.highlightDates(collectionDates);
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    final List<Date> contieneDias = new ArrayList<Date>();
                    final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                    firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                        @Override
                        public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                            List<Task<QuerySnapshot>> tasks = new ArrayList<Task<QuerySnapshot>>();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.getString("Dias libres") != null) {
                                    try {
                                        String diasnomot = doc.getString("Dias libres").replaceAll("B", "").replaceAll("O", "").replaceAll("V", "");
                                        final List<String> contieneDiasString = Arrays.asList(diasnomot.split("\\s*;\\s*"));
                                        for (String di : contieneDiasString) {
                                            contieneDias.add(formato.parse(di));

                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            return Tasks.whenAllSuccess(tasks);
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                            if (task.isSuccessful()) {
                                if (!contieneDias.isEmpty()) {
                                    Date max = Collections.max(contieneDias);
                                    Date min = Collections.min(contieneDias);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(max);
                                    cal.add(Calendar.MONTH, 2);
                                    max = cal.getTime();
                                    calendarPickerView2.init(min, max);
                                    calendarPickerView2.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                                        @Override
                                        public void onDateSelected(Date date) {
                                            try {
                                                calendarPickerView.clearSelectedDates();
                                                String dateS = new SimpleDateFormat("dd/MM/yyyy").format(date);
                                                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(dateS);
                                                Date currentTime = Calendar.getInstance().getTime();
                                                if (date1.before(currentTime)) {
                                                    Toast.makeText(getActivity(), "Este dia ya ha pasado, seleccione un dia futuro", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    dialogoLibresAd(date);
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onDateUnselected(Date date) {

                                        }
                                    });
                                    diasSolicTextV2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                                if (Menu.getCambioDeFragment()) {
                                                    actualizarCalendarioAd();
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                    Menu.setCambioDeFragmento(false);
                                                } else if (!Menu.getCambioDeFragment()) {
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                }
                                            }
                                            if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                if (Menu.getCambioDeFragment()) {
                                                    actualizarCalendarioAd();
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                    Menu.setCambioDeFragmento(false);
                                                } else if (!Menu.getCambioDeFragment()) {
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                }
                                            }
                                        }
                                    });

                                    if (calendarPickerView2 != null) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                collectionDates2.clear();
                                                calendarPickerView2.clearHighlightedDates();
                                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                                    if (doc.getString("Dias libres") != null) {
                                                        final List<String> diasAceptLista = Arrays.asList(doc.getString("Dias libres").replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*"));
                                                        List<Date> listaDates = new ArrayList<Date>();
                                                        for (String ds : diasAceptLista) {
                                                            try {
                                                                listaDates.add(new SimpleDateFormat("dd/MM/yyyy").parse(ds));
                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        for (Date da : listaDates) {
                                                            collectionDates2.add(da);
                                                            if (collectionDates2 != null) {
                                                                calendarPickerView2.highlightDates(collectionDates2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        hayDias = true;
                                    }
                                } else {
                                    diasSolicTextV2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (Menu.getCambioDeFragment()) {
                                                actualizarCalendarioAd();
                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                Menu.setCambioDeFragmento(false);
                                            } else if (!Menu.getCambioDeFragment()) {
                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                            }
                                        }
                                    });
                                    calendarPickerView2.init(hoy, siguienteAño.getTime());
                                    hayDias = false;
                                    diasSolicTextV2.setText("No hay dias libres asignados por ahora");
                                }
                                Menu.cargando(false);
                                touch(false);
                            } else {
                                Menu.cargando(false);
                                touch(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void touch(Boolean touch) {
        if (touch) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else if (!touch) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    @Override
    public Object getReturnTransition() {
        return super.getReturnTransition();
    }

}
