package com.renj.flowtest.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-29   15:05
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class DataUtils {
    private static String[] strings1 = {"测试", "好", "FlowLayout", "作者", "长短不一", "哈哈哈哈哈哈哈", "我是测试数据"
            , "就这样", "姓名", "张三三", "大家好啊，我是谁", "你好吗", "商品价格", "名称", "测试测试一下", "111111"};

    private static String[] strings2 = {"测", "Flow", "", "呵呵呵", "333"
            , "就", "姓名", "", "好啊，谁", "吗", "qws", "名称", "一下", "222"};

    public static List<String> getDataList(int dataCount) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dataCount; i++) {
            result.add(strings1[RandomUtils.randomInt(strings1.length)] + strings2[RandomUtils.randomInt(strings2.length)]);
        }
        return result;
    }
}
