package com.sleepingbear.pvhdictandvoc;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ViewPager mPager;
    private MainPagerAdapter adapter;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private TabLayout tabLayout;

    private int selectedTab = 0;

    private FloatingActionButton fab;

    private Activity mActivity;
    public boolean mIsCategory = true;

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        System.out.println("=============================================== App Start ======================================================================");

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        mActivity = this;

        //DB가 새로 생성이 되었으면 이전 데이타를 DB에 넣고 Flag를 N 처리함
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if ( "Y".equals(prefs.getString("db_new", "N")) ) {
            DicUtils.dicLog("backup data import");

            DicUtils.readInfoFromFile(this, db);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("db_new", "N");
            editor.commit();
        };

        //카테고리 추가 기능 구현
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View dialog_layout = inflater.inflate(R.layout.dialog_category_add, (ViewGroup) findViewById(R.id.my_d_category_root));

                //dialog 생성..
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setView(dialog_layout);
                final AlertDialog alertDialog = builder.create();

                ((TextView) dialog_layout.findViewById(R.id.my_d_category_add_tv_title)).setText("단어장 추가");
                ((TextView) dialog_layout.findViewById(R.id.my_d_category_add_tv_title)).setOnLongClickListener(hiddenFunc);
                final EditText et_ins = ((EditText) dialog_layout.findViewById(R.id.my_d_category_add_et_ins));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_ins)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("".equals(et_ins.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();

                            String insCategoryCode = DicQuery.getInsCategoryCode(db);
                            db.execSQL(DicQuery.getInsNewCategory("MY", insCategoryCode, et_ins.getText().toString()));

                            //기록
                            DicUtils.writeInfoToFile(getApplicationContext(), "CATEGORY_INSERT" + ":" + insCategoryCode + ":" + et_ins.getText().toString());

                            ((VocabularyFragment) adapter.getItem(selectedTab)).changeListView();

                            Toast.makeText(getApplicationContext(), "단어장을 추가하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_close)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle("베한사전 & 단어장");
        //ab.setIcon(R.mipmap.ic_launcher);

        // ViewPaper 를 정의한다.
        mPager = (ViewPager) findViewById(R.id.main_pager);
        adapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(adapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                DicUtils.dicLog(this.getClass().toString() + " onPageSelected" + " : " + position);
                selectedTab = position;

                //mPager.setCurrentItem(position);
                setChangeViewPaper(position, CommConstants.changeKind_title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //mPager.setCurrentItem(0);
        //setChangeViewPaper(selectedTab, CommConstants.changeKind_title);

        // 상단의 Tab 을 정의한다.
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //DicUtils.dicLog(this.getClass().toString() + " onTabSelected" + " : " + tab.getPosition());
                selectedTab = tab.getPosition();
                //tab 변경
                mPager.setCurrentItem(selectedTab);

                //setChangeViewPaper(selectedTab, CommConstants.changeKind_title);

                //메뉴 구성
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //DicUtils.dicLog(this.getClass().toString() + " onTabReselected" + " : " + tab.getPosition());
            }
        });

        checkPermission();
    }

    public boolean checkPermission() {
        Log.d(CommConstants.tag, "checkPermission");
        boolean isCheck = false;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            Log.d(CommConstants.tag, "권한 없음");
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                Toast.makeText(this, "(중요)파일로 내보내기, 가져오기를 하기 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            Log.d(CommConstants.tag, "2222");
        } else {
            Log.d(CommConstants.tag, "권한 있음");
            isCheck = true;
        }

        return isCheck;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(CommConstants.tag, "권한 허가");
                } else {
                    Log.d(CommConstants.tag, "권한 거부");
                    Toast.makeText(this, "파일 권한이 없기 때문에 파일 내보내기, 가져오기를 할 수 없습니다.\n만일 권한 팝업이 안열리면 '다시 묻지 않기'를 선택하셨기 때문입니다.\n어플을 지우고 다시 설치하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    protected View.OnLongClickListener hiddenFunc = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout newSentenceLayout = (LinearLayout) li.inflate(R.layout.dialog_hidden_function, null);

            final EditText pwd = (EditText) newSentenceLayout.findViewById(R.id.my_d_hd_et1);

            new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("코드를 입력해주세요.")
                    .setView(newSentenceLayout)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ( "F1".equals(pwd.getText().toString()) ) {
                                Intent intent = new Intent(getApplication(), LogActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("CODE", pwd.getText().toString());
                                intent.putExtras(bundle);

                                startActivity(intent);
                            }
                        }
                    })
                    .setNegativeButton("취소",null)
                    .show();
            return false;
        }
    };

    //뷰의 내용이 변경되었을때...
    public void setChangeViewPaper(int position, int changeKind) {
        //DicUtils.dicLog(this.getClass().toString() + " setChangeViewPaper" + " : " + position);
        try {
            fab.setVisibility(View.VISIBLE);

            if ( adapter.getItem(position) == null ) {
                return;
            }

            if (position == 0) {
                //단어장
                if ( ((VocabularyFragment) adapter.getItem(position)) != null ) {
                    ((VocabularyFragment) adapter.getItem(position)).setActionBarTitle();
                    ((VocabularyFragment) adapter.getItem(position)).changeListView();
                }
            } else if (position == 1) {
                //사전
                fab.setVisibility(View.INVISIBLE);
                if ( ((DictionaryFragment) adapter.getItem(position)) != null ) {
                    ((DictionaryFragment) adapter.getItem(position)).setActionBarTitle();
                }
            } else if (position == 2) {
                //오늘의 단어
                fab.setVisibility(View.INVISIBLE);
                if ( ((TodayFragment) adapter.getItem(position)) != null ) {
                    ((TodayFragment) adapter.getItem(position)).setActionBarTitle();
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            //DicUtils.dicLog(e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    private View.OnClickListener mPagerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String text = ((Button) v).getText().toString();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ((MenuItem)menu.findItem(R.id.action_delete)).setVisible(false);

        if ( selectedTab == 0 || selectedTab == 2 ) {
            ((MenuItem)menu.findItem(R.id.action_delete)).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            if (selectedTab == 0) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("단어장을 초기화 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                confirmAllDelete();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            } else if (selectedTab == 2) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("오늘의 단어를 초기화 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                confirmAllDelete();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            if ( selectedTab == 0 ) {
                bundle.putString("SCREEN", "VOCABULARY");
            } else if ( selectedTab == 1 ) {
                bundle.putString("SCREEN", "DICTIONARY");
            } else if ( selectedTab == 2 ) {
                bundle.putString("SCREEN", "TODAY");
            }

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.action_other) {
            Intent intent = new Intent(getApplication(), OtherActivity.class);
            Bundle bundle = new Bundle();
            intent.putExtras(bundle);

            startActivityForResult(intent, CommConstants.a_other);
        } else if (id == R.id.action_patch) {
            Intent intent = new Intent(getApplication(), PatchActivity.class);
            Bundle bundle = new Bundle();
            intent.putExtras(bundle);

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);
        switch ( requestCode ) {
            case CommConstants.a_other :
                if ( resultCode == Activity.RESULT_OK && "Y".equals(data.getStringExtra("isChange")) ) {
                    if ( selectedTab == 0 ) {
                        ((VocabularyFragment) adapter.getItem(0)).changeListView();
                    } else if ( selectedTab == 2 ) {
                        ((TodayFragment) adapter.getItem(2)).changeListView();
                    }
                }
                break;
            case CommConstants.a_vocabulary :
                ((VocabularyFragment) adapter.getItem(0)).changeListView();
                break;
            case CommConstants.a_dicCategory :
                ((VocabularyFragment) adapter.getItem(0)).changeListView();
                break;
        }
    }

    public void confirmAllDelete() {
          new AlertDialog.Builder(this)
                  .setTitle("알림")
                  .setMessage("초기화 후에는 데이타를 복구할 수 없습니다.")
                  .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          if (selectedTab == 0) {
                              DicDb.initVocabulary(db);

                              DicUtils.writeNewInfoToFile(getApplicationContext(), db);

                              ((VocabularyFragment) adapter.getItem(selectedTab)).changeListView();
                          } else if (selectedTab == 2) {
                              DicDb.initToday(db);

                              DicUtils.writeNewInfoToFile(getApplicationContext(), db);

                              ((TodayFragment) adapter.getItem(selectedTab)).changeListView();
                          }
                      }
                  })
                  .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                      }
                  })
                  .show();
      }
}

class MainPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm, AppCompatActivity activity) {
        super(fm);

        mFragmentList.add(new VocabularyFragment());
        mFragmentTitleList.add("단어장");

        mFragmentList.add(new DictionaryFragment());
        mFragmentTitleList.add("사전");

        mFragmentList.add(new TodayFragment());
        mFragmentTitleList.add("오늘의 단어");
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        //DicUtils.dicLog(this.getClass().toString() + " getItem" + " : " + position);
        return mFragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}