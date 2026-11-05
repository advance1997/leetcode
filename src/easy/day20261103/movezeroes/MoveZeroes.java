package easy.day20261103.movezeroes;

/**
 * LeetCode 283：移动零（Move Zeroes），难度：简单。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/move-zeroes/">移动零</a>。</p>
 * <p>给定整数数组 nums，将所有零移动到末尾，同时保持非零元素的相对顺序。
 * 必须原地修改数组，不能复制数组来完成操作；方法没有返回值。</p>
 * <ul>
 *   <li>{@code 1 <= nums.length <= 10000}。</li>
 *   <li>元素范围为 Java int 的完整范围，即 -2³¹ 到 2³¹ - 1。</li>
 * </ul>
 * <pre>
 * 输入：[0, 1, 0, 3, 12]
 * 修改后：[1, 3, 12, 0, 0]
 * 解释：1、3、12 的先后顺序不变，两个零移到末尾。
 *
 * 输入：[0]
 * 修改后：[0]
 * </pre>
 * <p>令 n 为数组长度。暴力搬移最坏时间 O(n²)，额外空间 O(1)；
 * 双指针压缩后补零时间 O(n)，额外空间 O(1)。两种方法均直接修改传入数组。
 * 原题进阶要求尽量减少操作次数，本实现跳过不必要的同位置写入和已有零的写入，
 * 不声称在所有输入上达到最少写入次数。</p>
 * <p>仓库额外允许空数组（不执行操作），null 抛出异常；不检查长度上限。
 * 仅比较和搬运元素，不对元素求和，因此完整 int 范围均适用。
 * 实现使用 Java 8 已有语法和 API。</p>
 */
public final class MoveZeroes {

    /**
     * 禁止实例化仅提供静态算法方法的工具类。
     */
    private MoveZeroes() {
    }

    /**
     * 遇到零时将未处理区间的后续元素逐个左移，再把零放到末尾。
     *
     * <p>activeEnd 之后是已归位的零；搬移后必须重新检查当前位置。
     * 最坏时间 O(n²)，额外空间 O(1)。</p>
     *
     * @param nums 要原地修改的整数数组，允许空数组
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static void moveZeroesBruteForce(int[] nums) {
        validateInput(nums);

        int currentIndex = 0;
        int activeEnd = nums.length;
        while (currentIndex < activeEnd) {
            if (nums[currentIndex] != 0) {
                currentIndex++;
                continue;
            }

            // 左移保持未处理元素的原顺序，不触碰已经归位的尾部零。
            for (int nextIndex = currentIndex + 1; nextIndex < activeEnd; nextIndex++) {
                nums[nextIndex - 1] = nums[nextIndex];
            }

            activeEnd--;
            nums[activeEnd] = 0;
            // 当前位置刚搬入新元素，不能递增，否则可能漏掉连续的零。
        }
    }

    /**
     * 使用读写双指针稳定压缩非零元素，再将剩余位置补零。
     *
     * <p>扫描期间 [0, writeIndex) 保存已读部分的全部非零元素，顺序不变。
     * 写位置不超过读位置，因此不会覆盖尚未读取的元素。
     * 时间 O(n)，额外空间 O(1)。</p>
     *
     * @param nums 要原地修改的整数数组，允许空数组
     * @throws IllegalArgumentException 如果数组为 null
     */
    public static void moveZeroes(int[] nums) {
        validateInput(nums);

        int writeIndex = 0;
        for (int readIndex = 0; readIndex < nums.length; readIndex++) {
            if (nums[readIndex] != 0) {
                // 没有遇到空位时无需自己覆盖自己，减少无零输入上的写入。
                if (writeIndex != readIndex) {
                    nums[writeIndex] = nums[readIndex];
                }
                writeIndex++;
            }
        }

        // 此时有效前缀已经完成，尾部可能留有非零元素的旧副本。
        for (int fillIndex = writeIndex; fillIndex < nums.length; fillIndex++) {
            if (nums[fillIndex] != 0) {
                nums[fillIndex] = 0;
            }
        }
    }

    /**
     * 校验数组引用，空数组按无须修改处理。
     *
     * @param nums 待校验的数组
     * @throws IllegalArgumentException 如果数组为 null
     */
    private static void validateInput(int[] nums) {
        if (nums == null) {
            throw new IllegalArgumentException("数组不能为 null");
        }
    }
}
