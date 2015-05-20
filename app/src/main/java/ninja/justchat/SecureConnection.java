package ninja.justchat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

// http://android-developers.blogspot.com/2009/05/painless-threading.html
public class SecureConnection extends AsyncTask<JSONObject, Object, Object> {

    private onAPIResponse listener;

    public SecureConnection(onAPIResponse listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        Log.d("SecureConnection", "Fetching...");
    }

    @Override
    protected Object doInBackground(JSONObject... params) {

        Object result;

        try {
            Log.d("SecureConnection", "Connecting");


            TrustManager tm[] = { new PublicKeyManager() };
            assert (null != tm);

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tm, null);

            URL url = new URL( "https://justchat.finn.ninja/api" );

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setDoOutput(true);

            PrintWriter postdata = new PrintWriter(connection.getOutputStream());
            postdata.println(params[0].toString());
            Log.d("SecureConnectionJSON", params[0].toString());
            postdata.close();

            connection.setSSLSocketFactory(context.getSocketFactory());

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();

            // Prepare return value
            result = sb.toString();

        } catch (Exception ex) {

            // Log error
            Log.e("doInBackground", ex.toString());

            // Prepare return value
            listener.onAPIResponse(ex);
            result = null;
        }

        return result;
    }

    @Override
    protected void onPostExecute(Object result) {
        if(result != null) {
            try {
                JSONObject jsonResponse = new JSONObject((String) result);
                listener.onAPIResponse(jsonResponse);
            } catch (Exception ex) {
                listener.onAPIResponse(ex);
            }
        }
    }
}