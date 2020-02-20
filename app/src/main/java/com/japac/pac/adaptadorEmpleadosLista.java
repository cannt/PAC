package com.japac.pac;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.japac.pac.marcadores.marcadoresEmpleados;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class adaptadorEmpleadosLista extends FirestoreRecyclerAdapter<marcadoresEmpleados, adaptadorEmpleadosLista.holderEmpleadosLista> {

    private adaptadorEmpleadosLista.OnItemClickListener listener;
    private Context context;

    public adaptadorEmpleadosLista(@NonNull FirestoreRecyclerOptions<marcadoresEmpleados> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final adaptadorEmpleadosLista.holderEmpleadosLista holder, int position, @NonNull marcadoresEmpleados model) {

        holder.textViewTitle.setText(model.getNombre());
        holder.textViewTitle.setTextSize(18);
        String obra = model.getObra();
        String address = null;
        if (obra == null) {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());
                if (geocoder != null) {
                    if (model.getGeoPoint() != null) {
                        addresses = geocoder.getFromLocation(model.getGeoPoint().getLatitude(), model.getGeoPoint().getLongitude(), 1);
                        if (addresses.size() > 0) {
                            address = addresses.get(0).getAddressLine(0);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            address = "Trabajando en " + model.getObra();
        }
        if (address != null) {
            holder.textViewJefe.setText(address);
        } else {
            if (model.getGeoPoint() != null) {
                holder.textViewJefe.setText(model.getGeoPoint().toString().replaceAll("GeoPoint", "").replaceAll("\\}", "").replaceAll("\\{", "").trim());
            }
        }
        holder.textViewOnline.setTextSize(13);
        if (model.getEstado().equals("online") && model.getObra()!=null) {
            if (holder.textViewOnline.getAnimation() != null) {
                if (holder.textViewOnline.getAnimation().isInitialized()) {
                    holder.textViewOnline.getAnimation().cancel();
                }
            }
            holder.textViewOnline.setText("Trabajando");
            holder.textViewOnline.setTextColor(Color.parseColor("#FF00ff00"));
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(200);
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    holder.textViewOnline.setTextColor(Color.parseColor("#FF00ff00"));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.textViewOnline.setTextColor(Color.parseColor("#8000ff00"));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {


                }
            });
            holder.textViewOnline.startAnimation(anim);
        } else if (model.getEstado().equals("offline") && model.getObra()!=null) {
            if (holder.textViewOnline.getAnimation() != null) {
                if (holder.textViewOnline.getAnimation().isInitialized()) {
                    holder.textViewOnline.getAnimation().cancel();
                }
            }
            holder.textViewOnline.setText("Trabajando");
            holder.textViewOnline.setTextColor(Color.parseColor("#FFff0000"));
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(200);
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    holder.textViewOnline.setTextColor(Color.parseColor("#FFff0000"));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.textViewOnline.setTextColor(Color.parseColor("#80ff0000"));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {


                }
            });
            holder.textViewOnline.startAnimation(anim);
        }else if (model.getEstado().equals("online")) {
            if (holder.textViewOnline.getAnimation() != null) {
                if (holder.textViewOnline.getAnimation().isInitialized()) {
                    holder.textViewOnline.getAnimation().cancel();
                }
            }
            holder.textViewOnline.setTextColor(Color.parseColor("#FF00ff00"));
            holder.textViewOnline.setText("Conectado");
        } else if (model.getEstado().equals("offline")) {
            if (holder.textViewOnline.getAnimation() != null) {
                if (holder.textViewOnline.getAnimation().isInitialized()) {
                    holder.textViewOnline.getAnimation().cancel();
                }
            }
            holder.textViewOnline.setTextColor(Color.parseColor("#FFff0000"));
            holder.textViewOnline.setText("Desconectado");
        }

    }

    @NonNull
    @Override
    public adaptadorEmpleadosLista.holderEmpleadosLista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        context = parent.getContext();
        return new adaptadorEmpleadosLista.holderEmpleadosLista(v);
    }

    class holderEmpleadosLista extends RecyclerView.ViewHolder {
        final TextView textViewTitle;
        final TextView textViewJefe;
        final TextView textViewOnline;


        holderEmpleadosLista(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textoObra);
            textViewJefe = itemView.findViewById(R.id.jefesObra);
            textViewOnline = itemView.findViewById(R.id.usuariosActivos);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(adaptadorEmpleadosLista.OnItemClickListener listener) {
        this.listener = listener;
    }

}

