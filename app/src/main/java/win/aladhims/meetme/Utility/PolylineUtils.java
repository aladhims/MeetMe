package win.aladhims.meetme.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Aladhims on 18/03/2017.
 */

public class PolylineUtils {


    public static String requestJSONDirection(LatLng source,LatLng dest){

        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(source.latitude));
        urlString.append(",");
        urlString.append(Double.toString(source.longitude));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(dest.latitude));
        urlString.append(",");
        urlString.append(Double.toString(dest.longitude));
        urlString.append("&mode=walking");
        urlString.append("&key=");
        urlString.append("AIzaSyBpRrFNEzBSV6otvWJ0IXyV7hP4dEQ_OgA");
        Log.d("HTTP",urlString.toString());

        return urlString.toString();
    }

    public static ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
            poly.add(p);
        }

        for(int i=0;i<poly.size();i++){
            Log.i("Location", "Point sent: Latitude: "+poly.get(i).latitude+" Longitude: "+poly.get(i).longitude);
        }
        return poly;
    }

    public static String getStringPolyline(String response){

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray routeArray = jsonObject.getJSONArray("routes");
            JSONObject firstRoute = routeArray.getJSONObject(0);
            JSONObject polyline = firstRoute.getJSONObject("overview_polyline");
            return polyline.getString("points");

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MAIN", "ERROR JSON PARSING");
            return null;
        }

    }



    public static void getResponse(Context context, String url,final VolleyCallback callback){


        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MAIN",error.getMessage());
            }
        });

        queue.add(stringRequest);
    }

    public interface VolleyCallback{
        void onSuccess(String string);
    }

}
