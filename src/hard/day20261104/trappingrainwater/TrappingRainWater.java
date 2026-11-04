package hard.day20261104.trappingrainwater;

/**
 * LeetCode 42：接雨水（Trapping Rain Water），难度：困难。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/trapping-rain-water/">接雨水</a>。</p>
 * <p>给定 n 个非负整数，表示按顺序排列、宽度均为 1 的柱子高度。
 * 计算下雨后这些柱子之间总共能接多少单位的水，返回总水量。
 * 柱子本身占据的空间不能计入水量，排列顺序不能改变。</p>
 * <ul>
 *   <li>{@code 1 <= height.length <= 20000}。</li>
 *   <li>{@code 0 <= height[i] <= 100000}。</li>
 * </ul>
 * <pre>
 * 输入：[0,1,0,2,1,0,1,3,2,1,2,1]
 * 输出：6
 * 解释：下标 2、4、5、6、9 分别接水 1、1、2、1、1。
 *
 * 输入：[4,2,0,3,2,5]
 * 输出：9
 * 解释：逐列水量为 [0,2,4,1,2,0]。
 * </pre>
 * <p>每列水量等于含自身的左右最高柱高度的较小值减去本列高度。
 * 本类提供暴力逐列扫描、前后缀最大值预处理、双指针三种方法，时间分别为
 * O(n²)、O(n)、O(n)，额外空间分别为 O(1)、O(n)、O(1)，均不修改输入。</p>
 * <p>原题范围内，至多 19998 个内部位置各积水 100000，总量不超过
 * 1999800000，int 安全。仓库额外允许空数组并返回 0，null 抛出异常。
 * 不逐项检查非负性与上限，调用方须遵循原题范围。实现使用 Java 8 已有语法和 API。</p>
 */
public final class TrappingRainWater {

    /**
     * 禁止实例化静态算法工具类。
     */
    private TrappingRainWater() {
    }

    /**
     * 每列重新扫描左右最高柱，累加该列水量。
     *
     * <p>时间 O(n²)，额外空间 O(1)。左右最大值都包含当前柱，水量不会为负。</p>
     *
     * @param height 满足原题数值范围的高度数组，不会被修改，允许空数组
     * @return 总接水量，少于三根柱时为 0
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static int trapBruteForce(int[] height) {
        validateInput(height);
        int totalWater = 0;

        // 首尾位置无法接水，只检查内部列。
        for (int index = 1; index < height.length - 1; index++) {
            int leftMaximum = height[index];
            int rightMaximum = height[index];
            for (int left = 0; left < index; left++) {
                leftMaximum = Math.max(leftMaximum, height[left]);
            }
            for (int right = index + 1; right < height.length; right++) {
                rightMaximum = Math.max(rightMaximum, height[right]);
            }

            // 水面由较矮的挡水边界决定，减去柱子占用的高度。
            totalWater += Math.min(leftMaximum, rightMaximum) - height[index];
        }

        return totalWater;
    }

    /**
     * 预处理每列左右最高柱，避免反复扫描相同区间。
     *
     * <p>两个最大值数组均包含当前柱，时间 O(n)，额外空间 O(n)。</p>
     *
     * @param height 满足原题数值范围的高度数组，不会被修改，允许空数组
     * @return 总接水量，少于三根柱时为 0
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static int trapWithPrefixMax(int[] height) {
        validateInput(height);
        if (height.length < 3) {
            return 0;
        }

        int length = height.length;
        int[] leftMaximum = new int[length];
        int[] rightMaximum = new int[length];
        leftMaximum[0] = height[0];
        rightMaximum[length - 1] = height[length - 1];

        // 左侧最大值依赖上一列，因此从左向右计算。
        for (int index = 1; index < length; index++) {
            leftMaximum[index] = Math.max(leftMaximum[index - 1], height[index]);
        }
        // 右侧最大值依赖下一列，必须反向计算。
        for (int index = length - 2; index >= 0; index--) {
            rightMaximum[index] = Math.max(rightMaximum[index + 1], height[index]);
        }

        int totalWater = 0;
        for (int index = 1; index < length - 1; index++) {
            totalWater += Math.min(leftMaximum[index], rightMaximum[index]) - height[index];
        }
        return totalWater;
    }

    /**
     * 使用相向双指针，优先结算已知边界最大值较小的一侧。
     *
     * <p>更新后 leftMaximum 是 [0,left] 的最大值，rightMaximum 是
     * [right,n-1] 的最大值。如果前者不超过后者，left 列的右侧必有足够高的边界，
     * 可以直接由 leftMaximum 确定水面；另一侧同理。时间 O(n)，额外空间 O(1)。</p>
     *
     * @param height 满足原题数值范围的高度数组，不会被修改，允许空数组
     * @return 总接水量，少于三根柱时为 0
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static int trap(int[] height) {
        validateInput(height);
        int left = 0;
        int right = height.length - 1;
        int leftMaximum = 0;
        int rightMaximum = 0;
        int totalWater = 0;

        // 使用闭区间，把每一列恰好结算一次，包括最后相遇的位置。
        while (left <= right) {
            leftMaximum = Math.max(leftMaximum, height[left]);
            rightMaximum = Math.max(rightMaximum, height[right]);

            if (leftMaximum <= rightMaximum) {
                // 完整左最大值已知，右侧已知边界足够高，未知中间区域不会降低水面。
                totalWater += leftMaximum - height[left];
                left++;
            } else {
                totalWater += rightMaximum - height[right];
                right--;
            }
        }

        return totalWater;
    }

    /**
     * 检查数组引用，其他原题范围由调用方保证。
     *
     * @param height 待校验数组，允许空数组
     * @throws IllegalArgumentException 如果数组为 null
     */
    private static void validateInput(int[] height) {
        if (height == null) {
            throw new IllegalArgumentException("高度数组不能为 null");
        }
    }
}
