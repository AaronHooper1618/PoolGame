import java.awt.*;

/**
 * Represents a ball that can move around in 2-dimensional space.
 */
class Ball {
	public final int radius; 
	public final double mass;

	public final int type;
	public static final int TYPE_CUEBALL = 0;
	public static final int TYPE_RED = 1;
	public static final int TYPE_BLUE = 2;
	public static final int TYPE_8BALL = 3;

	public double xPos, yPos;
	public double xVel, yVel;
	public boolean sunk;
	private int r, g, b;

	// TODO: this is way too many constructors lol
	//       either figure out how to organize this or mark which ones we're using and decide whether to cut the rest
	public Ball(int radius, int type, double mass, double xPos, double yPos, double xVel, double yVel){  // unused
		this.radius = radius; this.type = type; this.mass = mass;
		this.xPos = xPos; this.yPos = yPos;
		this.xVel = xVel; this.yVel = yVel;
		this.setColor(0, 0, 200);
		this.sunk = false;
	}

	public Ball(int radius, int type, double xPos, double yPos, double xVel, double yVel){ // used in TableState.nextCollisionPoint()
		this.radius = radius; this.type = type; this.mass = 50.0;
		this.xPos = xPos; this.yPos = yPos;
		this.xVel = xVel; this.yVel = yVel;
		this.setColor(0, 0, 200);
		this.sunk = false;
	}
	
	public Ball(int radius, int type, double mass, double xPos, double yPos){ // unused
		this.radius = radius; this.type = type; this.mass = mass;
		this.xPos = xPos; this.yPos = yPos;
		this.setColor(0, 0, 200);
		this.sunk = false;
	}

	public Ball(int radius, int type, double xPos, double yPos){ // used in GameState constructor
		this.radius = radius; this.type = type; this.mass = 50.0;
		this.xPos = xPos; this.yPos = yPos;
		this.setColor(0, 0, 200);
		this.sunk = false;
	}

	/**
	 * Sets the color of the ball given some RGB values.
	 * These values should be within the range 0-255 inclusive.
	 * 
	 * @param r The red component of the color.
	 * @param g The blue component of the color.
	 * @param b The green component of the color.
	 */
	public void setColor(int r, int g, int b){
		this.r = r; this.g = g; this.b = b;
	}

	/**
	 * Returns how quickly the ball is moving, independently of angle. 
	 * Essentially just sqrt(xVel^2 + yVel^2).
	 * 
	 * @return the magnitude of the ball's velocity
	 */
	public double getVelocity(){
		return Math.sqrt(this.xVel*this.xVel + this.yVel*this.yVel);
	}

	/**
	 * Returns the angle that the ball is moving in. 
	 * Essentially just calls Math.atan2().
	 * 
	 * @return the angle of the ball's velocity in radians 
	*/
	public double getAngle(){
		return Math.atan2(this.yVel, this.xVel);
	}

	/**
	 * Updates the position and velocity of the ball under the assumption that the ball
	 * has moved forward a certain amount of time on a surface with some amount of friction.
	 *
	 * @param     time the amount of time, in seconds, that the ball is moved forward
	 * @param friction the rate at which velocity decreases over time (velocity decreases by 1*friction every second)
	 */
	public void moveTime(double time, double friction){
		// get the velocity and its angle
		double velocity = this.getVelocity(); double angle = this.getAngle();

		// v0 = initial velocity; f = friction; t = time
		// velocity = v0 - f*t

		// we dont want this to go on to the point where our velocity ends up negative somehow, so...
		// 0 = v0 - f*t; solve for t -> t = v0/f
		if (friction > 0) {
			time = Math.min(time, velocity/friction);
		}

		// distance = ∫velocity dt 
		//          = ∫(v0 - f*t) dt 
		//          = t*v0 - (f*t^2)/2
		double distance = time*velocity - (friction*time*time)/2.0;

		// add the distance traveled to both xPos and yPos
		this.xPos += distance*Math.cos(angle);
		this.yPos += distance*Math.sin(angle);

		// change velocity since friction has affected it, then change xVel and yVel accordingly
		velocity -= friction*time;
		this.xVel = velocity*Math.cos(angle); this.yVel = velocity*Math.sin(angle);
	}

	/**
	 * Returns the amount of time in seconds it'd take for a ball to travel
	 * a given distance over a surface with some amount of friction.
	 * Useful if one needs to move a ball backwards some distance and then move it forward in time again.
	 *
	 * @param  distance the distance which we're trying to determine how long it takes to travel
	 * @param  friction the rate at which velocity decreases over time (velocity decreases by 1*friction every second)
	 * @return          the amount of time in seconds it would take to travel distance given friction
	 */
	public double distanceToTime(double distance, double friction){
		// per comments in moveTime(), distance = t*v0 - (f*t^2)/2
		// we can just solve this for t now
		// wolfram|alpha (and a bit of playing around) says that's t = (v - √(v^2 - 2df))/f

		double velocity = this.getVelocity();
		double sqrt_term = velocity*velocity - 2*distance*friction;

		if (friction == 0) {
			// oh yea, there's also an edge case here (can't divide by zero); in which case t = d/v
			return distance/velocity;
		}
		else if (sqrt_term >= 0) {
			return (velocity - Math.sqrt(velocity*velocity - 2*distance*friction)) / friction;
		}
		else {
			// if the sqrt_term is negative, we'd get an imaginary number
			// this occurs if the resultant time would've been greater than velocity/friction
			// but by then our velocity would be zero, so we can't ever get that far
			// therefore, just return the max amount of time we can travel instead (velocity/friction)

			return velocity/friction;
		}
	}

	/**
	 * Returns the distance between this ball and another ball's edges.
	 * Calculating the distance in this way makes collision detection easier as it'll be <= 0 if there is a collision.
	 * 
	 * @param other the other Ball we want to find the distance from.
	 * @return      the distance between the edges of this Ball and the other Ball.
	 */
	public double distanceFrom(Ball other){
		double distX = other.xPos - this.xPos; double distY = other.yPos - this.yPos;
		double distance = Math.sqrt(distX*distX + distY*distY);

		// subtract the radii of both balls in order to get the distance between their edges
		//     this function now multiplies the sum of the radii by 0.99 here in order to mitigate an issue where balls get stuck
		//     the issue seems to get exacerbated whenever balls are placed exactly right next to each other (0 distance)
		//     so doing this will make the balls look like they're next to each other even though they really aren't
		//     good enough hack for now.
		return distance - (this.radius + other.radius)*0.99;
	}

	/**
	 * Draws the Ball onto a Graphics object. Also supports isotropic scaling and offsetting.
	 * The parameters of this method should be determined automatically by some other method.
	 *
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 */
	public void drawBall(Graphics g, double scale, double xOffset, double yOffset){
		// adjust x, y and r based on scale, xOffset and yOffset for isotropic scaling
		int x = (int)((this.xPos-this.radius)*scale + xOffset);
		int y = (int)((this.yPos-this.radius)*scale + yOffset);
		int r = (int)(this.radius * scale);

		// draw the ball
		Color ballColor = new Color(this.r, this.g, this.b);
		g.setColor(ballColor); g.fillOval(x, y, r*2+1, r*2+1);
		g.setColor(Color.black); g.drawOval(x, y, r*2, r*2);

		// draws velocity vectors for debugging purposes
		g.setColor(Color.red); g.drawLine((int)(x+r), (int)(y+r), (int)(x+r+this.xVel/10*scale), (int)(y+r+this.yVel/10*scale));
	}
}