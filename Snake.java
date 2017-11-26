import java.util.*;
import java.awt.*;
public class Snake implements Comparable<Snake>{
	//Regular constants for snake game
	private int direction = Grid.RIGHT_MOVE;
	private int length = 1;
	private int headX = 0;
	private int headY = 0;
	private ArrayDeque<Section> sections = new ArrayDeque<>();
	private Color c = Color.black;
	private boolean dead = false;
	//Genetic algorithm movement coefficients
	private double moveAdjust = 0.5; //Depending on block location
	private double sectionAdjust = 0.5; //Depending on snake collision location
	private double leftMove = 0.5;
	private double rightMove = 0.5;
	private double downMove = 0.5;
	private double upMove = 0.5;
	private double fitness = 0;
	private double totalTime = 0;
	private double penalty = 0;
	private int updatesForStalling = 0;

	/*
	 * Fitness function will be dependent on the length of the snake, 
	 * divided by a time, so the fastest snakes have higher fitness
	 */

	//food for this snake
	private Section foodBlock;

	public Snake(Snake s){
		length = 1;
		direction = Grid.RIGHT_MOVE;
		headX = 0;
		headY = 0;
		dead = false;
		sections = new ArrayDeque<>();
		sections.addFirst(new Section(headX, headY, direction, c));
		c = s.getColor();
		moveAdjust = s.getMoveAdjust();
		sectionAdjust = s.getSectionAdjust();
		leftMove = s.getLeftMove();
		rightMove = s.getRightMove();
		downMove = s.getDownMove();
		upMove = s.getUpMove();
		generateFood();
	}

	public Snake(){
		sections.addFirst(new Section(headX, headY, direction, c));
		generateFood();
	}
	public Snake(double moveAdjust, double sectionAdjust, double leftMove, double rightMove, double downMove, double upMove, Color c){
		sections.addFirst(new Section(headX, headY, direction, c));
		this.c = c;
		this.moveAdjust = moveAdjust;
		this.sectionAdjust = sectionAdjust;
		this.leftMove = leftMove;
		this.rightMove = rightMove;
		this.downMove = downMove;
		this.upMove = upMove;
		//System.out.println((moveAdjust +  sectionAdjust +  leftMove +  rightMove +  downMove + upMove)/6.0);
		generateFood();
	}

