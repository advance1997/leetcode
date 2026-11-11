package learning.datastructures.linkedlist;

import java.util.Arrays;

/**
 * 非循环、无哨兵的整数双向链表，维护 head、tail 和有效节点数。
 *
 * <p>每个节点保存前驱与后继，允许重复值及任意 int 值。空表的 head、tail 均为 null；
 * 非空表满足 head.previous == null、tail.next == null，相邻节点的前后连接互相对应。
 * 节点不向外暴露，本类不支持并发修改，不对应某一道 LeetCode 题目。</p>
 *
 * <p>读取、删除下标为 [0, size)，插入下标为 [0, size]。非法下标抛出
 * IndexOutOfBoundsException 且不改变内容；空表首尾删除同样抛出该异常。
 * 头尾插删为 O(1)，按下标操作从较近端定位，最坏 O(n)；反转 O(n) 时间、O(1) 辅助空间。
 * 链表总存储 O(n)，数组快照占用 O(n) 返回空间。计数上限为 Integer.MAX_VALUE，
 * 实际节点数量还受可用内存限制。</p>
 *
 * <pre>
 * 依次 insert(0, 10)、insert(1, 30)、insert(1, 20)：[10, 20, 30]
 * 倒序快照：[30, 20, 10]
 * remove(1) 返回 20，reverse() 后为 [30, 10]
 * </pre>
 *
 * <p>采用 Java 8 已有语法与 API，完整图解见 DoublyLinkedList.md。</p>
 */
public final class DoublyLinkedList {
    /** 首个有效节点，空表为 null。 */
    private Node head;
    /** 最后一个有效节点，空表为 null。 */
    private Node tail;
    /** 有效节点数。 */
    private int size;

    /** 创建空双向链表。 */
    public DoublyLinkedList() {
    }

    /**
     * 常数时间读取节点数量。
     * @return 有效节点数
     */
    public int size() {
        return size;
    }

    /**
     * 从较近的一端定位并读取元素，最坏时间 O(n)。
     * @param index 元素下标，范围为 [0, size)
     * @return 指定位置的整数
     * @throws IndexOutOfBoundsException 如果下标非法
     */
    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    /**
     * 插入元素，首尾位置 O(1)，其余位置最坏 O(n)，新节点空间 O(1)。
     * @param index 插入位置，范围为 [0, size]
     * @param value 新节点的整数值
     * @throws IndexOutOfBoundsException 如果下标非法
     * @throws IllegalStateException 如果 size 已达到 Integer.MAX_VALUE
     */
    public void insert(int index, int value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("插入下标越界：" + index);
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数达到计数上限");
        }

        // 插入到 successor 前；尾插没有后继，前驱可直接从 tail 获取。
        Node successor = index == size ? null : nodeAt(index);
        Node predecessor = successor == null ? tail : successor.previous;
        Node inserted = new Node(value);
        inserted.previous = predecessor;
        inserted.next = successor;

        // 同时修复两侧邻居；缺少邻居时修改对应的端点入口。
        if (predecessor == null) {
            head = inserted;
        } else {
            predecessor.next = inserted;
        }
        if (successor == null) {
            tail = inserted;
        } else {
            successor.previous = inserted;
        }
        size++;
    }

    /**
     * 删除指定节点并返回值，首尾位置 O(1)，其余位置最坏 O(n)。
     * @param index 元素下标，范围为 [0, size)
     * @return 被删除的整数
     * @throws IndexOutOfBoundsException 如果下标非法，包括空表删除
     */
    public int remove(int index) {
        checkElementIndex(index);
        Node removed = nodeAt(index);
        Node predecessor = removed.previous;
        Node successor = removed.next;

        // 让前驱和后继互相连接；删除唯一节点时两个分支都将端点设为 null。
        if (predecessor == null) {
            head = successor;
        } else {
            predecessor.next = successor;
        }
        if (successor == null) {
            tail = predecessor;
        } else {
            successor.previous = predecessor;
        }

        removed.previous = null;
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 常数时间头插。
     * @param value 新元素
     * @throws IllegalStateException 如果节点数量达到计数上限
     */
    public void addFirst(int value) {
        insert(0, value);
    }

    /**
     * 常数时间尾插。
     * @param value 新元素
     * @throws IllegalStateException 如果节点数量达到计数上限
     */
    public void addLast(int value) {
        insert(size, value);
    }

    /**
     * 常数时间头删。
     * @return 被删首元素
     * @throws IndexOutOfBoundsException 如果链表为空
     */
    public int removeFirst() {
        return remove(0);
    }

    /**
     * 常数时间尾删。
     * @return 被删尾元素
     * @throws IndexOutOfBoundsException 如果链表为空
     */
    public int removeLast() {
        return remove(size - 1);
    }

    /**
     * 顺序查找首个匹配，最坏时间 O(n)。
     * @param value 待查找的值
     * @return 首次匹配的下标，无匹配返回 -1
     */
    public int indexOf(int value) {
        Node current = head;
        int index = 0;
        while (current != null) {
            if (current.value == value) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /**
     * 原地交换每个节点的前后连接并交换端点，时间 O(n)、辅助空间 O(1)。
     * 空表和单节点表均适用，节点数不变。
     */
    public void reverse() {
        Node current = head;
        while (current != null) {
            // 保存原后继，使交换连接后仍能沿原顺序处理所有节点。
            Node originalNext = current.next;
            current.next = current.previous;
            current.previous = originalNext;
            current = originalNext;
        }
        Node originalHead = head;
        head = tail;
        tail = originalHead;
    }

    /**
     * 生成正序的独立数组快照，时间和返回空间均为 O(n)。
     * @return 从 head 到 tail 的值，空表返回空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        Node current = head;
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.next;
        }
        return values;
    }

    /**
     * 生成倒序的独立数组快照，沿前驱遍历，时间和返回空间均为 O(n)。
     * @return 从 tail 到 head 的值，不改变链表顺序
     */
    public int[] toReversedArray() {
        int[] values = new int[size];
        Node current = tail;
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.previous;
        }
        return values;
    }

    /**
     * 从较近端寻找节点，调用方须先检查下标。
     * @param index 合法元素下标
     * @return 对应节点
     */
    private Node nodeAt(int index) {
        if (index < size / 2) {
            Node current = head;
            for (int step = 0; step < index; step++) {
                current = current.next;
            }
            return current;
        }

        Node current = tail;
        for (int step = size - 1; step > index; step--) {
            current = current.previous;
        }
        return current;
    }

    /**
     * 检查有效元素下标，在修改结构前拒绝非法输入。
     * @param index 待检查下标
     * @throws IndexOutOfBoundsException 如果下标不在 [0, size) 内
     */
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("元素下标越界：" + index);
        }
    }

    /** 私有节点，防止外部修改引用破坏结构。 */
    private static final class Node {
        /** 节点的整数值。 */
        private final int value;
        /** 前驱，首节点为 null。 */
        private Node previous;
        /** 后继，尾节点为 null。 */
        private Node next;

        /**
         * 创建尚未连接的节点。
         * @param value 节点值
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 运行正反向遍历、插入、删除、反转及删空示例。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        DoublyLinkedList list = new DoublyLinkedList();
        list.addFirst(10);
        list.addLast(30);
        list.insert(1, 20);
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(Arrays.toString(list.toReversedArray()));

        System.out.println(list.remove(1));
        list.reverse();
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(list.removeLast());
        System.out.println(list.removeFirst());
        System.out.println(list.size());
    }
}
