package win.aladhims.meetme;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Aladhims on 09/05/2017.
 */

public class PrefManager {

    private static final int PRIVATE_MODE = 0;
    private static final String IS_FIRST_LAUNCH = "FIRSTLAUNCH";
    private static final String WELCOME_PREF = "WELPREF";
    private static final boolean DEFAULT_VAL = true;

    Context mContext;
    SharedPreferences Welcomepreferences;
    SharedPreferences.Editor WelcomeEditor;

    public PrefManager(Context context){
        mContext = context;
        Welcomepreferences = mContext.getSharedPreferences(WELCOME_PREF,PRIVATE_MODE);
        WelcomeEditor = Welcomepreferences.edit();

    }

    public void setFirstTimeLaunch(boolean b){
        WelcomeEditor.putBoolean(IS_FIRST_LAUNCH,b);
        WelcomeEditor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return Welcomepreferences.getBoolean(IS_FIRST_LAUNCH,DEFAULT_VAL);
    }
    
}
