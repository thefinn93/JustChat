package ninja.justchat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.BasicConstraints;
import org.spongycastle.asn1.x509.ExtensionsGenerator;
import org.spongycastle.asn1.x509.KeyUsage;
import org.spongycastle.asn1.x509.X509Extension;
import org.spongycastle.crypto.params.AsymmetricKeyParameter;
import org.spongycastle.crypto.util.PrivateKeyFactory;
import org.spongycastle.openssl.PEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.bc.BcRSAContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;


/**
* Created by Brad Minogue on 4/28/2015.
 * Updated to include certificate generation request stuff by Finn Herzfeld on 5/18/2015
 * Links for certificate stuff:
 *  http://stackoverflow.com/a/24408462/403940
 *
*/
public class NameDialog implements View.OnClickListener {

    Dialog nameDialog;
    ChatActivity current;
    private ProgressDialog pd;

    public NameDialog(ChatActivity current)
    {
        //This needs current for referencing view and resources
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
        Button cancelButton = (Button)nameDialog.findViewById(R.id.cancel_button);
        // upon submission we need to save our input and close the dialog
        submitButton.setOnClickListener(new View.OnClickListener() {

            private Handler handler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    pd.dismiss();
                }
            };

            @Override
            public void onClick(View v) {
                // Store the name somewhere
                ChatActivity.name = editText.getText().toString();
                nameDialog.dismiss();

                // Create a progress dialog to show while we're generating the cert
                pd = ProgressDialog.show(current, "Generating Key Pair", "Sit tight, this only has to happen once", true, false);
                Thread thread = new Thread(new GenerateKeyPair(handler));
                thread.start();

                // Save the username to our preferences file under R.string.user_name
                SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(String.valueOf((R.string.user_name)), ChatActivity.name);
                editor.commit();

            }

        });

        //the only thing we need to do onclick for our cancle button is close the dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                nameDialog.cancel();
            }
        });
    }
}
