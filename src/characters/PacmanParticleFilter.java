package characters;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Random;

import graphics.Sonar;
import util.Coords;
import util.WeightedSet;

/**
* The version of Pacman that uses particle filtering to estimate the ghost's current location
*
* Please fill in the methods below and do not alter them (for the sake of my unit tests).
* Feel free to add whatever private/protected methods you need.
*
*
* @author Sarah Walling-Bell
* @version 4/11/19
*/
public class PacmanParticleFilter extends Pacman{
	private int N = 200;
	private Coords[] samples;

	/**
	* Creates a new Pacman agent that uses particle filtering
	* @param size
	* 			The size of the grid (e.g. 10 rows/columns)
	*
	* @param images
	* 			Images of Pacman in the 4 cardinal directions
	*
	* @param sonar
	* 			Pacman's sonar apparatus (i.e. his ears)
	*/
	public PacmanParticleFilter(int size, BufferedImage[] images, Sonar sonar) {
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


		//Randomly sample N times from the prior distribution
		samples = new Coords[N];
		for(int i = 0; i < samples.length; i++){
			samples[i] = belief.sample();
		}
	}


	/**
	* Elapses each sample forward using the transition distribution
	*
	* @param samples
	* 				The current set of samples
	* @return
	* 				An updated set of samples
	*/
	protected Coords[] elapse(Coords[] samples) {

		for(int i = 0; i < N; i++){
			Coords oldSample = samples[i];

			samples[i] = newSample(oldSample);
		}

		return samples;
	}


	private Coords newSample(Coords oldSample){

			//Get the list of legal directions and neighbors for the sample
			// List<Integer> legalDirs  = getLegalDirections(samples[i]);
			List<Coords> neighbors = getLegalNeighbors(oldSample);

			//add option not to move
			neighbors.add(oldSample);

			//generate random between 0 and 1.
			Random random = new Random();
			int rand = random.nextInt(neighbors.size());

			return neighbors.get(rand);
	}

	/**
	* Weights each sample using the emission distribution
	*
	* @param samples
	* 			A set of samples
	*
	* @param noisyDistance
	* 			The noisy distance between Pacman and the ghost
	*
	* @param pacmanLocation
	* 			Pacman's location
	*
	* @return
	* 			The weights for each sample
	*
	*/
	protected double[] weight(Coords[] samples, int noisyDistance, Coords pacmanLocation) {

		//a set of new weights associated with samples
		double[] weights = new double[N];

		for (int i = 0; i < N; i++){

			//Update the weight of each sample in the WeightedSet belief to the
			//noisyDistance in the emmision table for that sample.
			int manhatDist = sonar.manhattanDistance(pacmanLocation, samples[i]);
			double[] eTbl = sonar.getEmissionTable(manhatDist);
			weights[i] = eTbl[noisyDistance];

			//Update the weight in the belief Weighted Set
			belief.increment(samples[i], weights[i]);

		}
		belief.normalize();

		return weights;
	}

	/**
	* Resamples a new set of unweighted samples
	*
	* @param samples
	* 				A set of samples
	*
	* @param weights
	* 				The weights for each sample
	* @return
	* 			A new set of unweighted samples
	*/
	protected Coords[] resample(Coords[] samples, double[] weights) {

		//Sample N new samples based on the weights of the old samples.
		//(e.g., samples with higher weights are more likely to be sampled again)
		for (int i = 0; i < N; i++){
			samples[i] = belief.sample();
		}

		return samples;
	}

	/**
	* Updates Pacman's belief of the ghost's location
	*
	* @param noisyDistance
	* 			A noisy distance reading -- i.e., the noisy distance from Pacman to the ghost
	*/
	public void update(int noisyDistance){

		//propogate a new set of samples through time. (e.g., given the Samples
		// at time t-1, gather a set of new samples at time t)
		samples = elapse(samples);
		//update the weights of the samples
		double[] weights = weight(samples, noisyDistance, location);
		//resample according to the weights

		samples = resample(samples, weights);

		//TODO update belief... create new WeightedSet based on samples and set belief to that??

	}
}
