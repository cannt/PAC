package com.japac.pac.menu.administradores;


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

import android.os.CountDownTimer;
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
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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
import com.japac.pac.menu.menu;
import com.japac.pac.R;
import com.japac.pac.servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class gestionarDiasAdministradores extends Fragment {

    private FirebaseFirestore firebaseFirestore;

    private String empresa;
    private String nombre;
    private String empleSelec;
    private String otroMot;
    private String motiv;

    private CalendarPickerView calendarPickerView, calendarPickerView2;

    private final ArrayList<Date> collectionDates = new ArrayList<>(31);
    private final ArrayList<Date> collectionDates2 = new ArrayList<>(31);

    private TextView diasSolicTextV2;

    private View mEmpleados, mCambMot, mTresBtn, mDosBtn, mJustificar;

    private Spinner empleadosSpinner;

    private ArrayAdapter<String> empleadoAdapter;

    private SlidingUpPanelLayout slidingLayout;

    private Boolean hayDias = true;

    private ImageView xpand;

    public gestionarDiasAdministradores() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        menu.cargando(true);
        touch(true);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_gestionar_dias, null, false);
        final Date hoy = new Date();
        final Calendar siguienteAno = Calendar.getInstance();
        siguienteAno.add(Calendar.YEAR, 1);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        xpand = root.findViewById(R.id.btnXpand);
        diasSolicTextV2 = root.findViewById(R.id.DiasSoliList2);
        TextView diasSolicTextV = root.findViewById(R.id.DiasSoliList);
        diasSolicTextV.setText("Dias libres pendientes de aprobación");
        diasSolicTextV2.setText("Dias libres asignados\nDezlizar para mas detalles");
        calendarPickerView = root.findViewById(R.id.calendar_view);
        calendarPickerView2 = root.findViewById(R.id.calendar_view2);
        calendarPickerView.init(hoy, siguienteAno.getTime());
        calendarPickerView2.init(hoy, siguienteAno.getTime());
        slidingLayout = root.findViewById(R.id.sliding_layout);
        slidingLayout.setDragView(diasSolicTextV2);
        diasSolicTextV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        if(hayDias){
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                        menu.setCambioDeFragmento(false);
                    } else {
                        if(hayDias){
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                    }
                }
                if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    if (menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        menu.setCambioDeFragmento(false);
                    } else {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }

            }
        });
        diasSolicTextV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    if (menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        menu.setCambioDeFragmento(false);
                    } else {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }

                }
            }
        });
        xpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    if (menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        menu.setCambioDeFragmento(false);
                    } else {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    xpand.setImageResource(R.drawable.ic_expand_up);
                } else if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    if (menu.getCambioDeFragment()) {
                        actualizarCalendarioAd();
                        if(hayDias){
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                        menu.setCambioDeFragmento(false);
                    } else {
                        if(hayDias){
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
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
                    new CountDownTimer(2000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            diasSolicTextV2.setTextColor(Color.RED);
                        }

                        @Override
                        public void onFinish() {
                            diasSolicTextV2.setTextColor(Color.WHITE);
                        }
                    }.start();
                    xpand.setVisibility(View.INVISIBLE);
                    xpand.setClickable(false);
                    diasSolicTextV2.setText("No hay dias libres asignados por ahora");
                    if(!slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)){
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                } else {
                    if (xpand.getVisibility() == View.INVISIBLE) {
                        xpand.setVisibility(View.VISIBLE);
                        xpand.setClickable(true);
                        diasSolicTextV2.setText("Dias libres asignados");
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
                        if (menu.getCambioDeFragment()) {
                            actualizarCalendarioAd();
                            if (hayDias) {
                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            }
                            menu.setCambioDeFragmento(false);
                        } else {
                            if (hayDias) {
                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                            }
                        }
                    }
                }
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        String id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        NotificationManager notificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).cancelAll();
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
                        for (final DocumentChange doc : Objects.requireNonNull(queryDocumentSnapshots).getDocumentChanges()) {
                            if (doc.getDocument().exists()) {
                                switch (doc.getType()) {
                                    case ADDED:
                                    case MODIFIED:
                                        actualizarCalendarioAd();
                                        if (doc.getDocument().getBoolean("solicita") != null && doc.getDocument().getBoolean("solicita")) {
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(Objects.requireNonNull(doc.getDocument().getString("nombre"))).update("solicita", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.ANCHORED)) {
                                                        if (menu.getCambioDeFragment()) {
                                                            actualizarCalendarioAd();
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                            menu.setCambioDeFragmento(false);
                                                        } else {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                        }
                                                    }
                                                    menu.snackbar.setText("El empleado " + doc.getDocument().getString("nombre") + " a solicitado un dia libre");
                                                    TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(9);
                                                    snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                    menu.snackbar.show();
                                                }
                                            });
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
        menu.cargando(false);
        touch(false);
        return root;
    }

    private void dialogoLibresAd(final Date date) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String datefull = formato.format(date);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final ArrayList<String> contieneDias = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
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
                        } else {
                            contieneDias.size();
                            mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                            empleadosSpinner = mEmpleados.findViewById(R.id.spinnerObra);
                            empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    empleSelec = parent.getItemAtPosition(position).toString();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            empleadoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, contieneDias);
                            empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            empleadosSpinner.setAdapter(empleadoAdapter);
                            final TextView myMsgtitle = new TextView(getActivity());
                            myMsgtitle.setText("Dia " + datefull + " libre " + "actualmente asignado a los siguientes empleados" + contieneDias.get(0) + "\nSeleccione uno de la lista para gestionarlo");
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
                            final Button btnSiguient = mEmpleados.findViewById(R.id.btn1);
                            final Button btnCance = mEmpleados.findViewById(R.id.btn2);
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
                            } else {
                                contieneDias2.size();
                                mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                empleadosSpinner = mEmpleados.findViewById(R.id.spinnerObra);
                                empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        empleSelec = parent.getItemAtPosition(position).toString();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                                empleadoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, contieneDias2);
                                empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                empleadosSpinner.setAdapter(empleadoAdapter);
                                final TextView myMsgtitle = new TextView(getActivity());
                                myMsgtitle.setText("Empleados que quieren el dia " + datefull + " libre\nSeleccione uno de la lista para consultar sus motivos");
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
                                final Button btnSiguient = mEmpleados.findViewById(R.id.btn1);
                                final Button btnCance = mEmpleados.findViewById(R.id.btn2);
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
                                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                            contieneDias3.add(document.getString("nombre"));
                                        }
                                        if (!contieneDias3.isEmpty()) {
                                            if (contieneDias3.size() == 1) {
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("¿Desea darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final Button btnAsigDia = mDosBtn.findViewById(R.id.btn1);
                                                final Button btnCancelar = mDosBtn.findViewById(R.id.btn2);
                                                AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
                                                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                        final String datefull = formato.format(date);
                                                        titulo.setText("¿Porque motivo deseas darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                        final Button btnVaca = mCambMot.findViewById(R.id.Vacas);
                                                        final Button btnBaja = mCambMot.findViewById(R.id.Baja);
                                                        final Button btnOtros = mCambMot.findViewById(R.id.Otros);
                                                        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
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
                                                                final Button btnJusti = mJustificar.findViewById(R.id.btn1);
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


                                                                        } else {

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

                                            } else {
                                                contieneDias3.size();
                                                mEmpleados = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                                                empleadosSpinner = mEmpleados.findViewById(R.id.spinnerObra);
                                                empleadosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        empleSelec = parent.getItemAtPosition(position).toString();
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                });
                                                empleadoAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, contieneDias3);
                                                empleadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                empleadosSpinner.setAdapter(empleadoAdapter);
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("¿Desea asignar el dia " + datefull + " libre? \nSeleccione un empleado de la lista primero");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
                                                final Button btnSiguiente = mEmpleados.findViewById(R.id.btn1);
                                                final Button btnCancelar = mEmpleados.findViewById(R.id.btn2);
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
                                                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                        final String datefull = formato.format(date);
                                                        titulo.setText("¿Porque motivo deseas darle el dia " + datefull + " libre a " + empleSelec + "?");
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                                                        final Button btnVaca = mCambMot.findViewById(R.id.Vacas);
                                                        final Button btnBaja = mCambMot.findViewById(R.id.Baja);
                                                        final Button btnOtros = mCambMot.findViewById(R.id.Otros);
                                                        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
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
                                                                final Button btnJust = mJustificar.findViewById(R.id.btn1);
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


                                                                        } else {
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
                                            menu.snackbar.setText("No tienes ningun empleado registrado");
                                            TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(12);
                                            snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                            menu.snackbar.show();
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
        menu.cargando(true);
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
                    final String[] diasSotLista = tip.split("\\s*;\\s*");
                    final List<String> dias2 = new ArrayList<>();
                    for (String ds : diasSotLista) {
                        if (!ds.contains(fecha)) {
                            dias2.add(ds);
                        }
                    }
                    for (String ds2 : dias2) {
                        if (fin == null) {
                            fin = ds2 + ";";
                        } else {
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
                                firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(id2)).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre2).update(fecha.replace("O", "").replaceAll("/", "-"), otroMot);
                                                        }
                                                    });
                                                }
                                                if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                    if (menu.getCambioDeFragment()) {
                                                        actualizarCalendarioAd();
                                                        if(hayDias){
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                        }
                                                        menu.setCambioDeFragmento(false);
                                                    } else {
                                                        if(hayDias){
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                        }
                                                    }
                                                }
                                                menu.snackbar.setText("Dia libre " + fecha.replaceAll("O", "").replaceAll("V", "").replaceAll("B", "") + " asignado.");
                                                TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                tv.setTextSize(12);
                                                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                menu.snackbar.show();
                                                menu.cargando(false);
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
    }

    private void dialogoAceptAdmin(final String nombreEm, final String fechaText) {
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombreEm).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                String motiv2 = documentSnapshot.getString("Dias libres solicitados");
                final String[] listaMotiv = Objects.requireNonNull(motiv2).split("\\s*;\\s*");
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
myMsgtitle.setPadding(2,2,2,2);
        mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnAceptar = mTresBtn.findViewById(R.id.btn1);
        btnAceptar.setText("Aceptar");
        final Button btnRechazar = mTresBtn.findViewById(R.id.btn2);
        btnRechazar.setText("Rechazar");
        final Button btnCance = mTresBtn.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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

        btnRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        final String[] diasSotLista = Objects.requireNonNull(tip).split("\\s*;\\s*");
                        final List<String> dias2 = new ArrayList<>();
                        for (String ds : diasSotLista) {
                            if (!ds.contains(fech)) {
                                dias2.add(ds);
                            }
                        }
                        for (String ds2 : dias2) {
                            if (fin == null) {
                                fin = ds2 + ";";
                            } else {
                                fin = fin + ds2 + ";";
                            }
                        }
                        final Map<String, Object> mapD = new HashMap<>();
                        mapD.put("Dias libres solicitados", fin);
                        firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(documentSnapshot.getString("id")))
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
                                                        menu.snackbar.setText("El dia " + fech + " ha sido rechazado");
                                                        TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(12);
                                                        snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                        menu.snackbar.show();
                                                        menu.cargando(false);
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
                menu.cargando(true);
                touch(true);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        String aceptD = documentSnapshot.getString("Dias libres");
                        String acept = null;
                        final String[] diasSotLista = Objects.requireNonNull(tip).split("\\s*;\\s*");
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
                            } else {
                                fin = fin + ds2 + ";";
                            }
                        }
                        final Map<String, Object> mapSolis = new HashMap<>();
                        mapSolis.put("Dias libres solicitados", fin);
                        if (acept != null) {
                            if (aceptD == null) {
                                acept = acept + ";";
                            } else {
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
                                            firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(documentSnapshot.getString("id"))).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mapSolis.put("aceptado", true);
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nom)
                                                            .set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                                if (menu.getCambioDeFragment()) {
                                                                    actualizarCalendarioAd();
                                                                    if(hayDias){
                                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                    }
                                                                    menu.setCambioDeFragmento(false);
                                                                } else {
                                                                    if(hayDias){
                                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                    }
                                                                }
                                                            }
                                                            menu.snackbar.setText("El dia " + fech + " ha sido aceptado");
                                                            TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(12);
                                                            snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                            menu.snackbar.show();

                                                            menu.cargando(false);
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
                final String[] listaMotiv = Objects.requireNonNull(motiv).split("\\s*;\\s*");
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
        final Button btnEliminar = mTresBtn.findViewById(R.id.btn1);
        btnEliminar.setText("Eliminar dia libre");
        final Button btnCambMot = mTresBtn.findViewById(R.id.btn2);
        final Button btnCance = mTresBtn.findViewById(R.id.Cancelar);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombre3 + " tiene el dia " + date + " libre por " + motivo);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
