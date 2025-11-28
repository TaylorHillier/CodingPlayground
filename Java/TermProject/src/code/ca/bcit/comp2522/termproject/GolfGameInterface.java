package ca.bcit.comp2522.termproject;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Simple left-to-right golf game using JavaFX.
 * Demonstrates: inheritance, interfaces, abstract classes, collections,
 * generics, lambdas, method references, random, file I/O, streams, GUI, etc.
 */
public final class GolfGameInterface
{
    private static final double CANVAS_WIDTH  = 800.0;
    private static final double CANVAS_HEIGHT = 400.0;

    private static final double BALL_RADIUS = 6.0;

    private static final double TILE_WIDTH = 40.0;

    private static final double BASE_GROUND_Y = CANVAS_HEIGHT * 0.75;

    private static final double SAND_DISTANCE_MULTIPLIER    = 0.40; // 60% less far
    private static final double ROUGH_DISTANCE_MULTIPLIER   = 0.70;
    private static final double FAIRWAY_DISTANCE_MULTIPLIER = 1.00;

    private static final double GRAVITY_ACCELERATION_PIXELS_PER_SECOND = 420.0;

    // Used to make the arc rise a bit smoother and drop a bit sharper.
    private static final double GRAVITY_MULTIPLIER_ON_ASCENT  = 0.8;
    private static final double GRAVITY_MULTIPLIER_ON_DESCENT = 1.2;

    private static final double LANDING_BOUNCE_THRESHOLD_VELOCITY = 80.0;
    private static final double ROLL_FRICTION_FACTOR_FAIRWAY      = 0.96;
    private static final double ROLL_FRICTION_FACTOR_ROUGH        = 0.90;
    private static final double ROLL_FRICTION_FACTOR_SAND         = 0.80;
    private static final double ROLL_FRICTION_FACTOR_DEFAULT      = 0.92;


    private static final Path HIGH_SCORE_FILE_PATH =
        Path.of(System.getProperty("user.home"), "golf_best_score.txt");

    private final CountDownLatch gameFinishedLatch;

    private Stage           gameStage;
    private Canvas          gameCanvas;
    private GraphicsContext graphicsContext;

    private final List<TerrainTile> terrainTiles;
    private final Random            randomNumberGenerator;

    private final Map<ClubType, GolfClub> golfClubsByType;

    private BallState ballState;
    private double    cameraOffsetX;

    private int     strokesTakenCount;
    private int     parForHole;
    private Integer bestScoreFromFile;

    private AnimationTimer animationTimer;

    // UI controls
    private ComboBox<ClubType> clubSelectionComboBox;
    private Slider             shotPowerSlider;
    private Slider             launchAngleSlider;
    private Label              statusLabel;
    private Label              parAndScoreLabel;


    /**
     * Constructor. Initializes the golf game.
     *
     * @param gameFinishedLatch latch to signal when the window is closed
     */
    public GolfGameInterface(final CountDownLatch gameFinishedLatch)
    {
        this.gameFinishedLatch     = gameFinishedLatch;
        this.randomNumberGenerator = new Random();
        this.terrainTiles          = new ArrayList<>();
        this.golfClubsByType       = new EnumMap<>(ClubType.class);

        initializeClubs();
        generateSingleHoleCourse();
        initializeBallState();
        loadBestScoreFromFile();
    }

    public void openInNewStage()
    {
        gameStage = new Stage();
        gameStage.setTitle("Simple Golf Game");

        gameCanvas      = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        graphicsContext = gameCanvas.getGraphicsContext2D();

        final BorderPane rootPane = new BorderPane();
        rootPane.setCenter(gameCanvas);
        rootPane.setBottom(createControlPanel());

        final Scene scene = new Scene(rootPane);
        gameStage.setScene(scene);

        setupAnimationLoop();

        gameStage.setOnCloseRequest(windowEvent ->
                                    {
                                        if (animationTimer != null)
                                        {
                                            animationTimer.stop();
                                        }
                                        gameFinishedLatch.countDown();
                                    });

        gameStage.show();
    }

    // -------------------- Setup helpers --------------------

    private void initializeClubs()
    {
        // Strategy pattern: each club has its own distance profile.
        golfClubsByType.put(ClubType.DRIVER,
                            new DriverGolfClub("Driver", 260.0));
        golfClubsByType.put(ClubType.WEDGE,
                            new WedgeGolfClub("Wedge", 140.0));
        golfClubsByType.put(ClubType.PUTTER,
                            new PutterGolfClub("Putter", 60.0));
    }

