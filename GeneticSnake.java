import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import javax.imageio.ImageIO;
public class GeneticSnake{
	//pretty simple flappy bird game
	static JFrame f;
	static DrawPanel p;
	static int width = Grid.WIDTH;
	static int height = Grid.HEIGHT;
	static boolean autoGenerate = false;
	static boolean viewCurrentBest = false;
	static Snake currentBest;
	static int tick = 2;
	static int total = 0;
	static boolean setTime = true;

	public static void init(){
		f = new JFrame();
		p = new DrawPanel();
		p.addKeyListener(p); // testing
		f.setSize(width, height);
		p.setFocusable(true);
		f.add(p);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	static class DrawPanel extends JPanel implements KeyListener{
		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.black);
        	for(int i = 1; i < Grid.BLOCKS_X; i++)
        		g.drawLine(i*Grid.BLOCK_SIZE, 0, i*Grid.BLOCK_SIZE, Grid.HEIGHT);
        	for(int i = 1; i < Grid.BLOCKS_Y; i++)
        		g.drawLine(0, i*Grid.BLOCK_SIZE, Grid.WIDTH, i*Grid.BLOCK_SIZE);
        	if(viewCurrentBest){
        		g.setColor(currentBest.getColor());
        		Object[] secs = currentBest.getSections();
	        		for(int i = 0; i < secs.length; i++){
	        			Section sc = (Section)secs[i];
	        			g.fillRect(sc.getX(), sc.getY(), Grid.BLOCK_SIZE, Grid.BLOCK_SIZE);
	        		}
        		g.fillOval(currentBest.getFood().getX(), currentBest.getFood().getY(), Grid.BLOCK_SIZE, Grid.BLOCK_SIZE);
        	} else {
	        	for(Snake s : GeneticEvolution.getPopulation()){
	        		g.setColor(s.getColor());
	        		if(s.isDead()){
	        			GeneticEvolution.updateFinished(s);
	        		}
	        		Object[] secs = s.getSections();
	        		for(int i = 0; i < secs.length; i++){
	        			Section sc = (Section)secs[i];
	        			if(sc!=null)g.fillRect(sc.getX(), sc.getY(), Grid.BLOCK_SIZE, Grid.BLOCK_SIZE);
	        		}
	        		g.fillOval(s.getFood().getX(), s.getFood().getY(), Grid.BLOCK_SIZE, Grid.BLOCK_SIZE);
	        	}
	        	for(Snake s : GeneticEvolution.getFinished())
	        		GeneticEvolution.removePopulation(s);
        	}
        	Font f = new Font ("Monospaced", Font.BOLD, 20);
            g.setFont(f);
        	g.setColor(Color.red);
        	g.drawString("Generation: " + String.valueOf(GeneticEvolution.getGeneration()), 10, 20);
        	g.setColor(Color.red);
        	g.drawString("Length / Fitness: " + String.valueOf(GeneticEvolution.getMaxLength()) + " " + String.valueOf(GeneticEvolution.getCurrentAvg()), 10, Grid.HEIGHT - 10);
        }
      	public void keyPressed(KeyEvent e){
      		//Testing
      		/*if(e.getKeyCode() == KeyEvent.VK_A){
      			if(GeneticEvolution.getPopulation().size() > 0)GeneticEvolution.getPopulation().get(0).setDirection(Grid.LEFT_MOVE);
      		} else if(e.getKeyCode() == KeyEvent.VK_D){
      			if(GeneticEvolution.getPopulation().size() > 0)GeneticEvolution.getPopulation().get(0).setDirection(Grid.RIGHT_MOVE);
      		} else if(e.getKeyCode() == KeyEvent.VK_W){
      			if(GeneticEvolution.getPopulation().size() > 0)GeneticEvolution.getPopulation().get(0).setDirection(Grid.UP_MOVE);
      		} else if(e.getKeyCode() == KeyEvent.VK_S){
      			if(GeneticEvolution.getPopulation().size() > 0)GeneticEvolution.getPopulation().get(0).setDirection(Grid.DOWN_MOVE);
      		}*/
      		if(e.getKeyCode() == KeyEvent.VK_SPACE){
      			GeneticEvolution.overrideNextGeneration();
      		} else if(e.getKeyCode() == KeyEvent.VK_A){
      			autoGenerate = !autoGenerate;
      		} else if(e.getKeyCode() == KeyEvent.VK_S){
      			viewCurrentBest = !viewCurrentBest;
      		} else if(e.getKeyCode() == KeyEvent.VK_R){
      			GeneticEvolution.reset();
      		} else if(e.getKeyCode() == KeyEvent.VK_E){
      			if(tick == 2)tick = 50;
      			else tick = 2;
      		} else if(e.getKeyCode() == KeyEvent.VK_T){
      			setTime = !setTime;
      		} else if(e.getKeyCode() == KeyEvent.VK_G){
      			drawImage();
      		}
      	}
	    public void keyReleased(KeyEvent e){}
	    public void keyTyped(KeyEvent e){} 	
	}
	public static void drawImage(){
		try{
			ArrayList<Integer> vals = GeneticEvolution.getAverageFitness();
			File output = new File("avg_fitness_graph3.png");
			int max = 0;
			for(Integer i : vals)
				if(i > max)
					max = i;
			int scale = 1;
			while(Math.abs(max / scale - 700) > 250){
				scale ++;
			}
			int xMultiplier = 1;
			while(Math.abs(GeneticEvolution.getGeneration() * xMultiplier - 750) > 250 )
				xMultiplier ++;
			BufferedImage image = new BufferedImage(GeneticEvolution.getGeneration() * xMultiplier, max / scale, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setPaint(Color.WHITE);
			graphics.fillRect(0, 0, GeneticEvolution.getGeneration() * xMultiplier, max / scale);
			graphics.setPaint(Color.RED);
			for(int i = 1; i < vals.size(); i ++)
				graphics.drawLine((i - 1) * xMultiplier, (max - vals.get(i - 1)) / scale, i * xMultiplier, (max - vals.get(i)) / scale);
			ImageIO.write(image, "png", output);
		} catch (Exception e){
			System.out.println("Error generating graph");
		}
	}
	public static void updatePopulation(){
		for(Snake s : GeneticEvolution.getPopulation())
			s.update();
	}
	public static void update(){
		try{
			if(!viewCurrentBest){
				currentBest = null;
				if(autoGenerate){
					if(setTime){
						if(total / 6 < 1000){
							GeneticEvolution.initNextGeneration();
						} else {
							GeneticEvolution.overrideNextGeneration();
							total = 0;
						}
					} else {
						GeneticEvolution.initNextGeneration();
					}
				}
				updatePopulation();
			} else {
				//Viewing the current best snake
				if(currentBest == null || currentBest.isDead()){
					if(GeneticEvolution.getPopulation().size() > GeneticEvolution.getFinished().size()){
						ArrayList<Snake> sorted = new ArrayList<>(GeneticEvolution.getPopulation());
						Collections.sort(sorted);
						currentBest = new Snake(sorted.get(0));
					} else {
						ArrayList<Snake> sorted = new ArrayList<>(GeneticEvolution.getFinished());
						Collections.sort(sorted);
						currentBest = new Snake(sorted.get(0));
					}
				} else {
					currentBest.update();
				}
			}
			f.getToolkit().sync();
			p.repaint();
		}catch(Exception e){}
	}
	public static void main(String args[]){
		init();
		while(true){
			update();
			total += tick;
			try{
				Thread.sleep(tick);
			}catch(Exception e){}
		}
	}
}
