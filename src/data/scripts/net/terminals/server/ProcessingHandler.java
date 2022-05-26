package data.scripts.net.terminals.server;

import com.fs.starfarer.api.Global;
import data.scripts.net.data.packables.APackable;
import data.scripts.net.io.PacketContainer;
import data.scripts.net.io.Unpacked;
import data.scripts.plugins.state.DataDuplex;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ProcessingHandler extends ChannelInboundHandlerAdapter {
    public static final float TICK_RATE = Global.getSettings().getFloat("mpServerTickRate");

    private final Logger logger;

    private long initialTime;
    private final double timeU;
    private double deltaU;
    private long updateTime;
    private boolean doFlush = true;

    private int tick;

    private final DataDuplex serverDataDuplex;

    public ProcessingHandler(DataDuplex serverDataDuplex) {
        this.serverDataDuplex = serverDataDuplex;

        logger = Global.getLogger(ProcessingHandler.class);

        initialTime = System.nanoTime();
        timeU = 1000000000d / TICK_RATE;
        deltaU = 1d;

        updateTime = initialTime;

        tick = 0;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        logger.info("Server channel handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        logger.info("Server channel handler removed");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Unpacked unpacked = (Unpacked) msg;

        int clientTick = unpacked.getTick();
        logger.info("Received client tick notice: " + clientTick);

        Map<Integer, APackable> entities = unpacked.getUnpacked();

        // client doesn't send any entity deletions to worry about
        serverDataDuplex.updateInbound(entities, new ArrayList<Integer>());
    }

    /**
     * Called once when TCP connection is active
     * @param ctx context
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws IOException {
        logger.info("Channel active on server");
        logger.info("Server running at " + TICK_RATE + "Hz");

        Console.showMessage("Channel active on server");
        Console.showMessage("Server running at " + TICK_RATE + "Hz");

        ChannelFuture future = writeAndFlushPacket(ctx);
    }

    /**
     * Called once read is complete. Is used to wait and send next packet.
     * @param ctx context
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) throws IOException {
        // keep looping until timer lets it send another packet
        // probably should replace with a thread sleep

        long currentTime;
        while (deltaU < 1d) {
            currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            initialTime = currentTime;
        }

        // time delta
        //long diffTimeNanos = currentTime - updateTime;

        final ChannelFuture future = writeAndFlushPacket(ctx);

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                if (!future.isSuccess()) {
                    deltaU = 1d;
                    doFlush = true;
                }
            }
        });

        //updateTime = currentTime;
        deltaU--;
    }

    private ChannelFuture writeAndFlushPacket(ChannelHandlerContext ctx) throws IOException {
        if (doFlush) serverDataDuplex.flush();

        PacketContainer packet = serverDataDuplex.getPacket(tick);
        tick++;
        return ctx.writeAndFlush(packet);
    }
}
