package com.mindorks.tensorflowexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebviewActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = (WebView) findViewById(R.id.activity_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setWebViewClient(
                new WebViewClient()
                {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url){
                        view.loadUrl(url);
                        return true;
                    }
                }
        );
        Intent intent = new Intent(this.getIntent());
        String link = intent.getStringExtra("link");
        String result = intent.getStringExtra("result");
        String postData = "searchExpression="+result;
        mWebView.postUrl(link, postData.getBytes());
        //mWebView.postUrl("http://m.kipris.or.kr/mobile/search/search_design.do", postData.getBytes());
    }
}
