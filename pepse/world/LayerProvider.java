package pepse.world;

import danogl.collisions.Layer;

/** Implemented by game objects that know which default layer they belong to. */
public interface LayerProvider {
	int defaultLayer();
}

