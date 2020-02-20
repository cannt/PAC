package com.japac.pac.marcadores;

import com.google.firebase.firestore.GeoPoint;

public class marcadoresObras {

    private GeoPoint geoPoint;
    private String obra, jefe, tag;
    private long online;

    public marcadoresObras(GeoPoint geoPoint, String obra, String jefe, String tag, long online) {
        this.geoPoint = geoPoint;
        this.obra = obra;
        this.jefe = jefe;
        this.tag = tag;
        this.online = online;
    }

    public marcadoresObras() {
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint position) {
        this.geoPoint = position;
    }

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }

    public String getJefe() {
        return jefe;
    }

    public void setJefe(String jefe) {
        this.jefe = jefe;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

}
