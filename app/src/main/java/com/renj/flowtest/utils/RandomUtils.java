package com.renj.flowtest.utils;

import java.util.Random;

/**
 * ======================================================================
 * <p>
 * 作者：Renj
 * <p>
 * 创建时间：2020-10-10   14:57
 * <p>
 * 描述：随机数工具类
 * <p>
 * 修订历史：
 * <p>
 * ======================================================================
 */
public class RandomUtils {

    private static Random random = new Random();

    public static int randomInt() {
        return random.nextInt();
    }

    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }
}
