package com.adripher.adripher;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.Location;

import android.location.LocationListener;

import android.location.LocationManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,MapsActivityView {

    private GoogleMap mMap;
    LatLng position;
    Place place;
    IMapsActivityPresenterImp presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        presenter = new MapsActivityPresenter(this,this);
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, 1);

            // Hide the pick option in the UI to prevent users from starting the picker
            // multiple times.

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if(!position.equals(null)){
            mMap.addMarker(new MarkerOptions().position(position).title(String.valueOf(place.getAddress())));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
        }else{

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                position = place.getLatLng();
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                presenter.inicializaMapa();
            }
        }
    }
    @Override
    public void iniciarMapa(){
        //Inicializamos el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public String geolocalaiza(String direccion){
        //Geolocalizacion nos devuelve latituid y longitud a partir de una direccion
        final String[] result = new String [1];
        Locale espLocale = new Locale("es");
        LatLng latLng;
            Geocoder geocoder = new Geocoder(getApplicationContext(), espLocale);
            result[0] = null;
            try {
                List addressList = geocoder.getFromLocationName(direccion, 1);

                if (addressList != null && addressList.size() > 0) {
                    Address address1 = (Address) addressList.get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append(address1.getLatitude()).append("\n");
                    sb.append(address1.getLongitude());
                    result[0] = sb.toString();
                } else {
                    result[0] = "No calle";
                }
            } catch (IOException e) {
                Log.e("GeoLocalitation", "Unable to connect to Geocoder", e);
            }

    return result[0];
    }

}

