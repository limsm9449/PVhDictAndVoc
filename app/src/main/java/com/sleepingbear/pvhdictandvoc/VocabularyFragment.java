package com.sleepingbear.pvhdictandvoc;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class VocabularyFragment extends Fragment implements View.OnClickListener   {
    private View mainView;

    private SQLiteDatabase mDb;
    private VocabularyFlagmentCursorAdapter adapter;
    private LayoutInflater mInflater;

    private int mScreenKind;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mInflater = inflater;
        mainView = inflater.inflate(R.layout.fragment_vocabulary, container, false);
        mDb = (new DbHelper(getContext())).getWritableDatabase();

        changeListView();

        ((Button) mainView.findViewById(R.id.my_f_dic_b_1)).setOnClickListener(this);
        ((Button) mainView.findViewById(R.id.my_f_dic_b_2)).setOnClickListener(this);
        ((Button) mainView.findViewById(R.id.my_f_dic_b_3)).setOnClickListener(this);
        ((Button) mainView.findViewById(R.id.my_f_dic_b_4)).setOnClickListener(this);

        return mainView;
    }

    public void changeListView() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        Cursor cursor = mDb.rawQuery(DicQuery.getVocabularyCategoryCount(), null);

        ListView listView = (ListView) mainView.findViewById(R.id.my_a_cat_lv_category);
        adapter = new VocabularyFlagmentCursorAdapter(getContext(), cursor, 0, this, mDb, getFragmentManager(), mScreenKind);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if ( ((MainActivity)getActivity()).checkPermission() == false ) {
                    return true;
                }
                final Cursor cur = (Cursor) adapter.getItem(position);

                //layout 구성
                final View dialog_layout = mInflater.inflate(R.layout.dialog_category_iud, null);

                //dialog 생성..
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialog_layout);
                final AlertDialog alertDialog = builder.create();

                ((TextView) dialog_layout.findViewById(R.id.my_d_category_tv_category)).setText("단어장 관리");

                final EditText et_upd = ((EditText) dialog_layout.findViewById(R.id.my_d_category_et_upd));
                et_upd.setText(cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upd)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upd)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("".equals(et_upd.getText().toString())) {
                            Toast.makeText(getContext(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();

                            mDb.execSQL(DicQuery.getUpdCategory("MY", (String) v.getTag(), et_upd.getText().toString()));

                            //기록...
                            DicUtils.writeInfoToFile(getContext(), "CATEGORY_UPDATE" + ":" + (String) v.getTag() + ":" + et_upd.getText().toString());

                            changeListView();

                            Toast.makeText(getContext(), "단어장 이름을 수정하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_del)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_del)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String code = (String) v.getTag();

                        if ("MY000".equals(code)) {
                            Toast.makeText(getContext(), "기본 단어장은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle("알림")
                                    .setMessage("삭제된 데이타는 복구할 수 없습니다. 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alertDialog.dismiss();

                                            mDb.execSQL(DicQuery.getDelCategory("MY", code));
                                            mDb.execSQL(DicQuery.getDelDicVoc(code));

                                            //기록...
                                            //DicUtils.writeInfoToFile(getContext(), "CATEGORY_DELETE" + ":" + code);
                                            DicUtils.writeNewInfoToFile(getContext(), mDb);
                                            changeListView();

                                            Toast.makeText(getContext(), "단어장을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    }
                });

                final EditText et_saveName = ((EditText) dialog_layout.findViewById(R.id.my_d_category_et_save));
                et_saveName.setText("vocabulary.txt");
                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_save)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_save)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String code = (String) v.getTag();

                        String saveFileName = et_saveName.getText().toString();
                        if ("".equals(saveFileName)) {
                            Toast.makeText(getContext(), "저장할 파일명을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else if (saveFileName.indexOf(".") > -1 && !"txt".equals(saveFileName.substring(saveFileName.length() - 3, saveFileName.length()).toLowerCase())) {
                            Toast.makeText(getContext(), "확장자는 txt 입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            String fileName = "";

                            boolean existDir = false;
                            File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName);
                            if (!appDir.exists()) {
                                existDir = appDir.mkdirs();
                                if (saveFileName.indexOf(".") > -1) {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                                } else {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                                }
                            } else {
                                if (saveFileName.indexOf(".") > -1) {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                                } else {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                                }
                            }

                            File saveFile = new File(fileName);
                            if (saveFile.exists()) {
                                Toast.makeText(getContext(), "파일명이 존재합니다.", Toast.LENGTH_SHORT).show();
                                ;
                            } else {
                                try {
                                    saveFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                }

                                BufferedWriter bw = null;
                                try {
                                    bw = new BufferedWriter(new FileWriter(saveFile, true));

                                    Cursor cursor = mDb.rawQuery(DicQuery.getSaveVocabulary(code), null);
                                    while (cursor.moveToNext()) {
                                        bw.write(cursor.getString(cursor.getColumnIndexOrThrow("WORD")) + ": " + cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")) + " -> " + cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
                                        bw.newLine();
                                    }

                                    bw.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (bw != null) try {
                                        bw.close();
                                    } catch (IOException ioe2) {
                                    }
                                }

                                Toast.makeText(getContext(), "단어장을 정상적으로 내보냈습니다.", Toast.LENGTH_SHORT).show();

                                alertDialog.dismiss();
                            }
                        }
                    }
                });

                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upload)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upload)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String code = (String) v.getTag();

                        FileChooser filechooser = new FileChooser(getActivity());
                        filechooser.setFileListener(new FileChooser.FileSelectedListener() {
                            @Override
                            public void fileSelected(final File file) {
                                FileInputStream fis = null;
                                try {
                                    fis = new FileInputStream(new File(file.getAbsolutePath()));
                                    InputStreamReader isr = new InputStreamReader(fis);
                                    BufferedReader buffreader = new BufferedReader(isr);

                                    String readString = buffreader.readLine();
                                    while (readString != null) {
                                        String[] dicInfo = readString.split(":");
                                        String entryId = DicDb.getEntryIdForWord(mDb, dicInfo[0]);
                                        DicDb.insDicVoc(mDb, entryId, code);

                                        readString = buffreader.readLine();
                                    }
                                    isr.close();

                                    DicUtils.writeNewInfoToFile(getContext(), mDb);

                                    changeListView();

                                    Toast.makeText(getContext(), "단어장을 정상적으로 가져왔습니다.", Toast.LENGTH_SHORT).show();

                                    alertDialog.dismiss();

                                    changeListView();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        filechooser.setExtension("txt");
                        filechooser.showDialog();
                    }
                });

                ((Button) dialog_layout.findViewById(R.id.my_d_category_b_close)).setOnClickListener(new View.OnClickListener() {
                                                                                                         @Override
                                                                                                         public void onClick(View v) {
                                                                                                             alertDialog.dismiss();
                                                                                                         }
                                                                                                     }
                );

                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

                return true;
            }
        });

        listView.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_f_dic_b_1 ) {
            Intent intent = new Intent(this.getActivity().getApplication(), MySampleActivity.class);
            startActivity(intent);
        } else  if ( v.getId() == R.id.my_f_dic_b_2 ) {
            Intent intent = new Intent(this.getActivity().getApplication(), DicCategoryActivity.class);
            getActivity().startActivityForResult(intent, CommConstants.a_dicCategory);
        } else if ( v.getId() == R.id.my_f_dic_b_3 ) {
            Intent intent = new Intent(this.getActivity().getApplication(), GrammarActivity.class);
            startActivity(intent);
        } else if ( v.getId() == R.id.my_f_dic_b_4 ) {
            Intent intent = new Intent(this.getActivity().getApplication(), StudyActivity.class);
            startActivity(intent);
        }
    }
}


