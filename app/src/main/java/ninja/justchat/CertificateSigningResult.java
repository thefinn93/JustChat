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
                String CN = null;
                if(result.has("CN")) {
                    CN = result.getString("CN");
                }
                new NameDialog(current).onClick(new View(current), CN, result.getString("reason"));
            }
        } catch (JSONException e) {
            new NameDialog(current).onClick(new View(current), null, e.toString());
        }
    }

    @Override
    public void onAPIResponse(Exception error) {
        // TODO: Figure out why this is crashing the app, make it not do that
        error.printStackTrace();
        new NameDialog(current).onClick(new View(current), null, error.toString());
    }
}
