package br.univesp.aluno;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends Activity {
    private final static int PICKFILE_REQUEST_CODE = 1;
    public WebView mWebView;
    public TextView text;
    public DownloadManager manager;
    public File destinationDir;
    private ValueCallback<Uri[]> mFilePathCallback;
    private SwipeRefreshLayout layoutRefresh;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;



    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == PICKFILE_REQUEST_CODE) {
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            Uri[] resultsArray = new Uri[1];
            if (result == null) result = Uri.EMPTY;
            resultsArray[0] = result;
            mFilePathCallback.onReceiveValue(resultsArray);

        } else return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.main);

        ActionBar actionBar = getActionBar();
        actionBar.setLogo(R.drawable.logounivesp);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        text = (TextView) findViewById(R.id.Text);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setSavePassword(true);

        layoutRefresh = this.findViewById(R.id.tela);

        layoutRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mWebView.getUrl() != null) {
                    mWebView.reload();
                }
                layoutRefresh.setRefreshing(false);
            }
        });

        layoutRefresh.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (mWebView.getScrollY() == 0)
                            layoutRefresh.setEnabled(true);
                        else
                            layoutRefresh.setEnabled(false);

                    }
                });

        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        } else {
            cookieManager.setAcceptCookie(true);
        }

        final Activity activity = this;
        mWebView.setWebChromeClient(new WebChromeClient() {
                                        public void onProgressChanged(WebView view, int progress) {

                                            ProgressBar progbar = (ProgressBar) findViewById(R.id.progBar);

                                            progbar.setVisibility(View.VISIBLE);

                                            progbar.setProgress(progress * 4);

                                            ProgressBar loadingimage = (ProgressBar) findViewById(R.id.progressBar);

                                            loadingimage.setVisibility(View.VISIBLE);
                                            if (progress == 100) {
                                                loadingimage.setVisibility(View.INVISIBLE);
                                                progbar.setVisibility(View.INVISIBLE);

                                            }
                                        }

                                        @Override
                                        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                                            mFilePathCallback = filePathCallback;
                                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                            intent.setType("*/*");
                                            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
                                            // Launch Intent for picking file
                                            return true;
                                        }

                                    }
        );

        //	manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);


        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.equals("https://login.univesp.br/simplesaml/logout.php")) {
                    //      view.loadUrl("https://aluno.univesp.br/");
                    view.loadUrl("https://login.univesp.br/");
                    return true;
                }


                boolean shouldOverride = false;

                if (url.endsWith(".pdf")) {
                    shouldOverride = true;
                    Uri source = Uri.parse(url);


                    DownloadManager.Request request = new DownloadManager.Request(source);
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setMimeType("application/pdf");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, source.getLastPathSegment());


                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                    dm.enqueue(request);
                }
                return shouldOverride;

            }


            @Override
            public void onReceivedError(WebView view, int errcode, String description, String failingUrl) {

                super.onReceivedError(view, errcode, description, failingUrl);
                Toast.makeText(activity, "Ocorreu um erro inesperado, verifique sua conexÃ£o e tente novamente", Toast.LENGTH_LONG).show();

            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setMimeType(mimetype);
                String nome1[] = contentDisposition.split(";");
                String nome2[] = nome1[1].split("=");
                String nome = nome2[1].replace("\"", "");
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nome);
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

					/*Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
					intent.setType("");//any application,any extension
					*/

                Toast.makeText(getApplicationContext(), "Baixando arquivo", Toast.LENGTH_LONG).show();


            }
        });

        //   mWebView.loadUrl("https://aluno.univesp.br/");
        mWebView.loadUrl("https://login.univesp.br/");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && (text.getVisibility() == View.VISIBLE)) {
            text.setVisibility(View.INVISIBLE);
            mWebView.setVisibility(View.VISIBLE);
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }


        // TODO: Implement this method
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Início");
        menu.add(0, 2, 0, "Informações");
        menu.add(0, 3, 0, "Sair");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case 1:
                text.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadUrl("https://login.univesp.br/");
                return true;
            case 2:
                informations();
                return true;
            case 3:
                programClose();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void programClose() {
        finish();
    }

    void informations() {
        mWebView.setVisibility(View.INVISIBLE);
        text.setVisibility(View.VISIBLE);
    }



}




	
