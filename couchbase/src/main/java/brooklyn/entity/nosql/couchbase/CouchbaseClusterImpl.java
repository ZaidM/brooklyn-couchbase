package brooklyn.entity.nosql.couchbase;

import static brooklyn.util.JavaGroovyEquivalents.groovyTruth;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import brooklyn.entity.Entity;
import brooklyn.entity.basic.Attributes;
import brooklyn.entity.basic.Entities;
import brooklyn.entity.basic.EntityInternal;
import brooklyn.entity.basic.Lifecycle;
import brooklyn.entity.group.AbstractMembershipTrackingPolicy;
import brooklyn.entity.group.DynamicClusterImpl;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.entity.trait.Startable;
import brooklyn.event.SensorEvent;
import brooklyn.event.SensorEventListener;
import brooklyn.location.Location;
import brooklyn.util.collections.MutableMap;
import brooklyn.util.collections.MutableSet;
import brooklyn.util.time.Time;

public class CouchbaseClusterImpl extends DynamicClusterImpl implements CouchbaseCluster {
    private static final Logger log = LoggerFactory.getLogger(CouchbaseClusterImpl.class);
    private final Object mutex = new Object[0];

    public void init() {
        log.info("Initializing the Couchbase cluster...");
        super.init();

        //set of servers to add, initially an empty set.
        setAttribute(SERVERS_TO_BE_ADDED, Sets.<Entity>newHashSet());
    }

    @Override
    public void start(Collection<? extends Location> locations) {
        super.start(locations);

        connectSensors();
        connectEnrichers();

        //start timeout before adding the servers
        Time.sleep(getConfig(SERVICE_UP_TIME_OUT));

        Optional<Set<Entity>> upNodes = Optional.<Set<Entity>>fromNullable(getAttribute(COUCHBASE_CLUSTER_UP_NODES));
        if (upNodes.isPresent() && !upNodes.get().isEmpty()) {

            //TODO: select a new primary node if this one fails
            Entity primaryNode = upNodes.get().iterator().next();
            ((EntityInternal) primaryNode).setAttribute(CouchbaseNode.IS_PRIMARY_NODE, true);
            setAttribute(COUCHBASE_PRIMARY_NODE, primaryNode);

            removeServerToBeAdded(getPrimaryNode());

            if (getAttribute(COUCHBASE_CLUSTER_UP_NODES).size() >= getQuorumSize()) {
                log.info("number of SERVICE_UP nodes:{} in cluster:{} did reached Quorum:{}, adding the servers", new Object[]{getUpNodes().size(), getId(), getQuorumSize()});
                addServers();

                //wait for servers to be added to the couchbase server
                Time.sleep(getConfig(DELAY_BEFORE_ADVERTISING_CLUSTER));
                Entities.invokeEffector(this, getPrimaryNode(), CouchbaseNode.REBALANCE);

                setAttribute(IS_CLUSTER_INITIALIZED, true);
            } else {
                //retry waiting for service up?
                //check Repeater.
            }
        } else {
            setAttribute(SERVICE_STATE, Lifecycle.ON_FIRE);
        }

    }

    @Override
    public void stop() {
        super.stop();
    }

    public void connectEnrichers() {

//        subscribeToMembers(this, SERVICE_UP, new SensorEventListener<Boolean>() {
//            @Override
//            public void onEvent(SensorEvent<Boolean> event) {
//                setAttribute(SERVICE_UP, calculateServiceUp());
//            }
//        });
    }

    protected void connectSensors() {
        Map<String, Object> flags = MutableMap.<String, Object>builder()
                .put("name", "Controller targets tracker")
                .put("sensorsToTrack", ImmutableSet.of(CouchbaseNode.SERVICE_UP))
                .build();

        AbstractMembershipTrackingPolicy serverPoolMemberTrackerPolicy = new AbstractMembershipTrackingPolicy(flags) {
            protected void onEntityChange(Entity member) {
                onServerPoolMemberChanged(member);
            }

            protected void onEntityAdded(Entity member) {
                onServerPoolMemberChanged(member);
            }

            protected void onEntityRemoved(Entity member) {
                onServerPoolMemberChanged(member);
            }
        };

        addPolicy(serverPoolMemberTrackerPolicy);
        serverPoolMemberTrackerPolicy.setGroup(this);

    }

