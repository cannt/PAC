package com.japac.pac.Localizacion;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class LocalizacionUsuario {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private String id, nombre;


    public LocalizacionUsuario(GeoPoint geoPoint, Date timestamp, String id, String nombre) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.id = id;
        this.nombre = nombre;
    }

    public LocalizacionUsuario() {

    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "LocalizacionUsuario{" +
                "geoPoint=" + geoPoint +
                ", timestamp=" + timestamp +
                ", id='" + id + '\'' +
                ", nombre1='" + nombre + '\'' +
                '}';
    }
}
