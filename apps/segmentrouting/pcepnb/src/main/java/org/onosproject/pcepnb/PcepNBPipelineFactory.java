package org.onosproject.pcepnb;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PcepNBPipelineFactory implements ChannelPipelineFactory, ExternalResourceReleasable {

    private static final Logger log = LoggerFactory.getLogger(PcepNBPipelineFactory.class);

    protected PcepNBClient client;
    static final Timer TIMER = new HashedWheelTimer();
    protected IdleStateHandler idleHandler;
    protected ReadTimeoutHandler readTimeoutHandler;
    static final int DEFAULT_KEEP_ALIVE_TIME = 30;
    static final int DEFAULT_DEAD_TIME = 120;
    static final int DEFAULT_WAIT_TIME = 60;

    public PcepNBPipelineFactory(PcepNBClient client) {
        super();
        this.client = client;
        this.idleHandler = new IdleStateHandler(TIMER, DEFAULT_DEAD_TIME, DEFAULT_KEEP_ALIVE_TIME, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(TIMER, DEFAULT_WAIT_TIME);
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        log.info("MDSC-1.5:PipeLine established!");
        PcepNBChannelHandler handler = new PcepNBChannelHandler(client);

        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("pcepmessagedecoder", new PcepNBMessageDecoder());
        pipeline.addLast("pcepmessageencoder", new PcepNBMessageEncoder());
        pipeline.addLast("idle", idleHandler);
        pipeline.addLast("waittimeout", readTimeoutHandler);
        pipeline.addLast("handler", handler);
        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        TIMER.stop();
    }
}