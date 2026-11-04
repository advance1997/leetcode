package medium.day20261103.foursum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LeetCode 18：四数之和（4Sum），难度：中等。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/4sum/">四数之和</a>。</p>
 * <p>给定数组 nums 和目标 target，返回所有和等于 target 且不重复的数值四元组。
 * 每组四个元素必须来自不同下标，数值可以相同；答案顺序不限，返回数值而非下标。</p>
 * <ul>
 *   <li>{@code 1 <= nums.length <= 200}。</li>
 *   <li>{@code -1000000000 <= nums[i], target <= 1000000000}。</li>
 * </ul>
 * <pre>
 * 输入：nums = [1, 0, -1, 0, -2, 2], target = 0
 * 输出：[[-2, -1, 1, 2], [-2, 0, 0, 2], [-1, 0, 0, 1]]
 * 解释：同值四元组只输出一次，两个零来自不同位置。
 *
 * 输入：nums = [2, 2, 2, 2, 2], target = 8
 * 输出：[[2, 2, 2, 2]]
 * 解释：不同下标组合可以产生相同的数值组合，只保留一份。
 * </pre>
 * <p>本类排序副本后提供四重枚举与固定两数加双指针两种实现。
 * n 为输入长度，k 为答案数；最坏时间分别 O(n⁴)、O(n³)，
 * 辅助空间均 O(n)，输出空间 O(k)，不修改原数组。</p>
 * <p>四数和可达到正负 40 亿，超出 int 范围，所有求和及剪枝运算从第一个
 * 操作数开始使用 long。输入元素和返回四元组中的单个元素仍使用 Integer。</p>
 * <p>长度不足四时返回空列表；仓库额外允许空数组，对 null 抛出异常。
 * 不逐项校验数值范围及长度上限。实现使用 Java 8 已有语法和 API。</p>
 */
public final class FourSum {

    /**
     * 禁止实例化静态算法工具类。
     */
    private FourSum() {
    }

    /**
     * 排序副本后枚举四个递增下标，各层跳过本层重复值。
     *
     * <p>最坏时间 O(n⁴)，辅助空间 O(n)，输出空间 O(k)。</p>
     *
     * @param nums 遵循原题范围的数组，不会被修改
     * @param target 目标和
     * @return 不重复且内部非递减的四元组；不足四个元素或无解时为空
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static List<List<Integer>> fourSumBruteForce(int[] nums, int target) {
        validateInput(nums);
        List<List<Integer>> results = new ArrayList<>();
        if (nums.length < 4) {
            return results;
        }

        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        for (int first = 0; first < sorted.length - 3; first++) {
            if (first > 0 && sorted[first] == sorted[first - 1]) {
                continue;
            }
            for (int second = first + 1; second < sorted.length - 2; second++) {
                // 与本层起点比较，允许两个固定位置具有相同数值。
                if (second > first + 1 && sorted[second] == sorted[second - 1]) {
                    continue;
                }
                for (int third = second + 1; third < sorted.length - 1; third++) {
                    if (third > second + 1 && sorted[third] == sorted[third - 1]) {
                        continue;
                    }
                    for (int fourth = third + 1; fourth < sorted.length; fourth++) {
                        if (fourth > third + 1 && sorted[fourth] == sorted[fourth - 1]) {
                            continue;
                        }
                        // 加法发生前提升为 long，不能等 int 已溢出后再转换。
                        long sum = (long) sorted[first] + sorted[second]
                                + sorted[third] + sorted[fourth];
                        if (sum == target) {
                            results.add(Arrays.asList(sorted[first], sorted[second],
                                    sorted[third], sorted[fourth]));
                        }
                    }
                }
            }
        }

        return results;
    }

    /**
     * 固定两个数后用相向双指针搜索，结合上下界剪枝与分层去重。
     *
     * <p>最小可构成和已超过目标时结束当前枚举层，最大和仍不足时跳过当前候选。
     * 最坏时间 O(n³)，辅助空间 O(n)，输出空间 O(k)。</p>
     *
     * @param nums 遵循原题范围的数组，不会被修改
     * @param target 目标和
     * @return 不重复且内部非递减的四元组；不足四个元素或无解时为空
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static List<List<Integer>> fourSum(int[] nums, int target) {
        validateInput(nums);
        List<List<Integer>> results = new ArrayList<>();
        if (nums.length < 4) {
            return results;
        }

        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        int length = sorted.length;
        for (int first = 0; first < length - 3; first++) {
            if (first > 0 && sorted[first] == sorted[first - 1]) {
                continue;
            }

            long minimum = (long) sorted[first] + sorted[first + 1]
                    + sorted[first + 2] + sorted[first + 3];
            if (minimum > target) {
                // 后续首数及其最小搭配只会更大，因此结束整个首数枚举。
                break;
            }
            long maximum = (long) sorted[first] + sorted[length - 3]
                    + sorted[length - 2] + sorted[length - 1];
            if (maximum < target) {
                // 当前首数太小，但后续更大的首数仍可能有解。
                continue;
            }

            for (int second = first + 1; second < length - 2; second++) {
                if (second > first + 1 && sorted[second] == sorted[second - 1]) {
                    continue;
                }

                long pairMinimum = (long) sorted[first] + sorted[second]
                        + sorted[second + 1] + sorted[second + 2];
                if (pairMinimum > target) {
                    break;
                }
                long pairMaximum = (long) sorted[first] + sorted[second]
                        + sorted[length - 2] + sorted[length - 1];
                if (pairMaximum < target) {
                    continue;
                }

                int left = second + 1;
                int right = length - 1;
                while (left < right) {
                    long sum = (long) sorted[first] + sorted[second]
                            + sorted[left] + sorted[right];
                    if (sum < target) {
                        // 当前左端配最大右端仍不足，换更小右端也无解。
                        left++;
                    } else if (sum > target) {
                        // 当前右端配最小左端仍超标，换更大左端也无解。
                        right--;
                    } else {
                        results.add(Arrays.asList(sorted[first], sorted[second],
                                sorted[left], sorted[right]));

                        // 固定前两数后，这一对数值已经输出，跳过同值避免重复答案。
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
        }

        return results;
    }

    /**
     * 校验数组引用，长度不足四按无解处理。
     *
     * @param nums 待校验数组，其他原题约束由调用方保证
     * @throws IllegalArgumentException 如果数组为 null
     */
    private static void validateInput(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("数组不能为 null");
        }
    }
}
