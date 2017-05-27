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
 * Created by Aladhims on 20/05/2017.
 */

public class AddFriendViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.ci_add_friend) CircleImageView mCiAddFriend;
    public @BindView(R.id.tv_add_friend_name) TextView mTvNamaAdd;
    public @BindView(R.id.tv_add_friend_email) TextView mTvEmailAdd;
    public @BindView(R.id.btn_add_friend) Button mBtnAddFriend;
    public @BindView(R.id.ll_add_friend_item) LinearLayout mLLAddFriend;
    public AddFriendViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
