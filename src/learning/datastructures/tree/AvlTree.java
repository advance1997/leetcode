package learning.datastructures.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * 基于高度与旋转的整数 AVL 集合教学实现，不对应特定 LeetCode 题目。
 *
 * <p>左子树全部值严格小于根，右子树全部值严格大于根；每个节点左右子树高度差
 * 的绝对值不超过 1。空子树高度为 0，叶节点高度为 1，height 缓存以层数为单位。
 * 重复 add 返回 false，缺失 remove 返回 false，不改变集合；空树 min/max 抛出
 * NoSuchElementException。支持全部 int 值，不暴露节点，不支持并发修改。</p>
 *
 * <p>查找与最值使用迭代，时间 O(log(n+1))、辅助空间 O(1)。插入删除先检查存在性，
 * 再递归修改并自底向上再平衡，两次路径仍为 O(log(n+1)) 时间，递归空间 O(log(n+1))。
 * 中序遍历 O(n) 时间、O(log(n+1)) 控制栈及 O(n) 返回空间。
 * size、判空和高度读取 O(1)，节点总存储 O(n)。实际规模受内存及 int 计数限制。</p>
 * <pre>
 * 依次 add(30)、add(20)、add(10)，右旋后根为 20，中序为 [10, 20, 30]，高度为 2。
 * remove(20) 后保留 [10, 30]，并恢复高度和平衡规则。
 * </pre>
 * <p>使用 Java 8 已有语法和 API，配套文档为 AvlTree.md。</p>
 */
public final class AvlTree {
    /** 根节点，空树为 null。 */
    private Node root;
    /** 不同整数值的数量。 */
    private int size;

    /** 创建空搜索树。 */
    public AvlTree() {
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
     * 新增值并恢复 AVL 平衡，重复值不修改树。
     * @param value 待加入值
     * @return 新增为 true，重复为 false
     * @throws IllegalStateException 如果新增节点会超过计数上限
     */
    public boolean add(int value) {
        if (contains(value)) {
            return false;
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数达到计数上限");
        }
        // 子树旋转可能改变根，所以必须接收递归返回的新入口。
        root = insert(root, value);
        size++;
        return true;
    }

    /**
     * 删除值，并沿整个修改路径恢复高度和平衡。
     * @param value 待删除值
     * @return 删除成功为 true，缺失为 false 且不修改树
     */
    public boolean remove(int value) {
        if (!contains(value)) {
            return false;
        }
        root = delete(root, value);
        // 后继替换可能涉及两处位置，但逻辑上只删除一个值。
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
     * 返回以层数计算的树高，空树为 0，时间 O(1)。
     * @return 根节点缓存高度
     */
    public int height() {
        return nodeHeight(root);
    }

    /**
     * 递归插入已确认不存在的值，回溯时恢复平衡。
     * @param node 子树根，可为 null
     * @param value 新值
     * @return 调整后的子树根
     */
    private static Node insert(Node node, int value) {
        if (node == null) {
            return new Node(value);
        }
        if (value < node.value) {
            node.left = insert(node.left, value);
        } else {
            node.right = insert(node.right, value);
        }
        return rebalance(node);
    }

    /**
     * 删除已确认存在的值，局部摘除后对每个返回层进行再平衡。
     * @param node 非空子树根，value 在其子树中
     * @param value 待删除值
     * @return 调整后的子树根，可能为 null
     */
    private static Node delete(Node node, int value) {
        if (value < node.value) {
            node.left = delete(node.left, value);
        } else if (value > node.value) {
            node.right = delete(node.right, value);
        } else {
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            // 后继没有左孩子，但可能有右孩子，递归删除会保留它并修复路径。
            Node successor = node.right;
            while (successor.left != null) {
                successor = successor.left;
            }
            node.value = successor.value;
            node.right = delete(node.right, successor.value);
        }
        return rebalance(node);
    }

    /**
     * 更新高度后按左右子树的平衡因子选择单旋或双旋。
     * @param node 非空节点，其左右子树已经平衡且高度正确
     * @return 平衡后的局部根
     */
    private static Node rebalance(Node node) {
        updateHeight(node);
        int factor = balanceFactor(node);
        if (factor > 1) {
            // 左孩子右重是 LR；左孩子因子为 0 时也应直接右旋（删除可出现）。
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (factor < -1) {
            // 右孩子左重是 RL；右孩子因子为 0 时直接左旋。
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    /**
     * 右旋并保留中间子树，先更新下降的旧根，再更新上升的新根。
     * @param oldRoot 非空旧根，左孩子非空
     * @return 旋转后的新根
     */
    private static Node rotateRight(Node oldRoot) {
        Node newRoot = oldRoot.left;
        Node middle = newRoot.right;
        newRoot.right = oldRoot;
        oldRoot.left = middle;
        updateHeight(oldRoot);
        updateHeight(newRoot);
        return newRoot;
    }

    /**
     * 左旋，是右旋的对称操作，不改变中序序列。
     * @param oldRoot 非空旧根，右孩子非空
     * @return 旋转后的新根
     */
    private static Node rotateLeft(Node oldRoot) {
        Node newRoot = oldRoot.right;
        Node middle = newRoot.left;
        newRoot.left = oldRoot;
        oldRoot.right = middle;
        updateHeight(oldRoot);
        updateHeight(newRoot);
        return newRoot;
    }

    /**
     * 用两个已经正确的孩子高度更新节点高度。
     * @param node 非空节点
     */
    private static void updateHeight(Node node) {
        node.height = 1 + Math.max(nodeHeight(node.left), nodeHeight(node.right));
    }

    /**
     * 获取缓存高度，空子树高度为 0。
     * @param node 节点，可为 null
     * @return 子树层数
     */
    private static int nodeHeight(Node node) {
        return node == null ? 0 : node.height;
    }

    /**
     * 计算左高减右高的平衡因子。
     * @param node 非空节点
     * @return 正值表示左侧更高，负值表示右侧更高
     */
    private static int balanceFactor(Node node) {
        return nodeHeight(node.left) - nodeHeight(node.right);
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
        /** 当前子树高度，新叶节点为 1。 */
        private int height = 1;
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
     * 演示 LL 旋转、删除再平衡与集合语义。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        AvlTree tree = new AvlTree();
        for (int value : new int[] {30, 20, 10, 25, 40, 50}) {
            tree.add(value);
        }
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.height());
        System.out.println(tree.add(25));
        System.out.println(tree.remove(30));
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.height());
        System.out.println(tree.min());
        System.out.println(tree.max());
    }
}
