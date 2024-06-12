package subway;

import java.io.*;
import java.nio.charset.Charset;//UTF-8�ı��ĵ���ȡʹ��
import java.util.*;

class node {// ͼ�Ľڵ�(���Ŵ�1����)
    public String name;// վ������
    public int line;// վ��������·���

    public node(final String x, final int y) {
        name = x;
        line = y;
    }
}

class edge {// ʹ����ʽǰ�������ݽṹ���(�߱�Ŵ�1����)
    // �������˽���ʽǰ���ǣ���ο��� https://oi-wiki.org/graph/save/#_14
    public int to = 0;// �ñ�ָ����յ�ڵ���
    public int next = 0;// ͬ������һ���ߵı��(��Ч����nextָ��)
    public int time = 0;// ��ʱ(����)
    public int distance = 0;// ����(����)
    public int change = 0;// �Ƿ���Ҫ����(��0����1)
    // ��Ȼ���ݿ͹���ʵ�ɵ���ʱ�;��벻���ܳ���int��Χ��[0,2^31-1]

    public edge(final int p1, final int p2, final int p3, final int p4) {
        to = p1;
        next = p2;
        time = p3;
        distance = p4;
    }
}

class elem {
    public int i;// �ڵ���
    public int v;// �ѵıȽ�����

    public elem(final int x, final int y) {
        i = x;
        v = y;
    }
}

public class core {
    private static final int max_node = 200;// �������վ����
    private static final int max_edges = 1000;// ͼ������
    // ��Ȼ���ݿ͹���ʵ�����ݵ�����վ�����ͱ��������ܳ�����������

    public static int line_num = 0;// ������·��Ŀ
    public static node[] p = new node[max_node];// �±�=����
    private static edge[] e = new edge[max_edges * 2];// ����ͼ�������,�±�=����߱��;���������=�����
    private static int[] hd = new int[max_node];// hd[i]�Ǳ��i�ĵ�Ϊ���������ߵı��
    private static int cnt = 0;// ��ǰ����
    private static int n = 0;// ��ǰ�ڵ���

    private static void add_edge(final int u, final int v, final int t, final int d) {// ����һ�������(u,v)������յ��ŷֱ���u,v����ʱt������d
        e[++cnt] = new edge(v, hd[u], t, d);
        e[cnt].change = p[u].line == p[v].line ? 0 : 1;// ��ͬ��·Ҫ����
        hd[u] = cnt;
    }

    public static TreeMap<String, Integer> h = new TreeMap<>();// վ����-վ���Ź�ϣ������O(1)�ж�ĳ��վ���Ƿ����Ѵ���

