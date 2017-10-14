package com.sleepingbear.pvhdictandvoc;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DictionaryHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private DictionaryHistoryCursorAdapter adapter;
    private boolean isAllCheck = false;

    public int mSelect = 0;

    private AppCompatActivity mMainActivity;

    private RelativeLayout editRl;

    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        findViewById(R.id.my_iv_all).setOnClickListener(this);
        findViewById(R.id.my_iv_delete).setOnClickListener(this);

        editRl = (RelativeLayout) findViewById(R.id.my_dictionary_history_rl);
        editRl.setVisibility(View.GONE);

        //리스트 내용 변경
        changeListView();

        DicUtils.setAdView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_news_click, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_exit).setVisible(false);

        if ( isEditing ) {
            menu.findItem(R.id.action_exit).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_edit) {
            isEditing = true;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        } else if (id == R.id.action_exit) {
            isEditing = false;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_dictionaryHistory);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        if ( db != null ) {
            Cursor listCursor = db.rawQuery(DicQuery.getDictionaryHistoryword(), null);
            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new DictionaryHistoryCursorAdapter(this, listCursor, db, 0);
            adapter.editChange(isEditing);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(itemClickListener);
            listView.setSelection(0);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ( !isEditing ) {
                Cursor cur = (Cursor) adapter.getItem(position);
                final String word = cur.getString(cur.getColumnIndexOrThrow("WORD"));

                Intent intent = new Intent(getApplication(), DictionaryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("word", word);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        }
    };

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        switch (v.getId()) {
            case R.id.my_iv_all :
                isAllCheck = !isAllCheck;
                adapter.allCheck(isAllCheck);
                break;
            case R.id.my_iv_delete :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    new android.support.v7.app.AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("삭제하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.delete();
                                    changeListView();

                                    DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }

                break;
        }
    }

    public void changeEdit( boolean isEditing ) {
        //처음에 오류가 발생하는 경우가 있음
        if ( editRl == null ) {
            return;
        }

        this.isEditing = isEditing;

        if ( isEditing ) {
            editRl.setVisibility(View.VISIBLE);
        } else {
            editRl.setVisibility(View.GONE);
        }

        adapter.editChange(isEditing);
    }
}

class DictionaryHistoryCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    public boolean[] isCheck;
    public int[] seq;
    private boolean isEditing = false;
    int fontSize = 0;

    public DictionaryHistoryCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );

        isCheck = new boolean[cursor.getCount()];
        seq = new int[cursor.getCount()];
        while ( cursor.moveToNext() ) {
            isCheck[cursor.getPosition()] = false;
            seq[cursor.getPosition()] = cursor.getInt(cursor.getColumnIndexOrThrow("SEQ"));
        }
        cursor.moveToFirst();
    }

    static class ViewHolder {
        protected int position;
        protected CheckBox cb;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_dictionary_history_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.cb = (CheckBox) view.findViewById(R.id.my_cb_check);
        viewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                ViewHolder viewHolder = (ViewHolder)buttonView.getTag();
                isCheck[viewHolder.position] = isChecked;
                notifyDataSetChanged();

                DicUtils.dicLog("onCheckedChanged : " + viewHolder.position);
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.position = cursor.getPosition();
        viewHolder.cb.setTag(viewHolder);

        //seq[cursor.getPosition()] = cursor.getInt(cursor.getColumnIndexOrThrow("SEQ"));
        //entryId[cursor.getPosition()] = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));

        ((TextView) view.findViewById(R.id.my_tv_word)).setText(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_word)).setTextSize(fontSize);

        if ( isCheck[cursor.getPosition()] ) {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_on_background);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }

        if ( isEditing ) {
            view.findViewById(R.id.my_dictionary_history_rl).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.my_dictionary_history_rl).setVisibility(View.GONE);
        }
    }

    public void allCheck(boolean chk) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            isCheck[i] = chk;
        }

        notifyDataSetChanged();
    }

    public void delete() {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.delSearchHistory(mDb, seq[i]);
            }
        }
    }

    public boolean isCheck() {
        boolean rtn = false;
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                rtn = true;
                break;
            }
        }

        return rtn;
    }

    public void editChange(boolean isEditing) {
        this.isEditing = isEditing;
        notifyDataSetChanged();
    }
}


