package com.ausclouds.bdbsec.tjt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tjt
 * @time 2020-09-02
 * Java 排序算法
 * 当前要完成的事情：
 * 1、信息安全-漫画-投稿；
 * 2、复习面试题；
 * 3、刷机试题；
 * 4、写博客：如何顶住千万级并发、扫描物体如何判断出是什么东西？
 */
public class JavaSortAlgorithm {


    /**
     * 直接插入排序：
     * 首先，设定插入次数，即循环次数，for(int i=1;i<length;i++)，第1个数的那次不用插入，会有元素间的比较排序
     * 然后，设定插入数和得到已经排好序列的最后一个数的位数：insertNum和j=i-1
     * 从最后一个数开始向前循环，如果插入数小于当前数，就将当前数向后移动一位
     * 将当前数放置到空着的位置，即j+1。
     *
     * @param array
     */
    private static void justInsertSort(int[] array) {
        System.out.println("***********直接插入排序*************");
        int length = array.length;
        // insertNum 为要插入的数
        int insertNum;
        for (int i = 1; i < length; i++) {
            insertNum = array[i];
            System.out.println("insertNum: " + insertNum);
            // 已经排序好的序列元素个数
            int j = i - 1;
            System.out.println(Arrays.toString(array));
            // 序列从后到前循环，将大于insertNum 的数向后移动一格
            while (j >= 0 && array[j] > insertNum) {
                // 元素移动一格
                array[j + 1] = array[j];
                // j-- 之后继续于之前的比较，从后往前
                j--;
            }
            // 将需要插入的数放在要插入的位置。
            array[j + 1] = insertNum;
        }
    }

    /**
     * 希尔排序：
     * 首先，确定分的组数，然后对组中元素进行插入排序
     * 接下来，将length/2，重复1,2步，直到length=0 为止。
     *
     * @param array
     */
    private static void xiErSort(int[] array) {
        System.out.println("***********希尔排序*************");
        int length = array.length;
        while (length != 0) {
            length = length / 2;
            // 分的数组
            for (int x = 0; x < length; x++) {
                // 组中的元素，从第二个数开始
                for (int i = x + length; i < array.length; i += length) {
                    // j为有序序列最后一位的位数
                    int j = i - length;
                    // temp 为要插入的元素
                    int temp = array[i];
                    // 从后往前遍历
                    for (; j >= 0 && temp < array[j]; j -= length) {
                        // 向后移动length 位
                        array[j + length] = array[j];
                    }
                    array[j + length] = temp;
                }
            }
        }
    }

    /**
     * 简单选择排序：
     * 首先确定循环次数，并且记住当前数字和当前位置。
     * 将当前位置后面所有的数与当前数字进行对比，小数赋值给key，并记住小数的位置。
     * 比对完成后，将最小的值与第一个数的值交换。
     * 重复2、3步。
     *
     * @param array
     */
    private static void simpleSelectSort(int[] array) {
        System.out.println("***********简单选择排序*************");
        int length = array.length;
        // 循环次数
        for (int i = 0; i < length; i++) {
            int key = array[i];
            int position = i;
            // 选出最小的值和位置
            for (int j = i + 1; j < length; j++) {
                if (array[j] < key) {
                    key = array[j];
                    position = j;
                }
            }
            // 交换位置
            array[position] = array[i];
            array[i] = key;
        }
    }


    /**
     * 堆排序：
     * 将序列构建成大顶堆；
     * 将根节点与最后一个节点交换，然后断开最后一个节点；
     * 重复第一、二步，直到所有节点断开。
     *
     * @param array
     */
    public static void heapSort(int[] array) {
        System.out.println("***********堆排序*************");
        System.out.println("开始排序：");
        int arrayLength = array.length;
        // 循环建堆
        for (int i = 0; i < arrayLength - 1; i++) {
            // 建堆
            buildMaxHeap(array, arrayLength - 1 - i);
            // 交换堆顶和最后一个元素
            swap(array, 0, arrayLength - 1 - i);
            System.out.println(Arrays.toString(array));
        }
    }

    /**
     * 交换堆顶和最后一个元素
     *
     * @param data
     * @param i
     * @param j
     */
    private static void swap(int[] data, int i, int j) {
        int tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }

