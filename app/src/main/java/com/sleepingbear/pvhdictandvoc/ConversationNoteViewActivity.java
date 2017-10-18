package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Locale;

public class ConversationNoteViewActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener  {
    private TextToSpeech myTTS;
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private ConversationNoteViewCursorAdapter adapter;
    private String kind;

    private boolean isAllCheck = false;
    private int mSelect;
    private boolean isEditing = false;
    private boolean isForeignView = false;
    private boolean isChange = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_note_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        myTTS = new TextToSpeech(this, this);

        Bundle b = this.getIntent().getExtras();
        kind = b.getString("kind");

        this.findViewById(R.id.my_iv_all).setOnClickListener(this);
        this.findViewById(R.id.my_iv_delete).setOnClickListener(this);
        this.findViewById(R.id.my_iv_copy).setOnClickListener(this);
        this.findViewById(R.id.my_iv_move).setOnClickListener(this);

        this.findViewById(R.id.my_c_rl_tool).setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setTitle(b.getString("kindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        changeListView();

        DicUtils.setAdView(this);
    }

    public void changeListView() {
        Cursor cursor = db.rawQuery(DicQuery.getNoteList(kind), null);

        ListView listView = (ListView) this.findViewById(R.id.my_c_note_lv);
        adapter = new ConversationNoteViewCursorAdapter(getApplicationContext(), cursor, db, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cur = (Cursor) adapter.getItem(i);
                adapter.setStatus( cur.getString(cur.getColumnIndexOrThrow("SEQ")) );
                adapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) adapter.getItem(position);
                final String sampleSeq = cur.getString(cur.getColumnIndexOrThrow("SEQ"));
                final String foreign = cur.getString(cur.getColumnIndexOrThrow("SENTENCE1"));
                final String han = cur.getString(cur.getColumnIndexOrThrow("SENTENCE2"));

                if ( "C01".equals(kind.substring(0, 3)) || "C02".equals(kind.substring(0, 3)) ) {
                    //메뉴 선택 다이얼로그 생성
                    final String[] kindCodes = new String[]{"M1","M2"};
                    final String[] kindCodeNames = new String[]{"회화 학습","문장 상세"};

                    final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(ConversationNoteViewActivity.this);
                    dlg.setTitle("메뉴 선택");
                    dlg.setSingleChoiceItems(kindCodeNames, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mSelect = arg1;
                        }
                    });
                    dlg.setNeutralButton("TTS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myTTS.speak(foreign, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                    dlg.setNegativeButton("취소", null);
                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ( mSelect == 0 ) {
                                Bundle bundle = new Bundle();
                                bundle.putString("kind", "SAMPLE");
                                bundle.putString("sampleSeq", sampleSeq);

                                Intent intent = new Intent(getApplication(), ConversationNoteStudyActivity.class);
                                intent.putExtras(bundle);

                                startActivity(intent);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString("foreign", foreign);
                                bundle.putString("han", han);
                                bundle.putString("sampleSeq", sampleSeq);

                                Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                                intent.putExtras(bundle);

                                startActivity(intent);
                            }
                        }
                    });
                    dlg.show();
                } else {
                    //메뉴 선택 다이얼로그 생성
                    Cursor cursor = db.rawQuery(DicQuery.getNoteKindContextMenu(true), null);
                    final String[] kindCodes = new String[cursor.getCount()];
                    final String[] kindCodeNames = new String[cursor.getCount()];

                    int idx = 0;
                    while (cursor.moveToNext()) {
                        kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                        kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                        idx++;
                    }
                    cursor.close();

                    final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(ConversationNoteViewActivity.this);
                    dlg.setTitle("메뉴 선택");
                    dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mSelect = arg1;
                        }
                    });
                    dlg.setNeutralButton("TTS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            myTTS.speak(foreign, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                    dlg.setNegativeButton("취소", null);
                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ( mSelect == 0 ) {
                                Bundle bundle = new Bundle();
                                bundle.putString("kind", "SAMPLE");
                                bundle.putString("sampleSeq", sampleSeq);

                                Intent intent = new Intent(getApplication(), ConversationNoteStudyActivity.class);
                                intent.putExtras(bundle);

                                startActivity(intent);
                            } else if ( mSelect == 1 ) {
                                Bundle bundle = new Bundle();
                                bundle.putString("foreign", foreign);
                                bundle.putString("han", han);
                                bundle.putString("sampleSeq", sampleSeq);

                                Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
                                intent.putExtras(bundle);

                                startActivity(intent);
                            } else {
                                DicDb.insConversationToNote(db, kindCodes[mSelect], sampleSeq);
                                isChange = true;
                            }
                        }
                    });
                    dlg.show();
                }

                return true;
            }
        });
        listView.setSelection(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_note, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isEditing) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_exit).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_exit).setVisible(false);
        }

        if ( isForeignView ) {
            menu.findItem(R.id.action_view).setVisible(false);
            menu.findItem(R.id.action_hide).setVisible(true);
        } else {
            menu.findItem(R.id.action_view).setVisible(true);
            menu.findItem(R.id.action_hide).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_edit) {
            isEditing = true;
            invalidateOptionsMenu();

            this.findViewById(R.id.my_c_rl_tool).setVisibility(View.VISIBLE);

            adapter.editChange(isEditing);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_exit) {
            isEditing = false;
            invalidateOptionsMenu();

            this.findViewById(R.id.my_c_rl_tool).setVisibility(View.GONE);

            adapter.editChange(isEditing);
            adapter.notifyDataSetChanged();

            if ( adapter.isChange ) {
                DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
            }
        } else if (id == R.id.action_view) {
            isForeignView = true;
            invalidateOptionsMenu();

            adapter.setForeignView(isForeignView);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_hide) {
            isForeignView = false;
            invalidateOptionsMenu();

            adapter.setForeignView(isForeignView);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_conversationNoteView);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
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
                                    adapter.delete(kind);

                                    isChange = true;
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
            case R.id.my_iv_copy :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //메뉴 선택 다이얼로그 생성
                    Cursor cursor = db.rawQuery(DicQuery.getConversationNoteContextMenuExceptMe(kind), null);

                    if ( cursor.getCount() == 0 ) {
                        Toast.makeText(this, "등록된 회화 노트가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] kindCodes = new String[cursor.getCount()];
                        final String[] kindCodeNames = new String[cursor.getCount()];

                        int idx = 0;
                        while (cursor.moveToNext()) {
                            kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                            kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                            idx++;
                        }
                        cursor.close();

                        final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(ConversationNoteViewActivity.this);
                        dlg.setTitle("회화 노트 선택");
                        dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mSelect = arg1;
                            }
                        });
                        dlg.setNegativeButton("취소", null);
                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.copy(kindCodes[mSelect]);
                            }
                        });
                        dlg.show();
                    }
                }

                break;
            case R.id.my_iv_move :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //메뉴 선택 다이얼로그 생성
                    Cursor cursor = db.rawQuery(DicQuery.getConversationNoteContextMenuExceptMe(kind), null);

                    if ( cursor.getCount() == 0 ) {
                        Toast.makeText(this, "등록된 회화 노트가 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        final String[] kindCodes = new String[cursor.getCount()];
                        final String[] kindCodeNames = new String[cursor.getCount()];

                        int idx = 0;
                        while (cursor.moveToNext()) {
                            kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                            kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                            idx++;
                        }
                        cursor.close();

                        final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(ConversationNoteViewActivity.this);
                        dlg.setTitle("회화 노트 선택");
                        dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mSelect = arg1;
                            }
                        });
                        dlg.setNegativeButton("취소", null);
                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.move(kind, kindCodes[mSelect]);
                            }
                        });
                        dlg.show();
                    }
                }
                break;
        }
    }

    public void onInit(int status) {
        Locale loc = new Locale("en");

        if (status == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(new Locale("vi", "VN"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }
}

class ConversationNoteViewCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    private SQLiteDatabase mDb;
    public boolean[] isCheck;
    public int[] seq;
    private boolean isEditing = false;
    public HashMap statusData = new HashMap();
    public boolean isForeignView = false;
    public boolean isChange = false;

    public ConversationNoteViewCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
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

    public void dataChange() {
        mCursor.requery();
        mCursor.move(mCursor.getPosition());

        for ( int i = 0; i < isCheck.length; i++ ) {
            isCheck[i] = false;
        }

        //변경사항을 반영한다.
        notifyDataSetChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_conversation_note_view_item, parent, false);

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

        ((TextView) view.findViewById(R.id.my_tv_han)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
        if ( isForeignView || statusData.containsKey(cursor.getString(cursor.getColumnIndexOrThrow("SEQ")))  ) {
            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
        } else {
            ((TextView) view.findViewById(R.id.my_tv_foreign)).setText("Click..");
        }

        if ( isCheck[cursor.getPosition()] ) {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_on_background);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }

        if ( isEditing ) {
            view.findViewById(R.id.my_rl_left).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.my_rl_left).setVisibility(View.GONE);
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_han)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_foreign)).setTextSize(fontSize);
    }

    public void allCheck(boolean chk) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            isCheck[i] = chk;
        }

        notifyDataSetChanged();
    }

    public void delete(String kind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.delConversationFromNote(mDb, kind, seq[i]);
            }
        }

        isChange = true;

        dataChange();
    }

    public void copy(String copyKind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.insConversationToNote(mDb, copyKind, Integer.toString(seq[i]));
            }
        }

        isChange = true;

        dataChange();
    }

    public void move(String kind, String copyKind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.moveConversationToNote(mDb, kind, copyKind, seq[i]);
            }
        }

        isChange = true;

        dataChange();
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

    public void setForeignView(boolean foreignView) {
        isForeignView = foreignView;
        statusData.clear();
    }

    public void setStatus(String sampleSeq) {
        statusData.put(sampleSeq, "Y");
    }
}