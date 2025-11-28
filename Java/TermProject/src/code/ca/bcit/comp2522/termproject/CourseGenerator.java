package ca.bcit.comp2522.termproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates random single-hole golf courses with terrain and air obstacles.
 * <p>
 * Coding standards applied:
 * <ul>
 *     <li>final on all parameters</li>
 *     <li>private static final constants for all non-trivial numeric values</li>
 *     <li>local variables declared at the top of each scope, then assigned</li>
 *     <li>no logic changes from the original implementation</li>
 * </ul>
 *
 * @author Taylor
 * @version 1.0
 */
public final class CourseGenerator
{
    private static final int    ALTER_BY_ONE    = 1;
    private static final int    MIN_VALUE       = 1;
    private static final double MIN_HOLE_LENGTH = 0.0;

    // -------------------- Terrain / Par Constants --------------------

    private static final double RANDOM_HEIGHT_STEP_PIXELS = 10.0;

    private static final double MINIMUM_PAR_STROKES = 3.0;

    private static final double PIXELS_PER_STROKE_FOR_PAR_GUESS = 200.0;

    private static final int HOLE_TILE_BACK_HALF_DIVISOR = 2;

    private static final double INITIAL_TERRAIN_START_X_PIXELS = 0.0;

    private static final double INITIAL_HEIGHT_OFFSET_PIXELS = 0.0;

    private static final double RANDOM_HEIGHT_DELTA_CENTERING_RATIO = 0.5;

    private static final int RESERVED_TILES_BEFORE_HOLE = 2;

    private static final double TEE_BALL_OFFSET_RATIO_FROM_TILE_START = 0.25;

    private static final double TILE_CENTER_OFFSET_RATIO = 0.5;

    // -------------------- Air Obstacle Constants --------------------

    private static final int MINIMUM_AIR_OBSTACLES_PER_HOLE = 1;

    private static final int MAXIMUM_AIR_OBSTACLES_PER_HOLE = 3;

    private static final double AIR_OBSTACLE_MIN_HEIGHT_PIXELS = 30.0;

    private static final double AIR_OBSTACLE_MAX_HEIGHT_PIXELS = 60.0;

    private static final double AIR_OBSTACLE_WIDTH_RATIO = 0.6;

    private static final int MINIMUM_TERRAIN_TILES_FOR_AIR_OBSTACLES = 6;

    private static final int AIR_OBSTACLE_START_TILE_INDEX_OFFSET = 2;

    private static final int AIR_OBSTACLE_RESERVED_TILES_AT_END = 4;

    private static final double AIR_OBSTACLE_TOP_EXTRA_CLEARANCE_PIXELS = 20.0;

    private static final double AIR_OBSTACLE_BOTTOM_EXTRA_CLEARANCE_PIXELS = 40.0;

    // -------------------- Green / Probability Constants --------------------

    private static final int MINIMUM_GREEN_START_TILE_INDEX = 1;

    private static final int GREEN_TILES_BEFORE_HOLE = 2;

    private static final int RANDOM_PERCENTAGE_UPPER_BOUND_EXCLUSIVE = 100;

    private static final int WATER_TERRAIN_MAX_PERCENTAGE = 10;

    private static final int SAND_TERRAIN_MAX_PERCENTAGE = 25;

    private static final int ROUGH_TERRAIN_MAX_PERCENTAGE = 35;

    // -------------------- Constructors --------------------

    private CourseGenerator()
    {
        // Utility class; prevent instantiation.
    }

