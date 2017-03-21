package win.aladhims.meetme;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import win.aladhims.meetme.Model.Chat;
import win.aladhims.meetme.Model.User;
import win.aladhims.meetme.ViewHolder.ChatViewHolder;

public class DirectMeActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest request;
    private DatabaseReference rootRef,meetRef,chatRef;
    private final static String TAG = DirectMeActivity.class.getSimpleName();
    String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET};

    private FirebaseRecyclerAdapter<Chat,ChatViewHolder> mChatAdapter;

    @BindView(R.id.btn_chat_send)
    Button mBtnSendChat;
    @BindView(R.id.et_chat_message)
    EditText mEtChatMessage;
    @BindView(R.id.rv_chat)
    RecyclerView mChatRecyclerView;

    private Polyline mCurPolyLine;
    private MarkerOptions myMarkerOptions,friendMarkerOptions;
    private String myUid;
    private Marker myMarker,friendMarker;
    private boolean hasToMakeBound = true;
    private Location lastLoc;
    private LatLng friendLoc;

    private String friendID,meetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_me);
        rootRef = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationReq();

        Intent i = getIntent();

        friendID = i.getStringExtra(ListFriendActivity.FRIENDUID);
        meetID = i.getStringExtra(ListFriendActivity.MEETID);
        if(i.hasExtra(NotifyMeService.AGREEEXTRA)){
            Map<String, Object> map = new HashMap<>();
            map.put("/invite/"+ myUid + "/agree/",true);

            rootRef.updateChildren(map);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(request);



        meetRef = rootRef.child("meet").child(meetID);
        chatRef = meetRef.child("chat");

        LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lm.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(lm);

        mChatAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class,R.layout.chat_item,ChatViewHolder.class,chatRef) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final Chat model, int position) {
                if(model.getFromUid().equals(myUid)){
                    viewHolder.mTvSenderName.setVisibility(View.GONE);
                    viewHolder.mCiPhotoUserChat.setVisibility(View.GONE);
                    viewHolder.mMessageBody.setGravity(Gravity.END);
                    viewHolder.mTvMessage.setText(model.getPesan());
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
                    viewHolder.mTvMessage.setText(model.getPesan());
                }
            }
        };

        mChatRecyclerView.setAdapter(mChatAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mBtnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String chatKey = chatRef.push().getKey();
                Chat chat = new Chat(myUid,friendID,mEtChatMessage.getText().toString());
                chatRef.child(chatKey).setValue(chat);
                mEtChatMessage.setText("");
            }
        });

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
        mMap.setIndoorEnabled(true);
        myMarkerOptions = new MarkerOptions();
        myMarkerOptions.draggable(false);
        friendMarkerOptions = new MarkerOptions();
        friendMarkerOptions.draggable(false);
        friendMarkerOptions.position(new LatLng(-6.3660756,106.8346144));
        myMarkerOptions.position(new LatLng(-6.23233,104.23245));
        if(lastLoc != null){
            myMarkerOptions.position(new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude()));
        }



        myMarker = mMap.addMarker(myMarkerOptions);
        friendMarker = mMap.addMarker(friendMarkerOptions);

        meetRef.child(friendID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double Lat = (Double) dataSnapshot.child("LAT").getValue();
                double Long = (Double) dataSnapshot.child("LONG").getValue();
                friendLoc = new LatLng(Lat,Long);
                friendMarker.setPosition(new LatLng(Lat,Long));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestPerms();
        Toast.makeText(this, "Connected to google", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Map<String, Object> locChange = new HashMap<>();
        locChange.put("LAT",location.getLatitude());
        locChange.put("LONG",location.getLongitude());

        meetRef.child(myUid).updateChildren(locChange);
        Toast.makeText(this, location.toString(), Toast.LENGTH_SHORT).show();

        LatLng newLoc = new LatLng(location.getLatitude(),location.getLongitude());
        myMarker.setPosition(newLoc);
        if(friendLoc != null) {
            String newReq = Utility.requestJSONDirection(newLoc,friendLoc);
            Utility.getResponse(this, newReq, new Utility.VolleyCallback() {
                @Override
                public void onSuccess(String string) {
                    if (mCurPolyLine != null) {
                        if (mCurPolyLine.getPoints().size() != 0) {
                            mCurPolyLine.remove();
                        }
                    }
                    ArrayList<LatLng> locForPoly = Utility.decodePoly(Utility.getStringPolyline(string));
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
    }
}
