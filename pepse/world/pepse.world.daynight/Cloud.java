package pepse.world.pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.components.CoordinateSpace;
import danogl.util.Vector2;
import pepse.world.Avatar;
import pepse.world.Block;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A drifting cloud that rains whenever the avatar jumps (no matter where the avatar is).
 * Each raindrop falls straight down and fades out upon hitting the bottom of the window.
 *
 * This implementation keeps the cloud’s shape rigid by:
 *  1. Creating each block as its own GameObject, storing its fixed offset from a “marker.”
 *  2. Using one Component (on the marker) to drift the marker and re‐position all blocks each frame.
 *  3. As soon as avatar.jumpJustStarted(), the cloud emits one raindrop from its center.
 *     While avatar.isInAir(), additional raindrops appear every RAIN_SPAWN_INTERVAL seconds.
 */
public class Cloud {

	/** Pixel‐art layout (5×6) for the cloud. */
	private static final List<List<Integer>> CLOUD_SHAPE = List.of(
			List.of(0, 1, 1, 0, 0, 0),
			List.of(1, 1, 1, 1, 1, 0),
			List.of(1, 1, 1, 1, 1, 1),
			List.of(0, 1, 1, 1, 1, 0),
			List.of(0, 0, 1, 1, 0, 0)
	);

	/** Color for each cloud block (pure white). */
	private static final Color CLOUD_BLOCK_COLOR = Color.WHITE;

	/** Fraction down the screen where the cloud travels (20% from top). */
	private static final float CLOUD_VERTICAL_FACTOR = 0.2f;

	/** Horizontal drift speed of the cloud (pixels/sec). */
	private static final float CLOUD_SPEED = 30f;

	/** Seconds between spawning additional raindrops while avatar remains in the air. */
	private static final float RAIN_SPAWN_INTERVAL = 0.3f;

	/** Vertical fall speed of raindrops (pixels/sec). */
	private static final float RAIN_FALL_SPEED = 400f;

	/** How long (seconds) for a raindrop to fade from alpha=1 → 0 once it hits the bottom. */
	private static final float RAIN_FADE_DURATION = 0.5f;

