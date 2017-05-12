package win.aladhims.meetme;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
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
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;
import win.aladhims.meetme.Model.Chat;
import win.aladhims.meetme.Model.User;
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
    private StorageReference fotoChatRef;
    private ValueEventListener finishedListener;

    @BindView(R.id.btn_chat_send)
    Button mBtnSendChat;
    @BindView(R.id.et_chat_message)
    EditText mEtChatMessage;
    @BindView(R.id.rv_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.iv_chat_pick_photo)
    ImageView mIvPickPhotoChat;

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

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(request);

        meetRef = rootRef.child("meet").child(meetID);
        chatRef = meetRef.child("chat");

        LinearLayoutManager lm = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lm.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(lm);

        Toolbar directToolbar = (Toolbar) findViewById(R.id.meet_toolbar);
        setSupportActionBar(directToolbar);
        directToolbar.setTitleTextColor(Color.WHITE);
        final CircleImageView ciTeman = (CircleImageView) findViewById(R.id.ci_toolbar_meet);
        final TextView tvTeman = (TextView) findViewById(R.id.tv_toolbar_meet);

        rootRef.child("users").child(friendID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null){
                    User friend = dataSnapshot.getValue(User.class);
                    Glide.with(getApplicationContext())
                            .load(friend.getPhotoURL())
                            .into(ciTeman);

                    String[] firstName = friend.getName().split(" ");
                    tvTeman.setText(firstName[0]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //the adapter for the chat between them
        mChatAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class,R.layout.chat_item,ChatViewHolder.class,chatRef) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final Chat model, int position) {
                if(model.getFromUid().equals(myUid)){
                    viewHolder.mTvSenderName.setVisibility(View.GONE);
                    viewHolder.mCiPhotoUserChat.setVisibility(View.GONE);
                    viewHolder.mMessageBody.setGravity(Gravity.END);
                    if(!(model.getPesan()==null)){
                        viewHolder.mIvFotoPesan.setVisibility(View.GONE);
                        viewHolder.mTvMessage.setText(model.getPesan());
                    }else{
                        viewHolder.mTvMessage.setVisibility(View.GONE);
                        Glide.with(getParent())
                                .load(model.getFotoPesanURL())
                                .into(viewHolder.mIvFotoPesan);
                    }
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
                    if(!(model.getPesan()== null)) {
                        viewHolder.mIvFotoPesan.setVisibility(View.GONE);
                        viewHolder.mTvMessage.setText(model.getPesan());
                    }else{
                        viewHolder.mTvMessage.setVisibility(View.GONE);
                        Glide.with(getApplicationContext())
                                .load(model.getFotoPesanURL())
                                .into(viewHolder.mIvFotoPesan);

                    }
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

        fotoChatRef = FirebaseStorage.getInstance().getReference().child("chat").child(meetID).child(myUid);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_RC && resultCode == RESULT_OK){
            Uri uri = data.getData();

            fotoChatRef.putFile(uri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String downloadURL = taskSnapshot.getDownloadUrl().toString();

                            Chat chat = new Chat(myUid,friendID,null,downloadURL);
                            chatRef.push().setValue(chat);
                        }
                    });
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
        //make a new cool style for the map
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.mapraw));
        }catch (Resources.NotFoundException e){
            Log.e(TAG,"error parsing raw");
        }
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
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),CAMERA_RC);
        }
    }
}
