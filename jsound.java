import sun.misc.Signal;

import javax.sound.sampled.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

class jsound {
    final static String helpMessage = "\nto use jsound please give an argument to a mp3 file you want to play for example\n$java jsound {mp3 file}";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.printf("Please provide the path to an MP3 file.\n%s", helpMessage);
            System.exit(1);
        }
        String sound = args[0];
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
                    try {
                        rm(sound.replace(".mp3", ".wav"));
                    } catch (Exception ignored) {
                    }
                    System.exit(0);
                });
        System.out.printf("Now Playing %s\nPress Control-C to cancel\n", sound);
        try {
            rm(sound.replace(".mp3", ".wav"));
        } catch (Exception ignored) {
        }
        try {
            String wavPath = convertToWav(sound);
            playWav(wavPath);
            File wavFile = new File(wavPath);
            if (wavFile.exists()) {
                wavFile.delete();
            }
            try {
                rm(sound.replace(".mp3", ".wav"));
            } catch (Exception ignored) {
            }
        } catch (IOException | InterruptedException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.printf("Failed To Play %s Due To: %s\n", sound, e);
        }
    }

    private static String convertToWav(String mp3Path) throws IOException, InterruptedException {
        String wavPath = mp3Path.replace(".mp3", ".wav");

        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", mp3Path, "-acodec", "pcm_s16le", "-ac", "2", "-ar", "44100", wavPath);
        Process process = processBuilder.start();
        process.waitFor();

        return wavPath;
    }

    private static void playWav(String wavPath) throws IOException, InterruptedException, LineUnavailableException, UnsupportedAudioFileException {
        File wavFile = new File(wavPath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat audioFormat = audioInputStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);

        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        int bytesRead;

        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
            sourceDataLine.write(buffer, 0, bytesRead);
        }

        sourceDataLine.drain();
        sourceDataLine.stop();
        sourceDataLine.close();
        audioInputStream.close();
    }

    static void rm(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("rm", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder data = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            data.append(line);
        }
        int exitCode = process.waitFor();
    }
}