package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.record.ORecordTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrientAutomaticIndex<T extends OrientElement> extends OrientIndex<T> implements AutomaticIndex<T> {

    Set<String> autoIndexKeys = null;
    private static final String KEYS = "keys";

    public OrientAutomaticIndex(String name, Class<T> indexClass, Set<String> autoIndexKeys, OrientGraph graph, ODocument indexCfg) {
        super(name, indexClass, graph, indexCfg);
        init(autoIndexKeys);
    }

    public OrientAutomaticIndex(String name, OrientGraph graph, ODocument indexCfg) {
        super(name, null, graph, indexCfg);
        init();

    }

    public void addAutoIndexKey(String key) {
        if (null == key)
            this.autoIndexKeys = null;
        else {
            if (autoIndexKeys == null) {
                this.autoIndexKeys = new HashSet<String>();
                this.autoIndexKeys.add(key);
            } else {
                this.autoIndexKeys.add(key);
            }
        }

        this.saveConfiguration();
    }

    protected void autoUpdate(String key, Object newValue, Object oldValue, T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            if (oldValue != null)
                this.remove(key, oldValue, element);
            this.put(key, newValue, element);
        }
    }

    protected void autoRemove(String key, Object oldValue, T element) {
        if (this.getIndexClass().isAssignableFrom(element.getClass()) && (this.autoIndexKeys == null || this.autoIndexKeys.contains(key))) {
            this.remove(key, oldValue, element);
        }
    }

    public void removeAutoIndexKey(String key) {
        if (null != this.autoIndexKeys)
            this.autoIndexKeys.remove(key);

        this.saveConfiguration();
    }

    public Set<String> getAutoIndexKeys() {
        return this.autoIndexKeys;
    }

    private void init(Set<String> autoIndexKeys) {
        if (null != autoIndexKeys) {
            this.autoIndexKeys = new HashSet<String>();
            this.autoIndexKeys.addAll(autoIndexKeys);
        }
        indexCfg.field(KEYS, autoIndexKeys);
    }

    private void init() {
        ORecordTrackedList field = indexCfg.field(KEYS);
        if (null == field)
            this.autoIndexKeys = null;
        else {
            this.autoIndexKeys = new HashSet<String>();
            for (Object key : field) {
                this.autoIndexKeys.add((String) key);
            }
        }
    }

    private void saveConfiguration() {
        indexCfg.field(KEYS, this.autoIndexKeys);
        graph.saveIndexConfiguration();
    }
}