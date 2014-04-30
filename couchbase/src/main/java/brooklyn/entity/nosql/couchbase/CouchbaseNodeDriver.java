package brooklyn.entity.nosql.couchbase;

import brooklyn.entity.basic.SoftwareProcessDriver;

public interface CouchbaseNodeDriver extends SoftwareProcessDriver {
    public String getOsTag();
}