    // ���ܣ���ȡ��ȷ��ʽ������
    public static void init() throws Exception {
        File f = new File("data.txt");// ��ȡλ����Ŀ·���µ�data.txt
        if (!f.exists()) {
            throw new Exception("���ݲ�����! ");
        }
        try {
            // ��ȡ UTF-8�����ļ�    
            InputStreamReader fr = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
            BufferedReader bfr = new BufferedReader(fr);
            String ln = "";
            while (null != (ln = bfr.readLine())) {// ÿ��ѭ�����ȡһ����·
                ++line_num;
                int line_index = Integer.valueOf(ln);
                String[] name = bfr.readLine().split(" ");// վ�������б�
                String[] distance = bfr.readLine().split(" ");// ����
                String[] time = bfr.readLine().split(" ");// ʱ��

                for (int i = 0; i < name.length; ++i) {
                    Integer idx = h.get(name[i]);
                    if (idx == null) {// ���վ��δ���ֹ�
                        p[++n] = new node(name[i], line_index);// ��ӽڵ�
                        h.put(name[i], n);// ��ϣ�����
                        idx = n;// ��ǰ�ڵ����½ڵ�
                    }
                    if (i > 0) {// �ӱ�
                        int v = idx;// ��iվ
                        int u = h.get(name[i - 1]);// ��һվi-1
                        int t = Integer.valueOf(time[i - 1]);
                        int d = Integer.valueOf(distance[i - 1]);
                        add_edge(u, v, t, d);
                        add_edge(v, u, t, d);
                        // һ������ߺ�һ�����߱�ʾһ�������
                    }
                }
            }
            bfr.close();
            fr.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public static int pr[] = new int[max_edges];// �ڴ��б��i�ĵ����һվ��pr[i]
    public static int d[] = new int[max_edges];// �ڴ��б��iΪ�յ�����·ֵ

    private static Comparator<elem> cmp = new Comparator<elem>() {
        public int compare(elem lhs, elem rhs) {
            return rhs.v - lhs.v;
        }
    };

    public static int get_weight(int i, int type) {// type=1����С����/����; type=2����С��ʱ; type=3����С���ˣ�ȡ��i�Ķ�Ӧֵ
        // ������Ϊ�����Ǿ���ĺ���������أ����Ի�����Сһ��������С
        if (type == 1 || type == 4) {// type4�ǻ��ѣ���Ч����
            return e[i].distance;
        } else if (type == 2) {
            return e[i].time;
        } else if (type == 3) {
            return e[i].change;
        } else {
            return -1;
        }
    }

    private static final int big = (1 << 31) - 1;// ���ֵ����������ͨ·

    public static void dijkstra(int s, int type) {// ��sΪԴ�������·,type=1����С����/����; type=2����С��ʱ; type=3����С����
        // ��ʽǰ����ʵ�ֵĶ�����Ż���Dijkstra��Դ���·��ʱ�临�Ӷ�O(nlogn)
        d = new int[n + 3];// ��ǰ��s���յ�i���·֮����d[i]
        boolean vis[] = new boolean[n + 3];// �Ƿ��������i,����vis[i]=true
        Arrays.fill(d, 0, n + 1, big);
        Arrays.fill(vis, 0, n + 1, false);
        Arrays.fill(pr, 0, n + 1, 0);// 0�����������
        d[s] = 0;// ��㵽�Լ�������0
        Queue<elem> q = new PriorityQueue<>(cmp);// ���ȼ�����ʵ�ֶ����
        q.add(new elem(s, 0));// ��Ԫ����ѣ��ѵıȽ�������d[s]�Ĵ�С
        while (!q.isEmpty()) {
            elem t = q.peek();// ȡ�Ѷ�
            q.poll();// ɾ���Ѷ�
            int u = t.i;// ��ǰ��
            if (vis[u]) {// �Ѿ�������
                continue;
            }
            vis[u] = true;
            for (int i = hd[u], v; i != 0; i = e[i].next) {// ��ʽǰ���ǵı�������ȡ������uΪ���ıߣ�ÿ�α߱�ŷֱ���i
                v = e[i].to;// �ñ��յ�
                int d_edge = get_weight(i, type);// ��Ȩ
                if (d[v] > d[u] + d_edge) {// ���������β���ʽ������ǰ����
                    d[v] = d[u] + d_edge;// ���¸���ֵ
                    pr[v] = u;// ����·����
                    if (!vis[v]) {
                        q.add(new elem(v, d[v]));// ��Ԫ�����
                    }
                }
            }
        }
    }

    public static int cal_cost(int d) {// �Ӿ���d����ת��Ϊ�۸�
        if (d >= 24) {// >=24km��ÿ8����һ��Ǯ
            return 1 + cal_cost(d - 8);
        }
        if (d >= 12) {// [12,24)��ÿ6����һ��Ǯ
            return 1 + cal_cost(d - 6);
        }
        if (d >= 4) {// [4,12)��ÿ4����һ��Ǯ
            return 1 + cal_cost(d - 4);
        }
        return 2;// �𲽼�
    }

    public static String get_path(int r, int t, int type) {// ���·���滮��
        // type=1����С����; type=2����С��ʱ; type=3����С���ˣ�type=4����С����
        // s������ţ�t���յ���

        if (r == t) {
            return "�����յ�һ�¡�";
        }
        if (d[t] == big) {
            return "��·�����ڡ�";
        }

        Stack<Integer> s = new Stack<>();// �����յ㵽����·��ѹջ���õ�����㵽�յ��·��
        int i = t;
        while (i != 0) {// ��·����û����(�����˵�ǰt����0��)
            s.add(i);// ��ջ
            i = pr[i];// ����������·��
        }
        int len = s.size();// վ����

        String ans = "��";
        ans += p[r].name;
        ans += "��";
        ans += p[t].name;// �⼸������һ���ʱ����vscode�༭������
        if (type == 1) {
            ans += "����С�������·�滮����:";
        } else if (type == 2) {
            ans += "��������ʱ���·�滮����:";
        } else if (type == 3) {
            ans += "�����ٻ������·�滮����:";
        } else if (type == 4) {
            ans += "����С�������·�滮����:";
        }
        ans += "\n���ȳ���";
        ans += p[r].line;
        ans += "����\n";

        Integer prev = 0;// ��һվ
        while (!s.isEmpty()) {// ջ�ǿ�ʱ
            Integer now = s.peek(); // ��ǰվ��
            if (prev != 0) {
                ans += p[prev].name;
                ans += " -> ";
                ans += p[now].name;
                ans += "\n";

                if (p[now].line != p[prev].line) {// ��Ҫ����
                    ans += "����һվ�³�������";
                    ans += p[now].line;
                    ans += "���ߣ���������\n";
                }
            }
            prev = now;// ������һվ
            s.pop();// ��ջ
        }

        if (type == 1) {
            ans += "������";
            ans += d[t] / 10 + "." + d[t] % 10;
            ans += "ǧ��";
        } else if (type == 2) {
            ans += "��ʱ��";
            ans += d[t];
            ans += "����";
        } else if (type == 3) {
            ans += "���˴�����";
            ans += d[t];
            ans += "��";
        } else if (type == 4) {
            ans += "������:";
            ans += cal_cost(d[t] / 10);// ת��Ϊǧ��
            ans += "Ԫ";
        }

        ans += " ����������";
        ans += len;
        ans += "��վ�� ";
        return ans;
    }

    public static void main(String[] args) {
        new ui("�ۺ�ʵ��3 ����·�߹滮");// ͬ·���µ�ui.java�Ĺ��캯��
    }

    private core() {// ��ֹ����coreʵ��
    }
}
