package com.ausclouds.bdbsec.tjt;


import java.util.Scanner;

/**
 * @author tjt
 * @time 2020-9-6
 * @desc 进制转换：
 * 十六进制数采用表示16个符号来表示0~15，其中0~9就用0~9来表zhi示，形式相同，
 * 但10~15却必须用1个符号而不是两个符号来表示，于是就用A~F（小写a~f也行）来表示10~15了：
 * 10 -> A，11 -> B，12 -> C，13 -> D，14 -> E，15 -> F。
 * 将十六进制数转换成十进制数的话，就将每一位的十六进制数码（0~9和A~F）先转换成十进制数码（0~15），
 * 再将其乘以该数码所在位置的位权，并将其相加就可以。例如：
 * 十六进制数0x2AD5，前缀0x是用来表示十六进制数的
 * 0x2AD5 = 2*16^3 + 10*16^2 + 13*16^1 + 5*16^0 = 8192 + 2560 + 208 + 5 = 10965。
 */
public class Main {

    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";
    private static final String F = "F";

    /**
     * changeBinary
     * @param input
     * @return
     */
    private static int changeBinary(String input) {
        int length = input.length();
        // 输入格式为：0xA，剔除"0x"所占用的空间: length -2。
        int num = length - 2;
        int tenBinaryResult = 0;
        int[] binaryArray = new int[num];
        for (int i = 0; i < num; i++) {
            // 去掉"0x"两个字符，直接截取"A"。
            String singleBinary = input.substring(i + 2, i + 3);
            switch (singleBinary) {
                // 匹配十六进制
                case A:
                    binaryArray[i] = 10;
                    break;
                case B:
                    binaryArray[i] = 11;
                    break;
                case C:
                    binaryArray[i] = 12;
                    break;
                case D:
                    binaryArray[i] = 13;
                    break;
                case E:
                    binaryArray[i] = 14;
                    break;
                case F:
                    binaryArray[i] = 15;
                    break;
                default:
                    binaryArray[i] = Integer.parseInt(singleBinary);
            }
            // 转换为十进制 : 乘以该数码所在位置的位权
            double singleResult = binaryArray[i] * Math.pow(16, num - i - 1);
            // 位权相加
            tenBinaryResult += singleResult;
        }
        return tenBinaryResult;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            int output = changeBinary(input);
            System.out.println(output);
        }
    }

}