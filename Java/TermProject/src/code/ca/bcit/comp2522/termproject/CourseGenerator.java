package ca.bcit.comp2522.termproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates random single-hole golf courses.
 *
 * @author Taylor
 * @version 1.0
 */
public final class CourseGenerator
{
    private static final double MAX_HEIGHT_OFFSET_PIXELS  = 40.0;
    private static final double RANDOM_HEIGHT_STEP_PIXELS = 10.0;
    private static final double MINIMUM_PAR_STROKES       = 3.0;
    private static final double PIXELS_PER_STROKE_FOR_PAR = 200.0;

    private CourseGenerator()
    {
        // Utility class; prevent instantiation.
    }

    /**
     * Generates a single-hole course using random terrain tiles.
     *
     * @param randomNumberGenerator   source of randomness
     * @param numberOfTiles           number of tiles in the hole
     * @param tileWidthPixels         width of each tile in pixels
     * @param baseGroundCenterYPixels base ground y-position in pixels
     * @return new GolfCourse instance
     */
    public static GolfCourse generateSingleHole(final Random randomNumberGenerator,
                                                final int numberOfTiles,
                                                final double tileWidthPixels,
                                                final double baseGroundCenterYPixels)
    {
        final List<TerrainTile> terrainTiles = new ArrayList<>();

        double currentXPixels = 0.0;
        double currentHeightOffsetPixels = 0.0;

        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++)
        {
            final TerrainType terrainType;

            if (tileIndex == numberOfTiles - 1)
            {
                terrainType = TerrainType.HOLE;
            }
            else
            {
                final int randomValuePercentage = randomNumberGenerator.nextInt(100);

                if (randomValuePercentage < 10)
                {
                    terrainType = TerrainType.WATER;
                }
                else if (randomValuePercentage < 25)
                {
                    terrainType = TerrainType.SAND;
                }
                else if (randomValuePercentage < 35)
                {
                    terrainType = TerrainType.ROUGH;
                }
                else
                {
                    terrainType = TerrainType.FAIRWAY;
                }
            }

            final double randomHeightChangePixels =
                (randomNumberGenerator.nextDouble() - 0.5) * RANDOM_HEIGHT_STEP_PIXELS;

            currentHeightOffsetPixels += randomHeightChangePixels;

            currentHeightOffsetPixels =
                clamp(currentHeightOffsetPixels,
                      -MAX_HEIGHT_OFFSET_PIXELS,
                      MAX_HEIGHT_OFFSET_PIXELS);

            final double groundCenterYPixels =
                baseGroundCenterYPixels + currentHeightOffsetPixels;

            final double startXPixels = currentXPixels;
            final double endXPixels = currentXPixels + tileWidthPixels;

            final TerrainTile terrainTile =
                new TerrainTile(startXPixels,
                                endXPixels,
                                groundCenterYPixels,
                                terrainType);

            terrainTiles.add(terrainTile);

            currentXPixels = endXPixels;
        }

        final double holeLengthPixels =
            terrainTiles.get(terrainTiles.size() - 1).getEndXPixels();
        final int parStrokes =
            (int) Math.max(
                MINIMUM_PAR_STROKES,
                Math.round(holeLengthPixels / PIXELS_PER_STROKE_FOR_PAR)
                          );

        return new GolfCourse(terrainTiles, parStrokes);
    }

    private static double clamp(final double value,
                                final double minimum,
                                final double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
