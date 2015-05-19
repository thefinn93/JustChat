package ninja.justchat;

import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by finn on 5/19/15.
 */
public class CertificateSigningResult implements onAPIResponse {

    private ChatActivity current;

    CertificateSigningResult(ChatActivity current) {
        this.current = current;
    }

    @Override
    public void onAPIResponse(JSONObject result) {
        try {
            Log.d("CertSigningResult", result.toString());
            if (result.getBoolean("success")) {
                Log.d("CertSigningResult", "Got a cert successfully, your CN is " + result.getString("CN"));
            } else {
                new NameDialog(current).onClick(new View(current), result.getString("CN"), result.getString("reason"));
            }
        } catch (JSONException e) {
            new NameDialog(current).onClick(new View(current), null, e.toString());
        }
    }

    @Override
    public void onAPIResponse(Exception error) {
        // TODO: Figure out why this is throwing an error, make it not do that
        new NameDialog(current).onClick(new View(current), null, error.toString());
        error.printStackTrace();
    }
}
