package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Message;

public class DictionaryFragment extends Fragment implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private View mainView;
    private EditText et_search;
    private DictionaryCursorAdapter dicAdapter;

    private String mVhKind = "A";
    private String mWsKind = "W";

    private Activity mActivity;
    private Cursor dictionaryCursor;

    DicSearchTask task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_dictionary, container, false);

        mActivity = this.getActivity();

        dbHelper = new DbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        et_search = (EditText) mainView.findViewById(R.id.my_f_dic_et_search);
        et_search.addTextChangedListener(textWatcherInput);
        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ( keyCode == KeyEvent.KEYCODE_ENTER ) {
                    changeListView();
                }

                return false;
            }
        });

        ImageView iv_clear = (ImageView)mainView.findViewById(R.id.my_f_dic_iv_clear);
        iv_clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                et_search.setText("");

                changeListView();
            }
        });

        RadioButton rb_a = (RadioButton) mainView.findViewById(R.id.my_f_dic_rb_a);
        rb_a.setOnClickListener(this);

        RadioButton rb_vh = (RadioButton) mainView.findViewById(R.id.my_f_dic_rb_vh);
        rb_vh.setOnClickListener(this);

        RadioButton rb_hv = (RadioButton) mainView.findViewById(R.id.my_f_dic_rb_hv);
        rb_hv.setOnClickListener(this);

        RadioButton rb_word = (RadioButton) mainView.findViewById(R.id.my_f_dic_rb_word);
        rb_word.setOnClickListener(this);

        RadioButton rb_sentence = (RadioButton) mainView.findViewById(R.id.my_f_dic_rb_sentence);
        rb_sentence.setOnClickListener(this);

        //리스트 내용 변경
        changeListView();

        return mainView;
    }

    public void changeListView() {
        if (task != null ) return;

        task = new DicSearchTask();
        task.execute();
    }

    public void getData() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        StringBuffer sql = new StringBuffer();
        if ("W".equals(mWsKind)) {
            //단어일때...
            String searchText = et_search.getText().toString().trim();
            if ("".equals(searchText) || searchText.indexOf(" ") < 0) {
                sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN" + CommConstants.sqlCR);
                sql.append("  FROM DIC" + CommConstants.sqlCR);
                sql.append(" WHERE 1 = 1" + CommConstants.sqlCR);
                if (!"A".equals(mVhKind)) {
                    sql.append("   AND KIND = '" + mVhKind + "'" + CommConstants.sqlCR);
                }
                if (!"".equals(et_search.getText().toString())) {
                    sql.append(" AND WORD LIKE '" + et_search.getText().toString() + "%'" + CommConstants.sqlCR);
                }
                sql.append(" ORDER BY KIND DESC, ORD" + CommConstants.sqlCR);
            } else {
                // 여러 단어일때...
                Cursor wordCursor = null;
                String word = "";
                ArrayList<String> al = new ArrayList<String>();
                String[] splitStr = DicUtils.sentenceSplit(et_search.getText().toString());
                for (int m = 0; m < splitStr.length; m++) {
                    // 3 단어
                    word = DicUtils.getSentenceWord(splitStr, 3, m);
                    if (!"".equals(word)) {
                        wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                        if (wordCursor.moveToNext()) {
                            if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                                al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                            }

                            m += 4;

                            wordCursor.close();
                            continue;
                        }
                        wordCursor.close();
                    }

                    // 2 단어
                    word = DicUtils.getSentenceWord(splitStr, 2, m);
                    if (!"".equals(word)) {
                        wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                        if (wordCursor.moveToNext()) {
                            if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                                al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                            }

                            m += 2;

                            wordCursor.close();
                            continue;
                        }
                        wordCursor.close();
                    }

                    // 1 단어
                    word = DicUtils.getSentenceWord(splitStr, 1, m);
                    if (!"".equals(word)) {
                        wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                        if (wordCursor.moveToNext()) {
                            if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                                al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                            }
                        }
                        wordCursor.close();
                    }
                }
                //나머지 단어들
                for (int m = 0; m < splitStr.length; m++) {
                    // 2 단어
                    word = DicUtils.getSentenceWord(splitStr, 2, m);
                    if (!"".equals(word)) {
                        wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                        if (wordCursor.moveToNext()) {
                            if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                                al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                            }
                        }
                        wordCursor.close();
                    }

                    word = DicUtils.getSentenceWord(splitStr, 1, m);
                    if (!"".equals(word)) {
                        wordCursor = db.rawQuery(DicQuery.getDicForWord(word), null);
                        if (wordCursor.moveToNext()) {
                            if (!al.contains(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")))) {
                                al.add(wordCursor.getString(wordCursor.getColumnIndexOrThrow("ENTRY_ID")));
                            }
                        }
                        wordCursor.close();
                    }
                }

                for (int i = 0; i < al.size(); i++) {
                    if (i > 0) {
                        sql.append("UNION" + CommConstants.sqlCR);
                    }
                    sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, HANMUN, KIND, " + i + " ORD FROM DIC A WHERE ENTRY_ID = '" + al.get(i) + "'" + CommConstants.sqlCR);
                    if (!"A".equals(mVhKind)) {
                        sql.append("   AND KIND = '" + mVhKind + "'" + CommConstants.sqlCR);
                    }
                }
                sql.append(" ORDER BY KIND DESC, ORD" + CommConstants.sqlCR);
            }
        } else {
            //문장일때...
            if (!"".equals(et_search.getText().toString())) {
                if (DicUtils.isHangule(et_search.getText().toString())) {
                    sql.append("SELECT SEQ _id, SENTENCE1, SENTENCE2, 'HV' KIND" + CommConstants.sqlCR);
                    sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
                    sql.append(" WHERE SENTENCE2 LIKE '%" + et_search.getText().toString() + "%'" + CommConstants.sqlCR);
                    sql.append(" ORDER BY SENTENCE2" + CommConstants.sqlCR);
                } else {
                    sql.append("SELECT SEQ _id, SENTENCE1, SENTENCE2, 'VH' KIND" + CommConstants.sqlCR);
                    sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
                    sql.append(" WHERE SENTENCE1 LIKE '%" + et_search.getText().toString() + "%'" + CommConstants.sqlCR);
                    sql.append(" ORDER BY SENTENCE1" + CommConstants.sqlCR);
                }
            } else {
                sql.append("SELECT SEQ _id, SENTENCE1, SENTENCE2, 'VH' KIND" + CommConstants.sqlCR);
                sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
                sql.append(" ORDER BY SENTENCE1" + CommConstants.sqlCR);
            }
        }
        DicUtils.dicSqlLog(sql.toString());

        dictionaryCursor = db.rawQuery(sql.toString(), null);

        //결과가 나올때까지 기달리게 할려고 다음 로직을 추가한다. 안하면 progressbar가 사라짐.. cursor도  Thread 방식으로 돌아가나봄
        if ( dictionaryCursor.getCount() == 0 ) {
        }
    }

    public void setListView() {
        if ( dictionaryCursor.getCount() == 0 ) {
            Toast.makeText(getContext(), "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView dictionaryListView = (ListView) mainView.findViewById(R.id.lv_dictionary);
        dicAdapter = new DictionaryCursorAdapter(getContext(), dictionaryCursor, 0, mWsKind);
        dictionaryListView.setAdapter(dicAdapter);

        dictionaryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dictionaryListView.setOnItemClickListener(itemClickListener);
        dictionaryListView.setSelection(0);

        //소프트 키보드 없애기
        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
    }

    /**
     * 단어가 선택되면은 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) dicAdapter.getItem(position);
            cur.moveToPosition(position);

            if ("W".equals(mWsKind) ) {
                Intent intent = new Intent(getActivity().getApplication(), WordViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("entryId", cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID")));
                bundle.putString("seq", cur.getString(cur.getColumnIndexOrThrow("_id")));
                intent.putExtras(bundle);

                startActivity(intent);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString("viet", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
                bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));

                Intent intent = new Intent(getActivity().getApplication(), SentenceViewActivity.class);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        }
    };

    /**
     * 검색 단어가 변경되었으면 다시 검색을 한다.
     */
    TextWatcher textWatcherInput = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
               //changeListView();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_f_dic_rb_a ) {
            mVhKind = "A";
            changeListView();
        } else if ( v.getId() == R.id.my_f_dic_rb_vh ) {
            mVhKind = "VH";
            changeListView();
        } else if ( v.getId() == R.id.my_f_dic_rb_hv ) {
            mVhKind = "HV";
            changeListView();
        } else if ( v.getId() == R.id.my_f_dic_rb_word ) {
            mWsKind = "W";

            RadioGroup rg = (RadioGroup) mainView.findViewById(R.id.my_f_dic_rg_vhKind);
            rg.setVisibility(View.VISIBLE);

            changeListView();
        } else if ( v.getId() == R.id.my_f_dic_rb_sentence ) {
            mWsKind = "S";

            RadioGroup rg = (RadioGroup) mainView.findViewById(R.id.my_f_dic_rg_vhKind);
            rg.setVisibility(View.GONE);

            changeListView();
        }
    }

    private class DicSearchTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(getActivity());
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.setMessage("Searching...");
            pd.show();

            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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
            setListView();

            pd.dismiss();
            task = null;

            super.onPostExecute(result);
        }
    }
}

