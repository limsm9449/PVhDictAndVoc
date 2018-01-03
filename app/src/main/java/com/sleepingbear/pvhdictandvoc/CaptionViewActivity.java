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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class CaptionViewActivity extends AppCompatActivity implements View.OnClickListener  {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private CaptionViewCursorAdapter adapter;
    private ListView listView;
    public String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = this.getIntent().getExtras();
        code = b.getString("CODE");

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(b.getString("TITLE"));

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        findViewById(R.id.my_rb_all).setOnClickListener(this);
        findViewById(R.id.my_rb_han).setOnClickListener(this);
        findViewById(R.id.my_rb_foreign).setOnClickListener(this);

        getListView();

        DicUtils.setAdView(this);
    }

    public void getListView() {
        Cursor cursor = db.rawQuery(CaptionQuery.getDramaCaptionList(code), null);
        listView = (ListView) this.findViewById(R.id.my_lv);
        adapter = new CaptionViewCursorAdapter(getApplicationContext(), cursor, this);
        listView.setAdapter(adapter);
        listView.setFastScrollAlwaysVisible(true);
        listView.setFastScrollEnabled(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cur = (Cursor) adapter.getItem(i);

                Bundle bundle = new Bundle();
                bundle.putString("foreign", cur.getString(cur.getColumnIndexOrThrow("LANG_FOREIGN")));
                bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("LANG_HAN")));

                Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
        listView.setSelection(0);
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
            bundle.putString("SCREEN", CommConstants.screen_captionView);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_rb_all ) {
            adapter.setLang("A");
        } else if ( v.getId() == R.id.my_rb_han ) {
            adapter.setLang("H");
        } else if ( v.getId() == R.id.my_rb_foreign ) {
            adapter.setLang("F");
        }
    }
}


class CaptionViewCursorAdapter extends CursorAdapter {
    int fontSize = 0;
    private String lang = "A";

    public CaptionViewCursorAdapter(Context context, Cursor cursor, Activity activity) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_caption_view_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String tempTime = String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("TIME")));
        String timeStr = "";
        if ( tempTime.length() == 1 ) {
            timeStr = "00:00." + tempTime;
        } else {
            int calcTime = Integer.parseInt(tempTime.substring(0, tempTime.length() - 1));
            int minute = calcTime / 60;
            int sec = calcTime - minute * 60;
            timeStr = ( minute < 10 ? "0" : "" ) + minute + ":" + ( sec < 10 ? "0" : "" ) + sec + "." +  tempTime.substring(tempTime.length() - 1, tempTime.length());
        }

        ((TextView) view.findViewById(R.id.my_tv_time)).setText(timeStr);
        ((TextView) view.findViewById(R.id.my_tv_han)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("LANG_HAN"))));
        ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("LANG_FOREIGN"))));

        if ( "A".equals(lang) ) {
            view.findViewById(R.id.my_tv_han).setVisibility(View.VISIBLE);
            view.findViewById(R.id.my_tv_foreign).setVisibility(View.VISIBLE);
        } else if ( "H".equals(lang) ) {
            view.findViewById(R.id.my_tv_han).setVisibility(View.VISIBLE);
            view.findViewById(R.id.my_tv_foreign).setVisibility(View.GONE);
        } else if ( "F".equals(lang) ) {
            view.findViewById(R.id.my_tv_han).setVisibility(View.GONE);
            view.findViewById(R.id.my_tv_foreign).setVisibility(View.VISIBLE);
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_han)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);
    }

    public void setLang(String lang) {
        this.lang = lang;

        notifyDataSetChanged();
    }

}