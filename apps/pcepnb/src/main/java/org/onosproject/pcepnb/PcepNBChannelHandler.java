package org.onosproject.pcepnb;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.onosproject.pcep.controller.PccId;
import org.onosproject.pcepio.exceptions.PcepParseException;
import org.onosproject.pcepio.protocol.*;
import org.onosproject.pcepio.protocol.ver1.PcepLSObjectVer1;
import org.onosproject.pcepio.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

//import org.onosproject.pcep.controller.impl.PcepPacketStatsImpl;

public class PcepNBChannelHandler extends IdleStateAwareChannelHandler {
    static final byte DEADTIMER_MAXIMUM_VALUE = (byte) 0xFF;
    static final byte KEEPALIVE_MULTIPLE_FOR_DEADTIMER = 4;
    private static final Logger log = LoggerFactory.getLogger(PcepNBChannelHandler.class);
    private final PcepNBClient client;
    //private PcepClientDriver pc;
    private PccId thispccId;
    private Channel channel;
    private byte sessionId = 0;
    private byte keepAliveTime = 30;
    private byte deadTime = 120;
    //private PcepPacketStatsImpl pcepPacketStats;
    static final int MAX_WRONG_COUNT_PACKET = 5;
    static final int BYTE_MASK = 0xFF;

    private volatile ChannelState state;

    private volatile Boolean duplicatePccIdFound;

    protected PcepVersion pcepVersion;
    protected PcepFactory factory1;

    /**
     * Create a new unconnected PcepChannelHandler.
     * @param client parent controller
     */
    PcepNBChannelHandler(PcepNBClient client) {
        log.info("Handler constructor with client: {}", client);
        this.client = client;
        this.state = ChannelState.INIT;
        factory1 = client.getPcepMessageFactory1();
        duplicatePccIdFound = Boolean.FALSE;
        //pcepPacketStats = new PcepPacketStatsImpl();
    }

    /**
     * To disconnect PCE. TO DO:
     */
    public void disconnectPCE() {
        //pc.disconnectClient();
    }

    //*************************
    //  Channel State Machine
    //*************************

    /**
     * The state machine for handling the client/channel state. All state
     * transitions should happen from within the state machine (and not from other
     * parts of the code)
     */
    enum ChannelState {
        /**
         * Initial state before channel is connected.
         */
        INIT(false) {
            //to be set to OPENWAIT state
        },
        /**
         * Once the session is established, wait for open message.
         */
        OPENWAIT(false) {
            @Override
            void processPcepMessage(PcepNBChannelHandler h, PcepMessage m) throws IOException, PcepParseException {

                log.debug("Message received in OPEN WAIT State");

                //check for open message
                if (m.getType() != PcepType.OPEN) {
                    // When the message type is not open message increment the wrong packet statistics
                    //h.processUnknownMsg();
                    log.info("It is not MDSC-PCE OPEN message callback in state : OPENWAIT , message is {}", m.getType());
                } else {

                    //h.pcepPacketStats.addInPacket();
                    PcepOpenMsg pOpenmsg = (PcepOpenMsg) m;
                    log.info("Received MDSC-PCE OPEN message callback! {}", pOpenmsg);

                    h.sendKeepAliveMessage();
                    h.setState(KEEPWAIT);
                }
            }
        },
        /**
         * Once the open messages are exchanged, wait for keep alive message.
         */
        KEEPWAIT(false) {
            @Override
            void processPcepMessage(PcepNBChannelHandler h, PcepMessage m) throws IOException, PcepParseException {
                log.debug("message received in KEEPWAIT state");
                //check for keep alive message
                if (m.getType() != PcepType.KEEP_ALIVE) {
                    // When the message type is not keep alive message increment the wrong packet statistics
                    //h.processUnknownMsg();
                    log.info("It is not MDSC-PCE KEEP-ALIVE message callback in state : KEEPWAIT");
                } else {
                    // Set the client connected status
                    //h.pcepPacketStats.addInPacket();
                    final SocketAddress address = h.channel.getRemoteAddress();
                    if (!(address instanceof InetSocketAddress)) {
                        throw new IOException("Invalid PCE connection. MDSC-PCE is indentifed based on IP");
                    }

                    final InetSocketAddress inetAddress = (InetSocketAddress) address;
                    /*h.thispccId = PccId.pccId(IpAddress.valueOf(inetAddress.getAddress()));
                    //h.pc = h.controller.getPcepClientInstance(h.thispccId, h.sessionId, h.pcepVersion,
                            //h.pcepPacketStats);
                    // set the status of pcc as connected
                    h.pc.setConnected(true);
                    h.pc.setChannel(h.channel);

                    // set any other specific parameters to the pcc
                    h.pc.setPcVersion(h.pcepVersion);
                    h.pc.setPcSessionId(h.sessionId);
                    h.pc.setPcKeepAliveTime(h.keepAliveTime);
                    h.pc.setPcDeadTime(h.deadTime);
                    int keepAliveTimer = h.keepAliveTime & BYTE_MASK;
                    int deadTimer = h.deadTime & BYTE_MASK;
                    if (0 == h.keepAliveTime) {
                        h.deadTime = 0;
                    }*/

                    //TODO:Need to check if it's necessary for PNC-PCC to handle pipeline idle.
                    // handle keep alive and dead time
                    /*if (keepAliveTimer != PcepPipelineFactory.DEFAULT_KEEP_ALIVE_TIME
                            || deadTimer != PcepPipelineFactory.DEFAULT_DEAD_TIME) {

                        h.channel.getPipeline().replace("idle", "idle",
                                new IdleStateHandler(PcepPipelineFactory.TIMER, deadTimer, keepAliveTimer, 0));
                    }
                    log.debug("Dead timer : " + deadTimer);
                    log.debug("Keep alive time : " + keepAliveTimer);*/

                    //set the state handshake completion.
                    //h.sendKeepAliveMessage(); //HLK:For PNC-PCC it's not necessary to send keep-alive again!
                    //h.pcepPacketStats.addOutPacket();
                    h.setHandshakeComplete(true);

                    log.info("HLK: Test for STATE_ESTABLISHED");

                    h.setState(ESTABLISHED);

                    log.info("Test to send LS-report to PCE {}", h.channel.getRemoteAddress());
                    h.sendLSReportMessage();//hlk:for Test 4.5
                }
            }
        },
        /**
         * Once the keep alive messages are exchanged, the state is established.
         */
        ESTABLISHED(true) {
            @Override
            void processPcepMessage(PcepNBChannelHandler h, PcepMessage m) throws IOException, PcepParseException {

                //h.channel.getPipeline().remove("waittimeout");
                log.debug("Message received in established state " + m.getType());
                //dispatch the message
                //TODO:Other message type processor in ESTABLISHED state, need to be added.
                //TODO:Especially for ls-report messages for Huawei ACTN.
                //h.dispatchMessage(m);
            }
        };
        private boolean handshakeComplete;

