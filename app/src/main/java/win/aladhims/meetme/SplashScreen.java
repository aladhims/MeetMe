package win.aladhims.meetme;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class SplashScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(LocationServices.API)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {


                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            final LocationSettingsStates state = result.getLocationSettingsStates();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied. The client can initialize location
                                    // requests here.
                                    if(state.isGpsUsable() && state.isLocationUsable() && state.isNetworkLocationUsable()){
                                        googleApiClient.disconnect();
                                        startActivity(new Intent(getApplicationContext(),ListFriendActivity.class));
                                        finish();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied. But could be fixed by showing the user
                                    // a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        status.startResolutionForResult(
                                                SplashScreen.this, 1000);
                                    } catch (IntentSender.SendIntentException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    break;
                            }
                        }
                    });
                }

            },1000);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode == RESULT_OK){

            LocationSettingsStates settingsStates = LocationSettingsStates.fromIntent(data);
            if(settingsStates.isNetworkLocationUsable() && settingsStates.isGpsUsable() && settingsStates.isLocationUsable()){
                googleApiClient.disconnect();
                startActivity(new Intent(this,ListFriendActivity.class));
                finish();
            }
        }else{
            Toast.makeText(this,"GPS dibutuhkan untuk menjalankan aplikasi ini",Toast.LENGTH_LONG).show();
            finish();
        }
    }
}