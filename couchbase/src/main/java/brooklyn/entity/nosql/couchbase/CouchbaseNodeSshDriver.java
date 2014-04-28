package brooklyn.entity.nosql.couchbase;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.location.basic.SshMachineLocation;

public class CouchbaseNodeSshDriver extends AbstractSoftwareProcessSshDriver implements CouchbaseNodeDriver {

    public CouchbaseNodeSshDriver(final CouchbaseNodeImpl entity, final SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void install() {

    }

    @Override
    public void customize() {

    }

    @Override
    public void launch() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void stop() {

    }


}
