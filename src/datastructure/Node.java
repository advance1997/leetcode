package datastructure;

/**
 * @program: leetcode
 * @description: 简单链表节点类
 * @author: zhouh
 * @create: 2019-12-19 16:09
 **/
public class Node {

    Object val;

    Node next;

    public Node(){
        this.val = null;
        this.next = null;
    }

    public Node(Object val){
        this.val = val;
    }

}
