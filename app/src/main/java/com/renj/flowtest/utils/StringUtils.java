package com.renj.flowtest.utils;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-09   11:03
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class StringUtils {
    public static boolean isEmpty(String content) {
        if (content == null || content.length() == 0 || "null".equals(content)) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String content) {
        return !isEmpty(content);
    }
}