    private void generateSingleHoleCourse()
    {
        terrainTiles.clear();

        final int numberOfTiles = 30;
        double currentX = 0.0;
        double currentHeightOffset = 0.0;

        // Basic random terrain: fairway with some sand/water sprinkled in.
        for (int tileIndex = 0; tileIndex < numberOfTiles; tileIndex++)
        {
            final TerrainType terrainType;

            if (tileIndex == numberOfTiles - 1)
            {
                terrainType = TerrainType.HOLE;
            }
            else
            {
                final int randomValue = randomNumberGenerator.nextInt(100);

                if (randomValue < 10)
                {
                    terrainType = TerrainType.WATER;
                }
                else if (randomValue < 25)
                {
                    terrainType = TerrainType.SAND;
                }
                else if (randomValue < 35)
                {
                    terrainType = TerrainType.ROUGH;
                }
                else
                {
                    terrainType = TerrainType.FAIRWAY;
                }
            }

            // Gentle height changes.
            final double randomHeightChange =
                (randomNumberGenerator.nextDouble() - 0.5) * 10.0;
            currentHeightOffset += randomHeightChange;
            currentHeightOffset = clamp(currentHeightOffset, -40.0, 40.0);

            final double groundY = BASE_GROUND_Y + currentHeightOffset;

            final TerrainTile terrainTile =
                new TerrainTile(currentX, currentX + TILE_WIDTH, groundY, terrainType);

            terrainTiles.add(terrainTile);
            currentX += TILE_WIDTH;
        }

        // Simple par estimator: 1 stroke per ~ 200 pixels of length.
        final double holeLength = terrainTiles.get(terrainTiles.size() - 1).endX;
        parForHole = (int) Math.max(3, Math.round(holeLength / 200.0));
    }

    private void initializeBallState()
    {
        final TerrainTile startTile = terrainTiles.getFirst();

        final double ballStartX = startTile.startX + TILE_WIDTH * 0.25;
        final double ballStartY = startTile.groundY - BALL_RADIUS;

        ballState         = new BallState(ballStartX, ballStartY);
        cameraOffsetX     = 0.0;
        strokesTakenCount = 0;
    }


    private HBox createControlPanel()
    {
        final HBox controlPanel = new HBox(10.0);
        controlPanel.setPadding(new Insets(8.0));

        clubSelectionComboBox = new ComboBox<>();
        clubSelectionComboBox.getItems().addAll(ClubType.values());
        clubSelectionComboBox.getSelectionModel().select(ClubType.DRIVER);

        shotPowerSlider = new Slider(10.0, 100.0, 60.0);
        shotPowerSlider.setShowTickMarks(true);
        shotPowerSlider.setShowTickLabels(true);

        // New: launch angle slider (degrees)
        launchAngleSlider = new Slider(15.0, 70.0, 45.0);
        launchAngleSlider.setShowTickMarks(true);
        launchAngleSlider.setShowTickLabels(true);

        final Button hitBallButton = new Button("Hit");
        hitBallButton.setOnAction(actionEvent -> performShot());

        statusLabel      = new Label("Pick a club, angle, power, then hit.");
        parAndScoreLabel = new Label(buildParAndScoreText());

        controlPanel.getChildren().addAll(
            new Label("Club:"), clubSelectionComboBox,
            new Label("Angle:"), launchAngleSlider,
            new Label("Power:"), shotPowerSlider,
            hitBallButton,
            parAndScoreLabel,
            statusLabel
                                         );

        return controlPanel;
    }


    private void setupAnimationLoop()
    {
        animationTimer = new AnimationTimer()
        {
            private long lastUpdateNanoseconds = 0L;

            @Override
            public void handle(final long currentTimeNanoseconds)
            {
                if (lastUpdateNanoseconds == 0L)
                {
                    lastUpdateNanoseconds = currentTimeNanoseconds;
                    return;
                }

                final double deltaTimeSeconds =
                    (currentTimeNanoseconds - lastUpdateNanoseconds) / 1_000_000_000.0;
                lastUpdateNanoseconds = currentTimeNanoseconds;

                updateGameState(deltaTimeSeconds);
                renderGame();
            }
        };

        animationTimer.start();
    }

    // -------------------- Game logic --------------------

