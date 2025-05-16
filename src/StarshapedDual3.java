import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigInteger;
import java.util.*;
import java.util.List;

public class StarshapedDual3 extends JPanel implements KeyListener {

    private final VR[][] tableOfVertices;
    private int currentPolygonIndex;

    private static final int MARGIN = 20;
    //private static final int HEIGHT = 640;
    //private static final int HEIGHT = 1000;
    private static final int HEIGHT = 1150;
    private static final int WIDTH = HEIGHT;
    private static final int SYSTEM_WIDTH = (WIDTH - 3 * MARGIN) / 2;
    private static final int SYSTEM_HEIGHT = (HEIGHT - 3 * MARGIN) / 2;
    private static final boolean RANDOMIZED = false;
    private static final boolean SYMMETRIC = true;
    private static final boolean LONG = false;
    private static final int RANDOMNESS_PRECISION = 1000;


    public StarshapedDual3(VR[][] tableOfVertices) {
        this.tableOfVertices = tableOfVertices;
        this.currentPolygonIndex = 0;
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        if(RANDOMIZED) {
            if(SYMMETRIC) {
                if(LONG) {
                    this.tableOfVertices[0] = generateRandomLongPolygon();
                } else {
                    this.tableOfVertices[0] = generateRandomSymmetricPolygon();
                }
            } else {
                this.tableOfVertices[0] = generateRandomPolygon();
            }
        }
        //integerPointOnLine(new VR(-1, -1)).print();
        //System.out.println((new VR(-1, -1)).isInt());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw Cartesian Coordinate Systems
        drawCoordinateSystem(g2d, MARGIN, MARGIN, -1.5, 1.5);
        drawCoordinateSystem(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, -4, 4);
        drawCoordinateSystem(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, -1.5, 1.5);
        drawCoordinateSystem(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, -4, 4);

        // Draw integer points for both coordinate systems
        drawIntegerPoints(g2d, MARGIN, MARGIN, -1.5, 1.5);
        drawIntegerPoints(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, -4, 4);
        drawIntegerPoints(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, -1.5, 1.5);
        drawIntegerPoints(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, -4, 4);

        // Draw the current Polygon in both coordinate systems
        drawPolygonLeftUp(g2d, tableOfVertices[currentPolygonIndex]);
        drawPolygonRightUp(g2d, dualize(tableOfVertices[currentPolygonIndex]));
        VR[] reduced = reduce(tableOfVertices[currentPolygonIndex]);
        boolean valid = (reduced != null);
        if (valid) {
            drawPolygonLeftDown(g2d, reduced);
            drawPolygonRightDown(g2d, dualize(reduced));
        }

        // Display the current polygon index in the bottom right corner
        String labelText = (currentPolygonIndex + 1) + "/" + tableOfVertices.length;
        g2d.setFont(new Font("Arial", Font.PLAIN, 20)); // Increase font size
        g2d.setColor(Color.BLACK); // Set text color to black
        if (!RANDOMIZED) {
            g2d.drawString(labelText, WIDTH - MARGIN - 50, 50); // Adjust position for larger text
        }
        g2d.drawString("Area: "+computeArea(tableOfVertices[currentPolygonIndex]), 50, 50);
        if (valid) {
            g2d.drawString("Area: "+computeArea(reduced), 50, SYSTEM_HEIGHT + MARGIN + 50);
        } else {
            g2d.setFont(new Font("Arial", Font.PLAIN, 40));
            g2d.setColor(Color.RED);
            g2d.drawString("Dual of convex already contains "+(integerPointsInStarshapedStrict(dualize(tableOfVertices[currentPolygonIndex])).length)+" integer points!", 150, SYSTEM_HEIGHT + MARGIN + 150);
        }
    }

