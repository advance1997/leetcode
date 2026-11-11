package learning.datastructures.stack;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 支持按需扩容的整数数组栈教学实现，遵循后进先出，不对应特定 LeetCode 题目。
 *
 * <p>有效元素位于 [0, size)，栈顶下标为 size - 1，数组长度为容量。
 * 允许重复值及任意 int 值。空栈 pop、peek 抛出 NoSuchElementException 且不修改状态。
 * 快照按栈顶到栈底排列，不暴露内部数组；不支持并发修改。</p>
 *
 * <p>push 均摊 O(1)、扩容时 O(n)，pop、peek、size、isEmpty 为 O(1)。
 * toArray 时间及返回空间为 O(n)。栈存储 O(C)，C 为保留容量；弹出不缩容。
 * 计数与容量最大为 Integer.MAX_VALUE，实际可分配容量受 JVM 和内存限制。</p>
 * <pre>
 * push(10)、push(20) 后快照为 [20, 10]；peek() 返回 20 且 size 不变。
 * pop() 返回 20，随后快照为 [10]。
 * </pre>
 * <p>使用 Java 8 已有语法和 API，配套文档为 ArrayStack.md。</p>
 */
public final class ArrayStack {
    /** 默认容量较小，便于学习时观察扩容。 */
    private static final int DEFAULT_CAPACITY = 4;
    /** 存储槽位，弹出后保留容量以供复用。 */
    private int[] elements;
    /** 有效元素数，也是下一次写入位置。 */
    private int size;

    /** 创建默认容量的空栈。 */
    public ArrayStack() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 创建指定初始容量的空栈，允许零容量；初始化成本与容量有关。
     * @param initialCapacity 非负初始容量，不代表有效元素数量
     * @throws IllegalArgumentException 如果容量为负数
     */
    public ArrayStack(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("初始容量不能为负数");
        }
        elements = new int[initialCapacity];
    }

    /**
     * 将值压入栈顶，均摊 O(1)，单次扩容时 O(n)。
     * @param value 待入栈整数
     * @throws IllegalStateException 如果元素数已达到计数上限
     */
    public void push(int value) {
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("元素数达到计数上限");
        }
        ensureCapacity();
        // 先写入首个空槽位，再增加有效长度，保持 [0, size) 连续有效。
        elements[size] = value;
        size++;
    }

    /**
     * 移除并返回栈顶，时间 O(1)，不会缩容。
     * @return 最近入栈且尚未弹出的值
     * @throws NoSuchElementException 如果栈为空
     */
    public int pop() {
        checkNotEmpty();
        size--;
        int value = elements[size];
        // 对 int 数组清零用于显示已弃用槽位；有效范围由 size 而非 0 决定。
        elements[size] = 0;
        return value;
    }

    /**
     * 查看栈顶但不移除，时间 O(1)。
     * @return 当前栈顶值
     * @throws NoSuchElementException 如果栈为空
     */
    public int peek() {
        checkNotEmpty();
        return elements[size - 1];
    }

    /**
     * 返回从栈顶到栈底的独立快照，时间和返回空间 O(n)。
     * @return 快照，空栈返回空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        for (int index = 0; index < size; index++) {
            values[index] = elements[size - 1 - index];
        }
        return values;
    }

    /**
     * 满容量时扩大数组，零容量先增长到 1，之后通常翻倍。
     * 使用 long 计算容量，避免 int 乘法溢出；分配失败前不会覆盖旧数组。
     */
    private void ensureCapacity() {
        if (size < elements.length) {
            return;
        }
        long doubledCapacity = Math.max(1L, (long) elements.length * 2L);
        int newCapacity = (int) Math.min(doubledCapacity, Integer.MAX_VALUE);
        elements = Arrays.copyOf(elements, newCapacity);
    }

    /**
     * 返回有效元素数，时间 O(1)。
     * @return 当前元素数量
     */
    public int size() {
        return size;
    }

    /**
     * 判断是否为空，时间 O(1)。
     * @return 没有有效元素时返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 在访问前检查空栈，不使用任何整数值代表空状态。
     * @throws NoSuchElementException 如果栈为空
     */
    private void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("栈为空");
        }
    }

    /**
     * 演示后进先出、查看栈顶、弹空及复用。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        ArrayStack stack = new ArrayStack();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        System.out.println(Arrays.toString(stack.toArray()));
        System.out.println(stack.peek());
        System.out.println(stack.size());
        System.out.println(stack.pop());
        stack.push(40);
        System.out.println(Arrays.toString(stack.toArray()));
        while (!stack.isEmpty()) {
            System.out.println(stack.pop());
        }
        System.out.println(stack.isEmpty());
        stack.push(0);
        System.out.println(stack.peek());
    }
}