	//Movement
	public void update(){
		if(dead = getCollision()){
			updateFitness();
			return;
		}
		updateDirection();
		if(headX == foodBlock.getX() && headY == foodBlock.getY()){
			length++;
			generateFood();
			updatesForStalling = 0;
		} else if(sections.size() == length){
			sections.pollLast();
		}
		updatesForStalling++;
		totalTime+=0.01;
		/*if(updatesForStalling > 2 * Grid.BLOCKS_X * Grid.BLOCKS_Y && length <= 5){
			dead = true;
			updateFitness();
			return;
		}*/
		sections.addFirst(new Section(headX, headY, direction, c));
	}
	public void updateDirection(){
		nextDirection();
		if(direction == Grid.LEFT_MOVE)
			headX -= Grid.BLOCK_SIZE;
		else if(direction == Grid.UP_MOVE)
			headY -= Grid.BLOCK_SIZE;
		else if(direction == Grid.RIGHT_MOVE)
			headX += Grid.BLOCK_SIZE;
		else if(direction == Grid.DOWN_MOVE)
			headY += Grid.BLOCK_SIZE;
	}
	//Collision detection
	public boolean getCollision(){
		//Outside of grid
		if(headX >= Grid.WIDTH || headY >= Grid.HEIGHT || headX < 0 || headY < 0){
			return true;
		}
		//Self collision
		Iterator<Section> it = sections.iterator();
		it.next();
		while(it.hasNext()){
			if((it.next()).equals(sections.peekFirst())){
				return true;
			}
		}
		return false;
	}
	public boolean futureCollision(int headX, int headY){
		//Outside of grid
		if(headX >= Grid.WIDTH || headY >= Grid.HEIGHT || headX < 0 || headY < 0){
			return true;
		}
		//Self collision
		Iterator<Section> it = sections.iterator();
		Section s = new Section(headX, headY, -1, Color.black);
		it.next();
		while(it.hasNext()){
			if((it.next()).equals(s)){
				return true;
			}
		}
		return false;
	}
	//AI methods
	public void generateFood(){
		if(foodBlock == null)foodBlock = new Section(0, 0, -1, c);
		if(sections.size() == Grid.BLOCKS_X * Grid.BLOCKS_Y){
			dead = true;
			return;
		}
		do{
			foodBlock.setX((int)(Math.random()*Grid.BLOCKS_X-1)*Grid.BLOCK_SIZE);
			foodBlock.setY((int)(Math.random()*Grid.BLOCKS_Y-1)*Grid.BLOCK_SIZE);
		}while(sections.contains(foodBlock));
	}
	//using adjustment values, find the direction the snake SHOULD go
	private void nextDirection(){
		int moveY = ((foodBlock.getY()/Grid.BLOCK_SIZE) - (headY/Grid.BLOCK_SIZE));
		int moveX = ((foodBlock.getX()/Grid.BLOCK_SIZE) - (headX/Grid.BLOCK_SIZE));

		//FUTURE: Try changing the collide to spot high concentration of blocks vs shortest distance

		int collideY = Integer.MAX_VALUE;
		int actY = Integer.MAX_VALUE;
		int collideX = Integer.MAX_VALUE;
		int actX = Integer.MAX_VALUE;
		for(Section s : sections){
			if(s.getDirection() != direction){
				collideY = Math.abs((s.getY() - headY))<actY?(s.getY() - headY)/Grid.BLOCK_SIZE:collideY;
				actY = Math.min(Math.abs(s.getY() - headY), actY);
				collideX = Math.abs((s.getX() - headX))<actX?(s.getX() - headX)/Grid.BLOCK_SIZE:collideX;
				actX = Math.min(Math.abs(s.getX() - headX), actX);
			}
		}
		//ends
		collideY = Math.abs((0 - headY))<actY?(0 - headY)/Grid.BLOCK_SIZE:collideY;
		actY = Math.min(Math.abs(0 - headY), actY);
		collideY = Math.abs((Grid.HEIGHT - headY))<actY?(Grid.HEIGHT - headY)/Grid.BLOCK_SIZE:collideY;
		actY = Math.min(Math.abs(Grid.HEIGHT - headY), actY);

		collideX = Math.abs((0 - headX))<actX?(0 - headX)/Grid.BLOCK_SIZE:collideX;
		actX = Math.min(Math.abs(0 - headX), actX);
		collideX = Math.abs((Grid.WIDTH - headX))<actX?(Grid.WIDTH - headX)/Grid.BLOCK_SIZE:collideX;
		actX = Math.min(Math.abs(Grid.WIDTH - headX), actX);

		int direction = Grid.RIGHT_MOVE;
		double maximumY = 0;
		int directionY = Grid.DOWN_MOVE;
		double maximumX = 0;
		int directionX = Grid.RIGHT_MOVE;
		//check for Y
		int dY = (moveY > 0)?Grid.DOWN_MOVE:Grid.UP_MOVE; //Moving towards food
		int cY = (collideY > 0)?Grid.UP_MOVE:Grid.DOWN_MOVE; //Moving away from collision section

		int dYBackup = (dY == Grid.DOWN_MOVE)?Grid.UP_MOVE:Grid.DOWN_MOVE; //Check the other directions
		int cYBackup = (cY == Grid.DOWN_MOVE)?Grid.UP_MOVE:Grid.DOWN_MOVE; //Check the other directions

		double dvY = (downMove*moveAdjust)-((double)(Math.abs(moveY)*moveAdjust)/(double)Grid.WIDTH); //Direction values
		double dvYBackup = (upMove+moveAdjust)-((double)(Math.abs(moveY)*moveAdjust)/(double)Grid.WIDTH); //Direction values
		dvY = (futureCollision(headX, headY+Grid.BLOCK_SIZE)?Integer.MIN_VALUE:dvY); //Check if collision, if true set value to be low
		if(dY == Grid.UP_MOVE){
			dvY = (upMove*moveAdjust)-((double)(Math.abs(moveY)*moveAdjust)/(double)Grid.WIDTH);
			dvYBackup = (downMove+moveAdjust)-((double)(Math.abs(moveY)*moveAdjust)/(double)Grid.WIDTH);
			dvY = (futureCollision(headX, headY-Grid.BLOCK_SIZE)?Integer.MIN_VALUE:dvY);
		}

		double cvY = (downMove*sectionAdjust)-((double)(Math.abs(collideY)*sectionAdjust)/(double)Grid.WIDTH);
		double cvYBackup = (upMove+sectionAdjust)-((double)(Math.abs(collideY)*sectionAdjust)/(double)Grid.WIDTH);
		cvY = (futureCollision(headX, headY+Grid.BLOCK_SIZE)?Integer.MIN_VALUE:cvY);
		if(cY == Grid.UP_MOVE){
			cvY = (upMove*sectionAdjust)-((double)(Math.abs(collideY)*sectionAdjust)/(double)Grid.WIDTH);
			cvYBackup = (downMove+sectionAdjust)-((double)(Math.abs(collideY)*sectionAdjust)/(double)Grid.WIDTH);
			cvY = (futureCollision(headX, headY-Grid.BLOCK_SIZE)?Integer.MIN_VALUE:cvY);
		}

		if(dvY > cvY){
			maximumY = dvY;
			directionY = dY;
		} else {
			maximumY = cvY;
			directionY = cY;
		}
		if(dvYBackup > maximumY){
			maximumY = dvYBackup;
			directionY = dYBackup;
		}
		if(cvYBackup > maximumY){
			maximumY = cvYBackup;
			directionY = cYBackup;
		}
		//check for X
		int dX = (moveX > 0)?Grid.RIGHT_MOVE:Grid.LEFT_MOVE; //Moving in the direction of x towards the food
		int cX = (collideX > 0)?Grid.LEFT_MOVE:Grid.RIGHT_MOVE; //Moving opposite direction to closest collision section

		int dXBackup = (dX == Grid.RIGHT_MOVE)?Grid.LEFT_MOVE:Grid.RIGHT_MOVE; //Check the other directions
		int cXBackup = (dX == Grid.RIGHT_MOVE)?Grid.LEFT_MOVE:Grid.RIGHT_MOVE; //Check the other directions

		double dvX = (rightMove*moveAdjust)-((double)(Math.abs(moveX)*moveAdjust)/(double)Grid.WIDTH);
		double dvXBackup = (leftMove+moveAdjust)-((double)(Math.abs(moveX)*moveAdjust)/(double)Grid.WIDTH);
		dvX = (futureCollision(headX+Grid.BLOCK_SIZE, headY)?Integer.MIN_VALUE:dvX);
		if(dX == Grid.LEFT_MOVE){
			dvX = (leftMove*moveAdjust)-((double)(Math.abs(moveX)*moveAdjust)/(double)Grid.WIDTH);
			dvXBackup = (rightMove+moveAdjust)-((double)(Math.abs(moveX)*moveAdjust)/(double)Grid.WIDTH);
			dvX = (futureCollision(headX-Grid.BLOCK_SIZE, headY)?Integer.MIN_VALUE:dvX);
		}

		double cvX = (rightMove*sectionAdjust)-((double)(Math.abs(collideX)*sectionAdjust)/(double)Grid.WIDTH);
		double cvXBackup = (leftMove+sectionAdjust)-((double)(Math.abs(collideX)*sectionAdjust)/(double)Grid.WIDTH);
		cvX = (futureCollision(headX+Grid.BLOCK_SIZE, headY)?Integer.MIN_VALUE:cvX);
		if(cX == Grid.LEFT_MOVE){
			cvX = (leftMove*sectionAdjust)-((double)(Math.abs(collideX)*sectionAdjust)/(double)Grid.WIDTH);
			cvXBackup = (rightMove+moveAdjust)-((double)(Math.abs(moveX)*moveAdjust)/(double)Grid.WIDTH);
			cvX = (futureCollision(headX-Grid.BLOCK_SIZE, headY)?Integer.MIN_VALUE:cvX);
		}

		if(dvX > cvX){
			maximumX = dvX;
			directionX = dX;
		} else {
			maximumX = cvX;
			directionX = cX;
		}
		if(dvXBackup > maximumX){
			maximumX = dvXBackup;
			directionX = dXBackup;
		}
		if(cvXBackup > maximumX){
			maximumX = cvXBackup;
			directionX = cXBackup;
		}

		//Backup direction for when no direction is picked
		int backupDirection;
		if(!futureCollision(headX-Grid.BLOCK_SIZE, headY))backupDirection = Grid.LEFT_MOVE;
		else if(!futureCollision(headX+Grid.BLOCK_SIZE, headY))backupDirection = Grid.RIGHT_MOVE;
		else if(!futureCollision(headX, headY-Grid.BLOCK_SIZE))backupDirection = Grid.UP_MOVE;
		else backupDirection = Grid.DOWN_MOVE;

		//Pick the best direction the snake should go
		if(maximumX > maximumY && this.direction != -directionX)
			direction = directionX;
		else if(directionY != -this.direction){
			direction = directionY;
		} else {
			direction = backupDirection;
		}

		//Set this direction
		this.direction = direction;
	}

