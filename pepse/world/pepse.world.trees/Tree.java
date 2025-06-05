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
 * @author  Your Name
 * @since   1.0
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
	public static List<GameObject> createTree(float x, float groundY, Random rand) {
		List<GameObject> objects = new ArrayList<>();

		/* ----- 1. Generate trunk ---------------------------------------------------------------- */
		int trunkHeight = 4 + rand.nextInt(4);  // 4–7 segments

		for (int i = 0; i < trunkHeight; i++) {
			Vector2 segmentTopLeft = new Vector2(
					x + (LEAF_SIZE - TRUNK_WIDTH) / 2f,
					groundY - TRUNK_HEIGHT_UNIT * (i + 1)
			);

			GameObject trunkSegment = new GameObject(
					segmentTopLeft,
					new Vector2(TRUNK_WIDTH, TRUNK_HEIGHT_UNIT),
					new RectangleRenderable(TRUNK_COLOR)
			);
			// Make segment collide but never move:
			trunkSegment.physics().preventIntersectionsFromDirection(Vector2.ZERO);
			trunkSegment.physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
			trunkSegment.setTag("tree-trunk");

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
	/** Starts two BACK_AND_FORTH transitions that repeat forever. */
	private static void startLeafSwayTransitions(GameObject leaf, Random rand) {

		/* 1. Gentle rotation ±(5-12°) */
		float maxAngle = 5f + rand.nextFloat() * 7f;

		new Transition<>(
				leaf,                                           // gameObjectToUpdateThrough
				leaf.renderer()::setRenderableAngle,            // setter
				-maxAngle,                                      // initial value
				+maxAngle,                                      // final value
				Transition.CUBIC_INTERPOLATOR_FLOAT,            // interpolator
				0.8f + rand.nextFloat() * 0.8f,                 // 0.8-1.6 s per half-cycle
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null                                            // no on-finish callback
		);

		/* 2. Subtle scale change (90 % ↔ 110 %) */
		Vector2 fromDim = new Vector2(LEAF_SIZE * 0.9f, LEAF_SIZE * 0.9f);
		Vector2 toDim   = new Vector2(LEAF_SIZE * 1.1f, LEAF_SIZE * 1.1f);

		new Transition<>(
				leaf,
				leaf::setDimensions,
				fromDim,
				toDim,
				Transition.CUBIC_INTERPOLATOR_VECTOR,
				1.2f + rand.nextFloat() * 1.2f,                 // 1.2-2.4 s per half-cycle
				Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
				null
		);
	}
	/* Prevent instantiation */
	private Tree() { }
}
