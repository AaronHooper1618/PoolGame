import java.awt.*;

/**
 * Represents a wall that balls are able to collide with.
 */
public class Wall {
    public final double x1, y1; // first endpoint
    public final double x2, y2; // second endpoint
    public final double length;
    public final double angle;
    private int r, g, b;

    // TODO: Add a way to define what direction the normal force is facing (somehow?)
    /**
     * Creates a one-way wall which extends across 2 endpoints.
     * These walls have a normal force that goes in the opposite direction of their orientation.
     * i.e. Enclosed spaces should be drawn counter-clockwise and boxes should be drawn clockwise.
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

    /**
     * Determines if a Ball is colliding with this Wall.
     * If this is less than 0, the Ball is colliding with the Wall.
     * 
     * @param ball the Ball that we're determining the distance from
     * @return     the distance from the wall to the ball's edge
     */
    public double isBallColliding(Ball ball){
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
            if (x_distance < ball.radius){ // ...and within the wall on the x-axis...
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
	 * Draws the Wall onto a Graphics object. Also supports anisotropic scaling and offsetting.
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
