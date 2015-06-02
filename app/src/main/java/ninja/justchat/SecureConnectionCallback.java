package ninja.justchat;

import android.util.Log;

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
    @Override
    public void onAPIResponse(JSONObject result) {
        try {
            // TODO: Check the value of success (in result). if success = false, display reason
            JSONArray actions = result.getJSONArray("actions");
            for (int a = 0; a < actions.length(); a++) {
                JSONObject action = actions.getJSONObject(a);
                switch (action.getString("action")) {
                    case "join":
                        ChatActivity.channels.add(new Channel(action.getString("channel")));
                        ChatActivity.currentChannel = ChatActivity.channels.get(ChatActivity.channels.size() - 1);
                        break;
                    case "leave":
                        ChatActivity.channels.remove(new Channel(action.getString("channel")));
                        if (!ChatActivity.channels.isEmpty()) {
                            ChatActivity.currentChannel = ChatActivity.channels.get(ChatActivity.channels.size() - 1);
                        }
                        else {
                            ChatActivity.currentChannel = null;
                        }
                        break;
                    case "receiveMessage":
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
        } catch (JSONException e) {
            Log.d("SecureConnectionCB", result.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onAPIResponse(Exception error) {
        error.printStackTrace();
    }
}