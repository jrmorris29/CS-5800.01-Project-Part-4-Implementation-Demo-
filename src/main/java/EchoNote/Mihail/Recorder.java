package EchoNote.Mihail;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Records audio from the system's default microphone.
 * Supports starting/stopping recording and saving to WAV files.
 */
public class Recorder {

    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1; // mono
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private boolean permissionGranted;
    private boolean recording;
    private File currentOutputFile;
    private TargetDataLine targetLine;
    private Thread recordingThread;

    /**
     * Requests and verifies microphone permissions by attempting to open the default microphone.
     *
     * @return true if the microphone is accessible, false otherwise
     * @throws RecordingException if there's an error accessing the microphone
     */
    public boolean requestPermissions() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                permissionGranted = false;
                return false;
            }

            // Try to open and immediately close the line to verify access
            TargetDataLine testLine = (TargetDataLine) AudioSystem.getLine(info);
            testLine.open(format);
            testLine.close();

            permissionGranted = true;
            return true;

        } catch (LineUnavailableException e) {
            permissionGranted = false;
            throw new RecordingException("Failed to access microphone", e);
        }
    }

    /**
     * Begins recording audio to the specified output file.
     *
     * @param outputFile the WAV file to write audio data to
     * @throws IllegalArgumentException if outputFile is null
     * @throws RecordingException if recording is already active or microphone access fails
     */
    public void begin(File outputFile) {
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }

        if (recording) {
            throw new RecordingException("Recording is already in progress");
        }

        if (!permissionGranted) {
            throw new RecordingException("Microphone permissions not granted. Call requestPermissions() first.");
        }

        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(format);
            targetLine.start();

            currentOutputFile = outputFile;
            recording = true;

            // Start recording on a separate thread
            recordingThread = new Thread(() -> {
                try {
                    AudioInputStream audioStream = new AudioInputStream(targetLine);
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, currentOutputFile);
                } catch (IOException e) {
                    recording = false;
                    throw new RecordingException("Failed to write audio to file", e);
                }
            });

            recordingThread.start();

        } catch (LineUnavailableException e) {
            recording = false;
            throw new RecordingException("Failed to start recording", e);
        }
    }

    /**
     * Stops the current recording and returns the recorded file.
     *
     * @return the File containing the recorded audio
     * @throws RecordingException if no recording is active
     */
    public File end() {
        if (!recording) {
            throw new RecordingException("No recording in progress");
        }

        try {
            // Stop and close the target line
            targetLine.stop();
            targetLine.close();

            recording = false;

            // Wait for the recording thread to finish writing
            if (recordingThread != null && recordingThread.isAlive()) {
                recordingThread.join(5000); // Wait up to 5 seconds
            }

            File recordedFile = currentOutputFile;
            currentOutputFile = null;
            recordingThread = null;
            targetLine = null;

            return recordedFile;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RecordingException("Recording thread was interrupted", e);
        }
    }

    /**
     * Returns whether recording is currently active.
     *
     * @return true if recording, false otherwise
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * Creates the standard audio format for recording.
     * Format: 44.1kHz, 16-bit, mono, PCM_SIGNED
     *
     * @return the configured AudioFormat
     */
    private AudioFormat getAudioFormat() {
        return new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_IN_BITS,
                CHANNELS,
                SIGNED,
                BIG_ENDIAN
        );
    }
}
