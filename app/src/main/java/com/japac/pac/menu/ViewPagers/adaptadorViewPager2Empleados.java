package com.japac.pac.menu.ViewPagers;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.japac.pac.menu.Empleados.generarRegistroEmpleados;
import com.japac.pac.menu.Empleados.gestionarDiasEmpleados;
import com.japac.pac.menu.Empleados.mapaEmpleados;


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
