package brooklyn.entity.nosql.couchbase;

import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;

import brooklyn.entity.Entity;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.Sensors;

@ImplementedBy(CouchbaseClusterImpl.class)
public interface CouchbaseCluster {

    AttributeSensor<Map<Entity, String>> COUCHBASE_CLUSTER_NODES = Sensors.newSensor(new TypeToken<Map<Entity, String>>() {
    }, "couchbase.cluster.nodes", "Names of all active couchbase nodes in the cluster");

    AttributeSensor<List<String>> COUCHBASE_CLUSTER_BUCKETS = Sensors.newSensor(new TypeToken<List<String>>() {
    }, "couchbase.cluster.buckets", "Names of all the buckets the couchbase cluster");
}
