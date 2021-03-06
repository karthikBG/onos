/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.codec.impl;

import org.onosproject.codec.CodecContext;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.criteria.Criterion;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Traffic selector codec.
 */
public class TrafficSelectorCodec extends JsonCodec<TrafficSelector> {
    @Override
    public ObjectNode encode(TrafficSelector selector, CodecContext context) {
        checkNotNull(selector, "Traffic selector cannot be null");

        final ObjectNode result = context.mapper().createObjectNode();
        final ArrayNode jsonCriteria = result.putArray("criteria");

        if (selector.criteria() != null) {
            for (final Criterion criterion :selector.criteria()) {
                // TODO: would be better to have a codec that understands criteria
                jsonCriteria.add(criterion.toString());
            }
        }

        return result;
    }
}
