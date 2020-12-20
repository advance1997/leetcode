package middle;

import datastructure.Link;

/**
 * 给出两个 非空 的链表用来表示两个非负的整数。其中，它们各自的位数是按照 逆序 的方式存储的，
 	并且它们的每个节点只能存储 一位 数字。
	如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。
	您可以假设除了数字 0 之外，这两个数都不会以 0 开头。
	示例：
	输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
	输出：7 -> 0 -> 8
	原因：342 + 465 = 807
     * @ClassName: TwoNumbersAdd  
     * @Description: TODO
     * @author advance  
     * @date 2019年12月18日  
     *
 */
public class TwoNumbersAdd {

	static Link link1;

	static Link link2;

	static{
		link1 = new Link();
		link1.addNode(2);
		link1.addNode(4);
		link1.addNode(3);
		link2 = new Link();
		link2.addNode(5);
		link2.addNode(6);
		link2.addNode(4);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		link1.displayLink();
		System.out.println();
		link2.displayLink();


	}

}
