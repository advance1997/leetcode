# 数组循环队列（CircularArrayQueue）

[专题总览](Queue.md) · [Java 实现](../../src/learning/datastructures/queue/CircularArrayQueue.java)

## 1. 状态与接口

本实现为可扩容 FIFO 队列，维护 elements、head、size。容量 C=elements.length，逻辑第 i 个元素位于 `(head+i) mod C`，尾后待写位置为偏移 size，最后一个元素偏移为 size-1。源码不保存可冗余推导的 tail。

- 空：size=0，同时将 head 规范为 0。
- 满：size=C；允许使用所有 C 个槽位，不预留空位。
- 构造允许零容量，初次加入先扩到 1；负容量抛 IllegalArgumentException。
- API 为 addLast、removeFirst、peekFirst、size、isEmpty、toArray。空删除/查看抛 NoSuchElementException，不改变状态。

另一种常见方案是只维护 head/tail 并预留一个槽位，用 `(tail+1)%C==head` 判满，有效容量为 C-1。本实现使用 size，不混用两种判满公式。

## 2. 从假满到回绕

若每次头删后整体移动，出队 O(n)；若不搬移却只增长尾下标，前面的槽位不能复用。回绕把空闲槽位重新纳入使用：

```text
容量 4，下标：  0    1    2    3
入队四个后： [10,  20,  30,  40] head=0 size=4
出队两次后： [ 0,   0,  30,  40] head=2 size=2
再加入 50：  [50,   0,  30,  40] head=2 size=3
再加入 60：  [50,  60,  30,  40] head=2 size=4
逻辑顺序：   30 → 40 → 50 → 60
```

清零不表示 0 不能入队，有效范围由 head 与 size 决定。removeFirst 保存队首值、清槽位、让 head 前进一步并回绕、size--；size 变 0 时 head 重置为 0。peekFirst 不移动 head。

## 3. 回绕后扩容：必须按逻辑顺序复制

上述状态再入队 70，没有空槽位。新容量通常翻倍为 8：

```text
旧物理： [50,60,30,40]，head=2
读取次序： 2 → 3 → 0 → 1
新物理： [30,40,50,60,0,0,0,0]，head=0，size=4
加入 70：[30,40,50,60,70,0,0,0]，size=5
```

直接对旧数组做同位置复制会把队首和顺序弄错。源码先按逻辑偏移复制到新数组，再替换 elements 并把 head 设为 0；零容量时 size=0，复制循环零次，不会执行取模 0。

容量计算和 `head+offset` 使用 long 中间值，防止 int 溢出。计数上限 Integer.MAX_VALUE，满计数加入抛 IllegalStateException；实际数组分配仍可能更早受 JVM 长度限制或内存不足影响。

## 4. 正确性与复杂度

不变量：有效序列恰好是从 head 起连续 size 个环形槽位。尾加入在偏移 size 的未占用槽位写入并加一，保持旧顺序；头删除只跳过第一项并减一，留下原后缀，故 FIFO。扩容把第 i 个逻辑元素搬到新下标 i，逻辑顺序不变。

逐次容量只增 1 的方案，连续加入 m 次复制量可达 O(m²)；几何扩容的累计复制量为 O(m)，所以加入均摊 O(1)，触发扩容单次 O(n)。删除、查看、size、判空 O(1)；快照时间与返回空间 O(n)。数组总存储 O(C)，不缩容。初始化容量 C 也有相应分配初始化成本。

若已知容量上限、希望避免动态扩容，可用固定容量环形队列，满时显式失败。本实现选择动态增长，不会静默覆盖最旧数据。用于任务缓存等场景时，应由业务另行确定容量限制与满队列策略。

## 5. 运行示例

在仓库根目录执行，普通 JDK 可替换为 javac：

```bash
build_dir=$(mktemp -d /tmp/array-queue.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/queue/CircularArrayQueue.java
java -cp "$build_dir" learning.datastructures.queue.CircularArrayQueue
```

main 入队 10、20、30，出队 10，再加入 40，排空并复用，输出：

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

最后两行分别是空时 size 和重新入队 0 后的队首。main 不是完整回绕扩容测试，额外测试记录见下文。

## 6. 边界与练习

重点验证零容量首次入队、容量 1、多次回绕、满时扩容、回绕后扩容、删空后复用、重复值、空异常、快照独立性。手工画出上面的容量 4 例子，并说明为什么扩容前物理下标 0 不代表队首。不要把某个槽位值为 0 当作空满依据。

## 7. 验证记录

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 四个新增类与临时验证程序使用 `-Xlint:all -Werror` 编译成功，四个 main 的输出逐行符合对应文档。
- 两个数组实现分别以容量 0、1、4 测试，加上两个链式实现，共八组配置。每组使用固定种子 `20261110` 执行 5,000 步随机操作，与 Java ArrayDeque 对照，合计 40,000 步。
- 固定用例覆盖满环回绕后扩容、连续首插触发双端扩容、对端删除、空队列查看/删除异常、负容量拒绝、重复值、0、负数、int 最小值与最大值、排空重入队。
- 每步核对 size、isEmpty、首尾值（双端版本）、首到尾快照及快照独立性；数组版本核验 head、容量和逻辑到物理映射，链式版本核验可达数量、首尾入口、末端 null，双向版本额外核验前后互连和反向遍历。
- 四个类通过 `-private -Xdoclint:all -Werror` Javadoc 生成。全仓库 36 个 Java 文件编译通过，历史 MergeTwoLists.java 的未检查操作提示仍存在。

测试程序与产物均在仓库外临时目录，未增加依赖。有限测试不代替不变量证明；未实际分配计数上限规模，未测试内存耗尽或并发。代码使用 Java 8 已有语法和 API，但本次仅在 Java 21 实际运行；未执行阿里规约自动检查工具。
