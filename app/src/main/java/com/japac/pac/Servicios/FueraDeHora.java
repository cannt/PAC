package com.japac.pac.Servicios;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.japac.pac.Auth.Login;
import com.japac.pac.R;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FueraDeHora extends AppCompatActivity {
    public static boolean acepta = false;
    private TextView pPt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuera_de_hora);
        pPt = (TextView) findViewById(R.id.PrivacyPolicy);
        pPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://jatj98231.wixsite.com/pac-privacy-policy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        final AlertDialog.Builder fuera = new AlertDialog.Builder(FueraDeHora.this);
        fuera.setTitle("Fuera de jornada")
                .setMessage("¿Desea continuar de todas formas?")
                .setPositiveButton("continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        acepta = true;
                        Intent intent2 = new Intent(FueraDeHora.this, Login.class);
                        startActivity(intent2);
                    }
                })
                .setNegativeButton("salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        acepta = false;
                        finish();
                        System.exit(0);
                    }
                });
        final AlertDialog dialogoFuera = fuera.create();
        dialogoFuera.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogoFuera.create();
        }
        dialogoFuera.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final CharSequence negativeButtonText = defaultButton.getText();
                new CountDownTimer(10000, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        defaultButton.setText(String.format(
                                Locale.getDefault(), "%s (%d)",
                                negativeButtonText,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                        ));
                    }
                    @Override
                    public void onFinish() {
                        if ((dialogoFuera).isShowing()) {
                            dialogoFuera.dismiss();
                            finish();
                            System.exit(0);
                        }
                    }
                }.start();
            }
        });
        dialogoFuera.show();

    }

    public static boolean returnAcepta () {
        return acepta;
    }

    public void setAcepta(boolean Acepta) {
        this.acepta = Acepta;
    }

}
