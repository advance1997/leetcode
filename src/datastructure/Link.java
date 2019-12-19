package datastructure;

/**
 * @program: leetcode
 * @description: 简单链表
 * @author: zhouh
 * @create: 2019-12-19 15:29
 **/
public class Link {

    Node head;

    public Link(){
        this.head = new Node();
    }

    public void addNode(Object val){
        Node node = new Node(val);
        Node temp = head;
        while (temp.next != null){
            temp = temp.next;
        }
        temp.next = node;
    }

    public void displayLink(){
        //设置临时节点
        Node temp = head;
        //开始遍历
        while(temp.next != null){
            //输出节点
            System.out.print("{ "+ temp.next.val+" }  ");
            //开始下一个节点
            temp = temp.next;
        }
    }

    public static void main(String[] args) {
        Link link = new Link();
        link.addNode("周韩");
        link.addNode("周舟");
        link.addNode("江流");
        link.displayLink();
    }

}
