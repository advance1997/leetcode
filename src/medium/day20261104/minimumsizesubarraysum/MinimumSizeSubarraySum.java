package medium.day20261104.minimumsizesubarraysum;

/**
 * LeetCode 209：长度最小的子数组（Minimum Size Subarray Sum），难度：中等。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/minimum-size-subarray-sum/">长度最小的子数组</a>。</p>
 * <p>给定正整数数组 nums 和正整数 target，寻找元素和大于等于 target 的
 * 最短非空连续子数组，返回其长度。不存在这样的子数组时返回 0。
 * 子数组必须连续，不能重排输入或任意挑选若干元素。</p>
 * <ul>
 *   <li>{@code 1 <= target <= 1000000000}。</li>
 *   <li>{@code 1 <= nums.length <= 100000}。</li>
 *   <li>{@code 1 <= nums[i] <= 10000}。</li>
 * </ul>
 * <pre>
 * 输入：target = 7, nums = [2, 3, 1, 2, 4, 3]
 * 输出：2
 * 解释：连续子数组 [4, 3] 的和为 7，长度为 2，且不存在长度 1 的合法子数组。
 *
 * 输入：target = 4, nums = [1, 4, 4]
 * 输出：1
 *
 * 输入：target = 11, nums = [1, 1, 1, 1, 1, 1, 1, 1]
 * 输出：0，整个数组的和仍不足 11。
 * </pre>
 * <p>本类提供累计求和的暴力枚举和可变长度滑动窗口，最坏时间分别为 O(n²)、O(n)，
 * 额外空间均为 O(1)，均不修改输入。正数前提保证扩张使和增加、收缩使和减少。</p>
 * <p>原题范围内数组总和不超过 1000000000，int 运算安全。
 * 仓库对 null 或非正 target 抛出异常，额外允许空数组并返回 0；
 * 不逐项检查元素正数性与各项上限，调用方须遵循原题约束。
 * 代码使用 Java 8 已有语法和 API。前缀和加二分的 O(n log n) 进阶思路见同名题解。</p>
 */
public final class MinimumSizeSubarraySum {

    /**
     * 禁止实例化静态算法工具类。
     */
    private MinimumSizeSubarraySum() {
    }

    /**
     * 枚举左端点并累计右端点，每个左端找到首个达标区间后结束本轮。
     *
     * <p>继续右扩只会使同一起点的区间更长，因此第一次达标就是该起点的最短答案。
     * 最坏时间 O(n²)，额外空间 O(1)。</p>
     *
     * @param target 正目标值，遵循原题范围
     * @param nums 正整数数组，不会被修改，允许空数组
     * @return 最短达标非空子数组长度，无解时返回 0
     * @throws IllegalArgumentException 如果 nums 为 null 或 target 不为正数
     */
    public static int minSubArrayLenBruteForce(int target, int[] nums) {
        validateInput(target, nums);
        int minimumLength = nums.length + 1;

        for (int left = 0; left < nums.length; left++) {
            int sum = 0;
            for (int right = left; right < nums.length; right++) {
                // 只加入新元素，避免对每个区间再从头求和。
                sum += nums[right];
                if (sum >= target) {
                    minimumLength = Math.min(minimumLength, right - left + 1);
                    break;
                }
            }
        }

        return minimumLength == nums.length + 1 ? 0 : minimumLength;
    }

    /**
     * 使用滑动窗口维护连续区间和，在窗口达标时持续收缩以尝试更短答案。
     *
     * <p>right 负责加入新元素，left 负责移除旧元素，每个元素最多进入、离开窗口各一次。
     * 时间 O(n)，额外空间 O(1)。正数约束是这种单调移动方式的前提。</p>
     *
     * @param target 正目标值，遵循原题范围
     * @param nums 正整数数组，不会被修改，允许空数组
     * @return 最短达标非空子数组长度，无解时返回 0
     * @throws IllegalArgumentException 如果 nums 为 null 或 target 不为正数
     */
    public static int minSubArrayLen(int target, int[] nums) {
        validateInput(target, nums);
        int left = 0;
        int windowSum = 0;
        int minimumLength = nums.length + 1;

        for (int right = 0; right < nums.length; right++) {
            // 新元素进入右端，windowSum 精确对应当前闭区间 [left, right]。
            windowSum += nums[right];

            // 一次扩张可能允许多次收缩，必须用 while 而不是 if。
            while (windowSum >= target) {
                // 此时窗口合法，先记录长度，再尝试删除左端元素。
                minimumLength = Math.min(minimumLength, right - left + 1);
                windowSum -= nums[left];
                left++;
            }
        }

        // n+1 是不可能出现的合法长度，用于区分从未找到答案的情况。
        return minimumLength == nums.length + 1 ? 0 : minimumLength;
    }

    /**
     * 检查数组引用和正目标值，其他原题约束由调用方保证。
     *
     * @param target 待校验的目标值
     * @param nums 待校验数组，允许空数组
     * @throws IllegalArgumentException 如果 nums 为 null 或 target 不为正数
     */
    private static void validateInput(int target, int[] nums) {
        if (nums == null || target <= 0) {
            throw new IllegalArgumentException("数组不能为 null，target 必须为正数");
        }
    }
}
