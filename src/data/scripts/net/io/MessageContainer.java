package data.scripts.net.io;

import data.scripts.net.data.SourcePackable;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class MessageContainer {

    private final int tick;
    private final List<SourcePackable> packables;
    private final boolean flush;
    private final InetSocketAddress dest;
    private final ByteBuf output;
    private final int connectionID;
    private int bufSize;

    public MessageContainer(List<SourcePackable> packables, int tick, boolean flush, InetSocketAddress dest, ByteBuf output, int connectionID) throws IOException {
        this.tick = tick;
        this.packables = packables;
        this.flush = flush;
        this.dest = dest;
        this.output = output;
        this.connectionID = connectionID;
    }

    public void addPackable(SourcePackable toAdd) {
        packables.add(toAdd);
    }

    public void addPackables(List<SourcePackable> toAdd) {
        packables.addAll(toAdd);
    }

    public boolean isEmpty() {
        return packables.isEmpty();
    }

    public ByteBuf get() {
        // prevent ref count exception
        output.retain();

        output.clear();

        // write data
        output.writeInt(tick);
        output.writeInt(connectionID);

        for (SourcePackable packable : packables) {
            packable.write(flush, output);
        }

        bufSize = output.writerIndex();

        return output;
    }

    public InetSocketAddress getDest() {
        return dest;
    }

    public int getTick() {
        return tick;
    }

    public int getBufSize() {
        return bufSize;
    }

    public int getConnectionID() {
        return connectionID;
    }
}