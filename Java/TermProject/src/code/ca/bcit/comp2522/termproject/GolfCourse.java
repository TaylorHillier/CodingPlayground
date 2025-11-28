package ca.bcit.comp2522.termproject;

import java.util.List;
import java.util.Map;

/**
 * Represents a single golf hole made of terrain tiles and optional air obstacles.
 * Provides lookup utilities, par computation, and tile selection based on
 * world-space pixel coordinates.
 * <p>
 * Immutable container: tiles and obstacles are assigned once at construction.
 *
 * @author Taylor
 * @version 1.0
 */
public final class GolfCourse
{
    private static final double BALL_TEE_POSITION_RATIO    = 0.25;
    private static final double TILE_CENTER_RATIO          = 0.5;
    private static final double MINIMUM_HOLE_LENGTH_PIXELS = 0.0;

    private static final int PAR_MINIMUM          = 3;
    private static final int PAR_DEFAULT_FALLBACK = 4;
    private static final int PAR_MAXIMUM          = 5;

    private static final int DRIVER_STROKES_PAR3_THRESHOLD = 2;
    private static final int DRIVER_STROKES_PAR4_THRESHOLD = 3;

    private static final int    MIN_STROKES       = 0;
    private static final double MIN_ZERO_DOUBLE   = 0.0;
    private static final int    MIN_TERRAIN_TILES = 0;

    private final List<TerrainTile> terrainTiles;
    private final List<AirObstacle> airObstacles;
    private final int               parStrokes;

    /**
     * Creates a GolfCourse with a fixed set of terrain tiles, air obstacles, and
     * an initial par estimate. The layout cannot change after construction.
     *
     * @param terrainTiles list of terrain tiles making up the hole
     * @param airObstacles list of air obstacles above the course
     * @param parStrokes   initial par estimate (may be overridden by computePar)
     */
    public GolfCourse(final List<TerrainTile> terrainTiles,
                      final List<AirObstacle> airObstacles,
                      final int parStrokes)
    {
        this.terrainTiles = terrainTiles;
        this.airObstacles = airObstacles;
        this.parStrokes   = parStrokes;
    }

    /**
     * Returns the ordered terrain tiles of the hole.
     *
     * @return list of terrain tiles
     */
    public List<TerrainTile> getTerrainTiles()
    {
        return terrainTiles;
    }

    /**
     * Returns the air obstacles positioned above the hole.
     *
     * @return list of air obstacles
     */
    public List<AirObstacle> getAirObstacles()
    {
        return airObstacles;
    }

    /**
     * Returns the first terrain tile, representing the tee area.
     *
     * @return starting terrain tile
     */
    public TerrainTile getStartTile()
    {
        return terrainTiles.getFirst();
    }

    /**
     * Returns the final terrain tile in the layout.
     *
     * @return last terrain tile
     */
    public TerrainTile getLastTile()
    {
        return terrainTiles.getLast();
    }

    /**
     * Returns the terrain tile covering the given world X coordinate in pixels.
     * If X exceeds the course bounds, the last tile is returned.
     *
     * @param worldXPixels world-space x-position in pixels
     * @return containing tile
     */
    public TerrainTile getTileAtX(final double worldXPixels)
    {
        for (final TerrainTile terrainTile : terrainTiles)
        {
            final double startXPixels;
            final double endXPixels;

            startXPixels = terrainTile.getStartXPixels();
            endXPixels   = terrainTile.getEndXPixels();

            if (worldXPixels >= startXPixels && worldXPixels < endXPixels)
            {
                return terrainTile;
            }
        }

        return terrainTiles.getLast();
    }

