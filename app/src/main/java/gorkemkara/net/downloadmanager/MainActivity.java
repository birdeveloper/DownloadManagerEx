package gorkemkara.net.downloadmanager;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_DOWNLOAD_PATH = "http://gorkemkara.net/android/DownloadManager/DownloadManager.apk" ;
    String DOWNLOAD_PATH = Environment.getExternalStorageDirectory() + "/DownloadManager/Apk/";
    String APK_NAME = "Downloadmanager.apk";
    int downloadedVersionCode;
    int versionCode;
    TextView textView;
    private long enqueue;
    private DownloadManager dm;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            versionCode = BuildConfig.VERSION_CODE;


        textView = (TextView) findViewById(R.id.textView);

        if (!checkAndRequestPermissions()) {

            return;
        }
            new NewVersion().execute();


        }
    private boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int permissionWRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionREQUEST_INSTALL_PACKAGES;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionREQUEST_INSTALL_PACKAGES = ContextCompat.checkSelfPermission(this, Manifest.permission.REQUEST_INSTALL_PACKAGES);
        }

        int permissionREAD_EXTERNAL_STORAGE = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            permissionREAD_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (permissionREAD_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                listPermissionsNeeded.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
            }
        }
        return true;
    }

    private class NewVersion extends AsyncTask<Void,Void,Void> {
        int NewVersion;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            String weatherUrl="http://gorkemkara.net/android/DownloadManager/version.json";
            JSONObject jsonObject=null;
            try {
                String json=JSONParser.getJSONFromUrl(weatherUrl);
                try {
                    jsonObject=new JSONObject(json);
                }catch (JSONException e){
                    Log.e("JSONPARSER", "Error creating Json Object" +e.toString());}

                //En baştaki json objesinden list adlı array'ı çek
                JSONArray listArray=jsonObject.getJSONArray("downloadmanager");
                //list'in ilk objesini çek
                JSONObject firstObj=listArray.getJSONObject(0);
                //Bu alanda Name'i çek
                downloadedVersionCode = Integer.parseInt(firstObj.getString("versioncode"));

            }catch (JSONException e){
                Log.e("json","doINbackgrond");

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            if (downloadedVersionCode > versionCode){
                Log.i("Versiyon", "yeni"+String.valueOf(versionCode));
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Güncelleme indiriliyor...\nLütfen Bekleyiniz!"); // Setting Message
                progressDialog.setTitle("Gorkemkara.net | DownloadManager"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);
                dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(SERVER_DOWNLOAD_PATH));

                File direct = new File(getExternalStorageDirectory()
                        + "/DownloadManager/Apk");



                direct.mkdirs();
                request.setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI
                                | DownloadManager.Request.NETWORK_MOBILE)
                        .setAllowedOverRoaming(true).setTitle("GorkemKARA | DownloadManager")
                        .setDescription("Güncelleniyor...")
                        .setDestinationInExternalPublicDir("/DownloadManager/Apk/", "DownloadManager.apk");
                enqueue = dm.enqueue(request);




                registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            }
            textView.setText("Eski Version: "+ String.valueOf(versionCode)+"\n"+"Yeni Version: "+ String.valueOf(downloadedVersionCode) );
        }

    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            if (enqueue == id) {
                progressDialog.dismiss();
                try { //Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Poi/Apk/Poi.apk")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        /*Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                        promptInstall.setDataAndType(
                                FileProvider.getUriForFile(context, context.getPackageName() + ".provider", new File(Environment.getExternalStorageDirectory() + "/Poi/Apk/Poi.apk")),
                                "application/vnd.android.package-archive");
                        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(promptInstall);
                        Toast.makeText(LoginActivity.this, "İndirme Tamamlandı.", Toast.LENGTH_SHORT).show();
*/
                        Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(DOWNLOAD_PATH + APK_NAME));
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        intent1.setDataAndType(apkUri, "application/vnd.android.package-archive");
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent1);
                        Toast.makeText(MainActivity.this, "İndirme Tamamlandı.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                        promptInstall.setDataAndType(
                                Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/DownloadManager/Apk/DownloadManager.apk")),
                                "application/vnd.android.package-archive");
                        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(promptInstall);
                        Toast.makeText(MainActivity.this, "İndirme Tamamlandı.", Toast.LENGTH_SHORT).show();

                    }
                }
                catch ( ActivityNotFoundException e ) {
                    e.printStackTrace();

                    //Open the generic Apps page:
                    Intent intent1 = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    startActivity(intent1);
                }
            }
        }
    };
}
