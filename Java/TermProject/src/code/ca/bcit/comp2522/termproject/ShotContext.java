package ca.bcit.comp2522.termproject;

/**
 * Represents all contextual information needed to compute a golf shot.
 * A {@code ShotContext} includes the player-selected power percentage
 * and a terrain-based distance multiplier used to scale a club's output.
 *
 * <p>This class is immutable: once created, its values cannot change.</p>
 * <p>
 * author Taylor
 * version 1.0
 */
public final class ShotContext
{
    private final double powerPercentage;

    private final double terrainDistanceMultiplier;


    /**
     * Constructs a {@code ShotContext} with the specified power and terrain multiplier.
     *
     * @param powerPercentageParameter           the power percentage selected by the player
     * @param terrainDistanceMultiplierParameter the multiplier that adjusts distance based on terrain
     */
    public ShotContext(final double powerPercentageParameter,
                       final double terrainDistanceMultiplierParameter)
    {
        powerPercentage           = powerPercentageParameter;
        terrainDistanceMultiplier = terrainDistanceMultiplierParameter;
    }


    /**
     * Gets the percentage of power selected by the player.
     *
     * @return the power percentage
     */
    public double getPowerPercentage()
    {
        return powerPercentage;
    }


    /**
     * Gets the terrain-based multiplier used to modify distance calculations.
     *
     * @return terrain distance multiplier
     */
    public double getTerrainDistanceMultiplier()
    {
        return terrainDistanceMultiplier;
    }
}
