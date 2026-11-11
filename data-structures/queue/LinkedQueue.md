# 链式队列（LinkedQueue）

[专题总览](Queue.md) · [Java 实现](../../src/learning/datastructures/queue/LinkedQueue.java) · [单链表基础](../linked-list/LinkedList.md)

## 1. 用首尾指针消除遍历

```text
head → [10] → [20] → [30] → null
                       ↑
                      tail
```

head 是最早入队的未消费元素，tail 是最近加入的元素。只存 head 的朴素链表每次尾插需要 O(n) 扫描；保存 tail 后可以直接接入新节点。队列只头删，无需前驱，因此单链表已足够。

不变量：空时 head=tail=null、size=0；非空时从 head 恰好能走过 size 个节点到达 null，最后一个是 tail，且 tail.next=null。节点私有、允许所有 int 和重复值，不支持并发修改。

## 2. 入队的两种情况

已有 `[10,20]`，加入 30：

```text
1. 新建 30，next 默认 null
2. tail.next = 新节点       20 → 30
3. tail = 新节点           尾入口移动到 30
4. size++
```

空时 tail=null，不能读 tail.next，要先令 head=新节点，然后统一 tail=新节点、size++。新节点先分配成功，再改原结构。计数达到 Integer.MAX_VALUE 时拒绝加入，实际规模仍受可用内存限制。

## 3. 出队与唯一节点边界

```text
原来：head → 10 → 20 → null，tail=20
保存 removed=10
head = removed.next      head 移到 20
清理 removed.next，size--
返回 10
```

只有一个节点时，head 移动后为 null，必须同时把 tail=null。否则新一次入队可能接在已经脱离队列的旧节点之后，而 head 仍为空，破坏结构。空删除先抛 NoSuchElementException，不能先减少 size。

peekFirst 仅返回 head.value，不改连接。移除节点不再由队列保留，但实际内存回收时刻由垃圾收集器决定。

## 4. 为什么满足 FIFO

初始序列为空。addLast 只把新节点连接到原序列末端，所有已等待元素仍在它之前；removeFirst 只删掉最前端节点，因此每次取出最早加入且尚未删除的元素。空表插入建立正确的首尾，唯一节点删除恢复空表状态，计数与可达节点数同步。

API 为 addLast、removeFirst、peekFirst、size、isEmpty、toArray。快照从首到尾且独立，空时返回空数组。只允许一端入、一端出，不提供尾删或任意下标操作。

## 5. 复杂度、优势与代价

n 为有效节点数量。入队分配一个节点，时间 O(1)；出队、查看、size、判空均 O(1)，采用通常的节点分配成本模型。快照时间及返回空间 O(n)，有效节点总空间 O(n)。

相比只保存 head，增加一个 tail 状态使尾插无需扫描；代价是删空时必须同步更新两个入口。相比数组循环队列，不用整体扩容复制，也不预留空槽位；但每节点有对象和引用开销，频繁分配可能影响实际性能。不是“链表永远更快”。

适合用链表组织 FIFO 等待序列。这里不是阻塞队列，没有锁、等待唤醒或并发安全机制；有这些需求应选择合适的标准并发容器。

## 6. 运行与追踪

在仓库根目录运行：

```bash
build_dir=$(mktemp -d /tmp/linked-queue.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/queue/LinkedQueue.java
java -cp "$build_dir" learning.datastructures.queue.LinkedQueue
```

main 的逻辑顺序为 `[10,20,30]` → 删除 10 → `[20,30]` → 加入 40 → `[20,30,40]` → 依次删空 → 加入 0：

```text
[10, 20, 30]
10
[20, 30, 40]
20
30
40
0
0
```

普通 JDK 可使用等价 javac。重点观察删空后的 tail 以及重新加入 0 后的 head，不能用 0 作为无数据的标志。

## 7. 自测与验证

练习：画出空表第一次入队、唯一节点出队、删空后再次入队三组引用；说明为什么没有 tail 会使连续 n 次尾插达到 O(n²)。边界还应覆盖空查看/删除、重复值、负数、int 边界和快照独立性。练习建议不代表学习者已经完成。

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 四个新增类与临时验证程序使用 `-Xlint:all -Werror` 编译成功，四个 main 的输出逐行符合对应文档。
- 两个数组实现分别以容量 0、1、4 测试，加上两个链式实现，共八组配置。每组使用固定种子 `20261110` 执行 5,000 步随机操作，与 Java ArrayDeque 对照，合计 40,000 步。
- 固定用例覆盖满环回绕后扩容、连续首插触发双端扩容、对端删除、空队列查看/删除异常、负容量拒绝、重复值、0、负数、int 最小值与最大值、排空重入队。
- 每步核对 size、isEmpty、首尾值（双端版本）、首到尾快照及快照独立性；数组版本核验 head、容量和逻辑到物理映射，链式版本核验可达数量、首尾入口、末端 null，双向版本额外核验前后互连和反向遍历。
- 四个类通过 `-private -Xdoclint:all -Werror` Javadoc 生成。全仓库 36 个 Java 文件编译通过，历史 MergeTwoLists.java 的未检查操作提示仍存在。

测试程序与产物均在仓库外临时目录，未增加依赖。有限测试不代替不变量证明；未实际分配计数上限规模，未测试内存耗尽或并发。代码使用 Java 8 已有语法和 API，但本次仅在 Java 21 实际运行；未执行阿里规约自动检查工具。
