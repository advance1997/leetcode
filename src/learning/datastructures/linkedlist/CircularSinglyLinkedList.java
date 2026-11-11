package learning.datastructures.linkedlist;

import java.util.Arrays;

/**
 * 维护尾指针的非哨兵循环单链表教学实现，不对应特定 LeetCode 题目。
 *
 * <p>空表 tail 为 null；非空表的 head 由 tail.next 得到，末节点连接首节点。
 * 单节点的 next 指向自身；沿 next 恰好走 size 步回到起点，节点不向外暴露。
 * 支持任意 int 值及重复值，不支持并发修改。</p>
 *
 * <p>插入位置为 [0, size]，读取与删除位置为 [0, size)，非法下标抛出
 * IndexOutOfBoundsException 且不改变结构。计数上限为 Integer.MAX_VALUE，
 * 达到上限时插入抛出 IllegalStateException，实际规模还受内存约束。</p>
 *
 * <p>头插、尾插、头删为 O(1)，尾删与一般按下标操作最坏 O(n)。
 * 查找、快照及原地反转时间 O(n)，除 O(n) 返回快照外辅助空间 O(1)，
 * 插入分配一个节点，整体存储 O(n)。循环连接不等于能常数时间定位前驱。</p>
 *
 * <pre>
 * 尾插 10、30，在下标 1 插入 20：[10, 20, 30]，30.next 指向 10。
 * 删除下标 1 返回 20，反转后：[30, 10]，10.next 指向 30。
 * </pre>
 * <p>使用 Java 8 已有语法与 API，配套文档为 CircularSinglyLinkedList.md。</p>
 */
public final class CircularSinglyLinkedList {
    /** 尾节点；非空时 tail.next 是逻辑头节点。 */
    private Node tail;
    /** 有效节点数量。 */
    private int size;

    /** 创建空循环单链表。 */
    public CircularSinglyLinkedList() {
    }

    /**
     * 按下标读取，最坏 O(n)，尾下标可直接读取。
     * @param index 合法范围为 [0, size)
     * @return 对应值
     * @throws IndexOutOfBoundsException 如果下标非法
     */
    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    /**
     * 在指定位置插入，首尾 O(1)，中间最坏 O(n)。
     * @param index 范围为 [0, size]
     * @param value 新节点的值
     * @throws IndexOutOfBoundsException 如果下标非法
     * @throws IllegalStateException 如果计数达到上限
     */
    public void insert(int index, int value) {
        checkInsertIndex(index);
        Node inserted = new Node(value);
        if (size == 0) {
            // 第一个节点必须自环，不能把 next 留为 null。
            inserted.next = inserted;
            tail = inserted;
        } else {
            // 头插与尾插都在旧 tail 后改线，区别在于是否移动逻辑尾入口。
            Node previous = index == 0 || index == size ? tail : nodeAt(index - 1);
            inserted.next = previous.next;
            previous.next = inserted;
            if (index == size) {
                tail = inserted;
            }
        }
        size++;
    }

