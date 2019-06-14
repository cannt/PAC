package com.japac.pac.Marcadores;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarcadoresObras implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private String Tag;

    public MarcadoresObras(LatLng position, String title, String snippet, String tag) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.Tag = tag;
    }

    public MarcadoresObras() {
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        this.Tag = tag;
    }

}
