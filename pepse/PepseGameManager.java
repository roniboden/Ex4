package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.util.Vector2;
import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;

import java.util.List;
import java.util.Random;

public class PepseGameManager extends GameManager {


	@Override
	public void initializeGame(ImageReader imageReader, SoundReader soundReader,
							   UserInputListener inputListener,
							   WindowController windowController) {
		super.initializeGame(imageReader, soundReader, inputListener, windowController);
		Vector2 windowDimensions = windowController.getWindowDimensions();
		//creates the Sky
		GameObject sky=  Sky.create(windowDimensions);
		this.gameObjects().addGameObject(sky, Layer.BACKGROUND);
		//creates th ground
		int seed = new Random().nextInt();
		Terrain terrain= new Terrain(windowDimensions,seed);
		List<Block> groundBlocks = terrain.createInRange(0, (int) windowDimensions.x());
		for (Block block : groundBlocks) {
			this.gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
		}
    }

    public static void main(String[] args) {
        new PepseGameManager().run();
    }
}