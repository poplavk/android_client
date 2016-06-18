package com.example.mkai.pry.result;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.mkai.pry.R;
import com.example.mkai.pry.aleksey2093.GetFriendsLastResult;
import com.example.mkai.pry.suh.PersonInfo;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    final String LOG_TAG = "myLogs";
    final ArrayList<PersonInfo> list = new ArrayList<PersonInfo>();
    List<PersonDescriptor> results = new ArrayList<PersonDescriptor>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView resultsListView = (ListView) findViewById(R.id.resultsListView);
        // обрабатывает нажатие на пункт списка
        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d(LOG_TAG, " itemClick: position************* = " + position + ", id = "
                        + id);

                Uri address = Uri.parse("https://vk.com/" + list.get(position).link);
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);
            }
        });
/*
        //обрабатывает выделение пунктов списка (
        subsListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.d(LOG_TAG, "itemSelect: position = " + position + ", id = "
                        + id);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(LOG_TAG, "itemSelect: nothing");
            }
        });
        */
        /*results = getResults();
        ResultAdapter adapter = new ResultAdapter(results, this, getWidthDisplay());
        resultsListView.setAdapter(adapter);*/
        updateListResult(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateListResult(final Context context)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                results = getResults();
                final ListView resultsListView = (ListView) findViewById(R.id.resultsListView);
                final ResultAdapter adapter = new ResultAdapter(results, context, getWidthDisplay());
                assert resultsListView != null;
                resultsListView.post(new Runnable() {
                    @Override
                    public void run() {
                        resultsListView.setAdapter(adapter);
                    }
                });
            }
        });
        thread.setName("Thread update open Result");
        thread.start();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                final Context context = v.getContext();
                updateListResult(context);
            }
            //Открывает сслыку на страницу вк в браузере
//            case R.id.result: {
//                Uri address = Uri.parse("http://developer.alexanderklimov.ru");
//                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
//                startActivity(openlinkIntent);
//            }
            break;
        }
    }

    /**
     * Возвращает ширину фотографии
     * @return ширина
     */
    private int getWidthDisplay()
    {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metricsB = new DisplayMetrics();
        display.getMetrics(metricsB);
        int width = metricsB.widthPixels * 20; width = width / 100;
        return width;
    }

    private ArrayList<PersonInfo> loadResults()
    {
        SharedPreferences sharedPreferences = getSharedPreferences("friendClick",MODE_PRIVATE);
        final GetFriendsLastResult getFriendsLastResult = new GetFriendsLastResult(this);
        final String login = sharedPreferences.getString("name","");
//        list = new ArrayList<PersonInfo>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (login.length() != 0)
                {
                    ArrayList<PersonInfo> tmp = getFriendsLastResult.getPersonInfoArrayListResult(login);
                    list.addAll(tmp);
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return list;
    }

    protected List<PersonDescriptor> getResults() {
        /*for(int i=0; i < 21; i++ ) {
            results.add(new PersonDescriptor());
        }*/
        results.clear();
        ArrayList<PersonInfo> list = loadResults();
        for (PersonInfo personInfo : list) {
            results.add(new PersonDescriptor(personInfo));
        }
        return results;
    }
}
