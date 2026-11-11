# 带哨兵的循环双向链表（Circular Doubly Linked List）

[返回导航](../README.md) · [普通双向链表](DoublyLinkedList.md) · [循环单链表](CircularSinglyLinkedList.md) · [Java 实现](../../src/learning/datastructures/linkedlist/CircularDoublyLinkedList.java)

学习日期：2026-11-10（北京时间）。本节将双向连接与单哨兵环结合，重点是用结构统一首尾边界，而不是删掉必要的参数检查。

## 1. 为什么引入一个固定哨兵

无哨兵双向链表需要处理“没有前驱时更新 head”“没有后继时更新 tail”。加入一个永久存在但不表示数据的节点 S，让首节点的前驱、尾节点的后继都指向 S，两侧邻居就始终存在。

```text
空表：    S.next = S，S.previous = S

非空：    S ⇄ [10] ⇄ [20] ⇄ [30] ⇄ S
          ↑                        ↑
          └──── 同一个哨兵对象 ─────┘

逻辑头 = S.next；逻辑尾 = S.previous
```

`A ⇄ B` 表示 A.next=B 且 B.previous=A，两条引用都要维护。哨兵值在代码中为 0，只是占位，不参与查询；真实数据完全可以为 0。size 不包含 S。

结构不变量：从 S 出发沿任一方向走 size+1 条边回到 S，中途各业务节点恰好出现一次；每一对相邻节点的双向连接互相对应。空表不是 null，而是哨兵双向自环。

## 2. API 和实现边界

Java 类位于 `src/learning/datastructures/linkedlist/CircularDoublyLinkedList.java`，包名 `learning.datastructures.linkedlist`。

支持 size、get、insert、remove、addFirst/addLast、removeFirst/removeLast、indexOf、toArray、toReversedArray 和 reverse。与普通双向链表一致：读取/删除下标为 `[0,size)`，插入为 `[0,size]`；非法下标抛出 IndexOutOfBoundsException 且不改变结构。首尾删除空表也抛该异常。

任意 int 和重复值合法；数量达到 Integer.MAX_VALUE 时插入抛 IllegalStateException，实际数量还受内存限制。不暴露节点，不支持并发修改。`toReversedArray` 只倒序读取，`reverse` 才改变结构。快照均为独立数组。

节点查找从较近端开始，移动 `min(i,n-1-i)` 次，最坏仍 O(n)。哨兵不能作为有效下标被返回或删除，空表检查不能省略。

## 3. 插入：四条连接统一所有位置

向 `[10,30]` 的下标 1 插入 20，定位 successor=30，predecessor=10：

```text
旧结构： S ⇄ 10 ⇄ 30 ⇄ S
新节点：         20

inserted.previous = predecessor   20.previous = 10
inserted.next = successor          20.next = 30
predecessor.next = inserted        10.next = 20
successor.previous = inserted     30.previous = 20

新结构： S ⇄ 10 ⇄ 20 ⇄ 30 ⇄ S
```

先保存原邻居，再接新节点，最后更新两侧邻居；不应边覆盖边重新猜测邻居是谁。

| 场景 | predecessor | successor | 是否需要另一套改线代码 |
| --- | --- | --- | --- |
| 头插 | S | 原头 | 不需要 |
| 尾插 | 原尾 | S | 不需要 |
| 空表插入 | S | S | 不需要 |
| 中间插入 | 原前驱 | 原位置节点 | 不需要 |

空表时四句会让 S.next 与 S.previous 都指向新节点，同时新节点两边都指向 S。最后 size++。不要让业务节点自环，那是无哨兵循环单链表的单节点表示，不是本结构。

## 4. 删除：两侧互连，唯一元素也自动成立

对目标 X 保存或使用它的前驱和后继：

```text
删除前： P ⇄ X ⇄ N
修改：   X.previous.next = X.next
         X.next.previous = X.previous
删除后： P ⇄ N
```

随后清理 X 的两个引用，size--。头尾删除的 P 或 N 可以是 S，不需要 null 分支。

唯一业务节点时 `S ⇄ X ⇄ S`，第一句设 S.next=S，第二句设 S.previous=S，自动恢复空表。空表 remove 必须提前拒绝，否则会把哨兵当成数据节点清理，破坏整个结构。

**正确性：**插入把相邻 P/N 间的两条边替换为经过 X 的四条边，保持其余连接及顺序；删除执行逆操作。邻居始终存在，包括空表插入与唯一节点删除，因此统一改线也保持闭环与相邻节点的互反关系。参数检查保证被删对象永远不是 S。

## 5. 遍历与循环终止

正向从 S.next 开始，反向从 S.previous 开始。遇到 S 时停止，不把 S 输出。源码查找采用节点身份判断：

```java
while (current != sentinel) {
    // 只处理业务节点，然后沿 next 前进。
    current = current.next;
}
```

快照采用恰好 size 次访问；测试会额外验证 size 与实际环一致。空表起点就是 S，因此查找循环一次也不执行，查找 0 也应该返回 -1。不能判断“值为 0 就停止”，因为有效数据允许 0。

正常环没有 null，不能用 current != null 结束。也不要把 S.next 缓存在外部后跨越修改长期使用，节点不暴露就是为了避免这种耦合。

## 6. 反转：这次哨兵也必须参与

对包括哨兵在内的每个节点交换 previous 和 next，然后沿保存的原 next 继续。示例 `S⇄10⇄20⇄S`：

| 本轮节点 | 原 previous | 原 next | 新 previous | 新 next |
| --- | --- | --- | --- | --- |
| S | 20 | 10 | 10 | 20 |
| 10 | S | 20 | 20 | S |
| 20 | 10 | S | S | 10 |

处理后正向为 `S→20→10→S`，反向为 `S→10→20→S`。

