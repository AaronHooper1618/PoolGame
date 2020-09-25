import java.awt.*;
import java.util.*;

// TODO: should this just be TableState and we keep track of GameState (players/turn/fouls/etc.) in another class?
class GameState {
    private int w, h;
    private double friction;
    private Ball[] balls;

    public GameState(){
        w = 784; h = 561; friction = 200;
        balls = new Ball[16];
        int i = 0;

        this.balls[0] = new Ball(20, 200, 280); i++;
        this.balls[0].r = 235;
        this.balls[0].g = 240;
        this.balls[0].b = 209;

        for(int j = 0; j < 6; j++){
            for(int k = 0; k < j; k++){
                this.balls[i] = new Ball(20, 400+j*37, 300+k*40-j*20);
                i++;
            }
        }
    }

    public void replaceBall(int i, double xPos, double yPos, double xVel, double yVel){
        if(i < this.balls.length){
            this.balls[i] = new Ball(20, xPos, yPos, xVel, yVel);
        }
    }

    public void replaceBall(int i, Ball ball){
        if(i < this.balls.length){
            this.balls[i] = ball;
        }
    }

    public double distanceBetween(Ball a, Ball b){
        double distX = b.xPos - a.xPos; double distY = b.yPos - a.yPos;
        double distance = Math.sqrt(distX*distX + distY*distY);

        // note that this method returns the distance between the two balls' EDGES, not their centers
        // reason for this is for collision checking: if the balls are colliding, this will be <= 0
        return distance - (a.radius + b.radius);
    }

    public void handleBallCollisions(int i, int j){
        // formula and steps taken from https://imada.sdu.dk/~rolf/Edu/DM815/E10/2dcollisions.pdf
        // best to cross-reference these comments with that link

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
        //         (formula being vAn = (mB(vBn-vAn) + mAvAn + mBvBn) / (mA + mB) and vice-versa)
        //         TODO: this formula comes from https://en.wikipedia.org/wiki/Inelastic_collision which might be useful if we end up needing to apply restitution

        double newAVN = (b.mass*(bVelNormal - aVelNormal) + a.mass*aVelNormal + b.mass*bVelNormal) / (a.mass + b.mass);
        double newBVN = (a.mass*(aVelNormal - bVelNormal) + a.mass*aVelNormal + b.mass*bVelNormal) / (a.mass + b.mass);
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

    public void moveTime(double time){
        // shuffle the ball ordering around
        int[] order = new int[balls.length];
        for (int i = 0; i < order.length; i++) {order[i] = i;}
        Collections.shuffle(Arrays.asList(order));
        
        // move the balls
        for (int o = 0; o < order.length; o++) {
            int i = order[o];
            if (balls[i].xVel != 0 && balls[i].yVel != 0 || true){
                balls[i].moveTime(time, friction);
            
                // handle collisions between ball i and every other ball
                // TODO: balls still get stuck inside each other with this; this method may need a rewrite still
                for (int j = 0; j < balls.length; j++) {
                    if (i != j){ // dont check for collision with itself
                        double d = distanceBetween(balls[i], balls[j]); // TODO: this distance is wrong pls fix thnx
                        if (d < 0){
                            // rewind time to stop the balls from intersecting
                            double t = balls[i].distanceToTime(d, friction);
                            t = Math.max(t, 0-time);
                            balls[i].moveTime(t, friction);

                            this.handleBallCollisions(i, j);

                            // then move it forward again
                            balls[i].moveTime(-t, friction);
                        }
                    }
                }

                // handle wall collisions
                this.handleWallCollisions(i);
            }
        }
    }

    // TODO: handle isotropic rescaling (probably needs to be done in Ball.drawBall())
    public void drawBalls(Graphics g){
        for (int i = 0; i < balls.length; i++) {
            balls[i].drawBall(g);
        }
    }
}

class Ball {
    public final int radius; 
    public final double mass;
    public int r, g, b;
    public double xPos, yPos;
    public double xVel, yVel;

    // TODO: this is way too many constructors lol
    //       either figure out how to organize this or mark which ones we're using and decide whether to cut the rest
    //       also, there isn't one for pre-defining the color, so that's even more of a mess. that's nice.
    public Ball(int radius, double mass, double xPos, double yPos, double xVel, double yVel){  // unused
        this.radius = radius; this.mass = mass;
        this.xPos = xPos; this.yPos = yPos;
        this.xVel = xVel; this.yVel = yVel;
        r = 0; g = 0; b = 200;
    }

    public Ball(int radius, double xPos, double yPos, double xVel, double yVel){ // used in GameState.ReplaceBall()
        this.radius = radius; this.mass = 50.0;
        this.xPos = xPos; this.yPos = yPos;
        this.xVel = xVel; this.yVel = yVel;
        r = 0; g = 0; b = 200;
    }
    
    public Ball(int radius, double mass, double xPos, double yPos){ // unused
        this.radius = radius; this.mass = mass;
        this.xPos = xPos; this.yPos = yPos;
        r = 0; g = 0; b = 200;
    }

    public Ball(int radius, double xPos, double yPos){ // used in GameState constructor and (tentatively) CollisionCanvas MouseReleased() listener
        this.radius = radius; this.mass = 50.0;
        this.xPos = xPos; this.yPos = yPos;
        r = 0; g = 0; b = 200;
    }

    private double getVelocity(){
        return Math.sqrt(this.xVel*this.xVel + this.yVel*this.yVel);
    }

    private double getAngle(){
        return Math.atan2(this.yVel, this.xVel);
    }

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

    // finds how long it'd take for the ball to travel a certain distance
    // useful for rewinding time to avoid having balls stuck inside each other
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

    // TODO: handle isotropic rescaling (pass over w and h from GameState?)
    public void drawBall(Graphics g){
        int x = (int)Math.round(this.xPos-this.radius);
        int y = (int)Math.round(this.yPos-this.radius);

        Color ballColor = new Color(this.r, this.g, this.b);
        g.setColor(ballColor); g.fillOval(x, y, this.radius*2+1, this.radius*2+1);
        g.setColor(Color.black); g.drawOval(x, y, this.radius*2, this.radius*2);

        // draws velocity vectors for debugging purposes
        g.setColor(Color.red); g.drawLine((int)(x+this.radius), (int)(y+this.radius), (int)(x+this.radius+this.xVel/10), (int)(y+this.radius+this.yVel/10));
    }
}