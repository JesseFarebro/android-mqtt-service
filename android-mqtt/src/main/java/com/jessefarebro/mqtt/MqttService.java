package com.jessefarebro.mqtt;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import com.jessefarebro.mqtt.persist.SQLPersistence;
import org.eclipse.paho.client.mqttv3.*;

import java.util.Arrays;


public class MqttService extends Service implements MqttCallback {
	public static final String DEBUG_TAG = "MqttService";

	private AndroidMqttClient mClient;

    private MqttLog mLog;
	
	private AlarmManager mAlarmManager;
	private ConnectivityManager mConnectivityManager;
    private LocalBroadcastManager mLocalBroadcastManager;

    public static enum ConnectionStatus {
        INITIAL,
        CONNECTING,
        CONNECTED,
        WAITING_FOR_INTERNET,
        DATA_DISABLED,
        UNKNOWN
    }

    private ConnectionStatus mConnectionStatus
            = ConnectionStatus.INITIAL;

    @Override
    public void onCreate() {
        super.onCreate();

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mLocalBroadcastManager =  LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        final String action = intent.getAction();

        switch(action) {
            case Actions.START_W_OPTIONS:
                final MqttOptions options = (MqttOptions) intent.getParcelableExtra(Extras.OPTIONS);

                if (options == null) {
                    throw new NullPointerException(
                            "MqttOPtions is null, did you use MqttService.startWithOptions?"
                    );
                }
                mLog = new MqttLog(DEBUG_TAG, options.logLevel);

                configureClient(options);
                connect();
                break;

            case Actions.SUBSCRIBE:
                mLog.log(MqttLog.LogLevel.FULL, "Connection Status: " + mConnectionStatus);
                final String subscribeTopicFilter = intent.getStringExtra(Extras.TOPIC);
                final String[] subscribeTopicFilterArray = intent.getStringArrayExtra(Extras.TOPIC_ARRAY);
                final int subscribeTopicQos = intent.getIntExtra(Extras.QOS, MqttOptions.QOS.LEVEL_0.getValue());
                int[] subscribeTopicQosArray = intent.getIntArrayExtra(Extras.QOS_ARRAY);

                if (subscribeTopicFilter != null && subscribeTopicFilterArray == null) {
                    subscribeToTopic(subscribeTopicFilter, subscribeTopicQos);
                } else if (subscribeTopicFilter == null && subscribeTopicFilterArray != null) {
                    if (subscribeTopicQosArray == null) {
                        subscribeTopicQosArray = new int[subscribeTopicFilterArray.length];
                        Arrays.fill(subscribeTopicQosArray, MqttOptions.QOS.LEVEL_0.getValue());
                    }
                    subscribeToTopic(subscribeTopicFilterArray, subscribeTopicQosArray);
                }
                break;
            case Actions.PUBLISH:
                final String publishTopicFilter = intent.getStringExtra(Extras.TOPIC);
                final int publishTopicQos = intent.getIntExtra(Extras.QOS, MqttOptions.QOS.LEVEL_0.getValue());
                final byte[] publishPayload = intent.getByteArrayExtra(Extras.PAYLOAD);
                final boolean publishRetain = intent.getBooleanExtra(Extras.RETAINED, false);

                if(publishTopicFilter == null) {
                    throw new NullPointerException("Topic should not be null");
                }

                publishToTopic(publishTopicFilter, publishTopicQos, publishPayload, publishRetain);
                break;

            default:
                // From Service crash handle

        }

        return START_STICKY;
    }

