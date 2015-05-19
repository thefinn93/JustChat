package ninja.justchat;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
* Created by Brad Minogue on 4/28/2015.
*/
public class NameDialog implements View.OnClickListener {
    Dialog nameDialog;
    ChatActivity current;
    public NameDialog(ChatActivity current)
    {
        //This needs current for referncing view and recourses
        this.current = current;
    }

    @Override
    public void onClick(View v) {
        //Basic initialization of class
        nameDialog = new Dialog(current);
        nameDialog.setTitle("Set User Name");
        nameDialog.setContentView(R.layout.name_dialog_layout);
        nameDialog.show();
        //These three items are our input.
        final EditText editText =(EditText)nameDialog.findViewById(R.id.user_name_text_box);
        Button submitButton = (Button)nameDialog.findViewById(R.id.ok_button);
        Button cancleButton = (Button)nameDialog.findViewById(R.id.cancel_button);
        //upon submision we need to save our input and close the dialog
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.name = editText.getText().toString();
                //Our input is saved to sharedpreferences under R.string.user_name
                SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(String.valueOf((R.string.user_name)),
                        editText.getText().toString());
                editor.commit();
                JSONObject dataToSend = new JSONObject();
                try {
                    dataToSend.put("action", "register");
                    dataToSend.put("name", editText.getText().toString());
                } catch(JSONException e) {
                    Log.e("JSONEncoding", e.toString());
                    e.printStackTrace();
                }
                new SecureConnection(new SecureConnectionCallback()).execute(dataToSend);
                nameDialog.cancel();
            }
        });
        //the only thing we need to do onclick for our cancle button is close the dialog
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDialog.cancel();
            }
        });
    }
}