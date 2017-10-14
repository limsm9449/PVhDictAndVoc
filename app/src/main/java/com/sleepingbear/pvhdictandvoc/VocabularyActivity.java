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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class VocabularyActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private VocabularyCursorAdapter adapter;
    public int mSelect = 0;

    private String kind = "";
    private String mMemorization = "ALL";
    private int mOrder = -1;
    private String mOrderName = "";

    public String isChange = "N";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = this.getIntent().getExtras();
        kind = b.getString("kind");

        ActionBar ab = getSupportActionBar();
        ab.setTitle(b.getString("kindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        RadioButton rb_all = (RadioButton) this.findViewById(R.id.my_a_voc_rb_all);
        rb_all.setOnClickListener(this);

        RadioButton rb_m = (RadioButton) this.findViewById(R.id.my_a_voc_rb_m);
        rb_m.setOnClickListener(this);

        RadioButton rb_m_not = (RadioButton) this.findViewById(R.id.my_a_voc_rb_m_not);
        rb_m_not.setOnClickListener(this);

        Spinner spinner = (Spinner) this.findViewById(R.id.my_a_voc_s_ord);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dicOrderValue, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mOrder = parent.getSelectedItemPosition();
                mOrderName = getResources().getStringArray(R.array.dicOrderValue)[mOrder];

                //setActionBarTitle();

                getListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner.setSelection(0);

        DicUtils.setAdView(this);
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT B.SEQ _id," + CommConstants.sqlCR);
        sql.append("       B.SEQ," + CommConstants.sqlCR);
        sql.append("       B.WORD," + CommConstants.sqlCR);
        sql.append("       B.MEAN," + CommConstants.sqlCR);
        sql.append("       B.SPELLING," + CommConstants.sqlCR);
        sql.append("       B.ENTRY_ID," + CommConstants.sqlCR);
        sql.append("       A.MEMORIZATION," + CommConstants.sqlCR);
        sql.append("       A.INS_DATE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.KIND = '" + kind + "'" + CommConstants.sqlCR);
        if ( mMemorization.length() == 1 ) {
            sql.append("   AND A.MEMORIZATION = '" + mMemorization + "' " + CommConstants.sqlCR);
        }
        if ( mOrder == 0 ) {
            sql.append(" ORDER BY A.INS_DATE DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 1 ) {
            sql.append(" ORDER BY B.WORD DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 2 ) {
            sql.append(" ORDER BY B.MEAN DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 3 ) {
            sql.append(" ORDER BY A.INS_DATE" + CommConstants.sqlCR);
        } else if ( mOrder == 4 ) {
            sql.append(" ORDER BY B.WORD" + CommConstants.sqlCR);
        } else if ( mOrder == 5 ) {
            sql.append(" ORDER BY B.MEAN" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_v_lv_list);
        adapter = new VocabularyCursorAdapter(getApplicationContext(), cursor, this, db);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) adapter.getItem(position);
                final String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));

                new android.app.AlertDialog.Builder(VocabularyActivity.this)
                        .setTitle("알림")
                        .setMessage("삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DicDb.delDicVoc(db, entryId, kind);
                                //DicUtils.writeInfoToFile(getApplicationContext(), "MYWORD_DELETE" + ":" + kind + ":" + entryId);

                                adapter.dataChange();

                                isChange = "Y";

                                Toast.makeText(getApplicationContext(), "단어장을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

                return true;
            }
        });

        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            cur.moveToPosition(position);

            final String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));
            final String word = cur.getString(cur.getColumnIndexOrThrow("WORD"));
            final String seq = cur.getString(cur.getColumnIndexOrThrow("_id"));

            Intent intent = new Intent(getApplication(), WordViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("entryId", entryId);
            bundle.putString("seq", seq);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_a_voc_rb_all) {
            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_voc_rb_m) {
            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_voc_rb_m_not) {
            mMemorization = "N";
            getListView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this.getApplication(), VocabularyActivity.class);
        intent.putExtra("isChange", isChange);
        setResult(RESULT_OK, intent);

        finish();
    }
}

class VocabularyCursorAdapter extends CursorAdapter {
    private String entryId = "";
    private String seq = "";
    private Activity mActivity;
    private SQLiteDatabase mDb;
    private Cursor mCursor;

    static class ViewHolder {
        protected CheckBox memorizationCheck;
        protected String enrtyId;
        protected String seq;
    }

    public VocabularyCursorAdapter(Context context, Cursor cursor, Activity activity, SQLiteDatabase db) {
        super(context, cursor, 0);
        mCursor = cursor;
        mActivity = activity;
        mDb = db;
    }

    public void dataChange() {
        mCursor.requery();
        mCursor.move(mCursor.getPosition());

        //변경사항을 반영한다.
        notifyDataSetChanged();
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_vocabulary_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        //암기 체크
        viewHolder.memorizationCheck = (CheckBox) view.findViewById(R.id.my_c_vi_cb_memorization);
        viewHolder.memorizationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap hm = (HashMap) v.getTag();
                String entryId = (String) hm.get("entryId");

                DicDb.updMemory(mDb, entryId, (((CheckBox) v.findViewById(R.id.my_c_vi_cb_memorization)).isChecked() ? "Y" : "N"));

                //기록...
                //DicUtils.writeInfoToFile(context, "MEMORY" + ":" + entryId + ":" + (((CheckBox) v.findViewById(R.id.my_c_vi_cb_memorization)).isChecked() ? "Y" : "N"));

                dataChange();
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        HashMap param = new HashMap();
        param.put("WORD", cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        param.put("entryId", cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")));
        param.put("seq", cursor.getString(cursor.getColumnIndexOrThrow("SEQ")));
        param.put("position", cursor.getPosition());

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.memorizationCheck.setTag(param);
        viewHolder.enrtyId = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        viewHolder.seq = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));

        ((TextView) view.findViewById(R.id.my_c_vi_tv_word)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("WORD"))));
        ((TextView) view.findViewById(R.id.my_c_vi_tv_spelling)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING"))));
        ((TextView) view.findViewById(R.id.my_c_vi_tv_date)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE"))));
        ((TextView) view.findViewById(R.id.my_c_vi_tv_mean)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("MEAN"))));

        //암기 체크박스
        String memorization = cursor.getString(cursor.getColumnIndexOrThrow("MEMORIZATION"));
        CheckBox cb_memorization = (CheckBox)view.findViewById(R.id.my_c_vi_cb_memorization);
        cb_memorization.setTag(param);
        if ( "Y".equals(memorization) ) {
            cb_memorization.setChecked(true);
        } else {
            cb_memorization.setChecked(false);
        }

    }
}