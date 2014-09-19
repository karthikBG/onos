package org.onlab.onos.net.trivial.topology.impl;

import org.junit.Before;
import org.junit.Test;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.Device;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.Link;
import org.onlab.onos.net.Path;
import org.onlab.onos.net.PortNumber;
import org.onlab.onos.net.provider.ProviderId;
import org.onlab.onos.net.topology.ClusterId;
import org.onlab.onos.net.topology.GraphDescription;
import org.onlab.onos.net.topology.LinkWeight;
import org.onlab.onos.net.topology.TopologyCluster;
import org.onlab.onos.net.topology.TopologyEdge;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static org.junit.Assert.*;
import static org.onlab.onos.net.DeviceId.deviceId;
import static org.onlab.onos.net.PortNumber.portNumber;
import static org.onlab.onos.net.trivial.topology.impl.SimpleTopologyManagerTest.device;
import static org.onlab.onos.net.trivial.topology.impl.SimpleTopologyManagerTest.link;

/**
 * Test of the default topology implementation.
 */
public class DefaultTopologyTest {

    public static final ProviderId PID = new ProviderId("of", "foo.bar");

    public static final DeviceId D1 = deviceId("of:1");
    public static final DeviceId D2 = deviceId("of:2");
    public static final DeviceId D3 = deviceId("of:3");
    public static final DeviceId D4 = deviceId("of:4");
    public static final DeviceId D5 = deviceId("of:5");

    public static final PortNumber P1 = portNumber(1);
    public static final PortNumber P2 = portNumber(2);

    public static final LinkWeight WEIGHT = new LinkWeight() {
        @Override
        public double weight(TopologyEdge edge) {
            return edge.src().deviceId().equals(D4) ||
                    edge.dst().deviceId().equals(D4) ? 2.0 : 1.0;
        }
    };

    private DefaultTopology dt;

    @Before
    public void setUp() {
        long now = System.currentTimeMillis();
        Set<Device> devices = of(device("1"), device("2"),
                                 device("3"), device("4"),
                                 device("5"));
        Set<Link> links = of(link("1", 1, "2", 1), link("2", 1, "1", 1),
                             link("3", 2, "2", 2), link("2", 2, "3", 2),
                             link("1", 3, "4", 3), link("4", 3, "1", 3),
                             link("3", 4, "4", 4), link("4", 4, "3", 4));
        GraphDescription graphDescription =
                new DefaultGraphDescription(now, devices, links);

        dt = new DefaultTopology(PID, graphDescription);
        assertEquals("incorrect supplier", PID, dt.providerId());
        assertEquals("incorrect time", now, dt.time());
        assertEquals("incorrect device count", 5, dt.deviceCount());
        assertEquals("incorrect link count", 8, dt.linkCount());
        assertEquals("incorrect cluster count", 2, dt.clusterCount());
        assertEquals("incorrect broadcast set size", 6,
                     dt.broadcastSetSize(ClusterId.clusterId(0)));
    }

    @Test
    public void pathRelated() {
        Set<Path> paths = dt.getPaths(D1, D2);
        assertEquals("incorrect path count", 1, paths.size());

        paths = dt.getPaths(D1, D3);
        assertEquals("incorrect path count", 2, paths.size());

        paths = dt.getPaths(D1, D5);
        assertTrue("no paths expected", paths.isEmpty());

        paths = dt.getPaths(D1, D3, WEIGHT);
        assertEquals("incorrect path count", 1, paths.size());
    }

    @Test
    public void pointRelated() {
        assertTrue("should be infrastructure point",
                   dt.isInfrastructure(new ConnectPoint(D1, P1)));
        assertFalse("should not be infrastructure point",
                    dt.isInfrastructure(new ConnectPoint(D1, P2)));
    }

    @Test
    public void clusterRelated() {
        Set<TopologyCluster> clusters = dt.getClusters();
        assertEquals("incorrect cluster count", 2, clusters.size());

        TopologyCluster c = dt.getCluster(D1);
        Set<DeviceId> devs = dt.getClusterDevices(c);
        assertEquals("incorrect cluster device count", 4, devs.size());
        assertTrue("cluster should contain D2", devs.contains(D2));
        assertFalse("cluster should not contain D5", devs.contains(D5));
    }

}