package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>A utility class that builds a single <em>tree</em> instance for the Pepse world.</p>
 *
 * <p>The generated tree is composed of two kinds of parts:</p>
 * <ul>
 *   <li><strong>Trunk segments</strong> – narrow {@link GameObject}s, one per 30 px “tile”.
 *       Each segment is given {@linkplain GameObjectPhysics#IMMOVABLE_MASS infinite mass}
 *       plus a call to {@code physics().preventIntersectionsFromDirection(Vector2.ZERO)},
 *       so the avatar collides with the trunk but the trunk never moves.</li>
 *   <li><strong>Leaves</strong> – decorative square {@link GameObject}s that have
 *       <em>no</em> physics component, so the avatar can pass through them freely.</li>
 * </ul>
 *
 * <p>The class is <em>stateless</em> (all methods are {@code static}); therefore it is
 * consumed by {@code Flora} which decides where and when to create trees.</p>
 *
 * @author  Noa
 *
 */
public final class Tree {

	/* === Constants that control appearance ==================================================== */

	/** Color of trunk segments. */
	private static final Color TRUNK_COLOR = new Color(100, 50, 20);

	/** Color of leaf blocks.   */
	private static final Color LEAF_COLOR  = new Color( 50,200, 30);

	/** Probability (0–1) to skip an individual leaf block when generating foliage. */
	private static final float LEAF_MISSING_PROB = 0.15f;

	/** Width of a trunk segment, in world pixels.            */
	private static final float TRUNK_WIDTH        = 25f;

	/** Height of a single trunk “tile”, in world pixels.     */
	private static final float TRUNK_HEIGHT_UNIT  = 30f;

	/** Width & height of a single leaf block, in world pixels. */
	private static final float LEAF_SIZE          = 30f;
	private static final int TRUNK_SIZE          = 4;
	// Rotation Transition Constants
	private static final float
			MIN_LEAF_ROTATION_ANGLE_DEGREES = 5f;
	private static final float
			LEAF_ROTATION_ANGLE_RANGE_DEGREES = 7f;
	private static final float
			MIN_LEAF_ROTATION_TRANSITION_DURATION_SECONDS = 0.8f;
	private static final float
			LEAF_ROTATION_TRANSITION_DURATION_RANGE_SECONDS = 0.8f; // To achieve 0.8-1.6 range

	// Scale Transition Constants
	private static final float
			MIN_LEAF_SCALE_FACTOR = 0.9f;   // Represents 90% of LEAF_SIZE
	private static final float
			MAX_LEAF_SCALE_FACTOR = 1.1f;   // Represents 110% of LEAF_SIZE
	private static final float
			MIN_LEAF_SCALE_TRANSITION_DURATION_SECONDS = 1.2f;
	private static final float
			LEAF_SCALE_TRANSITION_DURATION_RANGE_SECONDS = 1.2f; // To achieve 1.2-2.4 range

	/* ================================================================================================= */

	/**
	 * Creates an entire tree at the requested <em>x</em>-coordinate.
	 *
	 * @param x         world-space <em>x</em> of the trunk centre.
	 * @param groundY   <em>y</em> coordinate of the terrain surface beneath the trunk.
	 * @param rand      random source (used for trunk height and missing leaves).
	 *
	 * @return an immutable {@link List} containing every {@link GameObject} that
	 *         makes up the tree (trunk + leaves).
	 *         The caller is responsible for adding them to the game-object collection
	 *         and choosing appropriate layers.
	 */
	public static List<GameObject> createTree(float x, float groundY,
											  Random rand) {
		List<GameObject> objects = new ArrayList<>();

		/* ----- 1. Generate trunk ---------------------------------------------------------------- */
		int trunkHeight = TRUNK_SIZE + rand.nextInt(TRUNK_SIZE);  // 4–7 segments

		for (int i = 0; i < trunkHeight; i++) {
			// pass the true column left-edge (float) and the **base** groundY
			GameObject trunkSegment =
					createTrunk(rand, x, groundY, i);
			objects.add(trunkSegment);
		}
		/* ---------- leaves OR fruit (never both)  ----------------------------- */
		int leafStartY = Math.round(groundY - TRUNK_HEIGHT_UNIT * trunkHeight);

		/* probability to grow a fruit on a given leaf tile */
		final float FRUIT_PROB = 0.25f;            // 25 %  – tweak as you like

		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {

				/* skip whole tile if “missing leaf” */
				if (rand.nextFloat() < LEAF_MISSING_PROB) continue;

				Vector2 tileTopLeft = new Vector2(
						x + dx * LEAF_SIZE,
						leafStartY - dy * LEAF_SIZE
				);

				/* 1️⃣ decide what grows on this tile */
				boolean growFruit = rand.nextFloat() < FRUIT_PROB;

				if (growFruit) {
					/* ---- fruit only ------------------------------------------------ */
					Vector2 fruitPos = new Vector2(
							tileTopLeft.x() + (LEAF_SIZE - Fruit.DIAM) / 2f,
							tileTopLeft.y() + (LEAF_SIZE - Fruit.DIAM) / 2f
					);
					Fruit fruit = new Fruit(fruitPos, rand);      // LayerProvider → STATIC
					objects.add(fruit);

				} else {
					/* ---- decorative leaf only ------------------------------------- */
					Leaf leaf = new Leaf(tileTopLeft, LEAF_SIZE, LEAF_COLOR); // LayerProvider → BACKGROUND
					startLeafSwayTransitions(leaf, rand);
					objects.add(leaf);
				}
			}
		}

		return objects;
	}
	/**
	 * Creates a single trunk segment at the specified <em>x</em>-coordinate.
	 *
	 * @param rand         random source (not used, but required for consistency).
	 * @param trunkX      world-space <em>x</em> of the trunk centre.
	 * @param baseGroundY <em>y</em> coordinate of the terrain surface beneath the trunk.
	 * @param index       zero-based index of this segment, counting from the ground up.
	 *
	 * @return a {@link GameObject} representing a single trunk segment.
	 */
	private static GameObject createTrunk(Random rand,
										  float trunkX,   // keep as float
										  float baseGroundY,
										  int index) {

		Vector2 segmentTopLeft = new Vector2(
				trunkX + (LEAF_SIZE - TRUNK_WIDTH) / 2f,
				baseGroundY - TRUNK_HEIGHT_UNIT * (index + 1)   // single offset
		);

		GameObject segment = new GameObject(
				segmentTopLeft,
				new Vector2(TRUNK_WIDTH, TRUNK_HEIGHT_UNIT),
				new RectangleRenderable(TRUNK_COLOR));

		segment.physics().preventIntersectionsFromDirection(Vector2.ZERO);
		segment.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);

		return segment;
	}
	/** Starts two BACK_AND_FORTH transitions that repeat forever. */
	private static void startLeafSwayTransitions(GameObject leaf, Random rand) {

		/* 1. Gentle rotation ±(5-12°) */
		float maxAngle = MIN_LEAF_ROTATION_ANGLE_DEGREES +
				rand.nextFloat() * LEAF_ROTATION_ANGLE_RANGE_DEGREES;

		new Transition<>(
				leaf,                                           // gameObjectToUpdateThrough
				leaf.renderer()::setRenderableAngle,            // setter
				-maxAngle,                                      // initial value
				+maxAngle,                                      // final value
				Transition.CUBIC_INTERPOLATOR_FLOAT,            // interpolator
				MIN_LEAF_ROTATION_TRANSITION_DURATION_SECONDS
						+ rand.nextFloat() *
						LEAF_ROTATION_TRANSITION_DURATION_RANGE_SECONDS, // 0.8-1.6 s per half-cycle
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null                                            // no on-finish callback
		);

		/* 2. Subtle scale change (90 % ↔ 110 %) */
		Vector2 fromDim = new Vector2(LEAF_SIZE *
				MIN_LEAF_SCALE_FACTOR, LEAF_SIZE * MIN_LEAF_SCALE_FACTOR);
		Vector2 toDim   = new Vector2(LEAF_SIZE *
				MAX_LEAF_SCALE_FACTOR, LEAF_SIZE * MAX_LEAF_SCALE_FACTOR);

		new Transition<>(
				leaf,
				leaf::setDimensions,
				fromDim,
				toDim,
				Transition.CUBIC_INTERPOLATOR_VECTOR,
				MIN_LEAF_SCALE_TRANSITION_DURATION_SECONDS +
						rand.nextFloat() * LEAF_SCALE_TRANSITION_DURATION_RANGE_SECONDS, // 1.2-2.4 s per half-cycle
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null
		);
	}

	/* Prevent instantiation */
	private Tree() { }
}
