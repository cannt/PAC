package com.japac.pac.localizacion;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class localizacionUsuario {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private String id, nombre, obra, estado;
    private Boolean desactivado;


    public localizacionUsuario(GeoPoint geoPoint, Date timestamp, String id, String nombre, String obra, String estado, Boolean desactivado) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.id = id;
        this.nombre = nombre;
        this.obra = obra;
        this.estado = estado;
        this.desactivado = desactivado;
    }

    public localizacionUsuario() {

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

    public String getObra() {
        return obra;
    }

    public void setObra(String obra) {
        this.obra = obra;
    }

    public String getEstado(){
        return estado;
    }

    public void setEstado(String estado){
        this.estado = estado;
    }

    public Boolean getDesactivado(){
        return desactivado;
    }

    public void setDesactivado(Boolean desactivado){
        this.desactivado = desactivado;
    }

    @Override
    public String toString() {
        return "localizacionUsuario{" +
                "geoPoint=" + geoPoint +
                ", timestamp=" + timestamp +
                ", id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", obra='" + obra + '\'' +
                ", estado='" + estado + '\'' +
                ", desactivado='" + desactivado + '\'' +
                '}';
    }
}
