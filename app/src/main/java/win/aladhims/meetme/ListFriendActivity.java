package win.aladhims.meetme;

import android.*;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import win.aladhims.meetme.Model.User;
import win.aladhims.meetme.Utility.PlacesUtils;
import win.aladhims.meetme.Utility.PolylineUtils;
import win.aladhims.meetme.ViewHolder.FriendViewHolder;

public class ListFriendActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    //final fields
    private static final String TAG = ListFriendActivity.class.getSimpleName();
    public static final String MYUIDEXTRAINTENT = "MYUID";
    public static final String FRIENDUID = "FRIENDUID";
    public static final String MEETID = "MEETID";
    String[] perms = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET};

    //Firebase Fields
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference rootRef,meetRef;
    private Query friendListRef;
    private FirebaseRecyclerAdapter<User,FriendViewHolder> mAdapter;

    private GoogleApiClient mGoogleApiClient;

    private SearchView searchView;

    @BindView(R.id.rv_friend_list) RecyclerView mRecyclerView;
    @BindView(R.id.pg_friend_list) ProgressBar mProgressBar;
    @BindView(R.id.friend_toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        setTitle("List Friend");
        toolbar.setTitleTextColor(Color.WHITE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser == null){
            startActivity(new Intent(this,SignInActivity.class));
            finish();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        rootRef = FirebaseDatabase.getInstance().getReference();
        friendListRef = rootRef.child("users");
        meetRef = rootRef.child("meet");

        mAdapter = new Adapter(User.class,R.layout.friend_item,FriendViewHolder.class,friendListRef);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mAdapter);

        if(!EasyPermissions.hasPermissions(this, perms)){
            EasyPermissions.requestPermissions(this, "minta lokasinya!",101,perms);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this,NotifyMeService.class);
        intent.putExtra(MYUIDEXTRAINTENT,mUser.getUid());
        startService(intent);
        Intent i = getIntent();
        if(Intent.ACTION_SEARCH.equals(i.getAction())){
            String query = i.getStringExtra(SearchManager.QUERY);
            doQuery(query);
        }
    }

    @Override
    public void onBackPressed() {
        if(!searchView.isIconified()){
            searchView.setIconified(true);
        }else {
            super.onBackPressed();
        }
    }

    private void doQuery(String query){
        Query q = friendListRef.orderByChild("email").equalTo(query);
        Log.d(TAG,q.toString());
        mProgressBar.setVisibility(View.VISIBLE);
        FirebaseRecyclerAdapter<User,FriendViewHolder> newAdapter = new Adapter(User.class,R.layout.friend_item,FriendViewHolder.class,q);
        if(newAdapter.getItemCount() >= 0) {
            mRecyclerView.swapAdapter(newAdapter, true);
        }else{
            mProgressBar.setVisibility(View.GONE);
            //TODO bikin textview buat nampilin kalo yg dicari gaada!
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_friend_menu,menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getApplicationContext().getSystemService(SEARCH_SERVICE);

        if(searchItem != null){
            searchView = (SearchView) searchItem.getActionView();
        }
        if(searchView != null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setQueryHint("Email Teman");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.startLayoutAnimation();
        ImageView closeButton = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery("",false);
                if(!mRecyclerView.getAdapter().equals(mAdapter)){
                    mRecyclerView.swapAdapter(mAdapter,true);
                }
                searchView.clearFocus();
                searchView.setIconified(true);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.signOut:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mAuth.signOut();
                startActivity(new Intent(this,SignInActivity.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class Adapter extends FirebaseRecyclerAdapter<User,FriendViewHolder>{

        public Adapter(Class<User> modelClass, int modelLayout, Class<FriendViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(final FriendViewHolder holder, final User user, int position) {
            mProgressBar.setVisibility(View.GONE);
            final String uid = getRef(position).getKey();
            if(uid.equals(mUser.getUid())){
                holder.itemView.setVisibility(View.GONE);
                holder.mLLFriend.setVisibility(View.GONE);
                holder.mCiFriendPhoto.setVisibility(View.GONE);
                holder.mTvFriendName.setVisibility(View.GONE);
                holder.mBtnMeetFriend.setVisibility(View.GONE);
                holder.mTvLastLoc.setVisibility(View.GONE);
            }else {
                Glide.with(getApplicationContext())
                        .load(user.getPhotoURL())
                        .into(holder.mCiFriendPhoto);
                String[] name = user.getName().split(" ");
                holder.mTvFriendName.setText(name[0]);
                rootRef.child("users").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("LAT").getValue()!=null&&dataSnapshot.child("LONG").getValue()!=null){
                            double latitude = (Double) dataSnapshot.child("LAT").getValue();
                            double longitude = (Double) dataSnapshot.child("LONG").getValue();
                            LatLng latLng = new LatLng(latitude,longitude);
                            String request = PlacesUtils.requestPlace(latLng);
                            PolylineUtils.getResponse(getApplicationContext(), request, new PolylineUtils.VolleyCallback() {
                                @Override
                                public void onSuccess(String string) {
                                    String place = PlacesUtils.getPlaces(string);
                                    holder.mTvLastLoc.setText(place);
                                }
                            });
                        }else {
                            holder.mTvLastLoc.setText("Lokasi terakhir tidak diketahui");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                holder.mBtnMeetFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressDialog("Menunggu Respon");

                        final String meetActId = rootRef.push().getKey();
                        Map<String, Object> collect = new HashMap<>();
                        collect.put("inviter", mUser.getUid());
                        collect.put("meetID", meetActId);
                        collect.put("agree", false);
                        Map<String, Object> up = new HashMap<>();
                        up.put("/invite/" + uid, collect);
                        rootRef.updateChildren(up);
                        final CountDownTimer timer = new CountDownTimer(30000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                hideProgressDialog();
                                rootRef.child("invite").child(uid).removeValue();
                                rootRef.child("invite").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue()!=null){
                                            boolean b = (Boolean) dataSnapshot.child("agree").getValue();
                                            if(!b){
                                                Toast.makeText(getApplicationContext(), user.getName() + " tidak merespon", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        };
                        timer.start();
                        rootRef.child("invite").child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    boolean b = (Boolean) dataSnapshot.child("agree").getValue();
                                    if (b) {
                                        hideProgressDialog();
                                        Intent i = new Intent(getApplicationContext(), DirectMeActivity.class);
                                        i.putExtra(MEETID, meetActId);
                                        i.putExtra(FRIENDUID, uid);
                                        startActivity(i);
                                        finish();
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        }
    }
}
