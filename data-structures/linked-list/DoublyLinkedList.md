# 双向链表（Doubly Linked List）

[返回导航](../README.md) · [单链表基础](LinkedList.md) · [Java 完整实现](../../src/learning/datastructures/linkedlist/DoublyLinkedList.java)

学习日期：2026-11-10（北京时间）。本节实现非循环、无哨兵、维护 head 和 tail 的整数双向链表。相关变体已另行完成：[循环单链表](CircularSinglyLinkedList.md)与[带哨兵的循环双向链表](CircularDoublyLinkedList.md)，分别配套 Java 实现与验证。

## 1. 为什么从单链表走向双向链表

单链表只知道后继。如果只拿到尾节点，仍无法直接找到它的前驱，删除尾节点通常要从头扫描。给每个节点增加 previous 引用，就可以沿两个方向移动。

```text
head                                      tail
 ↓                                         ↓
[null | 10 | next] ⇄ [prev | 20 | next] ⇄ [prev | 30 | null]
```

每个节点由“前驱引用、值、后继引用”组成。图中 `A ⇄ B` 表示同时满足 `A.next == B` 和 `B.previous == A`，不是一个自动双向的引用；代码必须分别维护这两条连接。

节点无须在内存中相邻，引用不等于数值下标。允许重复值，节点身份和节点保存的值是两回事。

本实现的状态必须满足：

- 空表：size=0，head 和 tail 都是 null。
- 非空表：head.previous 为 null，tail.next 为 null。
- 每一对相邻节点的 next 和 previous 相互对应。
- 从 head 正向或从 tail 反向遍历，均恰好经过 size 个有效节点，没有环。

单节点时 head 与 tail 指向同一个对象，它的两个连接都是 null。

## 2. 实现方式的取舍

| 常用组织方式 | 空表表示 | 优势 | 代价 |
| --- | --- | --- | --- |
| 无哨兵、非循环（本节） | head=tail=null | 首尾语义直观，不创建辅助节点 | 插删时需要检查缺失的邻居 |
| 两个哨兵、非循环 | 头哨兵与尾哨兵相连 | 中间与端点的插删可以统一 | 需要跳过两个无业务含义节点 |
| 单哨兵、循环双向链表（见独立专题） | 哨兵的前后引用均指向自身 | 用一个哨兵统一边界 | 遍历必须识别哨兵，不能等 null |

这些都是双向连接的不同组织方式。本节先把真实端点和边界分支讲清，再学习哨兵如何消除特殊情况。它们不是 Java 标准库接口要求的唯一实现方式。

## 3. 接口与使用约定

源码位于 `src/learning/datastructures/linkedlist/DoublyLinkedList.java`，包名为 `learning.datastructures.linkedlist`。没有共享或修改历史节点类型。

| 接口 | 行为 | 边界 |
| --- | --- | --- |
| `size()` | 读取有效节点数 | 空表返回 0 |
| `get(index)` | 按下标读取 | `[0,size)` |
| `insert(index,value)` | 在指定位置插入 | `[0,size]`，size 表示尾插 |
| `remove(index)` | 按下标删除并返回值 | `[0,size)` |
| `addFirst/addLast` | 首尾插入 | 支持空表 |
| `removeFirst/removeLast` | 首尾删除并返回值 | 空表抛越界异常 |
| `indexOf(value)` | 返回第一个匹配下标 | 没有匹配返回 -1 |
| `toArray/toReversedArray` | 返回正向或反向快照 | 不改变链表，返回独立数组 |
| `reverse()` | 原地反转节点连接 | 空表、单节点均适用 |

所有 int 值和重复值都合法。非法下标抛出 `IndexOutOfBoundsException`，且在修改前完成检查；节点数量达到 `Integer.MAX_VALUE` 时插入抛出 `IllegalStateException`，实际容量还受内存限制。节点不向外暴露，类不支持并发修改。

## 4. 定位：从较近端开始，仍不是随机访问

朴素方法只从 head 找下标 i，最多走 i 步。既然已经保存 tail 和 previous，就可以比较位置，从较近的一端出发。

```text
下标： 0 ⇄ 1 ⇄ 2 ⇄ 3 ⇄ 4 ⇄ 5
查 1：从 head 前进 1 次
查 4：从 tail 后退 1 次
```

源码 `nodeAt` 使用 `index < size / 2` 选择方向，移动次数为 `min(i,n-1-i)`，时间为 O(1+min(i,n-1-i))。相比单向扫描减少部分移动，最坏仍是 O(n)，没有达到数组的 O(1) 按下标读取。

