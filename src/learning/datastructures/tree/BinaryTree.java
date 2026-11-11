package learning.datastructures.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * 普通整数二叉树的构造与遍历教学实现，不对应特定 LeetCode 题目。
 *
 * <p>节点最多有左、右两个孩子，允许重复值及任意 int 值，不保证搜索树有序性质。
 * 通过紧凑层序 Integer 数组构造，null 表示缺失孩子，仅实际节点消耗后续两个孩子位置。
 * 空数组或全 null 数组表示空树；null 数组引用及无法归属任何父节点的非 null 值被拒绝。
 * 允许省略尾部缺失孩子或添加尾部 null，不修改输入，不向外暴露节点。</p>
 *
 * <p>构造后没有结构修改接口，所有遍历返回新的 int 数组。提供前、中、后序递归与迭代，
 * 以及层序遍历。令 n 为节点数、h 为树高（层数）、w 为最大层宽：遍历时间均 O(n)，
 * 递归和迭代深度优先遍历控制空间 O(h)，层序队列 O(w)，另有 O(n) 结果累积及返回空间。
 * 递归遇到很深的树可能耗尽调用栈，深树应使用迭代版本。</p>
 * <pre>
 * 输入：[1, 2, 3, null, 4, 5]
 * 前序：[1, 2, 4, 3, 5]；中序：[2, 4, 1, 5, 3]
 * 后序：[4, 2, 5, 3, 1]；层序：[1, 2, 3, 4, 5]
 * </pre>
 * <p>使用 Java 8 已有语法与 API，详细说明见 BinaryTree.md。</p>
 */
public final class BinaryTree {
    /** 根节点，空树为 null；构造完成后不再修改结构。 */
    private final Node root;

    /**
     * 保存已构建的根节点，外部只能通过工厂创建树。
     * @param root 根节点，可为 null
     */
    private BinaryTree(Node root) {
        this.root = root;
    }

    /**
     * 按紧凑层序构造树，节点入队后依次读取其左右孩子。
     *
     * <p>例如 [1, null, 2, 3] 表示 1 的右孩子是 2，2 的左孩子是 3。
     * 这不是按 2i+1、2i+2 下标访问孩子的完全位置数组。
     * 令 m 为输入长度，时间 O(m)，节点存储 O(n)，队列辅助空间 O(w)。</p>
     *
     * @param values 层序数组，元素 null 表示空位置，允许空数组
     * @return 新建的树，不保存输入数组引用
     * @throws IllegalArgumentException 如果数组引用为 null 或存在无父节点的有效值
     */
    public static BinaryTree fromLevelOrder(Integer[] values) {
        if (values == null) {
            throw new IllegalArgumentException("层序数组不能为 null");
        }
        if (values.length == 0) {
            return new BinaryTree(null);
        }

        Node root = values[0] == null ? null : new Node(values[0]);
        Deque<Node> parents = new ArrayDeque<>();
        if (root != null) {
            parents.addLast(root);
        }
        int index = 1;
        while (!parents.isEmpty() && index < values.length) {
            Node parent = parents.removeFirst();
            // 缺失的孩子不入队，所以只为实际节点分配后续孩子位置。
            if (values[index] != null) {
                parent.left = new Node(values[index]);
                parents.addLast(parent.left);
            }
            index++;
            if (index < values.length) {
                if (values[index] != null) {
                    parent.right = new Node(values[index]);
                    parents.addLast(parent.right);
                }
                index++;
            }
        }

        // 队列耗尽后允许冗余 null，但不能默默丢弃没有父节点的有效值。
        for (; index < values.length; index++) {
            if (values[index] != null) {
                throw new IllegalArgumentException("存在无法归属父节点的值，下标：" + index);
            }
        }
        return new BinaryTree(root);
    }

    /**
     * 递归前序遍历：根、左、右。
     * @return 新的前序值数组，空树返回空数组
     */
    public int[] preorderRecursive() {
        List<Integer> result = new ArrayList<>();
        collectPreorder(root, result);
        return toIntArray(result);
    }

    /**
     * 递归中序遍历：左、根、右，普通二叉树不保证结果有序。
     * @return 新的中序值数组，空树返回空数组
     */
    public int[] inorderRecursive() {
        List<Integer> result = new ArrayList<>();
        collectInorder(root, result);
        return toIntArray(result);
    }

    /**
     * 递归后序遍历：左、右、根。
     * @return 新的后序值数组，空树返回空数组
     */
    public int[] postorderRecursive() {
        List<Integer> result = new ArrayList<>();
        collectPostorder(root, result);
        return toIntArray(result);
    }

