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


/**
 * Created by Brad Minogue on 4/28/2015.
 * Updated to include certificate generation request stuff by Finn Herzfeld on 5/18/2015
 * Links for certificate stuff:
 *  http://stackoverflow.com/a/24408462/403940
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
        channelSelectBuilder.setTitle("Select name");
        channelSelectBuilder.setMessage("Select a name to identify yourself");

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
                ChatActivity.name = editText.getText().toString();

                // Create a progress dialog to show while we're generating the cert
                pd = ProgressDialog.show(current, "Generating Key Pair", "Sit tight, this only has to happen once", true, false);
                Thread thread = new Thread(new GenerateKeyPair(handler, current));
                thread.start();

                // Save the username to our preferences file under R.string.user_name
                // Only save if we're not debugging
                if (!BuildConfig.DEBUG) {
                    SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(String.valueOf((R.string.username)), ChatActivity.name);
                    editor.apply();
                }
            }
        });

        channelSelectBuilder.show();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();
        }
    };
}