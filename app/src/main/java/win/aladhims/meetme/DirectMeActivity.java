package win.aladhims.meetme;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;
import win.aladhims.meetme.Model.Chat;
import win.aladhims.meetme.Model.User;
import win.aladhims.meetme.Utility.ImageUtils;
import win.aladhims.meetme.Utility.PolylineUtils;
import win.aladhims.meetme.ViewHolder.ChatViewHolder;

public class DirectMeActivity extends BaseActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest request;

    private final static String TAG = DirectMeActivity.class.getSimpleName();
    private final static int CAMERA_RC = 0;
    String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET};

    private FirebaseRecyclerAdapter<Chat,ChatViewHolder> mChatAdapter;
    private DatabaseReference rootRef,meetRef,chatRef;
    private StorageReference rootStorageRef,fotoChatRef;
    private ValueEventListener finishedListener;

    @BindView(R.id.btn_chat_send)
    Button mBtnSendChat;
    @BindView(R.id.et_chat_message)
    EditText mEtChatMessage;
    @BindView(R.id.rv_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.iv_chat_pick_photo)
    ImageView mIvPickPhotoChat;
    @BindView(R.id.ci_toolbar_meet) CircleImageView ciTeman;

    private Polyline mCurPolyLine;
    private MarkerOptions myMarkerOptions = new MarkerOptions(),friendMarkerOptions = new MarkerOptions();
    private String myUid,friendPhotoURL,myPhotoURL,namaTeman,namaKu;
    private Marker myMarker,friendMarker;
    private boolean hasToMakeBound = true;
    private Location lastLoc;
    private LatLng friendLoc;
    private Uri uri;

    private String friendID,meetID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_direct_me);
        rootRef = FirebaseDatabase.getInstance().getReference();
        rootStorageRef = FirebaseStorage.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ButterKnife.bind(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //get intent's data
        Intent i = getIntent();
        friendID = i.getStringExtra(ListFriendActivity.FRIENDUID);
        meetID = i.getStringExtra(ListFriendActivity.MEETID);
        if(i.hasExtra(NotifyMeService.AGREEEXTRA)){
            Map<String, Object> map = new HashMap<>();
            map.put("/invite/"+ myUid + "/agree/",true);
            NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(NotifyMeService.NOTIFYID);

            rootRef.updateChildren(map);
        }
        createLocationReq();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(request);
        checkLocationSet(builder);
        meetRef = rootRef.child("meet").child(meetID);
        chatRef = meetRef.child("chat");

        LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lm.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(lm);

        Toolbar directToolbar = (Toolbar) findViewById(R.id.meet_toolbar);
        setSupportActionBar(directToolbar);
        directToolbar.setTitleTextColor(Color.WHITE);
        final TextView tvTeman = (TextView) findViewById(R.id.tv_toolbar_meet);

        rootRef.child("users").child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null){
                    User friend = dataSnapshot.getValue(User.class);
                    friendPhotoURL = friend.getPhotoURL();
                    Glide.with(getApplicationContext())
                            .load(friendPhotoURL)
                            .into(ciTeman);

                    String[] firstName = friend.getName().split(" ");
                    namaTeman = firstName[0];
                    tvTeman.setText(namaTeman);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rootRef.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    User me = dataSnapshot.getValue(User.class);
                    myPhotoURL = me.getPhotoURL();
                    String[] firstName = me.getName().split(" ");
                    namaKu = firstName[0];
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final RequestListener<String,GlideDrawable> listener = new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                Log.d(TAG,e.getMessage());
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        };
        //the adapter for the chat between them
        mChatAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class,R.layout.chat_item,ChatViewHolder.class,chatRef) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final Chat model, int position) {
                if(!(model.getPesan()== null)) {
                    viewHolder.mIvFotoPesan.setVisibility(View.GONE);
                    viewHolder.mTvMessage.setText(model.getPesan());
                }else{
                    Log.d(TAG,model.getFotoPesanURL());
                    StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(model.getFotoPesanURL());
                    viewHolder.mTvMessage.setVisibility(View.GONE);
                    viewHolder.mIvFotoPesan.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext())
                            .load(model.getFotoPesanURL())
                            .listener(listener)
                            .error(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_black_person_add))
                            .into(viewHolder.mIvFotoPesan);
                }
                if(model.getFromUid().equals(myUid)){
                    viewHolder.mTvSenderName.setVisibility(View.GONE);
                    viewHolder.mCiPhotoUserChat.setVisibility(View.GONE);
                    viewHolder.mMessageBody.setGravity(Gravity.END);
                }else{
                    rootRef.child("users").child(model.getFromUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Glide.with(getApplicationContext())
                                    .load(user.getPhotoURL())
                                    .into(viewHolder.mCiPhotoUserChat);
                            viewHolder.mTvSenderName.setText(user.getName());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        };

        mChatRecyclerView.setAdapter(mChatAdapter);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtnSendChat.setOnClickListener(this);
        mIvPickPhotoChat.setOnClickListener(this);

        finishedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    stopLocationUpdate();
                    closeDirect();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        fotoChatRef = rootStorageRef.child("chat").child(meetID).child(myUid);

    }

    private void checkLocationSet(LocationSettingsRequest.Builder builder){
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
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
                            return;
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    DirectMeActivity.this, 1000);
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

    private synchronized void closeDirect(){
        Toast.makeText(this, "berhenti bertemu", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void requestPerms(){
        if(EasyPermissions.hasPermissions(this, perms)){
            startLocationUpdate();
        } else {
            EasyPermissions.requestPermissions(this, "minta lokasinya!",101,perms);
        }
    }

    private void createLocationReq(){
        request = new LocationRequest();
        request.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdate(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,request,this);
        lastLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(lastLoc != null) {
            Map<String, Object> upFirstLoc = new HashMap<>();
            upFirstLoc.put("LAT", lastLoc.getLatitude());
            upFirstLoc.put("LONG", lastLoc.getLongitude());

            meetRef.child(myUid).updateChildren(upFirstLoc);
        }

    }

    private void uploadFotoChat(final Uri u,Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        fotoChatRef.child(u.getLastPathSegment()).putBytes(bytes)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String downloadURL = taskSnapshot.getDownloadUrl().toString();


                        Chat chat = new Chat(myUid,friendID,null,downloadURL);
                        chatRef.push().setValue(chat);
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(uri != null){
            outState.putString("cameraImageUri",uri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.containsKey("cameraImageUri")){
            uri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_RC && resultCode == RESULT_OK){
            uri = data.getData();
            Bitmap bmp = ImageUtils.compressImage(uri,this);
            uploadFotoChat(uri,bmp);
        }else if(requestCode == 1000 && resultCode == RESULT_OK){
            LocationSettingsStates settingsStates = LocationSettingsStates.fromIntent(data);
            if(settingsStates.isNetworkLocationUsable() && settingsStates.isGpsUsable() && settingsStates.isLocationUsable()){
                return;
            }
        }else{
            meetRef.removeValue();
            closeDirect();
        }
    }

    private void stopLocationUpdate(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this,"untuk mengakhiri pertemuan sentuh ikon silang",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG,"Map is Ready");
        //make a new cool style for the map
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapraw));
        }catch (Resources.NotFoundException e){
            Log.e(TAG,"error parsing raw");
        }
        mMap.setIndoorEnabled(true);
        myMarkerOptions.draggable(false);
        myMarkerOptions.title(namaKu);
        myMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        friendMarkerOptions.draggable(false);
        friendMarkerOptions.position(new LatLng(-6.3660756,106.8346144));
        friendMarkerOptions.title(namaTeman);
        friendMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        myMarkerOptions.position(new LatLng(-6.23233,104.23245));
        if(lastLoc != null){
            myMarkerOptions.position(new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude()));
        }
        myMarker = mMap.addMarker(myMarkerOptions);
        friendMarker = mMap.addMarker(friendMarkerOptions);

        /*listen for location change on the other side (friend location) and
        assign it to friendLoc variable and move friend marker to the new location
         */
        meetRef.child(friendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    double Lat = (Double) dataSnapshot.child("LAT").getValue();
                    double Long = (Double) dataSnapshot.child("LONG").getValue();
                    friendLoc = new LatLng(Lat, Long);
                    friendMarker.setPosition(new LatLng(Lat, Long));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestPerms();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Every location change make a new request to Google Direction for new Polyline direction and update the last location for the user
        Map<String, Object> locChange = new HashMap<>();
        locChange.put("/meet/"+meetID+"/"+myUid+"/LAT/",location.getLatitude());
        locChange.put("/meet/"+meetID+"/"+myUid+"/LONG/",location.getLongitude());
        locChange.put("/users/"+myUid+"/LAT/",location.getLatitude());
        locChange.put("/users/"+myUid+"/LONG/",location.getLongitude());

        rootRef.updateChildren(locChange);
        LatLng newLoc = new LatLng(location.getLatitude(),location.getLongitude());
        myMarker.setPosition(newLoc);
        //and also we check for friend location every our location change if the friend location if also change or not null
        if(friendLoc != null) {
            String newReq = PolylineUtils.requestJSONDirection(newLoc,friendLoc);
            PolylineUtils.getResponse(this, newReq, new PolylineUtils.VolleyCallback() {
                @Override
                public void onSuccess(String string) {
                    if (mCurPolyLine != null) {
                        if (mCurPolyLine.getPoints().size() != 0) {
                            mCurPolyLine.remove();
                        }
                        mCurPolyLine.setGeodesic(true);
                    }
                    ArrayList<LatLng> locForPoly = PolylineUtils.decodePoly(PolylineUtils.getStringPolyline(string));
                    PolylineOptions options = new PolylineOptions();
                    options.addAll(locForPoly);
                    mCurPolyLine = mMap.addPolyline(options);
                    if (hasToMakeBound) {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (int i = 0; i < mCurPolyLine.getPoints().size(); i++) {
                            builder.include(mCurPolyLine.getPoints().get(i));
                        }

                        LatLngBounds bounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                        hasToMakeBound = false;
                    }

                }
            });
        }
        meetRef.addValueEventListener(finishedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.direct_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.end_direct:
                meetRef.removeValue();
                closeDirect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_chat_send:
                String chatKey = chatRef.push().getKey();
                Chat chat = new Chat(myUid,friendID,mEtChatMessage.getText().toString(),null);
                chatRef.child(chatKey).setValue(chat);
                mEtChatMessage.setText("");
                break;
            case R.id.iv_chat_pick_photo:
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager())!= null) {
                    startActivityForResult(i, CAMERA_RC);
                }
        }
    }
}
