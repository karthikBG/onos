package org.onlab.onos.net.flow;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

import org.onlab.onos.net.DeviceId;

public class DefaultFlowRule implements FlowRule {

    private final DeviceId deviceId;
    private final int priority;
    private final TrafficSelector selector;
    private final TrafficTreatment treatment;
    private final FlowId id;
    private final long created;
    private final long life;
    private final long idle;
    private final long packets;
    private final long bytes;


    public DefaultFlowRule(DeviceId deviceId,
            TrafficSelector selector, TrafficTreatment treatment, int priority) {
        this.deviceId = deviceId;
        this.priority = priority;
        this.selector = selector;
        this.treatment = treatment;
        this.life = 0;
        this.idle = 0;
        this.packets = 0;
        this.bytes = 0;
        this.id = FlowId.valueOf(this.hashCode());
        this.created = System.currentTimeMillis();
    }

    public DefaultFlowRule(DeviceId deviceId, TrafficSelector selector,
            TrafficTreatment treatment, int priority,
            long life, long idle, long packets, long bytes, Integer flowId) {
        this.deviceId = deviceId;
        this.priority = priority;
        this.selector = selector;
        this.treatment = treatment;

        this.id = FlowId.valueOf(flowId);

        this.life = life;
        this.idle = idle;
        this.packets = packets;
        this.bytes = bytes;
        this.created = System.currentTimeMillis();
    }


    @Override
    public FlowId id() {
        return id;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public DeviceId deviceId() {
        return deviceId;
    }

    @Override
    public TrafficSelector selector() {
        return selector;
    }

    @Override
    public TrafficTreatment treatment() {
        return treatment;
    }

    @Override
    public long lifeMillis() {
        return life;
    }

    @Override
    public long idleMillis() {
        return idle;
    }

    @Override
    public long packets() {
        return packets;
    }

    @Override
    public long bytes() {
        return bytes;
    }

    @Override
    /*
     * The priority and statistics can change on a given treatment and selector
     *
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public int hashCode() {
        return Objects.hash(deviceId, selector, treatment);
    }

    @Override
    /*
     * The priority and statistics can change on a given treatment and selector
     *
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof FlowRule) {
            DefaultFlowRule that = (DefaultFlowRule) obj;
            if (!this.deviceId().equals(that.deviceId())) {
                return false;
            }
            if (!this.treatment().equals(that.treatment())) {
                return false;
            }
            if (!this.selector().equals(that.selector())) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("deviceId", deviceId)
                .add("priority", priority)
                .add("selector", selector)
                .add("treatment", treatment)
                .add("created", created)
                .toString();
    }


}