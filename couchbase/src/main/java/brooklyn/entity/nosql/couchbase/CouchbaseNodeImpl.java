package brooklyn.entity.nosql.couchbase;

import brooklyn.entity.basic.SoftwareProcessImpl;

public class CouchbaseNodeImpl extends SoftwareProcessImpl implements CouchbaseNode {
    @Override
    public Class getDriverInterface() {
        return null;
    }
}
