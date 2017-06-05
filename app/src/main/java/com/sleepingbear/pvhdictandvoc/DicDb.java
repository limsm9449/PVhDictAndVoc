package com.sleepingbear.pvhdictandvoc;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;

public class DicDb {

    public static String getWriteData() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT '" + CommConstants.tag_code_ins + "'||':'||A.CODE_GROUP||':'||A.CODE||':'||A.CODE_NAME WRITE_DATA" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP IN ('MY_VOC','C01','C02')" + CommConstants.sqlCR);
        sql.append("   AND CODE NOT IN ('VOC0001','C010001')" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_note_ins + "'||':'||CODE||':'||SAMPLE_SEQ WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_NOTE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP IN ('C01','C02') )" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_voc_ins + "'||':'||A.KIND||':'||A.ENTRY_ID||':'||A.INS_DATE||':'||A.MEMORIZATION WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_history_ins + "'||':'||SEQ||':'||WORD WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_SEARCH_HISTORY" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_click_word_ins + "'||':'||ENTRY_ID||':'||INS_DATE WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_CLICK_WORD" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 단어장 초기화
     * @param db
     */
    public static void initVocabulary(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE != 'VOC0001'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void initHistory(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 학습 회화 초기화
     * @param db
     */
    public static void initConversationNote(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP = 'C02') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C02'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * My 학습 초기화
     * @param db
     */
    public static void initMyConversationNote(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP = 'C01') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
        sql.append("   AND CODE != 'C010001'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void initDicClickWord(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insConversationToNote(SQLiteDatabase db, String code, String sampleSeq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + code + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + code + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insSearchHistory(SQLiteDatabase db, String word) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_SEARCH_HISTORY (WORD) " + CommConstants.sqlCR);
        sql.append("VALUES( '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insSearchHistory(SQLiteDatabase db, String seq, String word) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_SEARCH_HISTORY (SEQ, WORD) " + CommConstants.sqlCR);
        sql.append("VALUES( '" + seq + "', '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delSearchHistory(SQLiteDatabase db, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY " + CommConstants.sqlCR);
        sql.append(" WHERE SEQ = " + seq + "" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }
    /**
     * 클릭한 단어를 저장한다.
     * @param db
     * @param entryId
     * @param insDate
     */
    public static void insDicClickWord(SQLiteDatabase db, String entryId, String insDate) {
        if ( "".equals(insDate) ) {
            insDate = DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".");
        }

        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_CLICK_WORD (ENTRY_ID, INS_DATE) " + CommConstants.sqlCR);
        sql.append("VALUES ( '" + entryId + "','" + insDate + "') " + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicClickWord(SQLiteDatabase db, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        sql.append(" WHERE SEQ = " + seq + "" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }
    /**
     * 코드 등록
     * @param db
     * @param groupCode
     * @param code
     * @param codeName
     */
    public static void insCode(SQLiteDatabase db, String groupCode, String code, String codeName) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_CODE (CODE_GROUP, CODE, CODE_NAME) " + CommConstants.sqlCR);
        sql.append("VALUES('" + groupCode + "', '" + code + "', '" + codeName + "') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDicVoc(SQLiteDatabase db, String kind, String entryId, String insDate, String memory) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + kind + "', ENTRY_ID, '" + memory + "', RANDOM(), '" + insDate + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }



    public static void insDicVoc(SQLiteDatabase db, String entryId, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + kind + "', ENTRY_ID, 'N', RANDOM(), '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".")  + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDicVocForWord(SQLiteDatabase db, String word, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = (SELECT ENTRY_ID FROM DIC WHERE WORD = '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + kind + "', ENTRY_ID, 'N', RANDOM(), '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".")  + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = (SELECT ENTRY_ID FROM DIC WHERE WORD = '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicVoc(SQLiteDatabase db, String entryId, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicVocAll(SQLiteDatabase db, String entryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insToday(SQLiteDatabase db, String entryId, String today) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_TODAY(TODAY, ENTRY_ID) " + CommConstants.sqlCR);
        sql.append("SELECT '" + today + "', ENTRY_ID " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void updMemory(SQLiteDatabase db, String entryId, String memoryYn) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE DIC_VOC " + CommConstants.sqlCR);
        sql.append("   SET MEMORIZATION = '" + memoryYn + "'" + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "' " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 오늘이 단어 초기화
     * @param db
     */
    public static void initToday(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_TODAY" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static String getEntryIdForWord(SQLiteDatabase db, String word) {
        String rtn = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ENTRY_ID  " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        }
        cursor.close();

        return rtn;
    }

    public static boolean isExistMySample(SQLiteDatabase db, String sentence) {
        boolean rtn = false;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) CNT  " + CommConstants.sqlCR);
        sql.append("  FROM DIC_NOTE A, DIC_SAMPLE B " + CommConstants.sqlCR);
        sql.append(" WHERE A.SAMPLE_SEQ = B.SEQ " + CommConstants.sqlCR);
        sql.append("   AND A.CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP = 'C01')" + CommConstants.sqlCR);
        sql.append("   AND B.SENTENCE1 = '" + sentence + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            if ( cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) > 0 ) {
                rtn = true;
            }
        }
        cursor.close();

        return rtn;
    }

    public static String getSampleSeq(SQLiteDatabase db, String sentence) {
        String rtn = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SEQ  " + CommConstants.sqlCR);
        sql.append("  FROM DIC_SAMPLE " + CommConstants.sqlCR);
        sql.append(" WHERE SENTENCE1 = '" + sentence + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));
        }
        cursor.close();

        return rtn;
    }

    public static void initSample(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_MY_SAMPLE" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicMySample(SQLiteDatabase db, String sentence) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_MY_SAMPLE " + CommConstants.sqlCR);
        sql.append(" WHERE SENTENCE1 = '" + sentence + "'" + CommConstants.sqlCR);
        DicUtils.dicLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDicMySample(SQLiteDatabase db, String sentence1, String sentence2) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_MY_SAMPLE (TODAY, SENTENCE1, SENTENCE2)" + CommConstants.sqlCR);
        sql.append("VALUES( '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + "', '" + sentence1 + "', '" + sentence2 + "')" +  CommConstants.sqlCR);
        DicUtils.dicLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void moveDicVoc(SQLiteDatabase db, String currKind, String copyKind, String entryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + copyKind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + copyKind + "', ENTRY_ID, 'N', RANDOM(), '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + currKind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insConversationStudy(SQLiteDatabase db, String sampleSeq, String insDate) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("FROM    DIC_CODE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE_GROUP = 'C02'" +  CommConstants.sqlCR);
        sql.append("AND     CODE = '" + insDate + "'" + CommConstants.sqlCR);
        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            if ( cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) == 0 ) {
                sql.setLength(0);
                sql.append("INSERT INTO DIC_CODE(CODE_GROUP, CODE, CODE_NAME) " + CommConstants.sqlCR);
                sql.append("VALUES('C02', '" + insDate + "', '" + insDate + "')" + CommConstants.sqlCR);
                db.execSQL(sql.toString());
            }
        }
        cursor.close();

        sql.setLength(0);
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + insDate + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + insDate + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("UPDATE  DIC_CODE " + CommConstants.sqlCR);
        sql.append("SET     CODE_NAME = CODE || ' - ' || ( SELECT COUNT(*) FROM DIC_NOTE WHERE CODE = DIC_CODE.CODE ) || '개를 학습 하셨습니다.'  " + CommConstants.sqlCR);
        sql.append("WHERE   CODE_GROUP = 'C02' AND CODE = '" + insDate + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delConversationFromNote(SQLiteDatabase db, String code, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append(" WHERE CODE = '" + code + "'" + CommConstants.sqlCR);
        sql.append("   AND SAMPLE_SEQ = " + seq + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void moveConversationToNote(SQLiteDatabase db, String currKind, String copyKind, int sampleSeq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + copyKind + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + copyKind + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + currKind + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = " + sampleSeq + CommConstants.sqlCR);
        db.execSQL(sql.toString());
    }

    /**
     * 뜻을 가져온다.
     * @param db
     * @param word
     * @return
     */
    public static HashMap getMean(SQLiteDatabase db, String word) {
        HashMap rtn = new HashMap();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SPELLING, MEAN, ENTRY_ID  " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word.toLowerCase().replaceAll("'", " ") + "'" + CommConstants.sqlCR);
        sql.append("ORDER  BY WORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn.put("SPELLING", cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            rtn.put("MEAN", cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
            rtn.put("ENTRY_ID", cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")));
        }
        cursor.close();

        return rtn;
    }

    public static HashMap getWordsInfo(SQLiteDatabase db, String words) {
        HashMap hm = new HashMap();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT WORD, SPELLING, ENTRY_ID" + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD IN ('" + words.toLowerCase().replaceAll("'", " ").replaceAll(",", "','") + "')" + CommConstants.sqlCR);
        sql.append("ORDER  BY WORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        while ( cursor.moveToNext() ) {
            hm.put(cursor.getString(cursor.getColumnIndexOrThrow("WORD")) + "_SPELLING", cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            hm.put(cursor.getString(cursor.getColumnIndexOrThrow("WORD")) + "_ENTRY_ID", cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")));
        }
        cursor.close();

        return hm;
    }

    public static void insToday10(SQLiteDatabase db, String today) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_TODAY(TODAY, ENTRY_ID) " + CommConstants.sqlCR);
        sql.append("SELECT '" + today + "', ENTRY_ID FROM (" + CommConstants.sqlCR);
        sql.append("SELECT ENTRY_ID, WORD, RANDOM() RND" + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = 'VH'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID NOT IN ( SELECT ENTRY_ID FROM DIC_TODAY ) " + CommConstants.sqlCR);
        sql.append(" ) ORDER BY RND LIMIT 10" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }
}
