package com.sleepingbear.pvhdictandvoc;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class NewsWebViewActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private TextToSpeech myTTS;

    public SQLiteDatabase mDb;
    public ArrayAdapter urlAdapter;
    private WebView webView;
    private TextView mean;
    private RelativeLayout meanRl;
    private Bundle param;
    private String oldUrl = "";
    private String entryId = "";
    public int mSelect = 0;
    public int m2Select = 0;
    private String clickWord;

    private ImageButton addBtn;
    private ImageButton searchBtn;

    private ProgressDialog mProgress;

    private ActionMode mActionMode = null;

    private final Handler handler = new Handler();

    private ArrayList<NewsVo> enUrls;
    private NewsVo currItem;
    private String newsUrl;
    private boolean loadingComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_web_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        myTTS = new TextToSpeech(this, this);

        String js1 =
                ".html(function(index, oldHtml) {" +
                        "var word = oldHtml.replace(/(<br>)/gi, '\\n').replace(/(<([^>]+)>)/gi, '').replace(/(&nbsp;)/gi, '').split(' ');" +
                        "for ( var i = 0; i < word.length; i++ ) {" +
                        "  word[i] = '<span class=\"word\">' + word[i] + '</span>';" +
                        "}" +
                        "return word.join(' ').replace(/(\\n)/gi, '<br>');" +
                        "});";
        String js2 = "('.word').click(function(event) { window.android.setWord(event.target.innerHTML) });";

        /*
function fn(id) {
	var nodes = document.getElementsByClassName(id)[0].childNodes;
	for ( var i = 0; i < nodes.length; i++ ) {
		if ( nodes[i].tagName == "P" ) {
			words = nodes[i].innerHTML.replace(/(<([^>]+)>)/gi, '').split(" ");
			for ( var m = 0; m < words.length; m++ ) {
				words[m] = '<span onclick=\"alert(111)\">' + words[m] + '</span>';
			}
			nodes[i].innerHTML = words.join(" ");
		}
	}
}
fn("ta-justify");
         */
        enUrls = new ArrayList<>();
        int idx = 1;

        enUrls.add(new NewsVo("E" + idx++, "Youth Newspaper","http://tuoitre.vn/",
                new String[]{
                        "$('div.fck h1')" + js1 + "$" + js2,
                        "$('div.fck p')" + js1 + "$" + js2
                },
                new String[]{

                },
                "$('h1.title-2 a').text()",
                "$('div.fck p').text()"));
        enUrls.add(new NewsVo("E" + idx++, "Nhan Dan Newspaper","http://www.nhandan.org.vn/",
                new String[]{
                        "$('div.ndtitle h3')" + js1 + "$" + js2,
                        "$('div.ndcontent p')" + js1 + "$" + js2
                },
                new String[]{

                },
                "$('div.ndtitle h3').text()",
                "$('div.ndcontent p').text()"));
        enUrls.add(new NewsVo("E" + idx++, "Lao Dong Newspaper","http://m.laodong.com.vn/",
                new String[]{
                        "$('h1.article-title')" + js1 + "$" + js2,
                        "$('div.summary')" + js1 + "$" + js2,
                        "$('div p')" + js1 + "$" + js2
                },
                new String[]{

                },
                "$('h1.article-title').text()",
                "$('div#aka_divfirst p').text()"));
        enUrls.add(new NewsVo("E" + idx++, "Vietnam News express","http://vnexpress.net/",
                new String[]{
                        "$('div.title_news h1')" + js1 + "$" + js2,
                        "$('div.short_intro')" + js1 + "$" + js2,
                        "$('div p.Normal')" + js1 + "$" + js2
                },
                new String[]{

                },
                "$('.title_news h1').text()",
                "$('.fck_detail p').text()"));
        enUrls.add(new NewsVo("E" + idx++, "Vietnam.net","http://m.vietnamnet.vn/",
                new String[]{
                        "jQuery('div.article-detail h1')" + js1 + "$" + js2,
                        "jQuery('div p')" + js1 + "$" + js2
                },
                new String[]{
                },
                "jQuery('div.article-detail h1').text()",
                "jQuery('p').text()"));

        String currUrl = "";
        param = getIntent().getExtras();

        for ( int i = 0; i < enUrls.size(); i++ ) {
            DicUtils.dicLog(enUrls.get(i).getKind() + " : " + param.getString("kind"));
            if ( enUrls.get(i).getKind().equals(param.getString("kind")) ) {
                currItem = enUrls.get(i);
                currUrl = currItem.getUrl();
                break;
            }
        }

        if ( !"".equals(DicUtils.getString(param.getString("url"))) ) {
            DicUtils.dicLog("url param");

            if ( currUrl.indexOf("https") > -1 ) {
                currUrl = param.getString("url");
            } else {
                currUrl = param.getString("url");
            }
        }

        DicUtils.dicLog(currUrl);

        ActionBar ab = getSupportActionBar();
        //ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setTitle(currItem.getName());
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        mDb = (new DbHelper(this)).getWritableDatabase();

        //하단 뜻 영역을 숨김
        meanRl = (RelativeLayout) this.findViewById(R.id.my_c_webview_rl);
        meanRl.setVisibility(View.GONE);
        meanRl.setClickable(true);  //클릭시 하단 광고가 클릭되는 문제로 rl이 클릭이 되게 해준다.

        //버튼 설정
        addBtn = (ImageButton) this.findViewById(R.id.my_c_webview_ib_add);
        searchBtn = (ImageButton) this.findViewById(R.id.my_c_webview_ib_search);
        searchBtn.setVisibility(View.GONE);

        //뜻 롱클릭시 단어 상세 보기
        mean = (TextView) this.findViewById(R.id.my_c_webview_mean);
        mean.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(getApplication(), WordViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("entryId", entryId);
                intent.putExtras(bundle);

                startActivity(intent);

                return false;
            }
        });

        this.findViewById(R.id.my_c_webview_ib_add).setOnClickListener(this);
        this.findViewById(R.id.my_c_webview_ib_search).setOnClickListener(this);

        webView = (WebView) this.findViewById(R.id.my_c_webview_wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidBridge(), "android");
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        //webView.setContextClickable(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(currUrl);
        DicUtils.dicLog("First : " + currUrl);

        //registerForContextMenu(webView);

        DicUtils.setAdView(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_news_view, menu);

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_newsView);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.action_web_dictionary) {
            Bundle bundle = new Bundle();

            Intent webIntent = new Intent(getApplication(), WebDictionaryActivity.class);
            webIntent.putExtras(bundle);
            startActivity(webIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    myTTS.shutdown();

                    if ( webView.canGoBack() ) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getUrlSource(final String site)  {
        DicUtils.dicLog(site);

        new Thread(new Runnable() {
            public void run() {
                StringBuilder a = new StringBuilder();
                try {
                    //GNU Public, from ZunoZap Web Browser
                    URL url = new URL(site);
                    URLConnection urlc = url.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlc.getInputStream(), "UTF-8"));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        a.append(inputLine);
                        DicUtils.dicLog(inputLine);
                    }
                    in.close();
                } catch ( Exception e ) {
                    DicUtils.dicLog(e.toString());
                }
            }
        }).start();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        DicUtils.dicLog("onActionModeStarted");
        if (mActionMode == null) {
            mActionMode = mode;
            Menu menu = mode.getMenu();

            // Remove the default menu items (select all, copy, paste, search)
            menu.clear();

            // Inflate your own menu items
            mode.getMenuInflater().inflate(R.menu.menu_webview_cm, menu);

            //클릭시 onContextItemSelected를 호출해주도록 이벤트를 걸어준다.
            MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    onContextualMenuItemClicked(item);
                    return true;
                }
            };
            for (int i = 0, n = menu.size(); i < n; i++) {
                menu.getItem(i).setOnMenuItemClickListener(listener);
            }
        }

        super.onActionModeStarted(mode);
    }

    public void onContextualMenuItemClicked(MenuItem item) {
        DicUtils.dicLog("onContextualMenuItemClicked");
        switch (item.getItemId()) {
            case R.id.action_copy:
                webView.loadUrl("javascript:window.android.action('COPY', window.getSelection().toString())");

                break;
            case R.id.action_all_copy:
                webView.loadUrl("javascript:window.android.action('COPY', " + currItem.getBodyClass() + ")");

                break;
            case R.id.action_word_view:
                webView.loadUrl("javascript:window.android.action('WORD', window.getSelection().toString())");

                break;
            case R.id.action_translate:
                webView.loadUrl("javascript:window.android.action('TRANSLATE', window.getSelection().toString())");

                break;
            case R.id.action_word_search:
                webView.loadUrl("javascript:window.android.action('WORD_SEARCH', window.getSelection().toString())");

                break;
            case R.id.action_sentence_view:
                webView.loadUrl("javascript:window.android.action('SENTENCE', window.getSelection().toString())");

                break;
            case R.id.action_tts_all:
                webView.loadUrl("javascript:window.android.action('TTS', " + currItem.getBodyClass() + ")");

                break;
            case R.id.action_tts:
                webView.loadUrl("javascript:window.android.action('TTS', window.getSelection().toString())");

                break;
            default:
                // ...
                break;
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    public void onInit(int status) {
        Locale loc = new Locale("en");

        if (status == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(new Locale("vi", "VN"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        DicUtils.dicLog("onActionModeFinished");
        mActionMode = null;
        super.onActionModeFinished(mode);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_c_webview_ib_add ) {
            //메뉴 선택 다이얼로그 생성
            Cursor cursor = mDb.rawQuery(DicQuery.getSentenceViewContextMenu(), null);
            final String[] kindCodes = new String[cursor.getCount()];
            final String[] kindCodeNames = new String[cursor.getCount()];

            int idx = 0;
            while (cursor.moveToNext()) {
                kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                idx++;
            }
            cursor.close();

            final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("메뉴 선택");
            dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    mSelect = arg1;
                }
            });
            dlg.setNegativeButton("취소", null);
            dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DicDb.insDicVoc(mDb, entryId, kindCodes[mSelect]);
                    DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                    Toast.makeText(getApplicationContext(), "단어장에 등록했습니다. 메인화면의 '단어장' 탭에서 내용을 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            });
            dlg.show();
        } else if ( v.getId() == R.id.my_c_webview_ib_search ) {
            wordSearch();
        }
    }

    public void wordSearch() {
        final String[] kindCodes = new String[]{"Naver","Daum"};

        final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setTitle("검색 사이트 선택");
        dlg.setSingleChoiceItems(kindCodes, m2Select, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                m2Select = arg1;
            }
        });
        dlg.setNegativeButton("취소", null);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = new Bundle();
                bundle.putString("site", kindCodes[m2Select]);
                bundle.putString("word", clickWord);

                Intent intent = new Intent(getApplication(), WebDictionaryActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        dlg.show();
    }

    private class NewsVo {
        private String kind;
        private String name;
        private String url;
        private String[] changeClass;
        private String[] removeClass;
        private String titleClass;
        private String bodyClass;

        public NewsVo(String kind, String name, String url, String[] changeClass, String[] removeClass, String titleClass, String bodyClass) {
            this.kind = kind;
            this.name = name;
            this.url = url;
            this.changeClass = changeClass;
            this.removeClass = removeClass;
            this.titleClass = titleClass;
            this.bodyClass = bodyClass;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String[] getChangeClass() {
            return changeClass;
        }

        public void setChangeClass(String[] changeClass) {
            this.changeClass = changeClass;
        }

        public String[] getRemoveClass() {
            return removeClass;
        }

        public void setRemoveClass(String[] removeClass) {
            this.removeClass = removeClass;
        }

        public String getTitleClass() {
            return titleClass;
        }

        public void setTitleClass(String titleClass) {
            this.titleClass = titleClass;
        }

        public String getBodyClass() {
            return bodyClass;
        }

        public void setBodyClass(String bodyClass) {
            this.bodyClass = bodyClass;
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //return super.shouldOverrideUrlLoading(view, url);

            //The New Work Times 에서 다음 url을 호출할때 화면이 안나오는 문제가 있음
            if ( "data:text/html,".equals(url) ) {
                return false;
            } else {
                DicUtils.dicLog("url = " + url);
                //getUrlSource(url);

                view.loadUrl(url);

                return true;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            loadingComplete = false;

            if (mProgress == null) {
                mProgress = new ProgressDialog(NewsWebViewActivity.this);
                mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgress.setMessage("페이지 로딩 및 변환 중입니다.\n로딩이 완료후 단어를 클릭하시면 뜻을 보실 수 있습니다.\n" +
                        "해외사이트는 로딩이 오래 걸립니다.");
                mProgress.setCancelable(false);
                mProgress.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mProgress.dismiss();
                        mProgress = null;
                    }
                });
                mProgress.show();
            }

            DicUtils.dicLog("onPageStarted : " + url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
                mProgress = null;
            }

            DicUtils.dicLog("onReceivedError : " + error.toString());
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            DicUtils.dicLog("onPageFinished : " + url);

            //중복으로 호출이 되지 않도록
            if ( oldUrl.equals(url) ) {
                DicUtils.dicLog("중복호출");
                return;
            } else {
                if ( !"data:text/html,".equals(url) ) {
                    oldUrl = url;
                    DicUtils.dicLog("onPageFinished : " + url);

                    //html 단어 기능 변경
                    String[] changeClass = currItem.getChangeClass();
                    for (int i = 0; i < changeClass.length; i++) {
                        webView.loadUrl("javascript:" + changeClass[i]);
                        DicUtils.dicLog("javascript:" + changeClass[i]);
                    }

                    //광고 제거
                    String[] removeClass = currItem.getRemoveClass();
                    for (int i = 0; i < removeClass.length; i++) {
                        webView.loadUrl("javascript:" + removeClass[i]);
                        DicUtils.dicLog("javascript:" + removeClass[i]);
                    }

                    //webView.loadUrl("javascript:window.android.action('URL', window.location.href)");
                    newsUrl = url;

                    if (mProgress != null && mProgress.isShowing()) {
                        mProgress.dismiss();
                        mProgress = null;
                    }

                    loadingComplete = true;

                    Toast.makeText(getApplicationContext(), "로딩이 완료되었습니다.\n단어를 클릭하시면 단어뜻을 보실 수 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setWord(final String arg) { // must be final
            handler.post(new Runnable() {
                public void run() {
                    meanRl.setVisibility(View.VISIBLE);

                    clickWord = arg.replaceAll("[,.]", "");

                    HashMap info = DicDb.getMean(mDb, clickWord);
                    mean.setText(clickWord + " " + DicUtils.getString((String)info.get("SPELLING")) + " : " + DicUtils.getString((String)info.get("MEAN")));

                    entryId = DicUtils.getString((String)info.get("ENTRY_ID"));
                    if ( !"".equals(entryId) ) {
                        DicDb.insDicClickWord(mDb, entryId, "");

                        DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                        addBtn.setVisibility(View.VISIBLE);
                        searchBtn.setVisibility(View.GONE);
                    } else {
                        addBtn.setVisibility(View.GONE);
                        searchBtn.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        @JavascriptInterface
        public void action(final String kind, final String arg) { // must be final
            handler.post(new Runnable() {
                public void run() {
                    final String chgArg = arg.replaceAll("[\"',.:-]","");
                    DicUtils.dicLog(chgArg);
                    if ( "COPY".equals(kind) ) {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("simple text", chgArg);
                        clipboard.setPrimaryClip(clip);
                    } else if ( "WORD".equals(kind) ) {
                        HashMap info = DicDb.getMean(mDb, chgArg);

                        if ( info.containsKey("ENTRY_ID") ) {
                            Intent intent = new Intent(getApplication(), WordViewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("entryId", (String) info.get("ENTRY_ID"));
                            intent.putExtras(bundle);

                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "등록된 단어가 아닙니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else if ( "WORD_SEARCH".equals(kind) ) {
                        clickWord = chgArg;
                        wordSearch();
                    } else if ( "SENTENCE".equals(kind) ) {
                        Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("foreign", chgArg);
                        bundle.putString("han", "");
                        intent.putExtras(bundle);

                        startActivity(intent);
                    } else if ( "TTS".equals(kind) ) {
                        if ( chgArg.length() > 4000 ) {
                            Toast.makeText(getApplicationContext(), "TTS는 4,000자 까지만 가능합니다.", Toast.LENGTH_SHORT).show();
                            myTTS.speak(chgArg.substring(0, 3900), TextToSpeech.QUEUE_FLUSH, null);
                        } else {
                            myTTS.speak(chgArg, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    /*
                    } else if ( "URL".equals(kind) ) {
                        if ( chgArg.indexOf("http://") > -1 ) {
                            newsUrl = chgArg.substring(7, chgArg.length());
                        } else {
                            newsUrl = chgArg.substring(8, chgArg.length());
                        }
                        DicUtils.dicLog("URL : " + newsUrl);
                    */
                    } else if ( "TRANSLATE".equals(kind) ) {
                        //클립보드에 복사
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("simple text", chgArg);
                        clipboard.setPrimaryClip(clip);

                        Bundle bundle = new Bundle();
                        bundle.putString("site", "Google");
                        bundle.putString("sentence", chgArg);

                        Intent intent = new Intent(getApplication(), WebTranslateActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                }
            });
        }
    }
}


