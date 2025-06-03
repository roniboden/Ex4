package pepse.world.pepse.world.trees;



import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;
import java.util.Random;

import pepse.world.Block;
import pepse.world.Terrain;
import danogl.collisions.Layer;

public class Tree {
	private static final Color TRUNK_COLOR = new Color(100, 50, 20);
	private static final Color LEAF_COLOR = new Color(50, 200, 30);
    private static final float PROB = 0.15f; // Minimum X coordinate for tree planting
	/**
	 * Plants a static tree at the given x-coordinate.
	 * @param gameObjects The game object collection to add the tree to.
	 * @param x The world X where the tree trunk should start.
	 * @param terrain The terrain object to query for ground height.
	 * @param rand A random source (for height & leaf randomness).
	 */
	public static void plantTree(danogl.collisions.GameObjectCollection gameObjects, float x, Terrain terrain, Random rand) {
		// 1. Find ground Y at this x
		float groundY = terrain.groundHeightAt(x);

		// 2. Decide random trunk height (e.g., 4 to 7 blocks)
		int trunkHeight = 4 + rand.nextInt(4);

		// 3. Build the trunk (vertical stack of blocks)
		for (int i = 0; i < trunkHeight; i++) {
			Vector2 blockTopLeft = new Vector2(x, groundY - Block.SIZE * (i + 1));
			Block trunkBlock = new Block(blockTopLeft, new RectangleRenderable(TRUNK_COLOR));
			gameObjects.addGameObject(trunkBlock, Layer.STATIC_OBJECTS);
		}

		// 4. Leaves: place a 3x3 grid above the trunk (could randomize which are present)
		int leafStartY = (int) (groundY - Block.SIZE * trunkHeight);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				if (rand.nextDouble() < PROB) continue;
				Vector2 leafTopLeft = new Vector2(x + dx * Block.SIZE, leafStartY - dy * Block.SIZE);
				Block leafBlock = new Block(leafTopLeft, new RectangleRenderable(LEAF_COLOR));
				gameObjects.addGameObject(leafBlock, Layer.BACKGROUND);
			}
		}
	}
}
