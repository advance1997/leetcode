package learning.datastructures.tree;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * 带共享黑色 NIL 哨兵和父指针的经典红黑树整数集合教学实现。
 *
 * <p>保持严格 BST 顺序、根为黑、NIL 为黑、红节点无红孩子，且任意节点到下方
 * NIL 的所有路径黑节点数相同。本实现不是左倾红黑树，不要求红链接只朝左。
 * 支持全部 int 值；重复 add、缺失 remove 返回 false 且不改变集合。
 * 空树 min/max 抛出 NoSuchElementException。不暴露节点，不支持并发修改。</p>
 *
 * <p>NIL 不是有效数据、不计入 size，空树 root == nil。删除修复期间允许临时改变
 * nil.parent 以记录缺失位置的父节点，公开操作完成后恢复为 nil。
 * 增删查和最值最坏 O(log(n+1)) 时间、O(1) 辅助空间；中序快照 O(n) 时间，
 * O(log(n+1)) 控制栈及 O(n) 返回空间，节点总存储 O(n)。空树操作为 O(1)。</p>
 * <pre>
 * 依次加入 10、20、30，修复后根为黑色 20，两个孩子为红色 10、30。
 * 中序为 [10, 20, 30]；删除 20 后集合为 [10, 30]，红黑性质仍成立。
 * </pre>
 * <p>计数上限 Integer.MAX_VALUE，实际数量受内存限制；使用 Java 8 已有语法和 API。
 * 本类不对应特定 LeetCode 题目，配套文档为 RedBlackTree.md。</p>
 */
public final class RedBlackTree {
    /** 每棵树独立的共享黑叶哨兵，值无业务含义。 */
    private final Node nil;
    /** 根节点，空树为 nil，根的父节点也是 nil。 */
    private Node root;
    /** 有效业务节点数量，不含 nil。 */
    private int size;

    /** 创建空树并建立 NIL 的安全自引用。 */
    public RedBlackTree() {
        nil = new Node(0, false);
        nil.left = nil;
        nil.right = nil;
        nil.parent = nil;
        root = nil;
    }

    /**
     * 常数时间读取集合大小。
     * @return 不同整数数量
     */
    public int size() {
        return size;
    }

    /**
     * 常数时间判空。
     * @return 没有业务节点时为 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 按 BST 顺序查找，时间 O(log(n+1))。
     * @param value 待查值
     * @return 存在时为 true
     */
    public boolean contains(int value) {
        return findNode(value) != nil;
    }

    /**
     * 插入红节点，再通过变色和旋转修复红红冲突。
     * @param value 新值
     * @return 新增为 true，重复为 false
     * @throws IllegalStateException 如果新增会超过计数上限
     */
    public boolean add(int value) {
        Node parent = nil;
        Node current = root;
        while (current != nil) {
            if (value == current.value) {
                return false;
            }
            parent = current;
            current = value < current.value ? current.left : current.right;
        }
        if (size == Integer.MAX_VALUE) {
            throw new IllegalStateException("节点数达到计数上限");
        }

        Node inserted = new Node(value, true);
        inserted.left = nil;
        inserted.right = nil;
        inserted.parent = parent;
        if (parent == nil) {
            root = inserted;
        } else if (value < parent.value) {
            parent.left = inserted;
        } else {
            parent.right = inserted;
        }
        fixAfterInsert(inserted);
        size++;
        return true;
    }

    /**
     * 删除值，必要时修复替代位置缺少的一个黑色贡献。
     *
     * <p>双孩子节点移植中序后继；是否修复依据后继从旧位置移走之前的颜色，
     * 而不是最初目标的颜色。缺失键不改结构。</p>
     *
     * @param value 待删除值
     * @return 删除成功为 true，缺失为 false
     */
    public boolean remove(int value) {
        Node target = findNode(value);
        if (target == nil) {
            return false;
        }
        Node moved = target;
        boolean removedWasRed = moved.red;
        Node replacement;
        if (target.left == nil) {
            replacement = target.right;
            transplant(target, target.right);
        } else if (target.right == nil) {
            replacement = target.left;
            transplant(target, target.left);
        } else {
            moved = minimumNode(target.right);
            removedWasRed = moved.red;
            replacement = moved.right;
            if (moved.parent == target) {
                // 即使 replacement 是 NIL，也要记录它用于修复的父位置。
                replacement.parent = moved;
            } else {
                transplant(moved, moved.right);
                moved.right = target.right;
                moved.right.parent = moved;
            }
            transplant(target, moved);
            moved.left = target.left;
            moved.left.parent = moved;
            // 新位置继承目标颜色，缺失的黑色只发生在后继的旧位置。
            moved.red = target.red;
        }

        if (!removedWasRed) {
            fixAfterDelete(replacement);
        }
        target.left = nil;
        target.right = nil;
        target.parent = nil;
        size--;
        // 共享 NIL 无法同时表达所有叶子的父节点，修复后不再保留临时父引用。
        nil.parent = nil;
        return true;
    }

