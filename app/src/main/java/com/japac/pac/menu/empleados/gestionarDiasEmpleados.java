package com.japac.pac.menu.empleados;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

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
import com.google.android.material.snackbar.Snackbar;
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
public class gestionarDiasEmpleados extends Fragment {

    private FirebaseFirestore firebaseFirestore;

    private String id, empresa, nombre, otroMot, rol, hEntrada, hEntrada2, hSalida, hSalida2;

    private CalendarPickerView calendarPickerView, calendarPickerView2;

    private final ArrayList<Date> collectionDates = new ArrayList<>(31);
    private final ArrayList<Date> collectionDates2 = new ArrayList<>(31);

    private TextView diasSolicTextV2;

    private View mCambMot;
    private View mDosBtn;
    private View mJustificar;
    private View mRango;
    private View mHoras;

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
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
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
                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        xpand.setImageResource(R.drawable.ic_expand_up);
                    } else if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        xpand.setImageResource(R.drawable.ic_expand_down);
                    }
                    menu.setCambioDeFragmento(false);
                } else {

                    if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        xpand.setImageResource(R.drawable.ic_expand_up);
                    } else if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
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
                    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
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
        id = mAuth.getCurrentUser().getUid();
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        firebaseFirestore.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                empresa = documentSnapshot.getString("empresa");
                nombre = documentSnapshot.getString("nombre");
                firebaseFirestore.collection("Codigos").document(documentSnapshot.getString("codigo empresa")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        hEntrada = documentSnapshot.getString("hora de entrada");
                        hSalida = documentSnapshot.getString("hora de salida");
                        hEntrada2 = documentSnapshot.getString("hora de entrada partida");
                        hSalida2 = documentSnapshot.getString("hora de salida partida");
                    }
                });
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
                                if (documentSnapshot.exists()) {
                                    actualizarCalendarioEmpl();
                                    if (documentSnapshot.getBoolean("aceptado") != null && documentSnapshot.getBoolean("asignado") != null && documentSnapshot.getBoolean("rechazado") != null && documentSnapshot.getBoolean("eliminado") != null) {
                                        if (documentSnapshot.getBoolean("aceptado").equals(true)) {
                                            menu.snackbar.setText("Un día libre solicitado se ha aceptado");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("aceptado", false);
                                            hayDias = true;
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                        } else if (documentSnapshot.getBoolean("asignado").equals(true)) {
                                            menu.snackbar.setText("Se le ha asignado un día libre");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("asignado", false);
                                            hayDias = true;
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                                        } else if (documentSnapshot.getBoolean("rechazado").equals(true)) {
                                            menu.snackbar.setText("Un día libre solicitado se ha rechazado");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("rechazado", false);
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                        } else if (documentSnapshot.getBoolean("eliminado").equals(true)) {
                                            menu.snackbar.setText("Se le ha eliminado un día libre");
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).update("eliminado", false);
                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                        }
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(12);
                                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                        menu.snackbar.show();
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

    private void dialogoYaExiste(final String fecha, final String motivoAnt, String rango, final String fechaYmotivo) {
        menu.cargando(true);
        touch(true);
        final TextView titulo2 = new TextView(getActivity());
        if (rango != null) {
            final String[] horaRangDi = rango.split("\\s*;\\s*");
            titulo2.setText("Ya se ha solicitado el dia " + fecha + " libre desde las " + horaRangDi[0] +" hasta las " + horaRangDi[1] + " por " + motivoAnt + "\nEspere una respuesta de su administrador o edite la solicitud");
        } else {
            titulo2.setText("Ya se ha solicitado todo el dia " + fecha + " libre por " + motivoAnt + "\nEspere una respuesta de su administrador o edite la solicitud");
        }
        titulo2.setGravity(Gravity.CENTER_HORIZONTAL);
        View mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
        final Button btnElimS = mCuatroBtn.findViewById(R.id.btn1);
        btnElimS.setText("Eliminar solicitud");
        final Button btnCambM = mCuatroBtn.findViewById(R.id.btn2);
        btnCambM.setText("Cambiar motivo");
        final Button btnCambH = mCuatroBtn.findViewById(R.id.btn3);
        btnCambH.setText("Cambiar horas libres solicitadas");
        final Button btnCance = mCuatroBtn.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(getActivity())
                .setCustomTitle(titulo2)
                .setView(mCuatroBtn);
        final AlertDialog dialogoAdministrarDias = AdministrarDias.create();
        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministrarDias.cancel();
            }
        });
        btnCambH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                menu.cargando(true);
                touch(true);
                final TextView myMsgtitle = new TextView(getActivity());
                myMsgtitle.setText("¿Desea todo el dia libre o seleccionar un rango de horas?");
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
                        if(rango!=null){
                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(fechaYmotivo, FieldValue.delete());
                        }
                        dialogoRango.dismiss();
                    }
                });
                btnHoras.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        horasSelec(fecha, motivoAnt, true);
                        dialogoRango.dismiss();

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
                        dialogoAdministrarDias.cancel();
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
        btnCambM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                dialogoAdministrarDias.cancel();
                final TextView titulo = new TextView(getActivity());
                titulo.setGravity(Gravity.CENTER_HORIZONTAL);
                titulo.setText("Seleccione el nuevo motivo para tener el dia " + fecha + " libre");
                mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
                final Button btnVaca = mCambMot.findViewById(R.id.btn1);
                final Button btnBaja = mCambMot.findViewById(R.id.btn2);
                final Button btnOtros = mCambMot.findViewById(R.id.btn3);
                final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
                AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getActivity())
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
                    mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
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
                btnCambH.setEnabled(true);
                btnCance.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoAdministrarDias.setCanceledOnTouchOutside(false);
        if (mCuatroBtn.getParent() != null) {
            ((ViewGroup) mCuatroBtn.getParent()).removeView(mCuatroBtn);
            mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
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
        mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
        final Button btnVaca = mCambMot.findViewById(R.id.btn1);
        final Button btnBaja = mCambMot.findViewById(R.id.btn2);
        final Button btnOtros = mCambMot.findViewById(R.id.btn3);
        final Button btnCance = mCambMot.findViewById(R.id.Cancelar);
        AlertDialog.Builder AdministrarDiasLibres = new AlertDialog.Builder(getActivity())
                .setCustomTitle(titulo)
                .setView(mCambMot);
        final AlertDialog dialogoAdministrarDiasLibres = AdministrarDiasLibres.create();
        btnVaca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rango(datefull, "V");
                dialogoAdministrarDiasLibres.dismiss();
            }
        });
        btnBaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rango(datefull, "B");
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
                final AlertDialog.Builder Login = new AlertDialog.Builder(getActivity());
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
                            rango(datefull, "O");
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
            mCambMot = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
            dialogoAdministrarDiasLibres.show();
        } else {
            dialogoAdministrarDiasLibres.show();
        }
    }

    private void rango(final String fecha1, final String mot) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿Desea todo el dia libre o seleccionar un rango de horas?");
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
                anadeEmplSolis(fecha1, mot);
            }
        });
        btnHoras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRango.dismiss();
                horasSelec(fecha1, mot, false);
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

    private void horasSelec(final String fecha2, final String mot, final Boolean cambio) {
        menu.cargando(true);
        touch(true);
        final int[] horaEntOSal = {0};
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombre + " selecciones a que hora desea comenzar a librar el dia " + fecha2);
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
                    myMsgtitle.setText(nombre + " ahora seleccione a que hora terminara de librar el dia " + fecha2);
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
                                String motivoFinal = null;
                                if (mot.equals("V")) {
                                    motivoFinal = "V";
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por vacaciones");
                                } else if (mot.equals("B")) {
                                    motivoFinal = "B";
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por baja laboral");
                                } else if (mot.equals("O")) {
                                    motivoFinal = "O";
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por " + otroMot);
                                }else if(mot.equals("vacaciones")){
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por vacaciones");
                                    motivoFinal = "V";
                                }else if(mot.equals("baja laboral")){
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por baja laboral");
                                    motivoFinal = "B";
                                }else{
                                    myMsgtitle.setText(nombre + " ¿desea solicitar el siguiente dia libre?\n Dia " + fecha2 + " por " + mot);
                                    motivoFinal = "O";
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
                                final String finalMotivoFinal = motivoFinal;
                                btnConf.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        menu.cargando(true);
                                        touch(true);
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(fecha2.replaceAll("/", "-") + finalMotivoFinal, entradaDef + ";" + salidaDef).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                menu.cargando(false);
                                                touch(false);
                                                dialogoAlerta3.dismiss();
                                                if(!cambio){
                                                    anadeEmplSolis(fecha2, finalMotivoFinal);
                                                }
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
                            horasSelec(fecha2, mot, cambio);
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
                rango(fecha2, mot);
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

    private void anadeEmplSolis(final String date, final String mot) {
        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                    String textoToast = null;
                    final Map<String, Object> map2 = new HashMap<>();
                    final String diasLibresSoli = documentSnapshot.getString("Dias libres solicitados");
                    String insertar = null;
                    if (mot.equals("ELIMINAR")) {
                        if (diasLibresSoli.contains(date)) {
                            String diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "V");
                            if (diaRangoElim == null) {
                                diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "B");
                                if (diaRangoElim == null) {
                                    diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "O");
                                    if (diaRangoElim != null) {
                                        map2.put(date.replaceAll("/", "-") + "O", FieldValue.delete());
                                    }
                                } else {
                                    map2.put(date.replaceAll("/", "-") + "B", FieldValue.delete());
                                }
                            } else {
                                map2.put(date.replaceAll("/", "-") + "V", FieldValue.delete());
                            }
                            insertar = diasLibresSoli.replace(date + "V;", "").replace(date + "B;", "").replace(date + "O;", "");
                            if (insertar.equals("")) {
                                map2.put("Dias libres solicitados", null);
                            } else {
                                map2.put("Dias libres solicitados", insertar);
                            }
                            textoToast = "La solicitud para tener el dia " + date + " libre se ha eliminado";
                        }
                        if (documentSnapshot.getString(date.replaceAll("/", "-")) != null) {
                            map2.put(date.replaceAll("/", "-"), FieldValue.delete());
                        }
                    } else {
                        if (diasLibresSoli.contains(date)) {
                            String diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "V");
                            if (diaRangoElim == null) {
                                diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "B");
                                if (diaRangoElim == null) {
                                    diaRangoElim = documentSnapshot.getString(date.replaceAll("/", "-") + "O");
                                    if (diaRangoElim != null) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + mot, diaRangoElim).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + "O", FieldValue.delete());
                                            }
                                        });
                                    }
                                } else {
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + mot, diaRangoElim).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + "B", FieldValue.delete());
                                        }
                                    });
                                }
                            } else {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + mot, diaRangoElim).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-") + "V", FieldValue.delete());
                                    }
                                });
                            }
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
                            if (!mot.equals("O") && documentSnapshot.getString(date.replaceAll("/", "-")) != null) {
                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date.replaceAll("/", "-"), FieldValue.delete());
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
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date3, otroMot);
                                    }
                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            map2.put("solicita", true);
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).set(map2, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    menu.snackbar.setText(finalTextoToast);
                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(10);
                                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                    menu.snackbar.show();
                                                    if (menu.getCambioDeFragment()) {
                                                        actualizarCalendarioEmpl();
                                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                        }

                                                        menu.setCambioDeFragmento(false);
                                                    } else {
                                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
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
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).update(date3, otroMot);
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
                                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
                                                            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                                        }

                                                        menu.setCambioDeFragmento(false);
                                                    } else {
                                                        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED) {
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
                    if (new DateTime(date1).getDayOfYear() >= new DateTime(currentTime).getDayOfYear()
                            || new DateTime(date1).getYear() > new DateTime(currentTime).getYear()) {
                        menu.cargando(true);
                        touch(true);
                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        final String fecha = formato.format(date);
                        firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String DLN = documentSnapshot.getString("Dias libres");
                                    String DLNS = documentSnapshot.getString("Dias libres solicitados");
                                    if (DLN == null || !DLN.contains(fecha)) {
                                        if (DLNS == null || !DLNS.contains(fecha)) {
                                            dDiasLibresSelecEmpl(date);
                                        } else if (DLNS.contains(fecha)) {
                                            final String[] listaMotivSoli = DLNS.split("\\s*;\\s*");
                                            for (final String l2 : listaMotivSoli) {
                                                if (l2.contains(fecha)) {
                                                    Log.d("l2", l2);
                                                    final String rang = documentSnapshot.getString(l2.replaceAll("/", "-"));
                                                    if (l2.contains("V")) {
                                                        dialogoYaExiste(fecha, "vacaciones", rang, l2.replaceAll("/","-"));
                                                        menu.cargando(false);
                                                        touch(false);
                                                        break;
                                                    } else if (l2.contains("B")) {
                                                        menu.cargando(false);
                                                        touch(false);
                                                        dialogoYaExiste(fecha, "baja laboral", rang, l2.replaceAll("/","-"));
                                                        break;

                                                    } else if (l2.contains("O")) {
                                                        String camb = fecha.replaceAll("/", "-");
                                                        menu.cargando(false);
                                                        touch(false);
                                                        dialogoYaExiste(fecha, documentSnapshot.getString(camb), rang, l2.replaceAll("/","-"));
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
                                                final AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(getActivity())
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
                    final String[] diasSoliLista = documentSnapshot.getString("Dias libres solicitados").replaceAll("V", "").replaceAll("B", "").replaceAll("O", "").split("\\s*;\\s*");
                    for (String ds : diasSoliLista) {
                        if (ds != null) {
                            try {
                                final Date date1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ds);
                                Date currentTime = Calendar.getInstance().getTime();
                                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                final String dateS = fmt.format(date1);
                                if ((date1.before(currentTime))) {
                                    if (new DateTime(date1).getMonthOfYear() == new DateTime(currentTime).getMonthOfYear()
                                            && new DateTime(date1).getMonthOfYear() == new DateTime(currentTime).getMonthOfYear()
                                            && new DateTime(date1).getDayOfMonth() == new DateTime(currentTime).getDayOfMonth()) {
                                        collectionDates.add(date1);
                                        calendarPickerView.highlightDates(collectionDates);
                                    } else {
                                        firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(documentSnapshot.getString("nombre")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.getString("Dias libres solicitados") != null) {
                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(documentSnapshot.getString("nombre")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            DecimalFormat mFormat = new DecimalFormat("00");
                                                            if (documentSnapshot.getString(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear()) != null) {
                                                                firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(documentSnapshot.getString("nombre")).update(mFormat.format(new DateTime(date1).getDayOfMonth()) + "-" + mFormat.format(new DateTime(date1).getMonthOfYear()) + "-" + new DateTime(date1).getYear(), FieldValue.delete());
                                                            }

                                                        }
                                                    });
                                                    String tip = documentSnapshot.getString("Dias libres solicitados");
                                                    String fin = null;
                                                    final Map<String, String> mapD = new HashMap<>();
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
                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Empleado").document(documentSnapshot.getString("nombre")).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    if (documentSnapshot.getString("nombre") == null) {
                                                                        mapD.put("nombre", nombre);
                                                                    }
                                                                    firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(documentSnapshot.getString("nombre")).set(mapD, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres").document(documentSnapshot.getString("nombre")).set(mapD, SetOptions.merge());
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
                        List<Task<QuerySnapshot>> tasks2 = new ArrayList<>();
                        menu.cargando(true);
                        touch(true);
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            if (doc.getString("nombre").equals(nombre)) {
                                if (doc.getString("Dias libres") != null) {
                                    String diasnomot = doc.getString("Dias libres").replaceAll("B", "").replaceAll("O", "").replaceAll("V", "");
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
                                        if (new DateTime(date1).getDayOfYear() >= new DateTime(currentTime).getDayOfYear()
                                                || new DateTime(date1).getYear() > new DateTime(currentTime).getYear()) {
                                            menu.cargando(true);
                                            touch(true);
                                            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                            final String fecha = formato.format(date);
                                            firebaseFirestore.collection("Empresas").document(empresa).collection("Dias libres solicitados").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.exists()) {
                                                        String DLN = documentSnapshot.getString("Dias libres");
                                                        String DLNS = documentSnapshot.getString("Dias libres solicitados");
                                                        if (DLN == null || !DLN.contains(fecha)) {
                                                            if (DLNS == null || !DLNS.contains(fecha)) {
                                                                dDiasLibresSelecEmpl(date);
                                                            } else if (DLNS.contains(fecha)) {
                                                                final String[] listaMotivSoli = DLNS.split("\\s*;\\s*");
                                                                for (final String l2 : listaMotivSoli) {
                                                                    if (l2.contains(fecha)) {
                                                                        Log.d("l2", l2);
                                                                        final String rang = documentSnapshot.getString(l2.replaceAll("/", "-"));
                                                                        if (l2.contains("V")) {
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            dialogoYaExiste(fecha, "vacaciones",rang, l2.replaceAll("/","-"));
                                                                            break;
                                                                        } else if (l2.contains("B")) {
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            dialogoYaExiste(fecha, "baja laboral", rang, l2.replaceAll("/","-"));
                                                                            break;
                                                                        } else if (l2.contains("O")) {
                                                                            String camb = fecha.replaceAll("/", "-");
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            dialogoYaExiste(fecha, documentSnapshot.getString(camb), rang, l2.replaceAll("/","-"));
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
                                                                    final AlertDialog.Builder AdministrarDias = new AlertDialog.Builder(getActivity())
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
                                            if (doc.getString("nombre").equals(nombre)) {
                                                if (doc.getString("Dias libres") != null) {
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
                            xpand.setVisibility(View.INVISIBLE);
                            xpand.setClickable(false);
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
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

}
