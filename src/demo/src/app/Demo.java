package app;

import backtrace.io.log4j2.Appender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


public class Demo {


    public static void main(String[] args) throws InterruptedException {
        Logger rootLogger = (Logger) LogManager.getRootLogger();
        Appender backtraceAppender = (Appender) rootLogger.getAppenders().get("BacktraceAppender");

        try{
            String x = null;
            x.toLowerCase();
        }
        catch (Exception exception){
            rootLogger.debug("Example message - log4j2");
        }

        backtraceAppender.await();
        backtraceAppender.stop();

        System.out.println("End");
    }
}