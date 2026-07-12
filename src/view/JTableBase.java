package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class JTableBase extends JTable {

  private static final long serialVersionUID = 1L;

  public JTableBase() {}

  public JTable createTable(DefaultTableModel modeloTabela) {
    JTable tabela = new JTable(modeloTabela);
    tabela.setRowHeight(30);
    tabela.setFont(new Font("Segoe UI", Font.PLAIN, 16));
    tabela.setForeground(JBase.textos);
    tabela.setBackground(JBase.corInterna);
    tabela.setGridColor(new Color(90, 50, 170));
    tabela.setSelectionBackground(new Color(80, 40, 160));
    tabela.setSelectionForeground(JBase.textos);
    tabela.setShowGrid(true);
    tabela.setShowHorizontalLines(true);
    tabela.setShowVerticalLines(true);
    tabela.setIntercellSpacing(new Dimension(1, 1));
    tabela.setFillsViewportHeight(false);
    tabela.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

    createHeader(tabela);

    return tabela;
  }

  public void createHeader(JTable tabela) {
    JTableHeader header = tabela.getTableHeader();
    header.setFont(new Font("Segoe UI", Font.BOLD, 16));
    header.setBackground(JBase.corCampo);
    header.setForeground(JBase.textos);
    header.setReorderingAllowed(false);
    header.setResizingAllowed(false);
    header.setPreferredSize(new Dimension(header.getWidth(), 34));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
    centro.setHorizontalAlignment(SwingConstants.CENTER);
    centro.setVerticalAlignment(SwingConstants.CENTER);
    centro.setBackground(JBase.corCampo);
    centro.setForeground(JBase.textos);
    centro.setFont(new Font("Segoe UI", Font.PLAIN, 16));

    ((DefaultTableCellRenderer) header.getDefaultRenderer())
        .setHorizontalAlignment(SwingConstants.CENTER);

    for (int i = 0; i < tabela.getColumnCount(); i++) {
      tabela.getColumnModel().getColumn(i).setCellRenderer(centro);
    }
  }
}
