package win.aladhims.meetme.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import win.aladhims.meetme.R;

/**
 * Created by Aladhims on 20/03/2017.
 */

public class FriendViewHolder extends RecyclerView.ViewHolder {

    //this class is for defining and binding for each item in friend list

    public @BindView(R.id.ci_friend_list) CircleImageView mCiFriendPhoto;
    public @BindView(R.id.tv_name_friend_list) TextView mTvFriendName;
    public @BindView(R.id.btn_meet_friend) Button mBtnMeetFriend;
    public @BindView(R.id.ll_friend_item) LinearLayout mLLFriend;
    public @BindView(R.id.tv_friend_lastloc) TextView mTvLastLoc;
    public @BindView(R.id.wrapper_friend_name) LinearLayout mLLWrapper;
    public FriendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }

    public void hideAllViews(boolean b){
        if(b){
            itemView.setVisibility(View.GONE);
            mCiFriendPhoto.setVisibility(View.GONE);
            mTvFriendName.setVisibility(View.GONE);
            mBtnMeetFriend.setVisibility(View.GONE);
            mLLFriend.setVisibility(View.GONE);
            mTvLastLoc.setVisibility(View.GONE);
            mLLWrapper.setVisibility(View.GONE);
        }
    }
}