class VocabularyFlagmentCursorAdapter extends CursorAdapter {
    private String entryId = "";
    private String seq = "";
    private VocabularyFragment mFragment;
    private SQLiteDatabase mDb;
    private Cursor mCursor;
    private FragmentManager mFragmentManager;
    private int mScreenKind;

    private Context mContext;

    static class ViewHolder {
        protected String kind;
        protected String kindName;
    }

    public VocabularyFlagmentCursorAdapter(Context context, Cursor cursor, int flags, VocabularyFragment fragment, SQLiteDatabase db, FragmentManager fm, int screenKind) {
        super(context, cursor, 0);
        mContext = context;
        mCursor = cursor;
        mFragment = fragment;
        mDb = db;

        mFragmentManager = fm;
        mScreenKind = screenKind;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_vocabulary_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        view.setTag(viewHolder);

        //Item 선택
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder vViewHolder = (ViewHolder) v.getTag();

                Intent intent = new Intent(mFragment.getActivity().getApplication(), VocabularyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("kind", vViewHolder.kind);
                bundle.putString("kindName", vViewHolder.kindName);
                intent.putExtras(bundle);

                mFragment.getActivity().startActivityForResult(intent, CommConstants.a_vocabulary);
            }
        });

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        HashMap param = new HashMap();
        param.put("kind", cursor.getString(cursor.getColumnIndexOrThrow("KIND")));

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.kind = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
        viewHolder.kindName = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));

        TextView tv_category = (TextView) view.findViewById(R.id.my_c_s1i_tv_question);
        tv_category.setText(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")));

        TextView tv_cnt = (TextView) view.findViewById(R.id.my_f_cat_tv_cnt);
        tv_cnt.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("CNT"))));
    }
}