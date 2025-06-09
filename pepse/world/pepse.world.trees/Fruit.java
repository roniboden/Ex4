package pepse.world.pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.LayerProvider;

import java.awt.Color;
import java.util.Random;

/**
 * A collectable fruit that restores the avatar’s energy and then “re-grows”
 * after a cooldown.
 *
 * <p>When the avatar collides with the fruit it:</p>
 * <ol>
 *   <li>adds {@link #ENERGY_VALUE} units of energy to the avatar;</li>
 *   <li>becomes invisible and non-collidable;</li>
 *   <li>schedules a {@link ScheduledTask} that reactivates the fruit after
 *       {@link #RESPAWN_TIME} seconds, giving it a fresh random colour so the
 *       player can tell it has respawned.</li>
 * </ol>
 */
public class Fruit extends GameObject implements LayerProvider {

	/** Energy units restored when picked up. */
	public static final float ENERGY_VALUE = 10f;

	/** Seconds until the fruit reappears after being collected. */
	private static final float RESPAWN_TIME = 30f;

	/** Diameter of the fruit (60 % of a leaf tile). */
	public static final float DIAM = Block.SIZE * 0.6f;

	private final OvalRenderable renderable;
	private final Random rand;

	/**
	 * Creates a new fruit game object.
	 *
	 * @param topLeftCorner world-space position of the fruit’s top-left corner
	 * @param rand          deterministic random source used for respawn colour
	 */
	public Fruit(Vector2 topLeftCorner, Random rand) {
		super(topLeftCorner,
				new Vector2(DIAM, DIAM),
				new OvalRenderable(randomColor(rand)));

		this.renderable = (OvalRenderable) renderer().getRenderable();
		this.rand       = rand;
		setTag("fruit");
	}

	/** Handles collision with the avatar: recharge, hide, and schedule respawn. */
	@Override
	public void onCollisionEnter(GameObject other,
								 danogl.collisions.Collision col) {
		super.onCollisionEnter(other, col);

		if (!"avatar".equals(other.getTag())) {
			return;
		}

		((Avatar) other).addEnergy(ENERGY_VALUE);
		deactivate();

		new ScheduledTask(
				this,              // the task lives as long as this object
				RESPAWN_TIME,
				false,
				this::reactivate
		);
	}

	/** Makes the fruit invisible and non-collidable immediately after pickup. */
	private void deactivate() {
		renderer().setRenderable(null);
		setDimensions(Vector2.ZERO);
	}

	/** Restores the fruit’s renderable, size, and gives it a fresh random colour. */
	private void reactivate() {
		renderer().setRenderable(new OvalRenderable(randomColor(rand)));
		setDimensions(new Vector2(DIAM, DIAM));
	}

	/**
	 * Picks a random colour from a small palette so each respawn looks “fresh”.
	 *
	 * @param r random source (deterministic for this fruit)
	 * @return  a colour chosen from the predefined palette
	 */
	private static Color randomColor(Random r) {
		Color[] palette = {
				new Color(220, 40, 40),   // red
				new Color(240,180, 30),   // orange
				new Color(160, 40,160)    // purple
		};
		return palette[r.nextInt(palette.length)];
	}

	/** Fruits belong on the static layer so they collide with the avatar. */
	@Override
	public int defaultLayer() {
		return Layer.STATIC_OBJECTS;
	}
}
