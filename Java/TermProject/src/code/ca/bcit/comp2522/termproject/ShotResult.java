package ca.bcit.comp2522.termproject;

/**
 * Represents the predicted outcome of a golf shot when simulated on flat terrain.
 * A {@code ShotResult} stores the expected horizontal distance, measured in pixels,
 * based on the club, power level, and terrain multipliers used in the shot calculation.
 *
 * <p>This class is immutable: once created, the contained distance value cannot change.</p>
 * <p>
 * author Taylor
 * version 1.0
 */
public final class ShotResult
{
    /**
     * Expected horizontal range of the shot, expressed in pixels.
     */
    private final double expectedHorizontalRangePixels;

    /**
     * Constructs a {@code ShotResult} that stores the predicted horizontal range.
     *
     * @param expectedHorizontalRangePixelsParameter expected distance of the shot in pixels
     */
    public ShotResult(final double expectedHorizontalRangePixelsParameter)
    {
        this.expectedHorizontalRangePixels = expectedHorizontalRangePixelsParameter;
    }

    /**
     * Gets the expected horizontal distance of the shot in pixels.
     *
     * @return horizontal distance in pixels
     */
    public double getExpectedHorizontalRangePixels()
    {
        return expectedHorizontalRangePixels;
    }
}
