package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;

public class Avatar extends GameObject {
	private static final float GRAVITY =600 ;
	private static final float VELOCITY_X = 400;
	private static final float VELOCITY_Y =-650 ;
	private static final int JUMP_VALUE = 10;
	private static final int MOVE_VALUE = 1;
	private static final int STANDING_VALUE = 1;
	private static final int DEF_ENERGY = 100;
	private final UserInputListener inputListener;
	private int energy = DEF_ENERGY;


	public Avatar(Vector2 topLeftCorner,
				  UserInputListener inputListener,
				  ImageReader imageReader) {
		super(topLeftCorner, Vector2.ONES.mult(50), imageReader.readImage("pepse/assets/idle_0.png",true));
		physics().preventIntersectionsFromDirection(Vector2.ZERO);
		transform().setAccelerationY(GRAVITY);
		this.inputListener = inputListener;
	}
	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		float xVel = 0;
		if (inputListener.isKeyPressed(KeyEvent.VK_LEFT) && energy > MOVE_VALUE) {
			energy=energy-MOVE_VALUE;
			xVel -= VELOCITY_X;
		}
		if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)&& energy > MOVE_VALUE) {
			energy=energy-MOVE_VALUE;
			xVel += VELOCITY_X;
		}
		transform().setVelocityX(xVel);
		if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && getVelocity().y() == 0 && energy > JUMP_VALUE) {
			energy=energy-JUMP_VALUE;
			transform().setVelocityY(VELOCITY_Y);
		}
		else {
			if (energy < DEF_ENERGY) {
				energy++;
			}
		}
	}
	public int getEnergy() {
		return energy;

	}
	@Override
	public void onCollisionEnter(GameObject other, Collision collision) {
		super.onCollisionEnter(other, collision);
		if(other.getTag().equals("block")){
			this.transform().setVelocityY(0);
		}
	}

}
