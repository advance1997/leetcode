package medium.day20261103.containerwithmostwater;

/**
 * LeetCode 11：盛最多水的容器（Container With Most Water），难度：中等。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/container-with-most-water/">盛最多水的容器</a>。</p>
 * <p>给定长度为 n 的整数数组 height，第 i 条垂线从 (i, 0) 延伸到
 * (i, height[i])。选择两条不同的垂线，与 x 轴组成不能倾斜的容器，
 * 返回能够容纳的最大水量。中间的垂线不从面积中扣除。</p>
 * <ul>
 *   <li>{@code 2 <= n <= 100000}，n 等于数组长度。</li>
 *   <li>{@code 0 <= height[i] <= 10000}。</li>
 * </ul>
 * <pre>
 * 输入：[1, 8, 6, 2, 5, 4, 8, 3, 7]
 * 输出：49
 * 解释：下标 1 和 8 的宽度为 7，短边高度为 7，面积为 49。
 *
 * 输入：[1, 1]
 * 输出：1
 * 解释：宽度 1，短边高度 1。
 * </pre>
 * <p>面积为两线下标之差乘以两线高度的较小值。本类提供暴力枚举和相向双指针：
 * 前者时间 O(n²)，后者时间 O(n)，两者额外空间均为 O(1)，均不修改输入。</p>
 * <p>原题最大面积不超过 99999 × 10000 = 999990000，int 可以安全保存。
 * 仓库对 null 和不足两个元素的数组抛出异常，不校验其他原题约束。
 * 超出原题范围的输入不在接口保证之内。实现使用 Java 8 已有语法和 API。</p>
 */
public final class ContainerWithMostWater {

    /**
     * 禁止实例化算法工具类。
     */
    private ContainerWithMostWater() {
    }

    /**
     * 枚举所有不同下标构成的容器，返回最大面积。
     *
     * <p>每对下标恰好枚举一次，时间 O(n²)，额外空间 O(1)。</p>
     *
     * @param height 满足原题范围的高度数组，不会被修改
     * @return 所有合法容器中的最大面积
     * @throws IllegalArgumentException 如果数组为 null 或少于两个元素
     */
    public static int maxAreaBruteForce(int[] height) {
        validateInput(height);

        int maximumArea = 0;
        for (int leftIndex = 0; leftIndex < height.length - 1; leftIndex++) {
            // 右边界从下一位置开始，排除同一条线和重复数对。
            for (int rightIndex = leftIndex + 1; rightIndex < height.length; rightIndex++) {
                int width = rightIndex - leftIndex;
                int limitedHeight = Math.min(height[leftIndex], height[rightIndex]);
                int currentArea = width * limitedHeight;
                maximumArea = Math.max(maximumArea, currentArea);
            }
        }

        return maximumArea;
    }

    /**
     * 从最大宽度开始，每次舍弃较短边，返回最大容器面积。
     *
     * <p>已舍弃边界参与的未枚举容器均不超过已记录的候选面积。
     * 因此剩余区间保留所有仍可能改善结果的候选。时间 O(n)，额外空间 O(1)。</p>
     *
     * @param height 满足原题范围的高度数组，不会被修改
     * @return 所有合法容器中的最大面积
     * @throws IllegalArgumentException 如果数组为 null 或少于两个元素
     */
    public static int maxArea(int[] height) {
        validateInput(height);

        int leftIndex = 0;
        int rightIndex = height.length - 1;
        int maximumArea = 0;

        while (leftIndex < rightIndex) {
            // 先记录当前候选，再舍弃边界，避免跳过当前容器。
            int width = rightIndex - leftIndex;
            int limitedHeight = Math.min(height[leftIndex], height[rightIndex]);
            int currentArea = width * limitedHeight;
            maximumArea = Math.max(maximumArea, currentArea);

            // 保留短边只缩小宽度不可能改善面积，所以舍弃短边寻找新候选。
            if (height[leftIndex] <= height[rightIndex]) {
                // 等高时任意移动一侧均安全，本实现固定移动左侧。
                leftIndex++;
            } else {
                rightIndex--;
            }
        }

        return maximumArea;
    }

    /**
     * 校验构成容器所需的基本输入条件。
     *
     * @param height 待校验的数组，其他原题约束由调用方保证
     * @throws IllegalArgumentException 如果数组为 null 或少于两个元素
     */
    private static void validateInput(int[] height) {
        if (height == null || height.length < 2) {
            throw new IllegalArgumentException("高度数组不能为 null 且至少包含两个元素");
        }
    }
}
