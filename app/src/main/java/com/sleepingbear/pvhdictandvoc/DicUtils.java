package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            } else if ( preference.equals(CommConstants.preferences_webViewFont) ) {
                rtn = "3";
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
                fos = ctx.openFileOutput(CommConstants.infoFileName, Context.MODE_PRIVATE);
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
            isHangule = str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
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

    public static void setAdView(AppCompatActivity app) {
        AdView av = (AdView)app.findViewById(R.id.adView);
        if ( CommConstants.isFreeApp ) {
            AdRequest adRequest = new  AdRequest.Builder().build();
            av.loadAd(adRequest);
        } else {
            av.setVisibility(View.GONE);
        }
    }

    public static Document getDocument(String url) throws Exception {
        Document doc = null;
        doc = Jsoup.connect(url).timeout(60000).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US;   rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();

        return doc;
    }

    public static String getHtmlString(String title, String contents, int fontSize) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!doctype html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<script src='https://code.jquery.com/jquery-1.11.3.js'></script>");
        sb.append("<script>");
        sb.append("$( document ).ready(function() {");
        sb.append("    $('#news_title,#news_contents').html(function(index, oldHtml) {");
        sb.append("         var word = oldHtml.replace(/(\\n)/gi, '\\n ').replace(/(<([^>]+)>)/gi, '').replace(/(&nbsp;)/gi, '').split(' ');");
        sb.append("         for ( var i = 0; i < word.length; i++ ) {");
        sb.append("             word[i] = '<span class=\"word\">' + word[i] + '</span>';");
        sb.append("         }");
        sb.append("         return word.join(' ').replace(/(\\n)/gi, '<br>');");
        sb.append("    });");
        sb.append("    $('.word').click(function(event) {");
        sb.append("        window.android.setWord(event.target.innerHTML)");
        sb.append("    });");
        sb.append("});");
        sb.append("</script>");

        sb.append("<body>");
        sb.append("<font size='" + fontSize + "' face='돋움'><div id='news_contents'>");
        sb.append(contents);
        sb.append("</div></font></body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static String[] getSentencesArray(String str) {
        ArrayList al = new ArrayList();
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        Matcher reMatcher = re.matcher(str);
        while (reMatcher.find()) {
            dicLog(reMatcher.group());
            al.add(reMatcher.group());
        }

        String[] rtn = new String[al.size()];
        for ( int i = 0; i < al.size(); i++ ) {
            rtn[i] = (String)al.get(i);
        }
        return rtn;
    }

    public static String[] getNews(String kind) {
        String[] news = new String[5];
        int idx = 0;

        if ( "N".equals(kind) ) {
            news[idx++] = "Tuoi Tre Newspaper";
            news[idx++] = "Nhan Dan Newspaper";
            news[idx++] = "Lao Dong Newspaper";
            news[idx++] = "VN Express";
            news[idx++] = "Vietnam Net";
        } else if ( "C".equals(kind) ) {
            news[idx++] = CommConstants.news_tuoiTreNews;
            news[idx++] = CommConstants.news_nhanDanNews;
            news[idx++] = CommConstants.news_laoDongNews;
            news[idx++] = CommConstants.news_vnexpressNews;
            news[idx++] = CommConstants.news_vietnamNetNews;
        } else if ( "U".equals(kind) ) {
            news[idx++] = "http://tuoitre.vn/";
            news[idx++] = "http://www.nhandan.org.vn/";
            news[idx++] = "http://laodong.vn/";
            news[idx++] = "http://vnexpress.net/";
            news[idx++] = "http://m.vietnamnet.vn/";
        } else if ( "W".equals(kind) ) {
            news[idx++] = "E1";
            news[idx++] = "E2";
            news[idx++] = "E3";
            news[idx++] = "E4";
            news[idx++] = "E5";
        }

        return news;
    }

    public static String[] getNewsCategory(String newsCode, String kind) {
        String[] category = new String[1];
        int idx = 0;
        ArrayList al = new ArrayList();

        if ( newsCode.equals(CommConstants.news_tuoiTreNews) ) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("Thời sự",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/thoi-su.htm"));
            al.add(idx++, getNewsInfo("Thời sự - Xã hội",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/thoi-su/xa-hoi.htm"));
            al.add(idx++, getNewsInfo("Thời sự - Phóng sự",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/thoi-su/phong-su.htm"));
            al.add(idx++, getNewsInfo("Thời sự - Nghĩ",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/thoi-su/nghi.htm"));

            al.add(idx++, getNewsInfo("Thế giới",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/the-gioi.htm"));
            al.add(idx++, getNewsInfo("Thế giới - Bình luận",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/the-gioi/binh-luan.htm"));
            al.add(idx++, getNewsInfo("Thế giới - Kiều bào",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/the-gioi/kieu-bao.htm"));
            al.add(idx++, getNewsInfo("Thế giới - Muôn màu",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/the-gioi/muon-mau.htm"));
            al.add(idx++, getNewsInfo("Thế giới - Hồ sơ",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/the-gioi/ho-so.htm"));

            al.add(idx++, getNewsInfo("Pháp luật",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/phap-luat.htm"));
            al.add(idx++, getNewsInfo("Pháp luật - Chuyện pháp đình",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/phap-luat/chuyen-phap-dinh.htm"));
            al.add(idx++, getNewsInfo("Pháp luật - Tư vấn",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/phap-luat/tu-van.htm"));
            al.add(idx++, getNewsInfo("Pháp luật - Pháp lý",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/phap-luat/phap-ly.htm"));

            al.add(idx++, getNewsInfo("Kinh doanh",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/kinh-doanh.htm"));
            al.add(idx++, getNewsInfo("Kinh doanh - Tài chính",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/kinh-doanh/tai-chinh.htm"));
            al.add(idx++, getNewsInfo("Kinh doanh - Doanh nghiệp",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/kinh-doanh/doanh-nghiep.htm"));
            al.add(idx++, getNewsInfo("Kinh doanh - Mua sắm",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/kinh-doanh/mua-sam.htm"));
            al.add(idx++, getNewsInfo("Kinh doanh - Đầu tư",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/kinh-doanh/dau-tu.htm"));

            al.add(idx++, getNewsInfo("Bất động sản - Tin thị trường",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/tin-thi-truong"));
            al.add(idx++, getNewsInfo("Bất động sản - Quy hoạch - Chính sách",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/quy-hoach-chinh-sach"));
            al.add(idx++, getNewsInfo("Bất động sản - Đầu tư",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/dau-tu"));

            al.add(idx++, getNewsInfo("Xây dựng - Thông tin",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/xay-dung/thong-tin"));
            al.add(idx++, getNewsInfo("Xây dựng - Vật liệu",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/xay-dung/vat-lieu"));
            al.add(idx++, getNewsInfo("Xây dựng - Giải pháp",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/xay-dung/giai-phap"));

            al.add(idx++, getNewsInfo("Kiến trúc nhà đẹp - Kiến trúc",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/kien-truc-nha-dep/kien-truc"));
            al.add(idx++, getNewsInfo("Kiến trúc nhà đẹp - Nội ngoại thất",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/kien-truc-nha-dep/noi-ngoai-that"));
            al.add(idx++, getNewsInfo("Kiến trúc nhà đẹp - Không gian sống",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/kien-truc-nha-dep/khong-gian-song"));
            al.add(idx++, getNewsInfo("Kiến trúc nhà đẹp - Xu hướng mới",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/kien-truc-nha-dep/xu-huong-moi"));

            al.add(idx++, getNewsInfo("Phong thủy - Nhà ở",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/phong-thuy/nha-o"));
            al.add(idx++, getNewsInfo("Phong thủy - Văn phòng",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/phong-thuy/van-phong"));
            al.add(idx++, getNewsInfo("Phong thủy - Tuổi tác",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/phong-thuy/tuoi-tac"));

            al.add(idx++, getNewsInfo("Tư vấn-Hỏi đáp - Xây dựng",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/tu-van-hoi-dap/xay-dung"));
            al.add(idx++, getNewsInfo("Tư vấn-Hỏi đáp - Kiến trúc",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/tu-van-hoi-dap/kien-truc"));
            al.add(idx++, getNewsInfo("Tư vấn-Hỏi đáp - Luật",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/tu-van-hoi-dap/luat"));
            al.add(idx++, getNewsInfo("Tư vấn-Hỏi đáp - Hỏi đáp",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://batdongsan.tuoitre.vn/tin/tu-van-hoi-dap/hoi-dap"));

            al.add(idx++, getNewsInfo("Nhịp sống số - Điện thoại",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/dien-thoai.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Thiết bị số",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/thiet-bi-so.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Thị Trường",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/thi-truong.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Đánh giá",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/danh-gia-san-pham.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Blog",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/blog.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Bảo mật",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/bao-mat.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Tư vấn",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/tu-van-tieu-dung.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Thủ thuật- Kiến thức",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/thu-thuat-kien-thuc.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống số - Nhịp cầu",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://nhipsongso.tuoitre.vn/nhip-cau.htm"));

            al.add(idx++, getNewsInfo("Xe",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/xe.htm"));

            al.add(idx++, getNewsInfo("Du lịch - Những miền đất lạ",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://dulich.tuoitre.vn/nhung-mien-dat-la.htm"));
            al.add(idx++, getNewsInfo("Du lịch - Trải nghiệm - Khám phá",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://dulich.tuoitre.vn/trai-nghiem-kham-pha.htm"));
            al.add(idx++, getNewsInfo("Du lịch - Ẩm thực",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://dulich.tuoitre.vn/am-thuc.htm"));
            al.add(idx++, getNewsInfo("Du lịch - Văn hóa",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://dulich.tuoitre.vn/van-hoa.htm"));
            al.add(idx++, getNewsInfo("Du lịch - Góc ảnh lữ hành",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://dulich.tuoitre.vn/goc-anh-lu-hanh.htm"));

            al.add(idx++, getNewsInfo("Nhịp sống trẻ - Xu hướng",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/nhip-song-tre/xu-huong.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống trẻ - Khám phá",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/nhip-song-tre/kham-pha.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống trẻ - Yêu",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/nhip-song-tre/yeu.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống trẻ - Nhân vật",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/nhip-song-tre/nhan-vat.htm"));
            al.add(idx++, getNewsInfo("Nhịp sống trẻ - Việc làm",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/nhip-song-tre/viec-lam.htm"));

            al.add(idx++, getNewsInfo("Văn hóa - Đời sống",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/van-hoa/doi-song.htm"));
            al.add(idx++, getNewsInfo("Văn hóa - Văn học - Sách",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/van-hoa/van-hoc-sach.htm"));

            al.add(idx++, getNewsInfo("Giải trí - Âm nhạc",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giai-tri/am-nhac.htm"));
            al.add(idx++, getNewsInfo("Giải trí - Điện ảnh",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giai-tri/dien-anh.htm"));
            al.add(idx++, getNewsInfo("Giải trí - TV Show",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giai-tri/tv-show.htm"));
            al.add(idx++, getNewsInfo("Giải trí - Thời trang",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giai-tri/thoi-trang.htm"));
            al.add(idx++, getNewsInfo("Giải trí - Hậu trường",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giai-tri/hau-truong.htm"));

            al.add(idx++, getNewsInfo("Giáo dục - Học đường",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giao-duc/hoc-duong.htm"));
            al.add(idx++, getNewsInfo("Giáo dục - Du học",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giao-duc/du-hoc.htm"));
            al.add(idx++, getNewsInfo("Giáo dục - Câu chuyện giáo dục",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giao-duc/cau-chuyen-giao-duc.htm"));
            al.add(idx++, getNewsInfo("Giáo dục - Góc học tập",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/giao-duc/goc-hoc-tap.htm"));

            al.add(idx++, getNewsInfo("Khoa học - Thường thức",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/khoa-hoc/thuong-thuc.htm"));
            al.add(idx++, getNewsInfo("Khoa học - Phát minh",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/khoa-hoc/phat-minh.htm"));

            al.add(idx++, getNewsInfo("Sức khỏe - Dinh dưỡng",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/suc-khoe/dinh-duong.htm"));
            al.add(idx++, getNewsInfo("Sức khỏe - Mẹ & Bé",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/suc-khoe/me-va-be.htm"));
            al.add(idx++, getNewsInfo("Sức khỏe - Giới tính",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/suc-khoe/gioi-tinh.htm"));
            al.add(idx++, getNewsInfo("Sức khỏe - Phòng mạch",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/suc-khoe/phong-mach.htm"));
            al.add(idx++, getNewsInfo("Sức khỏe - Biết để khỏe",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/suc-khoe/biet-de-khoe.htm"));

            al.add(idx++, getNewsInfo("Giả - Thật",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://tuoitre.vn/gia-that.htm"));

            al.add(idx++, getNewsInfo("Thể thao - Champions League",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/champions-league"));
            al.add(idx++, getNewsInfo("Thể thao - Premier League",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/premier-league"));
            al.add(idx++, getNewsInfo("Thể thao - Serie A",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/seria"));
            al.add(idx++, getNewsInfo("Thể thao - La Liga",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/la-liga"));
            al.add(idx++, getNewsInfo("Thể thao - UEFA Cup",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/uefa-cup"));
            al.add(idx++, getNewsInfo("Thể thao - Bundesliga",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/bong-da/bundesliga"));
            al.add(idx++, getNewsInfo("Thể thao - Quần vợt",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/quan-vot"));
            al.add(idx++, getNewsInfo("Thể thao - Các môn khác",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/cac-mon-khac"));
            al.add(idx++, getNewsInfo("Thể thao - Thể thao & Cuộc sống",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/the-thao-cuoc-song"));
            al.add(idx++, getNewsInfo("Thể thao - Hậu trường",CommConstants.news_tuoiTreNews + "_" + cIdx++,"http://thethao.tuoitre.vn/tin/hau-truong"));
        } else if ( newsCode.equals(CommConstants.news_nhanDanNews) ) {
            int cIdx = 1;
            String domainUrl = "http://www.nhandan.org.vn/";

            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Tin tức - Sự kiện", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/tin-tuc-su-kien"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Xã luận", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/xa-luan"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Cùng suy ngẫm", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/cung-suy-ngam"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Bình luận - Phê phán", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/binh-luan-phe-phan"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Tuyên truyền Hiến pháp", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/tuyentruyenhienphap"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Dân tộc - Miền núi", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/dan-toc-mien-nui"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Đảng và cuộc sống", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/dang-va-cuoc-song"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Người Việt xa xứ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/nguoi-viet-xa-xu"));
            al.add(idx++, getNewsInfo("CHÍNH TRỊ - Đồng hành vì đồng bào biển đảo", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chinhtri/dong-hanh"));

            al.add(idx++, getNewsInfo("KINH TẾ - Thời sự", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "kinhte/thoi_su"));
            al.add(idx++, getNewsInfo("KINH TẾ - Nhận định", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "kinhte/nhan-dinh"));
            al.add(idx++, getNewsInfo("KINH TẾ - Chuyện làm ăn", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "kinhte/chuyen-lam-an"));

            al.add(idx++, getNewsInfo("Chứng khoán - THỊ TRƯỜNG", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-thitruong"));
            al.add(idx++, getNewsInfo("Chứng khoán - CHÍNH SÁCH", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-chinhsach"));
            al.add(idx++, getNewsInfo("Chứng khoán - TIN NGÀNH", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-tinnganh"));
            al.add(idx++, getNewsInfo("Chứng khoán - UPCOM", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-doanhnghiep"));
            al.add(idx++, getNewsInfo("Chứng khoán - TRÁI PHIẾU", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-traiphieu"));
            al.add(idx++, getNewsInfo("Chứng khoán - IPO", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/chungkhoan-ipo"));
            al.add(idx++, getNewsInfo("Chứng khoán - PHÁI SINH", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "chungkhoan/phai-sinh"));

            al.add(idx++, getNewsInfo("VĂN HÓA - Dòng chảy", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "vanhoa/dong-chay"));
            al.add(idx++, getNewsInfo("VĂN HÓA - Diễn đàn", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "vanhoa/dien-dan"));
            al.add(idx++, getNewsInfo("VĂN HÓA - Di sản", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "vanhoa/di-san"));
            al.add(idx++, getNewsInfo("VĂN HÓA - Du lịch", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "vanhoa/du_lich"));
            al.add(idx++, getNewsInfo("VĂN HÓA - Nghe - Đọc - Xem", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "vanhoa/nghe-doc-xem"));

            al.add(idx++, getNewsInfo("XÃ HỘI - Tin tức", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "xahoi/tin-tuc"));
            al.add(idx++, getNewsInfo("XÃ HỘI - Giao thông", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "xahoi/giao-thong"));
            al.add(idx++, getNewsInfo("XÃ HỘI - BHXH và cuộc sống", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "xahoi/bhxh-va-cuoc-song"));
            al.add(idx++, getNewsInfo("XÃ HỘI - Nhân ái", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "xahoi/nhan-ai"));
            al.add(idx++, getNewsInfo("XÃ HỘI - Phóng sự - Ký sự", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "phongsu"));

            al.add(idx++, getNewsInfo("THẾ GIỚI - Tin tức", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/tin-tuc"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Chuyện thời sự", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/chuyen-thoi-su"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Bình luận quốc tế", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/binh-luan-quoc-te"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Hồ sơ - Tư liệu", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/ho-so-tu-lieu"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Góc nhìn thứ Hai", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/goc-nhin-thu-hai"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Cộng đồng ASEAN", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/asean"));
            al.add(idx++, getNewsInfo("THẾ GIỚI - Cửa sổ thế giới", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thegioi/cua-so-the-gioi"));

            al.add(idx++, getNewsInfo("CÔNG NGHỆ - Thông tin số", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "congnghe/thong-tin-so"));
            al.add(idx++, getNewsInfo("CÔNG NGHỆ - Bảo mật", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "congnghe/bao-mat"));
            al.add(idx++, getNewsInfo("CÔNG NGHỆ - Viễn thông", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "congnghe/vien-thong"));
            al.add(idx++, getNewsInfo("CÔNG NGHỆ - Phần mềm - Giải pháp", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "congnghe/phan-mem-giai-phap"));

            al.add(idx++, getNewsInfo("KHOA HỌC - Khoa học", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "khoahoc/khoa-hoc"));
            al.add(idx++, getNewsInfo("KHOA HỌC - Môi trường", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "khoahoc/moi-truong"));
            al.add(idx++, getNewsInfo("KHOA HỌC - Thiên nhiên", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "khoahoc/thien-nhien"));
            al.add(idx++, getNewsInfo("KHOA HỌC - Nhân vật", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "khoahoc/nhan-vat"));

            al.add(idx++, getNewsInfo("GIÁO DỤC - Tin tức", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "giaoduc/tin-tuc"));
            al.add(idx++, getNewsInfo("GIÁO DỤC - Tuyển sinh", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "giaoduc/tuyen-sinh"));
            al.add(idx++, getNewsInfo("GIÁO DỤC - Diễn đàn", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "giaoduc/dien-dan"));
            al.add(idx++, getNewsInfo("GIÁO DỤC - Du học", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "giaoduc/du-hoc"));

            al.add(idx++, getNewsInfo("SỨC KHỎE - Tin tức", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "suckhoe/tin-tuc"));
            al.add(idx++, getNewsInfo("SỨC KHỎE - Sống khỏe", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "suckhoe/song-khoe"));
            al.add(idx++, getNewsInfo("SỨC KHỎE - Tiêu điểm", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "suckhoe/tieu-diem"));

            al.add(idx++, getNewsInfo("PHÁP LUẬT - Thời sự pháp luật", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "phapluat/thoi-su-phap-luat"));
            al.add(idx++, getNewsInfo("PHÁP LUẬT - Giải đáp thắc mắc", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "phapluat/giai-dap-thac-mac"));
            al.add(idx++, getNewsInfo("PHÁP LUẬT - Văn bản mới", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "phapluat/van-ban-moi"));
            al.add(idx++, getNewsInfo("PHÁP LUẬT - Cải cách tư pháp", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "phapluat/cai-cach-tu-phap"));

            al.add(idx++, getNewsInfo("THỂ THAO - Tin tức - Sự kiện", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thethao/tin-tuc"));
            al.add(idx++, getNewsInfo("THỂ THAO - Bình luận", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thethao/binh-luan"));
            al.add(idx++, getNewsInfo("THỂ THAO - Gương mặt", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "thethao/guong-mat"));

            al.add(idx++, getNewsInfo("BẠN ĐỌC - Đường dây nóng", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "bandoc/duong-day-nong"));
            al.add(idx++, getNewsInfo("BẠN ĐỌC - Điều tra qua thư bạn đọc", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "bandoc/dieu-tra-qua-thu-ban-doc"));
            al.add(idx++, getNewsInfo("BẠN ĐỌC - Bạn đọc viết", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "bandoc/ban-doc-viet"));
            al.add(idx++, getNewsInfo("BẠN ĐỌC - Bạn cần biết", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "bandoc/bancanbiet"));
            al.add(idx++, getNewsInfo("BẠN ĐỌC - Chương trình phát sóng", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "bandoc/chuong_trinh_phat_song"));

            al.add(idx++, getNewsInfo("Hà Nội", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi"));
            al.add(idx++, getNewsInfo("Những tình yêu Hà Nội", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/nhung-tinh-yeu-hn"));
            al.add(idx++, getNewsInfo("HÀ NỘI TRẺ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/ha-noi-tre"));
            al.add(idx++, getNewsInfo("TÀI CHÍNH – NGÂN HÀNG", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/tai-chinh-ngan-hang"));
            al.add(idx++, getNewsInfo("QUY HOẠCH - ĐẦU TƯ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/quy-hoach-dau-tu"));
            al.add(idx++, getNewsInfo("THỊ TRƯỜNG BĐS", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/thi-truong-bds"));
            al.add(idx++, getNewsInfo("CHÙM PHÓNG SỰ ẢNH", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/chum-phong-su-anh"));
            al.add(idx++, getNewsInfo("TIN MỚI NHẬN", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/tin-moi-nhan"));
            al.add(idx++, getNewsInfo("ĐỂ THỦ ĐÔ TA.. ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/de-thu-do-ta"));
            al.add(idx++, getNewsInfo("NHỎ NHẸ NHẮC NHAU", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/nho-nhe-nhac-nhau"));
            al.add(idx++, getNewsInfo("GƯƠNG SÁNG, VIỆC HAY", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/guong-sang-viec-hay"));
            al.add(idx++, getNewsInfo("CHUYỆN QUẢN LÝ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/chuyen-quan-ly"));
            al.add(idx++, getNewsInfo("ĐỐI THOẠI", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "hanoi/doi-thoai"));

            al.add(idx++, getNewsInfo("TP HCM", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm"));
            al.add(idx++, getNewsInfo("THÔNG TIN KINH TẾ", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/thong-tin-kinh-te"));
            al.add(idx++, getNewsInfo("CHUYỆN PHỐ PHƯỜNG", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/chuyen-pho-phuong"));
            al.add(idx++, getNewsInfo("TIN ĐỌC NHANH", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/tin-doc-nhanh"));
            al.add(idx++, getNewsInfo("ỐNG KÍNH CUỘC SỐNG", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/ong-kinh-cuoc-song"));
            al.add(idx++, getNewsInfo("DÂN BIẾT DÂN BÀN", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/dan-biet-dan-ban"));
            al.add(idx++, getNewsInfo("CHUYỆN XƯA CHUYỆN NAY", CommConstants.news_nhanDanNews + "_" + cIdx++, domainUrl + "tphcm/chuyen-xua-chuyen-nay"));
        } else if ( newsCode.equals(CommConstants.news_laoDongNews) ) {
            int cIdx = 1;

            al.add(idx++, getNewsInfo("Thời sự", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/thoi-su/"));
            al.add(idx++, getNewsInfo("Công đoàn", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/cong-doan/"));
            al.add(idx++, getNewsInfo("Thế giới", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/the-gioi/"));
            al.add(idx++, getNewsInfo("Xã hội", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/xa-hoi/"));
            al.add(idx++, getNewsInfo("Kinh tế", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/kinh-te/"));
            al.add(idx++, getNewsInfo("Pháp luật", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/phap-luat/"));
            al.add(idx++, getNewsInfo("Văn hóa - Giải trí", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/van-hoa-giai-tri/"));
            al.add(idx++, getNewsInfo("Thể thao", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/the-thao/"));
            al.add(idx++, getNewsInfo("Phóng sự", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/phong-su/"));
            al.add(idx++, getNewsInfo("Du lịch", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/du-lich/"));
            al.add(idx++, getNewsInfo("Công nghệ", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/cong-nghe/"));
            al.add(idx++, getNewsInfo("Sức khỏe", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/suc-khoe/"));
            al.add(idx++, getNewsInfo("Bạn đọc", CommConstants.news_laoDongNews + "_" + cIdx++, "http://laodong.vn/ban-doc/"));
        } else if ( newsCode.equals(CommConstants.news_vnexpressNews) ) {
            int cIdx = 1;
            String domainUrl = "https://vnexpress.net/";

            al.add(idx++, getNewsInfo("Thời sự", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/thoi-su")));
            al.add(idx++, getNewsInfo("Thế giới", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/the-gioi")));
            al.add(idx++, getNewsInfo("Kinh doanh", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://kinhdoanh.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Giải trí", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://giaitri.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Thể thao", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://thethao.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Pháp luật", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/phap-luat")));
            al.add(idx++, getNewsInfo("Giáo dục", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/giao-duc")));
            al.add(idx++, getNewsInfo("Sức khỏe", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://suckhoe.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Gia đình", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://giadinh.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Du lịch", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://dulich.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Khoa học", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/khoa-hoc")));
            al.add(idx++, getNewsInfo("Số hóa", CommConstants.news_vnexpressNews + "_" + cIdx++, "https://sohoa.vnexpress.net/"));
            al.add(idx++, getNewsInfo("Xe", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/oto-xe-may")));
            al.add(idx++, getNewsInfo("Cộng đồng", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/cong-dong")));
            al.add(idx++, getNewsInfo("Tâm sự", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/tam-su")));
            al.add(idx++, getNewsInfo("Ảnh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/anh")));
            al.add(idx++, getNewsInfo("Infographics", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/infographics")));
            al.add(idx++, getNewsInfo("Cười", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/tin-tuc/cuoi")));
        } else if ( newsCode.equals(CommConstants.news_vietnamNetNews) ) {
            int cIdx = 1;
            String domainUrl = "http://vietnamnet.vn/";

            al.add(idx++, getNewsInfo("Thời sự", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/")));
            al.add(idx++, getNewsInfo("Thời sự - Quân sự", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/quan-su/")));
            al.add(idx++, getNewsInfo("Thời sự - Chính trị", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/chinh-tri/")));
            al.add(idx++, getNewsInfo("Thời sự - Quốc hội", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/quoc-hoi/")));
            al.add(idx++, getNewsInfo("Thời sự - An toàn giao thông", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/an-toan-giao-thong/")));
            al.add(idx++, getNewsInfo("Thời sự - Clip Nóng", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/clip-nong/")));
            al.add(idx++, getNewsInfo("Thời sự - Bầu cử QH &amp; HĐND", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/bau-cu/")));
            al.add(idx++, getNewsInfo("Thời sự - Cải cách lương", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/tien-luong/")));
            al.add(idx++, getNewsInfo("Thời sự - Môi trường", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/thoi-su/moi-truong/")));
            al.add(idx++, getNewsInfo("Kinh doanh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/")));
            al.add(idx++, getNewsInfo("Kinh doanh - Tài chính", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/tai-chinh/")));
            al.add(idx++, getNewsInfo("Kinh doanh - Đầu tư", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/dau-tu/")));
            al.add(idx++, getNewsInfo("Kinh doanh - Thị trường", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/thi-truong/")));
            al.add(idx++, getNewsInfo("Kinh doanh - Doanh nhân", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/doanh-nhan/")));
            al.add(idx++, getNewsInfo("Kinh doanh - Tư vấn tài chính", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/kinh-doanh/tu-van-tai-chinh/")));
            al.add(idx++, getNewsInfo("Giải trí", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/")));
            al.add(idx++, getNewsInfo("Giải trí - Thế giới Sao", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/the-gioi-sao/")));
            al.add(idx++, getNewsInfo("Giải trí - Thời trang", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/thoi-trang/")));
            al.add(idx++, getNewsInfo("Giải trí - Nhạc", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/nhac/")));
            al.add(idx++, getNewsInfo("Giải trí - Phim", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/phim/")));
            al.add(idx++, getNewsInfo("Giải trí - Truyền hình", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/truyen-hinh/")));
            al.add(idx++, getNewsInfo("Giải trí - Sách", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/sach/")));
            al.add(idx++, getNewsInfo("Giải trí - Di sản - Mỹ thuật - Sân khấu", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giai-tri/di-san-my-thuat-san-khau/")));
            al.add(idx++, getNewsInfo("Thế giới", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/")));
            al.add(idx++, getNewsInfo("Thế giới - Bình luận quốc tế", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/binh-luan-quoc-te/")));
            al.add(idx++, getNewsInfo("Thế giới - Chân dung", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/chan-dung/")));
            al.add(idx++, getNewsInfo("Thế giới - Hồ sơ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/ho-so/")));
            al.add(idx++, getNewsInfo("Thế giới - Thế giới đó đây", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/the-gioi-do-day/")));
            al.add(idx++, getNewsInfo("Thế giới - Việt Nam và thế giới", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-gioi/viet-nam-va-the-gioi/")));
            al.add(idx++, getNewsInfo("Giáo dục", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/")));
            al.add(idx++, getNewsInfo("Giáo dục - Người thầy", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/nguoi-thay/")));
            al.add(idx++, getNewsInfo("Giáo dục - Tuyển sinh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/tuyen-sinh/")));
            al.add(idx++, getNewsInfo("Giáo dục - Du học", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/du-hoc/")));
            al.add(idx++, getNewsInfo("Giáo dục - Gương mặt trẻ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/guong-mat-tre/")));
            al.add(idx++, getNewsInfo("Giáo dục - Góc phụ huynh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/goc-phu-huynh/")));
            al.add(idx++, getNewsInfo("Giáo dục - Học tiếng Anh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/giao-duc/hoc-tieng-anh/")));
            al.add(idx++, getNewsInfo("Đời sống", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/")));
            al.add(idx++, getNewsInfo("Đời sống - Gia đình", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/gia-dinh/")));
            al.add(idx++, getNewsInfo("Đời sống - Sống lạ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/song-la/")));
            al.add(idx++, getNewsInfo("Đời sống - Giới trẻ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/gioi-tre/")));
            al.add(idx++, getNewsInfo("Đời sống - Tâm sự", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/tam-su/")));
            al.add(idx++, getNewsInfo("Đời sống - Mẹ và bé", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/me-va-be/")));
            al.add(idx++, getNewsInfo("Đời sống - Du lịch", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/du-lich/")));
            al.add(idx++, getNewsInfo("Đời sống - Ẩm thực", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/am-thuc/")));
            al.add(idx++, getNewsInfo("Đời sống - Mẹo vặt", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/doi-song/meo-vat/")));
            al.add(idx++, getNewsInfo("Pháp luật", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/phap-luat/")));
            al.add(idx++, getNewsInfo("Pháp luật - Hồ sơ vụ án", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/phap-luat/ho-so-vu-an/")));
            al.add(idx++, getNewsInfo("Pháp luật - Ký sự pháp đình", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/phap-luat/ky-su-phap-dinh/")));
            al.add(idx++, getNewsInfo("Pháp luật - Tư vấn pháp luật", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/phap-luat/tu-van-phap-luat/")));
            al.add(idx++, getNewsInfo("Thể thao", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/")));
            al.add(idx++, getNewsInfo("Thể thao - Bóng đá trong nước", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/bong-da-trong-nuoc/")));
            al.add(idx++, getNewsInfo("Thể thao - Bóng đá quốc tế", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/bong-da-quoc-te/")));
            al.add(idx++, getNewsInfo("Thể thao - Hậu trường", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/hau-truong/")));
            al.add(idx++, getNewsInfo("Thể thao - Các môn khác", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/cac-mon-khac/")));
            al.add(idx++, getNewsInfo("Thể thao - Video thể thao", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/video-the-thao/")));
            al.add(idx++, getNewsInfo("Thể thao - SEA Games", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/the-thao/sea-games/")));
            al.add(idx++, getNewsInfo("Công nghệ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/")));
            al.add(idx++, getNewsInfo("Công nghệ - Cộng đồng mạng", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/cong-dong-mang/")));
            al.add(idx++, getNewsInfo("Công nghệ - Sản phẩm", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/san-pham/")));
            al.add(idx++, getNewsInfo("Công nghệ - Ứng dụng", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/ung-dung/")));
            al.add(idx++, getNewsInfo("Công nghệ - Tin công nghệ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/tin-cong-nghe/")));
            al.add(idx++, getNewsInfo("Công nghệ - Viễn thông", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/vien-thong/")));
            al.add(idx++, getNewsInfo("Công nghệ - Bảo mật", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/cong-nghe/bao-mat/")));
            al.add(idx++, getNewsInfo("Sức khỏe", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Sức khoẻ 24h", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/suc-khoe-24h/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Chuyện phòng the", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/chuyen-phong-the/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Các bệnh", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/cac-loai-benh/")));
            al.add(idx++, getNewsInfo("Sức khỏe - An toàn thực phẩm", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/an-toan-thuc-pham/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Y học cổ truyền", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/y-hoc-co-truyen/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Tư vấn sức khỏe", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/tu-van-suc-khoe/")));
            al.add(idx++, getNewsInfo("Sức khỏe - Khoẻ đẹp", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/suc-khoe/khoe-dep/")));
            al.add(idx++, getNewsInfo("Bất động sản", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/bat-dong-san/")));
            al.add(idx++, getNewsInfo("Bất động sản - Dự án", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/bat-dong-san/du-an/")));
            al.add(idx++, getNewsInfo("Bất động sản - Nội thất", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/bat-dong-san/noi-that/")));
            al.add(idx++, getNewsInfo("Bất động sản - Tư vấn", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/bat-dong-san/kinh-nghiem-tu-van/")));
            al.add(idx++, getNewsInfo("Bất động sản - Tin bất động sản", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/bat-dong-san/thi-truong/")));
            al.add(idx++, getNewsInfo("Bạn đọc", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/ban-doc/")));
            al.add(idx++, getNewsInfo("Bạn đọc - Hồi âm", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/ban-doc/hoi-am/")));
            al.add(idx++, getNewsInfo("Bạn đọc - Chia sẻ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/ban-doc/chia-se/")));
            al.add(idx++, getNewsInfo("Bạn đọc - Thơ", CommConstants.news_vnexpressNews + "_" + cIdx++, getUrl(domainUrl, "/vn/ban-doc/tho/")));
            al.add(idx++, getNewsInfo("Blog", CommConstants.news_vnexpressNews + "_" + cIdx++, "http://vietnamnet.vn/vn/blog/"));
        }

        category = new String[al.size()];
        for ( int i = 0; i < al.size(); i++ ) {
            if ( "N".equals(kind) ) {
                category[i] = ((String[])al.get(i))[0];
            }else if ( "C".equals(kind) ) {
                category[i] = ((String[])al.get(i))[1];
            }else if ( "U".equals(kind) ) {
                category[i] = ((String[])al.get(i))[2];
            }
        }

        return category;
    }

    public static void getNewsCategoryNews(SQLiteDatabase db, String newsCode, String categoryCode, String url) {
        try {
            if ( newsCode.equals(CommConstants.news_tuoiTreNews) ) {
                Document doc = getDocument(url);

                Elements es = doc.select("div.top1_info");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h2 a.top1_title").size() > 0) {
                        newsTitle = es.get(i).select("h2 a.top1_title").text();
                        newsUrl = "http://tuoitre.vn" + es.get(i).select("h2 a.top1_title").attr("href");
                    }
                    if (es.get(i).select("div.top1_sapo").size() > 0) {
                        newsDesc = es.get(i).select("div.top1_sapo").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }
                es = doc.select("div.focus_bottom ul li");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h2 a.fw700").size() > 0) {
                        newsTitle = es.get(i).select("h2 a.fw700").text();
                        newsUrl = "http://tuoitre.vn" + es.get(i).select("h2 a.fw700").attr("href");
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }

                es = doc.select("div#listfocusleft div.block-feature");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h2.title-2 a.title-2").size() > 0) {
                        newsTitle = es.get(i).select("h2.title-2 a.title-2").text();
                        newsUrl = "http://dulich.tuoitre.vn" + es.get(i).select("h2.title-2 a.title-2").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }
                es = doc.select("div#listfocusleft ul.list-news li");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = "http://dulich.tuoitre.vn" + es.get(i).select("h2 a").attr("href");
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }

                es = doc.select("div.list-news-content div.clearfix");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h3 a.ff-bold").size() > 0) {
                        newsTitle = es.get(i).select("h3 a.ff-bold").text();
                        newsUrl = "http://tuoitre.vn" + es.get(i).select("h3 a.ff-bold").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }

                es = doc.select("div.newhot_most_content div.block-left");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = es.get(i).select("h3 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }

                es = doc.select("div.list-block-news article.block-new");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h3 a.title").size() > 0) {
                        newsTitle = es.get(i).select("h3 a.title").text();
                        newsUrl = getUrlDomain(url) + es.get(i).select("h3 a.title").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }

                es = doc.select("div.highlight-1 div#list-content article.art-news");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h4 a").size() > 0) {
                        newsTitle = es.get(i).select("h4 a").text();
                        newsUrl = getUrlDomain(url) + es.get(i).select("h4 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    boolean exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    if (exist) {
                        break;
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_nhanDanNews) ) {
                String domainUrl = "http://www.nhandan.org.vn/";

                boolean isExistNews = false;
                for ( int page = 0; page < 2; page ++ ) {
                    Document doc = getDocument(url + (page > 0 ? "?limitstart=" + (15 * page) : ""));

                    Elements es = doc.select("div.col-xs-12 div.media div.media-body");
                    for (int i = 0; i < es.size(); i++) {
                        DicUtils.dicSqlLog("for i : " + i);
                        String newsTitle = "";
                        String newsUrl = "";
                        String newsDesc = "";

                        if (es.get(i).select("h3 a").size() > 0) {
                            newsTitle = es.get(i).select("h3 a").text();
                            newsUrl = getUrl(domainUrl, es.get(i).select("h3 a").attr("href"));
                        }
                        if (es.get(i).select("h4 a").size() > 0) {
                            newsTitle = es.get(i).select("h4 a").text();
                            newsUrl = getUrl(domainUrl, es.get(i).select("h4 a").attr("href"));
                        }
                        if (es.get(i).select("div.fs_125").size() > 0) {
                            newsDesc = es.get(i).select("div.fs_125").text();
                        }
                        boolean exist = false;
                        if ( !"".equals(newsTitle) ) {
                            exist = DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                        }
                        /*
                        if (exist) {
                            isExistNews = true;
                            break;
                        }
                        */
                    }
                    /*
                    if ( isExistNews ) {
                        break;
                    }
                    */
                }
            } else if ( newsCode.equals(CommConstants.news_laoDongNews) ) {
                Document doc = getDocument(url);

                Elements es = doc.select("article");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("header h4 a").size() > 0) {
                        newsTitle = es.get(i).select("header h4 a").text();
                        newsUrl = es.get(i).select("header h4 a").attr("href");
                    } else {
                        newsTitle = es.get(i).select("header h4").text();
                        newsUrl = es.get(i).select("a").attr("href");
                    }
                    if (es.get(i).select("header p").size() > 0) {
                        newsDesc = es.get(i).select("header p").text();
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_vnexpressNews) ) {
                Document doc = getDocument(url);

                Elements es = doc.select("article");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h1.title_news a").size() > 0) {
                        newsTitle = es.get(i).select("h1 a").text();
                        newsUrl = es.get(i).select("h1 a").attr("href");
                    } else if (es.get(i).select("h3.title_news a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = es.get(i).select("h3 a").attr("href");
                    }
                    if (es.get(i).select("h2.description").size() > 0) {
                        newsDesc = es.get(i).select("h2.description").text();
                    } else if (es.get(i).select("h4.description").size() > 0) {
                        newsDesc = es.get(i).select("h4.description").text();
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }

                es = doc.select("ul#list_sub_featured li");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h6 a").size() > 0) {
                        newsTitle = es.get(i).select("h6 a").text();
                        newsUrl = es.get(i).select("h6 a").attr("href");
                    }
                    if (es.get(i).select("p").size() > 0) {
                        newsDesc = es.get(i).select("p").text();
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }
            } else if ( newsCode.equals(CommConstants.news_vietnamNetNews) ) {
                Document doc = getDocument(url);

                String domainUrl = "http://vietnamnet.vn/";

                Elements es = doc.select("div.TopArticle");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h2 a").size() > 0) {
                        newsTitle = es.get(i).select("h2 a").text();
                        newsUrl = getUrl(domainUrl, es.get(i).select("h2 a").attr("href"));
                    }
                    Elements es2 = es.get(i).select("p");
                    for (int i2 = 0; i2 < es2.size(); i2++) {
                        if ( "".equals(es2.get(i2).attr("class")) ) {
                            newsDesc = es2.get(i2).text();
                            break;
                        }
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }

                es = doc.select("ul.ListHeight li");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = getUrl(domainUrl, es.get(i).select("h3 a").attr("href"));
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }

                es = doc.select("div.HomeBlockLeft ul.ListArticle li");
                for (int i = 0; i < es.size(); i++) {
                    String newsTitle = "";
                    String newsUrl = "";
                    String newsDesc = "";

                    if (es.get(i).select("h3 a").size() > 0) {
                        newsTitle = es.get(i).select("h3 a").text();
                        newsUrl = getUrl(domainUrl, es.get(i).select("h3 a").attr("href"));
                    }
                    Elements es2 = es.get(i).select("p");
                    for (int i2 = 0; i2 < es2.size(); i2++) {
                        if ( !"".equals(es2.get(i2)) && "Lead m-t-5".equals(es2.get(i2).attr("class")) ) {
                            newsDesc = es2.get(i2).text();
                            break;
                        }
                    }

                    if ( !"".equals(newsTitle) ) {
                        DicDb.insNewsCategoryNews(db, newsCode, categoryCode, newsTitle, newsDesc, newsUrl);
                    }
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static String getNewsContents(SQLiteDatabase db, String newsCode, int seq, String url) {
        String contents = DicDb.getNewsContents(db, seq);

        try {
            if ( contents == null  || "".equals(contents) ) {
                if ( newsCode.equals(CommConstants.news_tuoiTreNews) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.block-feature h1.title-2");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("div.block-feature h2.txt-head");
                        if ( es.size() > 0 ) {
                            contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";
                        }
                        es = doc.select("div.fck p");
                        for (int i = 0; i < es.size(); i++) {
                            contents += es.get(i).text() + "\n\n";
                        }
                    }

                    es = doc.select("div.fck h1#object_title");
                    if ( es.size() > 0 ) {
                        contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("div.fck h2.txt-head");
                        if ( es.size() > 0 ) {
                            contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";
                        }
                        es = doc.select("div.fck div.content p");
                        for (int i = 0; i < es.size(); i++) {
                            contents += es.get(i).text() + "\n\n";
                        }
                    }

                    es = doc.select("article.fck header h1#object_title");
                    if ( es.size() > 0 ) {
                        contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("article.fck header p");
                        if ( es.size() > 0 ) {
                            contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";
                        }
                        es = doc.select("article.fck section p");
                        for (int i = 0; i < es.size(); i++) {
                            contents += es.get(i).text() + "\n\n";
                        }
                    }

                    DicDb.updNewsContents(db, seq, contents);
                } else if ( newsCode.equals(CommConstants.news_nhanDanNews) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.col-xs-12 div.media table tr td div.ndtitle");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).text()) + "\n";
                    }

                    es = doc.select("div.col-xs-12 div.media table tr td div.ndcontent p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, contents);
                } else if ( newsCode.equals(CommConstants.news_laoDongNews) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.title h1");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).text()) + "\n";
                    }

                    es = doc.select("div.article-content p");
                    for (int i = 0; i < es.size(); i++) {
                        contents += es.get(i).text() + "\n\n";
                    }

                    DicDb.updNewsContents(db, seq, contents);
                } else if ( newsCode.equals(CommConstants.news_vnexpressNews) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("section.sidebar_1 h1");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("section.sidebar_1 h2");
                        if ( es.size() > 0 ) {
                            contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";
                        }
                        es = doc.select("section.sidebar_1 article p");
                        for (int i = 0; i < es.size(); i++) {
                            contents += es.get(i).text() + "\n\n";
                        }
                    }

                    es = doc.select("section.infographics h1");
                    if ( es.size() > 0 ) {
                        contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("section.infographics h2");
                        if ( es.size() > 0 ) {
                            contents += removeHtmlTagFromContents(es.get(0).text()) + "\n";
                        }
                    }

                    DicDb.updNewsContents(db, seq, contents);

                } else if ( newsCode.equals(CommConstants.news_vietnamNetNews) ) {
                    Document doc = getDocument(url);
                    //DicUtils.dicLog(doc.html());

                    Elements es = doc.select("div.ArticleDetail h1");
                    if ( es.size() > 0 ) {
                        contents = removeHtmlTagFromContents(es.get(0).text()) + "\n";

                        es = doc.select("div.ArticleDetail div#ArticleContent p");
                        for (int i = 0; i < es.size(); i++) {
                            contents += es.get(i).text() + "\n\n";
                        }
                    }

                    DicDb.updNewsContents(db, seq, contents);
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return contents;
    }

    public static String[] getNewsInfo(String c, String n, String u) {
        String[] newsInfo = new String[3];
        newsInfo[0] = c;
        newsInfo[1] = n;
        newsInfo[2] = u;

        return newsInfo;
    }


    public static void setPreferences(Context mContext, String pref, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref, val);
        editor.commit();
    }

    public static String getPreferences(Context mContext, String pref, String defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String val = prefs.getString(pref, defaultVal);

        return val;
    }

    public static boolean equalPreferencesDate(Context mContext, String pref) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String date = prefs.getString(pref, "");
        dicLog(pref + " : " + date);

        if ( date.equals(getCurrentDate()) ) {
            return true;
        } else {
            setPreferences(mContext, pref, getCurrentDate());
            return false;
        }
    }

    public static void initNewsPreferences(Context mContext) {
        String[] news = getNews("C");
        for ( int n = 0; n < news.length; n++) {
            String[] newsCategory = getNewsCategory(news[n], "C");
            for ( int c = 0; c < newsCategory.length; c++ ) {
                setPreferences(mContext, news[n] + "_" + newsCategory[c], "-");
            }
        }
    }

    public static String getQueryParam(String str) {
        return str.replaceAll("\"","`").replaceAll("'","`");
    }

    public static String removeHtmlTagFromContents(String str) {
        String temp = str.replaceAll("<br>", "\n").replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
        String[] tempArr = temp.split("\n");

        String contents = "";
        boolean isStart = false;
        for ( int i = 0; i < tempArr.length; i++ ) {
            if ( isStart == false ) {
                if ( "".equals(tempArr[i].trim()) ) {
                    continue;
                } else {
                    isStart = true;
                    contents += tempArr[i].trim() + "\n";
                }
            } else {
                contents += tempArr[i].trim() + "\n";
            }
        }

        return contents.replaceAll("&nbsp;","");
    }

    public static String getWordString(String str) {
        return str.replaceAll("<br>", "").replaceAll("\"","").replaceAll("[.,:?]","");
    }

    public static String getUrlDomain(String url) {
        return url.substring(0, url.lastIndexOf("/") + 1);
    }

    public static String getUrl(String domain, String url) {
        if ( "/".equals(url.substring(0,1)) ) {
            return domain + url.substring(1, url.length());
        } else {
            return domain + url;
        }
    }
    public static Boolean isNetWork(AppCompatActivity context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        return (isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect);
    }
}
