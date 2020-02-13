package com.japac.pac.menu.empleados;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Arrays;
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
public class gestionarDiasEmpleados extends Fragment {

    private FirebaseFirestore firebaseFirestore;

    private String id, empresa, nombre, otroMot, MODLN, MODLN2, rol;

    private CalendarPickerView calendarPickerView, calendarPickerView2;

    private final ArrayList<Date> collectionDates = new ArrayList<>(31);
    private final ArrayList<Date> collectionDates2 = new ArrayList<>(31);

    private TextView diasSolicTextV2;

    private View mCambMot;
    private View mDosBtn;
    private View mJustificar;

    private SlidingUpPanelLayout slidingLayout;

    private Boolean hayDias = true;

    private ImageView xpand;

    public gestionarDiasEmpleados() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
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
                if (menu.getCambioDeFragment()) {
                    actualizarCalendarioEmpl();
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        if (hayDias) {
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                    }
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    menu.setCambioDeFragmento(false);
                } else {
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        if (hayDias) {
                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        }
                    }
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }

            }
        });
        diasSolicTextV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menu.getCambioDeFragment()) {
                    actualizarCalendarioEmpl();
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                    menu.setCambioDeFragmento(false);
                } else {
                    if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    }
                }
            }
        });
        xpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menu.getCambioDeFragment()) {
                    actualizarCalendarioEmpl();
                    if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        xpand.setImageResource(R.drawable.ic_expand_up);
                    } else if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        xpand.setImageResource(R.drawable.ic_expand_down);
                    }
                    menu.setCambioDeFragmento(false);
                } else {

                    if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        xpand.setImageResource(R.drawable.ic_expand_up);
                    } else if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        xpand.setImageResource(R.drawable.ic_expand_down);
                    }
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
                    if (newState != SlidingUpPanelLayout.PanelState.COLLAPSED) {
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
                    if (newState==SlidingUpPanelLayout.PanelState.EXPANDED) {
                        if (menu.getCambioDeFragment()) {
                            actualizarCalendarioEmpl();
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
        id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        NotificationManager notificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).cancelAll();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                empresa = documentSnapshot.getString("empresa");
                nombre = documentSnapshot.getString("nombre");
                rol = documentSnapshot.getString("rol");
                if (rol != null) {
                    if (rol.equals("Empleado")) {
                        actualizarCalendarioEmpl();
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {

                                    return;
                                }
                                if (Objects.requireNonNull(documentSnapshot).exists()) {
                                    actualizarCalendarioEmpl();
                                    if (documentSnapshot.getBoolean("aceptado") != null && documentSnapshot.getBoolean("asignado") != null && documentSnapshot.getBoolean("rechazado") != null && documentSnapshot.getBoolean("eliminado") != null) {
                                        if (Objects.requireNonNull(documentSnapshot.getBoolean("aceptado")).equals(true)) {
                                            menu.snackbar.setText("Un día libre solicitado se ha aceptado");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("aceptado", false);
                                        } else if (Objects.requireNonNull(documentSnapshot.getBoolean("asignado")).equals(true)) {
                                            menu.snackbar.setText("Se le ha asignado un día libre");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("asignado", false);
                                        } else if (Objects.requireNonNull(documentSnapshot.getBoolean("rechazado")).equals(true)) {
                                            menu.snackbar.setText("Un día libre solicitado se ha rechazado");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("rechazado", false);
                                        } else if (Objects.requireNonNull(documentSnapshot.getBoolean("eliminado")).equals(true)) {
                                            menu.snackbar.setText("Se le ha eliminado un día libre");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("eliminado", false);
                                        }
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(12);
                                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                        menu.snackbar.show();
                                        if (menu.getCambioDeFragment()) {
                                            actualizarCalendarioEmpl();
                                            if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                                if (hayDias) {
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                }
                                            }

                                            menu.setCambioDeFragmento(false);
                                        } else {
                                            if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                                if (hayDias) {
                                                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        return root;
    }

    private void dialogoYaExiste(final String fecha) {
        menu.cargando(true);
        touch(true);
        final TextView titulo2 = new TextView(getActivity());
        titulo2.setText("Ya se ha solicitado el dia " + fecha + " libre por " + MODLN2 + "\nespere una respuesta de su administrador o edite la solicitud");
        titulo2.setGravity(Gravity.CENTER_HORIZONTAL);
        View mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnElimS = mTresBtn.findViewById(R.id.btn1);
        final Button btnCambM = mTresBtn.findViewById(R.id.btn2);
        final Button btnCance = mTresBtn.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setCustomTitle(titulo2)
                .setView(mTresBtn);
        final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDias.cancel();
            }
        });
        btnCambM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                dialogoAdministrarDias.cancel();
                final TextView titulo = new TextView(getActivity());
                titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                titulo.setText("Seleccione el nuevo motivo para tener el dia " + fecha + " libre");
                mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
                final Button btnVaca = mCambMot.findViewById(R.id.Vacas);
                final Button btnBaja = mCambMot.findViewById(R.id.Baja);
                final Button btnOtros = mCambMot.findViewById(R.id.Otros);
                final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setCustomTitle(titulo)
                        .setView(mCambMot);
                final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
                btnVaca.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        anadeEmplSolis(fecha, "V");
                        dialogoAdministrarDiasLibres.dismiss();
                    }
                });
                btnBaja.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        anadeEmplSolis(fecha, "B");
                        dialogoAdministrarDiasLibres.dismiss();
                    }
                });
                btnOtros.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.cargando(true);
                        touch(true);
                        dialogoAdministrarDiasLibres.dismiss();
                        mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                        final AlertDialog.Builder Login = new AlertDialog.Builder(getActivity());
                        final TextView titulo = new TextView(getActivity());
                        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                        titulo.setText("Justifique el motivo");
                        final EditText sJustificar = mJustificar.findViewById(R.id.justificaDialogo);
                        Login
                                .setCustomTitle(titulo)
                                .setView(mJustificar);
                        final Button btnsSguiente = mJustificar.findViewById(R.id.btn1);
                        final AlertDialog dialogoLogin = Login.create();
                        btnsSguiente.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String JustTexto = sJustificar.getText().toString();

                                if (!JustTexto.isEmpty()) {

                                    otroMot = JustTexto;
                                    sJustificar.setHintTextColor(Color.GRAY);
                                    anadeEmplSolis(fecha, "O");
                                    dialogoLogin.dismiss();


                                } else {

                                    sJustificar.setHintTextColor(Color.RED);

                                }
                            }
                        });
                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(final DialogInterface dialog) {
                                sJustificar.setEnabled(true);
                                btnsSguiente.setEnabled(true);
                                menu.cargando(false);
                                touch(false);
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
                        menu.cargando(false);
                        touch(false);
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
        btnElimS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDias.dismiss();
                anadeEmplSolis(fecha, "ELIMINAR");
            }
        });
        dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCambM.setEnabled(true);
                btnElimS.setEnabled(true);
                btnCance.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoAdministrarDias.setCanceledOnTouchOutside(false);
        if (mTresBtn.getParent() != null) {
            ((ViewGroup) mTresBtn.getParent()).removeView(mTresBtn);
            mTresBtn = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoAdministrarDias.show();
        } else {
            dialogoAdministrarDias.show();
        }
    }

    private void dDiasLibresSelecEmpl(final Date date) {
        menu.cargando(true);
        touch(true);
        final TextView titulo = new TextView(getActivity());
        titulo.setGravity(Gravity.CENTER_HORIZONTAL);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        final String datefull = formato.format(date);
        titulo.setText("¿Porque motivo deseas tomarte el dia " + datefull + " libre?");
        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
        final Button btnVaca = mCambMot.findViewById(R.id.Vacas);
        final Button btnBaja = mCambMot.findViewById(R.id.Baja);
        final Button btnOtros = mCambMot.findViewById(R.id.Otros);
        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setCustomTitle(titulo)
                .setView(mCambMot);
        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
        btnVaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                anadeEmplSolis(datefull, "V");
                dialogoAdministrarDiasLibres.dismiss();
            }
        });
        btnBaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                anadeEmplSolis(datefull, "B");
                dialogoAdministrarDiasLibres.dismiss();
            }
        });
        btnOtros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                dialogoAdministrarDiasLibres.dismiss();
                mJustificar = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                final Button btnSiguiente = mJustificar.findViewById(R.id.btn1);
                final TextView titulo = new TextView(getActivity());
                titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                titulo.setText("Justifique el motivo");
                final AlertDialog.Builder Login = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
                final EditText sJustificar = mJustificar.findViewById(R.id.justificaDialogo);
                Login
                        .setCustomTitle(titulo)
                        .setView(mJustificar);
                final AlertDialog dialogoLogin = Login.create();
                btnSiguiente.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String JustTexto = sJustificar.getText().toString();

                        if (!JustTexto.isEmpty()) {

                            otroMot = JustTexto;
                            sJustificar.setHintTextColor(Color.GRAY);
                            anadeEmplSolis(datefull, "O");
                            dialogoLogin.dismiss();


                        } else {

                            sJustificar.setHintTextColor(Color.RED);

                        }
                    }
                });
                dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        sJustificar.setEnabled(true);
                        btnSiguiente.setEnabled(true);
                        menu.cargando(false);
                        touch(false);
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
                menu.cargando(false);
                touch(false);
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

    private void anadeEmplSolis(final String date, final String mot) {
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                    String textoToast = null;
                    final String diasLibresSoli = documentSnapshot.getString("Dias libres solicitados");
                    final Map<String, Object> map2 = new HashMap<>();
                    String insertar = null;
                    if (mot.equals("ELIMINAR")) {
                        if (Objects.requireNonNull(diasLibresSoli).contains(date)) {
                            insertar = diasLibresSoli.replace(date + "V;", "").replace(date + "B;", "").replace(date + "O;", "");
                            if (insertar.equals("")) {
                                map2.put("Dias libres solicitados", null);
                            } else {
                                map2.put("Dias libres solicitados", insertar);
                            }
                            textoToast = "La solicitud para tener el dia " + date + " libre se ha eliminado";
                        }
                    } else {
                        if (Objects.requireNonNull(diasLibresSoli).contains(date)) {
                            String fin = null;
                            String acept = null;
                            final String[] diasSotLista = diasLibresSoli.split("\\s*;\\s*");
                            final List<String> dias2 = new ArrayList<>();
                            for (String ds : diasSotLista) {
                                if (!ds.contains(date)) {
                                    dias2.add(ds);
                                } else {
                                    acept = ds.replaceAll("V", "").replaceAll("B", "").replaceAll("O", "") + mot;
                                    dias2.add(acept);
                                }
                            }
                            for (String ds2 : dias2) {
                                if (fin == null) {
                                    fin = ds2 + ";";
                                } else {
                                    fin = fin + ds2 + ";";
                                }
                            }
                            textoToast = "Cambiado el motivo de su solicitud para el dia libre " + date;
                            insertar = fin;
                        } else if (!diasLibresSoli.contains(date)) {
                            insertar = diasLibresSoli + date + mot + ";";
                            textoToast = "Dia " + date + " solicitado libre correctamente";
                        }
                        map2.put("Dias libres solicitados", insertar);
                    }
                    final String finalTextoToast = textoToast;
                    firebaseFirestore.collection("Todas las ids").document(id).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (mot.equals("O")) {
                                        String date3 = date.replaceAll("/", "-");
                                        map2.put(date3, otroMot);
                                    }
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            map2.put("solicita", true);
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (mot.equals("ELIMINAR") && diasLibresSoli.contains(date + "O")) {
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-"), FieldValue.delete()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        menu.snackbar.setText(finalTextoToast);
                                                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                        tv.setTextSize(10);
                                                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                                        menu.snackbar.show();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        menu.snackbar.setText(finalTextoToast);
                                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                        tv.setTextSize(10);
                                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                        menu.snackbar.show();
                                                    }
                                                    if (menu.getCambioDeFragment()) {
                                                        actualizarCalendarioEmpl();
                                                        if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                        }

                                                        menu.setCambioDeFragmento(false);
                                                    } else {
                                                        if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
                } else if (documentSnapshot.getString("Dias libres solicitados") == null) {
                    final Map<String, Object> map2 = new HashMap<>();
                    if (documentSnapshot.getString("nombre") == null) {
                        map2.put("nombre", nombre);
                    }
                    map2.put("Dias libres solicitados", date + mot + ";");
                    if (mot.equals("O")) {
                        String date3 = date.replaceAll("/", "-");
                        map2.put(date3, otroMot);
                    }
                    firebaseFirestore.collection("Todas las ids").document(id).update("Dias libres solicitados", date + mot + ";").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(nombre).update("Dias libres solicitados", date + mot + ";").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            map2.put("solicita", true);
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    menu.snackbar.setText("Dia solicitado, espere una respuesta de su administrador");
                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(10);
                                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                    menu.snackbar.show();
                                                    if (menu.getCambioDeFragment()) {
                                                        actualizarCalendarioEmpl();
                                                        if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                        }

                                                        menu.setCambioDeFragmento(false);
                                                    } else {
                                                        if (slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
                }
            }
        });
    }

    private void actualizarCalendarioEmpl() {
        final Date hoy = new Date();
        final Calendar siguienteAno = Calendar.getInstance();
        siguienteAno.add(Calendar.YEAR, 1);
        calendarPickerView.init(hoy, siguienteAno.getTime());
        collectionDates.clear();
        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(final Date date) {
                try {
                    calendarPickerView.clearSelectedDates();
                    String dateS = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                    Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateS);
                    Date currentTime = Calendar.getInstance().getTime();
                    if (date1.before(currentTime)) {
                        menu.snackbar.setText("Este dia ya ha pasado, seleccione un dia futuro");
                        TextView tv = ( menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(9);
                        snackbarDS.configSnackbar(getContext(),  menu.snackbar);
                        menu.snackbar.show();
                    } else {
                        menu.cargando(true);
                        touch(true);
                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        final String fecha = formato.format(date);
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String DLN = documentSnapshot.getString("Dias libres");
                                    String DLNS = documentSnapshot.getString("Dias libres solicitados");
                                    MODLN = null;
                                    MODLN2 = null;
                                    if (DLN == null || !DLN.contains(fecha)) {
                                        if (DLNS == null || !DLNS.contains(fecha)) {
                                            dDiasLibresSelecEmpl(date);
                                        } else if (DLNS.contains(fecha)) {
                                            final String[] listaMotivSoli = DLNS.split("\\s*;\\s*");
                                            for (String l2 : listaMotivSoli) {
                                                if (l2.contains(fecha)) {
                                                    if (l2.contains("V")) {
                                                        MODLN2 = "vacaciones";
                                                        dialogoYaExiste(fecha);
                                                        menu.cargando(false);
                                                        touch(false);
                                                        break;
                                                    } else if (l2.contains("B")) {
                                                        MODLN2 = "baja laboral";
                                                        menu.cargando(false);
                                                        touch(false);
                                                        dialogoYaExiste(fecha);
                                                        break;

                                                    } else if (l2.contains("O")) {
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                String camb = fecha.replaceAll("/", "-");
                                                                MODLN2 = documentSnapshot.getString(camb);
                                                                menu.cargando(false);
                                                                touch(false);
                                                                dialogoYaExiste(fecha);
                                                            }
                                                        });
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    } else if (DLN.contains(fecha)) {
                                        final String[] listaMotiv = DLN.split("\\s*;\\s*");
                                        for (String l : listaMotiv) {
                                            if (l.contains(fecha)) {
                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final Button btnVerDias = mDosBtn.findViewById(R.id.btn1);
                                                btnVerDias.setText("Ver mis dias libres");
                                                final Button btnCanc = mDosBtn.findViewById(R.id.btn2);
                                                btnCanc.setText("Cerrar mensaje");
                                                final TextView titulo2 = new TextView(getActivity());
                                                titulo2.setGravity(Gravity.CENTER_HORIZONTAL);
                                                final AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                                                        .setView(mDosBtn);
                                                if (l.contains("O")) {
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                            String camb = fecha.replaceAll("/", "-");
                                                            titulo2.setText("Ya tienes el dia " + fecha + " libre por " + documentSnapshot2.getString(camb));
                                                            AdministrarDias.setCustomTitle(titulo2);
                                                            final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                            btnCanc.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    dialogoAdministrarDias.cancel();
                                                                }
                                                            });
                                                            btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    if (menu.getCambioDeFragment()) {
                                                                        actualizarCalendarioEmpl();
                                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);


                                                                        menu.setCambioDeFragmento(false);
                                                                    } else {
                                                                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                                                                    }
                                                                    dialogoAdministrarDias.cancel();
                                                                }
                                                            });
                                                            dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                @Override
                                                                public void onShow(DialogInterface dialog) {
                                                                    btnVerDias.setEnabled(true);
                                                                    btnCanc.setEnabled(true);
                                                                    menu.cargando(false);
                                                                    touch(false);
                                                                }
                                                            });
                                                            dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                            if (mDosBtn.getParent() != null) {
                                                                ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                dialogoAdministrarDias.show();
                                                            } else {
                                                                dialogoAdministrarDias.show();
                                                            }
                                                        }
                                                    });
                                                    break;
                                                } else if (l.contains("V")) {
                                                    titulo2.setText("Ya tienes el dia " + fecha + " libre por vacaciones");
                                                    AdministrarDias.setCustomTitle(titulo2);
                                                    final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                    btnCanc.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialogoAdministrarDias.cancel();
                                                        }
                                                    });
                                                    btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if (menu.getCambioDeFragment()) {
                                                                actualizarCalendarioEmpl();
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                                                                menu.setCambioDeFragmento(false);
                                                            } else {
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

                                                            }
                                                            dialogoAdministrarDias.cancel();
                                                        }
                                                    });
                                                    dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(DialogInterface dialog) {
                                                            btnVerDias.setEnabled(true);
                                                            btnCanc.setEnabled(true);
                                                            menu.cargando(false);
                                                            touch(false);
                                                        }
                                                    });
                                                    dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                    if (mDosBtn.getParent() != null) {
                                                        ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                        mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoAdministrarDias.show();
                                                    } else {
                                                        dialogoAdministrarDias.show();
                                                    }
                                                } else if (l.contains("B")) {
                                                    titulo2.setText("Ya tienes el dia " + fecha + " libre por baja laboral");
                                                    AdministrarDias.setCustomTitle(titulo2);
                                                    final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                    btnCanc.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialogoAdministrarDias.cancel();
                                                        }
                                                    });
                                                    btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            if (menu.getCambioDeFragment()) {
                                                                actualizarCalendarioEmpl();
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                                menu.setCambioDeFragmento(false);
                                                            } else {
                                                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                                            }
                                                            dialogoAdministrarDias.cancel();
                                                        }
                                                    });
                                                    dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(DialogInterface dialog) {
                                                            btnVerDias.setEnabled(true);
                                                            btnCanc.setEnabled(true);
                                                            menu.cargando(false);
                                                            touch(false);
                                                        }
                                                    });
                                                    dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                    if (mDosBtn.getParent() != null) {
                                                        ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                        mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoAdministrarDias.show();
                                                    } else {
                                                        dialogoAdministrarDias.show();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                    menu.cargando(true);
                    touch(true);
                    collectionDates.clear();
                    calendarPickerView.clearHighlightedDates();
                    final String[] diasSoliLista = Objects.requireNonNull(documentSnapshot.getString("Dias libres solicitados")).replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
                    for (String ds : diasSoliLista) {
                        if (ds != null) {
                            try {
                                Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ds);
                                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date hoyFech = fmt.getCalendar().getTime();
                                final String dateS = fmt.format(Objects.requireNonNull(date1));
                                if (date1.before(hoyFech)) {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(Objects.requireNonNull(documentSnapshot.getString("nombre"))).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(Objects.requireNonNull(documentSnapshot.getString("nombre"))).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                if (documentSnapshot.getString("nombre") == null) {
                                                                    mapD.put("nombre", nombre);
                                                                }
                                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(Objects.requireNonNull(documentSnapshot.getString("nombre"))).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(Objects.requireNonNull(documentSnapshot.getString("nombre"))).set(mapD, SetOptions.merge());
                                                                        menu.cargando(false);
                                                                        touch(false);
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
                        List<Task<QuerySnapshot>> tasks2 = new ArrayList<>();
                        menu.cargando(true);
                        touch(true);
                        for (QueryDocumentSnapshot doc : Objects.requireNonNull(task.getResult())) {
                            if (Objects.requireNonNull(doc.getString("nombre")).equals(nombre)) {
                                if (doc.getString("Dias libres") != null) {
                                    String diasnomot = Objects.requireNonNull(doc.getString("Dias libres")).replaceAll("B", "").replaceAll("O", "").replaceAll("V", "");
                                    final String[] contieneDiasString = diasnomot.split("\\s*;\\s*");
                                    for (String di : contieneDiasString) {
                                        try {
                                            contieneDias.add(formato.parse(di));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                            }
                        }

                        menu.cargando(false);
                        touch(false);
                        return Tasks.whenAllSuccess(tasks2);
                    }

                }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
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
                                public void onDateSelected(final Date date) {
                                    try {
                                        calendarPickerView2.clearSelectedDates();
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
                                            menu.cargando(true);
                                            touch(true);
                                            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                            final String fecha = formato.format(date);
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        String DLN = documentSnapshot.getString("Dias libres");
                                                        String DLNS = documentSnapshot.getString("Dias libres solicitados");
                                                        MODLN = null;
                                                        MODLN2 = null;
                                                        if (DLN == null || !DLN.contains(fecha)) {
                                                            if (DLNS == null || !DLNS.contains(fecha)) {
                                                                dDiasLibresSelecEmpl(date);
                                                            } else if (DLNS.contains(fecha)) {
                                                                final String[] listaMotivSoli = DLNS.split("\\s*;\\s*");
                                                                for (String l2 : listaMotivSoli) {
                                                                    if (l2.contains(fecha)) {
                                                                        if (l2.contains("V")) {
                                                                            MODLN2 = "vacaciones,";
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            dialogoYaExiste(fecha);
                                                                            break;
                                                                        } else if (l2.contains("B")) {
                                                                            MODLN2 = "baja laboral,";
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            dialogoYaExiste(fecha);
                                                                            break;
                                                                        } else if (l2.contains("O")) {
                                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                                                    String camb = fecha.replaceAll("/", "-");
                                                                                    MODLN2 = documentSnapshot2.getString(camb) + ",";
                                                                                    menu.cargando(false);
                                                                                    touch(false);
                                                                                    dialogoYaExiste(fecha);
                                                                                }
                                                                            });
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } else if (DLN.contains(fecha)) {
                                                            final String[] listaMotiv = DLN.split("\\s*;\\s*");
                                                            for (String l : listaMotiv) {
                                                                if (l.contains(fecha)) {
                                                                    menu.cargando(true);
                                                                    touch(true);
                                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                    final Button btnVerDias = mDosBtn.findViewById(R.id.btn1);
                                                                    btnVerDias.setText("Ver mis dias libres");
                                                                    final Button btnCanc = mDosBtn.findViewById(R.id.btn2);
                                                                    btnCanc.setText("Cerrar mensaje");
                                                                    final TextView titulo2 = new TextView(getActivity());
                                                                    titulo2.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                    final AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                                                                            .setView(mDosBtn);
                                                                    if (l.contains("O")) {
                                                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                                                String camb = fecha.replaceAll("/", "-");
                                                                                titulo2.setText("Ya tienes el dia " + fecha + " libre por " + documentSnapshot2.getString(camb));
                                                                                AdministrarDias.setCustomTitle(titulo2);
                                                                                final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                                                btnCanc.setOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        dialogoAdministrarDias.cancel();
                                                                                    }
                                                                                });
                                                                                btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        dialogoAdministrarDias.cancel();
                                                                                    }
                                                                                });
                                                                                dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                    @Override
                                                                                    public void onShow(DialogInterface dialog) {
                                                                                        btnVerDias.setEnabled(true);
                                                                                        btnCanc.setEnabled(true);
                                                                                        menu.cargando(false);
                                                                                        touch(false);
                                                                                    }
                                                                                });
                                                                                dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                                                if (mDosBtn.getParent() != null) {
                                                                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                    dialogoAdministrarDias.show();
                                                                                } else {
                                                                                    dialogoAdministrarDias.show();
                                                                                }
                                                                            }
                                                                        });
                                                                        break;
                                                                    } else if (l.contains("V")) {
                                                                        titulo2.setText("Ya tienes el dia " + fecha + " libre por vacaciones");
                                                                        AdministrarDias.setCustomTitle(titulo2);
                                                                        final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                                        btnCanc.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                dialogoAdministrarDias.cancel();
                                                                            }
                                                                        });
                                                                        btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                dialogoAdministrarDias.cancel();
                                                                            }
                                                                        });
                                                                        dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                            @Override
                                                                            public void onShow(DialogInterface dialog) {
                                                                                btnVerDias.setEnabled(true);
                                                                                btnCanc.setEnabled(true);
                                                                                menu.cargando(false);
                                                                                touch(false);
                                                                            }
                                                                        });
                                                                        dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                                        if (mDosBtn.getParent() != null) {
                                                                            ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                            mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                            dialogoAdministrarDias.show();
                                                                        } else {
                                                                            dialogoAdministrarDias.show();
                                                                        }
                                                                    } else if (l.contains("B")) {
                                                                        titulo2.setText("Ya tienes el dia " + fecha + " libre por baja laboral");
                                                                        AdministrarDias.setCustomTitle(titulo2);
                                                                        final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
                                                                        btnCanc.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                dialogoAdministrarDias.cancel();
                                                                            }
                                                                        });
                                                                        btnVerDias.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                dialogoAdministrarDias.cancel();
                                                                            }
                                                                        });
                                                                        dialogoAdministrarDias.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                            @Override
                                                                            public void onShow(DialogInterface dialog) {
                                                                                btnVerDias.setEnabled(true);
                                                                                btnCanc.setEnabled(true);
                                                                                menu.cargando(false);
                                                                                touch(false);
                                                                            }
                                                                        });
                                                                        dialogoAdministrarDias.setCanceledOnTouchOutside(false);
                                                                        if (mDosBtn.getParent() != null) {
                                                                            ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                            mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                            dialogoAdministrarDias.show();
                                                                        } else {
                                                                            dialogoAdministrarDias.show();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            });
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
                                    if (menu.getCambioDeFragment()) {
                                        actualizarCalendarioEmpl();

                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                        }
                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                        }
                                        menu.setCambioDeFragmento(false);
                                    } else {

                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                        }
                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                        }
                                    }
                                }
                            });

                            if (calendarPickerView2 != null) {
                                collectionDates2.clear();
                                calendarPickerView2.clearHighlightedDates();
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        menu.cargando(true);
                                        touch(true);
                                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                            if (Objects.requireNonNull(doc.getString("nombre")).equals(nombre)) {
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
                                        menu.cargando(false);
                                        touch(false);
                                    }
                                });
                                hayDias = true;
                            }
                        } else {
                            diasSolicTextV2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (menu.getCambioDeFragment()) {
                                        actualizarCalendarioEmpl();
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
                            if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
                                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }
                        }
                    }
                });
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
