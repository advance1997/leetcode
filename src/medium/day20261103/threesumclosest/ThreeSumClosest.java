package medium.day20261103.threesumclosest;

import java.util.Arrays;

/**
 * LeetCode 16：最接近的三数之和（3Sum Closest），难度：中等。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/3sum-closest/">最接近的三数之和</a>。</p>
 * <p>给定整数数组 nums 和整数 target，从三个不同下标选取元素，使其和与 target
 * 的绝对差最小，返回这个和，不返回下标或三元组。原题保证每组输入恰好有一个解。
 * 不同下标上的相同数值可以同时使用。</p>
 * <ul>
 *   <li>{@code 3 <= nums.length <= 1000}。</li>
 *   <li>{@code -1000 <= nums[i] <= 1000}。</li>
 *   <li>{@code -10000 <= target <= 10000}。</li>
 * </ul>
 * <pre>
 * 输入：nums = [-1, 2, 1, -4], target = 1
 * 输出：2
 * 解释：-1 + 2 + 1 = 2，与目标的绝对差为 1，是最接近的和。
 *
 * 输入：nums = [0, 0, 0], target = 1
 * 输出：0
 * 解释：唯一三元组的和为 0，即使不能精确命中目标也要返回实际可构成的和。
 * </pre>
 * <p>暴力枚举时间 O(n³)、额外空间 O(1)；排序副本加双指针时间 O(n²)、
 * 额外空间 O(n)，n 为数组长度。两种方法均不修改原数组。</p>
 * <p>仓库对 null 或不足三个元素的数组抛出异常；若存在多个不同的等距最优和，
 * 额外约定返回较小的和。原题唯一解输入不受这项扩展影响。
 * 不校验数值范围、长度上限或唯一解条件。原题范围内三数和处于 [-3000, 3000]，
 * 与目标的绝对差不超过 13000，int 运算安全。实现使用 Java 8 已有语法和 API。</p>
 */
public final class ThreeSumClosest {

    /**
     * 禁止实例化仅提供静态算法方法的工具类。
     */
    private ThreeSumClosest() {
    }

    /**
     * 枚举所有不同下标的三元组，维护与目标最接近的和。
     *
     * <p>最坏时间 O(n³)，额外空间 O(1)；精确命中时可以立即结束。</p>
     *
     * @param nums 满足原题数值范围的数组，不会被修改
     * @param target 满足原题范围的目标值
     * @return 最接近目标的可构成三数和；不同和等距时返回较小的和
     * @throws IllegalArgumentException 如果数组为 null 或不足三个元素
     */
    public static int threeSumClosestBruteForce(int[] nums, int target) {
        validateInput(nums);

        // 用真实候选初始化，保证返回值一定能够由三个不同位置构成。
        int closestSum = nums[0] + nums[1] + nums[2];
        for (int first = 0; first < nums.length - 2; first++) {
            for (int second = first + 1; second < nums.length - 1; second++) {
                for (int third = second + 1; third < nums.length; third++) {
                    int currentSum = nums[first] + nums[second] + nums[third];
                    if (currentSum == target) {
                        return currentSum;
                    }
                    if (isBetter(currentSum, closestSum, target)) {
                        closestSum = currentSum;
                    }
                }
            }
        }

        return closestSum;
    }

    /**
     * 固定排序后的首元素，用相向双指针查找最接近目标的三数和。
     *
     * <p>先评估当前候选，再根据和的大小排除不可能更接近的左端或右端。
     * 最坏时间 O(n²)，复制数组带来 O(n) 辅助空间。</p>
     *
     * @param nums 满足原题数值范围的数组，不会被修改
     * @param target 满足原题范围的目标值
     * @return 最接近目标的可构成三数和；不同和等距时返回较小的和
     * @throws IllegalArgumentException 如果数组为 null 或不足三个元素
     */
    public static int threeSumClosest(int[] nums, int target) {
        validateInput(nums);

        // 排序产生单调性；副本用于保留调用者数组的原顺序。
        int[] sorted = nums.clone();
        Arrays.sort(sorted);
        int closestSum = sorted[0] + sorted[1] + sorted[2];

        for (int first = 0; first < sorted.length - 2; first++) {
            int left = first + 1;
            int right = sorted.length - 1;
            while (left < right) {
                int currentSum = sorted[first] + sorted[left] + sorted[right];
                if (currentSum == target) {
                    // 绝对差为零已经达到理论下界，不必继续扫描。
                    return currentSum;
                }
                if (isBetter(currentSum, closestSum, target)) {
                    closestSum = currentSum;
                }

                if (currentSum < target) {
                    // 当前左端搭配更小的右端，只会离目标更远，安全排除左端。
                    left++;
                } else {
                    // 当前右端搭配更大的左端，只会离目标更远，安全排除右端。
                    right--;
                }
            }
        }

        return closestSum;
    }

    /**
     * 按绝对差、随后按数值大小比较候选，统一两种算法的等距行为。
     *
     * @param candidate 当前可构成的三数和
     * @param best 已记录的可构成三数和
     * @param target 目标值，三个参数均应遵循类文档说明的数值范围
     * @return 候选绝对差更小，或等距且候选和更小时返回 true
     */
    private static boolean isBetter(int candidate, int best, int target) {
        int candidateDistance = Math.abs(candidate - target);
        int bestDistance = Math.abs(best - target);
        return candidateDistance < bestDistance
                || (candidateDistance == bestDistance && candidate < best);
    }

    /**
     * 校验选出三个不同位置所需的基本条件。
     *
     * @param nums 待校验数组，其他原题约束由调用方保证
     * @throws IllegalArgumentException 如果数组为 null 或不足三个元素
     */
    private static void validateInput(int[] nums) {
        if (nums == null || nums.length < 3) {
            throw new IllegalArgumentException("数组不能为 null 且至少包含三个元素");
        }
    }
}
