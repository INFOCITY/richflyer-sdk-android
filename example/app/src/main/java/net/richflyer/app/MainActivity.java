package net.richflyer.app;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.co.infocity.richflyer.RichFlyer;
import jp.co.infocity.richflyer.RichFlyerResultListener;
import jp.co.infocity.richflyer.action.RFAction;
import jp.co.infocity.richflyer.action.RFActionListener;
import jp.co.infocity.richflyer.history.RFContent;
import jp.co.infocity.richflyer.util.RFResult;
import jp.co.infocity.richflyer.RichFlyerPostingResultListener;

/**
 * Copyright © 2019 INFOCITY, Inc. All rights reserved.
 */

public class MainActivity extends AppCompatActivity {

    static private final String mServiceKey = "aaaaaaaa-0000-bbbb-1111-cccccccccccc";


    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    /**
     * Event送信ボタン用
     */
    enum EventParameter {
        event1,
        event2,
        event3,
        variableName1,
        variableValue1,
        variableName2,
        variableValue2,
        variableName3,
        variableValue3,
        standbyTime,
        sendButton;


        @IdRes
        int getResId() {
            switch (this) {
                case event1:
                    return R.id.event1;
                case event2:
                    return R.id.event2;
                case event3:
                    return R.id.event3;
                case variableName1:
                    return R.id.event_variable_name1;
                case variableValue1:
                    return R.id.event_variable_value1;
                case variableName2:
                    return R.id.event_variable_name2;
                case variableValue2:
                    return R.id.event_variable_value2;
                case variableName3:
                    return R.id.event_variable_name3;
                case variableValue3:
                    return R.id.event_variable_value3;
                case standbyTime:
                    return R.id.event_standby_time;
                case sendButton:
                    return R.id.event_send_button;
                default:
                    return -1;
            }
        }
    }

    /**
     * Segment登録用
     */
    enum SegmentParameter {
        segment1,
        segment2,
        segment3,
        segment4,
        segment5,
        segment6;

        @IdRes int getSpinnerId() {
            switch (this) {
                case segment1:
                    return R.id.segment_input1;
                case segment2:
                    return R.id.segment_input2;
                case segment3:
                    return R.id.segment_input3;
                case segment4:
                    return R.id.segment_input4;
                case segment5:
                    return R.id.segment_input5;
                case segment6:
                    return R.id.segment_input6;
                default:
                    return -1;
            }
        }

        @ArrayRes
        int getArrayId() {
            switch (this) {
                case segment1:
                    return R.array.segment_list1;
                case segment2:
                    return R.array.segment_list2;
                case segment3:
                    return R.array.segment_list3;
                case segment4:
                    return R.array.segment_list4;
                default:
                    return -1;
            }
        }

           int getSegmentTitle() {
               switch (this) {
                   case segment1:
                       return R.string.segment_title1;
                   case segment2:
                       return R.string.segment_title2;
                   case segment3:
                       return R.string.segment_title3;
                   case segment4:
                       return R.string.segment_title4;
                   case segment5:
                       return R.string.segment_title5;
                   case segment6:
                       return R.string.segment_title6;
                   default:
                       return -1;
               }
           }

           int getSegmentName() {
               switch (this) {
                   case segment1:
                       return R.string.segment_name1;
                   case segment2:
                       return R.string.segment_name2;
                   case segment3:
                       return R.string.segment_name3;
                   case segment4:
                       return R.string.segment_name4;
                   case segment5:
                       return R.string.segment_name5;
                   case segment6:
                       return R.string.segment_name6;
                   default:
                       return -1;
               }
           }
    }

    private HashMap<String, String> segments;
    private HashMap<String, String> stringSegments;
    private HashMap<String, Integer> integerSegments;
    private HashMap<String, Boolean> booleanSegments;
    private HashMap<String, Date> dateSegments;
    private ArrayList<String> postIds = new ArrayList<>();

