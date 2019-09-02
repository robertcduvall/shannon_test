package example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;


/**
 * A basic example JavaFX program for the first lab.
 * 
 * @author Robert C. Duvall
 */
public class ExampleBounce extends Application {
    public static final String TITLE = "Example JavaFX";
    public static final int SIZE = 400;
    public static final int FRAMES_PER_SECOND = 60;
    public static final int MILLISECOND_DELAY = 1000 / FRAMES_PER_SECOND;
    public static final double SECOND_DELAY = 1.0 / FRAMES_PER_SECOND;
    public static final Paint BACKGROUND = Color.AZURE;
    public static final Paint HIGHLIGHT = Color.OLIVEDRAB;
    public static final String BOUNCER_IMAGE = "ball.gif";
    public static final int BOUNCER_MIN_SPEED = -60;
    public static final int BOUNCER_MAX_SPEED = 60;
    public static final int BOUNCER_MIN_SIZE = 20;
    public static final int BOUNCER_MAX_SIZE = 40;
    public static final Paint MOVER_COLOR = Color.PLUM;
    public static final int MOVER_SIZE = 50;
    public static final int MOVER_SPEED = 5;
    public static final Paint GROWER_COLOR = Color.BISQUE;
    public static final double GROWER_RATE = 1.1;
    public static final int GROWER_SIZE = 50;
    

    // some things we need to remember during our game
    private Scene myScene;
    private ImageView myBouncer1;
    private ImageView myBouncer2;
    private Rectangle myMover;
    private Rectangle myGrower;
    private Point2D myVelocity1;
    private Point2D myVelocity2;
    private Random dice = new Random();


    /**
     * Initialize what will be displayed and how it will be updated.
     */
    @Override
    public void start (Stage stage) {
        // attach scene to the stage and display it
        myScene = setupGame(SIZE, SIZE, BACKGROUND);
        stage.setScene(myScene);
        stage.setTitle(TITLE);
        stage.show();
        // attach "game loop" to timeline to play it (basically just calling step() method repeatedly forever)
        KeyFrame frame = new KeyFrame(Duration.millis(MILLISECOND_DELAY), e -> step(SECOND_DELAY));
        Timeline animation = new Timeline();
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.getKeyFrames().add(frame);
        animation.play();
    }

    // Create the game's "scene": what shapes will be in the game and their starting properties
    private Scene setupGame (int width, int height, Paint background) {
        // create one top level collection to organize the things in the scene
        Group root = new Group();
        // make some shapes and set their properties
        Image image = new Image(this.getClass().getClassLoader().getResourceAsStream(BOUNCER_IMAGE));
        myBouncer1 = makeBouncer(image, width, height);
        myVelocity1 = new Point2D(getRandomInRange(BOUNCER_MIN_SPEED, BOUNCER_MAX_SPEED),
                                  getRandomInRange(BOUNCER_MIN_SPEED, BOUNCER_MAX_SPEED));
        myBouncer2 = makeBouncer(image, width, height);
        myVelocity2 = new Point2D(getRandomInRange(BOUNCER_MIN_SPEED, BOUNCER_MAX_SPEED),
                                  getRandomInRange(BOUNCER_MIN_SPEED, BOUNCER_MAX_SPEED));
        myMover = new Rectangle(width / 2 - MOVER_SIZE / 2, height / 2 - 100, MOVER_SIZE, MOVER_SIZE);
        myMover.setFill(MOVER_COLOR);
        myGrower = new Rectangle(width / 2 - GROWER_SIZE / 2, height / 2 + 50, GROWER_SIZE, GROWER_SIZE);
        myGrower.setFill(GROWER_COLOR);
        // order added to the group is the order in which they are drawn
        root.getChildren().add(myBouncer1);
        root.getChildren().add(myBouncer2);
        root.getChildren().add(myMover);
        root.getChildren().add(myGrower);
        // create a place to see the shapes
        Scene scene = new Scene(root, width, height, background);
        // respond to input
        scene.setOnKeyPressed(e -> handleKeyInput(e.getCode()));
        scene.setOnMouseClicked(e -> handleMouseInput(e.getX(), e.getY()));
        return scene;
    }

