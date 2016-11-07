package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

public class DicCategoryViewActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private DicCategoryViewCursorAdapter adapter;
    public String kind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dic_category_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = this.getIntent().getExtras();
        kind = b.getString("kind");

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle(b.getString("kindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        getListView();
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();
        if ( "W".equals(kind.substring(0,1)) ) {
            sql.append("SELECT B.SEQ _id," + CommConstants.sqlCR);
            sql.append("       B.SEQ," + CommConstants.sqlCR);
            sql.append("       B.WORD," + CommConstants.sqlCR);
            sql.append("       B.MEAN," + CommConstants.sqlCR);
            sql.append("       B.ENTRY_ID," + CommConstants.sqlCR);
            sql.append("       B.SPELLING," + CommConstants.sqlCR);
            sql.append("       'Y' IS_DIC" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CATEGORY_WORD A, DIC B" + CommConstants.sqlCR);
            sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
            sql.append("   AND A.CODE = '" + kind + "'" + CommConstants.sqlCR);
            sql.append(" ORDER BY B.WORD" + CommConstants.sqlCR);
        } else {
            /*
            sql.append("SELECT SEQ _id," + CommConstants.sqlCR);
            sql.append("       SEQ," + CommConstants.sqlCR);
            sql.append("       SENTENCE1," + CommConstants.sqlCR);
            sql.append("       SENTENCE2" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CATEGORY_SENT " + CommConstants.sqlCR);
            sql.append(" WHERE CODE = '" + kind + "'" + CommConstants.sqlCR);
            sql.append(" ORDER BY ORD" + CommConstants.sqlCR);
            */
            sql.append("SELECT IFNULL(B.SEQ, A.SEQ) _id," + CommConstants.sqlCR);
            sql.append("       IFNULL(B.SEQ, A.SEQ) SEQ," + CommConstants.sqlCR);
            sql.append("       IFNULL(B.WORD, A.SENTENCE1) WORD," + CommConstants.sqlCR);
            sql.append("       IFNULL(B.MEAN, A.SENTENCE2) MEAN," + CommConstants.sqlCR);
            sql.append("       B.ENTRY_ID," + CommConstants.sqlCR);
            sql.append("       B.SPELLING," + CommConstants.sqlCR);
            sql.append("       CASE WHEN B.SEQ IS NULL THEN 'N' ELSE 'Y' END IS_DIC" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CATEGORY_SENT A LEFT OUTER JOIN DIC B ON ( B.WORD = A.SENTENCE1 )" + CommConstants.sqlCR);
            sql.append(" WHERE A.CODE = '" + kind + "'" + CommConstants.sqlCR);
            sql.append(" ORDER BY A.SENTENCE1" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_dk_lv1);
        adapter = new DicCategoryViewCursorAdapter(getApplicationContext(), cursor, this, db);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setSelection(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

class DicCategoryViewCursorAdapter extends CursorAdapter {
    private String entryId = "";
    private String seq = "";
    private Activity mActivity;
    private Cursor mCursor;
    private SQLiteDatabase mDb;

    static class ViewHolder {
        protected String enrtyId;
        protected String seq;
        protected String sentence1;
        protected String sentence2;
        protected String isDic;
    }

    public DicCategoryViewCursorAdapter(Context context, Cursor cursor, Activity activity, SQLiteDatabase db) {
        super(context, cursor, 0);
        mCursor = cursor;
        mActivity = activity;
        mDb = db;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        view = LayoutInflater.from(context).inflate(R.layout.content_dic_category_view_item_w, parent, false);

        //Item 선택
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vViewHolder = (ViewHolder) v.getTag();

                if ( "Y".equals(vViewHolder.isDic) ) {
                    Intent intent = new Intent(mActivity.getApplication(), WordViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("entryId", vViewHolder.enrtyId);
                    bundle.putString("seq", vViewHolder.seq);
                    intent.putExtras(bundle);

                    mActivity.startActivity(intent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("viet", vViewHolder.sentence1);
                    bundle.putString("han", vViewHolder.sentence2);

                    Intent intent = new Intent(mActivity.getApplication(), SentenceViewActivity.class);
                    intent.putExtras(bundle);

                    mActivity.startActivity(intent);
                }
            }
        });

        ViewHolder viewHolder = new ViewHolder();
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.enrtyId = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        viewHolder.seq = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));
        viewHolder.sentence1 = cursor.getString(cursor.getColumnIndexOrThrow("WORD"));
        viewHolder.sentence2 = cursor.getString(cursor.getColumnIndexOrThrow("MEAN"));
        viewHolder.isDic = cursor.getString(cursor.getColumnIndexOrThrow("IS_DIC"));

        ((TextView) view.findViewById(R.id.my_c_dciw_tv_viet)).setText(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        ((TextView) view.findViewById(R.id.my_c_dciw_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
        ((TextView) view.findViewById(R.id.my_c_dciw_tv_mean)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
    }
}