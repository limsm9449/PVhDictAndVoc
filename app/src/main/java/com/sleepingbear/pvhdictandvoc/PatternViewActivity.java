package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Locale;

public class PatternViewActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    int fontSize = 0;

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private PatternViewCursorAdapter adapter;
    public String sqlWhere;
    public int mSelect = 0;
    private boolean isForeignView = false;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        myTTS = new TextToSpeech(this, this);

        Bundle b = this.getIntent().getExtras();
        sqlWhere = b.getString("SQL_WHERE");

        ((TextView)this.findViewById(R.id.my_tv_pattern_desc)).setText(b.getString("PATTERN") + " : " + b.getString("DESC"));
        ((TextView)this.findViewById(R.id.my_tv_pattern_desc)).setTextSize(fontSize);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        getListView();
    }

    public void getListView() {
        Cursor cursor = db.rawQuery(DicQuery.getPatternSampleList(sqlWhere), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_pattern_lv);
        adapter = new PatternViewCursorAdapter(getApplicationContext(), cursor, this);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cur = (Cursor) adapter.getItem(i);
                adapter.setStatus( cur.getString(cur.getColumnIndexOrThrow("SEQ")) );
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(PatternViewActivity.this);
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
            };
        });
        listView.setSelection(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_pattern, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ( isForeignView ) {
            ((MenuItem) menu.findItem(R.id.action_view)).setVisible(false);
            ((MenuItem) menu.findItem(R.id.action_hide)).setVisible(true);
        } else {
            ((MenuItem) menu.findItem(R.id.action_view)).setVisible(true);
            ((MenuItem) menu.findItem(R.id.action_hide)).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_view) {
            isForeignView = true;
            invalidateOptionsMenu();

            adapter.setForeignView(isForeignView);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_hide) {
            isForeignView = false;
            invalidateOptionsMenu();

            adapter.setForeignView(isForeignView);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_patternView);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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
}

class PatternViewCursorAdapter extends CursorAdapter {
    int fontSize = 0;
    public HashMap statusData = new HashMap();
    public boolean isForeignView = false;

    public PatternViewCursorAdapter(Context context, Cursor cursor, Activity activity) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_pattern_view_item, parent, false);

        return view;
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