## 5. 插入：新节点与两侧邻居都要连接

在 `[10,30]` 的下标 1 插入 20。先找到原位置的后继 successor=30，再读取它的前驱 predecessor=10。

```text
原来：  [10] ⇄ [30]
新建：         [20]

1. inserted.previous = predecessor   20 的前驱是 10
2. inserted.next = successor         20 的后继是 30
3. predecessor.next = inserted       10 的后继改为 20
4. successor.previous = inserted    30 的前驱改为 20

结果：  [10] ⇄ [20] ⇄ [30]
```

前两步只设置新节点，保留原邻居引用；后两步分别修复邻居的连接。若只修改 next，正向看似正常，反向仍可能跳过新节点，因此必须双向验证。

统一逻辑中的边界如下：

| 插入场景 | predecessor | successor | 额外处理 |
| --- | --- | --- | --- |
| 中间 | 原前驱 | 原位置节点 | 更新两侧邻居 |
| 头部 | null | 原 head | 将 head 设为新节点 |
| 尾部 | 原 tail | null | 将 tail 设为新节点 |
| 空表 | null | null | head、tail 都指向新节点 |

最后 size 加一。源码尾插直接使用 tail，无需遍历；头插定位 head 也不需要遍历。

## 6. 删除：让前驱与后继互相接上

删除 `[10,20,30]` 中的 20：

```text
原来：  [10] ⇄ [20] ⇄ [30]
         P      X      S

P.next = S
S.previous = P
X.previous = null
X.next = null

结果：  [10] ⇄ [30]       [20] 已脱离链表
```

P、X、S 分别是前驱、目标和后继。必须先保存邻居，再断开目标节点。清理引用不意味着立刻释放内存；实际回收时间由垃圾收集器决定。

头删时没有 P，直接令 head=S，并令新头的 previous=null。尾删时没有 S，令 tail=P，并令新尾的 next=null。删除唯一节点时两侧都为 null，head、tail 同时归空。最后 size 减一。

**为什么安全：**中间删除仅用 P 与 S 的互连替换经过 X 的两条连接，其余节点顺序不变。边界分支保持首尾引用为 null；唯一节点删除恢复空表不变量。插入则是这一过程的逆向操作，增加一个有效节点而不丢失原后缀。

## 7. 反转与倒序读取不是一回事

`toReversedArray()` 仅从 tail 沿 previous 读取，不修改链表。`reverse()` 则真正交换每个节点的前后引用，最后交换 head 与 tail。

对 `[10,20,30]`，沿**原 next 方向**逐个处理：

| 已处理节点 | 该节点的新 previous | 该节点的新 next | 下一处理节点（原后继） |
| --- | --- | --- | --- |
| 10 | 20 | null | 20 |
| 20 | 30 | 10 | 30 |
| 30 | null | 20 | null |

最后 head=30、tail=10，得到 `null ← 30 ⇄ 20 ⇄ 10 → null`。

```java
Node originalNext = current.next;
current.next = current.previous;
current.previous = originalNext;
current = originalNext;
```

这是类内部片段，完整可运行代码以源码文件为准。先存原后继，再交换，否则无法保证按原顺序继续遍历。

循环不变量是：已处理节点的两个连接已交换，未处理节点保持原连接，current 指向原顺序的第一个未处理节点。每轮沿保存的原 next 推进，恰好访问所有节点一次；完成后原相邻关系的两条边都反向，交换端点即可恢复完整结构。过程中暂时不满足全局双向互连，不能并发读取或修改。

空表循环不执行，交换两个 null 即可；单节点的两个引用都是 null，交换后仍合法。

## 8. 完整执行示例与运行方式

main 的操作追踪：

| 操作 | 正向序列 | 返回值或输出 |
| --- | --- | --- |
| addFirst(10) | `[10]` | — |
| addLast(30) | `[10,30]` | — |
| insert(1,20) | `[10,20,30]` | 正序输出 `[10, 20, 30]` |
| toReversedArray() | 不变 | `[30, 20, 10]` |
| remove(1) | `[10,30]` | 20 |
| reverse() | `[30,10]` | 正序输出 `[30, 10]` |
| removeLast() | `[30]` | 10 |
| removeFirst() | `[]` | 30 |
| size() | `[]` | 0 |

在仓库根目录运行：

```bash
build_dir=$(mktemp -d /tmp/doubly-list-demo.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/linkedlist/DoublyLinkedList.java
java -cp "$build_dir" learning.datastructures.linkedlist.DoublyLinkedList
```

普通完整 JDK 可使用等价的 javac 命令。七行输出为：

