package com.sleepingbear.pvhdictandvoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

public class DictionaryActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private EditText et_search;
    private DictionaryActivityCursorAdapter adapter;
    private Cursor cursor;
    private DictionaryActivitySearchTask task;
    private boolean isEmptyCondition;
    private int dSelect = 0;
    private int mSelect = 0;
    private int webDictionaryIdx = 0;
    private TextToSpeech myTTS;

    private String mWsKind = "W";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        dbHelper = new DbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();

        myTTS = new TextToSpeech(this, this);

        et_search = (EditText) findViewById(R.id.my_et_search);
        et_search.addTextChangedListener(textWatcherInput);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    changeListView(true);
                }

                return false;
            }
        });

        ImageView iv_clear = (ImageView)findViewById(R.id.my_iv_clear);
        iv_clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                et_search.setText("");
                et_search.requestFocus();

                //키보드 보이게 하는 부분
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        if ( !"".equals(b.getString("word")) ) {
            et_search.setText(b.getString("word"));
            changeListView(true);
        } else {
            //키보드 보이게 하는 부분
            et_search.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            changeListView(false);
        }

        findViewById(R.id.my_rb_word).setOnClickListener(this);
        findViewById(R.id.my_rb_sentence).setOnClickListener(this);
        findViewById(R.id.my_cb_tone).setOnClickListener(this);
        findViewById(R.id.my_iv_web).setOnClickListener(this);
        findViewById(R.id.my_dictionary_rl_web).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //소프트 키보드 없애기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_dictionary);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView(boolean isKeyin) {
        if ( isKeyin ) {
            findViewById(R.id.my_dictionary_rl_msg).setVisibility(View.GONE);

            if (task != null) {
                return;
            }

            isEmptyCondition = false;

            task = new DictionaryActivitySearchTask();
            task.execute();
        }
    }

    public void getData() {
        StringBuffer sql = new StringBuffer();
        String searchText = et_search.getText().toString().trim().toLowerCase();
        if ( "".equals(searchText) ) {
            isEmptyCondition = true;
        } else {
            if ( "W".equals(mWsKind) ) {
                if (searchText.indexOf(" ") < 0) {
                    if (((CheckBox) findViewById(R.id.my_cb_tone)).isChecked()) {
                        sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN, KIND, ORD" + CommConstants.sqlCR);
                        sql.append("  FROM DIC" + CommConstants.sqlCR);
                        sql.append(" WHERE 1 = 1" + CommConstants.sqlCR);
                        if (!"".equals(et_search.getText().toString())) {
                            sql.append(" AND WORD LIKE '%" + et_search.getText().toString() + "%'" + CommConstants.sqlCR);
                        }
                        sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
                    } else {
                        sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN, KIND, ORD" + CommConstants.sqlCR);
                        sql.append("  FROM DIC" + CommConstants.sqlCR);
                        sql.append(" WHERE 1 = 1" + CommConstants.sqlCR);
                        if (!"".equals(et_search.getText().toString())) {
                            sql.append(" AND WORD_ENG LIKE '%" + DicUtils.getEngString(et_search.getText().toString()) + "%'" + CommConstants.sqlCR);
                        }
                        sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
                    }
                } else {
                    // 여러 단어일때...
                    String word = "";
                    String[] splitStr = DicUtils.sentenceSplit(et_search.getText().toString().replaceAll("'", ""));
                    for (int m = 0; m < splitStr.length; m++) {
                        if (" ".equals(splitStr[m]) || "".equals(splitStr[m])) {
                            continue;
                        }
                        // 3 단어
                        word += DicUtils.getSentenceWord(splitStr, 3, m) + ",";
                        // 2 단어
                        word += DicUtils.getSentenceWord(splitStr, 2, m) + ",";
                        // 1 단어
                        word += DicUtils.getSentenceWord(splitStr, 1, m) + ",";
                    }
                    if (((CheckBox) findViewById(R.id.my_cb_tone)).isChecked()) {
                        sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN, KIND, ORD FROM DIC " + CommConstants.sqlCR);
                        sql.append(" WHERE WORD IN ('" + word.toLowerCase().replaceAll(",", "','") + "')" + CommConstants.sqlCR);
                        sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
                    } else {
                        word = DicUtils.getEngString(word);
                        sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN, KIND, ORD FROM DIC " + CommConstants.sqlCR);
                        sql.append(" WHERE WORD_ENG IN ('" + word.toLowerCase().replaceAll(",", "','") + "')" + CommConstants.sqlCR);
                        sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
                    }
                }
            } else {
                if (((CheckBox) findViewById(R.id.my_cb_tone)).isChecked()) {
                    String word = et_search.getText().toString().replaceAll(" ","%");

                    sql.append("SELECT SEQ _id, SENTENCE1, SENTENCE2, 'SAMPLE' KIND" + CommConstants.sqlCR);
                    sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
                    sql.append(" WHERE ( SENTENCE1 LIKE '%" + word + "%' OR SENTENCE2 LIKE '%" + word + "%' )"  + CommConstants.sqlCR);
                    sql.append(" ORDER BY SENTENCE2" + CommConstants.sqlCR);
                } else {
                    String word = DicUtils.getEngString(et_search.getText().toString().replaceAll(" ","%"));

                    sql.append("SELECT SEQ _id, SENTENCE1, SENTENCE2, 'SAMPLE' KIND" + CommConstants.sqlCR);
                    sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
                    sql.append(" WHERE ( SENTENCE1_ENG LIKE '%" + word + "%' OR SENTENCE2 LIKE '%" + word + "%' )"  + CommConstants.sqlCR);
                    sql.append(" ORDER BY SENTENCE2" + CommConstants.sqlCR);
                }
            }
            DicUtils.dicLog(sql.toString());

            cursor = db.rawQuery(sql.toString(), null);

            //결과가 나올때까지 기달리게 할려고 다음 로직을 추가한다. 안하면 progressbar가 사라짐.. cursor도  Thread 방식으로 돌아가나봄
            if ( cursor.getCount() != 0 ) {
                DicDb.insSearchHistory(db, et_search.getText().toString().trim().toLowerCase());
                DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
            }
        }
    }

    public void setListView() {
        if ( isEmptyCondition ) {
            //Toast.makeText(this, "검색할 단어를 입력하세요.", Toast.LENGTH_SHORT).show();
            et_search.requestFocus();

            //키보드 보이게 하는 부분
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            if (cursor.getCount() == 0) {
                Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                findViewById(R.id.my_dictionary_rl_web).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.my_dictionary_rl_web).setVisibility(View.GONE);
            }

            ListView dictionaryListView = (ListView) findViewById(R.id.my_lv);
            adapter = new DictionaryActivityCursorAdapter(this, cursor, 0, mWsKind);
            dictionaryListView.setAdapter(adapter);

            dictionaryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            dictionaryListView.setOnItemClickListener(itemClickListener);
            dictionaryListView.setOnItemLongClickListener(itemLongClickListener);
            dictionaryListView.setSelection(0);

            //소프트 키보드 없애기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
        }
    }

    /**
     * 단어가 선택되면은 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DicUtils.dicLog("onItemClick");

            Cursor cur = (Cursor) adapter.getItem(position);

            if ( "SAMPLE".equals(cur.getString(cur.getColumnIndexOrThrow("KIND"))) ) {
                Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("foreign", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
                bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));
                bundle.putString("sampleSeq", cur.getString(cur.getColumnIndexOrThrow("_id")));
                intent.putExtras(bundle);

                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplication(), WordViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("entryId", cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID")));
                bundle.putString("seq", cur.getString(cur.getColumnIndexOrThrow("_id")));
                intent.putExtras(bundle);

                startActivity(intent);
            }
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            DicUtils.dicLog("onItemLongClick");

            Cursor cur = (Cursor) adapter.getItem(i);

            if ( "SAMPLE".equals(cur.getString(cur.getColumnIndexOrThrow("KIND"))) ) {
                final String sampleSeq = cur.getString(cur.getColumnIndexOrThrow("_id"));
                final String foreign = cur.getString(cur.getColumnIndexOrThrow("SENTENCE1"));
                final String han = cur.getString(cur.getColumnIndexOrThrow("SENTENCE2"));

                //메뉴 선택 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getNoteKindContextMenu(false), null);
                final String[] kindCodes = new String[cursor.getCount()];
                final String[] kindCodeNames = new String[cursor.getCount()];

                int idx = 0;
                while (cursor.moveToNext()) {
                    kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                    kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                    idx++;
                }
                cursor.close();

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(DictionaryActivity.this);
                dlg.setTitle("메뉴 선택");
                dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSelect = arg1;
                    }
                });
                dlg.setNeutralButton("TTS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myTTS.speak(foreign, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DicDb.insConversationToNote(db, kindCodes[mSelect], sampleSeq);
                        DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                    }
                });
                dlg.show();
            } else {
                //단어장 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getVocabularyCategory(), null);

                final String[] kindCodes = new String[cursor.getCount()];
                final String[] kindCodeNames = new String[cursor.getCount()];

                int idx = 0;
                while (cursor.moveToNext()) {
                    kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                    kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")) + " 에 단어 추가";
                    idx++;
                }
                cursor.close();

                final AlertDialog.Builder dlg = new AlertDialog.Builder(DictionaryActivity.this);
                dlg.setTitle("기능 선택");
                dlg.setSingleChoiceItems(kindCodeNames, dSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        dSelect = arg1;
                    }
                });
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cur = adapter.getCursor();
                        DicDb.insDicVoc(db, cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID")), kindCodes[dSelect]);
                        DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                    }
                });

                dlg.show();
            }

            return true;
        }
    };

    /**
     * 검색 단어가 변경되었으면 다시 검색을 한다.
     */
    TextWatcher textWatcherInput = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //changeListView();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_cb_tone ) {
            changeListView(true);
        } else if ( v.getId() == R.id.my_iv_web ) {
            final String[] kindCodes = new String[]{"Naver","Daum"};

            final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setTitle("검색 사이트 선택");
            dlg.setSingleChoiceItems(kindCodes, webDictionaryIdx, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    webDictionaryIdx = arg1;
                }
            });
            dlg.setNegativeButton("취소", null);
            dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Bundle bundle = new Bundle();

                    bundle.putString("kind", (DicUtils.isHangule(et_search.getText().toString().trim()) ? CommConstants.dictionaryKind_h : CommConstants.dictionaryKind_f));
                    bundle.putString("site", kindCodes[webDictionaryIdx]);
                    bundle.putString("word", et_search.getText().toString().trim().toLowerCase());

                    Intent intent = new Intent(getApplication(), WebDictionaryActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            dlg.show();
        } else if ( v.getId() == R.id.my_rb_word ) {
            mWsKind = "W";

            changeListView(true);
        } else if ( v.getId() == R.id.my_rb_sentence ) {
            mWsKind = "S";

            changeListView(true);
        } else if ( v.getId() == R.id.my_cb_tone ) {
            changeListView(true);
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

    private class DictionaryActivitySearchTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(DictionaryActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            getData();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setListView();

            pd.dismiss();
            task = null;

            super.onPostExecute(result);
        }
    }
}


class DictionaryActivityCursorAdapter extends CursorAdapter {
    private String mWsKind = "";
    int fontSize = 0;

    public DictionaryActivityCursorAdapter(Context context, Cursor cursor, int flags, String _mWsKind) {
        super(context, cursor, 0);

        mWsKind = _mWsKind;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_dictionary_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if ( "W".equals(mWsKind) ) {
            String word = String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
            String mean = String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
            String spelling = String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            String hanmun = String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("HANMUN")));

            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(word);
            ((TextView) view.findViewById(R.id.my_tv_mean)).setText(mean);

            view.findViewById(R.id.my_tv_spelling).setVisibility(View.VISIBLE);
            if (!"".equals(hanmun)) {
                ((TextView) view.findViewById(R.id.my_tv_spelling)).setText(spelling + " " + hanmun);
            } else {
                ((TextView) view.findViewById(R.id.my_tv_spelling)).setText(spelling);
            }
        } else {
            view.findViewById(R.id.my_tv_spelling).setVisibility(View.GONE);

            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
            ((TextView) view.findViewById(R.id.my_tv_mean)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_mean)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setTextSize(fontSize);
    }
}