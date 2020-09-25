import java.awt.*;
import java.awt.event.*;

// TODO: change the name of this class to PoolGame? it's going to be the file we'll ultimately end up running
public class Collisions extends Frame {
	public static void main(String[] args) {new Collisions();}

	Collisions() {
		super("Collisions");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		setSize(800, 600);
		add("Center", new CollisionCanvas());
		setVisible(true);
	}
}

class CollisionCanvas extends Canvas implements Runnable{
    int w, h;
    int n, xPressed, yPressed;
    int xHeld, yHeld;
    long lastFrame;
    GameState game;

	CollisionCanvas() {
        lastFrame = System.currentTimeMillis();
        xPressed = -1; yPressed = -1;
        game = new GameState();

        Thread u = new Thread(this);
        u.start();

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == e.BUTTON1) { // Left button
                    xPressed = e.getX(); yPressed = e.getY();
                    xHeld = e.getX(); yHeld = e.getY();
				}
                else if (e.getButton() == e.BUTTON3) { // Right button
                    // stuff if we need it
                }
            }
            public void mouseReleased(MouseEvent e){
                if (e.getButton() == e.BUTTON1) { // Left button
                    Ball b = new Ball(20, xPressed, yPressed);
                    b.xVel = (xPressed - e.getX()) * 5;
                    b.yVel = (yPressed - e.getY()) * 5;
                    b.r = 235; b.g = 240; b.b = 209;
                    game.replaceBall(0, b);

                    xPressed = -1; yPressed = -1;
				}
            }
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
                // stuff if we need it
			}

            public void mouseDragged(MouseEvent e) {
                if (xPressed > -1 && yPressed > -1) {
                    xHeld = e.getX(); yHeld = e.getY();
                } 
            }
		});
    }

    // this refreshes the canvas at a given interval
    // code taken from http://www.learntosolveit.com/java/AnimatedCanvas.html
    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(16); // 16ms for ~60 fps
            } 
            catch (InterruptedException e) {}
            repaint();
        }
    }

    // the flickering was really starting to irritate me, so i implemented a double buffer following the first code snippet here:
    //     http://underpop.online.fr/j/java/help/getting-rid-of-flicker-and-tearing-d-graphics-and-animation-java.html.gz
    // this shouldn't be too much of a detriment on performance, i hope
    public void update(Graphics g){
        Image buffer = createImage(getSize().width, getSize().height);
        Graphics bufferG = buffer.getGraphics();
        if (bufferG != null){
            paint(bufferG); // paint onto the buffer first
            bufferG.dispose(); // get rid of the graphics object to avoid memory issues 
            g.drawImage(buffer, 0, 0, null); // then copy the buffer onto our canvas
        }
        else{
            paint(g); // if the buffer doesn't exist for whatever reason, just paint on the canvas directly
        }
    }

	public void paint(Graphics g) {
        // get w, h
        w = getSize().width; h = getSize().height;

        // figure out how long its been between now and the last frame
        long currentFrame = System.currentTimeMillis();
        double t = (double)(currentFrame - lastFrame)/1000.0;
        lastFrame = System.currentTimeMillis();

        // move the balls
        for (int i = 0; i < 10; i++){
            game.moveTime(t/10.0);
        }
        game.drawBalls(g);

        if (xPressed >= 0){
            g.setColor(Color.red); g.drawOval(xPressed-20, yPressed-20, 40, 40);
            g.drawLine(xPressed, yPressed, xPressed+(xPressed-xHeld)/2, yPressed+(yPressed-yHeld)/2);
        }
	}
}