    @SuppressWarnings("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conenection_tab_layout);

        initTabs();

        initEvents();

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

        int resId = getResources().getIdentifier(String.valueOf(R.color.themaColor1), "string", getPackageName());
        RichFlyer flyer = new RichFlyer(getApplicationContext(), token, mServiceKey,
                getString(resId), MainActivity.class);

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
     * SharedPreferencesを使うための汎用メソッド
     */
    protected void setPreferences(String key, String value){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
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

            // Tab3
            spec = tabHost.newTabSpec("Tab3")
                    .setIndicator("イベント", null)
                    .setContent(R.id.tab3);
            tabHost.addTab(spec);

            tabHost.setCurrentTab(0);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event送信画面
     */

    protected void initEvents(){

        Button register = (Button) findViewById(R.id.event_send_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editEvent1 = (EditText)findViewById(EventParameter.event1.getResId());
                EditText editEvent2 = (EditText)findViewById(EventParameter.event2.getResId());
                EditText editEvent3 = (EditText)findViewById(EventParameter.event3.getResId());
                ArrayList<String> events = new ArrayList<>();
                String event1 = editEvent1.getText().toString();
                if (event1 != null && event1.length() > 0) {
                    events.add(event1);
                }
                String event2 = editEvent2.getText().toString();
                if (event2 != null && event2.length() > 0) {
                    events.add(event2);
                }
                String event3 = editEvent3.getText().toString();
                if (event3 != null && event3.length() > 0) {
                    events.add(event3);
                }

                Map<String, String> variables = new HashMap<>();
                EditText editVariableName1 = (EditText)findViewById(EventParameter.variableName1.getResId());
                EditText editVariableValue1= (EditText)findViewById(EventParameter.variableValue1.getResId());
                EditText editVariableName2 = (EditText)findViewById(EventParameter.variableName2.getResId());
                EditText editVariableValue2= (EditText)findViewById(EventParameter.variableValue2.getResId());
                EditText editVariableName3 = (EditText)findViewById(EventParameter.variableName3.getResId());
                EditText editVariableValue3= (EditText)findViewById(EventParameter.variableValue3.getResId());
                String variableName1 = editVariableName1.getText().toString();
                String variableValue1 = editVariableValue1.getText().toString();
                if (variableName1 != null && variableName1.length() > 0 && variableValue1 != null && variableValue1.length() > 0) {
                    variables.put(variableName1, variableValue1);
                }
                String variableName2 = editVariableName2.getText().toString();
                String variableValue2 = editVariableValue2.getText().toString();
                if (variableName2 != null && variableName2.length() > 0 && variableValue2 != null && variableValue2.length() > 0) {
                    variables.put(variableName2, variableValue2);
                }
                String variableName3 = editVariableName3.getText().toString();
                String variableValue3 = editVariableValue3.getText().toString();
                if (variableName3 != null && variableName3.length() > 0 && variableValue3 != null && variableValue3.length() > 0) {
                    variables.put(variableName3, variableValue3);
                }

                EditText editStandbyTime= (EditText)findViewById(EventParameter.standbyTime.getResId());
                String standbyTimeStr = editStandbyTime.getText().toString();
                Integer standbyTime = null;
                if (standbyTimeStr != null && standbyTimeStr.length() > 0) {
                    standbyTime = new Integer(standbyTimeStr);
                }

                RichFlyer.postMessage(events.toArray(new String[events.size()]), variables, standbyTime, getApplicationContext(), new RichFlyerPostingResultListener() {
                    @Override
                    public void onCompleted(RFResult result, String[] eventPostIds) {
                        String message = "";
                        if (result.isResult()) {
                            Log.d("RichFlyer", "Cancel message has succeeded.");
                            String postIds = "";
                            for (String postId : eventPostIds) {
                                postIds = postId + "\n";
                                MainActivity.this.postIds.add(postId);
                            }
                            message = "メッセージ送信リクエストに成功しました。\n送信ID:\n" + postIds;
                        } else {
                            Log.d("RichFlyer", "Cancel message has failed.");
                            message = "メッセージ送信リクエストに失敗しました。\ncode:" + result.getErrorCode() + "\n" + result.getMessage();
                        }
                        showDialogMessage(message);
                    }
                });
            }
        });


        Button cancel = (Button) findViewById(R.id.event_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.postIds == null) return;

                for (String postId : MainActivity.this.postIds) {
                    RichFlyer.cancelPosting(postId, getApplicationContext(), new RichFlyerPostingResultListener() {
                        @Override
                        public void onCompleted(RFResult result, String[] eventPostIds) {
                            if (result.isResult()) {
                                Log.d("RichFlyer", "Cancel message has succeeded.");
                            } else {
                                Log.d("RichFlyer", "Cancel message has failed.");
                            }
                    }
                });
            }
            }
        });


    }

    /**
     * Segment登録画面
     */
    protected void initSegments(){

        segments = new HashMap<>();
        stringSegments = new HashMap<>();
        integerSegments = new HashMap<>();
        booleanSegments = new HashMap<>();
        dateSegments = new HashMap<>();

        for (SegmentParameter param : SegmentParameter.values()) {
            Object parts = findViewById(param.getSpinnerId());

            String segmentName = getString(param.getSegmentName());
            if (parts instanceof Spinner) {
                // 選択
                Spinner spinner = (Spinner)parts;
                spinner.setAdapter(makeArrayAdapter(param.getArrayId()));


                spinner.setOnItemSelectedListener(makeItemSelectedListener(segmentName));
            } else if (parts instanceof EditText) {
                // カレンダーから日付選択
                EditText editText=(EditText)parts;
                final Calendar myCalendar= Calendar.getInstance();

                DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH,month);
                        myCalendar.set(Calendar.DAY_OF_MONTH,day);
                        String myFormat="yyyy/MM/dd";
                        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat);
                        editText.setText(dateFormat.format(myCalendar.getTime()));
                        dateSegments.put(segmentName, myCalendar.getTime());
                    }
                };
                editText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DatePickerDialog(MainActivity.this, date,
                                myCalendar.get(Calendar.YEAR),
                                myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH)
                        ).show();
                    }
                });
            }
        }


        Button register = (Button) findViewById(R.id.segment_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RichFlyer.registerSegments(stringSegments, integerSegments, booleanSegments,
                        dateSegments, getApplicationContext(), new RichFlyerResultListener() {
                            @Override
                            public void onCompleted(RFResult result) {
                                if (result.isResult()) {
                                    showMessage(v, "セグメントを登録しました。");
                                } else {
                                    String message = "セグメントの登録に失敗しました。\ncode:" + result.getErrorCode() + "\n" + result.getMessage();
                                    showDialogMessage(message);
                                }
                            }
                        });
            }
        });
    }

    private void setDateTextView(TextView textView, final String key) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(i, i1, i2);
                // 出力フォーマットを設定
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                // Calendar型をString型に変換
                String strDate = sdf.format(calendar.getTime());
                textView.setText(strDate);
                Date date = calendar.getTime();
                dateSegments.put(key, calendar.getTime());
                Log.d("", "");
            }
        };

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, listener, 2000, 1, 1);
                dialog.show();
            }
        });
    }

    /**
     * 履歴画面
     */
    protected void initRecieve(){
        recyclerView = findViewById(R.id.notification_history_recyclerview);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);

        ArrayList<RFContent> notificationList = RichFlyer.getHistory(getBaseContext());
        if (notificationList != null) {
            mAdapter = new NotificationHistoryAdapter(notificationList);
            recyclerView.setAdapter(mAdapter);
        }
    }

    private ArrayAdapter<String> makeArrayAdapter(ArrayList<String> segments) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, segments);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
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
        try {
            Integer intValue = Integer.parseInt(value);
            integerSegments.put(key, intValue);
        } catch (NumberFormatException e) {
            if (value.equals("true") || value.equals("false")) {
                booleanSegments.put(key, value == "true" ? true : false);
            } else {
                stringSegments.put(key, value);
            }
        }
    }

    @SuppressLint("WrongConstant")
    private void showMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void showDialogMessage(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle( R.string.app_name )
                .setMessage(message)
                .setPositiveButton("OK" , null )
                .show();
    }
}
