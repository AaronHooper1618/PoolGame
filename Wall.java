import java.awt.*;

/**
 * Represents a wall that balls are able to collide with.
 */
public class Wall {
	public final double x1, y1; // first endpoint
	public final double x2, y2; // second endpoint
	public final double length;
	public final double angle;
	public final boolean sunk, always; // TODO: are these necessary now that pockets aren't made up of walls anymore?
	private int r, g, b;

	/**
	 * Creates a wall which extends across 2 endpoints.
	 * 
	 * @param x1 the x coordinate for the first endpoint
	 * @param y1 the y coordinate for the first endpoint
	 * @param x2 the x coordinate for the second endpoint
	 * @param y2 the y coordinate for the second endpoint
	 */
	public Wall(double x1, double y1, double x2, double y2){
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;

		double dx = this.x2 - this.x1; double dy = this.y2 - this.y1;
		this.length = Math.sqrt(dx*dx + dy*dy);
		this.angle = Math.atan2(dy, dx);

		this.sunk = false;
		this.always = false;
	}

	/**
	 * Creates a wall which extends across 2 endpoints.
	 * 
	 * @param     x1 the x coordinate for the first endpoint
	 * @param     y1 the y coordinate for the first endpoint
	 * @param     x2 the x coordinate for the second endpoint
	 * @param     y2 the y coordinate for the second endpoint
	 * @param   sunk determines whether the wall is sunken or not. only sunken balls are able to collide with sunken walls.
	 * @param always determines whether the wall can always be collided against, regardless of whether the ball is sunken or not.
	 */
	public Wall(double x1, double y1, double x2, double y2, boolean sunk, boolean always){
		this.x1 = x1; this.y1 = y1;
		this.x2 = x2; this.y2 = y2;

		double dx = this.x2 - this.x1; double dy = this.y2 - this.y1;
		this.length = Math.sqrt(dx*dx + dy*dy);
		this.angle = Math.atan2(dy, dx);

		this.sunk = sunk;
		this.always = always;
	}

	/**
	 * Sets the color of the wall given some RGB values.
	 * These values should be within the range 0-255 inclusive.
	 * 
	 * @param r The red component of the color.
	 * @param g The blue component of the color.
	 * @param b The green component of the color.
	 */
	public void setColor(int r, int g, int b){
		this.r = r; this.g = g; this.b = b;
	}

	// TODO: the value that this method returns isn't entirely consistent. needs to be rectified if we're going to make wall collisions accurate.
	/**
	 * Determines if a Ball is colliding with this Wall.
	 * If this is less than 0, the Ball is colliding with the Wall.
	 * 
	 * @param ball the Ball that we're determining the distance from
	 * @return     the distance from the wall to the ball's edge
	 */
	public double isBallColliding(Ball ball){
		// determine if the ball is already too far away on either the x-axis or y-axis to even touch the line in the first place
		// return 1 (not less than 0) if this is the case
		if (ball.xPos > Math.max(this.x1, this.x2) + ball.radius){return 1;} // too far right
		if (ball.xPos < Math.min(this.x1, this.x2) - ball.radius){return 1;} // too far left

		if (ball.yPos > Math.max(this.y1, this.y2) + ball.radius){return 1;} // too low
		if (ball.yPos < Math.min(this.y1, this.y2) - ball.radius){return 1;} // too high

		double a = -this.angle;

		// rotate the wall by -this.angle so its lying on the x-axis
		double xa = this.x1*Math.cos(a) - this.y1*Math.sin(a);
		double ya = this.x1*Math.sin(a) + this.y1*Math.cos(a);
		double xb = this.x2*Math.cos(a) - this.y2*Math.sin(a);
		// yb would be the same as ya; no need to calculate it here

		// rotate the ball along -this.angle accordingly
		double x_ball = ball.xPos*Math.cos(a) - ball.yPos*Math.sin(a);
		double y_ball = ball.xPos*Math.sin(a) + ball.yPos*Math.cos(a);

		// find how far away the ball is on the x axis
		//                  Math.max([distance from right side], [distance from left side])
		double x_distance = Math.max(x_ball - Math.max(xa, xb), Math.min(xa, xb) - x_ball);
		x_distance = Math.max(x_distance, 0);

		// find how far away the ball is on the y axis
		double y_distance = ya - (y_ball + ball.radius);

		if (Math.abs(y_distance) < ball.radius*2){ // ...if the ball's within the wall on the y-axis...
			if (x_distance < ball.radius/2){ // ...and within the wall on the x-axis...
				// ...return the amount of distance the ball has collided with the wall
				return y_distance + Math.sin(x_distance/ball.radius)*ball.radius;
			}
			else { // if it's not in that x coordinate range...
				return x_distance; // return how far away it is on the x axis.
			}
		}
		else { // otherwise...
			return Math.abs(y_distance); // ...return the absolute (not colliding) value of it
		}
	}

	/**
	 * Draws the Wall onto a Graphics object. Also supports isotropic scaling and offsetting.
	 * The parameters of this method should be determined automatically by some other method.
	 *
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 */
	public void drawWall(Graphics g, double scale, double xOffset, double yOffset){
		int xa = (int)(this.x1*scale + xOffset); int ya = (int)(this.y1*scale + yOffset);
		int xb = (int)(this.x2*scale + xOffset); int yb = (int)(this.y2*scale + yOffset);
		
		Color wallColor = new Color(this.r, this.g, this.b);
		g.setColor(wallColor); g.drawLine(xa, ya, xb, yb);
	}
}
