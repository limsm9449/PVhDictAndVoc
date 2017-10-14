package com.sleepingbear.pvhdictandvoc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = getIntent().getExtras();
        StringBuffer allSb = new StringBuffer();
        StringBuffer CurrentSb = new StringBuffer();
        StringBuffer tempSb = new StringBuffer();

        String screen = b.getString("SCREEN");
        if ( screen == null ) {
            screen = "";
        }
        String kind = b.getString("KIND");
        if ( kind == null ) {
            kind = "";
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 베트남 사전" + CommConstants.sqlCR);
        tempSb.append("- 성조가 있는 단어 검색, 성조가 없는 단어 검색, 예문을 검색합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어 상세, 예문을 클릭하시면 예문 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 웹사전 검색, 단어장에 추가할 수 있고, 예문을 길게 클릭하시면 회화노트에 등록 및 TTS를 들을 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .없는 단어일 경우 하단에 메세지가 나오고, 오른쪽 버튼을 클릭하시면 웹사전으로 검색하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_dictionary) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 사전 검색 History" + CommConstants.sqlCR);
        tempSb.append("- 베한 사전에서 검색한 단어를 조회합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 베한 사전으로 이동합니다." + CommConstants.sqlCR);
        tempSb.append(" .상단의 편집버튼(연필모양)을 클릭해서 검색 단어를 삭제하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_dictionaryHistory) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* Web 사전" + CommConstants.sqlCR);
        tempSb.append("- Naver, Daum 웹사전으로 검색을 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_webDictionary) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* Web 번역" + CommConstants.sqlCR);
        tempSb.append("- Google 을 사용하여 번역을 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_webTranslate) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 베트남 뉴스" + CommConstants.sqlCR);
        tempSb.append("- 5개의 베트남 뉴스가 있습니다. " + CommConstants.sqlCR);
        tempSb.append(" .외국사이트라서 로딩이 많이 느립니다. 참고하세요." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_news) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 뉴스 상세" + CommConstants.sqlCR);
        tempSb.append("- 베트남 뉴스를 보면서 필요한 단어 검색 기능이 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보다가 모르는 단어를 클릭을 하면 하단에 클릭한 단어의 뜻이 보입니다. " + CommConstants.sqlCR);
        tempSb.append(" .클릭단어의 뜻이 없을경우 하단 오른쪽의 검색 버튼을 클릭하면 Naver,Daum에서 단어 검색을 할 수 있습니다. " + CommConstants.sqlCR);
        tempSb.append(" .하단 단어를 길게 클릭하시면 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 단어 옆의 (+)를 클릭하시면 바로 단어장에 등록을 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스의 단어를 길게 클릭하시면 단어보기, 단어검색(Naver,Daum), 번역, 문장보기, TTS, 전체TTS(4000자까지), 복사, 전체복사 기능을 사용하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보면서 클릭한 단어는 '뉴스 클릭 단어' 화면에서 확인하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_newsView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 베트남 뉴스 Ver.2" + CommConstants.sqlCR);
        tempSb.append("- 베트남 사이트를 웹으로 볼 경우 다양한 자료를 받아서 보여주기 때문에 속도가 느린 문제가 있습니다." + CommConstants.sqlCR);
        tempSb.append("   그래서 속도 개선을 위해 텍스트만 받아서 카테고리별로 뉴스 제목을 보고 뉴스 기사를 볼 수 있도록 만들었습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 버튼을 클릭해서 뉴스 종류를 선택한 후에 상단 콤보에서 카테고리를 선택하면 뉴스 제목을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_news2) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 베트남 뉴스 상세" + CommConstants.sqlCR);
        tempSb.append("- 베트남 뉴스를 보면서 필요한 단어 검색 기능이 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보다가 모르는 단어를 클릭을 하면 하단에 클릭한 단어의 뜻이 보입니다. " + CommConstants.sqlCR);
        tempSb.append(" .클릭단어의 뜻이 없을경우 하단 오른쪽의 검색 버튼을 클릭하면 Naver,Daum에서 단어 검색을 할 수 있습니다. " + CommConstants.sqlCR);
        tempSb.append(" .하단 단어를 길게 클릭하시면 단어 상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 단어 옆의 (+)를 클릭하시면 바로 단어장에 등록을 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스의 단어를 길게 클릭하시면 단어보기, 단어검색(Naver,Daum), 번역, 문장보기, TTS, 전체TTS(4000자까지), 복사, 전체복사 기능을 사용하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .뉴스를 보면서 클릭한 단어는 '뉴스 클릭 단어' 화면에서 확인하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_news2View) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 뉴스 클릭 단어" + CommConstants.sqlCR);
        tempSb.append("- 베트남 뉴스를 보면서 클릭한 단어들에 대하여 관리하는 화면입니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 수정 버튼(연필모양)를 클릭하시면 단어를 선택, 삭제, 단어장에 저장, 신규 단어장에 저장할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어상세를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_newsClickWord) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 학습" + CommConstants.sqlCR);
        tempSb.append("- Easy, Normal, hard 별로 회화 학습을 할 수 있습니다. " + CommConstants.sqlCR);
        tempSb.append(" .해석을 보고 단어를 클릭해서 올바른 문장을 만드세요." + CommConstants.sqlCR);
        tempSb.append(" .오른쪽 상단 버튼의 눈 모양 버튼을 클릭하면 베트남 문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .학습한 회화는 '회화 노트' 화면의 '학습 회화'에서 일자별로 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_conversationStudy) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 검색" + CommConstants.sqlCR);
        tempSb.append("- 검색어로 회화를 검색합니다." + CommConstants.sqlCR);
        tempSb.append(" .'A B'로 검색을 하면 A와 B가 들어간 회화를 검색합니다." + CommConstants.sqlCR);
        tempSb.append(" .'A B,C D'로 검색을 하면 A와 B가 들어간 회화와 C와 D가 들어간 회화를 검색합니다." + CommConstants.sqlCR);
        tempSb.append(" .회화를 클릭하면 베트남 회화를 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .회화를 길게클릭하면 회화 학습, 문장 상세, 회화 노트에 추가, TTS 기능을 사용 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .오른쪽 상단 버튼의 눈 모양 버튼을 클릭하면 베트남 문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_conversation) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 패턴" + CommConstants.sqlCR);
        tempSb.append("- 회화 패턴별로 회화를 조회 및 회화 학습을 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .패턴을 클릭하면 패턴이 들어간 회화를 조회 합니다. " + CommConstants.sqlCR);
        tempSb.append(" .패턴을 길게 클릭하면 패턴이 들어간 회화를 학습 할 수 있습니다. " + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_pattern) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 패턴 예제" + CommConstants.sqlCR);
        tempSb.append("- 회화 패턴이 들어간 회화를 조회합니다.(예제에서 비슷한 패턴을 찾기 때문에 100% 정확하지는 않습니다.)" + CommConstants.sqlCR);
        tempSb.append(" .회화를 클릭하면 베트남 베트남 문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .회화를 길게 클릭하면 회화 학습, 문장 상세, 회화 노트에 추가, TTS 기능을 사용 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .오른쪽 상단 버튼의 눈 모양 버튼을 클릭하면 베트남 문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_patternView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 노트" + CommConstants.sqlCR);
        tempSb.append("- MY 회화, 학습 회화로 구성되어 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .'MY 회화'는 단어장 처럼 내 회화를 관리합니다." + CommConstants.sqlCR);
        tempSb.append(" .'학습 회화'는 매일 학습한 회화 내용입니다." + CommConstants.sqlCR);
        tempSb.append(" .노트를 길게 클릭하면 회화 학습, 회화 관리 기능을 사용 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .'회화 관리' 기능을 선택하면 노트 수정, 노트 삭제, 노트 내보내기, 노트 가져오기 기능을 사용 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_conversationNote) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 회화 노트 상세" + CommConstants.sqlCR);
        tempSb.append("- 회화 노트의 회화를 조회합니다." + CommConstants.sqlCR);
        tempSb.append(" .회화를 클릭하면 베트남 문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .회화를 길게 클릭하면 회화 학습, 문장 상세, 회화 노트에 추가, TTS 기능을 사용 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .오른쪽 상단 버튼의 눈 모양 버튼을 클릭하면 영어문장을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_conversationNoteView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문법" + CommConstants.sqlCR);
        tempSb.append("- 간단한 문법을 볼수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_grammar) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문법 상세" + CommConstants.sqlCR);
        tempSb.append("- 문법 설명 및 문법이 들어간 회화를 조회합니다.(예제에서 비슷한 문법을 찾기 때문에 100% 정확하지는 않습니다.)" + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_grammarView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* VSL" + CommConstants.sqlCR);
        tempSb.append("- 베트남 교재인 VSL을 학습할 수 있도록 만들었습니다." + CommConstants.sqlCR);
        tempSb.append(" .회화의 경우는 음성을 다운로드해서 들어보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_vsl) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카테고리별 단어" + CommConstants.sqlCR);
        tempSb.append("- 초급 단어, 중급 단어, 필수 단어, 카테고리별 단어, 상활별 회화를 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_category) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카테고리 상세" + CommConstants.sqlCR);
        tempSb.append("- 단어나 회화를 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_categoryView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 오늘의 단어" + CommConstants.sqlCR);
        tempSb.append("- 매일 랜덤하게 뽑은 10개의 단어를 학습할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_today) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장" + CommConstants.sqlCR);
        tempSb.append("- 단어장 목록을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단의 + 버튼을 클릭해서 신규 단어장을 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .기존 단어장을 길게 클릭하시면 수정, 추가, 삭제,  내보내기, 가져오기를 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어장을 클릭하시면 등록된 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_vocabularyNote) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 상세" + CommConstants.sqlCR);
        tempSb.append("- 단어 목록 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 수정 버튼능 클릭하면 단어장을 편집(삭제,복사,이동) 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 TTS 버튼을 클릭하면 단어,뜻을 들을 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_vocabularyNoteView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단답 학습" + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 뜻을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study1) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 정답 보기/ 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study2) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 학습입니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 Play 버튼을 클릭하시면 영어를 보여주고 잠시후에 뜻이 보여집니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study3) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 OX 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 OX 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study4) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 4지선다 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study5) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 TTS 학습" + CommConstants.sqlCR);
        tempSb.append("- TTS를 이용하여 학습을 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_study6) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어 상세" + CommConstants.sqlCR);
        tempSb.append("- 상단 콤보 메뉴를 선택하시면 네이버 사전, 다음 사전, 예제를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_wordView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문장 상세" + CommConstants.sqlCR);
        tempSb.append("- 문장의 발음 및 관련 단어를 조회하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어 보기 및 등록할 단어장을 선택 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 등록할 단어장을 선택할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭하시면 Default 단어장에 추가 됩니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        if ( screen.equals(CommConstants.screen_sentenceView) ) {
            CurrentSb.append(tempSb.toString());
        } else {
            allSb.append(tempSb.toString());
        }

        if ( "ALL".equals(b.getString("SCREEN")) ) {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(allSb.toString());
        } else {
            ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(CurrentSb.toString() + CommConstants.sqlCR + CommConstants.sqlCR + allSb.toString());
        }

        int fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        ((TextView) this.findViewById(R.id.my_c_help_tv1)).setTextSize(fontSize);
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
