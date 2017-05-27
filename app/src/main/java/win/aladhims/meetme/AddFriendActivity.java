package win.aladhims.meetme;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import win.aladhims.meetme.Model.User;
import win.aladhims.meetme.ViewHolder.AddFriendViewHolder;

public class AddFriendActivity extends BaseActivity {

    private FirebaseRecyclerAdapter<User,AddFriendViewHolder> mAdapter;
    @BindView(R.id.pg_add_friend) ProgressBar mProgressBar;
    @BindView(R.id.rv_add_friend) RecyclerView mRvAddFriend;

    DatabaseReference rootRef,usersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Add Friends");



        final FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("users");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvAddFriend.setLayoutManager(linearLayoutManager);
        mAdapter = new FirebaseRecyclerAdapter<User, AddFriendViewHolder>(User.class, R.layout.add_friend_item, AddFriendViewHolder.class, usersRef) {
            @Override
            protected void populateViewHolder(final AddFriendViewHolder viewHolder, final User user, final int position) {
                mProgressBar.setVisibility(View.INVISIBLE);

                final String uidItem = getRef(position).getKey();
                if (uidItem.equals(mUser.getUid()) ){
                    viewHolder.mBtnAddFriend.setVisibility(View.INVISIBLE);
                    viewHolder.mBtnAddFriend.setOnClickListener(null);
                }
                Glide.with(getApplicationContext())
                        .load(user.getPhotoURL())
                        .into(viewHolder.mCiAddFriend);
                final String[] name = user.getName().split(" ");
                viewHolder.mTvNamaAdd.setText(name[0]);
                viewHolder.mTvEmailAdd.setText(user.getEmail());
                final View.OnClickListener follow = new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Map<String, Object> updateAdd = new HashMap<>();
                        updateAdd.put("/friends/" + "/" + mUser.getUid() + "/" + uidItem, true);
                        updateAdd.put("/friends/" + "/" + uidItem + "/" + mUser.getUid(), true);
                        rootRef.updateChildren(updateAdd)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar.make(v,"mulai berteman dengan " + name[0],Snackbar.LENGTH_SHORT).show();
                                        viewHolder.mBtnAddFriend.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_person_remove));
                                        viewHolder.mBtnAddFriend.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                rootRef.child("friends").child(mUser.getUid()).child(uidItem).removeValue();
                                                rootRef.child("friends").child(uidItem).child(mUser.getUid()).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                viewHolder.mBtnAddFriend.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_black_person_add));
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                });
                    }
                };

                final View.OnClickListener unfollow = new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        rootRef.child("friends").child(mUser.getUid()).child(uidItem).removeValue();
                        rootRef.child("friends").child(uidItem).child(mUser.getUid()).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Snackbar.make(v,"berhenti berteman dengan " + name[0],Snackbar.LENGTH_SHORT).show();
                                        viewHolder.mBtnAddFriend.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_black_person_add));
                                        viewHolder.mBtnAddFriend.setOnClickListener(follow);
                                    }
                                });
                    }
                };

                rootRef.child("friends").child(mUser.getUid()).child(uidItem)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() == null) {
                                    viewHolder.mBtnAddFriend.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_black_person_add));
                                    viewHolder.mBtnAddFriend.setOnClickListener(follow);
                                } else {
                                    viewHolder.mBtnAddFriend.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_person_remove));
                                    viewHolder.mBtnAddFriend.setOnClickListener(unfollow);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

        };
        mRvAddFriend.setAdapter(mAdapter);

    }
}
