package com.japac.pac;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.japac.pac.marcadores.marcadoresObras;

public class adaptadorObrasLista extends FirestoreRecyclerAdapter<marcadoresObras, adaptadorObrasLista.holderObrasLista> {

    private OnItemClickListener listener;

    public adaptadorObrasLista(@NonNull FirestoreRecyclerOptions<marcadoresObras> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull holderObrasLista holder, int position, @NonNull marcadoresObras model) {
        holder.textViewTitle.setText(model.getObra());
        String jef = model.getJefe();
        if(jef==null){
            jef = "no";
        }
        if(jef.equals("no")){
            holder.textViewJefe.setText("Sin jefe de obra");
        }else{
            holder.textViewJefe.setText(model.getJefe());
        }
        if(model.getOnline() == 0){
            holder.textViewOnline.setTextColor(Color.RED);
        }else if(model.getOnline() > 0){
            holder.textViewOnline.setTextColor(Color.GREEN);
        }
        holder.textViewOnline.setText(String.valueOf(model.getOnline()));

    }

    @NonNull
    @Override
    public holderObrasLista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        return new holderObrasLista(v);
    }

    class holderObrasLista extends RecyclerView.ViewHolder{
        final TextView textViewTitle;
        final TextView textViewJefe;
        final TextView textViewOnline;


        holderObrasLista(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textoObra);
            textViewJefe = itemView.findViewById(R.id.jefesObra);
            textViewOnline = itemView.findViewById(R.id.usuariosActivos);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(position);
                    }
                }
            });

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

}
