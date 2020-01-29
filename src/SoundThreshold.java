import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class SoundThreshold {
    private float threshold;
    private byte[] buffer;
    private float[] samples;
    private float maxByteValue;
    private TargetDataLine line;

    public static void main(String[] args) {
        // demo program using for using this class

        // defines a new soundThreshold object with threshold of 60%, CD sample rate and buffer size of 64
        SoundThreshold soundThreshold = new SoundThreshold(0.6f, 44_100, 64);

        // using the isAboveThreshold is useful if you want to check the sound level but if the threshold
        // is not met the program can do other things and check the sound level again later
        System.out.print("Checking sound level once. sound level was above threshold? ");
        System.out.println(soundThreshold.isAboveThreshold());


        // using the isAboveThresholdBlocking is useful if you want your program to wait in place for the
        // sound threshold to be met
        System.out.print("Continuously checks sound level until its above threshold. Sound level was above threshold? ");
        System.out.println(soundThreshold.isAboveThresholdBlocking());
    }

    public SoundThreshold(float threshold, float sampleRate , int bufferSize){
        /*
        `this.threshold` value between 0 and 1 as float. (0 - 100%) "volume"
        `this.maxByteValue` fixed to 128. this should only change if sampleSizeInBits is changed from 8
                            to something else. 128 comes from 2^8 = 256 (maximum value of a byte)
                            first bit in the byte determines positive or negative as the AudioFormat is set
                            to signed = true. => 2^(8-1) = 128
        `this.buffer` byte array for loading in samples
        `this.samples` float array for normalized samples
        `this.line` reads mic audio
         */
        this.threshold = threshold;
        this.maxByteValue = 128; // 2^(sampleBitDepth - 1)
        this.buffer = new byte[bufferSize];
        this.samples = new float[bufferSize];

        /*
        `sampleRate` measures how often a sample is taken by the program. this should be rather large value
                      CD quality is 44100
         `sampleSizeInBits` is how good precision a sample have against a sound wave. 8 is quite bad quality
                            but but it makes one sample fit in a single byte and sound quality is not of a concern
                            https://www.bbc.co.uk/bitesize/guides/zpfdwmn/revision/3
         `channels` no more than one needed (mono sound)
         `singed` a byte is signed if the first bit determines if the byte is positive or negative. this is wanted as
                    that makes the normalized samples value go between -1 and 1
                    https://en.wikipedia.org/wiki/Signed_number_representations#Signed_magnitude_representation_(SMR)
         `bigEndian` this only matters if sampleSizeInBits is greater than 8 and one sample does not fit in a single byte
                        https://en.wikipedia.org/wiki/Endianness
         */
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);

        try {
            // opens up the data line and makes it available for reading
            this.line = AudioSystem.getTargetDataLine(format);
            this.line.open();
            this.line.start();
        } catch (LineUnavailableException e){
            e.printStackTrace();
        }
    }

    public boolean isAboveThreshold(){
        /*
        This method will return true if the samples RMS value is higher
        than the set threshold otherwise returns false
         */

        // loads the next few samples in to the buffer `this.buffer`
        this.line.read(this.buffer, 0, this.buffer.length);

        // loops over the buffer
        for (int i = 0; i < this.buffer.length; i++) {
            // adds each sample decided by the samples maximum value so all values are between -1 and 1
            this.samples[i] = this.buffer[i] / this.maxByteValue;
        }

        // calculates the root square mean value over the array
        // rms will be a value between 0 and 1 and will be compared to the sound threshold
        // https://dosits.org/science/advanced-topics/introduction-to-signal-levels/
        float rms = 0;
        for (float sample : this.samples){
            rms += sample * sample;
        }
        rms = (float)Math.sqrt(rms / this.samples.length);
        return rms >= this.threshold;
    }

    public boolean isAboveThresholdBlocking() {
        /*
        this method will always return true but wont do so until the sound threshold is met
         */
        while (true){
            if (this.isAboveThreshold()){
                return true;
            }
        }
    }
}
