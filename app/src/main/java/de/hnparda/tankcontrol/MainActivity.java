package de.hnparda.tankcontrol;

import static de.hnparda.tankcontrol.utils.NodeRequest.GET_DISTANCE;
import static de.hnparda.tankcontrol.utils.NodeRequest.TRIGGER_RELAY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.utilities.Scheme;

import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import de.hnparda.tankcontrol.utils.NodeRequest;
import de.hnparda.tankcontrol.utils.TankProgressBar;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sharedPreferences;
    public static String ip;
    public static String port;
    public static float upperLimit;
    public static float lowerLimit;
    public static SSLContext sslContext;
    public static HurlStack hurlStack;
    float ratio;
    TextView progressText;
    TankProgressBar tankProgressBar;
    boolean firstStart = true;
    boolean onForeground = true;
    boolean repeatRequest = true;

    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tankProgressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        sharedPreferences = getSharedPreferences("Arda", MODE_PRIVATE);


        try {

            AssetManager am = this.getAssets();
            InputStream inStream = am.open("nodemcu2.crt");
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(inStream);
            inStream.close();

            Log.e("testtest", certificate.toString());

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
            trustManagerFactory.init(keyStore);

            sslContext = SSLContext.getInstance("TLS");

            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);



        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Ayarlar").setIcon(R.drawable.icon_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ip = sharedPreferences.getString("NodeIP", "");
        port = sharedPreferences.getString("NodePort", "");
        upperLimit = sharedPreferences.getFloat("upperLimit", 20);
        lowerLimit = sharedPreferences.getFloat("lowerLimit", 170);
        ratio = (lowerLimit - upperLimit) / 100;
        onForeground = true;
        repeatRequest = true;

        if (firstStart) {
            firstStart = false;

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            tankProgressBar.setOnMeasureListener(() -> scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (repeatRequest) runOnUiThread(this::updateProgress);
                } catch (Exception e) {
                    Log.e("ERROR - unexpected exception", Objects.requireNonNull(e.getMessage()));
                }
            }, 0, 3, TimeUnit.SECONDS));

        } else updateProgress();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onForeground = false;
    }

    public void updateProgress() {
        if (!onForeground) return;
        try {

            float distance = Float.parseFloat(new NodeRequest().execute(GET_DISTANCE).get());
            if (distance > lowerLimit || distance < upperLimit) return;
            float dis = distance - upperLimit;
            float reversedProgress = dis / ratio;
            byte progress = (byte) Math.round(100 - reversedProgress);
            tankProgressBar.setProgress(progress);
            progressText.setText(String.valueOf(progress));


        } catch (ExecutionException | InterruptedException | NullPointerException e) {
            repeatRequest = false;
            Log.e("testtest crash", String.valueOf(e.getMessage()));
            if (Objects.requireNonNull(e.getMessage()).contains("trim()' on a null object reference"))
                Toast.makeText(this, "IPye ulasilamiyor.", Toast.LENGTH_SHORT).show();
            else throw new RuntimeException(e);
        }
    }

    public void switchPump(View view) {
        try {

            MaterialButton triggerBtn = (MaterialButton) view;
            String response = new NodeRequest().execute(TRIGGER_RELAY).get();
            String startPump = getResources().getString(R.string.startPump);
            String stopPump = getResources().getString(R.string.stopPump);
            String text = triggerBtn.getText() == startPump ? stopPump : startPump;
            if (Objects.equals(response, "success")) triggerBtn.setText(text);
            else {
                Toast.makeText(this, "iletisimde hata olustu!", Toast.LENGTH_LONG).show();
                Log.e("testtest", "error no respond");
                return;
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}