import java.util.*;
import java.awt.*;

/**
 * Represents a region bounded by walls that a ball can fall and sink into.
 */
public class Pocket {
	private final ArrayList<Wall> walls;

	/**
	 * Creates a pocket which balls can sink into as defined by its bounding walls.
	 * It's recommended that all the Walls in a Pocket should be sunken.
	 * 
	 * @param walls an ArrayList of Walls which define the boundaries of the pocket
	 */
	public Pocket(ArrayList<Wall> walls){
		this.walls = walls;
	}

	/**
	 * Determines whether the position of a given ball resides within the pocket.
	 * 
	 * @param b the ball in question
	 * @return true if the ball's position is inside the pocket
	 */
	public boolean ballInPocket(Ball b){
		int nPoints = walls.size() * 2;
		int[] xPoints = new int[nPoints]; int[] yPoints = new int[nPoints];

		// get the xPoints and yPoints for each of the walls in the pocket
		for(int i = 0; i < nPoints/2; i++){
			Wall w = this.walls.get(i);
			xPoints[i*2] = (int)(w.x1); xPoints[i*2 + 1] = (int)(w.x2);
			yPoints[i*2] = (int)(w.y1); yPoints[i*2 + 1] = (int)(w.y2);
		}

		// make a Polygon out of it and determine if the ball's xPos, yPos is within that polygon
		Polygon p = new Polygon(xPoints, yPoints, nPoints);
		return p.contains(b.xPos, b.yPos);
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
		int nPoints = walls.size() * 2;
		int[] xPoints = new int[nPoints]; int[] yPoints = new int[nPoints];

		for(int i = 0; i < nPoints/2; i++){
			Wall w = this.walls.get(i);
			// adjust x1, y1, x2 and y2 based on scale, xOffset and yOffset for isotropic scaling
			xPoints[i*2] = (int)(w.x1 * scale + xOffset); xPoints[i*2 + 1] = (int)(w.x2 * scale + xOffset);
			yPoints[i*2] = (int)(w.y1 * scale + yOffset); yPoints[i*2 + 1] = (int)(w.y2 * scale + yOffset);
		}

		g.setColor(Color.black);
		g.fillPolygon(xPoints, yPoints, nPoints);
	}
}
