// Import the necessary packages
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ProcessBuilder;
import java.lang.Process;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame implements KeyListener {

    // Declare the components
    private JLabel questionLabel;
    private JLabel answerLabel;
    private JLabel questionText;
    private JLabel answerText;
    private String[] AUFs = {" "," U "," U' "," U2 "};
    private String[] inserts = {"R U R'","R U' R'","R U2 R'","R' F R F'","L' U L","L' U' L","L' U2 L","L F' L' F"};
    private String[] rotations = {
            "","y","y2","y'",
            "x","x y","x y2","x y'",
            "x'","x' y","x' y2","x' y'",
            "z","z y","z y2","z y'",
            "z'","z' y","z' y2","z' y'",
            "z2","z2 y","z2 y2","z2 y'"
    };
    private String[] rotationInverses = {
            "","y'","y2","y",
            "x'","y' x'","y2 x'","y x'",
            "x","y' x","y2 x","y x",
            "z'","y' z'","y2 z'","y z'",
            "z","y' z","y2 z","y z",
            "z2","y' z2","y2 z2","y z2",
    };
    private String[] ZBLLs = {
            "U' L' U2 L U L' U L R U2 R' U' R U' R'",//U 2GLL
            "U2 R U R' U R U2 R' U R U2 R' U' R U' R'",
            "U' R U R' U' R U' R' U2 R U' R' U2 R U R'",
            "U R U2 R' U' R U' R' U' R U R' U R U2 R'",
            "U R' U2 R U R' U R U R' U' R U' R' U2 R",
            "U R' U2 R2 U R2 U R U' R U R' U' R U' R'",
            "U R U2 R2 U' R2 U' R' U R' U' R U R' U R",
            "R' U' R U' R' U2 R2 U R' U R U2 R'",
            "U2 R U R' U R U2 R2 U' R U' R' U2 R",
            "x' R2 D2 R' U' R D2 R2 D R U R' D' x",
            "U2 R U R' U R' U2 R2 U R2 U R2 U' R'",
            "R' U' R U' R U2 R2 U' R2 U' R2 U R"
    };
    private int ZBLL;
    // Constructor to initialize the components, layout and arrays
    public Main() throws IOException, InterruptedException {
        // Set the title of the window
        super("ZBLL trainer");

        // Create the components
        questionLabel = new JLabel("Solution:");
        answerLabel = new JLabel("Scramble:");
        questionText = new JLabel();
        answerText = new JLabel();
        questionText.setHorizontalAlignment(SwingConstants.LEFT);
        answerText.setHorizontalAlignment(SwingConstants.LEFT);


        // Set the font and color of the text labels
        questionText.setFont(new Font("Arial", Font.PLAIN, 16));
        questionText.setForeground(Color.black);
        answerText.setFont(new Font("Arial", Font.PLAIN, 16));
        answerText.setForeground(Color.black);

        // Create a panel to hold the labels
        JPanel panel = new JPanel();

        // Set the layout of the panel to box layout with vertical alignment
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Add some vertical space between the labels
        panel.add(Box.createVerticalStrut(10));

        // Add the components to the panel with some horizontal alignment
        panel.add(questionLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(questionText);
        panel.add(Box.createVerticalStrut(10));
        panel.add(answerLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(answerText);

        // Add some vertical space at the end of the panel
        panel.add(Box.createVerticalStrut(10));

        // Add the panel to the content pane of the window
        getContentPane().add(panel);

        // Set the size and location of the window
        setSize(600, 200);
        setLocation(100, 100);

        // Set the default close operation of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the window visible
        setVisible(true);

        // Display the first question and answer in the text labels
        ZBLL = ThreadLocalRandom.current().nextInt(ZBLLs.length);
        questionText.setText("");
        answerText.setText(generateScramble(ZBLL));

        // Add an action listener to this window to handle keyboard events
        addKeyListener(this);
    }

    // Method to handle keyboard events when any button is pressed
    public void keyPressed(KeyEvent e) {

    }

    // Method to handle keyboard events when any button is released
    public void keyReleased(KeyEvent e) {

    }

    // Method to handle keyboard events when any button is typed
    public void keyTyped(KeyEvent e) {
        questionText.setText(ZBLLs[ZBLL]);
        ZBLL = ThreadLocalRandom.current().nextInt(ZBLLs.length);
        answerText.setText(generateScramble(ZBLL));

    }

    // Main method to create an instance of the window with some sample questions and answers
    public static void main(String[] args) throws IOException, InterruptedException {
        // Create an instance of the window with these arrays as parameters
        new Main();

    }

    public String generateScramble (int ZBLL){
        ProcessBuilder pb = new ProcessBuilder("nissy","twophase","R' U' F "+inserts[ThreadLocalRandom.current().nextInt(6)]+AUFs[ThreadLocalRandom.current().nextInt(4)]+ZBLLs[ZBLL]+AUFs[ThreadLocalRandom.current().nextInt(4)]+" R' U' F");
        pb.directory(new File("/Users/michaelvogel/Downloads/nissy-2.0.5"));
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        InputStream is = p.getInputStream();
        boolean finished = false;
        try {
            finished = p.waitFor(1000, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        if (finished) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder sb = new StringBuilder();
            while (true) {
                try {
                    if (!((line = br.readLine()) != null)) break;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                sb.append(line);
            }
            try {
                is.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                br.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return rotations[ThreadLocalRandom.current().nextInt(24)]+" R' U' F "+ sb +" R' U' F";
        } else {
            return "took too long";
        }
    }
}
