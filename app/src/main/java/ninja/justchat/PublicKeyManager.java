package ninja.justchat;

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
    private static String PUB_KEY = "MIIHRTCCBi2gAwIBAgIHBZs9uk2bSjANBgkqhkiG9w0BAQsFADCBjDELMAkGA1UE\n" +
            "BhMCSUwxFjAUBgNVBAoTDVN0YXJ0Q29tIEx0ZC4xKzApBgNVBAsTIlNlY3VyZSBE\n" +
            "aWdpdGFsIENlcnRpZmljYXRlIFNpZ25pbmcxODA2BgNVBAMTL1N0YXJ0Q29tIENs\n" +
            "YXNzIDEgUHJpbWFyeSBJbnRlcm1lZGlhdGUgU2VydmVyIENBMB4XDTE1MDQyODA4\n" +
            "NTE0N1oXDTE2MDQyODExNDI0OFowUTELMAkGA1UEBhMCVVMxHDAaBgNVBAMTE2p1\n" +
            "c3RjaGF0LmZpbm4ubmluamExJDAiBgkqhkiG9w0BCQEWFXBvc3RtYXN0ZXJAZmlu\n" +
            "bi5uaW5qYTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAMTFWP939tR4\n" +
            "WCPkwA3+JCBc22/l8RxsGW+L81jCnnlZ4de8HCVkpGtQL1ASNvCTs41LjGyu7kR0\n" +
            "gEOOq8I1yZzXAEx3SZUySFsU8JsuUd3YwEC5iMdn/ZtK86QKdS9ovnDpUAEsPF2G\n" +
            "149xwnAz0D/NR1DjdIX74CmxGGKDxqwSR1XpKC+ZwdBm9pywPoAZ3M3iR5pE2YEJ\n" +
            "2a4xLbq/dc4+Gjoyuf7Xw4qVcS3m1tqb2JlW6uyActko9QeGgJOi70bGzcFoQ1tZ\n" +
            "Pox1fjFhyFHuUKr/i764DZQ6AYoa+amxj/Xn6UuY5QvB+gGEJOZFfI4PbyD9NMov\n" +
            "Nv9sSJj/DwUqROxpnsx+hhsZl8zGruTm2rvJgWVyRp9OlybZxuNbNmxsVmery+Tq\n" +
            "zH9n9qy+rxTEe7UVLCLalTGAy7lIGnd8sUemzoMBkHiP7L7JCd37exjoI51QvD3D\n" +
            "W+TgVjXLC8poepCMDX+Xki0kzBXIVUfxg1bfFu2BgQDL2Hgh0AuKWYHNDAKJIslr\n" +
            "iK1Rx9Imem+xj1RzHvPksQzakTUGjT+B8dNh+afxdc0q+XAz0w7R0I+/ACXiRGxE\n" +
            "ondTSsvOhl6Oy/2Pczq1ihrXGWNQqNt8VJ7jcRsc39Zl43Ftjl+qYR9S2SW8LyeF\n" +
            "X4f1mhB47P0MGM3WqHP/KEttYLB+XNBnAgMBAAGjggLkMIIC4DAJBgNVHRMEAjAA\n" +
            "MAsGA1UdDwQEAwIDqDATBgNVHSUEDDAKBggrBgEFBQcDATAdBgNVHQ4EFgQUeEqr\n" +
            "Exrr/8TKwyUwn2v0akXCBlwwHwYDVR0jBBgwFoAU60I00Jiwq5/0G2sI98xkLu8O\n" +
            "LEUwKgYDVR0RBCMwIYITanVzdGNoYXQuZmlubi5uaW5qYYIKZmlubi5uaW5qYTCC\n" +
            "AVYGA1UdIASCAU0wggFJMAgGBmeBDAECATCCATsGCysGAQQBgbU3AQIDMIIBKjAu\n" +
            "BggrBgEFBQcCARYiaHR0cDovL3d3dy5zdGFydHNzbC5jb20vcG9saWN5LnBkZjCB\n" +
            "9wYIKwYBBQUHAgIwgeowJxYgU3RhcnRDb20gQ2VydGlmaWNhdGlvbiBBdXRob3Jp\n" +
            "dHkwAwIBARqBvlRoaXMgY2VydGlmaWNhdGUgd2FzIGlzc3VlZCBhY2NvcmRpbmcg\n" +
            "dG8gdGhlIENsYXNzIDEgVmFsaWRhdGlvbiByZXF1aXJlbWVudHMgb2YgdGhlIFN0\n" +
            "YXJ0Q29tIENBIHBvbGljeSwgcmVsaWFuY2Ugb25seSBmb3IgdGhlIGludGVuZGVk\n" +
            "IHB1cnBvc2UgaW4gY29tcGxpYW5jZSBvZiB0aGUgcmVseWluZyBwYXJ0eSBvYmxp\n" +
            "Z2F0aW9ucy4wNQYDVR0fBC4wLDAqoCigJoYkaHR0cDovL2NybC5zdGFydHNzbC5j\n" +
            "b20vY3J0MS1jcmwuY3JsMIGOBggrBgEFBQcBAQSBgTB/MDkGCCsGAQUFBzABhi1o\n" +
            "dHRwOi8vb2NzcC5zdGFydHNzbC5jb20vc3ViL2NsYXNzMS9zZXJ2ZXIvY2EwQgYI\n" +
            "KwYBBQUHMAKGNmh0dHA6Ly9haWEuc3RhcnRzc2wuY29tL2NlcnRzL3N1Yi5jbGFz\n" +
            "czEuc2VydmVyLmNhLmNydDAjBgNVHRIEHDAahhhodHRwOi8vd3d3LnN0YXJ0c3Ns\n" +
            "LmNvbS8wDQYJKoZIhvcNAQELBQADggEBAHX1h73mBq54xJ3NFXTfSsD1yuviIrWy\n" +
            "dDXuvH3ELMN9ujGSur+Qdl82CygUtFZR8CX4kU7OCAaDLzOLgYxSmMMUKba0btAN\n" +
            "rPTFDm81bWISjunxjMWL6I3+zVBE4eKFXTamJzzbgJtrNtzTQGpz5rCu2NiZyCJm\n" +
            "S/lN5V1yC0Fw6KYN95H8dbcyNTuNkX20Qn8FTLZl7PmxUfmdx+NK5zY7qBE7ZDU2\n" +
            "yOwA3fo1Kmg40dTsSZodk9MCFNl+G4DAvXtNJyG5sDeu0pxZNhWtmRKbt1UO3d+8\n" +
            "7Sy1aimHOKNjWVhak9IGPD0Fj7DWlqIpBSr9i/pkCvVy6S29IwuZmqI=\n";

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

        assert (null != authType && authType.equalsIgnoreCase("RSA"));
        if (!(null != authType && authType.equalsIgnoreCase("RSA"))) {
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
