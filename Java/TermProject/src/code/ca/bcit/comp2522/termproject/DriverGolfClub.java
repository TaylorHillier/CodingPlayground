package ca.bcit.comp2522.termproject;

/**
 * Represents a driver golf club, used for achieving long-distance shots.
 * A driver applies no additional distance adjustment beyond the base
 * distance calculation performed in {@link AbstractGolfClub}.
 */
public final class DriverGolfClub extends AbstractGolfClub
{
    /**
     * The nominal yardage associated with a driver.
     */
    public static final double DRIVER_YARDAGE = 400.0;

    /**
     * Constructs a {@code DriverGolfClub} with a display name and base distance.
     *
     * @param displayName        the name shown to players (e.g., "Driver")
     * @param baseDistancePixels the base distance for calculations, in pixels
     */
    public DriverGolfClub(final String displayName,
                          final double baseDistancePixels)
    {
        super(displayName, baseDistancePixels);
    }

    /**
     * Applies the driver-specific distance adjustment.
     * The driver does not modify the raw distance; it returns it unchanged.
     *
     * @param rawDistancePixels the computed base distance before club adjustment
     * @param shotContext       the context describing power and terrain multipliers
     * @return the unmodified raw distance
     */
    @Override
    protected double adjustDistanceForClub(final double rawDistancePixels,
                                           final ShotContext shotContext)
    {
        return rawDistancePixels;
    }
}
