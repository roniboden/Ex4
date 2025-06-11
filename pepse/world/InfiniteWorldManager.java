//// File: Ex4/pepse/world/InfiniteWorldManager.java


package pepse.world;

import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.util.Vector2;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Keeps expanding the world: every update it checks the camera window,
 * and if it discovers that the user is approaching an un-generated area,
 * it calls {@code onRangeNeeded.accept(minX,maxX)} to let Terrain/Flora
 * build that strip and insert its objects.
 * @author Roni
 */
public class InfiniteWorldManager extends GameObject {

	/** How far beyond the camera edge we keep the world filled (pixels). */
	private static final int BUFFER = Block.SIZE * 2;

	/** --- callbacks provided by GameManager --- */
	private final Supplier<Float> leftEdgeSupplier;
	private final Supplier<Float> rightEdgeSupplier;
	private final BiConsumer<Integer,Integer> onRangeNeeded;

	/** --- current generated bounds --- */
	private int minGeneratedX;
	private int maxGeneratedX;
	private static final int BLOCK_BUFFER = 20;

	/**
	 * Creates an InfiniteWorldManager that will
	 * @param leftEdgeSupplier supplies the left edge of the camera view
	 * @param rightEdgeSupplier supplies the right edge of the camera view
	 * @param onRangeNeeded
	 * @param initialMinX
	 * @param initialMaxX
	 */
	public InfiniteWorldManager(
			Supplier<Float> leftEdgeSupplier,
			Supplier<Float> rightEdgeSupplier,
			BiConsumer<Integer,Integer> onRangeNeeded,
			int initialMinX,
			int initialMaxX) {
		super(Vector2.ZERO, Vector2.ZERO, null);   // invisible object
		this.leftEdgeSupplier  = leftEdgeSupplier;
		this.rightEdgeSupplier = rightEdgeSupplier;
		this.onRangeNeeded     = onRangeNeeded;
		this.minGeneratedX     = initialMinX;
		this.maxGeneratedX     = initialMaxX;
	}
/**
	 *
     * @return the default layer for this object, which is {@link Layer#BACKGROUND}.
	 */
	@Override
	public void update(float dt) {
		/* 1. look where the camera is right now */
		float camLeft  = leftEdgeSupplier.get();
		float camRight = rightEdgeSupplier.get();

		/* 2. if we are close to the left edge – generate more leftward */
		if (camLeft - BUFFER < minGeneratedX) {
			int newMin = minGeneratedX - Block.SIZE * BLOCK_BUFFER;           // grow by 20 blocks
			onRangeNeeded.accept(newMin, minGeneratedX);
			minGeneratedX = newMin;
		}

		/* 3. if close to right edge – generate more rightward */
		if (camRight + BUFFER > maxGeneratedX) {
			int newMax = maxGeneratedX + Block.SIZE * BLOCK_BUFFER;
			onRangeNeeded.accept(maxGeneratedX, newMax);
			maxGeneratedX = newMax;
		}
	}
}
