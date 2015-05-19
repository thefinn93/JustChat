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
        Button cancelButton = (Button)nameDialog.findViewById(R.id.cancel_button);
        // upon submision we need to save our input and close the dialog
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ChatActivity.name = editText.getText().toString();
                    nameDialog.cancel();
                    // TODO: Pop up a dialog letting the client know we're doing shit and their name isn't saved yet, or at least a spinner

                    // Save the username to our preferences file under R.string.user_name
                    SharedPreferences sharedPref = current.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(String.valueOf((R.string.user_name)), ChatActivity.name);
                    editor.commit();

                    /*
                     * OMG CRYPTO STUFF!!
                     * Here be dragons. If you edit this, let Finn know, mkay?
                     */

                    // Generate the key pair and prepare it for signing
                    Log.d("CertificateCreation", "Generating key pair....");
                    KeyPair keypair = generateKeyPair();
                    Log.d("CertificateCreation", "Key pair generated, creating CSR...");
                    String csr = generateCSRFile(keypair, ChatActivity.name);
                    Log.d("CertificateCreation", "CSR generated. Uploading...");

                    // Generate the JSON request, retreiving the key from
                    JSONObject dataToSend = new JSONObject();
                    dataToSend.put("action", "register");
                    dataToSend.put("csr", csr.toString());
                    new SecureConnection(new SecureConnectionCallback()).execute(dataToSend);

                    /*
                     * </dragons>
                     */
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (OperatorCreationException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //the only thing we need to do onclick for our cancle button is close the dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDialog.cancel();
            }
        });
    }


    /*
     * Generates a public/private RSA key pair 4096 bits in length. Excessive? You bet. Secure? maybe
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        return keyPair;
    }

    /*
     * Generates a Certificate Signing Request to be sent to the CA to get a signed certificate.
     */
    private static String generateCSRFile(KeyPair keyPair, String cn) throws IOException, OperatorCreationException {
        String subject = "CN=" + cn; // The subject of the CSR is only the user's name.

        // Get the things we need
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
        AlgorithmIdentifier signatureAlgorithm = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WITHRSA");
        AlgorithmIdentifier digestAlgorithm = new DefaultDigestAlgorithmIdentifierFinder().find("SHA-256");
        ContentSigner signer = new BcRSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm).build(privateKey);

        // Build the CSR
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(new X500Name(subject), keyPair.getPublic());
        ExtensionsGenerator extensionsGenerator = new ExtensionsGenerator();
        extensionsGenerator.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(true));
        extensionsGenerator.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign  | KeyUsage.cRLSign));
        csrBuilder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extensionsGenerator.generate());
        PKCS10CertificationRequest csr = csrBuilder.build(signer);

        // We now have the CSR, but we're going to encode it in PEM format first, so OpenSSL and such can handle it nicely
        PemObject pemObject = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());
        StringWriter pemString = new StringWriter();
        PemWriter pemWriter = new PemWriter(pemString);
        pemWriter.writeObject(pemObject);
        pemWriter.close();
        pemString.close();
        return pemString.toString();

    }
}
