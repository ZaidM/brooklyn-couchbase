package brooklyn.entity.nosql.couchbase;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import brooklyn.entity.basic.SoftwareProcessImpl;
import brooklyn.location.MachineProvisioningLocation;
import brooklyn.location.cloud.CloudLocationConfig;
import brooklyn.util.collections.MutableSet;
import brooklyn.util.config.ConfigBag;

public class CouchbaseNodeImpl extends SoftwareProcessImpl implements CouchbaseNode {

    @Override
    public Class<CouchbaseNodeDriver> getDriverInterface() {
        return CouchbaseNodeDriver.class;
    }

    @Override
    public CouchbaseNodeDriver getDriver() {
        return (CouchbaseNodeDriver) super.getDriver();
    }

    @Override
    public void init() {
        super.init();
    }

    protected Map<String, Object> obtainProvisioningFlags(@SuppressWarnings("rawtypes") MachineProvisioningLocation location) {
        ConfigBag result = ConfigBag.newInstance(super.obtainProvisioningFlags(location));
        result.configure(CloudLocationConfig.MIN_CORES, 4);
        result.configure(CloudLocationConfig.MIN_RAM, 4000);
        return result.getAllConfig();
    }

    @Override
    protected Collection<Integer> getRequiredOpenPorts() {
        // TODO this creates a huge list of inbound ports; much better to define on a security group using range syntax!
        int erlangRangeStart = getConfig(NODE_DATA_EXCHANGE_PORT_RANGE_START).iterator().next();
        int erlangRangeEnd = getConfig(NODE_DATA_EXCHANGE_PORT_RANGE_END).iterator().next();

        Set<Integer> newPorts = MutableSet.<Integer>copyOf(super.getRequiredOpenPorts());
        newPorts.remove(erlangRangeStart);
        newPorts.remove(erlangRangeEnd);
        for (int i = erlangRangeStart; i <= erlangRangeEnd; i++)
            newPorts.add(i);
        return newPorts;
    }
}
