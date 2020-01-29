package com.japac.pac.Marcadores;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterItem;

public class MarcadoresObras {

    private GeoPoint geoPoint;
    private String obra, jefe, Tag;
    private long online;

    public MarcadoresObras(GeoPoint geoPoint, String obra, String jefe, String tag, long online) {
        this.geoPoint = geoPoint;
        this.obra = obra;
        this.jefe = jefe;
        this.Tag = tag;
        this.online = online;
    }

    public MarcadoresObras() {
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

    public void setObra(String obr) {
        this.obra = obr;
    }

    public String getJefe() {
        return jefe;
    }

    public void setJefe(String jef) {
        this.jefe = jef;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String ta) {
        this.Tag = ta;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(int onl) {
        this.online = onl;
    }

}
