package com.sleepingbear.pvhdictandvoc;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class TodayFragment extends Fragment implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private View mainView;
    private TodayCursorAdapter adapter;

    private AppCompatActivity mMainActivity;

    public TodayFragment() {
    }

    public void setMainActivity(AppCompatActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_today, container, false);

        dbHelper = new DbHelper(getContext());
        db = dbHelper.getWritableDatabase();

        TextView tv_sel_date = (TextView) mainView.findViewById(R.id.my_f_today_tv_sel_date);
        tv_sel_date.setText(DicUtils.getDelimiterDate(DicUtils.getCurrentDate(),"."));

        ImageButton b_date = (ImageButton) mainView.findViewById(R.id.my_f_today_ib_date);
        b_date.setOnClickListener(this);

        //리스트 내용 변경
        changeListView();

        return mainView;
    }

    public void changeListView() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        StringBuffer sql = new StringBuffer();

        String today = ((TextView) mainView.findViewById(R.id.my_f_today_tv_sel_date)).getText().toString();

        sql.append("SELECT COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_TODAY " + CommConstants.sqlCR);
        sql.append(" WHERE TODAY = '" + today + "'" + CommConstants.sqlCR);
        Cursor cCursor = db.rawQuery(sql.toString(), null);
        if ( cCursor.moveToNext() ) {
            if ( cCursor.getInt(cCursor.getColumnIndexOrThrow("CNT")) == 0 ) {
                sql.delete(0, sql.length());
                sql.append("SELECT * FROM (" + CommConstants.sqlCR);
                sql.append("SELECT ENTRY_ID, WORD, RANDOM() RND" + CommConstants.sqlCR);
                sql.append("  FROM DIC " + CommConstants.sqlCR);
                sql.append(" WHERE KIND = 'VH'" + CommConstants.sqlCR);
                sql.append("   AND ENTRY_ID NOT IN ( SELECT ENTRY_ID FROM DIC_TODAY ) " + CommConstants.sqlCR);
                sql.append(" ) ORDER BY RND " + CommConstants.sqlCR);
                Cursor todayCursor = db.rawQuery(sql.toString(), null);
                int cnt = 0;
                while ( todayCursor.moveToNext() ) {
                    cnt++;
                    DicDb.insToday(db, todayCursor.getString(todayCursor.getColumnIndexOrThrow("ENTRY_ID")), today);

                    //기록...
                    DicUtils. writeInfoToFile(getContext(), "TODAY" + ":" + today + ":" + todayCursor.getString(todayCursor.getColumnIndexOrThrow("ENTRY_ID")));

                    if ( cnt == 10 ) {
                        break;
                    }
                }
                todayCursor.close();
            }
        }
        cCursor.close();

        sql.delete(0, sql.length());
        sql.append("SELECT B.SEQ _id, B.WORD, B.MEAN, B.ENTRY_ID, B.SPELLING, A.TODAY" + CommConstants.sqlCR);
        sql.append("  FROM DIC_TODAY A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID " + CommConstants.sqlCR);
        sql.append("   AND B.KIND = 'VH' " + CommConstants.sqlCR);
        sql.append("   AND SUBSTR(A.TODAY,1,10) <= '" + today + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY A.TODAY DESC, B.ORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor listCursor = db.rawQuery(sql.toString(), null);
        ListView listView = (ListView) mainView.findViewById(R.id.my_f_today_lv_dictionary);
        adapter = new TodayCursorAdapter(getContext(), listCursor, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setSelection(0);
    }

    /**
     * 단어가 선택되면은 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            //cur.moveToPosition(position);

            String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));
            String seq = cur.getString(cur.getColumnIndexOrThrow("_id"));

            Intent intent = new Intent(getActivity().getApplication(), WordViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("entryId", entryId);
            bundle.putString("seq", seq);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };


    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_f_today_ib_date ) {
            String date = ((TextView) mainView.findViewById(R.id.my_f_today_tv_sel_date)).getText().toString();
            DatePickerDialog dialog = new DatePickerDialog(this.getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            TextView tv_sel_date = (TextView) mainView.findViewById(R.id.my_f_today_tv_sel_date);
                            tv_sel_date.setText(year + "." + (monthOfYear + 1> 9 ? "" : "0") + (monthOfYear + 1) + "." + (dayOfMonth> 9 ? "" : "0") + dayOfMonth);

                            changeListView();
                        }
                    },
                    Integer.parseInt(DicUtils.getYear(date)),
                    Integer.parseInt(DicUtils.getMonth(date)) - 1,
                    Integer.parseInt(DicUtils.getDay(date)));
            dialog.show();
        }
    }

    public void setActionBarTitle() {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle("  오늘의 단어");
    }

}

class TodayCursorAdapter extends CursorAdapter {
    public TodayCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_today_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_f_dk_tv_viet)).setText(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        ((TextView) view.findViewById(R.id.my_f_today_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
        ((TextView) view.findViewById(R.id.my_f_today_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TODAY")));

        ((TextView) view.findViewById(R.id.my_f_today_tv_mean)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
    }
}