    private void subscribeToTopic(String topic, int qos) {
        try {
            mClient.subscribe(topic, qos);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void subscribeToTopic(String[] topics, int[] qos) {
        try {
            mClient.subscribe(topics, qos);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void publishToTopic(String topic, int qos, byte[] payload, boolean retain) {
        try {
            mClient.publish(topic, payload, qos, retain);
        } catch(MqttException ex) {
            ex.printStackTrace();
        }
    }


    private MqttConnectOptions configureConnectOptions(MqttOptions options) {
        final MqttConnectOptions connectOptions = new MqttConnectOptions();

        if (options.username != null) {
            connectOptions.setUserName(options.username);
        } else if (options.password != null) {
            connectOptions.setPassword(options.password.toCharArray());
        }

        return connectOptions;
    }

    private void connect() {
        if(mClient == null) {
            throw new NullPointerException("Client isn't configured");
        }
        mConnectionStatus = ConnectionStatus.CONNECTING;

        try {
            mClient.connect();

            mClient.setCallback(this);

            mConnectionStatus = ConnectionStatus.CONNECTED;
        } catch(MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void configureClient(MqttOptions options) {
        final MqttConnectOptions connectOpts =
                configureConnectOptions(options);
        mLog.log(MqttLog.LogLevel.FULL, "Broker URL: " + options.brokerURI);

        try {
            mClient = new AndroidMqttClient(options.brokerURI,
                    "asdgbsddgf", new SQLPersistence(this));

            mClient.setConnectOptions(connectOpts);
            mLog.log(MqttLog.LogLevel.FULL, "Client Created...");
        } catch (MqttException ex) {
            mLog.log(MqttLog.LogLevel.BASIC,
                    "Creating the AndroidMqttClient failed...\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        connect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        mLog.log(MqttLog.LogLevel.FULL, "Message arrived on " + topic);
        Uri uri = new Uri.Builder()
                .scheme("mqtt")
                .path(topic).build();

        Intent intent = new Intent(Intents.MESSAGE_RECEIVED);
        intent.putExtra(Extras.PAYLOAD, message.getPayload());
        intent.putExtra(Extras.QOS, message.getQos());
        intent.putExtra(Extras.TOPIC, topic);

        /*Intent intent = new Intent(Intents.MESSAGE_RECEIVED);
        intent.putExtra(Extras.TOPIC, topic);
        intent.putExtra(Extras.MESSAGE, (AndroidMqttMessage)message);
*/
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Intent intent = new Intent(Intents.DELIVERY_COMPLETE);

        mLocalBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent bind) {
        return null;
    }

    public static class Intents {
        public static final String MESSAGE_RECEIVED = "com.jessefarebro.mqtt.MESSAGE_RECEIVED";
        public static final String DELIVERY_COMPLETE = "com.jessefarebro.mqtt.DELIVERY_COMPLETE";
    }

    public static class Actions {
        public static final String START_W_OPTIONS = "com.jessefarebro.mqtt.actions.START_W_OPTIONS";
        public static final String SUBSCRIBE = "com.jessefarebro.mqtt.actions.SUBSCRIBE";
        public static final String PUBLISH = "com.jessefarebro.mqtt.actions.PUBLISH";
    }

    public static class Extras {
        public static final String OPTIONS = "com.jessefarebro.mqtt.extras.OPTIONS";
        public static final String TOPIC = "com.jessefarebro.mqtt.extras.TOPIC";
        public static final String TOPIC_ARRAY = "com.jessefarebro.mqtt.extras.TOPIC_ARRAY";
        public static final String QOS = "com.jessefarebro.mqtt.extras.QOS";
        public static final String QOS_ARRAY = "com.jessefarebro.mqtt.extras.QOS_ARRAY";
        public static final String PAYLOAD = "com.jessefarebro.mqtt.extras.PAYLOAD";
        public static final String RETAINED = "com.jessefarebro.mqtt.extras.RETAINED";

        public static final String MESSAGE = "com.jessefarebro.mqtt.extras.MESSAGE";
    }

    public static class IntentHelper {
        public static void startServiceWithOptions(Context context, MqttOptions options) {
            final Intent intent = new Intent(context, MqttService.class);
            intent.setAction(Actions.START_W_OPTIONS);
            intent.putExtra(Extras.OPTIONS, options);

            context.startService(intent);
        }

        public static void subscribeToTopic(Context context, String topic, int qos) {
            final Intent intent = new Intent(context, MqttService.class);
            intent.setAction(Actions.SUBSCRIBE);
            intent.putExtra(Extras.TOPIC, topic);

            if (qos != -1) {
                intent.putExtra(Extras.QOS, qos);
            }

            context.startService(intent);
        }

        public static void subscribeToTopic(Context context, String topic) {
            subscribeToTopic(context, topic, -1);
        }

        public static void subscribeToTopic(Context context, String[] topics, int[] qos) {
            final Intent intent = new Intent(context, MqttService.class);
            intent.setAction(Actions.SUBSCRIBE);
            intent.putExtra(Extras.TOPIC_ARRAY, topics);

            if (qos != null) {
                intent.putExtra(Extras.QOS_ARRAY, qos);
            }

            context.startService(intent);
        }

        public static void subscribeToTopic(Context context, String[] topics) {
            subscribeToTopic(context, topics, null);
        }

        public static void publishToTopic(Context context, String topic, int qos, byte[] payload, boolean retained) {
            final Intent intent = new Intent(context, MqttService.class);
            intent.setAction(Actions.PUBLISH);
            intent.putExtra(Extras.TOPIC, topic);
            if(qos != -1) {
                intent.putExtra(Extras.QOS, qos);
            }
            intent.putExtra(Extras.PAYLOAD, payload);
            intent.putExtra(Extras.RETAINED, retained);

            context.startService(intent);
        }

        public static void publishToTopic(Context context, String topic, byte[] payload) {
            publishToTopic(context, topic, -1, payload, false);
        }

    }
}
