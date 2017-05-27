package win.aladhims.meetme.Utility;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Aladhims on 09/05/2017.
 */

public class PrefManager {

    private static final int PRIVATE_MODE = 0;
    private static final String IS_FIRST_LAUNCH = "FIRSTLAUNCH";
    private static final String THE_CURRENT_UID = "CURRUID";
    private static final String WELCOME_PREF = "WELPREF";
    private static final String UID_PREF = "UIDPREF";
    private static final boolean DEFAULT_VAL = true;

    Context mContext;
    SharedPreferences Welcomepreferences, uidPreferences;
    SharedPreferences.Editor WelcomeEditor, uidEditor;

    public PrefManager(Context context){
        mContext = context;
        uidPreferences = mContext.getSharedPreferences(UID_PREF,PRIVATE_MODE);
        Welcomepreferences = mContext.getSharedPreferences(WELCOME_PREF,PRIVATE_MODE);
        WelcomeEditor = Welcomepreferences.edit();
        uidEditor = uidPreferences.edit();

    }

    public void setFirstTimeLaunch(boolean b){
        WelcomeEditor.putBoolean(IS_FIRST_LAUNCH,b);
        WelcomeEditor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return Welcomepreferences.getBoolean(IS_FIRST_LAUNCH,DEFAULT_VAL);
    }

    public void setCurrentUID(String uid){
        uidEditor.putString(THE_CURRENT_UID,uid);
        uidEditor.commit();
    }

    public String getCurrentUID(){
        return uidPreferences.getString(THE_CURRENT_UID,"NULL");
    }
    
}
