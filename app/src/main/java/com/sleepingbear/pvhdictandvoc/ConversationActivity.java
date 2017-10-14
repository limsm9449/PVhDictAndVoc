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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private EditText et_search;
    private ConversationCursorAdapter adapter;
    private int fontSize = 0;
    private Cursor dictionaryCursor;
    private int mSelect = 0;
    private TextToSpeech myTTS;
    DicSearchTask task;

    boolean isRandom = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        myTTS = new TextToSpeech(this, this);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        et_search = (EditText) findViewById(R.id.my_f_conv_et_search);
        et_search.addTextChangedListener(textWatcherInput);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    isRandom = false;
                    changeListView(true);
                }

                return false;
            }
        });

        findViewById(R.id.my_iv_clear).setOnClickListener(this);
        findViewById(R.id.my_ib_search).setOnClickListener(this);
        findViewById(R.id.my_iv_random).setOnClickListener(this);
        findViewById(R.id.my_iv_view).setOnClickListener(this);
        findViewById(R.id.my_iv_hide).setOnClickListener(this);
        
        findViewById(R.id.my_iv_hide).setVisibility(View.GONE);

        changeListView(false);

        DicUtils.setAdView(this);
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
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_conversation);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    
    public void changeListView(boolean isKeyin) {
        if ( isKeyin ) {
            findViewById(R.id.my_f_conv_rl_msg).setVisibility(View.GONE);

            if (task != null) {
                return;
            }
            task = new DicSearchTask();
            task.execute();
        }
    }

    public void getData() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
        if ( !"".equals(et_search.getText().toString()) ) {
            String[] search = et_search.getText().toString().split(",");
            String condi = "";
            for ( int i = 0; i < search.length; i++ ) {
                condi += ("".equals(condi) ? "" : " OR ") + " SENTENCE1_ENG LIKE '%" + DicUtils.getEngString(search[i]).replaceAll(" ","%") + "%'";
                condi += ("".equals(condi) ? "" : " OR ") + " SENTENCE2 LIKE '%" + search[i].replaceAll(" ","%") + "%'";
            }
            sql.append(" WHERE " + condi + CommConstants.sqlCR);
        }
        if ( isRandom ) {
            sql.append(" ORDER BY RANDOM()" + CommConstants.sqlCR);
            sql.append(" LIMIT 200" + CommConstants.sqlCR);
        } else {
            sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());

        dictionaryCursor = db.rawQuery(sql.toString(), null);

        //결과가 나올때까지 기달리게 할려고 다음 로직을 추가한다. 안하면 progressbar가 사라짐.. cursor도  Thread 방식으로 돌아가나봄
        if ( dictionaryCursor.getCount() == 0 ) {
        }
    }

    public void setListView() {
        if ( dictionaryCursor.getCount() == 0 ) {
            Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView dictionaryListView = (ListView) findViewById(R.id.my_f_conv_lv);
        adapter = new ConversationCursorAdapter(this, dictionaryCursor, 0);
        dictionaryListView.setAdapter(adapter);

        dictionaryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dictionaryListView.setOnItemClickListener(itemClickListener);
        dictionaryListView.setOnItemLongClickListener(itemLongClickListener);
        dictionaryListView.setSelection(0);

        //소프트 키보드 없애기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            adapter.setStatus( cur.getString(cur.getColumnIndexOrThrow("SEQ")) );
            adapter.notifyDataSetChanged();
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            final String sampleSeq = cur.getString(cur.getColumnIndexOrThrow("SEQ"));
            final String foreign = cur.getString(cur.getColumnIndexOrThrow("SENTENCE1"));
            final String han = cur.getString(cur.getColumnIndexOrThrow("SENTENCE2"));

            //메뉴 선택 다이얼로그 생성
            Cursor cursor = db.rawQuery(DicQuery.getNoteKindContextMenu(true), null);
            final String[] kindCodes = new String[cursor.getCount()];
            final String[] kindCodeNames = new String[cursor.getCount()];

            int idx = 0;
            while (cursor.moveToNext()) {
                kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                idx++;
            }
            cursor.close();

            final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(ConversationActivity.this);
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
                    if ( mSelect == 0 ) {
                        Bundle bundle = new Bundle();
                        bundle.putString("kind", "SAMPLE");
                        bundle.putString("sampleSeq", sampleSeq);

                        Intent intent = new Intent(getApplication(), ConversationNoteStudyActivity.class);
                        intent.putExtras(bundle);

                        startActivity(intent);
                    } else if ( mSelect == 1 ) {
                        Bundle bundle = new Bundle();
                        bundle.putString("foreign", foreign);
                        bundle.putString("han", han);
                        bundle.putString("sampleSeq", sampleSeq);

                        Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                        intent.putExtras(bundle);

                        startActivity(intent);
                    } else {
                        DicDb.insConversationToNote(db, kindCodes[mSelect], sampleSeq);
                        DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                    }
                }
            });
            dlg.show();

            return false;
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
        isRandom = false;

        if (v.getId() == R.id.my_ib_search) {
            changeListView(true);
        } else if (v.getId() == R.id.my_iv_view) {
            if (adapter != null) {
                adapter.setForeignView(true);
                adapter.notifyDataSetChanged();
            }

            findViewById(R.id.my_iv_view).setVisibility(View.GONE);
            findViewById(R.id.my_iv_hide).setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.my_iv_hide) {
            if (adapter != null) {
                adapter.setForeignView(false);
                adapter.notifyDataSetChanged();
            }

            findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);
            findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
        }  else if ( v.getId() == R.id.my_iv_random) {
            et_search.setText("");
            isRandom = true;
            changeListView(true);
        }  else if ( v.getId() == R.id.my_iv_clear) {
            et_search.setText("");
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
    public void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }

    private class DicSearchTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ConversationActivity.this);
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

            if( isRandom ) {
                Toast.makeText(ConversationActivity.this, "Random으로 200개의 예문을 조회하였습니다.", Toast.LENGTH_SHORT).show();
            }
            findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
            findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);

            pd.dismiss();
            task = null;

            super.onPostExecute(result);
        }
    }
}

class ConversationCursorAdapter extends CursorAdapter {
    int fontSize = 0;
    public boolean isForeignView = false;
    public HashMap statusData = new HashMap();

    public ConversationCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_conversation_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_han)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
        if ( isForeignView || statusData.containsKey(cursor.getString(cursor.getColumnIndexOrThrow("SEQ")))  ) {
            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
        } else {
            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText("Click..");
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_han)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);
    }

    public void setForeignView(boolean foreignView) {
        isForeignView = foreignView;
        statusData.clear();
    }

    public void setStatus(String sampleSeq) {
        statusData.put(sampleSeq, "Y");
    }
}
