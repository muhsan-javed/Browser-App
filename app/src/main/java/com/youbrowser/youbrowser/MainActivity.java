package com.youbrowser.youbrowser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.youbrowser.youbrowser.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private String webUrl = "https://google.com/";
    ActivityMainBinding binding;
    ProgressDialog progressDialog; // This Progress Dialog show Beale UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //if () // Testing
        //getSupportActionBar().setTitle();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);// Show FullScreen

        binding.myWebView.getSettings().setJavaScriptEnabled(true);
        checkConnection();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Please Wait");

        binding.swipeRefreshLayout.setColorSchemeColors(Color.BLUE,Color.YELLOW,Color.GREEN);
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.myWebView.reload();
            }
        });
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

        binding.btnNoInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();
            }
        });

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
            exitAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishAffinity();
                }
            }).show();
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

        }
        else if (mobileNetwork.isConnected()){
            binding.myWebView.loadUrl(webUrl);
            binding.myWebView.setVisibility(View.VISIBLE);
            binding.relativeLayout.setVisibility(View.GONE);

        }
        else {
            binding.myWebView.setVisibility(View.GONE);
            binding.relativeLayout.setVisibility(View.VISIBLE);
//            setTitle("Tun On the Internet"); // Testing

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
}