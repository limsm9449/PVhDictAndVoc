package com.sleepingbear.pvhdictandvoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class News2Activity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private News2CursorAdapter adapter;
    private int sNews = 0;
    private int sCategory = 0;
    private int sSeq = 0;
    private String sTitle = "";
    private String sUrl = "";

    private News2Task task;
    private String taskKind = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] newCodes = DicUtils.getNews("C");
                final String[] newNames = DicUtils.getNews("N");

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(News2Activity.this);
                dlg.setTitle("뉴스 선택");
                dlg.setSingleChoiceItems(newNames, sNews, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sNews = arg1;

                        DicUtils.setPreferences(getApplicationContext(), "sNews", Integer.toString(sNews));
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActionBar ab = getSupportActionBar();
                        ab.setTitle(newNames[sNews]);

                        changeSpinner(newCodes[sNews], 0);
                    }
                });
                dlg.show();
            }
        });


        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //이전 기록
        sNews = Integer.parseInt(DicUtils.getPreferences(getApplicationContext(), "sNews", "0"));
        sCategory = Integer.parseInt(DicUtils.getPreferences(getApplicationContext(), "sCategory", "0"));

        String[] newNames = DicUtils.getNews("N");
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(newNames[sNews]);

        String[] newCodes = DicUtils.getNews("C");
        changeSpinner(newCodes[sNews], sCategory);

        DicUtils.setAdView(this);
    }

    public void changeSpinner(String newsCode, int pos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, DicUtils.getNewsCategory(newsCode, "N"));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = (Spinner) findViewById(R.id.my_s_category);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                sCategory = position;

                DicUtils.setPreferences(getApplicationContext(), "sCategory", Integer.toString(sCategory));

                String newCode = DicUtils.getNews("C")[sNews];
                if ( DicUtils.equalPreferencesDate(getApplicationContext(), newCode + "_" + DicUtils.getNewsCategory(newCode, "C")[sCategory]) ) {
                    changeListView();
                } else {
                    taskKind = "NEWS_LIST";
                    task = new News2Task();
                    task.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if ( pos > adapter.getCount() - 1 ) {
            pos = 0;
        }
        spinner.setSelection(pos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_web) {
            String newCode = DicUtils.getNews("C")[sNews];

            Bundle bundle = new Bundle();
            bundle.putString("kind", DicUtils.getNews("W")[sNews]);
            bundle.putString("url", DicUtils.getNewsCategory(newCode, "U")[sCategory]);

            Intent helpIntent = new Intent(getApplication(), NewsWebViewActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);
        } else if (id == R.id.action_refresh) {
            taskKind = "NEWS_LIST";
            task = new News2Task();
            task.execute();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_news2);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        String newsCode = DicUtils.getNews("C")[sNews];
        String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

        Cursor cursor = db.rawQuery(DicQuery.getNewsList(newsCode, categoryCode), null);
        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new News2CursorAdapter(this, cursor, db, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            sSeq = position;

            Cursor cur = (Cursor) adapter.getItem(position);

            sTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            sUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

            taskKind = "NEWS_CONTENTS";
            task = new News2Task();
            task.execute();
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            sSeq = i;

            Cursor cur = (Cursor) adapter.getItem(i);

            sTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            sUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

            taskKind = "NEWS_CONTENTS_LONG";
            task = new News2Task();
            task.execute();

            return true;
        }
    };

    private class News2Task extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;
        private String contents = "";

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(News2Activity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if ( taskKind.equals("NEWS_LIST") ) {
                //기사 리스트를 읽어 온다.
                String newsCode = DicUtils.getNews("C")[sNews];
                String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

                DicUtils.getNewsCategoryNews(db, newsCode, categoryCode, DicUtils.getNewsCategory(newsCode, "U")[sCategory]);
            } else if ( taskKind.equals("NEWS_CONTENTS") || taskKind.equals("NEWS_CONTENTS_LONG") ) {
                //기사를 읽어 온다.
                String newsCode = DicUtils.getNews("C")[sNews];
                String categoryCode = DicUtils.getNewsCategory(newsCode, "C")[sCategory];

                contents = DicUtils.getNewsContents(db, newsCode, sSeq, sUrl);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            if ( taskKind.equals("NEWS_LIST") ) {
                changeListView();
            } else if ( taskKind.equals("NEWS_CONTENTS") ) {
                Cursor cur = (Cursor) adapter.getItem(sSeq);

                Bundle bundle = new Bundle();
                String newsCode = DicUtils.getNews("C")[sNews];
                bundle.putString("KIND", DicUtils.getNews("W")[sNews]);
                bundle.putString("CATEGORY", DicUtils.getNewsCategory(newsCode, "N")[sCategory]);
                bundle.putString("TITLE", cur.getString(cur.getColumnIndexOrThrow("TITLE")));
                bundle.putString("URL", cur.getString(cur.getColumnIndexOrThrow("URL")));
                bundle.putString("CONTENTS", contents);

                if ( "".equals(contents) ) {
                    Toast.makeText(getApplicationContext(), "검색된 뉴스 기사가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent helpIntent = new Intent(getApplication(), News2ViewActivity.class);
                    helpIntent.putExtras(bundle);
                    startActivity(helpIntent);
                }
            } else if ( taskKind.equals("NEWS_CONTENTS_LONG") ) {
                Cursor cur = (Cursor) adapter.getItem(sSeq);

                Bundle bundle = new Bundle();
                bundle.putString("kind", DicUtils.getNews("W")[sNews]);
                bundle.putString("url", cur.getString(cur.getColumnIndexOrThrow("URL")));

                Intent helpIntent = new Intent(getApplication(), NewsWebViewActivity.class);
                helpIntent.putExtras(bundle);
                startActivity(helpIntent);
            }

            super.onPostExecute(result);
        }
    }
}

class News2CursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    int fontSize = 0;

    public News2CursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_news2_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_title)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));
        ((TextView) view.findViewById(R.id.my_tv_desc)).setText(cursor.getString(cursor.getColumnIndexOrThrow("DESC")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_title)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_desc)).setTextSize(fontSize);

        if ( "".equals(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("DESC")))) ) {
            view.findViewById(R.id.my_tv_desc).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.my_tv_desc).setVisibility(View.VISIBLE);
        }

    }
}