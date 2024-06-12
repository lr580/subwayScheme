package subway;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ui extends JFrame {
	private void initialize() {
        try {
            core.init();// 调用同目录下代码文件core.java的类方法
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "找不到数据文件! ");
        }
    }

    public ui(String title) {
        super(title);
        setIconImage(new ImageIcon("icon.png").getImage());// 图标
        Container ct = getContentPane();
        initialize();

        JPanel p_main = new JPanel(new GridLayout(1, 2, 15, 15));
        ct.add(p_main, BorderLayout.CENTER);

        // String stations[] = new String[core.h.size()];
        Vector<String> stations = new Vector<>();
        Iterator<String> it = core.h.keySet().iterator();
        while (it.hasNext()) {
            stations.addElement(it.next());
        }

        JPanel p_side = new JPanel(new GridLayout(1, 2, 10, 15));
        JList<String> sele_start = new JList<>(stations);
        sele_start.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// 单选限制
        sele_start.setSelectedIndex(0);
        JScrollPane src_start = new JScrollPane(sele_start);
        JPanel p_mleft = new JPanel(new BorderLayout());
        p_mleft.add(new JLabel("选择起点: "), BorderLayout.NORTH);
        p_mleft.add(src_start, BorderLayout.CENTER);
        p_side.add(p_mleft);

        JList<String> sele_terminal = new JList<>(stations);
        sele_terminal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sele_terminal.setSelectedIndex(1);
        JScrollPane src_terminal = new JScrollPane(sele_terminal);
        JPanel p_mmid = new JPanel(new BorderLayout());
        p_mmid.add(new JLabel("选择终点: "), BorderLayout.NORTH);
        p_mmid.add(src_terminal, BorderLayout.CENTER);
        p_side.add(p_mmid);

        JPanel p_mright_top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        String types[] = { "最短距离规划", "最少用时规划", "最少换乘规划", "最小花费规划" };
        final JComboBox<String> choice = new JComboBox<>(types);
        p_mright_top.add(new JLabel("选择规划方式:"));
        p_mright_top.add(choice);

        JButton operate = new JButton("规划");
        p_mright_top.add(operate);

        JPanel p_mright = new JPanel(new BorderLayout());
        p_mright.add(p_mright_top, BorderLayout.NORTH);

        JTextArea result = new JTextArea(30, 1);
        result.setFont(new Font("", Font.PLAIN, 15));
        result.setText("规划结果: ");
        JScrollPane src_result = new JScrollPane(result);
        p_mright.add(src_result, BorderLayout.CENTER);
        p_main.add(p_side);
        p_main.add(p_mright);

        operate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int type = choice.getSelectedIndex() + 1;
                int start = core.h.get(sele_start.getSelectedValue());
                int end = core.h.get(sele_terminal.getSelectedValue());
                // 限定了初始有选项，所以不可能出现用户没选中的情况
                core.dijkstra(start, type);
                result.setText(core.get_path(start, end, type));
            }
        });

        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ui("综合实验3 地铁路线规划");
    }
}
