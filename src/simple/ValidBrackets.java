package simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @program: leetcode
 * @description: 校验括号
 * 给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串，判断字符串是否有效。
 *
 * 有效字符串需满足：
 *
 * 左括号必须用相同类型的右括号闭合。
 * 左括号必须以正确的顺序闭合。
 * 注意空字符串可被认为是有效字符串。
 *
 * 示例 1:
 *
 * 输入: "()"
 * 输出: true
 * 示例 2:
 *
 * 输入: "()[]{}"
 * 输出: true
 * 示例 3:
 *
 * 输入: "(]"
 * 输出: false
 * 示例 4:
 *
 * 输入: "([)]"
 * 输出: false
 * 示例 5:
 *
 * 输入: "{[]}"
 * 输出: true
 * @author: zhouh
 * @create: 2020-12-18 12:03
 **/
public class ValidBrackets {

    final static Map<Character, Character> temp = new HashMap<>();

    static {
        temp.put('(', ')');
        temp.put('[', ']');
        temp.put('{', '}');
    }

    public static void main(String[] args) {
        System.out.println(isValid2("{[]}()"));
    }

    public static boolean isValid(String s){
        if (null == s || s.length() == 0 || s.length() % 2 != 0){
            return false;
        }
        char[] sArr = s.toCharArray();
        int len = s.length();
        for(int i = 0 ; i < len; ){
            if(null != temp.get(sArr[i]) && sArr[i+1] == temp.get(sArr[i])){
                i += 2;
                continue;
            }
            if(null != temp.get(sArr[i]) && sArr[len - i - 1] == temp.get(sArr[i])){
                i += 1;
                continue;
            }
            return false;
        }
        return true;
    }

    public static boolean isValid1(String s){
        if (s.length() % 2 != 0) {
            return false;
        }

        int t = s.length() / 2;
        while (t > 0) {
            s = s.replace("()", "").replace("[]", "").replace("{}", "");
            t--;
        }

        return s.isEmpty();
    }

    public static boolean isValid2(String s){
        Stack<Character>stack = new Stack<Character>();
        for(char c: s.toCharArray()){
            if(c=='('){
                stack.push(')');
            }
            else if(c=='['){
                stack.push(']');
            }
            else if(c=='{'){
                stack.push('}');
            }
            else if(stack.isEmpty()||c!=stack.pop()){
                return false;
            }
        }
        return stack.isEmpty();
    }

}
