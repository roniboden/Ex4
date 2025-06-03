package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.util.Vector2;
import java.awt.event.KeyEvent;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.ImageRenderable;
import danogl.util.Vector2;

/**
 * Avatar with move/jump/energy logic, plus flags so that Cloud can detect
 * when the avatar is in the air and has just jumped.
 */
public class Avatar extends GameObject {
	private static final float GRAVITY        = 600f;
	private static final float VELOCITY_X     = 400f;
	private static final float VELOCITY_Y     = -650f;
	private static final float JUMP_COST      = 10f;
	private static final float MOVE_COST      = 0.5f;
	private static final float IDLE_REGEN     = 1f;
	private static final float MAX_ENERGY     = 100f;
	public  static final float SIZE          = 50f;
	private AnimationRenderable idleAnim;
	private AnimationRenderable runAnim;
	private AnimationRenderable jumpAnim;
	private boolean facingLeft = false; // to handle flipping
	private String state = "idle"; // "idle", "run", or "jump"

	private final UserInputListener inputListener;
	private float energy = MAX_ENERGY;

	// ─── Flags for ground/jump state, used by Cloud to decide when to rain ─────
	private boolean isOnGround = false;
	private boolean jumpJustStarted = false;

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

		// --- Idle animation ---
		ImageRenderable[] idleFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/idle_0.png", true),
				imageReader.readImage("pepse/assets/idle_1.png", true),
				imageReader.readImage("pepse/assets/idle_2.png", true),
				imageReader.readImage("pepse/assets/idle_3.png", true)
		};
		idleAnim = new AnimationRenderable(idleFrames, 0.2f); // change 0.2f to set frame rate

// --- Jump animation ---
		ImageRenderable[] jumpFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/jump_0.png", true),
				imageReader.readImage("pepse/assets/jump_1.png", true),
				imageReader.readImage("pepse/assets/jump_2.png", true),
				imageReader.readImage("pepse/assets/jump_3.png", true)
		};
		jumpAnim = new AnimationRenderable(jumpFrames, 0.1f);

// --- Run animation ---
		ImageRenderable[] runFrames = new ImageRenderable[] {
				imageReader.readImage("pepse/assets/run_0.png", true),
				imageReader.readImage("pepse/assets/run_1.png", true),
				imageReader.readImage("pepse/assets/run_2.png", true),
				imageReader.readImage("pepse/assets/run_3.png", true),
				imageReader.readImage("pepse/assets/run_4.png", true),
				imageReader.readImage("pepse/assets/run_5.png", true)
		};
		runAnim = new AnimationRenderable(runFrames, 0.1f);

// Set initial animation
		renderer().setRenderable(idleAnim);

	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);

		float xVel = transform().getVelocity().x();
		float yVel = transform().getVelocity().y();

		// Choose state
		String newState;
		if (yVel != 0) {
			newState = "jump";
		} else if (xVel != 0) {
			newState = "run";
		} else {
			newState = "idle";
		}

		// Flip sprite if direction changed
		if (xVel < 0 && !facingLeft) {
			renderer().setIsFlippedHorizontally(true);
			facingLeft = true;
		} else if (xVel > 0 && facingLeft) {
			renderer().setIsFlippedHorizontally(false);
			facingLeft = false;
		}

		// Change animation if needed
		if (!newState.equals(state)) {
			switch (newState) {
				case "idle":
					renderer().setRenderable(idleAnim);
					break;
				case "run":
					renderer().setRenderable(runAnim);
					break;
				case "jump":
					renderer().setRenderable(jumpAnim);
					break;
			}
			state = newState;
		}

		boolean leftPressed  = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
		boolean rightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
		boolean wantsJump    = inputListener.isKeyPressed(KeyEvent.VK_SPACE);

		// Run left
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

		// Detect on-ground: vertical velocity == 0
		boolean onGroundNow = getVelocity().y() == 0f;

		// Handle jump input
		if (wantsJump && onGroundNow && energy >= JUMP_COST) {
			transform().setVelocityY(VELOCITY_Y);
			energy -= JUMP_COST;
			// Mark that a jump has just started
			jumpJustStarted = true;
		}

		// Regenerate energy when idle on ground
		if (xVel == 0f && onGroundNow && !wantsJump && energy < MAX_ENERGY) {
			energy += IDLE_REGEN;
		}
		energy = Math.max(0f, Math.min(MAX_ENERGY, energy));

		// Update ground flag
		isOnGround = onGroundNow;
	}

	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		// If we collide with a “block” (tagged "block"), we stop vertical motion
		if (other.getTag().equals("block")) {
			transform().setVelocityY(0f);
		}
	}

	/**
	 * Returns true if the avatar is currently in the air (not on ground).
	 * Used by Cloud to know when to spawn rain.
	 */
	public boolean isInAir() {
		return !isOnGround;
	}

	/**
	 * Returns true the very first frame after the avatar leaves the ground.
	 * After checking, call clearJumpJustStarted() to reset the flag.
	 */
	public boolean jumpJustStarted() {
		return jumpJustStarted;
	}

	/** Clears the “just started” jump flag—call once after reading it. */
	public void clearJumpJustStarted() {
		jumpJustStarted = false;
	}

	/** Current energy level of the avatar. */
	public float getEnergy() {
		return energy;
	}
}
