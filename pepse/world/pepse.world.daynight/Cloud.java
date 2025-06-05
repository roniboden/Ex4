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
 * A drifting cloud that moves horizontally across the screen and generates raindrops
 * whenever the avatar jumps or remains in the air. The cloud’s pixel-art shape is defined
 * by a 5×6 grid (CLOUD_SHAPE), and each block of the cloud is represented as an independent
 * GameObject in CAMERA space. A “marker” GameObject holds the cloud’s center position, and
 * a single Component on that marker:
 * <ul>
 *   <li>Drifts the marker leftward at a constant speed (CLOUD_SPEED), wrapping around when off-screen.</li>
 *   <li>Repositions each cloud block each frame based on its stored offset from the marker.</li>
 *   <li>Listens for {@code avatar.jumpJustStarted()} to spawn exactly one raindrop at a random X under the cloud.</li>
 *   <li>While {@code avatar.isInAir()}, spawns additional raindrops every RAIN_SPAWN_INTERVAL seconds, each at a random X under the cloud.</li>
 * </ul>
 * Each raindrop falls at a fixed speed (RAIN_FALL_SPEED) and fades out over RAIN_FADE_DURATION seconds once it reaches the bottom of the window.
 */
public class Cloud {

	/** Pixel‐art layout (5×6) for the cloud shape. 1 = block present, 0 = transparent. */
	private static final List<List<Integer>> CLOUD_SHAPE = List.of(
			List.of(0, 1, 1, 0, 0, 0),
			List.of(1, 1, 1, 1, 1, 0),
			List.of(1, 1, 1, 1, 1, 1),
			List.of(0, 1, 1, 1, 1, 0),
			List.of(0, 0, 1, 1, 0, 0)
	);

	/** Color for each cloud block (pure white). */
	private static final Color CLOUD_BLOCK_COLOR = Color.WHITE;

	/** Fraction down the screen where the cloud travels (20% from the top). */
	private static final float CLOUD_VERTICAL_FACTOR = 0.2f;

	/** Horizontal drift speed of the cloud (pixels/sec). */
	private static final float CLOUD_SPEED = 30f;

	/** Seconds between spawning additional raindrops while the avatar remains in the air. */
	private static final float RAIN_SPAWN_INTERVAL = 0.3f;

	/** Vertical fall speed of raindrops (pixels/sec). */
	private static final float RAIN_FALL_SPEED = 400f;

	/** How long (seconds) for a raindrop to fade from alpha=1 → 0 once it hits the bottom. */
	private static final float RAIN_FADE_DURATION = 0.5f;

