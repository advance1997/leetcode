package learning.datastructures.linkedlist;

import java.util.Arrays;

/**
 * 单哨兵循环双向链表教学实现，不对应特定 LeetCode 题目。
 *
 * <p>哨兵不保存有效数据、不计入 size。空表的前后引用均指向哨兵自身；非空时
 * sentinel.next 为首节点，sentinel.previous 为尾节点。包含哨兵的相邻节点
 * 互相连接，遍历一圈回到哨兵。不暴露节点，不支持并发修改，允许任意 int 值及重复值。</p>
 *
 * <p>插入下标为 [0, size]，读取和删除为 [0, size)，非法下标抛出
 * IndexOutOfBoundsException 且结构不变；禁止删除哨兵。计数最多 Integer.MAX_VALUE，
 * 到达上限时插入抛出 IllegalStateException，实际规模受内存限制。</p>
 *
 * <p>首尾插删均为 O(1)，一般下标操作从较近端定位，最坏 O(n)。查找和反转
 * 时间 O(n)、辅助空间 O(1)，快照额外返回 O(n) 数组。插入分配一个节点，总存储 O(n)。</p>
 * <pre>
 * 插入 10、20、30：S ⇄ 10 ⇄ 20 ⇄ 30 ⇄ S（两个 S 是同一哨兵）。
 * 删除 20 后反转：S ⇄ 30 ⇄ 10 ⇄ S。
 * </pre>
 * <p>使用 Java 8 已有语法与 API，配套文档为 CircularDoublyLinkedList.md。</p>
 */
public final class CircularDoublyLinkedList {
    /** 固定哨兵，值为占位值而不是有效数据。 */
    private final Node sentinel = new Node(0);
    /** 不含哨兵的有效节点数。 */
    private int size;

    /** 创建空表，让哨兵的两个引用指向自身。 */
    public CircularDoublyLinkedList() {
        sentinel.next = sentinel;
        sentinel.previous = sentinel;
    }

    /**
     * 从较近端定位并读取，最坏 O(n)。
     * @param index 范围为 [0, size)
     * @return 对应值
     * @throws IndexOutOfBoundsException 如果下标非法
     */
    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    /**
     * 插入指定位置，首尾 O(1)，中间最坏 O(n)。
     * @param index 范围为 [0, size]
     * @param value 新值，允许 0 和重复值
     * @throws IndexOutOfBoundsException 如果下标非法
     * @throws IllegalStateException 如果计数达到上限
     */
    public void insert(int index, int value) {
        checkInsertIndex(index);
        // 尾插在哨兵之前；空表同样使用哨兵作为后继，无需空表分支。
        Node successor = index == size ? sentinel : nodeAt(index);
        Node predecessor = successor.previous;
        Node inserted = new Node(value);
        inserted.previous = predecessor;
        inserted.next = successor;
        predecessor.next = inserted;
        successor.previous = inserted;
        size++;
    }

    /**
     * 删除指定元素，首尾 O(1)，中间最坏 O(n)。
     * @param index 范围为 [0, size)
     * @return 被删除的值
     * @throws IndexOutOfBoundsException 如果下标非法或为空表
     */
    public int remove(int index) {
        // 即使哨兵一直存在，空表仍然没有可删除的业务元素。
        checkElementIndex(index);
        Node removed = nodeAt(index);
        removed.previous.next = removed.next;
        removed.next.previous = removed.previous;
        // 唯一元素删除时，上述两句自动恢复哨兵的双向自环。
        removed.previous = null;
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 查找首次匹配，遇到哨兵终止，最坏 O(n)。
     * @param value 待查找值
     * @return 首次匹配下标，未找到返回 -1
     */
    public int indexOf(int value) {
        Node current = sentinel.next;
        int index = 0;
        while (current != sentinel) {
            if (current.value == value) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    /**
     * 返回正向独立数组快照，时间与返回空间均为 O(n)。
     * @return 首到尾的值，空表返回空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        Node current = sentinel.next;
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.next;
        }
        return values;
    }

    /**
     * 沿前驱返回倒序独立快照，不改变链表，时间与返回空间均为 O(n)。
     * @return 尾到首的值，空表返回空数组
     */
    public int[] toReversedArray() {
        int[] values = new int[size];
        Node current = sentinel.previous;
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.previous;
        }
        return values;
    }

    /**
     * 交换包含哨兵在内的所有节点前后引用，时间 O(n)、辅助空间 O(1)。
     * 空表也会访问一次哨兵，交换后仍是双向自环，size 不变。
     */
    public void reverse() {
        Node current = sentinel;
        do {
            Node originalNext = current.next;
            current.next = current.previous;
            current.previous = originalNext;
            current = originalNext;
        } while (current != sentinel);
    }

    /**
     * 从较近一端定位，前置条件是不访问哨兵下标。
     * @param index 已校验的有效下标
     * @return 有效节点
     */
    private Node nodeAt(int index) {
        if (index < size / 2) {
            Node current = sentinel.next;
            for (int step = 0; step < index; step++) {
                current = current.next;
            }
            return current;
        }
        Node current = sentinel.previous;
        for (int step = size - 1; step > index; step--) {
            current = current.previous;
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
        /** 前驱引用。 */
        private Node previous;

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
        CircularDoublyLinkedList list = new CircularDoublyLinkedList();
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
