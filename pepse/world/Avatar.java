package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.util.Vector2;
import java.awt.event.KeyEvent;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.ImageRenderable;

/**
 * Represents the player-controlled avatar in the Pepse world.
 * The Avatar can move left/right, jump, and has an energy meter that depletes on movement/jumping
 * and regenerates when idle on the ground. The class handles animation switching (idle/run/jump),
 * flipping the sprite based on direction, and exposes flags to allow other objects (e.g., Cloud)
 * to detect when a jump begins or when the avatar is airborne.
 * @author Roni
 */
public class Avatar extends GameObject {
	// ─── Physics parameters ──────────────────────────────────────────────────────
	private static final float GRAVITY        = 600f;
	private static final float VELOCITY_X     = 400f;
	private static final float VELOCITY_Y     = -650f;
	// ─── Energy consumption parameters ──────────────────────────────────────────
	private static final float JUMP_COST      = 10f;
	private static final float MOVE_COST      = 0.5f;
	private static final float IDLE_REGEN     = 1f;
	// ─── Energy parameters ──────────────────────────────────────────────────────
	private static final float MAX_ENERGY     = 100f;
	// ─── Size of the avatar in pixels ────────────────────────────────────────────
	public  static final float SIZE          = 50f;
	// ─── Animation renderables and parameters ────────────────────────────────────
	private AnimationRenderable idleAnim;
	private AnimationRenderable runAnim;
	private AnimationRenderable jumpAnim;
	private static final float ONE_MS = 0.1f;
	private static final float TWO_MS = 0.2f;
	// ─── State variables ────────────────────────────────────────────────────────
	private boolean facingLeft = false; // to handle flipping
	private String state = "idle"; // current animation state: "idle", "run", or "jump"
    // ─── Input listener ─────────────────────────────────────────────────────────
	private final UserInputListener inputListener;
	// ─── Energy management ──────────────────────────────────────────────────────
	private float energy = MAX_ENERGY;

	// ─── Flags for ground/jump state, used by Cloud to decide when to rain ─────
	private boolean isOnGround = false;
	private boolean jumpJustStarted = false;

	// ─── Cases ──────────────────────────────────────────────────────
	private final String JUMP = "jump";
	private final String RUN = "run";
	private final String IDLE = "idle";

	// ─── Tags ──────────────────────────────────────────────────────
	private final String GROUND = "ground";

	/**
	 * Constructs an Avatar instance at the specified position with input and image resources.
	 * Initializes physics parameters (gravity, collision prevention) and loads the idle, run,
	 * and jump animations from image assets. Sets the initial renderable to the idle animation.
	 *
	 * @param topLeftCorner  The top-left corner of the avatar’s bounding box in world coordinates.
	 * @param inputListener  The UserInputListener for capturing keyboard events (left, right, jump).
	 * @param imageReader    The ImageReader used to load PNG frames for animations.
	 */
	public Avatar(Vector2 topLeftCorner,
				  UserInputListener inputListener,
				  ImageReader imageReader) {
		super(
				topLeftCorner,
				Vector2.ONES.mult(SIZE),
				imageReader.readImage("pepse/assets/idle_0.png", true)
		);
		physics().preventIntersectionsFromDirection(Vector2.ZERO);
		transform().setAccelerationY(GRAVITY);
		this.inputListener = inputListener;

		// --- Idle animation frames ---
		ImageRenderable[] idleFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/idle_0.png", true),
				imageReader.readImage("pepse/assets/idle_1.png", true),
				imageReader.readImage("pepse/assets/idle_2.png", true),
				imageReader.readImage("pepse/assets/idle_3.png", true)
		};
		idleAnim = new AnimationRenderable(idleFrames, TWO_MS);

