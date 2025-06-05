package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Layer;
import pepse.world.Block;
import pepse.world.GroundHeightProvider;
import pepse.world.LayerProvider;          // <<-- the tiny interface each object may implement

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Generates trees (trunk + leaves + fruit) in a horizontal range. */
public class Flora {

	/** A simple pair: which layer the object belongs to, and the object itself. */
	public record LayeredObject(int layer, GameObject obj) {}

	private final GroundHeightProvider groundHeightProvider;
	private final Random rand;

	public Flora(GroundHeightProvider groundHeightProvider, int seed) {
		this.groundHeightProvider = groundHeightProvider;
		this.rand                 = new Random(seed);
	}

	/**
	 * Create all tree parts whose trunk X lies in [minX, maxX) and
	 * return them together with their desired layer.
	 */
	public List<LayeredObject> createInRange(int minX, int maxX) {
		List<LayeredObject> out = new ArrayList<>();

		for (int x = minX; x < maxX; x += Block.SIZE) {
			if (rand.nextDouble() < 0.10) {                       // 10 % chance for a tree
				float groundY = groundHeightProvider.groundHeightAt(x);

				/* every part of the tree chooses its own default layer */
				for (GameObject part : Tree.createTree(x, groundY, rand)) {
					int layer = (part instanceof LayerProvider lp)
							? lp.defaultLayer()            // e.g. fruit → STATIC, leaf → BACKGROUND
							: Layer.STATIC_OBJECTS;        // fallback for trunk segments
					out.add(new LayeredObject(layer, part));
				}
			}
		}
		return out;
	}
}
