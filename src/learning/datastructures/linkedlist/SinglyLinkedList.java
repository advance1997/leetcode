package learning.datastructures.linkedlist;

import java.util.Arrays;

/**
 * 带哨兵节点的整数单链表教学实现，不对应特定 LeetCode 题目。
 *
 * <p>节点保存一个 int 值及后继引用，允许重复值和任意 int 值。
 * 哨兵不代表有效数据，size 等于从哨兵后继可达的有效节点数；末节点后继为 null。
 * 不暴露节点，不维护尾指针，不支持并发修改。</p>
 *
 * <p>支持按下标读取、插入、删除，按值查找、原地反转和数组快照。
 * 读取和删除下标范围为 [0, size)，插入为 [0, size]，非法下标抛出
 * IndexOutOfBoundsException，且不改变原链表。按值查找返回首个匹配下标，无匹配返回 -1。</p>
 *
 * <p>头部插入、删除为 O(1)，一般下标操作与按值查找最坏为 O(n)。
 * 原地反转时间 O(n)、辅助空间 O(1)；快照时间和返回空间均为 O(n)。
 * 链表存储占用 O(n)。教学容量最多 Integer.MAX_VALUE 个节点，实际还受可用内存限制。</p>
 *
 * <pre>
 * 插入 10、30，再向下标 1 插入 20：[10, 20, 30]
 * 删除下标 1：返回 20，剩余 [10, 30]
 * 反转：[30, 10]
 * </pre>
 *
 * <p>使用 Java 8 已有语法及 API；完整概念与操作图解见数据结构专题。</p>
 */
public final class SinglyLinkedList {

    /** 不计入 size 的固定哨兵，值没有业务含义。 */
    private final Node sentinel = new Node(0);

    /** 有效节点个数，空链表为 0。 */
    private int size;

    /** 创建空链表，哨兵后继默认为 null。 */
    public SinglyLinkedList() {
    }

    /**
     * 返回有效节点数量，时间 O(1)。
     *
     * @return 当前有效节点数量
     */
    public int size() {
        return size;
    }

    /**
     * 顺着后继引用读取指定位置的值，最坏时间 O(n)。
     *
     * @param index 元素下标，范围为 [0, size)
     * @return 指定位置保存的整数
     * @throws IndexOutOfBoundsException 如果下标不在合法范围
     */
    public int get(int index) {
        checkElementIndex(index);
        return predecessor(index).next.value;
    }

    /**
     * 在指定位置插入新节点，原位置及后续节点的逻辑下标加一。
     *
     * <p>头插为 O(1)，其余位置需要寻找前驱，最坏 O(n)；新增一个节点占用 O(1) 空间。</p>
     *
     * @param index 插入下标，范围为 [0, size]，size 表示末尾
     * @param value 新节点保存的整数，允许重复
     * @throws IndexOutOfBoundsException 如果下标不在合法范围
     * @throws IllegalStateException 如果节点数已达到 Integer.MAX_VALUE
     */
    public void insert(int index, int value) {
        // 插入允许 index == size，但不能跳过已有序列形成空洞。
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("插入下标越界：" + index);
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数已达到计数上限");
        }

        Node previous = predecessor(index);
        Node inserted = new Node(value);
        // 先保留原后继，再让前驱指向新节点，保证原后缀不会丢失。
        inserted.next = previous.next;
        previous.next = inserted;
        size++;
    }

    /**
     * 删除指定位置的节点并返回其值，头删 O(1)，最坏时间 O(n)。
     *
     * @param index 元素下标，范围为 [0, size)
     * @return 被删除节点保存的整数
     * @throws IndexOutOfBoundsException 如果下标不在合法范围
     */
    public int remove(int index) {
        checkElementIndex(index);

        Node previous = predecessor(index);
        Node removed = previous.next;
        // 前驱直接连接被删节点的后继；末节点的后继为 null，同样适用。
        previous.next = removed.next;
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 从头查找第一个值相等的节点，最坏时间 O(n)，辅助空间 O(1)。
     *
     * @param value 待查找的整数
     * @return 首次匹配的下标；没有匹配时返回 -1
     */
    public int indexOf(int value) {
        Node current = sentinel.next;
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
     * 原地反转全部有效节点，时间 O(n)、辅助空间 O(1)，size 不变。
     *
     * <p>previous 指向已经反转的前缀，current 指向尚未处理的后缀。
     * 空链表和单节点链表也适用，不创建替代节点。</p>
     */
    public void reverse() {
        Node previous = null;
        Node current = sentinel.next;
        while (current != null) {
            // 必须在修改 next 前保存原后继，否则无法继续访问未处理部分。
            Node next = current.next;
            current.next = previous;
            previous = current;
            current = next;
        }
        // 最后处理的节点成为新表头，哨兵本身不参与反转。
        sentinel.next = previous;
    }

    /**
     * 创建按链表顺序排列的独立数组快照，时间和返回空间均为 O(n)。
     *
     * @return 新数组，修改数组不会改变链表；空链表返回空数组
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
     * 找到合法位置的前驱；下标 0 的前驱就是哨兵。
     *
     * @param index 已由调用方检查的位置，范围为 [0, size]
     * @return 指定位置的前驱节点
     */
    private Node predecessor(int index) {
        Node previous = sentinel;
        // 前进 index 次后到达前驱，不是目标节点本身。
        for (int step = 0; step < index; step++) {
            previous = previous.next;
        }
        return previous;
    }

    /**
     * 在任何修改发生前验证读取或删除下标。
     *
     * @param index 待验证的元素下标
     * @throws IndexOutOfBoundsException 如果下标不在 [0, size) 内
     */
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("元素下标越界：" + index);
        }
    }

    /** 仅供链表内部使用的节点，避免外部修改连接破坏不变量。 */
    private static final class Node {
        /** 节点保存的整数，构造后不再修改。 */
        private final int value;
        /** 后继节点；末节点为 null。 */
        private Node next;

        /**
         * 创建尚未连接后继的节点。
         *
         * @param value 节点保存的整数
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 演示插入、查找、删除和反转，可直接作为学习入口运行。
     *
     * @param args 命令行参数，本示例不使用
     */
    public static void main(String[] args) {
        SinglyLinkedList list = new SinglyLinkedList();
        list.insert(0, 10);
        list.insert(1, 30);
        list.insert(1, 20);
        System.out.println(Arrays.toString(list.toArray()));

        // 下标读取与按值查找具有不同的输入和返回语义。
        System.out.println(list.get(1));
        System.out.println(list.indexOf(30));
        System.out.println(list.remove(1));
        System.out.println(Arrays.toString(list.toArray()));

        list.reverse();
        System.out.println(Arrays.toString(list.toArray()));
        System.out.println(list.size());
    }
}
