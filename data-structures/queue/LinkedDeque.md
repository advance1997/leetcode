# 链式双端队列（LinkedDeque）

[专题总览](Queue.md) · [Java 实现](../../src/learning/datastructures/queue/LinkedDeque.java) · [双向链表基础](../linked-list/DoublyLinkedList.md)

## 1. 为什么采用双向链表

普通链式队列只需尾插头删，单链表足够。双端队列还需要尾删，单链表即使有 tail 也需 O(n) 找前驱。增加 previous 引用，tail.previous 直接给出新尾，使两端插删都为 O(1)。

```text
head → [10] ⇄ [20] ⇄ [30] ← tail
首节点.previous=null，尾节点.next=null
```

本实现无哨兵、非循环，以清晰展示空表和单节点边界。带哨兵的循环双向链表也可以实现同样接口，其结构已在[链表专题](../linked-list/CircularDoublyLinkedList.md)实现；这里不重复建立第三份相同底层变体。

## 2. 状态与 API

空时 head=tail=null、size=0；非空时两端向外引用为 null，相邻节点的 next/previous 互相对应，从任一方向恰好走 size 个节点到 null。

提供 addFirst、addLast、removeFirst、removeLast、peekFirst、peekLast、size、isEmpty、toArray。没有下标访问接口，符合双端操作职责。允许任意 int 和重复值，快照首到尾且独立。空查看/删除抛 NoSuchElementException 且状态不变；计数达到 Integer.MAX_VALUE 时加入抛 IllegalStateException，实际受内存限制。不支持并发修改。

## 3. 两端加入图解

已有 `10⇄20`，首插 5：

```text
新节点.next = head         5 → 10
原 head.previous = 新节点  10 → 5（前驱方向）
head = 新节点
结果：5 ⇄ 10 ⇄ 20
```

尾插 30：新节点.previous=tail，原 tail.next=新节点，再令 tail=新节点，得到 `10⇄20⇄30`。新节点的外侧引用默认 null。

空表首插时还要令 tail=新节点；空表尾插时还要令 head=新节点。最终两个入口指向同一节点，两个方向均为 null。分配完成后再修改入口，size 最后增加。

## 4. 两端删除图解

从 `10⇄20⇄30` 删除队首：

```text
removed = head       保存 10
head = removed.next  移到 20
head.previous = null 清除新首的旧前驱
removed.next = null  断开被删节点
返回 10，size--
```

删除队尾对称：保存 30，tail=removed.previous 移到 20，tail.next=null，removed.previous=null，返回 30，size--。

| 场景 | 必要处理 |
| --- | --- |
| 非空多节点首删 | 新 head.previous=null |
| 非空多节点尾删 | 新 tail.next=null |
| 唯一节点首删 | head 变 null 后同步 tail=null |
| 唯一节点尾删 | tail 变 null 后同步 head=null |
| 空表删除 | 在任何改线前抛异常 |

只清一侧可能让正向遍历正确、反向仍引用旧节点，因此验证必须检查两向连接。断开节点不等于内存已立即回收。

## 5. 正确性与取舍

每次加入仅把一个节点连接在端点，保持所有旧节点次序；每次删除只移除端点并修复新端点的外侧引用，空/单节点分支保证入口一致。两侧连接同步维护，size 与可达节点数同步。因此既能按尾入首出实现 FIFO，也能按同端加入删除实现 LIFO。

两端插删、查看、size、判空为 O(1)，按通常节点分配模型分析；快照时间及返回空间 O(n)，节点总存储 O(n)。相比单链表双端实现，额外 previous 引用消除了尾删扫描；代价是对象与双引用开销、更多改线。相比环形数组，不需扩容复制，但不保证实际吞吐一定更高。

需要双向调度、队列/栈两种语义时可用 Deque。Java LinkedList 实现 Deque，但实际使用中 ArrayDeque 也常见。此教学类没有迭代器、并发同步、阻塞或有界容量策略，不应代替完整生产容器需求分析。

## 6. 运行与完整操作

在仓库根目录运行，普通 JDK 可使用 javac：

```bash
build_dir=$(mktemp -d /tmp/linked-deque.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/queue/LinkedDeque.java
java -cp "$build_dir" learning.datastructures.queue.LinkedDeque
```

操作序列为：`[10,20,30]` → 首删 10 → 尾加 40 → 首加 5 → 尾删 40 → `[5,20,30]` → 首端排空 → 加 0。预期输出：

```text
[10, 20, 30]
10
40
[5, 20, 30]
5
20
30
0
0
```

## 7. 自测与验证

重点验证空表四种查看/删除异常、单节点从任一端删除、交错首尾加入删除、重复值、两端读取不修改内容、删空后重建、快照独立。自测：画出“首加 10，尾删 10，尾加 20，首删 20”的所有入口变化，并解释为何没有 previous 的尾删不能直接做到 O(1)。

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 四个新增类与临时验证程序使用 `-Xlint:all -Werror` 编译成功，四个 main 的输出逐行符合对应文档。
- 两个数组实现分别以容量 0、1、4 测试，加上两个链式实现，共八组配置。每组使用固定种子 `20261110` 执行 5,000 步随机操作，与 Java ArrayDeque 对照，合计 40,000 步。
- 固定用例覆盖满环回绕后扩容、连续首插触发双端扩容、对端删除、空队列查看/删除异常、负容量拒绝、重复值、0、负数、int 最小值与最大值、排空重入队。
- 每步核对 size、isEmpty、首尾值（双端版本）、首到尾快照及快照独立性；数组版本核验 head、容量和逻辑到物理映射，链式版本核验可达数量、首尾入口、末端 null，双向版本额外核验前后互连和反向遍历。
- 四个类通过 `-private -Xdoclint:all -Werror` Javadoc 生成。全仓库 36 个 Java 文件编译通过，历史 MergeTwoLists.java 的未检查操作提示仍存在。

测试程序与产物均在仓库外临时目录，未增加依赖。有限测试不代替不变量证明；未实际分配计数上限规模，未测试内存耗尽或并发。代码使用 Java 8 已有语法和 API，但本次仅在 Java 21 实际运行；未执行阿里规约自动检查工具。
