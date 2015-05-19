package ninja.justchat;

import android.os.Handler;
import android.util.Log;

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
 * Created by finn on 5/19/15.
 */
public class GenerateKeyPair implements Runnable {

    private Handler handler;

    public void run() {
        /*
         * OMG CRYPTO STUFF!!
         * Here be dragons. If you edit this, let Finn know, mkay?
         */
        try {
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
            this.handler.sendEmptyMessage(0);
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    GenerateKeyPair(Handler handler) {
        this.handler = handler;
    }


    /*
     * Generates a public/private RSA key pair 4096 bits in length. Excessive? You bet. Secure? maybe
     */
    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
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
