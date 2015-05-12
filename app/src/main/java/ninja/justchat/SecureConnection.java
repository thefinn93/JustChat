package ninja.justchat;

import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

// http://android-developers.blogspot.com/2009/05/painless-threading.html
public class SecureConnection extends AsyncTask<String, Void, Object> {

    @Override
    protected void onPreExecute() {
        Log.d("SecureConnection", "Fetching...");
    }

    @Override
    protected Object doInBackground(String... params) {

        Object result = null;

        try {
            Log.d("SecureConnection", "Connecting");

            byte[] secret = null;

            TrustManager tm[] = { new PublicKeyManager() };
            assert (null != tm);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tm, null);

            URL url = new URL( "https://justchat.finn.ninja/" + params[0] );
            Log.d("SecureConnection", "Connecting to https://justchat.finn.ninja/" + params[0]);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            assert (null != connection);

            connection.setSSLSocketFactory(context.getSocketFactory());
            InputStreamReader instream = new InputStreamReader(connection.getInputStream());

            StreamTokenizer tokenizer = new StreamTokenizer(instream);

            secret = new byte[16];

            int idx = 0, token;
            while (idx < secret.length) {
                token = tokenizer.nextToken();
                if (token == StreamTokenizer.TT_EOF)
                    break;
                if (token != StreamTokenizer.TT_NUMBER)
                    continue;

                secret[idx++] = (byte) tokenizer.nval;
            }

            // Prepare return value
            result = (Object) secret;

        } catch (Exception ex) {

            // Log error
            Log.e("doInBackground", ex.toString());

            // Prepare return value
            result = (Object) ex;
        }

        return result;
    }

    @Override
    protected void onPostExecute(Object result) {

        assert (null != result);
        if (null == result)
            return;

        assert (result instanceof Exception || result instanceof byte[]);
        if (!(result instanceof Exception || result instanceof byte[]))
            return;

        if (result instanceof Exception) {
            ExitWithException((Exception) result);
            return;
        }

        ExitWithSecret((byte[]) result);
    }

    protected void ExitWithException(Exception ex) {
        Log.d("SecureConnection", "Error fetching secret " + ex.toString());
    }

    protected void ExitWithSecret(byte[] secret) {
        StringBuilder sb = new StringBuilder(secret.length * 3 + 1);

        Log.d("SecureConnection", sb.toString());
    }
}