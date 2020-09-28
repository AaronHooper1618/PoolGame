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
			for (int j = 0; j < balls.length; j++) {
				if (i != j){ // dont check for collision with itself
					double d = distanceBetween(balls[i], balls[j]);
					if (d < 0){
						// add the distance lost in distanceBetween() back
						// intended effect of this is to make collisions less likely
						// but if they do happen, the balls ACTUALLY won't be colliding anymore
						d -= (balls[i].radius + balls[j].radius) * 0.01;

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