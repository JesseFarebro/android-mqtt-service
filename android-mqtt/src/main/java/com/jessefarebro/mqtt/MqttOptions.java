package com.jessefarebro.mqtt;

import android.os.Parcel;
import android.os.Parcelable;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Locale;

/**
 * Created by Jesse on 2/5/14.
 */
public class MqttOptions extends MqttConnectOptions implements Parcelable {
    private static final String MQTT_URI_FORMAT = "tcp://%s:%d";

    public static enum QOS {
        LEVEL_0(0),
        LEVEL_1(1),
        LEVEL_2(2);

        private int value;
        private QOS(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    final String brokerURI;

    final String broker;
    final int port;
    final int keepalive;
    final String username;
    final String password;

    final MqttLog.LogLevel logLevel;

    public MqttOptions(String broker,
                       int port,
                       int keepalive,
                       String username,
                       String password,
                       MqttLog.LogLevel logLevel) {
        this.broker = broker;
        this.port = port;
        this.keepalive = keepalive;
        this.username = username;
        this.password = password;
        this.logLevel = logLevel;

        this.brokerURI = String.format(Locale.US, MQTT_URI_FORMAT, broker, port);
    }

    public MqttOptions(Parcel in) {
        broker = in.readString();
        port = in.readInt();
        keepalive = in.readInt();
        username = in.readString();
        password = in.readString();
        logLevel = MqttLog.LogLevel.fromValue(in.readInt());

        this.brokerURI = String.format(Locale.US, MQTT_URI_FORMAT, broker, port);
    }

    public static final Parcelable.Creator<MqttOptions> CREATOR =
            new Parcelable.Creator<MqttOptions>() {
                public MqttOptions createFromParcel(Parcel in) {
                    return new MqttOptions(in);
                }

                public MqttOptions[] newArray(int size) {
                    return new MqttOptions[size];
                }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(broker);
        out.writeInt(port);
        out.writeInt(keepalive);
        out.writeString(username);
        out.writeString(password);
        out.writeValue(logLevel.getValue());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Builder {
        private String broker;
        private int port;
        private int keepalive;
        private String password;
        private String username;
        private MqttLog.LogLevel logLevel;



        public Builder setLogLevel(MqttLog.LogLevel logLevel) {
            if (logLevel == null) {
                throw new NullPointerException("loglevel is null");
            }

            this.logLevel = logLevel;
            return this;
        }

        public Builder setPassword(String password) {
            if (password == null || password.length() == 0) {
                throw new NullPointerException("password is null or empty");
            }

            this.password = password;
            return this;
        }

        public Builder setUserName(String username) {
            if (username == null || username.length() == 0) {
                throw new NullPointerException("username is null or empty");
            }

            this.username = username;
            return this;
        }

        public Builder setKeepAliveInterval(int keepAliveInterval) {
            if (keepAliveInterval <= 0) {
                throw new IllegalArgumentException("keepAliveInterval less than zero");
            }

            this.keepalive = keepAliveInterval;
            return this;
        }

        public Builder setBroker(String broker) {
            if (broker == null || broker.trim().length() == 0) {
                throw new NullPointerException("Broker cannot be null.");
            }

            this.broker = broker;
            return this;
        }

        public Builder setPort(int port) {
            if (port <= 0) {
                throw new IllegalArgumentException("Port must be valid.");
            }

            this.port = port;
            return this;
        }

        public MqttOptions build() {
            ensureSaneDefaults();
            return new MqttOptions(broker, port, keepalive, username, password, logLevel);
        }

        private void ensureSaneDefaults() {
            if (broker == null || broker.length() == 0) {
                throw new NullPointerException("broker cannot be null on build()");
            } else if (port <= 0) {
                throw new NullPointerException("port isn't set or is less than 0 on build()");
            } else if (keepalive <= 0) {
                keepalive = 240000;
            } else if (logLevel == null) {
                logLevel = MqttLog.LogLevel.BASIC;
            }
        }
    }
}
