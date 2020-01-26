import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;

public class SoundThreshold {
    private float threshold;
    private byte[] buffer;
    private float[] samples;
    private float maxByteValue;
    private TargetDataLine line;

    public static void main(String[] args) {
        System.out.println("Setting up...");
        SoundThreshold soundThreshold = new SoundThreshold(0.85f, 4000, 64);
        System.out.println("Starting!");
        while (true){
            try {
                if (soundThreshold.isAboveThreshold()){
                    System.out.println("Was above the threshold!");
                    break;
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public SoundThreshold(float threshold, float sampleRate , int byteBufferSize){
        this.threshold = threshold;
        this.maxByteValue = 128; // 2^(sampleBitDepth - 1)
        this.buffer = new byte[byteBufferSize];
        this.samples = new float[byteBufferSize];
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);

        try {
            this.line = AudioSystem.getTargetDataLine(format);
            this.line.open();
            this.line.start();
            Thread.sleep(3000);
        } catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isAboveThreshold() throws LineUnavailableException {

        for(int b; (b = this.line.read(this.buffer, 0, buffer.length)) > -1;){
            for (int i = 0; i < b; i++) {
                this.samples[i] = this.buffer[i] / this.maxByteValue;
            }
            float rms = 0;
            for (float sample : this.samples){
                rms += sample * sample;
            }
            rms = (float)Math.sqrt(rms / this.samples.length);
            if (rms > this.threshold){
                return true;
            }
        }

        throw new LineUnavailableException();
    }

}
