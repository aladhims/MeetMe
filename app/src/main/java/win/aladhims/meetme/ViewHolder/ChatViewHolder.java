package win.aladhims.meetme.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import win.aladhims.meetme.R;

/**
 * Created by Aladhims on 20/03/2017.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.ci_photo_user_chat) CircleImageView mCiPhotoUserChat;
    public @BindView(R.id.tv_pengirim_chat) TextView mTvSenderName;
    public @BindView(R.id.tv_pesan_chat) TextView mTvMessage;
    public @BindView(R.id.ll_body_pesan) LinearLayout mMessageBody;
    public @BindView(R.id.iv_pesan_foto_chat) ImageView mIvFotoPesan;

    public ChatViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}
