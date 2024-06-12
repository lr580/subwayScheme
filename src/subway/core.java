package subway;

import java.io.*;
import java.nio.charset.Charset;//UTF-8文本文档读取使用
import java.util.*;

class node {// 图的节点(点编号从1递增)
    public String name;// 站点名字
    public int line;// 站点所属线路编号

    public node(final String x, final int y) {
        name = x;
        line = y;
    }
}

class edge {// 使用链式前向星数据结构存边(边编号从1递增)
    // 若您不了解链式前向星，请参考： https://oi-wiki.org/graph/save/#_14
    public int to = 0;// 该边指向的终点节点编号
    public int next = 0;// 同起点的下一条边的编号(等效链表next指针)
    public int time = 0;// 用时(分钟)
    public int distance = 0;// 距离(百米)
    public int change = 0;// 是否需要换乘(否0，是1)
    // 显然根据客观事实可得用时和距离不可能超过int范围即[0,2^31-1]

    public edge(final int p1, final int p2, final int p3, final int p4) {
        to = p1;
        next = p2;
        time = p3;
        distance = p4;
    }
}

class elem {
    public int i;// 节点编号
    public int v;// 堆的比较依据

    public elem(final int x, final int y) {
        i = x;
        v = y;
    }
}

public class core {
    private static final int max_node = 200;// 地铁最大站点数
    private static final int max_edges = 1000;// 图最大边数
    // 显然根据客观现实：广州地铁的站点数和边数不可能超过上述常量

    public static int line_num = 0;// 地铁线路数目
    public static node[] p = new node[max_node];// 下标=点编号
    private static edge[] e = new edge[max_edges * 2];// 无向图两倍存边,下标=有向边编号;两条有向边=无向边
    private static int[] hd = new int[max_node];// hd[i]是编号i的点为起点的首条边的编号
    private static int cnt = 0;// 当前边数
    private static int n = 0;// 当前节点数

    private static void add_edge(final int u, final int v, final int t, final int d) {// 增加一条有向边(u,v)，起点终点编号分别是u,v，用时t，距离d
        e[++cnt] = new edge(v, hd[u], t, d);
        e[cnt].change = p[u].line == p[v].line ? 0 : 1;// 不同线路要换乘
        hd[u] = cnt;
    }

    public static TreeMap<String, Integer> h = new TreeMap<>();// 站点名-站点编号哈希表，用于O(1)判断某个站点是否是已存在

