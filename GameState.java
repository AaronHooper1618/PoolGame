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

	public GameState(){
		w = 784; h = 561; friction = 200;
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
	 * Returns the distance between two balls' edges.
	 * Calculating the distance in this way makes collision detection easier as it'll be <= 0 if there is a collision.
	 * 
	 * @param a The first Ball.
	 * @param b The second Ball.
	 * @return The distance between the edges of Ball a and Ball b.
	 */
	public double distanceBetween(Ball a, Ball b){
		double distX = b.xPos - a.xPos; double distY = b.yPos - a.yPos;
		double distance = Math.sqrt(distX*distX + distY*distY);

		// subtract the radii of both balls in order to get the distance between their edges
		//     this function now multiplies the sum of the radii by 0.99 here in order to mitigate an issue where balls get stuck
		//     the issue seems to get exacerbated whenever balls are placed exactly right next to each other (0 distance)
		//     so doing this will make the balls look like they're next to each other even though they really aren't
		//     good enough hack for now.
		return distance - (a.radius + b.radius)*0.99;
	}

	/**
	 * Adjusts the velocities of two balls assuming they have collided with one another.
	 * Can also set a custom coefficient of restitution (ratio of final over initial velocities) to simulate inelastic collisions.
	 * This follows the algorithm described in https://imada.sdu.dk/~rolf/Edu/DM815/E10/2dcollisions.pdf.
	 * 
	 * @param   i The index of the first ball in this.balls that we're handling collisions for.
	 * @param   j The index of the second ball in this.balls that we're handling collisions for.
	 * @param cor The coefficient of restitution (1 for elastic collision; 0 for perfectly inelastic collision).
	 */
	public void handleBallCollisions(int i, int j, double cor){
		// step 0: get ball and and ball b
		Ball a = this.balls[i]; Ball b = this.balls[j];

		// step 1: find the unit normal vector (difference between centers, then divide that vector by its magnitude) 
		//         and unit tangent vector (same vector rotated 90 degrees)
		double normalX = b.xPos - a.xPos; double normalY = b.yPos - a.yPos;
		double magnitude = Math.sqrt(normalX*normalX + normalY*normalY);
		normalX /= magnitude; normalY /= magnitude;
		double tangentX = -normalY; double tangentY = normalX;

		// step 2: find the dot product between both (velocity*normal) and (velocity*tangent) for both balls
		//         (this is essentially the magnitude when we project our velocity vectors onto both the normal vector and the tangent vector)
		double aVelNormal = normalX * a.xVel + normalY * a.yVel;
		double aVelTangent = tangentX * a.xVel + tangentY * a.yVel;
		double bVelNormal = normalX * b.xVel + normalY * b.yVel;
		double bVelTangent = tangentX * b.xVel + tangentY * b.yVel;

		// step 3: the tangent components will not change magnitude at all (after all, its the part facing perpendicular to the other object)
		//         this means we've reduced this into a one dimensional collision along the normal vector. apply that formula to aVelNormal and bVelNormal.
		//         (formula being vAn = (CoR*mB(vBn-vAn) + mAvAn + mBvBn) / (mA + mB) and vice-versa; taken from https://en.wikipedia.org/wiki/Inelastic_collision)

		double newAVN = (cor*b.mass*(bVelNormal - aVelNormal) + a.mass*aVelNormal + b.mass*bVelNormal) / (a.mass + b.mass);
		double newBVN = (cor*a.mass*(aVelNormal - bVelNormal) + a.mass*aVelNormal + b.mass*bVelNormal) / (a.mass + b.mass);
		aVelNormal = newAVN; bVelNormal = newBVN;

		// step 4: you now have the normal and tangent velocities for both balls
		//         convert them back into vectors by multiplying the unit normal/tangent vectors by these velocities.

		// ball a
		double aNormalX = normalX * aVelNormal; double aNormalY = normalY * aVelNormal;
		double aTangentX = tangentX * aVelTangent; double aTangentY = tangentY * aVelTangent;

		// ball b
		double bNormalX = normalX * bVelNormal; double bNormalY = normalY * bVelNormal;
		double bTangentX = tangentX * bVelTangent; double bTangentY = tangentY * bVelTangent;

		// step 5: add the normal and tangent vectors together; change both balls' velocities to that sum
		a.xVel = aNormalX + aTangentX; a.yVel = aNormalY + aTangentY;
		b.xVel = bNormalX + bTangentX; b.yVel = bNormalY + bTangentY;
	}

	/**
	 * Handles a collision between a Ball and a boundary of the field, should it occur.
	 * Pushes the ball outside of the wall and then changes its velocity.
	 * 
	 * @param i The index of the ball in this.balls that we're handling wall collisions for.
	 */
	public void handleWallCollisions(int i){
		Ball a = this.balls[i];

		// TODO: this is ugly (well, even more so than usual), clean it up
		if (a.xPos < a.radius){ // left boundary
			a.moveTime((a.radius - a.xPos)/a.xVel, this.friction);
			a.xVel = 0-a.xVel;
		}
		else if (a.xPos > (this.w - a.radius)){ // right boundary
			a.moveTime(((this.w-a.radius - a.xPos))/a.xVel, this.friction);
			a.xVel = 0-a.xVel;
		}
		else if (a.yPos < a.radius){ // top boundary
			a.moveTime((a.radius - a.yPos)/a.yVel, this.friction);
			a.yVel = 0-a.yVel;
		}
		else if (a.yPos > (this.h - a.radius)){ // bottom boundary
			a.moveTime(((this.h-a.radius) - a.yPos)/a.yVel, this.friction);
			a.yVel = 0-a.yVel;
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
		int[] order = new int[balls.length];
		for (int i = 0; i < order.length; i++) {order[i] = i;}
		Collections.shuffle(Arrays.asList(order));
		
		// move the balls
		for (int o = 0; o < order.length; o++) {
			int i = order[o];
			balls[i].moveTime(time, friction);
			
			// handle collisions between ball i and every other ball
			// TODO: balls still get stuck inside each other with this; this method may need a rewrite still
			for (int j = 0; j < balls.length; j++) {
				if (i != j){ // dont check for collision with itself
					double d = distanceBetween(balls[i], balls[j]); // TODO: this distance is wrong pls fix thnx
					if (d < 0){
						// the ball with the higher velocity is able to move more over some period of time
						// we should pick the ball with the higher velocity so not as much time ends up getting rewinded
						int k = i;
						if (balls[i].getVelocity() < balls[j].getVelocity()){
							k = j;
						}

						// rewind time on ball k to stop the balls from intersecting
						double t = balls[k].distanceToTime(d, friction);
						balls[i].moveTime(t, friction);

						this.handleBallCollisions(i, j, 0.95);

						// then move ball k forward again
						balls[k].moveTime(-t, friction);
					}
				}
			}

			// handle wall collisions
			this.handleWallCollisions(i);
		}
	}

	/**
	 * Draws all the Balls that are in the GameState onto a Graphics object.
	 * Will also determine scale, xOffset and yOffset in advance in order to handle
	 * anisotropic scaling based on the width and height of the canvas.
	 * 
	 * @param g the Graphics object being drawn onto
	 * @param w the width of the canvas being drawn onto
	 * @param h the height of the canvas being drawn onto
	 */
	public void drawBalls(Graphics g, int w, int h){
		// calculate scale, xOffset and yOffset for anisotropic scaling
		double scale = Math.min((double)w/this.w, (double)h/this.h);
		double xOffset = (w - this.w*scale)/2; 
		double yOffset = (h - this.h*scale)/2;

		for (int i = 0; i < balls.length; i++) {
			balls[i].drawBall(g, scale, xOffset, yOffset);
		}
	}
}

/**
 * Represents a ball that can move around in 2-dimensional space.
 */
class Ball {
	public final int radius; 
	public final double mass;
	public double xPos, yPos;
	public double xVel, yVel;
	private int r, g, b;

	// TODO: this is way too many constructors lol
	//       either figure out how to organize this or mark which ones we're using and decide whether to cut the rest
	public Ball(int radius, double mass, double xPos, double yPos, double xVel, double yVel){  // unused
		this.radius = radius; this.mass = mass;
		this.xPos = xPos; this.yPos = yPos;
		this.xVel = xVel; this.yVel = yVel;
		this.setColor(0, 0, 200);
	}

	public Ball(int radius, double xPos, double yPos, double xVel, double yVel){ // unused
		this.radius = radius; this.mass = 50.0;
		this.xPos = xPos; this.yPos = yPos;
		this.xVel = xVel; this.yVel = yVel;
		this.setColor(0, 0, 200);
	}
	
	public Ball(int radius, double mass, double xPos, double yPos){ // unused
		this.radius = radius; this.mass = mass;
		this.xPos = xPos; this.yPos = yPos;
		this.setColor(0, 0, 200);
	}

	public Ball(int radius, double xPos, double yPos){ // used in GameState constructor and (tentatively) CollisionCanvas MouseReleased() listener
		this.radius = radius; this.mass = 50.0;
		this.xPos = xPos; this.yPos = yPos;
		this.setColor(0, 0, 200);
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
	 * Draws the Ball onto a Graphics object. Also supports anisotropic scaling and offsetting.
	 * The parameters of this method should be determined automatically by some other method.
	 *
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn ball
	 * @param xOffset the amount of pixels to offset the drawn ball by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn ball by on the yAxis
	 */
	public void drawBall(Graphics g, double scale, double xOffset, double yOffset){
		// adjust x, y and r based on scale, xOffset and yOffset for anisotropic scaling
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