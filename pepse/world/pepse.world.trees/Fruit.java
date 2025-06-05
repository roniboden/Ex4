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

/** A collectable fruit that restores energy, then re-grows after a delay. */
public class Fruit extends GameObject implements LayerProvider {

	/* ---------------- constants ---------------- */
	public static final float ENERGY_VALUE = 10f;
	private static final float RESPAWN_TIME = 30f;        // seconds
	public static final float DIAM = Block.SIZE * 0.6f;  // 60 % of a leaf tile

	/* ---------------- instance fields ---------------- */
	private final OvalRenderable renderable;              // cached so we can restore it
	private final Random rand;                            // for new colour on respawn

	public Fruit(Vector2 topLeftCorner, Random rand) {
		super(topLeftCorner,
				new Vector2(DIAM, DIAM),
				new OvalRenderable(randomColor(rand)));
		this.renderable = (OvalRenderable) renderer().getRenderable();
		this.rand = rand;
		setTag("fruit");
	}

	/* ------------------------------------------------- */
	@Override
	public void onCollisionEnter(GameObject other,
								 danogl.collisions.Collision col) {
		super.onCollisionEnter(other, col);

		if (!"avatar".equals(other.getTag())) return;

		/* 1) recharge avatar energy */
		((Avatar) other).addEnergy(ENERGY_VALUE);

		/* 2) hide & deactivate this fruit */
		deactivate();

		/* 3) schedule reactivation (“re-grow”) */
		new ScheduledTask(
				this,                     // owner; lives for the timer duration
				RESPAWN_TIME,
				false,
				this::reactivate
		);
	}

	/* ---------------- helpers ---------------- */
	private void deactivate() {
		renderer().setRenderable(null);                   // invisible
		setDimensions(Vector2.ZERO);                      // no collisions
	}

	private void reactivate() {
		// random fresh colour to look “new”
		renderer().setRenderable(new OvalRenderable(randomColor(rand)));
		setDimensions(new Vector2(DIAM, DIAM));
	}

	private static Color randomColor(Random r) {
		Color[] palette = { new Color(220, 40, 40),
				new Color(240,180, 30),
				new Color(160, 40,160) };
		return palette[r.nextInt(palette.length)];
	}
	@Override
	public int defaultLayer() {
		return Layer.STATIC_OBJECTS;
	}
}
