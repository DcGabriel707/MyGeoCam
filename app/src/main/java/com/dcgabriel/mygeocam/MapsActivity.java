package com.dcgabriel.mygeocam;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private static final String TAG = "MapsActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private boolean mLocationPermissionGranted = false;
    private String currentPhotoPath;
    private double currentlatitude;
    private double currentlongitude;
    private GoogleMap mMap;
    private ArrayList<Pic> photoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocation();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        photoList = new ArrayList<>();
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
        getLocation();
        LatLng newLocation = new LatLng(currentlatitude, currentlongitude);
        //mMap.addMarker(new MarkerOptions().position(newLocation).title("Here"));

        updateMap(newLocation);
    }

    //gets the location
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (mLocationPermissionGranted) {
            if (locationManager != null) {
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (gpsLocation != null) {
                    currentlatitude = gpsLocation.getLatitude();
                    currentlongitude = gpsLocation.getLongitude();
                } else if (netLocation != null) {
                    currentlatitude = netLocation.getLatitude();
                    currentlongitude = netLocation.getLongitude();
                }
            }
        }
    }

    //updates the map according to the location
    private void updateMap(LatLng location) {
        getPhotoList();
        populateMarkers();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 17);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(cameraUpdate);
    }

    //takes a photo
    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "takePhoto: failed to create file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.dcgabriel.mygeocam",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                mMap.clear();
                getPhotoList();
                populateMarkers();
            }
        }
    }

    //creates image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "[" + currentlatitude + "=" + currentlongitude + "]";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //gets the list of all the photos in the app's private picture directory
    private void getPhotoList() {
        File photosDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "getPhotoList: " + photosDir.getAbsolutePath());
        File[] files = photosDir.listFiles();
        for (File file : files) {
            String fileName = file.getName();
            Log.d(TAG, "getPhotoList: filename=" + fileName);
            String timestamp = fileName.substring(5, fileName.indexOf("["));
            double latitude = Double.valueOf(fileName.substring(fileName.indexOf("[") + 1, fileName.indexOf("=")));
            double longitude = Double.valueOf(fileName.substring(fileName.indexOf("=") + 1, fileName.indexOf("]")));

            Log.d(TAG, "getPhotoList: title=" + fileName + "longitude=" + longitude + " latitide=" + latitude + "timestamp=" + timestamp);
            Pic pic = new Pic(fileName, latitude, longitude, timestamp);
            photoList.add(pic);
        }

    }

    //populates the map with markers
    private void populateMarkers() {
        CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(customInfoWindowAdapter);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.showInfoWindow();
            }
        });
        for (Pic pic : photoList) {
            LatLng location = new LatLng(pic.getLatitude(), pic.getLongitude());
            Marker marker = mMap.addMarker((new MarkerOptions().position(location).title(pic.getTitle())));
            marker.setTag(pic);

            Log.d(TAG, "populateMarkers: title=" + pic.getTitle());
        }

    }

    //updates the location and the map
    public void refreshLocation(View view) {
        getLocation();
        LatLng newLocation = new LatLng(currentlatitude, currentlongitude);
        updateMap(newLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        updateMap(newLocation);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