    private void performShot()
    {
        if (ballState.isMoving)
        {
            return;
        }

        final ClubType selectedClubType =
            clubSelectionComboBox.getSelectionModel().getSelectedItem();

        if (selectedClubType == null)
        {
            statusLabel.setText("No club selected.");
            return;
        }

        final GolfClub selectedGolfClub = golfClubsByType.get(selectedClubType);
        final double powerPercentage = shotPowerSlider.getValue();

        final TerrainTile currentTile = findTileAtX(ballState.positionX);
        final double terrainDistanceMultiplier = switch (currentTile.terrainType)
        {
            case SAND -> SAND_DISTANCE_MULTIPLIER;
            case ROUGH -> ROUGH_DISTANCE_MULTIPLIER;
            case WATER -> 0.0;
            default -> FAIRWAY_DISTANCE_MULTIPLIER;
        };

        if (terrainDistanceMultiplier == 0.0)
        {
            // Ball is in water: reset to start of hole.
            initializeBallState();
            statusLabel.setText("Splash! Ball reset to the tee.");
            return;
        }

        final ShotContext shotContext =
            new ShotContext(powerPercentage, terrainDistanceMultiplier);

        final ShotResult shotResult = selectedGolfClub.computeShot(shotContext);

        // We now interpret the shotResult.totalHorizontalDistance as the
        // ideal range on flat ground, and compute a launch velocity from it.

        final double launchAngleDegrees = launchAngleSlider.getValue();
        final double launchAngleRadians = Math.toRadians(launchAngleDegrees);

        final double sinDoubleAngle = Math.sin(2.0 * launchAngleRadians);
        if (sinDoubleAngle <= 0.0)
        {
            statusLabel.setText("Invalid angle for a forward shot.");
            return;
        }

        final double idealRangePixels = shotResult.totalHorizontalDistance;

        // Classic projectile formula: range = v^2 * sin(2θ) / g  -> v = sqrt(range * g / sin(2θ))
        final double approximateGravityForRange =
            GRAVITY_ACCELERATION_PIXELS_PER_SECOND;
        final double initialSpeedPixelsPerSecond =
            Math.sqrt(idealRangePixels * approximateGravityForRange / sinDoubleAngle);

        final double initialVelocityX =
            initialSpeedPixelsPerSecond * Math.cos(launchAngleRadians);
        final double initialVelocityY =
            -initialSpeedPixelsPerSecond * Math.sin(launchAngleRadians); // negative = up on screen

        ballState.velocityX         = initialVelocityX;
        ballState.velocityY         = initialVelocityY;
        ballState.isMoving          = true;
        ballState.distanceRemaining = idealRangePixels; // optional diagnostic

        strokesTakenCount++;

        statusLabel.setText(
            "Shot with " + selectedGolfClub.getDisplayName()
            + " at " + Math.round(launchAngleDegrees) + "°"
            + " power " + Math.round(powerPercentage) + "%"
                           );
        parAndScoreLabel.setText(buildParAndScoreText());
    }


    private void updateGameState(final double deltaTimeSeconds)
    {
        if (ballState.isMoving)
        {
            // Decide whether we are rising or falling to tweak gravity visually.
            final double gravityMultiplier =
                (ballState.velocityY < 0.0)
                    ? GRAVITY_MULTIPLIER_ON_ASCENT
                    : GRAVITY_MULTIPLIER_ON_DESCENT;

            final double effectiveGravity =
                GRAVITY_ACCELERATION_PIXELS_PER_SECOND * gravityMultiplier;

            // Apply vertical acceleration.
            ballState.velocityY += effectiveGravity * deltaTimeSeconds;

            // Move by velocities.
            final double deltaX = ballState.velocityX * deltaTimeSeconds;
            final double deltaY = ballState.velocityY * deltaTimeSeconds;

            ballState.positionX += deltaX;
            ballState.positionY += deltaY;

            final TerrainTile currentTile = findTileAtX(ballState.positionX);
            final double groundY = currentTile.groundY - BALL_RADIUS;

            // Collision with ground (terrain surface)
            if (ballState.positionY >= groundY)
            {
                ballState.positionY = groundY;

                // If we are still coming down fast, do a small bounce.
                if (Math.abs(ballState.velocityY) > LANDING_BOUNCE_THRESHOLD_VELOCITY)
                {
                    ballState.velocityY = -ballState.velocityY * 0.35;
                }
                else
                {
                    // No more bounce; transition to rolling.
                    ballState.velocityY = 0.0;

                    final double rollFrictionFactor =
                        switch (currentTile.terrainType)
                        {
                            case SAND -> ROLL_FRICTION_FACTOR_SAND;
                            case ROUGH -> ROLL_FRICTION_FACTOR_ROUGH;
                            case FAIRWAY, HOLE -> ROLL_FRICTION_FACTOR_FAIRWAY;
                            default -> ROLL_FRICTION_FACTOR_DEFAULT;
                        };

                    ballState.velocityX *= rollFrictionFactor;

                    if (Math.abs(ballState.velocityX) < 10.0)
                    {
                        ballState.velocityX = 0.0;
                        ballState.isMoving  = false;
                        handleBallStop(currentTile);
                    }
                }
            }
        }

        updateCamera();
    }