```text
[10, 20, 30]
[30, 20, 10]
20
[30, 10]
10
30
0
```

## 9. 复杂度与优势、代价

n 为节点数。节点本身总存储 O(n)，普通操作辅助空间 O(1)；插入多分配一个节点，快照另需 O(n) 返回空间。

| 操作 | 只含头入口的单链表 | 本节双向链表 |
| --- | --- | --- |
| 头插、头删 | O(1) | O(1) |
| 尾插 | O(n)，加尾指针可降为 O(1) | O(1) |
| 尾删 | O(n)，有尾指针一般仍需找前驱 | O(1) |
| 按下标查找、插删 | 最坏 O(n) | 最坏 O(n)，可从较近端定位 |
| 按值查找 | 最坏 O(n) | 最坏 O(n) |
| 已知内部节点的删除 | 通常还需前驱 | 改线 O(1) |
| 逆向遍历 | 需额外办法，如栈或先反转 | 沿 previous 直接 O(n) |
| 原地反转 | O(n) 时间、O(1) 辅助空间 | O(n) 时间、O(1) 辅助空间 |

本实现不暴露节点，所以对外的 `remove(index)` 仍包含定位成本。双向链表减少了某些定位与回溯工作，但每节点多一个引用，插删要维护更多连接，也更容易出现只修复一侧的错误。

适用于需要双向遍历、首尾插删或已持有节点位置的局部调整。例如 LRU 缓存常用“哈希表定位节点 + 双向链表维护访问顺序”，哈希表解决查找成本，链表解决移动成本；本类的按下标接口本身不能直接保证 LRU 所需的 O(1) 操作。相关练习为 146「LRU 缓存」，本次并未新增该题题解。

Java `LinkedList<E>` 使用双向链表，且实现 List、Deque 接口；按下标 get 最坏仍为 O(n)。遍历时重复 get(i) 可能造成 O(n²)，应使用顺序迭代。双向链表也不意味着总比数组快，连续扫描、随机访问和对象分配成本都要考虑。

## 10. 边界与面试表达

| 用例 | 预期检查 |
| --- | --- |
| 空表反转与快照 | 不抛异常，两个快照都是空数组 |
| 空表首尾删除 | 抛 IndexOutOfBoundsException，保持为空 |
| 唯一节点插入、删除 | head 与 tail 同步建立、同步归空 |
| 中间插入、删除 | 正反向遍历都符合预期 |
| 连续尾删 | tail 与新尾 next 都正确 |
| 重复值查找 | 返回最左匹配下标 |
| 反转后继续首尾插删 | 端点与两向连接保持有效 |
| 修改快照内容 | 不影响原链表 |
| 非法下标 | 在修改前抛异常，size 与连接均不变 |

可以这样复述：

> 双向链表用 previous 和 next 记录两个方向，相邻节点必须互相指向。保存 head 与 tail 后首尾插删都是 O(1)。中间操作先定位，再同时修改两侧连接，按下标最坏仍为 O(n)。我会特别验证空表、唯一节点、正反向遍历和端点更新，避免正向正确但反向断链。

练习：画出删除唯一节点时的两个分支；解释反转为何必须保存原 next；给出一个“只更新 next”会导致反向遍历错误的插入例子。练习建议不代表已经掌握。

## 11. 验证记录

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 新实现与临时验证程序以 `-Xlint:all -Werror` 编译通过，main 七行输出与上文一致。
- 固定边界覆盖空表、单节点反转、唯一节点首尾删除、重新插入、重复值首个匹配、未匹配、int 最小值与最大值。
- 固定种子 `20261110` 的 5,000 步随机操作，与 ArrayList 对照插入、删除、首尾操作、反转和查找；每步检查正反向快照、size 和逐项 get。
- 通过临时验证程序读取内部连接，每步检查 head/tail、相邻节点双向互连、两端终止于 null，以及恰好 size 个可达节点。此检查未向生产源码增加测试接口。
- 覆盖负下标、读取或删除 size 位置、超出 size 的插入，确认异常后内容不变；检查快照修改不影响原表。
- Javadoc 使用 `-private -Xdoclint:all -Werror` 生成成功；数据结构文档内 18 个本地链接检查通过。
- 全仓库 28 个 Java 文件编译通过；历史 MergeTwoLists.java 的未检查操作提示仍存在。

验证程序及产物均位于仓库外临时目录。随机检查为有限范围验证，不替代不变量说明；没有实际构造 Integer.MAX_VALUE 个节点，也未测试内存耗尽与并发。仅在 Java 21 实际运行，未执行阿里规约自动检查工具。