    /**
     * 删除指定位置，头删 O(1)，尾删与中间删除最坏 O(n)。
     * @param index 范围为 [0, size)
     * @return 被删除的值
     * @throws IndexOutOfBoundsException 如果下标非法或为空表
     */
    public int remove(int index) {
        checkElementIndex(index);
        Node previous = index == 0 ? tail : nodeAt(index - 1);
        Node removed = previous.next;
        if (size == 1) {
            // 唯一节点同时是头尾，删除后必须恢复空表入口。
            tail = null;
        } else {
            previous.next = removed.next;
            if (removed == tail) {
                tail = previous;
            }
        }
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 查找首个匹配下标，只访问 size 个节点，最坏 O(n)。
     * @param value 待查找的值
     * @return 首个匹配下标，未找到返回 -1
     */
    public int indexOf(int value) {
        Node current = tail == null ? null : tail.next;
        for (int index = 0; index < size; index++) {
            if (current.value == value) {
                return index;
            }
            current = current.next;
        }
        return -1;
    }

    /**
     * 返回从逻辑头开始的一圈独立快照，时间及返回空间 O(n)。
     * @return 正序数组，空表返回空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        Node current = tail == null ? null : tail.next;
        // 环中不会遇到 null，必须明确限制访问次数。
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.next;
        }
        return values;
    }

    /**
     * 原地反转所有边并重新定位尾入口，时间 O(n)、辅助空间 O(1)。
     * 空表和单节点表无需修改，size 不变。
     */
    public void reverse() {
        if (size < 2) {
            return;
        }
        Node oldHead = tail.next;
        Node previous = tail;
        Node current = oldHead;
        // previous 从旧尾开始，使旧头反向连接旧尾，保留最终闭环边。
        for (int count = 0; count < size; count++) {
            Node originalNext = current.next;
            current.next = previous;
            previous = current;
            current = originalNext;
        }
        // 旧头成为新尾，新尾的 next 已经指向旧尾（新头）。
        tail = oldHead;
    }

    /**
     * 定位已经校验的下标；尾节点直接返回，其他位置从头前进。
     * @param index 合法元素下标
     * @return 对应节点
     */
    private Node nodeAt(int index) {
        if (index == size - 1) {
            return tail;
        }
        Node current = tail.next;
        for (int step = 0; step < index; step++) {
            current = current.next;
        }
        return current;
    }

    /**
     * 返回有效节点数，时间 O(1)。
     * @return 不包含哨兵（如果有）的节点数
     */
    public int size() {
        return size;
    }

    /**
     * 插入首元素，时间 O(1)。
     * @param value 新值，允许重复及任意 int 值
     * @throws IllegalStateException 如果计数达到上限
     */
    public void addFirst(int value) {
        insert(0, value);
    }

    /**
     * 插入尾元素，时间 O(1)。
     * @param value 新值，允许重复及任意 int 值
     * @throws IllegalStateException 如果计数达到上限
     */
    public void addLast(int value) {
        insert(size, value);
    }

    /**
     * 删除首元素，时间 O(1)。
     * @return 被删除的值
     * @throws IndexOutOfBoundsException 如果为空表
     */
    public int removeFirst() {
        return remove(0);
    }

    /**
     * 删除尾元素，复杂度见类注释。
     * @return 被删除的值
     * @throws IndexOutOfBoundsException 如果为空表
     */
    public int removeLast() {
        return remove(size - 1);
    }

    /**
     * 验证已有元素下标，先检查再修改，避免异常改变结构。
     * @param index 待验证下标
     * @throws IndexOutOfBoundsException 如果不在 [0, size) 内
     */
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("元素下标越界：" + index);
        }
    }

    /**
     * 验证插入位置与计数容量。
     * @param index 允许范围为 [0, size]
     * @throws IndexOutOfBoundsException 如果插入位置非法
     * @throws IllegalStateException 如果计数已达到 Integer.MAX_VALUE
     */
    private void checkInsertIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("插入下标越界：" + index);
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数达到计数上限");
        }
    }

    /** 私有节点，防止外部改线破坏闭环。 */
    private static final class Node {
        /** 不可变整数值。 */
        private final int value;
        /** 后继引用。 */
        private Node next;

        /**
         * 创建尚未接入环的节点。
         * @param value 节点值
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 演示插入、查找、删除、反转、删空与重新插入。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        CircularSinglyLinkedList list = new CircularSinglyLinkedList();
        list.addLast(10);
        list.addLast(30);
        list.insert(1, 20);
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(list.indexOf(20));
        System.out.println(list.remove(1));
        list.reverse();
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(list.removeLast());
        System.out.println(list.removeFirst());
        System.out.println(list.size());
        list.addFirst(0);
        System.out.println(Arrays.toString(list.toArray()));
    }
}
