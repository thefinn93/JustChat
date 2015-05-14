package ninja.justchat;

import java.io.InputStreamReader;
import java.io.PrintWriter;
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

import org.json.JSONException;
import org.json.JSONObject;

// http://android-developers.blogspot.com/2009/05/painless-threading.html
public class SecureConnection extends AsyncTask<JSONObject, Object, Object> {

    private ApiResponse callback;

    @Override
    protected void onPreExecute() {
        Log.d("SecureConnection", "Fetching...");
    }

    @Override
    protected Object doInBackground(JSONObject... params) {

        Object result = null;

        try {
            Log.d("SecureConnection", "Connecting");

            byte[] response = null;

            TrustManager tm[] = { new PublicKeyManager() };
            assert (null != tm);

            SSLContext context = SSLContext.getInstance("TLS");
            assert (null != context);
            context.init(null, tm, null);

            URL url = new URL( "https://justchat.finn.ninja/endpoint" );
            assert (null != url);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            assert (null != connection);

            connection.setDoOutput(true);

            PrintWriter postdata = new PrintWriter(connection.getOutputStream());
            postdata.println(params[0].toString());
            Log.d("SecureConnectionJSON", params[0].toString());
            postdata.close();
            postdata = null;

            connection.setSSLSocketFactory(context.getSocketFactory());
            InputStreamReader instream = new InputStreamReader(connection.getInputStream());
            assert (null != instream);

            StreamTokenizer tokenizer = new StreamTokenizer(instream);
            assert (null != tokenizer);

            response = new byte[16];
            assert (null != response);

            int idx = 0, token;
            while (idx < response.length) {
                token = tokenizer.nextToken();
                if (token == StreamTokenizer.TT_EOF)
                    break;
                if (token != StreamTokenizer.TT_NUMBER)
                    continue;

                response[idx++] = (byte) tokenizer.nval;
            }

            // Prepare return value
            result = (Object) response;

        } catch (Exception ex) {

            // Log error
            Log.e("doInBackground", ex.toString());

            // Prepare return value
            result = (Object) ex;
        }

        return result;
    }

    protected void onPostExecute(Object result) {
        // TODO: catch any issues with the execution of the task, handle them logically
        doAPICallback((byte[]) result);
    }

    protected void ExitWithException(Exception ex) {
        Log.d("SecureConnection", "Error fetching result " + ex.toString());
    }

    protected void doAPICallback(byte[] result) {
        StringBuilder sb = new StringBuilder(result.length * 3 + 1);

        try {
            JSONObject jsonObject = new JSONObject(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("SecureConnection", sb.toString());
    }
}