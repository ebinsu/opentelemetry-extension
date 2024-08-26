package core.framework.javaagent;

import org.slf4j.Marker;

import java.util.Iterator;

/**
 * @author ebin
 */
public class LogAttributeMarker implements Marker {
    @Override
    public String getName() {
        return "log-attribute";
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
