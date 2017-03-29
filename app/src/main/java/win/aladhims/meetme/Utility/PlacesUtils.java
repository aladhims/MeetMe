package win.aladhims.meetme.Utility;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aladhims on 29/03/2017.
 */

public class PlacesUtils {

    public static String requestPlace(LatLng latLng){

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/geocode/json?");
        urlString.append("latlng=");// from
        urlString.append(Double.toString(latLng.latitude)+",");
        urlString.append(Double.toString(latLng.longitude));
        urlString.append("&key=");
        urlString.append("AIzaSyBpRrFNEzBSV6otvWJ0IXyV7hP4dEQ_OgA");

        return urlString.toString();
    }

    public static String getPlaces(String response){
        StringBuilder placesBuilder = new StringBuilder();
        try {
            JSONObject JSONResponse = new JSONObject(response);
            JSONArray placesAlikeArray = JSONResponse.getJSONArray("results");
            JSONObject mostAccurate = placesAlikeArray.getJSONObject(0);
            JSONArray addressComponent = mostAccurate.getJSONArray("address_components");
            placesBuilder.append(addressComponent.getJSONObject(2).getString("short_name"));
            placesBuilder.append(", "+addressComponent.getJSONObject(3).getString("short_name"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return placesBuilder.toString();
    }
}
