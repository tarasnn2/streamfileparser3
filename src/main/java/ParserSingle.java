import org.apache.commons.io.IOUtils;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;

public class ParserSingle {

    private static final String RESULT = "static/result.txt";
    private static final String PATTERN_OLD = "static/pattern.txt";
    private static final String PATTERN_NEW = "static/newPattern.txt";
    //private static final String ORIGIN = "static/origin.txt";
    private static final String ORIGIN = "/home/tnaumenko/Downloads/big.txt";

    public static void main(String[] args) throws IOException {

        File originFile = new File(ORIGIN);

        try (FileInputStream streamPatternOld = new FileInputStream(new File(PATTERN_OLD));
             FileInputStream streamPatternNew = new FileInputStream(new File(PATTERN_NEW));
             BufferedReader origin = new BufferedReader(new FileReader(originFile));
             BufferedWriter result = new BufferedWriter(new FileWriter(new File(RESULT)))) {

            Instant start = Instant.now();
            new ParserSingle().process(IOUtils.toByteArray(streamPatternOld),
                    IOUtils.toByteArray(streamPatternNew),
                    0,
                    originFile.length(),
                    origin,
                    result);
            Instant end = Instant.now();
            System.out.println("seconds running: " + (end.getEpochSecond() - start.getEpochSecond()));
        }
    }

    public void process(byte[] patternOld,
                        byte[] patternNew,
                        long start,
                        long limit,
                        BufferedReader origin,
                        BufferedWriter result) throws IOException {

        long originCurrent = 0;
        int posCurrent = 0, value;
        byte[] values = new byte[patternOld.length];

        origin.skip(start);

        while ((value = origin.read()) != -1) {
            values[posCurrent++] = (byte) value;
            originCurrent++;

            //System.out.println("done " + originCurrent + " of " + limit);

            if (!Arrays.equals(Arrays.copyOf(patternOld, posCurrent), Arrays.copyOf(values, posCurrent))) {
                result.write(new String(Arrays.copyOf(values, posCurrent)));
                values = new byte[patternOld.length];
                posCurrent = 0;
            } else if (posCurrent == patternOld.length) {
                //System.out.println("in position " + originCurrent + " find pattern: " + new String(values));
                posCurrent = 0;

                //result.write(new String(values)); //one to one
                result.write(new String(patternNew)); //replace //comment out for removal
            }
        }
    }
}
