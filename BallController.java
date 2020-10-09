/**
 * Handles firing and placing balls onto the table given some mouse input.
 */
public class BallController {
	public final Ball ball;

	public int xPressed, yPressed;
	public int xHeld, yHeld;
	public int xReleased, yReleased;
	public int xMoved, yMoved;

	public boolean canPlace;

	public int mode;
	public static final int MODE_NONE = 0;
	public static final int MODE_SHOOTING = 1;
	public static final int MODE_PLACING = 2;

	public BallController(Ball ball){
		this.ball = ball;

		this.xPressed = -1; this.yPressed = -1;
		this.xHeld = -1; this.yHeld = -1;
		this.xReleased = -1; this.yReleased = -1;
		this.xMoved = -1; this.yMoved = -1;

		this.mode = BallController.MODE_NONE;
	}

	/**
	 * Updates where the mouse was last pressed.
	 * 
	 * @param x x coordinate where the mouse was last pressed
	 * @param y y coordinate where the mouse was last pressed
	 */
	public void pressMouse(int x, int y){
		this.xPressed = x; this.yPressed = y;
	}

	/**
	 * Updates where the mouse was last held.
	 * 
	 * @param x x coordinate where the mouse was last held
	 * @param y y coordinate where the mouse was last held
	 */
	public void holdMouse(int x, int y){
		this.xHeld = x; this.yHeld = y;
	}

	/**
	 * Updates where the mouse was last released.
	 * 
	 * @param x x coordinate where the mouse was last released
	 * @param y y coordinate where the mouse was last released
	 */
	public void releaseMouse(int x, int y){
		this.xReleased = x; this.yReleased = y;
	}

	/**
	 * Updates where the mouse was last moved.
	 * 
	 * @param x x coordinate where the mouse was last moved
	 * @param y y coordinate where the mouse was last moved
	 */
	public void moveMouse(int x, int y){
		this.xMoved = x; this.yMoved = y;
	}

	/**
	 * Resets the last pressed, held, released and moved mouse coordinates to (-1, -1) each.
	 */
	public void resetMouse(){
		this.pressMouse(-1, -1);
		this.holdMouse(-1, -1);
		this.releaseMouse(-1, -1);
		this.moveMouse(-1, -1);
	}

	/**
	 * Fires the ball based on how far back the mouse was dragged, given some scaling parameter.
	 * 
	 * @param scale Ratio between the size of the canvas and the size of the GameState the ball is in.
	 */
	public void shootBall(double scale){
		ball.xVel = (this.xPressed - this.xReleased) * 5 / scale;
		ball.yVel = (this.yPressed - this.yReleased) * 5 / scale;
	}

	/**
	 * Places the ball down at the spot the user last clicked on the canvas and marks the ball as not sunk.
	 * Also takes in scaling and offset parameters to ensure the ball is placed accurately within its GameState.
	 * 
	 * @param   scale Ratio between the size of the canvas and the size of the GameState the ball is in.
	 * @param xOffset Offset along the x-axis between the canvas and the GameState the ball is in.
	 * @param yOffset Offset along the y-axis between the canvas and the GameState the ball is in.
	 */
	public void placeBall(double scale, double xOffset, double yOffset){
		ball.xPos = (this.xPressed - xOffset)/scale; ball.xVel = 0;
		ball.yPos = (this.yPressed - yOffset)/scale; ball.yVel = 0;
		ball.sunk = false;
	}
}