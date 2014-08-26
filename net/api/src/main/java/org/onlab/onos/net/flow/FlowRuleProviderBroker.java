package org.onlab.onos.net.flow;

import org.onlab.onos.net.provider.ProviderBroker;

/**
 * Abstraction for a flow rule provider brokerage.
 */
public interface FlowRuleProviderBroker
        extends ProviderBroker<FlowRuleProvider, FlowRuleProviderService> {
}