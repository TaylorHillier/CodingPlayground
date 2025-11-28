package ca.bcit.comp2522.termproject;

import java.util.List;
import java.util.Objects;

public final class TerrainTileUtils
{
    private TerrainTileUtils()
    {
        // Prevent instantiation.
    }

    /**
     * Returns the first tile of the requested terrain type from the given list,
     * or null if none is found.
     *
     * @param terrainTiles       list of tiles to search (producer of tiles)
     * @param desiredTerrainType type to look for
     * @param <T>                concrete tile type extending TerrainTile
     * @return first matching tile or null
     */
    public static <T extends TerrainTile> T findFirstTileOfType(
        final List<T> terrainTiles,
        final TerrainType desiredTerrainType)
    {
        Objects.requireNonNull(terrainTiles, "terrainTiles must not be null");
        Objects.requireNonNull(desiredTerrainType, "desiredTerrainType must not be null");

        for (final T terrainTile : terrainTiles)
        {
            if (terrainTile.getTerrainType() == desiredTerrainType)
            {
                return terrainTile;
            }
        }

        return null;
    }
    
}
