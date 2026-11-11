package learning.datastructures.stack;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * 基于单链表头插、头删的整数链式栈教学实现，不对应特定 LeetCode 题目。
 *
 * <p>top 指向栈顶，沿 next 按栈顶到栈底排列，末节点 next 为 null。
 * 空栈 top 为 null 且 size 为 0。允许重复值及任意 int 值，不暴露节点。
 * 空栈 pop、peek 抛出 NoSuchElementException 且不改变状态，不支持并发修改。</p>
 *
 * <p>push、pop、peek、size、isEmpty 在通常节点分配成本模型下均为 O(1)。
 * 快照时间及返回空间 O(n)，有效节点存储 O(n)，每次 push 分配一个节点。
 * 计数上限为 Integer.MAX_VALUE，实际数量受可用内存限制。</p>
 * <pre>
 * push(10)、push(20) 后：top → 20 → 10 → null。
 * peek() 返回 20 不移除；pop() 返回 20，top 随后指向 10。
 * </pre>
 * <p>使用 Java 8 已有语法和 API，配套文档为 LinkedStack.md。</p>
 */
public final class LinkedStack {
    /** 最近入栈且尚未弹出的节点，空栈为 null。 */
    private Node top;
    /** 有效节点数量。 */
    private int size;

    /** 创建空链式栈。 */
    public LinkedStack() {
    }

    /**
     * 通过头插压入值，时间 O(1)，新增一个节点。
     * @param value 待入栈整数
     * @throws IllegalStateException 如果元素数已达到计数上限
     */
    public void push(int value) {
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("元素数达到计数上限");
        }
        // 新节点先连向旧栈顶，再更新入口，不需要扫描或维护尾指针。
        Node inserted = new Node(value, top);
        top = inserted;
        size++;
    }

    /**
     * 通过头删移除栈顶，时间 O(1)。
     * @return 最近入栈且尚未弹出的整数
     * @throws NoSuchElementException 如果栈为空
     */
    public int pop() {
        checkNotEmpty();
        Node removed = top;
        top = removed.next;
        removed.next = null;
        size--;
        return removed.value;
    }

    /**
     * 查看栈顶而不修改连接，时间 O(1)。
     * @return 栈顶值
     * @throws NoSuchElementException 如果栈为空
     */
    public int peek() {
        checkNotEmpty();
        return top.value;
    }

    /**
     * 按栈顶到栈底生成独立快照，时间和返回空间 O(n)。
     * @return 整数数组，空栈返回空数组
     */
    public int[] toArray() {
        int[] values = new int[size];
        Node current = top;
        for (int index = 0; index < size; index++) {
            values[index] = current.value;
            current = current.next;
        }
        return values;
    }

    /** 私有节点，不允许外部修改链表。 */
    private static final class Node {
        /** 不可变的整数值。 */
        private final int value;
        /** 更早入栈的下一个节点，栈底为 null。 */
        private Node next;

        /**
         * 创建并连接节点。
         * @param value 新值
         * @param next 原栈顶，可以为 null
         */
        private Node(int value, Node next) {
            this.value = value;
            this.next = next;
        }
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
        LinkedStack stack = new LinkedStack();
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
