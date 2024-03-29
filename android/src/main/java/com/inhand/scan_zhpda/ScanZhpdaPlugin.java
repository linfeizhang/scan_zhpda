package com.inhand.scan_zhpda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.*;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** ScanZhpdaPlugin */
public class ScanZhpdaPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private EventChannel eventChannel;
  private Context applicationContext;

  /// 广播名 ACTION
  private static final String ACTION_DATA_CODE_RECEIVED =
          "nlscan.action.SCANNER_RESULT";

  // flutter app里面要监听数据流,先不管
  private static final String CHARGING_CHANNEL = "scan_zhpda/send";

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "scan_zhpda");
    channel.setMethodCallHandler(this);
    eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(),CHARGING_CHANNEL);
    eventChannel.setStreamHandler(new EventChannel.StreamHandler() {

      private BroadcastReceiver chargingStateChangeReceiver;

      @Override
      public void onListen(Object arguments,@NonNull  EventChannel.EventSink events) {
        chargingStateChangeReceiver = createChargingStateChangeReceiver(events);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DATA_CODE_RECEIVED);
        applicationContext.registerReceiver(
                chargingStateChangeReceiver, filter);
      }

      @Override
      public void onCancel(Object arguments) {
        applicationContext.unregisterReceiver(chargingStateChangeReceiver);
        chargingStateChangeReceiver = null;
      }
    });

    applicationContext = flutterPluginBinding.getApplicationContext();
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "scan_zhpda");
    channel.setMethodCallHandler(new ScanZhpdaPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private BroadcastReceiver createChargingStateChangeReceiver(final EventChannel.EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String barocode = intent.getStringExtra("SCAN_BARCODE1");

        Map<String,Object> resultMap = new HashMap<>();
        if (barocode != null) {
          //成功
          resultMap.put("result",barocode);

          // Map 转 json
          JSONObject json = new JSONObject(resultMap);
          events.success(json.toString());
        } else {
          //成功
          resultMap.put("error","请求超时，请重新扫描");

          // Map 转 json
          JSONObject json = new JSONObject(resultMap);
          events.success(json.toString());
        }
      }
    };
  }
}
