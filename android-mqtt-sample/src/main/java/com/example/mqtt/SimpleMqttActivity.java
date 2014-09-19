package com.example.mqtt;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.jessefarebro.mqtt.AndroidMqttMessage;
import com.jessefarebro.mqtt.MqttService;

/**
 * Created by Jesse on 2/16/14.
 */
public class SimpleMqttActivity extends Activity {
    private TextView console;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_activity);
        console = (TextView) findViewById(R.id.console);

        final EditText topic = (EditText) findViewById(R.id.topic);
        final EditText payload = (EditText) findViewById(R.id.payload);

        LocalBroadcastManager.getInstance(this).registerReceiver(new SimpleMessageReceiver(),
                new IntentFilter(MqttService.Intents.MESSAGE_RECEIVED));

        LocalBroadcastManager.getInstance(this).registerReceiver(new SimpleDeliveryReceiver(),
                new IntentFilter(MqttService.Intents.DELIVERY_COMPLETE));

        Button subscribe = (Button) findViewById(R.id.subscribe);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SimpleMqttActivity", "subscribe clicked");
                MqttService.IntentHelper.subscribeToTopic(SimpleMqttActivity.this, topic.getText().toString());
                console.append("\nSubscribed to " + topic.getText().toString());
            }
        });

        Button publish = (Button) findViewById(R.id.publish);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MqttService.IntentHelper.publishToTopic(SimpleMqttActivity.this, topic.getText().toString(),
                        payload.getText().toString().getBytes());

                console.append("\n\nPublished " + payload.getText().toString() + " to " + topic.getText().toString());
            }
        });
    }

    private class SimpleMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SimpleMqttActivity", "Message received");
            //AndroidMqttMessage message = intent.getParcelableExtra(MqttService.Extras.MESSAGE);
            //Log.i("SimpleMqttActivity",message.toString());
            console.append("\n\nReceived message: " + new String(intent.getByteArrayExtra(MqttService.Extras.PAYLOAD))
                    + " in topic " + intent.getStringExtra(MqttService.Extras.TOPIC));
        }
    }

    private class SimpleDeliveryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("SimpleMqttActivity", "Delivery complete");
        }
    }
}