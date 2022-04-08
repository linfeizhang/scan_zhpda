
import 'dart:async';

import 'package:flutter/services.dart';

class ScanZhpda {
  static const MethodChannel _channel = MethodChannel('scan_zhpda');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
