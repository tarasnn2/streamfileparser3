import org.apache.commons.io.IOUtils;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;

public class ParserSimple1 {

    public static final String ORIGIN = "/home/tnaumenko/Downloads/big.txt";
    private static final String RESULT = "static/result.txt";
    private static final String PATTERN = "static/pattern.txt";
    private static final String PATTERN_NEW = "static/newPattern.txt";
    //private static final String ORIGIN = "static/origin.txt";

    public static void main(String[] args) throws IOException {
        Instant start = Instant.now();
        new ParserSimple1().process();
        Instant end = Instant.now();
        System.out.println("time seconds: " + (end.getEpochSecond() - start.getEpochSecond()));
    }

    public void process() throws IOException {
        byte[] pattern, newPattern;
        try (FileInputStream streamPattern = new FileInputStream(new File(PATTERN));
             FileInputStream streamNewPattern = new FileInputStream(new File(PATTERN_NEW))) {
            pattern = IOUtils.toByteArray(streamPattern);
            newPattern = IOUtils.toByteArray(streamNewPattern);
        }

        try (BufferedReader origin = new BufferedReader(new FileReader(new File(ORIGIN)));
             BufferedWriter result = new BufferedWriter(new FileWriter(new File(RESULT)))) {

            int posCurrent = 0, value;
            byte[] values = new byte[pattern.length];

            while ((value = origin.read()) != -1) {
                values[posCurrent++] = (byte) value;

                if (!Arrays.equals(Arrays.copyOf(pattern, posCurrent), Arrays.copyOf(values, posCurrent))) {
                    result.write(new String(Arrays.copyOf(values, posCurrent)));
                    values = new byte[pattern.length];
                    posCurrent = 0;
                } else if (posCurrent == pattern.length) {
                    //System.out.println("find pattern:" + new String(values));
                    posCurrent = 0;

                    //result.write(new String(values)); //one to one
                    result.write(new String(newPattern)); //replace
                }
            }
        }
    }
}
