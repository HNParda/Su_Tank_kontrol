package de.hnparda.tankcontrol.utils;

import static org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
import static de.hnparda.tankcontrol.MainActivity.ip;
import static de.hnparda.tankcontrol.MainActivity.sslContext;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.net.SocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class NodeRequest extends AsyncTask<Byte, String, String> {

    @Request
    public static byte GET_DISTANCE = 0;
    @Request
    public static byte TRIGGER_RELAY = 1;

    @Override
    protected String doInBackground(Byte... requestId) {
        try {

            HttpsURLConnection con = (HttpsURLConnection) new URL("https://" + ip + "/test").openConnection();
            //con.setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);

            con.setSSLSocketFactory(sslContext.getSocketFactory());

            Log.e("testtest request ip", con.getURL().toString());
            con.setRequestMethod("POST");
            con.setConnectTimeout(2000);
            con.setDoInput(true);
            con.setDoOutput(true);

            OutputStream outputStream = con.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            writer.write("password=SAdn39dSdn3lkds");
            writer.write("requestKey=" + requestId[0] + "");
            writer.flush();
            writer.close();
            outputStream.close();


            if (con.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                return br.readLine();
            } else return "failed ";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