    /**
     * 用显式栈前序遍历，先压右孩子再压左孩子。
     * @return 新的前序值数组，空树返回空数组
     */
    public int[] preorderIterative() {
        List<Integer> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        if (root != null) {
            stack.push(root);
        }
        while (!stack.isEmpty()) {
            Node node = stack.pop();
            result.add(node.value);
            // 栈后进先出，右先入栈才能保证左先被访问。
            if (node.right != null) {
                stack.push(node.right);
            }
            if (node.left != null) {
                stack.push(node.left);
            }
        }
        return toIntArray(result);
    }

    /**
     * 用显式栈保存左侧下降路径，中序返回时再访问根和右子树。
     * @return 新的中序值数组，空树返回空数组
     */
    public int[] inorderIterative() {
        List<Integer> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        Node current = root;
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.left;
            }
            // 左子树已完成，弹出待访问的祖先节点。
            current = stack.pop();
            result.add(current.value);
            current = current.right;
        }
        return toIntArray(result);
    }

    /**
     * 使用单栈及最近完成节点实现后序遍历，控制空间 O(h)。
     * @return 新的后序值数组，空树返回空数组
     */
    public int[] postorderIterative() {
        List<Integer> result = new ArrayList<>();
        Deque<Node> stack = new ArrayDeque<>();
        Node current = root;
        Node lastVisited = null;
        while (current != null || !stack.isEmpty()) {
            if (current != null) {
                stack.push(current);
                current = current.left;
            } else {
                Node candidate = stack.peek();
                // 左子树已处理；右子树存在且尚未完成时，先进入右子树。
                if (candidate.right != null && candidate.right != lastVisited) {
                    current = candidate.right;
                } else {
                    result.add(candidate.value);
                    lastVisited = stack.pop();
                }
            }
        }
        return toIntArray(result);
    }

    /**
     * 使用队列按层从左到右遍历，队列控制空间 O(w)。
     * @return 新的层序值数组，不包含 null 占位
     */
    public int[] levelOrder() {
        List<Integer> result = new ArrayList<>();
        Deque<Node> queue = new ArrayDeque<>();
        if (root != null) {
            queue.addLast(root);
        }
        while (!queue.isEmpty()) {
            Node node = queue.removeFirst();
            result.add(node.value);
            // 先进先出保证同层先于下一层，左先入保证同层从左到右。
            if (node.left != null) {
                queue.addLast(node.left);
            }
            if (node.right != null) {
                queue.addLast(node.right);
            }
        }
        return toIntArray(result);
    }

    /**
     * 将当前子树的前序结果追加到列表。
     * @param node 当前子树根，可为 null
     * @param result 本次遍历的结果列表
     */
    private static void collectPreorder(Node node, List<Integer> result) {
        if (node == null) {
            return;
        }
        result.add(node.value);
        collectPreorder(node.left, result);
        collectPreorder(node.right, result);
    }

    /**
     * 将当前子树的中序结果追加到列表。
     * @param node 当前子树根，可为 null
     * @param result 本次遍历的结果列表
     */
    private static void collectInorder(Node node, List<Integer> result) {
        if (node == null) {
            return;
        }
        collectInorder(node.left, result);
        result.add(node.value);
        collectInorder(node.right, result);
    }

    /**
     * 将当前子树的后序结果追加到列表。
     * @param node 当前子树根，可为 null
     * @param result 本次遍历的结果列表
     */
    private static void collectPostorder(Node node, List<Integer> result) {
        if (node == null) {
            return;
        }
        collectPostorder(node.left, result);
        collectPostorder(node.right, result);
        result.add(node.value);
    }

    /**
     * 转换为独立基本类型数组，不共享结果列表。
     * @param values 访问顺序列表
     * @return 相同顺序的 int 数组
     */
    private static int[] toIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int index = 0; index < values.size(); index++) {
            result[index] = values.get(index);
        }
        return result;
    }

    /** 私有树节点；仅在工厂构建期间连接孩子，之后不再修改。 */
    private static final class Node {
        /** 节点值，允许重复。 */
        private final int value;
        /** 左孩子，缺失为 null。 */
        private Node left;
        /** 右孩子，缺失为 null。 */
        private Node right;

        /**
         * 创建叶节点。
         * @param value 整数值
         */
        private Node(int value) {
            this.value = value;
        }
    }

    /**
     * 演示稀疏树的三种 DFS 顺序及层序遍历。
     * @param args 未使用的命令行参数
     */
    public static void main(String[] args) {
        BinaryTree tree = fromLevelOrder(new Integer[] {1, 2, 3, null, 4, 5});
        System.out.println(Arrays.toString(tree.preorderRecursive()));
        System.out.println(Arrays.toString(tree.preorderIterative()));
        System.out.println(Arrays.toString(tree.inorderRecursive()));
        System.out.println(Arrays.toString(tree.inorderIterative()));
        System.out.println(Arrays.toString(tree.postorderRecursive()));
        System.out.println(Arrays.toString(tree.postorderIterative()));
        System.out.println(Arrays.toString(tree.levelOrder()));
    }
}
