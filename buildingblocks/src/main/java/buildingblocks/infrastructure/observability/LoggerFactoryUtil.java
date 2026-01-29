package buildingblocks.infrastructure.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerFactoryUtil {

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void info(Class<?> clazz, String message, Object... args) {
        getLogger(clazz).info(message, args);
    }

    public static void warn(Class<?> clazz, String message, Object... args) {
        getLogger(clazz).warn(message, args);
    }

    public static void error(Class<?> clazz, String message, Object... args) {
        getLogger(clazz).error(message, args);
    }
}
