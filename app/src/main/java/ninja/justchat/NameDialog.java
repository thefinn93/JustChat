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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;


/**
* Created by Brad Minogue on 4/28/2015.
 * Updated to include certificate generation request stuff by Finn Herzfeld on 5/18/2015
 * Links for certificate stuff:
 *  http://stackoverflow.com/a/24408462/403940
 *
*/
public class NameDialog implements View.OnClickListener {

    ChatActivity current;
    private static ProgressDialog pd;

    public NameDialog(ChatActivity current) {
        this.current = current;
    }

    @Override
    public void onClick(View v) {
        onClick(v, null, null);
    }

    public void onClick(View v, String CN, String error) {
        AlertDialog.Builder nameDialogBuilder = new AlertDialog.Builder(current);
        nameDialogBuilder.setTitle("Select name");
        nameDialogBuilder.setMessage("Select a name to identify yourself");

        final EditText editText = new EditText(current);
        nameDialogBuilder.setView(editText);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        if(CN != null) {
            editText.setText(CN);
        }
        if(error != null) {
            editText.setError(error);
        }

        nameDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Store the name somewhere
                ChatActivity.name = editText.getText().toString();

                // Create a progress dialog to show while we're generating the cert
                pd = ProgressDialog.show(current, "Generating Key Pair", "Sit tight, this only has to happen once", true, false);
                Thread thread = new Thread(new GenerateKeyPair(new CertProcessingHandler(), current));
                thread.start();
            }
        });

        nameDialogBuilder.show();
    }

    static class CertProcessingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    pd.setTitle("Certificate Generated!");
                    pd.setMessage("Authenticating with the server...");
                    break;
                default:
                    pd.dismiss();
            }
        }
    }
}
