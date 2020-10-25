import java.awt.*;

/**
 * Represents a region bounded by walls that a ball can fall and sink into.
 */
public class Pocket {
	public double radius;
	public double xPos, yPos;

	/**
	 * Creates a pocket which balls can sink into as defined by its bounding walls.
	 * It's recommended that all the Walls in a Pocket should be sunken.
	 * 
	 * @param radius the size of the pocket's radius
	 * @param   xPos the x coordinate of the center of the pocket
	 * @param   yPos the y coordinate of the center of the pocket
	 */
	public Pocket(double radius, double xPos, double yPos){
		this.radius = radius;
		this.xPos = xPos; this.yPos = yPos;
	}

	/**
	 * Calculates the distance of a point from the center of this pocket.
	 * 
	 * @param xPos the x coordinate of the point
	 * @param yPos the y coordinate of the point
	 * @return     the distance of the point from the center of the pocket
	 */
	public double distanceFromPocket(double xPos, double yPos){
		return Math.sqrt((this.xPos - xPos)*(this.xPos - xPos) + (this.yPos - yPos)*(this.yPos - yPos));
	}

	/**
	 * Determines whether the position of a given ball resides within the pocket.
	 * 
	 * @param b the ball in question
	 * @return  true if the ball's position is inside the pocket
	 */
	public boolean ballInPocket(Ball b){
		if (b.radius > this.radius) { // if the ball's too big, it can't fit in the pocket
			return false;
		}
		else {
			double distance = this.distanceFromPocket(b.xPos, b.yPos);
			distance -= this.radius;

			if (distance < b.radius * (1 - 2*0.6)){
				return true;
			}
			return false;
		}
	}

	/**
	 * Draws the Pocket onto a Graphics object. Also supports isotropic scaling and offsetting.
	 * The parameters of this method should be determined automatically by some other method.
	 *
	 * @param       g the Graphics object being drawn onto
	 * @param   scale the factor to increase the size of the drawn pocket
	 * @param xOffset the amount of pixels to offset the drawn pocket by on the xAxis
	 * @param yOffset the amount of pixels to offset the drawn pocket by on the yAxis
	 */
	public void drawPocket(Graphics g, double scale, double xOffset, double yOffset){
		int x = (int)((this.xPos-this.radius)*scale + xOffset);
		int y = (int)((this.yPos-this.radius)*scale + yOffset);
		int r = (int)(this.radius * scale);

		g.setColor(Color.black); g.fillOval(x, y, r*2 + 2, r*2 + 2);
	}
}
