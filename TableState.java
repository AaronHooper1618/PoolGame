import java.awt.*;
import java.util.*;

/**
 * Represents a Table surface as well as the various Balls which are on top of it.
 */
class TableState {
	public final int w, h;
	private final double friction;
	private ArrayList<Ball> balls;
	private ArrayList<Wall> walls;

	public TableState(int w, int h){
		this.w = w; this.h = h; 
		this.friction = 200;
		
		this.balls = new ArrayList<Ball>();
		this.walls = new ArrayList<Wall>();
	}

	/** 
	 * Adds a new ball onto the table. 
	 * 
	 * @param ball The ball that we're going to add to the table.
	 */
	public void addBall(Ball ball){
		this.balls.add(ball);
	}

	/** 
	 * Adds a new wall onto the table. 
	 * 
	 * @param wall The wall that we're going to add to the table.
	 */
	public void addWall(Wall wall){
		this.walls.add(wall);
	}

	/** 
	 * Adds a new ball onto the table. 
	 * 
	 * @param i The index of the ball we're trying to get.
	 * @return  The ith ball in this.balls.
	 */
	public Ball getBall(int i){
		return this.balls.get(i);
	}

	/** 
	 * Adds a new wall onto the table. 
	 * 
	 * @param i The index of the wall we're trying to get.
	 * @return  The ith wall in this.walls.
	 */
	public Wall getWall(int i){
		return this.walls.get(i);
	}

	/**
	 * Replaces a ball in this.balls with another ball passed in as a parameter.
	 * 
	 * @param i The index of the ball in this.balls which we're replacing.
	 * @param ball The ball that we're going to replace this.balls[i] with.
	 */
	public void replaceBall(int i, Ball ball){
		this.balls.set(i, ball);
	}

	/**
	 * Moves all the Balls around a certain amount of time.
	 * Also handles inter-ball collisions and wall collisions for each of the balls as they move.
	 * 
	 * @param time the amount of time, in seconds, that all the balls are moved forward
	 */
	public void moveTime(double time){
		// shuffle the ball ordering around
		int[] ball_order = new int[balls.size()];
		for (int i = 0; i < ball_order.length; i++) {ball_order[i] = i;}
		Collections.shuffle(Arrays.asList(ball_order));
		
		// move the balls
		for (int b = 0; b < ball_order.length; b++) {
			int i = ball_order[b];
			getBall(i).moveTime(time, this.friction);
			
			// handle collisions between ball i and every other ball
			for (int j = 0; j < balls.size(); j++) {
				if (i != j){ // dont check for collision with itself
					CollisionHandler.handleBallCollisions(getBall(i), getBall(j), this.friction, 0.95);
				}
			}

			// shuffle the wall ordering around
			int[] wall_order = new int[walls.size()];
			for (int j = 0; j < wall_order.length; j++) {wall_order[j] = j;}
			Collections.shuffle(Arrays.asList(wall_order));

			for(int w = 0; w < wall_order.length; w++){
				int j = wall_order[w];
				CollisionHandler.handleWallCollisions(getBall(i), getWall(j), this.friction, 0.95);
			}
		}
	}

	/**
	 * Determines where a ball of a given radius at a certain position with a certain velocity
	 * will collide with some other wall or ball on the table.
	 * 
	 * @param radius the radius of the ball
	 * @param   xPos the x position of the ball
	 * @param   yPos the y position of the ball
	 * @param   xVel the speed of the ball along the x axis
	 * @param   yVel the speed of the ball along the y axis
	 * @return       an array of doubles containing the x coordinate and y coordinate of the collision point in that order
	 */
	public double[] nextCollisionPoint(int radius, double xPos, double yPos, double xVel, double yVel){
		// make a ghost ball with the parameters we're looking at
		Ball ghost = new Ball(radius, xPos, yPos, xVel, yVel);

		// while this ball is inbounds and moving
		while(ghost.xPos >= 0 && ghost.xPos < w && ghost.yPos >= 0 && ghost.yPos < h && ghost.getVelocity() > 0){
			// check if it collides with any of the balls that isn't the cue ball
			// TODO: ignoring the cue-ball is hard coded in by checking every ball in TableState.balls from index 1 onwards
			//       this function was written very hastily and needs to be refactored to fix this
			for (int b = 1; b < balls.size(); b++){
				if (getBall(b).distanceFrom(ghost) < 0){
					return new double[]{ ghost.xPos, ghost.yPos }; // return the first collision it can find
				}
			}

			// check if it collides with any of the walls
			for (int w = 0; w < walls.size(); w++){
				if (getWall(w).isBallColliding(ghost) < 0){
					return new double[]{ ghost.xPos, ghost.yPos }; // return the first collision it can find
				}
			}

			// if we didn't return earlier, then move the ball forward 1 millisecond in time
			ghost.moveTime(0.001, this.friction);
		}

		// if the ball got out of bounds or stopped moving, return some placeholder value that doesn't matter
		return new double[]{ -1000, -1000 };
	}

	/**
	 * Draws all the Balls and Walls that are in the GameState onto a Graphics object.
	 * Will also determine scale, xOffset and yOffset in advance in order to handle
	 * isotropic scaling based on the width and height of the canvas.
	 * 
	 * @param g the Graphics object being drawn onto
	 * @param w the width of the canvas being drawn onto
	 * @param h the height of the canvas being drawn onto
	 */
	public void draw(Graphics g, int w, int h){
		// calculate scale, xOffset and yOffset for isotropic scaling
		double scale = Math.min((double)w/this.w, (double)h/this.h);
		double xOffset = (w - this.w*scale)/2;
		double yOffset = (h - this.h*scale)/2;

		for (int i = 0; i < balls.size(); i++) {
			getBall(i).drawBall(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < walls.size(); i++){
			getWall(i).drawWall(g, scale, xOffset, yOffset);
		}
	}
}