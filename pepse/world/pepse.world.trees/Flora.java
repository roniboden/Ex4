package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Layer;
import pepse.world.Block;
import pepse.world.GroundHeightProvider;
import pepse.world.LayerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Plants trees (trunk + leaves + fruit) in a specified horizontal range and
 * returns every part together with its preferred layer.
 *
 * <p>All randomness is coordinate-deterministic: every decision is derived
 * from {@code Objects.hash(x, salt, worldSeed)}, so the same seed and the
 * same {@code x}-coordinate always recreate the identical tree, no matter
 * the call order.</p>
 */
public class Flora {

	/**
	 * Immutable pair <em>(layer, object)</em> returned by
	 * {@link #createInRange(int, int)}.
	 *
	 * @param layer the Danogl layer into which the object should be inserted
	 * @param obj   the {@link GameObject} instance
	 */
	public record LayeredObject(int layer, GameObject obj) {}

	/** Probability to plant a tree in a given block column. */
	private static final double TREE_PROBABILITY = 0.10;

	private final GroundHeightProvider groundHeightProvider;
	private final int worldSeed;

	/**
	 * @param groundHeightProvider supplier of surface Y for any {@code x}
	 * @param seed                 world seed for deterministic generation
	 */
	public Flora(GroundHeightProvider groundHeightProvider, int seed) {
		this.groundHeightProvider = groundHeightProvider;
		this.worldSeed            = seed;
	}

	/** Deterministic RNG keyed by (x, salt, worldSeed). */
	private Random rngForX(int x, int salt) {
		return new Random(Objects.hash(x, salt, worldSeed));
	}

	/**
	 * Generates all tree parts whose trunk centre lies in {@code [minX,maxX)}
	 * and returns them with their target layer.
	 *
	 * @param minX inclusive left bound, in world pixels
	 * @param maxX exclusive right bound, in world pixels
	 * @return list of {@link LayeredObject}s ready for insertion
	 */
	public List<LayeredObject> createInRange(int minX, int maxX) {
		List<LayeredObject> out = new ArrayList<>();

		int startX = (minX / Block.SIZE) * Block.SIZE;
		int endX   = ((maxX + Block.SIZE - 1) / Block.SIZE) * Block.SIZE;

		for (int x = startX; x < endX; x += Block.SIZE) {

			if (rngForX(x, 0).nextDouble() >= TREE_PROBABILITY) {
				continue;
			}

			float groundY = groundHeightProvider.groundHeightAt(x);
			Random treeRng = rngForX(x, 1);

			for (GameObject part : Tree.createTree(x, groundY, treeRng)) {
				int layer = (part instanceof LayerProvider lp)
						? lp.defaultLayer()
						: Layer.STATIC_OBJECTS;
				out.add(new LayeredObject(layer, part));
			}
		}
		return out;
	}
}