	/**
	 * Creates a drifting cloud (in CAMERA space) that rains whenever the avatar jumps,
	 * regardless of avatar’s horizontal position. Each raindrop falls and fades out at bottom.
	 *
	 * @param windowDimensions The (width, height) of the game window.
	 * @param gameObjects      The scene’s GameObjectCollection (for adding/removing objects).
	 * @param avatar           The Avatar instance (to check jumpJustStarted() and isInAir()).
	 * @return A “cloud marker” GameObject with no renderable, storing the cloud’s center & size.
	 */
	public static GameObject create(Vector2 windowDimensions,
									GameObjectCollection gameObjects,
									Avatar avatar) {
		// 1) Determine pixel dimensions of the cloud based on CLOUD_SHAPE (5×6)
		int rows = CLOUD_SHAPE.size();
		int cols = CLOUD_SHAPE.get(0).size();
		float cloudWidth  = cols * Block.SIZE;
		float cloudHeight = rows * Block.SIZE;

		// 2) Compute initial marker center: just off the right edge, 20% down the window
		Vector2 initialMarkerCenter = new Vector2(
				windowDimensions.x() + (cloudWidth / 2f),
				windowDimensions.y() * CLOUD_VERTICAL_FACTOR
		);

		// 3) Create the marker GameObject (no renderable—used only for position tracking)
		GameObject cloudMarker = new GameObject(
				initialMarkerCenter,
				new Vector2(cloudWidth, cloudHeight),
				/* renderable= */ null
		);
		cloudMarker.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		cloudMarker.setTag("cloudMarker");

		// 4) Add the marker to the scene on the BACKGROUND layer
		gameObjects.addGameObject(cloudMarker, danogl.collisions.Layer.BACKGROUND);

		// 5) Build each pixel‐art Block, compute its offset from marker, and add it to the scene
		Vector2 containerTopLeft = new Vector2(
				initialMarkerCenter.x() - (cloudWidth / 2f),
				initialMarkerCenter.y() - (cloudHeight / 2f)
		);

		List<GameObject> blocks = new ArrayList<>();
		List<Vector2>    offsets = new ArrayList<>();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (CLOUD_SHAPE.get(r).get(c) == 1) {
					// a) Compute this block’s top-left
					Vector2 blockTopLeft = new Vector2(
							containerTopLeft.x() + (c * Block.SIZE),
							containerTopLeft.y() + (r * Block.SIZE)
					);
					RectangleRenderable rr = new RectangleRenderable(CLOUD_BLOCK_COLOR);

					// b) Create the block and add to BACKGROUND
					GameObject block = new Block(blockTopLeft, rr);
					block.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
					block.setTag("cloudBlock");
					gameObjects.addGameObject(block, danogl.collisions.Layer.BACKGROUND);

					// c) Compute & store offset = (blockCenter − markerCenter)
					Vector2 offset = new Vector2(
							block.getCenter().x() - initialMarkerCenter.x(),
							block.getCenter().y() - initialMarkerCenter.y()
					);
					blocks.add(block);
					offsets.add(offset);
				}
			}
		}

		// 6) Attach one Component to the marker that:
		//    • Drifts the marker left & wraps around
		//    • Repositions all blocks each frame: markerCenter + storedOffset
		//    • On avatar.jumpJustStarted(): spawn immediate raindrop from cloud center
		//    • While avatar.isInAir(): spawn additional raindrops every RAIN_SPAWN_INTERVAL
		cloudMarker.addComponent(new danogl.components.Component() {
			private float timeSinceLastRain = 0f;
			private final Random random = new Random();

			@Override
			public void update(float dt) {
				// —— (a) Drift the marker left at CLOUD_SPEED; wrap if off left edge ——
				float newMarkerX = cloudMarker.getCenter().x() - (CLOUD_SPEED * dt);
				cloudMarker.transform().setCenterX(newMarkerX);
				if (newMarkerX < -(cloudWidth / 2f)) {
					cloudMarker.transform()
							.setCenterX(windowDimensions.x() + (cloudWidth / 2f));
				}

				// —— (b) Reposition every block to (markerCenter + itsOffset) ——
				Vector2 markerC = cloudMarker.getCenter();
				for (int i = 0; i < blocks.size(); i++) {
					Vector2 off = offsets.get(i);
					GameObject block = blocks.get(i);
					block.transform().setCenter(
							markerC.x() + off.x(),
							markerC.y() + off.y()
					);
				}

				// —— (c) If avatar jumped this frame, spawn one raindrop from cloud’s center ——
				if (avatar.jumpJustStarted()) {
					float cloudCenterX = markerC.x();
					spawnRaindropAtX(cloudCenterX);
					avatar.clearJumpJustStarted();
					// Reset timer so next drop is a full interval away
					timeSinceLastRain = 0f;
				}

				// —— (d) While avatar remains in the air, spawn more raindrops each interval ——
				timeSinceLastRain += dt;
				if (timeSinceLastRain >= RAIN_SPAWN_INTERVAL) {
					if (avatar.isInAir()) {
						float cloudCenterX = markerC.x();
						spawnRaindropAtX(cloudCenterX);
					}
					timeSinceLastRain = 0f;
				}
			}

			/** Spawns one raindrop at the given X, just under the cloud. */
			private void spawnRaindropAtX(float dropX) {
				final float dropSize = 10f;
				// Y = cloud bottom + half the drop’s height
				float cloudBottomY = cloudMarker.getCenter().y() + (cloudHeight / 2f);
				float dropCenterY  = cloudBottomY + (dropSize / 2f);

				Vector2 dropCenter = new Vector2(dropX, dropCenterY);
				RectangleRenderable rr = new RectangleRenderable(new Color(100, 200, 255));
				GameObject drop = new GameObject(
						dropCenter,
						new Vector2(dropSize, dropSize),
						rr
				);
				drop.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
				drop.renderer().setOpaqueness(1f);
				drop.setTag("raindrop");

				gameObjects.addGameObject(drop, danogl.collisions.Layer.FOREGROUND);

				// Attach a Component so the drop falls and fades out upon hitting the bottom
				drop.addComponent(new danogl.components.Component() {
					private boolean startedFading = false;
					private float fadeElapsed = 0f;

					@Override
					public void update(float dt) {
						if (!startedFading) {
							// (1) Fall straight down
							float newY = drop.getCenter().y() + (RAIN_FALL_SPEED * dt);
							drop.transform().setCenterY(newY);
							// (2) Once its center crosses the bottom line, begin fading
							if (newY >= windowDimensions.y() - (dropSize / 2f)) {
								startedFading = true;
								fadeElapsed = 0f;
							}
						} else {
							// Fade alpha from 1 → 0 over RAIN_FADE_DURATION
							fadeElapsed += dt;
							float alpha = 1f - (fadeElapsed / RAIN_FADE_DURATION);
							drop.renderer().setOpaqueness(Math.max(0f, alpha));
							// Remove when fade completes
							if (fadeElapsed >= RAIN_FADE_DURATION) {
								gameObjects.removeGameObject(drop);
							}
						}
					}
				});
			}
		});

		return cloudMarker;
	}
}