class DictionaryCursorAdapter extends CursorAdapter {
    private String mWsKind = "";

    public DictionaryCursorAdapter(Context context, Cursor cursor, int flags, String _mWsKind) {
        super(context, cursor, 0);

        mWsKind = _mWsKind;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_dictionary_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if ( "W".equals(mWsKind) ) {
            ((TextView) view.findViewById(R.id.my_f_dict_tv_viet)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("WORD"))));
            ((TextView) view.findViewById(R.id.my_f_dict_tv_mean)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("MEAN"))));
            if ( !"".equals(cursor.getString(cursor.getColumnIndexOrThrow("HANMUN"))) ) {
                ((TextView) view.findViewById(R.id.my_f_dict_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")) + " " + cursor.getString(cursor.getColumnIndexOrThrow("HANMUN")));
            } else {
                ((TextView) view.findViewById(R.id.my_f_dict_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            }
        } else {
            if ( "VH".equals(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("KIND")))) ) {
                ((TextView) view.findViewById(R.id.my_f_dict_tv_viet)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
                ((TextView) view.findViewById(R.id.my_f_dict_tv_mean)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
            } else {
                ((TextView) view.findViewById(R.id.my_f_dict_tv_viet)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))));
                ((TextView) view.findViewById(R.id.my_f_dict_tv_mean)).setText(String.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))));
            }
            ((TextView) view.findViewById(R.id.my_f_dict_tv_spelling)).setText("");
        }
    }
}
