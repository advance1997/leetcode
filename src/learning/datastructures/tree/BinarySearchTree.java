package learning.datastructures.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * 不自动平衡的整数二叉搜索树教学实现，采用集合语义，不对应特定 LeetCode 题目。
 *
 * <p>每个节点的整棵左子树值严格小于自身，整棵右子树值严格大于自身。
 * 支持全部 int 值；重复 add 返回 false，不建立重复节点；remove 缺失值返回 false。
 * 空树 min/max 抛出 NoSuchElementException。节点不向外暴露，不支持并发修改。</p>
 *
 * <p>查找、插入、删除和最值查询使用迭代，时间 O(h)、辅助空间 O(1)，h 为树的层数。
 * 平衡时 h 为 O(log n)，退化时 h 为 O(n)，本类不保证对数性能。
 * 中序快照时间 O(n)、控制栈 O(h)、返回空间 O(n)，总节点存储 O(n)。
 * 空树操作为 O(1)，计数上限为 Integer.MAX_VALUE，实际规模受可用内存限制。</p>
 * <pre>
 * 依次加入 8、3、10、1、6、14、4、7、13：
 * 中序为 [1, 3, 4, 6, 7, 8, 10, 13, 14]。
 * 再次 add(6) 返回 false；remove(3) 返回 true，剩余键仍保持搜索树顺序。
 * </pre>
 * <p>使用 Java 8 已有语法和 API，配套文档为 BinarySearchTree.md。</p>
 */
public final class BinarySearchTree {
    /** 根节点，空树为 null。 */
    private Node root;
    /** 不同整数值的数量。 */
    private int size;

    /** 创建空搜索树。 */
    public BinarySearchTree() {
    }

    /**
     * 常数时间读取有效节点数。
     * @return 不同值的数量
     */
    public int size() {
        return size;
    }

    /**
     * 常数时间判断空树。
     * @return 没有节点时返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 依据大小关系排除整棵子树，时间 O(h)，辅助空间 O(1)。
     * @param value 待查找值
     * @return 存在时为 true
     */
    public boolean contains(int value) {
        Node current = root;
        while (current != null) {
            if (value == current.value) {
                return true;
            }
            // 直接比较而不相减，避免 int 边界值作差溢出。
            current = value < current.value ? current.left : current.right;
        }
        return false;
    }

    /**
     * 将新值连接在搜索路径末端，时间 O(h)，不执行平衡旋转。
     * @param value 待加入值
     * @return 新增成功为 true，重复值为 false 且不改变结构
     * @throws IllegalStateException 如果新增节点会超过计数上限
     */
    public boolean add(int value) {
        Node parent = null;
        Node current = root;
        while (current != null) {
            if (value == current.value) {
                return false;
            }
            parent = current;
            current = value < current.value ? current.left : current.right;
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数达到计数上限");
        }

        Node inserted = new Node(value);
        if (parent == null) {
            root = inserted;
        } else if (value < parent.value) {
            parent.left = inserted;
        } else {
            parent.right = inserted;
        }
        size++;
        return true;
    }

    /**
     * 删除值；双孩子节点使用中序后继替换，再摘除后继节点。
     *
     * <p>时间 O(h)、辅助空间 O(1)，只减少一次 size。缺失值返回 false。
     * 替换值会暂时产生相同值的两个内部节点，摘除后继后恢复严格有序，不支持并发观察。</p>
     *
     * @param value 待删除值
     * @return 删除成功为 true，不存在为 false
     */
    public boolean remove(int value) {
        Node parent = null;
        Node current = root;
        while (current != null && current.value != value) {
            parent = current;
            current = value < current.value ? current.left : current.right;
        }
        if (current == null) {
            return false;
        }

        if (current.left != null && current.right != null) {
            // 右子树最小节点就是中序后继，它一定没有左孩子。
            Node successorParent = current;
            Node successor = current.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }
            current.value = successor.value;
            // 将真正需要摘除的目标转为后继，统一为至多一个孩子的删除。
            parent = successorParent;
            current = successor;
        }

        Node replacement = current.left != null ? current.left : current.right;
        if (parent == null) {
            // 删除根时必须改变 root；叶根的 replacement 为 null。
            root = replacement;
        } else if (parent.left == current) {
            parent.left = replacement;
        } else {
            parent.right = replacement;
        }
        current.left = null;
        current.right = null;
        size--;
        return true;
    }

    /**
     * 沿左边界查找最小值，时间 O(h)、辅助空间 O(1)。
     * @return 树中的最小值
     * @throws NoSuchElementException 如果为空树
     */
    public int min() {
        checkNotEmpty();
        Node current = root;
        while (current.left != null) {
            current = current.left;
        }
        return current.value;
    }

    /**
     * 沿右边界查找最大值，时间 O(h)、辅助空间 O(1)。
     * @return 树中的最大值
     * @throws NoSuchElementException 如果为空树
     */
    public int max() {
        checkNotEmpty();
        Node current = root;
        while (current.right != null) {
            current = current.right;
        }
        return current.value;
    }

    /**
     * 生成严格递增的中序快照，不修改树，不共享内部状态。
     * @return 新的有序数组，空树返回空数组
     */
    public int[] inorder() {
        int[] values = new int[size];
        int position = 0;
        Deque<Node> stack = new ArrayDeque<>();
        Node current = root;
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            current = stack.pop();
            values[position++] = current.value;
            current = current.right;
        }
        return values;
    }

    /**
     * 在最值查询前拒绝空树。
     * @throws NoSuchElementException 如果为空树
     */
    private void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("搜索树为空");
        }
    }

    /** 私有节点，删除双孩子节点时允许用后继值替换当前值。 */
    private static final class Node {
        /** 节点当前值。 */
        private int value;
        /** 左子树根，其全部值小于当前值。 */
        private Node left;
        /** 右子树根，其全部值大于当前值。 */
        private Node right;

        /**
         * 创建无孩子的新节点。
         * @param value 整数值
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 演示集合语义、查找、最值和三种删除情况。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        BinarySearchTree tree = new BinarySearchTree();
        for (int value : new int[] {8, 3, 10, 1, 6, 14, 4, 7, 13}) {
            tree.add(value);
        }
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.add(6));
        System.out.println(tree.contains(7));
        System.out.println(tree.min());
        System.out.println(tree.max());

        tree.remove(1);
        tree.remove(14);
        tree.remove(8);
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.size());
        System.out.println(tree.remove(99));
    }
}
