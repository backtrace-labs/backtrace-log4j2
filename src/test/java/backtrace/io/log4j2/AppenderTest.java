package backtrace.io.log4j2;

import backtrace.io.data.BacktraceReport;
import backtrace.io.http.BacktraceResult;
import net.jodah.concurrentunit.Waiter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppenderTest {

    @Test
    public void createAppenderWithoutName() {
        // GIVEN
        final String appenderName = null;

        // WHEN
        final Appender appender = Appender.createAppender(appenderName, null, null, null, null,
                "https://backtrace.io/", false, null,
                null, false, 0, 0, 0, false);

        // THEN
        Assert.assertNull(appender);
    }

    @Test
    public void checkAppNameAndAppVersion() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String message = "test-message";
        final String appName = "backtrace-app";
        final String appVersion = "1.0.0";

        final Appender appender = AppenderMock.createAppender("backtrace", null, null, "https://backtrace.io/", "token",
                null, false, appVersion,
                appName, false, 0, 0, 0, false);

        // WHEN
        appender.getBacktraceClient().setCustomRequestHandler(data -> {
            // THEN
            waiter.assertEquals(message, data.getReport().getMessage());
            waiter.assertEquals(appName, data.getAttributes().get("application"));
            waiter.assertEquals(appVersion, data.getAttributes().get("version"));
            waiter.resume();
            return BacktraceResult.onSuccess(data.getReport(), data.getReport().getMessage());
        });

        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(Level.DEBUG)
                .setLoggerName("backtrace")
                .setMessage(new SimpleMessage(message))
                .build();

        appender.append(logEvent);

        // THEN
        waiter.await(1000, 1);
        appender.stop();
    }

    @Test
    public void testUncaughtExceptionHandler() {
        // GIVEN
        final Waiter waiter = new Waiter();
        final Appender appender = AppenderMock.createAppender("backtrace", null, null, "https://backtrace.io/", "token",
                null, true, null,
                null, false, 0, 0, 0, true);

        appender.getBacktraceClient().setCustomRequestHandler(data -> {
            waiter.resume();
            return BacktraceResult.onSuccess(new BacktraceReport("test"), "test");
        });

        // WHEN
        final Thread testingThread = new Thread(() -> {
            Logger logger = (Logger) LogManager.getLogger("another-thread");
            appender.start();
            logger.addAppender(appender);
            throw new RuntimeException("Expected!");
        });
        testingThread.start();

        // THEN
        try {
            testingThread.join();
            waiter.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException exception) {
            Assert.fail(exception.getMessage());
        }
        appender.stop();
        testingThread.interrupt();
    }
}