循环使用 do-while，从 S 开始，直到沿原 next 回到 S。空表也处理 S 一次，交换两个自引用，仍合法。与普通单链表“哨兵不参与反转”的写法不同，这里 S 自己保存首尾入口，漏掉 S 会导致入口与业务节点的新连接不匹配。

**正确性：**循环每次保存原 next，再交换当前节点的两条边，已处理节点的边反向，未处理节点仍保留原边；按原环恰好访问所有节点一次，包括 S。全部交换后每对邻居仍互连，逻辑头尾随 S 的引用交换而改变，size 不变。中途结构可能暂时不满足完整不变量，不支持并发操作。

## 7. 完整执行与运行

main 与循环单链表使用相同操作：尾插 10、30，中间插 20，查找 20，删中间，反转，删尾删头，再插入 0。

| 阶段 | 正向结构 | size |
| --- | --- | --- |
| 创建 | `S ⇄ S` | 0 |
| 三次插入 | `S ⇄ 10 ⇄ 20 ⇄ 30 ⇄ S` | 3 |
| remove(1) | `S ⇄ 10 ⇄ 30 ⇄ S` | 2 |
| reverse() | `S ⇄ 30 ⇄ 10 ⇄ S` | 2 |
| removeLast() | `S ⇄ 30 ⇄ S` | 1 |
| removeFirst() | `S ⇄ S` | 0 |
| addFirst(0) | `S ⇄ 0 ⇄ S` | 1 |

在仓库根目录运行：

```bash
build_dir=$(mktemp -d /tmp/circular-double.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/linkedlist/CircularDoublyLinkedList.java
java -cp "$build_dir" learning.datastructures.linkedlist.CircularDoublyLinkedList
```

普通 JDK 可使用等价 javac。预期八行输出：

```text
[10, 20, 30]
1
20
[30, 10]
10
30
0
[0]
```

## 8. 成本、优势与替代方案

令 n 为业务节点数。实际有 n+1 个节点，总存储 O(n)，多一个固定哨兵。每节点比单链表多一个前驱引用。插入新分配一个节点，普通操作辅助空间 O(1)，快照返回空间 O(n)。

| 操作 | 时间 | 依据 |
| --- | --- | --- |
| 首尾插入、首尾删除、size | O(1) | 哨兵直接给出首尾，双向引用直接给出邻居 |
| 中间下标操作 | 最坏 O(n) | 从较近端定位，改线仅 O(1) |
| 按值查找、快照 | O(n) | 最多经过所有业务节点 |
| 原地反转 | O(n) | 交换 n+1 个节点的连接 |

相比无哨兵双向链表，渐进复杂度没有变化，优势是统一首尾边界、减少特殊分支；代价是多一个节点、遍历必须跳过哨兵、反转必须处理哨兵入口。不能虚构哨兵使按下标访问变成 O(1)。

相比“先复制值再重建逆序”，原地反转节省 O(n) 额外空间，却要求正确保存原后继。相比循环单链表，双向结构能后退和 O(1) 尾删，但要维护更多连接。

适用于需要统一边界的队列、局部摘除与插入等场景。LRU 缓存可用哈希表定位节点、双向链表维护顺序；本类只暴露值与下标，不直接实现 O(1) LRU。Java LinkedList API 不暴露这种哨兵环，也不要把此教学结构误认为其固定内部实现。

## 9. 易错点、复述与练习

| 常见错误 | 后果 |
| --- | --- |
| 空表只初始化 S.next | 反向操作会遇到 null |
| size 计入 S | 返回伪数据或错误接受越界下标 |
| 用值识别 S | 真正的数据 0 被误判 |
| 删除后只更新一侧 | 正向可能正确，反向却残留被删节点 |
| 反转漏掉 S | 入口与业务边的方向不一致 |
| 认为有 S 就不需要判空 | 可能删除永久哨兵 |

> 哨兵始终存在，空表时前后都指向自身，非空时它的 next 和 previous 分别给出首尾。插入统一接四条边，删除统一让两侧互连，唯一元素删除会自动回到哨兵自环。遍历以哨兵身份结束，size 不包含哨兵；按下标定位仍是 O(n)。

建议画出空表插入 0、唯一元素删除、两元素反转、反转后尾插的全部引用。练习未等同于学习者已经掌握。

## 10. 验证记录

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 两个新增实现与临时验证程序使用 `-Xlint:all -Werror` 编译成功；各自 main 的八行输出与文档一致。
- 本实现完成固定边界检查和固定种子 `20261110` 的 5,000 步随机操作，与 ArrayList 对照下标插删、首尾操作、查找和反转。两个实现合计 10,000 步。
- 每步检查 size、逐项 get、正序快照及快照独立性；双向版本同时验证倒序快照。
- 临时验证程序按节点身份检查环：恰好经过规定数量的不同节点后回到起点，不能提前闭合，也不能遇到 null；单向版额外检查 tail 位置，双向版检查哨兵与所有邻居的双向互连以及反向闭合。
- 固定用例覆盖空表反转、空表查找 0、空表删除异常、单节点自环、双节点删尾、删空重插、重复值、int 最小值和最大值。随机序列含负下标、读取或删除 size 位置、超范围插入，并核验异常后状态不变。
- 两个实现均通过 `-private -Xdoclint:all -Werror` Javadoc 生成。全仓库 30 个 Java 文件编译通过，仅历史 MergeTwoLists.java 保留已有未检查操作提示。

验证代码和产物位于仓库外临时目录。测试为有限范围验证，不代替结构不变量说明；没有构造计数上限规模，也未测试内存耗尽或并发使用。使用 Java 8 已有语法与 API，但仅在 Java 21 实际运行；未执行阿里规约自动检查工具。
