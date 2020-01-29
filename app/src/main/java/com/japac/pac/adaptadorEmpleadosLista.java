package com.japac.pac;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.japac.pac.Marcadores.MarcadoresEmpleados;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class adaptadorEmpleadosLista extends FirestoreRecyclerAdapter<MarcadoresEmpleados, adaptadorEmpleadosLista.holderEmpleadosLista> {

    private adaptadorEmpleadosLista.OnItemClickListener listener;
    private Context context;

    public adaptadorEmpleadosLista(@NonNull FirestoreRecyclerOptions<MarcadoresEmpleados> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull adaptadorEmpleadosLista.holderEmpleadosLista holder, int position, @NonNull MarcadoresEmpleados model) {
        holder.textViewTitle.setText(model.getNombre());
        holder.textViewTitle.setTextSize(18);
        String obra = model.getObra();
        String address = null;
        if(obra==null){
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(context, Locale.getDefault());
                addresses = geocoder.getFromLocation(model.getGeoPoint().getLatitude(), model.getGeoPoint().getLongitude(), 1);
                if(addresses.size()>0){
                    address = addresses.get(0).getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(obra!=null){
            address = "Trabajando en " + model.getObra();
        }
        if(address!=null){
            holder.textViewJefe.setText(address);
        }else{
            holder.textViewJefe.setText(model.getGeoPoint().toString().replaceAll("GeoPoint", "").replaceAll("\\}", "").replaceAll("\\{", "").trim());
        }
        holder.textViewOnline.setTextSize(13);
        if(model.getEstado().equals("online")){
            holder.textViewOnline.setTextColor(Color.GREEN);
            holder.textViewOnline.setText("Conectado");
        }else if(model.getEstado().equals("offline")){
            holder.textViewOnline.setTextColor(Color.RED);
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

    class holderEmpleadosLista extends RecyclerView.ViewHolder{
        TextView textViewTitle;
        TextView textViewJefe;
        TextView textViewOnline;


        public holderEmpleadosLista(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textoObra);
            textViewJefe = itemView.findViewById(R.id.jefesObra);
            textViewOnline = itemView.findViewById(R.id.usuariosActivos);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(adaptadorEmpleadosLista.OnItemClickListener listener){
        this.listener = listener;
    }

}

