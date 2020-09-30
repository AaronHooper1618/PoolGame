import java.awt.*;
import java.util.*;

// TODO: should this just be TableState and we keep track of GameState (players/turn/fouls/etc.) in another class?
/**
 * Represents a Game surface as well as the various Balls which are on top of it.
 */
class GameState {
	public final int w, h;
	private final double friction;
	private Ball[] balls;
	private Wall[] walls;

	public GameState(){
		w = 800; h = 600; friction = 200;
		balls = new Ball[16];
		int i = 0;

		this.balls[0] = new Ball(20, 200, 280); i++;
		this.balls[0].setColor(235, 240, 209);

		for(int j = 0; j < 6; j++){
			for(int k = 0; k < j; k++){
				this.balls[i] = new Ball(20, 400+j*37, 300+k*40-j*20);
				i++;
			}
		}

		walls = new Wall[13];
		// bounding walls
		this.walls[0] = new Wall(0, 0, 0, 599);
		this.walls[1] = new Wall(0, 599, 799, 599);
		this.walls[2] = new Wall(799, 599, 799, 0);
		this.walls[3] = new Wall(799, 0, 0, 0);

		// angled wall
		this.walls[4] = new Wall(599, 599, 799, 300);

		// enclosed space (left box; ccw orientation)
		this.walls[5] = new Wall(200, 400, 200, 480);
		this.walls[6] = new Wall(200, 480, 280, 480);
		this.walls[7] = new Wall(280, 480, 280, 400);
		this.walls[8] = new Wall(280, 400, 200, 400);

		// closed off space (right box; cw orientation)
		this.walls[9] = new Wall(400, 400, 480, 400);
		this.walls[10] = new Wall(480, 400, 480, 480);
		this.walls[11] = new Wall(480, 480, 400, 480);
		this.walls[12] = new Wall(400, 480, 400, 400);
	}

	/**
	 * Replaces a ball in this.balls with another ball passed in as a parameter.
	 * 
	 * @param i The index of the ball in this.balls which we're replacing.
	 * @param ball The ball that we're going to replace this.balls[i] with.
	 */
	public void replaceBall(int i, Ball ball){
		if(i < this.balls.length){
			this.balls[i] = ball;
		}
	}


	/**
	 * Moves all the Balls around a certain amount of time.
	 * Also handles inter-ball collisions and wall collisions for each of the balls as they move.
	 * 
	 * @param time the amount of time, in seconds, that all the balls are moved forward
	 */
	public void moveTime(double time){
		// shuffle the ball ordering around
		int[] ball_order = new int[balls.length];
		for (int i = 0; i < ball_order.length; i++) {ball_order[i] = i;}
		Collections.shuffle(Arrays.asList(ball_order));
		
		// move the balls
		for (int b = 0; b < ball_order.length; b++) {
			int i = ball_order[b];
			balls[i].moveTime(time, this.friction);
			
			// handle collisions between ball i and every other ball
			for (int j = 0; j < balls.length; j++) {
				if (i != j){ // dont check for collision with itself
					CollisionHandler.handleBallCollisions(balls[i], balls[j], this.friction, 0.95);
				}
			}

			// shuffle the wall ordering around
			int[] wall_order = new int[walls.length];
			for (int j = 0; j < wall_order.length; j++) {wall_order[j] = j;}
			Collections.shuffle(Arrays.asList(wall_order));

			for(int w = 0; w < wall_order.length; w++){
				int j = wall_order[w];
				CollisionHandler.handleWallCollisions(balls[i], walls[j], this.friction, 0.95);
			}
		}
	}

	/**
	 * Draws all the Balls and Walls that are in the GameState onto a Graphics object.
	 * Will also determine scale, xOffset and yOffset in advance in order to handle
	 * anisotropic scaling based on the width and height of the canvas.
	 * 
	 * @param g the Graphics object being drawn onto
	 * @param w the width of the canvas being drawn onto
	 * @param h the height of the canvas being drawn onto
	 */
	public void draw(Graphics g, int w, int h){
		// calculate scale, xOffset and yOffset for anisotropic scaling
		double scale = Math.min((double)w/this.w, (double)h/this.h);
		double xOffset = (w - this.w*scale)/2;
		double yOffset = (h - this.h*scale)/2;

		for (int i = 0; i < balls.length; i++) {
			balls[i].drawBall(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < walls.length; i++){
			walls[i].drawWall(g, scale, xOffset, yOffset);
		}
	}
}