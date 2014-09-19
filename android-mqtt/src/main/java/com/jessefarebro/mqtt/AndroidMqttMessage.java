package com.jessefarebro.mqtt;

import android.os.Parcel;
import android.os.Parcelable;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Locale;

/**
 * Created by jesse on 4/23/14.
 */
public class AndroidMqttMessage extends MqttMessage implements Parcelable {

    public AndroidMqttMessage(Parcel in) {
        setMutable(in.readInt() == 1);

        byte[] temp = new byte[in.readInt()];
        in.readByteArray(temp);
        setPayload(temp);

        setQos(in.readInt());
        setRetained(in.readInt() == 1);
        setDuplicate(in.readInt() == 1);
    }

    public static final Parcelable.Creator<AndroidMqttMessage> CREATOR =
            new Parcelable.Creator<AndroidMqttMessage>() {
                public AndroidMqttMessage createFromParcel(Parcel in) {
                    return new AndroidMqttMessage(in);
                }

                public AndroidMqttMessage[] newArray(int size) {
                    return new AndroidMqttMessage[size];
                }
            };
    @Override
    public void writeToParcel(Parcel out, int flags) {
        try {
            checkMutable();
            out.writeInt(1);
        } catch(IllegalStateException ex) {
            out.writeInt(0);
        }

        out.writeInt(getPayload().length);
        out.writeByteArray(getPayload());
        out.writeInt(getQos());
        out.writeInt((isRetained()) ? 1 : 0);
        out.writeInt((isDuplicate()) ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
