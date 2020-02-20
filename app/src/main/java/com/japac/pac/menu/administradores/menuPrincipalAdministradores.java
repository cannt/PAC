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
import com.google.firebase.storage.FirebaseStorage;
import com.japac.pac.localizacion.localizacionObra;
import com.japac.pac.localizacion.localizacionUsuario;
import com.japac.pac.marcadores.marcadoresObras;
import com.japac.pac.menu.menu;
import com.japac.pac.R;
import com.japac.pac.adaptadorObrasLista;
import com.japac.pac.servicios.snackbarDS;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */

public class menuPrincipalAdministradores extends Fragment implements OnMapReadyCallback,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
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

            if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
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

    private String id, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, sobra, busquedaString, ob, jf, jefes, codigoEmpleadoChech, JFC, JFO;
    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int ERROR_DIALOGO_PEDIR = 9001;
    private static final float ZOOM_PREDETERMINADO = 20f;

    private ArrayList<String> obs = new ArrayList<>(), jfs = new ArrayList<>(), lM;

    private FloatingActionButton icCrear, gps;

    private View mNombres;
    private View mDosText;
    private View mDos;

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
            id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            mDb = FirebaseFirestore.getInstance();
            FirebaseStorage almacen = FirebaseStorage.getInstance();
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
                        slidingLayout2 = Objects.requireNonNull(getView()).findViewById(R.id.sliding_layout2);
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

        RecyclerView recyclerView = Objects.requireNonNull(getView()).findViewById(R.id.recyclerview);
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
                    GeoPoint geoPoint1 = Objects.requireNonNull(task.getResult()).getGeoPoint("geoPoint");

                    mLocaliza = new LatLng(Objects.requireNonNull(geoPoint1).getLatitude(), geoPoint1.getLongitude());
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
            Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

        ocultarTeclado();
    }

    private void geoLocalizar() {
        menu.cargando(true);
        touch(true);
        busquedaString = mBuscar.getText().toString();
        InputMethodManager inputManager = (InputMethodManager)
                Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputManager).hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        if (!busquedaString.isEmpty()) {
            mDb.collection("Empresas").document(empresa).collection("Obras").document(busquedaString.toLowerCase().trim()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (Objects.requireNonNull(task.getResult()).exists()) {
                        if (slidingLayout2.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                            slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Objects.requireNonNull(task.getResult().getGeoPoint("geoPoint")).getLatitude(), Objects.requireNonNull(task.getResult().getGeoPoint("geoPoint")).getLongitude()), ZOOM_PREDETERMINADO));
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
        FusedLocationProviderClient mProovedor = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        try {
            if (compruebapermisos()) {
                final Task localizacion = mProovedor.getLastLocation();
                localizacion.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location locacizacionActual = (Location) task.getResult();
                            geoPointLocalizayo = new GeoPoint(Objects.requireNonNull(locacizacionActual).getLatitude(), locacizacionActual.getLongitude());
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
                                            online[0] = Objects.requireNonNull(documentSnapshot.getLong("online")).intValue();
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
        Objects.requireNonNull(mapFragment).getMapAsync(menuPrincipalAdministradores.this);
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
            resultado = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), perm);
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                listaPermisosNecesarios.add(perm);
            }
        }
        if (!listaPermisosNecesarios.isEmpty()) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), listaPermisosNecesarios.toArray(new String[listaPermisosNecesarios.size()]), Permisos);
            return false;
        }
        menu.cargando(false);
        touch(false);
        return true;
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(mBuscar.getWindowToken(), 0);
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
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
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
                            anadirMarcadores(Objects.requireNonNull(geoPoint2), obran, jefe1, online);
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
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Objects.requireNonNull(documentSnapshot.getGeoPoint("geoPoint")).getLatitude(), Objects.requireNonNull(documentSnapshot.getGeoPoint("geoPoint")).getLongitude()), ZOOM_PREDETERMINADO));
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
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task2.getResult())) {
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
                            for (final QueryDocumentSnapshot document1 : Objects.requireNonNull(task1.getResult())) {
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
                        jefeAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, jfs);
                        jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        jefeSpinner.setAdapter(jefeAdapter);
                        lM = new ArrayList();
                        lM.clear();
                        mDb.collection("Empresas").document(empresa).collection("Localizaciones Empleado").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                            @Override
                            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task1) {
                                List<Task<QuerySnapshot>> tasks4 = new ArrayList<>();
                                if (task1.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document2 : Objects.requireNonNull(task1.getResult())) {
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
        Objects.requireNonNull(vectorDrawable).setBounds(0, 0, 60, 60);
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
        View mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
        final Button btnCam = mCuatroBtn.findViewById(R.id.Vacas);
        btnCam.setText("Cambiar nombre");
        final Button btnElim = mCuatroBtn.findViewById(R.id.Baja);
        btnElim.setText("Eliminar obra");
        final Button btnAdJef = mCuatroBtn.findViewById(R.id.Otros);
        btnAdJef.setText("Administrar jefe");
        final Button btnCance = mCuatroBtn.findViewById(R.id.Cancelar);
        btnCance.setText("Cancelar");
        final AlertDialog.Builder obraAdministrarObras = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
                            if (!Objects.requireNonNull(jefe).equals("no") && !jefe.equals("")) {
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
            mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
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
                final int online = Objects.requireNonNull(documentSnapshot.getLong("online")).intValue();
                mDb.collection("Empresas").document(empresa).collection("Obras").document(Objects.requireNonNull(obraAn)).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mLocalizacionObra = new localizacionObra();
                        mLocalizacionObra.setGeoPoint(geoPointAn);
                        mLocalizacionObra.setObra(obraNu);
                        mLocalizacionObra.setOnline(online);
                        if (!Objects.requireNonNull(jefeAn).equals("no")) {
                            mLocalizacionObra.setJefe(jefeAn);
                            mDb.collection("Codigos").document(codigoEmpresa).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String codEmpl = documentSnapshot.getString(jefeAn);
                                    if (Objects.requireNonNull(codEmpl).contains("," + obraAn)) {
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
                                                    if (Objects.requireNonNull(jf).contains("," + obraAn)) {
                                                        jf = jf.replace("," + obraAn, "," + obraNu);
                                                    } else if (jf.contains(obraAn + ",")) {
                                                        jf = jf.replace(obraAn + ",", obraNu + ",");
                                                    } else if (jf.contains(obraAn)) {
                                                        jf = jf.replace(obraAn, obraNu);
                                                    }
                                                    final String finalJf = jf;
                                                    mDb.collection("Todas las ids").document(Objects.requireNonNull(idNu)).update("codigo empleado", finalCodEmpl).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        final EditText sNuevoNom = mDosText.findViewById(R.id.TextDos);
        final Button btnCamb = mDosText.findViewById(R.id.btn1);
        final Button btnCancelar = mDosText.findViewById(R.id.btn2);
        final AlertDialog.Builder alerta = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnElim = mDos.findViewById(R.id.btn1);
        btnElim.setText("Eliminar");
        final Button btnCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraEliminar = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
                        Objects.requireNonNull(jef);
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
                                        if (Objects.requireNonNull(JFO).contains("," + obraAd)) {
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnCamb = mDos.findViewById(R.id.btn1);
        btnCamb.setText("Cambiar");
        final Button btnCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraJefeExiste = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
        mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        final Button btnSiguiente = mNombres.findViewById(R.id.btn1);
        final Button btnCancelar = mNombres.findViewById(R.id.btn2);
        jefeSpinner = mNombres.findViewById(R.id.spinnerObra);
        final AlertDialog.Builder obraJefe = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setCustomTitle(myMsgtitle);
        jefeAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, jfs);
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
                    if (Objects.requireNonNull(codigoEmpleadoChech).length() >= 17) {
                        mDb.collection("Empresas").document(empresa).collection("Empleado").document(jefes).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final String idNueva = documentSnapshot.getString("id");
                                final String cE = documentSnapshot.getString("codigo empleado");
                                mDb.collection("Todas las ids").document(Objects.requireNonNull(idNueva)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                mDb.collection("Todas las ids").document(Objects.requireNonNull(idNueva)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                        if (Objects.requireNonNull(JFO).contains("," + obraAd)) {
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
                        mDb.collection("Todas las ids").document(Objects.requireNonNull(idCam)).update(MapC);
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
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
                            for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        myMsgtitle.setPadding(2, 2, 2, 2);
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

    @Override
    public void onClick(View v) {

        if (v.equals(gps)) {

            localizacion();
        }
        if (v.equals(icCrear)) {

            if (slidingLayout2.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
            crearObra();
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
}

