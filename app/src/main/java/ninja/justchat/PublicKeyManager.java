package ninja.justchat;

import android.util.Log;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by finn on 4/30/15. Stolen from the OWASP cert pinning example, who in turn stole it from
 * Moxie. https://www.owasp.org/index.php/Certificate_and_Public_Key_Pinning#Android
 */
public final class PublicKeyManager implements X509TrustManager {

    // DER encoded public key
    private static String PUB_KEY = "30820222300d06092a864886f70d01010105000382020f003082020a0282" +
            "020100c4c558ff77f6d4785823e4c00dfe24205cdb6fe5f11c6c196f8bf358c29e7959e1d7bc1c2564a4" +
            "6b502f501236f093b38d4b8c6caeee447480438eabc235c99cd7004c77499532485b14f09b2e51ddd8c0" +
            "40b988c767fd9b4af3a40a752f68be70e950012c3c5d86d78f71c27033d03fcd4750e37485fbe029b118" +
            "6283c6ac124755e9282f99c1d066f69cb03e8019dccde2479a44d98109d9ae312dbabf75ce3e1a3a32b9" +
            "fed7c38a95712de6d6da9bd89956eaec8072d928f507868093a2ef46c6cdc168435b593e8c757e3161c8" +
            "51ee50aaff8bbeb80d943a018a1af9a9b18ff5e7e94b98e50bc1fa018424e6457c8e0f6f20fd34ca2f36" +
            "ff6c4898ff0f052a44ec699ecc7e861b1997ccc6aee4e6dabbc9816572469f4e9726d9c6e35b366c6c56" +
            "67abcbe4eacc7f67f6acbeaf14c47bb5152c22da953180cbb9481a777cb147a6ce830190788fecbec909" +
            "ddfb7b18e8239d50bc3dc35be4e05635cb0bca687a908c0d7f97922d24cc15c85547f18356df16ed8181" +
            "00cbd87821d00b8a5981cd0c028922c96b88ad51c7d2267a6fb18f54731ef3e4b10cda9135068d3f81f1" +
            "d361f9a7f175cd2af97033d30ed1d08fbf0025e2446c44a277534acbce865e8ecbfd8f733ab58a1ad719" +
            "6350a8db7c549ee3711b1cdfd665e3716d8e5faa611f52d925bc2f27855f87f59a1078ecfd0c18cdd6a8" +
            "73ff284b6d60b07e5cd0670203010001";


    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {

        assert (chain != null);
        if (chain == null) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate array is null");
        }

        assert (chain.length > 0);
        if (!(chain.length > 0)) {
            throw new IllegalArgumentException(
                    "checkServerTrusted: X509Certificate is empty");
        }

        assert (null != authType && authType.equalsIgnoreCase("ECDHE_RSA"));
        if (!(null != authType && authType.equalsIgnoreCase("ECDHE_RSA"))) {
            Log.d("PublicKeyManager", "Auth type is " + authType);
            throw new CertificateException(
                    "checkServerTrusted: AuthType is not RSA");
        }

        // Perform customary SSL/TLS checks
        TrustManagerFactory tmf;
        try {
            tmf = TrustManagerFactory.getInstance("X509");
            tmf.init((KeyStore) null);

            for (TrustManager trustManager : tmf.getTrustManagers()) {
                ((X509TrustManager) trustManager).checkServerTrusted(
                        chain, authType);
            }

        } catch (Exception e) {
            throw new CertificateException(e);
        }

        // Hack ahead: BigInteger and toString(). We know a DER encoded Public
        // Key starts with 0x30 (ASN.1 SEQUENCE and CONSTRUCTED), so there is
        // no leading 0x00 to drop.
        RSAPublicKey pubkey = (RSAPublicKey) chain[0].getPublicKey();
        String encoded = new BigInteger(1 /* positive */, pubkey.getEncoded())
                .toString(16);

        // Pin it!
        final boolean expected = PUB_KEY.equalsIgnoreCase(encoded);
        assert(expected);
        if (!expected) {
            throw new CertificateException(
                    "checkServerTrusted: Expected public key: " + PUB_KEY
                            + ", got public key:" + encoded);
        }
    }

    public void checkClientTrusted(X509Certificate[] xcs, String string) {
        // throw new
        // UnsupportedOperationException("checkClientTrusted: Not supported yet.");
    }

    public X509Certificate[] getAcceptedIssuers() {
        // throw new
        // UnsupportedOperationException("getAcceptedIssuers: Not supported yet.");
        return null;
    }
}
