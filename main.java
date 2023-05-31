import java.io.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

class jsound {
    final static String helpMessage = "\nto use jsound please give an argument to a mp3 file you want to play for example\n$java jsound {mp3 file}";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.printf("Please Give Correct Number Of Args %s", helpMessage);
            System.exit(1);
        }
        String sound = args[0];
        System.out.printf("Now Playing %s\n", sound);
        try {
            File file = new File(sound);

            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = bufferedInputStream.read(buffer, 0, bufferSize)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byteArrayOutputStream.flush();
            byte[] audioBytes = byteArrayOutputStream.toByteArray();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioBytes);
            AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream,
                    new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false), bytesRead);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            clip.start();

            // Wait for the sound to finish playing
            Thread.sleep(clip.getMicrosecondLength() / 1000);

            // Clean up resources
            clip.close();
            audioInputStream.close();
            byteArrayInputStream.close();
            byteArrayOutputStream.close();
            bufferedInputStream.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.printf("Failed To Play %s Due To: %s\n", sound, e);
        }
    }
}