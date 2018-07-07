package com.jungle.util;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * 判断字符串为空。
     *
     * @param str 待检测字符串
     * @return true-空；false-不为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 判断字符串不为空。
     *
     * @param str 待检测字符串
     * @return true-不为空；false-为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
