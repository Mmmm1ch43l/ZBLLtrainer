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
    private static final int HEIGHT = 640;
    //private static final int HEIGHT = 1150;
    //private static final int HEIGHT = 1000;
    private static final int WIDTH = HEIGHT;
    private static final int SYSTEM_WIDTH = (WIDTH - 3 * MARGIN) / 2;
    private static final int SYSTEM_HEIGHT = (HEIGHT - 3 * MARGIN) / 2;
    private static final boolean RANDOMIZED = true;
    private static final boolean SYMMETRIC = true;
    private static final int RANDOMNESS_PRECISION = 1000;
    private static final int MAX_PRECISION = 5;


    public StarshapedDual3(VR[][] tableOfVertices) {
        this.tableOfVertices = tableOfVertices;
        this.currentPolygonIndex = 0;
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        if(RANDOMIZED) {
            if(SYMMETRIC) {
                this.tableOfVertices[0] = generateRandomSymmetricPolygon();
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
            g2d.drawString("Dual of convex already contains "+(containedIntegerPointsStrict(dualize(tableOfVertices[currentPolygonIndex])).length - 1)+" integer points!", 150, SYSTEM_HEIGHT + MARGIN + 150);
        }
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

    private void drawPolygonLeftUp(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, MARGIN, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -1.5, 1.5);
    }

    private void drawPolygonLeftDown(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, MARGIN, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -1.5, 1.5);
    }

    private void drawPolygonRightUp(Graphics2D g2d, VR[] vertices) {
        drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, MARGIN, SYSTEM_WIDTH, SYSTEM_HEIGHT, vertices, -4, 4);
    }

    private void drawPolygonRightDown(Graphics2D g2d, VR[] vertices) {
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            VR[] verticesSector = new VR[3];
            verticesSector[0] = vertices[i];
            verticesSector[1] = vertices[j];
            verticesSector[2] = new VR(0, 0);
            drawPolygon(g2d, 2 * MARGIN + SYSTEM_WIDTH, 2 * MARGIN + SYSTEM_HEIGHT, SYSTEM_WIDTH, SYSTEM_HEIGHT, verticesSector, -4, 4);
        }
    }

    private void drawPolygon(Graphics2D g2d, int xOffset, int yOffset, int width, int height, VR[] vertices, double minRange, double maxRange) {
        // Scale vertices to fit within the specified range
        double scaleX = width / (maxRange - minRange);
        double scaleY = height / (maxRange - minRange);

        // Convert vertices from double to int for drawing
        int[] xPoints = new int[vertices.length];
        int[] yPoints = new int[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            xPoints[i] = (int) (xOffset + (vertices[i].doubleValueX() - minRange) * scaleX);
            yPoints[i] = (int) (yOffset + height - (vertices[i].doubleValueY() - minRange) * scaleY);
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
                this.tableOfVertices[0] = generateRandomSymmetricPolygon();
            } else {
                this.tableOfVertices[0] = generateRandomPolygon();
            }
            /*double area = 2;
            while (area >= 1.13 || area < 0) {
                tableOfVertices[0] = generateRandomPolygon();
                area = computeArea(reduce(tableOfVertices[0]));
            }*/
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
                        new VR(1,3,1,1),
                        new VR(15,11,-1,1),
                        new VR(-13,11,1,11)
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
        double output = 0;
        for (int i = 0; i < vertices.length; i++) {
            int j = (i + 1) % vertices.length;
            output += vertices[j].crossProduct(vertices[i]).doubleValue();
        }
        return output/2;
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

    public VR[] containedIntegerPoints(VR[] polygon){
        ArrayList<VR> list = new ArrayList<>();
        int n = 0;
        for (VR vertex : polygon) {
            n = Math.max(n, vertex.floorAbs());
        }
        for (int i = -n; i < n+1; i++) {
            for (int j = -n; j < n+1; j++) {
                if (contains(polygon, new VR(i,j))){
                    list.add(new VR(i,j));
                }
            }
        }
        return list.toArray(new VR[0]);
    }

    public VR[] containedIntegerPointsStrict(VR[] polygon){
        ArrayList<VR> list = new ArrayList<>();
        int n = 0;
        for (VR vertex : polygon) {
            n = Math.max(n, vertex.floorAbs());
        }
        for (int i = -n; i < n+1; i++) {
            for (int j = -n; j < n+1; j++) {
                if (containsStrict(polygon, new VR(i,j))){
                    list.add(new VR(i,j));
                }
            }
        }
        return list.toArray(new VR[0]);
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

    public VR integerPointOnLine(VR direction){
        VR output = containedInnerIntegerPoint(new VR[]{direction,new VR(0,0)});
        if (output == null) {
            return null;
        } else {
            int sign = output.scalarProduct(direction).signum();
            return output.scale(new BR(sign));
        }
    }

    public VR[] reduce(VR[] input){
        VR[] convexHull = new VR[input.length];
        for (int i = 0; i < input.length; i++) {
            VR integerPoint = integerPointOnLine(input[i]);
            if (integerPoint == null){
                convexHull[i] = input[i].clone();
            } else {
                convexHull[i] = integerPoint.clone();
            }
        }
        VR[] convexDual = dualize(convexHull);
        if (containedIntegerPointsStrict(convexDual).length > 1){
            return null;
        }
        ArrayList<VR> list = new ArrayList<>();
        for (int i = 0; i < convexHull.length; i++) {
            int j = (i + 1) % convexHull.length;
            list.add(convexHull[i]);
            //convexDual[i].print();
            //integerPointOnLine(convexDual[i]).print();
            if (!convexDual[i].isInt()){
                int precision = 2;
                VR temp = convexHull[i].rotateRight().rotateSlightlyLeft(precision);
                temp = temp.scale(convexHull[i].scalarProduct(convexDual[i]).divide(convexHull[i].scalarProduct(temp)));
                VR rightVertex = containedInnerIntegerPoint(new VR[]{convexDual[i],temp,new VR(0,0)});
                while (precision++ < MAX_PRECISION && rightVertex == null) {
                    temp = convexHull[i].rotateRight().rotateSlightlyLeft(precision);
                    temp = temp.scale(convexHull[i].scalarProduct(convexDual[i]).divide(convexHull[i].scalarProduct(temp)));
                    rightVertex = containedInnerIntegerPoint(new VR[]{convexDual[i],temp,new VR(0,0)});
                }
                if (rightVertex == null){
                    System.out.println("Right Vertex not found!");
                    rightVertex = temp.clone();
                } else {
                    rightVertex = rightVertex.scale(convexHull[i].scalarProduct(convexDual[i]).divide(convexHull[i].scalarProduct(rightVertex)));
                    VR[] integerPoints = containedIntegerPoints(new VR[]{convexDual[i],rightVertex,new VR(0,0)});
                    for (VR integerPoint : integerPoints) {
                        if (integerPoint.toTheRightOf(rightVertex) < 0) {
                            rightVertex = integerPoint.clone();
                        }
                    }
                    rightVertex = rightVertex.scale(convexHull[i].scalarProduct(convexDual[i]).divide(convexHull[i].scalarProduct(rightVertex)));
                }
                precision = 2;
                temp = convexHull[j].rotateLeft().rotateSlightlyRight(precision);
                temp = temp.scale(convexHull[j].scalarProduct(convexDual[i]).divide(convexHull[j].scalarProduct(temp)));
                VR leftVertex = containedInnerIntegerPoint(new VR[]{temp,convexDual[i],new VR(0,0)});
                while (precision++ < MAX_PRECISION && leftVertex == null) {
                    temp = convexHull[j].rotateLeft().rotateSlightlyRight(precision);
                    temp = temp.scale(convexHull[j].scalarProduct(convexDual[i]).divide(convexHull[j].scalarProduct(temp)));
                    leftVertex = containedInnerIntegerPoint(new VR[]{temp,convexDual[i],new VR(0,0)});
                }
                if (leftVertex == null){
                    System.out.println("Left Vertex not found!");
                    leftVertex = temp.clone();
                } else {
                    leftVertex = leftVertex.scale(convexHull[j].scalarProduct(convexDual[i]).divide(convexHull[j].scalarProduct(leftVertex)));
                    VR[] integerPoints = containedIntegerPoints(new VR[]{leftVertex,convexDual[i],new VR(0,0)});
                    for (VR integerPoint : integerPoints) {
                        if (integerPoint.toTheRightOf(leftVertex) > 0) {
                            leftVertex = integerPoint.clone();
                        }
                    }
                    leftVertex = leftVertex.scale(convexHull[j].scalarProduct(convexDual[i]).divide(convexHull[j].scalarProduct(leftVertex)));
                }
                VR[] integerPoints = containedIntegerPointsStrict(new VR[]{leftVertex,rightVertex,convexDual[i]});
                //System.out.println(integerPoints.length);
                if (integerPoints.length > 0){
                    List<VR[]> pairs = new ArrayList<>();
                    for (VR integerPoint : integerPoints) {
                        if(integerPoint.isCoprimeInt()) {
                            BR angle = integerPoint.scalarProduct(rightVertex).pow(2).divide(integerPoint.normSquared()).negate();
                            angle = angle.multiply(new BR(integerPoint.scalarProduct(rightVertex).signum()));
                            pairs.add(new VR[]{integerPoint, new VR(angle, new BR(0))});
                        }
                    }
                    //System.out.println(pairs.size());
                    pairs.sort((a, b) -> Double.compare(a[1].doubleValueX(), b[1].doubleValueX()));
                    List<VR> toAdds = new ArrayList<>();
                    VR rightVertexNew = rightVertex.clone();
                    for (VR[] pair : pairs) {
                        if (containsStrict(new VR[]{leftVertex,rightVertexNew,convexDual[i]}, pair[0])){
                            rightVertexNew = rightVertex.clone();
                            List<VR> tempAdds = new ArrayList<>();
                            for (VR toAdd : toAdds) {
                                if (containsStrict(new VR[]{pair[0],rightVertexNew,convexDual[i]}, toAdd)){
                                    rightVertexNew = toAdd.clone();
                                    tempAdds.add(toAdd);
                                }
                            }
                            toAdds = tempAdds;
                            toAdds.add(pair[0]);
                            rightVertexNew = pair[0].clone();
                        }
                    }
                    VR[] toAddss = toAdds.toArray(new VR[0]);
                    list.add(dualOfLine(toAddss[0],rightVertex));
                    for (int k = 1; k < toAddss.length; k++) {
                        list.add(dualOfLine(toAddss[k], toAddss[k-1]));
                    }
                    list.add(dualOfLine(leftVertex, toAddss[toAddss.length-1]));
                } else {
                    list.add(dualOfLine(leftVertex, rightVertex));
                }
            }
        }
        return list.toArray(new VR[0]);
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
        while (containedIntegerPointsStrict(dualize(randomPolygon)).length > 1) {
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
            VR[] integerPoints = containedIntegerPointsStrict(dual);
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
        }
        System.out.println("Current random polygon:");
        for (VR vertex : randomPolygon) {
            vertex.print();
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
        while (containedIntegerPointsStrict(dualize(randomPolygon)).length > 1) {
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
            VR[] integerPoints = containedIntegerPointsStrict(dual);
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
        System.out.println("Current random polygon:");
        for (VR vertex : randomPolygon) {
            vertex.print();
        }
        return randomPolygon;
    }
}
