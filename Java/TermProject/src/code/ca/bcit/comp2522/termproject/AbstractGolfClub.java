package ca.bcit.comp2522.termproject;

/**
 * Abstract base for golf club implementations.
 * Provides shared distance computation logic
 * and delegates club-specific adjustment to subclasses.
 * <p>
 *
 * @author Taylor
 * @version 1.0
 */
public abstract class AbstractGolfClub implements GolfClub
{
    private static final double PERCENT_TO_DECIMAL_DIVISOR = 100.0;

    private final String displayName;

    private final double baseDistancePixels;

    /**
     * Constructs an AbstractGolfClub with a display name and base travel distance.
     *
     * @param displayName        human-readable name shown on UI
     * @param baseDistancePixels base distance in pixels at full power
     */
    protected AbstractGolfClub(final String displayName,
                               final double baseDistancePixels)
    {
        this.displayName        = displayName;
        this.baseDistancePixels = baseDistancePixels;
    }

    /**
     * Returns the display name of the club.
     *
     * @return display name string
     */
    @Override
    public final String getDisplayName()
    {
        return displayName;
    }

    /**
     * Computes expected horizontal distance for this club given the shot context.
     * The steps are:
     * <ol>
     *     <li>Convert power percentage to a decimal multiplier.</li>
     *     <li>Multiply by terrain distance multiplier.</li>
     *     <li>Apply subclass-specific adjustment.</li>
     * </ol>
     *
     * @param shotContext description of power and terrain characteristics
     * @return a {@link ShotResult} containing the adjusted expected distance in pixels
     */
    @Override
    public final ShotResult computeShot(final ShotContext shotContext)
    {
        final double powerMultiplier;
        final double terrainDistanceMultiplier;
        final double rawDistancePixels;
        final double adjustedDistancePixels;
        final ShotResult shotResult;


        powerMultiplier =
            shotContext.getPowerPercentage() / PERCENT_TO_DECIMAL_DIVISOR;

        terrainDistanceMultiplier =
            shotContext.getTerrainDistanceMultiplier();

        rawDistancePixels =
            baseDistancePixels * powerMultiplier * terrainDistanceMultiplier;

        adjustedDistancePixels =
            adjustDistanceForClub(rawDistancePixels, shotContext);

        shotResult = new ShotResult(adjustedDistancePixels);

        return shotResult;
    }

    /**
     * Allows subclasses to fine-tune the computed distance.
     *
     * @param rawDistancePixels base computed distance in pixels
     * @param shotContext       description of the current shot
     * @return adjusted distance in pixels
     */
    protected abstract double adjustDistanceForClub(final double rawDistancePixels,
                                                    final ShotContext shotContext);
}
