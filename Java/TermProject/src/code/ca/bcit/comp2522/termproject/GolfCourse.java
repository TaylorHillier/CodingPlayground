package ca.bcit.comp2522.termproject;

import java.util.List;

/**
 * Represents a complete golf hole, composed of a sequence of TerrainTile instances.
 *
 * @author Taylor
 * @version 1.0
 */
public final class GolfCourse
{
    private final List<TerrainTile> terrainTiles;
    private final int               parStrokes;

    /**
     * Constructs a GolfCourse.
     *
     * @param terrainTiles the tiles that make up the hole, left to right
     * @param parStrokes   the par value for this hole
     */
    public GolfCourse(final List<TerrainTile> terrainTiles,
                      final int parStrokes)
    {
        this.terrainTiles = List.copyOf(terrainTiles);
        this.parStrokes   = parStrokes;
    }

    public TerrainTile getTileAtX(final double worldXPixels)
    {
        for (final TerrainTile terrainTile : terrainTiles)
        {
            if (terrainTile.containsX(worldXPixels))
            {
                return terrainTile;
            }
        }

        return terrainTiles.get(terrainTiles.size() - 1);
    }

    public TerrainTile getStartTile()
    {
        return terrainTiles.get(0);
    }

    public TerrainTile getLastTile()
    {
        return terrainTiles.get(terrainTiles.size() - 1);
    }

    public List<TerrainTile> getTerrainTiles()
    {
        return terrainTiles;
    }

    public int getParStrokes()
    {
        return parStrokes;
    }
}
