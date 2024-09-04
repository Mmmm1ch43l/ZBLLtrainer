// Import the necessary packages
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ProcessBuilder;
import java.lang.Process;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame implements KeyListener {
    private static boolean runningWindows = false;

    private final boolean include2GLL = false;
    private final boolean includeU = true;
    private final boolean includeH = false;
    private final double pseudoRandomNumberGeneratorDecay = 0.95;

    private final PseudoRandomNumberGenerator prng;

    private final JLabel questionText;
    private final JLabel answerText;
    private final String[] AUFs = {" "," U "," U' "," U2 "};
    private final String[] inserts = {
            "R U R'","R U' R'","R U2 R'","R' F R F'",
            "L' U L","L' U' L","L' U2 L","L F' L' F",
            "R' U R","R' U' R","R' U2 R",
            "L U L'","L U' L'","L U2 L'"
    };
    private final String[] rotatedSalt1 = {
            "","R' Uw ","R' Uw2 ","R' Uw' ",
            "Rw ","Rw Uw ","Rw Uw2 ","Rw Uw' ",
            "Rw' ","Rw' Uw ","Rw' Uw2 ","Rw' Uw' ",
            "Fw ","Fw Uw ","Fw Uw2 ","Fw Uw' ",
            "Fw' ","Fw' Uw ","Fw' Uw2 ","Fw' Uw' ",
            "Rw2 ","Rw2 Uw ","Fw2 ","Rw2 Uw' "
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

    private String[] ZBLLs = new String[0];
    private final String[] UZBLLs = {
            // U 2GLL
            "U' L' U2 L U L' U L R U2 R' U' R U' R'",
            "U2 R U R' U R U2 R' U R U2 R' U' R U' R'",
            "U' R U R' U' R U' R' U2 R U' R' U2 R U R'",
            "U R U2 R' U' R U' R' U' R U R' U R U2 R'",
            "U R' U2 R U R' U R U R' U' R U' R' U2 R",
            "U R' U2 R2 U R2 U R U' R U R' U' R U' R'",
            "U R U2 R2 U' R2 U' R' U R' U' R U R' U R",
            "R' U' R U' R' U2 R2 U R' U R U2 R'",
            "U2 R U R' U R U2 R2 U' R U' R' U2 R",
            "R U R' U' R U' R U2 R2 U' R U R' U' R2 U' R2",
            "U2 R U R' U R' U2 R2 U R2 U R2 U' R'",
            "R' U' R U' R U2 R2 U' R2 U' R2 U R",
            // U diag
            "R U R' L' U2 R U' R' U' R U' M' x'",// block left (fl)
            "U2 L' R U R' U R U R' U2 L R U' R'",// block right (fr)
            "R2 D' R U R' D R U R U' R' U' R",// pair left front (fL)
            "U' R' D' R U' R' D R2 U2 R' U R U R'",// pair right front (fR)
            "R U' R' U' R U2 R2 D' R U R' D R",// pair front left (Fl)
            "U' F U' R' U R U F' R' U2 R",// pair front right (Fr)
            "U' Rw U2 R2 F R F' U2 Rw' R U R U' R'",// two pairs left like N-perm (fL+Fr)
            "U' R' U2 R F U' R' U R U R' U R U' F'",// two pairs right like N-perm (Fl+fR)
            "U F U R U' R' F' R U R' U' M' U R U' Rw'",// two pairs in the back (bL+bR)
            "F U R U2 R' U R U R' U R U2 R' U R U R' F'",// no pairs
            "F U R U2 R2 U2 R U R' U R U2 R U R' F'",// pair right back (bR)
            "U' R U2 R2 D' R U' R' D R U' R' F R U R U' R' F'",// pair left back (bL)
            /*
            // U bars
            "R' F R U' R' U' R U R' F' R U R' U' R' F R F' R",// big block front right
            "D R D' R2 F' R U R' Fw R Fw' R' U' F R2",// big block front left
            "U' R2 F' R U R' U' R' F R2 U' R' U2 R2 U R' U R",// bar in front (opposites on both sides)
            "U R' U R U R' F' R U R' U' R' F R2 U' R' U2 R U' R' U2 R",// T-perm in front
            "x' R2 U2 R' U2 R' F R F' Rw U' L' U R'",// pair left front (fL)
            "x' L2 U2 L U2 L F' L' F Lw' U R U' L",// pair right front (fR)
            "U' Rw U R' U' Rw' F R2 U' R' U' R U2 R' U' F'",// pair left back (bL) + FR edge opposite
            "R2 F R U R U' R' F' R U' R2 D' R U R' D R2",// pair right back (bR) + FR edge opposite
            "U R' U' R U' R' U2 R U' R' U2 R' D' R U2 R' D R2",// pair right back (bR) + FR edge adjacent
            "U R U R' U R U2 R' U R U2 R D R' U2 R D' R2",// pair left back (bL) + FR edge adjacent
            "U' R' U' R F R2 D' R U R' D R2 U' F'",// nothing + FR edge goes right
            "U' F U R2 D' R U' R' D R2 F' R' U R",// nothing + FR edge goes left
            // U Checkerboard
            "F R U' R' U R U R' U R U' R' F'",// nothing + like Y-perm
            "U F' R U R' U' R' F R2 U R' U2 R U R' U2 R U' R'",// nothing + like V-perm
            "U2 x R2 D2 R U2 R' D2 R U2 R x'",// block left
            "x' R2 D2 R' U2 R D2 R' U2 R' x",// block right
            "R U R' U R U' R' U2 R' D' R U2 R' D R2 U' R'",// two pairs left like N-perm (fL+Fr)
            "U' R' U2 R U R' U R R R D R' U2 R D' R' U2 R'",// two pairs right like N-perm (Fl+fR)
            "R' U2 R U R' U R' D' R U' R' D R U R",// pair right front (fR)
            "U2 R U2 R' U' R' D' R U R' D R U' R U' R'",// pair left front (fL)
            "U' R U' R' U' R U' R' U R' D' R U R' D R2 U R'",// two pairs right like other N-perm, i.e. more back (Fr+bR)
            "U' R' U R U R' U R U' R D R' U' R D' R2 U' R",// two pairs left like other N-perm, i.e. more back (Fl+bL)
            "R' U' R U2 R' F' R U R' U' R' F R2 U2 R' U R",// pair right back (bR)
            "U2 R U R' U R U R' U2 R U' R2 D' R U' R' D R",// pair left back (bL)
            // U left slash (BL and FR)
            "R2 D' R U2 R' D R U2 R",// block left
            "R2 D' Rw U2 Rw' D R U2 R",// block right
            "U R' U' R2 D R' U' R D' R2 U2 R",// two pairs right front (Fl+fR)
            "U2 R' U R U R' U2 R U R D R' U2 R D' R'",// pair front right and two pairs in back (Fr+bL+bR)
            "U2 F R U R' U' R2 D R' U' R D' R2 U' R U R' F'",// one pair front right (Fr)
            "U' R2 F' R U2 R U2 R' F U' R U R' U' R",// pair front left and right back (Fl+bR)
            "U' R' U R U R' U2 R U' D' R U' R' U2 R U' R' D",// pair right front (fR)
            "U' L U2 R' F R U' R U R' U F' R U2 R' L'",// pair left front (fL)
            "U F U R U2 R' U R U2 R2 F R F' R U' R' F'",// pair right back and H perm left side (bR)
            "U R U R' U R U' R' U R U' R' U' Rw' F R F' M'",// no pairs and H perm right side
            "U' R' U2 R' D' R U2 R' D R U2 R U R' U R",// pair left back and Z perm right side (bL)
            "U' R' U R U' R' U' R U2 R D R' U' R D' R2 U' R",// no pairs and nothing else either (sorta G-perm-ish)
            // U right slash (FL and BR)
            "U2 R2 D R' U2 R D' R' U2 R'",// block right
            "U2 R2 D Rw' U2 Rw D' R' U2 R'",// block left
            "U R U R2 D' R U R' D R2 U2 R'",// two pairs left front (Fr+fL)
            "R U' R' U' R U2 R' U' R' D' R U2 R' D R",// pair front left and two pairs in back (Fl+bL+bR)
            "R' U' R U R U R' U' R' U F R U R U' R' F'",// one pair front left (Fl)
            "U' R U R' U R U' R' U F' R U2 R' U2 R' F R",// pair front right and left back (Fr+bL)
            "R U R' U' R U R2 D' R U R' D R2 U R' U' R U' R'",// pair left front (fL)
            "U R' U R' U' D' R U' R' U2 R U' R' D R U' R",// pair right front (fR)
            "Rw U R' U R' D' R U R' D R U Rw' F R F'",// pair left back and H perm right side (bR)
            "U R U R' U R U' R' U R U' R' U' Rw' F R F' M'",// no pairs and H perm left side
            "U' R U2 R D R' U2 R D' R' U2 R' U' R U' R'",// pair right back and Z perm left side (bR)
            "U' R U' R' U R U R' U2 R' D' R U R' D R2 U R'",// no pairs and nothing else either (sorta G-perm-ish)
            */
    };

    private final String[] HZBLLs = {
            // H 2GLL
            "U R' U2 R U R' U R U R U R' U R U2 R'",
            "U R U2 R' U' R U' R' U' R' U' R U' R' U2 R",
            "U R U2 R' U' R U R' U' R U' R'",
            "U R' U2 R U R' U' R U R' U R",
            "R U R' U R U' R' U R U2 R'",
            "R' U' R U' R' U R U' R' U2 R",
            "R U R' U R U' R' U R U' R' U R' U' R2 U' R' U R' U R",
            "R U R' U R U2 R' U' R' U2 R U R' U R",
            //H diag
            "U F U R U' R' U R U' R' U R U' R' F'",// opposites
            "x' U' R U' R' U R' F2 R U' R U R' U x",// bars
            "U' R U R' U y' R' U R U' R2 F R F' R y",// bar front, back matches right
            "F' U' F U' R U' R' U R2 B' R' B R'",// bar front, back matches left
            "U Rw U2 R2 F R F' R U2 Rw2 F2 Rw U Rw' F Rw",// back opposite, front matches right
            "R' U' F' U F R U' F U R U' R' U R U' R' F'",// back opposite, front matches left
            "Rw U2 R2 F R F' R U2 Rw2 F' Rw U' Rw' F2 Rw",// adjacents, front matches right
            "R U' R' U R U R' U' L U L' U' R U R' U2 L U L'",// adjacents, front matches left
            //H vertical column (on right)
            "R' F' R U2 R U2 R' F U' R U' R'",// pair front right (Fr)
            "R U R' U R U L' U R' U' L",// pair back right (Br)
            "U R U R' U R U2 R' F R U' R' U' R U2 R' U' F'",// pairs front and back right (Fr+Br)
            "R' U' R D' R U' R' U2 R U2 R U R U' R2 D",// opposites front and back right (Fr+Br)
            "U' R U R2 F R F' Rw U' Rw' U Rw U Rw'",// pairs front right and back left (Fr+Bl)
            "U R' F R' F' R2 U' Rw' U Rw U' Rw' U' Rw",// pairs front left and back right (Fl+Br)
            "R' F' R U2 R U2 R' F R' U' R2 U' R' U R' U R",// pair front left (Fl) + solved/checkerboard thingy back right (br)
            "U2 R' U' R U' R' U F' R U R' U' R' F R2 U' R' U R",// pair back left (Bl) + solved/checkerboard thingy front right (fr)
            "U R' U2 R U R2 D' R U' R' D R2 U R' U R",// pair front left (Fl) + bar right
            "U' R U2 R' U' R2 D R' U R D' R2 U' R U' R'",// pair back left (Bl) + bar right
            "U' R' U2 R U R' U' F' R U R' U' R' F R U2 R",// solved/checkerboard thingy front left (fl)
            "U2 R2 D' R U' R' D R2 U' R2 D' R U2 R' D R2",// solved/checkerboard thingy back left (bl)
            //H horizontal column (on front)
            "F R U' R' U R U2 R' U' R U R' U' F'",// opposites front and back
            "U2 R U R' U R U' R2 F' R U2 R U2 R' F R U' R'",// bar front and back
            "U2 F U' R U2 R' U2 R U' R' U' R U R' U F'",// back opposite + pair front right (Fr)
            "U2 R F R2 U' R2 U' R2 U2 R2 U' F' R'",// back opposite + pair front left (Fl)
            "U F R' F' Rw U R U' Rw2 F2 Rw U L' U L",// bar in the back + pair front right (Fr)
            "U' F' Rw U R' U' Rw' F R2 U2 R' U' R U' R'",// bar in the back + pair front left (Fl)
            "U R' U2 R U2 R2 F' R U R U' R' F U R",// back matching right + front opposite
            "U' F' R U2 R' U2 R' F R U R U R' U' R U' R'",// back matching left + front opposite
            "U2 R U2 R' U' R U' R2 R' D' R U' R' D R U R",// back matching right + bar front
            "R' U2 R U R' U R2 R D R' U R D' R' U' R'",// back matching left + bar front
            "U R U2 R' U' R U R' U' F' R U R' U' R' F R2 U' R'",// back matching right + pair front right (Fr)
            "R' U2 R2 U R2 U R U2 R' F R U R U' R' F'",// back matching left + pair front left (Fl)
    };
    private final String[] _2GLLs = {
            "U' R U R' U R U2 R' L' U' L U' L' U2 L",// T 2GLL
            "U2 R U' R' U2 R U R' U2 R U R' U R U' R'",
            "U' R U R' U R U2 R' U' R U2 R' U' R U' R'",
            "R U2 R' U' R U' R' U R U R' U R U2 R'",
            "U2 R' U2 R U R' U R U' R' U' R U' R' U2 R",
            "U' R U R' U R U' R' U R' U' R2 U' R2 U2 R",
            "R' U R2 U R' U R' U' R U' R' U' R U R U' R'",
            "R U2 R' U' R U' R2 U2 R U R' U R",
            "U2 R' U2 R U R' U R2 U2 R' U' R U' R'",
            "U2 R U' R' U2 R U R' U R' U' R U R U R' U' R' U R",
            "U' R' U' R2 U R2 U R2 U2 R' U R' U R",
            "U' R U R2 U' R2 U' R2 U2 R U' R U' R'",
            "R U2 R' U2 R' U' R U R U' R' U2 R' U2 R",// L 2GLL
            "U2 R U R' U R U2 R' U2 R U2 R' U' R U' R'",
            "U2 R U2 R' U' R U' R' U R' U2 R U R' U R",
            "U R' U2 R U R' U R U' R U2 R' U' R U' R'",
            "U2 R U R' U R U2 R' U R' U' R U' R' U2 R",
            "U R' U' R U' R' U2 R U' R U R' U R U2 R'",
            "R2 U R' U R' U' R U' R' U' R U R U' R2",
            "U2 R2 U' R U R U' R' U' R U' R' U R' U R2",
            "U' R2 U' R U' R U R' U R U R' U' R' U R2",
            "U R2 U R' U' R' U R U R' U R U' R U' R2",
            "R U R' U R U' R' U R U' R' U R U2 R'",
            "U2 R U2 R' U' R U' R' U2 R U R' U R U2 R'",
            "R U R' U R U2 R' R' U2 R U R' U R",// Pi 2GLL
            "R U2 R' U' R U' R' R' U' R U' R' U2 R",
            "R U2 R' U' R U' R' U R U2 R' U' R U' R'",
            "U' R' U' R U' R' U2 R U R' U' R U' R' U2 R",
            "U2 R U2 R' U2 R U' R' U2 R U' R' U2 R U R'",
            "R' U2 R U2 R' U R U2 R' U R U2 R' U' R",
            "U R U' R' U2 R U R' U2 R U R' U2 R U2 R'",
            "U R' U R U2 R' U' R U2 R' U' R U2 R' U2 R",
            "U' R U2 R2 U' R2 U' R2 U2 R",
            "U' R' U2 R2 U R2 U R2 U2 R'",
            "U' R U2 R' U' R U' R' U' R U2 R' U' R U' R'",
            "U' F R U R' U' R U R' U' F' R U R' U' M' U R U' Rw'",
    };
    private int ZBLL;
    // Constructor to initialize the components, layout and arrays
    public Main() {

        // Set the title of the window
        super("ZBLL trainer");


        if(System.getProperty("os.name").startsWith("Windows")) runningWindows = true;
        if(include2GLL){
            String[] temp = new String[ZBLLs.length+_2GLLs.length];
            System.arraycopy(ZBLLs, 0, temp, 0, ZBLLs.length);
            System.arraycopy(_2GLLs, 0, temp, ZBLLs.length, _2GLLs.length);
            ZBLLs = temp;
        }
        if(includeU){
            String[] temp = new String[ZBLLs.length+UZBLLs.length];
            System.arraycopy(ZBLLs, 0, temp, 0, ZBLLs.length);
            System.arraycopy(UZBLLs, 0, temp, ZBLLs.length, UZBLLs.length);
            ZBLLs = temp;
        }
        if(includeH){
            String[] temp = new String[ZBLLs.length+HZBLLs.length];
            System.arraycopy(ZBLLs, 0, temp, 0, ZBLLs.length);
            System.arraycopy(HZBLLs, 0, temp, ZBLLs.length, HZBLLs.length);
            ZBLLs = temp;
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
        setSize(630, 200);
        setLocation(100, 100);

        // Set the default close operation of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the window visible
        setVisible(true);

        prng = new PseudoRandomNumberGenerator(ZBLLs.length,pseudoRandomNumberGeneratorDecay);

        // Display the first question and answer in the text labels
        String temp = "";
        while(temp.isEmpty()){
            ZBLL = prng.generate();
            temp = ZBLLs[ZBLL];
        }
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
        String temp = "";
        while(temp.isEmpty()){
            ZBLL = prng.generate();
            temp = ZBLLs[ZBLL];
        }
        answerText.setText(generateScramble(ZBLL));

    }

    // Main method to create an instance of the window with some sample questions and answers
    public static void main(String[] args) {
        // Create an instance of the window with these arrays as parameters
        new Main();

    }

    private String generateScramble (int ZBLL){
        int rotation = ThreadLocalRandom.current().nextInt(rotationSalt.length);
        return rotatedSalt1[rotation]+cancel(rotatedSalt2[rotation],cancel(nissy("twophase","R' U' F "+inserts[ThreadLocalRandom.current().nextInt(inserts.length)]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+ZBLLs[ZBLL]+AUFs[ThreadLocalRandom.current().nextInt(AUFs.length)]+" "+rotationSalt[rotation]),"R' U' F"));
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