    private void drawCoordinateSystem(Graphics2D g2d, int xOffset, int yOffset, double minRange, double maxRange) {
        // Draw Cartesian Coordinate System
        g2d.setColor(Color.BLACK);
        g2d.drawLine(xOffset, yOffset + SYSTEM_HEIGHT / 2, xOffset + SYSTEM_WIDTH, yOffset + SYSTEM_HEIGHT / 2); // X-axis
        g2d.drawLine(xOffset + SYSTEM_WIDTH / 2, yOffset, xOffset + SYSTEM_WIDTH / 2, yOffset + SYSTEM_HEIGHT); // Y-axis

        // Calculate scaling factors based on range
        double scaleX = SYSTEM_WIDTH / (maxRange - minRange);
        double scaleY = SYSTEM_HEIGHT / (maxRange - minRange);

        // Draw labels and tick marks for the axes
        int distance = 20;
        int xDistance = -5;
        int yDistance = 5;
        g2d.drawString("-1", xOffset + (int)((-1 - minRange) * scaleX) + xDistance, yOffset + SYSTEM_HEIGHT / 2 + distance); // X-axis negative label
        g2d.drawString("1", xOffset + (int)((1 - minRange) * scaleX) + xDistance, yOffset + SYSTEM_HEIGHT / 2 + distance); // X-axis positive label
        g2d.drawString("-1", xOffset + SYSTEM_WIDTH / 2 - distance, yOffset + (int)((maxRange - -1) * scaleY) + yDistance); // Y-axis negative label
        g2d.drawString("1", xOffset + SYSTEM_WIDTH / 2 - distance, yOffset + (int)((maxRange - 1) * scaleY) + yDistance); // Y-axis positive label

        // Draw tick marks
        int tickSize = 5;
        g2d.drawLine(xOffset + SYSTEM_WIDTH / 2 - tickSize, yOffset + (int)((maxRange - 1) * scaleY), xOffset + SYSTEM_WIDTH / 2 + tickSize, yOffset + (int)((maxRange - 1) * scaleY)); // Y-axis positive tick
        g2d.drawLine(xOffset + SYSTEM_WIDTH / 2 - tickSize, yOffset + (int)((maxRange - -1) * scaleY), xOffset + SYSTEM_WIDTH / 2 + tickSize, yOffset + (int)((maxRange - -1) * scaleY)); // Y-axis negative tick
        g2d.drawLine(xOffset + (int)((-1 - minRange) * scaleX), yOffset + SYSTEM_HEIGHT / 2 - tickSize, xOffset + (int)((-1 - minRange) * scaleX), yOffset + SYSTEM_HEIGHT / 2 + tickSize); // X-axis negative tick
        g2d.drawLine(xOffset + (int)((1 - minRange) * scaleX), yOffset + SYSTEM_HEIGHT / 2 - tickSize, xOffset + (int)((1 - minRange) * scaleX), yOffset + SYSTEM_HEIGHT / 2 + tickSize); // X-axis positive tick

        g2d.drawLine(xOffset + SYSTEM_WIDTH / 2 - 2 * tickSize, yOffset + 2 * tickSize, xOffset + SYSTEM_WIDTH / 2, yOffset); // Y-axis left part arrow
        g2d.drawLine(xOffset + SYSTEM_WIDTH / 2, yOffset, xOffset + SYSTEM_WIDTH / 2 + 2 * tickSize, yOffset + 2 * tickSize); // Y-axis right part arrow
        g2d.drawLine(xOffset  + SYSTEM_WIDTH - 2 * tickSize, yOffset + SYSTEM_HEIGHT / 2 - 2 * tickSize, xOffset+ SYSTEM_WIDTH, yOffset + SYSTEM_HEIGHT / 2); // X-axis upper part arrow
        g2d.drawLine(xOffset + SYSTEM_WIDTH, yOffset + SYSTEM_HEIGHT / 2, xOffset + SYSTEM_WIDTH - 2 * tickSize, yOffset + SYSTEM_HEIGHT / 2 + 2 * tickSize); // X-axis lower part arrow
    }

