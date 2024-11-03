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
    private final JLabel descriptionText;
    private final JLabel answerText;
    private final String[] AUFs = {" "," U "," U' "," U2 "};
    private final String[] inserts = {
            "R U R'","R U' R'","R U2 R'",
            "L' U L","L' U' L","L' U2 L",
            "R' U R","R' U' R","R' U2 R"
    };
    private final String[] rotatedSalt = {
            "R' U' F","F' D F","L' D2 F","B' D' F",
            "L U' F","B D F","R D2 F","F D' F",
            "L' U' F","B' D F","R' D2 F","F' D' F",
            "B U' R","R D R","F D2 R","L D' R",
            "B' U' R","R' D R","F' D2 R","L' D' R",
            "L2 U' F","B2 D F","B2 U' R","F2 D' F"
    };
    private final String[] rotationSalt = {
            "R' U' F","R' U F","R' U2 F","R' U' F",
            "R U' F","R U F","R U2 F","R U' F",
            "R' U' F","R' U F","R' U2 F","R' U' F",
            "F U' R","F U R","F U2 R","F U' R",
            "F' U' R","F' U R","F' U2 R","F' U' R",
            "R2 U' F","R2 U F","F2 U' R","R2 U' F"
    };

    private String[][] algs = new String[0][];
    private final String[][] CLLs = {
            //CPLL
            {"x R' U R U2 R2 F R F' R U2 x'",       "adj (bar on back, rotate down, bar stays)"},
            {"R U' R' U' F2 U' R U R' U F2",        "opp (LF stays)"},
            //H CLL
            {"R2 U2 R' U2 R2",                      "pure (vertically, right bar goes front)"},
            {"x' U2 R U2 R2 F2 R U2 x",             "diag (vertically, rotate up, bars swap)"},
            {"R U R' U R U R' F R' F' R",           "vertical bar (bar on right, stays)"},
            {"F R2 U' R2 U' R2 U R2 F'",            "horizontal bar (bar on front, goes back)"},
            //U CLL
            {"R2 F2 R U R' F R2 U2 R' U' R",        "pure (U' preAUF, bar goes front)"},
            {"F U R U' R' F'",                      "diag (U preAUF, bar goes left)"},
            {"F R U R' U2 F' R U' R' F",            "bars (U2 preAUF, bars swap)"},
            {"F R' F' R U' R U' R' U2 R U' R'",     "slashes (U' preAUF, FR corner goes FL)"},
            {"R U' R2 F R F' R U R' U' R U R'",     "RF-LB slash (slash goes right)"},
            {"R' U R' F R F' R U2 R' U R",          "LF-RB slash (slash goes right)"},                          //*
            //T CLL
            {"z' F2 R U R' F R2 U2 R' U' R U' z y2","pure (U preAUF, put up on left, bars swap)"},
            {"z' F R F' R U R' U R' F' R U2 z",     "diag (U2 preAUF, put up on left, front bars stay)"},       //
            {"R U R' U' R' F R F'",                 "bar left (U' preAUF, bar goes right)"},
            {"R' F' R U R U' R' F",                 "bar right (U preAUF, bar goes left)"},
            {"R' U R U2 R2 F R F' R",               "front bar (bar goes back)"},
            {"x2 R' F R U' R U R' U R' x' y",       "up bar (put bar DL, DBL corner stays, FU bar goes LF)"},
            //L CLL
            {"R' U R' U2 R U' R' U R U' R2",        "pure (U' preAUF, FL goes FR)"},                            //*
            {"R U2 R2 F R F' R U2 R'",              "diag (U2 preAUF, FR goes RB)"},                            //
            {"F R' F' R U R U' R'",                 "UB matching RF (U2 preAUF, FR stays)"},
            {"F' R U R' U' R' F R",                 "UL matching FR (U' preAUF, FL stays)"},
            {"R U' R' U R U' R' F R' F' R2 U R'",   "matching up opposite RF (U preAUF, FL stays)"},            //
            {"R' F R U' R' F R F' R U R2 F' R",     "matching up opposite FR (FR stays)"},                      //
            //Pi CLL
            {"R U' R2 U R2 U R2 U' R",              "pure (U' preAUF, bar goes left)"},                         //
            {"R' U' R' F R F' R U' R' U2 R",        "diag (U2 preAUF, bar goes back)"},
            {"R U2 R' U' R U R' U2 R' F R F'",      "RF-LB slash (U' preAUF, slash goes back)"},
            {"F R' F' R U2 R U' R' U R U2 R'",      "LF-RB slash (slash goes left)"},
            {"R' F R F' R U' R' U' R U' R'",        "bars (U preAUF, bars stay)"},
            {"F R2 U' R2 U R2 U R2 F'",             "slashes (bar goes back)"},
            //S CLL
            {"R U R' U R U2 R'",                    "pure (LF goes RB)"},
            {"R U R' U R' F R F' R U2 R'",          "diag (U2 preAUF, RB goes LB)"},                            //
            {"R U' R' F R' F' R",                   "Niklas (LF goes RF)"},
            {"F R' F' R U2 R U2 R'",                "anti Niklas (LF goes RF)"},
            {"R' F2 R U2 R U' R' F",                "slashes (LF goes RF)"},
            {"x' U2 R U' R2 F' R2 U R' U2 x",       "bars (U preAUF, rotate up, FLU goes FRU)"},                //*
            //AS CLL
            {"R' F' R U' R' F2 R",                  "pure (RF goes LB)"},
            {"R' U R U' R2 F R F' R U R' U' R",     "diag (RF goes RB)"},                                       //*
            {"R' F R F' R U R'",                    "Niklas (RF goes LF)"},
            {"F' R U R' U2 R' F2 R",                "anti Niklas (RF goes LF)"},
            {"R U2 R' U2 R' F R F'",                "slashes (RF goes LF)"},
            {"x' R U R2 F' R U R U' R2 F R x",      "bars (U preAUF, rotate up, FLD goes FRD)"},                //*
    };
    private int alg;
    // Constructor to initialize the components, layout and arrays
    public Main2x2() {

        // Set the title of the window
        super("ZBLL trainer");


        if(System.getProperty("os.name").startsWith("Windows")) runningWindows = true;
        if(includeCLL){
            String[][] temp = new String[algs.length+CLLs.length][];
            System.arraycopy(algs, 0, temp, 0, algs.length);
            System.arraycopy(CLLs, 0, temp, algs.length, CLLs.length);
            algs = temp;
        }

        // Create the components
        // Declare the components
        JLabel questionLabel = new JLabel("Solution:");
        JLabel answerLabel = new JLabel("Scramble:");
        questionText = new JLabel();
        descriptionText = new JLabel();
        answerText = new JLabel();
        questionText.setHorizontalAlignment(SwingConstants.LEFT);
        descriptionText.setHorizontalAlignment(SwingConstants.LEFT);
        answerText.setHorizontalAlignment(SwingConstants.LEFT);


        // Set the font and color of the text labels
        questionText.setFont(new Font("Arial", Font.PLAIN, 16));
        questionText.setForeground(Color.black);
        descriptionText.setFont(new Font("Arial", Font.PLAIN, 16));
        descriptionText.setForeground(Color.black);
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
        panel.add(Box.createHorizontalStrut(10));
        panel.add(descriptionText);
        panel.add(Box.createVerticalStrut(10));
        panel.add(answerLabel);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(answerText);

        // Add some vertical space at the end of the panel
        panel.add(Box.createVerticalStrut(10));

        // Add the panel to the content pane of the window
        getContentPane().add(panel);

        // Set the size and location of the window
        setSize(350, 155);
        setLocation(10, 10);

        // Set the default close operation of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the window visible
        setVisible(true);

        prng = new PseudoRandomNumberGenerator(algs.length,pseudoRandomNumberGeneratorDecay);

        // Display the first question and answer in the text labels
        String temp = "";
        while(temp.isEmpty()){
            alg = prng.generate();
            temp = algs[alg][0];
        }
        questionText.setText("");
        descriptionText.setText("");
        answerText.setText(generateScramble(alg));

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
        questionText.setText(algs[alg][0]);
        descriptionText.setText(algs[alg][1]);
        String temp = "";
        while(temp.isEmpty()){
            alg = prng.generate();
            temp = algs[alg][0];
        }
        answerText.setText(generateScramble(alg));

    }

    // Main method to create an instance of the window with some sample questions and answers
    public static void main(String[] args) {
        // Create an instance of the window with these arrays as parameters
        new Main2x2();

    }

    private String generateScramble (int alg){
        int rotation = ThreadLocalRandom.current().nextInt(rotationSalt.length);
        return cancel(rotatedSalt[rotation],cancel(trim(nissy("solve","corners","R' U' F "+inserts[ThreadLocalRandom.current().nextInt(inserts.length)]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+ algs[alg][0]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+" "+rotationSalt[rotation])),"R' U' F"));
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

    private String trim(String input){
        while (input.charAt(input.length() - 1)!=' '){
            input = input.substring(0,input.length()-1);
        }
        return input.substring(0,input.length()-1);
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