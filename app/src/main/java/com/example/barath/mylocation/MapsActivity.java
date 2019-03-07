package com.example.barath.mylocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 222;
    private static final int REQUEST_CHECK_SETTINGS = 444;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    showLocationOnMap(location.getLatitude(),location.getLongitude());
                }
            }
        };
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            getLocation();
        }

    }

    private void getLocation() {
        locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = GetCurrentLocationSettings(locationRequest);
        getLocationSettingHandeler(builder);
        Task<LocationSettingsResponse> task = getLocationSettingHandeler(builder);

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        showLocationOnMap(location.getLatitude(),location.getLongitude());
                                    }
                                }
                            });
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                }

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (isLocationSettingsNotSatisfied(e)) {

                    try {
                        showFixSettingDialog((ResolvableApiException) e);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void checkPermission() {
        if (isPermissionNotGranted()) {
            if (doShowUserExplanation()) {
                showExplenation();
            } else {
                requestPermission();
            }

        } else {
            getLocation();
        }
    }



    private void showFixSettingDialog(@NonNull ResolvableApiException e) throws IntentSender.SendIntentException {
        ResolvableApiException resolvable = e;
        resolvable.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
    }

    private boolean isLocationSettingsNotSatisfied(@NonNull Exception e) {
        return e instanceof ResolvableApiException;
    }

    private Task<LocationSettingsResponse> getLocationSettingHandeler(LocationSettingsRequest.Builder builder) {
        SettingsClient client = LocationServices.getSettingsClient(this);
        return client.checkLocationSettings(builder.build());
    }

    private LocationSettingsRequest.Builder  GetCurrentLocationSettings(LocationRequest locationRequest) {
        return new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest=LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private boolean isPermissionNotGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean doShowUserExplanation(){
        return ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION:
                if(isRequestedPermissionGranted(grantResults)){
                    getLocation();
                }
                else if(!isRequestedPermissionGranted(grantResults) && !doShowUserExplanation()) {

                    Toast.makeText(this,"give location permission in settings",Toast.LENGTH_LONG).show();
                }else {
                    showExplenation();
                }
                return;


        }

    }

    private void showExplenation() {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(false);
        alertBuilder.setMessage("Location permission is needed for show your location");
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int which) {
                requestPermission();
            }
        }).show();
    }

    private boolean isRequestedPermissionGranted(int[] grantResults){
        return grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }


    private void showLocationOnMap(Double lat,Double lon) {
        mMap.clear();

            LatLng present = new LatLng(lat, lon);

            mMap.addMarker(new MarkerOptions().position(present).title("MY location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(present,15.6f));

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }

    public void fabClick(View view){
        getLocation();
    }
}
