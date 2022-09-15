package net.richflyer.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TabHost;

import androidx.annotation.ArrayRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.co.infocity.richflyer.RichFlyer;
import jp.co.infocity.richflyer.RichFlyerResultListener;
import jp.co.infocity.richflyer.action.RFAction;
import jp.co.infocity.richflyer.action.RFActionListener;
import jp.co.infocity.richflyer.history.RFContent;
import jp.co.infocity.richflyer.util.RFResult;

/**
 * Copyright © 2019 INFOCITY, Inc. All rights reserved.
 */

public class MainActivity extends AppCompatActivity {

    static private final String mServiceKey = "aaaaaaaa-0000-bbbb-1111-cccccccccccc";


    /**
     * Segment登録用
     */
    enum SegmentParameter {
        genre,
        day,
        launchCount,
        dayTime;

        @IdRes int getSpinnerId() {
            switch (this) {
                case genre:
                    return R.id.segment_spinner_genre;
                case day:
                    return R.id.segment_spinner_day;
                case launchCount:
                    return R.id.segment_spinner_launch_count;
                case dayTime:
                    return R.id.segment_spinner_day_time;
                default:
                    return -1;
            }
        }

        @ArrayRes
        int getArrayId() {
            switch (this) {
                case genre:
                    return R.array.spinner_list_genre;
                case day:
                    return R.array.spinner_list_day;
                case launchCount:
                    return R.array.spinner_list_launch_count;
                case dayTime:
                    return R.array.spinner_list_daytime;
                default:
                    return -1;
            }
        }
    }

    private HashMap<String, String> segments;

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conenection_tab_layout);

        initTabs();

        initSegments();

        initRecieve();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            String token = task.getResult();

            Log.d("RichFlyer", "**** / " + token);

            initializeRichFlyer(token);
        });


        if (RichFlyer.richFlyerAction(getIntent())) {
            RichFlyer.parseAction(getIntent(), new RFActionListener() {
                @Override
                public void onRFEventOnClickButton(@NonNull RFAction action, @NonNull String index) {
                    Log.d("richflyer", "ボタンが押された");
                }

                @Override
                public void onRFEventOnClickStartApplication(String notificationId, String extendedProperty, @NonNull String index) {
                    Log.d("richflyer", "通知からアプリが起動した");
                }
            });
        }
    }

    private void initializeRichFlyer(String token) {
        RichFlyer.checkNotificationPermission(this);

        RichFlyer flyer = new RichFlyer(getApplicationContext(), token, mServiceKey,
                getString(R.color.themaColor1), MainActivity.class);

        flyer.startSetting(new RichFlyerResultListener() {
            @Override
            public void onCompleted(RFResult result) {
                if (result.isResult()) {
                    Log.d("RichFlyer", "RichFlyer初期化成功");
                } else {
                    Log.d("RichFlyer", "RichFlyer初期化失敗");
                }
            }
        });
    }


    /**
     * タブ作成
     */
    protected void initTabs() {
        try {
            TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
            tabHost.setup();
            TabHost.TabSpec spec;

            // Tab1
            spec = tabHost.newTabSpec("Tab1")
                    .setIndicator("セグメント", null)
                    .setContent(R.id.tab1);
            tabHost.addTab(spec);

            // Tab2
            spec = tabHost.newTabSpec("Tab2")
                    .setIndicator("レシーブ", null)
                    .setContent(R.id.tab2);
            tabHost.addTab(spec);

            tabHost.setCurrentTab(0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Segment登録画面
     */
    protected void initSegments(){

        segments = new HashMap<>();

        for (SegmentParameter param : SegmentParameter.values()) {
            Spinner spinner = (Spinner)findViewById(param.getSpinnerId());
            spinner.setAdapter(makeArrayAdapter(param.getArrayId()));
            spinner.setOnItemSelectedListener(makeItemSelectedListener(param.name()));
        }

        Button register = (Button) findViewById(R.id.segment_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Map<String, String> param = new HashMap<>();
                for (String key : segments.keySet()) {
                    param.put(key, segments.get(key));
                }
                System.out.println("********* / " + segments);
                RichFlyer.registerSegments(param, getApplicationContext(), new RichFlyerResultListener() {
                    @Override
                    public void onCompleted(RFResult result) {
                        if (result.isResult()) {
                            showMessage(v, "Segmentを" + segments + "の内容で登録しました。");
                        } else {
                            showMessage(v, "Segment登録が失敗しました。");
                        }
                    }
                });
            }
        });
    }

    /**
     * 履歴画面
     */
    protected void initRecieve(){

        Button recieve = (Button) findViewById(R.id.recieve_button);
        recieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ArrayList<RFContent> list = RichFlyer.getHistory(getBaseContext());
                if (list != null && list.size() > 0) {
                    RichFlyer.showHistoryNotification(getApplicationContext(), list.get(0).getNotificationId());
                }
            }
        });
    }

    private ArrayAdapter<String> makeArrayAdapter(@ArrayRes int id) {
        String[] arraySpinner = getResources().getStringArray(id);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private AdapterView.OnItemSelectedListener makeItemSelectedListener(final String key) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onSpinnerClick(key, parent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private void onSpinnerClick(String key, View parent) {
        Spinner spinner = (Spinner) parent;
        String value = spinner.getSelectedItem().toString();
        segments.put(key, value);
    }

    @SuppressLint("WrongConstant")
    private void showMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

}
