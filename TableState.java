import java.awt.*;
import java.util.*;

/**
 * Represents a Table surface as well as the various Balls which are on top of it.
 */
class TableState {
	public final int w, h;
	private final double friction;
	private ArrayList<Ball> balls; private Ball cueBall; private Ball eightBall;
	public boolean moving; public int[] movingByType;
	public int[] sunkByType;
	private ArrayList<Wall> walls;
	private ArrayList<Pocket> pockets;

	public TableState(int w, int h){
		this.w = w; this.h = h; 
		this.friction = 200;
		
		this.balls = new ArrayList<Ball>();
		this.walls = new ArrayList<Wall>();
		this.pockets = new ArrayList<Pocket>();

		this.moving = false; this.movingByType = new int[4];
		this.sunkByType = new int[4];
	}

	/** 
	 * Adds a new ball onto the table. 
	 * Will not add another cue ball or 8 ball to the table if one already exists.
	 * 
	 * @param ball The ball that we're going to add to the table.
	 */
	public void addBall(Ball ball){
		if (ball.type == Ball.TYPE_CUEBALL && this.cueBall != null){
			System.out.println("Cue ball already exists in this TableState.");
		}
		else if (ball.type == Ball.TYPE_8BALL && this.eightBall != null){
			System.out.println("8 ball already exists in this TableState.");
		}
		else {
			// make this ball the cueball/8ball if there isn't one already
			this.cueBall = (ball.type == Ball.TYPE_CUEBALL) ? ball : this.cueBall;
			this.eightBall = (ball.type == Ball.TYPE_8BALL) ? ball : this.eightBall;
			this.balls.add(ball);
		}
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
	 * Gets a ball from the table. 
	 * 
	 * @param i The index of the ball we're trying to get.
	 * @return  The ith ball in this.balls.
	 */
	public Ball getBall(int i){
		return this.balls.get(i);
	}

	/** 
	 * Gets the cue ball from the table.
	 * 
	 * @return The cue ball if it's on the table, null otherwise.
	 */
	public Ball getCueBall(){
		return this.cueBall;
	}

	/** 
	 * Gets the 8 ball from the table.
	 * 
	 * @return The 8 ball if it's on the table, null otherwise.
	 */
	public Ball get8Ball(){
		return this.eightBall;
	}

	/** 
	 * Gets a wall from the table. 
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
	 * Also handles inter-ball collisions, wall collisions and pocket detection for each of the balls as they move.
	 * 
	 * @param time the amount of time, in seconds, that all the balls are moved forward
	 */
	public void moveTime(double time){
		this.moving = false; this.movingByType = new int[4];
		this.sunkByType = new int[4];

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

			for(int p = 0; p < pockets.size(); p++){
				CollisionHandler.handlePocketCollisions(getBall(i), getPocket(p), this.friction, 0.95);
			}

			// update moving to track whether any non-sunken balls are moving or not
			//     (good for checking if balls are still prior to taking another shot; avoids iterating through movingByType to check)
			//     (waiting for sunken balls to stop moving is tedious though, so ignore those)
			// update movingByType so we can keep track of whether a ball of its type is moving or not
			//     (good for detecting fouls in which a player fails to hit a ball in their group first)
			if ((getBall(i).xVel != 0 || getBall(i).yVel != 0) && !getBall(i).sunk){
				this.movingByType[getBall(i).type] += 1;
				this.moving = true;
			}

			// update this.sunk accordingly if the ball is sunk
			if (getBall(i).sunk){
				this.sunkByType[getBall(i).type] += 1;
			}
		}
	}

