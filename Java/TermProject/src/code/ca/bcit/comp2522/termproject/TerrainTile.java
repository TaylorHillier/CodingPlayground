package ca.bcit.comp2522.termproject;

/**
 * Represents a single horizontal terrain segment in the course.
 * The tile covers [startXPixels, endXPixels) at a given ground height.
 *
 * @author Taylor
 * @version 1.0
 */
public final class TerrainTile
{
    private final double      startXPixels;
    private final double      endXPixels;
    private final double      groundCenterYPixels;
    private final TerrainType terrainType;

    /**
     * Constructs a TerrainTile.
     *
     * @param startXPixels        inclusive start x-coordinate in pixels
     * @param endXPixels          exclusive end x-coordinate in pixels
     * @param groundCenterYPixels y-coordinate of the ground in pixels
     * @param terrainType         type of terrain for this tile
     */
    public TerrainTile(final double startXPixels,
                       final double endXPixels,
                       final double groundCenterYPixels,
                       final TerrainType terrainType)
    {
        this.startXPixels        = startXPixels;
        this.endXPixels          = endXPixels;
        this.groundCenterYPixels = groundCenterYPixels;
        this.terrainType         = terrainType;
    }

    public double getStartXPixels()
    {
        return startXPixels;
    }

    public double getEndXPixels()
    {
        return endXPixels;
    }

    public double getGroundCenterYPixels()
    {
        return groundCenterYPixels;
    }

    public TerrainType getTerrainType()
    {
        return terrainType;
    }

    /**
     * Returns true if the given x-coordinate lies within this tile.
     *
     * @param worldXPixels x-coordinate in world pixels
     * @return true if worldXPixels is in [startXPixels, endXPixels)
     */
    public boolean containsX(final double worldXPixels)
    {
        return worldXPixels >= startXPixels && worldXPixels < endXPixels;
    }
}
