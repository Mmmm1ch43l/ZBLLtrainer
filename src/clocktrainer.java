import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ThreadLocalRandom;

public class clocktrainer extends JFrame implements KeyListener {
    private final JLabel answerText;
    private int[] currentSeed;
    private boolean solutionShown;
    private final String[] encoding = {"A","F","G","I","L","M","O","N","R","U","S","T"};

    public clocktrainer() {
        super("clock trainer");

        JLabel answerLabel = new JLabel("   Solution:");
        answerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        answerText = new JLabel();
        answerText.setHorizontalAlignment(SwingConstants.LEFT);
        answerText.setFont(new Font("Arial", Font.PLAIN, 16));
        answerText.setForeground(Color.black);

        currentSeed = generateScramble();
        answerText.setText("");
        solutionShown = false;

        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawClock(g, currentSeed);
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalStrut(305));
        panel.add(answerLabel);
        panel.add(answerText);

        getContentPane().add(panel);

        setSize(600, 370);
        setLocation(100, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        addKeyListener(this);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
        if (solutionShown){
            currentSeed = generateScramble();
            answerText.setText(" ");
            solutionShown = false;
        } else {
            answerText.setText(generateSolution(currentSeed));
            solutionShown = true;
        }
        repaint();
    }

    public static void main(String[] args) {
        new clocktrainer();
    }

    /*
    Memorisation:
    (lowercase letters are front, uppercase back)
    m1: d-c
    m2: (l-dl) [x2] + (R-U)
    m3: C-U
    m4: (UR-R) [x2] + (d-l)
    m5: (dl-l) + (ur-r) [x2] + (U-C+D) [position of D relative to 12]
    m6: (L-DL+R) [position of L relative to 12] [x2] + (dr-d) + (c-u)
    Execution looks like:
    UR: (m1 on UR, m2 on UL)
    R:  (DL to L on UL, C to L on UR)
    ul: (m3 on UL, m4 on UR)
    /:  (m5 on UL, m6 on UR)
    DL: (C to U on DL, UL to U on UR)
    L:  (C to R on UL, UR to R on UR)
    dr: (auf)
    */
    private String generateSolution(int[] scramble) {
        String s = "   ";
        //m1
        s += encode(scramble[5]-scramble[2] + scramble[14]-scramble[10]);
        //m2
        s += encode(scramble[1]-scramble[4]);
        s += " ";
        //m3
        s += encode(scramble[13]-scramble[10]);
        //m4
        s += encode(scramble[11]-scramble[14] + scramble[1]-scramble[5]);
        s += " ";
        //m5
        s += encode(scramble[6]-scramble[3] + scramble[2]-scramble[5] + scramble[16]-scramble[13] + scramble[10]);
        //m6
        s += encode(scramble[12]-scramble[15] + scramble[14] + scramble[0]-scramble[1] + scramble[4]-scramble[7]);
        return s;
    }

    private int[] generateScramble() {
        int[] output = new int[18];
        for (int i = 0; i < 18; i++) {
            output[i] = ThreadLocalRandom.current().nextInt(12);
        }
        output[9] = (12 - output[2]) % 12;
        output[11] = (12 - output[0]) % 12;
        output[15] = (12 - output[8]) % 12;
        output[17] = (12 - output[6]) % 12;
        return output;
    }

    private String encode(int value){
        return encoding[(value + 120) % 12];
    }

    private void drawClock(Graphics g, int[] seed){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(300, 0, 300, 300);
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(1, 1, 598, 298);
        g2d.drawLine(300, 0, 300, 299);
        for (int i = 0; i < 9; i++) {
            drawClockFaceLeft(g2d, seed[i], 50 + 100 * (i%3), 50 + 100 * (i/3));
        }
        for (int i = 0; i < 9; i++) {
            drawClockFaceRight(g2d, seed[17 - i], 350 + 100 * (i%3), 50 + 100 * (i/3));
        }
    }

    private void drawClockFaceLeft(Graphics2D g2d, int value, int centerX, int centerY) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX - 2, centerY - 34, centerX - 2, centerY - 40);
        g2d.drawLine(centerX + 2, centerY - 34, centerX + 2, centerY - 40);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(centerX, centerY + 34, centerX, centerY + 40);
        g2d.drawLine(centerX + 34, centerY, centerX + 40, centerY);
        g2d.drawLine(centerX - 34, centerY, centerX - 40, centerY);
        g2d.fillOval(centerX - 30, centerY - 30, 60, 60);
        for (int i = 1; i < 12; i++) {
            if (i == 3 || i == 6 || i == 9) continue;
            double angle = Math.toRadians((i * 30) - 90);
            int x = (int) Math.round(centerX + 35 * Math.cos(angle)) - 3;
            int y = (int) Math.round(centerY + 35 * Math.sin(angle)) - 3;
            g2d.fillOval(x, y, 7, 7);
        }
        double angle = Math.toRadians((value * 30) - 90);
        g2d.setColor(Color.WHITE);
        int x = (int) Math.round(centerX + 26 * Math.cos(angle));
        int y = (int) Math.round(centerY + 26 * Math.sin(angle));
        g2d.drawLine(centerX, centerY, x, y);
    }

    private void drawClockFaceRight(Graphics2D g2d, int value, int centerX, int centerY) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX - 2, centerY + 34, centerX - 2, centerY + 40);
        g2d.drawLine(centerX + 2, centerY + 34, centerX + 2, centerY + 40);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(centerX, centerY - 34, centerX, centerY - 40);
        g2d.drawLine(centerX + 34, centerY, centerX + 40, centerY);
        g2d.drawLine(centerX - 34, centerY, centerX - 40, centerY);
        g2d.fillOval(centerX - 30, centerY - 30, 60, 60);
        for (int i = 1; i < 12; i++) {
            if (i == 3 || i == 6 || i == 9) continue;
            double angle = Math.toRadians((i * 30) - 90);
            int x = (int) Math.round(centerX + 35 * Math.cos(angle)) - 3;
            int y = (int) Math.round(centerY + 35 * Math.sin(angle)) - 3;
            g2d.fillOval(x, y, 6, 6);
        }
        double angle = Math.toRadians((value * 30) + 90);
        g2d.setStroke(new BasicStroke(5));
        g2d.setColor(Color.BLACK);
        int x = (int) Math.round(centerX + 26 * Math.cos(angle));
        int y = (int) Math.round(centerY + 26 * Math.sin(angle));
        g2d.drawLine(centerX, centerY, x, y);
    }
}