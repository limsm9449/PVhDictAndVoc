package com.sleepingbear.pvhdictandvoc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class OtherActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("기타");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        ((Button) findViewById(R.id.my_f_other_b_a3)).setText("베트남 사이트");
        ((Button) findViewById(R.id.my_f_other_b_a3)).setOnClickListener(this);
        ((Button) findViewById(R.id.my_f_other_b_a4)).setText("번역도움(단어보기)");
        ((Button) findViewById(R.id.my_f_other_b_a4)).setOnClickListener(this);
        ((Button) findViewById(R.id.my_f_other_b_a5)).setText("E-Mail");
        ((Button) findViewById(R.id.my_f_other_b_a5)).setOnClickListener(this);
        ((Button) findViewById(R.id.my_f_other_b_a6)).setText("어플 백업 및 복구");
        ((Button) findViewById(R.id.my_f_other_b_a6)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_f_other_b_a3 ) {
            // 웹 사이트
            Intent intent = new Intent(this.getApplication(), WebViewActivity.class);
            this.startActivity(intent);
        } else if ( v.getId() == R.id.my_f_other_b_a4 ) {
            Bundle bundle = new Bundle();
            bundle.putString("viet", "");
            bundle.putString("han", "");

            Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if ( v.getId() == R.id.my_f_other_b_a5 ) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "베한 사전 어플");
            intent.putExtra(Intent.EXTRA_TEXT, "문제점을 적어 주세요.\n빠른 시간 안에 수정을 하겠습니다.\n감사합니다.");
            intent.setData(Uri.parse("mailto:limsm9449@gmail.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if ( v.getId() == R.id.my_f_other_b_a6 ) {
            //layout 구성
            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialog_layout = li.inflate(R.layout.dialog_dic_manage, null);

            //dialog 생성..
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialog_layout);
            final AlertDialog alertDialog = builder.create();

            final EditText et_saveName = ((EditText) dialog_layout.findViewById(R.id.my_d_dm_et_save));
            et_saveName.setText("backup_" + DicUtils.getCurrentDate() + ".txt");
            ((Button) dialog_layout.findViewById(R.id.my_d_dm_b_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String saveFileName = et_saveName.getText().toString();
                    if ("".equals(saveFileName)) {
                        Toast.makeText(getApplicationContext(), "저장할 파일명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else if (saveFileName.indexOf(".") > -1 && !"txt".equals(saveFileName.substring(saveFileName.length() - 3, saveFileName.length()).toLowerCase())) {
                        Toast.makeText(getApplicationContext(), "확장자는 txt 입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        //디렉토리 생성
                        String fileName = "";
                        boolean existDir = false;
                        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName);
                        if (!appDir.exists()) {
                            existDir = appDir.mkdirs();
                            //android 6 부터는 storage/emulated/0을 찾지 못해서 내부메모리에 저장한다.
                            if ( existDir ) {
                                if (saveFileName.indexOf(".") > -1) {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                                } else {
                                    fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                                }
                            } else {
                                if (saveFileName.indexOf(".") > -1) {
                                    fileName = getExternalFilesDir(null) +  File.separator + saveFileName;
                                } else {
                                    fileName = getExternalFilesDir(null) +  File.separator + saveFileName + ".txt";
                                }
                            }
                        } else {
                            if (saveFileName.indexOf(".") > -1) {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                            } else {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                            }
                        }

                        File saveFile = new File(fileName);
                        if (saveFile.exists()) {
                            Toast.makeText(getApplicationContext(), "파일명이 존재합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            DicUtils.writeNewInfoToFile(getApplicationContext(), (new DbHelper(getApplicationContext())).getWritableDatabase(), fileName);

                            Toast.makeText(getApplicationContext(), "백업 데이타를 정상적으로 내보냈습니다.", Toast.LENGTH_SHORT).show();

                            alertDialog.dismiss();
                        }
                    }
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_d_dm_b_upload)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileChooser filechooser = new FileChooser(OtherActivity.this);
                    filechooser.setFileListener(new FileChooser.FileSelectedListener() {
                        @Override
                        public void fileSelected(final File file) {
                            DicUtils.readInfoFromFile(getApplicationContext(), (new DbHelper(getApplicationContext())).getWritableDatabase(), file.getAbsolutePath());

                            Toast.makeText(getApplicationContext(), "백업 데이타를 정상적으로 가져왔습니다.", Toast.LENGTH_SHORT).show();

                            alertDialog.dismiss();
                        }
                    });
                    filechooser.setExtension("txt");
                    filechooser.showDialog();
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_d_dm_b_close)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
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
