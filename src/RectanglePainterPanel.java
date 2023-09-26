import javax.swing.*;
import java.awt.*;

public class RectanglePainterPanel extends JPanel {
    private Rectangle _rectangle = new Rectangle(0,0,1,1);
    private boolean _hasRect = false;

    public void createRectangle(Point start, Point end) {        
        int x = Math.min(start.x, end.x);
        int y = Math.min(start.y, end.y);
        int width = Math.abs(start.x - end.x);
        int height = Math.abs(start.y - end.y);
        _rectangle.setRect(x, y, width, height);
        _hasRect = true;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(_hasRect){
            Graphics2D g2d = (Graphics2D) g;
            // Draw existing rectangles
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            g2d.draw(_rectangle);
        }
        g.dispose();
    }
}
