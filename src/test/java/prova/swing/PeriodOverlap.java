package prova.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class PeriodOverlap extends JFrame {
  private static final long serialVersionUID = 61973565033178168L;

  public PeriodOverlap() {
    setTitle("Period Overlap Visualization");
    setSize(400, 300);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    add(new PeriodPanel());
    setVisible(true);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(PeriodOverlap::new);
  }

  class PeriodPanel extends JPanel {
    private static final long serialVersionUID = -2259762733473370796L;
    List<Period>              periods;

    public PeriodPanel() {
      periods = new ArrayList<>();
      periods.add(new Period(10, 50, Color.RED));
      periods.add(new Period(30, 80, Color.BLUE));
      periods.add(new Period(60, 100, Color.GREEN));
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      for (Period period : periods) {
        g.setColor(period.color);
        g.fillRect(period.start, 20, period.end - period.start, 30);
      }
    }
  }

  class Period {
    int   start;
    int   end;
    Color color;

    public Period(int start, int end, Color color) {
      this.start = start;
      this.end = end;
      this.color = color;
    }
  }
}
