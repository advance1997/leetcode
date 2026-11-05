package easy.day20261103.twosum;

import java.util.HashMap;
import java.util.Map;

/**
 * LeetCode 1：两数之和（Two Sum），难度：简单。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/two-sum/">两数之和</a>。</p>
 *
 * <p>给定整数数组 {@code nums} 和整数目标值 {@code target}，找出数组中
 * 和为目标值的两个整数，返回它们的数组下标。每种合法输入恰好有一个有效答案，
 * 同一个元素不能重复使用，返回下标的顺序不限。不同位置可以具有相同数值。</p>
 *
 * <p>原题约束：</p>
 * <ul>
 *   <li>{@code 2 <= nums.length <= 10000}。</li>
 *   <li>{@code -1000000000 <= nums[i] <= 1000000000}。</li>
 *   <li>{@code -1000000000 <= target <= 1000000000}。</li>
 *   <li>只会存在一个有效答案。</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * 输入：nums = [2, 7, 11, 15], target = 9
 * 输出：[0, 1]
 * 解释：nums[0] + nums[1] = 2 + 7 = 9。
 *
 * 输入：nums = [3, 2, 4], target = 6
 * 输出：[1, 2]
 * 解释：只能使用不同位置的元素，不能让下标 0 的数字 3 与自己配对。
 *
 * 输入：nums = [3, 3], target = 6
 * 输出：[0, 1]
 * 解释：两个 3 来自不同位置，允许配对。
 * </pre>
 *
 * <p>本类提供两种解法，n 表示数组长度：</p>
 * <ul>
 *   <li>暴力枚举：检查不同下标组成的数对，最坏时间 O(n²)，额外空间 O(1)。</li>
 *   <li>单次遍历与哈希表：查找之前出现的补数，平均时间 O(n)，额外空间 O(n)。
 *       平均时间以哈希查找和插入平均为 O(1) 为前提。</li>
 * </ul>
 *
 * <p>两种实现均不修改输入数组，空间分析不计固定长度的返回数组。
 * 本仓库额外校验空引用和数组最小长度，无解时抛出异常；不逐项校验原题数值范围
 * 或答案唯一性。原题范围内的求和与求补数不会超出 int 范围，超出约束的任意
 * int 输入不在本接口保证之内。代码使用 Java 8 可用的 API。</p>
 */
public final class TwoSum {

    /**
     * 哈希表负载因子，用于估算容纳全部输入元素时的初始容量。
     */
    private static final float HASH_MAP_LOAD_FACTOR = 0.75F;

    /**
     * 禁止实例化仅提供静态算法方法的工具类。
     */
    private TwoSum() {
    }

    /**
     * 使用暴力枚举寻找和为目标值的两个元素。
     *
     * <p>固定第一个下标，第二个下标只遍历其后的位置，
     * 保证所有不同下标的组合恰好检查一次。最坏时间 O(n²)，额外空间 O(1)。</p>
     *
     * @param nums 遵循原题约束的整数数组，方法不会修改其内容
     * @param target 遵循原题范围的目标值
     * @return 两个不同元素的下标，较小的下标在前
     * @throws IllegalArgumentException 如果数组为 null、元素不足两个或不存在有效答案
     */
    public static int[] twoSumBruteForce(int[] nums, int target) {
        validateInput(nums);

        // 最后一个元素后面没有可配对的位置，无需作为第一个候选元素。
        for (int firstIndex = 0; firstIndex < nums.length - 1; firstIndex++) {
            // 从下一位置开始，既不复用当前元素，也不重复检查反向数对。
            for (int secondIndex = firstIndex + 1;
                    secondIndex < nums.length;
                    secondIndex++) {
                // 原题数值范围保证两个元素相加不会溢出 int。
                if (nums[firstIndex] + nums[secondIndex] == target) {
                    return new int[] {firstIndex, secondIndex};
                }
            }
        }

        // 原题保证有解；异常用于明确表达仓库接口在无解输入下的行为。
        throw new IllegalArgumentException("不存在满足目标和的两个元素");
    }

    /**
     * 使用单次遍历和哈希表寻找和为目标值的两个元素。
     *
     * <p>哈希表将已经遍历的数字映射到其下标。每轮先查找补数，再存入当前元素，
     * 保证查到的下标严格小于当前下标。重复值保留最近一次出现的下标即可。</p>
     *
     * <p>在哈希操作平均为 O(1) 的前提下，时间复杂度为 O(n)，额外空间为 O(n)。</p>
     *
     * @param nums 遵循原题约束的整数数组，方法不会修改其内容
     * @param target 遵循原题范围的目标值
     * @return 两个不同元素的下标，较早遍历到的下标在前
     * @throws IllegalArgumentException 如果数组为 null、元素不足两个或不存在有效答案
     */
    public static int[] twoSum(int[] nums, int target) {
        validateInput(nums);

        // 按最多存入 n 个元素估算容量，避免默认容量较小时反复扩容。
        int initialCapacity = (int) (nums.length / HASH_MAP_LOAD_FACTOR) + 1;
        Map<Integer, Integer> indexByValue =
                new HashMap<>(initialCapacity, HASH_MAP_LOAD_FACTOR);

        for (int currentIndex = 0; currentIndex < nums.length; currentIndex++) {
            int currentValue = nums[currentIndex];

            // 已知当前数字后，另一个数字只能等于目标值减去当前数字。
            int complement = target - currentValue;
            Integer complementIndex = indexByValue.get(complement);

            // 下标 0 是合法结果；使用 null 判断不存在，不能用 0 判断。
            if (complementIndex != null) {
                return new int[] {complementIndex, currentIndex};
            }

            // 必须在查找之后存入，防止当前元素与自己配对。
            indexByValue.put(currentValue, currentIndex);
        }

        throw new IllegalArgumentException("不存在满足目标和的两个元素");
    }

    /**
     * 校验寻找两个不同元素所需的基本输入条件。
     *
     * <p>这里只检查空引用和最小长度，其他数据范围与唯一解条件由调用方遵循。</p>
     *
     * @param nums 待校验的数组
     * @throws IllegalArgumentException 如果数组为 null 或元素不足两个
     */
    private static void validateInput(int[] nums) {
        if (nums == null || nums.length < 2) {
            throw new IllegalArgumentException("数组不能为空且至少包含两个元素");
        }
    }
}