    private void handleBallStop(final TerrainTile currentTile)
    {
        if (currentTile.terrainType == TerrainType.WATER)
        {
            initializeBallState();
            statusLabel.setText("Ball rolled into water. Reset to tee.");
        }
        else if (currentTile.terrainType == TerrainType.HOLE)
        {
            statusLabel.setText("Ball in the hole! Strokes: " + strokesTakenCount);
            updateBestScoreIfNeeded();
        }
        else if (currentTile.terrainType == TerrainType.SAND)
        {
            statusLabel.setText("Stopped in sand. Next shot is heavily reduced.");
        }
        else
        {
            statusLabel.setText("Ball stopped on " + currentTile.terrainType.name().toLowerCase() + ".");
        }
    }

    private void updateCamera()
    {
        final double centerThreshold = CANVAS_WIDTH * 0.4;
        final double relativeBallX = ballState.positionX - cameraOffsetX;

        if (relativeBallX > centerThreshold)
        {
            cameraOffsetX = ballState.positionX - centerThreshold;
        }

        cameraOffsetX = Math.max(0.0, cameraOffsetX);
    }

    // -------------------- Rendering --------------------

    private void renderGame()
    {
        graphicsContext.setFill(Color.SKYBLUE);
        graphicsContext.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Draw tiles using method reference (week 7).
        terrainTiles.forEach(this::drawTerrainTile);

        drawBall();

        if (!ballState.isMoving)
        {
            drawAimArrow();
        }

    }

    private void drawAimArrow()
    {
        final double baseArrowX = ballState.positionX - cameraOffsetX;
        final double baseArrowY = ballState.positionY;

        final double launchAngleDegrees = launchAngleSlider.getValue();
        final double launchAngleRadians = Math.toRadians(launchAngleDegrees);

        // Base length scaled by power for visual feedback
        final double normalizedPower = shotPowerSlider.getValue() / 100.0;
        final double arrowLengthPixels = 80.0 + 80.0 * normalizedPower;

        final double arrowEndX =
            baseArrowX + arrowLengthPixels * Math.cos(launchAngleRadians);
        final double arrowEndY =
            baseArrowY - arrowLengthPixels * Math.sin(launchAngleRadians);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(2.0);
        graphicsContext.strokeLine(baseArrowX, baseArrowY, arrowEndX, arrowEndY);

        // Arrow head
        final double arrowHeadLengthPixels = 12.0;
        final double arrowHeadAngleOffsetRadians = Math.toRadians(160.0);

        final double leftHeadAngleRadians = launchAngleRadians + arrowHeadAngleOffsetRadians;
        final double rightHeadAngleRadians = launchAngleRadians - arrowHeadAngleOffsetRadians;

        final double leftHeadX =
            arrowEndX + arrowHeadLengthPixels * Math.cos(leftHeadAngleRadians);
        final double leftHeadY =
            arrowEndY - arrowHeadLengthPixels * Math.sin(leftHeadAngleRadians);

        final double rightHeadX =
            arrowEndX + arrowHeadLengthPixels * Math.cos(rightHeadAngleRadians);
        final double rightHeadY =
            arrowEndY - arrowHeadLengthPixels * Math.sin(rightHeadAngleRadians);

        graphicsContext.strokeLine(arrowEndX, arrowEndY, leftHeadX, leftHeadY);
        graphicsContext.strokeLine(arrowEndX, arrowEndY, rightHeadX, rightHeadY);
    }


