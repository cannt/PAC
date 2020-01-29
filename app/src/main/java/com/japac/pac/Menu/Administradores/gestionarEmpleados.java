package com.japac.pac.Menu.Administradores;


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
import androidx.annotation.Nullable;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.Localizacion.LocalizacionObra;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.Marcadores.MarcadoresEmpleados;
import com.japac.pac.Marcadores.MarcadoresObras;
import com.japac.pac.Menu.MenuAdmin;
import com.japac.pac.Menu.ViewPagers.Menu;
import com.japac.pac.R;
import com.japac.pac.adaptadorEmpleadosLista;
import com.japac.pac.adaptadorObrasLista;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class gestionarEmpleados extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getActivity(), "Mapa listo", Toast.LENGTH_SHORT).show();
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
                    mMap.clear();
                    firestoreNombres();
                    firestoreObras();
                }
            });
            listenerObs();
            listenerJfs();
            init();

        }
    }


    private Map<String, String> markersMapObras, markersMapEmpleado;

    private String id, IoF, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, sobra, busquedaString, ob, jf, jefes, codigoEmpleadoChech, JFC, JFO, sa, codigo;
    private FirebaseAuth mAuth;
    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int ERROR_DIALOGO_PEDIR = 9001;
    private static final float ZOOM_PREDETERMINADO = 20f;

    private List<String> obs, jfs;

    private FloatingActionButton icCrear, icReactivar, gps;

    private View mNombres, mCuatroBtn, mDosText, mDos;

    private EditText mBuscar;

    private TextView pPt, nom;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizaDirc, geoPointLocalizayo;

    private FusedLocationProviderClient mProovedor;

    private LocalizacionUsuario mLocalizarUsuario;

    private LocalizacionObra mLocalizacionObra;

    CollectionReference geoFirestoreRefJfs, geoFirestoreRef2Jfs, geoFirestoreRefObs, geoFirestoreRefObs2;

    FirebaseFirestore mDb;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private LatLngBounds mLocalizacionLaLo;

    private LatLng mLocaliza, mLocalizaAddress;

    private Marker marcadorCrea;

    private boolean obraBo = false, igual = false, crearMark = false, cambiar = false, ElimJefe = false, arrastrado = false;

    private ArrayAdapter<String> jefeAdapter;

    private Spinner jefeSpinner;

    private Double direccionLat, direccionLong;

    private SlidingUpPanelLayout slidingLayout2;

    private ImageView xpand2;

    private adaptadorEmpleadosLista adaptadorEmpleadosLista;

    private RecyclerView recyclerView;

    private ArrayList<LocalizacionUsuario> mLocalizacionUsuarios = new ArrayList<>();

    private CountDownTimer timerObs, timerJfs;

    private int count2 = 0;

    private ArrayList<String> lM;

    static final String patron = "0123456789BCDGHIKLMNOPQRSTUVWXYZbcdghiklmnopqrstuvwxyz";

    static SecureRandom aleatorio = new SecureRandom();

    public gestionarEmpleados() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (compruebapermisos() && isServicesOK()) {
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
                        if (documentSnapshot.contains("obra")) {
                            obcomprueba = documentSnapshot.getString("obra");
                        }
                        slidingLayout2 = (SlidingUpPanelLayout) getView().findViewById(R.id.sliding_layout2);
                        slidingLayout2.setTouchEnabled(false);
                        xpand2 = (ImageView) getView().findViewById(R.id.btnXpand2);
                        slidingLayout2.setDragView(xpand2);
                        xpand2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                                    if (Menu.getCambioDeFragment()) {
                                        setUpRecyclerView();
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                        Menu.setCambioDeFragmento(false);
                                    } else if (!Menu.getCambioDeFragment()) {
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                                    }
                                    xpand2.setImageResource(R.drawable.ic_expand_up);


                                } else if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                                    if (Menu.getCambioDeFragment()) {
                                        setUpRecyclerView();
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                        Menu.setCambioDeFragmento(false);
                                    } else if (!Menu.getCambioDeFragment()) {
                                        slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                                    }
                                    xpand2.setImageResource(R.drawable.ic_expand_down);

                                }

                            }
                        });
                        mBuscar = (EditText) getView().findViewById(R.id.input_buscar);
                        gps = (FloatingActionButton) getView().findViewById(R.id.ic_gps);
                        icCrear = (FloatingActionButton) getView().findViewById(R.id.ic_crearEmpleado);
                        icReactivar = (FloatingActionButton) getView().findViewById(R.id.ic_reactivar);
                        pPt = (TextView) getView().findViewById(R.id.PrivacyPolicy);
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
        return inflater.inflate(R.layout.fragment_gestionar_empleados, container, false);
    }

    private void setUpRecyclerView() {
        Query query = mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").orderBy("estado", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MarcadoresEmpleados> options = new FirestoreRecyclerOptions.Builder<MarcadoresEmpleados>()
                .setQuery(query, MarcadoresEmpleados.class)
                .build();

        adaptadorEmpleadosLista = new adaptadorEmpleadosLista(options);

        recyclerView = getView().findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptadorEmpleadosLista);
        adaptadorEmpleadosLista.startListening();
        adaptadorEmpleadosLista.setOnItemClickListener(new adaptadorEmpleadosLista.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                GeoPoint geoPoint = adaptadorEmpleadosLista.getItem(position).getGeoPoint();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 3000, null);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                dAdministrarEmpleados(adaptadorEmpleadosLista.getItem(viewHolder.getAdapterPosition()).getNombre());
                adaptadorEmpleadosLista.notifyItemChanged(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (adaptadorEmpleadosLista != null) {
            adaptadorEmpleadosLista.stopListening();
        }
    }

    private void centrarCamara() {

        mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    GeoPoint geoPoint1 = task.getResult().getGeoPoint("geoPoint");

                    mLocaliza = new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude());
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    setCamara();
                }
            }
        });
    }

    private void setCamara() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        double boundaryAbajo = mLocaliza.latitude - .1;
        double boundaryIzquierdo = mLocaliza.longitude - .1;
        double boundaryArriba = mLocaliza.latitude + .1;
        double boundaryDerecho = mLocaliza.longitude + .1;

        mLocalizacionLaLo = new LatLngBounds(
                new LatLng(boundaryAbajo, boundaryIzquierdo),
                new LatLng(boundaryArriba, boundaryDerecho));

        if (!mMap.getCameraPosition().target.equals(mLocalizacionLaLo)) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLocalizacionLaLo, 0));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    private void detalles() {

        if (mLocalizarUsuario == null) {
            Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mLocalizarUsuario = new LocalizacionUsuario();
            mLocalizarUsuario.setId(id);
            mLocalizarUsuario.setNombre(nombre);
            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            localizacion();

        }
    }

    private void guardarLocalizacion() {

        if (mLocalizarUsuario != null) {
            Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            DocumentReference locationRef = mDb
                    .collection("Empresas")
                    .document(empresa)
                    .collection("Localizaciones")
                    .document(nombre);
            locationRef.set(mLocalizarUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    centrarCamara();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }

    }

    private void init() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocalizar();
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                }
                return false;
            }
        });

        gps.setOnClickListener(gestionarEmpleados.this);
        icCrear.setOnClickListener(gestionarEmpleados.this);
        icReactivar.setOnClickListener(gestionarEmpleados.this);

        ocultarTeclado();
    }

    private void geoLocalizar() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        busquedaString = mBuscar.getText().toString();
        String busc = Normalizer.normalize(busquedaString.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        InputMethodManager inputManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        if (!busquedaString.isEmpty()) {
            mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").document(busc).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult().exists()) {
                        Log.d("ENTRA", "SIN ACENTO");
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
                            Toast.makeText(getActivity(), "No se a podido encontrar", Toast.LENGTH_SHORT).show();
                        }
                        if (list.size() > 0) {
                            Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                                    } else if (arrastrado) {
                                        direccionLat = marker.getPosition().latitude;
                                        direccionLong = marker.getPosition().longitude;
                                    }
                                    mLocalizaAddress = new LatLng(direccionLat, direccionLong);
                                    mMap.setOnInfoWindowClickListener(this);
                                }
                            });
                        } else {
                            mBuscar.getText().clear();
                        }
                    }
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        }


    }

    private void localizacion() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mProovedor = LocationServices.getFusedLocationProviderClient(getActivity());
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
                                } else if (!crearMark) {
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
                            } else if (!obraBo) {
                                mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                                mLocalizarUsuario.setTimestamp(null);
                            }
                            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            guardarLocalizacion();
                        } else {
                            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }

    }

    private void iniciarMapa() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(gestionarEmpleados.this);
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    public boolean isServicesOK() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOGO_PEDIR);
            dialog.show();
        } else {
            Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            Toast.makeText(getActivity(), "Mapas no funciona", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    private boolean compruebapermisos() {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        return true;
    }

    private void ocultarTeclado() {
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void firestoreObras() {
        Log.d("FIRESTORE OBRAS", "ENTRA");
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        geoFirestoreRefObs = mDb.collection("Empresas").document(empresa).collection("Obras");
        obs = new ArrayList<String>();
        obs.clear();
        if (markersMapObras != null) {
            markersMapObras.clear();
        }
        geoFirestoreRefObs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                List<Task<QuerySnapshot>> tasks2 = new ArrayList<Task<QuerySnapshot>>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        Log.d("obran", obran);
                        String jefe1 = document.getString("jefe");
                        GeoPoint geoPoint2 = document.getGeoPoint("geoPoint");
                        long online = document.getLong("online");
                        if (jefe1 != null) {
                            if (jefe1.equals("no")) {
                                jefe1 = "sin jefe de obra";
                            }
                        } else if (jefe1 == null) {
                            jefe1 = "sin jefe de obra";
                        }
                        if (!obs.contains(obran)) {
                            añadirMarcadores(geoPoint2, obran, jefe1, online);
                            obs.add(obran);
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
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                } else if (!task.isSuccessful()) {
                    Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
                        if (obs.contains("SIN OBRAS")) {
                            obs.remove("SIN OBRAS");
                        }
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

                    }
                });
            }
        });
    }

    private void listenerObs() {
        Log.d("listenerObs", "INICIADO");
        final int[] count = {0};
        geoFirestoreRefObs = mDb.collection("Empresas").document(empresa).collection("Obras");
        geoFirestoreRefObs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                List<Task<QuerySnapshot>> tasks2 = new ArrayList<Task<QuerySnapshot>>();
                if (task.isSuccessful()) {
                    Log.d("MdbOBS", "SUCCESS");
                    firestoreObras();
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d("RECORRE OBS", Integer.toString(count[0]));
                        Log.d("RECORRE OBS", document.getString("nombre"));
                        count[0]++;
                        Log.d("RECORRE OBS 2", Integer.toString(count[0]));
                    }

                }
                return Tasks.whenAllSuccess(tasks2);
            }

        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                Log.d("TASK OBS", "TERMINADA");
                Log.d("TICK OBS", "INICIADO");
                timerObs = new CountDownTimer(30000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.d("TICK OBS", Long.toString(millisUntilFinished));
                        Log.d("SIZE OBS", Integer.toString(obs.size()));
                        Log.d("COUNT SIZE", String.valueOf(count[0]));
                        if (obs.size() == count[0]) {
                            Log.d("OBS SIZE", " ES COUNT[0]");
                            timerObs.cancel();
                            timerObs.onFinish();

                        }
                    }

                    @Override
                    public void onFinish() {
                        if (obs.size() != count[0]) {
                            timerObs.start();
                        } else if (obs.size() == count[0]) {
                            final int[] cuentaObs = {obs.size()};
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
                                                    case REMOVED:
                                                        Log.d("cuentaObs PRIN", Integer.toString(cuentaObs[0]));
                                                        if (cuentaObs[0] == 0) {
                                                            mMap.clear();
                                                            firestoreObras();
                                                            firestoreNombres();
                                                        }
                                                        if (cuentaObs[0] > 0) {
                                                            cuentaObs[0] = cuentaObs[0] - 1;
                                                        }
                                                        Log.d("cuentaObs FIN", Integer.toString(cuentaObs[0]));
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
        });
    }

    private void listenerJfs() {
        Log.d("listenerJfs", "INICIADO");
        count2 = 0;
        mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task) {
                List<Task<QuerySnapshot>> tasks2 = new ArrayList<Task<QuerySnapshot>>();
                if (task.isSuccessful()) {
                    Log.d("MdbJFS", "SUCCESS");
                    firestoreNombres();
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.d("RECORRE JFS", Integer.toString(count2));
                        Log.d("RECORRE JFS", document.getString("nombre"));
                        count2++;
                        Log.d("RECORRE JFS 2", Integer.toString(count2));
                    }

                }
                return Tasks.whenAllSuccess(tasks2);
            }

        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                Log.d("TASK JFS", "TERMINADA");
                Log.d("TICK JFS", "INICIADO");
                timerJfs = new CountDownTimer(30000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.d("TICK JFS", Long.toString(millisUntilFinished));
                        if (lM != null) {
                            Log.d("lM", "NO NULL");
                            if (!lM.isEmpty()) {
                                Log.d("SIZE lM", Integer.toString(lM.size()));
                                if (lM.size() == count2) {
                                    Log.d("SIZE count2", Integer.toString(count2));
                                    timerJfs.cancel();
                                    timerJfs.onFinish();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFinish() {
                        if (lM.size() != count2) {
                            Log.d("FINISHED JFS", "SIZE MAL");
                            timerJfs.start();
                        } else if (lM.size() == count2) {
                            final int[] cuentaEmp = {lM.size()};
                            final ArrayList<String> Marca = new ArrayList<>();
                            Log.d("cuentaEmp INICIO", String.valueOf(cuentaEmp[0]));
                            mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.e(TAG, "onEvent: Listen failed", e);
                                        return;
                                    }
                                    if (queryDocumentSnapshots != null) {
                                        final Boolean[] marcBolean = {false};
                                        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                            if (documentChange.getDocument().exists()) {
                                                switch (documentChange.getType()) {
                                                    case ADDED:
                                                    case MODIFIED:
                                                    case REMOVED:
                                                        Log.d("ACTUALIZA", "ENTRA");
                                                        Log.d("DOCUMENTO", documentChange.getDocument().toString());
                                                        if (!Marca.contains(documentChange.getDocument().getString("nombre"))) {
                                                            Log.d("MARCA", "NO CONTIENE JEFE");
                                                            Marca.add(documentChange.getDocument().getString("nombre"));
                                                            Log.d("MARCA LIST", Marca.toString());
                                                        } else if (Marca.contains(documentChange.getDocument().getString("nombre"))) {
                                                            Log.d("MARCA", "CONTIENE JEFE");
                                                            marcBolean[0] = true;
                                                            Log.d("marcBolean", marcBolean.toString());
                                                        }
                                                        if (marcBolean[0]) {
                                                            Log.d("marcBolean", "ENTRA");
                                                            marcBolean[0] = false;
                                                            mMap.clear();
                                                            firestoreObras();
                                                            firestoreNombres();
                                                        }
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void añadirMarcadores(final GeoPoint geoPoint1, String title, String snippet, long onl) {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        markersMapObras = new HashMap<String, String>();

        MarkerOptions mo = new MarkerOptions()
                .rotation(0)
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_casa));
        Marker mkr = mMap.addMarker(mo
                .title(title)
                .snippet(snippet)
                .position(new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude())));
        MarcadoresObras marcadoresObras = new MarcadoresObras(geoPoint1, title, snippet, title, onl);
        markersMapObras.put(marcadoresObras.getTag(), mkr.getId());
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void añadirMarcadoresEmpleados(final GeoPoint geoPoint1, String nombre, String obra, String id) {
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Log.d("EMPLEADOS", "AÑADE MARCADOR");
        markersMapEmpleado = new HashMap<String, String>();
        String estadoDef = null;
        String ob = obra;
        MarkerOptions mo = new MarkerOptions()
                .rotation(0)
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_empleados));
        if (obra != null) {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_empleado_online));
            estadoDef = "online";
            ob = obra;
        }
        if (obra == null) {
            mo.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_empleado_offline));
            estadoDef = "offline";
            ob = "null";
        }

        Marker mkr = mMap.addMarker(mo
                .title(nombre)
                .position(new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude())));
        if (obra != null) {
            mkr.setSnippet(obra);
        }
        MarcadoresEmpleados marcadoresEmpleados = new MarcadoresEmpleados(geoPoint1, nombre, ob, estadoDef, id);
        markersMapEmpleado.put(marcadoresEmpleados.getTag(), mkr.getId());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void firestoreNombres() {
        Log.d("FIRESTORE NOMBRES", "ENTRA");
        Menu.cargando(true);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        geoFirestoreRefJfs = mDb.collection("Empresas").document(empresa).collection("Empleado");
        geoFirestoreRef2Jfs = mDb.collection("Empresas").document(empresa).collection("Administrador");
        jfs = new ArrayList<String>();
        jfs.clear();
        geoFirestoreRef2Jfs.get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                List<Task<QuerySnapshot>> tasks3 = new ArrayList<Task<QuerySnapshot>>();
                if (task2.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task2.getResult()) {
                        Log.d("emples", "ENTRA SI");
                        String jefe = document.getString("nombre");
                        if (!jfs.contains(jefe)) {
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
                        List<Task<QuerySnapshot>> tasks4 = new ArrayList<Task<QuerySnapshot>>();
                        if (task1.isSuccessful()) {
                            for (final QueryDocumentSnapshot document1 : task1.getResult()) {
                                Log.d("emples", "ENTRA");
                                String jefe = document1.getString("nombre");
                                if (!jfs.contains(jefe)) {
                                    if (jefe != null) {
                                        jfs.add(jefe);
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
                        Log.d("SIZE LISTA JFS", String.valueOf(jfs.size()));
                        mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
                        jefeSpinner = (Spinner) mNombres.findViewById(R.id.spinnerObra);
                        jefeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, jfs);
                        jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        jefeSpinner.setAdapter(jefeAdapter);
                        lM = new ArrayList();
                        lM.clear();
                        mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document2 : task.getResult()) {
                                        String jefe = document2.getString("nombre");
                                        if (!lM.contains(jefe)) {
                                            if (jefe != null) {
                                                Log.d("PASA", "PASA");
                                                lM.add(jefe);
                                                añadirMarcadoresEmpleados(document2.getGeoPoint("geoPoint"), document2.getString("nombre"), document2.getString("obra"), document2.getString("id"));
                                            } else if (jefe == null) {
                                                Log.d("jefe", "es null");
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        Menu.cargando(false);
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
            }
        });
    }

    private void dAdministrarEmpleados(final String empleadoSelec) {

        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Menu de administracion de empleados");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button botonDesactivar = mDos.findViewById(R.id.btn1);
        botonDesactivar.setText("Desactivar");
        final Button botonCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraAdministrarEmpledos = new AlertDialog.Builder(getActivity())
                .setCustomTitle(myMsgtitle);
        obraAdministrarEmpledos
                .setView(mDos);
        final AlertDialog dialogoAdministradorEmpleados = obraAdministrarEmpledos.create();
        botonDesactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorEmpleados.dismiss();
                dEliminarEmpleado(empleadoSelec);
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorEmpleados.dismiss();
            }
        });
        dialogoAdministradorEmpleados.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                botonDesactivar.setEnabled(true);
                botonCancelar.setEnabled(true);
            }
        });
        dialogoAdministradorEmpleados.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoAdministradorEmpleados.show();
        } else {
            dialogoAdministradorEmpleados.show();
        }


    }

    private void dEliminarEmpleado(final String empleadoSeleccionado) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Seguro que desea desactivar al empleado\n" + empleadoSeleccionado);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button botonDesactivar = mDos.findViewById(R.id.btn1);
        botonDesactivar.setText("Desactivar");
        final Button botonCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder EliminarEmpleado = new AlertDialog.Builder(getActivity())
                .setCustomTitle(myMsgtitle);
        EliminarEmpleado
                .setView(mDos);
        final AlertDialog dialogoEliminarEmpleado = EliminarEmpleado.create();
        botonDesactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoEliminarEmpleado.dismiss();
                mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String roll = documentSnapshot.getString(empleadoSeleccionado);
                            String rolElima = documentSnapshot.getString(empleadoSeleccionado);
                            if (roll.contains("_ELIMINADO")) {
                                final TextView myMsgtitle = new TextView(getActivity());
                                myMsgtitle.setText("El empleado " + jefes + " ya esta desactivado");
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);
                                final AlertDialog.Builder yaElim = new AlertDialog.Builder(getActivity())
                                        .setCustomTitle(myMsgtitle);
                                yaElim
                                        .setPositiveButton("Ok", null);
                                final AlertDialog dialogoYaElim = yaElim.create();
                                dialogoYaElim.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button positivoCrearEmp = dialogoYaElim.getButton(AlertDialog.BUTTON_POSITIVE);
                                        positivoCrearEmp.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialogoYaElim.dismiss();
                                                dialogoEliminarEmpleado.show();
                                            }
                                        });
                                    }
                                });
                                dialogoYaElim.setCanceledOnTouchOutside(false);
                                dialogoYaElim.show();
                            } else if (!roll.contains("_ELIMINADO")) {
                                if (roll.charAt(0) == 'J' && roll.charAt(1) == 'e' && roll.charAt(2) == 'F') {
                                    dElimEmpleado(empleadoSeleccionado);
                                } else {
                                    dElimEmpleado(empleadoSeleccionado);
                                }
                            }
                        }
                    }
                });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoEliminarEmpleado.dismiss();
            }
        });
        dialogoEliminarEmpleado.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                botonDesactivar.setEnabled(true);
                botonCancelar.setEnabled(true);
            }
        });
        dialogoEliminarEmpleado.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoEliminarEmpleado.show();
        } else {
            dialogoEliminarEmpleado.show();
        }

    }

    private void dElimEmpleado(final String empleadoSele) {
        mDb.collection("Empresas").document(empresa).collection("Empleado").document(empleadoSele).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documento1 = task.getResult();
                    final String idElim = documento1.getString("id");
                    final String code = documento1.getString("codigo empleado");
                    mDb.collection("Empresas").document(empresa).collection("Empleado").document(empleadoSele).update("DESACTIVADO", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mDb.collection("Todas las ids").document(idElim).update("DESACTIVADO", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDb.collection("Codigos").document(codigoEmpresa).update(empleadoSele, code + "_ELIMINADO").addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(empleadoSele).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    String norm = Normalizer.normalize(empleadoSele.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                                                    mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").document(norm).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mDb.collection("Empresas").document(empresa).collection("Obras").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            String JFob = document.getString("jefe");
                                                                            String obran = document.getString("obra");
                                                                            if (empleadoSele.equals(JFob)) {
                                                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obran).update("jefe", "no").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        mDb.collection("Jefes").document(idElim).delete();
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

    private void dGeneraEmpleado() {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Generar empleado");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        final AlertDialog.Builder empleadoGen = new AlertDialog.Builder(icCrear.getContext());
        View mCrearEmpleadoDialogo = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        nom = mCrearEmpleadoDialogo.findViewById(R.id.TextDos);
        final Button botonCrear = mCrearEmpleadoDialogo.findViewById(R.id.btn1);
        botonCrear.setText("Siguiente");
        final Button botonCancelar = mCrearEmpleadoDialogo.findViewById(R.id.btn2);
        empleadoGen
                .setView(mCrearEmpleadoDialogo)
                .setCustomTitle(myMsgtitle);
        final AlertDialog dialogoEmpleadoGen = empleadoGen.create();
        botonCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String letras1 = codigoRandom(4);
                String letras3 = codigoRandom(5) + codigoEmpleado.substring(codigoEmpleado.length() - 3);
                String snombre = nom.getText().toString();
                if (!snombre.isEmpty()) {
                    if (snombre.length() > 3) {
                        if (jfs.isEmpty()) {
                            codigo = letras1 + letras3;
                            mDb.collection("Codigos").document(codigoEmpresa).update(snombre, codigo);
                            firestoreNombres();
                        } else if (!jfs.isEmpty()) {
                            String ultimo = Iterables.getLast(jfs);
                            String ultimo1 = Normalizer.normalize(ultimo, Normalizer.Form.NFD)
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
                            dialogoEmpleadoGen.dismiss();
                            if (sa.equals(snombrea)) {
                                final String finalSnombre = snombre;
                                mDb.collection("Empresas").document(empresa).collection("Empleado").document(snombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Boolean desEli = task.getResult().getBoolean("DESACTIVADO");
                                            if (desEli) {
                                                dialogoEmpleadoGen.dismiss();
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("Empleado desactivado\n¿Desea reactivar al empleado " + finalSnombre + "?");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
                                                mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final AlertDialog.Builder reactivar = new AlertDialog.Builder(getActivity())
                                                        .setCustomTitle(myMsgtitle)
                                                        .setView(mDos);
                                                jefeSpinner.setOnItemSelectedListener(gestionarEmpleados.this);
                                                final Button btnReactivar = mDos.findViewById(R.id.btn1);
                                                btnReactivar.setText("Reactivar");
                                                final Button btnCancelar = mDos.findViewById(R.id.btn2);
                                                final AlertDialog dialogoReactivar = reactivar.create();
                                                btnReactivar.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(finalSnombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                mDb.collection("Todas las ids").document(documentSnapshot.getString("id")).update("DESACTIVADO", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(finalSnombre).update("DESACTIVADO", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                        mDb.collection("Codigos").document(codigoEmpresa).update(finalSnombre, documentSnapshot.getString(finalSnombre).replace("_ELIMINADO", ""));
                                                                                        dialogoReactivar.dismiss();
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
                                                btnCancelar.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialogoReactivar.dismiss();
                                                    }
                                                });
                                                dialogoReactivar.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialog) {
                                                        btnReactivar.setEnabled(true);
                                                        btnCancelar.setEnabled(true);
                                                    }
                                                });
                                                dialogoReactivar.setCanceledOnTouchOutside(false);
                                                nom.setText("");
                                                if (mDos.getParent() != null) {
                                                    ((ViewGroup) mDos.getParent()).removeView(mDos);
                                                    mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                    dialogoReactivar.show();
                                                } else {
                                                    dialogoReactivar.show();
                                                }
                                            } else if (!desEli) {
                                                mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        codigoEmpleadoChech = documentSnapshot.getString(finalSnombre);
                                                        if (codigoEmpleadoChech.charAt(0) == 'J' && codigoEmpleadoChech.charAt(1) == 'e' && codigoEmpleadoChech.charAt(2) == 'F') {
                                                            if (codigoEmpleadoChech.charAt(7) == 'E') {
                                                                nom.setError(finalSnombre + " ya es un empleado y jefe de obra de " + empresa);
                                                            } else if (codigoEmpleadoChech.charAt(7) == 'a') {
                                                                nom.setError(finalSnombre + " ya es un administrador y jefe de obra de " + empresa);
                                                            }
                                                        } else if (codigoEmpleadoChech.charAt(4) == 'E') {
                                                            nom.setError(finalSnombre + " ya es un empleado de " + empresa);
                                                        } else if (codigoEmpleadoChech.charAt(4) == 'a') {
                                                            nom.setError(finalSnombre + " ya es un administrador y jefe de obra de " + empresa);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                });
                            } else if (!sa.equals(snombrea)) {
                                if (sa.equals(ultimo1)) {
                                    codigo = letras1 + letras3;
                                    mDb.collection("Codigos").document(codigoEmpresa).update(snombre, codigo);
                                    dCompartir(codigo, snombre);
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
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoEmpleadoGen.dismiss();
            }
        });
        dialogoEmpleadoGen.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                nom.setEnabled(true);
                botonCancelar.setEnabled(true);
                botonCrear.setEnabled(true);
            }
        });
        dialogoEmpleadoGen.setCanceledOnTouchOutside(false);
        if (mDos.getParent() != null) {
            ((ViewGroup) mDos.getParent()).removeView(mDos);
            mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
            dialogoEmpleadoGen.show();
        } else {
            dialogoEmpleadoGen.show();
        }

    }

    private void dReactivarEmpleados(){
        final ArrayList<String> emplesDesac = new ArrayList<>();
        mDb.collection("Todas las ids").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                List<Task<QuerySnapshot>> tasks3 = new ArrayList<Task<QuerySnapshot>>();
                if (task2.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task2.getResult()) {
                        String nomb = document.getString("nombre");
                        Log.d("NOMB", nomb);
                        Boolean des = document.getBoolean("DESACTIVADO").booleanValue();
                        Log.d("DES", des.toString());
                        if (nomb!=null && des) {
                            Log.d("ENTRA", "DES");
                            emplesDesac.add(nomb);
                        }
                    }
                }
                return Tasks.whenAllSuccess(tasks3);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                if(!emplesDesac.isEmpty()){
                    mDos = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
                    Spinner desSpinner = (Spinner) mDos.findViewById(R.id.spinnerObra);
                    ArrayAdapter<String> desAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, emplesDesac);
                    desAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    desSpinner.setAdapter(desAdapter);
                    final String[] desSelec = {desAdapter.getItem(0).toString()};
                    desSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            desSelec[0] = parent.getItemAtPosition(position).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            desSelec[0] = parent.getItemAtPosition(0).toString();
                        }
                    });
                    final TextView myMsgtitle = new TextView(getActivity());
                    myMsgtitle.setText("Seleccione un empleado que reactivar");
                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                    myMsgtitle.setTextColor(Color.BLACK);
                    final AlertDialog.Builder reactivar = new AlertDialog.Builder(getActivity())
                            .setCustomTitle(myMsgtitle)
                            .setView(mDos);
                    final Button btnReactivar = mDos.findViewById(R.id.btn1);
                    btnReactivar.setText("Reactivar");
                    final Button btnCancelar = mDos.findViewById(R.id.btn2);
                    final AlertDialog dialogoReactivar = reactivar.create();
                    btnReactivar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(desSelec[0]).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    mDb.collection("Todas las ids").document(documentSnapshot.getString("id")).update("DESACTIVADO", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(desSelec[0]).update("DESACTIVADO", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            mDb.collection("Codigos").document(codigoEmpresa).update(desSelec[0], documentSnapshot.getString(desSelec[0]).replace("_ELIMINADO", ""));
                                                            dialogoReactivar.dismiss();
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
                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogoReactivar.dismiss();
                        }
                    });
                    dialogoReactivar.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialog) {
                            btnReactivar.setEnabled(true);
                            btnCancelar.setEnabled(true);
                        }
                    });
                    dialogoReactivar.setCanceledOnTouchOutside(false);
                    if (mDos.getParent() != null) {
                        ((ViewGroup) mDos.getParent()).removeView(mDos);
                        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                        dialogoReactivar.show();
                    } else {
                        dialogoReactivar.show();
                    }
                }else{
                    Toast.makeText(getContext(), "No hay empleados desactivados", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String codigoRandom(int largo) {
        StringBuilder sb = new StringBuilder(largo);
        for (int i = 0; i < largo; i++)
            sb.append(patron.charAt(aleatorio.nextInt(patron.length())));
        return sb.toString();

    }

    private void dCompartir(String cod, String nom) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "El codigo de empresa de " + empresa + " es: " + codigoEmpresa + "\nY el codigo del empleado " + nom + " es: " + cod);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir codigo");
        startActivity(shareIntent);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {

        if (v.equals(gps)) {

            localizacion();
        }
        if (v.equals(icCrear)) {
            dGeneraEmpleado();
        }
        if(v.equals(icReactivar)){

            dReactivarEmpleados();
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
        String tit = marker.getTitle();
        dAdministrarEmpleados(tit);

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