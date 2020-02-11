package com.japac.pac.servicios;

import android.content.Context;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;
import com.japac.pac.R;

public class snackbarDS {

    public static void configSnackbar(Context context, Snackbar snack) {
        addMargins(snack);
        setRoundBordersBg(context, snack);
        ViewCompat.setElevation(snack.getView(), 6f);
    }

    private static void addMargins(Snackbar snack) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snack.getView().getLayoutParams();
        params.setMargins(6, 6, 6, 6);
        snack.getView().setLayoutParams(params);
    }

    private static void setRoundBordersBg(Context context, Snackbar snackbar) {
        snackbar.getView().setBackground(context.getDrawable(R.drawable.bg_snackbar));

    }
}
