import java.awt.*;

/**
 * Represents a wall that balls are able to collide with.
 */
public class Wall {
    public final double x1, y1; // first endpoint
    public final double x2, y2; // second endpoint
    private final int normal;
    private final double length;
    private final double angle;
    public int r, g, b;

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
        this.normal = -1;

        double dx = this.x2 - this.x1; double dy = this.y2 - this.y1;
        this.length = Math.sqrt(dx*dx + dy*dy);
        this.angle = Math.atan2(dy, dx);
    }

    /**
     * Creates a wall which extends across 2 endpoints.
     * 
     * @param     x1 the x coordinate for the first endpoint
     * @param     y1 the y coordinate for the first endpoint
     * @param     x2 the x coordinate for the second endpoint
     * @param     y2 the y coordinate for the second endpoint
     * @param normal the direction of the normal force (-1 makes normal force point ccw; 1 makes normal force point cw)
     */
    public Wall(double x1, double y1, double x2, double y2, int normal){
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.normal = normal / Math.abs(normal);

        double dx = this.x2 - this.x1; double dy = this.y2 - this.y1;
        this.length = Math.sqrt(dx*dx + dy*dy);
        this.angle = Math.atan2(dy, dx);
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
        double yb = this.x2*Math.sin(a) + this.y2*Math.cos(a);

        // rotate the ball along -this.angle accordingly
        double x_ball = ball.xPos*Math.cos(a) - ball.yPos*Math.sin(a);
        double y_ball = ball.xPos*Math.sin(a) + ball.yPos*Math.cos(a);

        // find how far away the ball is on the x axis
        //                  Math.max([distance from right side], [distance from left side])
        double x_distance = Math.max(x_ball - Math.max(xa, xb), Math.min(xa, xb) - x_ball);
        x_distance = Math.max(x_distance, 0);

        // find how far away the ball is on the y axis
        double y_distance = ya - (y_ball + ball.radius);

        // TODO: should we return a more appropriate value than what's listed here?
        if (x_distance < ball.radius){ // if the rotated ball's within the same x coordinate range as the rotated wall...
            if (Math.abs(y_distance) < ball.radius*2){ // ...and its actually intersecting the wall...
                return y_distance; // ...return the negative (colliding) y_distance
            }
            else { // otherwise...
                return Math.abs(y_distance); // ...return the absolute (not colliding) value of it
            }
        }
        else { // if it's not in that x coordinate range...
            return x_distance; // return how far away it is on the x axis.
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
