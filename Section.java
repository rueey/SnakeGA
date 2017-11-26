import java.awt.*;
import java.util.*;
public class Section{
	private int x = -1;
	private int y = -1;
	private int direction;
	private Color c;
	public Section(int x, int y, int direction, Color c){
		this.x = x;
		this.y = y;
		this.c = c;
		this.direction = direction;
	}
	@Override
	public boolean equals(Object o){
		Section obj = (Section)o;
		return (obj.getX() == this.x && obj.getY() == this.y);
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public Color getColor(){
		return c;
	}
	public int getDirection(){
		return direction;
	}
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public void setColor(Color c){
		this.c = c;
	}
}