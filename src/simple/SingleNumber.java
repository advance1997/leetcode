package simple;

/**
 * @program: leetcode
 * @description: 给定一个非空整数数组，除了某个元素只出现一次以外，其余每个元素均出现两次。找出那个只出现了一次的元素。
 *
 * 解题思路：
 * a^a = 0 b^b = 0
 * 0^0 = 0
 * 0^a = a
 *
 * 异或交换规律   a^b^c^b^a = (a^a)^(b^b)^a=0^0^a=a
 *
 *
 * @author: zhouh
 * @create: 2020-12-17 18:41
 **/
public class SingleNumber {

    public static void main(String[] args) {
        System.out.println(singleNumber(new int[]{1,2,3,4,5,1,2,3,4}));
    }

    public static int singleNumber(int[] nums) {
        int num = 0;
        for(int i : nums) {
            num = num ^ i;
        }
        return num;
    }

}
