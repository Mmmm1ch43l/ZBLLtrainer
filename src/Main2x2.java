// Import the necessary packages

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main2x2 extends JFrame implements KeyListener {
    private static boolean runningWindows = false;

    private final boolean includeCLL = true;
    private final double pseudoRandomNumberGeneratorDecay = 0.95;

    private final PseudoRandomNumberGenerator prng;

    private final JLabel questionText;
    private final JLabel answerText;
    private final String[] AUFs = {" "," U "," U' "," U2 "};
    private final String[] inserts = {
            "R U R'","R U' R'","R U2 R'",
            "L' U L","L' U' L","L' U2 L",
            "R' U R","R' U' R","R' U2 R",
            "L U L'","L U' L'","L U2 L'"
    };
    private final String[] rotatedSalt1 = {
            "","R' U ","R' U2 ","R' U' ",
            "R ","R U ","R U2 ","R U' ",
            "R' ","R' U ","R' U2 ","R' U' ",
            "F ","F U ","F U2 ","F U' ",
            "F' ","F' U ","F' U2 ","F' U' ",
            "R2 ","R2 U ","F2 ","R2 U' "
    };
    private final String[] rotatedSalt2 = {
            "R' U' F","F","F","F",
            "U' F","F","F","F",
            "U' F","F","F","F",
            "U' R","R","R","R",
            "U' R","R","R","R",
            "U' F","F","U' R","F"
    };
    private final String[] rotationSalt = {
            "R' U' F","F' D F","L' D2 F","B' D' F",
            "L U' F","B D F","R D2 F","F D' F",
            "L' U' F","B' D F","R' D2 F","F' D' F",
            "B U' R","R D R","F D2 R","L D' R",
            "B' U' R","R' D R","F' D2 R","L' D' R",
            "L2 U' F","B2 D F","B2 U' R","F2 D' F"
    };

    private String[] algs = new String[0];
    private final String[] CLLs = {
            //CPLL
            "z R U R' U R U2 R2 F R F' R U R",
            "",
    };
    private int ALGS;
    // Constructor to initialize the components, layout and arrays
    public Main2x2() {

        // Set the title of the window
        super("ZBLL trainer");


        if(System.getProperty("os.name").startsWith("Windows")) runningWindows = true;
        if(includeCLL){
            String[] temp = new String[algs.length+CLLs.length];
            System.arraycopy(algs, 0, temp, 0, algs.length);
            System.arraycopy(CLLs, 0, temp, algs.length, CLLs.length);
            algs = temp;
        }

        // Create the components
        // Declare the components
        JLabel questionLabel = new JLabel("Solution:");
        JLabel answerLabel = new JLabel("Scramble:");
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
        setSize(630, 140);
        setLocation(10, 10);

        // Set the default close operation of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the window visible
        setVisible(true);

        prng = new PseudoRandomNumberGenerator(algs.length,pseudoRandomNumberGeneratorDecay);

        // Display the first question and answer in the text labels
        String temp = "";
        while(temp.isEmpty()){
            ALGS = prng.generate();
            temp = algs[ALGS];
        }
        questionText.setText("");
        answerText.setText(generateScramble(ALGS));

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
        questionText.setText(algs[ALGS]);
        String temp = "";
        while(temp.isEmpty()){
            ALGS = prng.generate();
            temp = algs[ALGS];
        }
        answerText.setText(generateScramble(ALGS));

    }

    // Main method to create an instance of the window with some sample questions and answers
    public static void main(String[] args) {
        // Create an instance of the window with these arrays as parameters
        new Main2x2();

    }

    private String generateScramble (int ZBLL){
        int rotation = ThreadLocalRandom.current().nextInt(rotationSalt.length);
        return rotatedSalt1[rotation]+cancel(rotatedSalt2[rotation],cancel(nissy("twophase","R' U' F "+inserts[ThreadLocalRandom.current().nextInt(inserts.length)]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+ algs[ZBLL]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+" "+rotationSalt[rotation]),"R' U' F"));
    }

    private String cancel(String left, String right){
        if(left.isEmpty()||right.isEmpty()) return left + right;
        if(right.length()<3) right = right + " ";
        if(left.charAt(left.length()-1)=='\''){
            if(left.charAt(left.length()-2)==right.charAt(0)){
                if(right.charAt(1)=='\''){
                    return left.substring(0,left.length()-1)+"2"+right.substring(2);
                } else if(right.charAt(1)=='2'){
                    return left.substring(0,left.length()-1)+right.substring(2);
                } else {
                    if(left.length()>2) return cancel(left.substring(0,left.length()-3),right.substring(2));
                    else return right.substring(2);
                }
            }
        } else if (left.charAt(left.length()-1)=='2'){
            if(left.charAt(left.length()-2)==right.charAt(0)){
                if(right.charAt(1)=='\''){
                    return left.substring(0,left.length()-1)+right.substring(2);
                } else if(right.charAt(1)=='2'){
                    if(left.length()>2) return cancel(left.substring(0,left.length()-3),right.substring(3));
                    else return right.substring(3);
                } else {
                    return left.substring(0,left.length()-1)+"'"+right.substring(1);
                }
            }
        } else {
            if(left.charAt(left.length()-1)==right.charAt(0)){
                if(right.charAt(1)=='\''){
                    if(left.length()>1) return cancel(left.substring(0,left.length()-2),right.substring(3));
                    else return right.substring(3);
                } else if(right.charAt(1)=='2'){
                    return left+"'"+right.substring(2);
                } else {
                    return left+"2"+right.substring(1);
                }
            }
        }
        return left + " " + right;
    }


    private static String nissy(String... input){
        String[] commands;
        String directory;
        if(runningWindows){
            commands = new String[]{"cmd", "/c", "start", "/b", "/wait", "nissy-2.0.5.exe"};
            directory = "C:\\Users\\lolra\\Downloads";
        } else {
            commands = new String[]{"nissy"};
            directory = "/Users/michaelvogel/Documents/FMC/nissy-2.0.5";
        }
        String[] inputs = Arrays.copyOf (commands,commands.length+input.length);
        System.arraycopy (input, 0, inputs, commands.length, input.length);
        ProcessBuilder pb = new ProcessBuilder(inputs);
        pb.directory(new File(directory));
        Process p;
        try {
            p = pb.start();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        InputStream is = p.getInputStream();
        boolean finished;
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
                    if ((line = br.readLine()) == null) break;
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
            return sb.toString();
        } else {
            return "took too long";
        }
    }
}