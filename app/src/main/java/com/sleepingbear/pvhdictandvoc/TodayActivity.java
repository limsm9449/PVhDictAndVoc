package com.sleepingbear.pvhdictandvoc;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class TodayActivity extends AppCompatActivity {
    int fontSize = 0;
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private TodayActivityCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        getListView();
    }

    public void getListView() {
        DicUtils.dicLog(this.getClass().toString() + " getListView");

        StringBuffer sql = new StringBuffer();

        String today = DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".");

        sql.append("SELECT COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_TODAY " + CommConstants.sqlCR);
        sql.append(" WHERE TODAY = '" + today + "'" + CommConstants.sqlCR);
        Cursor cCursor = db.rawQuery(sql.toString(), null);
        if ( cCursor.moveToNext() ) {
            if ( cCursor.getInt(cCursor.getColumnIndexOrThrow("CNT")) == 0 ) {
                DicDb.insToday10(db, today);
            }
        }
        cCursor.close();

        sql.delete(0, sql.length());
        sql.append("SELECT B.SEQ _id, B.WORD, B.MEAN, B.ENTRY_ID, B.SPELLING, A.TODAY" + CommConstants.sqlCR);
        sql.append("  FROM DIC_TODAY A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID " + CommConstants.sqlCR);
        sql.append("   AND B.KIND = 'VH' " + CommConstants.sqlCR);
        sql.append(" ORDER BY A.TODAY DESC, B.ORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor listCursor = db.rawQuery(sql.toString(), null);
        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new TodayActivityCursorAdapter(this, listCursor, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setSelection(0);
    }

    /**
     * 단어가 선택되면은 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            //cur.moveToPosition(position);

            String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));
            String seq = cur.getString(cur.getColumnIndexOrThrow("_id"));

            Intent intent = new Intent(getApplication(), WordViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("entryId", entryId);
            bundle.putString("seq", seq);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

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
            onBackPressed();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_today);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}

class TodayActivityCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public TodayActivityCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_today_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_viet)).setText(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TODAY")));
        ((TextView) view.findViewById(R.id.my_tv_mean)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_viet)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_date)).setTextSize(11);
        ((TextView) view.findViewById(R.id.my_tv_mean)).setTextSize(fontSize);
    }
}

