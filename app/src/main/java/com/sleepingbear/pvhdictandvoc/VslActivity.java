package com.sleepingbear.pvhdictandvoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

public class VslActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;

    private VslCursorAdapter adapter;

    public Spinner s_lvl1;
    public Spinner s_lvl2;
    public Spinner s_lvl3;
    SimpleCursorAdapter mAdapter1;
    SimpleCursorAdapter mAdapter2;
    SimpleCursorAdapter mAdapter3;
    private int mSelect = 0;

    private FileDownloadTask task;
    private String downloadURL = "";
    private String fileName = "";

    private MediaPlayer mp;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vsl);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        String[] from = new String[]{"LVL3"};
        int[] to = new int[]{android.R.id.text1};
        Cursor cursor1 = db.rawQuery(DicQuery.getVsl1Class(), null);
        mAdapter1 = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor1, from, to);
        mAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_lvl1 = (Spinner) findViewById(R.id.my_s_lvl1);
        s_lvl1.setAdapter(mAdapter1);
        s_lvl1.setSelection(0, true);
        s_lvl1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lvl2 = ((Cursor)s_lvl1.getSelectedItem()).getString(1);

                mAdapter2.changeCursor(db.rawQuery(DicQuery.getVsl1ClassDetail("G_" + lvl2), null));
                mAdapter2.notifyDataSetChanged();

                s_lvl2.setSelection(0, true);
                s_lvl3.setSelection(0, true);

                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Cursor cursor2 = db.rawQuery(DicQuery.getVsl1ClassDetail("-"), null);
        mAdapter2 = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor2, from, to);
        mAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_lvl2 = (Spinner) findViewById(R.id.my_s_lvl2);
        s_lvl2.setAdapter(mAdapter2);
        s_lvl2.setSelection(0, true);
        s_lvl2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_lvl3.setSelection(0, true);

                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Cursor cursor3 = db.rawQuery(DicQuery.getVsl1Kind(), null);
        mAdapter3 = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor3, from, to);
        mAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_lvl3 = (Spinner) findViewById(R.id.my_s_lvl3);
        s_lvl3.setAdapter(mAdapter3);
        s_lvl3.setSelection(0, true);
        s_lvl3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //사이즈 설정
        ((TextView)s_lvl1.getSelectedView()).setTextSize(fontSize);
        ((TextView)s_lvl2.getSelectedView()).setTextSize(fontSize);
        ((TextView)s_lvl3.getSelectedView()).setTextSize(fontSize);

        //리스트 내용 변경
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
            bundle.putString("SCREEN", CommConstants.screen_vsl);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        if ( db != null ) {
            String gClass = ((Cursor)s_lvl1.getSelectedItem()).getString(1);
            String gClassSub = ((Cursor)s_lvl2.getSelectedItem()).getString(1);
            String gKind = ((Cursor)s_lvl3.getSelectedItem()).getString(1);

            Cursor listCursor = db.rawQuery(DicQuery.getVslContents(gClass, gClassSub, gKind), null);
            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new VslCursorAdapter(this, listCursor, 0);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(itemClickListener);
            listView.setSelection(0);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            if ( "W".equals(cur.getString(cur.getColumnIndexOrThrow("LVL3"))) ) {
                HashMap wordsInfo = DicDb.getWordsInfo(db, cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
                if ( wordsInfo.containsKey(cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")) + "_ENTRY_ID")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("entryId", (String) wordsInfo.get(cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")) + "_ENTRY_ID"));

                    Intent intent = new Intent(getApplicationContext(), WordViewActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("foreign", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
                    bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));

                    Intent intent = new Intent(getApplicationContext(), SentenceViewActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            } else {
                if ( !"FILE".equals(cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")).substring(0, 4)) ) {
                    Bundle bundle = new Bundle();
                    bundle.putString("foreign", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
                    bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));

                    Intent intent = new Intent(getApplicationContext(), SentenceViewActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
    }

    public void mp3Download( String mp3File ) {
        final String fMp3File = mp3File;
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("MP3 파일은 용량이 커서 다운로드시 시간이 걸릴 수 있습니다. 다운로드 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = fMp3File.substring(0,4) + ".zip";

                        task = new FileDownloadTask();
                        task.execute();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void mp3Play( String mp3File, float mp3Speed ) {
        if ( "".equals(mp3File) ) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("왼쪽 다운로드 버튼을 클릭해서 MP3 파일을 먼저 다운로드 받으셔야 합니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            if ( mp != null ) {
                mp.release();
            }

            mp = new MediaPlayer();
            try{
                mp.setDataSource(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName + "/" + mp3File);
                mp.prepare();
            } catch ( Exception e ) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            mp.setLooping(false);
            mp.start();

            isPlaying = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(mp3Speed));
            }
        }
    }

    public void mp3Pause( String mp3File ) {
        if ( "".equals(mp3File) ) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("왼쪽 다운로드 버튼을 클릭해서 MP3 파일을 먼저 다운로드 받으셔야 합니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            if ( mp != null && isPlaying ) {
                mp.stop();
                mp.release();

                isPlaying = false;
            }
        }
    }

    public void mp3Speed( float mp3Speed ) {
        if ( mp != null ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ( isPlaying ) {
                    mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(mp3Speed));
                }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if ( mp != null ) {
            mp.release();
            mp = null;
        }
    }

    public class FileDownloadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(VslActivity.this);
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
                downloadURL = "";

                String mp3FileName = "";
                if ( "VSL1".equals(fileName.substring(0, 4)) ) {
                    mp3FileName = "VSL_MP3_1.zip";
                } else if ( "VSL2".equals(fileName.substring(0, 4)) ) {
                    mp3FileName = "VSL_MP3_2.zip";
                } else if ( "VSL3".equals(fileName.substring(0, 4)) ) {
                    mp3FileName = "VSL_MP3_3.zip";
                } else {
                    mp3FileName = "VSL_MP3_4.zip";
                }

                Document doc = DicUtils.getDocument("http://blog.naver.com/PostList.nhn?blogId=limsm9449&logNo=221105962247");
                String[] html = doc.html().split("\n");
                for ( int i = 0; i < html.length; i++ ) {
                    if ( html[i].indexOf("aPostFiles[1]") > -1 ) {
                        JSONArray ja = new JSONArray (html[i].replaceAll("aPostFiles\\[1\\] =", "").trim());

                        for (int si = 0; si < ja.length(); si++){
                            JSONObject obj = ja.getJSONObject(si);
                            if ( mp3FileName.equals(obj.getString("encodedAttachFileName")) ) {
                                downloadURL = obj.getString("encodedAttachFileUrl");
                                break;
                            }
                        }
                        break;
                    }
                }

                if ( !"".equals(downloadURL) ) {
                    InputStream inputStream = new URL(downloadURL).openStream();

                    File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName);
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }

                    File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName + "/" + fileName);
                    OutputStream out = new FileOutputStream(file);

                    int c = 0;
                    while ((c = inputStream.read()) != -1) {
                        out.write(c);
                    }
                    out.flush();
                    out.close();

                    Decompress d = new Decompress(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName + "/" + fileName, Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName + "/");
                    d.unzip();
                }
            } catch ( Exception e ) {
                DicUtils.dicLog("mp3Download 에러 = " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            adapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }
    }

}

