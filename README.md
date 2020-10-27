# PoolGame
PoolGame is an implementation of 8-ball written in Java for an SE 4361 group project which models elastic collisions and manages game logic seamlessly.

## Setup
### Prerequisites
This project requires JDK to compile and JRE to run properly.

### Build Steps
1. Compile all .java files in the directory with the following command.
```bash
javac *.java
```
2. Run the following command to package everything into a .jar file.
```bash
jar cfe PoolGame.jar PoolGame *.class
```
3. Run PoolGame.jar.
```bash
java -jar PoolGame.jar
```

## Usage
### Controls
#### Shooting Cue Ball
1. Click anywhere in the window and drag the mouse cursor back. 
2. Release the cursor to fire the cueball.

#### Placing Cue Ball
1. Click anywhere in the window to get the placement indicator to appear. 
2. Move the mouse cursor to a valid location (invalid locations will be marked red as you hover over them)
3. Click again to place the cue ball in that spot.

#### Resetting Game
1. Right click the mouse to reset the game.

### Rules
#### Win Condition
* Players win whenever they first pocket all the balls in their group and then the 8-ball without committing a foul.

#### Gameplay
* Players take turns shooting the cue ball. It's a given player's turn whenever their indicator is highlighted.
* Players are assigned a group based on whichever type of ball they pocket first (red or blue).
* A player's turn ends whenever they fail to pocket a ball in their group or they commit a foul.
* If a player commits a foul, their opponent is allowed to place the cue ball anywhere on the table before their turn.

#### Fouls
* Player fails to strike a ball in their group first before hitting any other ball.
* Player fails to strike any balls.
* Player pockets the cue ball.