	public void updateFitness(){
		fitness = Math.pow((double)length, 2)-(totalTime/10000.0);
	}
	public Object[] getSections(){
		return sections.toArray();
	}
	public Section getFood(){
		return foodBlock;
	}
	public boolean isDead(){
		return dead;
	}
	public Color getColor(){
		return c;
	}
	public double getFitness(){
		return fitness;
	}
	public int getDirection(){
		return direction;
	}
	public int getLength(){
		return length;
	}
	public void setDirection(int direction){
		this.direction = direction;
	}
	public double getMoveAdjust(){
		return moveAdjust;
	}
	public double getSectionAdjust(){
		return sectionAdjust;
	}
	public double getLeftMove(){
		return leftMove;
	}
	public double getRightMove(){
		return rightMove;
	}
	public double getDownMove(){
		return downMove;
	}
	public double getUpMove(){
		return upMove;
	}
	public void setMoveAdjust(double moveAdjust){
		this.moveAdjust = moveAdjust;
	}
	public void setSectionAdjust(double sectionAdjust){
		this.sectionAdjust = sectionAdjust;
	}
	public void setLeftMove(double leftMove){
		this.leftMove = leftMove;
	}
	public void setRightMove(double rightMove){
		this.rightMove = rightMove;
	}
	public void setDownMove(double downMove){
		this.downMove = downMove;
	}
	public void setUpMove(double upMove){
		this.upMove = upMove;
	}

	@Override
	public int compareTo(Snake s){
		if(this.fitness > s.fitness)
			return -1;
		else if(this.fitness < s.fitness)
			return 1;
		return 0;
	}
}