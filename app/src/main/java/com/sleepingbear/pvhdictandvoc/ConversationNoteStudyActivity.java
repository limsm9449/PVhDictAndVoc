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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.HashMap;
import java.util.Random;

public class ConversationNoteStudyActivity extends AppCompatActivity implements View.OnClickListener {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private TextView my_tv_han;
    private TextView my_tv_foreign;
    private String currForeign;
    private String currSeq;

    private String kind;
    private String sampleSeq;
    private String sqlWhere;

    ConversationNoteStudySearchTask task;

    private int fontSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_note_study);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        Bundle b = this.getIntent().getExtras();
        kind = b.getString("kind");
        sampleSeq = b.getString("sampleSeq");
        sqlWhere = b.getString("sqlWhere");

        ActionBar ab = getSupportActionBar();
        ab.setTitle("회화 학습");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        my_tv_han = (TextView) this.findViewById(R.id.my_tv_han);
        my_tv_foreign = (TextView) this.findViewById(R.id.my_tv_foreign);

        my_tv_han.setTextSize(fontSize);
        my_tv_foreign.setTextSize(fontSize);

        this.findViewById(R.id.my_iv_left).setOnClickListener(this);
        this.findViewById(R.id.my_iv_right).setOnClickListener(this);

        this.findViewById(R.id.my_iv_view).setOnClickListener(this);
        this.findViewById(R.id.my_iv_hide).setOnClickListener(this);
        this.findViewById(R.id.my_iv_hide).setVisibility(View.GONE);

        //리스트 내용 변경
        changeListView();

        DicUtils.setAdView(this);
    }

    public void changeListView() {
        if (task != null) {
            return;
        }
        task = new ConversationNoteStudySearchTask();
        task.execute();
    }

    public void getData() {
        DicUtils.dicLog(this.getClass().toString() + " getData");
        if ( db != null ) {
            if ( "PATTERN".equals(kind) || "IDIOM".equals(kind) ) {
                cursor = db.rawQuery(DicQuery.getPatternSampleList(sqlWhere), null);
            } else if ( "SAMPLE".equals(kind) ) {
                cursor = db.rawQuery(DicQuery.getSample(sampleSeq), null);
            }else {
                cursor = db.rawQuery(DicQuery.getNoteList(kind), null);
            }
        }
    }

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        switch (v.getId()) {
            case R.id.my_iv_left:
                this.findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
                this.findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);

                if ( !cursor.isFirst() ) {
                    cursor.moveToPrevious();
                    conversationShow();
                }

                break;
            case R.id.my_iv_right:
                this.findViewById(R.id.my_iv_hide).setVisibility(View.GONE);
                this.findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);

                if ( !cursor.isLast() ) {
                    cursor.moveToNext();
                    conversationShow();
                } else {
                    changeListView();
                }
                break;
            case R.id.my_iv_view:
                this.findViewById(R.id.my_iv_view).setVisibility(View.GONE);
                this.findViewById(R.id.my_iv_hide).setVisibility(View.VISIBLE);

                my_tv_foreign.setText(foreign);
                break;
            case R.id.my_iv_hide:
                this.findViewById(R.id.my_iv_view).setVisibility(View.VISIBLE);
                this.findViewById(R.id.my_iv_hide).setVisibility(View.GONE);

                conversationShow();
                break;
            default:
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

                if ( foreign.equals( currForeign) ) {
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
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

                    // 광고 추가
                    if ( CommConstants.isFreeApp ) {
                        PublisherAdView mPublisherAdView = new PublisherAdView(this);
                        mPublisherAdView.setAdSizes(new AdSize(300, 250));
                        mPublisherAdView.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));

                        // Create an ad request.
                        PublisherAdRequest.Builder publisherAdRequestBuilder = new PublisherAdRequest.Builder();
                        ((RelativeLayout) dialog_layout.findViewById(R.id.my_rl_admob)).addView(mPublisherAdView);

                        mPublisherAdView.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();

                                //개별 조회면은 다음 버튼을 안보이게 해준다.
                                if ( cursor.getCount() > 1 ) {
                                    dialog_layout.findViewById(R.id.my_b_next).setVisibility(View.VISIBLE);
                                }
                                dialog_layout.findViewById(R.id.my_b_close).setVisibility(View.VISIBLE);
                                dialog_layout.findViewById(R.id.my_b_detail).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdFailedToLoad(int i) {
                                super.onAdFailedToLoad(i);

                                if ( cursor.getCount() > 1 ) {
                                    dialog_layout.findViewById(R.id.my_b_next).setVisibility(View.VISIBLE);
                                }
                                dialog_layout.findViewById(R.id.my_b_close).setVisibility(View.VISIBLE);
                                dialog_layout.findViewById(R.id.my_b_detail).setVisibility(View.VISIBLE);
                            }
                        });

                        // Start loading the ad.
                        mPublisherAdView.loadAd(publisherAdRequestBuilder.build());

                        dialog_layout.findViewById(R.id.my_b_next).setVisibility(View.GONE);
                        dialog_layout.findViewById(R.id.my_b_close).setVisibility(View.GONE);
                        dialog_layout.findViewById(R.id.my_b_detail).setVisibility(View.GONE);
                    } else {
                        dialog_layout.findViewById(R.id.my_rl_admob).setVisibility(View.GONE);
                    }

                    dialog_layout.findViewById(R.id.my_b_next).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!cursor.isLast()) {
                                cursor.moveToNext();
                                conversationShow();
                            } else {
                                changeListView();
                            }

                            alertDialog.dismiss();
                        }
                    });
                    dialog_layout.findViewById(R.id.my_b_close).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();

                            if ( cursor.getCount() == 1 ) {
                                finishActivity();
                            }
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

                    FlowLayout wordArea = (FlowLayout) this.findViewById(R.id.my_ll_conversation_word);
                    wordArea.removeAllViews();
                }

                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", "NOTE_STUDY");

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
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

            FlowLayout wordArea = (FlowLayout) this.findViewById(R.id.my_ll_conversation_word);
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

    private void finishActivity() {
        this.finish();
    }

    private class ConversationNoteStudySearchTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(ConversationNoteStudyActivity.this);
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
            if ( cursor.getCount() == 1 ) {
                findViewById(R.id.my_iv_left).setVisibility(View.GONE);
                findViewById(R.id.my_iv_right).setVisibility(View.GONE);
            }

            cursor.moveToFirst();
            conversationShow();

            pd.dismiss();
            task = null;

            super.onPostExecute(result);
        }
    }
}
