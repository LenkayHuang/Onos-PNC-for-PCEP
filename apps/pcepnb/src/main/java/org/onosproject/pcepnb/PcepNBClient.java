package org.onosproject.pcepnb;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.onosproject.pcep.controller.driver.PcepAgent;
import org.onosproject.pcepio.protocol.PcepFactories;
import org.onosproject.pcepio.protocol.PcepFactory;
import org.onosproject.pcepio.protocol.PcepVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * The main controller class. Handles all setup and network listeners -
 * Distributed ownership control of pcc through IControllerRegistryService
 */
public class PcepNBClient {

    private static final Logger log = LoggerFactory.getLogger(PcepNBClient.class);

    private static final PcepFactory FACTORY1 = PcepFactories.getFactory(PcepVersion.PCEP_1);

    private ChannelGroup cg;

    // Configuration options
    private int pcepPort = 4189;
    private int workerThreads = 10;
    private String host = "192.168.1.50";

    // Start time of the controller
    private long systemStartTime;

    private PcepAgent agent;

    private NioClientSocketChannelFactory execFactory;

    // Perf. related configuration
    private static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    /**
     * Returns factory version for processing pcep messages.
     *
     * @return instance of factory version
     */
    public PcepFactory getPcepMessageFactory1() {
        return FACTORY1;
    }

    /**
     * To get system start time.
     *
     * @return system start time in milliseconds
     */
    public long getSystemStartTime() {
        return (this.systemStartTime);
    }

    /**
     * Tell controller that we're ready to accept pcc connections.
     */
    public void run() {
        try {
            log.info("start to run");
            ClientBootstrap bootstrap = createClientBootStrap();

            bootstrap.setOption("reuseAddr", true);
            bootstrap.setOption("child.keepAlive", true);
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.sendBufferSize", PcepNBClient.SEND_BUFFER_SIZE);

            log.info("start to new pipelineFac");
            ChannelPipelineFactory pfact = new PcepNBPipelineFactory(this);

            bootstrap.setPipelineFactory(pfact);
            log.info("start to connect");
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, pcepPort)).sync();
            log.info("connection future done");
            future.getChannel().getCloseFuture().awaitUninterruptibly();
            bootstrap.releaseExternalResources();
            log.info("Connect to PCE connection on {}", future);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates client boot strap.
     *
     * @return ClientBootStrap
     */
    private ClientBootstrap createClientBootStrap() {
        execFactory = new NioClientSocketChannelFactory(
                Executors.newCachedThreadPool(groupedThreads("onos/pcep", "clientBoss-%d")),
                Executors.newCachedThreadPool(groupedThreads("onos/pcep", "clientWorker-%d")));
        return new ClientBootstrap(execFactory);
    }

    /**
     * Initialize internal data structures.
     */
    public void init() {
        // These data structures are initialized here because other
        // module's startUp() might be called before ours
        this.systemStartTime = System.currentTimeMillis();
    }

    public Map<String, Long> getMemory() {
        Map<String, Long> m = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        m.put("total", runtime.totalMemory());
        m.put("free", runtime.freeMemory());
        return m;
    }

    public Long getUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        return rb.getUptime();
    }

    /**
     * Creates instance of Pcep client.
     *
     * @param pccId pcc identifier
     * @param sessionID session id
     * @param pv pcep version
     * @param pktStats pcep packet statistics
     * @return instance of PcepClient
     */
    /*protected PcepClientDriver getPcepClientInstance(PccId pccId, int sessionID, PcepVersion pv,
                                                     PcepPacketStats pktStats) {
        PcepClientDriver pcepClientDriver = new PcepClientImpl();
        pcepClientDriver.init(pccId, pv, pktStats);
        pcepClientDriver.setAgent(agent);
        return pcepClientDriver;
    }*/

    /**
     * Starts the pcep controller.
     */
    public void start() {
        log.info("PcepNB client Started!");
        this.init();
        this.run();
    }

    /**
     * Stops the pcep controller.
     */
    public void stop() {
        log.info("PcepNB client Stopped");
        execFactory.shutdown();
        //cg.close();
    }
}