        ChannelState(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

        void processPcepMessage(PcepNBChannelHandler h, PcepMessage m) throws IOException, PcepParseException {
            // do nothing
        }

        /**
         * Is this a state in which the handshake has completed.
         *
         * @return true if the handshake is complete
         */
        public boolean isHandshakeComplete() {
            return this.handshakeComplete;
        }

        /**
         * Sets handshake complete status.
         *
         * @param handshakeComplete status of handshake
         */
        public void setHandshakeComplete(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Into handler channel connected!");
        channel = e.getChannel();
        log.info("Connected to PCE: {}", channel.getRemoteAddress());

        sendHandshakeOpenMessage();
        log.info("PNC send OpenMsg!");
        // Wait for open message from pcc client
        setState(ChannelState.OPENWAIT);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        //log.info("Pcc disconnected callback for pc:{}. Cleaning up ...", getClientInfoString());
        if (thispccId != null) {
            if (!duplicatePccIdFound) {
                // if the disconnected client (on this ChannelHandler)
                // was not one with a duplicate-dpid, it is safe to remove all
                // state for it at the controller. Notice that if the disconnected
                // client was a duplicate-ip, calling the method below would clear
                // all state for the original client (with the same ip),
                // which we obviously don't want.
                //log.debug("{}:removal called", getClientInfoString());
                //if (pc != null) {
                    //pc.removeConnectedClient();
                //}
            } else {
                // A duplicate was disconnected on this ChannelHandler,
                // this is the same client reconnecting, but the original state was
                // not cleaned up - XXX check liveness of original ChannelHandler
                //log.debug("{}:duplicate found", getClientInfoString());
                duplicatePccIdFound = Boolean.FALSE;
            }
        } else {
            //log.warn("no pccip in channelHandler registered for " + "disconnected client {}", getClientInfoString());
        }
    }

    /**
     * Update the channels state. Only called from the state machine.
     *
     * @param state
     */
    private void setState(ChannelState state) {
        this.state = state;
    }

    /**
     * Send handshake open message.
     *
     * @throws IOException,PcepParseException
     */
    private void sendHandshakeOpenMessage() throws IOException, PcepParseException {
        //set capability tlvs for ls-capability
        LinkedList<PcepValueType> llOptionalTlv = new LinkedList<>();
        PceccCapabilityTlv capabilityTlv = new PceccCapabilityTlv(1);
        StatefulPceCapabilityTlv statefulTlv = new StatefulPceCapabilityTlv(4);
        llOptionalTlv.add(capabilityTlv);
        llOptionalTlv.add(statefulTlv);
        //build openObj
        PcepOpenObject pcepOpenobj = factory1.buildOpenObject()
                .setSessionId(sessionId)
                .setKeepAliveTime(keepAliveTime)
                .setDeadTime(deadTime)
                .setOptionalTlv(llOptionalTlv)
                .build();
        //build message
        PcepMessage msg = factory1.buildOpenMsg()
                .setPcepOpenObj(pcepOpenobj)
                .build();
        log.info("Sending OPEN message to {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(msg));
    }

    /**
     * Send keep alive message.
     *
     * @throws IOException when channel is disconnected
     * @throws PcepParseException while building keep alive message
     */
    private void sendKeepAliveMessage() throws IOException, PcepParseException {
        PcepMessage msg = factory1.buildKeepaliveMsg().build();
        log.info("Sending KEEPALIVE message to {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(msg));
    }

    /**
     * Send LS-report message
     * Test for Huawei ACTN
     *
     * @throws IOException when channel is disconnected
     * @throws PcepParseException while building ls-report message
     * */
    private void sendLSReportMessage() throws IOException, PcepParseException {
        log.info("sending LS-report message to {}", channel.getRemoteAddress());

        //set local-node-sub tlvs
        byte[] bytesLocal = {1,0,0,8,6};
        short hlengthLocal = 32;
        IgpRouterIdSubTlv igptlvLocal = new IgpRouterIdSubTlv(bytesLocal, hlengthLocal);
        List<PcepValueType> llNodeDescriptorSubTLVs = new LinkedList<>();
        llNodeDescriptorSubTLVs.add(igptlvLocal);
        LocalNodeDescriptorsTlv localNodeTlv = new LocalNodeDescriptorsTlv(llNodeDescriptorSubTLVs);

        //set remote-node-sub tlvs
        byte[] bytesRemote = {1,0,0,8,7};
        short hlengthRemote = 32;
        IgpRouterIdSubTlv igptlvRemote = new IgpRouterIdSubTlv(bytesRemote, hlengthRemote);
        List<PcepValueType> llRemoteTENodeDescriptorSubTLVs = new LinkedList<>();
        llRemoteTENodeDescriptorSubTLVs.add(igptlvRemote);
        RemoteNodeDescriptorsTlv remoteNodeTlv = new RemoteNodeDescriptorsTlv(llRemoteTENodeDescriptorSubTLVs);

        //set routing universe
        RoutingUniverseTlv routingUniverse = new RoutingUniverseTlv(1L);

        //set pcep-obj header
        PcepObjectHeader lsObjHeader = new PcepObjectHeader((byte)2, (byte)224, true, true, (short)52);

        //set Optional tlvs
        List<PcepValueType> optionalTlvs = new LinkedList<>();
        optionalTlvs.add(routingUniverse);
        optionalTlvs.add(localNodeTlv);
        optionalTlvs.add(remoteNodeTlv);

        //set ls-obj
        PcepLSObjectVer1 lsOBJ = new PcepLSObjectVer1(lsObjHeader, (byte)1, false, true, 10086L, optionalTlvs);
        //ls-Object set complete

        List<PcepLSObject> ll = new LinkedList<>();
        ll.add(lsOBJ);

        PcepLSReportMsg msg = factory1.buildPcepLSReportMsg().setLSReportList(ll).build();
        channel.write(Collections.singletonList(msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        PcepErrorMsg errMsg;
        log.info("exceptionCaught: " + e.toString());
        log.info("Detail is: {}", e.getCause());

        if (e.getCause() instanceof ReadTimeoutException) {
            if (ChannelState.OPENWAIT == state) {
                // When ReadTimeout timer is expired in OPENWAIT state, it is considered
                // OpenWait timer.
                errMsg = getErrorMsg(PcepErrorDetailInfo.ERROR_TYPE_1, PcepErrorDetailInfo.ERROR_VALUE_2);
                log.info("Sending PCEP-ERROR message to PCE.");
                channel.write(Collections.singletonList(errMsg));
                channel.close();
                state = ChannelState.INIT;
                return;
            } else if (ChannelState.KEEPWAIT == state) {
                // When ReadTimeout timer is expired in KEEPWAIT state, is is considered
                // KeepWait timer.
                errMsg = getErrorMsg(PcepErrorDetailInfo.ERROR_TYPE_1, PcepErrorDetailInfo.ERROR_VALUE_7);
                log.info("Sending PCEP-ERROR message to PCE.");
                channel.write(Collections.singletonList(errMsg));
                channel.close();
                state = ChannelState.INIT;
                return;
            }
        } else if (e.getCause() instanceof ClosedChannelException) {
            //log.debug("Channel for pc {} already closed", getClientInfoString());
        } else if (e.getCause() instanceof IOException) {
            //log.error("Disconnecting client {} due to IO Error: {}", getClientInfoString(), e.getCause().getMessage());
            if (log.isDebugEnabled()) {
                // still print stack trace if debug is enabled
                log.info("StackTrace for previous Exception: ", e.getCause());
            }
            channel.close();
        } else if (e.getCause() instanceof PcepParseException) {
            PcepParseException errMsgParse = (PcepParseException) e.getCause();
            byte errorType = errMsgParse.getErrorType();
            byte errorValue = errMsgParse.getErrorValue();

            if ((errorType == (byte) 0x0) && (errorValue == (byte) 0x0)) {
                //processUnknownMsg();
                //to process Unknown message
            } else {
                errMsg = getErrorMsg(errorType, errorValue);
                log.info("Sending PCEP-ERROR message to PCE.");
                channel.write(Collections.singletonList(errMsg));
            }
        } else if (e.getCause() instanceof RejectedExecutionException) {
            log.warn("Could not process message: queue full");
        } else {
            //log.error("Error while processing message from client " + getClientInfoString() + "state " + this.state);
            channel.close();
        }
    }

    @Override
    public String toString() {
        return "HLK TEST: client info toString!";
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (!isHandshakeComplete()) {
            return;
        }

        if (e.getState() == IdleState.READER_IDLE) {
            // When no message is received on channel for read timeout, then close
            // the channel
            log.info("Disconnecting PCE due to read timeout");
            ctx.getChannel().close();
        } else if (e.getState() == IdleState.WRITER_IDLE) {
            // Send keep alive message
            log.info("Sending keep alive message due to IdleState timeout ");
            sendKeepAliveMessage();//keepAlive request and reply
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (e.getMessage() instanceof List) {
            @SuppressWarnings("unchecked")
            List<PcepMessage> msglist = (List<PcepMessage>) e.getMessage();
            for (PcepMessage pm : msglist) {
                // Do the actual packet processing
                state.processPcepMessage(this, pm);
            }
        } else {
            state.processPcepMessage(this, (PcepMessage) e.getMessage());
        }
    }

    /**
     * To set the handshake status.
     *
     * @param handshakeComplete value is handshake status
     */
    public void setHandshakeComplete(boolean handshakeComplete) {
        this.state.setHandshakeComplete(handshakeComplete);
    }

    /**
     * Is this a state in which the handshake has completed.
     *
     * @return true if the handshake is complete
     */
    public boolean isHandshakeComplete() {
        return this.state.isHandshakeComplete();
    }

    /**
     * Builds pcep error message based on error value and error type.
     *
     * @param errorType  pcep error type
     * @param errorValue pcep error value
     * @return pcep error message
     * @throws PcepParseException while bulding error message
     */
    public PcepErrorMsg getErrorMsg(byte errorType, byte errorValue) throws PcepParseException {
        LinkedList<PcepErrorObject> llerrObj = new LinkedList<>();
        PcepErrorMsg errMsg;

        PcepErrorObject errObj = factory1.buildPcepErrorObject()
                .setErrorValue(errorValue)
                .setErrorType(errorType)
                .build();

        llerrObj.add(errObj);

        if (state == ChannelState.OPENWAIT) {
            //If Error caught in Openmessage
            PcepOpenObject openObj = null;
            ErrorObjListWithOpen errorObjListWithOpen = null;

            if (0 != sessionId) {
                openObj = factory1.buildOpenObject().setSessionId(sessionId).build();
                errorObjListWithOpen = new ErrorObjListWithOpen(llerrObj, openObj);
            } else {
                errorObjListWithOpen = new ErrorObjListWithOpen(llerrObj, null);
            }

            errMsg = factory1.buildPcepErrorMsg()
                    .setErrorObjListWithOpen(errorObjListWithOpen)
                    .build();
        } else {

            //If Error caught in other than Openmessage
            LinkedList<PcepError> llPcepErr = new LinkedList<>();

            PcepError pcepErr = factory1.buildPcepError()
                    .setErrorObjList(llerrObj)
                    .build();

            llPcepErr.add(pcepErr);

            PcepErrorInfo errInfo = factory1.buildPcepErrorInfo()
                    .setPcepErrorList(llPcepErr)
                    .build();

            errMsg = factory1.buildPcepErrorMsg()
                    .setPcepErrorInfo(errInfo)
                    .build();
        }
        return errMsg;
    }

}
