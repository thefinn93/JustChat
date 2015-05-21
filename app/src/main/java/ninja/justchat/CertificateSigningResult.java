package ninja.justchat;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by finn on 5/19/15.
 * Receives the result of a signing request and stores the certificate (or prompts the user to select a new name if it fails)
 */
public class CertificateSigningResult implements onAPIResponse {

    private ChatActivity current;
    private KeyPair keypair;

    CertificateSigningResult(ChatActivity current, KeyPair keypair) {
        this.current = current;
        this.keypair = keypair;
    }

    @Override
    public void onAPIResponse(JSONObject result) {
        try {
            Log.d("CertSigningResult", result.toString());
            if (result.getBoolean("success")) {
                Log.d("CertSigningResult", "Got a cert successfully, your CN is " + result.getString("CN"));
                Log.d("CertSigningResult", "Got the following cert: " + result.getString("cert"));
                KeyStore store = KeyStore.getInstance("BKS", "SC");

                InputStream pemstream = new ByteArrayInputStream(result.getString("cert").getBytes());

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate[] chain = {cf.generateCertificate(pemstream)};
                store.setKeyEntry("JustChatUser", keypair.getPrivate().getEncoded(), chain);
                Log.d("CertSigningResult", "Stored the cert!");
                Toast.makeText(current, "Successfully registered! Welcome, " + result.getString("CN"), Toast.LENGTH_LONG).show();
            } else {
                String CN = null;
                if(result.has("CN")) {
                    CN = result.getString("CN");
                }
                new NameDialog(current).onClick(new View(current), CN, result.getString("reason"));
            }
        } catch (JSONException e) {
            new NameDialog(current).onClick(new View(current), null, e.toString());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAPIResponse(Exception error) {
        // TODO: Figure out why this is crashing the app, make it not do that
        error.printStackTrace();
        new NameDialog(current).onClick(new View(current), null, error.toString());
    }
}
