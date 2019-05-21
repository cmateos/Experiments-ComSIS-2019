package caliper.stringConcatenation;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class StringConcatenationBenchmark extends SimpleBenchmark
{

    public StringConcatenationBenchmark()
    {
    }

    public void timeConcatenation(int i)
    {
        String s = "";
        for(int j = 0; j < i; j++)
        {
            String s1 = "";
            for(long l = 0L; l < (long)size; l++)
                s1 = (new StringBuilder()).append(s1).append("a").toString();

        }

    }

    public void timeStringBuilder(int i)
    {
        Object obj = null;
        for(int j = 0; j < i; j++)
        {
            StringBuilder stringbuilder = new StringBuilder(size);
            for(long l = 0L; l < (long)size; l++)
                stringbuilder.append("a");

        }

    }

    public static void main(String args[])
        throws Exception
    {
        String args1[] = new String[1];
        args1[0] = "-Jvmtype=-server,-client";
        Runner.main(caliper/stringConcatenation/StringConcatenationBenchmark, args1);
    }

    int size;
}
