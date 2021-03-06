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



package org.onlab.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements IPv6 packet format. (RFC 2460)
 */
public class IPv6 extends BasePacket {
    public static final byte FIXED_HEADER_LENGTH = 40; // bytes

    // TODO: Implement extension header.
    public static final byte PROTOCOL_TCP = 0x6;
    public static final byte PROTOCOL_UDP = 0x11;
    public static final byte PROTOCOL_ICMP6 = 0x3A;
    public static final Map<Byte, Class<? extends IPacket>> PROTOCOL_CLASS_MAP =
            new HashMap<>();

    static {
        IPv6.PROTOCOL_CLASS_MAP.put(IPv6.PROTOCOL_ICMP6, ICMP6.class);
        IPv6.PROTOCOL_CLASS_MAP.put(IPv6.PROTOCOL_TCP, TCP.class);
        IPv6.PROTOCOL_CLASS_MAP.put(IPv6.PROTOCOL_UDP, UDP.class);
    }

    protected byte version;
    protected byte trafficClass;
    protected int flowLabel;
    protected short payloadLength;
    protected byte nextHeader;
    protected byte hopLimit;
    protected byte[] sourceAddress = new byte[Ip6Address.BYTE_LENGTH];
    protected byte[] destinationAddress = new byte[Ip6Address.BYTE_LENGTH];

    /**
     * Default constructor that sets the version to 6.
     */
    public IPv6() {
        super();
        this.version = 6;
    }

    /**
     * Gets IP version.
     *
     * @return the IP version
     */
    public byte getVersion() {
        return this.version;
    }

    /**
     * Sets IP version.
     *
     * @param version the IP version to set
     * @return this
     */
    public IPv6 setVersion(final byte version) {
        this.version = version;
        return this;
    }

    /**
     * Gets traffic class.
     *
     * @return the traffic class
     */
    public byte getTrafficClass() {
        return this.trafficClass;
    }

    /**
     * Sets traffic class.
     *
     * @param trafficClass the traffic class to set
     * @return this
     */
    public IPv6 setTrafficClass(final byte trafficClass) {
        this.trafficClass = trafficClass;
        return this;
    }

    /**
     * Gets flow label.
     *
     * @return the flow label
     */
    public int getFlowLabel() {
        return this.flowLabel;
    }

    /**
     * Sets flow label.
     *
     * @param flowLabel the flow label to set
     * @return this
     */
    public IPv6 setFlowLabel(final int flowLabel) {
        this.flowLabel = flowLabel;
        return this;
    }

    /**
     * Gets next header.
     *
     * @return the next header
     */
    public byte getNextHeader() {
        return this.nextHeader;
    }

    /**
     * Sets next header.
     *
     * @param nextHeader the next header to set
     * @return this
     */
    public IPv6 setNextHeader(final byte nextHeader) {
        this.nextHeader = nextHeader;
        return this;
    }

    /**
     * Gets hop limit.
     *
     * @return the hop limit
     */
    public byte getHopLimit() {
        return this.hopLimit;
    }

    /**
     * Sets hop limit.
     *
     * @param hopLimit the hop limit to set
     * @return this
     */
    public IPv6 setHopLimit(final byte hopLimit) {
        this.hopLimit = hopLimit;
        return this;
    }

    /**
     * Gets source address.
     *
     * @return the IPv6 source address
     */
    public byte[] getSourceAddress() {
        return this.sourceAddress;
    }

    /**
     * Sets source address.
     *
     * @param sourceAddress the IPv6 source address to set
     * @return this
     */
    public IPv6 setSourceAddress(final byte[] sourceAddress) {
        this.sourceAddress = Arrays.copyOfRange(sourceAddress, 0, Ip6Address.BYTE_LENGTH);
        return this;
    }

    /**
     * Gets destination address.
     *
     * @return the IPv6 destination address
     */
    public byte[] getDestinationAddress() {
        return this.destinationAddress;
    }

