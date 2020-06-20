package com.japac.pac.menu.administradores;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
import com.google.android.material.snackbar.Snackbar;
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
import com.japac.pac.localizacion.localizacionObra;
import com.japac.pac.localizacion.localizacionUsuario;
import com.japac.pac.marcadores.marcadoresObras;
import com.japac.pac.menu.menu;
import com.japac.pac.R;
import com.japac.pac.adaptadorObrasLista;
import com.japac.pac.servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */

public class menuPrincipalAdministradores extends Fragment implements OnMapReadyCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        CompoundButton.OnCheckedChangeListener {

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

    private final Map<String, Marker> markersMapEmpleado = new HashMap<>();

    private CountDownTimer timerObs, timerJfs, timerMap;

    private String mislisT, id, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, sobra, busquedaString, ob, jf, jefes, codigoEmpleadoChech, JFC, JFO, hEntrada, hSalida, hEntrada2, hSalida2;
    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ArrayList<String> DLS = new ArrayList<>();
    private ArrayList<String> diasLibresSemana = new ArrayList<>();
    private static final int ERROR_DIALOGO_PEDIR = 9001;
    private static final float ZOOM_PREDETERMINADO = 20f;

    private ArrayList<String> obs = new ArrayList<>(), jfs = new ArrayList<>(), lM;

    private FloatingActionButton icCrear, gps, icHorario, icDiasLS;

    private View mNombres;
    private View mDosText;
    private View mDos;
    private View mTres;

    private EditText mBuscar;

    private TextView pPt;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizaDirc, geoPointLocalizayo;

    private localizacionUsuario mLocalizarUsuario;

    private localizacionObra mLocalizacionObra;

    private FirebaseFirestore mDb;

    private LatLng mLocaliza, mLocalizaAddress;

    private Marker marcadorCrea;

    private boolean obraBo = false, igual = false, crearMark = false, cambiar = false, ElimJefe = false, arrastrado = false, readyObs = false, readyJfs = false, alreadyObs = false, alreadyJfs = false;

    private ArrayAdapter<String> jefeAdapter;

    private Spinner jefeSpinner;

    private Double direccionLat, direccionLong;

    private SlidingUpPanelLayout slidingLayout2;

    private ImageView xpand2;

    private adaptadorObrasLista adaptadorObrasLista;

    private CollectionReference geoFirestoreRefObs;
    private CollectionReference geoFirestoreRefJfs;

