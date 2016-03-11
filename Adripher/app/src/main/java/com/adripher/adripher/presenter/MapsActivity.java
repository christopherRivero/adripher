package com.adripher.adripher.presenter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.location.Location;

import android.location.LocationManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adripher.adripher.R;
import com.adripher.adripher.adpater.PlaceAutocompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsActivityView,GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    LatLng position;
    Place place;
    IMapsActivityPresenterImp presenter;
    Button btnVerSitios;
    ImageView imgBorrarTexto;
    Bitmap bitmapFromURL;
    /**
     * GoogleApiClient wraps our service connection to Google Play Services and provides access
     * to the user's sign in state as well as the Google's APIs.
     */
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView mAutocompleteView;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        presenter = new MapsActivityPresenter(this, this);
        btnVerSitios = (Button) findViewById(R.id.btn_sitios);
        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        mAutocompleteView = (AutoCompleteTextView) findViewById(R.id.et_buscar);
        imgBorrarTexto = (ImageView) findViewById(R.id.img_borrar_texto);
        btnVerSitios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.irSitios();
            }
        });
        imgBorrarTexto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutocompleteView.setText("");
            }
        });
        presenter.irSitios();
        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        mAutocompleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutocompleteView.setText("");
            }
        });

        mAutocompleteView.setSelectAllOnFocus(true);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY, null);
        mAutocompleteView.setAdapter(mAdapter);
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
        if (!position.equals(null)) {
            String s = place.getName().toString();

            AsynkTaskCustomizeIcon asynkTaskCustomizeIcon = new AsynkTaskCustomizeIcon();
            asynkTaskCustomizeIcon.execute();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
            mMap.setInfoWindowAdapter(new MarcadorInformacionAdapter());
        } else {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            String locationProvider = LocationManager.NETWORK_PROVIDER;
            // Or use LocationManager.GPS_PROVIDER

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
            mMap.setInfoWindowAdapter(new MarcadorInformacionAdapter());
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(data, this);
                position = place.getLatLng();
                String toastMsg = String.format("Place: %s", place.getName());
                mAutocompleteView.setText(place.getAddress().toString());
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

    @Override
    public void irSitios() {
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setMessage("¿Desea salir de la aplicación?");
        alertDialog.setTitle("Adripher");
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // **** No hacemos nada, lo dejamos en la aplicaci�n
            }
        });
        alertDialog.show();
    }
    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i("TAG", "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i("TAG", "Called getPlaceById to get Place details for " + placeId);
        }
    };
    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("TAG", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            LatLng latLng = place.getLatLng();
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            mMap.setInfoWindowAdapter(new MarcadorInformacionAdapter());
            Log.i("TAg", "Place details received: " + place.getName());

            places.release();
        }
    };


    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("TAG", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public class MarcadorInformacionAdapter implements GoogleMap.InfoWindowAdapter {

        public MarcadorInformacionAdapter() {

        }

        public View getInfoWindow(Marker marker) {
            return null;
        }

        public View getInfoContents(Marker marker) {

            View v = getLayoutInflater().inflate(R.layout.ventana_informacion_mapa, null);
            //Declaramos los TextView de la ventana del piker
            TextView tvAtributions = (TextView) v.findViewById(R.id.tv_atributions);
            TextView tvAddress = (TextView) v.findViewById(R.id.tv_addres);
            TextView tvPhone = (TextView) v.findViewById(R.id.tv_phone);
            TextView tvUrl = (TextView) v.findViewById(R.id.tv_url);
            TextView tvName = (TextView) v.findViewById(R.id.tv_name);
            //Proporcionamos valor a los textView
            //Metemos la condición de que si el valor viene vacío, el texto sea "Sin asignar"
            //Titulo
            if(place.getAttributions()== null){
                tvName.setText(R.string.sin_asignar);
            }else{
                tvName.setText(place.getName());
            }
            if(place.getAttributions()== null){
                tvAtributions.setText(R.string.sin_asignar);
            }else{
                tvAtributions.setText(place.getAttributions());
            }
            //condición para la dirección
            if(place.getAddress()== null){
                tvAddress.setText(R.string.sin_asignar);
            }else{
                tvAddress.setText(place.getAddress());
            }
            //Condición para el telefono
            if(place.getPhoneNumber()== ""){
                tvPhone.setText(R.string.sin_asignar);
            }else{
                tvPhone.setText(place.getPhoneNumber());
            }
            //Condición para la url
            //SetOnClickLiestener a la url abrir navegador
            if(place.getWebsiteUri() == null){
                tvUrl.setText(R.string.sin_asignar);
            }else{
                SpannableString content = new SpannableString(place.getWebsiteUri().toString());
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                tvUrl.setText(content);
            }

            return v;
        }
    }
    public static Bitmap getBitmapFromURL(URL src) {
        try {
            URL url = src;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Asynktask que
     */
    public class AsynkTaskCustomizeIcon extends AsyncTask<Void, Integer, String> {
        ProgressDialog progDailog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if(place.getWebsiteUri() != null){
                    URL url = new URL(place.getWebsiteUri().toString()+"favicon.ico");
                    bitmapFromURL = getBitmapFromURL(url);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(bitmapFromURL == null){
                MarkerOptions markerOptions = new MarkerOptions().position(position);
                mMap.addMarker(markerOptions);
            }else{
                Bitmap resizedBitmap = getResizedBitmap(bitmapFromURL, 32);
                MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));
                mMap.addMarker(markerOptions);
            }
        }
    }

    /**
     * Para cambiar de tamaño las imagenes
     * @param image
     * @param maxSize
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}

