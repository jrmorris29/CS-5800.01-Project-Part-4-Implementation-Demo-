package EchoNote.Mihail;

import java.io.File;

public class Recorder {

    private boolean permissionGranted;
    private boolean recording;
    private File currentOutputFile;

    public boolean requestPermissions() {
        // TODO: request and track microphone permission from the system
        return false;
    }

    public void begin(File outputFile) {
        // TODO: start capturing audio from the default microphone
        //       and write it to the provided outputFile (.wav)
    }

    public File end() {
        // TODO: stop capturing audio and return the recorded file
        return null;
    }

    public boolean isRecording() {
        // TODO: return whether recording is currently active
        return recording;
    }
}
