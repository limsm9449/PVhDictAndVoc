package com.sleepingbear.pvhdictandvoc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class PatchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("패치 내용");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        StringBuffer patch = new StringBuffer();

        patch.append("- 어플 교육을 배우지 않고, 인터넷을 찾아가면 어플을 만들다 보니 개발도 시간이 많이 걸리고 많은 시행착오를 거치는것 같습니다." + CommConstants.sqlCR);
        patch.append("- 사용해보시고 문제점이 있으면 알려주시기 바랍니다." + CommConstants.sqlCR);
        patch.append("- 제 어플이 베트남어 공부에 도움이 되었으면 합니다." + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);

        patch.append("* 2016.10.22" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 사전 검색시 진행바를 보여주도록 수정" + CommConstants.sqlCR);
        patch.append("- 상단에 그림자 나오는 문제점 수정" + CommConstants.sqlCR);
        patch.append("- 학습 관련 오류사항 수정" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);

        patch.append("* 2016.10.14" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 없는 단어 및 예문을 검색시 오류로 어플이 죽는 오류 수정하였습니다." + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);

        patch.append("* 2016.10.10" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 단어장 UI를 변경하였습니다." + CommConstants.sqlCR);
        patch.append("- 단어장에서 클릭시 바로 단어보기로 이동하고, 길게 클릭시 단어 삭제 확인을 물어보도록 수정하였습니다." + CommConstants.sqlCR);
        patch.append("- 문장예문에서 단어를 클릭시 바로 단어보기 화면으로 이동하고, 길게 클릭시 단어장을 선택하도록 수정했습니다." + CommConstants.sqlCR);
        patch.append("- 안드로이드에서 권한 문제로 폴더를 찾지 못하는 문제점을 수정하였습니다." + CommConstants.sqlCR);
        patch.append("   .등록할 단어를 텍스트 파일로 만들어서 핸드폰 vhdictandvoc 폴더에 넣고, 단어장에서 가져오기 하시면 단어장에 등록을 하실 수 있습니다." + CommConstants.sqlCR);
        patch.append("   .단어장을 내보내기 하셔서 텍스트 파일로 단어 암기를 하실 수 있습니다." + CommConstants.sqlCR);

        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("* 2016.8.20" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 예문을 저장 및 삭제하는 기능을 추가 하였습니다." + CommConstants.sqlCR);
        patch.append("예문상세 화면에서 예문에 있는 별표를 클릭하여 저장을 하실 수 있습니다. " + CommConstants.sqlCR);
        patch.append("나의 예문 메뉴를 클릭해서 예문을 조회 하실 수 있습니다." + CommConstants.sqlCR);
        patch.append("- 카테고리에 Naver 회화를 추가했습니다." + CommConstants.sqlCR);
        patch.append("- 안드로이드 6 에서 폴더를 찾지 못하는 문제점을 수정하였습니다." + CommConstants.sqlCR);

        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("* 2016.7.24" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- 제가 공부하고자 만든 App 이지만 처음 개발을 하기 때문에 오류가 많습니다." + CommConstants.sqlCR);
        patch.append("사전 DB를 사용하기 때문에 DB 변경시 데이타가 사라지는 문제도 좀 있기 때문에 단어장은 백업을 받아 주시기 바랍니다. " + CommConstants.sqlCR);
        patch.append("애기 때문에 개발할 시간이 부족하지만 열심히 공부해서 좋은 어플을 만들도록 하겠습니다." + CommConstants.sqlCR);

        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("* 2016.7.23" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("- Tab을 오늘의 단어, 사전, 단어장으로 줄이고 학습, 기타를 메뉴로 옮겼습니다." + CommConstants.sqlCR);
        patch.append("- 속도 문제로 사전 검색시 검색할 단어를 다 입력한 후에 검색이 되도록 수정 했습니다." + CommConstants.sqlCR);
        patch.append("- 사전 검색에 문장을 넣을 경우 관련 단어들을 검색하도록 수정 했습니다." + CommConstants.sqlCR);

        ((TextView) this.findViewById(R.id.my_c_patch_tv1)).setText(patch.toString());
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
