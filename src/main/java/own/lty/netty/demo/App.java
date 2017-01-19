package own.lty.netty.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import own.lty.netty.demo.handlers.PrintStringHandler;

/**
 * Hello world!
 *
 */
public class App {
	
	private static final NioEventLoopGroup m_bossGroup = new NioEventLoopGroup();
    private static final NioEventLoopGroup m_workerGroup = new NioEventLoopGroup();
    
    private static final EventExecutorGroup eventHandlerGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors()*4);
    
    private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		
		System.out.println("bind service to "+args[0]+":"+args[1]);
		
		String host = args[0];
        int port = Integer.parseInt(args[1]);
		
		ServerBootstrap b = new ServerBootstrap();
        b.group(m_bossGroup, m_workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        try {
                        	pipeline.addFirst("idleStateHandler", new IdleStateHandler(0, 0, 600));
                        	pipeline.addAfter("idleStateHandler", "strDecode", new StringDecoder());
                            pipeline.addLast(eventHandlerGroup, "handler", new PrintStringHandler());
                            pipeline.addAfter("handler", "strEncode", new StringEncoder());
                        } catch (Throwable th) {
                            LOG.error("Severe error during pipeline creation", th);
                        }
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 32*1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 1024*1024)
                //.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                ;
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture = b.bind(host, port).sync();;
            LOG.info("Server binded host: {}, port: {}", host, port);
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();  
            closeFuture.addListener(ChannelFutureListener.CLOSE);  
        } catch (InterruptedException ex) {
            LOG.error(null, ex);
        } finally{
//        	m_bossGroup.shutdownGracefully();
//        	m_workerGroup.shutdownGracefully();
        }
	}
	
}
