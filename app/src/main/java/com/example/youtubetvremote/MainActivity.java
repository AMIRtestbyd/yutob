package com.example.youtubetvremote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextView hud;
    private ProgressBar loading;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean fullScreenVideo = false;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private static final String HOME = "https://www.youtube.com/";
    private static final String UA = "Mozilla/5.0 (Linux; Android 14; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 YouTubeTVRemote/2.0";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        hud = findViewById(R.id.hud);
        loading = findViewById(R.id.loading);

        configureWebView();
        webView.addJavascriptInterface(new Bridge(), "AndroidTVBridge");
        webView.loadUrl(HOME);
        webView.requestFocus();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        webView.setBackgroundColor(Color.BLACK);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(true);
        s.setUserAgentString(UA);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setTextZoom(100);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                loading.setVisibility(newProgress < 95 ? View.VISIBLE : View.GONE);
            }
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullScreenVideo) { callback.onCustomViewHidden(); return; }
                customView = view;
                customViewCallback = callback;
                ((FrameLayout) findViewById(R.id.root)).addView(view, new FrameLayout.LayoutParams(-1, -1));
                webView.setVisibility(View.GONE);
                fullScreenVideo = true;
            }
            @Override public void onHideCustomView() {
                exitFullscreenVideo();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                String host = u.getHost() == null ? "" : u.getHost().toLowerCase(Locale.US);
                boolean internal = host.endsWith("youtube.com") || host.endsWith("youtu.be") ||
                        host.endsWith("youtube-nocookie.com") || host.endsWith("google.com") ||
                        host.endsWith("googleusercontent.com");
                return !internal;
            }
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                handler.postDelayed(MainActivity.this::injectTvNavigation, 250);
            }
        });
    }

    private void injectTvNavigation() {
        String js = "javascript:(function(){"
                + "if(window.__ytTvV2){window.__ytTvRefresh&&window.__ytTvRefresh();return;}"
                + "window.__ytTvV2=true;"
                + "var css=document.createElement('style');css.textContent='"
                + ":focus{outline:0!important} .yt-tv-focus{outline:4px solid #fff!important;outline-offset:3px!important;box-shadow:0 0 0 2px rgba(255,0,0,.65)!important}"
                + "';document.head&&document.head.appendChild(css);"
                + "function visible(e){if(!e||e===document.body)return false;var r=e.getBoundingClientRect(),s=getComputedStyle(e);return r.width>4&&r.height>4&&r.bottom>0&&r.right>0&&r.top<innerHeight&&r.left<innerWidth&&s.display!=='none'&&s.visibility!=='hidden'&&s.opacity!=='0'&&e.offsetParent!==null;}"
                + "function clickable(e){if(!visible(e))return false;var t=(e.tagName||'').toLowerCase();return t==='a'||t==='button'||t==='input'||t==='textarea'||e.getAttribute('role')==='button'||e.getAttribute('role')==='tab'||e.getAttribute('role')==='menuitem'||e.hasAttribute('tabindex')||e.closest('a,button,[role=button]');}"
                + "function all(){var q='a,button,input,textarea,[role=button],[role=tab],[role=menuitem],[tabindex]';return Array.from(document.querySelectorAll(q)).filter(clickable);}"
                + "function clean(){document.querySelectorAll('.yt-tv-focus').forEach(function(e){e.classList.remove('yt-tv-focus');});}"
                + "function focus(e){if(!e)return;clean();e.classList.add('yt-tv-focus');e.setAttribute('tabindex','0');try{e.focus({preventScroll:true});}catch(_){try{e.focus()}catch(__){}};e.scrollIntoView({block:'nearest',inline:'nearest'});}"
                + "function current(){var a=document.activeElement;if(clickable(a))return a;var b=document.querySelector('.yt-tv-focus');if(clickable(b))return b;var c=all();if(!c.length)return null;var midX=innerWidth/2,midY=innerHeight/2;c.sort(function(x,y){var rx=x.getBoundingClientRect(),ry=y.getBoundingClientRect();return Math.hypot((rx.left+rx.width/2)-midX,(rx.top+rx.height/2)-midY)-Math.hypot((ry.left+ry.width/2)-midX,(ry.top+ry.height/2)-midY)});return c[0];}"
                + "function move(dir){var a=all(),cur=current();if(!a.length)return false;if(!cur){focus(a[0]);return true;}var cr=cur.getBoundingClientRect(),cx=cr.left+cr.width/2,cy=cr.top+cr.height/2,best=null,score=1e18;"
                + "a.forEach(function(e){if(e===cur)return;var r=e.getBoundingClientRect(),x=r.left+r.width/2,y=r.top+r.height/2,dx=x-cx,dy=y-cy;var primary=(dir==='left'||dir==='right')?dx:dy;var secondary=(dir==='left'||dir==='right')?dy:dx;var ok=dir==='left'?primary<-6:dir==='right'?primary>6:dir==='up'?primary<-6:primary>6;if(!ok)return;var distance=Math.abs(primary)+Math.abs(secondary)*1.8;if(distance<score){score=distance;best=e;}});if(best){focus(best);return true;}return false;}"
                + "window.__ytTvMove=move;"
                + "window.__ytTvClick=function(){var e=current();if(!e)return false;if(e.tagName==='INPUT'||e.tagName==='TEXTAREA'){try{e.focus();return true}catch(_){}}try{e.click();return true}catch(_){return false}};"
                + "window.__ytTvRefresh=function(){var e=current();if(e)focus(e);};"
                + "window.__ytTvHasVideo=function(){var v=Array.from(document.querySelectorAll('video')).find(function(x){return visible(x)&&x.readyState>=2;});return !!v;};"
                + "window.__ytTvVideo=function(){return Array.from(document.querySelectorAll('video')).find(function(x){return visible(x)&&x.readyState>=2;})||null;};"
                + "window.__ytTvSeek=function(d){var v=window.__ytTvVideo();if(!v)return false;if(!isFinite(v.duration))return false;v.currentTime=Math.max(0,Math.min(v.duration,v.currentTime+d));return true;};"
                + "window.__ytTvPlayPause=function(){var v=window.__ytTvVideo();if(!v)return false;if(v.paused)v.play();else v.pause();return true;};"
                + "window.__ytTvTime=function(){var v=window.__ytTvVideo();if(!v)return null;return JSON.stringify({t:v.currentTime||0,d:v.duration||0,p:v.paused});};"
                + "})();";
        webView.evaluateJavascript(js, null);
    }

    private void js(String script) {
        webView.post(() -> webView.evaluateJavascript("javascript:(function(){try{" + script + "}catch(e){}})();", null));
    }

    private void showHud(final String text) {
        hud.setText(text);
        hud.setVisibility(View.VISIBLE);
        handler.removeCallbacksAndMessages(hud);
        Runnable hide = () -> hud.setVisibility(View.GONE);
        hud.setTag(hide);
        handler.postAtTime(hide, hud, android.os.SystemClock.uptimeMillis() + 900);
    }

    private void seek(int seconds) {
        webView.evaluateJavascript("window.__ytTvSeek && window.__ytTvSeek(" + seconds + ")", value -> {
            if (!"true".equals(value)) {
                js("window.__ytTvMove && window.__ytTvMove(" + (seconds < 0 ? "'left'" : "'right'") + ")");
                return;
            }
            showHud((seconds < 0 ? "↶  " : "↷  ") + Math.abs(seconds) + " ثانیه");
        });
    }

    private void exitFullscreenVideo() {
        if (!fullScreenVideo) return;
        ((FrameLayout) findViewById(R.id.root)).removeView(customView);
        customView = null;
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        webView.setVisibility(View.VISIBLE);
        fullScreenVideo = false;
        webView.requestFocus();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event);
        if (fullScreenVideo && event.getKeyCode() == KeyEvent.KEYCODE_BACK) { exitFullscreenVideo(); return true; }

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                seek(-10); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                seek(10); return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                js("window.__ytTvMove&&window.__ytTvMove('up')"); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                js("window.__ytTvMove&&window.__ytTvMove('down')"); return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                js("window.__ytTvClick&&window.__ytTvClick() && AndroidTVBridge.onClick()"); return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_SPACE:
                js("window.__ytTvPlayPause&&window.__ytTvPlayPause() && AndroidTVBridge.onPlayPause()"); return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                js("var v=window.__ytTvVideo&&window.__ytTvVideo();if(v)v.play();AndroidTVBridge.onPlayPause()"); return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                js("var v=window.__ytTvVideo&&window.__ytTvVideo();if(v)v.pause();AndroidTVBridge.onPlayPause()"); return true;
            case KeyEvent.KEYCODE_BACK:
                if (webView.canGoBack()) { webView.goBack(); return true; }
                finish(); return true;
            default: return super.dispatchKeyEvent(event);
        }
    }

    @Override public void onBackPressed() {
        if (fullScreenVideo) { exitFullscreenVideo(); return; }
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    @Override protected void onResume() { super.onResume(); if (webView != null) webView.onResume(); }
    @Override protected void onPause() { if (webView != null) webView.onPause(); super.onPause(); }
    @Override protected void onDestroy() { if (webView != null) { webView.loadUrl("about:blank"); webView.removeAllViews(); webView.destroy(); } handler.removeCallbacksAndMessages(null); super.onDestroy(); }

    public class Bridge {
        @android.webkit.JavascriptInterface public void onClick() { }
        @android.webkit.JavascriptInterface public void onPlayPause() { }
    }
}