    /**
     * 查找最小值，时间 O(log(n+1))。
     * @return 最小整数
     * @throws NoSuchElementException 如果为空树
     */
    public int min() {
        checkNotEmpty();
        return minimumNode(root).value;
    }

    /**
     * 查找最大值，时间 O(log(n+1))。
     * @return 最大整数
     * @throws NoSuchElementException 如果为空树
     */
    public int max() {
        checkNotEmpty();
        Node current = root;
        while (current.right != nil) {
            current = current.right;
        }
        return current.value;
    }

    /**
     * 返回独立、严格递增的中序数组，不输出 NIL 占位值。
     * @return 新数组，空树为空数组
     */
    public int[] inorder() {
        int[] values = new int[size];
        int position = 0;
        Deque<Node> stack = new ArrayDeque<>();
        Node current = root;
        while (current != nil || !stack.isEmpty()) {
            while (current != nil) {
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
     * 查找业务节点，直接比较大小避免减法溢出。
     * @param value 待查值
     * @return 匹配节点，不存在返回 nil
     */
    private Node findNode(int value) {
        Node current = root;
        while (current != nil) {
            if (value == current.value) {
                return current;
            }
            current = value < current.value ? current.left : current.right;
        }
        return nil;
    }

    /**
     * 沿左边界取得非空子树最小节点。
     * @param node 非 NIL 子树根
     * @return 最小业务节点
     */
    private Node minimumNode(Node node) {
        while (node.left != nil) {
            node = node.left;
        }
        return node;
    }

    /**
     * 修复插入后的红父红子，左右分支互为镜像。
     * @param node 刚插入的红节点
     */
    private void fixAfterInsert(Node node) {
        while (node.parent.red) {
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;
                if (uncle.red) {
                    // I-L1：红叔叔，父叔变黑、祖父变红，冲突向上传递。
                    node.parent.red = false;
                    uncle.red = false;
                    node.parent.parent.red = true;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.right) {
                        // I-L2：内侧折线，先旋成外侧直线。
                        node = node.parent;
                        rotateLeft(node);
                    }
                    // I-L3：外侧直线，父黑祖父红，再旋祖父。
                    node.parent.red = false;
                    node.parent.parent.red = true;
                    rotateRight(node.parent.parent);
                }
            } else {
                Node uncle = node.parent.parent.left;
                if (uncle.red) {
                    // I-R1：红叔叔的镜像处理。
                    node.parent.red = false;
                    uncle.red = false;
                    node.parent.parent.red = true;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.left) {
                        // I-R2：右左折线先右旋父节点。
                        node = node.parent;
                        rotateRight(node);
                    }
                    // I-R3：右右直线变色后左旋祖父。
                    node.parent.red = false;
                    node.parent.parent.red = true;
                    rotateLeft(node.parent.parent);
                }
            }
        }
        root.red = false;
    }

