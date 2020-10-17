/**
 * Utility class used to resolve collisions that occur between Balls, Walls and Pockets.
 * The methods here handle both collision detection and collision resolution.
 * The collision handling methods here can modify the Balls' velocities, positions and sunken states that you put into them.
 */
public class CollisionHandler {
	/**
	 * Adjusts the velocities of two non-sunken balls assuming they have collided with one another.
	 * Can also set a custom coefficient of restitution (ratio of final over initial velocities) to simulate inelastic collisions.
	 * This follows the algorithm described in https://imada.sdu.dk/~rolf/Edu/DM815/E10/2dcollisions.pdf.
	 * 
	 * @param        a the first Ball that we're handling collisions for.
	 * @param        b the second Ball that we're handling collisions for.
	 * @param friction the rate at which velocity decreases over time (velocity decreases by 1*friction every second)
	 * @param      cor the coefficient of restitution (1 for elastic collision; 0 for perfectly inelastic collision).
	 */
	public static void handleBallCollisions(Ball a, Ball b, double friction, double cor){
		double distance = a.distanceFrom(b);
		
		if (distance < 0 && !a.sunk && !b.sunk){
			// add the distance lost in distanceBetween() back
			// intended effect of this is to make collisions less likely
			// but if they do happen, the balls ACTUALLY won't be colliding anymore
			distance -= (a.radius + b.radius) * 0.01;

			// the ball with the higher velocity is able to move more over some period of time
			// so we should pick the ball with the higher velocity so not as much time ends up getting rewinded
			// lets set a variable to keep track of which ball to move
			boolean moveA = a.getVelocity() > b.getVelocity();

			// rewind time on the faster ball to stop the balls from intersecting
			double time = 0;
			if (moveA){
				time = a.distanceToTime(distance, friction);
				a.moveTime(time, friction);
			}
			else{
				time = b.distanceToTime(distance, friction);
				b.moveTime(time, friction);
			}

			// == ELASTIC COLLISION SIMULATION == \\
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
			// == ELASTIC COLLISION SIMULATION == \\

			// then move the initially faster ball forward again
			if (moveA) {
				a.moveTime(-time, friction);
			}
			else {
				b.moveTime(-time, friction);
			}
		}
	}
	
	/**
	 * Handles a collision between a Ball and a Wall, should it occur.
	 * Pushes the ball outside of the wall and then changes its velocity.
	 * Can also set a custom coefficient of restitution (ratio of final over initial velocities) to simulate inelastic collisions.
	 * 
	 * @param     ball The ball that we're handling wall collisions for.
	 * @param     wall The wall that we're handling collisions for.
	 * @param friction the rate at which velocity decreases over time (velocity decreases by 1*friction every second)
	 * @param      cor The coefficient of restitution (1 for elastic collision; 0 for perfectly inelastic collision).
	 */
	public static void handleWallCollisions(Ball ball, Wall wall, double friction, double cor){
		double distance = wall.isBallColliding(ball);

		if (distance < 0 && (ball.sunk == wall.sunk || wall.always)) {
			// if we're more than a.radius into the wall, we might as well be on the other side of it
			// so add 2*a.radius and try to move that distance instead
			// effectively makes the walls have 2-way collision rather than 1-way
			if (distance < -ball.radius) {
				distance += 2 * ball.radius;
			}

			// get the unit tangent vector (Wall but with length of 1)
			double tangentX = (wall.x2 - wall.x1) / wall.length; 
			double tangentY = (wall.y2 - wall.y1) / wall.length;

			// get the unit normal vector
			double normalX = tangentY; double normalY = -tangentX;

			// we can determine how far to move backwards with a couple steps
			double velNormal = normalX * ball.xVel + normalY * ball.yVel; // get the dot product of the ball's velocity onto the normal vector
			velNormal = velNormal / ball.getVelocity(); // calculate how much of the ball's velocity is going along the normal vector as a ratio
			distance /= velNormal; // divide the distance from isBallColliding() by that ratio

			// move the ball back that determined distance
			double time = ball.distanceToTime(-distance, friction);
			ball.moveTime(time, friction);

			// the rest is pretty similar to the elastic collisions in handleBallCollisions()
			double velTangent = tangentX * ball.xVel + tangentY * ball.yVel; // so get the dot product of the ball's velocity onto the tangent vector
			tangentX *= velTangent; tangentY *= velTangent; // and scale tangentX/tangentY by that dot product 

			velNormal = normalX * ball.xVel + normalY * ball.yVel; // repeat for normal vector
			velNormal *= cor; // except multiply it by the coefficient of restitution
			normalX *= -velNormal; normalY *= -velNormal; // and also invert it

			// change balls[i]'s velocity accordingly and move it forwards in time
			ball.xVel = tangentX + normalX; ball.yVel = tangentY + normalY;
			ball.moveTime(-time, friction);
		}
	}

	/**
	 * Changes the ball's sunken property to true should a ball fall into the pocket.
	 * Also resolves collisions within the boundaries of the pocket.
	 * 
	 * @param     ball The ball that we're handling the sunken state of.
	 * @param   pocket The pocket which the ball may have fallen into.
	 * @param friction the rate at which velocity decreases over time (velocity decreases by 1*friction every second)
	 * @param      cor The coefficient of restitution (1 for elastic collision; 0 for perfectly inelastic collision).
	 */
	public static void handlePocketCollisions(Ball ball, Pocket pocket, double friction, double cor){
		ball.sunk = ball.sunk || pocket.ballInPocket(ball);

		if (ball.sunk){ // if the ball's in a pocket, we need to keep it in there
			// so we're gonna try to make a wall tangent to the pocket and let handleWallCollisions do the heavy lifting
			// TODO: this method doesn't work well if the size of the ball and the size of the pocket are fairly close to one another

			// scale the distance from ball to the pocket up such that it's the size of the pocket's radius
			double distance = pocket.distanceFromPocket(ball.xPos, ball.yPos);
			double dx = ball.xPos - pocket.xPos; double dy = ball.yPos - pocket.yPos;
			dx /= distance; dy /= distance; // TODO: what if distance = 0?
			dx *= pocket.radius; dy *= pocket.radius;

			// we can now get the point where the tangent wall will touch the pocket
			double x = pocket.xPos + dx; double y = pocket.yPos + dy;

			// get the line running perpendicular to that distance vector and turn it into a wall for the ball to collide with
			double tx = -dy; double ty = dx;
			Wall tangent = new Wall(x-tx, y-ty, x+tx, y+ty, true, true);
			CollisionHandler.handleWallCollisions(ball, tangent, friction, cor);
		}
	}
}
