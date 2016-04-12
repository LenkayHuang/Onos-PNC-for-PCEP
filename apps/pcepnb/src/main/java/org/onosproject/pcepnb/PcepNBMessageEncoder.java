package org.onosproject.pcepnb;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.onosproject.pcepio.protocol.PcepMessage;
import org.onosproject.pcepio.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PcepNBMessageEncoder extends OneToOneEncoder {
    protected static final Logger log = LoggerFactory.getLogger(PcepNBMessageEncoder.class);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        log.debug("Sending message");
        if (!(msg instanceof List)) {
            log.debug("Invalid msg.");
            return msg;
        }

        @SuppressWarnings("unchecked")
        List<PcepMessage> msglist = (List<PcepMessage>) msg;

        ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

        for (PcepMessage pm : msglist) {
            pm.writeTo(buf);
        }

        HexDump.pcepHexDump(buf);

        return buf;
    }
}