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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.gcacace.signaturepad.views.SignaturePad;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.japac.pac.Auth.Login;
import com.japac.pac.Localizacion.LocalizacionObra;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.Marcadores.MarcadoresEmpleados;
import com.japac.pac.Marcadores.MarcadoresObras;
import com.japac.pac.Menu.ViewPagers.Menu;
import com.japac.pac.PDF.TemplatePDF;
import com.japac.pac.R;
import com.japac.pac.adaptadorEmpleadosLista;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Object> map = new HashMap<>();

    private String trayecto, obraselect, nombreNu, emailConf, entrada_salida, SHAREempleado, SHAREano, SHAREmes, cif, ano1, mes1, mesnu, id, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, busquedaString, jefes, codigoEmpleadoChech, sa, codigo;
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

    private View mNombres, mTres, mDos, mAnoMes, mLogin, mObras, mFueraObra, mFirmar;

    private EditText mBuscar;

    private TextView pPt, nom;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizayo;

    private FusedLocationProviderClient mProovedor;

    private LocalizacionUsuario mLocalizarUsuario;

    CollectionReference geoFirestoreRefJfs, geoFirestoreRef2Jfs, geoFirestoreRefObs;

    FirebaseFirestore mDb;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private LatLngBounds mLocalizacionLaLo;

    private LatLng mLocaliza, mLocalizaAddress;

    private Marker marcadorCrea;

    private boolean trayectoBo = false, crearMark = false, arrastrado = false, next = true, end = false, emailShare, readyObs = false, readyJfs = false, alreadyObs = false, alreadyJfs = false;

    private ArrayAdapter<String> jefeAdapter, anoAdapter, obraAdapter;

    private Spinner jefeSpinner, anoMesSpinner, obraSpinner;

    private Double direccionLat, direccionLong, latitudGuardada, longitudGuardada, latitudDetectada, longitudDetectada, distan, distan2 = 1.0, dis;

    private SlidingUpPanelLayout slidingLayout2;

    private ImageView xpand2;

    private adaptadorEmpleadosLista adaptadorEmpleadosLista;

    private RecyclerView recyclerView;

    private CountDownTimer timerObs, timerJfs, timerPDF, timerLeergeo;

    private ArrayList<String> lM;

    static final String patron = "0123456789BCDGHIKLMNOPQRSTUVWXYZbcdghiklmnopqrstuvwxyz";

    static SecureRandom aleatorio = new SecureRandom();

    private File folder, localFile, fileShare;

    public gestionarEmpleados() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_gestionar_empleados, container, false);
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
                        cif = documentSnapshot.getString("cif");
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
        return RootView;
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()), (float) Math.floor(mMap.getCameraPosition().zoom + 1)), 600, null);
            }
        });
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    Menu.cargando(true);
                    touch(true);
                    mDb.collection("Empresas").document(empresa).collection("Empleado").document(adaptadorEmpleadosLista.getItem(viewHolder.getAdapterPosition()).getNombre()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            final TextView myMsgtitle = new TextView(getActivity());
                            myMsgtitle.setText("Introduzca los credenciales del empleado " + adaptadorEmpleadosLista.getItem(viewHolder.getAdapterPosition()).getNombre());
                            myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                            myMsgtitle.setTextColor(Color.BLACK);
                            mLogin = getLayoutInflater().inflate(R.layout.dialogo_login, null, false);
                            final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
                            final EditText semail = mLogin.findViewById(R.id.emailDialogo);
                            semail.setEnabled(false);
                            semail.setText(documentSnapshot.getString("email"));
                            final EditText scontraseña = mLogin.findViewById(R.id.contraseñaDialogo);
                            final Button botonSig = mLogin.findViewById(R.id.btn1);
                            final Button botonCan = mLogin.findViewById(R.id.btn2);
                            Login
                                    .setView(mLogin)
                                    .setCustomTitle(myMsgtitle);
                            final AlertDialog dialogoLogin = Login.create();
                            botonSig.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialogoLogin.dismiss();
                                    final String emailNu = semail.getText().toString();
                                    final String contraseñaNu = scontraseña.getText().toString();
                                    if (!emailNu.isEmpty() && !contraseñaNu.isEmpty()) {
                                        if (!emailNu.equals(emailAn)) {
                                            mAuth.signOut();
                                            mAuth.signInWithEmailAndPassword(emailNu, contraseñaNu).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {
                                                        id = mAuth.getCurrentUser().getUid();
                                                        mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if (documentSnapshot.exists()) {

                                                                    codigoEmpresa = documentSnapshot.getString("codigo empresa");
                                                                    comp = documentSnapshot.getString("comprobar");
                                                                    empresa = documentSnapshot.getString("empresa");
                                                                    nombreNu = documentSnapshot.getString("nombre");
                                                                    emailConf = documentSnapshot.getString("email");
                                                                    roles = documentSnapshot.getString("rol");
                                                                    obcomprueba = documentSnapshot.getString("obra");
                                                                    if (comp.equals("iniciada")) {
                                                                        entrada_salida = "Salida";
                                                                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                                                                        entrada_salida = "Entrada";
                                                                    }
                                                                    if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                                                        obcomprueba = documentSnapshot.getString("obra");
                                                                    }
                                                                    dRegistrar();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(getActivity(), "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                                                        dialogoLogin.show();
                                                    }
                                                }
                                            });
                                        } else if (emailNu.equals(emailAn)) {
                                            Menu.cargando(true);
                                            touch(true);
                                            semail.getText().clear();
                                            semail.setError("Introduzca el email del empleado invitado");
                                            scontraseña.getText().clear();

                                        }
                                    } else if (emailNu.isEmpty()) {
                                        semail.setError("Introduzca el email del empleado invitado");
                                        if (contraseñaNu.isEmpty()) {
                                            scontraseña.setError("Introduzca la contraseña del empleado invitado");
                                        }

                                    } else if (contraseñaNu.isEmpty()) {
                                        scontraseña.setError("Introduzca el email del empleado invitado");
                                        if (emailNu.isEmpty()) {
                                            semail.setError("Introduzca la contraseña del empleado invitado");
                                        }
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
                                    scontraseña.setEnabled(true);
                                    botonSig.setEnabled(true);
                                    botonCan.setEnabled(true);
                                    Menu.cargando(false);
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
                    });
                } else if (direction == ItemTouchHelper.RIGHT) {
                    dAdministrarEmpleados(adaptadorEmpleadosLista.getItem(viewHolder.getAdapterPosition()).getNombre());
                }
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
        if (!emailShare) {
            deleteRecursive(folder);
        }
    }

    public void onPause() {
        super.onPause();
        if (!emailShare) {
            deleteRecursive(folder);
        }
    }

    public void onResume() {
        super.onResume();
        if (emailShare) {
            emailShare = false;
            deleteRecursive(folder);
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

    private void touch(Boolean touch) {
        if (touch) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else if (!touch) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
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

        gps.setOnClickListener(gestionarEmpleados.this);
        icCrear.setOnClickListener(gestionarEmpleados.this);
        icReactivar.setOnClickListener(gestionarEmpleados.this);

        ocultarTeclado();
    }

    private void geoLocalizar() {
        Menu.cargando(true);
        touch(true);
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
                            mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                            mLocalizarUsuario.setTimestamp(null);
                            Menu.cargando(false);
                            touch(false);
                            guardarLocalizacion();
                        } else {
                            Menu.cargando(false);
                            touch(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }

    }

    private void iniciarMapa() {
        Menu.cargando(true);
        touch(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapa);
        mapFragment.getMapAsync(gestionarEmpleados.this);
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
                            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                                List<Task<QuerySnapshot>> tasks3 = new ArrayList<Task<QuerySnapshot>>();
                                if (task2.isSuccessful()) {
                                    for (final QueryDocumentSnapshot document2 : task2.getResult()) {
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
                                return Tasks.whenAllSuccess(tasks3);
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
        firestoreNombres();
        Log.d("listenerJfs", "INICIADO");
        timerJfs = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("TICK JFS", Long.toString(millisUntilFinished));
                if (readyJfs) {
                    timerJfs.cancel();
                    timerJfs.onFinish();
                }
            }

            @Override
            public void onFinish() {
                if (readyJfs) {
                    readyJfs = false;
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
                }
            }
        }.start();
    }

    private void firestoreObras() {
        Log.d("FIRESTORE OBRAS", "ENTRA");
        Menu.cargando(true);
        touch(true);
        alreadyObs = false;
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
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();
        Menu.cargando(false);
        touch(false);
    }

    private void dAdministrarEmpleados(final String empleadoSelec) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Menu de administracion de empleados");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
        final Button botonDesactivar = mTres.findViewById(R.id.btn2);
        botonDesactivar.setText("Desactivar");
        final Button botonRegistro = mTres.findViewById(R.id.btn1);
        botonRegistro.setText("Generar Registro");
        final Button botonCancelar = mTres.findViewById(R.id.Cancelar);
        final AlertDialog.Builder obraAdministrarEmpledos = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        obraAdministrarEmpledos
                .setView(mTres);
        final AlertDialog dialogoAdministradorEmpleados = obraAdministrarEmpledos.create();
        botonDesactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorEmpleados.dismiss();
                dEliminarEmpleado(empleadoSelec);
            }
        });
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAdministradorEmpleados.dismiss();
                leerRegistro(empleadoSelec);
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
                botonRegistro.setEnabled(true);
                botonCancelar.setEnabled(true);
                Menu.cargando(false);
                touch(false);
            }
        });
        dialogoAdministradorEmpleados.setCanceledOnTouchOutside(false);
        if (mTres.getParent() != null) {
            ((ViewGroup) mTres.getParent()).removeView(mTres);
            mTres = getLayoutInflater().inflate(R.layout.dialogo_tresbtn, null);
            dialogoAdministradorEmpleados.show();
        } else {
            dialogoAdministradorEmpleados.show();
        }


    }

    private void leerRegistro(final String empleado) {
        mDb
                .collection("Empresas")
                .document(empresa)
                .collection("Empleado")
                .document(empleado)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot1) {
                        if (documentSnapshot1.exists()) {
                            Menu.cargando(true);
                            touch(true);
                            final String nif = documentSnapshot1.getString("NIF");
                            final String naf = documentSnapshot1.getString("NAF");
                            String idreg = documentSnapshot1.getString("id");
                            mDb
                                    .collection("Empresas")
                                    .document(empresa)
                                    .collection("Empleado")
                                    .document(empleado)
                                    .collection("Registro")
                                    .document("AÑOS")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                            String ans = documentSnapshot2.getString("años");
                                            final List<String> ansL = Arrays.asList(ans.split("\\s*,\\s*"));
                                            mDb.collection("Empresas").document(empresa).collection("Empleado").document(empleado).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    final String idEm = documentSnapshot.getString("id");
                                                    try {

                                                        localFile = File.createTempFile("firma", "jpg");
                                                        almacenRef.child(empresa + "/Firmas/" + empleado + "/" + idEm + ".jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                Menu.cargando(false);
                                                                touch(false);
                                                                elegirFechasAños(ansL, "Empleado", empleado, nif, naf, idEm);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                Menu.cargando(false);
                                                                touch(false);
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                        Menu.cargando(false);
                                                        touch(false);
                                                    }

                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Menu.cargando(false);
                                    touch(false);
                                    Toast.makeText(getActivity(), "El empleado " + empleado + " no a registrado ninguna jornada todavia", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

    }

    private void elegirFechasAños(List<String> años, final String roles1, final String empleado, final String nif, final String naf, final String id1) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el año");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        anoMesSpinner = (Spinner) mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ano1 = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, años);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final Button botonSiguiente = mAnoMes.findViewById(R.id.btn1);
        final Button botonCancelar = mAnoMes.findViewById(R.id.btn2);
        final AlertDialog.Builder añoEle = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        añoEle
                .setView(mAnoMes);
        final AlertDialog dialogoAñoEle = añoEle.create();
        botonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection(roles1)
                        .document(empleado)
                        .collection("Registro")
                        .document("AÑOS")
                        .collection(ano1)
                        .document("MESES")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot3) {
                                String ms = documentSnapshot3.getString("meses");
                                List<String> msL = Arrays.asList(ms.split("\\s*,\\s*"));
                                Menu.cargando(false);
                                touch(false);
                                elegirFechasMeses(msL, roles1, empleado, ano1, nif, naf, id1);
                                dialogoAñoEle.dismiss();

                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoAñoEle.dismiss();
            }
        });
        dialogoAñoEle.setCanceledOnTouchOutside(false);
        dialogoAñoEle.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                anoMesSpinner.setEnabled(true);
                botonSiguiente.setEnabled(true);
                botonCancelar.setEnabled(true);
                Menu.cargando(false);
                touch(false);
            }
        });
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mAnoMes);
            mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
            dialogoAñoEle.show();
        } else {
            dialogoAñoEle.show();
        }
    }

    private void elegirFechasMeses(List<String> meses, final String roles1, final String empleado, final String ano3, final String nif, final String naf, final String id1) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Elija el mes");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        anoMesSpinner = (Spinner) mAnoMes.findViewById(R.id.spinnerObra);
        anoMesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mes1 = adapterView.getItemAtPosition(i).toString();
                mesnu = adapterView.getItemAtPosition(i).toString();
                if (mes1.equals("01")) {
                    mes1 = "Enero";
                } else if (mes1.equals("02")) {
                    mes1 = "Febrero";
                } else if (mes1.equals("03")) {
                    mes1 = "Marzo";
                } else if (mes1.equals("04")) {
                    mes1 = "Abril";
                } else if (mes1.equals("05")) {
                    mes1 = "Mayo";
                } else if (mes1.equals("06")) {
                    mes1 = "Junio";
                } else if (mes1.equals("07")) {
                    mes1 = "Julio";
                } else if (mes1.equals("08")) {
                    mes1 = "Agosto";
                } else if (mes1.equals("09")) {
                    mes1 = "Septiembre";
                } else if (mes1.equals("10")) {
                    mes1 = "Octubre";
                } else if (mes1.equals("11")) {
                    mes1 = "Nobiembre";
                } else if (mes1.equals("12")) {
                    mes1 = "Diciembre";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        anoAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, meses);
        anoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        anoMesSpinner.setAdapter(anoAdapter);
        final Button botonSiguiente = mAnoMes.findViewById(R.id.btn1);
        final Button botonCancelar = mAnoMes.findViewById(R.id.btn2);
        final AlertDialog.Builder mesEle = new AlertDialog.Builder(getContext())
                .setCustomTitle(myMsgtitle);
        jefeSpinner.setOnItemSelectedListener(this);
        mesEle
                .setView(mAnoMes);
        final AlertDialog dialogoMesEle = mesEle.create();
        botonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Menu.cargando(true);
                touch(true);
                mDb
                        .collection("Empresas")
                        .document(empresa)
                        .collection(roles1)
                        .document(empleado)
                        .collection("Registro")
                        .document("AÑOS")
                        .collection(ano3)
                        .document("MESES")
                        .collection(mesnu)
                        .document("DIAS")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshotd) {
                                if (documentSnapshotd.exists()) {
                                    Menu.cargando(false);
                                    touch(false);
                                    String dia = documentSnapshotd.getString("dias");
                                    List<String> diL = Arrays.asList(dia.split("\\s*,\\s*"));
                                    creacionPdf(diL, empleado, roles1, mesnu, ano3, empresa, id1, nif, naf);
                                    dialogoMesEle.dismiss();
                                }
                            }
                        });
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoMesEle.dismiss();
            }
        });
        dialogoMesEle.setCanceledOnTouchOutside(false);
        dialogoMesEle.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                anoMesSpinner.setEnabled(true);
                botonSiguiente.setEnabled(true);
                botonCancelar.setEnabled(true);
                Menu.cargando(false);
                touch(false);
            }
        });
        if (mAnoMes.getParent() != null) {
            ((ViewGroup) mAnoMes.getParent()).removeView(mAnoMes);
            mAnoMes = getLayoutInflater().inflate(R.layout.dialogo_spinner, null);
            dialogoMesEle.show();
        } else {
            dialogoMesEle.show();
        }
    }

    private void creacionPdf(final List<String> diasList, final String empl, final String rolT, String me, final String anoT, String empr, final String idT, String NIF, String NAF) {
        Menu.cargando(true);
        touch(true);
        final TemplatePDF templatePDF = new TemplatePDF(getContext());
        templatePDF.openDocument(empl, me, anoT);
        templatePDF.addMetaData(empr, empl, me, anoT);
        templatePDF.crearHeader(empr, empl, cif, NIF, NAF, mes1, anoT);
        final int[] i = {0};
        timerPDF = new CountDownTimer(999999999, 1000) {
            @Override
            public void onTick(long mmenushareillisUntilFinished) {

                if (next) {

                    if (i[0] == diasList.size()) {
                        i[0] = 0;
                        end = false;
                        timerPDF.cancel();
                    } else {

                        next = false;
                        final String diList = diasList.get(i[0]);

                        mDb.collection("Empresas")
                                .document(empresa)
                                .collection(rolT)
                                .document(empl)
                                .collection("Registro")
                                .document("AÑOS")
                                .collection(anoT)
                                .document("MESES")
                                .collection(mesnu)
                                .document("DIAS")
                                .collection(diList).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<String> horas = new ArrayList<>();
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        horas.add(document.getId());
                                    }
                                }
                                String horaEn = horas.get(0);
                                String horaSa = horas.get(horas.size() - 1);
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                                Long diferencia;
                                try {
                                    Date dateEn = simpleDateFormat.parse(horaEn);
                                    Date dateSa = simpleDateFormat.parse(horaSa);
                                    diferencia = dateSa.getTime() - dateEn.getTime();
                                    int days = (int) (diferencia / (1000 * 60 * 60 * 24));
                                    int hours = (int) ((diferencia - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                                    int mindif = (int) (diferencia - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                                    int horasExtras = 0;
                                    int minExtras = 0;

                                    if (hours >= 8) {
                                        if (hours > 8) {
                                            horasExtras = hours - 8;
                                        } else {
                                            horasExtras = 0;
                                        }
                                        if (mindif > 0) {
                                            minExtras = mindif;
                                        } else if (mindif == 0) {
                                            minExtras = 0;
                                        }
                                    }
                                    if (horasExtras != 0) {
                                        hours = hours - horasExtras;
                                    }
                                    if (minExtras != 0) {
                                        mindif = mindif - minExtras;
                                    }
                                    int horasTotales = hours + horasExtras;
                                    int minutosTotales = mindif + minExtras;
                                    horaEn = horas.get(0);
                                    horaSa = horas.get(horas.size() - 1);
                                    if (i[0] == diasList.size() - 1) {
                                        end = true;
                                    }
                                    templatePDF.tablaMain(diList, horaEn, horaSa, hours + ":" + mindif, horasExtras + ":" + minExtras, horasTotales + ":" + minutosTotales, idT, localFile.getAbsolutePath(), end, anoT, empresa, empl, mesnu, almacen);
                                    if (end) {
                                        SHAREempleado = empl;
                                        SHAREano = anoT;
                                        SHAREmes = mesnu;
                                        Menu.cargando(false);
                                        touch(false);
                                        Menu.datos(folder, fileShare, SHAREempleado, mes1, SHAREano, SHAREmes, empresa, emailAn);
                                    }
                                    next = true;
                                    i[0]++;
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

            }

            @Override
            public void onFinish() {

            }

        }.start();
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null) {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    deleteRecursive(child);
                }
            }
            if (fileOrDirectory != null) {
                fileOrDirectory.delete();
            }
        }
    }

    private void dEliminarEmpleado(final String empleadoSeleccionado) {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Seguro que desea desactivar al empleado\n" + empleadoSeleccionado);
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
        final Button botonDesactivar = mDos.findViewById(R.id.btn1);
        botonDesactivar.setText("Desactivar");
        final Button botonCancelar = mDos.findViewById(R.id.btn2);
        final AlertDialog.Builder EliminarEmpleado = new AlertDialog.Builder(getContext())
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
                                Menu.cargando(true);
                                touch(true);
                                final TextView myMsgtitle = new TextView(getActivity());
                                myMsgtitle.setText("El empleado " + jefes + " ya esta desactivado");
                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                myMsgtitle.setTextColor(Color.BLACK);
                                final AlertDialog.Builder yaElim = new AlertDialog.Builder(getContext())
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
                                        Menu.cargando(false);
                                        touch(false);
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
                Menu.cargando(false);
                touch(false);
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
        Menu.cargando(true);
        touch(true);
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
                                                Menu.cargando(true);
                                                touch(true);
                                                dialogoEmpleadoGen.dismiss();
                                                final TextView myMsgtitle = new TextView(getActivity());
                                                myMsgtitle.setText("Empleado desactivado\n¿Desea reactivar al empleado " + finalSnombre + "?");
                                                myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                myMsgtitle.setTextColor(Color.BLACK);
                                                mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                final AlertDialog.Builder reactivar = new AlertDialog.Builder(getContext())
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
                                                        Menu.cargando(false);
                                                        touch(false);
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
                Menu.cargando(false);
                touch(false);
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

    private void dReactivarEmpleados() {
        final ArrayList<String> emplesDesac = new ArrayList<>();
        mDb.collection("Todas las ids").get().continueWithTask(new Continuation<QuerySnapshot, Task<List<QuerySnapshot>>>() {
            @Override
            public Task<List<QuerySnapshot>> then(@NonNull Task<QuerySnapshot> task2) {
                Menu.cargando(true);
                touch(true);
                List<Task<QuerySnapshot>> tasks3 = new ArrayList<Task<QuerySnapshot>>();
                if (task2.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task2.getResult()) {
                        String nomb = document.getString("nombre");
                        Log.d("NOMB", nomb);
                        Boolean des = document.getBoolean("DESACTIVADO").booleanValue();
                        Log.d("DES", des.toString());
                        if (nomb != null && des) {
                            Log.d("ENTRA", "DES");
                            emplesDesac.add(nomb);
                        }
                    }
                }
                Menu.cargando(false);
                touch(false);
                return Tasks.whenAllSuccess(tasks3);
            }
        }).addOnCompleteListener(new OnCompleteListener<List<QuerySnapshot>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<List<QuerySnapshot>> task) {
                Menu.cargando(true);
                touch(true);
                if (!emplesDesac.isEmpty()) {
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
                    final AlertDialog.Builder reactivar = new AlertDialog.Builder(getContext())
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
                            Menu.cargando(false);
                            touch(false);
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
                } else {
                    Toast.makeText(getActivity(), "No hay empleados desactivados", Toast.LENGTH_SHORT).show();
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

    private int leeObras(Spinner spinner, String obraselecionada) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(obraselecionada)) {
                return i;
            }
        }
        return 0;
    }

    private void dRegistrar() {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Seleccione la obra");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mObras = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
        obraSpinner = (Spinner) mObras.findViewById(R.id.spinnerObra);
        obraAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, obs);
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
                } else if (!obraselect.equals(obcomprueba)) {
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
                        Toast.makeText(getActivity(), "Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero", Toast.LENGTH_LONG).show();
                        botonJornada.setText("Finalizar");
                        botonJornada.setTextColor(Color.RED);
                        obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                    } else if (comp.equals("finalizada") || comp.equals("no")) {
                        entrada_salida = "Entrada";
                        leerGeo(obraselect);
                    }
                } else if (botonJornada.getText().equals("Finalizar")) {
                    if (comp.contentEquals("finalizada") || comp.contentEquals("no")) {
                        Toast.makeText(getActivity(), "Debes iniciar primero una jornada", Toast.LENGTH_SHORT).show();
                        botonJornada.setText("Iniciar");
                        botonJornada.setTextColor(Color.RED);
                    } else if (comp.contentEquals("iniciada")) {
                        entrada_salida = "Salida";
                        leerGeo(obraselect);
                    }

                }
            }
        });
        botonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoRegistro.dismiss();
                dConfirma();
            }
        });
        dialogoRegistro.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (comp.equals("iniciada")) {
                    botonJornada.setText("Finalizar");
                    obraSpinner.setSelection(leeObras(obraSpinner, obcomprueba));
                    obraselect = obraSpinner.getItemAtPosition(leeObras(obraSpinner, obcomprueba)).toString();
                    obraSpinner.setEnabled(false);
                    botonJornada.setEnabled(true);
                    botonCancelar.setEnabled(true);
                } else if (comp.equals("finalizada") || comp.equals("no")) {
                    botonJornada.setText("Iniciar");
                    obraSpinner.setEnabled(true);
                    botonJornada.setEnabled(true);
                    botonCancelar.setEnabled(true);
                }
                Menu.cargando(false);
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
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText("Introduzca los credenciales del empleado invitado");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mLogin = getLayoutInflater().inflate(R.layout.dialogo_login, null, false);
        final AlertDialog.Builder Login = new AlertDialog.Builder(getContext());
        final EditText semail = mLogin.findViewById(R.id.emailDialogo);
        final EditText scontraseña = mLogin.findViewById(R.id.contraseñaDialogo);
        final Button botonSig = mLogin.findViewById(R.id.btn1);
        final Button botonCan = mLogin.findViewById(R.id.btn2);
        Login
                .setView(mLogin)
                .setCustomTitle(myMsgtitle);
        final AlertDialog dialogoLogin = Login.create();
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogoLogin.dismiss();
                final String emailNu = semail.getText().toString();
                final String contraseñaNu = scontraseña.getText().toString();
                if (!emailNu.isEmpty() && !contraseñaNu.isEmpty()) {
                    if (!emailNu.equals(emailAn)) {
                        mAuth.signOut();
                        mAuth.signInWithEmailAndPassword(emailNu, contraseñaNu).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    id = mAuth.getCurrentUser().getUid();
                                    mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (documentSnapshot.exists()) {

                                                codigoEmpresa = documentSnapshot.getString("codigo empresa");
                                                comp = documentSnapshot.getString("comprobar");
                                                empresa = documentSnapshot.getString("empresa");
                                                nombreNu = documentSnapshot.getString("nombre");
                                                emailConf = documentSnapshot.getString("email");
                                                roles = documentSnapshot.getString("rol");
                                                obcomprueba = documentSnapshot.getString("obra");
                                                if (comp.equals("iniciada")) {
                                                    entrada_salida = "Salida";
                                                } else if (comp.equals("finalizada") || comp.equals("no")) {
                                                    entrada_salida = "Entrada";
                                                }
                                                if (documentSnapshot.getString("obra") != null && documentSnapshot.getString("obra") != "no") {
                                                    obcomprueba = documentSnapshot.getString("obra");
                                                }

                                                if (obraMark) {
                                                    Menu.cargando(true);
                                                    touch(true);
                                                    final TextView myMsgtitle = new TextView(getActivity());
                                                    mDos = getLayoutInflater().inflate(R.layout.dialogo_dosbtn, null);
                                                    final Button botonJornada = mDos.findViewById(R.id.btn1);
                                                    final Button botonCancelar = mDos.findViewById(R.id.btn2);
                                                    myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
                                                    myMsgtitle.setTextColor(Color.BLACK);
                                                    String obFin = null;
                                                    if (obcomprueba != null) {
                                                        if (obcomprueba.equals(obraMarker)) {
                                                            myMsgtitle.setText(nombreNu + " ¿desea finalizar la jornada en la obra " + obraMarker + "?");
                                                            obFin = obraMarker;
                                                        } else if (!obcomprueba.equals(obraMarker)) {
                                                            myMsgtitle.setText("Ya existe una jornada iniciada en " + obcomprueba + ", finalizala primero");
                                                            obFin = obcomprueba;
                                                        }
                                                        botonJornada.setText("Finalizar");
                                                    } else if (obcomprueba == null) {
                                                        myMsgtitle.setText(nombreNu + " desea iniciar la jornada en la obra " + obraMarker);
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
                                                            dialogoOtro.dismiss();
                                                            leerGeo(finalObFin);
                                                        }
                                                    });
                                                    botonCancelar.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialogoOtro.dismiss();
                                                        }
                                                    });
                                                    dialogoOtro.setCanceledOnTouchOutside(false);
                                                    dialogoOtro.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(final DialogInterface dialog) {
                                                            botonJornada.setEnabled(true);
                                                            botonCancelar.setEnabled(true);
                                                            Menu.cargando(false);
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
                                                } else if (!obraMark) {
                                                    dRegistrar();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(), "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                                    dialogoLogin.show();
                                }
                            }
                        });
                    } else if (emailNu.equals(emailAn)) {
                        Menu.cargando(true);
                        touch(true);
                        semail.getText().clear();
                        semail.setError("Introduzca el email del empleado invitado");
                        scontraseña.getText().clear();

                    }
                } else if (emailNu.isEmpty()) {
                    semail.setError("Introduzca el email del empleado invitado");
                    if (contraseñaNu.isEmpty()) {
                        scontraseña.setError("Introduzca la contraseña del empleado invitado");
                    }

                } else if (contraseñaNu.isEmpty()) {
                    scontraseña.setError("Introduzca el email del empleado invitado");
                    if (emailNu.isEmpty()) {
                        semail.setError("Introduzca la contraseña del empleado invitado");
                    }
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
                scontraseña.setEnabled(true);
                botonSig.setEnabled(true);
                botonCan.setEnabled(true);
                Menu.cargando(false);
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

    public void dConfirma() {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombre + " confirme la operación");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mLogin = getLayoutInflater().inflate(R.layout.dialogo_confirmar, null, false);
        final AlertDialog.Builder Confirma = new AlertDialog.Builder(getContext());
        final EditText semail = mLogin.findViewById(R.id.emailDialogo);
        semail.setEnabled(false);
        semail.setText(emailAn);
        final EditText scontraseña = mLogin.findViewById(R.id.contraseñaDialogo);
        final Button botonSig = mLogin.findViewById(R.id.btn1);
        Confirma
                .setView(mLogin)
                .setCustomTitle(myMsgtitle);
        final AlertDialog dialogoConfirma = Confirma.create();
        botonSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contraseñaAn = scontraseña.getText().toString();
                if (!contraseñaAn.isEmpty()) {
                    mAuth.signOut();
                    mAuth.signInWithEmailAndPassword(emailAn, contraseñaAn).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                id = mAuth.getCurrentUser().getUid();
                                dialogoConfirma.dismiss();
                                Intent intent = new Intent(Menu.getInstance(), Login.class);
                                Menu.getInstance().finish();
                                startActivity(intent);
                            }else if(!task.isSuccessful()){
                                Toast.makeText(getActivity(), "No se pudo iniciar sesion, compruebe los datos", Toast.LENGTH_SHORT).show();
                                scontraseña.setError("Comprebe la contraseña");
                            }
                        }
                    });
                } else if (contraseñaAn.isEmpty()) {
                    scontraseña.setError("Introduzca la contraseña");
                }
            }
        });
        dialogoConfirma.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                scontraseña.setEnabled(true);
                botonSig.setEnabled(true);
                Menu.cargando(false);
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
                Menu.cargando(true);
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
                                                mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombreNu).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                trayecto = documentSnapshot.getString("marca temporal");
                                                                                if (trayecto != null) {
                                                                                    String fechaGuardada = "Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                                            " del " + trayecto.charAt(19) + trayecto.charAt(20) +
                                                                                            " de " + trayecto.charAt(25) + trayecto.charAt(26) + trayecto.charAt(27) + trayecto.charAt(28);
                                                                                    DateFormat fechaF = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
                                                                                    String fechaAhora = "Iniciado el " + fechaF.format(Calendar.getInstance().getTime());
                                                                                    if (fechaAhora.equals(fechaGuardada)) {
                                                                                        trayectoBo = true;
                                                                                        DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                                                                                        String horaAhora = hourFormat.format(Calendar.getInstance().getTime());
                                                                                        trayecto = trayecto + fechaAhora.replace("Iniciado el ", " ") + " a las " + horaAhora;
                                                                                    }
                                                                                }
                                                                                distan2 = 1.2;
                                                                                Menu.cargando(false);
                                                                                touch(false);
                                                                                enviajornada(obra, null);
                                                                                cancel();
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
                                Menu.cargando(false);
                                touch(false);
                                compruebaObra(obra);
                                cancel();
                            }
                        } else if (Double.compare(distan, 50.0) > 0) {
                            distan2 = 1.0;
                            Toast.makeText(getActivity(), "Localizando...", Toast.LENGTH_LONG).show();
                            dis = dis + 50.0;
                        }
                    }

                    @Override
                    public void onFinish() {
                        latitudDetectada = geoPointLocalizayo.getLatitude();
                        longitudDetectada = geoPointLocalizayo.getLongitude();
                        distan = SphericalUtil.computeDistanceBetween(new LatLng(latitudDetectada, longitudDetectada), new LatLng(latitudGuardada, longitudGuardada));
                        if (distan2 == 1.2) {
                            distan2 = 1.0;
                            Menu.cargando(false);
                            touch(false);
                            cancel();
                        } else if (distan2 == 1.0) {
                            distan2 = 1.1;
                            if (distan == null || latitudDetectada == null || longitudDetectada == null || Double.compare(distan, dis) > 0) {
                                if (entrada_salida.equals("Entrada")) {
                                    comp = "iniciada";
                                    mDb.collection("Todas las ids").document(id).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mDb.collection("Todas las ids").document(id).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(final DocumentSnapshot documentSnapshot) {
                                                                            mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombreNu).update("obra", obra).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    trayecto = documentSnapshot.getString("marca temporal");
                                                                                    if (trayecto != null) {
                                                                                        String fechaGuardada = "Iniciado el " + trayecto.charAt(12) + trayecto.charAt(13) +
                                                                                                " del " + trayecto.charAt(19) + trayecto.charAt(20) +
                                                                                                " de " + trayecto.charAt(25) + trayecto.charAt(26) + trayecto.charAt(27) + trayecto.charAt(28);
                                                                                        DateFormat fechaF = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
                                                                                        String fechaAhora = "Iniciado el " + fechaF.format(Calendar.getInstance().getTime());
                                                                                        if (fechaAhora.equals(fechaGuardada)) {
                                                                                            trayectoBo = true;
                                                                                            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                                                                                            String horaAhora = hourFormat.format(Calendar.getInstance().getTime());
                                                                                            trayecto = trayecto + fechaAhora.replace("Iniciado el ", " ") + " a las " + horaAhora;
                                                                                        }
                                                                                    }
                                                                                    Menu.cargando(false);
                                                                                    touch(false);
                                                                                    if (dis >= 650) {
                                                                                        Menu.cargando(true);
                                                                                        touch(true);
                                                                                        final TextView myMsgtitle = new TextView(getActivity());
                                                                                        myMsgtitle.setText("Se te ha detectado muy lejos de la obra " + obra);
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

                                                                                                } else if (JustTexto.isEmpty()) {

                                                                                                    sJustificar.setHintTextColor(Color.RED);

                                                                                                }
                                                                                            }
                                                                                        });
                                                                                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                                                                            @Override
                                                                                            public void onShow(final DialogInterface dialog) {
                                                                                                sJustificar.setEnabled(true);
                                                                                                botonSiguiente.setEnabled(true);
                                                                                                Menu.cargando(false);
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
                                        Menu.cargando(false);
                                        touch(false);
                                        Menu.cargando(true);
                                        touch(true);
                                        final TextView myMsgtitle = new TextView(getActivity());
                                        myMsgtitle.setText("Se te ha detectado muy lejos de la obra " + obra);
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

                                                } else if (JustTexto.isEmpty()) {

                                                    sJustificar.setHintTextColor(Color.RED);

                                                }
                                            }
                                        });
                                        dialogoLogin.setOnShowListener(new DialogInterface.OnShowListener() {
                                            @Override
                                            public void onShow(final DialogInterface dialog) {
                                                sJustificar.setEnabled(true);
                                                botonSiguiente.setEnabled(true);
                                                Menu.cargando(false);
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
                    }
                }.start();
            }
        });
    }

    private void compruebaObra(final String obra) {
        Menu.cargando(true);
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
                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("comprobar", comp).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombreNu).update("obra", null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                DateFormat dayFormat = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
                                                                DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
                                                                String fecha = dayFormat.format(Calendar.getInstance().getTime());
                                                                String hora = hourFormat.format(Calendar.getInstance().getTime());
                                                                trayecto = "Iniciado el " + fecha + " a las " + hora + " y finalizado el ";
                                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).update("marca temporal", trayecto).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Menu.cargando(false);
                                                                        touch(false);
                                                                        enviajornada(obra, obcomp);
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
                        Menu.cargando(false);
                        touch(false);
                        Toast.makeText(getActivity(), "No has iniciado jornada en esta obra", Toast.LENGTH_SHORT).show();
                    }
                }
                Menu.cargando(false);
                touch(false);
            }
        });
        getActivity().overridePendingTransition(0, 0);
    }

    private void enviajornada(final String obra, String obcomp) {
        Menu.cargando(true);
        touch(true);

        DateFormat dfecha = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat daño = new SimpleDateFormat("yyyy");
        DateFormat dmes = new SimpleDateFormat("MM");
        DateFormat ddia = new SimpleDateFormat("dd");
        final String[] fecha = {dfecha.format(Calendar.getInstance().getTime())};
        final String ano1 = daño.format(Calendar.getInstance().getTime());
        final String mes = dmes.format(Calendar.getInstance().getTime());
        final String dia = ddia.format(Calendar.getInstance().getTime());

        DateFormat dhora = new SimpleDateFormat("HH:mm:ss");
        final String[] hora = {dhora.format(Calendar.getInstance().getTime())};

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String mañaOtard = null;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            mañaOtard = "Mañana";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            mañaOtard = "Tarde";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            mañaOtard = "Tarde";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            mañaOtard = "Noche";
        }


        map.put("nombre", nombreNu);
        map.put("Entrada o Salida", entrada_salida);
        map.put("obra", obra);
        map.put("rol", roles);
        map.put("fecha", fecha[0]);
        map.put("hora", hora[0]);
        map.put("Mañana o tarde", mañaOtard);
        map.put("UID", id);
        map.put("iniciado por", nombreNu);
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
        final String finalMañaOtard = mañaOtard;
        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String es = null;
                String mt = null;
                if (entrada_salida.equals("Entrada")) {
                    es = "E";
                } else if (entrada_salida.equals("Salida")) {
                    es = "S";
                }
                if (finalMañaOtard.equals("Mañana")) {
                    mt = "M";
                } else if (finalMañaOtard.equals("Tarde") || finalMañaOtard.equals("Noche")) {
                    mt = "T";
                }
                String exis = documentSnapshot.getString(dia + mes + ano1);
                if (exis != null) {
                    exis = exis + es + mt + hora[0] + ",";
                } else if (exis == null) {
                    exis = es + mt + hora[0] + ",";
                }
                mapA.put(dia + mes + ano1, exis);
                mDb.collection("Empresas").document(empresa).collection("Registro").document(ano1).collection(mes).document(dia).collection(hora[0]).document(nombreNu).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                    mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").collection(ano1).document("MESES").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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
                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").set(mapA).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").collection(ano1).document("MESES").set(mapM).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").set(mapD).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDb.collection("Empresas").document(empresa).collection(roles).document(nombreNu).collection("Registro").document("AÑOS").collection(ano1).document("MESES").collection(mes).document("DIAS").collection(dia).document(hora[0]).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        int valorOnline = documentSnapshot.getLong("online").intValue();
                                                                        Log.d("valor", String.valueOf(valorOnline));
                                                                        final Map<String, Object> mapES = new HashMap<>();
                                                                        if (entrada_salida.equals("Entrada")) {
                                                                            if (valorOnline >= 0) {
                                                                                valorOnline = valorOnline + 1;
                                                                                mapES.put("online", valorOnline);
                                                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).set(mapES, SetOptions.merge());
                                                                            }

                                                                        } else if (entrada_salida.equals("Salida")) {
                                                                            if (valorOnline > 0) {
                                                                                valorOnline = valorOnline - 1;
                                                                                mapES.put("online", valorOnline);
                                                                                mDb.collection("Empresas").document(empresa).collection("Obras").document(obra).set(mapES, SetOptions.merge());
                                                                            }
                                                                        }
                                                                        DateFormat dfecha1 = new SimpleDateFormat("dd MM yyyy");
                                                                        fecha[0] = dfecha1.format(Calendar.getInstance().getTime());

                                                                        DateFormat dhora1 = new SimpleDateFormat("HH:mm:ss");
                                                                        hora[0] = dhora1.format(Calendar.getInstance().getTime());

                                                                        final Map<String, Object> mapf1 = new HashMap<>();
                                                                        mapf1.put("Desde", nombre);
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
                                                                                Menu.cargando(false);
                                                                                touch(false);
                                                                                firmar();
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
                });
            }
        });
    }

    private void firmar() {
        Menu.cargando(true);
        touch(true);
        final TextView myMsgtitle = new TextView(getActivity());
        myMsgtitle.setText(nombre + " debe firmar para confirmar la operación");
        myMsgtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        myMsgtitle.setTextColor(Color.BLACK);
        mFirmar = getLayoutInflater().inflate(R.layout.dialogo_firmar, null, false);
        final AlertDialog.Builder Firmar = new AlertDialog.Builder(getContext());
        final SignaturePad firma = mFirmar.findViewById(R.id.firmaCon2);
        final Button botonFirm = mFirmar.findViewById(R.id.btn1);
        final Button botonBor = mFirmar.findViewById(R.id.btn2);
        final StorageReference[] firmaRef = new StorageReference[1];
        final FirebaseAuth mAuth2 = FirebaseAuth.getInstance();
        FirebaseStorage almacen2 = FirebaseStorage.getInstance();
        final StorageReference almacenRef2 = almacen.getReferenceFromUrl("gs://pacusuarios-9035b.appspot.com");
        final String id2 = mAuth2.getCurrentUser().getUid();
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
                Menu.cargando(true);
                touch(true);
                Bitmap firmaImagen = firma.getSignatureBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                firmaImagen.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = firmaRef[0].putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Menu.cargando(false);
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
                                Menu.cargando(false);
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
                Menu.cargando(true);
                touch(true);
                firma.clear();
                Menu.cargando(false);
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
                Menu.cargando(false);
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
        if (v.equals(icReactivar)) {

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
        if (jfs.contains(tit)) {
            dAdministrarEmpleados(tit);
        } else if (obs.contains(tit)) {
            dLogin(true, tit);
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