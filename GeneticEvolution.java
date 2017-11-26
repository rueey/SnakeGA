import java.util.*;
import java.awt.*;
public class GeneticEvolution {

	/* 
	 * Genetic evolution algorithm for snake game
	 * Using tournament selection, and continuous values (float) for genotype representation
	 * Created November 25, 2017 by Rui Li
	 */

	private static ArrayList<Snake> population = new ArrayList<>();
	private static ArrayList<Snake> finished = new ArrayList<>();
	private static int totalSnakes = 150;
	private static ArrayList<Color> usedColors = new ArrayList<>();
	private static ArrayList<Integer> averageFitness = new ArrayList<>();

	//some constants for mutation and crossover
	private static final int K_PARENTS = 5;
	private static final double MUTATE_PROBABILITY = 0.15;
	private static final double PARENT_BIAS = 0.70;
	private static final double CROSSOVER_PROBABILITY = 0.85;
	private static int generation = 0;
	private static int maxLength = 0;
	private static int currentAvg = 0;

	public static void initNextGeneration(){
		if(population.size() == 0){
			generation++;
			if(finished.size() == 0){
				//first generation
				for(int i = 0; i < totalSnakes; i++)
					createRandomOffspring();
				usedColors.clear();
			} else {
				//perform crossovers and mutations
				//First get fitnesses
				for(Snake s : finished)
					s.updateFitness();
				//All rebreed, with 1 child each
				double totalFitness = 0;
				ArrayList<Snake> avgFitnessList = new ArrayList<>(finished);
				Collections.sort(avgFitnessList);
				for(int i = 0; i < (int)(0.25*avgFitnessList.size()); i++)
					totalFitness += avgFitnessList.get(i).getFitness();
				for(int i = 0; i < finished.size(); i++){
					//update length
					maxLength = Math.max(maxLength, finished.get(i).getLength());
					boolean[] viz = new boolean[finished.size()];
					Snake parent1 = tournamentSelection(generateKParents(viz));
					Snake parent2 = tournamentSelection(generateKParents(viz));
					//see if we create crossover or nah
					if(Math.random() > CROSSOVER_PROBABILITY){
						createRandomOffspring();
					} else {
						double fitness1 = parent1.getFitness();
						double fitness2 = parent2.getFitness();
						double moveAdjust = getAllele(parent1.getMoveAdjust(), fitness1, parent2.getMoveAdjust(), fitness2);
						double sectionAdjust = getAllele(parent1.getSectionAdjust(), fitness1, parent2.getSectionAdjust(), fitness2);
						double leftMove = getAllele(parent1.getLeftMove(), fitness1, parent2.getLeftMove(), fitness2);
						double rightMove = getAllele(parent1.getRightMove(), fitness1, parent2.getRightMove(), fitness2);
						double downMove = getAllele(parent1.getDownMove(), fitness1, parent2.getDownMove(), fitness2);
						double upMove = getAllele(parent1.getUpMove(), fitness1, parent2.getUpMove(), fitness2);
						Color currColor = Color.black;
						do{
							int r = (int)(Math.random()*256);
							int g = (int)(Math.random()*256);
							int b = (int)(Math.random()*256);
							currColor = new Color(r, g, b);
						} while(usedColors.contains(currColor));
						Snake offSpring = new Snake(moveAdjust, sectionAdjust, leftMove, rightMove, downMove, upMove, currColor);
						if(Math.random() < MUTATE_PROBABILITY)
							mutate(offSpring, ((parent1.getFitness() > parent2.getFitness())?parent1:parent2), ((parent1.getFitness() > parent2.getFitness())?parent2:parent1));
						population.add(offSpring);
					}
				}
				currentAvg = (int)Math.round(totalFitness/(0.25*avgFitnessList.size()));
				averageFitness.add(currentAvg);
				usedColors.clear();
				finished.clear();
			}
		}
	}
	public static void overrideNextGeneration(){
		for(Snake s : population)
			finished.add(s);
		for(Snake s : finished)
			if(population.contains(s))population.remove(s);
		population.clear();
		initNextGeneration();
	}
	public static void createRandomOffspring(){
		Color currColor = Color.black;
		do{
			int r = (int)(Math.random()*256);
			int g = (int)(Math.random()*256);
			int b = (int)(Math.random()*256);
			currColor = new Color(r, g, b);
		} while(usedColors.contains(currColor));
		//Create snake with random values
		//All alleles have a float value between 0-1
		Snake s = new Snake(Math.random()*6, Math.random()*6, Math.random()*6, Math.random()*6, Math.random()*6, Math.random()*6, currColor);
		population.add(s);
	}
	public static double getAllele(double adjust1, double fitness1, double adjust2, double fitness2){
		double ret = adjust1;
		if(fitness2 > fitness1){
			if(Math.random() > PARENT_BIAS){
				ret = adjust2;
			}
		}
		return ret;
	}
	public static ArrayList<Snake> generateKParents(boolean[] viz){
		ArrayList<Snake> selection = new ArrayList<>();
		for(int j = 0; j < K_PARENTS; j++){
			int idx = (int)(Math.random()*finished.size());
			if(!viz[idx]){
				selection.add(finished.get(idx));
				viz[idx] = true;
			}
		}
		return selection;
	}
	public static Snake tournamentSelection(ArrayList<Snake> selected){
		Snake max = new Snake();
		for(Snake s : selected){
			if(s.getFitness() > max.getFitness())
				max = s;
		}
		return max;
	}
	public static void mutate(Snake s, Snake parent1, Snake parent2){
		//parent1 is the one with best fitness
		double moveAdjust = Math.random();
		double sectionAdjust = Math.random();
		double leftMove = Math.random();
		double rightMove = Math.random();
		double downMove = Math.random();
		double upMove = Math.random();
		s.setMoveAdjust(s.getMoveAdjust() + ((Math.random() > 0.5 && s.getMoveAdjust() - moveAdjust > 0)?(-1 * moveAdjust):moveAdjust));
		s.setSectionAdjust(s.getSectionAdjust() + ((Math.random() > 0.5 && s.getSectionAdjust() - sectionAdjust > 0)?(-1 * sectionAdjust):sectionAdjust));
		s.setLeftMove(s.getLeftMove() + ((Math.random() > 0.5 && s.getLeftMove() - leftMove > 0)?(-1 * leftMove):leftMove));
		s.setRightMove(s.getRightMove() + ((Math.random() > 0.5 && s.getRightMove() - rightMove > 0)?(-1 * rightMove):rightMove));
		s.setDownMove(s.getDownMove() + ((Math.random() > 0.5 && s.getDownMove() - downMove > 0)?(-1 * downMove):downMove));
		s.setUpMove(s.getUpMove() + ((Math.random() > 0.5 && s.getUpMove() - upMove > 0)?(-1 * upMove):upMove));
	}
	public static void reset(){
		population = new ArrayList<>();
		finished = new ArrayList<>();
	}
	public static void updateFinished(Snake s){
		finished.add(s);
	}
	public static void removePopulation(Snake s){
		if(population.contains(s))
			population.remove(s);
	}
	public static ArrayList<Snake> getPopulation(){
		return population;
	}
	public static ArrayList<Snake> getFinished(){
		return finished;
	}
	public static ArrayList<Integer> getAverageFitness(){
		return averageFitness;
	}
	public static int getGeneration(){
		return generation;
	}
	public static int getMaxLength(){
		return maxLength;
	}
	public static int getCurrentAvg(){
		return currentAvg;
	}
}