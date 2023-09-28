import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.Point2D;

public class RectanglePainterPanel extends JPanel {
    private ArrayList<Rectangle> _rectangles = new ArrayList<Rectangle>();
    private ArrayList<Boolean> _drawn = new ArrayList<Boolean>();
    private int _currentPage = 0;
    private Point _mouseDown = null;
    private Point _latestDragPoint = null;
    private boolean _newRectangle = false;
    private boolean _draggingOld = false;
    private int _dragPoint = 0;

    public void setPage(int page) {
        _currentPage = page;
        repaint();
    }

    public Boolean hasRectangle(int page){
        return _rectangles.size() > page;
    }

    private void createRectangle(Point start, Point end) {
        if(!hasRectangle(_currentPage)){                       
            _rectangles.add(_currentPage, getRectangle(start, end));
            _drawn.add(_currentPage, false);
            repaint();
        }
    }

    public void MouseDown(Point current)
    {
        if(!hasRectangle(_currentPage)){
            _mouseDown = current;
            _newRectangle = true;
        }
        else{
            _draggingOld = true;
            Rectangle cur = _rectangles.get(_currentPage);
            double distTopLeft = Point2D.distance(current.getX(), current.getY(), cur.getX(), cur.getY());
            double distBottomRight = Point2D.distance(current.getX(), current.getY(), cur.getMaxX(), cur.getMaxY());
            if(distTopLeft < distBottomRight){
                _dragPoint = 1;
            }
            else _dragPoint = 2;
        }
    }

    public void MouseUp(Point current){
        if(_newRectangle){
            createRectangle(_mouseDown, current);
            _mouseDown = null;
            _newRectangle = false;
        }
        _draggingOld = false;
        _dragPoint = 0;
        repaint();
    }

    public void drag(Point current){
        _latestDragPoint = current;
         update(current);
         repaint();
    }

    private void update(Point current)
    {
        if(_draggingOld){            
            Rectangle cur = _rectangles.get(_currentPage);
            if(_dragPoint==1){                                
                _rectangles.get(_currentPage).setRect(current.getX(), current.getY(), cur.width, cur.height);
            }
            else if(_dragPoint==2){                
                _rectangles.set(_currentPage, getRectangle(cur.getLocation(), current));
            }
        }
    }

    private Rectangle getRectangle(Point topLeft, Point bottomRight)
    {
        int x = Math.min(topLeft.x, bottomRight.x);
        int y = Math.min(topLeft.y, bottomRight.y);
        int width = Math.abs(topLeft.x - bottomRight.x);
        int height = Math.abs(topLeft.y - bottomRight.y); 
        return new Rectangle(x, y , width, height);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if(hasRectangle(_currentPage)){            
            // Draw existing rectangles
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(4));
            g2d.draw(_rectangles.get(_currentPage));
            _drawn.set(_currentPage, true);
        }else if(_newRectangle && _mouseDown != null && _latestDragPoint != null){
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(getRectangle(_mouseDown, _latestDragPoint));
        }
        g.dispose();
    }
}
