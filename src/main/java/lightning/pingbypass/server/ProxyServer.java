package lightning.pingbypass.server;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import lightning.pingbypass.PingBypassConfig;
import lightning.pingbypass.handler.PbHandshakeHandler;
import lightning.pingbypass.protocol.PbProtocolHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyServer {
    private static final Logger LOGGER;
    private static final ProxyServer.Lazy SERVER_NIO_EVENTLOOP;
    private static final ProxyServer.Lazy SERVER_EPOLL_EVENTLOOP;
    private final List endpoints;
    private final List connections;
    private final PingBypassConfig config;
    private final PbProtocolHandler protocolHandler;
    private volatile boolean alive;
    private volatile boolean stayConnected;
    private volatile Connection serverConnection;
    private final RegistryCache registryCache;

    public ProxyServer(PingBypassConfig arg0) {
        this.endpoints = Collections.synchronizedList(Lists.newArrayList());
        this.connections = Collections.synchronizedList(Lists.newArrayList());
        this.config = arg0;
        this.alive = true;
        this.protocolHandler = new PbProtocolHandler();
        this.registryCache = new RegistryCache();
        this.protocolHandler.registerStayHandler(this);
    }

    public void bind(InetAddress arg0, int arg1) throws IOException {
        LOGGER.info("PingBypass proxy binding to {}:{}", arg0, arg1);
        Object var3 = this.endpoints;
        // synchronized (this.endpoints)
        if (Epoll.isAvailable()) {
            Object var4 = EpollServerSocketChannel.class;
            Object var5 = ((EventLoopGroup) SERVER_EPOLL_EVENTLOOP.get());
            LOGGER.info("Using epoll channel type");
        } else {
            var4 = NioServerSocketChannel.class;
            var5 = ((EventLoopGroup) SERVER_NIO_EVENTLOOP.get());
            LOGGER.info("Using default NIO channel type");
        }
        this.endpoints.add(((ServerBootstrap) ((ServerBootstrap) new ServerBootstrap().channel(var4)).childHandler(new ProxyServer.1(this)).group(var5).localAddress(arg0, arg1)).bind().syncUninterruptibly());
        LOGGER.info("PingBypass proxy is now listening on {}:{}", arg0, arg1);
        break label_6;
        Object var6 = null;
        throw var6;
        label_6:
    }

    public void tick() {
        Object var1 = this.connections;
        Object var2 = var1;
        // synchronized (var1)
        Object var3 = this.connections.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Connection) var3.next());
            if (!(var4.isConnected())) {
                var3.remove();
                var4.handleDisconnection();
                break label_1;
                try {
                }
                var4.tick();
            } catch (Exception ex4) {
                Object var5 = ex4;
                LOGGER.warn("Failed to handle packet for {}", var4.getLoggableAddress(false), var5);
                var4.send(new ClientboundDisconnectPacket(Component.literal("Internal server error")));
                var4.disconnect(Component.literal("Internal server error"));
            }
        }
        break label_10;
        Object var6 = null;
        throw var6;
        label_10:
    }

    public void shutdown() {
        this.alive = false;
        LOGGER.info("Shutting down PingBypass proxy server...");
        Object var1 = this.endpoints.iterator();
        while (var1.hasNext()) {
            Object var2 = ((ChannelFuture) var1.next());
            try {
                var2.channel().close().sync();
            } catch (InterruptedException ex3) {
                Object var3 = ex3;
                LOGGER.error("Interrupted whilst closing channel");
            }
        }
        this.endpoints.clear();
        var1 = this.connections;
        var2 = var1;
        // synchronized (var1)
        var3 = this.connections.iterator();
        while (var3.hasNext()) {
            Object var4 = ((Connection) var3.next());
            var4.disconnect(Component.literal("Proxy server shutting down"));
        }
        this.connections.clear();
        break label_12;
        Object var5 = null;
        throw var5;
        label_12:
        LOGGER.info("PingBypass proxy server shut down.");
    }

    public boolean isAlive() {
        return this.alive;
    }

    public List getConnections() {
        return this.connections;
    }

    public List getEndpoints() {
        return this.endpoints;
    }

    public PingBypassConfig getConfig() {
        return this.config;
    }

    public PbProtocolHandler getProtocolHandler() {
        return this.protocolHandler;
    }

    public boolean isStayConnected() {
        return this.stayConnected;
    }

    public void setStayConnected(boolean arg0) {
        this.stayConnected = arg0;
    }

    public Connection getServerConnection() {
        return this.serverConnection;
    }

    public void setServerConnection(Connection arg0) {
        this.serverConnection = arg0;
    }

    public RegistryCache getRegistryCache() {
        return this.registryCache;
    }

    static {
        LOGGER = LoggerFactory.getLogger(ProxyServer.class);
        SERVER_NIO_EVENTLOOP = new ProxyServer.Lazy(() -> new NioEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Server IO #%d").setDaemon(true).build()));
        SERVER_EPOLL_EVENTLOOP = new ProxyServer.Lazy(() -> new EpollEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("PingBypass Epoll Server IO #%d").setDaemon(true).build()));
    }


    class Lazy {
        private final Supplier supplier;
        private volatile Object value;

        Lazy(Supplier arg0) {
            this.supplier = arg0;
        }

        Object get() {
            if (this.value != null) break label_5;
            Object var1 = this;
            Object var2 = var1;
            // synchronized (var1)
            if (this.value == null) {
                this.value = this.supplier.get();
            }
            break label_5;
            Object var3 = null;
            throw var3;
            label_5:
            return this.value;
        }

    }

    class 1 extends ChannelInitializer {

        1(ProxyServer arg0) {
            Objects.requireNonNull(arg0);
            this.this$0 = arg0;
        }

        protected void initChannel(Channel arg0) {
            try {
                arg0.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
            } catch (ChannelException ex2) {
                Object var2 = ex2;
            }
            var2 = arg0.pipeline().addLast("timeout", new ReadTimeoutHandler(30));
            Connection.configureSerialization(var2, PacketFlow.SERVERBOUND, false, null);
            Object var3 = new PbSession();
            this.this$0.connections.add(var3);
            var3.configurePacketHandler(var2);
            var3.setListenerForServerboundHandshake(new PbHandshakeHandler(this.this$0, var3));
        }

    }
}
