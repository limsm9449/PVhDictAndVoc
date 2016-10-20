package com.sleepingbear.pvhdictandvoc;

import android.app.AlertDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MySampleActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MySampleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sample);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("나의 예문");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //리스트 내용 변경
        changeListView();
    }

    public void changeListView() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        StringBuffer sql = new StringBuffer();

        Cursor cursor = db.rawQuery(DicQuery.getMySample(), null);
        ListView listView = (ListView) findViewById(R.id.my_c_lv);
        adapter = new MySampleCursorAdapter(this, cursor);
        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
        listView.setSelection(0);
    }

    /**
     * 예문을 선택되면은 예문 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            cur.moveToPosition(position);

            Bundle bundle = new Bundle();
            bundle.putString("viet", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
            bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));

            Intent intent = new Intent(getApplicationContext(), SentenceViewActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, 90);
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch ( requestCode ) {
            case 90:
                if ( resultCode == RESULT_OK ) {
                    Bundle res = data.getExtras();
                    if ( res.getBoolean("isChange") ) {
                        changeListView();
                    }
                }
                break;
        }
    }

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int pos = position;
            new AlertDialog.Builder(MySampleActivity.this)
                    .setTitle("알림")
                    .setMessage("삭제 하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Cursor cur = (Cursor) adapter.getItem(pos);
                            cur.moveToPosition(pos);
                            DicDb.delDicMySample(db, cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));

                            // 기록..
                            DicUtils.writeInfoToFile(getApplicationContext(), "MYSAMPLE_DELETE" + ":" + cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));

                            changeListView();
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
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

class MySampleCursorAdapter extends CursorAdapter {

    public MySampleCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_my_sample_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_c_msi_tv_foreign)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
        ((TextView) view.findViewById(R.id.my_c_msi_tv_han)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
        ((TextView) view.findViewById(R.id.my_c_msi_tv_date)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("TODAY"))));
    }
}
