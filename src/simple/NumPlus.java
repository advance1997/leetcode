package simple;

/**
 * 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回他们的数组下标。
	你可以假设每种输入只会对应一个答案。但是，你不能重复利用这个数组中同样的元素。
	示例:
	给定 nums = [2, 7, 11, 15], target = 9
	因为 nums[0] + nums[1] = 2 + 7 = 9
	所以返回 [0, 1]
     * @ClassName: NumPlus  
     * @Description: TODO
     * @author advance  
     * @date 2019年12月18日  
     *
 */
public class NumPlus {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] nums ={ 4,2,6,5,3};
		int[] results = twoSum(nums, 6);
		for (int i = 0; i < results.length; i++) {
			System.out.println(results[i]);
		}
	}
	
	public static int[] twoSum(int[] nums, int target) {
        int[] returnData = new int[2];
        for(int i = 0; i < nums.length ; i++){
        	for(int j = 0; j < nums.length ; j++){ 
        		if(j == i){
        			continue;
        		}
        		int sum = nums[i] + nums[j];
        		if(sum == target){
        			returnData[0] = i;
        			returnData[1] = j;
        			return returnData;
        		}
        	}
        }
        return returnData;
    }
	
	public static int[] twoSum2(int[] nums, int target) {
        for(int i = 0; i < nums.length ; i++){
        	for(int j = 0; j < nums.length; j++){      
        		if(i == j){
        			continue;
        		}
        		if(nums[i] == target - nums[j]){
        			return new int[]{i , j};
        		}
        	}
        }
        return new int[]{};
    }
}
