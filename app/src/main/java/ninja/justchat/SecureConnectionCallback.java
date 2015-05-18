package ninja.justchat;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by finn on 5/13/15.
 *
 * Based on sample code from http://stackoverflow.com/a/9963705/403940 and some explanation at
 * http://www.justinmccandless.com/blog/Setting+Up+a+Callback+Function+in+Android
 */

public class SecureConnectionCallback implements onAPIResponse {
    @Override
    public void onAPIResponse(JSONObject result) {
        Log.d("ConnectionCB", result.toString());
    }

    @Override
    public void onAPIResponse(Exception error) {
        error.printStackTrace();
    }
}