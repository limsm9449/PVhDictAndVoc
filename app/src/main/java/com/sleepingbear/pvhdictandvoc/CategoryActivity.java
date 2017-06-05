package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CategoryActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;
    private CategoryCursorAdapter adapter;

    private Cursor cursor;
    public Spinner s_group;
    private String categoryGroupCode = "C01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        Cursor cursor = db.rawQuery(DicQuery.getCategoryGroupKind(), null);
        String[] from = new String[]{"KIND_NAME"};
        int[] to = new int[]{android.R.id.text1};
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_group = (Spinner) findViewById(R.id.my_s_category);
        s_group.setAdapter(mAdapter);
        s_group.setSelection(0, true);
        s_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryGroupCode = ((Cursor) s_group.getSelectedItem()).getString(1);

                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //사이즈 설정
        View v = s_group.getSelectedView();
        ((TextView)v).setTextSize(fontSize);

        changeListView();
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
            bundle.putString("SCREEN", CommConstants.screen_category);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        cursor = db.rawQuery(DicQuery.getCategoryList(categoryGroupCode), null);

        if ( cursor.getCount() == 0 ) {
            Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new CategoryCursorAdapter(getApplicationContext(), cursor, 0);
        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("CATEGORY", cur.getString(cur.getColumnIndexOrThrow("CATEGORY")));
            bundle.putString("SAMPLES", cur.getString(cur.getColumnIndexOrThrow("SAMPLES")));

            Intent intent = new Intent(getApplication(), CategoryViewActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };
}

class CategoryCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public CategoryCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_category_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_category)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("CATEGORY"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_category)).setTextSize(fontSize);
    }

}