# 链式栈（LinkedStack）

[栈总览](Stack.md) · [Java 实现](../../src/learning/datastructures/stack/LinkedStack.java) · [单链表基础](../linked-list/LinkedList.md)

## 1. 选择链表头作为栈顶

栈只需要操作一个端点，单链表头插、头删都不需要寻找前驱。无需为教学栈维护尾指针、双向连接或环，也无需暴露任意下标插删。

```text
push(10)、push(20)、push(30) 后：
top → [30 | next] → [20 | next] → [10 | null]
        栈顶                       栈底
size=3
```

结构不变量：top 指向最近入栈且尚未弹出的节点；从 top 沿 next 恰好经过 size 个节点后到达 null，没有环。空栈 top=null 且 size=0。节点私有，避免外部改线破坏结构。

## 2. 入栈：先连旧头，再更新入口

初始 `top→20→10→null`，push(30)：

```text
1. 创建新节点，值为 30
2. 新节点.next = 原 top       30 → 20 → 10 → null
3. top = 新节点
4. size++
```

源码通过 `new Node(value, top)` 在构造时完成连接，再更新 top。空栈时原 top=null，同一段代码自然建立单节点表。若先覆盖 top 再尝试读取旧头，可能丢失原有链表，因此应保留连接顺序。

每次创建一个新节点，没有整体扩容复制，但分配可能受内存和垃圾收集影响。O(1) 是算法成本模型，不是实际延迟承诺。

## 3. 出栈：把入口移动到后继

从 `top→30→20→10→null` 弹出：

```text
removed = top           保存值为 30 的节点
top = removed.next      新入口为 20
removed.next = null     清除脱离节点的连接
size--                  有效节点剩 2 个
返回 removed.value      返回 30
```

唯一节点被弹出时，其 next=null，top 自动归空，不需要单独分支。删除后节点不再由栈保留，但垃圾收集何时回收不由 pop 保证。peek 仅返回 top.value，不改变 top 或 size。

**正确性：**初始为空。push 将新节点接在原序列之前，原有元素顺序保留，最近值成为首节点；pop 移除该首节点并恢复上一个栈顶。每次计数与节点增减一致，且新节点只连接已有的无环链，保持无环性。先判断空栈，避免读取 null 或负计数。

## 4. 与尾部操作的对比

朴素做法若把单链表尾作为栈顶，尾插可借助 tail 做到 O(1)，但尾删仍要寻找前驱，最坏 O(n)。使用头部操作消除该遍历，且省掉尾指针状态。

双向链表也能实现栈，但栈不需要双向导航，增加 previous 引用通常没有必要。循环链表也不会让基础栈的操作复杂度进一步降低，反而增加终止条件。

## 5. API、复杂度与容量

提供 push、pop、peek、size、isEmpty、toArray，与数组栈语义一致。快照按栈顶到栈底，返回独立 int 数组。允许重复及所有 int 值；空栈 peek/pop 抛 NoSuchElementException，不改变内容。size 达到 Integer.MAX_VALUE 时 push 抛 IllegalStateException，实际规模仍受内存约束；不支持并发修改。

令 n 为当前有效元素数：

| 操作 | 时间 | 空间变化 |
| --- | --- | --- |
| push | O(1) | 新增一个节点 O(1) |
| pop | O(1) | 一个节点脱离结构，无额外集合 |
| peek、size、判空 | O(1) | O(1) 辅助空间 |
| toArray | O(n) | O(n) 返回数组 |

有效节点总存储 O(n)，每节点包含值、next 引用及 JVM 对象开销，不能按只有 4 字节整数估算节点空间。相比数组栈没有预留槽位或扩容复制，但对象较多、局部性通常较差；没有哪一种结构在所有场景下都最好。

## 6. 运行与示例

在仓库根目录执行：

```bash
build_dir=$(mktemp -d /tmp/linked-stack.XXXXXX)
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 \
    -d "$build_dir" src/learning/datastructures/stack/LinkedStack.java
java -cp "$build_dir" learning.datastructures.stack.LinkedStack
```

普通 JDK 可使用等价 javac。main 与数组栈执行相同操作，展示相同抽象可以采用不同存储：

```text
[30, 20, 10]
30
3
30
[40, 20, 10]
40
20
10
true
0
```

追踪要点：peek 后仍有三个节点；pop 30 后 push 40，新头是 40；弹出顺序为 40、20、10；弹空后 push(0) 重新建立单节点结构，0 不表示空。

## 7. 易错点与复述

常见错误：入栈覆盖旧头导致断链；将栈顶放尾部却宣称 pop 为 O(1)；弹出唯一节点后 size 未归零；peek 修改状态；用某个整数代表空栈；把对象不再可达当成内存已立即释放。

> 链式栈把单链表头作为栈顶，push 新建节点连接旧头，pop 将头移动到后继，都是常数次引用操作。peek 只读不修改。相比数组栈避免扩容复制，但每次入栈都分配节点，空间还包含对象和引用开销。

练习：画出空表入栈、弹出唯一节点、连续压入两个相同值的节点关系；说明为什么值相同也应作为两次独立 push 保存。与 [栈总览](Stack.md) 中括号匹配、撤销等场景联系理解，练习未代表学习者完成。

## 8. 验证记录

2026-11-10 在 OpenJDK 21.0.12.1 实际执行通过：

- 两个新增类和临时验证程序使用 `-Xlint:all -Werror` 编译成功，两个 main 的十行输出均与文档一致。
- 数组栈分别使用初始容量 0、1、默认 4；链式栈使用空构造，共四组配置。每组验证固定值 0、-1、int 最小值和最大值、重复值，以及空栈 pop/peek 异常。
- 每组连续入栈 4,097 个值后完整逆序弹出，覆盖数组多次扩容、内容保留和弹空后的状态。
- 每组以固定种子 `20261110` 执行 5,000 步随机操作，与 ArrayDeque 对照，合计 20,000 步。每步检查 size、判空、peek、快照顺序和快照独立性。
- 检查数组容量不小于 size；链式栈逐项检查实际节点值、可达数量以及末端为 null，防止多余节点或环。负初始容量拒绝行为也已检查。
- 标准 Deque 示例的后进先出与空时 peek 返回 null 已实际核验。
- 两个类通过 `-private -Xdoclint:all -Werror` Javadoc 生成。全仓库 32 个 Java 文件编译通过，仅历史 MergeTwoLists.java 保留已有未检查操作提示。

验证程序、文档产物及 class 文件均在仓库外临时目录，未新增依赖。上述为有限验证，不等同于所有操作序列的证明；未实际分配接近 Integer.MAX_VALUE 的数组或节点，未测试内存耗尽与并发。源码使用 Java 8 已有语法和 API，但仅在 Java 21 实际运行；未执行阿里规约自动检查工具。
