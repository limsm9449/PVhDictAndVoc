package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class DicQuery {
    public static String getDicForWord(String word) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT A.*, " + CommConstants.sqlCR);
        sql.append("       SEQ _id, " + CommConstants.sqlCR);
        sql.append("       (SELECT COUNT(*) FROM DIC_VOC WHERE ENTRY_ID = A.ENTRY_ID) MY_VOC " + CommConstants.sqlCR);
        sql.append("  FROM DIC A " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word.toLowerCase().replaceAll("'", " ") + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getVocCategory() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME," + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append("                       WHERE KIND = A.CODE" + CommConstants.sqlCR);
        sql.append("                       GROUP BY  KIND),0) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String updVocRandom() {
        StringBuffer sql = new StringBuffer();

        sql.append("UPDATE DIC_VOC" + CommConstants.sqlCR);
        sql.append("   SET RANDOM_SEQ = RANDOM()" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSampleAnswerForStudy(String vocKind, int answerCnt) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id," + CommConstants.sqlCR);
        sql.append("       WORD," + CommConstants.sqlCR);
        sql.append("       REPLACE(MEAN, '<br>', ' ') MEAN" + CommConstants.sqlCR);
        sql.append("FROM   DIC" + CommConstants.sqlCR);
        sql.append("WHERE  ENTRY_ID NOT IN (SELECT ENTRY_ID FROM DIC_VOC WHERE KIND = '" + vocKind + "')" + CommConstants.sqlCR);
        sql.append("AND    SPELLING != ''" + CommConstants.sqlCR);
        sql.append("ORDER  BY RANDOM()" + CommConstants.sqlCR);
        sql.append("LIMIT  " + answerCnt + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getVocabularyCategoryCount() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME," + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append("                       WHERE KIND = A.CODE),0) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSentenceViewContextMenu() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 2 _id, 2 ORD, CODE KIND, CODE_NAME||' 등록' KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,4" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /*
    public static String getVocabularyActivityContextMenu() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, 1 ORD, '-' KIND, '단어 보기' KIND_NAME" + CommConstants.sqlCR);
        sql.append(" UNION" + CommConstants.sqlCR);
        sql.append("SELECT 2 _id, 2 ORD, '-' KIND, '삭제' KIND_NAME" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }
    */

    public static String getVocabularyCategory() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getInsCategoryCode(SQLiteDatabase mDb) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT MAX(CODE) CODE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        String insCategoryCode = "";
        Cursor maxCategoryCursor = mDb.rawQuery(sql.toString(), null);
        if ( maxCategoryCursor.moveToNext() ) {
            int maxCategory = Integer.parseInt(maxCategoryCursor.getString(maxCategoryCursor.getColumnIndexOrThrow("CODE")).substring(2,5));
            insCategoryCode = "MY" + DicUtils.lpadding(Integer.toString(maxCategory + 1), 3, "0");
        }

        return insCategoryCode;
    }

    public static String getInsNewCategory(String codeGroup, String code, String codeName) {
        StringBuffer sql = new StringBuffer();

        sql.append("INSERT INTO DIC_CODE(CODE_GROUP, CODE, CODE_NAME)" + CommConstants.sqlCR);
        sql.append("VALUES('" + codeGroup + "', '" + code + "', '" + codeName + "')" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getUpdCategory(String codeGroup, String code, String codeName) {
        StringBuffer sql = new StringBuffer();

        sql.append("UPDATE DIC_CODE" + CommConstants.sqlCR);
        sql.append("   SET CODE_NAME = '" + codeName + "'" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + codeGroup + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDelCategory(String codeGroup, String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + codeGroup + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDelDicVoc(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMainCategoryCount() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'GRP'" + CommConstants.sqlCR);
        sql.append("   AND CODE <> 'MY'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSubCategoryCount(String codeGroup) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, 2 ORD, CODE KIND, CODE_NAME KIND_NAME, " + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_CATEGORY_WORD" + CommConstants.sqlCR);
        sql.append("                       WHERE CODE = A.CODE),0) W_CNT, " + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_CATEGORY_SENT" + CommConstants.sqlCR);
        sql.append("                       WHERE CODE = A.CODE),0) S_CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + codeGroup + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,4" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMyDic() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT A.*, B.WORD " + CommConstants.sqlCR);
        sql.append(" FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);


        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMyMemoryDic() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT A.*, B.WORD " + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.MEMORIZATION = 'Y'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getToday() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT A.TODAY, B.WORD, B.ENTRY_ID " + CommConstants.sqlCR);
        sql.append("  FROM DIC_TODAY A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getVocabularyCount() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getGrammar() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, GRAMMAR, MEAN, DESCRIPTION, SAMPLES, ORD " + CommConstants.sqlCR);
        sql.append(" FROM DIC_GRAMMAR" + CommConstants.sqlCR);
        sql.append("ORDER BY GRAMMAR" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSaveVocabulary(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT B.WORD, B.SPELLING, B.MEAN" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY B.WORD" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMySample() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, TODAY, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("  FROM DIC_MY_SAMPLE" + CommConstants.sqlCR);
        sql.append(" ORDER BY TODAY DESC, SENTENCE1" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }
}
