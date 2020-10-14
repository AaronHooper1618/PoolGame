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
	public boolean lastMoving; public int[] lastMovingByType; public int[] lastSunkByType;

	public int turn;
	public int[] groups;
	// TODO: public boolean foul;

	public GameState(){
		this.w = 224; this.h = 112;
		this.padding_top = 50; this.padding_bottom = 30;
		this.padding_left = 30; this.padding_right = 30;
		table = new TableState(this.w, this.h);
		lastMoving = table.moving; lastMovingByType = table.movingByType.clone(); lastSunkByType = table.sunkByType.clone();

		// set up player state info
		turn = 0; groups = new int[]{-1, -1};

		// add cueball
		double radius = 3.35;
		Ball cue = new Ball(radius, Ball.TYPE_CUEBALL, 56, 56);
		table.addBall(cue);

		// add rack
		double distance = Math.sqrt(3)*radius; // distance between each column of balls on the rack
		int reds = 7; int blues = 7; int type;
		int xi = 168; int yi = 56;

		for(int j = 0; j < 5; j++){
			for(int k = 0; k <= j; k++){
				// determine what type of ball is going to be placed in this spot
				if (j == 2 && k == 1){type = Ball.TYPE_8BALL;} // 8 ball in center of rack
				else if (Math.random() < (double)(reds)/(reds+blues)){type = Ball.TYPE_RED; reds--;} // pick a red ball based on how many reds and blues are left
				else {type = Ball.TYPE_BLUE; blues--;} // pick a blue ball if we dont pick a red ball

				table.addBall(new Ball(radius, type, xi+j*distance, yi+k*radius*2-j*radius));
			}
		}
		
		// add walls
		double mid_w = this.w/2; double pr = 7; double pd = Math.sqrt(2*pr*pr)/2;
		// bounding walls (inner)
		table.addWall(new Wall(0, pr, 0, h-pr));       // TL to BL pocket
		table.addWall(new Wall(pr, h, mid_w-pd, h));   // BL to BM pocket
		table.addWall(new Wall(mid_w+pd, h, w-pr, h)); // BM to BR pocket 
		table.addWall(new Wall(w, h-pr, w, pr));       // BR to TR pocket
		table.addWall(new Wall(w-pr, 0, mid_w+pd, 0)); // TR to TM pocket
		table.addWall(new Wall(mid_w-pd, 0, pr, 0));   // TM to TL pocket

		// bounding walls (outer)
		double t = 18;
		table.addWall(new Wall(-t, -t, -t, h+t));
		table.addWall(new Wall(-t, h+t, w+t, h+t));
		table.addWall(new Wall(w+t, h+t, w+t, -t));
		table.addWall(new Wall(w+t, -t, -t, -t));

		// add pockets
		// top-left pocket
		Wall w1 = new Wall(pr, 0, 0, pr, true, false);
		Wall w2 = new Wall(0, pr, -pr, 0, true, true);  // the inner walls for each pocket always collide
		Wall w3 = new Wall(-pr, 0, 0, -pr, true, true); // this is to stop balls from going outside of the pocket in some cases
		Wall w4 = new Wall(0, -pr, pr, 0, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		ArrayList<Wall> p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));

		// bottom-left pocket
		w1 = new Wall(0, h-pr, pr, h, true, false);
		w2 = new Wall(pr, h, 0, h+pr, true, true);
		w3 = new Wall(0, h+pr, -pr, h, true, true);
		w4 = new Wall(-pr, h, 0, h-pr, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));

		// bottom-middle pocket
		w1 = new Wall(mid_w-pd, h, mid_w+pd, h, true, false);
		w2 = new Wall(mid_w+pd, h, mid_w+pd, h+pd*2, true, true);
		w3 = new Wall(mid_w+pd, h+pd*2, mid_w-pd, h+pd*2, true, true);
		w4 = new Wall(mid_w-pd, h+pd*2, mid_w-pd, h, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));

		// bottom-right pocket
		w1 = new Wall(w-pr, h, w, h-pr, true, false);
		w2 = new Wall(w, h-pr, w+pr, h, true, true);
		w3 = new Wall(w+pr, h, w, h+pr, true, true);
		w4 = new Wall(w, h+pr, w-pr, h, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));

		// top-right pocket
		w1 = new Wall(w, pr, w-pr, 0, true, false);
		w2 = new Wall(w-pr, 0, w, -pr, true, true);
		w3 = new Wall(w, -pr, w+pr, 0, true, true);
		w4 = new Wall(w+pr, 0, w, pr, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));

		// top-middle pocket
		w1 = new Wall(mid_w+pd, 0, mid_w-pd, 0, true, false);
		w2 = new Wall(mid_w-pd, 0, mid_w-pd, -pd*2, true, true);
		w3 = new Wall(mid_w-pd, -pd*2, mid_w+pd, -pd*2, true, true);
		w4 = new Wall(mid_w+pd, -pd*2, mid_w+pd, 0, true, true);
		table.addWall(w1); table.addWall(w2); table.addWall(w3); table.addWall(w4);
		p = new ArrayList<Wall>(Arrays.asList(w1, w2, w3, w4)); table.addPocket(new Pocket(p));
	}

	/**
	 * Moves the game forward a certain amount of time.
	 * 
	 * @param time the amount of time, in seconds, that the game is moved forward
	 */
	public void moveTime(double time){
		table.moveTime(time);

		// assign players their ball groups if they sink a ball and don't have groups yet
		if (groups[turn] == -1){
			if (this.table.sunkByType[Ball.TYPE_RED] > 0){
				groups[turn] = Ball.TYPE_RED; 
				groups[(turn+1)%2] = Ball.TYPE_BLUE;
			}
			if (this.table.sunkByType[Ball.TYPE_BLUE] > 0){
				groups[turn] = Ball.TYPE_BLUE; 
				groups[(turn+1)%2] = Ball.TYPE_RED;
			}
		}

		// change turn as soon as balls stop moving
		if (!table.moving && lastMoving){
			if (groups[turn] != -1){
				// only change turn if the player hasn't sunken a ball in their group
				if (this.table.sunkByType[groups[turn]] == lastSunkByType[groups[turn]]){
					turn = (turn+1)%2;
				}
			}
		 	lastSunkByType = table.sunkByType.clone();
		}

		lastMoving = this.table.moving; lastMovingByType = this.table.movingByType.clone();
	}

	/**
	 * Determines the color that the player text in the UI should be drawn in, based on
	 * their assigned ball group and whether they can hit/place the cueball right now.
	 * 
	 * @param player The player's number (either 0 or 1).
	 * @return       The color that that player's text should be drawn in.
	 */
	private Color getPlayerColor(int player){
		Color pColor;
		switch (this.groups[player]){ // adjust color based on what group of balls they're hitting
			case Ball.TYPE_RED: pColor = new Color(200, 7, 23); break;
			case Ball.TYPE_BLUE: pColor = new Color(10, 7, 200); break;
			default: pColor = Color.black; break;
		}
		// change color based on if they're allowed to place/hit the ball at the moment
		pColor = (this.turn==player && !this.table.moving) ? pColor : new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue(), (int)(0.4*255)); 
		return pColor;
	}

	/**
	 * Draws everything in the GameState onto a Graphics object as well as the UI.
	 * Will also determine scale, xOffset and yOffset in advance in order to handle
	 * isotropic scaling based on the width and height of the canvas.
	 * 
	 * @param g the Graphics object being drawn onto
	 * @param w the width of the canvas being drawn onto
	 * @param h the height of the canvas being drawn onto
	 */
	public void draw(Graphics g, int w, int h){
		double scale = getScale(w, h); double xOffset = getXOffset(w, h); double yOffset = getYOffset(w, h);

		// place player turn indicators on screen
		// TODO: this is all ad-hoc and really disgusting at the moment; maybe rewrite this so it can take in some arbitrary font size somehow
		Font UIFont = new Font("Arial", Font.BOLD, (int)(20*scale)); g.setFont(UIFont); 
		g.setColor(getPlayerColor(0)); g.drawString("P1", (int)(-19*scale + xOffset), (int)(-25*scale + yOffset));
		g.setColor(getPlayerColor(1)); g.drawString("P2", (int)((this.w-5)*scale+xOffset), (int)(-25*scale + yOffset));

		table.fillPolygon(g, scale, xOffset, yOffset, new Color(155, 126, 70), new int[]{0, 1, 2, 3, 4, 5, 10, 6, 7, 8, 9}); // wooden frame
		table.fillPolygon(g, scale, xOffset, yOffset, new Color(1, 162, 76), new int[]{0, 1, 2, 3, 4, 5});                   // felt playing field

		table.drawObjects(g, scale, xOffset, yOffset);
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
