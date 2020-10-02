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
	int n, xPressed, yPressed;
	int xHeld, yHeld;
	long lastFrame;
	TableState game;

	PoolCanvas() {
		lastFrame = System.currentTimeMillis();
		xPressed = -1; yPressed = -1;
		game = new TableState();

		Thread u = new Thread(this);
		u.start();

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) { // Left button
					xPressed = e.getX(); yPressed = e.getY();
					xHeld = e.getX(); yHeld = e.getY();
				}
				else if (e.getButton() == MouseEvent.BUTTON3) { // Right button
					// reset the TableState on right mouse button click
					game = new TableState();
				}
			}
			public void mouseReleased(MouseEvent e){
				if (e.getButton() == MouseEvent.BUTTON1) { // Left button
					// gets scale and offsets to account for isotropic scaling
					double scale = Math.min((double)w/game.w, (double)h/game.h);
					double xOffset = (w - game.w*scale)/2; 
					double yOffset = (h - game.h*scale)/2;

					// replaces the first ball with a ball whose velocity is based on how far you dragged the mouse
					Ball b = new Ball(20, (xPressed-xOffset)/scale, (yPressed-yOffset)/scale);
					b.xVel = (xPressed - e.getX()) * 5 / scale;
					b.yVel = (yPressed - e.getY()) * 5 / scale;
					b.setColor(235, 240, 209);
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

		// move the balls, then draw them
		for (int i = 0; i < 10; i++){
			game.moveTime(t/10.0);
		}
		game.draw(g, w, h);

		// TODO: this is a nightmare to look at; need to figure out somewhere else to throw this into and how to fix it
		if (xPressed >= 0){
			double scale = Math.min((double)w/game.w, (double)h/game.h); // recalculates scaling to account for isotropic scaling
			double xOffset = (w - game.w*scale)/2; 
			double yOffset = (h - game.h*scale)/2;

			g.setColor(Color.red); g.drawOval(xPressed-(int)(20*scale), yPressed-(int)(20*scale), (int)(40*scale), (int)(40*scale)); // draws where the ball would be placed
			g.drawLine(xPressed, yPressed, xPressed+(xPressed-xHeld)/2, yPressed+(yPressed-yHeld)/2); // draws the initial velocity vector of the ball

			// determines where the collision point of this ball would be
			double xVel = (xPressed - xHeld) * 5 / scale; double yVel = (yPressed - yHeld) * 5 / scale;
			double[] pos = game.nextCollisionPoint(20, (xPressed-xOffset)/scale, (yPressed-yOffset)/scale, xVel, yVel);
			double xPos = pos[0]*scale + xOffset; double yPos = pos[1]*scale + yOffset;

			// draws where the ball would be at that collision point
			g.drawOval((int)(xPos-(20*scale)), (int)(yPos-(20*scale)), (int)(40*scale), (int)(40*scale));
		}
	}
}