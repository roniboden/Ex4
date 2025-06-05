//// File: Ex4/pepse/world/InfiniteWorldManager.java
//package pepse.world;
//
//import danogl.GameObject;
//import danogl.collisions.Layer;
//import danogl.util.Vector2;
//
//import java.util.function.BiConsumer;
//import java.util.function.Supplier;
//
///**
// * InfiniteWorldManager watches:
// *   • cameraLeftXSupplier.get()   → current camera left‐edge X
// *   • cameraRightXSupplier.get()  → current camera right‐edge X
// *
// * Whenever the camera goes outside the “already generated” X‐range
// * [generatedMinX .. generatedMaxX], it calls onRangeNeeded.accept(minX, maxX)
// * to fill that exact interval.  It never draws anything itself (zero‐size, null renderable).
// */
//public class InfiniteWorldManager extends GameObject {
//	private final Supplier<Float> cameraLeftXSupplier;
//	private final Supplier<Float> cameraRightXSupplier;
//	private final BiConsumer<Integer, Integer> onRangeNeeded;
//
//	// The X‐interval [generatedMinX .. generatedMaxX) that is already built:
//	private int generatedMinX;
//	private int generatedMaxX;
//
//	/**
//	 * @param cameraLeftXSupplier   Returns camera’s left‐edge X (world‐space).
//	 * @param cameraRightXSupplier  Returns camera’s right‐edge X (world‐space).
//	 * @param onRangeNeeded         A BiConsumer(minX, maxX) that must generate and add
//	 *                              ground+trees in exactly [minX .. maxX).
//	 * @param initialMinX           The left bound (inclusive) already generated.
//	 * @param initialMaxX           The right bound (exclusive) already generated.
//	 */
//	public InfiniteWorldManager(Supplier<Float> cameraLeftXSupplier,
//								Supplier<Float> cameraRightXSupplier,
//								BiConsumer<Integer, Integer> onRangeNeeded,
//								int initialMinX,
//								int initialMaxX) {
//		// Invisible zero‐size GameObject (no collisions, no drawing)
//		super(
//				new Vector2(0f, 0f),
//				new Vector2(0f, 0f),
//				null
//		);
//
//		this.cameraLeftXSupplier  = cameraLeftXSupplier;
//		this.cameraRightXSupplier = cameraRightXSupplier;
//		this.onRangeNeeded        = onRangeNeeded;
//
//		this.generatedMinX = initialMinX;
//		this.generatedMaxX = initialMaxX;
//	}
//
//	@Override
//	public void update(float deltaTime) {
//		super.update(deltaTime);
//
//		float cameraLeftX  = cameraLeftXSupplier.get();
//		float cameraRightX = cameraRightXSupplier.get();
//
//		// If camera scrolled left of generatedMinX, create [newMinX..oldMinX)
//		if (cameraLeftX < generatedMinX) {
//			int newMinX = (int) Math.floor(cameraLeftX);
//			int oldMinX = generatedMinX;
//
//			onRangeNeeded.accept(newMinX, oldMinX);
//			generatedMinX = newMinX;
//		}
//
//		// If camera scrolled right of generatedMaxX, create [oldMaxX..newMaxX)
//		if (cameraRightX > generatedMaxX) {
//			int oldMaxX = generatedMaxX;
//			int newMaxX = (int) Math.ceil(cameraRightX);
//
//			onRangeNeeded.accept(oldMaxX, newMaxX);
//			generatedMaxX = newMaxX;
//		}
//	}
//}

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
 */
public class InfiniteWorldManager extends GameObject {

	/** How far beyond the camera edge we keep the world filled (pixels). */
	private static final int BUFFER = Block.SIZE * 2;

	/* --- callbacks provided by GameManager --- */
	private final Supplier<Float> leftEdgeSupplier;
	private final Supplier<Float> rightEdgeSupplier;
	private final BiConsumer<Integer,Integer> onRangeNeeded;

	/* --- current generated bounds --- */
	private int minGeneratedX;
	private int maxGeneratedX;

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

	@Override
	public void update(float dt) {
		/* 1. look where the camera is right now */
		float camLeft  = leftEdgeSupplier.get();
		float camRight = rightEdgeSupplier.get();

		/* 2. if we are close to the left edge – generate more leftward */
		if (camLeft - BUFFER < minGeneratedX) {
			int newMin = minGeneratedX - Block.SIZE * 20;           // grow by 20 blocks
			onRangeNeeded.accept(newMin, minGeneratedX);
			minGeneratedX = newMin;
		}

		/* 3. if close to right edge – generate more rightward */
		if (camRight + BUFFER > maxGeneratedX) {
			int newMax = maxGeneratedX + Block.SIZE * 20;
			onRangeNeeded.accept(maxGeneratedX, newMax);
			maxGeneratedX = newMax;
		}
	}
}
