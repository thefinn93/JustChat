package ninja.justchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Chad Dugie on 5/24/2015.
 *
 *
 *
 *
 */
public class ChannelSelect implements View.OnClickListener {

    ChatActivity current;
    private ProgressDialog pd;

    public ChannelSelect(ChatActivity current)
    {
        //This needs current for referencing view and resources
        this.current = current;
    }

    @Override
    public void onClick(View v) {
        onClick(v, null, null);
    }

    public void onClick(View v, String CN, String error) {
        AlertDialog.Builder channelSelectBuilder = new AlertDialog.Builder(current);
        channelSelectBuilder.setTitle("Select channel");
        channelSelectBuilder.setMessage("Select a channel to join");

        final EditText editText = new EditText(current);
        channelSelectBuilder.setView(editText);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if(CN != null) {
            editText.setText(CN);
        }
        if(error != null) {
            editText.setError(error);
        }

        channelSelectBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Store the name somewhere
                try {
                    JSONObject channelSelection = new JSONObject();
                    channelSelection.put("action", "join");
                    channelSelection.put("channel", editText.getText().toString());
                    SecureConnectionCallback callback = new SecureConnectionCallback();
                    new SecureConnection(callback, ChatActivity.keymanagers).execute(channelSelection);
                    //Thread thread = new Thread(new SecureConnection(new SecureConnectionCallback()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                    // Create a progress dialog to show while we're generating the cert
                    pd = ProgressDialog.show(current, "Generating Key Pair", "Sit tight, this only has to happen once", true, false);

                    // Save the username to our preferences file under R.string.user_name
                    // Only save if we're not debugging
                    if (!BuildConfig.DEBUG) {
                        SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(String.valueOf((R.string.username)), ChatActivity.name);
                        editor.apply();
                    }
                }
            }

            );

            channelSelectBuilder.show();
        }

    }