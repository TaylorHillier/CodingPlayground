package ca.bcit.comp2522.termproject;

/**
 * Abstract base for golf club implementations. Provides a common
 * distance calculation and delegates fine tuning to subclasses.
 *
 * @author Taylor
 * @version 1.0
 */
public abstract class AbstractGolfClub implements GolfClub
{
    private final String displayName;
    private final double baseDistancePixels;

    /**
     * Constructs an AbstractGolfClub.
     *
     * @param displayName        name shown to the player
     * @param baseDistancePixels base flat-ground distance in pixels at 100% power
     */
    protected AbstractGolfClub(final String displayName,
                               final double baseDistancePixels)
    {
        this.displayName        = displayName;
        this.baseDistancePixels = baseDistancePixels;
    }

    @Override
    public String getDisplayName()
    {
        return displayName;
    }

    @Override
    public ShotResult computeShot(final ShotContext shotContext)
    {
        final double powerMultiplier = shotContext.getPowerPercentage() / 100.0;
        final double terrainMultiplier = shotContext.getTerrainDistanceMultiplier();

        final double rawDistancePixels =
            baseDistancePixels * powerMultiplier * terrainMultiplier;

        final double adjustedDistancePixels = adjustDistanceForClub(rawDistancePixels, shotContext);

        return new ShotResult(adjustedDistancePixels);
    }

    /**
     * Template method allowing subclasses to fine-tune the raw distance for this club.
     *
     * @param rawDistancePixels raw distance in pixels
     * @param shotContext       context describing this shot
     * @return adjusted distance
     */
    protected abstract double adjustDistanceForClub(final double rawDistancePixels,
                                                    final ShotContext shotContext);
}
