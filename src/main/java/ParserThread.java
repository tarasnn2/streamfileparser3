import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class ParserThread extends RecursiveAction {

    private static final String RESULT = "static/result.txt";
    private static final String PATTERN_OLD = "static/pattern.txt";
    private static final String PATTERN_NEW = "static/newPattern.txt";
    //private static final String ORIGIN = "static/origin.txt";
    private static final String ORIGIN = "/home/tnaumenko/Downloads/big.txt";

    private byte[] patternOld;
    private byte[] patternNew;
    private long start;
    private long limit;
    private BufferedReader origin;
    private BufferedWriter result;

    public ParserThread(byte[] patternOld,
                        byte[] patternNew,
                        long start,
                        long limit,
                        BufferedReader origin,
                        BufferedWriter result) {
        this.patternOld = patternOld;
        this.patternNew = patternNew;
        this.start = start;
        this.limit = limit;
        this.origin = origin;
        this.result = result;
    }

    public static void main(String[] args) throws IOException {

        File originFile = new File(ORIGIN);
        File patternOldFile = new File(PATTERN_OLD);

        try (FileInputStream streamPatternOld = new FileInputStream(patternOldFile);
             FileInputStream streamPatternNew = new FileInputStream(new File(PATTERN_NEW));
             BufferedReader origin = new BufferedReader(new FileReader(originFile));
             BufferedWriter result = new BufferedWriter(new FileWriter(new File(RESULT)))) {


            ForkJoinTask<?> task = new ParserThread(IOUtils.toByteArray(streamPatternOld),
                    IOUtils.toByteArray(streamPatternNew),
                    0,
                    originFile.length(),
                    origin,
                    result);
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(task);
        }
    }

    @Override
    protected void compute() {
        if (limit - start < 100_000) {
            process();
        } else {
            long middle = start + ((limit - start) / 2);
            System.out.println("[start=" + start + ",middle=" + middle + ",end=" + limit + "]");
            invokeAll(new ParserThread(this.patternOld,
                            this.patternNew,
                            this.start,
                            middle,
                            this.origin,
                            this.result),
                    new ParserThread(this.patternOld,
                            this.patternNew,
                            middle,
                            this.limit,
                            this.origin,
                            this.result));
        }
    }

    private void process() {

        long originCurrent = 0;
        int posCurrent = 0, value;
        byte[] values = new byte[patternOld.length];

        try {
            System.out.println("start job");
            while ((value = origin.read()) != -1) {
                values[posCurrent++] = (byte) value;
                originCurrent++;

                //System.out.println("пройдено " + originCurrent + " из " + limit);

                if (!Arrays.equals(Arrays.copyOf(patternOld, posCurrent), Arrays.copyOf(values, posCurrent))) {
                    result.write(new String(Arrays.copyOf(values, posCurrent)));
                    values = new byte[patternOld.length];
                    posCurrent = 0;
                } else if (posCurrent == patternOld.length) {
                    System.out.println("на позиции " + originCurrent + " найдена последовательность: " + new String(values));
                    posCurrent = 0;

                    //result.write(new String(values)); //1 в 1
                    result.write(new String(patternNew)); //замена
                    //закоментить для удаления
                }
            }
            System.out.println("stop job");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
