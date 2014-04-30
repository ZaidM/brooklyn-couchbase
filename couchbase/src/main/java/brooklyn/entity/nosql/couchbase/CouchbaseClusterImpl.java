package brooklyn.entity.nosql.couchbase;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.entity.group.DynamicClusterImpl;
import brooklyn.location.Location;

public class CouchbaseClusterImpl extends DynamicClusterImpl implements CouchbaseCluster {
    private static final Logger log = LoggerFactory.getLogger(CouchbaseClusterImpl.class);

    public void init() {
        log.info("Initializing the Couchbase cluster...");
        super.init();
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        super.start(locations);
        connectSensors();
    }

    protected void connectSensors() {

    }


}
