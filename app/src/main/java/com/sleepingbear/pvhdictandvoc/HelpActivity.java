package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = getIntent().getExtras();
        StringBuffer allSb = new StringBuffer();
        StringBuffer CurrentSb = new StringBuffer();
        StringBuffer tempSb = new StringBuffer();

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 오늘의 단어" + CommConstants.sqlCR);
        tempSb.append("- 오늘의 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 삭제 버튼을 클릭하면 오늘의 단어를 전체 삭제하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "TODAY".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 사전" + CommConstants.sqlCR);
        tempSb.append("- 베한 사전, 한베 사전을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. Default는 베한 사전입니다. 한베를 선택하시면 한베 사전을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. Default는 단어입니다. 예문을 선택하시면 예문을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("3. 단어를 클릭하시면 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("4. 예문을 클릭하시면 문장 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "DICTIONARY".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어 상세" + CommConstants.sqlCR);
        tempSb.append("- 단어의 뜻, 발음, 상세 뜻, 예제, 기타 예제별로 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 별표를 클릭하시면 Default 단어장에 추가 됩니다." + CommConstants.sqlCR);
        tempSb.append("2. 별표를 길게 클릭하시면 추가할 단어장을 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "WORDVIEW".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문장 상세" + CommConstants.sqlCR);
        tempSb.append("- 문장의 발음 및 관련 단어를 조회하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 연필을 클릭해서 문장을 입력하시면 관련 단어를 조회하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. 단어를 클릭하시면 단어 보기 및 등록할 단어장을 선택 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("3. 별표를 클릭하시면 Default 단어장에 추가 됩니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "SENTENCEVIEW".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장" + CommConstants.sqlCR);
        tempSb.append("- 내가 등록한 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 하단의 + 버튼을 클릭해서 신규 단어장을 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. 기존 단어장을 길게 클릭하시면 수정 및 추가, 삭제 및 내보내기, 가져오기를 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("3. 단어장을 클릭하시면 등록 된 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "VOCABULARY".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 - 나의 예문" + CommConstants.sqlCR);
        tempSb.append("- 내가 체크한 예문을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "MY_SAMPLE".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 - 카테고리" + CommConstants.sqlCR);
        tempSb.append("- 카테고리 별로 단어 및 문장을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 단어를 클릭하시면 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. 문장을 클릭하시면 문장 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "DICCATEGORY".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 - 단어 학습" + CommConstants.sqlCR);
        tempSb.append("- 등록한 단어를 5가지 방법으로 공부할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 단어장 선택, 학습 종류 선택, 시간 선택을 하신후 학습시작을 클릭하세요." + CommConstants.sqlCR);
        tempSb.append("2. Default는 현재부터 60일전에 등록한 단어입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단답 학습" + CommConstants.sqlCR);
        tempSb.append("1. 단어를 클릭하시면 뜻을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. 별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append("3. 단어를 길게 클릭하시면 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY1".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append("1. 별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append("2. 단어를 길게 클릭하시면 정답 보기/ 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY2".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY3".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 OX 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 OX 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY4".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 4지선다 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "STUDY5".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 기타" + CommConstants.sqlCR);
        tempSb.append("- 베트남 사이트, 번역도움(단어보기), E-Mail, 어플 백업 및 복구 기능을 실행 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("1. 베트남 사이트 : 베트남 사이트를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("2. 번역도움(단어보기) : 번역할 문장을 입력해서 관련 단어를 조회할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("3. E-Mail : 개발자에게 App 관련 메일을 보낼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("4. 어플 백업 및 복구 : 어플 데이타를 백업하거나 복구하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "OTHER".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 베트남 사이트 " + CommConstants.sqlCR);
        tempSb.append("1. 해석할 문장을 선택하여 클립보드에 복사를 하세요." + CommConstants.sqlCR);
        tempSb.append("2. 오른쪽 하단에 있는 i(Action 버튼)을 클릭하세요." + CommConstants.sqlCR);
        tempSb.append("3. 선택한 문장을 기준으로 관련 단어들을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("4. 문장을 선택 안하고 i(Action 버튼)을 클릭할 경우 클립보드에 들어있는 문장을 기준으로 단어가 조회 됩니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( "WEB_VIEW".equals(b.getString("SCREEN")) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        if ( "ALL".equals(b.getString("SCREEN")) ) {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(allSb.toString());
        } else {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(CurrentSb.toString() + CommConstants.sqlCR + CommConstants.sqlCR + allSb.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
