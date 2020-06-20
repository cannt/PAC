package com.japac.pac.menu.empleados;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.auth.login;
import com.japac.pac.localizacion.localizacionUsuario;
import com.japac.pac.marcadores.marcadoresObras;
import com.japac.pac.menu.menu;
import com.japac.pac.R;
import com.japac.pac.adaptadorObrasLista;
import com.japac.pac.servicios.servicioLocalizacion;
import com.japac.pac.servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.view.View.VISIBLE;
import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */

public class mapaEmpleados extends Fragment implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        menu.snackbar.setText("Mapa listo");
        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextSize(10);
        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
        menu.snackbar.show();
        mMap = googleMap;

        if (compruebapermisos()) {
            detalles();

            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.setIndoorEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.estilo)));
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (pulseMap == 0) {
                        pulseMap = 1;
                        menu.snackbar.setText("Vuelva a tocar el mapa para actualizar la información");
                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(10);
                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                        menu.snackbar.show();
                        timerMap = new CountDownTimer(5000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                if (pulseMap == 0) {
                                    menu.snackbar.dismiss();
                                    mMap.clear();
                                    firestoreNombres();
                                    firestoreObras();
                                    menu.snackbar.setText("Actualizando informacion del mapa");
                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setTextSize(10);
                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                    menu.snackbar.show();
                                } else {
                                    pulseMap = 0;
                                }
                            }
                        }.start();
                    } else if (pulseMap == 1) {
                        pulseMap = 0;
                        timerMap.cancel();
                        timerMap.onFinish();
                    }
                }
            });
            listenerObs();
            listenerJfs();
            init();

        }
    }

    private int pulseMap = 0;

    private final Map<String, Marker> markersMapObra = new HashMap<>();
    private final Map<GeoPoint, String> markersMapObras2 = new HashMap<>();

    private final Map<String, Object> map = new HashMap<>();

    private CountDownTimer timerObs, timerJfs, timerLeergeo, timerSnackLocaliza, timerMap;

    private String trayecto;
    private String obraselect;
    private String id;
    private String codigoEmpresa;
    private String comp;
    private String empresa;
    private String roles;
    private String nombre;
    private String nombreNu;
    private String emailAn;
    private String emailConf;
    private String codigoEmpleado;
    private String obcomprueba;
    private String busquedaString;
    private String entrada_salida;
    private String snackbarLocalizando;
    private String hEntrada;
    private String hEntrada2;
    private String hSalida;
    private String hSalida2;
    private String diasVaca;
    private String diasVacaRango;
    private String mislisT;
    private String marca;
    private Date hEntradaDate = null;
    private Date hEntrada2Date = null;
    private Date hSalidaDate = null;
    private Date hSalida2Date = null;
    private FirebaseAuth mAuth;
    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private ArrayList<String> diasLibresSemana;

    private static final int ERROR_DIALOGO_PEDIR = 9001;
    private static final float ZOOM_PREDETERMINADO = 20f;

    private ArrayList<String> obs = new ArrayList<>(), jfs = new ArrayList<>(), emailList;

    private FloatingActionButton icInicio, gps, icAyuda;

    private View mNombres;
    private View mDos;
    private View mLogin;
    private View mFueraObra;
    private View viewGreyNormal;
    private View viewGreyExpande;
    private View mFirmar;

    private ImageView alertaAyuda;

    private EditText mBuscar;

    private TextView pPt;
    private TextView ayudaGeneral;
    private TextView ayudaPolitica;
    private TextView ayudaIcJornada;
    private TextView ayudaIcGps;
    private TextView ayudaExpand;
    private TextView ayudaObras;
    private TextView ayudaSalir;

    private TextView tituloFueraJornada;

    private Animation anim;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizayo;

    private localizacionUsuario mLocalizarUsuario;

    private FirebaseFirestore mDb;
    private FirebaseStorage almacen;

    private LatLng mLocaliza;

    private boolean trayectoBo = false, otro = false, readyObs = false, readyJfs = false, alreadyObs = false, alreadyJfs = false, ayuda = false;

    private ArrayAdapter<String> jefeAdapter;

    private Spinner jefeSpinner, obraSpinner;

    private Double latitudDetectada, longitudDetectada, latitudGuardada, longitudGuardada, distan, distan2 = 1.0, dis;

    private SlidingUpPanelLayout slidingLayout2;

    private ImageView xpand2;

    private adaptadorObrasLista adaptadorObrasLista;

    private CollectionReference geoFirestoreRefObs;
    private CollectionReference geoFirestoreRefJfs;

    private int mayor = 0;

    private SimpleDateFormat formatJor = new SimpleDateFormat("HH:mm");

    public mapaEmpleados() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_mapa_empleados, container, false);
        if (compruebapermisos() && isServicesOK()) {
            mAuth = FirebaseAuth.getInstance();
            id = mAuth.getCurrentUser().getUid();
            mDb = FirebaseFirestore.getInstance();
            almacen = FirebaseStorage.getInstance();
            mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        codigoEmpresa = documentSnapshot.getString("codigo empresa");
                        mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                diasLibresSemana = new ArrayList<>();
                                diasLibresSemana.clear();
                                diasLibresSemana = (ArrayList<String>) documentSnapshot.get("dias libres semana");
                                hEntrada = documentSnapshot.getString("hora de entrada");
                                if (hEntrada != null) {
                                    try {
                                        hEntradaDate = formatJor.parse(hEntrada);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                hSalida = documentSnapshot.getString("hora de salida");
                                if (hSalida != null) {
                                    try {
                                        hSalidaDate = formatJor.parse(hSalida);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                hEntrada2 = documentSnapshot.getString("hora de entrada partida");
                                if (hEntrada2 != null) {
                                    try {
                                        hEntrada2Date = formatJor.parse(hEntrada2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                hSalida2 = documentSnapshot.getString("hora de salida partida");
                                if (hSalida2 != null) {
                                    try {
                                        hSalida2Date = formatJor.parse(hSalida2);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                try {
                                    Log.d("hEntrada", hEntrada);
                                    Log.d("hSalida", hSalida);
                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                    Date entradaDate = format.parse(hEntrada);
                                    Date salidaDate = format.parse(hSalida);
                                    Log.d("entradaDate", entradaDate.toString());
                                    Log.d("salidaDate", salidaDate.toString());
                                    long milis = salidaDate.getTime() - entradaDate.getTime();
                                    Log.d("hours", String.valueOf(milis));
                                    mislisT = String.valueOf(milis);
                                    Log.d("mislisT", mislisT);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        marca = documentSnapshot.getString("hora marcada");
                        diasVaca = documentSnapshot.getString("Dias libres");
                        comp = documentSnapshot.getString("comprobar");
                        empresa = documentSnapshot.getString("empresa");
                        nombre = documentSnapshot.getString("nombre");
                        nombreNu = nombre;
                        roles = documentSnapshot.getString("rol");
                        emailAn = documentSnapshot.getString("email");
                        codigoEmpleado = documentSnapshot.getString("codigo empleado");
                        if (documentSnapshot.contains("obra")) {
                            obcomprueba = documentSnapshot.getString("obra");
                        }
                        slidingLayout2 = getView().findViewById(R.id.sliding_layout2);
                        slidingLayout2.setTouchEnabled(false);
                        xpand2 = getView().findViewById(R.id.btnXpand2);
                        mBuscar = getView().findViewById(R.id.input_buscar);
                        xpand2.setOnClickListener(mapaEmpleados.this);
                        slidingLayout2.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                            @Override
                            public void onPanelSlide(View panel, float slideOffset) {

                            }

                            @Override
                            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                                if (previousState.equals(SlidingUpPanelLayout.PanelState.EXPANDED) && newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                    if (menu.getCambioDeFragment()) {
                                        setUpRecyclerView();
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                                        menu.setCambioDeFragmento(false);
                                    } else {
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                                    }
                                }
                            }
                        });
                        gps = getView().findViewById(R.id.ic_gps);
                        icInicio = getView().findViewById(R.id.ic_inicia);
                        icAyuda = getView().findViewById(R.id.ic_ayuda);
                        if (comp.equals("iniciada")) {
                            icInicio.setImageResource(R.drawable.ic_finalizar_jornada);
                        } else if (comp.equals("finalizada") || comp.equals("no")) {
                            icInicio.setImageResource(R.drawable.ic_inicio_jornada);
                        }
                        pPt = getView().findViewById(R.id.PrivacyPolicy);
                        pPt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        });
                        setUpRecyclerView();
                        iniciarMapa();
                    }
                }
            });
        }
        return RootView;
    }

    private void actualizaDatos() {
        menu.cargando(true);
        touch(true);
        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    comp = documentSnapshot.getString("comprobar");
                    empresa = documentSnapshot.getString("empresa");
                    nombre = documentSnapshot.getString("nombre");
                    nombreNu = nombre;
                    roles = documentSnapshot.getString("rol");
                    emailAn = documentSnapshot.getString("email");
                    codigoEmpleado = documentSnapshot.getString("codigo empleado");
                    if (documentSnapshot.contains("obra")) {
                        obcomprueba = documentSnapshot.getString("obra");
                    }
                    if (comp.equals("iniciada")) {
                        icInicio.setImageResource(R.drawable.ic_finalizar_jornada);
                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                        icInicio.setImageResource(R.drawable.ic_inicio_jornada);
                    }
                    firestoreNombres();
                    firestoreObras();
                    menu.cargando(false);
                    touch(false);
                }
            }
        });
    }

    private void dQuien(final Boolean obraBolean, final String obraMarker) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿Quien va a registrar la jornada?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        View mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button botonYo = mTres.findViewById(R.id.btn2);
        botonYo.setTextSize(15);
        botonYo.setText(nombre);
        final Button botonOtro = mTres.findViewById(R.id.btn1);
        botonOtro.setTextSize(15);
        botonOtro.setText("Empleado invitado");
        final Button botonCancelar = mTres.findViewById(R.id.Cancelar);
        botonCancelar.setTextSize(15);
        final AlertDialog.Builder Quien = new AlertDialog.Builder(getContext());
        Quien
                .setCustomTitle(myMsgtitle)
                .setView(mTres);
        final AlertDialog dialogoQuien = Quien.create();
        botonYo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoQuien.dismiss();
                if (obraBolean) {
                    menu.cargando(true);
                    touch(true);
                    final TextView myMsgtitle = new TextView(getActivity());
                    mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                    final Button botonJornada = mDos.findViewById(R.id.btn1);
                    final Button botonCancelar = mDos.findViewById(R.id.btn2);
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    myMsgtitle.setTextColor(Color.BLACK);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    myMsgtitle.setLayoutParams(params);

                    String obFin = null;
                    if (obcomprueba != null) {
                        if (obcomprueba.equals(obraMarker)) {
                            myMsgtitle.setText(nombre + " ¿desea finalizar la jornada en la obra " + obraMarker + "?");
                            obFin = obraMarker;
                        } else {
                            myMsgtitle.setText("Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero");
                            obFin = obcomprueba;
                        }
                        botonJornada.setText("Finalizar");
                    } else {
                        myMsgtitle.setText(nombre + " ¿desea iniciar la jornada en la obra " + obraMarker + "?");
                        obFin = obraMarker;
                        botonJornada.setText("Iniciar");
                    }
                    final AlertDialog.Builder Yo = new AlertDialog.Builder(getContext());
                    Yo
                            .setCustomTitle(myMsgtitle)
                            .setView(mDos);
                    final AlertDialog dialogoYo = Yo.create();
                    final String finalObFin = obFin;
                    botonJornada.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogoYo.dismiss();
                            if (comp.equals("iniciada")) {
                                entrada_salida = "Salida";
                            } else if (comp.equals("finalizada") || comp.equals("no")) {
                                entrada_salida = "Entrada";
                            }
                            if (Jornada()) {
                                leerGeo(finalObFin);
                            } else {
                                menu.cargando(true);
                                touch(true);
                                final View[] mDosBtn = {getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null)};
                                final Button btnCont = mDosBtn[0].findViewById(R.id.btn1);
                                btnCont.setText("Continuar");
                                final Button btnSal = mDosBtn[0].findViewById(R.id.btn2);
                                btnSal.setText("Cancelar y salir");
                                final AlertDialog.Builder fuera = new AlertDialog.Builder(getContext())
                                        .setCustomTitle(tituloFueraJornada)
                                        .setView(mDosBtn[0]);
                                final AlertDialog dialogoFuera = fuera.create();
                                btnCont.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        leerGeo(finalObFin);
                                    }
                                });
                                btnSal.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        menu.getInstance().finish();
                                        System.exit(0);
                                    }
                                });
                                dialogoFuera.setCanceledOnTouchOutside(false);
                                dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        btnCont.setEnabled(true);
                                        btnSal.setEnabled(true);
                                        menu.cargando(false);
                                        touch(false);
                                    }
                                });
                                final String[] horasDV = {null};
                                if (diasVacaRango != null) {
                                    mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String docu = diasVacaRango.replaceAll("/", "-").trim();
                                            horasDV[0] = documentSnapshot.getString(docu);
                                            if (horasDV[0] != null) {
                                                final String[] horaRangDi = horasDV[0].split("\\s*;\\s*");
                                                String[] horaRangEn = horaRangDi[0].split("\\s*:\\s*");
                                                String[] horaRangSa = horaRangDi[1].split("\\s*:\\s*");
                                                final Calendar calAhora = Calendar.getInstance();
                                                Calendar calEntradaDia = Calendar.getInstance();
                                                calEntradaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangEn[0]));
                                                calEntradaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangEn[1]));
                                                calEntradaDia.set(Calendar.SECOND, 00);
                                                Calendar calSalidaDia = Calendar.getInstance();
                                                calSalidaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangSa[0]));
                                                calSalidaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangSa[1]));
                                                calSalidaDia.set(Calendar.SECOND, 00);
                                                if (calAhora.getTime().after(calEntradaDia.getTime()) && calAhora.getTime().before(calSalidaDia.getTime())) {
                                                    if (diasVacaRango.contains("V")) {
                                                        tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("V", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por vacaciones\n¿Desea registrar la jornada de todas formas?");
                                                        fuera.setCustomTitle(tituloFueraJornada);
                                                        if (mDosBtn[0].getParent() != null) {
                                                            ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                            mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                            dialogoFuera.show();
                                                        } else {
                                                            dialogoFuera.show();
                                                        }

                                                    } else if (diasVacaRango.contains("B")) {
                                                        tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("B", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por baja laboral\n¿Desea registrar la jornada de todas formas?");
                                                        fuera.setCustomTitle(tituloFueraJornada);
                                                        if (mDosBtn[0].getParent() != null) {
                                                            ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                            mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                            dialogoFuera.show();
                                                        } else {
                                                            dialogoFuera.show();
                                                        }

                                                    } else if (diasVacaRango.contains("O")) {
                                                        mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                String d = diasVacaRango.replace("O", "").replaceAll("/", "-");
                                                                tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("O", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por " + documentSnapshot.getString(d) + "\n¿Desea registrar la jornada de todas formas?");
                                                                fuera.setCustomTitle(tituloFueraJornada);
                                                                if (mDosBtn[0].getParent() != null) {
                                                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                    dialogoFuera.show();
                                                                } else {
                                                                    dialogoFuera.show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    menu.cargando(false);
                                                    touch(false);
                                                    leerGeo(finalObFin);
                                                }
                                            } else {
                                                tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                                if (mDosBtn[0].getParent() != null) {
                                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                    dialogoFuera.show();
                                                } else {
                                                    dialogoFuera.show();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                    if (mDosBtn[0].getParent() != null) {
                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                        dialogoFuera.show();
                                    } else {
                                        dialogoFuera.show();
                                    }
                                }
                            }
                        }
                    });
                    botonCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (otro) {
                                dConfirma();
                            }
                            dialogoYo.dismiss();
                        }
                    });
                    dialogoYo.setCanceledOnTouchOutside(false);
                    dialogoYo.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            botonJornada.setEnabled(true);
                            botonCancelar.setEnabled(true);
                            menu.cargando(false);
                            touch(false);
                        }
                    });
                    dialogoYo.setCanceledOnTouchOutside(false);
                    if (mDos.getParent() != null) {
                        ((ViewGroup) mDos.getParent()).removeView(mDos);
                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                        dialogoYo.show();
                    } else {
                        dialogoYo.show();
                    }
                } else {
                    dRegistrar();
                }
            }
        });
        botonOtro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoQuien.dismiss();
                if (obraBolean) {
                    if (comp.equals("iniciada")) {
                        entrada_salida = "Salida";
                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                        entrada_salida = "Entrada";
                    }
                    dLogin(true, obraMarker);
                } else {
                    dLogin(false, null);
                }

            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoQuien.dismiss();
            }
        });
        dialogoQuien.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                botonYo.setEnabled(true);
                botonOtro.setEnabled(true);
                botonCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoQuien.setCanceledOnTouchOutside(false);
        if (mTres.getParent() != null) {
            ((ViewGroup) mTres.getParent()).removeView(mTres);
            mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoQuien.show();
        } else {
            dialogoQuien.show();
        }

    }

    private int leeObras(Spinner spinner, String obraselecionada) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(obraselecionada)) {
                return i;
            }
        }
        return 0;
    }

    private void dRegistrar() {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Seleccione la obra");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        View mObras = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        obraSpinner = mObras.findViewById(R.id.spinnerObra);
        ArrayAdapter<String> obraAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, obs);
        obraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        obraSpinner.setAdapter(obraAdapter);
        final Button botonJornada = mObras.findViewById(R.id.btn1);
        final Button botonCancelar = mObras.findViewById(R.id.btn2);
        obraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                obraselect = adapterView.getItemAtPosition(i).toString();
                if (obraselect.equals(obcomprueba)) {
                    botonJornada.setText("Finalizar");
                } else {
                    botonJornada.setText("Iniciar");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (comp.equals("iniciada")) {
                    obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                    obraselect = adapterView.getItemAtPosition(leeObras(obraSpinner, obcomprueba)).toString();
                } else if (comp.equals("finalizada") || comp.equals("no")) {
                    obraSpinner.setSelection(0);
                    obraselect = adapterView.getItemAtPosition(0).toString();
                }

            }
        });
        final AlertDialog.Builder registroJornada = new AlertDialog.Builder(getContext());
        registroJornada.setCustomTitle(myMsgtitle);
        registroJornada.setView(mObras);
        final AlertDialog dialogoRegistro = registroJornada.create();
        botonJornada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRegistro.dismiss();
                if (botonJornada.getText().equals("Iniciar")) {
                    if (comp.equals("iniciada")) {
                        menu.snackbar.setText("Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero");
                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(10);
                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                        menu.snackbar.show();
                        botonJornada.setText("Finalizar");
                        botonJornada.setTextColor(Color.RED);
                        obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                        entrada_salida = "Entrada";
                        if (Jornada()) {
                            leerGeo(obraselect);
                        } else {
                            menu.cargando(true);
                            touch(true);
                            final View[] mDosBtn = {getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null)};
                            final Button btnCont = mDosBtn[0].findViewById(R.id.btn1);
                            btnCont.setText("Continuar");
                            final Button btnSal = mDosBtn[0].findViewById(R.id.btn2);
                            btnSal.setText("Cancelar y salir");
                            final AlertDialog.Builder fuera = new AlertDialog.Builder(getContext())
                                    .setCustomTitle(tituloFueraJornada)
                                    .setView(mDosBtn[0]);
                            final AlertDialog dialogoFuera = fuera.create();
                            btnCont.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoFuera.dismiss();
                                    leerGeo(obraselect);
                                }
                            });
                            btnSal.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoFuera.dismiss();
                                    menu.getInstance().finish();
                                    System.exit(0);
                                }
                            });
                            dialogoFuera.setCanceledOnTouchOutside(false);
                            dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCont.setEnabled(true);
                                    btnSal.setEnabled(true);
                                    menu.cargando(false);
                                    touch(false);
                                }
                            });
                            final String[] horasDV = {null};
                            if (diasVacaRango != null) {
                                mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String docu = diasVacaRango.replaceAll("/", "-").trim();
                                        horasDV[0] = documentSnapshot.getString(docu);
                                        if (horasDV[0] != null) {
                                            final String[] horaRangDi = horasDV[0].split("\\s*;\\s*");
                                            String[] horaRangEn = horaRangDi[0].split("\\s*:\\s*");
                                            String[] horaRangSa = horaRangDi[1].split("\\s*:\\s*");
                                            final Calendar calAhora = Calendar.getInstance();
                                            Calendar calEntradaDia = Calendar.getInstance();
                                            calEntradaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangEn[0]));
                                            calEntradaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangEn[1]));
                                            calEntradaDia.set(Calendar.SECOND, 00);
                                            Calendar calSalidaDia = Calendar.getInstance();
                                            calSalidaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangSa[0]));
                                            calSalidaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangSa[1]));
                                            calSalidaDia.set(Calendar.SECOND, 00);
                                            if (calAhora.getTime().after(calEntradaDia.getTime()) && calAhora.getTime().before(calSalidaDia.getTime())) {
                                                if (diasVacaRango.contains("V")) {
                                                    tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("V", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por vacaciones\n¿Desea registrar la jornada de todas formas?");
                                                    fuera.setCustomTitle(tituloFueraJornada);
                                                    if (mDosBtn[0].getParent() != null) {
                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoFuera.show();
                                                    } else {
                                                        dialogoFuera.show();
                                                    }

                                                } else if (diasVacaRango.contains("B")) {
                                                    tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("B", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por baja laboral\n¿Desea registrar la jornada de todas formas?");
                                                    fuera.setCustomTitle(tituloFueraJornada);
                                                    if (mDosBtn[0].getParent() != null) {
                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoFuera.show();
                                                    } else {
                                                        dialogoFuera.show();
                                                    }

                                                } else if (diasVacaRango.contains("O")) {
                                                    mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            String d = diasVacaRango.replace("O", "").replaceAll("/", "-");
                                                            tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("O", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por " + documentSnapshot.getString(d) + "\n¿Desea registrar la jornada de todas formas?");
                                                            fuera.setCustomTitle(tituloFueraJornada);
                                                            if (mDosBtn[0].getParent() != null) {
                                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                dialogoFuera.show();
                                                            } else {
                                                                dialogoFuera.show();
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                menu.cargando(false);
                                                touch(false);
                                                leerGeo(obraselect);
                                            }
                                        } else {
                                            tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                            if (mDosBtn[0].getParent() != null) {
                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                dialogoFuera.show();
                                            } else {
                                                dialogoFuera.show();
                                            }
                                        }
                                    }
                                });
                            } else {
                                tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                if (mDosBtn[0].getParent() != null) {
                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                    dialogoFuera.show();
                                } else {
                                    dialogoFuera.show();
                                }
                            }
                        }
                    }
                } else if (botonJornada.getText().equals("Finalizar")) {
                    if (comp.contentEquals("finalizada") || comp.contentEquals("no")) {
                        menu.snackbar.setText("Debes iniciar primero una jornada");
                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(10);
                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                        menu.snackbar.show();
                        botonJornada.setText("Iniciar");
                        botonJornada.setTextColor(Color.RED);
                    } else if (comp.contentEquals("iniciada")) {
                        entrada_salida = "Salida";
                        if (Jornada()) {
                            leerGeo(obraselect);
                        } else {
                            menu.cargando(true);
                            touch(true);
                            final View[] mDosBtn = {getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null)};
                            final Button btnCont = mDosBtn[0].findViewById(R.id.btn1);
                            btnCont.setText("Continuar");
                            final Button btnSal = mDosBtn[0].findViewById(R.id.btn2);
                            btnSal.setText("Cancelar y salir");
                            final AlertDialog.Builder fuera = new AlertDialog.Builder(getContext())
                                    .setCustomTitle(tituloFueraJornada)
                                    .setView(mDosBtn[0]);
                            final AlertDialog dialogoFuera = fuera.create();
                            btnCont.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoFuera.dismiss();
                                    leerGeo(obraselect);
                                }
                            });
                            btnSal.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoFuera.dismiss();
                                    menu.getInstance().finish();
                                    System.exit(0);
                                }
                            });
                            dialogoFuera.setCanceledOnTouchOutside(false);
                            dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCont.setEnabled(true);
                                    btnSal.setEnabled(true);
                                    menu.cargando(false);
                                    touch(false);
                                }
                            });
                            final String[] horasDV = {null};
                            if (diasVacaRango != null) {
                                mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String docu = diasVacaRango.replaceAll("/", "-").trim();
                                        horasDV[0] = documentSnapshot.getString(docu);
                                        if (horasDV[0] != null) {
                                            final String[] horaRangDi = horasDV[0].split("\\s*;\\s*");
                                            String[] horaRangEn = horaRangDi[0].split("\\s*:\\s*");
                                            String[] horaRangSa = horaRangDi[1].split("\\s*:\\s*");
                                            final Calendar calAhora = Calendar.getInstance();
                                            Calendar calEntradaDia = Calendar.getInstance();
                                            calEntradaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangEn[0]));
                                            calEntradaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangEn[1]));
                                            calEntradaDia.set(Calendar.SECOND, 00);
                                            Calendar calSalidaDia = Calendar.getInstance();
                                            calSalidaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangSa[0]));
                                            calSalidaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangSa[1]));
                                            calSalidaDia.set(Calendar.SECOND, 00);
                                            if (calAhora.getTime().after(calEntradaDia.getTime()) && calAhora.getTime().before(calSalidaDia.getTime())) {
                                                if (diasVacaRango.contains("V")) {
                                                    tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("V", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por vacaciones\n¿Desea registrar la jornada de todas formas?");
                                                    fuera.setCustomTitle(tituloFueraJornada);
                                                    if (mDosBtn[0].getParent() != null) {
                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoFuera.show();
                                                    } else {
                                                        dialogoFuera.show();
                                                    }

                                                } else if (diasVacaRango.contains("B")) {
                                                    tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("B", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por baja laboral\n¿Desea registrar la jornada de todas formas?");
                                                    fuera.setCustomTitle(tituloFueraJornada);
                                                    if (mDosBtn[0].getParent() != null) {
                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        dialogoFuera.show();
                                                    } else {
                                                        dialogoFuera.show();
                                                    }

                                                } else if (diasVacaRango.contains("O")) {
                                                    mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            String d = diasVacaRango.replace("O", "").replaceAll("/", "-");
                                                            tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("O", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por " + documentSnapshot.getString(d) + "\n¿Desea registrar la jornada de todas formas?");
                                                            fuera.setCustomTitle(tituloFueraJornada);
                                                            if (mDosBtn[0].getParent() != null) {
                                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                dialogoFuera.show();
                                                            } else {
                                                                dialogoFuera.show();
                                                            }
                                                        }
                                                    });
                                                }
                                            } else {
                                                menu.cargando(false);
                                                touch(false);
                                                leerGeo(obraselect);
                                            }
                                        } else {
                                            tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                            if (mDosBtn[0].getParent() != null) {
                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                dialogoFuera.show();
                                            } else {
                                                dialogoFuera.show();
                                            }
                                        }
                                    }
                                });
                            } else {
                                tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                if (mDosBtn[0].getParent() != null) {
                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                    dialogoFuera.show();
                                } else {
                                    dialogoFuera.show();
                                }
                            }
                        }
                    }

                }
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRegistro.dismiss();
                if (otro) {
                    dConfirma();
                }
            }
        });
        dialogoRegistro.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (comp.equals("iniciada")) {
                    botonJornada.setText("Finalizar");
                    obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba), true);
                    obraselect = obraSpinner.getItemAtPosition(leeObras(obraSpinner, obcomprueba)).toString();
                    obraSpinner.setEnabled(false);
                    botonJornada.setEnabled(true);
                    botonCancelar.setEnabled(true);
                } else if (comp.equals("finalizada") || comp.equals("no")) {
                    GeoPoint geoPointReferencia;
                    Double distanciaReferencia = null;
                    Double distanciaReferencia2 = null;
                    mayor = 0;
                    latitudDetectada = geoPointLocalizayo.getLatitude();
                    longitudDetectada = geoPointLocalizayo.getLongitude();
                    for (int ob = 0; ob < adaptadorObrasLista.getItemCount(); ob++) {
                        geoPointReferencia = adaptadorObrasLista.getItem(ob).getGeoPoint();
                        if (distanciaReferencia == null) {
                            distanciaReferencia = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(geoPointReferencia.getLatitude(), geoPointReferencia.getLongitude()));
                        } else {
                            distanciaReferencia2 = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(geoPointReferencia.getLatitude(), geoPointReferencia.getLongitude()));
                        }
                        if (distanciaReferencia != null && distanciaReferencia2 != null) {
                            if (distanciaReferencia2 < distanciaReferencia) {
                                mayor = ob;
                                distanciaReferencia = distanciaReferencia2;
                            }
                        }
                    }
                    obraSpinner.setSelection(leeObras(obraSpinner, adaptadorObrasLista.getItem(mayor).getObra()), true);
                    mayor = 0;
                    botonJornada.setText("Iniciar");
                    obraSpinner.setEnabled(true);
                    botonJornada.setEnabled(true);
                    botonCancelar.setEnabled(true);
                }
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoRegistro.setCanceledOnTouchOutside(false);
        if (mObras.getParent() != null) {
            ((ViewGroup) mObras.getParent()).removeView(mObras);
            mObras = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
            dialogoRegistro.show();
        } else {
            dialogoRegistro.show();
        }


    }

    private void dLogin(final Boolean obraMark, final String obraMarker) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Introduzca los credenciales del empleado invitado");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mLogin = getLayoutInflater().inflate(R.layout.dialogo_login, null, false);
        final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
        final EditText semail = mLogin.findViewById(R.id.emailDialogo);
        final EditText scontrasena = mLogin.findViewById(R.id.contrasenaDialogo);
        final Button botonSig = mLogin.findViewById(R.id.btn1);
        final Button botonCan = mLogin.findViewById(R.id.btn2);
        Login
                .setView(mLogin)
                .setCustomTitle(myMsgtitle);
        final AlertDialog dialogoLogin = Login.create();
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailNu = semail.getText().toString();
                final String contrasenaNu = scontrasena.getText().toString();
                if (!emailNu.isEmpty() && !contrasenaNu.isEmpty()) {
                    if (!emailNu.equals(emailAn)) {
                        if (emailList.contains(emailNu)) {
                            mAuth.signOut();
                            mAuth.signInWithEmailAndPassword(emailNu, contrasenaNu).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    menu.cargando(true);
                                    touch(true);
                                    if (task.isSuccessful()) {
                                        id = mAuth.getCurrentUser().getUid();
                                        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot.exists()) {
                                                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                                                    comp = documentSnapshot.getString("comprobar");
                                                    empresa = documentSnapshot.getString("empresa");
                                                    nombre = documentSnapshot.getString("nombre");
                                                    emailConf = documentSnapshot.getString("email");
                                                    roles = documentSnapshot.getString("rol");
                                                    obcomprueba = documentSnapshot.getString("obra");
                                                    if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                                        obcomprueba = documentSnapshot.getString("obra");
                                                    }
                                                    otro = true;
                                                    menu.cargando(false);
                                                    touch(false);
                                                    if (obraMark) {
                                                        menu.cargando(true);
                                                        touch(true);
                                                        final TextView myMsgtitle = new TextView(getActivity());
                                                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                        final Button botonJornada = mDos.findViewById(R.id.btn1);
                                                        final Button botonCancelar = mDos.findViewById(R.id.btn2);
                                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                        myMsgtitle.setTextColor(Color.BLACK);
                                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                        myMsgtitle.setLayoutParams(params);

                                                        String obFin = null;
                                                        if (obcomprueba != null) {
                                                            if (obcomprueba.equals(obraMarker)) {
                                                                myMsgtitle.setText(nombre + " ¿desea finalizar la jornada en la obra " + obraMarker + "?");
                                                                obFin = obraMarker;
                                                            } else {
                                                                myMsgtitle.setText("Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero");
                                                                obFin = obcomprueba;
                                                            }
                                                            botonJornada.setText("Finalizar");
                                                        } else {
                                                            myMsgtitle.setText(nombre + " ¿desea iniciar la jornada en la obra " + obraMarker + "?");
                                                            obFin = obraMarker;
                                                            botonJornada.setText("Iniciar");
                                                        }
                                                        final AlertDialog.Builder Otro = new AlertDialog.Builder(getContext());
                                                        Otro
                                                                .setCustomTitle(myMsgtitle)
                                                                .setView(mDos);
                                                        final AlertDialog dialogoOtro = Otro.create();
                                                        final String finalObFin = obFin;
                                                        botonJornada.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (comp.equals("iniciada")) {
                                                                    entrada_salida = "Salida";
                                                                } else if (comp.equals("finalizada") || comp.equals("no")) {
                                                                    entrada_salida = "Entrada";
                                                                }
                                                                dialogoOtro.dismiss();
                                                                if (Jornada()) {
                                                                    leerGeo(finalObFin);
                                                                } else {
                                                                    menu.cargando(true);
                                                                    touch(true);
                                                                    final View[] mDosBtn = {getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null)};
                                                                    final Button btnCont = mDosBtn[0].findViewById(R.id.btn1);
                                                                    btnCont.setText("Continuar");
                                                                    final Button btnSal = mDosBtn[0].findViewById(R.id.btn2);
                                                                    btnSal.setText("Cancelar y salir");
                                                                    final AlertDialog.Builder fuera = new AlertDialog.Builder(getContext())
                                                                            .setCustomTitle(tituloFueraJornada)
                                                                            .setView(mDosBtn[0]);
                                                                    final AlertDialog dialogoFuera = fuera.create();
                                                                    btnCont.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialogoFuera.dismiss();
                                                                            leerGeo(finalObFin);
                                                                        }
                                                                    });
                                                                    btnSal.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            dialogoFuera.dismiss();
                                                                            menu.getInstance().finish();
                                                                            System.exit(0);
                                                                        }
                                                                    });
                                                                    dialogoFuera.setCanceledOnTouchOutside(false);
                                                                    dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                        @Override
                                                                        public void onShow(DialogInterface dialog) {
                                                                            btnCont.setEnabled(true);
                                                                            btnSal.setEnabled(true);
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                        }
                                                                    });
                                                                    final String[] horasDV = {null};
                                                                    if (diasVacaRango != null) {
                                                                        mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                String docu = diasVacaRango.replaceAll("/", "-").trim();
                                                                                horasDV[0] = documentSnapshot.getString(docu);
                                                                                if (horasDV[0] != null) {
                                                                                    final String[] horaRangDi = horasDV[0].split("\\s*;\\s*");
                                                                                    String[] horaRangEn = horaRangDi[0].split("\\s*:\\s*");
                                                                                    String[] horaRangSa = horaRangDi[1].split("\\s*:\\s*");
                                                                                    final Calendar calAhora = Calendar.getInstance();
                                                                                    Calendar calEntradaDia = Calendar.getInstance();
                                                                                    calEntradaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangEn[0]));
                                                                                    calEntradaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangEn[1]));
                                                                                    calEntradaDia.set(Calendar.SECOND, 00);
                                                                                    Calendar calSalidaDia = Calendar.getInstance();
                                                                                    calSalidaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangSa[0]));
                                                                                    calSalidaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangSa[1]));
                                                                                    calSalidaDia.set(Calendar.SECOND, 00);
                                                                                    if (calAhora.getTime().after(calEntradaDia.getTime()) && calAhora.getTime().before(calSalidaDia.getTime())) {
                                                                                        if (diasVacaRango.contains("V")) {
                                                                                            tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("V", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por vacaciones\n¿Desea registrar la jornada de todas formas?");
                                                                                            fuera.setCustomTitle(tituloFueraJornada);
                                                                                            if (mDosBtn[0].getParent() != null) {
                                                                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                dialogoFuera.show();
                                                                                            } else {
                                                                                                dialogoFuera.show();
                                                                                            }

                                                                                        } else if (diasVacaRango.contains("B")) {
                                                                                            tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("B", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por baja laboral\n¿Desea registrar la jornada de todas formas?");
                                                                                            fuera.setCustomTitle(tituloFueraJornada);
                                                                                            if (mDosBtn[0].getParent() != null) {
                                                                                                ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                                                mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                dialogoFuera.show();
                                                                                            } else {
                                                                                                dialogoFuera.show();
                                                                                            }

                                                                                        } else if (diasVacaRango.contains("O")) {
                                                                                            mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                                    String d = diasVacaRango.replace("O", "").replaceAll("/", "-");
                                                                                                    tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("O", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por " + documentSnapshot.getString(d) + "\n¿Desea registrar la jornada de todas formas?");
                                                                                                    fuera.setCustomTitle(tituloFueraJornada);
                                                                                                    if (mDosBtn[0].getParent() != null) {
                                                                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                        dialogoFuera.show();
                                                                                                    } else {
                                                                                                        dialogoFuera.show();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    } else {
                                                                                        menu.cargando(false);
                                                                                        touch(false);
                                                                                        leerGeo(finalObFin);
                                                                                    }
                                                                                } else {
                                                                                    tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                                                                    if (mDosBtn[0].getParent() != null) {
                                                                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                        dialogoFuera.show();
                                                                                    } else {
                                                                                        dialogoFuera.show();
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                                                        if (mDosBtn[0].getParent() != null) {
                                                                            ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                            mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                            dialogoFuera.show();
                                                                        } else {
                                                                            dialogoFuera.show();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        botonCancelar.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (otro) {
                                                                    dConfirma();
                                                                }
                                                                dialogoOtro.dismiss();
                                                            }
                                                        });
                                                        dialogoOtro.setCanceledOnTouchOutside(false);
                                                        dialogoOtro.setOnShowListener(new DialogInterface.OnShowListener() {
                                                            @Override
                                                            public void onShow(final DialogInterface dialog) {
                                                                dialogoLogin.dismiss();
                                                                botonJornada.setEnabled(true);
                                                                botonCancelar.setEnabled(true);
                                                                menu.cargando(false);
                                                                touch(false);
                                                            }
                                                        });
                                                        dialogoOtro.setCanceledOnTouchOutside(false);
                                                        if (mDos.getParent() != null) {
                                                            ((ViewGroup) mDos.getParent()).removeView(mDos);
                                                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                            dialogoOtro.show();
                                                        } else {
                                                            dialogoOtro.show();
                                                        }
                                                    } else {
                                                        dialogoLogin.dismiss();
                                                        dRegistrar();
                                                    }
                                                } else {
                                                    menu.snackbar.setText("No se pudo iniciar sesion, compruebe los datos");
                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                    tv.setTextSize(10);
                                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                    menu.snackbar.show();
                                                    menu.cargando(false);
                                                    touch(false);
                                                }
                                            }
                                        });
                                    } else {
                                        menu.snackbar.setText("No se pudo iniciar sesion, compruebe los datos");
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(10);
                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                        menu.snackbar.show();
                                        dialogoLogin.show();
                                    }
                                }
                            });

                        } else {
                            menu.snackbar.setText("Usuario desactivado");
                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextSize(10);
                            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                            menu.snackbar.show();
                            menu.snackbar.show();
                        }
                    } else {
                        semail.getText().clear();
                        semail.setError("Introduzca el email del empleado invitado");
                        scontrasena.getText().clear();

                    }
                } else if (emailNu.isEmpty()) {
                    semail.setError("Introduzca el email del empleado invitado");
                    if (contrasenaNu.isEmpty()) {
                        scontrasena.setError("Introduzca la contraseña del empleado invitado");
                    }

                } else {
                    scontrasena.setError("Introduzca el email del empleado invitado");
                }
            }
        });
        botonCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoLogin.dismiss();
            }
        });
        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                semail.setEnabled(true);
                scontrasena.setEnabled(true);
                botonSig.setEnabled(true);
                botonCan.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoLogin.setCanceledOnTouchOutside(false);
        if (mLogin.getParent() != null) {
            ((ViewGroup) mLogin.getParent()).removeView(mLogin);
            mLogin = getLayoutInflater().inflate(R.layout.dialogo_login, null, false);
            dialogoLogin.show();

        } else {
            dialogoLogin.show();
        }
    }

    private void dConfirma() {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombreNu + " confirme la operación");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mLogin = getLayoutInflater().inflate(R.layout.dialogo_confirmar, null, false);
        final AlertDialog.Builder Confirma = new AlertDialog.Builder(getContext());
        final EditText semail = mLogin.findViewById(R.id.emailDialogo);
        semail.setEnabled(false);
        semail.setText(emailAn);
        final EditText scontrasena = mLogin.findViewById(R.id.contraseñaDialogo);
        final Button botonSig = mLogin.findViewById(R.id.btn1);
        Confirma
                .setView(mLogin)
                .setCustomTitle(myMsgtitle);
        final AlertDialog dialogoConfirma = Confirma.create();
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contrasenaAn = scontrasena.getText().toString();
                if (!contrasenaAn.isEmpty()) {
                    mAuth.signOut();
                    mAuth.signInWithEmailAndPassword(emailAn, contrasenaAn).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                id = mAuth.getCurrentUser().getUid();
                                otro = false;
                                dialogoConfirma.dismiss();
                                new CountDownTimer(6000, 6000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        menu.cargando(true);
                                        touch(true);
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(10);
                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                        menu.snackbar.show();
                                    }

                                    @Override
                                    public void onFinish() {
                                        menu.cargando(false);
                                        touch(false);
                                        Intent intent = new Intent(menu.getInstance(), login.class);
                                        menu.getInstance().finish();
                                        startActivity(intent);
                                    }
                                }.start();
                            } else if (!task.isSuccessful()) {
                                menu.snackbar.setText("No se pudo iniciar sesion, compruebe los datos");
                                TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                tv.setTextSize(10);
                                snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                menu.snackbar.show();
                                scontrasena.setError("Compruebe la contraseña");
                            }
                        }
                    });
                } else {
                    scontrasena.setError("Introduzca la contraseña");
                }
            }
        });
        dialogoConfirma.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                scontrasena.setEnabled(true);
                botonSig.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoConfirma.setCanceledOnTouchOutside(false);
        if (mLogin.getParent() != null) {
            ((ViewGroup) mLogin.getParent()).removeView(mLogin);
            mLogin = getLayoutInflater().inflate(R.layout.dialogo_confirmar, null, false);
            dialogoConfirma.show();

        } else {

            dialogoConfirma.show();
        }
    }

    private void leerGeo(final String obra) {
        mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                menu.cargando(true);
                touch(true);
                dis = 50.0;
                GeoPoint geopointGuardado = documentSnapshot.getGeoPoint("geoPoint");
                latitudGuardada = geopointGuardado.getLatitude();
                longitudGuardada = geopointGuardado.getLongitude();
                timerLeergeo = new CountDownTimer(60000, 5000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        latitudDetectada = geoPointLocalizayo.getLatitude();
                        longitudDetectada = geoPointLocalizayo.getLongitude();
                        distan = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(latitudGuardada, longitudGuardada));
                        if (Double.compare(distan, dis) <= 0) {
                            if (entrada_salida.equals("Entrada")) {
                                comp = "iniciada";
                                mDb.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDb.collection("Todas las ids").document(id).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDb.collection("Empresas").document(empresa).collection("Localizaciones " + roles).document(nombre).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                trayecto = documentSnapshot.getString("marca temporal");
                                                                                if (trayecto != null) {
                                                                                    String fechaGuardada = "Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                                            " del " + trayecto.charAt(19) + trayecto.charAt(20) +
                                                                                            " de " + trayecto.charAt(25) + trayecto.charAt(26) + trayecto.charAt(27) + trayecto.charAt(28);
                                                                                    DateFormat fechaF = new SimpleDateFormat("dd 'del' MM 'de' yyyy", Locale.getDefault());
                                                                                    String fechaAhora = "Iniciado el " + fechaF.format(Calendar.getInstance().getTime());
                                                                                    if (fechaAhora.equals(fechaGuardada)) {
                                                                                        trayectoBo = true;
                                                                                        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                                                                        String horaAhora = hourFormat.format(Calendar.getInstance().getTime());
                                                                                        trayecto = trayecto + fechaAhora.replace("Iniciado el ", " ") + " a las " + horaAhora;
                                                                                    }
                                                                                }
                                                                                distan2 = 1.2;
                                                                                menu.cargando(false);
                                                                                touch(false);
                                                                                if (timerSnackLocaliza != null) {
                                                                                    timerSnackLocaliza.cancel();
                                                                                }
                                                                                cancel();
                                                                                enviajornada(obra, null);
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
                            } else if (entrada_salida.equals("Salida")) {
                                distan2 = 1.2;
                                menu.cargando(false);
                                touch(false);
                                if (timerSnackLocaliza != null) {
                                    timerSnackLocaliza.cancel();
                                }
                                cancel();
                                compruebaObra(obra);
                            }
                        } else if (Double.compare(distan, 50.0) > 0) {
                            distan2 = 1.0;
                            dis = dis + 50.0;
                            if (timerSnackLocaliza == null) {
                                timerSnackLocaliza = new CountDownTimer(60000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        if (snackbarLocalizando == null) {
                                            snackbarLocalizando = "Localizando";
                                        } else if (snackbarLocalizando.equals("Localizando")) {
                                            snackbarLocalizando = ".Localizando.";
                                        } else if (snackbarLocalizando.equals(".Localizando.")) {
                                            snackbarLocalizando = "..Localizando..";
                                        } else if (snackbarLocalizando.equals("..Localizando..")) {
                                            snackbarLocalizando = "...Localizando...";
                                        } else if (snackbarLocalizando.equals("...Localizando...")) {
                                            snackbarLocalizando = "Localizando";
                                        }
                                        menu.snackbar.setText(snackbarLocalizando);
                                        menu.snackbar.setDuration(1000);
                                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(10);
                                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                        menu.snackbar.show();
                                    }

                                    @Override
                                    public void onFinish() {

                                    }
                                }.start();
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        latitudDetectada = geoPointLocalizayo.getLatitude();
                        longitudDetectada = geoPointLocalizayo.getLongitude();
                        distan = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(latitudGuardada, longitudGuardada));
                        if (distan2 == 1.2) {
                            distan2 = 1.0;
                            if (timerSnackLocaliza != null) {
                                timerSnackLocaliza.cancel();
                            }
                            menu.cargando(false);
                            touch(false);
                        } else if (distan2 == 1.0) {
                            distan2 = 1.1;
                            if (latitudDetectada == null || longitudDetectada == null || Double.compare(distan, dis) > 0) {
                                if (entrada_salida.equals("Entrada")) {
                                    comp = "iniciada";
                                    mDb.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDb.collection("Todas las ids").document(id).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                                            mDb.collection("Empresas").document(empresa).collection("Localizaciones " + roles).document(nombre).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    trayecto = documentSnapshot.getString("marca temporal");
                                                                                    if (trayecto != null) {
                                                                                        String fechaGuardada = "Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                                                " del " + trayecto.charAt(19) + trayecto.charAt(20) +
                                                                                                " de " + trayecto.charAt(25) + trayecto.charAt(26) + trayecto.charAt(27) + trayecto.charAt(28);
                                                                                        DateFormat fechaF = new SimpleDateFormat("dd 'del' MM 'de' yyyy", Locale.getDefault());
                                                                                        String fechaAhora = "Iniciado el " + fechaF.format(Calendar.getInstance().getTime());
                                                                                        if (fechaAhora.equals(fechaGuardada)) {
                                                                                            trayectoBo = true;
                                                                                            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                                                                            String horaAhora = hourFormat.format(Calendar.getInstance().getTime());
                                                                                            trayecto = trayecto + fechaAhora.replace("Iniciado el ", " ") + " a las " + horaAhora;
                                                                                        }
                                                                                    }
                                                                                    menu.cargando(false);
                                                                                    touch(false);
                                                                                    if (dis >= 650) {
                                                                                        menu.cargando(true);
                                                                                        touch(true);
                                                                                        final TextView myMsgtitle = new TextView(getActivity());
                                                                                        myMsgtitle.setText("Se te ha detectado muy lejos de la obra " + obra);
                                                                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                        myMsgtitle.setLayoutParams(params);
                                                                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                                        myMsgtitle.setTextColor(Color.BLACK);

                                                                                        mFueraObra = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                                                                        final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                                                                                        final EditText sJustificar = mFueraObra.findViewById(R.id.justificaDialogo);
                                                                                        final Button botonSiguiente = mFueraObra.findViewById(R.id.btn1);
                                                                                        Login
                                                                                                .setCustomTitle(myMsgtitle)
                                                                                                .setView(mFueraObra);
                                                                                        final AlertDialog dialogoLogin = Login.create();
                                                                                        botonSiguiente.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                final String JustTexto = sJustificar.getText().toString();

                                                                                                if (!JustTexto.isEmpty()) {


                                                                                                    map.put("Justificacion fuera de obra: ", JustTexto);

                                                                                                    sJustificar.setHintTextColor(Color.GRAY);
                                                                                                    dialogoLogin.dismiss();
                                                                                                    enviajornada(obra, null);

                                                                                                } else {

                                                                                                    sJustificar.setHintTextColor(Color.RED);

                                                                                                }
                                                                                            }
                                                                                        });
                                                                                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                            @Override
                                                                                            public void onShow(final DialogInterface dialog) {
                                                                                                sJustificar.setEnabled(true);
                                                                                                botonSiguiente.setEnabled(true);
                                                                                                menu.cargando(false);
                                                                                                touch(false);
                                                                                            }
                                                                                        });
                                                                                        dialogoLogin.setCanceledOnTouchOutside(false);
                                                                                        if (mFueraObra.getParent() != null) {
                                                                                            ((ViewGroup) mFueraObra.getParent()).removeView(mFueraObra);
                                                                                            mFueraObra = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);

                                                                                            dialogoLogin.show();

                                                                                        } else {

                                                                                            dialogoLogin.show();
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
                                            });
                                        }
                                    });
                                } else if (entrada_salida.equals("Salida")) {
                                    if (dis >= 650) {
                                        menu.cargando(false);
                                        touch(false);
                                        menu.cargando(true);
                                        touch(true);
                                        final TextView myMsgtitle = new TextView(getActivity());
                                        myMsgtitle.setText("Se te ha detectado muy lejos de la obra " + obra);
                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        myMsgtitle.setLayoutParams(params);
                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                        myMsgtitle.setTextColor(Color.BLACK);

                                        mFueraObra = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);
                                        final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                                        final EditText sJustificar = mFueraObra.findViewById(R.id.justificaDialogo);
                                        final Button botonSiguiente = mFueraObra.findViewById(R.id.btn1);
                                        Login
                                                .setCustomTitle(myMsgtitle)
                                                .setView(mFueraObra);
                                        final AlertDialog dialogoLogin = Login.create();
                                        botonSiguiente.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                final String JustTexto = sJustificar.getText().toString();

                                                if (!JustTexto.isEmpty()) {


                                                    map.put("Justificacion fuera de obra: ", JustTexto);

                                                    sJustificar.setHintTextColor(Color.GRAY);
                                                    dialogoLogin.dismiss();
                                                    compruebaObra(obra);

                                                } else {

                                                    sJustificar.setHintTextColor(Color.RED);

                                                }
                                            }
                                        });
                                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(final DialogInterface dialog) {
                                                sJustificar.setEnabled(true);
                                                botonSiguiente.setEnabled(true);
                                                menu.cargando(false);
                                                touch(false);
                                            }
                                        });
                                        dialogoLogin.setCanceledOnTouchOutside(false);
                                        if (mFueraObra.getParent() != null) {
                                            ((ViewGroup) mFueraObra.getParent()).removeView(mFueraObra);
                                            mFueraObra = getLayoutInflater().inflate(R.layout.dialogo_justificar, null, false);

                                            dialogoLogin.show();

                                        } else {

                                            dialogoLogin.show();
                                        }
                                    }
                                }
                            }
                        }
                        timerSnackLocaliza.cancel();
                        timerSnackLocaliza = null;
                        menu.snackbar.setDuration(5000);
                    }
                }.start();
            }
        });
    }

    private boolean Jornada() {
        menu.cargando(true);
        touch(true);
        final boolean[] hora = {true};
        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    hora[0] = true;
                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                    mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            diasLibresSemana = new ArrayList<>();
                            diasLibresSemana.clear();
                            diasLibresSemana = (ArrayList<String>) documentSnapshot.get("dias libres semana");
                            hEntrada = documentSnapshot.getString("hora de entrada");
                            if (hEntrada != null) {
                                try {
                                    hEntradaDate = formatJor.parse(hEntrada);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            hSalida = documentSnapshot.getString("hora de salida");
                            if (hSalida != null) {
                                try {
                                    hSalidaDate = formatJor.parse(hSalida);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            hEntrada2 = documentSnapshot.getString("hora de entrada partida");
                            if (hEntrada2 != null) {
                                try {
                                    hEntrada2Date = formatJor.parse(hEntrada2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            hSalida2 = documentSnapshot.getString("hora de salida partida");
                            if (hSalida2 != null) {
                                try {
                                    hSalida2Date = formatJor.parse(hSalida2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    diasVaca = documentSnapshot.getString("Dias libres");
                    tituloFueraJornada = new TextView(getContext());
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    tituloFueraJornada.setLayoutParams(params);
                    tituloFueraJornada.setGravity(Gravity.CENTER_HORIZONTAL);
                    tituloFueraJornada.setTextColor(Color.BLACK);
                    tituloFueraJornada.setPadding(2, 2, 2, 2);
                    final Calendar calAhora = Calendar.getInstance();
                    int weekday = calAhora.get(Calendar.DAY_OF_WEEK);
                    DateFormatSymbols dfs = new DateFormatSymbols();
                    if (diasVaca != null) {
                        final String[] diasSotLista = diasVaca.split("\\s*;\\s*");
                        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date = calAhora.getTime();
                        final String datefull = formato.format(date);
                        for (final String ds : diasSotLista) {
                            if (ds.contains(datefull)) {
                                diasVacaRango = ds;
                                if (ds.contains("V")) {
                                    tituloFueraJornada.setText("Hoy es dia " + ds.replaceAll("V", "") + " asignado libre por vacaciones");
                                    hora[0] = false;
                                    break;
                                } else if (ds.contains("B")) {
                                    tituloFueraJornada.setText("Hoy es dia " + ds.replaceAll("B", "") + " asignado libre por baja laboral");
                                    hora[0] = false;
                                    break;
                                } else if (ds.contains("O")) {
                                    mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String d = ds.replace("O", "").replaceAll("/", "-");
                                            tituloFueraJornada.setText("Hoy es dia " + ds.replaceAll("O", "") + " asignado libre por " + documentSnapshot.getString(d));
                                        }
                                    });
                                    hora[0] = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (hora[0]) {
                        if (diasLibresSemana != null && diasLibresSemana.size() > 0) {
                            if (diasLibresSemana.contains(dfs.getWeekdays()[weekday])) {
                                hora[0] = false;
                                if (dfs.getWeekdays()[weekday].equals("Monday")) {
                                    tituloFueraJornada.setText("Hoy es Lunes, dia asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Tuesday")) {
                                    tituloFueraJornada.setText("Hoy es Martes, asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Wednesday")) {
                                    tituloFueraJornada.setText("Hoy es Miercoles, asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Thursday")) {
                                    tituloFueraJornada.setText("Hoy es Jueves, asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Friday")) {
                                    tituloFueraJornada.setText("Hoy es Viernes, asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Saturday")) {
                                    tituloFueraJornada.setText("Hoy es Sabado, asignado por " + empresa + " como dia de descanso semanal");
                                } else if (dfs.getWeekdays()[weekday].equals("Sunday")) {
                                    tituloFueraJornada.setText("Hoy es Domingo, asignado por " + empresa + " como dia de descanso semanal");
                                }
                            }
                        }
                    }
                    if (hora[0]) {
                        if (hSalida2Date == null && hEntrada2Date == null) {
                            Calendar calEntrada = Calendar.getInstance();
                            calEntrada.set(Calendar.HOUR_OF_DAY, new DateTime(hEntradaDate).getHourOfDay());
                            calEntrada.set(Calendar.MINUTE, new DateTime(hEntradaDate).getMinuteOfHour());
                            calEntrada.set(Calendar.SECOND, 00);
                            Calendar calSalida = Calendar.getInstance();
                            calSalida.set(Calendar.HOUR_OF_DAY, new DateTime(hSalidaDate).getHourOfDay());
                            calSalida.set(Calendar.MINUTE, new DateTime(hSalidaDate).getMinuteOfHour());
                            calSalida.set(Calendar.SECOND, 00);
                            if (calAhora.getTime().equals(calEntrada.getTime()) || calAhora.getTime().equals(calSalida.getTime())) {
                                hora[0] = true;
                            } else if (calAhora.getTime().after(calEntrada.getTime()) && calAhora.getTime().before(calSalida.getTime())) {
                                hora[0] = true;
                            } else {
                                tituloFueraJornada.setText("Son las " + calAhora.get(Calendar.HOUR_OF_DAY) + ":" + calAhora.get(Calendar.MINUTE) + ",esta fuera del horario laboral");
                                hora[0] = false;
                            }

                        } else {
                            Calendar calEntrada = Calendar.getInstance();
                            calEntrada.set(Calendar.HOUR_OF_DAY, new DateTime(hEntradaDate).getHourOfDay());
                            calEntrada.set(Calendar.MINUTE, new DateTime(hEntradaDate).getMinuteOfHour());
                            calEntrada.set(Calendar.SECOND, 00);
                            Calendar calSalida = Calendar.getInstance();
                            calSalida.set(Calendar.HOUR_OF_DAY, new DateTime(hSalidaDate).getHourOfDay());
                            calSalida.set(Calendar.MINUTE, new DateTime(hSalidaDate).getMinuteOfHour());
                            calSalida.set(Calendar.SECOND, 00);
                            Calendar calSalida2 = Calendar.getInstance();
                            calSalida2.set(Calendar.HOUR_OF_DAY, new DateTime(hSalida2Date).getHourOfDay());
                            calSalida2.set(Calendar.MINUTE, new DateTime(hSalida2Date).getMinuteOfHour());
                            calSalida2.set(Calendar.SECOND, 00);
                            Calendar calEntrada2 = Calendar.getInstance();
                            calEntrada2.set(Calendar.HOUR_OF_DAY, new DateTime(hEntrada2Date).getHourOfDay());
                            calEntrada2.set(Calendar.MINUTE, new DateTime(hEntrada2Date).getMinuteOfHour());
                            calEntrada2.set(Calendar.SECOND, 00);
                            if (calAhora.getTime().equals(calEntrada.getTime())
                                    || calAhora.getTime().equals(calSalida.getTime())
                                    || calAhora.getTime().equals(calSalida2.getTime())
                                    || calAhora.getTime().equals(calEntrada2.getTime())) {
                                hora[0] = true;
                            } else if ((calAhora.getTime().after(calEntrada.getTime()) && calAhora.getTime().before(calSalida2.getTime()))
                                    || (calAhora.getTime().after(calEntrada2.getTime()) && calAhora.getTime().before(calSalida.getTime()))) {
                                hora[0] = true;
                            } else {
                                tituloFueraJornada.setText("Son las " + calAhora.get(Calendar.HOUR_OF_DAY) + ":" + calAhora.get(Calendar.MINUTE) + ",esta fuera del horario laboral");
                                hora[0] = false;
                            }
                        }
                    }
                    menu.cargando(false);
                    touch(false);
                }
            }
        });
        return hora[0];
    }

    private void compruebaObra(final String obra) {
        menu.cargando(true);
        touch(true);
        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final String obcomp = documentSnapshot.getString("obra");
                if (documentSnapshot.exists()) {
                    if (obra.equals(obcomp)) {
                        comp = "finalizada";
                        mDb.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDb.collection("Todas las ids").document(id).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection("Localizaciones " + roles).document(nombre).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Calendar CalendarioEditadoPruebas = Calendar.getInstance();
                                                                CalendarioEditadoPruebas.set(Calendar.HOUR_OF_DAY, 18);
                                                                CalendarioEditadoPruebas.set(Calendar.MINUTE, 20);
                                                                CalendarioEditadoPruebas.set(Calendar.SECOND, 00);
                                                                DateFormat dayFormat = new SimpleDateFormat("dd 'del' MM 'de' yyyy", Locale.getDefault());
                                                                DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                                                String fecha = dayFormat.format(CalendarioEditadoPruebas.getTime());
                                                                final String hora = hourFormat.format(CalendarioEditadoPruebas.getTime());
                                                                trayecto = "Iniciado el " + fecha + " a las " + hora + " y finalizado el ";
                                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).update("marca temporal", trayecto).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        menu.cargando(false);
                                                                        touch(false);
                                                                        if (marca != null) {
                                                                            Log.d("marca", marca);
                                                                            try {
                                                                                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                                                                Log.d("hMarca", marca);
                                                                                Log.d("hHoraAhora", hora);
                                                                                if (marca.contains(";")) {
                                                                                    final String[] marcaYhoras = marca.split("\\s*;\\s*");
                                                                                    Date entradaDate = format.parse(marcaYhoras[0]);
                                                                                    Date salidaDate = format.parse(hora);
                                                                                    Log.d("entradaDate", entradaDate.toString());
                                                                                    Log.d("salidaDate", salidaDate.toString());
                                                                                    long milis = (salidaDate.getTime() - entradaDate.getTime()) + Long.parseLong(marcaYhoras[1]);
                                                                                    Log.d("hours", String.valueOf(milis));
                                                                                    String milisTAhora = String.valueOf(milis);
                                                                                    Log.d("milisTAhora", milisTAhora);
                                                                                    Long minutes = ((Long.parseLong(mislisT) / (1000 * 60)) % 60);
                                                                                    Log.d("minutes", String.valueOf(minutes));
                                                                                    Long hours = ((Long.parseLong(mislisT) / (1000 * 60 * 60)) % 24);
                                                                                    Log.d("hours", String.valueOf(hours));
                                                                                    final Long hoursDeMas = ((Long.parseLong(milisTAhora) / (1000 * 60 * 60)) % 24) - hours;
                                                                                    if (Long.parseLong(milisTAhora) > Long.parseLong(mislisT)) {
                                                                                        menu.cargando(true);
                                                                                        touch(true);
                                                                                        final TextView myMsgtitle = new TextView(getActivity());
                                                                                        if (minutes > 00) {
                                                                                            myMsgtitle.setText("Hoy a trabajado " + hoursDeMas + " horas mas de las " + hours + ":" + minutes + " designadas en el horario laboral\n¿Son estas horas, extras o complementarias?");
                                                                                        }
                                                                                        myMsgtitle.setText("Hoy a trabajado " + hoursDeMas + " horas mas de las " + hours + " designadas en el horario laboral\n¿Son estas horas, extras o complementarias?");
                                                                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                        myMsgtitle.setLayoutParams(params);
                                                                                        myMsgtitle.setTextColor(Color.BLACK);
                                                                                        View mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                        final Button btnEx = mDosBtn.findViewById(R.id.btn1);
                                                                                        btnEx.setText("Extras");
                                                                                        final Button btnCom = mDosBtn.findViewById(R.id.btn2);
                                                                                        btnCom.setText("Complementarias");
                                                                                        final AlertDialog.Builder HorasDeMas1 = new AlertDialog.Builder(getContext())
                                                                                                .setCustomTitle(myMsgtitle)
                                                                                                .setView(mDosBtn);
                                                                                        final AlertDialog dialogoHorasDeMas1 = HorasDeMas1.create();
                                                                                        btnEx.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                menu.cargando(true);
                                                                                                touch(true);
                                                                                                final TextView myMsgtitle = new TextView(getActivity());
                                                                                                myMsgtitle.setText("¿Son horas extras normales o de fuerza mayor?");
                                                                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                                myMsgtitle.setLayoutParams(params);
                                                                                                myMsgtitle.setTextColor(Color.BLACK);
                                                                                                View mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                final Button btnNor = mDosBtn.findViewById(R.id.btn1);
                                                                                                btnNor.setText("normales");
                                                                                                final Button btnFM = mDosBtn.findViewById(R.id.btn2);
                                                                                                btnFM.setText("Fuerza mayor");
                                                                                                final AlertDialog.Builder HorasDeMas2 = new AlertDialog.Builder(getContext())
                                                                                                        .setCustomTitle(myMsgtitle)
                                                                                                        .setView(mDosBtn);
                                                                                                final AlertDialog dialogoHorasDeMas2 = HorasDeMas2.create();
                                                                                                btnNor.setOnClickListener(new View.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View v) {
                                                                                                        dialogoHorasDeMas2.dismiss();
                                                                                                        map.put("horas extras", hoursDeMas);
                                                                                                        enviajornada(obra, obcomp);
                                                                                                    }
                                                                                                });
                                                                                                btnFM.setOnClickListener(new View.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View v) {
                                                                                                        dialogoHorasDeMas2.dismiss();
                                                                                                        map.put("horas extras FM", hoursDeMas);
                                                                                                        enviajornada(obra, obcomp);
                                                                                                    }
                                                                                                });
                                                                                                dialogoHorasDeMas2.setCanceledOnTouchOutside(false);
                                                                                                dialogoHorasDeMas2.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                                    @Override
                                                                                                    public void onShow(DialogInterface dialog) {
                                                                                                        btnNor.setEnabled(true);
                                                                                                        btnFM.setEnabled(true);
                                                                                                        dialogoHorasDeMas1.dismiss();
                                                                                                        menu.cargando(false);
                                                                                                        touch(false);
                                                                                                    }
                                                                                                });
                                                                                                if (mDosBtn.getParent() != null) {
                                                                                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                    dialogoHorasDeMas2.show();
                                                                                                } else {
                                                                                                    dialogoHorasDeMas2.show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                                        btnCom.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                dialogoHorasDeMas1.dismiss();
                                                                                                map.put("horas complementarias", hoursDeMas);
                                                                                                enviajornada(obra, obcomp);
                                                                                            }
                                                                                        });
                                                                                        dialogoHorasDeMas1.setCanceledOnTouchOutside(false);
                                                                                        dialogoHorasDeMas1.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                            @Override
                                                                                            public void onShow(DialogInterface dialog) {
                                                                                                btnEx.setEnabled(true);
                                                                                                btnCom.setEnabled(true);
                                                                                                menu.cargando(false);
                                                                                                touch(false);
                                                                                            }
                                                                                        });
                                                                                        if (mDosBtn.getParent() != null) {
                                                                                            ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                                            mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                            dialogoHorasDeMas1.show();
                                                                                        } else {
                                                                                            dialogoHorasDeMas1.show();
                                                                                        }
                                                                                    } else {
                                                                                        enviajornada(obra, obcomp);
                                                                                    }
                                                                                } else {
                                                                                    Date entradaDate = format.parse(marca);
                                                                                    Date salidaDate = format.parse(hora);
                                                                                    Log.d("entradaDate", entradaDate.toString());
                                                                                    Log.d("salidaDate", salidaDate.toString());
                                                                                    long milis = salidaDate.getTime() - entradaDate.getTime();
                                                                                    Log.d("hours", String.valueOf(milis));
                                                                                    String milisTAhora = String.valueOf(milis);
                                                                                    Log.d("milisTAhora", milisTAhora);
                                                                                    Long hours = ((Long.parseLong(mislisT) / (1000 * 60 * 60)) % 24);
                                                                                    final Long hoursDeMas = ((Long.parseLong(milisTAhora) / (1000 * 60 * 60)) % 24) - hours;
                                                                                    if (Long.parseLong(milisTAhora) > Long.parseLong(mislisT)) {
                                                                                        menu.cargando(true);
                                                                                        touch(true);
                                                                                        final TextView myMsgtitle = new TextView(getActivity());
                                                                                        myMsgtitle.setText("Hoy a trabajado " + hoursDeMas + " horas mas de las " + hours + " designadas en el horario laboral\n¿Son estas horas, extras o complementarias?");
                                                                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                        myMsgtitle.setLayoutParams(params);
                                                                                        myMsgtitle.setTextColor(Color.BLACK);
                                                                                        View mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                        final Button btnEx = mDosBtn.findViewById(R.id.btn1);
                                                                                        btnEx.setText("Extras");
                                                                                        final Button btnCom = mDosBtn.findViewById(R.id.btn2);
                                                                                        btnCom.setText("Complementarias");
                                                                                        final AlertDialog.Builder HorasDeMas1 = new AlertDialog.Builder(getContext())
                                                                                                .setCustomTitle(myMsgtitle)
                                                                                                .setView(mDosBtn);
                                                                                        final AlertDialog dialogoHorasDeMas1 = HorasDeMas1.create();
                                                                                        btnEx.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                menu.cargando(true);
                                                                                                touch(true);
                                                                                                final TextView myMsgtitle = new TextView(getActivity());
                                                                                                myMsgtitle.setText("¿Son horas extras normales o de fuerza mayor?");
                                                                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                                                                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                                                myMsgtitle.setLayoutParams(params);
                                                                                                myMsgtitle.setTextColor(Color.BLACK);
                                                                                                View mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                final Button btnNor = mDosBtn.findViewById(R.id.btn1);
                                                                                                btnNor.setText("normales");
                                                                                                final Button btnFM = mDosBtn.findViewById(R.id.btn2);
                                                                                                btnFM.setText("Fuerza mayor");
                                                                                                final AlertDialog.Builder HorasDeMas2 = new AlertDialog.Builder(getContext())
                                                                                                        .setCustomTitle(myMsgtitle)
                                                                                                        .setView(mDosBtn);
                                                                                                final AlertDialog dialogoHorasDeMas2 = HorasDeMas2.create();
                                                                                                btnNor.setOnClickListener(new View.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View v) {
                                                                                                        dialogoHorasDeMas2.dismiss();
                                                                                                        map.put("horas extras", hoursDeMas);
                                                                                                        enviajornada(obra, obcomp);
                                                                                                    }
                                                                                                });
                                                                                                btnFM.setOnClickListener(new View.OnClickListener() {
                                                                                                    @Override
                                                                                                    public void onClick(View v) {
                                                                                                        dialogoHorasDeMas2.dismiss();
                                                                                                        map.put("horas extras FM", hoursDeMas);
                                                                                                        enviajornada(obra, obcomp);
                                                                                                    }
                                                                                                });
                                                                                                dialogoHorasDeMas2.setCanceledOnTouchOutside(false);
                                                                                                dialogoHorasDeMas2.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                                    @Override
                                                                                                    public void onShow(DialogInterface dialog) {
                                                                                                        btnNor.setEnabled(true);
                                                                                                        btnFM.setEnabled(true);
                                                                                                        dialogoHorasDeMas1.dismiss();
                                                                                                        menu.cargando(false);
                                                                                                        touch(false);
                                                                                                    }
                                                                                                });
                                                                                                if (mDosBtn.getParent() != null) {
                                                                                                    ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                                                    mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                                    dialogoHorasDeMas2.show();
                                                                                                } else {
                                                                                                    dialogoHorasDeMas2.show();
                                                                                                }

                                                                                            }
                                                                                        });
                                                                                        btnCom.setOnClickListener(new View.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(View v) {
                                                                                                dialogoHorasDeMas1.dismiss();
                                                                                                map.put("horas complementarias", hoursDeMas);
                                                                                                enviajornada(obra, obcomp);
                                                                                            }
                                                                                        });
                                                                                        dialogoHorasDeMas1.setCanceledOnTouchOutside(false);
                                                                                        dialogoHorasDeMas1.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                            @Override
                                                                                            public void onShow(DialogInterface dialog) {
                                                                                                btnEx.setEnabled(true);
                                                                                                btnCom.setEnabled(true);
                                                                                                menu.cargando(false);
                                                                                                touch(false);
                                                                                            }
                                                                                        });
                                                                                        if (mDosBtn.getParent() != null) {
                                                                                            ((ViewGroup) mDosBtn.getParent()).removeView(mDosBtn);
                                                                                            mDosBtn = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                                            dialogoHorasDeMas1.show();
                                                                                        } else {
                                                                                            dialogoHorasDeMas1.show();
                                                                                        }
                                                                                    } else {
                                                                                        enviajornada(obra, obcomp);
                                                                                    }
                                                                                }

                                                                            } catch (ParseException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                        } else {
                                                                            enviajornada(obra, obcomp);
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
                    } else {
                        menu.cargando(false);
                        touch(false);
                        menu.snackbar.setText("No has iniciado jornada en esta obra");
                        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextSize(10);
                        snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                        menu.snackbar.show();
                    }
                }
                menu.cargando(false);
                touch(false);
            }
        });
        getActivity().overridePendingTransition(0, 0);
    }

    private void enviajornada(final String obra, String obcomp) {
        menu.cargando(true);
        touch(true);

        DateFormat dfecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Log.d("dfecha INICIAR", String.valueOf(dfecha));
        DateFormat dano = new SimpleDateFormat("yyyy", Locale.getDefault());
        Log.d("dano INICIAR", String.valueOf(dano));
        DateFormat dmes = new SimpleDateFormat("MM", Locale.getDefault());
        Log.d("dmes INICIAR", String.valueOf(dmes));
        DateFormat ddia = new SimpleDateFormat("dd", Locale.getDefault());
        Log.d("ddia INICIAR", String.valueOf(ddia));
        final String[] fecha = {dfecha.format(Calendar.getInstance().getTime())};
        Log.d("fecha INICIAR", fecha[0]);
        final String ano1 = dano.format(Calendar.getInstance().getTime());
        Log.d("ano1 INICIAR", ano1);
        final String mes = dmes.format(Calendar.getInstance().getTime());
        Log.d("mes INICIAR", mes);
        final String dia = ddia.format(Calendar.getInstance().getTime());
        Log.d("dia INICIAR", dia);
        final String[] hora = new String[1];
        if (entrada_salida.equals("Entrada")) {
            Calendar CalendarioEditadoPruebas = Calendar.getInstance();
            CalendarioEditadoPruebas.set(Calendar.HOUR_OF_DAY, 7);
            CalendarioEditadoPruebas.set(Calendar.MINUTE, 30);
            CalendarioEditadoPruebas.set(Calendar.SECOND, 00);
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Log.d("hourFormat INICIA", String.valueOf(hourFormat));
            Log.d("CALENDAR INICIA", String.valueOf(CalendarioEditadoPruebas.getTime()));
            hora[0] = hourFormat.format(CalendarioEditadoPruebas.getTime());
            Log.d("hora INICIA", hora[0]);
        } else {
            Calendar CalendarioEditadoPruebas = Calendar.getInstance();
            CalendarioEditadoPruebas.set(Calendar.HOUR_OF_DAY, 19);
            CalendarioEditadoPruebas.set(Calendar.MINUTE, 43);
            CalendarioEditadoPruebas.set(Calendar.SECOND, 00);
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Log.d("hourFormat FINALIZA", String.valueOf(hourFormat));
            Log.d("CALENDAR FINALIZA", String.valueOf(CalendarioEditadoPruebas.getTime()));
            hora[0] = hourFormat.format(CalendarioEditadoPruebas.getTime());
            Log.d("hora FINALIZA", hora[0]);

        }

        /*DateFormat dhora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            hora[0] = dhora.format(Calendar.getInstance().getTime());*/

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String manaOtard = null;
        if (timeOfDay < 12) {
            manaOtard = "Mañana";
        } else if (timeOfDay < 16) {
            manaOtard = "Tarde";
        } else if (timeOfDay < 21) {
            manaOtard = "Tarde";
        } else {
            manaOtard = "Noche";
        }
        map.put("nombre", nombre);
        map.put("Entrada o Salida", entrada_salida);
        map.put("obra", obra);
        map.put("rol", roles);
        map.put("fecha", fecha[0]);
        map.put("hora", hora[0]);
        map.put("Mañana o tarde", manaOtard);
        map.put("UID", id);
        if (otro) {
            map.put("iniciado por", nombreNu);
        }
        if (trayectoBo && obcomp != null) {
            trayectoBo = false;
            map.put("Trayecto desde " + obcomp + " hasta " + obra, trayecto);
        }
        if (distan2 == 1.1) {
            distan2 = 1.0;
            map.put("Ubicacion detectada correctamente", false);
            if (distan != null && distan > 0) {
                map.put("Distancia hasta la obra " + obra, distan + " metros");
            } else {
                map.put("Distancia hasta la obra " + obra, ">" + dis + " metros");
            }
            if (dis >= 650) {

                if (latitudDetectada != null && longitudDetectada != null) {

                    map.put("Coordenadas fuera de area de trabajo", geoPointLocalizayo);

                } else {

                    map.put("Coordenadas fuera de area de trabajo", " no detectadas correctamente");

                }

            }

        } else if (distan2 == 1.0 || distan2 == 1.2) {
            map.put("Distancia hasta la obra " + obra, +distan + " metros");
            map.put("Ubicacion detectada correctamente", true);
        }
        final Map<String, String> mapA = new HashMap<>();
        final Map<String, String> mapM = new HashMap<>();
        final Map<String, String> mapD = new HashMap<>();
        final String finalmanaOtard = manaOtard;
        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String es = null;
                String mt = null;
                if (entrada_salida.equals("Entrada")) {
                    es = "E";
                } else if (entrada_salida.equals("Salida")) {
                    es = "S";
                }
                if (finalmanaOtard.equals("Mañana")) {
                    mt = "M";
                } else {
                    mt = "T";
                }
                String exis = documentSnapshot.getString(dia + mes + ano1);
                if (exis != null) {
                    exis = exis + es + mt + hora[0] + ",";
                } else {
                    exis = es + mt + hora[0] + ",";
                }
                mapA.put(dia + mes + ano1, exis);
            }
        });

        mDb.collection("Empresas").document(empresa).collection("Registro").document(ano1).collection(mes).document(dia).collection(hora[0]).document(nombre).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                    mapA.put("años", a);

                                }
                            }
                            mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                mapM.put("meses", m);
                                            }
                                        }
                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                            mapD.put("dias", d);
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
                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").set(mapA).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").set(mapM).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").set(mapD).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombre).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").collection(dia).document(hora[0]).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                int valorOnline = documentSnapshot.getLong("online").intValue();
                                                                final Map<String, Object> mapES = new HashMap<>();
                                                                final Map<String, Object> mapMarca = new HashMap<>();
                                                                if (entrada_salida.equals("Entrada")) {
                                                                    if (marca == null) {
                                                                        marca = hora[0];
                                                                        Log.d("marca", marca);
                                                                        mapMarca.put("hora marcada", marca);
                                                                    } else {
                                                                        Log.d("marca", marca);
                                                                        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                                                        Log.d("hMarca", marca);
                                                                        Log.d("hHoraAhora", hora[0]);
                                                                        if (marca.contains(";")) {
                                                                            final String[] marcaYhoras = marca.split("\\s*;\\s*");
                                                                            long milis = Long.parseLong(marcaYhoras[1]);
                                                                            Log.d("hours", String.valueOf(milis));
                                                                            String milisTAhora = String.valueOf(milis);
                                                                            Log.d("milisTAhora", milisTAhora);
                                                                            marca = hora[0] + ";" + milisTAhora;
                                                                        } else {
                                                                            marca = hora[0];
                                                                        }
                                                                        mapMarca.put("hora marcada", marca);
                                                                    }
                                                                    mDb.collection("Todas las ids").document(id).set(mapMarca, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(nombre).set(mapMarca, SetOptions.merge());
                                                                        }
                                                                    });
                                                                    if (valorOnline >= 0) {
                                                                        valorOnline = valorOnline + 1;
                                                                        mapES.put("online", valorOnline);
                                                                        mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).set(mapES, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mDb.collection("Empresas").document(empresa).collection("Localizaciones " + roles).document(nombre).update("obra", obra);
                                                                            }
                                                                        });
                                                                    }
                                                                    menu.snackbar.setText("Jornada iniciada en " + obra + " correctamente");
                                                                } else if (entrada_salida.equals("Salida")) {
                                                                    if (marca != null) {
                                                                        Log.d("marca", marca);
                                                                        try {
                                                                            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                                                                            Log.d("hMarca", marca);
                                                                            Log.d("hHoraAhora", hora[0]);
                                                                            if (marca.contains(";")) {
                                                                                final String[] marcaYhoras = marca.split("\\s*;\\s*");
                                                                                Date entradaDate = format.parse(marcaYhoras[0]);
                                                                                Date salidaDate = format.parse(hora[0]);
                                                                                Log.d("entradaDate", entradaDate.toString());
                                                                                Log.d("salidaDate", salidaDate.toString());
                                                                                long milis = (salidaDate.getTime() - entradaDate.getTime()) + Long.parseLong(marcaYhoras[1]);
                                                                                Log.d("hours", String.valueOf(milis));
                                                                                String milisTAhora = String.valueOf(milis);
                                                                                Log.d("milisTAhora", milisTAhora);
                                                                                marca = hora[0] + ";" + milisTAhora;
                                                                                if (Long.parseLong(milisTAhora) > Long.parseLong(mislisT)) {
                                                                                    map.put("UID", id);
                                                                                }
                                                                            } else {
                                                                                Date entradaDate = format.parse(marca);
                                                                                Date salidaDate = format.parse(hora[0]);
                                                                                Log.d("entradaDate", entradaDate.toString());
                                                                                Log.d("salidaDate", salidaDate.toString());
                                                                                long milis = salidaDate.getTime() - entradaDate.getTime();
                                                                                Log.d("hours", String.valueOf(milis));
                                                                                String milisTAhora = String.valueOf(milis);
                                                                                Log.d("milisTAhora", milisTAhora);
                                                                                marca = hora[0] + ";" + milisTAhora;
                                                                                if (Long.parseLong(milisTAhora) > Long.parseLong(mislisT)) {

                                                                                }
                                                                            }
                                                                            Log.d("marca", marca);
                                                                            mapMarca.put("hora marcada", marca);
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    mDb.collection("Todas las ids").document(id).set(mapMarca, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(nombre).set(mapMarca, SetOptions.merge());
                                                                        }
                                                                    });
                                                                    if (valorOnline > 0) {
                                                                        valorOnline = valorOnline - 1;
                                                                        mapES.put("online", valorOnline);
                                                                        mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).set(mapES, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mDb.collection("Empresas").document(empresa).collection("Localizaciones " + roles).document(nombre).update("obra", null);
                                                                            }
                                                                        });
                                                                    }
                                                                    menu.snackbar.setText("Jornada finalizada en " + obra + " correctamente");
                                                                    if (hEntradaDate != null && hSalidaDate != null && hEntrada2Date == null && hSalida2Date == null) {

                                                                    } else if (hEntradaDate != null && hSalidaDate != null && hEntrada2Date != null && hSalida2Date != null) {

                                                                    }
                                                                }
                                                                if (otro) {
                                                                    otro = false;

                                                                    DateFormat dfecha1 = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
                                                                    fecha[0] = dfecha1.format(Calendar.getInstance().getTime());

                                                                    DateFormat dhora1 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                                                    hora[0] = dhora1.format(Calendar.getInstance().getTime());

                                                                    final Map<String, Object> mapf1 = new HashMap<>();
                                                                    mapf1.put("Desde", nombreNu);
                                                                    mapf1.put("fechaR", fecha[0]);
                                                                    mapf1.put("horaR", hora[0]);
                                                                    mapf1.put("obraR", obra);
                                                                    mapf1.put("saR", entrada_salida);

                                                                    mDb.collection("Todas las ids").document(id).set(mapf1, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mapA.clear();
                                                                            mapM.clear();
                                                                            mapD.clear();
                                                                            map.clear();
                                                                            menu.cargando(false);
                                                                            touch(false);
                                                                            firmar();
                                                                        }
                                                                    });
                                                                } else {
                                                                    mapA.clear();
                                                                    mapM.clear();
                                                                    mapD.clear();
                                                                    map.clear();
                                                                    mapES.clear();
                                                                    actualizaDatos();
                                                                    menu.cargando(false);
                                                                    touch(false);
                                                                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                                    tv.setTextSize(10);
                                                                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                                    menu.snackbar.show();
                                                                }
                                                                servicioLocalizacion.setEstado("online");
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
    }

    private void firmar() {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombreNu + " debe firmar para confirmar la operación");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setTextColor(Color.BLACK);
        mFirmar = getLayoutInflater().inflate(R.layout.dialogo_firmar, null, false);
        final AlertDialog.Builder Firmar = new AlertDialog.Builder(getContext());
        final SignaturePad firma = mFirmar.findViewById(R.id.firmaCon2);
        final Button botonFirm = mFirmar.findViewById(R.id.btn1);
        final Button botonBor = mFirmar.findViewById(R.id.btn2);
        final StorageReference[] firmaRef = new StorageReference[1];
        final FirebaseAuth mAuth2 = FirebaseAuth.getInstance();
        final StorageReference almacenRef2 = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
        Firmar
                .setCustomTitle(myMsgtitle)
                .setView(mFirmar);
        final AlertDialog dialogoFirmar = Firmar.create();
        firma.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                botonFirm.setEnabled(true);
                botonBor.setEnabled(true);
            }

            @Override
            public void onSigned() {
                botonFirm.setEnabled(true);
                botonBor.setEnabled(true);
            }

            @Override
            public void onClear() {
                botonFirm.setEnabled(false);
                botonBor.setEnabled(false);
            }
        });
        botonFirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                Bitmap firmaImagen = firma.getSignatureBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                firmaImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = firmaRef[0].putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        menu.cargando(false);
                        touch(false);
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
                        mDb.collection("Todas las ids").document(id).set(updates, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                menu.cargando(false);
                                touch(false);
                                dialogoFirmar.dismiss();
                                dConfirma();
                            }
                        });
                    }
                });

            }
        });
        botonBor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                firma.clear();
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoFirmar.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.contains("empresa")) {
                            if (compruebapermisos()) {
                                if (mAuth2.getCurrentUser() != null) {
                                    String desde = documentSnapshot.getString("Desde");
                                    firmaRef[0] = almacenRef2
                                            .child(documentSnapshot.getString("empresa"))
                                            .child("Registros desde " + desde)
                                            .child(documentSnapshot.getString("nombre"))
                                            .child(documentSnapshot.get("obraR").toString())
                                            .child(documentSnapshot.get("fechaR").toString())
                                            .child(documentSnapshot.get("horaR").toString())
                                            .child(documentSnapshot.getString("saR") +
                                                    " de " +
                                                    documentSnapshot.getString("nombre") +
                                                    " en la obra " +
                                                    documentSnapshot.get("obraR").toString() +
                                                    " desde la cuenta de " +
                                                    desde +
                                                    " el dia " +
                                                    documentSnapshot.get("fechaR").toString() +
                                                    " a las " +
                                                    documentSnapshot.get("fechaR").toString() +
                                                    ".jpg");
                                }
                            }
                        }
                    }
                });
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoFirmar.setCanceledOnTouchOutside(false);
        if (mFirmar.getParent() != null) {
            ((ViewGroup) mFirmar.getParent()).removeView(mFirmar);
            mFirmar = getLayoutInflater().inflate(R.layout.dialogo_firmar, null, false);

            dialogoFirmar.show();

        } else {

            dialogoFirmar.show();
        }
    }

    private void setUpRecyclerView() {
        mDb.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        xpand2.setOnClickListener(mapaEmpleados.this);
                    } else if (task.getResult().isEmpty()) {
                        xpand2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                menu.snackbar.setText("No se ha registrado ninguna obra por el momento");
                                TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                tv.setTextSize(10);
                                snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                menu.snackbar.show();
                            }
                        });
                    }
                } else if (!task.isSuccessful()) {
                    xpand2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            menu.snackbar.setText("No se ha registrado ninguna obra por el momento");
                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextSize(10);
                            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                            menu.snackbar.show();
                        }
                    });
                }
            }
        });
        Query query = mDb.collection("Empresas").document(empresa).collection("Obras").orderBy("online", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<marcadoresObras> options = new FirestoreRecyclerOptions.Builder<marcadoresObras>()
                .setQuery(query, marcadoresObras.class)
                .build();

        adaptadorObrasLista = new adaptadorObrasLista(options);

        RecyclerView recyclerView = getView().findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptadorObrasLista);
        adaptadorObrasLista.startListening();
        adaptadorObrasLista.setOnItemClickListener(new adaptadorObrasLista.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (menu.getCambioDeFragment()) {
                    setUpRecyclerView();
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    menu.setCambioDeFragmento(false);
                } else {
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }

                setCamara();
                GeoPoint geoPoint = adaptadorObrasLista.getItem(position).getGeoPoint();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 600, null);
                markersMapObra.get(markersMapObras2.get(adaptadorObrasLista.getItem(position).getGeoPoint())).showInfoWindow();
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    menu.cargando(true);
                    touch(true);
                    final TextView myMsgtitle = new TextView(getActivity());
                    mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                    final Button botonJornada = mDos.findViewById(R.id.btn1);
                    final Button botonCancelar = mDos.findViewById(R.id.btn2);
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    myMsgtitle.setLayoutParams(params);
                    myMsgtitle.setTextColor(Color.BLACK);

                    String obFin = null;
                    if (obcomprueba != null) {
                        if (obcomprueba.equals(adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra())) {
                            myMsgtitle.setText(nombre + " ¿desea finalizar la jornada en la obra " + adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra() + "?");
                            obFin = adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra();
                        } else if (!obcomprueba.equals(adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra())) {
                            myMsgtitle.setText("Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero");
                            obFin = obcomprueba;
                        }
                        botonJornada.setText("Finalizar");
                    } else {
                        myMsgtitle.setText(nombre + " ¿desea iniciar la jornada en la obra " + adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra() + "?");
                        obFin = adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra();
                        botonJornada.setText("Iniciar");
                    }
                    final AlertDialog.Builder Yo = new AlertDialog.Builder(getContext());
                    Yo
                            .setCustomTitle(myMsgtitle)
                            .setView(mDos);
                    final AlertDialog dialogoYo = Yo.create();
                    final String finalObFin = obFin;
                    botonJornada.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (comp.equals("iniciada")) {
                                entrada_salida = "Salida";
                            } else if (comp.equals("finalizada") || comp.equals("no")) {
                                entrada_salida = "Entrada";
                            }
                            dialogoYo.dismiss();
                            if (Jornada()) {
                                leerGeo(finalObFin);
                            } else {
                                menu.cargando(true);
                                touch(true);
                                final View[] mDosBtn = {getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null)};
                                final Button btnCont = mDosBtn[0].findViewById(R.id.btn1);
                                btnCont.setText("Continuar");
                                final Button btnSal = mDosBtn[0].findViewById(R.id.btn2);
                                btnSal.setText("Cancelar y salir");
                                final AlertDialog.Builder fuera = new AlertDialog.Builder(getContext())
                                        .setCustomTitle(tituloFueraJornada)
                                        .setView(mDosBtn[0]);
                                final AlertDialog dialogoFuera = fuera.create();
                                btnCont.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        leerGeo(finalObFin);
                                    }
                                });
                                btnSal.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialogoFuera.dismiss();
                                        menu.getInstance().finish();
                                        System.exit(0);
                                    }
                                });
                                dialogoFuera.setCanceledOnTouchOutside(false);
                                dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        btnCont.setEnabled(true);
                                        btnSal.setEnabled(true);
                                        menu.cargando(false);
                                        touch(false);
                                    }
                                });
                                final String[] horasDV = {null};
                                if (diasVacaRango != null) {
                                    mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String docu = diasVacaRango.replaceAll("/", "-").trim();
                                            horasDV[0] = documentSnapshot.getString(docu);
                                            if (horasDV[0] != null) {
                                                final String[] horaRangDi = horasDV[0].split("\\s*;\\s*");
                                                String[] horaRangEn = horaRangDi[0].split("\\s*:\\s*");
                                                String[] horaRangSa = horaRangDi[1].split("\\s*:\\s*");
                                                final Calendar calAhora = Calendar.getInstance();
                                                Calendar calEntradaDia = Calendar.getInstance();
                                                calEntradaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangEn[0]));
                                                calEntradaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangEn[1]));
                                                calEntradaDia.set(Calendar.SECOND, 00);
                                                Calendar calSalidaDia = Calendar.getInstance();
                                                calSalidaDia.set(Calendar.HOUR_OF_DAY, Integer.parseInt(horaRangSa[0]));
                                                calSalidaDia.set(Calendar.MINUTE, Integer.parseInt(horaRangSa[1]));
                                                calSalidaDia.set(Calendar.SECOND, 00);
                                                if (calAhora.getTime().after(calEntradaDia.getTime()) && calAhora.getTime().before(calSalidaDia.getTime())) {
                                                    if (diasVacaRango.contains("V")) {
                                                        tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("V", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por vacaciones\n¿Desea registrar la jornada de todas formas?");
                                                        fuera.setCustomTitle(tituloFueraJornada);
                                                        if (mDosBtn[0].getParent() != null) {
                                                            ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                            mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                            dialogoFuera.show();
                                                        } else {
                                                            dialogoFuera.show();
                                                        }

                                                    } else if (diasVacaRango.contains("B")) {
                                                        tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("B", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por baja laboral\n¿Desea registrar la jornada de todas formas?");
                                                        fuera.setCustomTitle(tituloFueraJornada);
                                                        if (mDosBtn[0].getParent() != null) {
                                                            ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                            mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                            dialogoFuera.show();
                                                        } else {
                                                            dialogoFuera.show();
                                                        }

                                                    } else if (diasVacaRango.contains("O")) {
                                                        mDb.collection("Empresas").document(empresa).collection("Dias libres").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                String d = diasVacaRango.replace("O", "").replaceAll("/", "-");
                                                                tituloFueraJornada.setText("Hoy es dia " + diasVacaRango.replaceAll("O", "") + " asignado libre desde las " + horaRangDi[0] + " hasta las " + horaRangDi[1] + " por " + documentSnapshot.getString(d) + "\n¿Desea registrar la jornada de todas formas?");
                                                                fuera.setCustomTitle(tituloFueraJornada);
                                                                if (mDosBtn[0].getParent() != null) {
                                                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                                    dialogoFuera.show();
                                                                } else {
                                                                    dialogoFuera.show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    menu.cargando(false);
                                                    touch(false);
                                                    leerGeo(finalObFin);
                                                }
                                            } else {
                                                tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                                if (mDosBtn[0].getParent() != null) {
                                                    ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                                    mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                    dialogoFuera.show();
                                                } else {
                                                    dialogoFuera.show();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    tituloFueraJornada.setText(tituloFueraJornada.getText().toString() + "\n¿Desea registrar la jornada de todas formas?");
                                    if (mDosBtn[0].getParent() != null) {
                                        ((ViewGroup) mDosBtn[0].getParent()).removeView(mDosBtn[0]);
                                        mDosBtn[0] = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                        dialogoFuera.show();
                                    } else {
                                        dialogoFuera.show();
                                    }
                                }
                            }
                        }
                    });
                    botonCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (otro) {
                                dConfirma();
                            }
                            dialogoYo.dismiss();
                        }
                    });
                    dialogoYo.setCanceledOnTouchOutside(false);
                    dialogoYo.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialog) {
                            botonJornada.setEnabled(true);
                            botonCancelar.setEnabled(true);
                            menu.cargando(false);
                            touch(false);
                        }
                    });
                    dialogoYo.setCanceledOnTouchOutside(false);
                    if (mDos.getParent() != null) {
                        ((ViewGroup) mDos.getParent()).removeView(mDos);
                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                        dialogoYo.show();
                    } else {
                        dialogoYo.show();
                    }
                } else if (direction == ItemTouchHelper.RIGHT) {
                    if (comp.equals("iniciada")) {
                        entrada_salida = "Salida";
                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                        entrada_salida = "Entrada";
                    }
                    dLogin(true, adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra());
                }
                adaptadorObrasLista.notifyItemChanged(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (adaptadorObrasLista != null) {
            adaptadorObrasLista.stopListening();
        }
    }

    private void centrarCamara() {

        mDb.collection("Empresas").document(empresa).collection("Localizaciones Empleado").document(nombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    menu.cargando(true);
                    touch(true);
                    GeoPoint geoPoint1 = task.getResult().getGeoPoint("geoPoint");

                    mLocaliza = new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude());
                    menu.cargando(false);
                    touch(false);
                    setCamara();
                }
            }
        });
    }

    private void setCamara() {
        menu.cargando(true);
        touch(true);
        double boundaryAbajo = mLocaliza.latitude - .1;
        double boundaryIzquierdo = mLocaliza.longitude - .1;
        double boundaryArriba = mLocaliza.latitude + .1;
        double boundaryDerecho = mLocaliza.longitude + .1;

        LatLngBounds mLocalizacionLaLo = new LatLngBounds(
                new LatLng(boundaryAbajo, boundaryIzquierdo),
                new LatLng(boundaryArriba, boundaryDerecho));

        if (!mMap.getCameraPosition().target.equals(mLocalizacionLaLo)) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLocalizacionLaLo, 0));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }
        menu.cargando(false);
        touch(false);

    }

    private void touch(Boolean touch) {
        if (touch) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

    private void detalles() {

        if (mLocalizarUsuario == null) {
            menu.cargando(true);
            touch(true);
            mLocalizarUsuario = new localizacionUsuario();
            mLocalizarUsuario.setId(id);
            mLocalizarUsuario.setNombre(nombre);
            mLocalizarUsuario.setEstado("online");
            mLocalizarUsuario.setDesactivado(false);
            menu.cargando(false);
            touch(false);
            localizacion();

        }
    }

    private void guardarLocalizacion() {

        if (mLocalizarUsuario != null) {
            menu.cargando(true);
            touch(true);
            DocumentReference locationRef = mDb
                    .collection("Empresas")
                    .document(empresa)
                    .collection("Localizaciones Empleado")
                    .document(nombre);
            locationRef.set(mLocalizarUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    menu.cargando(false);
                    touch(false);
                    centrarCamara();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    menu.cargando(false);
                    touch(false);
                }
            });
        }

    }

    private void init() {
        menu.cargando(true);
        touch(true);
        mBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocalizar();
                    menu.cargando(false);
                    touch(false);

                }
                return false;
            }
        });
        gps.setOnClickListener(mapaEmpleados.this);
        icInicio.setOnClickListener(mapaEmpleados.this);
        icAyuda.setOnClickListener(mapaEmpleados.this);

        ocultarTeclado();
    }

    private void geoLocalizar() {
        menu.cargando(true);
        touch(true);
        busquedaString = mBuscar.getText().toString();
        InputMethodManager inputManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        if (!busquedaString.isEmpty()) {
            mDb.collection("Empresas").document(empresa).collection("Obras").document(busquedaString.toLowerCase().trim()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().exists()) {
                        if (menu.getCambioDeFragment()) {
                            setUpRecyclerView();
                            if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }

                            menu.setCambioDeFragmento(false);
                        } else {

                            if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(task.getResult().getGeoPoint("geoPoint").getLatitude(), task.getResult().getGeoPoint("geoPoint").getLongitude()), ZOOM_PREDETERMINADO));
                        mBuscar.getText().clear();
                    } else if (!task.getResult().exists()) {
                        Geocoder geocoder = new Geocoder(getActivity());
                        List<Address> list = new ArrayList<>();
                        try {
                            list = geocoder.getFromLocationName(busquedaString, 1);
                        } catch (IOException e) {
                            menu.snackbar.setText("No se a podido encontrar");
                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                            tv.setTextSize(10);
                            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                            menu.snackbar.show();
                        }
                        if (list.size() > 0) {
                            menu.cargando(true);
                            touch(true);
                            final Address address = list.get(0);
                            if (menu.getCambioDeFragment()) {
                                setUpRecyclerView();

                                if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                }
                                menu.setCambioDeFragmento(false);
                            } else {

                                if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                }
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), ZOOM_PREDETERMINADO));
                            mBuscar.getText().clear();
                        } else {
                            mBuscar.getText().clear();
                        }
                    }
                    menu.cargando(false);
                    touch(false);
                }
            });
        }


    }

    private void localizacion() {
        menu.cargando(true);
        touch(true);
        FusedLocationProviderClient mProovedor = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
            if (compruebapermisos()) {
                final Task localizacion = mProovedor.getLastLocation();
                localizacion.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location locacizacionActual = (Location) task.getResult();
                            if (locacizacionActual != null) {
                                geoPointLocalizayo = new GeoPoint(locacizacionActual.getLatitude(), locacizacionActual.getLongitude());
                            }
                            if (geoPointLocalizayo != null) {
                                mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                                mLocalizarUsuario.setTimestamp(null);
                                menu.cargando(false);
                                touch(false);
                                guardarLocalizacion();
                            }
                        } else {
                            menu.cargando(false);
                            touch(false);
                        }
                    }
                });
            }
        } catch (SecurityException ignored) {

        }

    }

    private void iniciarMapa() {
        menu.cargando(true);
        touch(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(mapaEmpleados.this);
        menu.cargando(false);
        touch(false);

    }

    private boolean isServicesOK() {
        menu.cargando(true);
        touch(true);
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            menu.cargando(false);
            touch(false);
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            menu.cargando(false);
            touch(false);
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOGO_PEDIR);
            dialog.show();
        } else {
            menu.cargando(false);
            touch(false);
            menu.snackbar.setText("Mapas no funciona");
            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
            tv.setTextSize(10);
            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
            menu.snackbar.show();

        }
        return false;
    }

    private boolean compruebapermisos() {
        menu.cargando(true);
        touch(true);
        int resultado;
        List<String> listaPermisosNecesarios = new ArrayList<>();
        for (String perm : permisos) {
            resultado = ContextCompat.checkSelfPermission(getActivity(), perm);
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                listaPermisosNecesarios.add(perm);
            }
        }
        if (!listaPermisosNecesarios.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listaPermisosNecesarios.toArray(new String[listaPermisosNecesarios.size()]), Permisos);
            return false;
        }
        menu.cargando(false);
        touch(false);
        return true;
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBuscar.getWindowToken(), 0);
    }

    private void firestoreObras() {
        menu.cargando(true);
        touch(true);
        alreadyObs = false;
        geoFirestoreRefObs = mDb.collection("Empresas").document(empresa).collection("Obras");
        if (obs != null) {
            obs.clear();
        }
        if (markersMapObra != null) {
            markersMapObra.clear();
        }
        if (markersMapObras2 != null) {
            markersMapObras2.clear();
        }
        geoFirestoreRefObs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                List<Task<QuerySnapshot>> tasks2 = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        String jefe1 = document.getString("jefe");
                        GeoPoint geoPoint2 = document.getGeoPoint("geoPoint");
                        long online = document.getLong("online");
                        if (jefe1 != null) {
                            if (jefe1.equals("no")) {
                                jefe1 = "sin jefe de obra";
                            }
                        } else {
                            jefe1 = "sin jefe de obra";
                        }
                        if (!obs.contains(obran)) {
                            anadirMarcadores(geoPoint2, obran, jefe1, online);
                        }
                    }
                    mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.contains("obra")) {
                                obcomprueba = documentSnapshot.getString("obra");
                            }
                        }
                    });
                    menu.cargando(false);
                    touch(false);
                } else if (!task.isSuccessful()) {
                    menu.cargando(false);
                    touch(false);
                    obs.add("SIN OBRAS");
                }
                return Tasks.whenAllSuccess(tasks2);
            }

        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                geoFirestoreRefObs.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        obs.remove("SIN OBRAS");
                        mBuscar.getText().clear();
                        busquedaString = null;
                        readyObs = true;
                        alreadyObs = true;
                        mDb.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().isEmpty()) {
                                        xpand2.setOnClickListener(mapaEmpleados.this);
                                    } else if (task.getResult().isEmpty()) {
                                        xpand2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                menu.snackbar.setText("No se ha registrado ninguna obra por el momento");
                                                TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                                tv.setTextSize(10);
                                                snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                                menu.snackbar.show();
                                            }
                                        });
                                    }
                                } else if (!task.isSuccessful()) {
                                    xpand2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            menu.snackbar.setText("No se ha registrado ninguna obra por el momento");
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
                });
            }
        });
    }

    private void listenerObs() {
        firestoreObras();
        final int[] contador = {0};
        timerObs = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                contador[0]++;
                if (readyObs) {
                    contador[0] = 30;
                }
                if (contador[0] == 30) {
                    timerObs.cancel();
                    timerObs.onFinish();
                }
            }

            @Override
            public void onFinish() {
                if (readyObs) {
                    geoFirestoreRefObs.addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "onEvent: Listen failed", e);
                                return;
                            }
                            if (queryDocumentSnapshots != null) {
                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (documentChange.getDocument().exists()) {
                                        switch (documentChange.getType()) {
                                            case ADDED:
                                            case MODIFIED:
                                                if (alreadyObs) {
                                                    String obAn = documentChange.getDocument().getString("obra antigua");
                                                    if (obAn != null) {
                                                        Marker marker = markersMapObra.get(documentChange.getDocument().getString("obra antigua"));
                                                        if (marker != null) {
                                                            marker.remove();
                                                            markersMapObra.remove(documentChange.getDocument().getString("obra"));
                                                            markersMapObras2.remove(documentChange.getDocument().getString("obra"));
                                                            obs.remove(documentChange.getDocument().getString("obra"));
                                                            geoFirestoreRefObs.document(documentChange.getDocument().getString("obra")).update("obra antigua", FieldValue.delete());
                                                        }
                                                    }
                                                    anadirMarcadores(documentChange.getDocument().getGeoPoint("geoPoint"), documentChange.getDocument().getString("obra"), documentChange.getDocument().getString("jefe"), documentChange.getDocument().getLong("online"));
                                                }
                                                break;
                                            case REMOVED:
                                                if (alreadyObs) {
                                                    Marker marker = markersMapObra.get(documentChange.getDocument().getString("obra"));
                                                    if (marker != null) {
                                                        marker.remove();
                                                        markersMapObra.remove(documentChange.getDocument().getString("obra"));
                                                        obs.remove(documentChange.getDocument().getString("obra"));
                                                        markersMapObras2.remove(documentChange.getDocument().getString("obra"));
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                    timerObs.start();
                }
            }
        }.start();
    }

    private void firestoreNombres() {
        menu.cargando(true);
        touch(true);
        alreadyJfs = false;
        geoFirestoreRefJfs = mDb.collection("Empresas").document(empresa).collection("Empleado");
        CollectionReference geoFirestoreRef2Jfs = mDb.collection("Empresas").document(empresa).collection("Administrador");
        jfs = new ArrayList<>();
        emailList = new ArrayList<>();
        emailList.clear();
        jfs.clear();
        geoFirestoreRef2Jfs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                List<Task<QuerySnapshot>> tasks3 = new ArrayList<>();
                if (task2.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task2.getResult()) {
                        String jefe = document.getString("nombre");
                        if (!jfs.contains(jefe) && !document.getBoolean("desactivado")) {
                            jfs.add(jefe);
                        }
                    }
                } else if (!task2.isSuccessful()) {
                    jfs.add("sin empleados");
                }
                return Tasks.whenAllSuccess(tasks3);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                geoFirestoreRefJfs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                    @Override
                    public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task1) {
                        List<Task<QuerySnapshot>> tasks4 = new ArrayList<>();
                        if (task1.isSuccessful()) {
                            for (final QueryDocumentSnapshot document1 : task1.getResult()) {
                                String jefe = document1.getString("nombre");
                                if (!jfs.contains(jefe)) {
                                    if (jefe != null && !document1.getBoolean("desactivado")) {
                                        emailList.add(document1.getString("email"));
                                        jfs.add(jefe);
                                    }
                                }
                            }
                            emailList.remove(emailAn);
                            jfs.remove(nombre);
                        }
                        return Tasks.whenAllSuccess(tasks4);
                    }
                }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                        mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
                        jefeSpinner = mNombres.findViewById(R.id.spinnerObra);
                        jefeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, jfs);
                        jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        jefeSpinner.setAdapter(jefeAdapter);
                        readyJfs = true;
                        alreadyJfs = true;
                        menu.cargando(false);
                        touch(false);
                    }
                });
            }
        });
    }

    private void listenerJfs() {
        final int[] contador2 = {0};
        firestoreNombres();
        timerJfs = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                contador2[0]++;
                if (readyJfs) {
                    contador2[0] = 30;
                }
                if (contador2[0] == 30) {
                    timerJfs.cancel();
                    timerJfs.onFinish();
                }
            }

            @Override
            public void onFinish() {
                if (readyJfs) {
                    mDb.collection("Empresas").document(empresa).collection("Localizaciones Empleado").addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "onEvent: Listen failed", e);
                                return;
                            }
                            if (queryDocumentSnapshots != null) {
                                for (final DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (documentChange.getDocument().exists()) {
                                        switch (documentChange.getType()) {
                                            case ADDED:
                                            case MODIFIED:
                                            case REMOVED:
                                                if (alreadyJfs) {
                                                    if (documentChange.getDocument().getString("nombre").equals(nombre)) {
                                                        if (documentChange.getDocument().getBoolean("desactivado") != null) {
                                                            if (documentChange.getDocument().getBoolean("desactivado")) {
                                                                mAuth.signOut();
                                                                ((ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE))
                                                                        .clearApplicationUserData();
                                                                menu.finishTask();
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                    timerJfs.start();
                }
            }
        }.start();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        menu.cargando(true);
        touch(true);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, 60, 60);
        Bitmap bitmap = Bitmap.createBitmap(60, 60, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        menu.cargando(false);
        touch(false);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void anadirMarcadores(final GeoPoint geoPoint1, String title, String snippet, long onl) {
        menu.cargando(true);
        touch(true);
        Marker marker = markersMapObra.get(title);
        if (marker != null) {
            marker.remove();
            markersMapObra.remove(title);
            markersMapObras2.remove(title);
            obs.remove(title);
        }
        MarkerOptions mo = new MarkerOptions()
                .rotation(0)
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_casa));
        Marker mkr = mMap.addMarker(mo
                .title(title)
                .snippet(snippet)
                .position(new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude())));
        markersMapObra.put(title, mkr);
        markersMapObras2.put(geoPoint1, title);
        obs.add(title);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        menu.cargando(false);
        touch(false);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(gps)) {
            if (!ayuda) {
                localizacion();
            } else {
                if (ayudaIcGps.getVisibility() == View.GONE) {
                    alertaAyuda.setVisibility(View.GONE);
                    ayudaGeneral.setVisibility(View.GONE);
                    ayudaPolitica.setVisibility(View.GONE);
                    ayudaIcJornada.setVisibility(View.GONE);
                    ayudaSalir.setVisibility(VISIBLE);
                    gps.clearAnimation();
                    icInicio.startAnimation(anim);
                    icAyuda.startAnimation(anim);
                    xpand2.startAnimation(anim);
                    ayudaIcGps.setVisibility(View.VISIBLE);
                } else {
                    ayudaIcGps.setVisibility(View.GONE);
                    gps.setAnimation(anim);
                }
            }

        } else if (v.equals(icInicio)) {
            if (!ayuda) {
                dQuien(false, null);
            } else {
                if (ayudaIcJornada.getVisibility() == View.GONE) {
                    alertaAyuda.setVisibility(View.GONE);
                    ayudaGeneral.setVisibility(View.GONE);
                    ayudaPolitica.setVisibility(View.GONE);
                    ayudaIcGps.setVisibility(View.GONE);
                    ayudaSalir.setVisibility(VISIBLE);
                    icInicio.clearAnimation();
                    icAyuda.startAnimation(anim);
                    gps.startAnimation(anim);
                    xpand2.startAnimation(anim);
                    ayudaIcJornada.setVisibility(View.VISIBLE);
                } else {
                    ayudaIcJornada.setVisibility(View.GONE);
                    icInicio.setAnimation(anim);
                }
            }
        } else if (v.equals(icAyuda)) {
            if (!ayuda) {
                ayuda = true;
                viewGreyNormal = getView().findViewById(R.id.viewGrey2);
                alertaAyuda = getView().findViewById(R.id.alerta_ayuda);
                ayudaGeneral = getView().findViewById(R.id.ayuda_general_empleados_mapa);
                ayudaPolitica = getView().findViewById(R.id.ayuda_politica_de_privacidad);
                ayudaIcJornada = getView().findViewById(R.id.ayuda_ic_inicia);
                ayudaIcGps = getView().findViewById(R.id.ayuda_ic_gps);
                ayudaExpand = getView().findViewById(R.id.ayuda_expand);
                ayudaSalir = getView().findViewById(R.id.ayuda_salir);
                anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(400);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                icInicio.startAnimation(anim);
                icAyuda.startAnimation(anim);
                gps.startAnimation(anim);
                xpand2.startAnimation(anim);
                viewGreyNormal.setVisibility(View.VISIBLE);
                alertaAyuda.setVisibility(View.VISIBLE);
                ayudaGeneral.setVisibility(View.VISIBLE);
                ayudaExpand.setVisibility(VISIBLE);
                ayudaSalir.setVisibility(VISIBLE);
                viewGreyExpande = getView().findViewById(R.id.viewGrey3);
                ayudaObras = getView().findViewById(R.id.ayuda_obras_empleado);
            } else {
                icInicio.clearAnimation();
                icAyuda.clearAnimation();
                gps.clearAnimation();
                xpand2.clearAnimation();
                alertaAyuda.setVisibility(View.GONE);
                ayudaGeneral.setVisibility(View.GONE);
                ayudaPolitica.setVisibility(View.GONE);
                ayudaIcGps.setVisibility(View.GONE);
                ayudaExpand.setVisibility(View.GONE);
                ayudaIcJornada.setVisibility(View.GONE);
                viewGreyNormal.setVisibility(View.GONE);
                ayudaSalir.setVisibility(View.GONE);
                ayuda = false;
            }
        } else if (v.equals(xpand2)) {

            ocultarTeclado();
            if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                if (menu.getCambioDeFragment()) {
                    setUpRecyclerView();
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    menu.setCambioDeFragmento(false);
                } else {
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                xpand2.setImageResource(R.drawable.ic_expand_up);
                if (ayuda) {
                    alertaAyuda.setVisibility(VISIBLE);
                    ayudaGeneral.setVisibility(VISIBLE);
                    ayudaPolitica.setVisibility(View.GONE);
                    ayudaIcGps.setVisibility(View.GONE);
                    ayudaIcJornada.setVisibility(View.GONE);
                    viewGreyExpande.setVisibility(View.GONE);
                    ayudaExpand.setVisibility(VISIBLE);
                    ayudaSalir.setVisibility(VISIBLE);
                    ayudaObras.setVisibility(View.GONE);
                    icInicio.startAnimation(anim);
                    icAyuda.startAnimation(anim);
                    gps.startAnimation(anim);
                    xpand2.startAnimation(anim);
                }

            } else if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                if (ayuda) {
                    alertaAyuda.setVisibility(View.GONE);
                    ayudaGeneral.setVisibility(View.GONE);
                    ayudaPolitica.setVisibility(View.GONE);
                    ayudaIcGps.setVisibility(View.GONE);
                    ayudaIcJornada.setVisibility(View.GONE);
                    ayudaSalir.setVisibility(View.GONE);
                    ayudaExpand.setVisibility(View.GONE);
                    viewGreyExpande.setVisibility(VISIBLE);
                    ayudaObras.setVisibility(VISIBLE);
                    icInicio.startAnimation(anim);
                    icAyuda.startAnimation(anim);
                    gps.startAnimation(anim);
                    xpand2.startAnimation(anim);
                }
                if (menu.getCambioDeFragment()) {
                    setUpRecyclerView();
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    menu.setCambioDeFragmento(false);
                } else {
                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
                xpand2.setImageResource(R.drawable.ic_expand_down);

            }


        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String obr = marker.getTitle();
        if (markersMapObra.get(obr) != null) {
            dQuien(true, obr);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), (float) Math.floor(mMap
                        .getCameraPosition().zoom + 1)), 300,
                null);
        if (marker.isInfoWindowShown()) {
            marker.hideInfoWindow();
        } else {
            marker.showInfoWindow();
        }
        return true;
    }
}

