package ru;

import com.sun.xml.internal.messaging.saaj.packaging.mime.util.LineInputStream;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class XMLResolutionTest {

    private static String in = System.getProperty("user.dir") + "/testFiles/in.xml";
    private static String out = System.getProperty("user.dir") + "/testFiles/result/out.xml";
    private static String xslt = System.getProperty("user.dir") + "/testFiles/xslt.xml";
    private static String xsd = System.getProperty("user.dir") + "/testFiles/xsdShema.xml";
    private static String outRight = System.getProperty("user.dir") + "/testFiles/outRight.xml";
    private static String wrongIn = System.getProperty("user.dir") + "/testFiles/wrongIn.xml";
    private static String wrongShema = System.getProperty("user.dir") + "/testFiles/wrongShema.xml";
    private static String wrongXslt = System.getProperty("user.dir") + "/testFiles/nowhere.xml";
    private TestAppender testAppender = new TestAppender();

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLResolution.logger.addAppender(testAppender);
    }

    @After
    public void setOut(){
        XMLResolution.logger.removeAppender(testAppender);
    }

    @Test
    public void whenAlldone() {
        boolean result = false;
        boolean loggerEmpty = false;
        XMLResolution.main(new String[]{in, xsd, xslt, out});
        List<LoggingEvent> list = testAppender.getLog();
        if (list.size() == 0)
            loggerEmpty = true;
        try {
            Diff diff = new Diff(textForFiles(out), textForFiles(outRight));
            result = diff.similar();
        } catch (Exception ex) {
            result = false;
        }
        assertTrue(loggerEmpty && result);
    }

    @Test
    public void whenInWrong() {
        boolean loggerNotEmpty = false;
        XMLResolution.main(new String[]{wrongIn, xsd, xslt, out});
        List<LoggingEvent> list = testAppender.getLog();
        LoggingEvent firstLogEntry = list.get(0);
        String errorMessage=(String)firstLogEntry.getMessage();
        if (list.size() == 1 && errorMessage.equals("FileIn validation problem"))
            loggerNotEmpty = true;
        assertTrue(loggerNotEmpty);
    }

    @Test
    public void whenShemaWrongForOut() {
        boolean loggerNotEmpty = false;
        XMLResolution.main(new String[]{in, wrongShema, xslt, out});
        List<LoggingEvent> list = testAppender.getLog();
        LoggingEvent firstLogEntry = list.get(0);
        String errorMessage=(String)firstLogEntry.getMessage();
        if (list.size() == 1 && errorMessage.equals("FileOut validation problem"))
            loggerNotEmpty = true;
        assertTrue(loggerNotEmpty);
    }

    @Test
    public void whenConvertingGoWrong() {
        boolean loggerNotEmpty = false;
        XMLResolution.main(new String[]{in, xsd, wrongXslt, out});
        List<LoggingEvent> list = testAppender.getLog();
        LoggingEvent firstLogEntry = list.get(0);
        String errorMessage=(String)firstLogEntry.getMessage();
        if (list.size() == 1 && errorMessage.equals("Converting problem"))
            loggerNotEmpty = true;
        assertTrue(loggerNotEmpty);
    }


    public static String textForFiles(String name) throws IOException {
        Object[] arrayOutFile = Files.lines(Paths.get(name), StandardCharsets.UTF_8).toArray();
        String[] arrayStrOut = Arrays.copyOf(arrayOutFile, arrayOutFile.length, String[].class);
        return String.join("", arrayStrOut);
    }

    class TestAppender extends AppenderSkeleton {

        private final List<LoggingEvent> log = new ArrayList<>();

        @Override
        protected void append(LoggingEvent loggingEvent) {
            log.add(loggingEvent);
        }

        @Override
        public void close() {

        }

        @Override
        public boolean requiresLayout() {
            return false;
        }

        public List<LoggingEvent> getLog() {
            return new ArrayList<LoggingEvent>(log);
        }
    }
}
