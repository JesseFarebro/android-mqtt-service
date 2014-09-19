DEPRECATED
=========================
I would strongly recommend using the official [Paho Android client](http://www.eclipse.org/paho/clients/android/).

Setup
----
* Make sure to add the service to your AndroidManifest.xml
* Edit the appropriate fields in the class with the information required

Simple Example
----
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
         android:versionCode="1"
         android:versionName="1.0">

         <receiver android:name=".MyReceiver" android:enabled="true" android:exported="false">
             <intent-filter>
                 <action android:name="android.intent.action.BOOT_COMPLETED" />
             </intent-filter>
         </receiver>

         <service android:name="com.jessefarebro.mqtt.MqttService" android:exported="false" />

         <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
</manifest>
```


License
-------

    Copyright 2013 Jesse Farebrother
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
