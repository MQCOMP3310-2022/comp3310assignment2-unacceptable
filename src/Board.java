import java.awt.Graphics;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
// import java.util.Scanner;
// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;


public class Board {
    Grid grid;
    SQLiteConnectionManager wordleDatabaseConnection;
    int secretWordIndex;
    int numberOfWords;

    public Board(){
        wordleDatabaseConnection = new SQLiteConnectionManager("words.db");
        int setupStage = 0;

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined())
        {
            System.out.println("Wordle created and connected.");
            if(wordleDatabaseConnection.createWordleTables())
            {
                System.out.println("Wordle structures in place.");
                setupStage = 1;
            }
        }

        if(setupStage == 1)
        {
            //let's add some words to valid 4 letter words from the data.txt file

            try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
                String line;
                int i = 1;
                while ((line = br.readLine()) != null) {
                   //System.out.println(line);
                   wordleDatabaseConnection.addValidWord(i,line);
                   i++;
                }
                numberOfWords = i;
                setupStage = 2;
            }catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }
        else{
            System.out.println("Not able to Launch. Sorry!");
        }



        grid = new Grid(6,4, wordleDatabaseConnection);
        secretWordIndex = 2;
        String theWord = wordleDatabaseConnection.getWordAtIndex(2);
        grid.setWord(theWord);
    }

    public void resetBoard(){
        grid.reset();
    }

    void paint(Graphics g){
        grid.paint(g);
    }    

    public void keyPressed(KeyEvent e){
        System.out.println("Key Pressed! " + e.getKeyCode());

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            grid.keyPressedEnter();
            System.out.println("Enter Key");
        }
        if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            grid.keyPressedBackspace();
            System.out.println("Backspace Key");
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            grid.keyPressedEscape();
            
            secretWordIndex = ( secretWordIndex + 1 ) % numberOfWords;
            String theWord = wordleDatabaseConnection.getWordAtIndex(secretWordIndex);
            grid.setWord(theWord);

            System.out.println("Escape Key");
        }
        if(e.getKeyCode()>= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z){
            grid.keyPressedLetter(e.getKeyChar());
            System.out.println("Character Key");
        }

    }
}
