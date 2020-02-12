package com.japac.pac.menu.viewPagers;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.japac.pac.menu.empleados.generarRegistroEmpleados;
import com.japac.pac.menu.empleados.gestionarDiasEmpleados;
import com.japac.pac.menu.empleados.mapaEmpleados;


public class adaptadorViewPager2Empleados extends FragmentStateAdapter {
    public adaptadorViewPager2Empleados(@NonNull FragmentActivity fragmentActivity) {

        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new mapaEmpleados();
            case 1:
                return new generarRegistroEmpleados();
            default:
                return new gestionarDiasEmpleados();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
