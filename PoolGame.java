import java.awt.*;
import java.awt.event.*;

public class PoolGame extends Frame {
	public static void main(String[] args) {new PoolGame();}

	PoolGame() {
		super("Pool Game");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		setSize(800, 600);
		add("Center", new PoolCanvas());
		setVisible(true);
	}
}

class PoolCanvas extends Canvas implements Runnable{
	int w, h;
	long lastFrame;
	GameState game; 
	BallController cueBallController;
	double scale, xOffset, yOffset;

	PoolCanvas() {
		lastFrame = System.currentTimeMillis();
		game = new GameState();
		cueBallController = new BallController(game.table.getCueBall());

		Thread u = new Thread(this); u.start();

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && !game.table.moving) { // Left button
					cueBallController.pressMouse(e.getX(), e.getY());
					cueBallController.holdMouse(e.getX(), e.getY());

					// place the ball down if we can and we're in placing mode
					if (cueBallController.mode == BallController.MODE_PLACING && cueBallController.canPlace){
						cueBallController.placeBall(scale, xOffset, yOffset);
						cueBallController.resetMouse();
						cueBallController.mode = BallController.MODE_NONE; game.foul = false;
					}
					else {
						// if the ball's sunk, we're placing it to get it out of the pocket. otherwise, we're gonna shoot it
						cueBallController.mode = game.foul ? BallController.MODE_PLACING : BallController.MODE_SHOOTING;
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3) { // Right button
					// reset the GameState on right mouse button click
					game = new GameState();
					cueBallController = new BallController(game.table.getCueBall());
				}
			}
			public void mouseReleased(MouseEvent e){
				if (e.getButton() == MouseEvent.BUTTON1) { // Left button
					cueBallController.releaseMouse(e.getX(), e.getY());
					if (cueBallController.mode == BallController.MODE_SHOOTING){
						cueBallController.shootBall(scale);
						cueBallController.resetMouse();
						cueBallController.mode = BallController.MODE_NONE;
					}
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				cueBallController.moveMouse(e.getX(), e.getY());
			}

			public void mouseDragged(MouseEvent e) {
				cueBallController.holdMouse(e.getX(), e.getY());
			}
		});
	}

	/**
	 * Adjusts scale, xOffset and yOffset based on the width and height of this canvas.
	 * Useful for events dependent on mouse functionality in the event that the window is resized.
	 */
	public void calibrateScaling(){
		scale = game.getScale(w, h);
		xOffset = game.getXOffset(w, h); 
		yOffset = game.getYOffset(w, h);
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
		w = getSize().width; h = getSize().height; calibrateScaling();
		Image buffer = createImage(w, h);
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
		w = getSize().width; h = getSize().height; calibrateScaling();

		// turn the Graphics object into a Graphics2D object, then apply anti-aliasing to it
		Graphics2D g2d = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHints(rh);

		// figure out how long its been between now and the last frame
		long currentFrame = System.currentTimeMillis();
		double t = (double)(currentFrame - lastFrame)/1000.0;
		lastFrame = System.currentTimeMillis();

		// move the balls, then draw them
		for (int i = 0; i < 10; i++){
			game.moveTime(t/10.0);
		}
		game.draw(g2d, w, h);

		if (cueBallController.mode == BallController.MODE_SHOOTING){
			// gets velocity of ball assuming you released the mouse right now
			double[] vel = cueBallController.getShotSpeed(scale, cueBallController.xPressed, cueBallController.yPressed, cueBallController.xHeld, cueBallController.yHeld);
			double xVel = vel[0]; double yVel = vel[1];
			game.drawShotPreview(g2d, w, h, xVel, yVel);
		}
		else if (cueBallController.mode == BallController.MODE_PLACING){
			cueBallController.canPlace = game.drawPlacePreview(g2d, w, h, cueBallController.xMoved, cueBallController.yMoved);
		}
	}
}