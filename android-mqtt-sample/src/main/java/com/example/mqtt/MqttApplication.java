package com.example.mqtt;

import android.app.Application;
import com.jessefarebro.mqtt.MqttLog;
import com.jessefarebro.mqtt.MqttOptions;
import com.jessefarebro.mqtt.MqttService;

/**
 * Created by jesse on 4/21/14.
 */
public class MqttApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final MqttOptions options = new MqttOptions.Builder()
                .setBroker("iot.eclipse.org")
                .setPort(1883)
                .setKeepAliveInterval(25000)
                .setLogLevel(MqttLog.LogLevel.FULL)
                .build();

        MqttService.IntentHelper.startServiceWithOptions(this, options);
    }
}