    /**
     * Generates a single-hole course using random terrain tiles and air obstacles.
     *
     * @param randomNumberGenerator     source of randomness
     * @param numberOfTiles             number of tiles in the hole
     * @param tileWidthPixels           width of each tile in pixels
     * @param baseGroundCenterYPixels   base ground y-position in pixels
     * @param maximumHeightOffsetPixels maximum vertical offset allowed for ground
     * @return new GolfCourse instance
     */
    public static GolfCourse generateSingleHole(final Random randomNumberGenerator,
                                                final int numberOfTiles,
                                                final double tileWidthPixels,
                                                final double baseGroundCenterYPixels,
                                                final double maximumHeightOffsetPixels)
    {
        final List<TerrainTile> terrainTiles;
        final List<AirObstacle> airObstacles;

        final int minimumHoleTileIndex;
        final int maximumHoleTileIndex;
        final int holeTileIndex;

        double currentXPixels;
        double currentHeightOffsetPixels;

        final double teeBallXPixels;
        final double holeCupXPixels;
        final double holeLengthPixels;

        final int parStrokesGuess;

        terrainTiles = new ArrayList<>();
        airObstacles = new ArrayList<>();

        currentXPixels            = INITIAL_TERRAIN_START_X_PIXELS;
        currentHeightOffsetPixels = INITIAL_HEIGHT_OFFSET_PIXELS;

        // Choose a random tile index for the HOLE in the back half of the course.
        minimumHoleTileIndex = numberOfTiles / HOLE_TILE_BACK_HALF_DIVISOR;
        maximumHoleTileIndex = numberOfTiles - RESERVED_TILES_BEFORE_HOLE;

        holeTileIndex = minimumHoleTileIndex + randomNumberGenerator.nextInt(
            Math.max(MIN_VALUE, maximumHoleTileIndex - minimumHoleTileIndex + ALTER_BY_ONE));

        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++)
        {
            final double randomHeightChangePixels;
            final double groundCenterYPixels;
            final double startXPixels;
            final double endXPixels;
            final TerrainType terrainTypeForTile;

            randomHeightChangePixels = (randomNumberGenerator.nextDouble() - RANDOM_HEIGHT_DELTA_CENTERING_RATIO)
                                       * RANDOM_HEIGHT_STEP_PIXELS;

            currentHeightOffsetPixels += randomHeightChangePixels;

            currentHeightOffsetPixels = clamp(currentHeightOffsetPixels,
                                              -maximumHeightOffsetPixels,
                                              maximumHeightOffsetPixels);

            groundCenterYPixels = baseGroundCenterYPixels + currentHeightOffsetPixels;

            startXPixels = currentXPixels;
            endXPixels   = currentXPixels + tileWidthPixels;

            if (tileIndex == holeTileIndex)
            {
                // Exact tile that contains the cup.
                terrainTypeForTile = TerrainType.HOLE;
            }
            else
            {
                final int greenStartIndex;
                final int greenEndIndex;
                final boolean inGreenZone;

                greenStartIndex = Math.max(MINIMUM_GREEN_START_TILE_INDEX, holeTileIndex - GREEN_TILES_BEFORE_HOLE);
                greenEndIndex   = holeTileIndex - ALTER_BY_ONE;
                inGreenZone     = tileIndex >= greenStartIndex && tileIndex <= greenEndIndex;

                if (inGreenZone)
                {
                    terrainTypeForTile = TerrainType.GREEN;
                }
                else
                {
                    final int randomValuePercentage;

                    randomValuePercentage = randomNumberGenerator.nextInt(RANDOM_PERCENTAGE_UPPER_BOUND_EXCLUSIVE);

                    if (randomValuePercentage < WATER_TERRAIN_MAX_PERCENTAGE)
                    {
                        terrainTypeForTile = TerrainType.WATER;
                    }
                    else if (randomValuePercentage < SAND_TERRAIN_MAX_PERCENTAGE)
                    {
                        terrainTypeForTile = TerrainType.SAND;
                    }
                    else if (randomValuePercentage < ROUGH_TERRAIN_MAX_PERCENTAGE)
                    {
                        terrainTypeForTile = TerrainType.ROUGH;
                    }
                    else
                    {
                        terrainTypeForTile = TerrainType.FAIRWAY;
                    }
                }
            }

            terrainTiles.add(
                new TerrainTile(
                    startXPixels,
                    endXPixels,
                    groundCenterYPixels,
                    terrainTypeForTile
                )
                            );

            currentXPixels += tileWidthPixels;
        }

        // Tee ball X: quarter into the first tile (matches initializeBallAtTee).
        teeBallXPixels = tileWidthPixels * TEE_BALL_OFFSET_RATIO_FROM_TILE_START;

        // Hole cup X: middle of the hole tile.
        holeCupXPixels = holeTileIndex * tileWidthPixels + tileWidthPixels * TILE_CENTER_OFFSET_RATIO;

        holeLengthPixels = Math.max(MIN_HOLE_LENGTH, holeCupXPixels - teeBallXPixels);

