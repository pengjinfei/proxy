package com.pengjinfei.proxy.client;

import com.pengjinfei.proxy.client.configuration.PortMapping;
import com.pengjinfei.proxy.client.configuration.ProxyConfiguration;
import com.pengjinfei.proxy.client.handler.ProxyClientHandler;
import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@SpringBootApplication
public class ClientApplication implements CommandLineRunner {

	@Autowired
	private ProxyConfiguration configuration;

	@Override
	public void run(String... strings) throws Exception {
		List<PortMapping> mapping = configuration.getMapping();
		String passwd = configuration.getPasswd();
		Map<Integer, SocketAddress> portMap = new HashMap<>(16);
		for (PortMapping portMapping : mapping) {
			portMap.put(portMapping.getFacadePort(), new InetSocketAddress(portMapping.getRealIp(), portMapping.getRealPort()));
		}
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ProxyMessageDecoder(1024*1024,0,4,passwd.getBytes()))
                                .addLast(new ProxyMessageEncoder(passwd.getBytes()))
                                .addLast(new ProxyClientHandler(portMap));
					}
				});
		ChannelFuture channelFuture = bootstrap.connect(configuration.getIp(), configuration.getPort()).sync();
		channelFuture.channel().closeFuture().sync();
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
