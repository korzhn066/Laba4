import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.Vector;
import javax.swing.JPanel;

@SuppressWarnings("serial")

public class GraphicsDisplay extends JPanel {
    public Vector<Double[][]> data = new Vector<Double[][]>();
    private Vector<Double[][]> realDataCoordinates = new Vector<Double[][]>();

    private float zoomX = 1;
    private float zoomY = 1;
    private float translateX = 0;
    private float translateY = 0;

    private boolean showAxis = true;
    private boolean isStartZoom = false;
    private boolean showMarkers = true;
    private boolean rotate = false;
    private int rotation = 0;

    private boolean isLabel = false;
    private String label;
    private float labelX;
    private float labelY;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;

    private BasicStroke rectStroke;
    private boolean isStartDrag = false;
    private float rectXStart;
    private float rectYStart;
    private float rectWidth;
    private float rectHeight;
    // Различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{10, 5, 10, 5, 10, 5, 3, 2, 3, 2, 3, 2}, 10.0f);

        rectStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{10, 10}, 0.0f);

        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);

        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
    }

    public void showGraphics(Double[][] graphicsData) {
        this.data.add(graphicsData);
        this.realDataCoordinates.add(graphicsData);
        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    public void Rotate()
    {
        this.rotate = true;
        repaint();
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        if (data == null || data.size() == 0) return;

        minX = data.get(0)[0][0];
        maxX = data.get(0)[data.get(0).length - 1][0];
        minY = data.get(0)[0][1];
        maxY = minY;

        for (int i = 1; i < data.size(); i++) {
            if (minX > data.get(i)[0][0])
                minX = data.get(i)[0][0];

            if (maxX < data.get(i)[data.get(0).length - 1][0])
                minX = data.get(i)[data.get(0).length - 1][0];
        }

        for (int j = 0; j < data.size(); j++) {
            for (int i = 1; i < data.get(j).length; i++) {
                if (data.get(j)[i][1] < minY) {
                    minY = data.get(j)[i][1];
                }
                if (data.get(j)[i][1] > maxY) {
                    maxY = data.get(j)[i][1];
                }
            }
        }

        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        scale = Math.min(scaleX, scaleY);
        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY -
                    minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX -
                    minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();


        if (isStartZoom) zoomToRegion(canvas);
        if (rotate) {
            rotation += 90;
            canvas.rotate(Math.toRadians(rotation), (float)getWidth()/2, (float)getHeight()/2);
            rotate = false;
        }

        if (showAxis)
            paintAxis(canvas);

        paintGraphics(canvas);

        if (showMarkers) paintMarkers(canvas);
        if (isLabel) paintLabel(canvas);

        paintRect(canvas);


        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintRect(Graphics2D canvas){
        canvas.setColor(Color.BLACK);
        canvas.setStroke(rectStroke);
        canvas.draw(new Rectangle2D.Float(rectXStart, rectYStart, rectWidth, rectHeight));
    }

    protected void paintGraphics(Graphics2D canvas) {
        for (int j = 0; j < data.size(); j++) {
            PrintGraphic(canvas, j);
        }
    }

    public void zoomToRegion(Graphics2D canvas) {
        if (zoomX < 2){
            zoomX += 1 - rectWidth / getWidth();
            zoomY += 1 - rectHeight / getHeight();
            translateX += 1 - rectWidth / getWidth();
            translateY += 1 - rectHeight / getHeight();
        }

        canvas.scale(zoomX, zoomY);
        //canvas.translate(-getWidth() * translateX, 0);
        isStartZoom = false;


    }

    protected void PrintGraphic(Graphics2D canvas, int j){
        var colors = new Color[]{Color.RED, Color.GREEN, Color.GRAY, Color.BLUE};
        var strokes = new BasicStroke[]{new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{10, 5, 10, 5, 10, 5,
                        3, 2, 3, 2, 3, 2}, 10.0f),
                new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f, null, 0.0f),
                new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f, null, 0.0f),
                new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f, null, 0.0f),
        };

        var baseStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{10, 5, 10, 5, 10, 5,
                        3, 2, 3, 2, 3, 2}, 10.0f);

        if (j < 4) {
            canvas.setStroke(strokes[j]);
            canvas.setColor(colors[j]);
        }
        else {
            canvas.setStroke(baseStroke);
            canvas.setColor(Color.DARK_GRAY);
        }

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < data.get(j).length; i++) {
            Point2D.Double point = xyToPoint(data.get(j)[i][0],
                    data.get(j)[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }

        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.RED);
        canvas.setPaint(Color.RED);

        for (int j = 0; j < data.size(); j++) {
            int i = 0;
            var dataTemp = new Double[data.get(j).length][2];

            for (Double[] point : data.get(j)) {
                Ellipse2D.Double marker = new Ellipse2D.Double();

                int sumOfNumbers = 0;
                int temp = (int) (double) point[1];
                while (temp > 0) {
                    sumOfNumbers += temp % 10;
                    temp /= 10;
                }

                if (sumOfNumbers < 10) {
                    canvas.setColor(Color.GREEN);
                }

                Point2D.Double center = xyToPoint(point[0], point[1]);
                Point2D.Double corner = shiftPoint(center, 3, 3);

                marker.setFrameFromCenter(center, corner);

                dataTemp[i][0] = marker.x;
                dataTemp[i][1] = marker.y;
                i++;
                canvas.draw(marker);
                canvas.fill(marker);
            }

            realDataCoordinates.set(j, dataTemp);
        }
    }

    protected void paintLabel(Graphics2D canvas){
        canvas.setColor(Color.BLUE);
        canvas.drawString(label, labelX, labelY);
    }

    protected void paintAxis(Graphics2D canvas) {
// Установить особое начертание для осей
        canvas.setStroke(axisStroke);
// Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
// Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
// Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
// Создать объект контекста отображения текста - для полученияхарактеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
// Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
// Она должна быть видна, если левая граница показываемойобласти (minX) <= 0.0,
// а правая (maxX) >= 0.0
// Сама ось - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY),
                    xyToPoint(0, minY)));
// Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на верхний конецоси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести левый "скат" стрелки в точку с относительнымикоординатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5,
                    arrow.getCurrentPoint().getY() + 20);
// Вести нижнюю часть стрелки в точку с относительнымикоординатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10,
                    arrow.getCurrentPoint().getY());
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси Y
// Определить, сколько места понадобится для надписи "y"
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
// Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
// Она должна быть видна, если верхняя граница показываемойобласти (maxX) >= 0.0,
// а нижняя (minY) <= 0.0
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
// Стрелка оси X
            GeneralPath arrow = new GeneralPath();
// Установить начальную точку ломаной точно на правый конецоси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
// Вести верхний "скат" стрелки в точку с относительнымикоординатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
// Вести левую часть стрелки в точку с относительнымикоординатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
// Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
// Нарисовать подпись к оси X
// Определить, сколько места понадобится для надписи "x"
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
// Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x", (float) (labelPos.getX() -
                    bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected double[] translatePointToXY(int x, int y) {
        return new double[]{minX + (double)x / this.scale, maxY - (double)y / this.scale};
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    private boolean isMoveCoordinate = false;
    private int labelI, labelJ;

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            boolean no = true;
            for (int i = 0; i < realDataCoordinates.size(); i++) {
                for (int j = 0; j < realDataCoordinates.get(i).length; j++) {
                    if (ev.getX() >= realDataCoordinates.get(i)[j][0] - 6 && ev.getX() <= realDataCoordinates.get(i)[j][0] + 6 &&
                            ev.getY() >= realDataCoordinates.get(i)[j][1] - 6 && ev.getY() <= realDataCoordinates.get(i)[j][1] + 6){
                        label = "X= " + data.get(i)[j][0] + " Y= " + data.get(i)[j][1];
                        labelX = ev.getX();
                        labelY = ev.getY();
                        labelI = i;
                        labelJ = j;
                        isLabel = true;
                        isMoveCoordinate = true;
                        repaint();
                        no = false;
                    }
                }
            }

            if (no) {
                label = "";
                labelX = ev.getX();
                labelY = ev.getY();
                isLabel = true;
                isMoveCoordinate = false;
                repaint();
            }

        }

        public void mouseDragged(MouseEvent ev) {
            if (isMoveCoordinate) {
                var a =  translatePointToXY(ev.getX(), ev.getY());
                data.get(labelI)[labelJ][0] = a[0];
                data.get(labelI)[labelJ][1] = a[1];
                repaint();
            }else {
                if (!isStartDrag){
                    rectXStart = ev.getX();
                    rectYStart = ev.getY();
                    isStartDrag = true;
                }

                rectWidth = Math.abs(ev.getX() - rectXStart) ;
                rectHeight= Math.abs(ev.getY() - rectYStart) ;
                repaint();
            }
        }
    }

    public class MouseHandler extends MouseAdapter {
        public MouseHandler() {
        }

        public void mouseClicked(MouseEvent ev) {


        }

        public void mousePressed(MouseEvent ev) {

        }

        public void mouseReleased(MouseEvent ev) {
            isStartDrag = false;
            isMoveCoordinate = false;

            rectXStart = 0;
            rectYStart = 0;
            rectWidth = 0;
            rectHeight = 0;
        }
    }
}