	/**
	 * Determines where a particular ball will collide with some other wall or ball or sink
	 * into some other pocket on the table, provided that it's moving at some given velocity.
	 * 
	 * @param ball the ball that we're determining the next collision point for
	 * @param xVel the speed of the ball along the x axis
	 * @param yVel the speed of the ball along the y axis
	 * @return     an array of doubles containing the x coordinate and y coordinate of the collision point in that order
	 */
	public double[] nextCollisionPoint(Ball ball, double xVel, double yVel){
		// make a ghost ball with the parameters we're looking at
		// also set the sunken state to the ball's sunken state
		Ball ghost = new Ball(ball.radius, ball.type, ball.xPos, ball.yPos, xVel, yVel);
		ghost.sunk = ball.sunk;

		do {
			// check if it collides with any of the balls that isn't itself
			for (int b = 0; b < balls.size(); b++){
				if (getBall(b) != ball){
					if (getBall(b).distanceFrom(ghost) < 0 && !ghost.sunk && !getBall(b).sunk){
						return new double[]{ ghost.xPos, ghost.yPos }; // return the first collision it can find
					}
				}
			}

			// check if it collides with any of the walls
			for (int w = 0; w < walls.size(); w++){
				if (getWall(w).isBallColliding(ghost) < 0 && (ghost.sunk == getWall(w).sunk || getWall(w).always)){
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

			// if we didn't return earlier, then move the ghost ball forward 1 millisecond in time
			ghost.moveTime(0.001, this.friction);
		} while (ghost.xPos >= 0 && ghost.xPos < w && ghost.yPos >= 0 && ghost.yPos < h && ghost.getVelocity() > 0); // while this ghost ball is inbounds and moving

		// if the ghost ball got out of bounds or stopped moving, return some placeholder value that doesn't matter
		return new double[]{ -1000, -1000 };
	}

	/**
	 * Draws all the Balls, Walls and Pockets that are in the TableState onto a Graphics object.
	 * Scaling and offset parameters should be set by GameState.draw() automatically.
	 * 
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 */
	public void drawObjects(Graphics g, double scale, double xOffset, double yOffset){
		for (int i = 0; i < pockets.size(); i++){
			getPocket(i).drawPocket(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < walls.size(); i++){
			getWall(i).drawWall(g, scale, xOffset, yOffset);
		}

		for (int i = 0; i < balls.size(); i++) {
			getBall(i).drawBall(g, scale, xOffset, yOffset);
		}
	}

	/**
	 * Fills a polygon on a Graphics object with a given color based on some walls that bound it.
	 * Scaling and offset parameters should be set by GameState.draw() automatically.
	 * 
	 * @param g       the Graphics object being drawn onto
	 * @param scale   the factor to increase the size of the filled polygon
	 * @param xOffset the amount of pixels to offset the filled polygon by on the x axis
	 * @param yOffset the amount of pixels to offset the filled polygon by on the y axis
	 * @param color   the color the polygon is going to be filled with
	 * @param walls   the walls that define the boundaries of the polygon
	 */
	public void fillPolygon(Graphics g, double scale, double xOffset, double yOffset, Color color, ArrayList<Wall> walls){
		int nPoints = (walls.size()) * 2;
		int[] xPoints = new int[nPoints]; int[] yPoints = new int[nPoints];

		for(int i = 0; i < nPoints/2; i++){
			Wall w = walls.get(i);
			// adjust x1, y1, x2 and y2 based on scale, xOffset and yOffset for isotropic scaling
			xPoints[i*2] = (int)(w.x1 * scale + xOffset); xPoints[i*2 + 1] = (int)(w.x2 * scale + xOffset);
			yPoints[i*2] = (int)(w.y1 * scale + yOffset); yPoints[i*2 + 1] = (int)(w.y2 * scale + yOffset);
		}

		g.setColor(color);
		g.fillPolygon(xPoints, yPoints, nPoints);
	}

	/**
	 * Fills a polygon on a Graphics object with a given color based on the indices of some walls in this.walls that bound it.
	 * Scaling and offset parameters should be set by GameState.draw() automatically.
	 * 
	 * @param g       the Graphics object being drawn onto
	 * @param scale   the factor to increase the size of the filled polygon
	 * @param xOffset the amount of pixels to offset the filled polygon by on the x axis
	 * @param yOffset the amount of pixels to offset the filled polygon by on the y axis
	 * @param color   the color the polygon is going to be filled with
	 * @param indices the index of the walls in this.walls that define the boundaries of the polygon
	 */
	public void fillPolygon(Graphics g, double scale, double xOffset, double yOffset, Color color, int[] indices){
		ArrayList<Wall> w = new ArrayList<Wall>();

		for (int i = 0; i < indices.length; i++){
			w.add(this.getWall(indices[i]));
		}

		fillPolygon(g, scale, xOffset, yOffset, color, w);
	}

	/**
	 * Draws the velocity vector (scaled down by a factor of 10) as well as the next
	 * collision point for the cue ball assuming it will have a velocity of (xVel, yVel).
	 * This method does not draw any of the objects on the table and should be called after TableState.draw().
	 * Scaling and offset parameters should be set by GameState.drawShotPreview() automatically.
	 * 
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 * @param    xVel the velocity along the x-axis the cue ball will be moving at
	 * @param    yVel the velocity along the y-axis the cue ball will be moving at
	 */
	public void drawShotPreview(Graphics g, double scale, double xOffset, double yOffset, double xVel, double yVel){
		if (this.cueBall != null){
			// gets position and radius of cue ball
			double xPos = this.cueBall.xPos; double yPos = this.cueBall.yPos; double radius = this.cueBall.radius;

			// draws the new velocity vector of the cue ball as a Wall
			// kinda weird, but we can leverage a lot of the busy work with scaling from Wall.drawWall() this way
			Wall velocity = new Wall(xPos, yPos, xPos+xVel/10, yPos+yVel/10); velocity.setColor(255, 0, 0);
			velocity.drawWall(g, scale, xOffset, yOffset);

			// determines where the collision point of the cue ball would be
			double[] pos = this.nextCollisionPoint(this.cueBall, xVel, yVel);

			// applies isotropic scaling to that point and the radius of the cue ball
			double x = pos[0]*scale + xOffset; double y = pos[1]*scale + yOffset;
			radius = radius*scale;

			// draws where the cue ball would be at that collision point
			g.drawOval((int)(x-radius), (int)(y-radius), (int)(2*radius), (int)(2*radius));
		}
	}

	// TODO: this only works with the cueball at the moment, but we need to be able to replace the 8 ball in some instances.
	/**
	 * Draws a preview of where the cue ball would be located, given the user clicks at the coordinate (xPos, yPos) on the canvas.
	 * Will also return whether the cue ball can be placed in that location or not based on TableState.nextCollisionPoint().
	 * This method does not draw any of the objects on the table and should be called after GameState.draw().
	 * Scaling and offset parameters should be set by GameState.drawPlacePreview() automatically.
	 * 
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 * @param    xPos x coordinate of where the user would place the cue ball on the canvas
	 * @param    yPos y coordinate of where the user would place the cue ball on the canvas
	 */
	public boolean drawPlacePreview(Graphics g, double scale, double xOffset, double yOffset, double xPos, double yPos){
		if (this.cueBall != null){
			double x = (xPos-xOffset)/scale; double y = (yPos-yOffset)/scale;
			Ball preview = new Ball(this.cueBall.radius, Ball.TYPE_CUEBALL, x, y);

			double cueX = this.cueBall.xPos; double cueY = this.cueBall.yPos; // make a backup of the cueball's position
			this.cueBall.xPos = -1000; this.cueBall.yPos = -1000;             // move the cueball somewhere we shouldn't be able to place it
			double[] collision = this.nextCollisionPoint(preview, 0, 0);      // now we can check if this placement collides with anything except the cueball
			this.cueBall.xPos = cueX; this.cueBall.yPos = cueY;               // and restore the cueball's position afterwards
			                                                                  // kinda hacky, but it works
			
			double radius = this.cueBall.radius*scale;
			g.setColor(Color.red); g.drawOval((int)(xPos-radius), (int)(yPos-radius), (int)(2*radius), (int)(2*radius));

			// keep track of if the cue ball doesn't collide with anything in this spot, as well as if it's in bounds
			boolean valid = (collision[0] == -1000 && (x>=0 && x<w && y>=0 && y<h));

			if (!valid){
				g.setColor(new Color(255, 0, 0, 127));
				g.fillOval((int)(xPos-radius), (int)(yPos-radius), (int)(2*radius), (int)(2*radius));
			}

			return valid;
		}
		return false;
	}
}