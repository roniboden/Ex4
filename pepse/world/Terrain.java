package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.pepse.util.ColorSupplier;
import pepse.util.NoiseGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates and manages the terrain (ground) for the Pepse world.
 *
 * <p>The surface height at each {@code x}-coordinate is computed by adding
 * smooth-noise (OpenSimplex/Perlin) to a baseline that sits two-thirds of the
 * window height from the top.
 * For every surface column we add ground blocks
 * downward until we exceed one full window-height, ensuring the player can
 * never see “under” the terrain even when the camera follows the avatar.</p>
 * @author Noa
 */
public class Terrain implements GroundHeightProvider {

	/* ─── tuning constants ─────────────────────────────────────────────── */

	/** Vertical amplitude of the noise-function in pixels. */
	private static final float HEIGHT_FACTOR    = Block.SIZE * 20f;

	/** Horizontal frequency scaler (lower = smoother terrain). */
	private static final float HORIZONTAL_SCALE = 0.08f;

	/** X-offset fed into the noise generator so x=0 is not a flat band. */
	private static final int   STARTING_POINT   = 100;

	/** Baseline = two-thirds of the window height. */
	private static final float PART_WINDOW      = 2f / 3f;

	/** Base ground colour before subtle random variation. */
	private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
	private static String GROUND_TAG = "ground";

	/* ─── instance fields ──────────────────────────────────────────────── */

	private final Vector2        windowDimensions;
	private final NoiseGenerator noiseGenerator;
	private final int            groundHeightAtX0;

	/**
	 * Constructs a {@code Terrain} object that generates deterministic ground
	 * for a given window size and world seed.
	 *
	 * @param windowDimensions dimensions of the game window (pixels)
	 * @param seed             world seed; the same seed produces identical
	 *                         terrain across runs
	 */
	public Terrain(Vector2 windowDimensions, int seed) {
		this.windowDimensions = windowDimensions;
		this.groundHeightAtX0 = (int) (windowDimensions.y() * PART_WINDOW);
		this.noiseGenerator   = new NoiseGenerator(seed, STARTING_POINT);
	}

	/**
	 * Returns the y-coordinate of the terrain surface at a given x.
	 *
	 * @param x world-space x-coordinate
	 * @return  y-coordinate of the topmost ground block at {@code x}
	 */
	@Override
	public float groundHeightAt(float x) {
		double noise = noiseGenerator.noise(x * HORIZONTAL_SCALE, HEIGHT_FACTOR);
		return groundHeightAtX0 + (float) noise;
	}

	/**
	 * Generates all {@link Block}s that make up the terrain in the horizontal
	 * interval {@code [minX, maxX)}.
	 *
	 * <p>The range is first aligned to the 32-pixel block grid so that calling
	 * this method in different orders yields exactly the same set of blocks.
	 * Each column is then filled from the computed surface downward until the
	 * depth exceeds one full window height, guaranteeing no gaps are visible
	 * when the camera moves.</p>
	 *
	 * @param minX left bound (inclusive) in world pixels
	 * @param maxX right bound (exclusive) in world pixels
	 * @return     list of newly created ground blocks for this interval
	 */
	public List<Block> createInRange(int minX, int maxX) {
		List<Block> blocks = new ArrayList<>();
		Renderable  rend   = new RectangleRenderable(
				ColorSupplier.approximateColor(BASE_GROUND_COLOR));

		/* align to 32-px grid */
		int startX = (minX / Block.SIZE) * Block.SIZE;
		int endX   = ((maxX + Block.SIZE - 1) / Block.SIZE) * Block.SIZE;

		for (int x = startX; x < endX; x += Block.SIZE) {

			/* top block of this column */
			float rawSurfaceY = groundHeightAt(x);
			int   topBlockY   = (int) (Math.floor(rawSurfaceY / Block.SIZE) * Block.SIZE);

			/* ensure depth ≥ window height (+ one extra row) */
			float requiredDepth = windowDimensions.y() + Block.SIZE;
			for (float y = topBlockY; y < topBlockY + requiredDepth; y += Block.SIZE) {
				Block b = new Block(new Vector2(x, y), rend);
				b.setTag(GROUND_TAG);
				blocks.add(b);
			}
		}
		return blocks;
	}
}
