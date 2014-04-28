package brooklyn.entity.nosql.couchbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import brooklyn.entity.AbstractEc2LiveTest;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.location.Location;
import brooklyn.test.EntityTestUtils;

public class CouchbaseNodeEc2LiveTest extends AbstractEc2LiveTest {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseNodeEc2LiveTest.class);

    @Override
    protected void doTest(Location loc) {
        CouchbaseNode entity = app.createAndManageChild(EntitySpec.create(CouchbaseNode.class));
        app.start(ImmutableList.of(loc));

        EntityTestUtils.assertAttributeEqualsEventually(entity, CouchbaseNode.SERVICE_UP, true);

    }
}
