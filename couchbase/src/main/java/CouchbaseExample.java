import java.util.List;

import com.google.common.collect.Lists;

import brooklyn.entity.basic.AbstractApplication;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.StartableApplication;
import brooklyn.entity.nosql.couchbase.CouchbaseCluster;
import brooklyn.entity.nosql.couchbase.CouchbaseNode;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.launcher.BrooklynLauncher;
import brooklyn.util.CommandLineUtil;

public class CouchbaseExample extends AbstractApplication implements StartableApplication {

    public static final String DEFAULT_LOCATION_SPEC = "couchbasetest";

    //public static final String DEFAULT_LOCATION_SPEC = "AWS Oregon (us-west-2)";
    //public static final String DEFAULT_LOCATION_SPEC = "AWS Virginia (us-east-1)";

    public void init() {
        addChild(EntitySpec.create(CouchbaseCluster.class)
                .configure(CouchbaseCluster.INITIAL_SIZE, 3));

    }

    public static void main(String[] argv) {
        List<String> args = Lists.newArrayList(argv);
        String port = CommandLineUtil.getCommandLineOption(args, "--port", "8081+");
        String location = CommandLineUtil.getCommandLineOption(args, "--location", DEFAULT_LOCATION_SPEC);

        BrooklynLauncher launcher = BrooklynLauncher.newInstance()
                .application(EntitySpec.create(StartableApplication.class, CouchbaseExample.class)
                        .displayName("Couchbase Example"))
                .webconsolePort(port)
                .location(location)
                .start();

        Entities.dumpInfo(launcher.getApplications());
    }
}
