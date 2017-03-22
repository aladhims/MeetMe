package win.aladhims.meetme;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import win.aladhims.meetme.Model.User;
import win.aladhims.meetme.ViewHolder.FriendViewHolder;

public class ListFriendActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    //final fields
    private static final String TAG = ListFriendActivity.class.getSimpleName();
    public static final String MYUIDEXTRAINTENT = "MYUID";
    public static final String FRIENDUID = "FRIENDUID";
    public static final String MEETID = "MEETID";

    //Firebase Fields
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference rootRef,friendListRef,meetRef;
    private FirebaseRecyclerAdapter<User,FriendViewHolder> mAdapter;

    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.rv_friend_list) RecyclerView mRecyclerView;
    @BindView(R.id.pg_friend_list) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friend);
        ButterKnife.bind(this);

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

        mAdapter = new FirebaseRecyclerAdapter<User, FriendViewHolder>(User.class,R.layout.friend_item,FriendViewHolder.class,friendListRef) {
            @Override
            protected void populateViewHolder(FriendViewHolder holder, final User user, int position) {
                mProgressBar.setVisibility(View.GONE);
                final String uid = getRef(position).getKey();
                if(uid.equals(mUser.getUid())){
                    holder.itemView.setVisibility(View.GONE);
                }
                Glide.with(getApplicationContext())
                        .load(user.getPhotoURL())
                        .into(holder.mCiFriendPhoto);
                holder.mTvFriendName.setText(user.getName());
                holder.mBtnMeetFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressDialog();

                        final String meetActId = rootRef.push().getKey();
                        Map<String, Object> collect = new HashMap<>();
                        collect.put("inviter", mUser.getUid());
                        collect.put("meetID", meetActId);
                        collect.put("agree", false);
                        Map<String, Object> up = new HashMap<>();
                        up.put("/invite/" + uid, collect);
                        rootRef.updateChildren(up);
                        final CountDownTimer timer = new CountDownTimer(30000,1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {}

                            @Override
                            public void onFinish() {
                                hideProgressDialog();
                                rootRef.child("invite").child(uid).removeValue();
                                Toast.makeText(getApplicationContext(),user.getName() + " tidak merespon",Toast.LENGTH_LONG).show();
                            }
                        };
                        timer.start();
                        rootRef.child("invite").child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    boolean b = (Boolean) dataSnapshot.child("agree").getValue();
                                    if (b) {
                                        hideProgressDialog();
                                        Intent i = new Intent(getApplicationContext(), DirectMeActivity.class);
                                        i.putExtra(MEETID, meetActId);
                                        i.putExtra(FRIENDUID, uid);
                                        startActivity(i);
                                        timer.cancel();
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
        };

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mAdapter);

        Intent i = new Intent(this,NotifyMeService.class);
        i.putExtra(MYUIDEXTRAINTENT,mUser.getUid());
        startService(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_friend_menu,menu);
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
}
