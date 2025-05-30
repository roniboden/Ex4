package pepse.world;


import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a basic block in the game world.
 * Each block has fixed size, does not move, and prevents objects from intersecting it.
 */
public class Block extends GameObject {
	public static final int SIZE = 30;

	/**
	 * Constructs a new Block at the given top-left corner, using the given renderable.
	 *
	 * @param topLeftCorner The position of the top-left corner of the block.
	 * @param renderable    The renderable to render the block.
	 */
	public Block(Vector2 topLeftCorner, Renderable renderable) {
		super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
		physics().preventIntersectionsFromDirection(Vector2.ZERO);
		physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
	}
}
