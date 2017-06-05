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
import android.widget.ListView;
import android.widget.TextView;

public class GrammarActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    public GrammarCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("문법");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        changeListView();
    }

    public void changeListView() {
        Cursor cursor = db.rawQuery(DicQuery.getGrammar(), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_g_lv1);
        adapter = new GrammarCursorAdapter(getApplicationContext(), cursor, this);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
            bundle.putString("SCREEN", CommConstants.screen_grammar);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

}


class GrammarCursorAdapter extends CursorAdapter {
    int fontSize = 0;
    private Cursor mCursor;
    private Activity mActivity;

    private Context mContext;

    static class ViewHolder {
        protected String grammar;
        protected String mean;
        protected String description;
        protected String samples;
    }

    public GrammarCursorAdapter(Context context, Cursor cursor, Activity activity) {
        super(context, cursor, 0);
        mContext = context;
        mCursor = cursor;
        mActivity = activity;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_grammar_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        view.setTag(viewHolder);

        //Item 선택
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vViewHolder = (ViewHolder) v.getTag();

                Intent intent = new Intent(mActivity.getApplication(), GrammarViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("grammar", (vViewHolder.grammar == null ? "" : vViewHolder.grammar));
                bundle.putString("mean", (vViewHolder.mean == null ? "" : vViewHolder.mean));
                bundle.putString("description", (vViewHolder.description == null ? "" : vViewHolder.description));
                bundle.putString("samples", (vViewHolder.samples == null ? "" : vViewHolder.samples));
                intent.putExtras(bundle);

                mActivity.startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.grammar = cursor.getString(cursor.getColumnIndexOrThrow("GRAMMAR"));
        viewHolder.mean = cursor.getString(cursor.getColumnIndexOrThrow("MEAN"));
        viewHolder.description = cursor.getString(cursor.getColumnIndexOrThrow("DESCRIPTION"));
        viewHolder.samples = cursor.getString(cursor.getColumnIndexOrThrow("SAMPLES"));

        ((TextView) view.findViewById(R.id.my_c_gi_tv_grammar)).setText(cursor.getString(cursor.getColumnIndexOrThrow("GRAMMAR")));
        ((TextView) view.findViewById(R.id.my_c_gi_tv_mean)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_c_gi_tv_grammar)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_c_gi_tv_mean)).setTextSize(fontSize);
    }
}