    private void drawIntegerPoints(Graphics2D g2d, int xOffset, int yOffset, double minRange, double maxRange) {
        // Calculate scaling factors based on range
        double scaleX = SYSTEM_WIDTH / (maxRange - minRange);
        double scaleY = SYSTEM_HEIGHT / (maxRange - minRange);

        // Draw integer points
        g2d.setColor(Color.BLUE);
        int pointSize = 4; // Diameter of the points
        for (int x = (int) Math.ceil(minRange); x <= (int) Math.floor(maxRange); x++) {
            for (int y = (int) Math.ceil(minRange); y <= (int) Math.floor(maxRange); y++) {
                int xPos = (int) (xOffset + (x - minRange) * scaleX - pointSize / 2);
                int yPos = (int) (yOffset + SYSTEM_HEIGHT - (y - minRange) * scaleY - pointSize / 2);
                g2d.fillOval(xPos, yPos, pointSize, pointSize);
            }
        }
    }

    private void drawPolygonLeftUp(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, MARGIN, MARGIN, vertices, -1.5, 1.5);
    }

    private void drawPolygonLeftDown(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, vertices, -1.5, 1.5);
    }

    private void drawPolygonRightUp(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, vertices, -4, 4);
    }

    private void drawPolygonRightDown(Graphics2D g2d, VR[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            VR[] verticesSector = new VR[3];
            verticesSector[0] = vertices[i];
            verticesSector[1] = vertices[j];
            verticesSector[2] = new VR(0, 0);
            drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, verticesSector, -4, 4);
        }
    }

    private void drawPolygon(Graphics2D g2d, int xOffset, int yOffset, VR[] vertices, double minRange, double maxRange) {
        // Scale vertices to fit within the specified range
        double scaleX = SYSTEM_WIDTH / (maxRange - minRange);
        double scaleY = SYSTEM_HEIGHT / (maxRange - minRange);

        // Convert vertices from double to int for drawing
        int[] xPoints = new int[vertices.length];
        int[] yPoints = new int[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            xPoints[i] = (int) (xOffset + (vertices[i].doubleValueX() - minRange) * scaleX);
            yPoints[i] = (int) (yOffset + SYSTEM_HEIGHT - (vertices[i].doubleValueY() - minRange) * scaleY);
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
        if (RANDOMIZED) {
            if(SYMMETRIC) {
                if(LONG) {
                    double area = 4;
                    while (area >= 2) {
                        tableOfVertices[0] = generateRandomLongPolygon();
                        area = computeArea(reduce(tableOfVertices[0]));
                        System.out.println("Area: " + area);
                    }
                } else {
                    this.tableOfVertices[0] = generateRandomSymmetricPolygon();
                }
            } else {
                double area = 2;
                while (area >= 1.5) {
                    tableOfVertices[0] = generateRandomPolygon();
                    area = computeArea(reduce(tableOfVertices[0]));
                    System.out.println("Area: " + area);
                }
            }
            System.out.println("Current random polygon:");
            for (VR vertex : tableOfVertices[0]) {
                vertex.print();
            }
            repaint();
        } else {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                currentPolygonIndex = (currentPolygonIndex - 1 + tableOfVertices.length) % tableOfVertices.length;
                repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                currentPolygonIndex = (currentPolygonIndex + 1) % tableOfVertices.length;
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        VR[][] tableOfVertices = {
                {
                        new VR(3,5,4,5),
                        new VR(4,5,-3,5),
                        new VR(-3,5,-4,5),
                        new VR(-4,5,3,5)
                },
                {
                        new VR(1, 1),
                        new VR(0, -1),
                        new VR(-1, 0)
                },
                {
                        new VR(1, 0),
                        new VR(0, -1),
                        new VR(-1, 0),
                        new VR(0, 1)
                },
                {
                        new VR(11,10,0,1),
                        new VR(0,1,-11,10),
                        new VR(-11,10,0,1),
                        new VR(0,1,11,10)
                },
                {
                        new VR(5,2,1,1),
                        new VR(5,2,-1,1),
                        new VR(-1,0)
                },
                {
                        new VR(1, 0),
                        new VR(1,2,-1,1),
                        new VR(-1, 0),
                        new VR(-1,2,1,1)
                },
                {
                        new VR(1,1,1,3),
                        new VR(1,3,-1,1),
                        new VR(-1,1,-1,3),
                        new VR(-1,3,1,1)
                },
                {
                        new VR(4,3,1,1),
                        new VR(0, -1),
                        new VR(-1,1,1,4)
                },
                {
                        new VR(5,4,1,1),
                        new VR(-1,4,-5,4),
                        new VR(-1,1,1,4)
                },
                {
                        new VR(-1,101,0,1),
                        new VR(3435,101,10204,101),
                        new VR(1,101,0,1),
                        new VR(-3435,101,-10204,101)
                },
                {
                        new VR(1,3,1,1),
                        new VR(15,11,-1,1),
                        new VR(-13,11,1,11)
                },
                {
                        new VR(1,1,1,2),
                        new VR(1,2,-1,2),
                        new VR(-1,2,-1,1),
                        new VR(-1,1,-1,2),
                        new VR(-1,2,1,2),
                        new VR(1,2,1,1)
                },
                {
                        new VR(1,1,1,3),
                        new VR(2,3,-1,3),
                        new VR(-1,2,-1,1),
                        new VR(-1,1,-1,3),
                        new VR(-2,3,1,3),
                        new VR(1,2,1,1)
                }
        };

        JFrame frame = new JFrame("Polygon Drawer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH + 10, HEIGHT + 30);
        StarshapedDual3 mainPanel = new StarshapedDual3(tableOfVertices);
        frame.add(mainPanel);
        frame.addKeyListener(mainPanel);
        frame.setVisible(true);
    }

    public VR dualOfLine(VR leftVertex, VR rightVertex) {
        VR normal = rightVertex.subtract(leftVertex).rotateLeft();
        return normal.scale(normal.scalarProduct(leftVertex).invert());
    }

    public VR[] dualize(VR[] vertices) {
        VR[] output = new VR[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            output[i] = dualOfLine(vertices[i], vertices[j]);
        }
        return output;
    }

    public double computeArea(VR[] vertices) {
        BR area = new BR(0);
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            area = area.add(vertices[j].crossProduct(vertices[i]));
        }
        return area.divide(new BR(2)).doubleValue();
    }

    public boolean contains(VR[] polygon, VR point){
        for (int i = 0; i < polygon.length; i++) {
            int j = (i + 1) % polygon.length;
            if (point.subtract(polygon[i]).toTheRightOf(polygon[j].subtract(polygon[i])) < 0) return false;
        }
        return true;
    }

    public boolean containsStrict(VR[] polygon, VR point){
        for (int i = 0; i < polygon.length; i++) {
            int j = (i + 1) % polygon.length;
            if (point.subtract(polygon[i]).toTheRightOf(polygon[j].subtract(polygon[i])) <= 0) return false;
        }
        return true;
    }

    public void integerPointsInStarshapedStrict(ArrayList<VR> list, VR[] polygon){
        VR temp;
        for (int i = 0; i < polygon.length; i++) {
            temp = integerPointOnRayStrict(polygon[i]);
            if (temp != null) list.add(temp);
            integerPointsInTriangle(list, new VR[]{new VR(0,0), polygon[i], polygon[(i+1)%polygon.length]});
        }
    }

    public VR[] integerPointsInStarshapedStrict(VR[] polygon){
        ArrayList<VR> list = new ArrayList<>();
        integerPointsInStarshapedStrict(list, polygon);
        return list.toArray(new VR[0]);
    }

    public void integerPointsInTriangle (ArrayList<VR> list, VR[] polygon){
        VR parent = polygon[2].subtract(polygon[1]).rotateLeft().parentPoint();
        BigInteger index = parent.scalarProduct(polygon[0]).floor().add(BigInteger.ONE);
        VR leftDirection = polygon[1].subtract(polygon[0]);
        leftDirection = leftDirection.scale(parent.scalarProduct(leftDirection).invert());
        VR left = polygon[0].add(leftDirection.scale((new BR(index)).subtract(parent.scalarProduct(polygon[0]))));
        VR rightDirection = polygon[2].subtract(polygon[0]);
        rightDirection = rightDirection.scale(parent.scalarProduct(rightDirection).invert());
        VR right = polygon[0].add(rightDirection.scale((new BR(index)).subtract(parent.scalarProduct(polygon[0]))));
        VR inverse;
        if (parent.getY().signum() == 0) {
            inverse = parent.clone();
        } else {
            BigInteger number = parent.getX().getEnumerator().modInverse(parent.getY().getEnumerator().abs());
            inverse = new VR(new BR(number), new BR(BigInteger.ONE.subtract(parent.getX().getEnumerator().multiply(number)).divide(parent.getY().getEnumerator())));
        }
        VR point = inverse.scale(new BR(index));
        BigInteger outerBound = parent.scalarProduct(polygon[1]).ceiling();
        parent = parent.rotateLeft();
        BigInteger innerBound;
        VR temp;
        for (BigInteger i = index; i.compareTo(outerBound) < 0; i = i.add(BigInteger.ONE)) {
            innerBound = left.subtract(point).divide(parent).ceiling();
            for (BigInteger j = right.subtract(point).divide(parent).floor().add(BigInteger.ONE); j.compareTo(innerBound) < 0; j = j.add(BigInteger.ONE)) {
                temp = point.add(parent.scale(new BR(j)));
                if (temp.isCoprimeInt()) list.add(temp);
            }
            point = point.add(inverse);
            left = left.add(leftDirection);
            right = right.add(rightDirection);
        }
    }

    public VR containedInnerIntegerPoint(VR[] polygon){
        int n = 0;
        for (VR vertex : polygon) {
            n = Math.max(n, vertex.floorAbs());
        }
        for (int i = 1; i < n+1; i++) {
            for (int j = -i+1; j < i; j++) {
                if (contains(polygon, new VR(i,j))){
                    return new VR(i,j);
                }
                if (contains(polygon, new VR(-i,j))){
                    return new VR(-i,j);
                }
                if (contains(polygon, new VR(j,i))){
                    return new VR(j,i);
                }
                if (contains(polygon, new VR(j,-i))){
                    return new VR(j,-i);
                }
            }
            if (contains(polygon, new VR(i,i))){
                return new VR(i,i);
            }
            if (contains(polygon, new VR(-i,i))){
                return new VR(-i,i);
            }
            if (contains(polygon, new VR(i,-i))){
                return new VR(i,-i);
            }
            if (contains(polygon, new VR(-i,-i))){
                return new VR(-i,-i);
            }
        }
        return null;
    }

    public VR integerPointOnRayStrict (VR direction){
        VR parent = direction.parentPoint();
        if (parent.normSquared().compareTo(direction.normSquared()) < 0) {
            return parent;
        } else {
            return null;
        }
    }

    public VR integerPointsOnSegment (VR left, VR right){
        return null;
    }

    public void intermediatePoints(ArrayList<VR> list, VR left, VR right, VR dual) {
        VR temp;
        //System.out.print("looking for Right Vertex");
        VR parent = left.parentPoint();
        BigInteger n = parent.divide(left).floor();
        VR inverse;
        if (parent.getY().signum() == 0) {
            inverse = parent.clone();
        } else {
            BigInteger number = parent.getX().getEnumerator().modInverse(parent.getY().getEnumerator().abs());
            inverse = new VR(new BR(number), new BR(BigInteger.ONE.subtract(parent.getX().getEnumerator().multiply(number)).divide(parent.getY().getEnumerator())));
        }
        VR point = inverse.clone();
        parent = parent.rotateLeft();
        VR rightVertex = point.add(parent.scale(new BR(dual.scale(new BR(BigInteger.ONE,n)).subtract(point).divide(parent).floor())));
        for (BigInteger i = BigInteger.TWO; i.compareTo(n) <= 0; i = i.add(BigInteger.ONE)) {
            point = point.add(inverse);
            temp = point.add(parent.scale(new BR(dual.scale(new BR(i,n)).subtract(point).divide(parent).floor())));
            if (temp.toTheRightOf(rightVertex) < 0) {
                rightVertex = temp.clone();
            }
        }
        rightVertex = rightVertex.scale(left.scalarProduct(dual).divide(left.scalarProduct(rightVertex)));
        //System.out.println(".");
        //rightVertex.print();
        //System.out.print("looking for Left Vertex");
        parent = right.parentPoint();
        n = parent.divide(right).floor();
        if (parent.getY().signum() == 0) {
            inverse = parent.clone();
        } else {
            BigInteger number = parent.getX().getEnumerator().modInverse(parent.getY().getEnumerator().abs());
            inverse = new VR(new BR(number), new BR(BigInteger.ONE.subtract(parent.getX().getEnumerator().multiply(number)).divide(parent.getY().getEnumerator())));
        }
        point = inverse.clone();
        parent = parent.rotateRight();
        VR leftVertex = point.add(parent.scale(new BR(dual.scale(new BR(BigInteger.ONE,n)).subtract(point).divide(parent).floor())));
        for (BigInteger i = BigInteger.TWO; i.compareTo(n) <= 0; i = i.add(BigInteger.ONE)) {
            point = point.add(inverse);
            temp = point.add(parent.scale(new BR(dual.scale(new BR(i,n)).subtract(point).divide(parent).floor())));
            if (temp.toTheRightOf(leftVertex) > 0) {
                leftVertex = temp.clone();
            }
        }
        leftVertex = leftVertex.scale(right.scalarProduct(dual).divide(right.scalarProduct(leftVertex)));
        //System.out.println(".");
        //leftVertex.print();
        //System.out.print("looking for Inner Points");
        ArrayList<VR> integerPoints = new ArrayList<>();
        integerPointsInTriangle(integerPoints, new VR[]{dual,leftVertex,rightVertex});
        //System.out.println(".");
        //System.out.println(integerPoints.length);
        if (integerPoints.size() > 0){
            //System.out.print("dealing with Inner Points");
            List<VR[]> pairs = new ArrayList<>();
            for (VR integerPoint : integerPoints) {
                BR angle = integerPoint.scalarProduct(rightVertex).pow(2).divide(integerPoint.normSquared()).negate();
                angle = angle.multiply(new BR(integerPoint.scalarProduct(rightVertex).signum()));
                pairs.add(new VR[]{integerPoint, new VR(angle, new BR(0))});
            }
            pairs.sort((a, b) -> Double.compare(a[1].doubleValueX(), b[1].doubleValueX()));
            integerPoints = new ArrayList<>();
            VR rightVertexNew = rightVertex.clone();
            for (VR[] pair : pairs) {
                if (containsStrict(new VR[]{leftVertex,rightVertexNew,dual}, pair[0])){
                    rightVertexNew = rightVertex.clone();
                    ArrayList<VR> toAdds = new ArrayList<>();
                    for (VR integerPoint : integerPoints) {
                        if (containsStrict(new VR[]{pair[0],rightVertexNew,dual}, integerPoint)){
                            rightVertexNew = integerPoint.clone();
                            toAdds.add(integerPoint);
                        }
                    }
                    integerPoints = toAdds;
                    integerPoints.add(pair[0]);
                    rightVertexNew = pair[0].clone();
                }
            }
            list.add(dualOfLine(integerPoints.get(0),rightVertex));
            for (int k = 1; k < integerPoints.size(); k++) {
                list.add(dualOfLine(integerPoints.get(k), integerPoints.get(k-1)));
            }
            list.add(dualOfLine(leftVertex, integerPoints.get(integerPoints.size()-1)));
            //System.out.println(".");
        } else {
            list.add(dualOfLine(leftVertex, rightVertex));
        }
    }

    public VR[] reduce(VR[] input){
        if (isSymmetric(input)) {
            return reduceSymmetric(input);
        } else {
            return reduceAsymmetric(input);
        }
    }

    public VR[] reduceAsymmetric(VR[] input){
        VR[] convexHull = new VR[input.length];
        for (int i = 0; i < input.length; i++) {
            VR integerPoint = integerPointOnRayStrict(input[i]);
            if (integerPoint == null){
                convexHull[i] = input[i].clone();
            } else {
                convexHull[i] = integerPoint.clone();
            }
        }
        VR[] convexDual = dualize(convexHull);
        if (integerPointsInStarshapedStrict(convexDual).length > 0){
            return null;
        }
        ArrayList<VR> list = new ArrayList<>();
        for (int i = 0; i < convexHull.length; i++) {
            int j = (i + 1) % convexHull.length;
            list.add(convexHull[i]);
            if (!convexDual[i].isInt()){
                intermediatePoints(list, convexHull[i], convexHull[j], convexDual[i]);
            }
        }
        return list.toArray(new VR[0]);
    }

    public VR[] reduceSymmetric(VR[] input){
        VR[] convexHull = new VR[input.length];
        for (int i = 0; i < input.length/2; i++) {
            VR integerPoint = integerPointOnRayStrict(input[i]);
            if (integerPoint == null){
                convexHull[i] = input[i].clone();
            } else {
                convexHull[i] = integerPoint.clone();
            }
            convexHull[i+input.length/2] = convexHull[i].negate();
        }
        VR[] convexDual = dualize(convexHull);
        if (integerPointsInStarshapedStrict(convexDual).length > 0){
            return null;
        }
        ArrayList<VR> list = new ArrayList<>();
        for (int i = 0; i < convexHull.length/2; i++) {
            int j = (i + 1) % convexHull.length;
            list.add(convexHull[i]);
            if (!convexDual[i].isInt()){
                intermediatePoints(list, convexHull[i], convexHull[j], convexDual[i]);
            }
        }
        VR[] output = new VR[2*list.size()];
        for (int i = 0; i < list.size(); i++) {
            output[i] = list.get(i);
            output[i+list.size()] = output[i].negate();
        }
        return output;
    }
    public int[] randomPermutation(int n) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = numbers.get(i);
        }
        return result;
    }

    public VR[] generateRandomPolygon() {
        Random random = new Random();
        int n = 3;
        VR[] randomPolygon = new VR[n];
        for (int i = 0; i < n; i++) {
            randomPolygon[i] = new VR(random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION, random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION);
            while (randomPolygon[i].normSquared().signum() == 0) {
                randomPolygon[i] = new VR(random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION, random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION);
            }
        }
        while (!containsStrict(randomPolygon,new VR(0,0))) {
            for (int i = 0; i < n; i++) {
                randomPolygon[i] = new VR(random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION, random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION);
            }
        }
        while (integerPointsInStarshapedStrict(dualize(randomPolygon)).length > 0) {
            for (int i = 0; i < n; i++) {
                randomPolygon[i] = randomPolygon[i].scale(new BR(2));
            }
        }
        int[] permutation = randomPermutation(n);
        for (int i = 0; i < n; i++) {
            int rightIndex = permutation[i];
            int leftIndex = (rightIndex + n - 1) % n;
            VR[] dual = dualize(randomPolygon);
            while (containedInnerIntegerPoint(new VR[]{dual[leftIndex], dual[rightIndex], new VR(0,0)}) == null) {
                randomPolygon[rightIndex] = randomPolygon[rightIndex].scale(new BR(1,2));
                dual = dualize(randomPolygon);
            }
            VR[] integerPoints = integerPointsInStarshapedStrict(dual);
            if (integerPoints.length > 0) {
                BR scale = new BR(1);
                for (VR integerPoint : integerPoints) {
                    BR scaleNew = randomPolygon[rightIndex].scalarProduct(dual[rightIndex]).divide(randomPolygon[rightIndex].scalarProduct(integerPoint));
                    if (scaleNew.compareTo(scale) > 0) {
                        scale = scaleNew.clone();
                    }
                }
                randomPolygon[rightIndex] = randomPolygon[rightIndex].scale(scale);
            }
        }
        return randomPolygon;
    }

    public VR[] generateRandomSymmetricPolygon() {
        Random random = new Random();
        int n = 2;
        VR[] randomPolygon = new VR[2*n];
        for (int i = 0; i < n; i++) {
            randomPolygon[i] = new VR(random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION, random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION);
            while (randomPolygon[i].normSquared().signum() == 0) {
                randomPolygon[i] = new VR(random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION, random.nextInt(-3*RANDOMNESS_PRECISION, 3*RANDOMNESS_PRECISION+1), 2*RANDOMNESS_PRECISION);
            }
        }
        if (randomPolygon[1].toTheRightOf(randomPolygon[0]) <= 0) {
            if (randomPolygon[1].toTheRightOf(randomPolygon[0]) == 0) {
                randomPolygon[1] = randomPolygon[1].rotateRight();
                if (randomPolygon[1].toTheRightOf(randomPolygon[0]) <= 0) {
                    randomPolygon[1] = randomPolygon[1].negate();
                }
            } else {
                randomPolygon[1] = randomPolygon[1].negate();
            }
        }
        for (int i = 0; i < n; i++) {
            randomPolygon[n+i] = randomPolygon[i].negate();
        }
        while (integerPointsInStarshapedStrict(dualize(randomPolygon)).length > 0) {
            for (int i = 0; i < 2*n; i++) {
                randomPolygon[i] = randomPolygon[i].scale(new BR(2));
            }
        }
        int[] permutation = randomPermutation(n);
        for (int i = 0; i < n; i++) {
            int rightIndex = permutation[i];
            int leftIndex = (rightIndex + 2*n - 1) % (2*n);
            VR[] dual = dualize(randomPolygon);
            while (containedInnerIntegerPoint(new VR[]{dual[leftIndex], dual[rightIndex], new VR(0,0)}) == null) {
                randomPolygon[rightIndex] = randomPolygon[rightIndex].scale(new BR(1,2));
                dual = dualize(randomPolygon);
            }
            VR[] integerPoints = integerPointsInStarshapedStrict(dual);
            if (integerPoints.length > 1) {
                BR scale = new BR(1);
                for (VR integerPoint : integerPoints) {
                    if (integerPoint.normSquared().signum() > 0) {
                        BR scaleNew = randomPolygon[rightIndex].scalarProduct(dual[rightIndex]).divide(randomPolygon[rightIndex].scalarProduct(integerPoint));
                        if (scaleNew.compareTo(scale) > 0) {
                            scale = scaleNew.clone();
                        }
                    }
                }
                randomPolygon[rightIndex] = randomPolygon[rightIndex].scale(scale);
            }
            randomPolygon[n+rightIndex] = randomPolygon[rightIndex].negate();
        }
        return randomPolygon;
    }

    public VR[] generateRandomLongPolygon() {
        Random random = new Random();
        int n = 7;
        BigInteger x = BigInteger.valueOf(random.nextInt(n,n*n));
        BigInteger y = BigInteger.valueOf(random.nextInt(Math.max(n*n,2*x.intValue()+1),3*n*n));
        while (x.gcd(y).compareTo(BigInteger.ONE) > 0) {
            x = BigInteger.valueOf(random.nextInt(n,n*n));
            y = BigInteger.valueOf(random.nextInt(Math.max(n*n,2*x.intValue()+1),2*n*n));
        }
        VR[] randomPolygon = new VR[]{
                new VR(new BR(x,BigInteger.valueOf(n)), new BR(y,BigInteger.valueOf(n))),
                new VR(1,n,0,1),
                new VR(new BR(x.negate(),BigInteger.valueOf(n)), new BR(y.negate(),BigInteger.valueOf(n))),
                new VR(-1,n,0,1),
        };
        while (containedInnerIntegerPoint(dualize(randomPolygon)) != null) {
            x = BigInteger.valueOf(random.nextInt(n,n*n));
            y = BigInteger.valueOf(random.nextInt(Math.max(n*n,2*x.intValue()+1),2*n*n));
            while (x.gcd(y).compareTo(BigInteger.ONE) > 0) {
                x = BigInteger.valueOf(random.nextInt(n,n*n));
                y = BigInteger.valueOf(random.nextInt(Math.max(n*n,2*x.intValue()+1),2*n*n));
            }
            randomPolygon = new VR[]{
                    new VR(new BR(x,BigInteger.valueOf(n)), new BR(y,BigInteger.valueOf(n))),
                    new VR(1,n,0,1),
                    new VR(new BR(x.negate(),BigInteger.valueOf(n)), new BR(y.negate(),BigInteger.valueOf(n))),
                    new VR(-1,n,0,1),
            };
        }
        return randomPolygon;
    }

    public static boolean isSymmetric(VR[] polygon) {
        if (polygon.length % 2 != 0) return false;
        for (int i = 0; i < polygon.length/2; i++) {
            if (!polygon[i].equals(polygon[i+polygon.length/2].negate())) return false;
        }
        return true;
    }
}