    /**
     * Sets destination address.
     *
     * @param destinationAddress the IPv6 destination address to set
     * @return this
     */
    public IPv6 setDestinationAddress(final byte[] destinationAddress) {
        this.destinationAddress = Arrays.copyOfRange(destinationAddress, 0, Ip6Address.BYTE_LENGTH);
        return this;
    }

    @Override
    public byte[] serialize() {
        byte[] payloadData = null;
        if (this.payload != null) {
            this.payload.setParent(this);
            payloadData = this.payload.serialize();
        }

        this.payloadLength = payloadData == null ? 0 : (short) payloadData.length;

        final byte[] data = new byte[FIXED_HEADER_LENGTH + payloadLength];
        final ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putInt((this.version & 0xf) << 28 | (this.trafficClass & 0xff) << 20 | this.flowLabel & 0xfffff);
        bb.putShort(this.payloadLength);
        bb.put(this.nextHeader);
        bb.put(this.hopLimit);
        bb.put(this.sourceAddress, 0, Ip6Address.BYTE_LENGTH);
        bb.put(this.destinationAddress, 0, Ip6Address.BYTE_LENGTH);

        if (payloadData != null) {
            bb.put(payloadData);
        }

        return data;
    }

    @Override
    public IPacket deserialize(final byte[] data, final int offset,
                               final int length) {
        final ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        int iscratch;

        iscratch = bb.getInt();
        this.version = (byte) (iscratch >> 28 & 0xf);
        this.trafficClass = (byte) (iscratch >> 20 & 0xff);
        this.flowLabel = iscratch & 0xfffff;
        this.payloadLength = bb.getShort();
        this.nextHeader = bb.get();
        this.hopLimit = bb.get();
        bb.get(this.sourceAddress, 0, Ip6Address.BYTE_LENGTH);
        bb.get(this.destinationAddress, 0, Ip6Address.BYTE_LENGTH);

        IPacket payload;
        if (IPv6.PROTOCOL_CLASS_MAP.containsKey(this.nextHeader)) {
            final Class<? extends IPacket> clazz = IPv6.PROTOCOL_CLASS_MAP
                    .get(this.nextHeader);
            try {
                payload = clazz.newInstance();
            } catch (final Exception e) {
                throw new RuntimeException(
                        "Error parsing payload for IPv6 packet", e);
            }
        } else {
            payload = new Data();
        }
        this.payload = payload.deserialize(data, bb.position(),
                bb.limit() - bb.position());
        this.payload.setParent(this);

        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 2521;
        int result = super.hashCode();
        ByteBuffer bb;
        bb = ByteBuffer.wrap(this.destinationAddress);
        for (int i = 0; i < 4; i++) {
            result = prime * result + bb.getInt();
        }
        result = prime * result + this.trafficClass;
        result = prime * result + this.flowLabel;
        result = prime * result + this.hopLimit;
        result = prime * result + this.nextHeader;
        result = prime * result + this.payloadLength;
        bb = ByteBuffer.wrap(this.sourceAddress);
        for (int i = 0; i < 4; i++) {
            result = prime * result + bb.getInt();
        }
        result = prime * result + this.version;
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof IPv6)) {
            return false;
        }
        final IPv6 other = (IPv6) obj;
        if (!Arrays.equals(this.destinationAddress, other.destinationAddress)) {
            return false;
        }
        if (this.trafficClass != other.trafficClass) {
            return false;
        }
        if (this.flowLabel != other.flowLabel) {
            return false;
        }
        if (this.hopLimit != other.hopLimit) {
            return false;
        }
        if (this.nextHeader != other.nextHeader) {
            return false;
        }
        if (this.payloadLength != other.payloadLength) {
            return false;
        }
        if (!Arrays.equals(this.sourceAddress, other.sourceAddress)) {
            return false;
        }
        return true;
    }
}