    protected synchronized void onServerPoolMemberChanged(Entity member) {
        if (log.isTraceEnabled()) log.trace("For {}, considering membership of {} which is in locations {}",
                new Object[]{this, member, member.getLocations()});

        //FIXME: make use of servers to be added after cluster initialization.
        synchronized (mutex) {
            if (belongsInServerPool(member)) {

                Optional<Set<Entity>> upNodes = Optional.fromNullable(getUpNodes());
                if (upNodes.isPresent()) {

                    if (!upNodes.get().contains(member)) {
                        Set<Entity> newNodes = Sets.newHashSet(getUpNodes());
                        newNodes.add(member);
                        setAttribute(COUCHBASE_CLUSTER_UP_NODES, newNodes);

                        //add to set of servers to be added.
                        addServerToBeAdded(member);
                    } else {
                        log.warn("Node already in cluster up nodes {}: {};", this, member);
                    }
                } else {
                    Set<Entity> newNodes = Sets.newHashSet();
                    newNodes.add(member);
                    setAttribute(COUCHBASE_CLUSTER_UP_NODES, newNodes);
                    addServerToBeAdded(member);
                }
            } else {
                Set<Entity> upNodes = getUpNodes();
                if (upNodes != null && upNodes.contains(member)) {
                    upNodes.remove(member);
                    removeServerToBeAdded(member);
                    setAttribute(COUCHBASE_CLUSTER_UP_NODES, upNodes);
                    log.info("Removing couchbase node {}: {}; from cluster", new Object[]{this, member});
                }
            }
            if (log.isTraceEnabled()) log.trace("Done {} checkEntity {}", this, member);
        }
    }

    protected boolean belongsInServerPool(Entity member) {
        if (!groovyTruth(member.getAttribute(Startable.SERVICE_UP))) {
            if (log.isTraceEnabled()) log.trace("Members of {}, checking {}, eliminating because not up", this, member);
            return false;
        }
        if (!getMembers().contains(member)) {
            if (log.isTraceEnabled())
                log.trace("Members of {}, checking {}, eliminating because not member", this, member);

            return false;
        }
        if (log.isTraceEnabled()) log.trace("Members of {}, checking {}, approving", this, member);

        return true;
    }


    protected EntitySpec<?> getMemberSpec() {
        EntitySpec<?> result = super.getMemberSpec();
        if (result != null) return result;
        return EntitySpec.create(CouchbaseNode.class);
    }


    protected int getQuorumSize() {
        Integer quorumSize = getConfig(CouchbaseCluster.INITIAL_QUORUM_SIZE);
        if (quorumSize != null && quorumSize > 0)
            return quorumSize;
        // by default the quorum would be floor(initial_cluster_size/2) + 1
        return (int) Math.floor(getConfig(INITIAL_SIZE) / 2) + 1;
    }

    protected int getActualSize() {
        return Optional.fromNullable(getAttribute(CouchbaseCluster.ACTUAL_CLUSTER_SIZE)).or(-1);
    }

    private Set<Entity> getUpNodes() {
        return getAttribute(COUCHBASE_CLUSTER_UP_NODES);
    }

    private Set<Entity> getServersToAdd() {

        return getAttribute(SERVERS_TO_BE_ADDED);
    }

    private Entity getPrimaryNode() {
        return getAttribute(COUCHBASE_PRIMARY_NODE);
    }

    @Override
    protected boolean calculateServiceUp() {
        if (!super.calculateServiceUp()) return false;
        Set<Entity> upNodes = getAttribute(COUCHBASE_CLUSTER_UP_NODES);
        if (upNodes == null || upNodes.isEmpty() || upNodes.size() < getQuorumSize()) return false;
        return true;
    }

    protected void addServers() {
        //FIXME: disambiguate between method names to differentiate between the stage phase and commit phase.
        log.info("adding the SERVICE_UP couchbase nodes to the cluster..");

        Set<Entity> serversToAdd = MutableSet.<Entity>copyOf(getServersToAdd());
        if (!serversToAdd.isEmpty()) {
            for (Entity e : serversToAdd) {
                String hostname = e.getAttribute(Attributes.HOSTNAME) + ":" + e.getConfig(CouchbaseNode.COUCHBASE_WEB_ADMIN_PORT).iterator().next();
                String username = e.getConfig(CouchbaseNode.COUCHBASE_ADMIN_USERNAME);
                String password = e.getConfig(CouchbaseNode.COUCHBASE_ADMIN_PASSWORD);

                Entities.invokeEffectorWithArgs(this, getPrimaryNode(), CouchbaseNode.SERVER_ADD, hostname, username, password);
                //FIXME check feedback of whether the server was added.
                removeServerToBeAdded(e);
            }
        } else {
            log.warn("no servers to be added on the cluster: {}", this);
        }
    }

    private void addServerToBeAdded(Entity e) {
        Set<Entity> serversToBeAdded = MutableSet.copyOf(getServersToAdd());
        serversToBeAdded.add(e);
        setAttribute(SERVERS_TO_BE_ADDED, serversToBeAdded);

        //FIXME: find a way to collect servers to be added before adding them directly.
        if (isClusterInitialized()) {
            addServers();
        }
    }

    private void removeServerToBeAdded(Entity e) {
        Set<Entity> serversToBeAdded = MutableSet.copyOf(getServersToAdd());
        if (!serversToBeAdded.contains(e)) {
            serversToBeAdded.remove(e);
            setAttribute(SERVERS_TO_BE_ADDED, serversToBeAdded);
        }
    }

    public boolean isClusterInitialized() {
        return getAttribute(IS_CLUSTER_INITIALIZED);
    }
}
