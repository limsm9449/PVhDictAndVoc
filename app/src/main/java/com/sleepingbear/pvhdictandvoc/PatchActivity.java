package com.sleepingbear.pvhdictandvoc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

        ActionBar ab = getSupportActionBar();
        ab.setTitle("패치 내용");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        StringBuffer patch = new StringBuffer();

        patch.append("* 신규 패치" + CommConstants.sqlCR);
        patch.append("- 안드로이드 7에서 다이얼로그 버튼이 안보이는 문제점 수정" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);
        patch.append("" + CommConstants.sqlCR);

        patch.append("- 단어장에서 신규 추가시 오류 수정" + CommConstants.sqlCR);
        patch.append("- 사전 검색시 문장의 단어가 몇개 빠지는 문제점 수정" + CommConstants.sqlCR);
        patch.append("- 사전 검색 데이타에서 상세 화면으로 전환시 발생하는 오류 수정" + CommConstants.sqlCR);
        patch.append("- 베한사전, 한베 사전을 통합했습니다." + CommConstants.sqlCR);
        patch.append("- 성조로 검색, 성조없이 검색, 예문 검색이 가능하도록 변경했습니다." + CommConstants.sqlCR);
        patch.append("* 2017.06.05 : 화면 UI 및 기능 개선" + CommConstants.sqlCR);
        patch.append("- 베한사전, 베트남 뉴스, 베트남 회화로 크게 기능을 개선 했습니다." + CommConstants.sqlCR);

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
