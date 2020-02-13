package com.japac.pac.menu;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.menu.viewPagers.adaptadorViewPager2Administradores;
import com.japac.pac.menu.viewPagers.adaptadorViewPager2Empleados;
import com.japac.pac.pdf.myPrintDocumentAdapter;
import com.japac.pac.pdf.fragmentoCompartir;
import com.japac.pac.R;
import com.japac.pac.servicios.servicioLocalizacion;
import com.japac.pac.servicios.snackbarDS;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.security.AccessController.getContext;

public class menu extends AppCompatActivity implements
        fragmentoCompartir.ItemClickListener {
    private static final int Permisos = 8991;
    private final String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private FirebaseStorage almacen;
    private StorageReference almacenRef;

    private static FirebaseFirestore mDb;

    private static String rol, SHAREempleado, mes1, SHAREano, SHAREmes, empresa, emailAn;
    private static String nombre;
    private static String empre;
    private String obComp;
    private static String compro;

    private static File folder, fileShare;

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager2;
    private static ProgressBar progressBar;
    private static View view;
    private static menu instance;

    private Boolean emailShare;
    private Boolean doubleBackToExitPressedOnce = false;

    private static boolean cambio = false;

    public static Snackbar snackbar;

    public static boolean getCambioDeFragment() {
        return cambio;
    }

    public static void setCambioDeFragmento(boolean cambio1) {
        cambio = cambio1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        progressBar = findViewById(R.id.progressbar);
        view = findViewById(R.id.viewGrey);
        instance = this;
        if (compruebapermisos()) {
            cargando(true);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mDb = FirebaseFirestore.getInstance();
            mDb.collection("Todas las ids").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    rol = documentSnapshot.getString("rol");
                    almacen = FirebaseStorage.getInstance();
                    almacenRef = almacen.getReference();
                    viewPager2 = findViewById(R.id.view_pager);
                    nombre = documentSnapshot.getString("nombre");
                    empre = documentSnapshot.getString("empresa");
                    compro = documentSnapshot.getString("comprobar");
                    estado("online");
                    if (rol.equals("Administrador")) {
                        viewPager2.setAdapter(new adaptadorViewPager2Administradores(menu.this));
                    } else if (rol.equals("Empleado")) {
                        viewPager2.setAdapter(new adaptadorViewPager2Empleados(menu.this));
                    }
                    snackbar = Snackbar.make(findViewById(R.id.viewSnack), "Bienvenido " + nombre, 5000)
                            .setActionTextColor(Color.WHITE);
                    viewPager2.setUserInputEnabled(false);
                    tabLayout = findViewById(R.id.tabs);
                    tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            if (rol.equals("Administrador")) {
                                switch (position) {
                                    case 0: {
                                        tab.setText("Obras");
                                        tab.setIcon(R.drawable.ic_obras);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    case 1: {
                                        tab.setText("Empleados");
                                        tab.setIcon(R.drawable.ic_empleados);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    default: {
                                        tab.setText("Dias libres");
                                        tab.setIcon(R.drawable.ic_gesdias);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                }
                            } else if (rol.equals("Empleado")) {
                                switch (position) {
                                    case 0: {
                                        tab.setText("Mapa");
                                        tab.setIcon(R.drawable.ic_obras);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    case 1: {
                                        tab.setText("Generar registro");
                                        tab.setIcon(R.drawable.ic_registro);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    default: {
                                        tab.setText("Dias libres");
                                        tab.setIcon(R.drawable.ic_gesdias);
                                        Objects.requireNonNull(tab.getIcon()).setColorFilter(ContextCompat.getColor(menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    tabLayoutMediator.attach();
                    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition()).getIcon()).setColorFilter(ContextCompat.getColor(menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                            cambio = true;
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {
                            Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition()).getIcon()).setColorFilter(ContextCompat.getColor(menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                    cargando(false);
                }
            });

        }
    }

    public void menuShareA() {
        menu.cargando(false);
        com.japac.pac.pdf.fragmentoCompartir fragmentoCompartir = new fragmentoCompartir();
        fragmentoCompartir.show(getSupportFragmentManager(), "menu Compartir");
        emailShare = false;
    }

    public static void cargando(Boolean carg) {

        if (carg) {
            if (progressBar.getProgress() != 25) {
                progressBar.setProgress(25);
            }
            if (progressBar.getVisibility() != View.VISIBLE) {
                progressBar.setVisibility(View.VISIBLE);
            }
            if (view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (progressBar.getProgress() != 100) {
                progressBar.setProgress(100);
            }
            if (progressBar.getVisibility() != View.INVISIBLE) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            if (view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        }
    }


    private boolean compruebapermisos() {
        cargando(true);
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
        cargando(false);
        return true;
    }

    public static void datos(File folder1, File fileShare1, String SHAREempleado1, String mes12, String SHAREano1, String SHAREmes1, String empresa1, String emailAn1) {
        cargando(true);
        folder = folder1;
        fileShare = fileShare1;
        SHAREempleado = SHAREempleado1;
        mes1 = mes12;
        SHAREano = SHAREano1;
        SHAREmes = SHAREmes1;
        empresa = empresa1;
        emailAn = emailAn1;
        cargando(false);
    }

    public static menu getInstance() {
        return instance;
    }

    @Override
    public void onItemClick(final String item) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        folder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");
        fileShare = new File(folder, "Resgistro de " + SHAREempleado + " a " + mes1 + " de " + SHAREano + ".pdf");
        final Uri pathShare = Uri.fromFile(fileShare);
        almacenRef.child(empresa + "/Registros/" + SHAREempleado + "/" + SHAREano + "/" + SHAREmes + "/" + SHAREempleado + "_" + SHAREmes + "_" + SHAREano + ".pdf").getFile(fileShare).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                switch (item) {
                    case "Email":
                        cargando(true);
                        emailShare = true;
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("vnd.android.cursor.dir/email");

                        emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAn);

                        emailIntent.putExtra(Intent.EXTRA_STREAM, pathShare);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Registro de jornada del mes " + SHAREmes + " del ano " + SHAREano + " del empleado " + SHAREempleado);
                        startActivity(Intent.createChooser(emailIntent, "Envia email..."));
                        cargando(false);
                        break;
                    case "Descargar":
                        cargando(true);
                        File folder2 = new File(Environment.getExternalStorageDirectory().toString(), "Registros de " + empresa);
                        if (!folder2.exists()) {
                            folder2.mkdirs();
                        }
                        File fileShare2 = new File(folder2, "Registro de " + SHAREempleado + " a " + mes1 + " de " + SHAREano + ".pdf");
                        if (fileShare2.exists()) {
                            fileShare2.delete();
                        }
                        fileShare.renameTo(fileShare2);
                        Uri url = Uri.fromFile(fileShare2);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(url);
                        startActivity(intent);
                        cargando(false);
                        break;
                    case "Imprimir":
                        cargando(true);
                        emailShare = true;
                        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

                        String jobName = "PAC Registro PDF";

                        Objects.requireNonNull(printManager).print(jobName, new myPrintDocumentAdapter(getContext(), fileShare.getPath()), new PrintAttributes.Builder().build());
                        cargando(false);
                        break;
                }
            }
        });
    }

    public static void estado(final String estado) {
        if(mDb==null){
            mDb = FirebaseFirestore.getInstance();
        }
        if( empre!=null && nombre!=null) {
            mDb.collection("Empresas").document(empre).collection("Empleado").document(nombre).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    compro = documentSnapshot.getString("comprobar");
                    if (rol != null && rol.equals("Empleado")) {
                        HashMap<String, String> mapEstado = new HashMap<>();
                        if (estado.equals("offline")) {
                            if (compro.equals("iniciada")) {
                                mapEstado.put("estado", "trabajando offline");
                            } else if (compro.equals("finalizada") || compro.equals("no")) {
                                mapEstado.put("estado", estado);
                            }
                        } else if (estado.equals("online")) {
                            if (compro.equals("iniciada")) {
                                mapEstado.put("estado", "trabajando online");
                            } else if (compro.equals("finalizada") || compro.equals("no")) {
                                mapEstado.put("estado", estado);
                            }
                        }
                        mDb.collection("Empresas").document(empre).collection("Localizacion marcadores").document(Normalizer.normalize(nombre.toLowerCase().trim(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")).set(mapEstado, SetOptions.merge());
                    }
                }
            });
        }
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent serviceIntent = new Intent(this, servicioLocalizacion.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                menu.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : Objects.requireNonNull(manager).getRunningServices(Integer.MAX_VALUE)) {
            if ("com.japac.pac.Servicios.servicioLocalizacion".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() != 0 && view.getVisibility() != View.VISIBLE) {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1, false);
        }else if(viewPager2.getCurrentItem()==0){
            if (doubleBackToExitPressedOnce) {
                System.exit(0);
            }
            this.doubleBackToExitPressedOnce = true;
            showSnack();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

    private void killSnack() {
        if (menu.snackbar.isShown()) {
           menu.snackbar.dismiss();
        }
    }

    private void showSnack() {
        menu.snackbar.setText("Pulse atras otra vez para cerrar PAC");
        TextView tv = (menu.snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextSize(10);
        snackbarDS.configSnackbar(this, menu.snackbar);
        menu.snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationService();
        servicioLocalizacion.finaliza(false);
        estado("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        killSnack();
        servicioLocalizacion.finaliza(true);
        estado("offline");
    }

    public static void finishTask(){
        instance.finishAndRemoveTask();
    }
}
