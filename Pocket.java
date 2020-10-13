import java.util.*;
import java.awt.*;

/**
 * Represents a region bounded by walls that a ball can fall and sink into.
 */
public class Pocket {
	private final ArrayList<Wall> walls;
	private Polygon lastCalculatedPolygon; private double lastCalculatedRadius;

	/**
	 * Creates a pocket which balls can sink into as defined by its bounding walls.
	 * It's recommended that all the Walls in a Pocket should be sunken.
	 * 
	 * @param walls an ArrayList of Walls which define the boundaries of the pocket
	 */
	public Pocket(ArrayList<Wall> walls){
		this.walls = walls;
		this.lastCalculatedRadius = -1.0;
	}

	/**
	 * Determines whether the position of a given ball resides within the pocket.
	 * 
	 * @param b the ball in question
	 * @return true if the ball's position is inside the pocket
	 */
	public boolean ballInPocket(Ball b){
		int nPoints = (walls.size()+1) * 2;
		int[] xPoints = new int[nPoints]; int[] yPoints = new int[nPoints];
		double r = b.radius * 0.6;
		Polygon p;

		if (lastCalculatedRadius != r){
			Wall last_wall = null; int direction = 1;

			// get the xPoints and yPoints for each of the walls in the pocket
			for(int i = 0; i < nPoints/2; i++){
				Wall w = this.walls.get(i % this.walls.size());
				double xa = w.x1; double ya = w.y1;
				double xb = w.x2; double yb = w.y2;
				double dx = (xb-xa)*direction; double dy = (yb-ya)*direction;

				// calculate how much we'd have to move the wall such that it ends up...
				double length = Math.sqrt(dx*dx + dy*dy); double lscale = r/length;
				double nx = -dy*lscale; double ny = dx*lscale;

				// ...pushing the wall inwards by a length of r
				xa += nx; ya += ny; xb += nx; yb += ny;

				// set the next xPoints and yPoints accordingly
				xPoints[i*2] = (int)xa; xPoints[i*2 + 1] = (int)xb;
				yPoints[i*2] = (int)ya; yPoints[i*2 + 1] = (int)yb;

				Wall wall = new Wall(xa, ya, xb, yb);

				if (last_wall != null){ // if there is a last wall...
					// get the intersection point between this wall and the last wall
					double[] intersection = Pocket.IntersectionPoint(wall, last_wall);
					double xi = intersection[0]; double yi = intersection[1];

					if (Math.min(xa, xb) <= xi && Math.max(xa, xb) >= xi){ // make sure that intersection point is on this pushed-in wall somewhere first
						// if so, set the x/y point to the intersection point instead of whatever it was before, so there arent any edges sticking out
						xPoints[i*2] = (int)xi; xPoints[i*2 - 1] = (int)xi;
						yPoints[i*2] = (int)yi; yPoints[i*2 - 1] = (int)yi;

						// and keep track of the last wall in the pocket that we pushed in
						last_wall = new Wall(xa, ya, xb, yb);
					}
					else { // otherwise, we've been pushing the walls outwards instead of inwards (oops) and need to invert the direction we push the walls
						i = 0; direction *= -1; last_wall = null;
					}
				}
				else { // if there isn't a last wall...
					// just keep track of the last wall in the pocket that we pushed in for now
					last_wall = new Wall(xa, ya, xb, yb);
				}
			}

			// we double count the first wall to calculate intersection points accurately
			// so get rid of the erroneous coordinates at the beginning/end of xPoints+yPoints
			xPoints = Arrays.copyOfRange(xPoints, 1, nPoints-1);
			yPoints = Arrays.copyOfRange(yPoints, 1, nPoints-1);

			// make a Polygon out of it and determine if the ball's xPos, yPos is within that polygon
			p = new Polygon(xPoints, yPoints, nPoints-2);
			lastCalculatedPolygon = p; lastCalculatedRadius = r;
		}
		else {
			// this is a really expensive method to do every frame
			// and, more likely than not, all the balls are going to be the same size
			// so if we already calculated this polygon with the same radius last time, then just reuse it again
			p = lastCalculatedPolygon;
		}

		// find if the ball is completely contained within the Polygon we made
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
		int nPoints = (walls.size()) * 2;
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

	/**
	 * Finds the points where two walls will intersect with one another. Used for ballInPocket().
	 * 
	 * @param a the first of two walls
	 * @param b the second of two walls
	 * @return  an array of doubles containing the x and y component of the intersection point in that order
	 */
	private static double[] IntersectionPoint(Wall a, Wall b){
		double dxa = a.x2 - a.x1; double dxb = b.x2 - b.x1;
		double dya = a.y2 - a.y1; double dyb = b.y2 - b.y1;

		// get the slopes and intercepts of both lines
		double ma = dya/dxa; double mb = dyb/dxb;
		double ba = a.y1 - ma*a.x1; double bb = b.y1 - mb*b.x1;

		if (ma == mb){
			return new double[]{Double.NaN, Double.NaN};
		}

		double xi = Double.MAX_VALUE; double yi = Double.MAX_VALUE;
		// edge case where dx is 0 (x=x; y=mx+b)
		if (dxa == 0){xi = a.x1; yi = mb*xi + bb;}
		else if (dxb == 0){xi = b.x1; yi = ma*xi + ba;}

		// edge case where dy is 0 (y=y; x=(y-b)/m)
		// (x=x if m == 0)
		if (dya == 0){yi = a.y1; xi = dxb==0 ? b.x2 : (yi - bb)/mb;}
		else if (dyb == 0){yi = b.y1; xi = dxa==0 ? a.x2 : (yi - ba)/ma;}

		// if none of the edge cases above apply, then calculate the intersection point normally
		if (xi == Double.MAX_VALUE && yi == Double.MAX_VALUE) {
			// y = ma*x + ba = mb*x + bb
			// (ma-mb)*x + (ba-bb) = 0
			// x = (bb-ba)/(ma-mb); y = mx+b
			xi = (bb-ba)/(ma-mb); yi = ma*xi + ba;
		}

		return new double[]{xi, yi};
	}
}
