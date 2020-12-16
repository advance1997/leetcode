package simple;

/**
 * 实现 strStr() 函数。
 * 给定一个 haystack 字符串和一个 needle 字符串，在 haystack 字符串中找出 needle 字符串出现的第一个位置 (从0开始)。如果不存在，则返回  -1。
 * 示例 1:
 * 输入: haystack = "hello", needle = "ll"
 * 输出: 2
 * 示例 2:
 * 输入: haystack = "aaaaa", needle = "bba"
 * 输出: -1
 * 说明:
 * 当 needle 是空字符串时，我们应当返回什么值呢？这是一个在面试中很好的问题。
 */
public class LastIndexStr {

    public static void main(String[] args) {
        System.out.println(strStr("", ""));
    }

    public static int strStr(String haystack, String needle) {
        if(haystack.length() == 0 && needle.length() == 0){
            return 0;
        }

        if(haystack.length() == 0){
            return -1;
        }

        if(needle.length() == 0){
            return 0;
        }
        char[] haystacks = haystack.toCharArray();
        char[] needles = needle.toCharArray();
        //通过双层遍历对比是否存在一样的字符串
        //外层遍历
        flag : for(int i = 0 ; i <= haystacks.length - needles.length ; i++){
            //内层遍历
            for(int j = 0 ; j < needles.length; j++){
                //字符相等则开始内圈循环，如果不同的话直接跳出循环
                if(haystacks[i+j] != needles[j]){
                    continue flag;
                }
                if(j == needles.length - 1){
                    return i;
                }
            }
        }
        return -1;
    }

}
