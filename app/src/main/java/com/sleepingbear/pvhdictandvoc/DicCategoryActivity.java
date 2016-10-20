package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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


public class DicCategoryActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    public DicCategoryCursorAdapter adapter;
    public Spinner s_dicgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dic_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("카테고리");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(DicQuery.getMainCategoryCount(), null);
        String[] from = new String[]{"KIND_NAME"};
        int[] to = new int[]{android.R.id.text1};
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_dicgroup = (Spinner) this.findViewById(R.id.my_c_dc_s_dicgroup);
        s_dicgroup.setAdapter(mAdapter);
        s_dicgroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        changeListView();
    }

    public void changeListView() {
        Cursor cursor = db.rawQuery(DicQuery.getSubCategoryCount(((Cursor) s_dicgroup.getSelectedItem()).getString(1)), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_dc_lv_dickind);
        adapter = new DicCategoryCursorAdapter(getApplicationContext(), cursor, this);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelection(0);
    }

    @Override
    // 상단 메뉴 구성
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    // 메뉴 상태 변경
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", "DIC_CATEGORY");

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

class DicCategoryCursorAdapter extends CursorAdapter {
    private String entryId = "";
    private String seq = "";
    private Cursor mCursor;
    private Activity mActivity;

    private Context mContext;

    static class ViewHolder {
        protected String kind;
        protected String kindName;
    }

    public DicCategoryCursorAdapter(Context context, Cursor cursor, Activity activity) {
        super(context, cursor, 0);
        mContext = context;
        mCursor = cursor;
        mActivity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_dic_category_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        view.setTag(viewHolder);

        //Item 선택
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vViewHolder = (ViewHolder) v.getTag();

                Intent intent = new Intent(mActivity.getApplication(), DicCategoryViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("kind", vViewHolder.kind);
                bundle.putString("kindName", vViewHolder.kindName);
                intent.putExtras(bundle);

                mActivity.startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.kind = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
        viewHolder.kindName = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));

        ((TextView) view.findViewById(R.id.my_c_dci_tv_category)).setText(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")));

        if ( "W".equals(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("KIND"))).substring(0,1)) ) {
            ((TextView) view.findViewById(R.id.my_c_dci_tv_cnt)).setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("W_CNT"))));
        } else {
            ((TextView) view.findViewById(R.id.my_c_dci_tv_cnt)).setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("S_CNT"))));
        }
    }
}