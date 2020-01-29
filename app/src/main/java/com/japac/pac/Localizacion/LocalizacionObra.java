package com.japac.pac.Localizacion;

import com.google.firebase.firestore.GeoPoint;

public class LocalizacionObra {

    private GeoPoint geoPoint;
    private String jefe, obra;
    private int online;


    public LocalizacionObra(GeoPoint geoPoint, String jefe, String obra, int online) {
        this.geoPoint = geoPoint;
        this.jefe = jefe;
        this.obra = obra;
        this.online = online;
    }

    public LocalizacionObra() {

    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getJefe() {
        return jefe;
    }

    public void setJefe(String jefe) {
        this.jefe = jefe;
    }

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }

    public int getOnline(){return online;}

    public void setOnline(int online){this.online = online;}

    @Override
    public String toString() {
        return "LocalizacionObra{" +
                "geoPoint=" + geoPoint +
                ", jefe='" + jefe + '\'' +
                ", obra='" + obra + '\'' +
                ", online='" + online + '\'' +
                '}';
    }
}
