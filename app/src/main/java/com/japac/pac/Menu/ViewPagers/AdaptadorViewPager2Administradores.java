package com.japac.pac.Menu.ViewPagers;



import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.japac.pac.Menu.Administradores.gestionarDiasAdministradores;
import com.japac.pac.Menu.Administradores.gestionarEmpleados;
import com.japac.pac.Menu.Administradores.menuPrincipalAdministradores;

public class AdaptadorViewPager2Administradores extends FragmentStateAdapter {


    public AdaptadorViewPager2Administradores(@NonNull FragmentActivity fragmentActivity) {

        super(fragmentActivity);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new menuPrincipalAdministradores();
            case 1:
                return new gestionarEmpleados();
            default:
                return new gestionarDiasAdministradores();
        }
    }


    @Override
    public int getItemCount() {
        return 3;
    }




}