        parStrokesGuess = (int) Math.max(MINIMUM_PAR_STROKES,
                                         Math.round(holeLengthPixels / PIXELS_PER_STROKE_FOR_PAR_GUESS));

        generateAirObstaclesForHole(randomNumberGenerator,
                                    terrainTiles,
                                    maximumHeightOffsetPixels,
                                    tileWidthPixels,
                                    airObstacles);

        return new GolfCourse(terrainTiles, airObstacles, parStrokesGuess);
    }

    // -------------------- Internal Helpers --------------------

    private static void generateAirObstaclesForHole(final Random randomNumberGenerator,
                                                    final List<TerrainTile> terrainTiles,
                                                    final double maximumHeightOffsetPixels,
                                                    final double tileWidthPixels,
                                                    final List<AirObstacle> airObstacles)
    {
        final int terrainTileCount;

        terrainTileCount = terrainTiles.size();

        if (terrainTileCount < MINIMUM_TERRAIN_TILES_FOR_AIR_OBSTACLES)
        {
            return;
        }

        final int obstacleCount;
        final int lastIndex;

        obstacleCount = MINIMUM_AIR_OBSTACLES_PER_HOLE + randomNumberGenerator.nextInt(
            MAXIMUM_AIR_OBSTACLES_PER_HOLE - MINIMUM_AIR_OBSTACLES_PER_HOLE + 1);

        lastIndex = terrainTileCount - ALTER_BY_ONE;

        for (int obstacleIndex = 0; obstacleIndex < obstacleCount; obstacleIndex++)
        {
            final int tileIndex;
            final TerrainTile baseTile;
            final double groundCenterYPixels;

            final double obstacleWidthPixels;
            final double centerXPixels;
            final double leftXPixels;
            final double rightXPixels;

            final double topLimitYPixels;
            final double bottomLimitYPixels;

            final double obstacleHeightPixels;
            final double topYPixels;
            final double bottomYPixels;

            // Avoid very early tiles (tee) and last tiles (green/hole).
            tileIndex = AIR_OBSTACLE_START_TILE_INDEX_OFFSET + randomNumberGenerator.nextInt(
                Math.max(MIN_VALUE, lastIndex - AIR_OBSTACLE_RESERVED_TILES_AT_END));

            baseTile = terrainTiles.get(tileIndex);

            groundCenterYPixels = baseTile.getGroundCenterYPixels();

            obstacleWidthPixels = tileWidthPixels * AIR_OBSTACLE_WIDTH_RATIO;

            centerXPixels =
                (baseTile.getStartXPixels() + baseTile.getEndXPixels()) * TILE_CENTER_OFFSET_RATIO;

            leftXPixels  = centerXPixels - obstacleWidthPixels * TILE_CENTER_OFFSET_RATIO;
            rightXPixels = centerXPixels + obstacleWidthPixels * TILE_CENTER_OFFSET_RATIO;

            // Put obstacle somewhere between "cliff top" and mid-air.
            topLimitYPixels = groundCenterYPixels - maximumHeightOffsetPixels - AIR_OBSTACLE_TOP_EXTRA_CLEARANCE_PIXELS;

            bottomLimitYPixels = groundCenterYPixels - AIR_OBSTACLE_BOTTOM_EXTRA_CLEARANCE_PIXELS;

            if (bottomLimitYPixels <= topLimitYPixels)
            {
                continue;
            }

            obstacleHeightPixels = AIR_OBSTACLE_MIN_HEIGHT_PIXELS + randomNumberGenerator.nextDouble() *
                                                                    (AIR_OBSTACLE_MAX_HEIGHT_PIXELS - AIR_OBSTACLE_MIN_HEIGHT_PIXELS);

            topYPixels = topLimitYPixels + randomNumberGenerator.nextDouble()
                                           * (bottomLimitYPixels - topLimitYPixels - obstacleHeightPixels);

            bottomYPixels = topYPixels + obstacleHeightPixels;

            airObstacles.add(new AirObstacle(leftXPixels,
                                             rightXPixels,
                                             topYPixels,
                                             bottomYPixels)
                            );
        }
    }

    private static double clamp(final double value,
                                final double minimum,
                                final double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
