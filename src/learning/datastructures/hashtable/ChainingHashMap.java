package learning.datastructures.hashtable;

import java.util.NoSuchElementException;

/**
 * 整数键到整数值的链地址哈希表教学实现，不对应特定 LeetCode 题目。
 *
 * <p>相同键覆盖值且不增加 size，不同键可以冲突并共存于同一桶的链表。
 * 支持全部 int 键值；不存在的 get/remove 抛出 NoSuchElementException。
 * 快照每行是 [key, value]，顺序不保证，且与内部结构独立。不支持并发修改。</p>
 *
 * <p>容量从 4 开始，始终为二次幂，新键使负载超过 0.75 时翻倍，不缩容、不树化。
 * 最大桶数为 1 &lt;&lt; 30，超过该容量的负载限制时新键插入抛出 IllegalStateException；
 * 已有键仍可更新。实际分配还受内存限制。</p>
 *
 * <p>令 n 为映射数、C 为桶数：哈希分布良好且负载受控时查询/删除期望 O(1)，
 * 插入期望均摊 O(1)；单桶退化时查询/删除 O(n)，扩容或快照 O(C+n)。
 * 存储 O(C+n)，快照返回空间 O(n)。</p>
 * <pre>
 * put(1, 10)、put(5, 50) 可落入同一初始桶，get(5) 返回 50。
 * put(1, 11) 后 size 仍为 2，remove(1) 返回 11。
 * </pre>
 * <p>使用 Java 8 已有语法与 API，配套文档为 ChainingHashMap.md。</p>
 */
public final class ChainingHashMap {
    /** 初始桶数，使用二次幂便于位掩码映射。 */
    private static final int INITIAL_CAPACITY = 4;
    /** 防止二倍扩容发生 int 溢出的容量上限。 */
    private static final int MAX_CAPACITY = 1 << 30;
    /** 桶数组，每个桶保存一条冲突链的头节点。 */
    private Node[] buckets = new Node[INITIAL_CAPACITY];
    /** 不同键的数量。 */
    private int size;

    /** 创建空表。 */
    public ChainingHashMap() {
    }

    /**
     * 新增映射或覆盖已有值，覆盖不改变 size，也不触发扩容。
     * @param key 整数键
     * @param value 整数值
     * @throws IllegalStateException 如果新键需要超过最大允许容量
     */
    public void put(int key, int value) {
        Node existing = findNode(key);
        if (existing != null) {
            existing.value = value;
            return;
        }

        // 先确认新键，避免重复 put 在阈值附近触发无意义扩容。
        if (size >= buckets.length - buckets.length / 4) {
            if (buckets.length == MAX_CAPACITY) {
                throw new IllegalStateException("达到哈希表容量限制");
            }
            resize();
        }
        int index = bucketIndex(key, buckets.length);
        buckets[index] = new Node(key, value, buckets[index]);
        size++;
    }

    /**
     * 查询键对应值。
     * @param key 待查询键
     * @return 对应值
     * @throws NoSuchElementException 如果键不存在
     */
    public int get(int key) {
        Node node = findNode(key);
        if (node == null) {
            throw new NoSuchElementException("键不存在：" + key);
        }
        return node.value;
    }

    /**
     * 检查键存在性，不用任何整数值代表缺失。
     * @param key 待检查键
     * @return 键存在时为 true
     */
    public boolean containsKey(int key) {
        return findNode(key) != null;
    }

    /**
     * 删除映射并返回旧值，缺失时不改变结构。
     * @param key 待删除键
     * @return 原值
     * @throws NoSuchElementException 如果键不存在
     */
    public int remove(int key) {
        int index = bucketIndex(key, buckets.length);
        Node previous = null;
        Node current = buckets[index];
        while (current != null) {
            if (current.key == key) {
                // 桶头没有前驱，其他位置通过前驱绕过目标。
                if (previous == null) {
                    buckets[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                current.next = null;
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }
        throw new NoSuchElementException("键不存在：" + key);
    }

    /**
     * 获取独立键值快照，时间 O(C+n)，返回空间 O(n)，不保证行顺序。
     * @return 每行长度为 2，依次为键和值
     */
    public int[][] entries() {
        int[][] result = new int[size][2];
        int position = 0;
        for (Node bucket : buckets) {
            for (Node node = bucket; node != null; node = node.next) {
                result[position][0] = node.key;
                result[position][1] = node.value;
                position++;
            }
        }
        return result;
    }

    /**
     * 在唯一候选桶内逐项比较真正的键。
     * @param key 待查询键
     * @return 匹配节点，缺失返回 null
     */
    private Node findNode(int key) {
        Node current = buckets[bucketIndex(key, buckets.length)];
        while (current != null) {
            if (current.key == key) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    /** 将容量翻倍并重新分桶，调用方已检查最大容量，时间 O(C+n)。 */
    private void resize() {
        Node[] expanded = new Node[buckets.length * 2];
        for (Node bucket : buckets) {
            Node current = bucket;
            while (current != null) {
                // 改 next 前保存旧后继；容量改变后必须重新计算桶下标。
                Node originalNext = current.next;
                int index = bucketIndex(current.key, expanded.length);
                current.next = expanded[index];
                expanded[index] = current;
                current = originalNext;
            }
        }
        buckets = expanded;
    }

    /** 冲突链节点，键不可变，值可以覆盖。 */
    private static final class Node {
        /** 整数键。 */
        private final int key;
        /** 当前关联值。 */
        private int value;
        /** 同桶的下一个节点。 */
        private Node next;

        /**
         * 创建冲突链节点。
         * @param key 键
         * @param value 值
         * @param next 原桶头，可为 null
         */
        private Node(int key, int value, Node next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * 返回有效映射数量，时间 O(1)。
     * @return 不同键的数量
     */
    public int size() {
        return size;
    }

    /**
     * 判断是否为空，时间 O(1)。
     * @return 没有有效映射时为 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 扰动整数哈希并映射到二次幂容量内，支持负数及 int 最小值。
     * @param key 整数键
     * @param capacity 非零二次幂容量
     * @return 范围为 [0, capacity) 的下标
     */
    private static int bucketIndex(int key, int capacity) {
        int hash = key ^ (key >>> 16);
        return hash & (capacity - 1);
    }

    /**
     * 演示冲突键、覆盖、删除后查询和特殊整数键。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        ChainingHashMap map = new ChainingHashMap();
        map.put(1, 10);
        map.put(5, 50);
        map.put(1, 11);
        System.out.println(map.size());
        System.out.println(map.get(1));
        System.out.println(map.remove(1));
        System.out.println(map.get(5));
        System.out.println(map.containsKey(1));
        map.put(Integer.MIN_VALUE, -1);
        System.out.println(map.get(Integer.MIN_VALUE));
    }
}
