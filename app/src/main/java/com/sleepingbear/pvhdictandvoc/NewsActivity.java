package com.sleepingbear.pvhdictandvoc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private ListView listView;
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("베트남 뉴스");
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        listView = (ListView) findViewById(R.id.my_f_news_lv);

        ArrayList<NewsVo> items = new ArrayList<>();
        int idx = 1;
        items.add(new NewsVo("E" + idx++, "Tuoi Tre Newspaper"));
        items.add(new NewsVo("E" + idx++, "Nhan Dan Newspaper"));
        items.add(new NewsVo("E" + idx++, "Lao Dong Newspaper"));
        items.add(new NewsVo("E" + idx++, "VN Express"));
        items.add(new NewsVo("E" + idx++, "Vietnam Net"));

        adapter = new NewsAdapter(getApplicationContext(), 0, items);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);

        DicUtils.setAdView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_news);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            NewsVo cur = adapter.getItem(position);

            DicUtils.dicLog(cur.getName());

            Intent intent = new Intent(getApplication(), NewsWebViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("kind", cur.getKind());
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    private class NewsVo {
        private String kind;
        private String name;

        public NewsVo(String kind, String name) {
            this.kind = kind;
            this.name = name;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private class NewsAdapter extends ArrayAdapter<NewsVo> {
        int fontSize = 0;
        private ArrayList<NewsVo> items;

        public NewsAdapter(Context context, int textViewResourceId, ArrayList<NewsVo> objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;

            fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.content_news_item, null);
            }

            ((TextView)v.findViewById(R.id.my_tv_newname)).setText(items.get(position).getName());

            //사이즈 설정
            ((TextView) v.findViewById(R.id.my_tv_newname)).setTextSize(fontSize);

            return v;
        }
    }

}
