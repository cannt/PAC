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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

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
import com.japac.pac.menu.menu;
import com.japac.pac.R;
import com.japac.pac.servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
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
    private String hEntrada;
    private String hEntrada2;
    private String hSalida;
    private String hSalida2;

    private CalendarPickerView calendarPickerView, calendarPickerView2;

    private final ArrayList<Date> collectionDates = new ArrayList<>(31);
    private final ArrayList<Date> collectionDates2 = new ArrayList<>(31);

    private TextView diasSolicTextV2;

    private View mEmpleados, mCambMot, mTresBtn, mDosBtn, mJustificar, mRango, mHoras, mCuatroBtn;

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
                        if (hayDias) {
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                        menu.setCambioDeFragmento(false);
                    } else {
                        if (hayDias) {
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
                    if (!slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
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
        String id = mAuth.getCurrentUser().getUid();
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                empresa = documentSnapshot.getString("empresa");
                nombre = documentSnapshot.getString("nombre");
                hEntrada = documentSnapshot.getString("hora de entrada");
                hSalida = documentSnapshot.getString("hora de salida");
                hEntrada2 = documentSnapshot.getString("hora de entrada partida");
                hSalida2 = documentSnapshot.getString("hora de salida partida");
                actualizarCalendarioAd();
                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {

                            return;
                        }
                        for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getDocument().exists()) {
                                switch (doc.getType()) {
                                    case ADDED:
                                    case MODIFIED:
                                    case REMOVED:
                                        actualizarCalendarioAd();
                                        if (doc.getDocument().getBoolean("solicita") != null && doc.getDocument().getBoolean("solicita")) {
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(doc.getDocument().getString("nombre")).update("solicita", false).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(9);
                                                    snackbarDS.configSnackbar(getContext(), menu.snackbar);
                                                    menu.snackbar.show();
                                                }
                                            });
                                        } else if (doc.getDocument().getBoolean("asignado") != null && doc.getDocument().getBoolean("asignado")) {
                                            hayDias = true;
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
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String diasS = document.getString("Dias libres");
                        if (diasS != null) {
                            if (diasS.contains(datefull)) {
                                if (!document.getBoolean("desactivado")) {
                                    contieneDias.add(document.getString("nombre"));
                                }
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
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            myMsgtitle.setLayoutParams(params);
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);

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
                                    if (!document.getBoolean("desactivado")) {
                                        contieneDias2.add(document.getString("nombre"));
                                    }
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
                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                myMsgtitle.setLayoutParams(params);
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);

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
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if (!document.getBoolean("desactivado")) {
                                                contieneDias3.add(document.getString("nombre"));
                                            }
                                        }
                                        if (!contieneDias3.isEmpty()) {
                                            if (contieneDias3.size() == 1) {
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("¿Desea darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                myMsgtitle.setLayoutParams(params);
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);

                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final Button btnAsigDia = mDosBtn.findViewById(R.id.btn1);
                                                final Button btnCancelar = mDosBtn.findViewById(R.id.btn2);
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
                                                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                        final String datefull = formato.format(date);
                                                        titulo.setText("¿Porque motivo deseas darle el dia " + datefull + " libre a " + contieneDias3.get(0) + "?");
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
                                                        final Button btnVaca = mCambMot.findViewById(R.id.btn1);
                                                        final Button btnBaja = mCambMot.findViewById(R.id.btn2);
                                                        final Button btnOtros = mCambMot.findViewById(R.id.btn3);
                                                        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                                                        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                                                                .setCustomTitle(titulo)
                                                                .setView(mCambMot);
                                                        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                                                        btnVaca.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                rango(contieneDias3.get(0), datefull + "V");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnBaja.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                rango(contieneDias3.get(0), datefull + "B");
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
                                                                            rango(contieneDias3.get(0), datefull + "O");
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
                                                            mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
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
                                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                myMsgtitle.setLayoutParams(params);
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);

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
                                                        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
                                                        final Button btnVaca = mCambMot.findViewById(R.id.btn1);
                                                        final Button btnBaja = mCambMot.findViewById(R.id.btn2);
                                                        final Button btnOtros = mCambMot.findViewById(R.id.btn3);
                                                        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                                                        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                                                                .setCustomTitle(titulo)
                                                                .setView(mCambMot);
                                                        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                                                        btnVaca.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                rango(empleSelec, datefull + "V");
                                                                dialogoAdministrarDiasLibres.dismiss();

                                                            }
                                                        });
                                                        btnBaja.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                rango(empleSelec, datefull + "B");
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
                                                                            rango(empleSelec, datefull + "O");
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
                                                            mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
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
                                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(12);
                                            snackbarDS.configSnackbar(getContext(), menu.snackbar);
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

    private void rango(final String nombre1, final String fecha1) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Asigne todo el dia libre o seleccione las horas libres");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mRango = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnTodo = mRango.findViewById(R.id.btn2);
        btnTodo.setText("Todo el dia");
        final Button btnHoras = mRango.findViewById(R.id.btn1);
        btnHoras.setText("Seleccionar horas");
        final Button btnCancelar = mRango.findViewById(R.id.Cancelar);
        final AlertDialog.Builder rango = new AlertDialog.Builder(requireContext())
                .setCustomTitle(myMsgtitle)
                .setView(mRango);
        final AlertDialog dialogoRango = rango.create();
        btnTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRango.dismiss();
                asignarDiaLibreAd(nombre1, fecha1);
            }
        });
        btnHoras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRango.dismiss();
                horasSelec(nombre1, fecha1);
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRango.dismiss();
            }
        });
        dialogoRango.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnTodo.setEnabled(true);
                btnHoras.setEnabled(true);
                btnCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoRango.setCanceledOnTouchOutside(false);
        if (mRango.getParent() != null) {
            ((ViewGroup) mRango.getParent()).removeView(mRango);
            mRango = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoRango.show();
        } else {
            dialogoRango.show();
        }
    }

    private void horasSelec(final String nombre2, final String fecha2) {
        menu.cargando(true);
        touch(true);
        final int[] horaEntOSal = {0};
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿A que hora empezara a librar " + nombre2 + " ?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.GREEN);
        mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
        final TimePicker timePicker = mHoras.findViewById(R.id.elegirHora);
        timePicker.setIs24HourView(true);
        if (hEntrada != null) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            Date entrada = null;
            try {
                entrada = format.parse(hEntrada);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            timePicker.setHour(new DateTime(entrada).getHourOfDay());
            timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
        } else {
            timePicker.setHour(00);
            timePicker.setMinute(00);
        }
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                if (hEntrada != null && hSalida != null && hEntrada2 == null && hSalida2 == null) {
                    Date entrada = null;
                    Date salida = null;
                    try {
                        entrada = format.parse(hEntrada);
                        salida = format.parse(hSalida);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (hourOfDay < new DateTime(entrada).getHourOfDay() || hourOfDay > new DateTime(salida).getHourOfDay()) {
                        timePicker.setHour(new DateTime(entrada).getHourOfDay());
                    } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                    } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                    }
                } else if (hEntrada != null && hSalida != null && hEntrada2 != null && hSalida2 != null) {
                    Date entrada = null;
                    Date salida = null;
                    Date entrada2 = null;
                    Date salida2 = null;
                    try {
                        entrada = format.parse(hEntrada);
                        salida2 = format.parse(hSalida2);
                        entrada2 = format.parse(hEntrada2);
                        salida = format.parse(hSalida);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                        timePicker.setHour(new DateTime(entrada).getHourOfDay());
                    } else if ((hourOfDay > new DateTime(salida2).getHourOfDay() && hourOfDay < new DateTime(entrada2).getHourOfDay())) {
                        if (horaEntOSal[0] == 0) {
                            horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                            timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                        } else if (horaEntOSal[0] == new DateTime(entrada2).getHourOfDay()) {
                            horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                            timePicker.setHour(new DateTime(salida2).getHourOfDay());
                        } else if (horaEntOSal[0] == new DateTime(salida2).getHourOfDay()) {
                            horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                            timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                        }
                    } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                        timePicker.setHour(new DateTime(salida).getHourOfDay());
                    } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                    } else if (hourOfDay == new DateTime(salida2).getHourOfDay() && minute > new DateTime(salida2).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(salida2).getMinuteOfHour());
                    } else if (hourOfDay == new DateTime(entrada2).getHourOfDay() && minute < new DateTime(entrada2).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(entrada2).getMinuteOfHour());
                    } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                    }
                }
            }
        });
        final Button btnCont = mHoras.findViewById(R.id.btn1);
        final Button btnCancelar = mHoras.findViewById(R.id.btn2);
        final AlertDialog.Builder rangoHora = new AlertDialog.Builder(getContext());
        rangoHora.setCustomTitle(myMsgtitle)
                .setView(mHoras);
        final AlertDialog dialogoRangoHora = rangoHora.create();
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DecimalFormat decimalFormat = new DecimalFormat("00");
                final String entradaDef = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                if (!entradaDef.isEmpty()) {
                    final int[] horaEntOSal = {0};
                    final TextView myMsgtitle = new TextView(getActivity());
                    myMsgtitle.setText("¿A que hora terminara de librar " + nombre2 + " ?");
                    myMsgtitle.setTextColor(Color.RED);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    myMsgtitle.setLayoutParams(params);
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                    final TimePicker timePicker = mHoras.findViewById(R.id.elegirHora);
                    timePicker.setIs24HourView(true);
                    if (hSalida != null) {
                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                        Date salida = null;
                        try {
                            salida = format.parse(hSalida);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        timePicker.setHour(new DateTime(salida).getHourOfDay());
                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                    } else {
                        timePicker.setHour(00);
                        timePicker.setMinute(00);
                    }
                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                        @Override
                        public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                            if (hEntrada != null && hSalida != null && hEntrada2 == null && hSalida2 == null) {
                                Date entrada = null;
                                Date salida = null;
                                Date entDef = null;
                                try {
                                    entrada = format.parse(hEntrada);
                                    salida = format.parse(hSalida);
                                    entDef = format.parse(entradaDef);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (hourOfDay < new DateTime(entDef).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(entDef).getHourOfDay());
                                } else if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(salida).getHourOfDay());
                                } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(entDef).getHourOfDay() && minute < new DateTime(entDef).getMinuteOfHour()) {
                                    timePicker.setHour(new DateTime(entDef).getMinuteOfHour());
                                }
                            } else if (hEntrada != null && hSalida != null && hEntrada2 != null && hSalida2 != null) {
                                Date entrada = null;
                                Date salida = null;
                                Date entrada2 = null;
                                Date salida2 = null;
                                Date entDef = null;
                                try {
                                    entrada = format.parse(hEntrada);
                                    salida2 = format.parse(hSalida2);
                                    entrada2 = format.parse(hEntrada2);
                                    salida = format.parse(hSalida);
                                    entDef = format.parse(entradaDef);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                } else if ((hourOfDay > new DateTime(salida2).getHourOfDay() && hourOfDay < new DateTime(entrada2).getHourOfDay())) {
                                    if (horaEntOSal[0] == 0) {
                                        horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                                        timePicker.setHour(new DateTime(salida2).getHourOfDay());
                                    } else if (horaEntOSal[0] == new DateTime(salida2).getHourOfDay()) {
                                        horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                                        timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                                    } else if (horaEntOSal[0] == new DateTime(entrada2).getHourOfDay()) {
                                        horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                                        timePicker.setHour(new DateTime(salida2).getHourOfDay());
                                    }
                                } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(salida).getHourOfDay());
                                } else if (hourOfDay < new DateTime(entDef).getHourOfDay()) {
                                    timePicker.setHour(new DateTime(entDef).getHourOfDay());
                                } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(salida2).getHourOfDay() && minute > new DateTime(salida2).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(salida2).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(entrada2).getHourOfDay() && minute < new DateTime(entrada2).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(entrada2).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                    timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                } else if (hourOfDay == new DateTime(entDef).getHourOfDay() && minute < new DateTime(entDef).getMinuteOfHour()) {
                                    timePicker.setHour(new DateTime(entDef).getMinuteOfHour());
                                }
                            }
                        }
                    });
                    final Button btnCon2 = mHoras.findViewById(R.id.btn1);
                    final Button btnCancelar2 = mHoras.findViewById(R.id.btn2);
                    btnCancelar2.setText("Atras");
                    final AlertDialog.Builder rangoHora2 = new AlertDialog.Builder(getContext());
                    rangoHora2.setCustomTitle(myMsgtitle)
                            .setView(mHoras);
                    final AlertDialog dialogoRangoHora2 = rangoHora2.create();
                    btnCon2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String salidaDef = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                            if (Integer.parseInt(entradaDef.substring(0, 2)) >= Integer.parseInt(salidaDef.substring(0, 2))
                                    && Integer.parseInt(entradaDef.substring(3)) >= Integer.parseInt(salidaDef.substring(3))) {
                                final Snackbar snackbar = Snackbar.make(mHoras, "La hora de finalizacion del tiempo libre no puede ser antes o igual a la de inicio", 5000);
                                if (Integer.parseInt(entradaDef.substring(0, 2)) > Integer.parseInt(salidaDef.substring(0, 2))) {
                                    snackbar.setText("La hora de finalizacion del tiempo libre no puede ser antes que la de inicio");
                                } else if (Integer.parseInt(entradaDef.substring(0, 2)) == Integer.parseInt(salidaDef.substring(0, 2))) {
                                    snackbar.setText("La hora de finalizacion del tiempo libre no puede ser igual a la de inicio");
                                }
                                TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                tv.setTextSize(10);
                                snackbarDS.configSnackbar(getActivity(), snackbar);
                                snackbar.setAction("Entendido", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snackbar.dismiss();
                                    }
                                }).setActionTextColor(Color.WHITE);
                                snackbar.show();
                            } else {
                                dialogoRangoHora2.dismiss();
                                final TextView myMsgtitle = new TextView(getActivity());
                                if (fecha2.contains("V")) {
                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre2 + "?\n El dia " + fecha2.replace("V", "") + " por vacaciones");
                                } else if (fecha2.contains("B")) {
                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre2 + "?\n El dia " + fecha2.replace("B", "") + " por baja laboral");
                                } else if (fecha2.contains("O")) {
                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre2 + "?\n El dia " + fecha2.replace("O", "") + " por " + otroMot);
                                }
                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                myMsgtitle.setLayoutParams(params);
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.GRAY);
                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                final TextView textView = mDosBtn.findViewById(R.id.elegirHora);
                                textView.setText("De " + entradaDef + " A " + salidaDef);
                                textView.setTextColor(Color.parseColor("#FF00ff00"));
                                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                                anim.setDuration(200);
                                anim.setStartOffset(20);
                                anim.setRepeatMode(Animation.REVERSE);
                                anim.setRepeatCount(Animation.INFINITE);
                                anim.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        textView.setTextColor(Color.parseColor("#FF00ff00"));
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        textView.setTextColor(Color.parseColor("#8000ff00"));
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {


                                    }
                                });
                                textView.startAnimation(anim);
                                final Button btnConf = mDosBtn.findViewById(R.id.btn1);
                                btnConf.setText("Confirmar");
                                final Button btnCancelar3 = mDosBtn.findViewById(R.id.btn2);
                                final AlertDialog.Builder alerta3 = new AlertDialog.Builder(getContext());
                                alerta3.setCustomTitle(myMsgtitle)
                                        .setView(mDosBtn);
                                final AlertDialog dialogoAlerta3 = alerta3.create();
                                btnConf.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        menu.cargando(true);
                                        touch(true);
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre2).update(fecha2.replaceAll("/", "-"), entradaDef + ";" + salidaDef).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                menu.cargando(false);
                                                touch(false);
                                                dialogoAlerta3.dismiss();
                                                asignarDiaLibreAd(nombre2, fecha2);
                                            }
                                        });
                                    }
                                });
                                btnCancelar3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoAlerta3.dismiss();
                                    }
                                });
                                dialogoAlerta3.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        dialogoRangoHora2.dismiss();
                                        textView.setEnabled(true);
                                        btnConf.setEnabled(true);
                                        btnCancelar3.setEnabled(true);
                                    }
                                });
                                dialogoAlerta3.setCanceledOnTouchOutside(false);
                                if (mDosBtn.getParent() != null) {
                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                    dialogoAlerta3.show();
                                } else {
                                    dialogoAlerta3.show();
                                }
                            }
                        }
                    });
                    btnCancelar2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogoRangoHora2.dismiss();
                            horasSelec(nombre2, fecha2);
                        }
                    });
                    dialogoRangoHora2.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            dialogoRangoHora.dismiss();
                            timePicker.setEnabled(true);
                            btnCon2.setEnabled(true);
                            btnCancelar2.setEnabled(true);
                        }
                    });
                    dialogoRangoHora2.setCanceledOnTouchOutside(false);
                    if (mHoras.getParent() != null) {
                        ((ViewGroup) mHoras.getParent()).removeView(mHoras);
                        mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                        dialogoRangoHora2.show();
                    } else {
                        dialogoRangoHora2.show();
                    }
                    dialogoRangoHora.show();
                }
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRangoHora.dismiss();
                rango(nombre2, fecha2);
            }
        });
        dialogoRangoHora.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                timePicker.setEnabled(true);
                btnCont.setEnabled(true);
                btnCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoRangoHora.setCanceledOnTouchOutside(false);
        if (mHoras.getParent() != null) {
            ((ViewGroup) mHoras.getParent()).removeView(mHoras);
            mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
            dialogoRangoHora.show();
        } else {
            dialogoRangoHora.show();
        }
    }

    private void asignarDiaLibreAd(final String nombre3, final String fecha3) {
        menu.cargando(true);
        touch(true);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre3).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                        if (!ds.contains(fecha3)) {
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
                    aceptD = documentSnapshot.getString("Dias libres") + fecha3 + ";";
                } else {
                    aceptD = fecha3 + ";";
                }
                mapSolis.put("Dias libres", aceptD);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre3).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre3).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                firebaseFirestore.collection("Todas las ids").document(id2).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mapSolis.put("asignado", true);
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                if (fecha3.contains("O")) {
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(fecha3.replace("O", "").replaceAll("/", "-"), otroMot);
                                                }
                                            }
                                        });
                                        menu.snackbar.setText("Dia libre " + fecha3.replaceAll("O", "").replaceAll("V", "").replaceAll("B", "") + " asignado.");
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(12);
                                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        snackbarDS.configSnackbar(getContext(), menu.snackbar);
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

    private void dialogoAceptAdmin(final String nombreEm, final String fechaText) {
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombreEm).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                String motiv2 = documentSnapshot.getString("Dias libres solicitados");
                final String[] listaMotiv = motiv2.split("\\s*;\\s*");
                for (String mot : listaMotiv) {
                    if (mot.contains(fechaText)) {
                        Log.d("fecha", fechaText.replaceAll("/", "-"));

                        if (mot.contains("V")) {
                            final String rangHor = documentSnapshot.getString(fechaText.replaceAll("/", "-") + "V");
                            dialogoAcReDia(nombreEm, "vacaciones", fechaText, rangHor);
                        } else if (mot.contains("B")) {
                            final String rangHor = documentSnapshot.getString(fechaText.replaceAll("/", "-") + "B");
                            dialogoAcReDia(nombreEm, "baja laboral", fechaText, rangHor);
                        } else if (mot.contains("O")) {
                            final String rangHor = documentSnapshot.getString(fechaText.replaceAll("/", "-") + "O");
                            String camb = fechaText.replaceAll("/", "-");
                            dialogoAcReDia(nombreEm, documentSnapshot.getString(camb), fechaText, rangHor);
                        }
                        break;
                    }
                }
            }
        });
    }

    private void dialogoAcReDia(final String nom, String mot, final String fech, final String rangoHour) {
        final TextView myMsgtitle = new TextView(getActivity());
        if (rangoHour != null) {
            Log.d("rangohour", rangoHour);
            final String[] rangoHourSplited = rangoHour.split("\\s*;\\s*");
            Log.d("rangoHourSplited", rangoHourSplited[0] + " y " + rangoHourSplited[1]);
            myMsgtitle.setText(nom + " quiere el dia " + fech + " libre desde las " + rangoHourSplited[0] + " hasta las " + rangoHourSplited[1] + " por " + mot);
        } else {
            myMsgtitle.setText(nom + " quiere todo el dia " + fech + " libre por " + mot);
        }
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnAceptar = mTresBtn.findViewById(R.id.btn2);
        btnAceptar.setText("Aceptar");
        final Button btnRechazar = mTresBtn.findViewById(R.id.btn1);
        btnRechazar.setText("Rechazar");
        final Button btnCance = mTresBtn.findViewById(R.id.Cancelar);
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

        btnRechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                try {
                                    final Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fech);
                                    DecimalFormat mFormat = new DecimalFormat("00");
                                    if (documentSnapshot.getString(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear()) != null) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), FieldValue.delete());
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        final String[] diasSotLista = tip.split("\\s*;\\s*");
                        final List<String> dias2 = new ArrayList<>();
                        for (String ds : diasSotLista) {
                            if (!ds.contains(fech)) {
                                dias2.add(ds);
                            } else {
                                if (rangoHour != null) {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).update(ds.replaceAll("/", "-"), FieldValue.delete());
                                }
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
                                                        menu.snackbar.setText("El dia " + fech + " ha sido rechazado");
                                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(12);
                                                        snackbarDS.configSnackbar(getContext(), menu.snackbar);
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
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                try {
                                    final Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fech);
                                    final DecimalFormat mFormat = new DecimalFormat("00");
                                    if (documentSnapshot.getString(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear()) != null) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nom).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), documentSnapshot.getString(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear())).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), FieldValue.delete());
                                            }
                                        });
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        String tip = documentSnapshot.getString("Dias libres solicitados");
                        String fin = null;
                        String aceptD = documentSnapshot.getString("Dias libres");
                        String acept = null;
                        final String[] diasSotLista = tip.split("\\s*;\\s*");
                        final List<String> dias2 = new ArrayList<>();
                        for (final String ds : diasSotLista) {
                            if (!ds.contains(fech)) {
                                dias2.add(ds);
                            } else {
                                acept = ds;
                                if (rangoHour != null) {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nom).update(ds.replaceAll("/", "-"), rangoHour).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nom).update(ds.replaceAll("/", "-"), FieldValue.delete());
                                        }
                                    });
                                }
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
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nom)
                                            .set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Todas las ids").document(documentSnapshot.getString("id"))
                                                    .set(mapSolis, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                            menu.snackbar.setText("El dia " + fech + " ha sido aceptado");
                                                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                            tv.setTextSize(12);
                                                            snackbarDS.configSnackbar(getContext(), menu.snackbar);
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

        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre2).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                motiv = documentSnapshot.getString("Dias libres");
                final String[] listaMotiv = motiv.split("\\s*;\\s*");
                for (String mot : listaMotiv) {
                    if (mot.contains(date)) {
                        if (mot.contains("V")) {
                            dialogoYaDia(nombre2, date, "vacaciones", documentSnapshot.getString(mot.replaceAll("/", "-")), mot);
                        } else if (mot.contains("B")) {
                            dialogoYaDia(nombre2, date, "baja laboral", documentSnapshot.getString(mot.replaceAll("/", "-")), mot);
                        } else if (mot.contains("O")) {
                            dialogoYaDia(nombre2, date, documentSnapshot.getString(date.replaceAll("/", "-")), documentSnapshot.getString(mot.replaceAll("/", "-")), mot);
                        }
                        break;
                    }
                }
            }
        });
    }

    private void dialogoYaDia(final String nombre3, final String date, final String motivo, final String rangoHour, final String dateMasMot) {
        menu.cargando(true);
        touch(true);
        mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
        final Button btnCambMot = mCuatroBtn.findViewById(R.id.btn1);
        btnCambMot.setText("Cambiar motivo");
        final Button btnEliminar = mCuatroBtn.findViewById(R.id.btn2);
        btnEliminar.setText("Eliminar dia libre");
        final Button btnCambRang = mCuatroBtn.findViewById(R.id.btn3);
        btnCambRang.setText("Cambiar horas libres");
        final Button btnCance = mCuatroBtn.findViewById(R.id.Cancelar);
        final TextView myMsgtitle = new TextView(getActivity());
        if (rangoHour != null) {
            final String[] rangoHourSplited = rangoHour.split("\\s*;\\s*");
            myMsgtitle.setText(nombre3 + " tiene el dia " + date + " libre desde las " + rangoHourSplited[0] + " hasta las " + rangoHourSplited[1] + " por " + motivo);
        } else {
            myMsgtitle.setText(nombre3 + " tiene todo el dia " + date + " libre por " + motivo);
        }
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        AlertDialog.Builder AdministrarDiasLibresAd2 = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mCuatroBtn);
        final AlertDialog dialogoAdministrarDiasLibresAd2 = AdministrarDiasLibresAd2.create();

        btnCambMot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String motAn = motivo;
                if(motivo.equals("vacaciones")){
                    motAn = "V";
                }else if(motivo.equals("baja laboral")){
                    motAn = "B";
                }else{
                    motAn = "O";
                }
                dialogoAdministrarDiasLibresAd2.dismiss();
                final TextView titulo = new TextView(getActivity());
                titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                titulo.setTextColor(Color.BLACK);
                titulo.setText("Seleccione el nuevo motivo para el dia " + date + " libre de " + nombre3);
                mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
                final Button btnVaca = mCambMot.findViewById(R.id.btn1);
                final Button btnBaja = mCambMot.findViewById(R.id.btn2);
                final Button btnOtros = mCambMot.findViewById(R.id.btn3);
                final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getContext())
                        .setCustomTitle(titulo)
                        .setView(mCambMot);
                final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                final String finalMotAn = motAn;
                btnVaca.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministrarDiasLibres.dismiss();
                        motivNu(nombre3, date, "V", rangoHour, finalMotAn);

                    }
                });
                btnBaja.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoAdministrarDiasLibres.dismiss();
                        motivNu(nombre3, date, "B", rangoHour, finalMotAn);

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
                                    motivNu(nombre3, date, "O", rangoHour, finalMotAn);
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
                    mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
                    dialogoAdministrarDiasLibres.show();
                } else {
                    dialogoAdministrarDiasLibres.show();
                }
            }
        });

        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDiasLibresAd2.dismiss();
                ((ViewGroup) myMsgtitle.getParent()).removeView(myMsgtitle);

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
                                                        if(dateMasMot.contains("O")){
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(date.replaceAll("/", "-"), FieldValue.delete());
                                                        }
                                                        if(rangoHour!=null){
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(dateMasMot.replaceAll("/", "-"), FieldValue.delete());
                                                        }
                                                        if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
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
                                                        menu.snackbar.setText("Dia libre eliminado correctamente");
                                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(12);
                                                        snackbarDS.configSnackbar(getContext(), menu.snackbar);
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

        btnCambRang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                final int[] horaEntOSal = {0};
                dialogoAdministrarDiasLibresAd2.dismiss();
                final TextView myMsgtitle = new TextView(getActivity());
                myMsgtitle.setText("Asigne todo el dia libre o seleccione las horas libres");
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                myMsgtitle.setLayoutParams(params);
                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                myMsgtitle.setTextColor(Color.BLACK);
                mRango = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
                final Button btnTodo = mRango.findViewById(R.id.btn2);
                btnTodo.setText("Todo el dia");
                final Button btnHoras = mRango.findViewById(R.id.btn1);
                btnHoras.setText("Seleccionar horas");
                final Button btnCancelar = mRango.findViewById(R.id.Cancelar);
                final AlertDialog.Builder rango = new AlertDialog.Builder(requireContext())
                        .setCustomTitle(myMsgtitle)
                        .setView(mRango);
                final AlertDialog dialogoRango = rango.create();
                btnTodo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.cargando(true);
                        touch(true);
                        if(rangoHour!=null){
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(dateMasMot.replaceAll("/", "-"), FieldValue.delete());
                            dialogoRango.dismiss();
                            menu.snackbar.setText("Se ha asignado todo el dia " + date + " libre correctamente");
                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextSize(12);
                            snackbarDS.configSnackbar(getContext(), menu.snackbar);
                            menu.snackbar.show();
                            menu.cargando(false);
                            touch(false);
                        }
                    }
                });
                btnHoras.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.cargando(true);
                        touch(true);
                        final TextView myMsgtitle = new TextView(getActivity());
                        myMsgtitle.setText("¿A que hora empezara a librar " + nombre3 + " el dia " + date + "?");
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        myMsgtitle.setLayoutParams(params);
                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                        myMsgtitle.setTextColor(Color.GREEN);
                        mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                        final TimePicker timePicker = mHoras.findViewById(R.id.elegirHora);
                        timePicker.setIs24HourView(true);
                        if (hEntrada != null) {
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                            Date entrada = null;
                            try {
                                entrada = format.parse(hEntrada);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            timePicker.setHour(new DateTime(entrada).getHourOfDay());
                            timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                        } else {
                            timePicker.setHour(00);
                            timePicker.setMinute(00);
                        }
                        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                            @Override
                            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                if (hEntrada != null && hSalida != null && hEntrada2 == null && hSalida2 == null) {
                                    Date entrada = null;
                                    Date salida = null;
                                    try {
                                        entrada = format.parse(hEntrada);
                                        salida = format.parse(hSalida);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (hourOfDay < new DateTime(entrada).getHourOfDay() || hourOfDay > new DateTime(salida).getHourOfDay()) {
                                        timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                    } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                    } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                    }
                                } else if (hEntrada != null && hSalida != null && hEntrada2 != null && hSalida2 != null) {
                                    Date entrada = null;
                                    Date salida = null;
                                    Date entrada2 = null;
                                    Date salida2 = null;
                                    try {
                                        entrada = format.parse(hEntrada);
                                        salida2 = format.parse(hSalida2);
                                        entrada2 = format.parse(hEntrada2);
                                        salida = format.parse(hSalida);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                                        timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                    } else if ((hourOfDay > new DateTime(salida2).getHourOfDay() && hourOfDay < new DateTime(entrada2).getHourOfDay())) {
                                        if (horaEntOSal[0] == 0) {
                                            horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                                            timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                                        } else if (horaEntOSal[0] == new DateTime(entrada2).getHourOfDay()) {
                                            horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                                            timePicker.setHour(new DateTime(salida2).getHourOfDay());
                                        } else if (horaEntOSal[0] == new DateTime(salida2).getHourOfDay()) {
                                            horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                                            timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                                        }
                                    } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                                        timePicker.setHour(new DateTime(salida).getHourOfDay());
                                    } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                    } else if (hourOfDay == new DateTime(salida2).getHourOfDay() && minute > new DateTime(salida2).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(salida2).getMinuteOfHour());
                                    } else if (hourOfDay == new DateTime(entrada2).getHourOfDay() && minute < new DateTime(entrada2).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(entrada2).getMinuteOfHour());
                                    } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                    }
                                }
                            }
                        });
                        final Button btnCont = mHoras.findViewById(R.id.btn1);
                        final Button btnCancelar = mHoras.findViewById(R.id.btn2);
                        final AlertDialog.Builder rangoHora = new AlertDialog.Builder(getContext());
                        rangoHora.setCustomTitle(myMsgtitle)
                                .setView(mHoras);
                        final AlertDialog dialogoRangoHora = rangoHora.create();
                        btnCont.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final DecimalFormat decimalFormat = new DecimalFormat("00");
                                final String entradaDef = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                                if (!entradaDef.isEmpty()) {
                                    final TextView myMsgtitle = new TextView(getActivity());
                                    myMsgtitle.setText("¿A que hora terminara de librar " + nombre3 + " el dia " + date + "?");
                                    myMsgtitle.setTextColor(Color.RED);
                                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    myMsgtitle.setLayoutParams(params);
                                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                    mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                                    final TimePicker timePicker = mHoras.findViewById(R.id.elegirHora);
                                    timePicker.setIs24HourView(true);
                                    if (hSalida != null) {
                                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                        Date salida = null;
                                        try {
                                            salida = format.parse(hSalida);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        timePicker.setHour(new DateTime(salida).getHourOfDay());
                                        timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                    } else {
                                        timePicker.setHour(00);
                                        timePicker.setMinute(00);
                                    }
                                    timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                                        @Override
                                        public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                                            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                            if (hEntrada != null && hSalida != null && hEntrada2 == null && hSalida2 == null) {
                                                Date entrada = null;
                                                Date salida = null;
                                                Date entDef = null;
                                                try {
                                                    entrada = format.parse(hEntrada);
                                                    salida = format.parse(hSalida);
                                                    entDef = format.parse(entradaDef);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                if (hourOfDay < new DateTime(entDef).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(entDef).getHourOfDay());
                                                } else if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                                } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(salida).getHourOfDay());
                                                } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(entDef).getHourOfDay() && minute < new DateTime(entDef).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(entDef).getMinuteOfHour());
                                                }
                                            } else if (hEntrada != null && hSalida != null && hEntrada2 != null && hSalida2 != null) {
                                                Date entrada = null;
                                                Date salida = null;
                                                Date entrada2 = null;
                                                Date salida2 = null;
                                                Date entDef = null;
                                                try {
                                                    entrada = format.parse(hEntrada);
                                                    salida2 = format.parse(hSalida2);
                                                    entrada2 = format.parse(hEntrada2);
                                                    salida = format.parse(hSalida);
                                                    entDef = format.parse(entradaDef);
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                if (hourOfDay < new DateTime(entrada).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(entrada).getHourOfDay());
                                                } else if ((hourOfDay > new DateTime(salida2).getHourOfDay() && hourOfDay < new DateTime(entrada2).getHourOfDay())) {
                                                    if (horaEntOSal[0] == 0) {
                                                        horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                                                        timePicker.setHour(new DateTime(salida2).getHourOfDay());
                                                    } else if (horaEntOSal[0] == new DateTime(salida2).getHourOfDay()) {
                                                        horaEntOSal[0] = new DateTime(entrada2).getHourOfDay();
                                                        timePicker.setHour(new DateTime(entrada2).getHourOfDay());
                                                    } else if (horaEntOSal[0] == new DateTime(entrada2).getHourOfDay()) {
                                                        horaEntOSal[0] = new DateTime(salida2).getHourOfDay();
                                                        timePicker.setHour(new DateTime(salida2).getHourOfDay());
                                                    }
                                                } else if (hourOfDay > new DateTime(salida).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(salida).getHourOfDay());
                                                } else if (hourOfDay < new DateTime(entDef).getHourOfDay()) {
                                                    timePicker.setHour(new DateTime(entDef).getHourOfDay());
                                                } else if (hourOfDay == new DateTime(entrada).getHourOfDay() && minute < new DateTime(entrada).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(entrada).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(salida2).getHourOfDay() && minute > new DateTime(salida2).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(salida2).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(entrada2).getHourOfDay() && minute < new DateTime(entrada2).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(entrada2).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(salida).getHourOfDay() && minute > new DateTime(salida).getMinuteOfHour()) {
                                                    timePicker.setMinute(new DateTime(salida).getMinuteOfHour());
                                                } else if (hourOfDay == new DateTime(entDef).getHourOfDay() && minute < new DateTime(entDef).getMinuteOfHour()) {
                                                    timePicker.setHour(new DateTime(entDef).getMinuteOfHour());
                                                }
                                            }
                                        }
                                    });
                                    final Button btnCon2 = mHoras.findViewById(R.id.btn1);
                                    final Button btnCancelar2 = mHoras.findViewById(R.id.btn2);
                                    final AlertDialog.Builder rangoHora2 = new AlertDialog.Builder(getContext());
                                    rangoHora2.setCustomTitle(myMsgtitle)
                                            .setView(mHoras);
                                    final AlertDialog dialogoRangoHora2 = rangoHora2.create();
                                    btnCon2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final String salidaDef = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                                            if (Integer.parseInt(entradaDef.substring(0, 2)) >= Integer.parseInt(salidaDef.substring(0, 2))
                                                    && Integer.parseInt(entradaDef.substring(3)) >= Integer.parseInt(salidaDef.substring(3))) {
                                                final Snackbar snackbar = Snackbar.make(mHoras, "La hora de finalizacion del tiempo libre no puede ser antes o igual a la de inicio", 5000);
                                                if (Integer.parseInt(entradaDef.substring(0, 2)) > Integer.parseInt(salidaDef.substring(0, 2))) {
                                                    snackbar.setText("La hora de finalizacion del tiempo libre no puede ser antes que la de inicio");
                                                } else if (Integer.parseInt(entradaDef.substring(0, 2)) == Integer.parseInt(salidaDef.substring(0, 2))) {
                                                    snackbar.setText("La hora de finalizacion del tiempo libre no puede ser igual a la de inicio");
                                                }
                                                TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                tv.setTextSize(10);
                                                snackbarDS.configSnackbar(getActivity(), snackbar);
                                                snackbar.setAction("Entendido", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        snackbar.dismiss();
                                                    }
                                                }).setActionTextColor(Color.WHITE);
                                                snackbar.show();
                                            } else {
                                                dialogoRangoHora2.dismiss();
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                if (dateMasMot.contains("V")) {
                                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre3 + "?\n El dia " + date + " por vacaciones");
                                                } else if (dateMasMot.contains("B")) {
                                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre3 + "?\n El dia " + date + " por baja laboral");
                                                } else if (dateMasMot.contains("O")) {
                                                    myMsgtitle.setText("¿Desea asignar el siguiente dia libre para " + nombre3 + "?\n El dia " + date + " por " + motivo);
                                                }
                                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                myMsgtitle.setLayoutParams(params);
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.GRAY);
                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                                final TextView textView = mDosBtn.findViewById(R.id.elegirHora);
                                                textView.setText("De " + entradaDef + " A " + salidaDef);
                                                textView.setTextColor(Color.parseColor("#FF00ff00"));
                                                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                                                anim.setDuration(200);
                                                anim.setStartOffset(20);
                                                anim.setRepeatMode(Animation.REVERSE);
                                                anim.setRepeatCount(Animation.INFINITE);
                                                anim.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {
                                                        textView.setTextColor(Color.parseColor("#FF00ff00"));
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        textView.setTextColor(Color.parseColor("#8000ff00"));
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {


                                                    }
                                                });
                                                textView.startAnimation(anim);
                                                final Button btnConf = mDosBtn.findViewById(R.id.btn1);
                                                btnConf.setText("Confirmar");
                                                final Button btnCancelar3 = mDosBtn.findViewById(R.id.btn2);
                                                final AlertDialog.Builder alerta3 = new AlertDialog.Builder(getContext());
                                                alerta3.setCustomTitle(myMsgtitle)
                                                        .setView(mDosBtn);
                                                final AlertDialog dialogoAlerta3 = alerta3.create();
                                                btnConf.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        menu.cargando(true);
                                                        touch(true);
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre3).update(dateMasMot.replaceAll("/", "-"), entradaDef + ";" + salidaDef).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                dialogoAlerta3.dismiss();
                                                                menu.snackbar.setText("Se ha asignado el dia " + date + " libre desde las " + entradaDef + " hasta las " + salidaDef + " a " + nombre3 + " correctamente");
                                                                TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                tv.setTextSize(12);
                                                                snackbarDS.configSnackbar(getContext(), menu.snackbar);
                                                                menu.snackbar.show();
                                                                menu.cargando(false);
                                                                touch(false);

                                                            }
                                                        });
                                                    }
                                                });
                                                btnCancelar3.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoAlerta3.dismiss();
                                                    }
                                                });
                                                dialogoAlerta3.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialog) {
                                                        dialogoRangoHora2.dismiss();
                                                        textView.setEnabled(true);
                                                        btnConf.setEnabled(true);
                                                        btnCancelar3.setEnabled(true);
                                                    }
                                                });
                                                dialogoAlerta3.setCanceledOnTouchOutside(false);
                                                if (mDosBtn.getParent() != null) {
                                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                                    dialogoAlerta3.show();
                                                } else {
                                                    dialogoAlerta3.show();
                                                }
                                            }
                                        }
                                    });
                                    btnCancelar2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogoRangoHora2.dismiss();
                                        }
                                    });
                                    dialogoRangoHora2.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            dialogoRangoHora.dismiss();
                                            timePicker.setEnabled(true);
                                            btnCon2.setEnabled(true);
                                            btnCancelar2.setEnabled(true);
                                        }
                                    });
                                    dialogoRangoHora2.setCanceledOnTouchOutside(false);
                                    if (mHoras.getParent() != null) {
                                        ((ViewGroup) mHoras.getParent()).removeView(mHoras);
                                        mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                                        dialogoRangoHora2.show();
                                    } else {
                                        dialogoRangoHora2.show();
                                    }
                                    dialogoRangoHora.show();
                                }
                            }
                        });
                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogoRangoHora.dismiss();
                            }
                        });
                        dialogoRangoHora.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                dialogoRango.dismiss();
                                timePicker.setEnabled(true);
                                btnCont.setEnabled(true);
                                btnCancelar.setEnabled(true);
                                menu.cargando(false);
                                touch(false);
                            }
                        });
                        dialogoRangoHora.setCanceledOnTouchOutside(false);
                        if (mHoras.getParent() != null) {
                            ((ViewGroup) mHoras.getParent()).removeView(mHoras);
                            mHoras = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                            dialogoRangoHora.show();
                        } else {
                            dialogoRangoHora.show();
                        }

                    }
                });
                btnCancelar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoRango.dismiss();
                    }
                });
                dialogoRango.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        btnTodo.setEnabled(true);
                        btnHoras.setEnabled(true);
                        btnCancelar.setEnabled(true);
                        menu.cargando(false);
                        touch(false);
                    }
                });
                dialogoRango.setCanceledOnTouchOutside(false);
                if (mRango.getParent() != null) {
                    ((ViewGroup) mRango.getParent()).removeView(mRango);
                    mRango = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
                    dialogoRango.show();
                } else {
                    dialogoRango.show();
                }
            }
        });
        dialogoAdministrarDiasLibresAd2.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCambMot.setEnabled(true);
                btnCambRang.setEnabled(true);
                btnEliminar.setEnabled(true);
                btnCance.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoAdministrarDiasLibresAd2.setCanceledOnTouchOutside(false);
        if (mCuatroBtn.getParent() != null) {
            ((ViewGroup) mCuatroBtn.getParent()).removeView(mCuatroBtn);
            mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
            dialogoAdministrarDiasLibresAd2.show();
        } else {
            dialogoAdministrarDiasLibresAd2.show();
        }
    }

    private void motivNu(final String nombre4, final String fecha, final String motivo, final String rango, final String motivoAn) {
        menu.cargando(true);
        touch(true);
        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre4).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                final String[] listaMotiv = documentSnapshot.getString("Dias libres").split("\\s*;\\s*");
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
                                                        if(rango!=null){
                                                            final Map<String, Object> mapRango = new HashMap<>();
                                                            mapRango.put(fecha.replaceAll("/", "-")+motivo, rango);
                                                            mapRango.put(fecha.replaceAll("/", "-")+motivoAn, FieldValue.delete());
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre4).set(mapRango, SetOptions.merge());
                                                        }
                                                        if (slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
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
                                                        menu.snackbar.setText("Motivo del dia libre " + fecha + " actualizado");
                                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(10);
                                                        snackbarDS.configSnackbar(getContext(), menu.snackbar);
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
                    if (new DateTime(date1).getDayOfYear() >= new DateTime(currentTime).getDayOfYear()
                            || new DateTime(date1).getYear() > new DateTime(currentTime).getYear()) {
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
                        final String[] diasSoliLista = doc.getString("Dias libres solicitados").replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
                        for (String ds : diasSoliLista) {
                            if (ds != null) {
                                try {
                                    final Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ds);
                                    Date currentTime = Calendar.getInstance().getTime();
                                    SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    final String dateS = fmt.format(date1);
                                    if (date1.before(currentTime)) {
                                        if (new DateTime(date1).getMonthOfYear() == new DateTime(currentTime).getMonthOfYear()
                                                && new DateTime(date1).getMonthOfYear() == new DateTime(currentTime).getMonthOfYear()
                                                && new DateTime(date1).getDayOfMonth() == new DateTime(currentTime).getDayOfMonth()) {
                                            collectionDates.add(date1);
                                            calendarPickerView.highlightDates(collectionDates);
                                        } else {
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(doc.getString("nombre")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.getString("Dias libres solicitados") != null) {
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(doc.getString("nombre")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                DecimalFormat mFormat = new DecimalFormat("00");
                                                                if (documentSnapshot.getString(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear()) != null) {
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(doc.getString("nombre")).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), FieldValue.delete());
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(doc.getString("nombre")).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), FieldValue.delete());
                                                                }

                                                            }
                                                        });
                                                        String tip = documentSnapshot.getString("Dias libres solicitados");
                                                        String fin = null;
                                                        final Map<String, Object> mapD = new HashMap<>();
                                                        final String[] diasSotLista = tip.split("\\s*;\\s*");
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
                                        }
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
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (doc.getString("Dias libres") != null && !doc.getBoolean("desactivado")) {
                                    try {
                                        String diasnomot = doc.getString("Dias libres").replaceAll("B", "").replaceAll("O", "").replaceAll("V", "");
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
                                                if (new DateTime(date1).getDayOfYear() >= new DateTime(currentTime).getDayOfYear()
                                                        || new DateTime(date1).getYear() > new DateTime(currentTime).getYear()) {
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
                                                    if (doc.getString("Dias libres") != null && !doc.getBoolean("desactivado")) {
                                                        final String[] diasAceptLista = doc.getString("Dias libres").replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
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
                                    if (!slidingLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
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
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}
