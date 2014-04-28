package brooklyn.entity.nosql.couchbase;

import brooklyn.config.ConfigKey;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.entity.basic.SoftwareProcess;
import brooklyn.entity.proxying.ImplementedBy;
import brooklyn.event.basic.BasicAttributeSensorAndConfigKey;
import brooklyn.util.flags.SetFromFlag;

@ImplementedBy(CouchbaseNodeImpl.class)
public interface CouchbaseNode extends SoftwareProcess {

    @SetFromFlag("version")
    ConfigKey<String> SUGGESTED_VERSION = ConfigKeys.newConfigKeyWithDefault(SoftwareProcess.SUGGESTED_VERSION,
            "2.2.0");
    @SetFromFlag("arch")
    ConfigKey<String> SUGGESTED_ARCH = ConfigKeys.newStringConfigKey("couchbase.node.suggestedArch", "64-bit or 32-bit supported version of couchbase", "x86_64");

    @SetFromFlag("baseUrl")
    ConfigKey<String> BASE_URL = ConfigKeys.newStringConfigKey("couchbase.node.baseUrl", "base url for downloading couchbase packages", "http://packages.couchbase.com/releases");
    @SetFromFlag("rpmDownloadUrl")
    BasicAttributeSensorAndConfigKey<String> RPM_DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
            SoftwareProcess.DOWNLOAD_URL, "${baseUrl}/${version}/couchbase-server-community_${version}_${arch}.rpm");

    @SetFromFlag("debDownloadUrl")
    BasicAttributeSensorAndConfigKey<String> DEB_DOWNLOAD_URL = new BasicAttributeSensorAndConfigKey<String>(
            SoftwareProcess.DOWNLOAD_URL, "${baseUrl}/${version}/couchbase-server-community_${version}_${arch}.deb");
}
