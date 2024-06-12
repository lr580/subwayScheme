package subway;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ui extends JFrame {
	private void initialize() {
        try {
            core.init();// ����ͬĿ¼�´����ļ�core.java���෽��
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "�Ҳ��������ļ�! ");
        }
    }

    public ui(String title) {
        super(title);
        setIconImage(new ImageIcon("icon.png").getImage());// ͼ��
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
        sele_start.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// ��ѡ����
        sele_start.setSelectedIndex(0);
        JScrollPane src_start = new JScrollPane(sele_start);
        JPanel p_mleft = new JPanel(new BorderLayout());
        p_mleft.add(new JLabel("ѡ�����: "), BorderLayout.NORTH);
        p_mleft.add(src_start, BorderLayout.CENTER);
        p_side.add(p_mleft);

        JList<String> sele_terminal = new JList<>(stations);
        sele_terminal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sele_terminal.setSelectedIndex(1);
        JScrollPane src_terminal = new JScrollPane(sele_terminal);
        JPanel p_mmid = new JPanel(new BorderLayout());
        p_mmid.add(new JLabel("ѡ���յ�: "), BorderLayout.NORTH);
        p_mmid.add(src_terminal, BorderLayout.CENTER);
        p_side.add(p_mmid);

        JPanel p_mright_top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        String types[] = { "��̾���滮", "������ʱ�滮", "���ٻ��˹滮", "��С���ѹ滮" };
        final JComboBox<String> choice = new JComboBox<>(types);
        p_mright_top.add(new JLabel("ѡ��滮��ʽ:"));
        p_mright_top.add(choice);

        JButton operate = new JButton("�滮");
        p_mright_top.add(operate);

        JPanel p_mright = new JPanel(new BorderLayout());
        p_mright.add(p_mright_top, BorderLayout.NORTH);

        JTextArea result = new JTextArea(30, 1);
        result.setFont(new Font("", Font.PLAIN, 15));
        result.setText("�滮���: ");
        JScrollPane src_result = new JScrollPane(result);
        p_mright.add(src_result, BorderLayout.CENTER);
        p_main.add(p_side);
        p_main.add(p_mright);

        operate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int type = choice.getSelectedIndex() + 1;
                int start = core.h.get(sele_start.getSelectedValue());
                int end = core.h.get(sele_terminal.getSelectedValue());
                // �޶��˳�ʼ��ѡ����Բ����ܳ����û�ûѡ�е����
                core.dijkstra(start, type);
                result.setText(core.get_path(start, end, type));
            }
        });

        setSize(900, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new ui("�ۺ�ʵ��3 ����·�߹滮");
    }
}
