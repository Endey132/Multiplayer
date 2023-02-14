package data.scripts.net.data.packables.metadata;

import data.scripts.net.data.packables.DestExecute;
import data.scripts.net.data.packables.EntityData;
import data.scripts.net.data.packables.RecordLambda;
import data.scripts.net.data.packables.SourceExecute;
import data.scripts.net.data.records.ByteRecord;
import data.scripts.net.data.tables.BaseEntityManager;
import data.scripts.net.data.tables.InboundEntityManager;
import data.scripts.net.io.BaseConnectionWrapper;
import data.scripts.plugins.MPPlugin;

public class ServerConnectionData extends EntityData {

    public static byte TYPE_ID;

    private byte connectionState;
    private byte connectionID;

    public ServerConnectionData(short instanceID, final byte connectionID, final BaseConnectionWrapper connection) {
        super(instanceID);

        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection state"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return (byte) connection.getConnectionState().ordinal();
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ServerConnectionData serverConnectionData = (ServerConnectionData) packable;
                        serverConnectionData.setConnectionState(value);
                    }
                }
        ));
        addRecord(new RecordLambda<>(
                ByteRecord.getDefault().setDebugText("connection id"),
                new SourceExecute<java.lang.Byte>() {
                    @Override
                    public java.lang.Byte get() {
                        return connectionID;
                    }
                },
                new DestExecute<Byte>() {
                    @Override
                    public void execute(Byte value, EntityData packable) {
                        ServerConnectionData serverConnectionData = (ServerConnectionData) packable;
                        serverConnectionData.setConnectionID(value);
                    }
                }
        ));
    }

    @Override
    public void init(MPPlugin plugin, InboundEntityManager manager) {

    }

    @Override
    public void update(float amount, BaseEntityManager manager) {

    }

    @Override
    public void delete() {

    }

    @Override
    public byte getTypeID() {
        return TYPE_ID;
    }

    public byte getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(byte connectionState) {
        this.connectionState = connectionState;
    }

    public void setConnectionID(byte connectionID) {
        this.connectionID = connectionID;
    }

    public byte getConnectionID() {
        return connectionID;
    }
}