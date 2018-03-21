package com.pengjinfei.proxy.client;

import com.pengjinfei.proxy.client.configuration.ProxyConfiguration;
import com.pengjinfei.proxy.client.handler.ProxyClientHandler;
import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import com.pengjinfei.proxy.constants.NettyConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@SpringBootApplication
@Slf4j
public class ClientApplication implements CommandLineRunner {

	@Autowired
	private ProxyConfiguration configuration;

	private AtomicInteger retryTimes = new AtomicInteger(0);

	private static final int MAX_RETRY = 5;

	@Override
	public void run(String... strings) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		connect(group);
	}

	private void connect(EventLoopGroup group) {
		Exception exception = null;
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline()
									.addLast(new ProxyMessageDecoder(NettyConstant.MAX_FRAME_LENGTH, 0, NettyConstant.FIELD_LENGTH, configuration.getPasswd().getBytes()))
									.addLast(new ProxyMessageEncoder(configuration.getPasswd().getBytes()))
									.addLast(new IdleStateHandler(0, 11,0))
									.addLast(new ProxyClientHandler(configuration.getMapping()));
						}
					});
			ChannelFuture channelFuture = bootstrap.connect(configuration.getIp(), configuration.getPort()).sync();
			channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			exception = e;
			log.error("Error occurred in bootstrap.", e);
		} finally {
			int i = retryTimes.incrementAndGet();
			if (i >= MAX_RETRY) {
				group.shutdownGracefully();
			} else if (exception != null && exception instanceof InterruptedException) {
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
