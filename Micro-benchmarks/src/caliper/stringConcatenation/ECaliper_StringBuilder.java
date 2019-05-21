package caliper.stringConcatenation;


public class ECaliper_StringBuilder
{

    public ECaliper_StringBuilder()
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
                c.timeStringBuilder(c1);
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
        (new ECaliper_StringBuilder()).run();
    }

    private StringConcatenationBenchmark c;
}
