package ca.bcit.comp2522.termproject;

/**
 * Represents a single horizontal terrain segment in the golf course.
 * A {@code TerrainTile} covers a continuous x-range
 * {@code [startXPixels, endXPixels)} at a specific ground height in pixels.
 *
 * <p>This class is immutable. Once constructed, its bounds and terrain type cannot change.</p>
 * <p>
 * author Taylor
 * version 1.0
 */
public final class TerrainTile
{
    private final double startXPixels;

    private final double endXPixels;

    private final double groundCenterYPixels;

    private final TerrainType terrainType;

    /**
     * Constructs a {@code TerrainTile} with explicit pixel boundaries and a terrain type.
     *
     * @param startXPixels        inclusive start x-coordinate in pixels
     * @param endXPixels          exclusive end x-coordinate in pixels
     * @param groundCenterYPixels y-coordinate of the ground center in pixels
     * @param terrainType         terrain type associated with this tile
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

    /**
     * Gets the inclusive starting x-coordinate of this tile in pixels.
     *
     * @return start x-coordinate in pixels
     */
    public double getStartXPixels()
    {
        return startXPixels;
    }

    /**
     * Gets the exclusive ending x-coordinate of this tile in pixels.
     *
     * @return end x-coordinate in pixels
     */
    public double getEndXPixels()
    {
        return endXPixels;
    }

    /**
     * Gets the vertical center point of the terrain in this tile, in pixels.
     *
     * @return ground y-coordinate in pixels
     */
    public double getGroundCenterYPixels()
    {
        return groundCenterYPixels;
    }

    /**
     * Gets the terrain type of this tile.
     *
     * @return terrain type value
     */
    public TerrainType getTerrainType()
    {
        return terrainType;
    }

    /**
     * Determines whether the provided world x-coordinate lies within this tile's range.
     * The range is {@code [startXPixels, endXPixels)}, meaning the start is inclusive
     * and the end is exclusive.
     *
     * @param worldXPixelsParameter world-space x-coordinate in pixels
     * @return {@code true} if the coordinate lies within the tile’s horizontal span
     */
    public boolean containsX(final double worldXPixelsParameter)
    {
        final boolean isWithinLowerBound;
        final boolean isWithinUpperBound;
        final boolean isWithinTile;

        isWithinLowerBound = (worldXPixelsParameter >= startXPixels);
        isWithinUpperBound = (worldXPixelsParameter < endXPixels);
        isWithinTile       = isWithinLowerBound && isWithinUpperBound;

        return isWithinTile;
    }
}
