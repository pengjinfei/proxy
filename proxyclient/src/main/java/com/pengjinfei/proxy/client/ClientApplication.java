package com.pengjinfei.proxy.client;

import com.pengjinfei.proxy.client.configuration.PortMapping;
import com.pengjinfei.proxy.client.configuration.ProxyConfiguration;
import com.pengjinfei.proxy.client.handler.ProxyClientHandler;
import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import com.pengjinfei.proxy.message.ConnectReq;
import com.pengjinfei.proxy.message.MessageType;
import com.pengjinfei.proxy.message.ProxyMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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
import java.util.LinkedList;
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
						ChannelPipeline pipeline = socketChannel.pipeline();
						pipeline.addLast(new ProxyMessageDecoder(1024,4,4,passwd.getBytes()));
						pipeline.addLast(new ProxyMessageEncoder(passwd.getBytes()));
						pipeline.addLast(new ProxyClientHandler(portMap));
					}
				});
		ChannelFuture channelFuture = bootstrap.connect(configuration.getIp(), configuration.getPort()).sync();
		channelFuture.addListener((ChannelFutureListener) future -> {
			ProxyMessage<ConnectReq> msg = new ProxyMessage<>();
			msg.setMessageType(MessageType.CONNECT_REQ);
			ConnectReq connectReq = new ConnectReq();
			List<Integer> portList = new LinkedList<>();
			for (Map.Entry<Integer, SocketAddress> entry : portMap.entrySet()) {
				portList.add(entry.getKey());
			}
			connectReq.setPortList(portList);
			msg.setBody(connectReq);
			future.channel().writeAndFlush(msg);
		});
		channelFuture.channel().closeFuture().sync();
	}

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}
}
