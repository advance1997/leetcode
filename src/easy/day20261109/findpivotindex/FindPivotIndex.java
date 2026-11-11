package easy.day20261109.findpivotindex;

/**
 * LeetCode 724：寻找数组的中心下标（Find Pivot Index），难度：简单。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/find-pivot-index/">寻找数组的中心下标</a>。</p>
 *
 * <p>给定整数数组 nums，返回左侧所有元素之和等于右侧所有元素之和的中心下标。
 * 两侧均不包含当前元素；边界外的空区间之和为 0。存在多个答案时返回最左侧下标，
 * 没有答案时返回 -1。数组可以包含负数，中心下标不一定在数组的几何中间。</p>
 *
 * <p>原题约束：{@code 1 <= nums.length <= 10000}，
 * {@code -1000 <= nums[i] <= 1000}。</p>
 *
 * <pre>
 * 输入：[1, 7, 3, 6, 5, 6]；输出：3。
 * 解释：下标 3 左侧为 1 + 7 + 3 = 11，右侧为 5 + 6 = 11。
 * 输入：[1, 2, 3]；输出：-1，没有符合条件的下标。
 * 输入：[2, 1, -1]；输出：0，左侧为空，右侧 1 + (-1) = 0。
 * </pre>
 *
 * <p>本类包含暴力枚举与总和结合滚动前缀和两种实现。令 n 为数组长度，
 * 暴力法最坏时间 O(n²)，优化法时间 O(n)，两者额外空间均为 O(1)。
 * 原题范围内任意区间和的绝对值不超过 10000000，使用 int 安全。</p>
 *
 * <p>仓库额外约定：null 抛出 IllegalArgumentException，空数组返回 -1。
 * 两种方法均不修改输入，不逐项检查长度上限或数值范围，调用方应遵守原题约束。
 * 代码仅使用 Java 8 已有语法和 API。</p>
 */
public final class FindPivotIndex {

    /**
     * 禁止实例化仅提供静态算法方法的工具类。
     */
    private FindPivotIndex() {
    }

    /**
     * 枚举候选下标，并分别重新计算左右两侧的和。
     *
     * <p>从左到右检查保证返回最左侧答案。最坏时间 O(n²)，额外空间 O(1)。</p>
     *
     * @param nums 符合原题数值范围的整数数组，允许空数组，不会被修改
     * @return 最左侧中心下标；不存在或数组为空时返回 -1
     * @throws IllegalArgumentException 如果 nums 为 null
     */
    public static int pivotIndexBruteForce(int[] nums) {
        validateInput(nums);

        // 候选下标递增，因此第一个匹配就是题目要求的最左侧答案。
        for (int pivot = 0; pivot < nums.length; pivot++) {
            int leftSum = 0;
            int rightSum = 0;

            // 左区间为 [0, pivot)，不包含候选元素。
            for (int index = 0; index < pivot; index++) {
                leftSum += nums[index];
            }

            // 右区间从 pivot + 1 开始；到达边界时空区间保持为 0。
            for (int index = pivot + 1; index < nums.length; index++) {
                rightSum += nums[index];
            }

            if (leftSum == rightSum) {
                return pivot;
            }
        }

        // 包括空数组在内，没有候选下标满足条件时统一返回 -1。
        return -1;
    }

    /**
     * 使用总和与滚动前缀和在线性时间内寻找中心下标。
     *
     * <p>检查下标 pivot 前，leftSum 恰好等于 [0, pivot) 的元素和。
     * 右侧和由总和减去左侧和与当前元素得到；比较失败后才扩展左侧区间。
     * 两次顺序扫描，时间 O(n)，额外空间 O(1)。</p>
     *
     * @param nums 符合原题数值范围的整数数组，允许空数组，不会被修改
     * @return 最左侧中心下标；不存在或数组为空时返回 -1
     * @throws IllegalArgumentException 如果 nums 为 null
     */
    public static int pivotIndex(int[] nums) {
        validateInput(nums);

        // 先获得全局总和，之后无需为每个候选下标重新遍历右侧区间。
        int totalSum = 0;
        for (int number : nums) {
            totalSum += number;
        }

        // 第一个候选下标左侧为空，因此前缀和从 0 开始。
        int leftSum = 0;
        for (int pivot = 0; pivot < nums.length; pivot++) {
            // 总和由左区间、当前元素和右区间组成，当前元素必须单独扣除。
            int rightSum = totalSum - leftSum - nums[pivot];
            if (leftSum == rightSum) {
                return pivot;
            }

            // 检查完成后，当前元素才成为下一个候选下标的左侧元素。
            leftSum += nums[pivot];
        }

        return -1;
    }

    /**
     * 检查仓库约定的空引用异常，不额外扫描原题已保证的数值范围。
     *
     * @param nums 待检查的输入数组
     * @throws IllegalArgumentException 如果 nums 为 null
     */
    private static void validateInput(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("nums 不能为 null");
        }
    }
}