myMsgtitle.setPadding(2,2,2,2);
        AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
                final Button btnVaca = mCambMot.findViewById(R.id.Vacas);
                final Button btnBaja = mCambMot.findViewById(R.id.Baja);
                final Button btnOtros = mCambMot.findViewById(R.id.Otros);
                final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
                        final Button btnJust = mJustificar.findViewById(R.id.btn1);
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


                                } else {
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
                menu.cargando(true);
                touch(true);
                dialogoAdministrarDiasLibresAd2.dismiss();
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre3).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final String diasLibresAcept = documentSnapshot.getString("Dias libres");
                        String insertar = null;
                        insertar = Objects.requireNonNull(diasLibresAcept).replace(date + "V;", "").replace(date + "B;", "").replace(date + "O;", "");
                        if (insertar.equals("")) {
                            insertar = null;
                        }
                        final String finalInsertar = insertar;
                        firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(documentSnapshot.getString("id"))).update("Dias libres", insertar).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre3).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                                            if (menu.getCambioDeFragment()) {
                                                                                actualizarCalendarioAd();
                                                                                if(hayDias){
                                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                                }
                                                                                menu.setCambioDeFragmento(false);
                                                                            } else {
                                                                                if(hayDias){
                                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                                }
                                                                            }
                                                                        }
                                                                        menu.snackbar.setText("Dia libre eliminado correctamente");
                                                                        TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                        tv.setTextSize(12);
                                                                        snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                                        menu.snackbar.show();
                                                                        menu.cargando(false);
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
        menu.cargando(true);
        touch(true);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre4).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final String[] listaMotiv = Objects.requireNonNull(documentSnapshot.getString("Dias libres")).split("\\s*;\\s*");
                for (final String m : listaMotiv) {
                    if (m.contains(fecha)) {
                        firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(documentSnapshot.getString("id"))).update("Dias libres", Objects.requireNonNull(documentSnapshot.getString("Dias libres")).replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre4).update("Dias libres", Objects.requireNonNull(documentSnapshot.getString("Dias libres")).replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update("Dias libres", Objects.requireNonNull(documentSnapshot.getString("Dias libres")).replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre4).update("Dias libres", Objects.requireNonNull(documentSnapshot.getString("Dias libres")).replace(m, fecha + motivo)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        if (motivo.equals("O")) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update(fecha.replaceAll("/", "-"), otroMot).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(fecha.replaceAll("/", "-"), otroMot);
                                                                }
                                                            });
                                                        }
                                                        if (!motivo.equals("O") && m.contains("O")) {
                                                            menu.cargando(true);
                                                            touch(true);
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).update(fecha.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre4).update(fecha.replaceAll("/", "-"), FieldValue.delete());
                                                                }
                                                            });
                                                        }
                                                        if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                                            if (menu.getCambioDeFragment()) {
                                                                actualizarCalendarioAd();
                                                                if(hayDias){
                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                }
                                                                menu.setCambioDeFragmento(false);
                                                            } else {
                                                                if(hayDias){
                                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                }
                                                            }
                                                        }
                                                        menu.snackbar.setText("Motivo del dia libre " + fecha + " actualizado");
                                                        TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(10);
                                                        snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                                                        menu.snackbar.show();
                                                        menu.cargando(false);
                                                        touch(false);
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
        menu.cargando(true);
        touch(true);
        final Date hoy = new Date();
        final Calendar siguienteAno = Calendar.getInstance();
        siguienteAno.add(Calendar.YEAR, 1);
        calendarPickerView.init(hoy, siguienteAno.getTime());
        collectionDates.clear();
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                try {
                    calendarPickerView.clearSelectedDates();
                    String dateS = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                    Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateS);
                    Date currentTime = Calendar.getInstance().getTime();
                    if (Objects.requireNonNull(date1).before(currentTime)) {
                        menu.snackbar.setText("Este dia ya ha pasado, seleccione un dia futuro");
                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(10);
                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                        menu.snackbar.show();
                    } else {
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
                        final String[] diasSoliLista = Objects.requireNonNull(doc.getString("Dias libres solicitados")).replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
                        for (String ds : diasSoliLista) {
                            if (ds != null) {
                                try {
                                    Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ds);
                                    Date currentTime = Calendar.getInstance().getTime();
                                    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    final String dateS = fmt.format(Objects.requireNonNull(date1));
                                    if (date1.before(currentTime)) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(Objects.requireNonNull(doc.getString("nombre"))).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                                                    String tip = documentSnapshot.getString("Dias libres solicitados");
                                                    String fin = null;
                                                    final Map<String, String> mapD = new HashMap<>();
                                                    final String[] diasSotLista = Objects.requireNonNull(tip).split("\\s*;\\s*");
                                                    final List<String> dias2 = new ArrayList<>();
                                                    for (String ds : diasSotLista) {
                                                        if (!ds.contains(dateS)) {
                                                            dias2.add(ds);
                                                        }
                                                    }
                                                    for (String ds2 : dias2) {
                                                        if (fin == null) {
                                                            fin = ds2 + ";";
                                                        } else {
                                                            fin = fin + ds2 + ";";
                                                        }
                                                    }

                                                    mapD.put("Dias libres solicitados", fin);
                                                    firebaseFirestore.collection("Todas las ids").document(Objects.requireNonNull(documentSnapshot.getString("id")))
                                                            .set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(Objects.requireNonNull(doc.getString("nombre"))).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    if (documentSnapshot.getString("nombre") == null) {
                                                                        mapD.put("nombre", nombre);
                                                                    }
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(Objects.requireNonNull(doc.getString("nombre"))).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(Objects.requireNonNull(doc.getString("nombre"))).set(mapD, SetOptions.merge());
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
                                        calendarPickerView.highlightDates(collectionDates);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    final List<Date> contieneDias = new ArrayList<>();
                    final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                        @Override
                        public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                            List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                            for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                                if (doc.getString("Dias libres") != null) {
                                    try {
                                        String diasnomot = Objects.requireNonNull(doc.getString("Dias libres")).replaceAll("B", "").replaceAll("O", "").replaceAll("V", "");
                                        final String[] contieneDiasString = diasnomot.split("\\s*;\\s*");
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
                                                String dateS = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                                                Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateS);
                                                Date currentTime = Calendar.getInstance().getTime();
                                                if (Objects.requireNonNull(date1).before(currentTime)) {
                                                    menu.snackbar.setText("Este dia ya ha pasado, seleccione un dia futuro");
                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(10);
                                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                    menu.snackbar.show();
                                                } else {
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
                                                if (menu.getCambioDeFragment()) {
                                                    actualizarCalendarioAd();
                                                    if(hayDias){
                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                    }
                                                    menu.setCambioDeFragmento(false);
                                                } else {
                                                    if(hayDias){
                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                    }
                                                }
                                            }
                                            if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                if (menu.getCambioDeFragment()) {
                                                    actualizarCalendarioAd();
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                    menu.setCambioDeFragmento(false);
                                                } else {
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
                                                        final String[] diasAceptLista = Objects.requireNonNull(doc.getString("Dias libres")).replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
                                                        List<Date> listaDates = new ArrayList<>();
                                                        for (String ds : diasAceptLista) {
                                                            try {
                                                                listaDates.add(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ds));
                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        for (Date da : listaDates) {
                                                            collectionDates2.add(da);
                                                            calendarPickerView2.highlightDates(collectionDates2);
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
                                            if (menu.getCambioDeFragment()) {
                                                actualizarCalendarioAd();
                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                menu.setCambioDeFragmento(false);
                                            } else {
                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                            }
                                        }
                                    });
                                    calendarPickerView2.init(hoy, siguienteAno.getTime());
                                    hayDias = false;
                                    diasSolicTextV2.setText("No hay dias libres asignados por ahora");
                                    if(!slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)){
                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                    }
                                }
                                menu.cargando(false);
                                touch(false);
                            } else {
                                menu.cargando(false);
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
            Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}