    public menuPrincipalAdministradores() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_menu_principal, container, false);
        if (compruebapermisos() && isServicesOK()) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            id = mAuth.getCurrentUser().getUid();
            mDb = FirebaseFirestore.getInstance();
            FirebaseStorage almacen = FirebaseStorage.getInstance();
            mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        hEntrada = documentSnapshot.getString("hora de entrada");
                        hSalida = documentSnapshot.getString("hora de salida");
                        hEntrada2 = documentSnapshot.getString("hora de entrada partida");
                        hSalida2 = documentSnapshot.getString("hora de salida partida");
                        diasLibresSemana.clear();
                        diasLibresSemana = (ArrayList<String>) documentSnapshot.get("dias libres semana");
                        codigoEmpresa = documentSnapshot.getString("codigo empresa");
                        comp = documentSnapshot.getString("comprobar");
                        empresa = documentSnapshot.getString("empresa");
                        nombre = documentSnapshot.getString("nombre");
                        roles = documentSnapshot.getString("rol");
                        nombreAm = documentSnapshot.getString("nombre");
                        emailAn = documentSnapshot.getString("email");
                        codigoEmpleado = documentSnapshot.getString("codigo empleado");
                        if (documentSnapshot.contains("obra")) {
                            obcomprueba = documentSnapshot.getString( "obra");
                        }
                        if (hEntrada == null && hSalida == null) {
                            final TextView myMsgtitle = new TextView(getActivity());
                            myMsgtitle.setText("La empresa " + empresa + " no a designado el horario laboral" + "\npor favor designe el horario laboral inmediatamente");
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            myMsgtitle.setLayoutParams(params);
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);

                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                            final Button btnCamb = mDos.findViewById(R.id.btn1);
                            btnCamb.setText("Continuar");
                            final Button btnCancelar = mDos.findViewById(R.id.btn2);
                            final AlertDialog.Builder horarioCamb = new AlertDialog.Builder(requireContext())
                                    .setCustomTitle(myMsgtitle)
                                    .setView(mDos);
                            final AlertDialog dialogoHorarioCamb = horarioCamb.create();
                            btnCamb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoHorarioCamb.dismiss();
                                    tipoHorario();

                                }
                            });
                            btnCancelar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Snackbar snackbar = Snackbar.make(mDos, "Debe designar un horario laboral cuanto antes", 5000);
                                    final CountDownTimer timerAler = new CountDownTimer(5500, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        @Override
                                        public void onFinish() {
                                            snackbar.dismiss();
                                            dialogoHorarioCamb.dismiss();
                                            menu.snackbar.setText("Debe designar un horario laboral cuanto antes");
                                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(10);
                                            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                            menu.snackbar.show();
                                        }
                                    }.start();
                                    TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setTextSize(10);
                                    snackbarDS.configSnackbar(getActivity(), snackbar);
                                    snackbar.setAction("Continuar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogoHorarioCamb.dismiss();
                                            timerAler.cancel();
                                            tipoHorario();
                                        }
                                    }).setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                }
                            });
                            dialogoHorarioCamb.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCamb.setEnabled(true);
                                    btnCancelar.setEnabled(true);
                                }
                            });
                            dialogoHorarioCamb.setCanceledOnTouchOutside(false);
                            if (mDos.getParent() != null) {
                                ((ViewGroup) mDos.getParent()).removeView(mDos);
                                mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                dialogoHorarioCamb.show();
                            } else {
                                dialogoHorarioCamb.show();
                            }

                        } else if (diasLibresSemana == null) {
                            final TextView myMsgtitle = new TextView(getActivity());
                            myMsgtitle.setText("La empresa " + empresa + " no a designado los dias libres semanales" + "\npor favor designe los dias libres semanales inmediatamente");
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            myMsgtitle.setLayoutParams(params);
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);

                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                            final Button btnCamb = mDos.findViewById(R.id.btn1);
                            btnCamb.setText("Continuar");
                            final Button btnCancelar = mDos.findViewById(R.id.btn2);
                            final AlertDialog.Builder horarioCamb = new AlertDialog.Builder(requireContext())
                                    .setCustomTitle(myMsgtitle)
                                    .setView(mDos);
                            final AlertDialog dialogoHorarioCamb = horarioCamb.create();
                            btnCamb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoHorarioCamb.dismiss();
                                    diasLibresSemanasCambiar();

                                }
                            });
                            btnCancelar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Snackbar snackbar = Snackbar.make(mDos, "Debe designar los dias libres semanales cuanto antes", 5000);
                                    final CountDownTimer timerAler = new CountDownTimer(5500, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        @Override
                                        public void onFinish() {
                                            snackbar.dismiss();
                                            dialogoHorarioCamb.dismiss();
                                            menu.snackbar.setText("Debe designar los dias libres semanales cuanto antes");
                                            TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                            tv.setTextSize(10);
                                            snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                                            menu.snackbar.show();
                                        }
                                    }.start();
                                    TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setTextSize(10);
                                    snackbarDS.configSnackbar(getActivity(), snackbar);
                                    snackbar.setAction("Continuar", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogoHorarioCamb.dismiss();
                                            timerAler.cancel();
                                            diasLibresSemanasCambiar();
                                        }
                                    }).setActionTextColor(Color.WHITE);
                                    snackbar.show();

                                }
                            });
                            dialogoHorarioCamb.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    btnCamb.setEnabled(true);
                                    btnCancelar.setEnabled(true);
                                }
                            });
                            dialogoHorarioCamb.setCanceledOnTouchOutside(false);
                            if (mDos.getParent() != null) {
                                ((ViewGroup) mDos.getParent()).removeView(mDos);
                                mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                dialogoHorarioCamb.show();
                            } else {
                                dialogoHorarioCamb.show();
                            }
                        }
                        slidingLayout2 = requireView().findViewById(R.id.sliding_layout2);
                        slidingLayout2.setTouchEnabled(false);
                        xpand2 = getView().findViewById(R.id.btnXpand2);
                        mBuscar = getView().findViewById(R.id.input_buscar);
                        xpand2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
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

                                } else if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
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
                        });
                        slidingLayout2.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                            @Override
                            public void onPanelSlide(View panel, float slideOffset) {

                            }

                            @Override
                            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                                if (previousState.equals(SlidingUpPanelLayout.PanelState.EXPANDED) && newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                    slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                }
                            }
                        });
                        gps = getView().findViewById(R.id.ic_gps);
                        icCrear = getView().findViewById(R.id.ic_crearObra);
                        icHorario = getView().findViewById(R.id.ic_horario);
                        icDiasLS = getView().findViewById(R.id.ic_diasLS);
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

    private void setUpRecyclerView() {
        Query query = mDb.collection("Empresas").document(empresa).collection("Obras").orderBy("online", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<marcadoresObras> options = new FirestoreRecyclerOptions.Builder<marcadoresObras>()
                .setQuery(query, marcadoresObras.class)
                .build();

        adaptadorObrasLista = new adaptadorObrasLista(options);

        RecyclerView recyclerView = requireView().findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptadorObrasLista);
        adaptadorObrasLista.startListening();
        adaptadorObrasLista.setOnItemClickListener(new adaptadorObrasLista.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                setCamara();
                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
                dAdmin(adaptadorObrasLista.getItem(viewHolder.getAdapterPosition()).getObra());
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

        mDb.collection("Empresas").document(empresa).collection("Localizaciones Administrador").document(nombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    .collection("Localizaciones Administrador")
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
        gps.setOnClickListener(menuPrincipalAdministradores.this);
        icCrear.setOnClickListener(menuPrincipalAdministradores.this);
        icHorario.setOnClickListener(menuPrincipalAdministradores.this);
        icDiasLS.setOnClickListener(menuPrincipalAdministradores.this);

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
                        if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                            slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
                            MarkerOptions options = new MarkerOptions()
                                    .title("Crear obra")
                                    .position(new LatLng(address.getLatitude(), address.getLongitude()))
                                    .draggable(true)
                                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_add_obra));
                            if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), ZOOM_PREDETERMINADO));

                            if (marcadorCrea != null) {

                                marcadorCrea.remove();

                            }

                            marcadorCrea = mMap.addMarker(options);
                            marcadorCrea.showInfoWindow();
                            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                @Override
                                public void onMarkerDragStart(Marker marker) {
                                    arrastrado = true;
                                }

                                @Override
                                public void onMarkerDrag(Marker marker) {

                                }

                                @Override
                                public void onMarkerDragEnd(Marker marker) {

                                }
                            });
                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    marcadorCrea.remove();
                                    crearMark = true;
                                    if (!arrastrado) {
                                        direccionLat = address.getLatitude();
                                        direccionLong = address.getLongitude();
                                    } else {
                                        direccionLat = marker.getPosition().latitude;
                                        direccionLong = marker.getPosition().longitude;
                                    }
                                    mLocalizaAddress = new LatLng(direccionLat, direccionLong);
                                    mMap.setOnInfoWindowClickListener(this);
                                    crearObra();
                                }
                            });
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
                            geoPointLocalizayo = new GeoPoint(locacizacionActual.getLatitude(), locacizacionActual.getLongitude());
                            final int[] online = new int[1];
                            online[0] = 0;
                            if (obraBo) {
                                obraBo = false;
                                if (crearMark) {
                                    geoPointLocalizaDirc = new GeoPoint(mLocalizaAddress.latitude, mLocalizaAddress.longitude);
                                    mLocalizacionObra.setGeoPoint(geoPointLocalizaDirc);
                                } else {
                                    mLocalizacionObra.setGeoPoint(geoPointLocalizayo);
                                }
                                crearMark = false;
                                if (igual) {
                                    igual = false;
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(ob.toLowerCase().trim()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            online[0] = documentSnapshot.getLong("online").intValue();
                                            mDb.collection("Empresas").document(empresa).collection("Obras").document(ob.toLowerCase().trim()).delete();
                                        }
                                    });
                                }
                                mLocalizacionObra.setOnline(online[0]);
                                mDb.collection("Empresas").document(empresa).collection("Obras").document(sobra.toLowerCase().trim()).set(mLocalizacionObra);
                            } else {
                                mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                                mLocalizarUsuario.setTimestamp(null);
                            }
                            menu.cargando(false);
                            touch(false);
                            guardarLocalizacion();
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
        mapFragment.getMapAsync(menuPrincipalAdministradores.this);
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
                        if (sobra != null) {
                            mDb.collection("Empresas").document(empresa).collection("Obras").document(sobra.toLowerCase().trim()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(documentSnapshot.getGeoPoint("geoPoint").getLatitude(), documentSnapshot.getGeoPoint("geoPoint").getLongitude()), ZOOM_PREDETERMINADO));
                                    }
                                }
                            });
                        }
                        sobra = null;
                        mBuscar.getText().clear();
                        busquedaString = null;
                        readyObs = true;
                        alreadyObs = true;

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
        if (markersMapEmpleado != null) {
            markersMapEmpleado.clear();
        }
        geoFirestoreRefJfs = mDb.collection("Empresas").document(empresa).collection("Empleado");
        CollectionReference geoFirestoreRef2Jfs = mDb.collection("Empresas").document(empresa).collection("Administrador");
        if (jfs != null) {
            jfs.clear();
        }
        geoFirestoreRef2Jfs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                List<Task<QuerySnapshot>> tasks3 = new ArrayList<>();
                if (task2.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task2.getResult()) {
                        String jefe = document.getString("nombre");
                        if (!jfs.contains(jefe)) {
                            jfs.add(jefe);
                        }
                    }
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
                                    if (jefe != null) {
                                        if (!document1.getBoolean("desactivado")) {
                                            jfs.add(jefe);
                                        }
                                    }
                                }
                            }
                            jfs.remove(nombreAm);
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
                        lM = new ArrayList();
                        lM.clear();
                        mDb.collection("Empresas").document(empresa).collection("Localizaciones Empleado").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                            @Override
                            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task1) {
                                List<Task<QuerySnapshot>> tasks4 = new ArrayList<>();
                                if (task1.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document2 : task1.getResult()) {
                                        String jefe = document2.getString("nombre");
                                        if (!lM.contains(jefe)) {
                                            if (jefe != null) {
                                                lM.add(jefe);
                                                if (document2.getBoolean("desactivado") != null) {
                                                    if (!document2.getBoolean("desactivado")) {
                                                        anadirMarcadoresEmpleados(document2.getGeoPoint("geoPoint"), document2.getString("nombre"), document2.getString("obra"), document2.getString("id"), document2.getString("estado"));
                                                    }
                                                } else {
                                                    anadirMarcadoresEmpleados(document2.getGeoPoint("geoPoint"), document2.getString("nombre"), document2.getString("obra"), document2.getString("id"), document2.getString("estado"));
                                                }
                                            }
                                        }
                                    }
                                }
                                return Tasks.whenAllSuccess(tasks4);
                            }
                        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                                readyJfs = true;
                                alreadyJfs = true;
                                menu.cargando(false);
                                touch(false);
                            }
                        });
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
                                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (documentChange.getDocument().exists()) {
                                        switch (documentChange.getType()) {
                                            case ADDED:
                                            case MODIFIED:
                                                if (alreadyJfs) {
                                                    if (documentChange.getDocument().getBoolean("desactivado") != null) {
                                                        if (!documentChange.getDocument().getBoolean("desactivado")) {
                                                            String obN = documentChange.getDocument().getString("obra");
                                                            anadirMarcadoresEmpleados(documentChange.getDocument().getGeoPoint("geoPoint"), documentChange.getDocument().getString("nombre"), obN, documentChange.getDocument().getString("id"), documentChange.getDocument().getString("estado"));
                                                        } else if (documentChange.getDocument().getBoolean("desactivado")) {
                                                            Marker marker = markersMapEmpleado.get(documentChange.getDocument().getString("nombre"));
                                                            if (marker != null) {
                                                                marker.remove();
                                                                markersMapEmpleado.remove(documentChange.getDocument().getString("nombre"));
                                                                jfs.remove(documentChange.getDocument().getString("nombre"));
                                                            }
                                                        }
                                                    }
                                                }
                                                break;
                                            case REMOVED:
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

    private void anadirMarcadoresEmpleados(final GeoPoint geoPoint1, String nombre, String obra, String id, String estado) {
        menu.cargando(true);
        touch(true);
        Marker marker = markersMapEmpleado.get(nombre);
        if (marker != null) {
            marker.remove();
            markersMapEmpleado.remove(nombre);
            jfs.remove(nombre);
        }
        MarkerOptions mo = new MarkerOptions()
                .rotation(0)
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_empleados));
        if (obra != null) {
            obra = "Trabajando en " + obra;
        }
        if (estado.equals("online")) {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_empleado_online));
        } else if (estado.equals("offline")) {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_empleado_offline));
        } else if (estado.equals("offline full")) {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_empleados));
        } else {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_empleados));
        }
        Marker mkr = mMap.addMarker(mo
                .title(nombre)
                .position(new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude())));
        if (obra != null) {
            mkr.setSnippet(obra);
        }
        markersMapEmpleado.put(nombre, mkr);
        if (!jfs.contains(nombre)) {
            jfs.add(nombre);
        }
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        menu.cargando(false);
        touch(false);
    }

    private void dAdmin(final String obraAd) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("menu de administracion de la obra " + obraAd);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        View mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
        final Button btnCam = mCuatroBtn.findViewById(R.id.btn1);
        btnCam.setText("Cambiar nombre");
        final Button btnElim = mCuatroBtn.findViewById(R.id.btn2);
        btnElim.setText("Eliminar obra");
        final Button btnAdJef = mCuatroBtn.findViewById(R.id.btn3);
        btnAdJef.setText("Administrar jefe");
        final Button btnCance = mCuatroBtn.findViewById(R.id.Cancelar);
        btnCance.setText("Cancelar");
        final AlertDialog.Builder obraAdministrarObras = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mCuatroBtn);
        final AlertDialog dialogoAdministradorObras = obraAdministrarObras.create();
        dialogoAdministradorObras.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCam.setEnabled(true);
                btnAdJef.setEnabled(true);
                btnCance.setEnabled(true);
                btnElim.setEnabled(true);
            }
        });
        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorObras.dismiss();
                dCambiaNombreObra(obraAd);
            }
        });
        btnElim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorObras.dismiss();
                dObraEliminar(obraAd);
            }
        });
        btnAdJef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorObras.dismiss();
                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String jefe = documentSnapshot.getString("jefe");
                            if (!jefe.equals("no") && !jefe.equals("")) {
                                dJefeExiste(obraAd, jefe);
                            } else {
                                dJefes(obraAd, jefe);
                            }
                        }
                    }
                });
            }
        });
        btnCance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorObras.dismiss();
            }
        });
        dialogoAdministradorObras.setCanceledOnTouchOutside(false);
        if (mCuatroBtn.getParent() != null) {
            ((ViewGroup) mCuatroBtn.getParent()).removeView(mCuatroBtn);
            mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_cuatrobtn, null);
            dialogoAdministradorObras.show();
        } else {
            dialogoAdministradorObras.show();
        }

    }

    private void CambiarNombre(String obraAn, final String obraNu) {
        menu.cargando(true);
        touch(true);
        mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAn).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final GeoPoint geoPointAn = documentSnapshot.getGeoPoint("geoPoint");
                final String obraAn = documentSnapshot.getString("obra");
                final String jefeAn = documentSnapshot.getString("jefe");
                final int online = documentSnapshot.getLong("online").intValue();
                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAn).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mLocalizacionObra = new localizacionObra();
                        mLocalizacionObra.setGeoPoint(geoPointAn);
                        mLocalizacionObra.setObra(obraNu);
                        mLocalizacionObra.setOnline(online);
                        if (!jefeAn.equals("no")) {
                            mLocalizacionObra.setJefe(jefeAn);
                            mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String codEmpl = documentSnapshot.getString(jefeAn);
                                    if (codEmpl.contains("," + obraAn)) {
                                        codEmpl = codEmpl.replace("," + obraAn, "," + obraNu);

                                    } else if (codEmpl.contains("/" + obraAn)) {
                                        if (codEmpl.contains("/" + obraAn + ",")) {
                                            codEmpl = codEmpl.replace("/" + obraAn + ",", "/" + obraNu + ",");
                                        } else {
                                            codEmpl = codEmpl.replace("JeF/" + obraAn, "JeF/" + obraNu);
                                        }
                                    }
                                    final String finalCodEmpl = codEmpl;
                                    mDb.collection("Codigos").document(codigoEmpresa).update(jefeAn, codEmpl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefeAn).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    final String idNu = documentSnapshot.getString("id");
                                                    String jf = documentSnapshot.getString("jefe");
                                                    if (jf.contains("," + obraAn)) {
                                                        jf = jf.replace("," + obraAn, "," + obraNu);
                                                    } else if (jf.contains(obraAn + ",")) {
                                                        jf = jf.replace(obraAn + ",", obraNu + ",");
                                                    } else if (jf.contains(obraAn)) {
                                                        jf = jf.replace(obraAn, obraNu);
                                                    }
                                                    final String finalJf = jf;
                                                    mDb.collection("Todas las ids").document(idNu).update("codigo empleado", finalCodEmpl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefeAn).update("codigo empleado", finalCodEmpl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefeAn).update("jefe", finalJf).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mDb.collection("Todas las ids").document(idNu).update("jefe", finalJf);
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
                            mLocalizacionObra.setJefe("no");
                        }
                        mDb.collection("Empresas").document(empresa).collection("Obras").document(obraNu).set(mLocalizacionObra).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraNu).update("obra antigua", obraAn).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
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

    private void dCambiaNombreObra(final String obraAn) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Nuevo nombre para la obra " + obraAn);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        final EditText sNuevoNom = mDosText.findViewById(R.id.TextDos);
        final Button btnCamb = mDosText.findViewById(R.id.btn1);
        final Button btnCancelar = mDosText.findViewById(R.id.btn2);
        final AlertDialog.Builder alerta = new AlertDialog.Builder(getContext());
        alerta.setCustomTitle(myMsgtitle)
                .setView(mDosText);
        final AlertDialog dialogoAlerta = alerta.create();
        btnCamb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String obraNu = sNuevoNom.getText().toString();
                if (!obraNu.isEmpty()) {
                    dialogoAlerta.dismiss();
                    sNuevoNom.setError(null);
                    sNuevoNom.setHintTextColor(Color.GRAY);
                    CambiarNombre(obraAn.toLowerCase().trim(), obraNu);
                } else {
                    sNuevoNom.setHintTextColor(Color.RED);
                    sNuevoNom.setError("¿Cual es el nuevo nombre de la obra?");
                }
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAlerta.dismiss();
            }
        });
        dialogoAlerta.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                sNuevoNom.setEnabled(true);
                btnCamb.setEnabled(true);
                btnCancelar.setEnabled(true);
            }
        });
        dialogoAlerta.setCanceledOnTouchOutside(false);
        if (mDosText.getParent() != null) {
            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
            mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
            dialogoAlerta.show();
        } else {
            dialogoAlerta.show();
        }

    }

    private void dObraEliminar(final String obraAd) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿Seguro que quiere eliminar la obra " + obraAd + "?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnElim = mDos.findViewById(R.id.btn1);
        btnElim.setText("Eliminar");
        final Button btnCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraEliminar = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mDos);
        final AlertDialog dialogoObraEliminar = obraEliminar.create();
        btnElim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final String jef = documentSnapshot.getString("jefe");
                        if (!jef.equals("no")) {
                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jef).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        JFC = documentSnapshot.getString("codigo empleado");
                                        JFO = documentSnapshot.getString("jefe");
                                        if (JFC.contains("," + obraAd)) {
                                            JFC = JFC.replace("," + obraAd, "");

                                        } else if (JFC.contains("/" + obraAd)) {
                                            if (JFC.contains("/" + obraAd + ",")) {
                                                JFC = JFC.replace("/" + obraAd + ",", "/");
                                            } else {
                                                JFC = JFC.replace("JeF/" + obraAd, "");
                                            }
                                        }
                                        if (JFO.contains("," + obraAd)) {
                                            JFO = JFO.replace("," + obraAd, "");
                                        } else if (JFO.contains(obraAd + ",")) {
                                            JFO = JFO.replace(obraAd + ",", "");
                                        } else if (JFO.contains(obraAd)) {
                                            JFO = JFO.replace(obraAd, "");
                                        }
                                        ElimJefe = true;
                                        anadeJefes(obraAd, jef);
                                        dialogoObraEliminar.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    menu.cargando(true);
                                    touch(true);
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                    menu.cargando(false);
                                    touch(false);
                                    dialogoObraEliminar.dismiss();
                                    ElimJefe = false;
                                }
                            });
                        } else if (jef.equals("no")) {
                            menu.cargando(true);
                            touch(true);
                            mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                            menu.cargando(false);
                            touch(false);
                            dialogoObraEliminar.dismiss();
                            ElimJefe = false;
                        }
                    }
                });
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoObraEliminar.dismiss();
            }
        });
        dialogoObraEliminar.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnElim.setEnabled(true);
                btnCancelar.setEnabled(true);
            }
        });
        dialogoObraEliminar.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoObraEliminar.show();
        } else {
            dialogoObraEliminar.show();
        }
    }

    private void dJefeExiste(final String obraAd, final String obraAdJf) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("El jefe de obra de " + obraAd + " es " + obraAdJf + "\n¿Desea cambiarlo?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnCamb = mDos.findViewById(R.id.btn1);
        btnCamb.setText("Cambiar");
        final Button btnCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraJefeExiste = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle)
                .setView(mDos);
        final AlertDialog dialogoObraJefeExiste = obraJefeExiste.create();
        btnCamb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiar = true;
                dialogoObraJefeExiste.dismiss();
                dJefes(obraAd, obraAdJf);
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoObraJefeExiste.dismiss();
            }
        });
        dialogoObraJefeExiste.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCamb.setEnabled(true);
                btnCancelar.setEnabled(true);


            }
        });
        dialogoObraJefeExiste.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoObraJefeExiste.show();
        } else {
            dialogoObraJefeExiste.show();
        }

    }

    private void dJefes(final String obraAd, final String obraAdJf) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Seleccione el nuevo jefe para la obra " + obraAd);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        final Button btnSiguiente = mNombres.findViewById(R.id.btn1);
        final Button btnCancelar = mNombres.findViewById(R.id.btn2);
        jefeSpinner = mNombres.findViewById(R.id.spinnerObra);
        final AlertDialog.Builder obraJefe = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        jefeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, jfs);
        jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jefeSpinner.setAdapter(jefeAdapter);
        jefeSpinner.setOnItemSelectedListener(this);
        obraJefe
                .setView(mNombres);
        final AlertDialog dialogoObraJefe = obraJefe.create();
        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jefes.equals(obraAdJf)) {
                    menu.snackbar.setText(obraAdJf + " ya es el jefe de la obra " + obraAd);
                    TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextSize(10);
                    snackbarDS.configSnackbar(getActivity(), menu.snackbar);
                    menu.snackbar.show();
                    localizacion();
                    dialogoObraJefe.dismiss();
                } else {
                    anadeJefes(obraAd, obraAdJf);
                    dialogoObraJefe.dismiss();
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoObraJefe.dismiss();
            }
        });
        dialogoObraJefe.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                jefeSpinner.setEnabled(true);
                btnSiguiente.setEnabled(true);
                btnCancelar.setEnabled(true);
            }
        });
        dialogoObraJefe.setCanceledOnTouchOutside(false);
        if (mNombres.getParent() != null) {
            ((ViewGroup) mNombres.getParent()).removeView(mNombres);
            dialogoObraJefe.show();
        } else {
            dialogoObraJefe.show();
        }
    }

    private void anadeJefes(final String obraAd, final String obraAdJf) {
        menu.cargando(true);
        touch(true);
        if (ElimJefe) {
            jefes = obraAdJf;
        }
        Map<String, Object> jefes1 = new HashMap<>();
        jefes1.put("jefe", jefes);
        mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).update(jefes1);
        mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    codigoEmpleadoChech = documentSnapshot.getString(jefes);
                    if (codigoEmpleadoChech.length() >= 17) {
                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final String idNueva = documentSnapshot.getString("id");
                                final String cE = documentSnapshot.getString("codigo empleado");
                                mDb.collection("Todas las ids").document(idNueva).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {

                                            final String obr = documentSnapshot.getString("jefe");
                                            if (!ElimJefe) {
                                                JFC = cE + "," + obraAd;
                                                JFO = obr + "," + obraAd;
                                            } else {
                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                                ElimJefe = false;
                                            }
                                            final Map<String, Object> obE = new HashMap<>();
                                            obE.put("jefe", JFO);
                                            mDb.collection("Codigos").document(codigoEmpresa).update(jefes, JFC);
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).update(obE);
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).update("codigo empleado", JFC);
                                            mDb.collection("Todas las ids").document(idNueva).update(obE);
                                            mDb.collection("Todas las ids").document(idNueva).update("codigo empleado", JFC);
                                            mDb.collection("Jefes").document(idNueva).set(obE);
                                            menu.cargando(false);
                                            touch(false);
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final String idNueva = documentSnapshot.getString("id");
                                final String cE = documentSnapshot.getString("codigo empleado");
                                mDb.collection("Todas las ids").document(idNueva).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            if (!ElimJefe) {
                                                JFO = obraAd;
                                                JFC = cE + "JeF/" + obraAd;
                                            } else {
                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                                ElimJefe = false;
                                            }
                                            final Map<String, Object> ob1 = new HashMap<>();
                                            ob1.put("jefe", JFO);
                                            mDb.collection("Codigos").document(codigoEmpresa).update(jefes, JFC);
                                            mDb.collection("Codigos").document(codigoEmpresa).update(jefes, JFC);
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).update(ob1);
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).update("codigo empleado", JFC);
                                            mDb.collection("Todas las ids").document(idNueva).update(ob1);
                                            mDb.collection("Todas las ids").document(idNueva).update("codigo empleado", JFC);
                                            mDb.collection("Jefes").document(idNueva).set(ob1);
                                            menu.cargando(false);
                                            touch(false);
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
        if (cambiar) {
            mDb.collection("Empresas").document(empresa).collection("Empleado").document(obraAdJf).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        menu.cargando(true);
                        touch(true);
                        final String idCam = documentSnapshot.getString("id");
                        JFC = documentSnapshot.getString("codigo empleado");
                        JFO = documentSnapshot.getString("jefe");
                        if (JFC.contains("," + obraAd)) {
                            JFC = JFC.replace("," + obraAd, "");

                        } else if (JFC.contains("/" + obraAd)) {
                            if (JFC.contains("/" + obraAd + ",")) {
                                JFC = JFC.replace("/" + obraAd + ",", "/");
                            } else {
                                JFC = JFC.replace("JeF/" + obraAd, "");
                            }
                        }
                        if (JFO.contains("," + obraAd)) {
                            JFO = JFO.replace("," + obraAd, "");
                        } else if (JFO.contains(obraAd + ",")) {
                            JFO = JFO.replace(obraAd + ",", "");
                        } else if (JFO.contains(obraAd)) {
                            JFO = JFO.replace(obraAd, "");
                        }
                        final Map<String, Object> MapC = new HashMap<>();
                        MapC.put("codigo empleado", JFC);
                        MapC.put("jefe", JFO);
                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(obraAdJf).update(MapC);
                        mDb.collection("Todas las ids").document(idCam).update(MapC);
                        mDb.collection("Codigos").document(codigoEmpresa).update(obraAdJf, JFC);
                        mDb.collection("Jefes").document(idCam).update("jefe", JFO);
                        menu.cargando(false);
                        touch(false);
                    }
                }
            });
            cambiar = false;
        }
    }


    private void crearObra() {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿Que nombre tendra la obra?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        final AlertDialog.Builder obraCrear = new AlertDialog.Builder(icCrear.getContext());
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        final EditText obra = mDosText.findViewById(R.id.TextDos);
        obra.setHint("Nombre de la obra");
        final Button btnCrear = mDosText.findViewById(R.id.btn1);
        btnCrear.setText("Crear");
        final Button btnCancelar = mDosText.findViewById(R.id.btn2);
        obraCrear
                .setCustomTitle(myMsgtitle)
                .setView(mDosText);
        final AlertDialog dialogoObraCrear = obraCrear.create();
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sobra = obra.getText().toString().toLowerCase().trim();
                if (!sobra.isEmpty()) {
                    obra.setHintTextColor(Color.GRAY);
                    obra.setError(null);
                    mDb.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            mLocalizacionObra = new localizacionObra();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                ob = documentSnapshot.getString("obra");
                                if (ob.equalsIgnoreCase(sobra)) {
                                    jf = documentSnapshot.getString("jefe");
                                    igual = true;
                                    break;
                                }
                            }
                            if (igual) {
                                igual = false;
                                dObraExiste();
                            } else {
                                mLocalizacionObra.setJefe("no");
                                mLocalizacionObra.setObra(sobra);
                                obraBo = true;
                                localizacion();
                                dialogoObraCrear.dismiss();
                            }
                        }
                    });
                    dialogoObraCrear.dismiss();
                } else {
                    obra.setHintTextColor(Color.RED);
                    obra.setError("Identifique la obra");
                }
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoObraCrear.dismiss();
            }
        });
        dialogoObraCrear.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                obra.setEnabled(true);
                btnCrear.setEnabled(true);
                btnCancelar.setEnabled(true);
            }
        });
        dialogoObraCrear.setCanceledOnTouchOutside(false);
        if (mDosText.getParent() != null) {
            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
            mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
            dialogoObraCrear.show();
        } else {
            dialogoObraCrear.show();
        }

    }

    private void dObraExiste() {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("La obra " + ob + " ya existe\n¿Desea actualizar la localizacion de " + ob + "?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);

        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnAct = mDos.findViewById(R.id.btn1);
        btnAct.setText("Actualizar");
        final Button btnCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraExiste = new AlertDialog.Builder(icCrear.getContext());
        obraExiste
                .setCustomTitle(myMsgtitle)
                .setView(mDos);
        final AlertDialog dialogoObraExiste = obraExiste.create();
        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocalizacionObra = new localizacionObra();
                mLocalizacionObra.setJefe(jf);
                mLocalizacionObra.setObra(sobra);
                obraBo = true;
                localizacion();
                dialogoObraExiste.dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoObraExiste.dismiss();
            }
        });
        dialogoObraExiste.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnAct.setEnabled(true);
                btnCancelar.setEnabled(true);
            }
        });
        dialogoObraExiste.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoObraExiste.show();
        } else {
            dialogoObraExiste.show();
        }

    }

    private void tipoHorario() {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Que tipo de horario laboral tiene " + empresa);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button btnPartida = mTres.findViewById(R.id.btn1);
        btnPartida.setText("Jornada partida");
        final Button btnCompleta = mTres.findViewById(R.id.btn2);
        btnCompleta.setText("Jornada completa");
        final Button btnCancelar = mTres.findViewById(R.id.Cancelar);
        final AlertDialog.Builder horarioJornada = new AlertDialog.Builder(requireContext())
                .setCustomTitle(myMsgtitle)
                .setView(mTres);
        final AlertDialog dialogoJorarioJornada = horarioJornada.create();
        btnPartida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoJorarioJornada.dismiss();
                horarioCambiar(true);
            }
        });
        btnCompleta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoJorarioJornada.dismiss();
                horarioCambiar(false);
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoJorarioJornada.dismiss();
            }
        });
        dialogoJorarioJornada.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                btnCancelar.setEnabled(true);
                btnCompleta.setEnabled(true);
                btnPartida.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoJorarioJornada.setCanceledOnTouchOutside(false);
        if (mTres.getParent() != null) {
            ((ViewGroup) mTres.getParent()).removeView(mTres);
            mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoJorarioJornada.show();
        } else {
            dialogoJorarioJornada.show();
        }

    }

    private void horarioCambiar(final Boolean Partida) {
        menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿A que hora empiezan a trabajar sus empleados?");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.GREEN);
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
        final TimePicker timePicker = mDosText.findViewById(R.id.elegirHora);
        timePicker.setIs24HourView(true);
        timePicker.setHour(00);
        timePicker.setMinute(00);
        final Button btnCont = mDosText.findViewById(R.id.btn1);
        final Button btnCancelar = mDosText.findViewById(R.id.btn2);
        final AlertDialog.Builder alerta = new AlertDialog.Builder(getContext());
        alerta.setCustomTitle(myMsgtitle)
                .setView(mDosText);
        final AlertDialog dialogoAlerta = alerta.create();
        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DecimalFormat decimalFormat = new DecimalFormat("00");
                final String entrada = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                if (!entrada.isEmpty()) {
                    final TextView myMsgtitle = new TextView(getActivity());
                    if (Partida) {
                        myMsgtitle.setText("¿A que hora finalizan la primera mitad de la jornada partida sus empleados?");
                    } else {
                        myMsgtitle.setText("¿A que hora terminan de trabajar sus empleados?");
                    }
                    myMsgtitle.setTextColor(Color.RED);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    myMsgtitle.setLayoutParams(params);
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                    final TimePicker timePicker = mDosText.findViewById(R.id.elegirHora);
                    timePicker.setIs24HourView(true);
                    timePicker.setHour(00);
                    timePicker.setMinute(00);
                    final Button btnCon2 = mDosText.findViewById(R.id.btn1);
                    final Button btnCancelar2 = mDosText.findViewById(R.id.btn2);
                    btnCancelar2.setText("Atras");
                    final AlertDialog.Builder alerta2 = new AlertDialog.Builder(getContext());
                    alerta2.setCustomTitle(myMsgtitle)
                            .setView(mDosText);
                    final AlertDialog dialogoAlerta2 = alerta2.create();
                    btnCon2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Partida) {
                                final String salida2 = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                                if (Integer.parseInt(entrada.substring(0, 2)) >= Integer.parseInt(salida2.substring(0, 2))
                                        && Integer.parseInt(entrada.substring(3)) >= Integer.parseInt(salida2.substring(3))) {
                                    final Snackbar snackbar = Snackbar.make(mDosText, "La hora de salida no puede ser antes o igual a la de entrada", 5000);
                                    if (Integer.parseInt(entrada.substring(0, 2)) > Integer.parseInt(salida2.substring(0, 2))) {
                                        snackbar.setText("La hora de salida no puede ser antes que la de entrada");
                                    } else if (Integer.parseInt(entrada.substring(0, 2)) == Integer.parseInt(salida2.substring(0, 2))) {

                                        snackbar.setText("La hora de salida no puede ser igual a la de entrada");
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
                                    dialogoAlerta2.dismiss();
                                    horarioCambiarPartida(entrada, salida2);
                                }
                            } else {
                                final String salida = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                                if (Integer.parseInt(entrada.substring(0, 2)) >= Integer.parseInt(salida.substring(0, 2))
                                        && Integer.parseInt(entrada.substring(3)) >= Integer.parseInt(salida.substring(3))) {
                                    final Snackbar snackbar = Snackbar.make(mDosText, "La hora de salida no puede ser antes o igual a la de entrada", 5000);
                                    if (Integer.parseInt(entrada.substring(0, 2)) > Integer.parseInt(salida.substring(0, 2))) {
                                        snackbar.setText("La hora de salida no puede ser antes que la de entrada");
                                    } else if (Integer.parseInt(entrada.substring(0, 2)) == Integer.parseInt(salida.substring(0, 2))) {

                                        snackbar.setText("La hora de salida no puede ser igual a la de entrada");
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
                                    if (hEntrada != null && hSalida != null && hEntrada.equals(entrada) && hSalida.equals(salida) && hSalida2 == null && hEntrada2 == null) {
                                        final Snackbar snackbar = Snackbar.make(mDosText, "Su horario ya estaba asignado de " + entrada + " a " + salida, 5000);
                                        final CountDownTimer timerAler = new CountDownTimer(5000, 1000) {
                                            @Override
                                            public void onTick(long millisUntilFinished) {
                                            }

                                            @Override
                                            public void onFinish() {
                                                dialogoAlerta.dismiss();
                                            }
                                        }.start();
                                        TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                        tv.setTextSize(10);
                                        snackbarDS.configSnackbar(getActivity(), snackbar);
                                        snackbar.setAction("Entendido", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                                dialogoAlerta.dismiss();
                                                timerAler.cancel();
                                            }
                                        }).setActionTextColor(Color.WHITE);
                                        snackbar.show();
                                    } else {
                                        dialogoAlerta.dismiss();
                                        final TextView myMsgtitle = new TextView(getActivity());
                                        myMsgtitle.setText("¿Desea guardar el siguiente horario laboral?");
                                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        myMsgtitle.setLayoutParams(params);
                                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                        myMsgtitle.setTextColor(Color.GRAY);
                                        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                        final TextView textView = mDosText.findViewById(R.id.elegirHora);
                                        textView.setText("De " + entrada + " A " + salida);
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
                                        final Button btnConf = mDosText.findViewById(R.id.btn1);
                                        btnConf.setText("Confirmar");
                                        final Button btnCancelar3 = mDosText.findViewById(R.id.btn2);
                                        final AlertDialog.Builder alerta3 = new AlertDialog.Builder(getContext());
                                        alerta3.setCustomTitle(myMsgtitle)
                                                .setView(mDosText);
                                        final AlertDialog dialogoAlerta3 = alerta3.create();
                                        btnConf.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                menu.cargando(true);
                                                touch(true);
                                                final Map<String, Object> horario = new HashMap<>();
                                                if (hEntrada != null && !hEntrada.equals(entrada) || hEntrada == null) {
                                                    hEntrada = entrada;
                                                    horario.put("hora de entrada", entrada);
                                                }
                                                if (hSalida != null && !hSalida.equals(salida) || hSalida == null) {
                                                    hSalida = salida;
                                                    horario.put("hora de salida", salida);
                                                }
                                                if (hEntrada2 != null) {
                                                    horario.put("hora de entrada partida", FieldValue.delete());
                                                }
                                                if (hSalida2 != null) {
                                                    horario.put("hora de salida partida", FieldValue.delete());
                                                }


                                                mDb.collection("Todas las ids").document(id).set(horario, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Codigos").document(codigoEmpresa).set(horario, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                textView.clearAnimation();
                                                                dialogoAlerta3.dismiss();
                                                                menu.cargando(false);
                                                                touch(false);
                                                            }
                                                        });
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
                                                dialogoAlerta2.dismiss();
                                                textView.setEnabled(true);
                                                btnConf.setEnabled(true);
                                                btnCancelar3.setEnabled(true);
                                            }
                                        });
                                        dialogoAlerta3.setCanceledOnTouchOutside(false);
                                        if (mDosText.getParent() != null) {
                                            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
                                            mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                            dialogoAlerta3.show();
                                        } else {
                                            dialogoAlerta3.show();
                                        }

                                    }
                                }
                            }
                        }
                    });
                    btnCancelar2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogoAlerta2.dismiss();
                            horarioCambiar(Partida);
                        }
                    });
                    dialogoAlerta2.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            dialogoAlerta.dismiss();
                            timePicker.setEnabled(true);
                            btnCon2.setEnabled(true);
                            btnCancelar2.setEnabled(true);
                        }
                    });
                    dialogoAlerta2.setCanceledOnTouchOutside(false);
                    if (mDosText.getParent() != null) {
                        ((ViewGroup) mDosText.getParent()).removeView(mDosText);
                        mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                        dialogoAlerta2.show();
                    } else {
                        dialogoAlerta2.show();
                    }
                    dialogoAlerta.show();
                }
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAlerta.dismiss();
            }
        });
        dialogoAlerta.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                timePicker.setEnabled(true);
                btnCont.setEnabled(true);
                btnCancelar.setEnabled(true);
                menu.cargando(false);
                touch(false);
            }
        });
        dialogoAlerta.setCanceledOnTouchOutside(false);
        if (mDosText.getParent() != null) {
            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
            mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
            dialogoAlerta.show();
        } else {
            dialogoAlerta.show();
        }
    }

    private void horarioCambiarPartida(final String entrada, final String salida2) {

        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿A que hora comienzan la segunda mitad de la jornada partida sus empleados?");
        myMsgtitle.setTextColor(Color.GREEN);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
        final TimePicker timePicker = mDosText.findViewById(R.id.elegirHora);
        timePicker.setIs24HourView(true);
        timePicker.setHour(00);
        timePicker.setMinute(00);
        final Button btnCon3 = mDosText.findViewById(R.id.btn1);
        final Button btnCancelar3 = mDosText.findViewById(R.id.btn2);
        final AlertDialog.Builder alerta3 = new AlertDialog.Builder(getContext());
        alerta3.setCustomTitle(myMsgtitle)
                .setView(mDosText);
        final AlertDialog dialogoAlerta3 = alerta3.create();
        btnCon3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DecimalFormat decimalFormat = new DecimalFormat("00");
                final String entrada2 = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                if (Integer.parseInt(salida2.substring(0, 2)) >= Integer.parseInt(entrada2.substring(0, 2))
                        && Integer.parseInt(salida2.substring(3)) >= Integer.parseInt(entrada2.substring(3))) {
                    final Snackbar snackbar = Snackbar.make(mDosText, "La hora de entrada no puede ser antes o igual a la de salida", 5000);
                    if (Integer.parseInt(salida2.substring(0, 2)) > Integer.parseInt(entrada2.substring(0, 2))) {
                        snackbar.setText("La hora de entrada  no puede ser antes que la de salida");
                    } else if (Integer.parseInt(salida2.substring(0, 2)) == Integer.parseInt(entrada2.substring(0, 2))) {

                        snackbar.setText("La hora de entrada no puede ser igual a la de salida");
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
                    final TextView myMsgtitle = new TextView(getActivity());
                    myMsgtitle.setText("¿A que hora terminan de trabajar sus empleados?");
                    myMsgtitle.setTextColor(Color.RED);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    myMsgtitle.setLayoutParams(params);
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                    final TimePicker timePicker = mDosText.findViewById(R.id.elegirHora);
                    timePicker.setIs24HourView(true);
                    timePicker.setHour(00);
                    timePicker.setMinute(00);
                    final Button btnCon4 = mDosText.findViewById(R.id.btn1);
                    final Button btnCancelar4 = mDosText.findViewById(R.id.btn2);
                    btnCancelar4.setText("Atras");
                    final AlertDialog.Builder alerta4 = new AlertDialog.Builder(getContext());
                    alerta4.setCustomTitle(myMsgtitle)
                            .setView(mDosText);
                    final AlertDialog dialogoAlerta4 = alerta4.create();
                    btnCon4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final String salida = decimalFormat.format(timePicker.getHour()) + ":" + decimalFormat.format(timePicker.getMinute());
                            if (Integer.parseInt(entrada2.substring(0, 2)) >= Integer.parseInt(salida.substring(0, 2))
                                    && Integer.parseInt(entrada2.substring(3)) >= Integer.parseInt(salida.substring(3))) {
                                final Snackbar snackbar = Snackbar.make(mDosText, "La hora de salida no puede ser antes o igual a la de entrada", 5000);
                                if (Integer.parseInt(entrada2.substring(0, 2)) > Integer.parseInt(salida.substring(0, 2))) {
                                    snackbar.setText("La hora de salida no puede ser antes que la de entrada");
                                } else if (Integer.parseInt(entrada2.substring(0, 2)) == Integer.parseInt(salida.substring(0, 2))) {

                                    snackbar.setText("La hora de salida no puede ser igual a la de entrada");
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
                                if (hEntrada != null && hSalida2 != null && hEntrada2 != null && hSalida != null && hEntrada.equals(entrada) && hSalida2.equals(salida2) && hEntrada2.equals(entrada2) && hSalida.equals(salida)) {
                                    final Snackbar snackbar = Snackbar.make(mDosText, "Su horario partido ya estaba asignado de " + entrada + " a " + salida2 + " y de " + entrada2 + " a " + salida, 5000);
                                    final CountDownTimer timerAler = new CountDownTimer(5000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        @Override
                                        public void onFinish() {
                                            dialogoAlerta4.dismiss();
                                        }
                                    }.start();
                                    TextView tv = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
                                    tv.setTextSize(10);
                                    snackbarDS.configSnackbar(getActivity(), snackbar);
                                    snackbar.setAction("Entendido", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                            dialogoAlerta4.dismiss();
                                            timerAler.cancel();
                                        }
                                    }).setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else {
                                    final TextView myMsgtitle = new TextView(getActivity());
                                    myMsgtitle.setText("¿Desea guardar el siguiente horario laboral?");
                                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    myMsgtitle.setLayoutParams(params);
                                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                    myMsgtitle.setTextColor(Color.GRAY);
                                    mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                    final TextView textView = mDosText.findViewById(R.id.elegirHora);
                                    textView.setText("De " + entrada + " A " + salida2
                                            + "\nY"
                                            + "\n De " + entrada2 + " A " + salida);
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
                                    final Button btnConf = mDosText.findViewById(R.id.btn1);
                                    btnConf.setText("Confirmar");
                                    final Button btnCancelar5 = mDosText.findViewById(R.id.btn2);
                                    final AlertDialog.Builder alerta5 = new AlertDialog.Builder(getContext());
                                    alerta5.setCustomTitle(myMsgtitle)
                                            .setView(mDosText);
                                    final AlertDialog dialogoAlerta5 = alerta5.create();
                                    btnConf.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            menu.cargando(true);
                                            touch(true);
                                            final Map<String, Object> horario = new HashMap<>();
                                            if (hEntrada != null && !hEntrada.equals(entrada) || hEntrada == null) {
                                                hEntrada = entrada;
                                                horario.put("hora de entrada", entrada);
                                            }
                                            if (hSalida2 != null && !hSalida2.equals(salida2) || hSalida2 == null) {
                                                hSalida2 = salida2;
                                                horario.put("hora de salida partida", salida2);
                                            }
                                            if (hEntrada2 != null && !hEntrada2.equals(entrada2) || hEntrada2 == null) {
                                                hEntrada2 = entrada2;
                                                horario.put("hora de entrada partida", entrada2);
                                            }
                                            if (hSalida != null && !hSalida.equals(salida) || hSalida == null) {
                                                hSalida = salida;
                                                horario.put("hora de salida", salida);
                                            }
                                            mDb.collection("Todas las ids").document(id).set(horario, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDb.collection("Codigos").document(codigoEmpresa).set(horario, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            textView.clearAnimation();
                                                            dialogoAlerta5.dismiss();
                                                            menu.cargando(false);
                                                            touch(false);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                    btnCancelar5.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialogoAlerta5.dismiss();
                                        }
                                    });
                                    dialogoAlerta5.setOnShowListener(new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface dialog) {
                                            dialogoAlerta4.dismiss();
                                            textView.setEnabled(true);
                                            btnConf.setEnabled(true);
                                            btnCancelar3.setEnabled(true);
                                        }
                                    });
                                    dialogoAlerta5.setCanceledOnTouchOutside(false);
                                    if (mDosText.getParent() != null) {
                                        ((ViewGroup) mDosText.getParent()).removeView(mDosText);
                                        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_textview, null);
                                        dialogoAlerta5.show();
                                    } else {
                                        dialogoAlerta5.show();
                                    }
                                }
                            }
                        }
                    });
                    btnCancelar4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogoAlerta4.dismiss();
                            horarioCambiarPartida(entrada, salida2);
                        }
                    });
                    dialogoAlerta4.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            dialogoAlerta3.dismiss();
                            timePicker.setEnabled(true);
                            btnCon4.setEnabled(true);
                            btnCancelar4.setEnabled(true);
                        }
                    });
                    dialogoAlerta4.setCanceledOnTouchOutside(false);
                    if (mDosText.getParent() != null) {
                        ((ViewGroup) mDosText.getParent()).removeView(mDosText);
                        mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
                        dialogoAlerta4.show();
                    } else {
                        dialogoAlerta4.show();
                    }
                    dialogoAlerta3.show();
                }
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
                timePicker.setEnabled(true);
                btnCon3.setEnabled(true);
                btnCancelar3.setEnabled(true);
            }
        });
        dialogoAlerta3.setCanceledOnTouchOutside(false);
        if (mDosText.getParent() != null) {
            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
            mDosText = getLayoutInflater().inflate(R.layout.dialogo_horario, null);
            dialogoAlerta3.show();
        } else {
            dialogoAlerta3.show();
        }
    }

    private void diasLibresSemanasCambiar() {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("A continuacion seleccione los dias de descanso semanales");
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myMsgtitle.setLayoutParams(params);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.GRAY);
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dias_semana, null);
        final CheckBox checkLunes = mDosText.findViewById(R.id.check_lunes);
        final CheckBox checkLMartes = mDosText.findViewById(R.id.check_martes);
        final CheckBox checkMiercoles = mDosText.findViewById(R.id.check_miercoles);
        final CheckBox checkJueves = mDosText.findViewById(R.id.check_jueves);
        final CheckBox checkViernes = mDosText.findViewById(R.id.check_viernes);
        final CheckBox checkSabado = mDosText.findViewById(R.id.check_sabado);
        final CheckBox checkDomingo = mDosText.findViewById(R.id.check_domingo);
        checkLunes.setOnCheckedChangeListener(this);
        checkLMartes.setOnCheckedChangeListener(this);
        checkMiercoles.setOnCheckedChangeListener(this);
        checkJueves.setOnCheckedChangeListener(this);
        checkViernes.setOnCheckedChangeListener(this);
        checkSabado.setOnCheckedChangeListener(this);
        checkDomingo.setOnCheckedChangeListener(this);
        final Button btnConfDias = mDosText.findViewById(R.id.btn1);
        final Button btnCancelar4 = mDosText.findViewById(R.id.btn2);
        final AlertDialog.Builder alertaDias = new AlertDialog.Builder(getContext());
        alertaDias.setCustomTitle(myMsgtitle)
                .setView(mDosText);
        final AlertDialog dialogoAlertaDias = alertaDias.create();
        btnConfDias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.cargando(true);
                touch(true);
                if (!DLS.isEmpty()) {
                    diasLibresSemana = DLS;
                    final Map<String, ArrayList<String>> diasLS = new HashMap<>();
                    diasLS.put("dias libres semana", DLS);
                    mDb.collection("Todas las ids").document(id).set(diasLS, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDb.collection("Codigos").document(codigoEmpresa).set(diasLS, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialogoAlertaDias.dismiss();
                                    menu.cargando(false);
                                    touch(false);
                                }
                            });
                        }
                    });
                } else {
                    menu.cargando(false);
                    touch(false);
                    new CountDownTimer(2000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (myMsgtitle.getCurrentTextColor() == Color.RED) {
                                myMsgtitle.setTextColor(Color.GRAY);
                            } else {
                                myMsgtitle.setTextColor(Color.RED);
                            }
                            alertaDias.setCustomTitle(myMsgtitle);
                        }

                        @Override
                        public void onFinish() {
                            myMsgtitle.setTextColor(Color.GRAY);
                            alertaDias.setCustomTitle(myMsgtitle);
                        }
                    }.start();
                }
            }

        });
        btnCancelar4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAlertaDias.dismiss();
            }
        });
        dialogoAlertaDias.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                DLS.clear();
                checkLunes.setEnabled(true);
                checkLMartes.setEnabled(true);
                checkMiercoles.setEnabled(true);
                checkJueves.setEnabled(true);
                checkViernes.setEnabled(true);
                checkSabado.setEnabled(true);
                checkDomingo.setEnabled(true);
                btnConfDias.setEnabled(true);
                btnCancelar4.setEnabled(true);
            }
        });
        dialogoAlertaDias.setCanceledOnTouchOutside(false);
        if (mDosText.getParent() != null) {
            ((ViewGroup) mDosText.getParent()).removeView(mDosText);
            mDosText = getLayoutInflater().inflate(R.layout.dialogo_dias_semana, null);
            dialogoAlertaDias.show();
        } else {
            dialogoAlertaDias.show();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.equals(gps)) {

            localizacion();
        }
        if (v.equals(icCrear)) {

            slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            crearObra();
        }
        if (v.equals(icHorario)) {
            mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.getString("hora de entrada") != null && documentSnapshot.getString("hora de salida") != null) {
                        final TextView myMsgtitle = new TextView(getActivity());
                        if (documentSnapshot.getString("hora de salida partida") != null && documentSnapshot.getString("hora de entrada partida") != null) {
                            myMsgtitle.setText("El horario laboral de " + empresa
                                    + "\nactualmente es de " + documentSnapshot.getString("hora de entrada") + " A " + documentSnapshot.getString("hora de salida partida")
                                    + " Y De " + documentSnapshot.getString("hora de entrada partida") + " A " + documentSnapshot.getString("hora de salida")
                                    + "\n¿Desea modificarlo?");
                        } else {
                            myMsgtitle.setText("El horario laboral de " + empresa
                                    + "\nactualmente es de " + documentSnapshot.getString("hora de entrada") + " A " + documentSnapshot.getString("hora de salida")
                                    + "\n¿Desea modificarlo?");
                        }
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        myMsgtitle.setLayoutParams(params);
                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                        myMsgtitle.setTextColor(Color.BLACK);

                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                        final Button btnCamb = mDos.findViewById(R.id.btn1);
                        btnCamb.setText("Cambiar");
                        final Button btnCancelar = mDos.findViewById(R.id.btn2);
                        final AlertDialog.Builder horarioCamb = new AlertDialog.Builder(getContext())
                                .setCustomTitle(myMsgtitle)
                                .setView(mDos);
                        final AlertDialog dialogoHorarioCamb = horarioCamb.create();
                        btnCamb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogoHorarioCamb.dismiss();
                                tipoHorario();

                            }
                        });
                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogoHorarioCamb.dismiss();
                            }
                        });
                        dialogoHorarioCamb.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                btnCamb.setEnabled(true);
                                btnCancelar.setEnabled(true);


                            }
                        });
                        dialogoHorarioCamb.setCanceledOnTouchOutside(false);
                        if (mDos.getParent() != null) {
                            ((ViewGroup) mDos.getParent()).removeView(mDos);
                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                            dialogoHorarioCamb.show();
                        } else {
                            dialogoHorarioCamb.show();
                        }

                    } else {
                        tipoHorario();
                    }
                }
            });

        }
        if (v.equals(icDiasLS)) {
            mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (diasLibresSemana != null) {
                        final TextView myMsgtitle = new TextView(getActivity());
                        if (diasLibresSemana.size() == 2) {

                            myMsgtitle.setText("Actualmente estan asignados los siguientes dias libres semanales:\n"
                                    + "(" + diasLibresSemana.toString()
                                    .replaceAll("Monday", "Lunes")
                                    .replaceAll("Tuesday", "Martes")
                                    .replaceAll("Wednesday", "Miercoles")
                                    .replaceAll("Thursday", "Jueves")
                                    .replaceAll("Friday", "Viernes")
                                    .replaceAll("Saturday", "Sabado")
                                    .replaceAll("Sunday", "Domingo")
                                    .replaceAll(",", " y")
                                    .replaceAll("]", "")
                                    .replaceAll("\\[", "") + ")"
                                    + "\n ¿desea cambiarlos?");
                        } else if (diasLibresSemana.size() >= 3) {
                            myMsgtitle.setText("Actualmente estan asignados los siguientes dias libres semanales:\n"
                                    + "(" + diasLibresSemana.toString()
                                    .replaceAll("Monday", "Lunes")
                                    .replaceAll("Tuesday", "Martes")
                                    .replaceAll("Wednesday", "Miercoles")
                                    .replaceAll("Thursday", "Jueves")
                                    .replaceAll("Friday", "Viernes")
                                    .replaceAll("Saturday", "Sabado")
                                    .replaceAll("Sunday", "Domingo")
                                    .replaceAll("]", "")
                                    .replaceAll("\\[", "")
                                    .replaceFirst(",([^,]+)$", " y$1") + ")"
                                    + "\n ¿desea cambiarlos?");
                        } else {
                            myMsgtitle.setText("Actualmente estan asignados los siguientes dias libres semanales:\n"
                                    + "(" + diasLibresSemana.toString()
                                    .replaceAll("Monday", "Lunes")
                                    .replaceAll("Tuesday", "Martes")
                                    .replaceAll("Wednesday", "Miercoles")
                                    .replaceAll("Thursday", "Jueves")
                                    .replaceAll("Friday", "Viernes")
                                    .replaceAll("Saturday", "Sabado")
                                    .replaceAll("Sunday", "Domingo")
                                    .replaceAll("]", "")
                                    .replaceAll("\\[", "") + ")"
                                    + "\n ¿desea cambiarlos?");
                        }
                        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        myMsgtitle.setLayoutParams(params);
                        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                        myMsgtitle.setTextColor(Color.BLACK);

                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                        final Button btnCamb = mDos.findViewById(R.id.btn1);
                        btnCamb.setText("Cambiar");
                        final Button btnCancelar = mDos.findViewById(R.id.btn2);
                        final AlertDialog.Builder horarioCamb = new AlertDialog.Builder(requireContext())
                                .setCustomTitle(myMsgtitle)
                                .setView(mDos);
                        final AlertDialog dialogoHorarioCamb = horarioCamb.create();
                        btnCamb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogoHorarioCamb.dismiss();
                                diasLibresSemanasCambiar();

                            }
                        });
                        btnCancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogoHorarioCamb.dismiss();
                            }
                        });
                        dialogoHorarioCamb.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                btnCamb.setEnabled(true);
                                btnCancelar.setEnabled(true);
                            }
                        });
                        dialogoHorarioCamb.setCanceledOnTouchOutside(false);
                        if (mDos.getParent() != null) {
                            ((ViewGroup) mDos.getParent()).removeView(mDos);
                            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                            dialogoHorarioCamb.show();
                        } else {
                            dialogoHorarioCamb.show();
                        }


                    } else {
                        diasLibresSemanasCambiar();
                    }
                }
            });
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        jefes = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String obr = marker.getTitle();
        if (markersMapObra.get(obr) != null) {
            dAdmin(obr);
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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d("isChecked", String.valueOf(isChecked));
        String DLSEN = null;
        if (buttonView.getText().toString().equals("Lunes")) {
            DLSEN = "Monday";
        } else if (buttonView.getText().toString().equals("Martes")) {
            DLSEN = "Tuesday";
        } else if (buttonView.getText().toString().equals("Miercoles")) {
            DLSEN = "Wednesday";
        } else if (buttonView.getText().toString().equals("Jueves")) {
            DLSEN = "Thursday";
        } else if (buttonView.getText().toString().equals("Viernes")) {
            DLSEN = "Friday";
        } else if (buttonView.getText().toString().equals("Sabado")) {
            DLSEN = "Saturday";
        } else if (buttonView.getText().toString().equals("Domingo")) {
            DLSEN = "Sunday";
        }
        if (DLSEN != null) {
            if (isChecked) {
                Log.d("isChecked", "true");
                DLS.add(DLSEN);
            } else {
                Log.d("isChecked", "false");
                DLS.remove(DLSEN);
            }
        }
    }
}

