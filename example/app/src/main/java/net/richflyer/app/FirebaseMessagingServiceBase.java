package net.richflyer.app;

/**
 * Copyright © 2019 INFOCITY, Inc. All rights reserved.
 */

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import jp.co.infocity.richflyer.RFSendPushInformation;
import jp.co.infocity.richflyer.RichFlyer;
import jp.co.infocity.richflyer.RichFlyerResultListener;
import jp.co.infocity.richflyer.util.RFResult;

//FirebaseMessagingServiceを継承し、通知を受信する
public class FirebaseMessagingServiceBase extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //受信した情報をライブラリ側に渡す
        Map<String, String> data = remoteMessage.getData();
        RFSendPushInformation spi = new RFSendPushInformation(this, R.mipmap.ic_launcher);
        spi.setPushData(remoteMessage.getData());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Log.d("### refreshToken ##", s);

        RichFlyer richFlyer = new RichFlyer(getApplicationContext());
        richFlyer.tokenRefresh(s, new RichFlyerResultListener() {
            @Override
            public void onCompleted(RFResult result) {
                if (result.isResult()) {
                    Log.d("RichFlyer", "Refresh Token成功");
                } else {
                    Log.d("RichFlyer", "Refresh Token失敗");
                }
            }
        });
    }
}
