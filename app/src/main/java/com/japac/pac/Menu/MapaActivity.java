package com.japac.pac.Menu;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.Localizacion.LocalizacionObra;
import com.japac.pac.Localizacion.LocalizacionUsuario;
import com.japac.pac.Marcadores.MarcadoresObras;
import com.japac.pac.R;
import com.japac.pac.Servicios.FueraDeHora;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Mapa listo", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (compruebapermisos()) {
            detalles();

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
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

            init();

        }
    }

    private Map<String, String> markersMap;

    private String id,IoF, codigoEmpresa, comp, empresa, nombre, roles, nombreAm, emailAn, codigoEmpleado, obcomprueba, sobra, busquedaString, ob, jf, jefes, codigoEmpleadoChech, JFC, JFO;
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

    private ImageView gps, icCrear;

    private View mNombres;

    private EditText mBuscar;

    private TextView pPt;

    private GoogleMap mMap;

    private GeoPoint geoPointLocalizaDirc, geoPointLocalizayo;

    private FusedLocationProviderClient mProovedor;

    private LocalizacionUsuario mLocalizarUsuario;

    private LocalizacionObra mLocalizacionObra;

    CollectionReference geoFirestoreRef, geoFirestoreRef2;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);


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
                        } else {
                            Toast.makeText(MapaActivity.this, "NO EXISTE ID", Toast.LENGTH_SHORT).show();
                        }
                        mBuscar = (EditText) findViewById(R.id.input_buscar);
                        gps = (ImageView) findViewById(R.id.ic_gps);
                        icCrear = (ImageView) findViewById(R.id.ic_crearObra);
                        pPt = (TextView) findViewById(R.id.PrivacyPolicy);
                        pPt.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }
                        });


                        iniciarMapa();
                    }
                });
            }
    }

    private void centrarCamara() {

        mDb.collection("Empresas").document(empresa).collection("Localizaciones").document(nombre).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    GeoPoint geoPoint1 = task.getResult().getGeoPoint("geoPoint");

                    mLocaliza = new LatLng(geoPoint1.getLatitude(), geoPoint1.getLongitude());

                    setCamara();
                }
            }
        });
    }

    private void setCamara() {

        double boundaryAbajo = mLocaliza.latitude - .1;
        double boundaryIzquierdo = mLocaliza.longitude - .1;
        double boundaryArriba = mLocaliza.latitude + .1;
        double boundaryDerecho = mLocaliza.longitude + .1;

        mLocalizacionLaLo = new LatLngBounds(
                new LatLng(boundaryAbajo, boundaryIzquierdo),
                new LatLng(boundaryArriba, boundaryDerecho));

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLocalizacionLaLo, 0));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_PREDETERMINADO), 2000, null);
        Toast.makeText(MapaActivity.this, "HECHO", Toast.LENGTH_SHORT).show();


    }

    private void detalles() {

        if (mLocalizarUsuario == null) {
            mLocalizarUsuario = new LocalizacionUsuario();
            mLocalizarUsuario.setId(id);
            mLocalizarUsuario.setNombre(nombre);
            localizacion();

        } else {

        }
    }

    private void guardarLocalizacion() {

        if (mLocalizarUsuario != null) {
            DocumentReference locationRef = mDb
                    .collection("Empresas")
                    .document(empresa)
                    .collection("Localizaciones")
                    .document(nombre);
            locationRef.set(mLocalizarUsuario).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {


                    centrarCamara();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

    }


    private void init() {

        mBuscar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    geoLocalizar();
                }
                return false;
            }
        });

        gps.setOnClickListener(MapaActivity.this);
        icCrear.setOnClickListener(MapaActivity.this);

        ocultarTeclado();
    }

    private void geoLocalizar() {
        busquedaString = mBuscar.getText().toString();
        Geocoder geocoder = new Geocoder(MapaActivity.this);
        List<Address> list = new ArrayList<>();
        if (obs.contains(busquedaString)) {
            mDb.collection("Empresas").document(empresa).collection("Obras").document(busquedaString).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    GeoPoint geo = documentSnapshot.getGeoPoint("geoPoint");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(geo.getLatitude(), geo.getLongitude()), ZOOM_PREDETERMINADO));
                }
            });
        }
        try {
            list = geocoder.getFromLocationName(busquedaString, 1);
        } catch (IOException e) {
            Toast.makeText(this, "No se a podido encontrar", Toast.LENGTH_SHORT).show();
        }
        if (list.size() > 0) {
            final Address address = list.get(0);

            MarkerOptions options = new MarkerOptions()
                    .title("Crear obra")
                    .position(new LatLng(address.getLatitude(), address.getLongitude()))
                    .draggable(true);

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
                    if(!arrastrado){
                        direccionLat = address.getLatitude();
                        direccionLong = address.getLongitude();
                    }else if(arrastrado){
                        direccionLat = marker.getPosition().latitude;
                        direccionLong = marker.getPosition().longitude;
                    }
                    mLocalizaAddress = new LatLng(direccionLat, direccionLong);
                    mMap.setOnInfoWindowClickListener(this);
                    /*crearObra();*/
                }
            });


        }
    }

    private void localizacion() {

        mProovedor = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (compruebapermisos()) {

                final Task localizacion = mProovedor.getLastLocation();
                localizacion.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {

                            Location locacizacionActual = (Location) task.getResult();
                            geoPointLocalizayo = new GeoPoint(locacizacionActual.getLatitude(), locacizacionActual.getLongitude());
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
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(ob).delete();
                                }
                                mDb.collection("Empresas").document(empresa).collection("Obras").document(sobra).set(mLocalizacionObra);
                            } else if (!obraBo) {
                                mLocalizarUsuario.setGeoPoint(geoPointLocalizayo);
                                mLocalizarUsuario.setTimestamp(null);
                            }
                            guardarLocalizacion();
                        } else {

                        }
                    }
                });
                firestoreNombres();
            }
        } catch (SecurityException e) {

        }

    }


    private void iniciarMapa() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);

        mapFragment.getMapAsync(MapaActivity.this);


    }

    public boolean isServicesOK() {

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapaActivity.this);

        if (available == ConnectionResult.SUCCESS) {

            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapaActivity.this, available, ERROR_DIALOGO_PEDIR);
        } else {
            Toast.makeText(this, "Mapas no funciona", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    private boolean compruebapermisos() {
        int resultado;
        List<String> listaPermisosNecesarios = new ArrayList<>();
        for (String perm : permisos) {
            resultado = ContextCompat.checkSelfPermission(this, perm);
            if (resultado != PackageManager.PERMISSION_GRANTED) {
                listaPermisosNecesarios.add(perm);
            }
        }
        if (!listaPermisosNecesarios.isEmpty()) {
            ActivityCompat.requestPermissions(this, listaPermisosNecesarios.toArray(new String[listaPermisosNecesarios.size()]), Permisos);
            return false;
        }
        return true;
    }

    private void ocultarTeclado() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void firestoreObras() {
        mMap.clear();
        geoFirestoreRef = mDb.collection("Empresas").document(empresa).collection("Obras");
        obs = new ArrayList<String>();
        geoFirestoreRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String obran = document.getString("obra");
                        String jefe1 = document.getString("jefe");
                        int online = document.getLong("online").intValue();
                        GeoPoint geoPoint2 = document.getGeoPoint("geoPoint");
                        if (jefe1 != null) {
                            if (jefe1.equals("no")) {
                                jefe1 = "sin jefe de obra";
                            }
                        } else if (jefe1 == null) {
                            jefe1 = "sin jefe de obra";
                        }
                        añadirMarcadores(geoPoint2.getLatitude(), geoPoint2.getLongitude(), obran, jefe1, online);
                        obs.add(obran);
                    }
                    mDb.collection("Todas las ids").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.contains("obra")) {
                                obcomprueba = documentSnapshot.getString("obra");
                            }
                        }
                    });
                } else if (!task.isSuccessful()) {
                    obs.add("SIN OBRAS");
                }
                if (obs.contains("SIN OBRAS")) {
                    obs.remove("SIN OBRAS");
                }
            }
        });
    }

    private void añadirMarcadores(final double latitud, final double longitud, String title, String snippet, int onl) {
        markersMap = new HashMap<String, String>();
        MarkerOptions mo = new MarkerOptions()
                .rotation(0);
        Marker mkr = mMap.addMarker(mo
                .title(title)
                .snippet(snippet)
                .position(new LatLng(latitud, longitud)));
        MarcadoresObras marcadoresObras = new MarcadoresObras(new GeoPoint(latitud, longitud), title, snippet, title, onl);
        markersMap.put(marcadoresObras.getTag(), mkr.getId());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mkr.showInfoWindow();

    }

    private void firestoreNombres() {
        geoFirestoreRef = mDb.collection("Empresas").document(empresa).collection("Empleado");
        geoFirestoreRef2 = mDb.collection("Empresas").document(empresa).collection("Administrador");
        jfs = new ArrayList<String>();
        geoFirestoreRef2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String jefe = document.getString("nombre");
                        jfs.add(jefe);
                    }
                    geoFirestoreRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task2) {
                            if (task2.isSuccessful()) {
                                for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                    String jefe = document2.getString("nombre");
                                    jfs.add(jefe);
                                }
                                jfs.remove(nombreAm);
                            }
                        }
                    });
                    mNombres = getLayoutInflater().inflate(R.layout.dialogo_spinner, null, false);
                    jefeSpinner = (Spinner) mNombres.findViewById(R.id.spinnerObra);
                    jefeAdapter = new ArrayAdapter<String>(MapaActivity.this, android.R.layout.simple_spinner_item, jfs);
                    jefeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    jefeSpinner.setAdapter(jefeAdapter);
                } else if (!task.isSuccessful()) {
                    jfs.add("sin empleados");
                }
                firestoreObras();
            }
        });
    }

    private void dAdmin(final String obraAd) {

        final AlertDialog.Builder obraAdministrarObras = new AlertDialog.Builder(MapaActivity.this)
                .setTitle("Menu de administracion de la obra " + obraAd)
                .setItems(new CharSequence[]{"Cambiar nombre", "Administrar jefe", "Eliminar obra", "cancelar"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                dialog.dismiss();
                                dCambiaNombreObra(obraAd);
                                break;
                            case 1:
                                dialog.dismiss();
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
                                break;
                            case 2:
                                dObraEliminar(obraAd);
                                break;
                            case 3:
                                dialog.dismiss();
                                break;
                        }
                    }
                });
        final AlertDialog dialogoAdministradorObras = obraAdministrarObras.create();
        dialogoAdministradorObras.setCanceledOnTouchOutside(false);
        dialogoAdministradorObras.show();

    }

    private void CambiarNombre(String obraAn, final String obraNu) {

        mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAn).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final GeoPoint geoPointAn = documentSnapshot.getGeoPoint("geoPoint");
                final String obraAn = documentSnapshot.getString("obra");
                final String jefeAn = documentSnapshot.getString("jefe");
                mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAn).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mLocalizacionObra = new LocalizacionObra();
                        mLocalizacionObra.setGeoPoint(geoPointAn);
                        mLocalizacionObra.setObra(obraNu);
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
                                firestoreNombres();
                            }
                        });
                    }
                });
            }
        });
    }

    private void dCambiaNombreObra(final String obraAn) {
        final AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Nuevo nombre para la obra " + obraAn);

        final EditText nuevoNombre = new EditText(this);

        alerta.setView(nuevoNombre)
                .setCancelable(false)
                .setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String obraNu = nuevoNombre.getText().toString();
                        CambiarNombre(obraAn, obraNu);
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alerta.show();

    }

    private void dObraEliminar(final String obraAd) {
        final AlertDialog.Builder obraEliminar = new AlertDialog.Builder(MapaActivity.this)
                .setTitle("¿Seguro que quiere eliminar la obra " + obraAd + "?");
        obraEliminar
                .setNeutralButton("Eliminar", null)
                .setPositiveButton("Cancelar", null);
        final AlertDialog dialogoObraEliminar = obraEliminar.create();
        dialogoObraEliminar.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button NeutralEl = (Button) dialogoObraEliminar.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button PositivoCa = (Button) dialogoObraEliminar.getButton(AlertDialog.BUTTON_POSITIVE);
                NeutralEl.setOnClickListener(new View.OnClickListener() {
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
                                            mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                            dialogoObraEliminar.dismiss();
                                            firestoreObras();
                                            ElimJefe = false;
                                        }
                                    });
                                } else if (jef.equals(null) || jef.equals("no")) {
                                    mDb.collection("Empresas").document(empresa).collection("Obras").document(obraAd).delete();
                                    dialogoObraEliminar.dismiss();
                                    firestoreObras();
                                    ElimJefe = false;
                                }
                            }
                        });
                    }
                });

                PositivoCa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoObraEliminar.dismiss();
                    }
                });
            }
        });
        dialogoObraEliminar.setCanceledOnTouchOutside(false);
        dialogoObraEliminar.show();
    }

    private void dJefeExiste(final String obraAd, final String obraAdJf) {

        final AlertDialog.Builder obraJefeExiste = new AlertDialog.Builder(MapaActivity.this)
                .setTitle("El jefe de obra de " + obraAd + " es " + obraAdJf)
                .setMessage("¿Desea cambiarlo?")
                .setPositiveButton("Cambiar", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoObraJefeExiste = obraJefeExiste.create();
        dialogoObraJefeExiste.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positivoCam = (Button) dialogoObraJefeExiste.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCan = (Button) dialogoObraJefeExiste.getButton(AlertDialog.BUTTON_NEGATIVE);

                positivoCam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cambiar = true;
                        dialogoObraJefeExiste.dismiss();
                        dJefes(obraAd, obraAdJf);
                    }
                });

                negativoCan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoObraJefeExiste.dismiss();
                    }
                });

            }
        });
        dialogoObraJefeExiste.setCanceledOnTouchOutside(false);
        dialogoObraJefeExiste.show();

    }

    private void dJefes(final String obraAd, final String obraAdJf) {

        final AlertDialog.Builder obraJefe = new AlertDialog.Builder(MapaActivity.this)
                .setTitle("Seleccione el nuevo jefe para la obra " + obraAd);
        jefeSpinner.setOnItemSelectedListener(this);
        obraJefe
                .setView(mNombres)
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoObraJefe = obraJefe.create();
        dialogoObraJefe.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positivoAc = (Button) dialogoObraJefe.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCa = (Button) dialogoObraJefe.getButton(AlertDialog.BUTTON_NEGATIVE);

                positivoAc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (jefes.equals(obraAdJf)) {
                            Toast.makeText(MapaActivity.this, obraAdJf + " ya es el jefe de la obra " + obraAd, Toast.LENGTH_LONG).show();
                            localizacion();
                            dialogoObraJefe.dismiss();
                        } else {
                            añadeJefes(obraAd, obraAdJf);
                            dialogoObraJefe.dismiss();
                        }
                    }
                });

                negativoCa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoObraJefe.dismiss();
                    }
                });
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
                    }
                }
            });
            cambiar = false;
        }
        firestoreNombres();
    }


    /*private void crearObra() {

        final AlertDialog.Builder obraCrear = new AlertDialog.Builder(icCrear.getContext());
        View mCrearDialogo = getLayoutInflater().inflate(R.layout.activity_menu_crear_obra, null);
        final EditText obra = mCrearDialogo.findViewById(R.id.insObra);
        obraCrear
                .setView(mCrearDialogo)
                .setTitle("¿Que nombre tendra la obra?")
                .setPositiveButton("Crear", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoObraCrear = obraCrear.create();
        dialogoObraCrear.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positivoCrea = (Button) dialogoObraCrear.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCancela = (Button) dialogoObraCrear.getButton(AlertDialog.BUTTON_NEGATIVE);
                positivoCrea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sobra = obra.getText().toString();
                        if (!sobra.isEmpty()) {
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
                            obra.setError("Identifique la obra");
                        }
                    }
                });
                negativoCancela.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoObraCrear.dismiss();
                    }
                });
            }
        });
        dialogoObraCrear.setCanceledOnTouchOutside(false);
        dialogoObraCrear.show();

    }*/

    private void dObraExiste() {


        final AlertDialog.Builder obraExiste = new AlertDialog.Builder(icCrear.getContext());
        obraExiste
                .setTitle("La obra " + ob + " existe")
                .setMessage("¿Desea actualizar la localizacion de " + ob + "?")
                .setPositiveButton("Actualizar", null)
                .setNegativeButton("Cancelar", null);
        final AlertDialog dialogoObraExiste = obraExiste.create();
        dialogoObraExiste.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button positivoActEx = (Button) dialogoObraExiste.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativoCancEx = (Button) dialogoObraExiste.getButton(AlertDialog.BUTTON_NEGATIVE);

                positivoActEx.setOnClickListener(new View.OnClickListener() {
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

                negativoCancEx.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogoObraExiste.dismiss();
                    }
                });
            }
        });
        dialogoObraExiste.setCanceledOnTouchOutside(false);
        dialogoObraExiste.show();

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

            /*crearObra();*/
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
        dAdmin(obr);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), (float) Math.floor(mMap
                        .getCameraPosition().zoom + 1)), 300,
                null);
        marker.showInfoWindow();
        return true;
    }
}
