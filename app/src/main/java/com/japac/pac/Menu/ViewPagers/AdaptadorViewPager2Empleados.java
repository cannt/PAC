package com.japac.pac.Menu.ViewPagers;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.japac.pac.Menu.Empleados.generarRegistroEmpleados;
import com.japac.pac.Menu.Empleados.gestionarDiasEmpleados;
import com.japac.pac.Menu.Empleados.mapaEmpleados;


public class AdaptadorViewPager2Empleados extends FragmentStateAdapter {
    public AdaptadorViewPager2Empleados(@NonNull FragmentActivity fragmentActivity) {

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