    // 功能：读取正确格式的数据
    public static void init() throws Exception {
        File f = new File("data.txt");// 读取位于项目路径下的data.txt
        if (!f.exists()) {
            throw new Exception("数据不存在! ");
        }
        try {
            // 读取 UTF-8编码文件    
            InputStreamReader fr = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
            BufferedReader bfr = new BufferedReader(fr);
            String ln = "";
            while (null != (ln = bfr.readLine())) {// 每次循环体读取一条线路
                ++line_num;
                int line_index = Integer.valueOf(ln);
                String[] name = bfr.readLine().split(" ");// 站点名字列表
                String[] distance = bfr.readLine().split(" ");// 距离
                String[] time = bfr.readLine().split(" ");// 时间

                for (int i = 0; i < name.length; ++i) {
                    Integer idx = h.get(name[i]);
                    if (idx == null) {// 这个站点未出现过
                        p[++n] = new node(name[i], line_index);// 添加节点
                        h.put(name[i], n);// 哈希表更新
                        idx = n;// 当前节点是新节点
                    }
                    if (i > 0) {// 加边
                        int v = idx;// 第i站
                        int u = h.get(name[i - 1]);// 上一站i-1
                        int t = Integer.valueOf(time[i - 1]);
                        int d = Integer.valueOf(distance[i - 1]);
                        add_edge(u, v, t, d);
                        add_edge(v, u, t, d);
                        // 一条有向边和一条反边表示一条无向边
                    }
                }
            }
            bfr.close();
            fr.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public static int pr[] = new int[max_edges];// 在答案中编号i的点的上一站是pr[i]
    public static int d[] = new int[max_edges];// 在答案中编号i为终点的最短路值

    private static Comparator<elem> cmp = new Comparator<elem>() {
        public int compare(elem lhs, elem rhs) {
            return rhs.v - lhs.v;
        }
    };

    public static int get_weight(int i, int type) {// type=1是最小距离/花费; type=2是最小用时; type=3是最小换乘；取边i的对应值
        // 这是因为花费是距离的函数且正相关，所以花费最小一定距离最小
        if (type == 1 || type == 4) {// type4是花费，等效距离
            return e[i].distance;
        } else if (type == 2) {
            return e[i].time;
        } else if (type == 3) {
            return e[i].change;
        } else {
            return -1;
        }
    }

    private static final int big = (1 << 31) - 1;// 最大值，代表不存在通路

    public static void dijkstra(int s, int type) {// 以s为源点跑最短路,type=1是最小距离/花费; type=2是最小用时; type=3是最小换乘
        // 链式前向星实现的二叉堆优化的Dijkstra单源最短路，时间复杂度O(nlogn)
        d = new int[n + 3];// 当前从s到终点i最短路之和是d[i]
        boolean vis[] = new boolean[n + 3];// 是否遍历过点i,是则vis[i]=true
        Arrays.fill(d, 0, n + 1, big);
        Arrays.fill(vis, 0, n + 1, false);
        Arrays.fill(pr, 0, n + 1, 0);// 0代表遍历结束
        d[s] = 0;// 起点到自己距离是0
        Queue<elem> q = new PriorityQueue<>(cmp);// 优先级队列实现二叉堆
        q.add(new elem(s, 0));// 新元素入堆，堆的比较依据是d[s]的大小
        while (!q.isEmpty()) {
            elem t = q.peek();// 取堆顶
            q.poll();// 删除堆顶
            int u = t.i;// 当前点
            if (vis[u]) {// 已经遍历过
                continue;
            }
            vis[u] = true;
            for (int i = hd[u], v; i != 0; i = e[i].next) {// 链式前向星的遍历；即取所有以u为起点的边，每次边编号分别是i
                v = e[i].to;// 该边终点
                int d_edge = get_weight(i, type);// 边权
                if (d[v] > d[u] + d_edge) {// 满足三角形不等式，代表当前更优
                    d[v] = d[u] + d_edge;// 更新更优值
                    pr[v] = u;// 更新路径答案
                    if (!vis[v]) {
                        q.add(new elem(v, d[v]));// 新元素入堆
                    }
                }
            }
        }
    }

    public static int cal_cost(int d) {// 从距离d公里转化为价格
        if (d >= 24) {// >=24km，每8公里一块钱
            return 1 + cal_cost(d - 8);
        }
        if (d >= 12) {// [12,24)，每6公里一块钱
            return 1 + cal_cost(d - 6);
        }
        if (d >= 4) {// [4,12)，每4公里一块钱
            return 1 + cal_cost(d - 4);
        }
        return 2;// 起步价
    }

    public static String get_path(int r, int t, int type) {// 输出路径规划答案
        // type=1是最小距离; type=2是最小用时; type=3是最小换乘；type=4是最小花费
        // s是起点编号，t是终点编号

        if (r == t) {
            return "起点和终点一致。";
        }
        if (d[t] == big) {
            return "线路不存在。";
        }

        Stack<Integer> s = new Stack<>();// 将从终点到起点的路径压栈，得到从起点到终点的路径
        int i = t;
        while (i != 0) {// 当路径还没走完(走完了当前t就是0了)
            s.add(i);// 入栈
            i = pr[i];// 不断走这条路径
        }
        int len = s.size();// 站点数

        String ans = "从";
        ans += p[r].name;
        ans += "到";
        ans += p[t].name;// 这几行连在一起的时候，用vscode编辑不可行
        if (type == 1) {
            ans += "的最小距离最短路规划如下:";
        } else if (type == 2) {
            ans += "的最少用时最短路规划如下:";
        } else if (type == 3) {
            ans += "的最少换乘最短路规划如下:";
        } else if (type == 4) {
            ans += "的最小花费最短路规划如下:";
        }
        ans += "\n首先乘坐";
        ans += p[r].line;
        ans += "号线\n";

        Integer prev = 0;// 上一站
        while (!s.isEmpty()) {// 栈非空时
            Integer now = s.peek(); // 当前站点
            if (prev != 0) {
                ans += p[prev].name;
                ans += " -> ";
                ans += p[now].name;
                ans += "\n";

                if (p[now].line != p[prev].line) {// 需要换乘
                    ans += "从这一站下车，换乘";
                    ans += p[now].line;
                    ans += "号线，继续乘坐\n";
                }
            }
            prev = now;// 更新上一站
            s.pop();// 弹栈
        }

        if (type == 1) {
            ans += "距离是";
            ans += d[t] / 10 + "." + d[t] % 10;
            ans += "千米";
        } else if (type == 2) {
            ans += "用时是";
            ans += d[t];
            ans += "分钟";
        } else if (type == 3) {
            ans += "换乘次数是";
            ans += d[t];
            ans += "次";
        } else if (type == 4) {
            ans += "花费是:";
            ans += cal_cost(d[t] / 10);// 转化为千米
            ans += "元";
        }

        ans += " ，共经过了";
        ans += len;
        ans += "个站点 ";
        return ans;
    }

    public static void main(String[] args) {
        new ui("综合实验3 地铁路线规划");// 同路径下的ui.java的构造函数
    }

    private core() {// 禁止创建core实例
    }
}
