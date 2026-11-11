package medium.day20261105.findallanagramsinastring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LeetCode 438：找到字符串中所有字母异位词（Find All Anagrams in a String），难度：中等。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/find-all-anagrams-in-a-string/">找到字符串中所有字母异位词</a>。</p>
 * <p>给定字符串 s 和 p，找到 s 中所有与 p 互为字母异位词的连续子串，返回起始下标。
 * 异位词要求长度和每个字母的出现次数相同，不要求字母顺序相同。
 * 答案顺序不限，子串允许相互重叠；本实现按下标升序返回。</p>
 * <ul>
 *   <li>{@code 1 <= s.length(), p.length() <= 30000}。</li>
 *   <li>s 和 p 仅包含小写英文字母 a 到 z。</li>
 * </ul>
 * <pre>
 * 输入：s = "cbaebabacd", p = "abc"
 * 输出：[0, 6]
 * 解释：从 0 开始的 "cba" 和从 6 开始的 "bac" 都符合要求。
 *
 * 输入：s = "abab", p = "ab"
 * 输出：[0, 1, 2]
 * 解释：三个相互重叠的窗口 "ab"、"ba"、"ab" 均应记录。
 * </pre>
 * <p>设 n 为文本长度、m 为模式长度、C=26、k 为答案数量，且 n 不小于 m。
 * 暴力重建计数时间 O(m+(n-m+1)(m+C))；滑动窗口时间 O(m+nC)，
 * C 固定时为 O(n+m)。两者辅助空间均 O(C)，输出空间 O(k)。</p>
 * <p>仓库允许空文本并返回空列表；任意字符串为 null 或模式为空时抛出异常。
 * 不逐项校验字符集与长度上限。字符串不会被修改，不进行大小写或空格转换。
 * 计数不超过 30000，int 安全。实现使用 Java 8 已有语法和 API。</p>
 */
public final class FindAllAnagramsInAString {

    /**
     * 原题小写英文字母集合的大小。
     */
    private static final int LETTER_COUNT = 26;

    /**
     * 用于将 a 到 z 映射为下标 0 到 25。
     */
    private static final char FIRST_LETTER = 'a';

    /**
     * 禁止实例化静态算法工具类。
     */
    private FindAllAnagramsInAString() {
    }

    /**
     * 枚举每个长度等于模式的窗口，重新统计字母频次并比较。
     *
     * <p>n 不小于 m 时，时间 O(m+(n-m+1)(m+C))，辅助空间 O(C)，输出空间 O(k)。</p>
     *
     * @param text 仅含小写英文字母的文本，允许空串
     * @param pattern 仅含小写英文字母的非空模式串
     * @return 所有匹配子串的起始下标，按升序排列，无匹配时为空
     * @throws IllegalArgumentException 如果任意参数为 null 或 pattern 为空
     */
    public static List<Integer> findAnagramsBruteForce(String text, String pattern) {
        validateInput(text, pattern);
        List<Integer> results = new ArrayList<>();
        if (text.length() < pattern.length()) {
            return results;
        }

        int[] patternCounts = countLetters(pattern);
        int[] windowCounts = new int[LETTER_COUNT];
        for (int start = 0; start <= text.length() - pattern.length(); start++) {
            // 每个起点重新计数，复用数组以避免重复分配。
            Arrays.fill(windowCounts, 0);
            for (int offset = 0; offset < pattern.length(); offset++) {
                windowCounts[text.charAt(start + offset) - FIRST_LETTER]++;
            }
            if (Arrays.equals(patternCounts, windowCounts)) {
                results.add(start);
            }
        }

        return results;
    }

    /**
     * 用固定长度滑动窗口复用相邻区间的字母频次。
     *
     * <p>每轮加入新右端，若长度超出模式长度则移除旧左端；仅在长度恰好相同时比较。
     * n 不小于 m 时，时间 O(m+nC)，C 固定为 26，辅助空间 O(C)，输出空间 O(k)。</p>
     *
     * @param text 仅含小写英文字母的文本，允许空串
     * @param pattern 仅含小写英文字母的非空模式串
     * @return 所有匹配子串的起始下标，按升序排列，无匹配时为空
     * @throws IllegalArgumentException 如果任意参数为 null 或 pattern 为空
     */
    public static List<Integer> findAnagrams(String text, String pattern) {
        validateInput(text, pattern);
        List<Integer> results = new ArrayList<>();
        if (text.length() < pattern.length()) {
            return results;
        }

        int windowLength = pattern.length();
        int[] patternCounts = countLetters(pattern);
        int[] windowCounts = new int[LETTER_COUNT];
        for (int right = 0; right < text.length(); right++) {
            windowCounts[text.charAt(right) - FIRST_LETTER]++;

            // 每轮只加入一个字符，超长时也只需移除一个，不需要连续收缩。
            if (right >= windowLength) {
                int outgoingIndex = right - windowLength;
                windowCounts[text.charAt(outgoingIndex) - FIRST_LETTER]--;
            }

            // 前 m-1 轮还没有完整窗口，不应记录答案。
            if (right >= windowLength - 1 && Arrays.equals(patternCounts, windowCounts)) {
                results.add(right - windowLength + 1);
            }
        }

        return results;
    }

    /**
     * 将模式串转为固定长度的字母频次数组。
     *
     * @param text 只含小写英文字母的非 null 字符串
     * @return 下标 0 到 25 分别对应 a 到 z 的出现次数
     */
    private static int[] countLetters(String text) {
        int[] counts = new int[LETTER_COUNT];
        for (int index = 0; index < text.length(); index++) {
            counts[text.charAt(index) - FIRST_LETTER]++;
        }
        return counts;
    }

    /**
     * 校验空引用和非空模式，其他原题约束由调用方保证。
     *
     * @param text 文本字符串，允许空串
     * @param pattern 模式字符串，不允许空串
     * @throws IllegalArgumentException 如果任意参数为 null 或 pattern 为空
     */
    private static void validateInput(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("字符串不能为 null，模式串不能为空");
        }
    }
}
