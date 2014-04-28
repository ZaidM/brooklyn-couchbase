package brooklyn.entity.nosql.couchbase;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.location.basic.SshMachineLocation;

public class CouchbaseNodeSshDriver extends AbstractSoftwareProcessSshDriver implements CouchbaseNodeDriver {

    public CouchbaseNodeSshDriver(final CouchbaseNodeImpl entity, final SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void install() {

        //for reference https://github.com/urbandecoder/couchbase/blob/master/recipes/server.rb

        //download urls
        //Ubuntu 12.04
        //http://packages.couchbase.com/releases/2.2.0/couchbase-server-community_2.2.0_x86_64.deb
        //http://packages.couchbase.com/releases/2.2.0/couchbase-server-community_2.2.0_x86.deb

        //for RedHat 6
        //http://packages.couchbase.com/releases/2.2.0/couchbase-server-community_2.2.0_x86_64.rpm
        //http://packages.couchbase.com/releases/2.2.0/couchbase-server-community_2.2.0_x86.rpm

        //for Mac OS
        //http://packages.couchbase.com/releases/2.2.0/couchbase-server-community_2.2.0_x86_64.zip


        //installation instructions (http://docs.couchbase.com/couchbase-manual-2.5/cb-install/#preparing-to-install)
        //RedHat dependencies
        //sudo yum install -y pkgconfig
        //root-> yum install openssl098e
        //rpm –install couchbase-server version.rpm

        //Ubuntu dependencies
        //root-> apt-get install libssl0.9.8
        //> dpkg -i couchbase-server version.deb

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
