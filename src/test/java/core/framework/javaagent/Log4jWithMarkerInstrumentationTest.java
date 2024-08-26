package core.framework.javaagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Iterator;

/**
 * @author ebin
 */
public class Log4jWithMarkerInstrumentationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(Log4jWithMarkerInstrumentationTest.class);

    public static void main(String[] args) {
        LOGGER.warn(new ErrorCodeMarker("test"), "warn()");
        LOGGER.warn(new ErrorCodeMarker("test"), "warn({})", 1);
        LOGGER.warn(new ErrorCodeMarker("test"), "warn({},{})", 1, 2);
        LOGGER.warn(new ErrorCodeMarker("test"), "warn(ex)", new Throwable());
    }

    public static class ErrorCodeMarker implements Marker {
        public String code;

        public ErrorCodeMarker(String code) {
            this.code = code;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void add(Marker reference) {

        }

        @Override
        public boolean remove(Marker reference) {
            return false;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }

        @Override
        public boolean hasReferences() {
            return false;
        }

        @Override
        public Iterator<Marker> iterator() {
            return null;
        }

        @Override
        public boolean contains(Marker other) {
            return false;
        }

        @Override
        public boolean contains(String name) {
            return false;
        }
    }
}
