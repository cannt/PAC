package com.japac.pac.Marcadores;

import com.google.firebase.firestore.GeoPoint;

public class MarcadoresEmpleados {

    private GeoPoint geoPoint;
    private String nombre, obra, estado, Tag;

    public MarcadoresEmpleados(GeoPoint geoPoint, String nombre, String obra, String estado, String tag) {
        this.geoPoint = geoPoint;
        this.nombre = nombre;
        this.obra = obra;
        this.estado = estado;
        this.Tag = tag;
    }

    public MarcadoresEmpleados() {
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

}

