package com.jessefarebro.mqtt.persist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.internal.MqttPersistentData;

import java.util.Enumeration;
import java.util.Vector;


public class SQLPersistence implements MqttClientPersistence {
    private final Context context;
    private SQLPersistenceHelper helper;
    private String clientHash;

    public SQLPersistence(Context ctx) {
        this.context = ctx;
    }

    @Override
    public void open(String clientId, String serverURI) throws MqttPersistenceException {
        helper = SQLPersistenceHelper.getInstance(context);

        clientHash = "_" + Base64.encodeToString((clientId + serverURI).getBytes(),
                Base64.URL_SAFE | Base64.NO_PADDING);

        if (!helper.tableExists(clientHash)) {
            helper.getWritableDatabase().execSQL("CREATE TABLE IF NOT EXISTS " + clientHash + "("
                    + SQLPersistenceHelper.Columns.Persistence.ID
                    + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + SQLPersistenceHelper.Columns.Persistence.KEY
                    + " TEXT NOT NULL, "
                    + SQLPersistenceHelper.Columns.Persistence.HEADER
                    + " BLOB NOT NULL, "
                    + SQLPersistenceHelper.Columns.Persistence.PAYLOAD
                    + " BLOB" + ");"
            );
        }
    }

    @Override
    public void close() throws MqttPersistenceException {
        if (helper == null) {
            throw new MqttPersistenceException(
                    MqttPersistenceException.REASON_CODE_CLIENT_EXCEPTION);
        }

        helper.close();
        helper = null;

        clientHash = null;
    }

    @Override
    public void put(String key, MqttPersistable mqttPersistable) throws MqttPersistenceException {
        final ContentValues cv = new ContentValues();

        cv.put(SQLPersistenceHelper.Columns.Persistence.KEY, key);
        cv.put(SQLPersistenceHelper.Columns.Persistence.HEADER,
                mqttPersistable.getHeaderBytes());

        if (mqttPersistable.getPayloadBytes() != null) {
           cv.put(SQLPersistenceHelper.Columns.Persistence.PAYLOAD,
                   mqttPersistable.getPayloadBytes());
        }

        helper.getWritableDatabase().insert(clientHash, null, cv);
    }

    @Override
    public MqttPersistable get(String key) throws MqttPersistenceException {
        final Cursor c = helper.getReadableDatabase().query(
                clientHash,
                new String[] {SQLPersistenceHelper.Columns.Persistence.HEADER,
                    SQLPersistenceHelper.Columns.Persistence.PAYLOAD, },
                "key=?",
                new String[]{key},
                null,
                null,
                null
        );

        if (c != null) {
            if (c.getCount() == 1) {
                c.moveToFirst();

                final int headerIndex = c.getColumnIndex(
                        SQLPersistenceHelper.Columns.Persistence.HEADER);
                final int payloadIndex = c.getColumnIndex(
                        SQLPersistenceHelper.Columns.Persistence.PAYLOAD);

                final byte[] header = c.getBlob(headerIndex);
                final byte[] payload = c.getBlob(payloadIndex);

                c.close();

                return new MqttPersistentData(
                        key, header, 0, header.length, payload, 0, payload.length);
            }
        }

        throw new MqttPersistenceException(MqttPersistenceException.REASON_CODE_UNEXPECTED_ERROR);
    }

    @Override
    public void remove(String key) throws MqttPersistenceException {
        final int result = helper.getWritableDatabase().delete(
                clientHash, "key=?", new String[] {key});

        if (result == 0) {
            throw new MqttPersistenceException();
        }
    }

    @Override
    public void clear() throws MqttPersistenceException {
        helper.getWritableDatabase().delete(clientHash, null, null);
    }

    @Override
    public boolean containsKey(String key) throws MqttPersistenceException {
        final Cursor c = helper.getReadableDatabase().query(
                clientHash,
                new String[] {SQLPersistenceHelper.Columns.Persistence.ID},
                "key=?",
                new String[] {key},
                null,
                null,
                null
        );

        final boolean exists = c.getCount() == 1;
        c.close();
        return exists;
    }

    @Override
    public Enumeration keys() throws MqttPersistenceException {
        final Cursor c = helper.getReadableDatabase().query(
                clientHash,
                new String[] {SQLPersistenceHelper.Columns.Persistence.KEY},
                null,
                null,
                null,
                null,
                null
        );

        final int count = c.getCount();
        final Vector result = new Vector(count);

        if (count > 0) {
            while (c.moveToNext()) {
                final int keyIndex = c.getColumnIndex(SQLPersistenceHelper.Columns.Persistence.KEY);

                result.addElement(c.getString(keyIndex));
            }
        }
        c.close();

        return result.elements();
    }

    private static class SQLPersistenceHelper extends SQLiteOpenHelper {
        public static final String DB_NAME = "MqttPersist.db";
        public static final int DB_VERSION = 1;

        private static SQLPersistenceHelper helper;

        public static SQLPersistenceHelper getInstance(Context ctx) {
            if (helper == null) {
                helper = new SQLPersistenceHelper(ctx);
            }
            return helper;
        }

        public boolean tableExists(String table) {
            final Cursor c = getReadableDatabase().rawQuery(
                    "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?;",
                    new String[] {table}
            );

            if (c != null) {
                if (c.getCount() == 1) {
                    c.close();
                    return true;
                }
                c.close();
            }
            return false;
        }

        public SQLPersistenceHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) { }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


        public static final class Columns {
            public static final class Persistence {
                static final String ID = "_id";
                static final String KEY = "key";
                static final String HEADER = "header";
                static final String PAYLOAD = "payload";
            }
        }
    }

}
