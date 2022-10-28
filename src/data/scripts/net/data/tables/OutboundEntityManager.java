package data.scripts.net.data.tables;

import data.scripts.net.data.packables.BasePackable;

import java.util.Map;

public interface OutboundEntityManager extends BaseEntityManager {

    enum PacketType {
        SOCKET,
        DATAGRAM
    }

    Map<Short, BasePackable> getOutbound();

    PacketType getOutboundPacketType();
}