    /**
     * Computes horizontal tee-to-cup distance in pixels.
     *
     * @return hole length in pixels
     */
    public double getHoleLengthPixels()
    {
        final TerrainTile teeTile;
        final TerrainTile holeTile;

        final double teeStartXPixels;
        final double teeEndXPixels;
        final double teeBallXPixels;

        final double holeStartXPixels;
        final double holeEndXPixels;
        final double holeCupXPixels;

        final double rawHoleLengthPixels;

        teeTile  = getStartTile();
        holeTile = getHoleTile();

        teeStartXPixels = teeTile.getStartXPixels();
        teeEndXPixels   = teeTile.getEndXPixels();

        teeBallXPixels = teeStartXPixels + (teeEndXPixels - teeStartXPixels) * BALL_TEE_POSITION_RATIO;

        holeStartXPixels = holeTile.getStartXPixels();
        holeEndXPixels   = holeTile.getEndXPixels();

        holeCupXPixels = holeStartXPixels + (holeEndXPixels - holeStartXPixels) * TILE_CENTER_RATIO;

        rawHoleLengthPixels = holeCupXPixels - teeBallXPixels;

        return Math.max(MINIMUM_HOLE_LENGTH_PIXELS, rawHoleLengthPixels);
    }

    /**
     * Returns the tile marked as the HOLE. If none exists, returns the last tile.
     *
     * @return the hole tile
     */
    public TerrainTile getHoleTile()
    {
        for (final TerrainTile terrainTile : terrainTiles)
        {
            if (terrainTile.getTerrainType() == TerrainType.HOLE)
            {
                return terrainTile;
            }
        }

        return getLastTile();
    }

    /**
     * Counts how many tiles match the given terrain type.
     *
     * @param terrainType type of terrain to count
     * @return count of tiles of that type
     */
    public long countTerrainTilesByType(final TerrainType terrainType)
    {
        long count;

        count = MIN_TERRAIN_TILES;

        for (final TerrainTile terrainTile : terrainTiles)
        {
            if (terrainTile.getTerrainType() == terrainType)
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Computes par based on the driver’s full-power fairway range.
     * <ul>
     *     <li>≤ 2 driver-lengths → par 3</li>
     *     <li>≤ 3 driver-lengths → par 4</li>
     *     <li>otherwise → par 20 (debug value preserved)</li>
     * </ul>
     *
     * @param golfClubsByType           mapping of club types to clubs
     * @param maximumPowerPercentage    power percentage for full swing
     * @param fairwayDistanceMultiplier fairway distance multiplier (usually 1.0)
     * @return computed par value
     */
    public int computePar(final Map<ClubType, GolfClub> golfClubsByType,
                          final double maximumPowerPercentage,
                          final double fairwayDistanceMultiplier)
    {
        final double holeLengthPixels;
        final ShotContext fullPowerContext;
        final GolfClub driverGolfClub;

        final double driverRangePixels;
        final double strokesEstimate;

        final int par;

        holeLengthPixels = getHoleLengthPixels();

        fullPowerContext = new ShotContext(maximumPowerPercentage, fairwayDistanceMultiplier);

        driverGolfClub = golfClubsByType.get(ClubType.DRIVER);

        final int strokes;

        if (parStrokes <= MIN_STROKES)
        {
            strokes = PAR_DEFAULT_FALLBACK;
        }
        else
        {
            strokes = parStrokes;
        }

        int max = Math.max(PAR_MINIMUM, Math.min(PAR_MAXIMUM, strokes));

        if (driverGolfClub == null)
        {
            return max;
        }

        driverRangePixels = driverGolfClub.computeShot(fullPowerContext)
                                          .getExpectedHorizontalRangePixels();

        if (driverRangePixels <= MIN_ZERO_DOUBLE)
        {
            return max;
        }

        strokesEstimate = holeLengthPixels / driverRangePixels;

        if (strokesEstimate <= DRIVER_STROKES_PAR3_THRESHOLD)
        {
            par = PAR_MINIMUM;
        }
        else if (strokesEstimate <= DRIVER_STROKES_PAR4_THRESHOLD)
        {
            par = PAR_DEFAULT_FALLBACK;
        }
        else
        {
            par = PAR_MAXIMUM;
        }
        
        return par;
    }
}
