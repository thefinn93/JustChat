package ninja.justchat;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by finn on 5/19/15.
 */
public class CertificateSigningResult implements onAPIResponse {

    @Override
    public void onAPIResponse(JSONObject result) {
        Log.d("CertSigningResult", result.toString());
    }

    @Override
    public void onAPIResponse(Exception error) {
        error.printStackTrace();
    }
}