class VslCursorAdapter extends CursorAdapter {
    int fontSize = 0;
    private Context mContext;

    static class ViewHolder {
        protected Cursor cursor;
        protected ImageView ivDownload;
        protected ImageView ivPlay;
        protected ImageView ivPause;
        protected Spinner sSpeed;
        protected String mp3File;
    }

    public VslCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
        mContext = context;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_vsl_item, parent, false);

        VslCursorAdapter.ViewHolder viewHolder = new VslCursorAdapter.ViewHolder();

        viewHolder.sSpeed = (Spinner) view.findViewById(R.id.my_s_speed);
        String[] speeds = new String[]{"0.4","0.6","0.8","1.0", "1.2", "1.4", "1.6"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this.mContext, android.R.layout.simple_spinner_dropdown_item, speeds);
        viewHolder.sSpeed.setAdapter(arrayAdapter);
        viewHolder.sSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                float selectedSpeed = Float.parseFloat(adapterView.getItemAtPosition(i).toString());

                if(mContext instanceof VslActivity){
                    ((VslActivity)mContext).mp3Speed(selectedSpeed);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        viewHolder.sSpeed.setSelection(3);

        viewHolder.ivDownload = (ImageView) view.findViewById(R.id.my_iv_download);
        viewHolder.ivPlay = (ImageView) view.findViewById(R.id.my_iv_play);
        viewHolder.ivPause = (ImageView) view.findViewById(R.id.my_iv_pause);
        viewHolder.ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mp3File = (String) v.getTag();

                if(mContext instanceof VslActivity){
                    ((VslActivity)mContext).mp3Download(mp3File);
                }
            }
        });
        viewHolder.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mp3File = ((ViewHolder) v.getTag()).mp3File;

                if(mContext instanceof VslActivity){
                    float mp3Speed = Float.parseFloat(((ViewHolder) v.getTag()).sSpeed.getSelectedItem().toString());
                    ((VslActivity)mContext).mp3Play(mp3File, mp3Speed);
                }
            }
        });
        viewHolder.ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mp3File = (String) v.getTag();

                if(mContext instanceof VslActivity){
                    ((VslActivity)mContext).mp3Pause(mp3File);
                }
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        VslCursorAdapter.ViewHolder viewHolder =(VslCursorAdapter.ViewHolder) view.getTag();
        viewHolder.cursor = cursor;
        if ( "D".equals(cursor.getString(cursor.getColumnIndexOrThrow("LVL3"))) &&
                cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")).length() > 4 &&
                "FILE".equals(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")).substring(0, 4)) ) {
            view.findViewById(R.id.my_tv_foreign).setVisibility(View.GONE);
            view.findViewById(R.id.my_tv_han).setVisibility(View.GONE);
            view.findViewById(R.id.my_rl_button).setVisibility(View.VISIBLE);

            String tMp3File = cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"));
            String mp3File = tMp3File.substring(5, tMp3File.length());

            File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.folderVslName + "/" + mp3File);
            if ( file.exists() ) {
                view.findViewById(R.id.my_iv_download).setVisibility(View.GONE);
                viewHolder.mp3File = mp3File;
                viewHolder.ivPlay.setTag(viewHolder);
                viewHolder.ivPause.setTag(mp3File);
            } else {
                view.findViewById(R.id.my_iv_download).setVisibility(View.VISIBLE);
                viewHolder.mp3File = "";
                viewHolder.ivDownload.setTag(mp3File);
                viewHolder.ivPlay.setTag(viewHolder);
                viewHolder.ivPause.setTag("");
            }
        } else {
            view.findViewById(R.id.my_tv_foreign).setVisibility(View.VISIBLE);
            view.findViewById(R.id.my_tv_han).setVisibility(View.VISIBLE);
            view.findViewById(R.id.my_rl_button).setVisibility(View.GONE);

            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")));
            ((TextView) view.findViewById(R.id.my_tv_han)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2")));

            if ("D".equals(cursor.getString(cursor.getColumnIndexOrThrow("LVL3")))) {
                ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextColor(ContextCompat.getColor(context, R.color.my_text_vsl_d));
            } else if ("W".equals(cursor.getString(cursor.getColumnIndexOrThrow("LVL3")))) {
                ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextColor(ContextCompat.getColor(context, R.color.my_text_vsl_w));
            } else if ("G".equals(cursor.getString(cursor.getColumnIndexOrThrow("LVL3")))) {
                ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextColor(ContextCompat.getColor(context, R.color.my_text_vsl_g));
            }

            if ( "".equals(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2")))) ) {
                view.findViewById(R.id.my_tv_han).setVisibility(View.GONE);
            }

            //사이즈 설정
            ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);
            ((TextView) view.findViewById(R.id.my_tv_han)).setTextSize(fontSize);
        }
    }

}
