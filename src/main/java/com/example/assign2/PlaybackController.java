package com.example.assign2; // Defines package location for this class

import javafx.scene.media.Media; // Imports Media class representing media source files
import javafx.scene.media.MediaPlayer; // Imports MediaPlayer class to handle audio/video playback logic
import javafx.scene.media.MediaView; // Imports MediaView node class to display video frames
import java.io.File; // Imports File class for file path handling
import java.util.List; // Imports List interface for playlist collections

public class PlaybackController { // Defines controller class managing media playback state and views

    private MediaPlayer mediaPlayer; // Reference to active JavaFX MediaPlayer instance
    private final MediaView mediaView = new MediaView(); // View component for rendering video frames
    private final SpinningDiskView diskView = new SpinningDiskView(); // Custom view component for spinning record visual
    private int currentIndex = -1; // Tracks index of currently loaded media file (-1 means none loaded)
    private boolean isMuted = false; // Tracks global mute state across track changes

    public MediaView getMediaView() { return mediaView; } // Getter for video display view component
    public SpinningDiskView getDiskView() { return diskView; } // Getter for spinning record display view component
    public int getCurrentIndex() { return currentIndex; } // Getter for index of currently selected track
    public void setCurrentIndex(int index) { this.currentIndex = index; } // Setter to explicitly update current index value

    public void selectMedia(int index, List<File> playlist, Runnable onFinishedCallback) { // Loads and plays track at specified index
        if (index < 0 || index >= playlist.size()) return; // Guards against out-of-bounds playlist index requests

        stopMedia(); // Stops any currently active media playback before loading new file
        currentIndex = index; // Updates current track index tracker

        File currentFile = playlist.get(currentIndex); // Retrieves file object at current index from playlist
        String fileName = currentFile.getName().toLowerCase();
        boolean isVideo = fileName.endsWith(".mp4") || fileName.endsWith(".m4v") || fileName.endsWith(".mov") || fileName.endsWith(".m3u8"); // Obtains lowercase filename to check extension type

        // VISIBILITY LOGIC: Show video OR disk depending on the file type
        if (isVideo) { // Checks if file extension indicates video format
            // It's a video: Show MediaView, Hide Disk
            mediaView.setVisible(true); // Makes video display node visible
            mediaView.setManaged(true);
            diskView.setVisible(false); // Hides spinning disk visual node
            diskView.setManaged(false);
        } else { // Handles audio formats (.mp3, .wav, .m4a)
            // It's audio (mp3, wav, etc): Hide MediaView, Show Disk
            mediaView.setVisible(false); // Hides video display node
            mediaView.setManaged(false);
            diskView.setVisible(true); // Makes spinning disk visual node visible
            diskView.setManaged(true);
        } // Closes if-else check for file type

        Media media = new Media(currentFile.toURI().toString()); // Creates JavaFX Media object from file URI string
        mediaPlayer = new MediaPlayer(media); // Instantiates new MediaPlayer for the created media source
        mediaPlayer.setMute(isMuted); // Restores user's mute preference onto newly created media player
        mediaView.setMediaPlayer(mediaPlayer); // Attaches media player instance to video view node
        mediaPlayer.setOnError(() -> System.err.println("Media playback error: " + mediaPlayer.getError()));
        mediaPlayer.setOnReady(() -> mediaPlayer.play());

        mediaPlayer.setOnEndOfMedia(() -> { // Sets event handler for when track reaches end of playback
            diskView.stopSpin(); // Halts spinning disk animation on completion
            onFinishedCallback.run(); // Triggers callback to automatically process next item
        }); // Closes end-of-media handler callback

        mediaPlayer.play(); // Starts media playback immediately

        // Only spin if the disk is actually visible (it's a song)
        if (diskView.isVisible()) { // Checks if visual disk component is visible on screen
            diskView.playSpin(); // Starts rotating disk animation for audio playback
        } // Closes disk check if block
    } // Closes selectMedia method

