package ninja.justchat;

import org.json.JSONObject;

/**
 * Created by finn on 5/13/15.
 * Based on sample code from http://stackoverflow.com/a/9963705/403940 and some explanation at
 * http://www.justinmccandless.com/blog/Setting+Up+a+Callback+Function+in+Android
 */

public interface onAPIResponse {
    void onAPIResponse(JSONObject result);
    void onAPIResponse(Exception error);
}

