import java.awt.*;

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
		Ball cue = new Ball(20, 200, 280);
		cue.setColor(235, 240, 209);
		table.addBall(cue);

		// add rack
		for(int j = 0; j < 6; j++){
			for(int k = 0; k < j; k++){
				table.addBall(new Ball(20, 400+j*37, 300+k*40-j*20));
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

		// left box
		table.addWall(new Wall(200, 400, 200, 480));
		table.addWall(new Wall(200, 480, 280, 480));
		table.addWall(new Wall(280, 480, 280, 400));
		table.addWall(new Wall(280, 400, 200, 400));

		// right box
		table.addWall(new Wall(400, 400, 480, 400));
		table.addWall(new Wall(480, 400, 480, 480));
		table.addWall(new Wall(480, 480, 400, 480));
		table.addWall(new Wall(400, 480, 400, 400));
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