    private void drawTerrainTile(final TerrainTile terrainTile)
    {
        final double screenStartX = terrainTile.startX - cameraOffsetX;
        final double screenEndX = terrainTile.endX - cameraOffsetX;
        final double tileWidthOnScreen = screenEndX - screenStartX;

        if (screenEndX < 0 || screenStartX > CANVAS_WIDTH)
        {
            return;
        }

        switch (terrainTile.terrainType)
        {
            case FAIRWAY -> graphicsContext.setFill(Color.GREEN);
            case ROUGH -> graphicsContext.setFill(Color.DARKGREEN);
            case SAND -> graphicsContext.setFill(Color.KHAKI);
            case WATER -> graphicsContext.setFill(Color.DEEPSKYBLUE);
            case HOLE -> graphicsContext.setFill(Color.DARKGREEN);
            default -> graphicsContext.setFill(Color.GRAY);
        }

        graphicsContext.fillRect(
            screenStartX,
            terrainTile.groundY,
            tileWidthOnScreen,
            CANVAS_HEIGHT - terrainTile.groundY
                                );

        if (terrainTile.terrainType == TerrainType.HOLE)
        {
            final double flagPoleX = screenStartX + tileWidthOnScreen * 0.5;
            final double flagTopY = terrainTile.groundY - 40.0;

            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeLine(flagPoleX, terrainTile.groundY, flagPoleX, flagTopY);

            graphicsContext.setFill(Color.RED);
            graphicsContext.fillPolygon(
                new double[]{flagPoleX, flagPoleX + 18.0, flagPoleX},
                new double[]{flagTopY, flagTopY + 8.0, flagTopY + 16.0},
                3
                                       );
        }
    }

    private void drawBall()
    {
        final double screenBallX = ballState.positionX - cameraOffsetX;

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillOval(
            screenBallX - BALL_RADIUS,
            ballState.positionY - BALL_RADIUS,
            BALL_RADIUS * 2.0,
            BALL_RADIUS * 2.0
                                );
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeOval(
            screenBallX - BALL_RADIUS,
            ballState.positionY - BALL_RADIUS,
            BALL_RADIUS * 2.0,
            BALL_RADIUS * 2.0
                                  );
    }

    // -------------------- Helpers --------------------

    private TerrainTile findTileAtX(final double worldX)
    {
        // Simple linear scan over a List (collections).
        for (final TerrainTile terrainTile : terrainTiles)
        {
            if (worldX >= terrainTile.startX && worldX < terrainTile.endX)
            {
                return terrainTile;
            }
        }

        // Fallback to last tile.
        return terrainTiles.getLast();
    }

