package br.com.improveengenhariasst.online;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Improve SST Online - Android.
 * Portal PHP/MySQL mantido na hospedagem. Nenhum dado é copiado para dentro do APK.
 * O WebView só navega internamente no domínio HTTPS configurado.
 */
public class MainActivity extends Activity {
    private static final String HOME_URL = "https://sistema.improveengenhariasst.com.br/";
    private static final String ALLOWED_HOST = "sistema.improveengenhariasst.com.br";
    private static final int PICK_FILE_REQUEST = 5421;

    private WebView webView;
    private ProgressBar loadProgress;
    private LinearLayout offlineView;
    private ValueCallback<Uri[]> uploadCallback;
    private boolean hasRenderedPage = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(18, 59, 86));
        getWindow().setNavigationBarColor(Color.rgb(18, 59, 86));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(239, 245, 248));
        setContentView(root);

        webView = new WebView(this);
        FrameLayout.LayoutParams pageParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        root.addView(webView, pageParams);

        loadProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        loadProgress.setMax(100);
        loadProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(5, 145, 143)));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP);
        root.addView(loadProgress, progressParams);
        loadProgress.setVisibility(View.GONE);

        buildOfflineView(root);
        configureWebView();

        if (state != null) {
            webView.restoreState(state);
        } else {
            webView.loadUrl(HOME_URL);
        }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }

    private void buildOfflineView(FrameLayout root) {
        offlineView = new LinearLayout(this);
        offlineView.setOrientation(LinearLayout.VERTICAL);
        offlineView.setGravity(Gravity.CENTER);
        offlineView.setPadding(dp(24), dp(25), dp(24), dp(25));
        offlineView.setBackgroundColor(Color.rgb(239, 245, 248));

        TextView headline = new TextView(this);
        headline.setText(R.string.error_title);
        headline.setTextColor(Color.rgb(20, 59, 85));
        headline.setTextSize(22);
        headline.setGravity(Gravity.CENTER);
        headline.setTypeface(null, android.graphics.Typeface.BOLD);
        offlineView.addView(headline);

        TextView message = new TextView(this);
        message.setText(R.string.error_message);
        message.setTextColor(Color.rgb(73, 97, 114));
        message.setTextSize(15);
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        messageParams.topMargin = dp(14);
        offlineView.addView(message, messageParams);

        Button retry = new Button(this);
        retry.setText(R.string.retry);
        retry.setOnClickListener(v -> {
            offlineView.setVisibility(View.GONE);
            webView.reload();
        });
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        retryParams.topMargin = dp(20);
        offlineView.addView(retry, retryParams);

        root.addView(offlineView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        offlineView.setVisibility(View.GONE);
    }

    private void configureWebView() {
        WebView.setWebContentsDebuggingEnabled(false);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Exigido pelos filtros/formulários do portal.
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true); // Seleção de anexos via seletor do Android.
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setTextZoom(100);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); // Protege páginas privadas contra cache local.
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) settings.setSafeBrowsingEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setBackgroundColor(Color.WHITE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                return routeUrl(request.getUrl());
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadProgress.setVisibility(View.VISIBLE);
                offlineView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hasRenderedPage = true;
                loadProgress.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    loadProgress.setVisibility(View.GONE);
                    offlineView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Nunca ignorar erros de certificado HTTPS.
                handler.cancel();
                loadProgress.setVisibility(View.GONE);
                offlineView.setVisibility(View.VISIBLE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                loadProgress.setProgress(newProgress);
                if (newProgress >= 100) loadProgress.setVisibility(View.GONE);
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams chooserParams) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                try {
                    Intent picker = chooserParams.createIntent();
                    startActivityForResult(picker, PICK_FILE_REQUEST);
                    return true;
                } catch (ActivityNotFoundException | SecurityException e) {
                    uploadCallback.onReceiveValue(null);
                    uploadCallback = null;
                    Toast.makeText(MainActivity.this,
                        "Nenhum seletor de arquivos disponível no dispositivo.", Toast.LENGTH_LONG).show();
                    return true;
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                startSecureDownload(url, userAgent, contentDisposition, mimeType);
            }
        });
    }

    /** Retorna true quando o aplicativo interceptou a navegação. */
    private boolean routeUrl(Uri uri) {
        if (uri == null) return true;
        String scheme = uri.getScheme();
        if ("https".equalsIgnoreCase(scheme) && isTrustedHost(uri)) return false;
        if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)
                || "tel".equalsIgnoreCase(scheme) || "mailto".equalsIgnoreCase(scheme)) {
            openExternal(uri);
        } else {
            Toast.makeText(this, "Este tipo de link não é compatível com o aplicativo.",
                Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean isTrustedHost(Uri uri) {
        return "https".equalsIgnoreCase(uri.getScheme())
            && ALLOWED_HOST.equalsIgnoreCase(uri.getHost())
            && (uri.getPort() == -1 || uri.getPort() == 443);
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException | SecurityException ex) {
            Toast.makeText(this, "Não foi possível abrir o link.", Toast.LENGTH_LONG).show();
        }
    }

    private void startSecureDownload(String url, String userAgent, String disposition, String mimeType) {
        try {
            Uri uri = Uri.parse(url);
            if (!isTrustedHost(uri)) {
                if ("https".equalsIgnoreCase(uri.getScheme())) openExternal(uri);
                return;
            }
            String name = URLUtil.guessFileName(url, disposition, mimeType);
            // Proteger nome do arquivo no armazenamento, sem permitir caminhos.
            name = name.replaceAll("[^a-zA-Z0-9._() -]", "_");
            if (name.length() > 100) name = name.substring(name.length() - 100);
            if (name.isEmpty()) name = "documento_improve";

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(name);
            request.setDescription("Arquivo do Improve SST Online");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            if (mimeType != null && !mimeType.isEmpty()) request.setMimeType(mimeType);
            String sessionCookie = CookieManager.getInstance().getCookie(url);
            if (sessionCookie != null && !sessionCookie.isEmpty())
                request.addRequestHeader("Cookie", sessionCookie);
            if (userAgent != null && !userAgent.isEmpty())
                request.addRequestHeader("User-Agent", userAgent);
            // Android 10+ não exige permissão de armazenamento para salvar em Downloads.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) throw new IllegalStateException("DownloadManager ausente");
            manager.enqueue(request);
            Toast.makeText(this, "Download iniciado: " + name, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível baixar o arquivo.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && uploadCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            uploadCallback.onReceiveValue(result);
            uploadCallback = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (offlineView.getVisibility() == View.VISIBLE) {
            offlineView.setVisibility(View.GONE);
            webView.loadUrl(HOME_URL);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (uploadCallback != null) {
            uploadCallback.onReceiveValue(null);
            uploadCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
