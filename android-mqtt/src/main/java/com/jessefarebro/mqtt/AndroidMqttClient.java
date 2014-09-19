package com.jessefarebro.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

/**
 * Created by Jesse on 2/5/14.
 */
public class AndroidMqttClient extends MqttAsyncClient {
    private final MqttWireMessage mPingRequest;
    private MqttConnectOptions mOptions;

    public AndroidMqttClient(String serverURI, String clientId) throws MqttException {
        super(serverURI, clientId);

        mPingRequest = new MqttPingReq();
    }

    public AndroidMqttClient(String serverURI, String clientId, MqttClientPersistence persistence)
            throws MqttException {
        super(serverURI, clientId, persistence);

        mPingRequest = new MqttPingReq();
    }

    @Override
    public IMqttToken connect() throws MqttException {
        if(mOptions != null) {
            return super.connect(mOptions);
        } else {
            return super.connect();
        }
    }

    public void setConnectOptions(MqttConnectOptions options) {
        mOptions = options;
    }

    public void ping() throws MqttException {
        super.comms.sendNoWait(mPingRequest, null);
    }
}
