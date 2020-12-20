package simple;

/**
 * @program: leetcode
 * @description: 编写一个函数来查找字符串数组中的最长公共前缀。
 * 如果不存在公共前缀，返回空字符串 ""。
 *
 * 示例 1:
 *
 * 输入: ["flower","flow","flight"]
 * 输出: "fl"
 * 示例 2:
 *
 * 输入: ["dog","racecar","car"]
 * 输出: ""
 * 解释: 输入不存在公共前缀。
 * 说明:
 *
 * 所有输入只包含小写字母 a-z 。
 *
 * 解题思路：选择数组第一个字符串默认为最长公共前缀，然后依次对比
 *
 * @author: zhouh
 * @create: 2020-12-18 10:44
 **/
public class LongestCommonPrefix {

    public static void main(String[] args) {
        String[] strs = new String[]{"c","acc","ccc"};
        System.out.println(longestCommonPrefix(strs));
    }

    public static String getCommonPrefix(String[] strs){
        if (strs.length == 0) {
            return "";
        }
        String result = strs[0];
        if (strs.length == 1) {
            return result;
        }
        for (int i = 1; i < strs.length; i++) {
            if (!strs[i].startsWith(result)) {
                result = result.substring(0, result.length() - 1);
                i--;
            }
        }
        return result;
    }

    /**
     * 方法一：横向扫描
     */
    public static String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) {
            return "";
        }
        String prefix = strs[0];
        int count = strs.length;
        for (int i = 1; i < count; i++) {
            prefix = longestCommonPrefix(prefix, strs[i]);
            if (prefix.length() == 0) {
                break;
            }
        }
        return prefix;
    }

    public static String longestCommonPrefix(String str1, String str2) {
        int length = Math.min(str1.length(), str2.length());
        int index = 0;
        while (index < length && str1.charAt(index) == str2.charAt(index)) {
            index++;
        }
        return str1.substring(0, index);
    }

}