		// --- Jump animation frames ---
		ImageRenderable[] jumpFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/jump_0.png", true),
				imageReader.readImage("pepse/assets/jump_1.png", true),
				imageReader.readImage("pepse/assets/jump_2.png", true),
				imageReader.readImage("pepse/assets/jump_3.png", true)
		};
		jumpAnim = new AnimationRenderable(jumpFrames, ONE_MS);

		// --- Run animation frames ---
		ImageRenderable[] runFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/run_0.png", true),
				imageReader.readImage("pepse/assets/run_1.png", true),
				imageReader.readImage("pepse/assets/run_2.png", true),
				imageReader.readImage("pepse/assets/run_3.png", true),
				imageReader.readImage("pepse/assets/run_4.png", true),
				imageReader.readImage("pepse/assets/run_5.png", true)
		};
		runAnim = new AnimationRenderable(runFrames, ONE_MS);

		// Set initial animation to idle
		renderer().setRenderable(idleAnim);
	}

	/**
	 * Updates the avatar’s state each frame: handles gravity, horizontal movement based on left/right keys,
	 * jump logic when the space key is pressed, energy consumption and regeneration, animation switching,
	 * and flipping the sprite horizontally based on direction of motion.
	 *
	 * @param deltaTime Time (in seconds) since the last frame; used for consistent physics updates.
	 */
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		float xVel = transform().getVelocity().x();
		float yVel = transform().getVelocity().y();

		// Determine new animation state based on vertical/horizontal velocities
		String newState;
		if (yVel != 0) {
			newState = JUMP;
		} else if (xVel != 0) {
			newState = RUN;
		} else {
			newState = IDLE;
		}

		// Flip sprite horizontally if moving left vs. right
		if (xVel < 0 && !facingLeft) {
			renderer().setIsFlippedHorizontally(true);
			facingLeft = true;
		} else if (xVel > 0 && facingLeft) {
			renderer().setIsFlippedHorizontally(false);
			facingLeft = false;
		}

		// Switch animation if the state changed
		if (!newState.equals(state)) {
			switch (newState) {
				case IDLE:
					renderer().setRenderable(idleAnim);
					break;
				case RUN:
					renderer().setRenderable(runAnim);
					break;
				case JUMP:
					renderer().setRenderable(jumpAnim);
					break;
			}
			state = newState;
		}

		// Read input for movement/jump
		boolean leftPressed  = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
		boolean rightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
		boolean wantsJump    = inputListener.isKeyPressed(KeyEvent.VK_SPACE);

		// Horizontal movement: consume energy per frame while moving
		xVel = 0f;
		if (leftPressed && energy >= MOVE_COST) {
			xVel = -VELOCITY_X;
			energy -= MOVE_COST;
		}
		if (rightPressed && energy >= MOVE_COST) {
			xVel = VELOCITY_X;
			energy -= MOVE_COST;
		}
		transform().setVelocityX(xVel);

		// Check if on the ground (vertical velocity == 0)
		boolean onGroundNow = transform().getVelocity().y() == 0f;

		// Handle jump input: only jump if on ground and enough energy
		if (wantsJump && onGroundNow && energy >= JUMP_COST) {
			transform().setVelocityY(VELOCITY_Y);
			energy -= JUMP_COST;
			jumpJustStarted = true;
		}

		// Regenerate energy when idle on ground and not jumping
		if (xVel == 0f && onGroundNow && !wantsJump && energy < MAX_ENERGY) {
			energy += IDLE_REGEN;
		}
		energy = Math.max(0f, Math.min(MAX_ENERGY, energy));

		// Update ground state flag
		isOnGround = onGroundNow;
	}

	/**
	 * Called when the avatar collides with another GameObject. If the other object is tagged "block",
	 * the avatar’s vertical velocity is set to zero to simulate landing on the block.
	 *
	 * @param other     The other GameObject involved in the collision.
	 * @param collision The Collision object containing collision details (ignored here).
	 */
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		if (other.getTag().equals(GROUND)) {
			transform().setVelocityY(0f);
		}
	}

	/**
	 * Returns true if the avatar is currently airborne (not on the ground).
	 * Used by Cloud to determine when to trigger rain.
	 *
	 * @return {@code true} if the avatar is in the air; {@code false} if on ground.
	 */
	public boolean isInAir() {
		return !isOnGround;
	}

	/**
	 * Returns true on the very first frame after the avatar leaves the ground (i.e., starts a jump).
	 * After checking, callers should invoke {@link #clearJumpJustStarted()} to reset this flag.
	 *
	 * @return {@code true} if the jump has just started; {@code false} otherwise.
	 */
	public boolean jumpJustStarted() {
		return jumpJustStarted;
	}

	/**
	 * Clears the "just started jump" flag. Call this method once after reading {@link #jumpJustStarted()}
	 * to reset the flag for subsequent frames.
	 */
	public void clearJumpJustStarted() {
		jumpJustStarted = false;
	}

	/**
	 * Returns the avatar’s current energy level, clamped between 0 and {@value #MAX_ENERGY}.
	 * Energy is consumed during movement and jumping, and regenerates when idle on the ground.
	 *
	 * @return The current energy value.
	 */
	public float getEnergy() {
		return energy;
	}
	/**
	 * Adds the specified amount of energy to the avatar, clamping it to a maximum of {@value #MAX_ENERGY}.
	 * This method is used by collectable items like Fruit to restore energy.
	 *
	 * @param amount The amount of energy to add (can be negative to consume energy).
	 */
	public void addEnergy(float amount) {
		energy = Math.min(MAX_ENERGY, energy + amount);
	}
}
