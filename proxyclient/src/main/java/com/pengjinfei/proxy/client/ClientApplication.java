package com.pengjinfei.proxy.client;

import com.pengjinfei.proxy.client.configuration.ExhaustedTimer;
import com.pengjinfei.proxy.client.configuration.ProxyConfiguration;
import com.pengjinfei.proxy.client.handler.ProxyClientHandler;
import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import com.pengjinfei.proxy.constants.NettyConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@SpringBootApplication
@Slf4j
public class ClientApplication implements CommandLineRunner {

	@Autowired
	private ProxyConfiguration configuration;

	private ExhaustedTimer timer = new ExhaustedTimer(1, TimeUnit.MINUTES);

	@Value("${proxy.maxTry}")
	private int maxRetry;

	@Override
	public void run(String... strings) throws Exception {
		if (maxRetry == 0) {
			maxRetry = Integer.MAX_VALUE;
		}
		log.info("max try:{}", maxRetry);
		EventLoopGroup group = new EpollEventLoopGroup();
		connect(group);
	}

	private void connect(EventLoopGroup group) {
		Exception exception = null;
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(EpollSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline()
									.addLast(new ProxyMessageDecoder(NettyConstant.MAX_FRAME_LENGTH, 0, NettyConstant.FIELD_LENGTH, configuration.getPasswd().getBytes()))
									.addLast(new ProxyMessageEncoder(configuration.getPasswd().getBytes()))
									.addLast(new IdleStateHandler(NettyConstant.PROXY_READ_TIMEOUT, NettyConstant.PROXY_WRITE_TIMEOUT,0))
									.addLast(new ProxyClientHandler(configuration.getMapping()));
						}
					});
			ChannelFuture channelFuture = bootstrap.connect(configuration.getIp(), configuration.getPort()).sync();
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			exception = e;
			log.error("Error occurred in bootstrap.", e);
		} finally {
			int i = timer.incrementAndGet();
			if (i >= maxRetry) {
				log.error("server closed due to exausted times");
				group.shutdownGracefully();
			} else if (exception instanceof InterruptedException) {
				log.error("server closed due to interrupted.");
				group.shutdownGracefully();
			} else {
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					group.shutdownGracefully();
				}
				log.info("start to reconnect {} times", i);
				connect(group);
			}
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
