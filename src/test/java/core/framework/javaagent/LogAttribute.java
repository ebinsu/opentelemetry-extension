package core.framework.javaagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author ebin
 */
public class LogAttribute {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAttribute.class);
    private static final String NULL_STRING = "null";
    private static final String KEYWORD = "=";

    public static void info(String key, Object... values) {
        String value = MDC.get(key);
        if (value == null) {
            value = valuesString(values);
        } else {
            value = value + " , " + valuesString(values);
        }
        if (value.contains(KEYWORD)) {
            value = value.replace(KEYWORD, "*");
        }
        MDC.put(key, value);
        LOGGER.info(new LogAttributeMarker(), "{} = {}", key, value);
    }

    private static String valuesString(Object... values) {
        if (values == null) {
            return NULL_STRING;
        } else if (values.length == 1) {
            if (values[0] == null) {
                return NULL_STRING;
            } else {
                return String.valueOf(values[0]);
            }
        } else {
            return Stream.of(values).map(m -> {
                if (m == null) {
                    return NULL_STRING;
                } else {
                    return String.valueOf(m);
                }
            }).collect(Collectors.joining(",", "[", "]"));
        }
    }
}
