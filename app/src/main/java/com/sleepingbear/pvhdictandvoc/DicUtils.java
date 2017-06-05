package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Administrator on 2015-11-27.
 */
public class DicUtils {
    public static void setDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "Y");
        editor.commit();

        dicLog(DicUtils.class.toString() + " setDbChange : " + "Y");
    }

    public static String getDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(CommConstants.flag_dbChange, "N");
    }

    public static void clearDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "N");
        editor.commit();
    }

    public static String getPreferencesValue(Context context, String preference) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String rtn = sharedPref.getString( preference, "" );
        if ( "".equals( rtn ) ) {
            if ( preference.equals(CommConstants.preferences_font) ) {
                rtn = "17";
            } else {
                rtn = "";
            }
        }

        DicUtils.dicLog(rtn);

        return rtn;
    }

    public static String getString(String str) {
        if (str == null)
            return "";
        else
            return str.trim();
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return year + "" + (month + 1 > 9 ? "" : "0") + (month + 1) + "" + (day > 9 ? "" : "0") + day;
    }

    public static String getAddDay(String date, int addDay) {
        String mDate = date.replaceAll("[.-/]", "");

        int year = Integer.parseInt(mDate.substring(0, 4));
        int month = Integer.parseInt(mDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(mDate.substring(6, 8));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day + addDay);

        return c.get(Calendar.YEAR) + "" + (c.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (c.get(Calendar.MONTH) + 1) + "" + (c.get(Calendar.DAY_OF_MONTH) > 9 ? "" : "0") + c.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDelimiterDate(String date, String delimiter) {
        if (getString(date).length() < 8) {
            return "";
        } else {
            return date.substring(0, 4) + delimiter + date.substring(4, 6) + delimiter + date.substring(6, 8);
        }
    }

    public static String getYear(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(0, 4);
        }
    }

    public static String getMonth(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(4, 6);
        }
    }

    public static String getDay(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(6, 8);
        }
    }

    public static void dicSqlLog(String str) {
        if (BuildConfig.DEBUG) {
            Log.d("VhDictAndVoc Sql ====>", str);
        }
    }

    public static void dicLog(String str) {
        if (BuildConfig.DEBUG) {
            Calendar cal = Calendar.getInstance();
            String time = cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초";

            Log.d("VhDictAndVoc ====>", time + " : " + str);
        }
    }

    public static String lpadding(String str, int length, String fillStr) {
        String rtn = "";

        for (int i = 0; i < length - str.length(); i++) {
            rtn += fillStr;
        }
        return rtn + (str == null ? "" : str);
    }

    public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        String tmpSentence = sentence + " ";

        int startPos = 0;
        for ( int i = 0; i < tmpSentence.length(); i++ ) {
            if ( CommConstants.sentenceSplitStr.indexOf(tmpSentence.substring(i, i + 1)) > -1 ) {
                if ( i == 0 ) {
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                } else {
                    if ( i != startPos ) {
                        al.add(tmpSentence.substring(startPos, i));
                    }
                    al.add(tmpSentence.substring(i, i + 1));
                    startPos = i + 1;
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }

    public static String getSentenceWord(String[] sentence, int kind, int position) {
        String rtn = "";
        if ( kind == 1 ) {
            rtn = sentence[position];
        } else if ( kind == 2 ) {
            if ( position + 2 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2];
                }
            }
        } else if ( kind == 3 ) {
            if ( position + 4 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) && " ".equals(sentence[position + 3]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2] + sentence[position + 3] + sentence[position + 4];
                }
            }
        }

        //dicLog(rtn);
        return rtn;
    }

    public static String getOneSpelling(String spelling) {
        String rtn = "";
        String[] str = spelling.split(",");
        if ( str.length == 1 ) {
            rtn = spelling;
        } else {
            rtn = str[0] + "(" + str[1] + ")";
        }

        return rtn;
    }

    /**
     * 데이타 복원
     * @param ctx
     * @param db
     * @param fileName
     */
    public static void readInfoFromFile(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile start, " + fileName);

        //데이타 복구
        FileInputStream fis = null;
        try {
            //데이타 초기화
            DicDb.initMyConversationNote(db);
            DicDb.initConversationNote(db);
            DicDb.initVocabulary(db);
            DicDb.initDicClickWord(db);
            DicDb.initHistory(db);

            if ( "".equals(fileName) ) {
                fis = ctx.openFileInput(CommConstants.infoFileName);
            } else {
                fis = new FileInputStream(new File(fileName));
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(isr);

            //출력...
            String readString = buffreader.readLine();
            while (readString != null) {
                dicLog(readString);

                String[] row = readString.split(":");
                if ( row[0].equals("CATEGORY_INSERT") ) {
                    int maxCode = Integer.parseInt(row[1].substring(2,5));
                    String insMaxCode = "VOC" + DicUtils.lpadding(Integer.toString(maxCode + 1), 4, "0");
                    DicDb.insCode(db, "MY_VOC", insMaxCode, row[2]);
                } else if ( row[0].equals("MYWORD_INSERT") ) {
                    int maxCode = Integer.parseInt(row[1].substring(2,5));
                    String insMaxCode = "VOC" + DicUtils.lpadding(Integer.toString(maxCode + 1), 4, "0");
                    DicDb.insDicVoc(db, insMaxCode, row[3], row[2], "N");
                } else if ( row[0].equals("MEMORY") ) {
                    DicDb.updMemory(db, row[1], row[2]);
                } else if ( row[0].equals(CommConstants.tag_code_ins) ) {
                    DicDb.insCode(db, row[1], row[2], row[3]);
                } else if ( row[0].equals(CommConstants.tag_note_ins) ) {
                    DicDb.insConversationToNote(db, row[1], row[2]);
                } else if ( row[0].equals(CommConstants.tag_voc_ins) ) {
                    DicDb.insDicVoc(db, row[1], row[2], row[3], row[4]);
                } else if ( row[0].equals(CommConstants.tag_history_ins) ) {
                    DicDb.insSearchHistory(db, row[1], row[2]);
                } else if ( row[0].equals(CommConstants.tag_click_word_ins) ) {
                    DicDb.insDicClickWord(db, row[1], row[2]);
                }

                readString = buffreader.readLine();
            }

            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile end");
    }

    /**
     * 데이타 기록
     * @param ctx
     * @param db
     */
    public static void writeInfoToFile(Context ctx, SQLiteDatabase db, String fileName) {
        System.out.println("writeNewInfoToFile start");

        try {
            FileOutputStream fos = null;

            if ( "".equals(fileName) ) {
                fos = ctx.openFileOutput(CommConstants.infoFileName, ctx.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            Cursor cursor = db.rawQuery(DicQuery.getWriteData(), null);
            while (cursor.moveToNext()) {
                String writeData = cursor.getString(cursor.getColumnIndexOrThrow("WRITE_DATA"));
                DicUtils.dicLog(writeData);
                if ( writeData != null ) {
                    fos.write((writeData.getBytes()));
                    fos.write("\n".getBytes());
                }
            }
            cursor.close();

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }

        System.out.println("writeNewInfoToFile end");
    }



    public static boolean isHangule(String pStr) {
        boolean isHangule = false;
        String str = (pStr == null ? "" : pStr);
        try {
            if(str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                isHangule = true;
            } else {
                isHangule = false;
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }

        return isHangule;
    }

    public static String getBtnString(String word){
        String rtn = "";

        if ( word.length() == 1 ) {
            rtn = "  " + word + "  ";
        } else if ( word.length() == 2 ) {
            rtn = "  " + word + " ";
        } else if ( word.length() == 3 ) {
            rtn = " " + word + " ";
        } else if ( word.length() == 4 ) {
            rtn = " " + word;
        } else {
            rtn = " " + word + " ";
        }

        return rtn;
    }

    public static String getEngString(String word) {
        if ( word == null || "".equals(word) ) {
            return "";
        } else {
            return word.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a").replaceAll("[óòỏõọôốồổỗộơớờởỡợȏ]", "o").replaceAll("[éèẻẽẹêếềểễệ]", "e").replaceAll("[úùủũụưứừửữự]", "u").replaceAll("[íìỉĩị]", "i").replaceAll("[ýỳỷỹỵ]", "y");
        }
    }

}


/*
    public static void writeInfoToFile(Context ctx, String saveData) {
        try {
            FileOutputStream fos = ctx.openFileOutput(CommConstants.infoFileName, ctx.MODE_APPEND);
            fos.write(saveData.getBytes());
            fos.write("\n".getBytes());
            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }
    }
    */

    /*
    public static void readInfoFromFile(Context ctx, SQLiteDatabase db) {
        readInfoFromFile(ctx, db, "");
    }
    */
    /*
    public static void readInfoFromFile(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile start");

        //데이타 복구
        FileInputStream fis = null;
        try {
            if ( fileName == null || fileName.length() == 0 ) {
                fis = ctx.openFileInput(CommConstants.infoFileName);
            } else {
                //데이타 초기화
                DicDb.initToday(db);
                DicDb.initVocabulary(db);
                DicDb.initSample(db);

                fis = new FileInputStream(new File(fileName));
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(isr);

            //출력...
            String readString = buffreader.readLine();
            while (readString != null) {
                dicLog(readString);

                String[] row = readString.split(":");
                switch (row[0]) {
                    case "TODAY":
                        //오늘의 단어장
                        //DicUtils. writeInfoToFile(getContext(), "TODAY" + ":" + today + ":" + todayCursor.getString(todayCursor.getColumnIndexOrThrow("ENTRY_ID")));
                        DicDb.insToday(db, row[2], row[1]);
                        break;
                    case "MYWORD_INSERT":
                        //단어장 추가
                        //DicUtils.writeInfoToFile(context, "MYWORD_INSERT" + ":" + "MY" + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + ":" + viewHolder.entryId);
                        DicDb.insDicVoc(db, row[3], row[1], row[2]);
                        break;
                    case "MYWORD_DELETE":
                        //단어장 삭제
                        //DicUtils. writeInfoToFile(getApplicationContext(), "MYWORD_DELETE" + ":" + kindCodes[mSelect] + ":" + entryId);
                        DicDb.delDicVoc(db, row[2], row[1]);
                        break;
                    case "MYWORD_DELETE_ALL":
                        //단어장 전체 삭제
                        //DicUtils. writeInfoToFile(getApplicationContext(), "MYWORD_DELETE_ALL" + ":" + entryId);
                        DicDb.delDicVocAll(db, row[1]);
                        break;
                    case "CATEGORY_INSERT":
                        //DicUtils. writeInfoToFile(getContext(), "CATEGORY_INSERT" + ":" + insCategoryCode + ":" + et_ins.getText().toString());
                        db.execSQL(DicQuery.getInsNewCategory("MY", row[1], row[2]));
                        break;
                    case "CATEGORY_UPDATE":
                        //DicUtils. writeInfoToFile(getContext(), "CATEGORY_UPDATE" + ":" + (String) v.getTag() + ":" + et_ins.getText().toString());
                        db.execSQL(DicQuery.getUpdCategory("MY", row[1], row[2]));
                        break;
                    case "CATEGORY_DELETE":
                        //DicUtils. writeInfoToFile(getContext(), "CATEGORY_DELETE" + ":" + code);
                        db.execSQL(DicQuery.getDelCategory("MY", row[1]));
                        db.execSQL(DicQuery.getDelDicVoc(row[1]));
                        break;
                    case "MEMORY":
                        //DicUtils.writeInfoToFile(context, "MEMORY" + ":" + entryId + ":" + (((CheckBox) v.findViewById(R.id.my_c_vi_cb_memorization)).isChecked() ? "Y" : "N"));
                        DicDb.updMemory(db, row[1], row[2]);
                        break;
                    case "MYSAMPLE_INSERT":
                        DicDb.insDicMySample(db, row[1], row[2]);
                        break;
                    case "MYSAMPLE_DELETE":
                        //DicUtils.writeInfoToFile(context, "MEMORY" + ":" + entryId + ":" + (((CheckBox) v.findViewById(R.id.my_c_vi_cb_memorization)).isChecked() ? "Y" : "N"));
                        DicDb.delDicMySample(db, row[1]);
                        break;
                }
                readString = buffreader.readLine();
            }

            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile end");

        //데이타 기록
        writeNewInfoToFile(ctx, db);
    }
    */

/**
 * 데이타 기록
 * @param ctx
 * @param db
 */
    /*
    public static void writeNewInfoToFile(Context ctx, SQLiteDatabase db) {
        System.out.println("writeNewInfoToFile start");

        writeNewInfoToFile(ctx, db, "");

        System.out.println("writeNewInfoToFile end");
    }
    public static void writeNewInfoToFile(Context ctx, SQLiteDatabase db, String fileName) {
        try {
            FileOutputStream fos = null;

            if ( fileName == null || fileName.length() == 0 ) {
                fos = ctx.openFileOutput(CommConstants.infoFileName, ctx.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            Cursor cursor = null;

            //Today 기록
            //DicUtils. writeInfoToFile(getContext(), "TODAY" + ":" + today + ":" + todayCursor.getString(todayCursor.getColumnIndexOrThrow("ENTRY_ID")));
            cursor = db.rawQuery(DicQuery.getToday(), null);
            while (cursor.moveToNext()) {
                fos.write(("TODAY" + ":" + cursor.getString(cursor.getColumnIndexOrThrow("TODAY")) + ":" + cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"))).getBytes());
                fos.write("\n".getBytes());
            }
            cursor.close();

            //카테고리 기록
            //DicUtils. writeInfoToFile(getContext(), "CATEGORY_INSERT" + ":" + insCategoryCode + ":" + et_ins.getText().toString());
            cursor = db.rawQuery(DicQuery.getVocabularyCategoryCount(), null);
            while (cursor.moveToNext()) {
                if (!"MY000".equals(cursor.getString(cursor.getColumnIndexOrThrow("KIND")))) {
                    fos.write(("CATEGORY_INSERT" + ":" + cursor.getString(cursor.getColumnIndexOrThrow("KIND")) + ":" + cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"))).getBytes());
                    fos.write("\n".getBytes());
                }
            }
            cursor.close();

            //단어 기록
            //DicUtils.writeInfoToFile(context, "MYWORD_INSERT" + ":" + "MY" + ":" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + ":" + viewHolder.entryId);
            cursor = db.rawQuery(DicQuery.getMyDic(), null);
            while (cursor.moveToNext()) {
                fos.write(("MYWORD_INSERT" + ":" + cursor.getString(cursor.getColumnIndexOrThrow("KIND")) + ":" + cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")) + ":" + cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"))).getBytes());
                fos.write("\n".getBytes());
            }
            cursor.close();

            //암기 기록
            //DicUtils.writeInfoToFile(context, "MEMORY" + ":" + entryId + ":" + (((CheckBox) v.findViewById(R.id.my_c_vi_cb_memorization)).isChecked() ? "Y" : "N"));
            cursor = db.rawQuery(DicQuery.getMyMemoryDic(), null);
            while (cursor.moveToNext()) {
                fos.write(("MEMORY" + ":" + cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")) + ":" + "Y").getBytes());
                fos.write("\n".getBytes());
            }
            cursor.close();

            //나의 예문 기록
            cursor = db.rawQuery(DicQuery.getMySample(), null);
            while (cursor.moveToNext()) {
                fos.write(("MYSAMPLE_INSERT" + ":" + cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1")) + ":" + cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))).getBytes());
                fos.write("\n".getBytes());
            }
            cursor.close();

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }
    }
    */