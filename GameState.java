import java.awt.*;
import java.util.*;

/**
 * Represents the state of a pool game being played.
 * Contains a TableState, but also keeps track of things such as whose turn it is, fouls and so on.
 */
class GameState {
	public final int w, h;
	public int padding_left, padding_right, padding_top, padding_bottom;
	public TableState table;

	public GameState(){
		this.w = 800; this.h = 600;
		this.padding_top = 50; this.padding_bottom = 50;
		this.padding_left = 50; this.padding_right = 50;
		table = new TableState(this.w, this.h);
		double radius = 20;

		// add cueball
		Ball cue = new Ball(radius, Ball.TYPE_CUEBALL, 200, 280);
		table.addBall(cue);

		// add rack
		double distance = Math.sqrt(3)*radius; // distance between each column of balls on the rack
		int reds = 7; int blues = 7; int type;

		for(int j = 0; j < 6; j++){
			for(int k = 0; k < j; k++){
				// determine what type of ball is going to be placed in this spot
				if (j == 3 && k == 1){type = Ball.TYPE_8BALL;} // 8 ball in center of rack
				else if (Math.random() < (double)(reds)/(reds+blues)){type = Ball.TYPE_RED; reds--;} // pick a red ball based on how many reds and blues are left
				else {type = Ball.TYPE_BLUE; blues--;} // pick a blue ball if we dont pick a red ball

				table.addBall(new Ball(radius, type, 400+j*distance, 300+k*radius*2-j*radius));
			}
		}
		
		// add walls
		// bounding walls (inner)
		table.addWall(new Wall(0, 0, 0, 599));
		table.addWall(new Wall(0, 599, 799, 599));
		table.addWall(new Wall(799, 599, 799, 0));
		table.addWall(new Wall(799, 0, 0, 0));

		// bounding walls (outer)
		double t = 20;
		table.addWall(new Wall(-t, -t, -t, 599+t));
		table.addWall(new Wall(-t, 599+t, 799+t, 599+t));
		table.addWall(new Wall(799+t, 599+t, 799+t, -t));
		table.addWall(new Wall(799+t, -t, -t, -t));

		// angled triangle
		table.addWall(new Wall(599, 599, 799, 300));
		table.addWall(new Wall(799, 300, 799, 599));
		table.addWall(new Wall(799, 599, 599, 599));

		// left box (walls)
		table.addWall(new Wall(200, 400, 200, 480));
		table.addWall(new Wall(200, 480, 280, 480));
		table.addWall(new Wall(280, 480, 280, 400));
		table.addWall(new Wall(280, 400, 200, 400));

		// right box (pocket)
		Wall w1 = new Wall(400, 400, 480, 400, true);
		Wall w2 = new Wall(480, 400, 480, 480, true);
		Wall w3 = new Wall(480, 480, 400, 480, true);
		Wall w4 = new Wall(400, 480, 400, 400, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);

		// adds Pocket based on walls from right box
		ArrayList<Wall> p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4));
		table.addPocket(new Pocket(p));
	}

	/**
	 * Moves the game forward a certain amount of time.
	 * 
	 * @param time the amount of time, in seconds, that the game is moved forward
	 */
	public void moveTime(double time){
		table.moveTime(time);
	}

	/**
	 * Draws everything in the GameState onto a Graphics object.
	 * Will also determine scale, xOffset and yOffset in advance in order to handle
	 * isotropic scaling based on the width and height of the canvas.
	 * 
	 * @param g the Graphics object being drawn onto
	 * @param w the width of the canvas being drawn onto
	 * @param h the height of the canvas being drawn onto
	 */
	public void draw(Graphics g, int w, int h){
		table.fillPolygon(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h), new Color(155, 126, 70), new int[]{0, 1, 2, 3, 4, 5, 6, 7}); // wooden frame
		table.fillPolygon(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h), new Color(1, 162, 76), new int[]{0, 1, 2, 3});               // felt playing field
		table.fillPolygon(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h), new Color(155, 126, 70), new int[]{8, 9, 10});               // wooden angled triangle
		table.fillPolygon(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h), new Color(155, 126, 70), new int[]{11, 12, 13, 14});         // wooden left box

		table.drawObjects(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h));
	}

	/**
	 * Draws the velocity vector (scaled down by a factor of 10) as well as the next
	 * collision point for the cue ball assuming it will have a velocity of (xVel, yVel).
	 * This method does not draw any of the objects on the table and should be called after GameState.draw().
	 * 
	 * @param    g the Graphics object being drawn onto
	 * @param    w the width of the canvas being drawn onto
	 * @param    h the height of the canvas being drawn onto
	 * @param xVel the velocity along the x-axis the cue ball will be moving at
	 * @param yVel the velocity along the y-axis the cue ball will be moving at
	 */
	public void drawShotPreview(Graphics g, int w, int h, double xVel, double yVel){
		// gets scale factor and offsets for isotropic scaling; throws it at this.table
		double scale = this.getScale(w, h); double xOffset = this.getXOffset(w, h); double yOffset = this.getYOffset(w, h);
		table.drawShotPreview(g, scale, xOffset, yOffset, xVel, yVel);
	}

	/**
	 * Draws a preview of where the cue ball would be located, given the user clicks at the coordinate (xPos, yPos) on the canvas.
	 * Will also return whether the cue ball can be placed in that location or not based on TableState.nextCollisionPoint().
	 * This method does not draw any of the objects on the table and should be called after GameState.draw().
	 * 
	 * @param    g the Graphics object being drawn onto
	 * @param    w the width of the canvas being drawn onto
	 * @param    h the height of the canvas being drawn onto
	 * @param xPos x coordinate of where the user would place the cue ball on the canvas
	 * @param yPos y coordinate of where the user would place the cue ball on the canvas
	 */
	public boolean drawPlacePreview(Graphics g, int w, int h, double xPos, double yPos){
		// gets scale factor and offsets for isotropic scaling; throws it at this.table
		double scale = this.getScale(w, h); double xOffset = this.getXOffset(w, h); double yOffset = this.getYOffset(w, h);
		return table.drawPlacePreview(g, scale, xOffset, yOffset, xPos, yPos);
	}

	/**
	 * Determines the factor by which to scale the image when drawing onto
	 * a canvas of a given width and height.
	 * 
	 * @param target_w the width of the canvas being drawn onto
	 * @param target_h the height of the canvas being drawn onto
	 * @return         the scaling factor needed to fit the game onto the canvas
	 */
	public double getScale(int target_w, int target_h){
		int w = this.w + this.padding_left + this.padding_right;
		int h = this.h + this.padding_top + this.padding_bottom;
		return Math.min((double)target_w/w, (double)target_h/h);
	}

	/**
	 * Determines the offset on the x-axis needed to center the image onto 
	 * the canvas after it's scaled isotropically.
	 * 
	 * @param target_w the width of the canvas being drawn onto
	 * @param target_h the height of the canvas being drawn onto
	 * @return         the offset on the x-axis needed to fit the game onto the canvas
	 */
	public double getXOffset(int target_w, int target_h){
		double scale = this.getScale(target_w, target_h);
		return (target_w + (-this.w+this.padding_left-this.padding_right)*scale)/2;
	}

	/**
	 * Determines the offset on the x-axis needed to center the image onto 
	 * the canvas after it's scaled isotropically.
	 * 
	 * @param target_w the width of the canvas being drawn onto
	 * @param target_h the height of the canvas being drawn onto
	 * @return         the offset on the y-axis needed to fit the game onto the canvas
	 */
	public double getYOffset(int target_w, int target_h){
		double scale = this.getScale(target_w, target_h);
		return (target_h + (-this.h+this.padding_top-this.padding_bottom)*scale)/2;
	}
}
