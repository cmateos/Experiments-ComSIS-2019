package caliper.stringConcatenation;

public class ECaliper_StringConcatenation
{

    public ECaliper_StringConcatenation()
    {
        c = new StringConcatenationBenchmark();
    }

    public void run()
    {
        long l = 0L;
        char c1 = '\u2710';
        do
        {
            if(l >= 0x7fffffffffffffffL)
                break;
            try
            {
                c.timeConcatenation(c1);
                l++;
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
                throw new RuntimeException(exception);
            }
        } while(true);
    }

    public static void main(String args[])
        throws Exception
    {
        (new ECaliper_StringConcatenation()).run();
    }

    private StringConcatenationBenchmark c;
}
