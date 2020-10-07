import java.awt.*;
import java.util.*;

/**
 * Represents the state of a pool game being played.
 * Contains a TableState, but also keeps track of things such as whose turn it is, fouls and so on.
 */
class GameState {
	public final int w, h;
	public TableState table;

	public GameState(){
		this.w = 800; this.h = 600;
		table = new TableState(this.w, this.h);

		// add cueball
		Ball cue = new Ball(20, Ball.TYPE_CUEBALL, 200, 280);
		table.addBall(cue);

		// add rack
		for(int j = 0; j < 6; j++){
			for(int k = 0; k < j; k++){
				table.addBall(new Ball(20, Ball.TYPE_BLUE, 400+j*37, 300+k*40-j*20));
			}
		}
		
		// add walls
		// bounding walls
		table.addWall(new Wall(0, 0, 0, 599));
		table.addWall(new Wall(0, 599, 799, 599));
		table.addWall(new Wall(799, 599, 799, 0));
		table.addWall(new Wall(799, 0, 0, 0));

		// angled wall
		table.addWall(new Wall(599, 599, 799, 300));

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
		table.draw(g, getScale(w, h), getXOffset(w, h), getYOffset(w, h));
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
		return Math.min((double)target_w/this.w, (double)target_h/this.h);
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
		return (target_w - this.w*scale)/2;
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
		return (target_h - this.h*scale)/2;
	}
}