    private double clamp(final double value,
                         final double minimum,
                         final double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private String buildParAndScoreText()
    {
        final String bestScoreText =
            (bestScoreFromFile == null)
                ? "No best score yet"
                : "Best: " + bestScoreFromFile;

        // Streaming & filtering example: count sand tiles.
        final long sandTileCount = terrainTiles.stream()
                                               .filter(tile -> tile.terrainType == TerrainType.SAND)
                                               .count();

        final String sandSummary =
            "Sand tiles: " + sandTileCount;

        return "Par: " + parForHole + " | Strokes: " + strokesTakenCount
               + " | " + bestScoreText + " | " + sandSummary;
    }

    private void loadBestScoreFromFile()
    {
        try
        {
            if (Files.exists(HIGH_SCORE_FILE_PATH))
            {
                final List<String> fileLines =
                    Files.readAllLines(HIGH_SCORE_FILE_PATH);

                final List<Integer> parsedScores =
                    fileLines.stream()
                             .map(String::trim)
                             .filter(line -> !line.isEmpty())
                             .map(Integer::parseInt)
                             .collect(Collectors.toList());

                if (!parsedScores.isEmpty())
                {
                    bestScoreFromFile = parsedScores.getFirst();
                }
            }
        }
        catch (final IOException | NumberFormatException exception)
        {
            bestScoreFromFile = null;
        }
    }

    private void updateBestScoreIfNeeded()
    {
        if (bestScoreFromFile == null || strokesTakenCount < bestScoreFromFile)
        {
            bestScoreFromFile = strokesTakenCount;

            try
            {
                final String scoreAsString = Integer.toString(strokesTakenCount);
                Files.writeString(HIGH_SCORE_FILE_PATH, scoreAsString);
            }
            catch (final IOException ioException)
            {
                statusLabel.setText("Hole complete, but failed to save best score.");
            }
        }

        parAndScoreLabel.setText(buildParAndScoreText());
    }

    // -------------------- Nested classes / enums --------------------

    private enum TerrainType
    {
        FAIRWAY,
        ROUGH,
        SAND,
        WATER,
        HOLE
    }

    private enum ClubType
    {
        DRIVER,
        WEDGE,
        PUTTER
    }

    /**
     * Simple ball state container.
     * Nested class (week 7).
     */
    /**
     * Simple ball state container.
     * Nested class (week 7).
     */
    private static final class BallState
    {
        double positionX;
        double positionY;

        double velocityX;
        double velocityY;

        // distanceRemaining is now optional; we no longer rely on it
        // but keep it so you do not have to change other fields everywhere.
        double distanceRemaining;

        boolean isMoving;

        BallState(final double positionX,
                  final double positionY)
        {
            this.positionX         = positionX;
            this.positionY         = positionY;
            this.velocityX         = 0.0;
            this.velocityY         = 0.0;
            this.distanceRemaining = 0.0;
            this.isMoving          = false;
        }
    }


    /**
     * Terrain tile: startX -> endX at a certain ground height.
     */
    private static final class TerrainTile
    {
        final double      startX;
        final double      endX;
        final double      groundY;
        final TerrainType terrainType;

        TerrainTile(final double startX,
                    final double endX,
                    final double groundY,
                    final TerrainType terrainType)
        {
            this.startX      = startX;
            this.endX        = endX;
            this.groundY     = groundY;
            this.terrainType = terrainType;
        }
    }

    /**
     * Context object passed into a club when computing a shot.
     */
    private static final class ShotContext
    {
        final double powerPercentage;
        final double terrainDistanceMultiplier;

        ShotContext(final double powerPercentage,
                    final double terrainDistanceMultiplier)
        {
            this.powerPercentage           = powerPercentage;
            this.terrainDistanceMultiplier = terrainDistanceMultiplier;
        }
    }

    /**
     * Result of a shot.
     */
    private static final class ShotResult
    {
        final double totalHorizontalDistance;

        ShotResult(final double totalHorizontalDistance)
        {
            this.totalHorizontalDistance = totalHorizontalDistance;
        }
    }

    /**
     * Interface (week 4) + Strategy pattern for clubs.
     */
    private interface GolfClub
    {
        String getDisplayName();

        ShotResult computeShot(ShotContext shotContext);
    }

    /**
     * Abstract base class for clubs (week 3).
     */
    private static abstract class AbstractGolfClub implements GolfClub
    {
        private final   String displayName;
        protected final double baseDistance;

        protected AbstractGolfClub(final String displayName,
                                   final double baseDistance)
        {
            this.displayName  = displayName;
            this.baseDistance = baseDistance;
        }

        @Override
        public String getDisplayName()
        {
            return displayName;
        }

        @Override
        public ShotResult computeShot(final ShotContext shotContext)
        {
            final double powerMultiplier = shotContext.powerPercentage / 100.0;
            final double terrainMultiplier = shotContext.terrainDistanceMultiplier;

            final double rawDistance = baseDistance * powerMultiplier * terrainMultiplier;
            final double adjustedDistance = adjustDistanceForClub(rawDistance, shotContext);

            return new ShotResult(adjustedDistance);
        }

        /**
         * Template method that concrete clubs override.
         */
        protected abstract double adjustDistanceForClub(double rawDistance,
                                                        ShotContext shotContext);
    }

    private static final class DriverGolfClub extends AbstractGolfClub
    {
        DriverGolfClub(final String displayName,
                       final double baseDistance)
        {
            super(displayName, baseDistance);
        }

        @Override
        protected double adjustDistanceForClub(final double rawDistance,
                                               final ShotContext shotContext)
        {
            // Driver: best on fairway, slightly penalized in rough/sand (handled by terrain).
            return rawDistance;
        }
    }

    private static final class WedgeGolfClub extends AbstractGolfClub
    {
        WedgeGolfClub(final String displayName,
                      final double baseDistance)
        {
            super(displayName, baseDistance);
        }

        @Override
        protected double adjustDistanceForClub(final double rawDistance,
                                               final ShotContext shotContext)
        {
            // Wedge: slightly better from bad lies, give a small boost.
            return rawDistance * 1.1;
        }
    }

    private static final class PutterGolfClub extends AbstractGolfClub
    {
        PutterGolfClub(final String displayName,
                       final double baseDistance)
        {
            super(displayName, baseDistance);
        }

        @Override
        protected double adjustDistanceForClub(final double rawDistance,
                                               final ShotContext shotContext)
        {
            // Putter: very consistent and short; cap distance.
            return Math.min(rawDistance, 80.0);
        }
    }
}
