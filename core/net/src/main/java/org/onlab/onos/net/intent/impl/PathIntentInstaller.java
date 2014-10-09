package org.onlab.onos.net.intent.impl;

import static org.onlab.onos.net.flow.DefaultTrafficTreatment.builder;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.onos.ApplicationId;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.Link;
import org.onlab.onos.net.flow.DefaultFlowRule;
import org.onlab.onos.net.flow.DefaultTrafficSelector;
import org.onlab.onos.net.flow.FlowRule;
import org.onlab.onos.net.flow.FlowRuleBatchEntry;
import org.onlab.onos.net.flow.FlowRuleBatchEntry.FlowRuleOperation;
import org.onlab.onos.net.flow.FlowRuleBatchOperation;
import org.onlab.onos.net.flow.FlowRuleService;
import org.onlab.onos.net.flow.TrafficSelector;
import org.onlab.onos.net.flow.TrafficTreatment;
import org.onlab.onos.net.intent.IntentExtensionService;
import org.onlab.onos.net.intent.IntentInstaller;
import org.onlab.onos.net.intent.PathIntent;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

/**
 * Installer for {@link PathIntent path connectivity intents}.
 */
@Component(immediate = true)
public class PathIntentInstaller implements IntentInstaller<PathIntent> {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    private final ApplicationId appId = ApplicationId.getAppId();

    @Activate
    public void activate() {
        intentManager.registerInstaller(PathIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterInstaller(PathIntent.class);
    }

    @Override
    public void install(PathIntent intent) {
        TrafficSelector.Builder builder =
                DefaultTrafficSelector.builder(intent.selector());
        Iterator<Link> links = intent.path().links().iterator();
        ConnectPoint prev = links.next().dst();
        List<FlowRuleBatchEntry> rules = Lists.newLinkedList();
        while (links.hasNext()) {
            builder.matchInport(prev.port());
            Link link = links.next();
            TrafficTreatment treatment = builder()
                    .setOutput(link.src().port()).build();

            FlowRule rule = new DefaultFlowRule(link.src().deviceId(),
                    builder.build(), treatment,
                    123, appId, 600);
            rules.add(new FlowRuleBatchEntry(FlowRuleOperation.ADD, rule));
            //flowRuleService.applyFlowRules(rule);
            prev = link.dst();
        }
        FlowRuleBatchOperation batch = new FlowRuleBatchOperation(rules);
        try {
            flowRuleService.applyBatch(batch).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void uninstall(PathIntent intent) {
        TrafficSelector.Builder builder =
                DefaultTrafficSelector.builder(intent.selector());
        Iterator<Link> links = intent.path().links().iterator();
        ConnectPoint prev = links.next().dst();
        List<FlowRuleBatchEntry> rules = Lists.newLinkedList();

        while (links.hasNext()) {
            builder.matchInport(prev.port());
            Link link = links.next();
            TrafficTreatment treatment = builder()
                    .setOutput(link.src().port()).build();
            FlowRule rule = new DefaultFlowRule(link.src().deviceId(),
                    builder.build(), treatment,
                    123, appId, 600);
            rules.add(new FlowRuleBatchEntry(FlowRuleOperation.REMOVE, rule));
            //flowRuleService.removeFlowRules(rule);
            prev = link.dst();
        }
        FlowRuleBatchOperation batch = new FlowRuleBatchOperation(rules);
        try {
            flowRuleService.applyBatch(batch).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}