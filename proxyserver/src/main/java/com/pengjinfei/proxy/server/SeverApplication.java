package com.pengjinfei.proxy.server;

import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import com.pengjinfei.proxy.server.handler.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created on 3/17/18
 *
 * @author Pengjinfei
 */
@SpringBootApplication
@Slf4j
public class SeverApplication implements CommandLineRunner{

    @Value("${proxy.port}")
    int port;

    public static void main(String[] args) {
        SpringApplication.run(SeverApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final String passwd = RandomStringUtils.randomAlphabetic(16);
        log.info("passwd is {}", passwd);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new ProxyMessageDecoder(1024,4,4,passwd.getBytes()));
                            pipeline.addLast(new ProxyMessageEncoder(passwd.getBytes()));
                            pipeline.addLast(new ProxyServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
