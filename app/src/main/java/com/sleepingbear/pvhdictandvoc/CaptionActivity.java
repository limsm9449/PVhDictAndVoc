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
import android.os.Environment;
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

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class CaptionActivity extends AppCompatActivity {

    private CaptionDbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;
    private CaptionCursorAdapter adapter;

    private Cursor cursor;
    public Spinner s_group;
    private String categoryGroupCode = "G001";


    private CaptionActivity.FileDownloadTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbDownload();

        DicUtils.setAdView(this);
    }

    public void initScreen() {
        dbHelper = new CaptionDbHelper(this);
        db = dbHelper.getWritableDatabase();

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        Cursor cursor = db.rawQuery(CaptionQuery.getCategoryGroupKind(), null);
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
            bundle.putString("SCREEN", CommConstants.screen_caption);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        cursor = db.rawQuery(CaptionQuery.getCategoryList(categoryGroupCode), null);

        if ( cursor.getCount() == 0 ) {
            Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new CaptionCursorAdapter(getApplicationContext(), cursor, 0);
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
            bundle.putString("CODE", cur.getString(cur.getColumnIndexOrThrow("KIND")));
            bundle.putString("TITLE", cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

            Intent intent = new Intent(getApplication(), CaptionViewActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };


    public void dbDownload() {
        if ( DicUtils.isNetWork(this) ) {
            taskKind = "CAPTION_TXT";
            task = new CaptionActivity.FileDownloadTask();
            task.execute();
        } else {
            initScreen();
        }
    }

    public String taskKind = "";
    public String version = "";
    public class FileDownloadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(CaptionActivity.this);
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
            try {
                if ( "CAPTION_TXT".equals(taskKind) ) {
                    Document doc = DicUtils.getDocument("http://limsm9449data.cafe24.com/vnCaption.txt");
                    version = doc.text().trim();
                } else if ( "CAPTION_ZIP".equals(taskKind) ) {
                    InputStream inputStream = new URL("http://limsm9449data.cafe24.com/vnCaption.zip").openStream();

                    File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName);
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }

                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.zip");
                    OutputStream out = new FileOutputStream(file);

                    int c = 0;
                    while ((c = inputStream.read()) != -1) {
                        out.write(c);
                    }
                    out.flush();
                    out.close();

                    //압축 해제
                    Decompress d = new Decompress(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.zip", Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/");
                    d.unzip();

                    //External에서 Internal로 옮김
                    int byteread = 0;
                    File externalFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.db");
                    if (externalFile.exists()) {
                        InputStream inStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.db");
                        FileOutputStream fos = new FileOutputStream("/data/data/" + getPackageName() + "/databases/vnCaption.db");
                        byte[] buffer = new byte[1024];
                        while ((byteread = inStream.read(buffer)) != -1) {
                            fos.write(buffer, 0, byteread);
                        }
                        inStream.close();
                        fos.close();
                    }

                    DicUtils.setPreferences( CaptionActivity.this, "CAPTION_VERSION", version );

                    //임시 파일 삭제
                    //(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.db")).delete();
                    //(new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/vnCaption.zip")).delete();
                }
            } catch ( Exception e ) {
                DicUtils.dicLog("Download 에러 = " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            if ( "CAPTION_TXT".equals(taskKind) ) {
                if ( !DicUtils.getPreferences(CaptionActivity.this, "CAPTION_VERSION", "-").equals(version) ) {
                    new android.support.v7.app.AlertDialog.Builder(CaptionActivity.this)
                            .setTitle("알림")
                            .setMessage("DB 파일이 변경되었습니다. DB 파일은 용량이 커서 다운로드시 시간이 걸릴 수 있습니다. 다운로드 하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    taskKind = "CAPTION_ZIP";
                                    task = new CaptionActivity.FileDownloadTask();
                                    task.execute();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initScreen();
                                }
                            })
                            .show();
                } else {
                    initScreen();
                }
            } else if ( "CAPTION_ZIP".equals(taskKind) ) {
                initScreen();
            }

            super.onPostExecute(result);
        }
    }
}

class CaptionCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public CaptionCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_caption_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_category)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_category)).setTextSize(fontSize);
    }

}