package com.example.assign2; // Defines package location for this class

import javafx.animation.Animation; // Imports Animation class for transition control states
import javafx.animation.Interpolator; // Imports Interpolator class for linear animation interpolation
import javafx.animation.RotateTransition; // Imports RotateTransition class to rotate JavaFX nodes smoothly
import javafx.scene.image.Image; // Imports Image class for loading graphic image files
import javafx.scene.layout.StackPane; // Imports StackPane layout manager to host disk view
import javafx.scene.paint.Color; // Imports Color class as fill fallback
import javafx.scene.paint.ImagePattern; // Imports ImagePattern to tile/fill shapes with images
import javafx.scene.shape.Circle; // Imports Circle class to represent circular vinyl disk shape
import javafx.util.Duration; // Imports Duration class to specify animation durations

import java.net.URL; // Imports URL class to load external resource files safely

public class SpinningDiskView extends StackPane { // Custom view node extending StackPane to hold spinning disk visual

    private final Circle diskShape; // Holds Circle shape representing vinyl disk object
    private final RotateTransition rotateTransition; // Holds rotation animation configuration and control instance

    public SpinningDiskView() { // Constructor for SpinningDiskView component
        diskShape = new Circle(200); // Instantiates Circle shape with radius of 200 pixels

        // Loads disk image directly from root resources directory (/disk.png)
        URL imageUrl = getClass().getResource("/disk.png"); // Resolves resource URL for disk image from root folder

        if (imageUrl != null) { // Checks if resource image URL was successfully found
            Image diskImage = new Image(imageUrl.toExternalForm()); // Loads Image object from external form URL string
            diskShape.setFill(new ImagePattern(diskImage)); // Fills circle shape with loaded image pattern texture
        } else { // Handles case when image resource cannot be loaded
            System.err.println("❌ ERROR: disk.png not found in root resources."); // Prints error notification to console
            diskShape.setFill(Color.RED); // Fills circle shape with solid red fallback color indicator
        } // Closes image check if-else block

        getChildren().add(diskShape); // Appends configured circle shape to StackPane node tree

        rotateTransition = new RotateTransition(Duration.seconds(3), diskShape); // Configures rotation animation targeting disk shape over 3 seconds
        rotateTransition.setByAngle(360); // Sets full 360-degree rotation angle for single cycle
        rotateTransition.setCycleCount(Animation.INDEFINITE); // Configures animation loop to run indefinitely
        rotateTransition.setInterpolator(Interpolator.LINEAR); // Sets linear interpolation for constant smooth speed rotation
    } // Closes constructor

    public void playSpin() { // Starts or resumes rotating disk animation
        if (rotateTransition.getStatus() != Animation.Status.RUNNING) { // Checks if rotation animation is not already active
            rotateTransition.play(); // Starts playing rotation animation sequence
        } // Closes status check
    } // Closes playSpin method

    public void pauseSpin() { // Pauses rotating disk animation at current angle
        if (rotateTransition.getStatus() == Animation.Status.RUNNING) { // Checks if rotation animation is actively running
            rotateTransition.pause(); // Pauses rotation animation at present frame
        } // Closes status check
    } // Closes pauseSpin method

    public void stopSpin() { // Stops rotating disk animation and resets angle back to 0
        rotateTransition.stop(); // Halts rotation transition completely
        diskShape.setRotate(0); // Resets rotation angle of circle node back to 0 degrees
    } // Closes stopSpin method

    public Circle getDiskShape() { // Getter method to access internal Circle node
        return diskShape; // Returns reference to Circle node instance
    } // Closes getDiskShape method
} // Closes SpinningDiskView class