	/**
	 * Creates a drifting cloud in CAMERA space that rains whenever the avatar jumps
	 * or stays in the air. Each raindrop originates from a random X under the cloud’s width.
	 *
	 * <p>The cloud is built as follows:
	 * <ol>
	 *   <li>Calculate the cloud’s pixel dimensions (cloudWidth × cloudHeight) from CLOUD_SHAPE.</li>
	 *   <li>Instantiate a “marker” GameObject (no visible renderable) at the right edge of the screen,
	 *       20% down from the top. This marker’s center defines the cloud’s current position.</li>
	 *   <li>Add each block of the pixel-art cloud as a separate GameObject (tagged “cloudBlock”)
	 *       in CAMERA space, computing and storing its offset from the marker.</li>
	 *   <li>Attach a Component to the marker that:</li>
	 *     <ul>
	 *       <li>Drifts the marker leftward at CLOUD_SPEED, wrapping back to the right edge when off-screen.</li>
	 *       <li>Repositions every block each frame so that blockCenter = markerCenter + storedOffset.</li>
	 *       <li>When {@code avatar.jumpJustStarted()} is true, spawns a single raindrop at a random X under the cloud,
	 *           then resets the jump flag and rain-timer.</li>
	 *       <li>While {@code avatar.isInAir()}, once RAIN_SPAWN_INTERVAL seconds have elapsed, spawns another raindrop
	 *           at a random X under the cloud, then resets the rain-timer.</li>
	 *     </ul>
	 *   <li>Each raindrop is an independent GameObject in CAMERA space that:
	 *     <ul>
	 *       <li>Falls downward at RAIN_FALL_SPEED.</li>
	 *       <li>Once it reaches the bottom of the window, fades its alpha from 1 to 0 over RAIN_FADE_DURATION.</li>
	 *       <li>Is removed from the scene once its fade-out completes.</li>
	 *     </ul>
	 *   </li>
	 * </ol>
	 *
	 * @param windowDimensions The (width, height) of the game window.
	 * @param gameObjects      The scene’s GameObjectCollection, used to add and remove GameObjects.
	 * @param avatar           The Avatar instance, used to check {@code jumpJustStarted()} and {@code isInAir()}.
	 * @return The “cloud marker” GameObject (with no renderable) whose center and size define the cloud.
	 */
	public static GameObject create(Vector2 windowDimensions,
									GameObjectCollection gameObjects,
									Avatar avatar) {
		// 1) Determine the cloud’s total pixel size from CLOUD_SHAPE (5 rows × 6 cols)
		int rows = CLOUD_SHAPE.size();
		int cols = CLOUD_SHAPE.get(0).size();
		float cloudWidth  = cols * Block.SIZE;
		float cloudHeight = rows * Block.SIZE;

		// 2) Compute the marker’s initial center: just off the right edge, 20% down the screen
		Vector2 initialMarkerCenter = new Vector2(
				windowDimensions.x() + (cloudWidth / 2f),
				windowDimensions.y() * CLOUD_VERTICAL_FACTOR
		);

		// 3) Create the “marker” GameObject (no renderable; used only for position tracking)
		GameObject cloudMarker = new GameObject(
				initialMarkerCenter,
				new Vector2(cloudWidth, cloudHeight),
				/* renderable= */ null
		);
		cloudMarker.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
		cloudMarker.setTag("cloudMarker");

		// 4) Add the marker to the scene on the BACKGROUND layer
		gameObjects.addGameObject(cloudMarker, danogl.collisions.Layer.BACKGROUND);

		// 5) Build each pixel‐art block, compute its offset from the marker, and add to scene
		Vector2 containerTopLeft = new Vector2(
				initialMarkerCenter.x() - (cloudWidth / 2f),
				initialMarkerCenter.y() - (cloudHeight / 2f)
		);

		List<GameObject> blocks = new ArrayList<>();
		List<Vector2>    offsets = new ArrayList<>();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (CLOUD_SHAPE.get(r).get(c) == 1) {
					// (a) Compute this block’s top-left corner
					Vector2 blockTopLeft = new Vector2(
							containerTopLeft.x() + (c * Block.SIZE),
							containerTopLeft.y() + (r * Block.SIZE)
					);
					RectangleRenderable rr = new RectangleRenderable(CLOUD_BLOCK_COLOR);

					// (b) Create the block and add it to BACKGROUND
					GameObject block = new Block(blockTopLeft, rr);
					block.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
					block.setTag("cloudBlock");
					gameObjects.addGameObject(block, danogl.collisions.Layer.BACKGROUND);

					// (c) Compute and store the offset = (blockCenter - markerCenter)
					Vector2 offset = new Vector2(
							block.getCenter().x() - initialMarkerCenter.x(),
							block.getCenter().y() - initialMarkerCenter.y()
					);
					blocks.add(block);
					offsets.add(offset);
				}
			}
		}

		// 6) Attach a single Component to the marker that:
		//    • Drifts the marker left and wraps around.
		//    • Repositions all blocks each frame at (markerCenter + storedOffset).
		//    • On avatar.jumpJustStarted(): spawn one raindrop from a random X under the cloud.
		//    • While avatar.isInAir(): spawn additional raindrops every RAIN_SPAWN_INTERVAL.
		cloudMarker.addComponent(new danogl.components.Component() {
			private float timeSinceLastRain = 0f;
			private final Random random = new Random();

			/**
			 * Called once per frame to:
			 * <ol>
			 *   <li>Move the cloud marker left by CLOUD_SPEED * dt; wrap to the right edge if off-screen.</li>
			 *   <li>Reposition each cloud block to (markerCenter + its stored offset).</li>
			 *   <li>If {@code avatar.jumpJustStarted()} is true, spawn one raindrop at a random X under the cloud,
			 *       clear the jump flag, and reset the rain timer.</li>
			 *   <li>Otherwise, if {@code timeSinceLastRain} ≥ RAIN_SPAWN_INTERVAL and {@code avatar.isInAir()} is true,
			 *       spawn a raindrop at a random X under the cloud and reset the rain timer.</li>
			 * </ol>
			 *
			 * @param dt Time (in seconds) since the last frame; used for motion and timing calculations.
			 */
			@Override
			public void update(float dt) {
				// —— (a) Drift the marker left by CLOUD_SPEED * dt; wrap if off-screen ——
				float newMarkerX = cloudMarker.getCenter().x() - (CLOUD_SPEED * dt);
				cloudMarker.transform().setCenterX(newMarkerX);
				if (newMarkerX < -(cloudWidth / 2f)) {
					cloudMarker.transform()
							.setCenterX(windowDimensions.x() + (cloudWidth / 2f));
				}

				// —— (b) Reposition all blocks to markerCenter + theirStoredOffset ——
				Vector2 markerC = cloudMarker.getCenter();
				for (int i = 0; i < blocks.size(); i++) {
					Vector2 off = offsets.get(i);
					GameObject block = blocks.get(i);
					block.transform().setCenter(
							markerC.x() + off.x(),
							markerC.y() + off.y()
					);
				}

				// —— (c) Immediate raindrop when avatar.jumpJustStarted() ——
				if (avatar.jumpJustStarted()) {
					float randomX = random.nextFloat() * cloudWidth
							+ (markerC.x() - (cloudWidth / 2f));
					spawnRaindropAtX(randomX);
					avatar.clearJumpJustStarted();
					timeSinceLastRain = 0f;
				}

				// —— (d) While avatar is in the air, spawn more raindrops each interval ——
				timeSinceLastRain += dt;
				if (timeSinceLastRain >= RAIN_SPAWN_INTERVAL) {
					if (avatar.isInAir()) {
						float randomX = random.nextFloat() * cloudWidth
								+ (markerC.x() - (cloudWidth / 2f));
						spawnRaindropAtX(randomX);
					}
					timeSinceLastRain = 0f;
				}
			}

			/**
			 * Spawns a single raindrop at the specified X-coordinate, positioned just under the bottom
			 * of the cloud. The new raindrop falls downward at RAIN_FALL_SPEED and, upon reaching the
			 * bottom of the window, fades out over RAIN_FADE_DURATION seconds, then is removed.
			 *
			 * @param dropX The X-coordinate at which the raindrop’s center should be spawned.
			 */
			private void spawnRaindropAtX(float dropX) {
				final float dropSize = 10f;
				// Compute Y = cloudBottom + half drop size
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

				// Attach a small Component so the drop falls and fades out on bottom
				drop.addComponent(new danogl.components.Component() {
					private boolean startedFading = false;
					private float fadeElapsed = 0f;

					/**
					 * Called once per frame on the raindrop to:
					 * <ol>
					 *   <li>Move the drop downward at RAIN_FALL_SPEED if it has not yet reached the bottom.</li>
					 *   <li>Once the drop’s center crosses the bottom of the window, begin fading alpha
					 *       from 1 to 0 over RAIN_FADE_DURATION.</li>
					 *   <li>After fading completes, remove the raindrop from the scene.</li>
					 * </ol>
					 *
					 * @param dt Time (in seconds) since the last frame; used for motion and fading calculations.
					 */
					@Override
					public void update(float dt) {
						if (!startedFading) {
							// (1) Fall straight down
							float newY = drop.getCenter().y() + (RAIN_FALL_SPEED * dt);
							drop.transform().setCenterY(newY);
							// (2) Begin fade once its center crosses the bottom line
							if (newY >= windowDimensions.y() - (dropSize / 2f)) {
								startedFading = true;
								fadeElapsed = 0f;
							}
						} else {
							// Fade alpha linearly over RAIN_FADE_DURATION
							fadeElapsed += dt;
							float alpha = 1f - (fadeElapsed / RAIN_FADE_DURATION);
							drop.renderer().setOpaqueness(Math.max(0f, alpha));
							// Remove once fade completes
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
