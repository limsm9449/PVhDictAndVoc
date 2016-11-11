package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class WordViewActivity extends AppCompatActivity implements View.OnClickListener, OnInitListener {
    private TextToSpeech myTTS;

    private ImageButton ib_myVoc;
    private ImageButton ib_close;

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private WordViewCursorAdapter wordViewAdapter;
    //private DicRefWordAdapter dicRefWordAdapter;

    private boolean myVoc = false;

    private Activity mActivity;

    private String entryId;
    private String word;
    private String seq;
    private String vocKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_view);

        myTTS = new TextToSpeech(this, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("단어 상세");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        mActivity = this;

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle b = getIntent().getExtras();
        entryId = b.getString("entryId");

        getWordInfo();
    }

    public void getWordInfo() {
        ImageButton ib_tts = (ImageButton) findViewById(R.id.my_c_wv_ib_tts);
        ib_tts.setVisibility(View.GONE);
        ib_tts.setOnClickListener(this);

        ib_myVoc = (ImageButton) findViewById(R.id.my_c_wv_ib_myvoc);
        ib_myVoc.setOnClickListener(this);
        ib_myVoc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //단어장 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getVocabularyCategory(), null);
                final String[] kindCodes = new String[cursor.getCount()];
                final String[] kindCodeNames = new String[cursor.getCount()];

                int idx = 0;
                while ( cursor.moveToNext() ) {
                    kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                    kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                    idx++;
                }
                cursor.close();

                final AlertDialog.Builder dlg = new AlertDialog.Builder(mActivity);
                dlg.setTitle("단어장 선택");
                dlg.setSingleChoiceItems(kindCodeNames, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        vocKind = kindCodes[arg1];
                    }
                });
                dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DicDb.insDicVoc(db, entryId, vocKind);
                        ImageButton ib_myvoc = (ImageButton)findViewById(R.id.my_c_wv_ib_myvoc);
                        ib_myvoc.setImageResource(android.R.drawable.star_on);
                        myVoc = true;            }
                });

                dlg.show();

                return false;
            }
        });

        Cursor wordCursor = db.rawQuery(" SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, (SELECT COUNT(*) FROM DIC_VOC WHERE ENTRY_ID = '" + entryId + "') MY_VOC FROM DIC WHERE ENTRY_ID = '" + entryId + "'", null);
        if ( wordCursor.moveToNext() ) {
            TextView tv_word = (TextView)this.findViewById(R.id.my_c_wv_tv_word);
            tv_word.setText(wordCursor.getString(wordCursor.getColumnIndexOrThrow("WORD")));

            TextView tv_spelling = (TextView)this.findViewById(R.id.my_c_wv_tv_spelling);
            tv_spelling.setText(wordCursor.getString(wordCursor.getColumnIndexOrThrow("SPELLING")));

            TextView tv_mean = (TextView)this.findViewById(R.id.my_c_wv_tv_mean);
            tv_mean.setText(wordCursor.getString(wordCursor.getColumnIndexOrThrow("MEAN")));

            if ( "0".equals(wordCursor.getString(wordCursor.getColumnIndexOrThrow("MY_VOC"))) ) {
                ImageButton ib_myvoc = (ImageButton)this.findViewById(R.id.my_c_wv_ib_myvoc);
                ib_myvoc.setImageResource(android.R.drawable.star_off);
                myVoc = false;
            } else {
                ImageButton ib_myvoc = (ImageButton)this.findViewById(R.id.my_c_wv_ib_myvoc);
                ib_myvoc.setImageResource(android.R.drawable.star_on);
                myVoc = true;
            }

            seq = wordCursor.getString(wordCursor.getColumnIndexOrThrow("_id"));
            word = wordCursor.getString(wordCursor.getColumnIndexOrThrow("WORD"));
        }
        wordCursor.close();

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 ORD1, 0 _id, 1 ORD2, '* 상세 뜻' DATA1, '' DATA2, '' SPELLING " + CommConstants.sqlCR);
        sql.append(" UNION " + CommConstants.sqlCR);
        sql.append("SELECT 2 ORD1, SEQ, 1 ORD2, MEAN DATA1, '' DATA2, '' SPELLING " + CommConstants.sqlCR);
        sql.append("   FROM DIC_MEAN " + CommConstants.sqlCR);
        sql.append("  WHERE DIC_SEQ = '" + seq + "'  " + CommConstants.sqlCR);
        sql.append("  UNION  " + CommConstants.sqlCR);
        sql.append("SELECT 2 ORD1, A.SEQ, 2 ORD2, C.SENTENCE1, C.SENTENCE2, C.SPELLING " + CommConstants.sqlCR);
        sql.append("  FROM DIC_MEAN A JOIN DIC_MEAN_SAMPLE B ON A.SEQ = B. MEAN_SEQ " + CommConstants.sqlCR);
        sql.append("                  JOIN DIC_SAMPLE C ON B.SAMPLE_SEQ = C.SEQ " + CommConstants.sqlCR);
        sql.append(" WHERE A.DIC_SEQ = '" + seq + "'  " + CommConstants.sqlCR);
        sql.append("  UNION  " + CommConstants.sqlCR);
        sql.append("SELECT 3 ORD1, 0 SEQ, 1 ORD2, '* 예제' DATA1, '' DATA2, '' SPELLING " + CommConstants.sqlCR);
        sql.append(" UNION   " + CommConstants.sqlCR);
        sql.append("SELECT 4 ORD1, A.SEQ, 1 ORD2, B.SENTENCE1, B.SENTENCE2, B.SPELLING " + CommConstants.sqlCR);
        sql.append("  FROM DIC_WORD_SAMPLE A JOIN DIC_SAMPLE B ON A.SAMPLE_SEQ = B.SEQ " + CommConstants.sqlCR);
        sql.append(" WHERE A.DIC_SEQ = '" + seq + "'  " + CommConstants.sqlCR);
        sql.append(" UNION   " + CommConstants.sqlCR);
        sql.append("SELECT 5 ORD1, 0 SEQ, 1 ORD2, '* 기타 예제' DATA1, '' DATA2, '' SPELLING " + CommConstants.sqlCR);
        sql.append(" UNION   " + CommConstants.sqlCR);
        sql.append("SELECT * FROM (   " + CommConstants.sqlCR);
        sql.append("   SELECT 6 ORD1, SEQ, 1 ORD2, SENTENCE1, SENTENCE2, SPELLING " + CommConstants.sqlCR);
        sql.append("     FROM DIC_SAMPLE " + CommConstants.sqlCR);
        sql.append("    WHERE (SENTENCE1 LIKE (SELECT '%'||WORD||'%' FROM DIC WHERE ENTRY_ID = '" + entryId + "')  " + CommConstants.sqlCR);
        sql.append("           OR SENTENCE2 LIKE (SELECT '%'||WORD||'%' FROM DIC WHERE ENTRY_ID = '" + entryId + "'))  " + CommConstants.sqlCR);
        sql.append("      AND SEQ NOT IN (SELECT SAMPLE_SEQ FROM DIC_MEAN_SAMPLE WHERE MEAN_SEQ IN (SELECT SEQ FROM DIC_MEAN WHERE DIC_SEQ = '" + seq + "')  " + CommConstants.sqlCR);
        sql.append("                       UNION   " + CommConstants.sqlCR);
        sql.append("                       SELECT SAMPLE_SEQ FROM DIC_WORD_SAMPLE WHERE DIC_SEQ = '" + seq + "')   " + CommConstants.sqlCR);
        sql.append("    LIMIT 300 )  " + CommConstants.sqlCR);
        sql.append("ORDER BY 1,2,3  " + CommConstants.sqlCR);
        Log.i("vhDictAndVoc", sql.toString());
        Cursor dicViewCursor = db.rawQuery(sql.toString(), null);

        int[] ords = new int[dicViewCursor.getCount()];
        String[] spellings = new String[dicViewCursor.getCount()];

        int meanIdx = 1;
        int sampleIdx = 1;
        int idx = 0;
        while ( dicViewCursor.moveToNext() ) {
            String ord1 = DicUtils.getString(dicViewCursor.getString(dicViewCursor.getColumnIndexOrThrow("ORD1")));
            String ord2 = DicUtils.getString(dicViewCursor.getString(dicViewCursor.getColumnIndexOrThrow("ORD2")));

            if ( ("2".equals(ord1) && "1".equals(ord2)) || "3".equals(ord1) || "5".equals(ord1)  ) {
                ords[idx++] = meanIdx++;
                sampleIdx = 1;
            } else if ( ("2".equals(ord1) && "2".equals(ord2)) || "4".equals(ord1) || "6".equals(ord1) ) {
                ords[idx++] = sampleIdx++;
            }
        }

        ListView dicViewListView = (ListView) this.findViewById(R.id.my_c_wv_lv_list);
        wordViewAdapter = new WordViewCursorAdapter(this, dicViewCursor, 0, ords, spellings);
        dicViewListView.setAdapter(wordViewAdapter);
        dicViewListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dicViewListView.setOnItemClickListener(itemClickListener);
    }

    /**
     * 단어가 선택되면은 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) wordViewAdapter.getItem(position);
            cur.moveToPosition(position);
            String ord1 = cur.getString(cur.getColumnIndexOrThrow("ORD1"));
            String ord2 = cur.getString(cur.getColumnIndexOrThrow("ORD2"));
            String viet = cur.getString(cur.getColumnIndexOrThrow("DATA1"));
            String han = cur.getString(cur.getColumnIndexOrThrow("DATA2"));

            if ( ("2".equals(ord1) && "2".equals(ord2)) ||
                    "4".equals(ord1) ||
                    "6".equals(ord1) ) {
                Bundle bundle = new Bundle();
                bundle.putString("viet", viet);
                bundle.putString("han", han);

                Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_c_wv_ib_myvoc:
                if ( myVoc ) {
                    ImageButton ib_myvoc = (ImageButton)this.findViewById(R.id.my_c_wv_ib_myvoc);
                    ib_myvoc.setImageResource(android.R.drawable.star_off);
                    myVoc = false;

                    DicDb.delDicVocAll(db, entryId);

                    // 기록..
                    DicUtils. writeInfoToFile(getApplicationContext(), "MYWORD_DELETE_ALL" + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + ":" + entryId);
                } else {
                    ImageButton ib_myvoc = (ImageButton)this.findViewById(R.id.my_c_wv_ib_myvoc);
                    ib_myvoc.setImageResource(android.R.drawable.star_on);
                    myVoc = true;

                    DicDb.insDicVoc(db, entryId, "MY000");

                    // 기록..
                    DicUtils. writeInfoToFile(getApplicationContext(), "MYWORD_INSERT" + ":" + "MY000" + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + ":" + entryId);
                }

                break;
            case R.id.my_c_wv_ib_tts:
                //myTTS.speak(((TextView)this.findViewById(R.id.my_c_wv_tv_spelling)).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                myTTS.speak(((TextView)this.findViewById(R.id.my_c_wv_tv_word)).getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                break;
        }
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
            bundle.putString("SCREEN", "WORDVIEW");

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onInit(int status) {
        Locale loc = new Locale("en");
        Log.i("-------------", Arrays.toString(loc.getAvailableLocales()));

        if (status == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(new Locale("vi", "VN"));
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                ((ImageButton) findViewById(R.id.my_c_wv_ib_tts)).setVisibility(View.GONE);
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

}

class WordViewCursorAdapter extends CursorAdapter {
    private int[] rowOrds;
    private String[] rowSpellings;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    public WordViewCursorAdapter(Context context, Cursor cursor, int flags, int ords[], String[] spellings) {
        super(context, cursor, 0);

        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();

        rowOrds = ords;
        rowSpellings = spellings;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_word_view_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView tv_kind = (TextView) view.findViewById(R.id.my_c_wvi_tv_kind);
        TextView tv_mean = (TextView) view.findViewById(R.id.my_c_wvi_tv_mean);
        TextView tv_viet = (TextView) view.findViewById(R.id.my_c_wvi_tv_viet);
        TextView tv_spelling = (TextView) view.findViewById(R.id.my_c_wvi_tv_spelling);
        TextView tv_han = (TextView) view.findViewById(R.id.my_c_wvi_tv_han);

        String data1 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("DATA1"))).trim();
        String data2 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("DATA2"))).trim();
        String ord1 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("ORD1")));
        String ord2 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("ORD2")));
        String spelling = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));

        if ( "1".equals(ord1) || "3".equals(ord1) || "5".equals(ord1) ) {
            tv_kind.setVisibility(View.VISIBLE);
            tv_mean.setVisibility(View.GONE);
            tv_viet.setVisibility(View.GONE);
            tv_spelling.setVisibility(View.GONE);
            tv_han.setVisibility(View.GONE);

            tv_kind.setText(data1);
        } else if ( "2".equals(ord1) && "1".equals(ord2) ) {
            tv_kind.setVisibility(View.GONE);
            tv_mean.setVisibility(View.VISIBLE);
            tv_viet.setVisibility(View.GONE);
            tv_spelling.setVisibility(View.GONE);
            tv_han.setVisibility(View.GONE);

            tv_mean.setText(rowOrds[cursor.getPosition() - 1] + ". " + data1);
        } else {
            tv_kind.setVisibility(View.GONE);
            tv_mean.setVisibility(View.GONE);
            tv_viet.setVisibility(View.VISIBLE);
            tv_spelling.setVisibility(View.VISIBLE);
            tv_han.setVisibility(View.VISIBLE);

            tv_viet.setText(rowOrds[cursor.getPosition() - 1] + ". " + data1);
            tv_han.setText("     " + data2);
            tv_spelling.setText("    -> " + spelling);
        }
    }
}
