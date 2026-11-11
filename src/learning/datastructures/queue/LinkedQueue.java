package learning.datastructures.queue;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 带首尾指针的单链表先进先出队列教学实现，不对应特定 LeetCode 题目。
 *
 * <p>空时 head、tail 均为 null；非空时 tail.next 为 null，沿 next 恰好经过 size 个节点。</p>
 * <p>支持任意 int 值和重复值，快照从首到尾且独立。空时查看或删除抛出
 * NoSuchElementException，失败不改变状态。不支持并发修改，不暴露存储或节点。</p>
 * <p>首尾接口均为 O(1)（通常节点分配模型），每次加入分配一个节点，总存储 O(n)。
 * 快照时间和返回空间 O(n)。计数上限 Integer.MAX_VALUE，实际规模受 JVM 和内存限制。</p>
 * <pre>
 * addLast(10)、addLast(20) 后：[10, 20]。
 * removeFirst() 返回 10，peekFirst() 随后返回 20。
 * </pre>
 * <p>使用 Java 8 已有语法与 API，配套文档为 LinkedQueue.md。</p>
 */
public final class LinkedQueue {

    /** 首个节点，空时为 null。 */
    private Node head;
    /** 最后一个节点，空时为 null。 */
    private Node tail;
    /** 有效节点数量。 */
    private int size;

    /** 创建空容器。 */
    public LinkedQueue() {
    }

    /**
     * 尾插节点，时间 O(1)，新增一个节点。
     * @param value 任意 int 值，允许重复
     * @throws IllegalStateException 如果计数已达到上限
     */
    public void addLast(int value) {
        checkCapacity();
        Node inserted = new Node(value);
        // 使用 tail 直接连接新节点，避免从 head 扫描。
        if (tail == null) {
            head = inserted;
        } else {
            tail.next = inserted;
        }
        tail = inserted;
        size++;
    }

    /**
     * 头删节点，时间 O(1)；删空时同步清理 tail。
     * @return 原首元素
     * @throws NoSuchElementException 如果为空
     */
    public int removeFirst() {
        checkNotEmpty();
        Node removed = head;
        head = removed.next;
        if (head == null) {
            tail = null;
        }
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 查看首元素，时间 O(1)。
     * @return 首元素
     * @throws NoSuchElementException 如果为空
     */
    public int peekFirst() {
        checkNotEmpty();
        return head.value;
    }

    /**
     * 返回从首到尾的独立快照，时间与返回空间 O(n)。
     * @return 快照，空时为空数组
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
     * 防止有效节点计数溢出。
     * @throws IllegalStateException 如果计数已达到上限
     */
    private void checkCapacity() {
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("元素数达到计数上限");
        }
    }

    /** 私有节点，禁止外部改线。 */
    private static final class Node {
        /** 节点值。 */
        private final int value;
        /** 后继，末节点为 null。 */
        private Node next;


        /**
         * 创建未连接的节点。
         * @param value 节点值
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 常数时间读取有效元素数。
     * @return 有效元素数量
     */
    public int size() {
        return size;
    }

    /**
     * 常数时间判断空状态。
     * @return 无元素时返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 在修改前拒绝空容器读取或删除。
     * @throws NoSuchElementException 如果为空
     */
    private void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("队列为空");
        }
    }

    /**
     * 演示首尾操作、删除后复用和逻辑快照。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        LinkedQueue queue = new LinkedQueue();
        queue.addLast(10);
        queue.addLast(20);
        queue.addLast(30);
        System.out.println(Arrays.toString(queue.toArray()));
        System.out.println(queue.removeFirst());
        queue.addLast(40);
        // 按先进先出的顺序消费剩余元素。
        System.out.println(Arrays.toString(queue.toArray()));
        while (!queue.isEmpty()) {
            System.out.println(queue.removeFirst());
        }
        System.out.println(queue.size());
        queue.addLast(0);
        System.out.println(queue.peekFirst());
    }
}
