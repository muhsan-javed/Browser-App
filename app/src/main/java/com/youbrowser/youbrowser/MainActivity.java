package com.youbrowser.youbrowser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.youbrowser.youbrowser.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    String webUrl = "https://google.com/";
    ActivityMainBinding binding;
    ProgressDialog progressDialog; // This Progress Dialog show Beale UI

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// Show FullScreen

        if (savedInstanceState != null){
            binding.myWebView.restoreState(savedInstanceState);
        }else {
            binding.myWebView.getSettings().setJavaScriptEnabled(true);
            binding.myWebView.getSettings().setLoadWithOverviewMode(true);
            binding.myWebView.getSettings().setUseWideViewPort(true);
            binding.myWebView.getSettings().setDomStorageEnabled(true);
            binding.myWebView.getSettings().setLoadsImagesAutomatically(true);
            checkConnection();
        }
        // ADD Downloading Function
        // s = URL   ,  s1 = UserAgent ,  s2 = contentDisposition  ,  s3 = mime type  , l = contentLength
        binding.myWebView.setDownloadListener((s, s1, s2, s3, l) -> Dexter.withContext(MainActivity.this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(s));
                        request.setMimeType(s3);
                        String cookies = CookieManager.getInstance().getCookie(s);
                        request.addRequestHeader("cookie",cookies);
                        request.addRequestHeader("User-Agent",s1);
                        request.setDescription("Downloading File....");
                        request.setTitle(URLUtil.guessFileName(s,s2,s3));
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(s,s2,s3));

                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);
                        Toast.makeText(MainActivity.this, "Downloading....", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Please Wait");

        binding.swipeRefreshLayout.setColorSchemeColors(Color.BLUE,Color.YELLOW,Color.GREEN);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> binding.myWebView.reload());
        // Open website your app
        binding.myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                binding.swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        binding.myWebView.setWebChromeClient( new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBarWeb.setVisibility(View.VISIBLE);
                binding.progressBarWeb.setProgress(newProgress);
                setTitle("Loading...");
                progressDialog.show();
                if (newProgress == 100){
                    binding.progressBarWeb.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                    progressDialog.dismiss();
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        binding.btnNoInternetConnection.setOnClickListener(view -> checkConnection());

    }

    @Override
    public void onBackPressed() {
        if (binding.myWebView.canGoBack()){
            binding.myWebView.goBack();
        }else {
            AlertDialog.Builder exitAlertDialog = new AlertDialog.Builder(this);
            exitAlertDialog.setIcon(R.drawable.ic_baseline_exit_to_app_24);
            exitAlertDialog.setMessage(R.string.exitAlertDialog);
            exitAlertDialog.setTitle("Exit");
            exitAlertDialog.setNegativeButton("No",null);
            exitAlertDialog.setPositiveButton("Yes", (dialogInterface, i) -> finishAffinity()).show();
        }
    }

    public void checkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()){
            binding.myWebView.loadUrl(webUrl);
            binding.myWebView.setVisibility(View.VISIBLE);
            binding.relativeLayout.setVisibility(View.GONE);
        }else if (mobileNetwork.isConnected()){
            binding.myWebView.loadUrl(webUrl);
            binding.myWebView.setVisibility(View.VISIBLE);
            binding.relativeLayout.setVisibility(View.GONE);
        }else {
            binding.myWebView.setVisibility(View.GONE);
            binding.relativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int getId = item.getItemId();
        switch(getId){
            case R.id.main_menu_previous:
                onBackPressed();
                break;
            case R.id.main_menu_next:
                if (binding.myWebView.canGoForward()){
                    binding.myWebView.goForward();
                }
                break;
            case R.id.main_menu_reload:
                checkConnection();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        binding.myWebView.saveState(outState);
    }
}