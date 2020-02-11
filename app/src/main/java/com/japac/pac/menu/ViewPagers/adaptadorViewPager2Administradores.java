package com.japac.pac.menu.ViewPagers;



import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.japac.pac.menu.Administradores.gestionarDiasAdministradores;
import com.japac.pac.menu.Administradores.gestionarEmpleados;
import com.japac.pac.menu.Administradores.menuPrincipalAdministradores;

public class adaptadorViewPager2Administradores extends FragmentStateAdapter {


    public adaptadorViewPager2Administradores(@NonNull FragmentActivity fragmentActivity) {

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

