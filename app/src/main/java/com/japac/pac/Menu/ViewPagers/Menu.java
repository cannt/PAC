package com.japac.pac.Menu.ViewPagers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.View;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.PDF.MyPrintDocumentAdapter;
import com.japac.pac.PDF.fragmentoCompartir;
import com.japac.pac.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class Menu extends AppCompatActivity implements
        fragmentoCompartir.ItemClickListener{
    private FirebaseAuth mAuth;
    public static final int Permisos = 8991;
    String[] permisos = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    FirebaseFirestore mDb;
    FirebaseStorage almacen;
    StorageReference almacenRef;

    private static String rol, SHAREempleado, mes1, SHAREano, SHAREmes, empresa, emailAn;

    private static File folder, fileShare;

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager2;
    private static ProgressBar progressBar;
    private static View view;
    private static Menu instance;

    private Boolean emailShare;

    private static boolean cambio = false;

    private static fragmentoCompartir fragmentoCompartir;


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
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        view = (View) findViewById(R.id.viewGrey);
        instance = this;
        if (compruebapermisos()) {
            cargando(true);
            mAuth = FirebaseAuth.getInstance();
            mDb = FirebaseFirestore.getInstance();
                    mDb.collection("Todas las ids").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    rol = documentSnapshot.getString("rol");
                    almacen = FirebaseStorage.getInstance();
                    almacenRef = almacen.getReference();
                    viewPager2 = findViewById(R.id.view_pager);
                    if (rol.equals("Administrador")) {
                        viewPager2.setAdapter(new AdaptadorViewPager2Administradores(Menu.this));
                    } else if (rol.equals("Empleado")) {
                        viewPager2.setAdapter(new AdaptadorViewPager2Empleados(Menu.this));
                    }
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
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    case 1: {
                                        tab.setText("Empleados");
                                        tab.setIcon(R.drawable.ic_empleados);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    default: {
                                        tab.setText("Dias libres");
                                        tab.setIcon(R.drawable.ic_gesdias);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                }
                            } else if (rol.equals("Empleado")) {
                                switch (position) {
                                    case 0: {
                                        tab.setText("Mapa");
                                        tab.setIcon(R.drawable.ic_obras);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    case 1: {
                                        tab.setText("Generar registro");
                                        tab.setIcon(R.drawable.ic_registro);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    default: {
                                        tab.setText("Dias libres");
                                        tab.setIcon(R.drawable.ic_gesdias);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
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
                            tabLayout.getTabAt(tab.getPosition()).getIcon().setColorFilter(ContextCompat.getColor(Menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                            cambio = true;
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {
                            tabLayout.getTabAt(tab.getPosition()).getIcon().setColorFilter(ContextCompat.getColor(Menu.this, R.color.transparenteBlanco), PorterDuff.Mode.SRC_IN);
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
        Menu.cargando(false);
        fragmentoCompartir = new fragmentoCompartir();
        fragmentoCompartir.show(getSupportFragmentManager(), "Menu Compartir");
        emailShare = false;
    }

    public static void cargando(Boolean carg) {

        if (carg) {
            progressBar.setProgress(25);
            progressBar.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        } else if (!carg) {
            progressBar.setProgress(100);
            progressBar.setVisibility(View.INVISIBLE);
            view.setVisibility(View.GONE);
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

    public static void datos(File folder1, File fileShare1, String SHAREempleado1, String mes12, String SHAREano1, String SHAREmes1, String empresa1, String emailAn1){
        cargando(true);
        folder =folder1;
        fileShare=fileShare1;
        SHAREempleado = SHAREempleado1;
        mes1 = mes12;
        SHAREano = SHAREano1;
        SHAREmes = SHAREmes1;
        empresa = empresa1;
        emailAn = emailAn1;
        cargando(false);
    }

    public static Menu getInstance() {
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
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Registro de jornada del mes " + SHAREmes + " del año " + SHAREano + " del empleado " + SHAREempleado);
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

                        String jobName =  "PAC Registro PDF";

                        printManager.print(jobName, new MyPrintDocumentAdapter(getContext(), fileShare.getPath()), new PrintAttributes.Builder().build());
                        cargando(false);
                        break;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() != 0 && view.getVisibility()!=View.VISIBLE) {
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1,false);
        }
    }

}
