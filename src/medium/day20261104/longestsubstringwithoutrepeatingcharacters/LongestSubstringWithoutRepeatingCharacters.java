package medium.day20261104.longestsubstringwithoutrepeatingcharacters;

import java.util.HashSet;
import java.util.Set;

/**
 * LeetCode 3：无重复字符的最长子串（Longest Substring Without Repeating Characters），难度：中等。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/longest-substring-without-repeating-characters/">无重复字符的最长子串</a>。</p>
 * <p>给定字符串 s，返回其中不含重复字符的最长连续子串的长度。
 * 必须连续，不能跳过中间字符拼接成子序列。空字符串返回 0。</p>
 * <ul>
 *   <li>本次核对的公开题面镜像约束为 {@code 0 <= s.length() <= 100000}。</li>
 *   <li>s 由英文字母、数字、符号和空格组成，大小写分别比较，空格也参与去重。</li>
 * </ul>
 * <pre>
 * 输入："abcabcbb"，输出：3，最长子串之一为 "abc"。
 * 输入："bbbbb"，输出：1，最长子串为 "b"。
 * 输入："pwwkew"，输出：3，最长子串之一为 "wke"；"pwke" 不连续，不是子串。
 * </pre>
 * <p>本类提供从每个起点扩张的枚举法和复用字符集合的滑动窗口。
 * 在哈希操作平均 O(1) 的前提下，时间上界分别为 O(n²)、O(n)，
 * 辅助空间均为 O(min(n,C))，n 为字符串长度，C 为字符集合大小。</p>
 * <p>仓库对 null 抛出异常，不校验长度上限及字符范围。实现按 Java char
 * 即 UTF-16 代码单元处理，不能将其宣称为按 Unicode 码点或用户感知字符计数的算法。
 * 不对输入进行大小写转换、去空格或重排；实现使用 Java 8 已有语法和 API。</p>
 */
public final class LongestSubstringWithoutRepeatingCharacters {

    /**
     * 字符集合从较小容量开始，避免为尚未知晓的窗口大小预分配整串长度。
     */
    private static final int INITIAL_SET_CAPACITY = 16;

    /**
     * 禁止实例化静态算法工具类。
     */
    private LongestSubstringWithoutRepeatingCharacters() {
    }

    /**
     * 枚举每个左端点，遇到首个重复字符时结束当前起点的扩张。
     *
     * <p>每个起点重建字符集合，哈希操作平均 O(1) 时，时间上界 O(n²)，
     * 辅助空间 O(min(n,C))。</p>
     *
     * @param text 待查找字符串，按 Java char 比较，允许空串
     * @return 最长无重复连续子串的长度，空串为 0
     * @throws IllegalArgumentException 如果 text 为 null
     */
    public static int lengthOfLongestSubstringBruteForce(String text) {
        validateInput(text);
        int maximumLength = 0;

        for (int left = 0; left < text.length(); left++) {
            Set<Character> characters = new HashSet<>(INITIAL_SET_CAPACITY);
            for (int right = left; right < text.length(); right++) {
                // add 返回 false 表示当前字符已经存在，再扩张也无法消除这个重复。
                if (!characters.add(text.charAt(right))) {
                    break;
                }
                maximumLength = Math.max(maximumLength, right - left + 1);
            }
        }

        return maximumLength;
    }

    /**
     * 使用同向双指针维护无重复窗口，出现冲突时从左侧连续删除字符。
     *
     * <p>加入新字符前，集合精确表示无重复区间 [left,right)。
     * 移除旧的同值字符后加入新字符，得到合法闭区间 [left,right]，再更新答案。
     * 哈希操作平均 O(1) 时，时间 O(n)，辅助空间 O(min(n,C))。</p>
     *
     * @param text 待查找字符串，按 Java char 比较，允许空串
     * @return 最长无重复连续子串的长度，空串为 0
     * @throws IllegalArgumentException 如果 text 为 null
     */
    public static int lengthOfLongestSubstring(String text) {
        validateInput(text);
        Set<Character> windowCharacters = new HashSet<>(INITIAL_SET_CAPACITY);
        int left = 0;
        int maximumLength = 0;

        for (int right = 0; right < text.length(); right++) {
            char currentCharacter = text.charAt(right);

            // 当前字符尚未加入集合，若存在同值旧字符，需要删到旧字符离开窗口为止。
            while (windowCharacters.contains(currentCharacter)) {
                windowCharacters.remove(text.charAt(left));
                left++;
            }

            // 恢复无重复条件后才加入并记录长度，避免把非法窗口计入答案。
            windowCharacters.add(currentCharacter);
            maximumLength = Math.max(maximumLength, right - left + 1);
        }

        return maximumLength;
    }

    /**
     * 校验字符串引用，空字符串属于合法输入。
     *
     * @param text 待校验字符串
     * @throws IllegalArgumentException 如果 text 为 null
     */
    private static void validateInput(String text) {
        if (text == null) {
            throw new IllegalArgumentException("字符串不能为 null");
        }
    }
}
