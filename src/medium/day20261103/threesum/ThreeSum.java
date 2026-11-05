package medium.day20261103.threesum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LeetCode 15：三数之和（3Sum），难度：中等。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/3sum/">三数之和</a>。</p>
 * <p>给定整数数组 nums，返回所有元素之和为零且不重复的三元组。
 * 每组三个元素必须来自不同下标，但数值可以相同。返回的是数值而非下标，
 * 三元组内部和答案之间的排列顺序不限，不能重复输出相同的数值组合。</p>
 * <ul>
 *   <li>{@code 3 <= nums.length <= 3000}。</li>
 *   <li>{@code -100000 <= nums[i] <= 100000}。</li>
 * </ul>
 * <pre>
 * 输入：[-1, 0, 1, 2, -1, -4]
 * 输出：[[-1, -1, 2], [-1, 0, 1]]
 * 解释：两个不同位置的 -1 可以同时使用；相同数值组合只输出一次。
 *
 * 输入：[0, 1, 1]
 * 输出：[]，没有和为零的组合。
 *
 * 输入：[0, 0, 0]
 * 输出：[[0, 0, 0]]，使用三个不同位置的零。
 * </pre>
 * <p>本类在排序副本后提供两种解法：三重枚举最坏时间 O(n³)，
 * 固定首元素加双指针最坏时间 O(n²)。n 为数组长度，k 为答案数；
 * 两者均使用 O(n) 辅助空间保存副本，输出占 O(k)，不修改调用者数组。</p>
 * <p>三个数相加位于 [-300000, 300000]，原题范围内 int 运算安全。
 * 仓库对 null 抛出异常，长度不足三时返回空列表；不校验数值和长度上限。
 * 实现使用 Java 8 已有语法和 API，不保证超出原题范围的任意 int 输入。</p>
 */
public final class ThreeSum {

    /**
     * 禁止实例化静态算法工具类。
     */
    private ThreeSum() {
    }

    /**
     * 在排序副本上枚举三个不同下标，并在每层跳过同值候选。
     *
     * <p>跳过的是同一选择层的重复值，不会删除数组中的重复元素。
     * 最坏时间 O(n³)，辅助空间 O(n)，输出空间 O(k)。</p>
     *
     * @param nums 遵循原题数值范围的数组，不会被修改
     * @return 不重复的零和三元组，每组三个数按非递减顺序排列；无解或长度不足三时为空
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static List<List<Integer>> threeSumBruteForce(int[] nums) {
        validateInput(nums);
        List<List<Integer>> results = new ArrayList<>();
        if (nums.length < 3) {
            return results;
        }

        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        for (int first = 0; first < sorted.length - 2; first++) {
            if (first > 0 && sorted[first] == sorted[first - 1]) {
                continue;
            }

            for (int second = first + 1; second < sorted.length - 1; second++) {
                // 只跳过当前第一元素下的重复第二元素，保留合法的相同数值配对。
                if (second > first + 1 && sorted[second] == sorted[second - 1]) {
                    continue;
                }

                for (int third = second + 1; third < sorted.length; third++) {
                    if (third > second + 1 && sorted[third] == sorted[third - 1]) {
                        continue;
                    }
                    int sum = sorted[first] + sorted[second] + sorted[third];
                    if (sum == 0) {
                        results.add(Arrays.asList(sorted[first], sorted[second], sorted[third]));
                    }
                }
            }
        }

        return results;
    }

    /**
     * 排序后固定第一个数，在其右侧用相向双指针寻找剩余两个数。
     *
     * <p>和偏小时排除当前左端，和偏大时排除当前右端；找到答案后跳过两端同值。
     * 最坏时间 O(n²)，辅助空间 O(n)，输出空间 O(k)，保留输入数组的原顺序。</p>
     *
     * @param nums 遵循原题数值范围的数组，不会被修改
     * @return 不重复的零和三元组，每组三个数按非递减顺序排列；无解或长度不足三时为空
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static List<List<Integer>> threeSum(int[] nums) {
        validateInput(nums);
        List<List<Integer>> results = new ArrayList<>();
        if (nums.length < 3) {
            return results;
        }

        // 返回数值而非原始下标，允许排序；复制是为了保留调用者的输入。
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        for (int first = 0; first < sorted.length - 2; first++) {
            // 最小候选已经为正，后面的任意三数之和都不可能为零。
            if (sorted[first] > 0) {
                break;
            }
            if (first > 0 && sorted[first] == sorted[first - 1]) {
                continue;
            }

            int left = first + 1;
            int right = sorted.length - 1;
            while (left < right) {
                int sum = sorted[first] + sorted[left] + sorted[right];
                if (sum < 0) {
                    // 即使搭配当前最大的右端仍太小，当前左端不可能产生答案。
                    left++;
                } else if (sum > 0) {
                    // 即使搭配当前最小的左端仍太大，当前右端不可能产生答案。
                    right--;
                } else {
                    results.add(Arrays.asList(sorted[first], sorted[left], sorted[right]));

                    // 同一个首元素下，这一对数值已输出；跳过同值避免重复三元组。
                    int leftValue = sorted[left];
                    int rightValue = sorted[right];
                    while (left < right && sorted[left] == leftValue) {
                        left++;
                    }
                    while (left < right && sorted[right] == rightValue) {
                        right--;
                    }
                }
            }
        }

        return results;
    }

    /**
     * 校验数组引用，允许长度不足三的数组作为无解输入。
     *
     * @param nums 待校验的数组，其他原题范围由调用方保证
     * @throws IllegalArgumentException 如果数组为 null
     */
    private static void validateInput(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("数组不能为 null");
        }
    }
}
