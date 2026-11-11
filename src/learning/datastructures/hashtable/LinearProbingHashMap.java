package learning.datastructures.hashtable;

import java.util.NoSuchElementException;

/**
 * 使用线性探测和删除标记的整数哈希表教学实现，不对应特定 LeetCode 题目。
 *
 * <p>键和值均支持全部 int 值，独立状态数组区分未使用、有效、已删除槽位。
 * 重复键覆盖值而不增加 size；缺失键的 get/remove 抛出 NoSuchElementException。
 * entries 返回独立的 [key, value] 行数组且不保证顺序，不支持并发修改。</p>
 *
 * <p>容量从 4 开始、始终为二次幂，有效数量不超过容量一半。
 * used 统计有效与已删除槽位总数，达到容量的四分之三时下次新键插入先原容量重建。
 * 新键超过半容量时优先翻倍扩容，最大容量 1 &lt;&lt; 30，不缩容。</p>
 *
 * <p>令 n 为有效映射数、C 为容量。良好哈希分布及受控负载下查询/删除期望 O(1)，
 * 插入期望均摊 O(1)，不保证单次最坏常数。单次普通探测最坏 O(C)；
 * 重建期望 O(C+n)，极端聚集时最坏 O(C+n²)。存储 O(C)，快照 O(C) 时间、O(n) 返回空间。</p>
 * <pre>
 * put(1, 10)、put(5, 50) 初始可能占据槽位 1、2。
 * remove(1) 留下删除标记，get(5) 仍能越过该槽位找到 50。
 * </pre>
 * <p>达到容量负载上限时新键插入抛出 IllegalStateException，仍可更新已有键。
 * 实际容量受内存限制；使用 Java 8 已有语法与 API，配套文档为 LinearProbingHashMap.md。</p>
 */
public final class LinearProbingHashMap {
    /** 初始二次幂容量。 */
    private static final int INITIAL_CAPACITY = 4;
    /** 防止翻倍溢出的最大容量。 */
    private static final int MAX_CAPACITY = 1 << 30;
    /** 从未占用，查询可以停止。 */
    private static final byte EMPTY = 0;
    /** 有效键值，查询需要比较键。 */
    private static final byte OCCUPIED = 1;
    /** 已删除，查询必须继续；插入可以复用。 */
    private static final byte DELETED = 2;
    /** 各槽位的键，仅 OCCUPIED 槽位有效。 */
    private int[] keys = new int[INITIAL_CAPACITY];
    /** 与 keys 对应的值。 */
    private int[] values = new int[INITIAL_CAPACITY];
    /** 独立槽位状态，不占用特殊整数键。 */
    private byte[] states = new byte[INITIAL_CAPACITY];
    /** 有效映射数量。 */
    private int size;
    /** 非 EMPTY 槽位数，即有效数量加删除标记数量。 */
    private int used;

    /** 创建空线性探测表。 */
    public LinearProbingHashMap() {
    }

    /**
     * 新增或更新键值；先查重，再决定是否扩容或清除删除标记。
     * @param key 整数键
     * @param value 整数值
     * @throws IllegalStateException 如果新键需要超过最大容量
     */
    public void put(int key, int value) {
        int existing = findIndex(key);
        if (existing >= 0) {
            values[existing] = value;
            return;
        }

        if (size >= keys.length / 2) {
            if (keys.length == MAX_CAPACITY) {
                throw new IllegalStateException("达到哈希表容量限制");
            }
            rebuild(keys.length * 2);
        } else if (used >= keys.length - keys.length / 4) {
            // 有效项不多但墓碑积累时，保持容量重建即可恢复短探测链。
            rebuild(keys.length);
        }

        int index = bucketIndex(key, keys.length);
        while (states[index] == OCCUPIED) {
            index = (index + 1) & (keys.length - 1);
        }
        if (states[index] == EMPTY) {
            used++;
        }
        keys[index] = key;
        values[index] = value;
        states[index] = OCCUPIED;
        size++;
    }

    /**
     * 查询键对应值，越过删除标记。
     * @param key 待查询键
     * @return 对应值
     * @throws NoSuchElementException 如果键不存在
     */
    public int get(int key) {
        int index = findIndex(key);
        if (index < 0) {
            throw new NoSuchElementException("键不存在：" + key);
        }
        return values[index];
    }

    /**
     * 检查键是否存在。
     * @param key 待检查键
     * @return 存在时为 true
     */
    public boolean containsKey(int key) {
        return findIndex(key) >= 0;
    }

    /**
     * 删除有效项并保留探测路径，缺失时不修改结构。
     * @param key 待删除键
     * @return 原关联值
     * @throws NoSuchElementException 如果键不存在
     */
    public int remove(int key) {
        int index = findIndex(key);
        if (index < 0) {
            throw new NoSuchElementException("键不存在：" + key);
        }
        int oldValue = values[index];
        states[index] = DELETED;
        keys[index] = 0;
        values[index] = 0;
        size--;
        // used 不减少，墓碑仍延长查询路径，由后续插入按阈值重建。
        return oldValue;
    }

    /**
     * 返回独立键值快照，扫描所有槽位，时间 O(C)，返回空间 O(n)。
     * @return 每行 [key, value]，顺序不保证
     */
    public int[][] entries() {
        int[][] result = new int[size][2];
        int position = 0;
        for (int index = 0; index < keys.length; index++) {
            if (states[index] == OCCUPIED) {
                result[position][0] = keys[index];
                result[position][1] = values[index];
                position++;
            }
        }
        return result;
    }

    /**
     * 沿同一探测序列查找，EMPTY 才能证明后方没有该键。
     * @param key 待查找键
     * @return 匹配槽位；不存在返回 -1
     */
    private int findIndex(int key) {
        int index = bucketIndex(key, keys.length);
        for (int checked = 0; checked < keys.length; checked++) {
            if (states[index] == EMPTY) {
                return -1;
            }
            if (states[index] == OCCUPIED && keys[index] == key) {
                return index;
            }
            index = (index + 1) & (keys.length - 1);
        }
        return -1;
    }

    /**
     * 将所有有效项重新探测到新数组，删除标记不迁移，size 不变。
     * @param capacity 新的二次幂容量，不小于当前容量
     */
    private void rebuild(int capacity) {
        int[] newKeys = new int[capacity];
        int[] newValues = new int[capacity];
        byte[] newStates = new byte[capacity];
        for (int oldIndex = 0; oldIndex < keys.length; oldIndex++) {
            if (states[oldIndex] != OCCUPIED) {
                continue;
            }
            int index = bucketIndex(keys[oldIndex], capacity);
            while (newStates[index] == OCCUPIED) {
                index = (index + 1) & (capacity - 1);
            }
            newKeys[index] = keys[oldIndex];
            newValues[index] = values[oldIndex];
            newStates[index] = OCCUPIED;
        }
        // 所有新数组构建成功后统一替换，恢复 used == size。
        keys = newKeys;
        values = newValues;
        states = newStates;
        used = size;
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
        LinearProbingHashMap map = new LinearProbingHashMap();
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
