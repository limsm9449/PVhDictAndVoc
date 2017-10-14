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
import android.widget.Toast;

public class PatternActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private PatternCursorAdapter adapter;

    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        changeListView();

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
            bundle.putString("SCREEN", CommConstants.screen_pattern);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        cursor = db.rawQuery(DicQuery.getPatternList(), null);

        if ( cursor.getCount() == 0 ) {
            Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView listView = (ListView) findViewById(R.id.my_f_convation_lv);
        adapter = new PatternCursorAdapter(getApplicationContext(), cursor, 0);
        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("PATTERN", cur.getString(cur.getColumnIndexOrThrow("PATTERN")));
            bundle.putString("DESC", cur.getString(cur.getColumnIndexOrThrow("DESC")));
            bundle.putString("SQL_WHERE", cur.getString(cur.getColumnIndexOrThrow("SQL_WHERE")));

            Intent intent = new Intent(getApplication(), PatternViewActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("kind", "PATTERN");
            bundle.putString("sqlWhere", cur.getString(cur.getColumnIndexOrThrow("SQL_WHERE")));

            Intent intent = new Intent(getApplicationContext(), ConversationNoteStudyActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);

            return true;
        }
    };
}

class PatternCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public PatternCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_pattern_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_pattern)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("PATTERN"))));
        ((TextView) view.findViewById(R.id.my_tv_pattern_desc)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("DESC"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_pattern)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_pattern_desc)).setTextSize(fontSize);
    }

}