package brooklyn.entity.nosql.couchbase;

import static brooklyn.util.ssh.BashCommands.*;
import static java.lang.String.format;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import brooklyn.entity.basic.AbstractSoftwareProcessSshDriver;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.drivers.downloads.DownloadResolver;
import brooklyn.location.OsDetails;
import brooklyn.location.basic.SshMachineLocation;
import brooklyn.util.ssh.BashCommands;

public class CouchbaseNodeSshDriver extends AbstractSoftwareProcessSshDriver implements CouchbaseNodeDriver {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseNodeSshDriver.class);
    private boolean isClusterInit = false;

    public CouchbaseNodeSshDriver(final CouchbaseNodeImpl entity, final SshMachineLocation machine) {
        super(entity, machine);
    }

    @Override
    public void install() {
        //for reference https://github.com/urbandecoder/couchbase/blob/master/recipes/server.rb
        //installation instructions (http://docs.couchbase.com/couchbase-manual-2.5/cb-install/#preparing-to-install)

        DownloadResolver resolver = Entities.newDownloader(this);
        List<String> urls = resolver.getTargets();
        String saveAs = resolver.getFilename();
        log.warn("saveAs filename is {}", saveAs);

        OsDetails osDetails = getMachine().getMachineDetails().getOsDetails();

        if (osDetails.isLinux()) {
            List<String> commands = installLinux(urls, saveAs);
            //FIXME installation return error but the server is up and running.
            newScript(INSTALLING)
                    .body.append(commands).execute();
        }
    }

    private List<String> installLinux(List<String> urls, String saveAs) {

        log.info("Installing from package manager couchbase-server version:%s", getVersion());

        String apt = chainGroup(
                "export DEBIAN_FRONTEND=noninteractive",
                "which apt-get",
                sudo("apt-get update"),
                sudo("apt-get install -y libssl0.9.8"),
                sudo(format("dpkg -i %s", saveAs)));

        String yum = chainGroup(
                "which yum",
                sudo("yum install -y pkgconfig"),
                //FIXME openssl098e wasn't found on CentOS 5.6
                //sudo("yum install -y openssl098e"),
                sudo("yum install -y openssl"),
                sudo(format("rpm --install %s", saveAs)));

        return ImmutableList.<String>builder()
                .add(INSTALL_CURL)
                .addAll(BashCommands.commandsToDownloadUrlsAs(urls, saveAs))
                .add(alternatives(apt, yum))
                .build();
    }

    @Override
    public void customize() {

        //initialize the node
//        newScript(CUSTOMIZING)
//                .body.append(format("%s node-init -c %s --node-init-data-path=/tmp/data --node-init-index-path=/tmp/index", getCouchbaseCliCmd(), getCouchbaseHostnameAndPort()))
//                .execute();
        //http://blog.couchbase.com/often-overlooked-linux-os-tweaks

        //turn off swappiness
        //sudo echo 0 > /proc/sys/vm/swappiness

        //disable THP
        //sudo echo never > /sys/kernel/mm/transparent_hugepage/enabled
        //sudo echo never > /sys/kernel/mm/transparent_hugepage/defrag
    }

    @Override
    public void launch() {
        //automatically starts after installation
        //sudo /etc/init.d/couchbase-server start
    }

    @Override
    public boolean isRunning() {
        return (newScript(CHECK_RUNNING)
                .body.append("pwd")
                .execute() == 0);
    }

    @Override
    public void stop() {
        newScript(STOPPING)
                .body.append(sudo("/etc/init.d/couchbase-server stop"))
                .execute();
    }

    @Override
    public String getVersion() {
        return entity.getConfig(CouchbaseNode.SUGGESTED_VERSION);
    }

    @Override
    public String getOsTag() {
        OsDetails os = getLocation().getOsDetails();
        if (os == null) {
            // Default to generic linux
            return "x86_64.rpm";
        } else {
            //FIXME should be a better way to check for OS name and version
            String osName = os.getName().toLowerCase();
            String fileExtension = osName.contains("deb") || osName.contains("ubuntu") ? ".deb" : ".rpm";
            String arch = os.is64bit() ? "x86_64" : "x86";
            return arch + fileExtension;
        }
    }

    private String getUsername() {
        return entity.getConfig(CouchbaseNode.COUCHBASE_ADMIN_USERNAME);
    }

    private String getPassword() {
        return entity.getConfig(CouchbaseNode.COUCHBASE_ADMIN_PASSWORD);
    }

    private String getWebPort() {
        return entity.getConfig(CouchbaseNode.COUCHBASE_WEB_ADMIN_PORT).iterator().next().toString();
    }

    private String getCouchbaseHostnameAndCredentials() {
        return format("-c %s:%s -u %s -p %s", getHostname(), getWebPort(), getUsername(), getPassword());
    }

    private String getCouchbaseHostnameAndPort() {
        return format("-c %s:%s", getHostname(), getWebPort());
    }

    private String getClusterInitRamSize() {
        return entity.getConfig(CouchbaseNode.COUCHBASE_CLUSTER_INIT_RAM_SIZE).toString();
    }

    public void clusterInit() {
        newScript("clusterInit")
                .body.append(
                couchbaseCli("cluster-init") +
                        getCouchbaseHostnameAndPort() +
                        " --cluster-init-username=" + getUsername() +
                        " --cluster-init-password=" + getPassword() +
                        " --cluster-init-port=" + getWebPort() +
                        " --cluster-init-ramsize=" + getClusterInitRamSize())
                .execute();
    }

    public void rebalance() {
        newScript("rebalance")
                .body.append(
                couchbaseCli("rebalance") +
                        getCouchbaseHostnameAndCredentials())
                .execute();
    }

    public void serverAdd(String serverToAdd) {
        //FIXME add username/password of the server to add
        newScript("serverAdd")
                .body.append(
                couchbaseCli("server-add") +
                        getCouchbaseHostnameAndCredentials() +
                        " --server-add=" + serverToAdd +
                        " --server-add-username=Administrator" +
                        " --server-add-password=password").execute();

    }

    public static String couchbaseCli(String cmd) {
        return "/opt/couchbase/bin/couchbase-cli " + cmd + " ";
    }

}
