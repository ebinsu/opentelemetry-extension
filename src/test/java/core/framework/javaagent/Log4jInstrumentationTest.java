package core.framework.javaagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ebin
 */
public class Log4jInstrumentationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log4jInstrumentationTest.class);

    public static void main(String[] args) {
        LogAttribute.info("1", "1");
        LogAttribute.info("1", "2");

//        LOGGER.warn("warn()");
//        LOGGER.warn("warn ({})", "1");
//        LOGGER.error("warn ({},{})", 1, 2);
//        LOGGER.warn("warn ({},{},{})", 1, 2, 3);
    }
}
