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
	private ArrayList<Pocket> pockets;

	public TableState(int w, int h){
		this.w = w; this.h = h; 
		this.friction = 200;
		
		this.balls = new ArrayList<Ball>();
		this.walls = new ArrayList<Wall>();
		this.pockets = new ArrayList<Pocket>();
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
	 * Adds a new pocket onto the table. 
	 * 
	 * @param pocket The pocket that we're going to add to the table.
	 */
	public void addPocket(Pocket pocket){
		this.pockets.add(pocket);
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
	 * Gets a pocket from the table. 
	 * 
	 * @param i The index of the pocket we're trying to get.
	 * @return  The ith pocket in this.pockets.
	 */
	public Pocket getPocket(int i){
		return this.pockets.get(i);
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

			// make a backup of the ball for later
			Ball backup = new Ball(0, getBall(i).xPos, getBall(i).yPos, getBall(i).xVel, getBall(i).yVel);

			// shuffle the wall ordering around
			int[] wall_order = new int[walls.size()];
			for (int j = 0; j < wall_order.length; j++) {wall_order[j] = j;}
			Collections.shuffle(Arrays.asList(wall_order));

			for(int w = 0; w < wall_order.length; w++){
				int j = wall_order[w];
				CollisionHandler.handleWallCollisions(getBall(i), getWall(j), this.friction, 0.95);
			}

			boolean in_pocket = false;
			for(int p = 0; p < pockets.size(); p++){
				in_pocket = in_pocket || getPocket(p).ballInPocket(getBall(i)); // keep track of if the ball is in a pocket or not after handling wall collisions
				CollisionHandler.handlePocketCollisions(getBall(i), getPocket(p));
			}

			if (!in_pocket && getBall(i).sunk){ // if the ball's not in a pocket even though it's sunk...
				// ...revert the position and velocity back to what it was before this step via the backup Ball we made earlier
				//    inaccurate but strict way to ensure that the ball stays in the pocket
				getBall(i).xPos = backup.xPos; getBall(i).yPos = backup.yPos;
				getBall(i).xVel = backup.xVel; getBall(i).yVel = backup.xVel;
			}
		}
	}

	// TODO: maybe change the parameters to (Ball ball, double xVel, double yVel)?
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
		// also sets the sunken state to the cueball's sunken state
		//     TODO: cue-ball position is hard-coded in here; refactor to fix this
		Ball ghost = new Ball(radius, xPos, yPos, xVel, yVel);
		ghost.sunk = getBall(0).sunk;

		// while this ball is inbounds and moving
		while(ghost.xPos >= 0 && ghost.xPos < w && ghost.yPos >= 0 && ghost.yPos < h && ghost.getVelocity() > 0){
			// check if it collides with any of the balls that isn't the cue ball
			// TODO: ignoring the cue-ball is hard coded in by checking every ball in TableState.balls from index 1 onwards
			//       this function was written very hastily and needs to be refactored to fix this
			for (int b = 1; b < balls.size(); b++){
				if (getBall(b).distanceFrom(ghost) < 0 && !ghost.sunk && !getBall(b).sunk){
					return new double[]{ ghost.xPos, ghost.yPos }; // return the first collision it can find
				}
			}

			// check if it collides with any of the walls
			for (int w = 0; w < walls.size(); w++){
				if (getWall(w).isBallColliding(ghost) < 0 && ghost.sunk == getWall(w).sunk){
					return new double[]{ ghost.xPos, ghost.yPos }; // return the first collision it can find
				}
			}

			// check if it sinks into any of the pockets if the ball isn't sunk already
			if (!ghost.sunk){
				for (int p = 0; p < pockets.size(); p++){
					if (getPocket(p).ballInPocket(ghost)){
						return new double[]{ ghost.xPos, ghost.yPos }; // return the first sunken into pocket it can find
					}
				}
			}

			// if we didn't return earlier, then move the ball forward 1 millisecond in time
			ghost.moveTime(0.001, this.friction);
		}

		// if the ball got out of bounds or stopped moving, return some placeholder value that doesn't matter
		return new double[]{ -1000, -1000 };
	}

	/**
	 * Draws all the Balls and Walls that are in the TableState onto a Graphics object.
	 * Scaling and offset parameters should be set by GameState.draw() automatically.
	 * 
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 */
	public void draw(Graphics g, double scale, double xOffset, double yOffset){
		for (int i = 0; i < pockets.size(); i++){
			getPocket(i).drawPocket(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < balls.size(); i++) {
			getBall(i).drawBall(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < walls.size(); i++){
			getWall(i).drawWall(g, scale, xOffset, yOffset);
		}
	}

	/**
	 * Draws the velocity vector (scaled down by a factor of 10) as well as the next
	 * collision point for the cue ball assuming it will have a velocity of (xVel, yVel).
	 * This method does not draw any of the objects on the table and should be called after TableState.draw().
	 * Scaling and offset parameters should be set by GameState.drawMovePreview() automatically.
	 * 
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 * @param    xVel the velocity along the x-axis the cue ball will be moving at
	 * @param    yVel the velocity along the y-axis the cue ball will be moving at
	 */
	public void drawMovePreview(Graphics g, double scale, double xOffset, double yOffset, double xVel, double yVel){
		// TODO: it's assumed that the cue ball will be at index 0 in this function. rewrite this to get rid of that assumption?
		// gets position and radius of cue ball
		double xPos = this.getBall(0).xPos; double yPos = this.getBall(0).yPos; int radius = this.getBall(0).radius;

		// draws the new velocity vector of the cue ball as a Wall
		// kinda weird, but we can leverage a lot of the busy work with scaling from Wall.drawWall() this way
		Wall velocity = new Wall(xPos, yPos, xPos+xVel/10, yPos+yVel/10); velocity.setColor(255, 0, 0);
		velocity.drawWall(g, scale, xOffset, yOffset);

		// determines where the collision point of the cue ball would be
		double[] pos = this.nextCollisionPoint(radius, xPos, yPos, xVel, yVel);

		// applies isotropic scaling to that point and the radius of the cue ball
		double x = pos[0]*scale + xOffset; double y = pos[1]*scale + yOffset;
		radius = (int)(radius*scale);

		// draws where the cue ball would be at that collision point
		g.drawOval((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
	}
}