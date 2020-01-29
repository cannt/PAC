package com.japac.pac.Menu.ViewPagers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.japac.pac.Menu.Empleados.gestionarDiasEmpleados;
import com.japac.pac.Menu.MenuAdmin;
import com.japac.pac.Menu.MenuEmpleado;
import com.japac.pac.R;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class Menu extends AppCompatActivity {
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

    private String rol;

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager2;
    private static ProgressBar progressBar;
    private static View view;

    private static boolean cambio = false;


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
                                        tab.setText("Menu principal");
                                        tab.setIcon(R.drawable.ic_gesdias);
                                        tab.getIcon().setColorFilter(ContextCompat.getColor(Menu.this, android.R.color.white), PorterDuff.Mode.SRC_IN);
                                        break;
                                    }
                                    case 1: {
                                        tab.setText("Mapa");
                                        tab.setIcon(R.drawable.ic_gesdias);
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

    @Override
    public void onBackPressed() {
        if (viewPager2.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager2.getCurrentItem());
            ((Activity) fragment.getContext()).finish();
            viewPager2.setCurrentItem(viewPager2.getCurrentItem() - 1);
        }
    }

}
