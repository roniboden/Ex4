package pepse.world;


import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import java.awt.*;
import danogl.util.Vector2;
import pepse.pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Terrain is responsible for generating the ground blocks in the game world.
 * It uses a noise function to determine terrain height at any x-coordinate,
 * and allows generation of a list of blocks over a continuous horizontal range.
 */
public class Terrain {
	private static final float AMPLITUDE = 80f;
	private static final float FREQUENCY = 0.03f;
	/** The base color used for ground blocks before approximation. */
	private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);

	/** Number of vertical blocks stacked at each terrain column. */
	private static final int TERRAIN_DEPTH = 20;

	/** Dimensions of the game window. Used to define ground baseline. */
	private final Vector2 windowDimensions;

	/** Noise generator to create pseudo-random terrain height. */
	private final NoiseGenerator noiseGenerator;

	/** The base height at x=0 for terrain, used as a reference point. */
	private final float groundHeightAtX0;

	/**
	 * Constructs a Terrain object.
	 *
	 * @param windowDimensions The dimensions of the game window.
	 * @param seed             A seed value for generating deterministic noise-based terrain.
	 */
	public Terrain(Vector2 windowDimensions, int seed) {
		this.windowDimensions = windowDimensions;
		this.noiseGenerator = new NoiseGenerator(seed, 100);
		this.groundHeightAtX0 = windowDimensions.y() * 2 / 3f;
	}

	/**
	 * Calculates the height (y-coordinate) of the terrain at a given x-coordinate.
	 * The height is based on a smooth noise function added to a base ground level.
	 *
	 * @param x The x-coordinate in the world for which to calculate terrain height.
	 * @return The y-coordinate of the top of the ground at this x-coordinate.
	 */
	public double groundHeightAt(float x) {
		double noise = noiseGenerator.noise(x, FREQUENCY); 
		return groundHeightAtX0 + (float)(noise * AMPLITUDE);
	}

	/**
	 * Creates a list of ground blocks spanning a given horizontal range.
	 * For each column, blocks are stacked vertically from the terrain surface downward.
	 *
	 * @param minX The minimum x-coordinate (inclusive) for which to create terrain blocks.
	 * @param maxX The maximum x-coordinate (exclusive) for which to create terrain blocks.
	 * @return A list of Block objects representing terrain within the specified range.
	 */
//	public List<Block> createInRange(int minX, int maxX) {
//		List<Block> blocks = new ArrayList<>();
//		Renderable blockRenderable = new RectangleRenderable(
//				ColorSupplier.approximateColor(BASE_GROUND_COLOR)
//		);
//
//		// Align range to block size
//		int startX = (minX / Block.SIZE) * Block.SIZE;
//		int endX = ((maxX + Block.SIZE - 1) / Block.SIZE) * Block.SIZE;
//
//		for (int x = startX; x < endX; x += Block.SIZE) {
//			float groundTopY = (float) (Math.floor(groundHeightAt(x) / Block.SIZE) * Block.SIZE);
//
//			for (int i = 0; i < TERRAIN_DEPTH; i++) {
//				Vector2 blockTopLeft = new Vector2(x, groundTopY + i * Block.SIZE);
//				Block block = new Block(blockTopLeft, blockRenderable);
//				//need to be removed
//				block.setTag("ground");
//				blocks.add(block);
//			}
//		}
//
//		return blocks;
//	}
	public List<Block> createInRange(int minX, int maxX) {
		List<Block> blocks = new ArrayList<>();
		Renderable blockRenderable = new RectangleRenderable(
				ColorSupplier.approximateColor(BASE_GROUND_COLOR)
		);

		int startX = (minX / Block.SIZE) * Block.SIZE;
		int endX = ((maxX + Block.SIZE - 1) / Block.SIZE) * Block.SIZE;

		for (int x = startX; x < endX; x += Block.SIZE) {
			float rawHeight = (float) groundHeightAt(x);
			System.out.println("rawHeight: " + rawHeight);
			int topBlockY = ((int)(rawHeight / Block.SIZE)) * Block.SIZE;

			for (int i = 0; i < TERRAIN_DEPTH; i++) {
				int blockY = topBlockY + i * Block.SIZE;
				Vector2 topLeft = new Vector2(x, blockY);
				Block block = new Block(topLeft, blockRenderable);
				block.setTag("ground");
				blocks.add(block);
			}
		}

		return blocks;
	}
}
