package com.sleepingbear.pvhdictandvoc;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CaptionQuery {
    public static String getDramaGroupKind() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, CODE KIND, DESC KIND_NAME" + CommConstants.sqlCR);
        sql.append("FROM    DIC_DRAMA" + CommConstants.sqlCR);
        sql.append("WHERE   P_CODE = 'DRAMA'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDramaList(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, CODE KIND, DESC KIND_NAME, DIRECTORY" + CommConstants.sqlCR);
        sql.append("FROM    DIC_DRAMA" + CommConstants.sqlCR);
        sql.append("WHERE   P_CODE = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static boolean existDramaCaption(SQLiteDatabase db, String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("FROM    DIC_DRAMA_CAPTION" + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        int cnt = 0;
        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            cnt = cursor.getInt(cursor.getColumnIndexOrThrow("CNT"));
        }
        cursor.close();

        return (cnt > 0);
    }

    public static boolean existDrama(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("FROM    DIC_DRAMA" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        int cnt = 0;
        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            cnt = cursor.getInt(cursor.getColumnIndexOrThrow("CNT"));
        }
        cursor.close();

        return (cnt > 0);
    }

    public static String getDramaCaptionList(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, TIME, LANG_FOREIGN, LANG_HAN" + CommConstants.sqlCR);
        sql.append("FROM    DIC_DRAMA_CAPTION" + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static void initDrama(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_DRAMA" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_DRAMA_CAPTION" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDrama(SQLiteDatabase db, String drama, String code, String codeName) {
        StringBuffer sql = new StringBuffer();
        if ( drama.equals("DRAMA") ) {
            sql.append("INSERT INTO DIC_DRAMA (P_CODE, CODE, DESC)" + CommConstants.sqlCR);
            sql.append("VALUES ('" + drama + "','" + code + "','" + codeName.trim().replaceAll("'", "''") + "')" + CommConstants.sqlCR);
        } else {
            sql.append("INSERT INTO DIC_DRAMA (P_CODE, CODE, DESC, DIRECTORY)" + CommConstants.sqlCR);
            sql.append("VALUES ('" + code.substring(0, 4) + "','" + code + "','" + codeName.trim().replaceAll("'", "''") + "','" + drama + "')" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insCaption(SQLiteDatabase db, String code, String time, String han, String foreign) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_DRAMA_CAPTION (CODE, TIME, LANG_FOREIGN, LANG_HAN)" + CommConstants.sqlCR);
        sql.append("VALUES ('" + code + "','" + time + "','" + foreign.trim().replaceAll("'", "''") + "','" + han.trim().replaceAll("'", "''") + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

}
