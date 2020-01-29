package com.japac.pac.Menu.Empleados;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.japac.pac.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class menuPrincipalEmpleados extends Fragment {


    public menuPrincipalEmpleados() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gestionar_empleados, container, false);
    }

}
