package ninja.justchat;

import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by finn on 5/13/15.
 *
 * Based on sample code from http://stackoverflow.com/a/9963705/403940 and some explanation at
 * http://www.justinmccandless.com/blog/Setting+Up+a+Callback+Function+in+Android
 */

public class SecureConnectionCallback implements onAPIResponse {

    private FragmentActivity frag;

    SecureConnectionCallback(FragmentActivity frag) {
        // Not sure what exactly we should be passing. Ideally we would get a callback function to handle errors, but we dont have time for that
        this.frag = frag;
    }

    SecureConnectionCallback() {
        this.frag = null;
    }

    private void displayError(String error) {
        if(this.frag != null) {
            EditText inputbox = (EditText) this.frag.findViewById(R.id.entryBox);
            inputbox.setError(error);
        }
    }

    @Override
    public void onAPIResponse(JSONObject result) {
        try {
            if(result.getBoolean("success")) {
                JSONArray actions = result.getJSONArray("actions");
                for (int a = 0; a < actions.length(); a++) {
                    JSONObject action = actions.getJSONObject(a);
                    displayError(result.toString());
                    switch (action.getString("action")) {
                        case "join":
                            ChatActivity.channels.add(new Channel(action.getString("channel")));
                            ChatActivity.currentChannel = ChatActivity.channels.get(ChatActivity.channels.size() - 1);
                            break;
                        case "leave":
                            ChatActivity.channels.remove(new Channel(action.getString("channel")));
                            if (!ChatActivity.channels.isEmpty()) {
                                ChatActivity.currentChannel = ChatActivity.channels.get(ChatActivity.channels.size() - 1);
                            } else {
                                ChatActivity.currentChannel = null;
                            }
                            break;
                        case "sendmsg":
                            for (Channel targetChannel : ChatActivity.channels) {
                                if (targetChannel.name == action.getString("channel")) {
                                    targetChannel.chatLog.add(action.getString("message"));
                                }
                            }
                            break;
                        default:
                            Log.e("API_Response", "Received invalid action: " + action.getString("action"));
                            break;
                    }
                }
            } else {
                displayError(result.getString("reason"));
            }
        } catch (JSONException e) {
            displayError(e.toString());
            Log.d("SecureConnectionCB", result.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onAPIResponse(Exception error) {
        error.printStackTrace();
    }
}