    /**
     * 建堆：对data数组从0到lastIndex建大顶堆
     *
     * @param data
     * @param lastIndex
     */
    private static void buildMaxHeap(int[] data, int lastIndex) {
        // 从lastIndex处节点（最后一个节点）的父节点开始
        for (int i = (lastIndex - 1) / 2; i >= 0; i--) {
            // k保存正在判断的节点
            int k = i;
            // 如果当前k节点的子节点存在
            while (k * 2 + 1 <= lastIndex) {
                // k节点的左子节点的索引
                int biggerIndex = 2 * k + 1;
                //如果biggerIndex小于lastIndex，即biggerIndex+1代表的k节点的右子节点存在
                if (biggerIndex < lastIndex) {
                    // 若果右子节点的值较大
                    if (data[biggerIndex] < data[biggerIndex + 1]) {
                        // biggerIndex总是记录较大子节点的索引
                        biggerIndex++;
                    }
                }
                // 如果k节点的值小于其较大的子节点的值
                if (data[k] < data[biggerIndex]) {
                    // 交换他们
                    swap(data, k, biggerIndex);
                    // 将biggerIndex赋予k，开始while循环的下一次循环，重新保证k节点的值大于其左右子节点的值
                    k = biggerIndex;
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 冒泡排序：
     * 设置循环次数。
     * 设置开始比较的位数，和结束的位数。
     * 两两比较，将最小的放到前面去。
     * 重复2、3步，直到循环次数完毕。
     *
     * @param array
     */
    private static void bubbleSort(int[] array) {
        int length = array.length;
        int temp;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }

    /**
     * 快速排序：
     * 选择第一个数为p，小于p的数放在左边，大于p的数放在右边；
     * 递归的将p左边和右边的数都按照第一步进行，直到不能递归。
     *
     * @param array
     * @param start
     * @param end
     */
    private static void quicklySort(int[] array, int start, int end) {
        System.out.println("***********快速排序*************");
        if (start < end) {
            // 选定的基准值（第一个数值作为基准值）
            int base = array[start];
            // 记录临时中间值
            int temp;
            int i = start, j = end;
            do {
                while ((array[i] < base) && (i < end)) {
                    i++;
                }
                while ((array[j] > base) && (j > start)) {
                    j--;
                }
                if (i <= j) {
                    temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                    i++;
                    j--;
                }
            } while (i <= j);
            if (start < j) {
                quicklySort(array, start, j);
            }
            if (end > i) {
                quicklySort(array, i, end);
            }
        }
    }

    /**
     * 归并排序：
     * 选择相邻两个数组成一个有序序列；
     * 选择相邻的两个有序序列组成一个有序序列；
     * 重复第二步，直到全部组成一个有序序列。
     *
     * @param arr
     * @param l
     * @param r
     */
    private static void mergeSort(int[] arr, int l, int r) {
        System.out.println("***********归并排序*************");
        if (l < r) {
            int q = (l + r) / 2;
            mergeSort(arr, l, q);
            mergeSort(arr, q + 1, r);
            merge(arr, l, q, r);
        }
    }

    /**
     * @param arr 排序数组
     * @param l   数组最左边下标
     * @param q   数组中间位置下标
     * @param r   数组最右位置下标
     */
    private static void merge(int[] arr, int l, int q, int r) {
        /**
         * 因为每次切割后左边下标都是（l,q），右边数组的下标是(q+1,r)
         * 所以左边数组的元素个数就是q - l + 1
         * 右边的数组元素个数就是r - q
         */
        // 切割后左边数组的数据长度
        final int n1 = q - l + 1;
        // 切割后右边数组的数据长度
        final int n2 = r - q;
        /**创建两个新数组将切割后的数组分别放进去，长度加1是为了放置无穷大的数据标志位**/
        // 加一操作是增加无穷大标志位
        final int[] left = new int[n1 + 1];
        // 加一操作是增加无穷大标志位
        final int[] right = new int[n2 + 1];
        //两个循环将数据添加至新数组中
        /**左边的数组下标是从l到q**/
        /**遍历左边的数组*/
        for (int i = 0; i < n1; i++) {
            left[i] = arr[l + i];
        }
        for (int i = 0; i < n2; i++) {
            right[i] = arr[q + 1 + i];
        }

        // 将最大的正整数放在两个新数组的最后一位
        left[n1] = Integer.MAX_VALUE;
        right[n2] = Integer.MAX_VALUE;

        int i = 0, j = 0;
        // 将小的放在前面
        for (int k = l; k <= r; k++) {
            if (left[i] <= right[j]) {
                arr[k] = left[i];
                i = i + 1;
            } else {
                arr[k] = right[j];
                j = j + 1;
            }
        }
    }

    /**
     * 基数排序：
     * 将所有的数的个位数取出，按照个位数进行排序，构成一个序列；
     * 将新构成的所有的数的十位数取出，按照十位数进行排序，构成一个序列。
     *
     * @param array 排序队列中存在负数除外
     */
    private static void baseSort(int[] array) {
        System.out.println("***********基数排序*************");
        // 首先确定排序的趟数;
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        int time = 0;
        // 判断位数;
        while (max > 0) {
            max /= 10;
            time++;
        }
        // 建立10个队列;
        List<ArrayList> queue = new ArrayList<ArrayList>();
        for (int i = 0; i < 10; i++) {
            ArrayList<Integer> queue1 = new ArrayList<Integer>();
            queue.add(queue1);
        }
        // 进行time次分配和收集;
        for (int i = 0; i < time; i++) {
            //分配数组元素;
            for (int j = 0; j < array.length; j++) {
                // 得到数字的第time+1位数;
                int x = array[j] % (int) Math.pow(10, i + 1) / (int) Math.pow(10, i);
                ArrayList<Integer> queue2 = queue.get(x);
                queue2.add(array[j]);
                queue.set(x, queue2);
            }
            // 元素计数器;
            int count = 0;
            // 收集队列元素;
            for (int k = 0; k < 10; k++) {
                while (queue.get(k).size() > 0) {
                    ArrayList<Integer> queue3 = queue.get(k);
                    array[count] = queue3.get(0);
                    queue3.remove(0);
                    count++;
                }
            }
        }
    }

    /**
     * 测试排序算法
     *
     * @param args
     */
    public static void main(String[] args) {
        int[] array = new int[]{32, 43, 0, 1314, 23, 4, 12, 5, 520};
        System.out.println("array's origin sort: " + Arrays.toString(array));
        //justInsertSort(array);
        //simpleSelectSort(array);
        //xiErSort(array);
        //heapSort(array);
        //bubbleSort(array);
        //quicklySort(array, 0, array.length - 1);
        //mergeSort(array, 0, array.length - 1);
        baseSort(array);
        System.out.println("array's sort after use algorithm: " + Arrays.toString(array));
        int[] array2 = new int[]{32, 43, 0, 1314, 23, -4, 12, 5, 520};
        baseSort(array2);

    }



}
