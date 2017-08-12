package win.aladhims.meetme;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Aladhims on 21/03/2017.
 */

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog makeRawProgressDialog(String message){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}
