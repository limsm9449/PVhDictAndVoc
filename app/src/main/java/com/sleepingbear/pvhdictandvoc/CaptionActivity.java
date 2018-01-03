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

public class CaptionActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;
    private CaptionCursorAdapter adapter;

    private Cursor cursor;
    public Spinner s_group;
    private String categoryGroupCode = "G001";
    private String dramaCode;
    private String dramaTitle;
    private String dramaDirectory;


    private FileDownloadTask task;

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

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        checkDramaVersion();

        DicUtils.setAdView(this);
    }

    public void initScreen() {
        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        Cursor cursor = db.rawQuery(CaptionQuery.getDramaGroupKind(), null);
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
        cursor = db.rawQuery(CaptionQuery.getDramaList(categoryGroupCode), null);

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

            if ( CaptionQuery.existDramaCaption(db, cur.getString(cur.getColumnIndexOrThrow("KIND"))) ) {
                Bundle bundle = new Bundle();
                bundle.putString("CODE", cur.getString(cur.getColumnIndexOrThrow("KIND")));
                bundle.putString("TITLE", cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

                Intent intent = new Intent(getApplication(), CaptionViewActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            } else {
                dramaCode = cur.getString(cur.getColumnIndexOrThrow("KIND"));
                dramaTitle = cur.getString(cur.getColumnIndexOrThrow("KIND_NAME"));
                dramaDirectory = cur.getString(cur.getColumnIndexOrThrow("DIRECTORY"));

                if ( DicUtils.isNetWork(CaptionActivity.this) ) {
                    taskKind = "DRAMA_CAPTION";
                    task = new FileDownloadTask();
                    task.execute();
                } else {
                    new android.support.v7.app.AlertDialog.Builder(CaptionActivity.this)
                            .setTitle("알림")
                            .setMessage("네트워크에 연결되어있지 않습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }
            }
        }
    };


    public void checkDramaVersion() {
        if ( DicUtils.isNetWork(this) ) {
            taskKind = "DRAMA_VERSION";
            task = new FileDownloadTask();
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
                if ( "DRAMA_VERSION".equals(taskKind) ) {
                    version = DicUtils.getUrlText("http://limsm9449data.cafe24.com/drama/vn/vnCaption.txt");

                    if ( !DicUtils.getPreferences(CaptionActivity.this, "CAPTION_VERSION", "-").equals(version) ||
                            CaptionQuery.existDrama(db) == false) {
                        // 드라마 초기화
                        CaptionQuery.initDrama(db);

                        try {
                            db.beginTransaction();
                            String[] list = DicUtils.getUrlText("http://limsm9449data.cafe24.com/drama/vn/vnList.txt").split("\n");
                            for (int i = 0; i < list.length; i++) {
                                String[] drama = list[i].split("\\^");
                                CaptionQuery.insDrama(db, drama[0], drama[1], drama[2]);
                            }
                            db.setTransactionSuccessful();
                            db.endTransaction();
                        } catch ( Exception e ) {
                            DicUtils.dicLog("DRAMA_VERSION 에러 = " + e.toString());
                            db.endTransaction();
                        }

                        DicUtils.setPreferences( CaptionActivity.this, "CAPTION_VERSION", version );
                    }
                } else if ( "DRAMA_CAPTION".equals(taskKind) ) {
                    String[] list = DicUtils.getUrlText("http://limsm9449data.cafe24.com/drama/vn/" + dramaDirectory + "/" + dramaCode + ".txt").split("\n");

                    try {
                        db.beginTransaction();
                        for ( int i = 0; i < list.length; i++ ) {
                            String[] caption = list[i].split("\\^");
                            CaptionQuery.insCaption(db, dramaCode, caption[0], caption[1], caption[2]);
                        }
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    } catch ( Exception e ) {
                        DicUtils.dicLog("DRAMA_CAPTION 에러 = " + e.toString());
                        db.endTransaction();
                    }
                /*
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
                */
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

            if ( "DRAMA_VERSION".equals(taskKind) ) {
                /*
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
                */

                initScreen();
            /*
            } else if ( "CAPTION_ZIP".equals(taskKind) ) {
                initScreen();
            */
            } else if ( "DRAMA_CAPTION".equals(taskKind) ) {
                Bundle bundle = new Bundle();
                bundle.putString("CODE", dramaCode);
                bundle.putString("TITLE", dramaTitle);

                Intent intent = new Intent(getApplication(), CaptionViewActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
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