package com.adripher.adripher.presenter;

import android.support.v4.app.FragmentActivity;

/**
 * Created by jmercadal on 10/03/2016.
 */
public class MapsActivityPresenter implements IMapsActivityPresenterImp {
    FragmentActivity fragmentActivity;
    MapsActivityView mapsActivityView;

    public MapsActivityPresenter(FragmentActivity fragmentActivity, MapsActivityView mapsActivityView) {
        this.fragmentActivity = fragmentActivity;
        this.mapsActivityView = mapsActivityView;
    }

    @Override
    public void inicializaMapa() {
        mapsActivityView.iniciarMapa();
    }

    @Override
    public void irSitios() {
        mapsActivityView.irSitios();
    }
}
