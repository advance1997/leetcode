package simple;

/**
 * 将两个升序链表合并为一个新的 升序 链表并返回。新链表是通过拼接给定的两个链表的所有节点组成的。 
 * 示例：
 *
 * 输入：1->2->4, 1->3->4
 * 输出：1->1->2->3->4->4
 *
 * @program: leetcode
 * @description:
 * @author: zhouh
 * @create: 2020-12-18 16:32
 **/
public class MergeTwoLists {

    public static void main(String[] args) {
        Link<Integer> link = new Link<>();
        link.addNode(1);
        link.addNode(2);
        link.display();
    }

}

class Link<T>{

    Node<Integer> head;

    public Link(){
        this.head = new Node<>();
    }

    /**
     * 新增节点
     * @param val
     */
    public void addNode(int val){
        Node node = new Node(val);
        Node temp = head;
        while (temp.next != null){
            temp = temp.next;
        }
        temp.next = node;
    }

    /**
     * 打印列表
     */
    public void display(){
        Node temp = head;
        while (temp.next != null){
            temp = temp.next;
            System.out.print("{" + temp.val + "} ");
        }
    }

}

class Node<T>{
    Node next;
    T val;

    public Node(){
        this.next = null;
        this.val = null;
    }

    Node(T val){
        this.val = val;
    }

}