    public void playMedia(List<File> playlist, Runnable onFinishedCallback) { // Resumes playback or starts initial item
        if (mediaPlayer != null) { // Checks if existing media player instance is loaded
            mediaPlayer.play(); // Resumes playback of paused media
            if (diskView.isVisible()) { // Checks if spinning disk visual is currently visible
                diskView.playSpin(); // Spin disk on resume if it's audio
            } // Closes disk check if block
        } else if (!playlist.isEmpty()) { // Handles case when no player exists but playlist has files
            selectMedia(0, playlist, onFinishedCallback); // Starts playing first track in playlist from beginning
        } // Closes media player check if block
    } // Closes playMedia method

    public void pauseMedia() { // Pauses current playback without resetting position
        if (mediaPlayer != null) { // Ensures media player exists before attempting to pause
            mediaPlayer.pause(); // Pauses media player playback
            diskView.pauseSpin(); // Pause disk rotation animation in current frame state
        } // Closes if check
    } // Closes pauseMedia method

    public void stopMedia() { // Halts current playback and resets track position to start
        if (mediaPlayer != null) { // Ensures media player exists before attempting to stop
            mediaPlayer.stop(); // Stops media player playback entirely
            diskView.stopSpin(); // Stop disk rotation and reset angle back to 0 degrees
        } // Closes if check
    } // Closes stopMedia method

    public void togglePlayPause(List<File> playlist, Runnable onFinishedCallback) { // Toggles play/pause state
        if (mediaPlayer == null) { // Checks if no track has been loaded yet
            if (!playlist.isEmpty()) { // Checks if playlist contains files to play
                selectMedia(0, playlist, onFinishedCallback); // Loads and plays first track in playlist
            } // Closes playlist empty check
            return; // Returns early after starting initial track
        } // Closes media player null check

        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) { // Checks if media is actively playing
            mediaPlayer.pause(); // Pauses active playback
            diskView.pauseSpin(); // Pauses disk rotation animation
        } else { // Runs if media is currently paused or stopped
            mediaPlayer.play(); // Resumes active playback
            if (diskView.isVisible()) { // Checks if spinning disk node is visible
                diskView.playSpin(); // Resumes rotating disk animation
            } // Closes disk visibility check
        } // Closes playback status check
    } // Closes togglePlayPause method

    public void playNext(List<File> playlist, Runnable onFinishedCallback) { // Advances to play next track in list
        if (!playlist.isEmpty()) { // Checks that playlist is non-empty
            int nextIndex = (currentIndex + 1) % playlist.size(); // Calculates next index with wrap-around to start
            selectMedia(nextIndex, playlist, onFinishedCallback); // Loads and plays calculated next track index
        } // Closes playlist check
    } // Closes playNext method

    public void playPrevious(List<File> playlist, Runnable onFinishedCallback) { // Navigates to play previous track in list
        if (!playlist.isEmpty()) { // Checks that playlist is non-empty
            int prevIndex = (currentIndex - 1 + playlist.size()) % playlist.size(); // Calculates previous index with wrap-around to end
            selectMedia(prevIndex, playlist, onFinishedCallback); // Loads and plays calculated previous track index
        } // Closes playlist check
    } // Closes playPrevious method

    public void adjustVolume(double delta) { // Adjusts volume by delta offset value
        if (mediaPlayer != null) { // Ensures media player exists before adjusting volume
            double newVolume = Math.min(1.0, Math.max(0.0, mediaPlayer.getVolume() + delta)); // Clamps volume within 0.0 to 1.0 range
            mediaPlayer.setVolume(newVolume); // Sets updated volume level on media player
        } // Closes if check
    } // Closes adjustVolume method

    public void toggleMute() { // Toggles sound mute state on/off
        isMuted = !isMuted; // Inverts boolean value of mute flag
        if (mediaPlayer != null) { // Checks if active media player exists
            mediaPlayer.setMute(isMuted); // Applies updated mute state to media player instance
        } // Closes if check
    } // Closes toggleMute method
} // Closes PlaybackController class