    /**
     * 修复删除黑节点造成的路径黑色不足，直至到根或由红节点吸收。
     * @param node 替代节点，可为带临时 parent 的共享 NIL
     */
    private void fixAfterDelete(Node node) {
        while (node != root && !node.red) {
            if (node == node.parent.left) {
                Node sibling = node.parent.right;
                if (sibling.red) {
                    // D-L1：红兄弟转成黑兄弟情形，缺失位置不变。
                    sibling.red = false;
                    node.parent.red = true;
                    rotateLeft(node.parent);
                    sibling = node.parent.right;
                }
                if (!sibling.left.red && !sibling.right.red) {
                    // D-L2：兄弟及其孩子均黑，兄弟变红，欠缺向父节点上移。
                    sibling.red = true;
                    node = node.parent;
                } else {
                    if (!sibling.right.red) {
                        // D-L3：近侄红、远侄黑，先把红色转到远侧。
                        sibling.left.red = false;
                        sibling.red = true;
                        rotateRight(sibling);
                        sibling = node.parent.right;
                    }
                    // D-L4：远侄红，通过旋转与颜色转移补足黑高，结束修复。
                    sibling.red = node.parent.red;
                    node.parent.red = false;
                    sibling.right.red = false;
                    rotateLeft(node.parent);
                    node = root;
                }
            } else {
                Node sibling = node.parent.left;
                if (sibling.red) {
                    // D-R1：红兄弟的镜像处理。
                    sibling.red = false;
                    node.parent.red = true;
                    rotateRight(node.parent);
                    sibling = node.parent.left;
                }
                if (!sibling.right.red && !sibling.left.red) {
                    // D-R2：兄弟无红孩子，将欠缺上移。
                    sibling.red = true;
                    node = node.parent;
                } else {
                    if (!sibling.left.red) {
                        // D-R3：右侧为近侄，先左旋兄弟。
                        sibling.right.red = false;
                        sibling.red = true;
                        rotateLeft(sibling);
                        sibling = node.parent.left;
                    }
                    // D-R4：左侧为远侄，右旋父节点完成修复。
                    sibling.red = node.parent.red;
                    node.parent.red = false;
                    sibling.left.red = false;
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
        node.red = false;
    }

    /**
     * 用 replacement 接替 oldNode 的父连接，不负责复制孩子或颜色。
     * @param oldNode 被替换的业务节点
     * @param replacement 替代节点，可为 nil
     */
    private void transplant(Node oldNode, Node replacement) {
        if (oldNode.parent == nil) {
            root = replacement;
        } else if (oldNode == oldNode.parent.left) {
            oldNode.parent.left = replacement;
        } else {
            oldNode.parent.right = replacement;
        }
        replacement.parent = oldNode.parent;
    }

    /**
     * 左旋并维护父子双向连接和根入口，不在本方法内变色。
     * @param node 旧子树根，右孩子必须不是 nil
     */
    private void rotateLeft(Node node) {
        Node promoted = node.right;
        node.right = promoted.left;
        if (promoted.left != nil) {
            promoted.left.parent = node;
        }
        promoted.parent = node.parent;
        if (node.parent == nil) {
            root = promoted;
        } else if (node == node.parent.left) {
            node.parent.left = promoted;
        } else {
            node.parent.right = promoted;
        }
        promoted.left = node;
        node.parent = promoted;
    }

    /**
     * 右旋，是左旋的镜像，不改变中序顺序或节点颜色。
     * @param node 旧子树根，左孩子必须不是 nil
     */
    private void rotateRight(Node node) {
        Node promoted = node.left;
        node.left = promoted.right;
        if (promoted.right != nil) {
            promoted.right.parent = node;
        }
        promoted.parent = node.parent;
        if (node.parent == nil) {
            root = promoted;
        } else if (node == node.parent.right) {
            node.parent.right = promoted;
        } else {
            node.parent.left = promoted;
        }
        promoted.right = node;
        node.parent = promoted;
    }

    /**
     * 在最值查询前拒绝空树。
     * @throws NoSuchElementException 如果为空树
     */
    private void checkNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException("红黑树为空");
        }
    }

    /** 私有节点，NIL 和业务节点共用此类型，但通过对象身份区分。 */
    private static final class Node {
        /** 值，业务节点构造后不变，删除使用节点移植。 */
        private final int value;
        /** true 为红色，false 为黑色。 */
        private boolean red;
        /** 左孩子，缺失为 nil。 */
        private Node left;
        /** 右孩子，缺失为 nil。 */
        private Node right;
        /** 父节点，根的父节点为 nil。 */
        private Node parent;

        /**
         * 创建节点，引用由树在接入前设置。
         * @param value 节点值
         * @param red 初始是否为红色
         */
        private Node(int value, boolean red) {
            this.value = value;
            this.red = red;
        }
    }

    /**
     * 演示增删查、重复值和有序输出。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        RedBlackTree tree = new RedBlackTree();
        for (int value : new int[] {10, 20, 30, 15, 25, 5, 1}) {
            tree.add(value);
        }
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.add(15));
        System.out.println(tree.remove(20));
        System.out.println(tree.remove(99));
        System.out.println(Arrays.toString(tree.inorder()));
        System.out.println(tree.contains(25));
        System.out.println(tree.min());
        System.out.println(tree.max());
    }
}
