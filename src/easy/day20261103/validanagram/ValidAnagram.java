package easy.day20261103.validanagram;

/**
 * LeetCode 242：有效的字母异位词（Valid Anagram），难度：简单。
 *
 * <p>官方题目：
 * <a href="https://leetcode.cn/problems/valid-anagram/">有效的字母异位词</a>。</p>
 *
 * <p>给定两个字符串 {@code s} 和 {@code t}，判断 {@code t} 是否为 {@code s}
 * 的字母异位词，返回布尔值。字母异位词包含相同的字符，且每个字符出现的次数相同，
 * 字符顺序可以不同。相同字符串也满足条件，重复字符必须按实际次数参与比较。</p>
 *
 * <p>原题约束：</p>
 * <ul>
 *   <li>{@code 1 <= s.length(), t.length() <= 50000}。</li>
 *   <li>两个字符串仅包含小写英文字母 {@code a} 到 {@code z}。</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * 输入：s = "anagram", t = "nagaram"
 * 输出：true
 * 解释：a 出现 3 次，n、g、r、m 各出现 1 次，两个字符串的字符频次相同。
 *
 * 输入：s = "rat", t = "car"
 * 输出：false
 * 解释：s 中有 t 而没有 c，t 中有 c 而没有 t，字符频次不同。
 *
 * 输入：s = "aab", t = "abb"
 * 输出：false
 * 解释：虽然都只包含 a 和 b，但各字符的出现次数不同。
 * </pre>
 *
 * <p>本类提供两种实现。令 n 为长度相等时的字符串长度：</p>
 * <ul>
 *   <li>暴力匹配：为每个字符查找尚未使用的相同字符，最坏时间 O(n²)，额外空间 O(n)。</li>
 *   <li>数组计数：先统计库存再逐个扣减，时间 O(n + C)，额外空间 O(C)；
 *       本题字符集大小 C = 26，因此可简写为 O(n) 时间、O(1) 额外空间。</li>
 * </ul>
 *
 * <p>长度不等时两种方法均直接返回 false，耗时 O(1)，不分配辅助数组。
 * 仓库额外约定：null 输入抛出异常，两个空字符串返回 true。
 * 不逐项检查字符集和长度上限，调用方仍需遵守原题约束；两个实现均不修改输入。</p>
 *
 * <p>原题进阶要求考虑 Unicode 字符，当前实现只解决小写英文字母版本。
 * Unicode 扩展需要先明确字符语义，可按码点使用计数映射，详见同名题解。
 * 代码仅使用 Java 8 已有语法和 API。</p>
 */
public final class ValidAnagram {

    /**
     * 小写英文字母数量，用于直接寻址计数数组。
     */
    private static final int LOWERCASE_LETTER_COUNT = 26;

    /**
     * 小写英文字母起点，用于将字母映射到从零开始的数组下标。
     */
    private static final char FIRST_LOWERCASE_LETTER = 'a';

    /**
     * 禁止实例化仅提供静态算法方法的工具类。
     */
    private ValidAnagram() {
    }

    /**
     * 使用逐个匹配的暴力方法判断两个字符串是否为字母异位词。
     *
     * <p>每次匹配后标记目标位置，保证同一个字符位置不能被重复使用。
     * 长度相等时最坏时间 O(n²)，额外空间 O(n)；长度不等时直接返回。</p>
     *
     * @param source 原字符串，仅包含小写英文字母；允许空字符串
     * @param target 待比较字符串，仅包含小写英文字母；允许空字符串
     * @return 当且仅当每个字母出现次数相同时返回 true
     * @throws IllegalArgumentException 如果任意字符串为 null
     */
    public static boolean isAnagramBruteForce(String source, String target) {
        validateInput(source, target);

        // 字符总量不同，不可能通过重排变成相同字符串。
        if (source.length() != target.length()) {
            return false;
        }

        // 标记 target 的位置是否已匹配，避免用一个 a 匹配多个 a。
        boolean[] matchedPositions = new boolean[target.length()];

        for (int sourceIndex = 0; sourceIndex < source.length(); sourceIndex++) {
            char currentCharacter = source.charAt(sourceIndex);
            boolean foundMatch = false;

            // 对当前字符，从头寻找尚未使用的相同字符。
            for (int targetIndex = 0; targetIndex < target.length(); targetIndex++) {
                if (!matchedPositions[targetIndex]
                        && currentCharacter == target.charAt(targetIndex)) {
                    matchedPositions[targetIndex] = true;
                    foundMatch = true;
                    break;
                }
            }

            // 某个字符没有剩余匹配位置，就已经能确定频次不相等。
            if (!foundMatch) {
                return false;
            }
        }

        // 等长且 source 的每个位置都匹配成功，意味着建立了完整的一一配对。
        return true;
    }

    /**
     * 使用固定长度的频次数组判断两个字符串是否为字母异位词。
     *
     * <p>先统计 source，再遍历 target 扣减对应计数。处理 target 的前 k 个字符后，
     * 每个计数等于该字母在 source 中的总次数减去其在 target 前 k 个字符中的次数。
     * 某个计数为负即可返回 false；等长且全部扣减未出现负数时返回 true。</p>
     *
     * <p>长度相等时，时间 O(n + C)、额外空间 O(C)，本题 C = 26；
     * 长度不等时耗时 O(1)，不分配计数数组。</p>
     *
     * @param source 原字符串，仅包含小写英文字母；允许空字符串
     * @param target 待比较字符串，仅包含小写英文字母；允许空字符串
     * @return 当且仅当每个字母出现次数相同时返回 true
     * @throws IllegalArgumentException 如果任意字符串为 null
     */
    public static boolean isAnagram(String source, String target) {
        validateInput(source, target);

        // 后续省略最终全零扫描，依赖这里的等长条件。
        if (source.length() != target.length()) {
            return false;
        }

        // a 到 z 对应下标 0 到 25，数组直接保存每个字母的剩余数量。
        int[] remainingCounts = new int[LOWERCASE_LETTER_COUNT];

        for (int sourceIndex = 0; sourceIndex < source.length(); sourceIndex++) {
            int letterIndex = source.charAt(sourceIndex) - FIRST_LOWERCASE_LETTER;
            remainingCounts[letterIndex]++;
        }

        for (int targetIndex = 0; targetIndex < target.length(); targetIndex++) {
            int letterIndex = target.charAt(targetIndex) - FIRST_LOWERCASE_LETTER;
            remainingCounts[letterIndex]--;

            // 库存已全部统计，扣成负数说明 target 对该字母的需求超过 source。
            if (remainingCounts[letterIndex] < 0) {
                return false;
            }
        }

        // 两串等长，所以最终计数总和为零；所有计数非负，意味着每一项都为零。
        return true;
    }

    /**
     * 检查两个字符串是否存在空引用。
     *
     * <p>允许空字符串，不检查字符集和长度上限。</p>
     *
     * @param source 原字符串
     * @param target 待比较字符串
     * @throws IllegalArgumentException 如果任意字符串为 null
     */
    private static void validateInput(String source, String target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("待比较的字符串不能为 null");
        }
    }
}
