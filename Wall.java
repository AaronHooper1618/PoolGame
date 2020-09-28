import java.awt.*;

/**
 * Represents a wall that balls are able to collide with.
 */
public class Wall {
    public final int x1, y1; // first endpoint
    public final int x2, y2; // second endpoint
    private int r, g, b;

    /**
     * Creates a wall which extends across 2 endpoints.
     * 
     * @param x1 The x coordinate for the first endpoint.
     * @param y1 The y coordinate for the first endpoint.
     * @param x2 The x coordinate for the second endpoint.
     * @param y2 The y coordinate for the second endpoint.
     */
    public Wall(int x1, int y1, int x2, int y2){
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
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
