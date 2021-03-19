package com.ausclouds.bdbsec.tjt;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Redis 布隆过滤器
 * 引入依赖：
 * <dependency>
 *     <groupId>com.google.guava</groupId>
 *     <artifactId>guava</artifactId>
 *     <version>22.0</version>
 * </dependency>
 */
public class BloomFilterDemon {
    private static char ABC = 'a';



    // initial_size: 表示预计放入的元素数量，当实际数量超出这个数值时，误判率会上升
    private static long initial_size = 1000000;

    // error_rate: 错误率
    private static double error_rate = 0.0001;

    public static void main(String[] args) {
        BloomFilter<String> bloomFilter =
                BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), initial_size, error_rate);
        bloomFilter.put("what");
        boolean isContain = bloomFilter.mightContain("what");

        Set<Character> set = new HashSet<Character>();
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        

    }


}
