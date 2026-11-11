package hard.day20261105.minimumwindowsubstring;

import java.util.Arrays;

/**
 * LeetCode 76：最小覆盖子串（Minimum Window Substring），难度：困难。
 *
 * <p>官方题目：<a href="https://leetcode.cn/problems/minimum-window-substring/">最小覆盖子串</a>。</p>
 * <p>给定字符串 s、t，返回 s 中包含 t 全部字符（包括重复次数）的最短连续子串。
 * 允许窗口存在额外字符，字符顺序不限，大小写不同。无覆盖子串时返回空字符串。
 * 原题保证答案唯一。</p>
 * <ul>
 *   <li>{@code 1 <= s.length(), t.length() <= 100000}。</li>
 *   <li>s、t 由大小写英文字母组成。</li>
 * </ul>
 * <pre>
 * 输入：s = "ADOBECODEBANC", t = "ABC"
 * 输出："BANC"，它包含 A、B、C，且没有更短的覆盖子串。
 * 输入：s = "a", t = "a"，输出："a"。
 * 输入：s = "a", t = "aa"，输出：""，一个 a 不能满足两个 a 的需求。
 * </pre>
 * <p>设 n 为文本长度、m 为目标长度、C=128。暴力累计窗口并扫描计数表的时间上界
 * O(m+n²C)，滑动窗口为 O(n+m+C)，两者辅助空间 O(C)，返回字符串占 O(L)，
 * L 为答案长度。不修改输入，仅在最终返回时截取答案。</p>
 * <p>仓库额外允许空文本并返回空字符串；任一参数为 null 或目标为空时抛出异常。
 * 若出现多个等长最短窗口，返回起点最早者，这是对原题唯一解保证之外的扩展。
 * 不逐项验证字符集和长度上限；数组直接按 ASCII 代码索引，只保证原题英文字母输入。
 * 计数不超过 100000，int 安全，代码使用 Java 8 已有语法和 API。</p>
 */
public final class MinimumWindowSubstring {

    /**
     * ASCII 范围覆盖原题所有大小写英文字母，可直接用 char 作为下标。
     */
    private static final int CHARACTER_COUNT = 128;

    /**
     * 禁止实例化静态算法工具类。
     */
    private MinimumWindowSubstring() {
    }

    /**
     * 枚举左端并累计窗口，每次扫描计数表检查是否覆盖全部需求。
     *
     * <p>同一起点第一次覆盖后即可停止扩张，后续区间只会更长。
     * 时间上界 O(m+n²C)，辅助空间 O(C)，输出空间 O(L)。</p>
     *
     * @param text 由英文字母组成的文本，允许空串
     * @param target 由英文字母组成的非空目标串
     * @return 最短覆盖子串；等长时取起点最早者，无解时为空字符串
     * @throws IllegalArgumentException 如果参数为 null 或 target 为空
     */
    public static String minWindowBruteForce(String text, String target) {
        validateInput(text, target);
        if (text.length() < target.length()) {
            return "";
        }

        int[] requiredCounts = countLetters(target);
        int[] windowCounts = new int[CHARACTER_COUNT];
        int bestStart = 0;
        int bestLength = text.length() + 1;
        for (int left = 0; left < text.length(); left++) {
            Arrays.fill(windowCounts, 0);
            for (int right = left; right < text.length(); right++) {
                windowCounts[text.charAt(right)]++;
                if (covers(windowCounts, requiredCounts)) {
                    int length = right - left + 1;
                    if (length < bestLength) {
                        bestStart = left;
                        bestLength = length;
                    }
                    break;
                }
            }
        }

        return bestLength == text.length() + 1 ? "" : text.substring(bestStart, bestStart + bestLength);
    }

    /**
     * 用可变窗口和已满足的字符份数，在覆盖时持续收缩寻找最短答案。
     *
     * <p>matchedCount 等于每种字符的 min(窗口数量,需求数量) 之和，
     * 等于目标长度时恰好覆盖。时间 O(n+m+C)，辅助空间 O(C)，输出空间 O(L)。</p>
     *
     * @param text 由英文字母组成的文本，允许空串
     * @param target 由英文字母组成的非空目标串
     * @return 最短覆盖子串；等长时取起点最早者，无解时为空字符串
     * @throws IllegalArgumentException 如果参数为 null 或 target 为空
     */
    public static String minWindow(String text, String target) {
        validateInput(text, target);
        if (text.length() < target.length()) {
            return "";
        }

        int[] requiredCounts = countLetters(target);
        int[] windowCounts = new int[CHARACTER_COUNT];
        int left = 0;
        int matchedCount = 0;
        int bestStart = 0;
        int bestLength = text.length() + 1;
        for (int right = 0; right < text.length(); right++) {
            char incoming = text.charAt(right);
            windowCounts[incoming]++;
            // 加入后的计数仍未超过需求，说明本次补上了一份必需字符。
            if (windowCounts[incoming] <= requiredCounts[incoming]) {
                matchedCount++;
            }

            while (matchedCount == target.length()) {
                int length = right - left + 1;
                if (length < bestLength) {
                    bestStart = left;
                    bestLength = length;
                }

                char outgoing = text.charAt(left);
                // 这里比较删除前的计数；删掉必需份额才减少 matchedCount。
                if (windowCounts[outgoing] <= requiredCounts[outgoing]) {
                    matchedCount--;
                }
                windowCounts[outgoing]--;
                left++;
            }
        }

        // 只在最后复制答案，避免每次更新最佳窗口都生成新的字符串。
        return bestLength == text.length() + 1 ? "" : text.substring(bestStart, bestStart + bestLength);
    }

    /**
     * 统计原题英文字母字符的需求数量。
     *
     * @param text 非 null 的英文字母字符串
     * @return 按 ASCII 代码索引的频次数组
     */
    private static int[] countLetters(String text) {
        int[] counts = new int[CHARACTER_COUNT];
        for (int index = 0; index < text.length(); index++) {
            counts[text.charAt(index)]++;
        }
        return counts;
    }

    /**
     * 逐项检查窗口是否满足全部字符需求，允许窗口数量超过需求。
     *
     * @param windowCounts 当前窗口的频次数组
     * @param requiredCounts 目标需求数组，长度与窗口数组相同
     * @return 每项窗口计数都不少于需求时返回 true
     */
    private static boolean covers(int[] windowCounts, int[] requiredCounts) {
        for (int index = 0; index < requiredCounts.length; index++) {
            if (windowCounts[index] < requiredCounts[index]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验空引用与空目标，其他原题约束由调用方保证。
     *
     * @param text 文本字符串，允许空串
     * @param target 目标字符串，不允许空串
     * @throws IllegalArgumentException 如果参数为 null 或 target 为空
     */
    private static void validateInput(String text, String target) {
        if (text == null || target == null || target.isEmpty()) {
            throw new IllegalArgumentException("字符串不能为 null，目标字符串不能为空");
        }
    }
}
