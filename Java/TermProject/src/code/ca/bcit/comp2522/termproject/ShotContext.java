package ca.bcit.comp2522.termproject;

/**
 * Holds the inputs required to compute a golf shot.
 *
 * @author Taylor
 * @version 1.0
 */
public final class ShotContext
{
    private final double powerPercentage;
    private final double terrainDistanceMultiplier;

    /**
     * Constructs a ShotContext.
     *
     * @param powerPercentage           the player-selected power (0-100)
     * @param terrainDistanceMultiplier distance multiplier based on terrain
     */
    public ShotContext(final double powerPercentage,
                       final double terrainDistanceMultiplier)
    {
        this.powerPercentage           = powerPercentage;
        this.terrainDistanceMultiplier = terrainDistanceMultiplier;
    }

    public double getPowerPercentage()
    {
        return powerPercentage;
    }

    public double getTerrainDistanceMultiplier()
    {
        return terrainDistanceMultiplier;
    }
}
