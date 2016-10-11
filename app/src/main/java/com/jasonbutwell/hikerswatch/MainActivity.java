package com.jasonbutwell.hikerswatch;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {

    private LocationManager locationManager;
    private String provider;

    private Double lat;
    private Double lon;
    private Double alt;
    private Float bearing;
    private Float speed;
    private Float accuracy;

    private String streetAddress;

    private Geocoder geocoder;

    private List<Address> listAddresses;

    private boolean locationFound;

    // Get the location info
    private void getLocationInfo(Location location) {

        // grab all the info we want from the location
        lat = location.getLatitude();
        lon = location.getLongitude();

        alt = location.getAltitude();
        accuracy = location.getAccuracy();

        speed = location.getSpeed();
        bearing = location.getBearing();
    }

    private void setViewText( int id, String text ) {
        TextView tv = (TextView) findViewById( id );

        if ( tv != null )
            tv.setText( text );
    }

    // update the UI to reflect the data we have obtained from the location and the address data
    private void updateTextViews() {

        int ids[] = { R.id.latView, R.id.lngView, R.id.altView, R.id.accuracyView, R.id.speedView, R.id.bearingView, R.id.addressView };
        String labels[] = { "Latitude: ", "Longitude: ", "Altitude: ", "Accuracy: ", "Speed: ", "Bearing: ", "Address: \r\n\r\n" };

        setViewText(ids[0], labels[0] + lat.toString());
        setViewText(ids[1], labels[1] + lon.toString());

        setViewText(ids[2], labels[2] + alt.toString());
        setViewText(ids[3], labels[3] + accuracy.toString()+ "m");

        setViewText(ids[4], labels[4] + speed.toString());
        setViewText(ids[5], labels[5] + bearing.toString());

        setViewText(ids[6], labels[6] + streetAddress.toString());
    }

    // obtain our street address from our (lat, lng) location
    private void getAddress( Geocoder geocoder ) {

        try {
            // List of Addresses. We store the addresses we find at that lat long location in this list.
            // We only want 1 for now.
            listAddresses = geocoder.getFromLocation(lat, lon, 1);

            // Ensure we actually do have some locations in our list to display
            if ( listAddresses != null && listAddresses.size() > 0 ) {
                // store the street address

                //Log.i("PlaceInfo", listAddresses.get(0).toString());

                // clear the address string for when the location changes
                streetAddress = "";

                // obtain the fields of the address and concatenate together into one string
                for (int i = 0; i < listAddresses.get(0).getMaxAddressLineIndex(); i++ ) {
                    streetAddress += listAddresses.get(0).getAddressLine(i)+"\r\n";
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Send output to the log
    private void logInfo() {
        // Output the info we have obtained from the location to the console for now
        Log.i("Location", "LAT/LON:"+lat.toString()+","+lon.toString());

        Log.i("Latitude", String.valueOf(lat));
        Log.i("Longitude", String.valueOf(lon));
        Log.i("Altitude", String.valueOf(alt));
        Log.i("Bearing", String.valueOf(bearing));
        Log.i("Speed", String.valueOf(speed));
        Log.i("Accuracy", String.valueOf(accuracy));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        locationFound = false;

//        ImageView iv = (ImageView) findViewById(R.id.imageView);
//        iv.setAlpha(0.25f);

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

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

        if (location != null) {
            locationFound = true;
            //Log.i( "Location", "Location Achieved" );

            // call to onLocationChanged in first instance of execution from onCreate() method.
            if ( locationFound )
                onLocationChanged(location);
        } else {
            locationFound = false;
            //Log.i( "Location", "No Location" );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        locationManager.removeUpdates(this);
    }

    // when app reactives from coming to the foreground from the background
    @Override
    protected void onResume() {
        super.onResume();

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

        // 400 = 400 milli seconds update window
        // 1 = meter distance accuracy

        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        // Only update if we got the last known location

            // Store the location info we want
            getLocationInfo(location);

            // We need a geocoder to resolve the lat / long to a street address
            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            // obtain the address and store it
            getAddress(geocoder);

            // update the UI textview elements
            updateTextViews();

            // log the info to the logCat
            //logInfo();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("Location","Provider is enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("Location","Provider is disabled!");
    }
}
