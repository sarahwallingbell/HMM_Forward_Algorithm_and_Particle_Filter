package characters;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import graphics.Sonar;
import util.Coords;
import util.WeightedSet;


/**
* The version of Pacman that uses the Forward Algorithm to estimate the ghost's current location
*
* Please fill in the methods below and do not alter them (for the sake of my unit tests).
* Feel free to add whatever private/protected methods you need.
*
* @author Sarah Walling-Bell
* @version 4/12/19
*/
public class PacmanForwardAlgorithm extends Pacman{

	/**
	* Creates a new Pacman agent that uses the Forward Algorithm
	* @param size
	* 			The size of the grid (e.g. 10 rows/columns)
	*
	* @param images
	* 			Images of Pacman in the 4 cardinal directions
	*
	* @param sonar
	* 			Pacman's sonar apparatus (i.e. his ears)
	*/
	public PacmanForwardAlgorithm(int size, BufferedImage[] images, Sonar sonar) {
		super(size, images, sonar);

		//compute the prior distribution. initialize p(G0), a uniform distribution over the cells.
		belief = new WeightedSet();
		for(int r = 0; r < size; r++){
			for(int c = 0; c < size; c++){
				Coords coords = new Coords(r, c);
				belief.addEvent(coords, 1);
			}
		}
		belief.normalize();


	}

	/**
	* Updates Pacman's belief of the ghost's location.
	*
	* @param noisyDistance
	* 			A noisy distance reading -- i.e., the noisy distance from Pacman to the ghost
	*/
	public void update(int noisyDistance){

			WeightedSet<Coords> temp = new WeightedSet<Coords>();

			Set<Coords> set_p = belief.getElements(); //Xt
			Set<Coords> set_q = belief.getElements(); //Xt-1

			// Compute the distribution over set_p (Xt)
			for(Coords p : set_p){
				double sum = 0;

				for(Coords q : set_q){
					//Compute the transition distribution and multiply that by weight of q
					sum += sumHelper(p, q) * belief.getWeight(q); // sum += p(Xt = p | Xt-1 = q) * F(q)
				}

				//Compute the emmision distribution and multiply it by sum to get a new weight for Coords p
				double newWeight = newWeightHelper(p, noisyDistance) * sum;
				temp.addEvent(p, newWeight);

			}
			temp.normalize();

			belief = temp;
	}


	/**
	* Compute the emmision distribution p(et | Xt = p) where et is out noisyDistance.
	*
	* @param p
	* 			A Coords at time Xt
	* @param noisyDistance
	* 			A noisy distance reading -- i.e., the noisy distance from Pacman to the ghost
	* @return
	*				the emmision distribution
	*/
	private double newWeightHelper(Coords p, int noisyDistance){

		//compute the manhattanDistance from where pacman is to coord1 (where ghost might be)
		int manhatDist = sonar.manhattanDistance(location, p);

		//get the emission table of the manhattanDistance from sonar.java
		//emmision table is the set of probabilities of it being a true distance given a noisy distance.
		double[] eTbl = sonar.getEmissionTable(manhatDist);

		//get the probability of true distance given our noisyDistance
		return eTbl[noisyDistance];
	}


	/**
	* Compute the transition distribution p(Xt = p | Xt-1 = q)
	*
	* @param p
	* 			A Coords at time Xt
	* @param q
	*				A Coords at time Xt-1
	* @return
	*				the tranistion distrubution
	*/
	private double sumHelper(Coords p, Coords q){

		//figure out if the coords are neighbors
		List<Coords> neighbors  = getLegalNeighbors(q); //list of coord2's neighbor coordinates

		if(neighbors.contains(p)){
			double numMoves = neighbors.size() + 1;
			return 1.0/numMoves;
		}
		return 0.0; //This should be 0.0 but it's incompatible with her code. Packman.java move method. If we put 0.0 there's a chance we never enter the if statement.

	}
}
