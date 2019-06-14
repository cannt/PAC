package com.japac.pac.Localizacion;

import com.google.firebase.firestore.GeoPoint;

public class LocalizacionObra {

    private GeoPoint geoPoint;
    private String jefe, obra;


    public LocalizacionObra(GeoPoint geoPoint, String jefe, String obra) {
        this.geoPoint = geoPoint;
        this.jefe = jefe;
        this.obra = obra;
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

    @Override
    public String toString() {
        return "LocalizacionObra{" +
                "geoPoint=" + geoPoint +
                ", jefe='" + jefe + '\'' +
                ", obra='" + obra + '\'' +
                '}';
    }
}
