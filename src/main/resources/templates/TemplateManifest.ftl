<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
  package="${settings.getTopLevelPacakge()}" android:versionCode="1" android:versionName="1.0">

  <application android:icon="@drawable/icon" android:label="@string/${settings.getActivityLabelKey()}">
    <activity android:name=".${settings.getActivityName()}">
      <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
      </intent-filter>
    </activity>
  </application>

</manifest>

