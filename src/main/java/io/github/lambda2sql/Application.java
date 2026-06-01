package io.github.lambda2sql;

import io.github.lambda2sql.core.Dialect;
import io.github.lambda2sql.core.SqlGenerator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Swing 桌面程序入口。
 *
 * <p>这个类只负责创建窗口、收集用户输入并把生成请求交给 {@link SqlGenerator}。
 * SQL 解析和拼装逻辑都放在 core 包中，避免界面代码和业务逻辑混在一起。</p>
 */
public final class Application {

    private Application() {

    }

    /**
     * 程序启动入口。Swing 组件需要在事件分发线程中创建和更新。
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Application::showWindow);
    }

    /**
     * 创建主窗口，并绑定“生成”按钮的点击事件。
     */
    private static void showWindow() {
        JFrame frame = new JFrame("Lambda2SQL");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setMinimumSize(new java.awt.Dimension(600, 500));
        frame.setLocationRelativeTo(null);

        JTextField tableNameField = new JTextField("user", 24);
        JComboBox<Dialect> dialectBox = new JComboBox<>(Dialect.values());
        JTextArea inputArea = new JTextArea(10, 60);
        JTextArea outputArea = new JTextArea(12, 60);
        JLabel statusLabel = new JLabel("就绪");
        JButton generateButton = new JButton("生成 SQL");

        outputArea.setEditable(false);
        outputArea.setLineWrap(false);

        SqlGenerator generator = new SqlGenerator();
        generateButton.addActionListener(event -> {
            try {
                Dialect dialect = (Dialect) dialectBox.getSelectedItem();
                outputArea.setText(generator.generate(tableNameField.getText(), dialect, inputArea.getText()).sql());
                statusLabel.setText("生成成功");
            } catch (IllegalArgumentException ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        frame.add(buildTopPanel(tableNameField, dialectBox, generateButton), BorderLayout.NORTH);
        frame.add(buildEditorPanel(inputArea, outputArea), BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    /**
     * 构建顶部工具区：表名输入、方言选择和生成按钮。
     */
    private static JPanel buildTopPanel(JTextField tableNameField, JComboBox<Dialect> dialectBox, JButton generateButton) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.WEST;

        constraints.gridx = 0;
        panel.add(new JLabel("表名"), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(tableNameField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("数据库类型"), constraints);
        constraints.gridx = 3;
        panel.add(dialectBox, constraints);
        constraints.gridx = 4;
        panel.add(generateButton, constraints);

        return panel;
    }

    /**
     * 构建中间编辑区：上方输入 Lambda 条件，下方展示生成后的 SQL。
     */
    private static JPanel buildEditorPanel(JTextArea inputArea, JTextArea outputArea) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.weighty = 0.45;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(inputArea), constraints);

        constraints.gridy = 1;
        constraints.weighty = 0.55;
        panel.add(new JScrollPane(outputArea), constraints);
        return panel;
    }
}
