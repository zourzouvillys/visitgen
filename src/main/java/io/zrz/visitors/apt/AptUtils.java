package io.zrz.visitors.apt;

import com.google.common.base.Strings;

public class AptUtils {

  public static String defaultString(String in, String defaultValue) {
    if (Strings.isNullOrEmpty(in)) {
      return defaultValue;
    }
    return in;
  }

}
