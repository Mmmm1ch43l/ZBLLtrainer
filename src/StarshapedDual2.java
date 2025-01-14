import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class StarshapedDual2 extends JPanel implements KeyListener {

    private final double[][][] tableOfVertices;
    private int currentPolygonIndex;

    private static final int MARGIN = 20;
    private static final int HEIGHT = 640;
    //private static final int HEIGHT = 1100;
    private static final int WIDTH = HEIGHT;
    private static final int SYSTEM_WIDTH = (WIDTH - 3 * MARGIN) / 2;
    private static final int SYSTEM_HEIGHT = (HEIGHT - 3 * MARGIN) / 2;
    private static final double smallAngleTan = Math.tan(1e-3);

    public StarshapedDual2(double[][][] tableOfVertices) {
        this.tableOfVertices = tableOfVertices;
        this.currentPolygonIndex = 0;
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        //System.out.println(contains(tableOfVertices[1], new double[]{0,0}));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw Cartesian Coordinate Systems
        drawCoordinateSystem(g2d, MARGIN, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, -1.5, 1.5);
        drawCoordinateSystem(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, -4, 4);
        drawCoordinateSystem(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, -1.5, 1.5);
        drawCoordinateSystem(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, -4, 4);

        // Draw integer points for both coordinate systems
        drawIntegerPoints(g2d, MARGIN, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, -1.5, 1.5);
        drawIntegerPoints(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, -4, 4);
        drawIntegerPoints(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, -1.5, 1.5);
        drawIntegerPoints(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, -4, 4);

        // Draw the current Polygon in both coordinate systems
        drawPolygonLeftUp(g2d, tableOfVertices[currentPolygonIndex]);
        drawPolygonRightUp(g2d, dualize(tableOfVertices[currentPolygonIndex]));
        double[][] reduced = reduce(tableOfVertices[currentPolygonIndex]);
        drawPolygonLeftDown(g2d, reduced);
        drawPolygonRightDown(g2d, dualize(reduced));

        // Display the current polygon index in the bottom right corner
        String labelText = (currentPolygonIndex + 1) + "/" + tableOfVertices.length;
        g2d.setFont(new Font("Arial", Font.PLAIN, 20)); // Increase font size
        g2d.setColor(Color.BLACK); // Set text color to black
        g2d.drawString(labelText, WIDTH - MARGIN - 50, 50); // Adjust position for larger text
        g2d.drawString("Area: "+computeArea(tableOfVertices[currentPolygonIndex]), 50, 50);
        g2d.drawString("Area: "+computeArea(reduced), 50, SYSTEM_HEIGHT + MARGIN + 50);
    }

    private void drawCoordinateSystem(Graphics2D g2d, int xOffset, int yOffset, int width, int height, double minRange, double maxRange) {
        // Draw Cartesian Coordinate System
        g2d.setColor(Color.BLACK);
        g2d.drawLine(xOffset, yOffset + height / 2, xOffset + width, yOffset + height / 2); // X-axis
        g2d.drawLine(xOffset + width / 2, yOffset, xOffset + width / 2, yOffset + height); // Y-axis

        // Calculate scaling factors based on range
        double scaleX = width / (maxRange - minRange);
        double scaleY = height / (maxRange - minRange);

        // Draw labels and tick marks for the axes
        int distance = 20;
        int xDistance = -5;
        int yDistance = 5;
        g2d.drawString("-1", xOffset + (int)((-1 - minRange) * scaleX) + xDistance, yOffset + height / 2 + distance); // X-axis negative label
        g2d.drawString("1", xOffset + (int)((1 - minRange) * scaleX) + xDistance, yOffset + height / 2 + distance); // X-axis positive label
        g2d.drawString("-1", xOffset + width / 2 - distance, yOffset + (int)((maxRange - -1) * scaleY) + yDistance); // Y-axis negative label
        g2d.drawString("1", xOffset + width / 2 - distance, yOffset + (int)((maxRange - 1) * scaleY) + yDistance); // Y-axis positive label

        // Draw tick marks
        int tickSize = 5;
        g2d.drawLine(xOffset + width / 2 - tickSize, yOffset + (int)((maxRange - 1) * scaleY), xOffset + width / 2 + tickSize, yOffset + (int)((maxRange - 1) * scaleY)); // Y-axis positive tick
        g2d.drawLine(xOffset + width / 2 - tickSize, yOffset + (int)((maxRange - -1) * scaleY), xOffset + width / 2 + tickSize, yOffset + (int)((maxRange - -1) * scaleY)); // Y-axis negative tick
        g2d.drawLine(xOffset + (int)((-1 - minRange) * scaleX), yOffset + height / 2 - tickSize, xOffset + (int)((-1 - minRange) * scaleX), yOffset + height / 2 + tickSize); // X-axis negative tick
        g2d.drawLine(xOffset + (int)((1 - minRange) * scaleX), yOffset + height / 2 - tickSize, xOffset + (int)((1 - minRange) * scaleX), yOffset + height / 2 + tickSize); // X-axis positive tick

        g2d.drawLine(xOffset + width / 2 - 2 * tickSize, yOffset + 2 * tickSize, xOffset + width / 2, yOffset); // Y-axis left part arrow
        g2d.drawLine(xOffset + width / 2, yOffset, xOffset + width / 2 + 2 * tickSize, yOffset + 2 * tickSize); // Y-axis right part arrow
        g2d.drawLine(xOffset  + width - 2 * tickSize, yOffset + height / 2 - 2 * tickSize, xOffset+ width , yOffset + height / 2); // X-axis upper part arrow
        g2d.drawLine(xOffset + width, yOffset + height / 2, xOffset + width - 2 * tickSize, yOffset + height / 2 + 2 * tickSize); // X-axis lower part arrow
    }

    private void drawIntegerPoints(Graphics2D g2d, int xOffset, int yOffset, int width, int height, double minRange, double maxRange) {
        // Calculate scaling factors based on range
        double scaleX = width / (maxRange - minRange);
        double scaleY = height / (maxRange - minRange);

        // Draw integer points
        g2d.setColor(Color.BLUE);
        int pointSize = 4; // Diameter of the points
        for (int x = (int) Math.ceil(minRange); x <= (int) Math.floor(maxRange); x++) {
            for (int y = (int) Math.ceil(minRange); y <= (int) Math.floor(maxRange); y++) {
                int xPos = (int) (xOffset + (x - minRange) * scaleX - pointSize / 2);
                int yPos = (int) (yOffset + height - (y - minRange) * scaleY - pointSize / 2);
                g2d.fillOval(xPos, yPos, pointSize, pointSize);
            }
        }
    }

    private void drawPolygonLeftUp(Graphics2D g2d, double[][] vertices) {
        drawPolygon(g2d, MARGIN, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -1.5, 1.5);
    }

    private void drawPolygonLeftDown(Graphics2D g2d, double[][] vertices) {
        drawPolygon(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -1.5, 1.5);
    }

    private void drawPolygonRightUp(Graphics2D g2d, double[][] vertices) {
        drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -4, 4);
    }

    private void drawPolygonRightDown(Graphics2D g2d, double[][] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            double[][] verticesSector = new double[3][];
            verticesSector[0] = vertices[i];
            verticesSector[1] = vertices[j];
            verticesSector[2] = new double[]{0, 0};
            drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, verticesSector, -4, 4);
        }
    }

    private void drawPolygon(Graphics2D g2d, int xOffset, int yOffset, int width, int height, double[][] vertices, double minRange, double maxRange) {
        // Scale vertices to fit within the specified range
        double scaleX = width / (maxRange - minRange);
        double scaleY = height / (maxRange - minRange);

        // Convert vertices from double to int for drawing
        int[] xPoints = new int[vertices.length];
        int[] yPoints = new int[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            xPoints[i] = (int) (xOffset + (vertices[i][0] - minRange) * scaleX);
            yPoints[i] = (int) (yOffset + height - (vertices[i][1] - minRange) * scaleY);
        }

        // Set the transparency level
        float alpha = 0.3f; // 50% transparent
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(ac);

        // Draw the filled Polygon
        g2d.setColor(new Color(255, 0, 0, (int) (alpha * 255))); // Red color with alpha
        g2d.fillPolygon(xPoints, yPoints, vertices.length);

        // Reset to default composite for other drawings
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.RED);
        g2d.drawPolygon(xPoints, yPoints, vertices.length);

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            currentPolygonIndex = (currentPolygonIndex - 1 + tableOfVertices.length) % tableOfVertices.length;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            currentPolygonIndex = (currentPolygonIndex + 1) % tableOfVertices.length;
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        double[][][] tableOfVertices = {
                {
                        {Math.sqrt(2)*3/5, Math.sqrt(2)*4/5},
                        {Math.sqrt(2)*4/5, -Math.sqrt(2)*3/5},
                        {-Math.sqrt(2)*3/5, -Math.sqrt(2)*4/5},
                        {-Math.sqrt(2)*4/5, Math.sqrt(2)*3/5}
                },
                {
                        {1, 1},
                        {0, -1},
                        {-1, 0}
                },
                {
                        {1, 0},
                        {0, -1},
                        {-1, 0},
                        {0, 1}
                },
                {
                        {1, 0},
                        {0.5, -1},
                        {-1, 0},
                        {-0.5, 1}
                },
                {
                        {1, 1./3},
                        {1./3, -1},
                        {-1, -1./3},
                        {-1./3, 1}
                },
                {
                        {4./3, 1},
                        {0, -1},
                        {-1, 0.25}
                }
        };

        JFrame frame = new JFrame("Polygon Drawer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH + 10, HEIGHT + 30);
        StarshapedDual2 mainPanel = new StarshapedDual2(tableOfVertices);
        frame.add(mainPanel);
        frame.addKeyListener(mainPanel);
        frame.setVisible(true);
    }

    public double[][] dualize(double[][] vertices) {
        double[][] output = new double[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            double[] normal = new double[]{-(vertices[j][1] - vertices[i][1]),vertices[j][0] - vertices[i][0]};
            double scale = 1 / norm(normal);
            normal = new double[]{scale*normal[0],scale*normal[1]};
            scale = normal[0] * vertices[i][0] + normal[1] * vertices[i][1];
            output[i] = new double[]{normal[0] / scale,normal[1] / scale};
        }
        return output;
    }

    public double computeArea(double[][] vertices) {
        double output = 0;
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            output += Math.abs(vertices[i][0]*vertices[j][1]-vertices[i][1]*vertices[j][0]);
        }
        return output/2;
    }

    public double norm(double[] vertex) {
        return Math.sqrt(Math.pow(vertex[0], 2) + Math.pow(vertex[1], 2));
    }

    public boolean contains(double[][] polygon, double[] point){
        for (int i = 0; i < polygon.length; i++) {
            int j = (i + 1) % polygon.length;
            if ((polygon[j][0]-polygon[i][0])*(point[1]-polygon[i][1]) >= (polygon[j][1]-polygon[i][1])*(point[0]-polygon[i][0])) return false;
        }
        return true;
    }

    public double[][] containedIntegerPoints(double[][] polygon){
        ArrayList<double[]> list = new ArrayList<>();
        int n = 0;
        for (double[] doubles : polygon) {
            n = Math.max(n, (int) Math.floor(Math.max(Math.abs(doubles[0]), Math.abs(doubles[1]))));
        }
        for (int i = -n; i < n+1; i++) {
            for (int j = -n; j < n+1; j++) {
                if (contains(polygon, new double[]{i,j})){
                    list.add(new double[]{i,j});
                }
            }
        }
        return list.toArray(new double[0][]);
    }

    public double[] containedInnerIntegerPoint(double[][] polygon){
        int n = 0;
        for (double[] doubles : polygon) {
            n = Math.max(n, (int) Math.floor(Math.max(Math.abs(doubles[0]), Math.abs(doubles[1]))));
        }
        for (int i = 1; i < n+1; i++) {
            for (int j = -i+1; j < i; j++) {
                if (contains(polygon, new double[]{i,j})){
                    return new double[]{i,j};
                }
                if (contains(polygon, new double[]{-i,j})){
                    return new double[]{-i,j};
                }
                if (contains(polygon, new double[]{j,i})){
                    return new double[]{j,i};
                }
                if (contains(polygon, new double[]{j,-i})){
                    return new double[]{j,-i};
                }
            }
            if (contains(polygon, new double[]{i,i})){
                return new double[]{i,i};
            }
            if (contains(polygon, new double[]{-i,i})){
                return new double[]{-i,i};
            }
            if (contains(polygon, new double[]{i,-i})){
                return new double[]{i,-i};
            }
            if (contains(polygon, new double[]{-i,-i})){
                return new double[]{-i,-i};
            }
        }
        return null;
    }

    public double[][] reduce(double[][] convexHull){
        double[][] convexDual = dualize(convexHull);
        if (containedIntegerPoints(convexDual).length > 1){
            System.out.println("Dual already contains " + (containedIntegerPoints(convexDual).length-1) + " integer points");
            return null;
        }
        ArrayList<double[]> list = new ArrayList<>();
        for (int i = 0; i < convexHull.length; i++) {
            int j = (i + 1) % convexHull.length;
            list.add(convexHull[i]);
            double[] temp = new double[]{convexHull[i][1],-convexHull[i][0]};
            temp = new double[]{temp[0] + smallAngleTan*temp[1],-smallAngleTan*temp[0] + temp[1]};
            double scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*temp[0] + convexHull[i][1]*temp[1]);
            temp = new double[]{scalingFactor*temp[0],scalingFactor*temp[1]};
            double[] rightVertex = containedInnerIntegerPoint(new double[][]{convexDual[i],temp,new double[]{0,0}});
            if (rightVertex == null){
                rightVertex = temp.clone();
            } else {
                scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*rightVertex[0] + convexHull[i][1]*rightVertex[1]);
                rightVertex = new double[]{scalingFactor*rightVertex[0],scalingFactor*rightVertex[1]};
                double[][] integerPoints = containedIntegerPoints(new double[][]{convexDual[i],rightVertex,new double[]{0,0}});
                for (double[] integerPoint : integerPoints) {
                    if (rightVertex[0]*integerPoint[1] > rightVertex[1]*integerPoint[0]) {
                        rightVertex = integerPoint.clone();
                    }
                }
                scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*rightVertex[0] + convexHull[i][1]*rightVertex[1]);
                rightVertex = new double[]{scalingFactor*rightVertex[0],scalingFactor*rightVertex[1]};
            }
            temp = new double[]{-convexHull[j][1],convexHull[j][0]};
            temp = new double[]{temp[0] - smallAngleTan*temp[1],smallAngleTan*temp[0] + temp[1]};
            scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*temp[0] + convexHull[i][1]*temp[1]);
            temp = new double[]{scalingFactor*temp[0],scalingFactor*temp[1]};
            double[] leftVertex = containedInnerIntegerPoint(new double[][]{temp,convexDual[i],new double[]{0,0}});
            if (leftVertex == null){
                leftVertex = temp.clone();
            } else {
                scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*leftVertex[0] + convexHull[i][1]*leftVertex[1]);
                leftVertex = new double[]{scalingFactor*leftVertex[0],scalingFactor*leftVertex[1]};
                double[][] integerPoints = containedIntegerPoints(new double[][]{leftVertex,convexDual[i],new double[]{0,0}});
                for (double[] integerPoint : integerPoints) {
                    if (leftVertex[0]*integerPoint[1] < leftVertex[1]*integerPoint[0]) {
                        leftVertex = integerPoint.clone();
                    }
                }
                scalingFactor = (convexHull[i][0]*convexDual[i][0] + convexHull[i][1]*convexDual[i][1]) / (convexHull[i][0]*leftVertex[0] + convexHull[i][1]*leftVertex[1]);
                leftVertex = new double[]{scalingFactor*leftVertex[0],scalingFactor*leftVertex[1]};
            }
            temp = new double[]{leftVertex[1] - rightVertex[1],rightVertex[0] - leftVertex[0]};
            scalingFactor = 1 / norm(temp);
            temp = new double[]{scalingFactor*temp[0],scalingFactor*temp[1]};
            scalingFactor = 1/(temp[0] * leftVertex[0] + temp[1] * leftVertex[1]);
            temp = new double[]{scalingFactor*temp[0],scalingFactor*temp[1]};
            list.add(temp);
            System.out.println("Left Vertex: (" + leftVertex[0] + ", " + leftVertex[1] + ")");
            System.out.println("Right Vertex: (" + rightVertex[0] + ", " + rightVertex[1] + ")");
            System.out.println("Resulting Vertex: (" + temp[0] + ", " + temp[1] + ")");
        }
        return list.toArray(new double[0][]);
    }
}
