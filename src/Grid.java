import java.awt.Graphics;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Consumer;


public class Grid implements Iterable<Cell>{
    Cell[][] cells;
    int activeRow;
    int activeColumn;
    String wordToGuess;
    boolean gameFinished;
    SQLiteConnectionManager wordleDatabaseConnection;

    //Streak implementation
    int streak;
    
    public Grid(int rows, int wordLength, SQLiteConnectionManager sqlConn){
        cells = new Cell[rows][wordLength];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                int x = 10 + 80 * i;
                int y = 10 + 80 * j;
                cells[i][j] = new Cell(i,j,y,x);
            }
        }
        activeRow = 0;
        activeColumn = 0;
        cells[activeRow][activeColumn].setActive();
        wordToGuess = "";
        gameFinished = false;
        wordleDatabaseConnection = sqlConn;
        
        streak = 0; //streak implementation
    }

    void setWord(String word){
        wordToGuess = word;
    }

    public void paint(Graphics g) {
        doToEachCell((Cell c) -> c.paint(g));
    }

    public void reset(){
        //if game has reset before it has finished then reset streak
        if(!gameFinished){
            streak = 0;
        }
        
        cells[activeRow][activeColumn].setInactive();
        activeRow = 0;
        activeColumn = 0;
        gameFinished = false;
        doToEachCell((Cell c) -> c.reset());
        cells[activeRow][activeColumn].setActive();
        //how to get a new word?
    }

    /**
     * Takes a cell consumer (i.e. a function that has a single `Cell` argument and
     * returns `void`) and applies that consumer to each cell in the grid.
     *
     * @param func The `Cell` to `void` function to apply at each spot.
     */
    public void doToEachCell(Consumer<Cell> func) {
        for (Cell[] i:cells) {
            for (Cell j:i) {
                func.accept(j);
            }
        } //Fixed to use for each loop instead
    }

	@Override
	public Iterator<Cell> iterator() {
		return new CellIterator(cells);
	}

    void keyPressedBackspace(){
        if(!gameFinished){
            cells[activeRow][activeColumn].setCharacter(' ', 0);
            if(activeColumn > 0){
                cells[activeRow][activeColumn].setInactive();
                activeColumn--;
                cells[activeRow][activeColumn].setActive();
            }
        }
    }

    void keyPressedEscape(){
        reset();
    }

    void keyPressedEnter(){
        if(!gameFinished){
            
            //is the row full? If so, let's compare!
            if( activeColumn == cells[activeRow].length -1 &&
            !" ".equals(cells[activeRow][activeColumn].getStoredCharacter())){  //Fixed to have correct literal position
                
                if(checkActiveRowAgainstWord()){
                    //success!
                    for(int i = 0; i < cells[activeRow].length; i++){
                        cells[activeRow][i].setInactive();
                        cells[activeRow][i].setState(3);
                    }
                    streak++; //increase streak
                    gameFinished = true;                 
                }else{
                    if(activeRow >= cells.length-1){
                        // run out of guesses to use
                        for(int i = 0; i < cells[activeRow].length; i++){
                            cells[activeRow][i].setInactive();
                            cells[activeRow][i].setState(4);
                        }
                        streak = 0; //reset streak
                        gameFinished = true;
                    }else{
                        //do stuff to highlihgt correct characters
                        applyHighlightingToCurrentRow();
                        //move to next row
                        cells[activeRow][activeColumn].setInactive();
                        activeRow++;
                        activeColumn = 0;
                        cells[activeRow][activeColumn].setActive();
                    }
                }

            }
        }
    }

    void keyPressedLetter(char letter){
        if(!gameFinished){
            System.out.println("grid keypress received letter: " + letter);
            cells[activeRow][activeColumn].setCharacter(letter, 1);
            if(activeColumn < cells[activeRow].length -1){
                //not last character
                cells[activeRow][activeColumn].setInactive();
                activeColumn++;
                cells[activeRow][activeColumn].setActive();
            }
            // if they keep on typing characters, then just repopulate the last valye
        }
    }

    protected boolean checkActiveRowAgainstWord(){
        String word ="";
        for(int i = 0; i < cells[activeRow].length; i++){
            word = word + cells[activeRow][i].getStoredCharacter().toLowerCase(Locale.US);
            //Now converts capital letters to lowercases
        }
        return word.equals(wordToGuess);
    }

    protected void applyHighlightingToCurrentRow(){
        BitSet highlighted = new BitSet(cells[activeRow].length);
        highlighted.clear();

        //check for green characters
        for(int i = 0; i < cells[activeRow].length; i++){
            if(cells[activeRow][i].getStoredCharacter().equals(""+wordToGuess.charAt(i))){
                cells[activeRow][i].setState(3);
                highlighted.set(i);
            }
        }

        
        //crude method
        for(int i = 0; i < cells[activeRow].length; i++){
            if(!highlighted.get(i)){
                char c = cells[activeRow][i].getStoredCharacter().charAt(0);
                int countHighlighted = numberAlreadyHighlighted(c, cells[activeRow], highlighted);
                int countOccurences = numberOccurencesInGoal(c);

                if(wordToGuess.contains(cells[activeRow][i].getStoredCharacter())){
                    if(countHighlighted < countOccurences){
                        cells[activeRow][i].setState(2); 
                        highlighted.set(i);
                    }
                }
            }
        }

    }

    protected int numberAlreadyHighlighted(char searchChar, Cell[] word, BitSet highlighted){
        // count the number of characters in the search word
        int counter = 0;
        for(int i = 0; i < word.length; i++){
            if(word[i].getStoredCharacter().equals(""+searchChar) && highlighted.get(i)){
                counter++;
            }
        }
        return counter;
    }

    protected int numberOccurencesInGoal(char searchChar){
        int counter = 0;
        for( int i = 0; i < wordToGuess.length(); i++ ){
            if(wordToGuess.charAt(i) == searchChar){
                counter++;
            }
        }
        return counter;
    }

}