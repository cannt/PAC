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
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.Localizacion.LocalizacionObra;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.Marcadores.MarcadoresEmpleados;
import com.japac.pac.Marcadores.MarcadoresObras;
import com.japac.pac.Menu.ViewPagers.Menu;
import com.japac.pac.R;
import com.japac.pac.adaptadorObrasLista;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */

public class menuPrincipalAdministradores extends Fragment implements OnMapReadyCallback,
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
                    firestoreNombres();
                }
            });
            listenerObs();
            listenerJfs();
            init();

        }
    }

    private Map<String, String> markersMap, markersMapEmpleado;

    private CountDownTimer timerObs, timerJfs;

    private String id, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, sobra, busquedaString, ob, jf, jefes, codigoEmpleadoChech, JFC, JFO;
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

    private ArrayList<String> obs, jfs, lM;

    private FloatingActionButton icCrear, gps;

    private View mNombres, mCuatroBtn, mDosText, mDos;

    private EditText mBuscar;

    private TextView pPt;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizaDirc, geoPointLocalizayo;

    private FusedLocationProviderClient mProovedor;

    private LocalizacionUsuario mLocalizarUsuario;

    private LocalizacionObra mLocalizacionObra;

    FirebaseFirestore mDb;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private LatLngBounds mLocalizacionLaLo;

    private LatLng mLocaliza, mLocalizaAddress;

    private Marker marcadorCrea;

    private boolean obraBo = false, igual = false, crearMark = false, cambiar = false, ElimJefe = false, arrastrado = false, readyObs = false, readyJfs = false, alreadyObs = false, alreadyJfs = false;

    private ArrayAdapter<String> jefeAdapter;

    private Spinner jefeSpinner;

    private Double direccionLat, direccionLong;

    private SlidingUpPanelLayout slidingLayout2;

    private ImageView xpand2;

    private adaptadorObrasLista adaptadorObrasLista;

    private RecyclerView recyclerView;

    private CollectionReference geoFirestoreRefObs, geoFirestoreRefJfs, geoFirestoreRef2Jfs;

    public menuPrincipalAdministradores() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_menu_principal, container, false);
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
                        slidingLayout2.setTouchEnabled(true);
                        xpand2 = (ImageView) getView().findViewById(R.id.btnXpand2);
                        mBuscar = (EditText) getView().findViewById(R.id.input_buscar);
                        xpand2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ocultarTeclado();
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
                        gps = (FloatingActionButton) getView().findViewById(R.id.ic_gps);
                        icCrear = (FloatingActionButton) getView().findViewById(R.id.ic_crearObra);
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
        return RootView;
    }

    private void setUpRecyclerView() {
        Query query = mDb.collection("Empresas").document(empresa).collection("Obras").orderBy("online", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<MarcadoresObras> options = new FirestoreRecyclerOptions.Builder<MarcadoresObras>()
                .setQuery(query, MarcadoresObras.class)
                .build();

        adaptadorObrasLista = new adaptadorObrasLista(options);

        recyclerView = getView().findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptadorObrasLista);
        adaptadorObrasLista.startListening();

        adaptadorObrasLista.setOnItemClickListener(new adaptadorObrasLista.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                setCamara();
                slidingLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                GeoPoint geoPoint = adaptadorObrasLista.getItem(position).getGeoPoint();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 600, null);
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

        mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Menu.cargando(true);
                    touch(true);
                    GeoPoint geoPoint1 = task.getResult().getGeoPoint("geoPoint");

                    mLocaliza = new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude());
                    Menu.cargando(false);
                    touch(false);
                    setCamara();
                }
            }
        });
    }

    private void setCamara() {
        Menu.cargando(true);
        touch(true);
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
        touch(false);

    }

    private void touch(Boolean touch) {
        if (touch) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else if (!touch) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void detalles() {

        if (mLocalizarUsuario == null) {
            Menu.cargando(true);
            touch(true);
            mLocalizarUsuario = new LocalizacionUsuario();
            mLocalizarUsuario.setId(id);
            mLocalizarUsuario.setNombre(nombre);
            Menu.cargando(false);
            touch(false);
            localizacion();

        }
    }

    private void guardarLocalizacion() {

        if (mLocalizarUsuario != null) {
            Menu.cargando(true);
            touch(true);
            DocumentReference locationRef = mDb
                    .collection("Empresas")
                    .document(empresa)
                    .collection("Localizaciones")
                    .document(nombre);
            locationRef.set(mLocalizarUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Menu.cargando(false);
                    touch(false);
                    centrarCamara();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Menu.cargando(false);
                    touch(false);
                }
            });
        }

    }

    private void init() {
        Menu.cargando(true);
        touch(true);
        mBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocalizar();
                    Menu.cargando(false);
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
        Menu.cargando(true);
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
                            Toast.makeText(getActivity(), "No se a podido encontrar", Toast.LENGTH_SHORT).show();
                        }
                        if (list.size() > 0) {
                            Menu.cargando(true);
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
                                    } else if (arrastrado) {
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
                    Menu.cargando(false);
                    touch(false);
                }
            });
        }


    }

    private void localizacion() {
        Menu.cargando(true);
        touch(true);
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
                            touch(false);
                            guardarLocalizacion();
                        } else {
                            Menu.cargando(false);
                            touch(false);
                        }
                    }
                });
                firestoreNombres();
            }
        } catch (SecurityException e) {

        }

    }


    private void iniciarMapa() {
        Menu.cargando(true);
        touch(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(menuPrincipalAdministradores.this);
        Menu.cargando(false);
        touch(false);

    }

    public boolean isServicesOK() {
        Menu.cargando(true);
        touch(true);
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if (available == ConnectionResult.SUCCESS) {
            Menu.cargando(false);
            touch(false);
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Menu.cargando(false);
            touch(false);
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOGO_PEDIR);
            dialog.show();
        } else {
            Menu.cargando(false);
            touch(false);
            Toast.makeText(getActivity(), "Mapas no funciona", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    private boolean compruebapermisos() {
        Menu.cargando(true);
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
        Menu.cargando(false);
        touch(false);
        return true;
    }

    private void ocultarTeclado() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBuscar.getWindowToken(), 0);
    }

    private void firestoreObras() {
        Log.d("FIRESTORE OBRAS", "ENTRA");
        Menu.cargando(true);
        touch(true);
        alreadyObs = false;
        geoFirestoreRefObs = mDb.collection("Empresas").document(empresa).collection("Obras");
        obs = new ArrayList<String>();
        obs.clear();
        if (markersMap != null) {
            markersMap.clear();
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
                    touch(false);
                } else if (!task.isSuccessful()) {
                    Menu.cargando(false);
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
                        readyObs = true;
                        alreadyObs = true;

                    }
                });
            }
        });
    }

    private void listenerObs() {
        firestoreObras();
        Log.d("listenerObs", "INICIADO");
        final int[] contador = {0};
        timerObs = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                contador[0]++;
                Log.d("contador OBS", String.valueOf(contador[0]));
                Log.d("TICK OBS", Long.toString(millisUntilFinished));
                if (readyObs) {
                    contador[0] = 30;
                }
                if (contador[0] == 30) {
                    Log.d("contador OBS ENTRA", String.valueOf(contador[0]));
                    timerObs.cancel();
                    timerObs.onFinish();
                }
            }

            @Override
            public void onFinish() {
                if (readyObs) {
                    Log.d("readyObs", "TRUE");
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
                                                Log.d("listenerObs", "ENTRA");
                                                if (alreadyObs) {
                                                    Log.d("alreadyObs", "TRUE");
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
                } else if (!readyObs) {
                    Log.d("readyObs", "FALSE");
                    timerObs.start();
                }
            }
        }.start();
    }

    private void firestoreNombres() {
        Log.d("FIRESTORE NOMBRES", "ENTRA");
        Menu.cargando(true);
        touch(true);
        alreadyJfs = false;
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
                        mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
                            @Override
                            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task1) {
                                List<Task<QuerySnapshot>> tasks4 = new ArrayList<Task<QuerySnapshot>>();
                                if (task1.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document2 : task1.getResult()) {
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
                                return Tasks.whenAllSuccess(tasks4);
                            }
                        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                                readyJfs = true;
                                alreadyJfs = true;
                                Menu.cargando(false);
                                touch(false);
                            }
                        });
                    }
                });
            }
        });
    }

    private void listenerJfs() {
        Log.d("listenerJfs", "INICIADO");
        final int[] contador2 = {0};
        firestoreNombres();
        timerJfs = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                contador2[0]++;
                Log.d("contador JFS", String.valueOf(contador2[0]));
                Log.d("TICK JFS", Long.toString(millisUntilFinished));
                if (readyJfs) {
                    contador2[0] = 30;
                }
                if (contador2[0] == 30) {
                    Log.d("contador JFS ENTRA", String.valueOf(contador2[0]));
                    timerJfs.cancel();
                    timerJfs.onFinish();
                }
            }

            @Override
            public void onFinish() {
                if (readyJfs) {
                    Log.d("readyJfs", "TRUE");
                    mDb.collection("Empresas").document(empresa).collection("Localizacion marcadores").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                                Log.d("listenerJfs", "ENTRA");
                                                if (alreadyJfs) {
                                                    Log.d("alreadyJfs", "TRUE");
                                                    mMap.clear();
                                                    firestoreNombres();
                                                    firestoreObras();
                                                }
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else if (!readyJfs) {
                    Log.d("readyJfs", "FALSE");
                    timerJfs.start();
                }
            }
        }.start();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Menu.cargando(true);
        touch(true);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        Menu.cargando(false);
        touch(false);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void añadirMarcadores(final GeoPoint geoPoint1, String title, String snippet, long onl) {
        Menu.cargando(true);
        touch(true);
        markersMap = new HashMap<String, String>();

        MarkerOptions mo = new MarkerOptions()
                .rotation(0)
                .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_marcador_casa));
        Marker mkr = mMap.addMarker(mo
                .title(title)
                .snippet(snippet)
                .position(new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude())));
        MarcadoresObras marcadoresObras = new MarcadoresObras(geoPoint1, title, snippet, title, onl);
        markersMap.put(marcadoresObras.getTag(), mkr.getId());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        Menu.cargando(false);
        touch(false);
    }

    private void añadirMarcadoresEmpleados(final GeoPoint geoPoint1, String nombre, String obra, String id) {
        Menu.cargando(true);
        touch(true);
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
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        Menu.cargando(false);
        touch(false);
    }

    private void dAdmin(final String obraAd) {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Menu de administracion de la obra " + obraAd);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mCuatroBtn = getLayoutInflater().inflate(R.layout.dialogo_motivo, null);
        final Button btnCam = (Button) mCuatroBtn.findViewById(R.id.Vacas);
        btnCam.setText("Cambiar nombre");
        final Button btnElim = (Button) mCuatroBtn.findViewById(R.id.Baja);
        btnElim.setText("Eliminar obra");
        final Button btnAdJef = (Button) mCuatroBtn.findViewById(R.id.Otros);
        btnAdJef.setText("Administrar jefe");
        final Button btnCance = (Button) mCuatroBtn.findViewById(R.id.Cancelar);
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
                            if (!jefe.equals("no") && !jefe.equals(null) && !jefe.equals("")) {
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
        Menu.cargando(true);
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
                        mLocalizacionObra = new LocalizacionObra();
                        mLocalizacionObra.setGeoPoint(geoPointAn);
                        mLocalizacionObra.setObra(obraNu);
                        mLocalizacionObra.setOnline(online);
                        if (!jefeAn.equals("no") && !jefeAn.equals(null)) {
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
                        } else if (jefeAn.equals("no") || jefeAn.equals(null)) {
                            mLocalizacionObra.setJefe("no");
                        }
                        mDb.collection("Empresas").document(empresa).collection("Obras").document(obraNu).set(mLocalizacionObra).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Menu.cargando(false);
                                touch(false);
                                firestoreNombres();
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
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        final EditText sNuevoNom = mDosText.findViewById(R.id.TextDos);
        final Button btnCamb = (Button) mDosText.findViewById(R.id.btn1);
        final Button btnCancelar = (Button) mDosText.findViewById(R.id.btn2);
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
                } else if (obraNu.isEmpty()) {
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
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnElim = (Button) mDos.findViewById(R.id.btn1);
        btnElim.setText("Eliminar");
        final Button btnCancelar = (Button) mDos.findViewById(R.id.btn2);
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
                        if (!jef.equals(null) && !jef.equals("no")) {
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
                                        añadeJefes(obraAd, jef);
                                        dialogoObraEliminar.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Menu.cargando(true);
                                    touch(true);
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                    Menu.cargando(false);
                                    touch(false);
                                    dialogoObraEliminar.dismiss();
                                    firestoreObras();
                                    ElimJefe = false;
                                }
                            });
                        } else if (jef.equals(null) || jef.equals("no")) {
                            Menu.cargando(true);
                            touch(true);
                            mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                            Menu.cargando(false);
                            touch(false);
                            dialogoObraEliminar.dismiss();
                            firestoreObras();
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
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnCamb = (Button) mDos.findViewById(R.id.btn1);
        btnCamb.setText("Cambiar");
        final Button btnCancelar = (Button) mDos.findViewById(R.id.btn2);
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
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        final Button btnSiguiente = (Button) mNombres.findViewById(R.id.btn1);
        final Button btnCancelar = (Button) mNombres.findViewById(R.id.btn2);
        jefeSpinner = (Spinner) mNombres.findViewById(R.id.spinnerObra);
        final AlertDialog.Builder obraJefe = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        jefeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, jfs);
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
                    Toast.makeText(getActivity(), obraAdJf + " ya es el jefe de la obra " + obraAd, Toast.LENGTH_LONG).show();
                    localizacion();
                    dialogoObraJefe.dismiss();
                } else {
                    añadeJefes(obraAd, obraAdJf);
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
            firestoreNombres();
            dialogoObraJefe.show();
        } else {
            dialogoObraJefe.show();
        }
    }

    private void añadeJefes(final String obraAd, final String obraAdJf) {
        Menu.cargando(true);
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
                                            final String em = documentSnapshot.getString("email");
                                            if (!ElimJefe) {
                                                JFC = cE + "," + obraAd;
                                                JFO = obr + "," + obraAd;
                                            } else if (ElimJefe) {
                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                                firestoreObras();
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
                                            Menu.cargando(false);
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
                                            final String em = documentSnapshot.getString("email");
                                            if (!ElimJefe) {
                                                JFO = obraAd;
                                                JFC = cE + "JeF/" + obraAd;
                                            } else if (ElimJefe) {
                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                                firestoreObras();
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
                                            Menu.cargando(false);
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
                        Menu.cargando(true);
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
                        Menu.cargando(false);
                        touch(false);
                    }
                }
            });
            cambiar = false;
        }
        firestoreNombres();
    }


    private void crearObra() {
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("¿Que nombre tendra la obra?");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        final AlertDialog.Builder obraCrear = new AlertDialog.Builder(icCrear.getContext());
        mDosText = getLayoutInflater().inflate(R.layout.dialogo_dosbtn_texto, null);
        final EditText obra = mDosText.findViewById(R.id.TextDos);
        obra.setHint("Nombre de la obra");
        final Button btnCrear = (Button) mDosText.findViewById(R.id.btn1);
        btnCrear.setText("Crear");
        final Button btnCancelar = (Button) mDosText.findViewById(R.id.btn2);
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
                            mLocalizacionObra = new LocalizacionObra();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                ob = documentSnapshot.getString("obra");
                                GeoPoint geo = documentSnapshot.getGeoPoint("geoPoint");
                                if (ob.equalsIgnoreCase(sobra)) {
                                    jf = documentSnapshot.getString("jefe");
                                    igual = true;
                                    break;
                                }
                            }
                            if (igual) {
                                igual = false;
                                dObraExiste();
                            } else if (!igual) {
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
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button btnAct = (Button) mDos.findViewById(R.id.btn1);
        btnAct.setText("Actualizar");
        final Button btnCancelar = (Button) mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder obraExiste = new AlertDialog.Builder(icCrear.getContext());
        obraExiste
                .setCustomTitle(myMsgtitle)
                .setView(mDos);
        final AlertDialog dialogoObraExiste = obraExiste.create();
        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocalizacionObra = new LocalizacionObra();
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        if (obs.contains(obr)) {
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

