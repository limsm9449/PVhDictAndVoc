package com.sleepingbear.pvhdictandvoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Random;

public class ConversationStudyActivity extends AppCompatActivity implements View.OnClickListener {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private TextView my_tv_han;
    private TextView my_tv_foreign;
    private String currForeign;
    private String currSeq;
    private int difficult = 1;
    private boolean isStart = false;

    private int fontSize = 0;

    ConversationStudySearchTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_study);

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

        my_tv_han = (TextView) findViewById(R.id.my_tv_han);
        my_tv_foreign = (TextView) findViewById(R.id.my_tv_foreign);

        my_tv_han.setTextSize(fontSize);
        my_tv_foreign.setTextSize(fontSize);

        findViewById(R.id.my_iv_left).setOnClickListener(this);
        findViewById(R.id.my_iv_right).setOnClickListener(this);

        findViewById(R.id.my_iv_view).setOnClickListener(this);
        findViewById(R.id.my_iv_hide).setOnClickListener(this);

        findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);
        findViewById(R.id.my_iv_hide).setVisibility(View.GONE);

        findViewById(R.id.my_rb_easy).setOnClickListener(this);
        findViewById(R.id.my_rb_normal).setOnClickListener(this);
        findViewById(R.id.my_rb_difficult).setOnClickListener(this);

        //리스트 내용 변경
        changeListView(true);
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
            bundle.putString("SCREEN", CommConstants.screen_conversationStudy);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView(boolean isKeyin) {
        if ( isKeyin ) {
            if (task != null) {
                return;
            }
            task = new ConversationStudySearchTask();
            task.execute();
        }
    }

    public void getData() {
        DicUtils.dicLog(this.getClass().toString() + " getData");
        if ( db != null ) {
            cursor = db.rawQuery(DicQuery.getConversationStudyList(difficult), null);

            if ( cursor.getCount() == 0 ) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        switch (v.getId()) {
            case R.id.my_iv_left:
                findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
                findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);

                if ( !cursor.isFirst() ) {
                    cursor.moveToPrevious();
                    conversationShow();
                }

                isStart = false;

                break;
            case R.id.my_iv_right:
                findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
                findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);

                if ( isStart ) {
                    DicDb.insConversationStudy(db, currSeq, DicUtils.getDelimiterDate(DicUtils.getCurrentDate(),"."));
                    DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                }
                if ( !cursor.isLast() ) {
                    cursor.moveToNext();
                    conversationShow();
                } else {
                    changeListView(true);
                }
                break;
            case R.id.my_iv_view:
                my_tv_foreign.setText(foreign);
                findViewById(R.id.my_iv_view).setVisibility(View.GONE);
                findViewById(R.id.my_iv_hide).setVisibility(View.VISIBLE);

                break;
            case R.id.my_iv_hide:
                findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);
                findViewById(R.id.my_iv_hide).setVisibility(View.GONE);

                conversationShow();
                break;
            case R.id.my_rb_easy:
                difficult = 1;
                changeListView(true);
                break;
            case R.id.my_rb_normal:
                difficult = 2;
                changeListView(true);
                break;
            case R.id.my_rb_difficult:
                difficult = 3;
                changeListView(true);
                break;
            default:
                isStart = true;

                String foreign = (String)my_tv_foreign.getText();

                //영문보기를 클릭하고 단어 클릭시 오류가 발생해서 체크를 해줌.
                if ( foreign.length() >= currForeign.length() ) {
                    Toast.makeText(this, "Refresh 버튼을 클릭한 후에 단어를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if ( "".equals(foreign) ) {
                    foreign = ((String)v.getTag()).trim();
                } else {
                    foreign += " " + ((String)v.getTag()).trim();
                }

                if ( foreign.equals( currForeign.substring( 0, foreign.length() ) ) ) {
                    my_tv_foreign.setText(foreign);
                    v.setBackgroundColor(Color.rgb(189, 195, 195));
                }

                //정답이면...
                if ( foreign.equals( currForeign) ) {
                    if ( isStart ) {
                        DicDb.insConversationStudy(db, currSeq, DicUtils.getDelimiterDate(DicUtils.getCurrentDate(),"."));
                        DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View dialog_layout = inflater.inflate(R.layout.dialog_correct_answer, null);

                    //dialog 생성..
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setView(dialog_layout);
                    final android.app.AlertDialog alertDialog = builder.create();

                    ((TextView) dialog_layout.findViewById(R.id.my_tv_han)).setText(my_tv_han.getText());
                    ((TextView) dialog_layout.findViewById(R.id.my_tv_foreign)).setText(my_tv_foreign.getText());

                    int fontSize = Integer.parseInt( DicUtils.getPreferencesValue( getApplicationContext(), CommConstants.preferences_font ) );
                    ((TextView) dialog_layout.findViewById(R.id.my_tv_han)).setTextSize(fontSize);
                    ((TextView) dialog_layout.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);

                    dialog_layout.findViewById(R.id.my_b_next).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!cursor.isLast()) {
                                cursor.moveToNext();
                                conversationShow();
                            } else {
                                changeListView(true);
                            }

                            alertDialog.dismiss();
                        }
                    });
                    dialog_layout.findViewById(R.id.my_b_close).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
                    dialog_layout.findViewById(R.id.my_b_detail).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putString("foreign", (String)my_tv_foreign.getText());
                            bundle.putString("han", (String)my_tv_han.getText());
                            bundle.putString("sampleSeq", currSeq);

                            Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);

                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

                    FlowLayout wordArea = (FlowLayout) findViewById(R.id.my_ll_conversation_word);
                    wordArea.removeAllViews();
                }

                break;
        }
    }

    private String foreign = "";
    private String[] foreignArr;
    public void conversationShow() {
        if ( cursor.getCount() > 0 ) {
            currSeq = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));
            currForeign = cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"));
            my_tv_han.setText(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2")));
            my_tv_foreign.setText("");

            foreign = cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"));

            FlowLayout wordArea = (FlowLayout) findViewById(R.id.my_ll_conversation_word);
            wordArea.removeAllViews();

            foreignArr = getRandForeign(foreign.split(" "));
            for ( int i = 0; i < foreignArr.length; i++ ) {
                Button btn = new Button(this);
                btn.setBackgroundColor(Color.rgb(249, 151, 53));
                btn.setTextColor(Color.rgb(255, 255, 255));
                btn.setText( DicUtils.getBtnString( foreignArr[i] ) );
                btn.setAllCaps(false);
                btn.setTextSize(18);

                btn.setLayoutParams((new FlowLayout.LayoutParams(3, 3)));

                btn.setId(i);
                btn.setTag( DicUtils.getBtnString( foreignArr[i] ) );
                btn.setGravity(Gravity.TOP);
                btn.setOnClickListener(this);
                wordArea.addView(btn);
            }

            isStart = false;
        } else {
            my_tv_han.setText("");
            my_tv_foreign.setText("");
            currForeign = "";
        }
    }

    public String[] getRandForeign(String[] arr) {
        String[] rtnArr = new String[arr.length];

        Random random = new Random();
        HashMap hm = new HashMap();
        int cnt = 0;
        while ( true ) {
            int randomIdx = random.nextInt(arr.length);
            if ( !hm.containsKey(randomIdx + "") ) {
                hm.put(randomIdx + "", randomIdx + "");
                rtnArr[cnt++] = arr[randomIdx];
            }

            if ( cnt == arr.length ) {
                break;
            }
        }

        String str1 = "";
        String str2 = "";
        for ( int i = 0; i < arr.length; i++ ) {
            str1 += arr[i] + " ";
            str2 += rtnArr[i] + " ";
        }
        DicUtils.dicLog(str1 + " : " + str2);

        return rtnArr;
    }

    private class ConversationStudySearchTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ConversationStudyActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            getData();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            cursor.moveToFirst();
            conversationShow();

            pd.dismiss();
            task = null;

            super.onPostExecute(result);
        }
    }
}
