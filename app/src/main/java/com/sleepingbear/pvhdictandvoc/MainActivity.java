package com.sleepingbear.pvhdictandvoc;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        System.out.println("=============================================== App Start ======================================================================");
        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //DB가 새로 생성이 되었으면 이전 데이타를 DB에 넣고 Flag를 N 처리함
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if ( "Y".equals(prefs.getString("db_new", "N")) ) {
            DicUtils.dicLog("backup data import");

            DicUtils.readInfoFromFile(this, db, "");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("db_new", "N");
            editor.commit();
        }

        checkPermission();

        //20일 이전 오늘의 단어 데이타 삭제
        String delDate = DicUtils.getDelimiterDate(DicUtils.getAddDay(DicUtils.getCurrentDate(), -20), ".");
        DicDb.delOldToday(db, delDate);

        //5일 이전 뉴스 데이타 삭제
        delDate = DicUtils.getDelimiterDate(DicUtils.getAddDay(DicUtils.getCurrentDate(), -5), ".");
        DicDb.delOldNews(db, delDate);


        findViewById(R.id.my_b_foreign_dic).setOnClickListener(this);
        findViewById(R.id.my_b_dic_history).setOnClickListener(this);

        findViewById(R.id.my_b_web_translate).setOnClickListener(this);
        findViewById(R.id.my_b_web_dic).setOnClickListener(this);

        findViewById(R.id.my_b_news).setOnClickListener(this);
        findViewById(R.id.my_b_news2).setOnClickListener(this);
        findViewById(R.id.my_b_news_word).setOnClickListener(this);

        findViewById(R.id.my_b_conversation_study).setOnClickListener(this);
        findViewById(R.id.my_b_conv_search).setOnClickListener(this);
        findViewById(R.id.my_b_pattern).setOnClickListener(this);
        findViewById(R.id.my_b_conv_note).setOnClickListener(this);

        findViewById(R.id.my_b_grammar).setOnClickListener(this);
        findViewById(R.id.my_b_vsl).setOnClickListener(this);
        findViewById(R.id.my_b_category).setOnClickListener(this);

        findViewById(R.id.my_b_today).setOnClickListener(this);
        findViewById(R.id.my_b_voc).setOnClickListener(this);
        findViewById(R.id.my_b_voc_study).setOnClickListener(this);

        DicUtils.setAdView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "최고의 베트남어 학습 어플");
            msg.putExtra(Intent.EXTRA_TEXT, "베트남어.. 참 어렵죠? '최고의 베트남어 학습' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.pvhdictandvoc ");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));

            return true;
        } else if (id == R.id.action_patch) {
            startActivity(new Intent(getApplication(), PatchActivity.class));

            return true;
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            Intent helpIntent = new Intent(getApplication(), HelpActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);

            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getApplication(), SettingsActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.my_b_foreign_dic:
                Intent dictionaryIntent = new Intent(getApplication(), DictionaryActivity.class);
                dictionaryIntent.putExtras(bundle);
                startActivity(dictionaryIntent);

                break;
            case R.id.my_b_dic_history:
                Intent dicHistoryIntent = new Intent(getApplication(), DictionaryHistoryActivity.class);
                dicHistoryIntent.putExtras(bundle);
                startActivity(dicHistoryIntent);

                break;
            case R.id.my_b_web_dic:
                Intent webIntent = new Intent(getApplication(), WebDictionaryActivity.class);
                webIntent.putExtras(bundle);
                startActivity(webIntent);

                break;
            case R.id.my_b_web_translate:
                Intent webTranslateIntent = new Intent(getApplication(), WebTranslateActivity.class);
                webTranslateIntent.putExtras(bundle);
                startActivity(webTranslateIntent);

                break;
            case R.id.my_b_news:
                Intent newsIntent = new Intent(getApplication(), NewsActivity.class);
                newsIntent.putExtras(bundle);
                startActivity(newsIntent);

                break;
            case R.id.my_b_news2:
                Intent news2Intent = new Intent(getApplication(), News2Activity.class);
                news2Intent.putExtras(bundle);
                startActivity(news2Intent);

                break;
            case R.id.my_b_news_word:
                Intent newClickWordIntent = new Intent(getApplication(), NewsClickWordActivity.class);
                newClickWordIntent.putExtras(bundle);
                startActivity(newClickWordIntent);

                break;
            case R.id.my_b_conversation_study:
                startActivity(new Intent(getApplication(), ConversationStudyActivity.class));

                break;
            case R.id.my_b_conv_search:
                startActivity(new Intent(getApplication(), ConversationActivity.class));
                break;
            case R.id.my_b_conv_note:
                startActivity(new Intent(getApplication(), ConversationNoteActivity.class));
                break;
            case R.id.my_b_pattern:
                startActivity(new Intent(getApplication(), PatternActivity.class));
                break;
            case R.id.my_b_grammar:
                startActivity(new Intent(getApplication(), GrammarActivity.class));
                break;
            case R.id.my_b_vsl:
                startActivity(new Intent(getApplication(), VslActivity.class));
                break;
            case R.id.my_b_today:
                startActivity(new Intent(getApplication(), TodayActivity.class));
                break;
            case R.id.my_b_category:
                startActivity(new Intent(getApplication(), CategoryActivity.class));
                break;
            case R.id.my_b_voc:
                startActivity(new Intent(getApplication(), VocabularyNoteActivity.class));
                break;
            case R.id.my_b_voc_study:
                startActivity(new Intent(getApplication(), StudyActivity.class));
                break;
        }
    }

    public boolean checkPermission() {
        Log.d(CommConstants.tag, "checkPermission");
        boolean isCheck = false;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            Log.d(CommConstants.tag, "권한 없음");
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                //Toast.makeText(this, "(중요)파일로 내보내기, 가져오기를 하기 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            Log.d(CommConstants.tag, "2222");
        } else {
            Log.d(CommConstants.tag, "권한 있음");
            isCheck = true;
        }

        return isCheck;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(CommConstants.tag, "권한 허가");
                } else {
                    Log.d(CommConstants.tag, "권한 거부");
                    Toast.makeText(this, "파일 권한이 없기 때문에 파일 내보내기, 가져오기를 할 수 없습니다.\n만일 권한 팝업이 안열리면 '다시 묻지 않기'를 선택하셨기 때문입니다.\n어플을 지우고 다시 설치하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        //종료 시점에 변경 사항을 기록한다.
        if ( "Y".equals(DicUtils.getDbChange(getApplicationContext())) ) {
            DicUtils.writeInfoToFile(this, db, "");
            DicUtils.clearDbChange(this);
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();

            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }
}