    // Change properties of shapes in small ways to animate them over time
    // Note, there are more sophisticated ways to animate shapes, but these simple ways work fine to start
    private void step (double elapsedTime) {
        // update "actors" attributes
        moveBouncer(myBouncer1, myVelocity1, elapsedTime);
        moveBouncer(myBouncer2, myVelocity2, elapsedTime);
        myMover.setRotate(myMover.getRotate() - 1);
        myGrower.setRotate(myGrower.getRotate() + 1);

        // check for collisions
        // with shapes, can check precisely
        // NEW Java 10 syntax that simplifies things (but watch out it can make code harder to understand)
        // var intersection = Shape.intersect(myMover, myGrower);
        Shape intersection = Shape.intersect(myMover, myGrower);
        if (intersection.getBoundsInLocal().getWidth() != -1) {
            myMover.setFill(HIGHLIGHT);
        }
        else {
            myMover.setFill(MOVER_COLOR);
        }
        // with images can only check bounding box
        if (myGrower.getBoundsInParent().intersects(myBouncer1.getBoundsInParent()) ||
            myGrower.getBoundsInParent().intersects(myBouncer2.getBoundsInParent())) {
            myGrower.setFill(HIGHLIGHT);
        }
        else {
            myGrower.setFill(GROWER_COLOR);
        }

        // bounce off all the walls
        myVelocity1 = bounceOffWalls(myBouncer1, myVelocity1, myScene);
        myVelocity2 = bounceOffWalls(myBouncer2, myVelocity2, myScene);
    }

    // What to do each time a key is pressed
    private void handleKeyInput (KeyCode code) {
        if (code == KeyCode.RIGHT) {
            myMover.setX(myMover.getX() + MOVER_SPEED);
        }
        else if (code == KeyCode.LEFT) {
            myMover.setX(myMover.getX() - MOVER_SPEED);
        }
        else if (code == KeyCode.UP) {
            myMover.setY(myMover.getY() - MOVER_SPEED);
        }
        else if (code == KeyCode.DOWN) {
            myMover.setY(myMover.getY() + MOVER_SPEED);
        }
        // NEW Java 12 syntax that some prefer (but watch out for the many special cases!)
        //   https://blog.jetbrains.com/idea/2019/02/java-12-and-intellij-idea/
        // Note, must set Project Language Level to "12 Preview - Switch Expressions" under File -> Project Structure
        // switch (code) {
        //     case RIGHT -> myMover.setX(myMover.getX() + MOVER_SPEED);
        //     case LEFT -> myMover.setX(myMover.getX() - MOVER_SPEED);
        //     case UP -> myMover.setY(myMover.getY() - MOVER_SPEED);
        //     case DOWN -> myMover.setY(myMover.getY() + MOVER_SPEED);
        // }
    }

    // What to do each time a key is pressed
    private void handleMouseInput (double x, double y) {
        if (myGrower.contains(x, y)) {
            myGrower.setScaleX(myGrower.getScaleX() * GROWER_RATE);
            myGrower.setScaleY(myGrower.getScaleY() * GROWER_RATE);
        }
    }

    // Returns an "interesting", non-zero random value in the range (min, max) or (-min, -max)
    private int getRandomInRange (int min, int max) {
        return min + dice.nextInt(max - min) + 1;
    }

    // Create a bouncer from a given image
    private ImageView makeBouncer (Image image, int screenWidth, int screenHeight) {
        ImageView result = new ImageView(image);
        // make sure it stays a circle
        int size = getRandomInRange(BOUNCER_MIN_SIZE, BOUNCER_MAX_SIZE);
        result.setFitWidth(size);
        result.setFitHeight(size);
        // make sure it stays within the bounds
        result.setX(getRandomInRange(size,  screenWidth - size));
        result.setY(getRandomInRange(size, screenHeight - size));
        return result;
    }

    // Move a bouncer based on its velocity
    private void moveBouncer (ImageView bouncer, Point2D velocity, double elapsedTime) {
        bouncer.setX(bouncer.getX() + velocity.getX() * elapsedTime);
        bouncer.setY(bouncer.getY() + velocity.getY() * elapsedTime);
    }

    // Return velocity that may be updated if bounce occurred
    private Point2D bounceOffWalls (ImageView bouncer, Point2D velocity, Scene scene) {
        if (bouncer.getX() < 0 || bouncer.getX() > scene.getWidth() - bouncer.getBoundsInLocal().getWidth()) {
            velocity = new Point2D(-velocity.getX(), velocity.getY());
        }
        if (bouncer.getY() < 0 || bouncer.getY() > scene.getHeight() - bouncer.getBoundsInLocal().getHeight()) {
            velocity = new Point2D(velocity.getX(), -velocity.getY());
        }
        return velocity;
    }


    /**
     * Start the program.
     */
    public static void main (String[] args) {
        launch(args);
    }
}
