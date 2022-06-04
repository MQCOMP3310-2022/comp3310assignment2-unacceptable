import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class App extends JFrame {

    class WordleGame extends JPanel implements KeyListener{
        Board board;
        boolean stageBuilt = false;

        public WordleGame(){
            setPreferredSize(new Dimension(330, 490));
            this.addKeyListener(this);
            board = new Board();
            stageBuilt = true;
            this.setFocusable(true);
            this.requestFocus();
        }

        @Override
        public void paint(Graphics g) {
          if (stageBuilt && isVisible()) {
            board.paint(g);
          }
        }

        @Override
        public void keyPressed (KeyEvent e) {}    
        
        @Override
        public void keyReleased (KeyEvent e) {
            board.keyPressed(e);
        }    

        @Override
        public void keyTyped (KeyEvent e) {}    

    }

    public static void main(String[] args) throws Exception {
        App window = new App();
        window.run();
    }

    private App() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        WordleGame canvas = new WordleGame();
        this.setContentPane(canvas);
        this.pack(); 
        this.setVisible(true);
    }

    public void run() {
        while (true) {
            Instant startTime = Instant.now();
            this.repaint();
            Instant endTime = Instant.now();
            long howLong = Duration.between(startTime, endTime).toMillis();
            try {
                Thread.sleep(20L - howLong);
            } catch (InterruptedException e) {
                System.out.println("thread was interrupted, but who cares?");
            } catch (IllegalArgumentException e) {
                System.out.println("application can't keep up with framerate");
            }
        }
    }


}
