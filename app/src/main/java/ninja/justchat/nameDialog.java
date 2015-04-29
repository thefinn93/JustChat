package ninja.justchat;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
* Created by FryedMan on 4/28/2015.
*/
public class nameDialog implements View.OnClickListener{
    Dialog nameDialog;
    ChatActivity current;
    public nameDialog(ChatActivity current)
    {
        this.current = current;
    }

    @Override
    public void onClick(View v) {
        nameDialog = new Dialog(current);
        this.current = current;
        nameDialog.setTitle("Set User Name");
        nameDialog.setContentView(R.layout.name_dialog_layout);
        nameDialog.show();
        final EditText editText =(EditText)nameDialog.findViewById(R.id.user_name_text_box);
        Button submitButton = (Button)nameDialog.findViewById(R.id.ok_button);
        Button cancleButton = (Button)nameDialog.findViewById(R.id.cancle_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(current.getApplicationContext(), "working", Toast.LENGTH_SHORT).show();
                ChatActivity.name = editText.getText().toString();
                nameDialog.cancel();
                SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(String.valueOf((R.string.user_name)), editText.getText().toString());
                editor.commit();
            }
        });
        cancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDialog.cancel();
            }
        });
    }
}
