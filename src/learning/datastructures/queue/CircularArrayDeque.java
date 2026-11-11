package learning.datastructures.queue;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 可扩容环形数组双端队列教学实现，不对应特定 LeetCode 题目。
 *
 * <p>使用 head 与 size，逻辑偏移 i 映射为 (head+i) mod C；空时 head=0，容量可以为 0。</p>
 * <p>支持任意 int 值和重复值，快照从首到尾且独立。空时查看或删除抛出
 * NoSuchElementException，失败不改变状态。不支持并发修改，不暴露存储或节点。</p>
 * <p>加入均摊 O(1)、扩容单次 O(n)，查看和删除 O(1)。存储 O(C)，C 为保留容量，删除不缩容。
 * 快照时间和返回空间 O(n)。计数上限 Integer.MAX_VALUE，实际规模受 JVM 和内存限制。</p>
 * <pre>
 * addLast(10)、addLast(20) 后：[10, 20]。
 * removeFirst() 返回 10，peekFirst() 随后返回 20。
 * </pre>
 * <p>使用 Java 8 已有语法与 API，配套文档为 CircularArrayDeque.md。</p>
 */
public final class CircularArrayDeque {

    /** 默认容量为 4，便于观察回绕与扩容。 */
    private static final int DEFAULT_CAPACITY = 4;
    /** 底层槽位，出队不缩容。 */
    private int[] elements;
    /** 队首物理下标，空队列规范为 0。 */
    private int head;
    /** 有效元素数量，用于区分空与满。 */
    private int size;

    /** 创建默认容量的空容器。 */
    public CircularArrayDeque() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 创建指定容量的空容器，允许零容量。
     * @param initialCapacity 非负初始容量，不是有效元素数
     * @throws IllegalArgumentException 如果容量为负数
     */
    public CircularArrayDeque(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("初始容量不能为负数");
        }
        elements = new int[initialCapacity];
    }

    /**
     * 在尾端加入值，均摊 O(1)，扩容时 O(n)。
     * @param value 任意 int 值，允许重复
     * @throws IllegalStateException 如果计数已达到上限
     */
    public void addLast(int value) {
        ensureCapacity();
        // size 是尾后空位的逻辑偏移，扩容保证该槽位可写。
        elements[physicalIndex(size)] = value;
        size++;
    }

    /**
     * 移除队首，时间 O(1)，无需搬移其余元素。
     * @return 原首元素
     * @throws NoSuchElementException 如果为空
     */
    public int removeFirst() {
        checkNotEmpty();
        int value = elements[head];
        elements[head] = 0;
        head = head == elements.length - 1 ? 0 : head + 1;
        size--;
        if (size == 0) {
            head = 0;
        }
        return value;
    }

    /**
     * 查看队首但不移除，时间 O(1)。
     * @return 首元素
     * @throws NoSuchElementException 如果为空
     */
    public int peekFirst() {
        checkNotEmpty();
        return elements[head];
    }

    /**
     * 返回从首到尾的独立数组，时间与返回空间 O(n)。
     * @return 逻辑顺序快照，空时为空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        for (int index = 0; index < size; index++) {
            values[index] = elements[physicalIndex(index)];
        }
        return values;
    }

    /**
     * 将逻辑偏移映射到物理槽位，调用方保证容量非零且偏移合法。
     * 使用 long 加法避免 head + offset 的 int 溢出。
     * @param offset 相对于 head 的非负偏移
     * @return 环形数组下标
     */
    private int physicalIndex(int offset) {
        return (int) (((long) head + offset) % elements.length);
    }

    /**
     * 满时按逻辑顺序复制到新数组，零容量先增长到 1，通常翻倍。
     * @throws IllegalStateException 如果计数已达到 Integer.MAX_VALUE
     */
    private void ensureCapacity() {
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("元素数达到计数上限");
        }
        if (size < elements.length) {
            return;
        }
        int newCapacity = (int) Math.min(Integer.MAX_VALUE,
                Math.max(1L, (long) elements.length * 2L));
        int[] expanded = new int[newCapacity];
        // 不能直接复制物理数组，否则回绕后的逻辑顺序会被打乱。
        for (int index = 0; index < size; index++) {
            expanded[index] = elements[physicalIndex(index)];
        }
        elements = expanded;
        head = 0;
    }

    /**
     * 在首端加入值，均摊 O(1)，扩容时 O(n)。
     * @param value 待加入值
     * @throws IllegalStateException 如果计数已达到上限
     */
    public void addFirst(int value) {
        ensureCapacity();
        // 避免 (head - 1) % capacity 在 Java 中产生负余数。
        head = head == 0 ? elements.length - 1 : head - 1;
        elements[head] = value;
        size++;
    }

    /**
     * 移除尾元素，时间 O(1)。
     * @return 原尾元素
     * @throws NoSuchElementException 如果为空
     */
    public int removeLast() {
        checkNotEmpty();
        int tailIndex = physicalIndex(size - 1);
        int value = elements[tailIndex];
        elements[tailIndex] = 0;
        size--;
        if (size == 0) {
            head = 0;
        }
        return value;
    }

    /**
     * 查看尾元素但不删除，时间 O(1)。
     * @return 尾元素
     * @throws NoSuchElementException 如果为空
     */
    public int peekLast() {
        checkNotEmpty();
        return elements[physicalIndex(size - 1)];
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
        CircularArrayDeque queue = new CircularArrayDeque();
        queue.addLast(10);
        queue.addLast(20);
        queue.addLast(30);
        System.out.println(Arrays.toString(queue.toArray()));
        System.out.println(queue.removeFirst());
        queue.addLast(40);
        queue.addFirst(5);
        System.out.println(queue.removeLast());
        System.out.println(Arrays.toString(queue.toArray()));
        while (!queue.isEmpty()) {
            System.out.println(queue.removeFirst());
        }
        System.out.println(queue.size());
        queue.addLast(0);
        System.out.println(queue.peekFirst());
    }
}
