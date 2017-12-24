package com.sleepingbear.pvhdictandvoc;


public class CaptionQuery {
    public static String getCategoryGroupKind() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, CODE KIND, DESC KIND_NAME" + CommConstants.sqlCR);
        sql.append("FROM    CATEGORY" + CommConstants.sqlCR);
        sql.append("WHERE   P_CODE = 'DRAMA'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getCategoryList(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, CODE KIND, DESC KIND_NAME" + CommConstants.sqlCR);
        sql.append("FROM    CATEGORY" + CommConstants.sqlCR);
        sql.append("WHERE   P_CODE = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getCategoryCaptionList(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, TIME, LANG_FOREIGN, LANG_HAN" + CommConstants.sqlCR);
        sql.append("FROM    CAPTION" + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }
}
