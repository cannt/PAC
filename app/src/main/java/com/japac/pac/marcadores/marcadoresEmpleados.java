package com.japac.pac.marcadores;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class marcadoresEmpleados {

    private GeoPoint geoPoint;
    private String nombre, obra, estado, Tag;
    private Boolean desactivado;
    private @ServerTimestamp
    Date timestamp;

    public marcadoresEmpleados(GeoPoint geoPoint, String nombre, String obra, String estado, String tag, Boolean desactivado, Date timestamp) {
        this.geoPoint = geoPoint;
        this.nombre = nombre;
        this.obra = obra;
        this.estado = estado;
        this.Tag = tag;
        this.desactivado = desactivado;
        this.timestamp = timestamp;
    }

    public marcadoresEmpleados() {
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint position) {
        this.geoPoint = position;
    }

    public String getNombre() {return nombre;}

    public void setNombre(String nom){this.nombre = nom;}

    public String getObra() {
        return obra;
    }

    public void setObra(String obr) {
        this.obra = obr;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String est) {
        this.estado = est;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String ta) {
        this.Tag = ta;
    }

    public Boolean getDesactivado(){return desactivado;}

    public void setDesactivado(Boolean desactivado){
        this.desactivado = desactivado;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}

