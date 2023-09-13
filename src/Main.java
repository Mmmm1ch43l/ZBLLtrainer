// Import the necessary packages
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame implements KeyListener {

    // Declare the components
    private JLabel questionLabel;
    private JLabel answerLabel;
    private JLabel questionText;
    private JLabel answerText;

    // Declare the arrays of questions and answers
    private String[] questions;
    private String[] answers;

    // Declare a variable to keep track of the current index
    private int index;

    // Constructor to initialize the components, layout and arrays
    public Main(String[] questions, String[] answers) {
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
        setSize(400, 200);
        setLocation(100, 100);

        // Set the default close operation of the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make the window visible
        setVisible(true);

        // Initialize the arrays of questions and answers with the parameters
        this.questions = questions;
        this.answers = answers;

        // Initialize the index to zero
        index = 0;

        // Display the first question and answer in the text labels
        questionText.setText(questions[index]);
        answerText.setText(answers[index]);

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

        // Increment the index by one and wrap around if it reaches the end of the array length
        index = (index + 1) % questions.length;

        // Display the next question and answer in the text labels
        questionText.setText(questions[index]);
        answerText.setText(answers[index]);

    }

    // Main method to create an instance of the window with some sample questions and answers
    public static void main(String[] args) {

        // Create an array of questions
        String[] questions = {"asdf","asdff","What is the capital of Switzerland?", "What is 2 + 2?", "Who wrote Hamlet?"};

        // Create an array of answers
        String[] answers = {"asdffe","eofejf","Bern", "4", "William Shakespeare"};

        // Create an instance of the window with these arrays as parameters
        new Main(questions, answers);

    }
}
