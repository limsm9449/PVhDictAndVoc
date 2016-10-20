package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SentenceViewActivity extends AppCompatActivity implements View.OnClickListener {
    public DbHelper dbHelper;
    public SQLiteDatabase db;
    public SentenceViewCursorAdapter sentenceViewAdapter;
    public int mSelect = 0;
    public String han;
    public String notHan;
    public boolean isMySample = false;
    public boolean isChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("문장 상세");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle b = getIntent().getExtras();
        if ( "".equals(b.getString("viet")) ) {
            ((TextView) findViewById(R.id.my_c_sv_tv_viet)).setText("");
            ((TextView) findViewById(R.id.my_c_sv_tv_viet_spelling)).setText("");
            ((TextView) findViewById(R.id.my_c_sv_tv_han)).setText("");

            getNewSentence();
        } else {
            notHan = b.getString("viet");
            han = b.getString("han");
            changeListView();
        }

        ImageButton mySample = (ImageButton) findViewById(R.id.my_c_sv_ib_mysample);
        mySample.setOnClickListener(this);
        if ( DicDb.isExistMySample(db, notHan) ) {
            isMySample = true;
            mySample.setImageResource(android.R.drawable.star_on);
        } else {
            isMySample = false;
            mySample.setImageResource(android.R.drawable.star_off);
        }
    }

    public void changeListView() {
        //문장의 단어를 구한다.
        //String[] splitStr = b.getString("viet").split(CommConstants.splitStr);
        String[] splitStr = DicUtils.sentenceSplit(notHan);

        Cursor wordCursor = null;
        String word = "";
        String spelling = "";
        ArrayList<String> al = new ArrayList<String>();
        for ( int m = 0; m < splitStr.length; m++ ) {
            if ( CommConstants.sentenceSplitStr.indexOf(splitStr[m]) > -1 ) {
                spelling += splitStr[m];
            } else {
                // 3 단어
                word = DicUtils.getSentenceWord(splitStr, 3, m);
                if ( !"".equals(word) ) {
                    wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                    if ( wordCursor.moveToNext() ) {
                        if ( !al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                            al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                        }
                        spelling += DicUtils.getOneSpelling(wordCursor.getString(wordCursor.getColumnIndexOrThrow("SPELLING"))) + " ";

                        m += 4;

                        wordCursor.close();
                        continue;
                    }
                    wordCursor.close();
                }

                // 2 단어
                word = DicUtils.getSentenceWord(splitStr, 2, m);
                if ( !"".equals(word) ) {
                    wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                    if ( wordCursor.moveToNext() ) {
                        if ( !al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                            al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                        }
                        spelling += DicUtils.getOneSpelling(wordCursor.getString(wordCursor.getColumnIndexOrThrow("SPELLING"))) + " ";

                        m += 2;

                        wordCursor.close();
                        continue;
                    }
                    wordCursor.close();
                }

                // 1 단어
                word = DicUtils.getSentenceWord(splitStr, 1, m);
                if ( !"".equals(word) ) {
                    wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                    if (wordCursor.moveToNext()) {
                        if ( !al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                            al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                        }
                        spelling += DicUtils.getOneSpelling(wordCursor.getString(wordCursor.getColumnIndexOrThrow("SPELLING"))) + " ";
                    } else {
                        spelling += splitStr[m];
                    }
                    wordCursor.close();
                }
            }
        }
        //나머지 단어들
        for ( int m = 0; m < splitStr.length; m++ ) {
            // 2 단어
            word = DicUtils.getSentenceWord(splitStr, 2, m);
            if ( !"".equals(word) ) {
                wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                if ( wordCursor.moveToNext() ) {
                    if ( !al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                        al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                    }
                }
                wordCursor.close();
            }

            word = DicUtils.getSentenceWord(splitStr, 1, m);
            if ( !"".equals(word) ) {
                wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                if (wordCursor.moveToNext()) {
                    if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                        al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                    }
                }
                wordCursor.close();
            }
        }

        ((TextView) findViewById(R.id.my_c_sv_tv_viet)).setText(notHan);
        ((TextView) findViewById(R.id.my_c_sv_tv_viet_spelling)).setText("-> " + spelling.replaceAll("[\\[\\]]", ""));
        ((TextView) findViewById(R.id.my_c_sv_tv_han)).setText(han);

        StringBuffer sql = new StringBuffer();
        if ( al.size() == 0 ) {
            sql.append("SELECT DISTINCT SEQ _id, 1 ORD,  WORD, MEAN, ENTRY_ID, SPELLING, (SELECT COUNT(*) FROM DIC_VOC WHERE ENTRY_ID = A.ENTRY_ID) MY_VOC FROM DIC A WHERE ENTRY_ID = 'xxxxxxxx'" + CommConstants.sqlCR);
        } else {
            for (int i = 0; i < al.size(); i++) {
                if (i > 0) {
                    sql.append("UNION" + CommConstants.sqlCR);
                }
                sql.append("SELECT SEQ _id, " + i + " ORD,  WORD, MEAN, ENTRY_ID, SPELLING, (SELECT COUNT(*) FROM DIC_VOC WHERE ENTRY_ID = A.ENTRY_ID) MY_VOC FROM DIC A WHERE ENTRY_ID = '" + al.get(i) + "'" + CommConstants.sqlCR);
            }
            sql.append(" ORDER BY ORD, WORD" + CommConstants.sqlCR);
        }
        //DicUtils.dicSqlLog(sql.toString());
        wordCursor = db.rawQuery(sql.toString(), null);

        ListView dicViewListView = (ListView) this.findViewById(R.id.my_c_sv_lv_list);
        sentenceViewAdapter = new SentenceViewCursorAdapter(this, wordCursor, 0);
        dicViewListView.setAdapter(sentenceViewAdapter);
        dicViewListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dicViewListView.setOnItemClickListener(itemClickListener);

        dicViewListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) sentenceViewAdapter.getItem(position);
                cur.moveToPosition(position);

                final String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));
                final String word = cur.getString(cur.getColumnIndexOrThrow("WORD"));
                final String seq = cur.getString(cur.getColumnIndexOrThrow("_id"));

                //메뉴 선택 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getSentenceViewContextMenu(), null);
                final String[] kindCodes = new String[cursor.getCount()];
                final String[] kindCodeNames = new String[cursor.getCount()];

                int idx = 0;
                while ( cursor.moveToNext() ) {
                    kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                    kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                    idx++;
                }
                cursor.close();

                final AlertDialog.Builder dlg = new AlertDialog.Builder(SentenceViewActivity.this);
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
                        DicDb.insDicVoc(db, entryId, kindCodes[mSelect]);
                        sentenceViewAdapter.dataChange();
                        DicUtils. writeInfoToFile(getApplicationContext(), "MYWORD_INSERT" + ":" + kindCodes[mSelect] + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(),".") + ":" + entryId);
                    }
                });
                dlg.show();

                return true;
            };
        });
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) sentenceViewAdapter.getItem(position);
            cur.moveToPosition(position);

            Intent intent = new Intent(getApplication(), WordViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("entryId", cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID")));
            bundle.putString("seq", cur.getString(cur.getColumnIndexOrThrow("_id")));
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_c_sv_ib_mysample :
                ImageButton mySample = (ImageButton) findViewById(R.id.my_c_sv_ib_mysample);
                if ( isMySample ) {
                    isMySample = false;
                    mySample.setImageResource(android.R.drawable.star_off);

                    DicDb.delDicMySample(db, notHan);

                    // 기록..
                    DicUtils.writeInfoToFile(getApplicationContext(), "MYSAMPLE_DELETE" + ":" + notHan);

                    isChange = true;
                } else {
                    isMySample = true;
                    mySample.setImageResource(android.R.drawable.star_on);

                    DicDb.insDicMySample(db, notHan, han);

                    // 기록..
                    DicUtils.writeInfoToFile(getApplicationContext(), "MYSAMPLE_INSERT" + ":" + notHan + ":" + han);

                    isChange = true;
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_sentenceview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Bundle conData = new Bundle();
            conData.putBoolean("isChange", isChange);
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);

            finish();
        } else if (id == R.id.action_sentence_write) {
            getNewSentence();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", "SENTENCEVIEW");

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void getNewSentence() {
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout newSentenceLayout = (LinearLayout) li.inflate(R.layout.dialog_new_sentence, null);

        final EditText newSentece = (EditText) newSentenceLayout.findViewById(R.id.my_d_ns_et1);

        new AlertDialog.Builder(this)
                .setTitle("문장을 입력해 주세요.")
                .setView(newSentenceLayout)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ( "".equals(newSentece.getText().toString()) ) {
                            new android.app.AlertDialog.Builder(SentenceViewActivity.this)
                                    .setTitle("알림")
                                    .setMessage("문장을 입력하셔야 합니다.")
                                    .setPositiveButton("확인", null)
                                    .show();
                        } else {
                            notHan = newSentece.getText().toString().trim();
                            han = "";
                            changeListView();
                        }
                    }
                })
                .setNegativeButton("취소",null)
                .show();
    }
}

class SentenceViewCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    private Cursor mCursor;

    static class ViewHolder {
        protected String entryId;
        protected String word;
        protected ImageButton myvoc;
        protected boolean isMyVoc;
        protected int position;
    }

    public SentenceViewCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
        mCursor = cursor;
        mDb = ((SentenceViewActivity)context).db;
    }

    public void dataChange() {
        mCursor.requery();
        mCursor.move(mCursor.getPosition());

        //변경사항을 반영한다.
        notifyDataSetChanged();
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_sentence_view_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.myvoc = (ImageButton) view.findViewById(R.id.my_c_svi_ib_myvoc);
        viewHolder.myvoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder)v.getTag();

                if ( viewHolder.isMyVoc ) {
                    DicDb.delDicVocAll(mDb, viewHolder.entryId);

                    // 기록..
                    DicUtils.writeInfoToFile(context, "MYWORD_DELETE_ALL" + ":" + viewHolder.entryId);
                } else {
                    DicDb.insDicVoc(mDb, viewHolder.entryId, "MY000");

                    // 기록..
                    DicUtils.writeInfoToFile(context, "MYWORD_INSERT" + ":" + "MY000" + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + ":" + viewHolder.entryId);
                }

                dataChange();
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.entryId = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        viewHolder.word = cursor.getString(cursor.getColumnIndexOrThrow("WORD"));
        viewHolder.position = cursor.getPosition();
        viewHolder.myvoc.setTag(viewHolder);

        ((TextView) view.findViewById(R.id.my_c_svi_word)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("WORD"))));
        ((TextView) view.findViewById(R.id.my_c_svi_spelling)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING"))));
        ((TextView) view.findViewById(R.id.my_c_svi_mean)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("MEAN"))));

        ImageButton ib_myvoc = (ImageButton)view.findViewById(R.id.my_c_svi_ib_myvoc);
        if ( cursor.getInt(cursor.getColumnIndexOrThrow("MY_VOC")) > 0 ) {
            ib_myvoc.setImageResource(android.R.drawable.star_on);
            viewHolder.isMyVoc = true;
        } else {
            ib_myvoc.setImageResource(android.R.drawable.star_off);
            viewHolder.isMyVoc = false;
        }
    }
}