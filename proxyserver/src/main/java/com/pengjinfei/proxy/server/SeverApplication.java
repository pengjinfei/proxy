package com.pengjinfei.proxy.server;

import com.pengjinfei.proxy.codec.ProxyMessageDecoder;
import com.pengjinfei.proxy.codec.ProxyMessageEncoder;
import com.pengjinfei.proxy.constants.NettyConstant;
import com.pengjinfei.proxy.server.handler.ProxyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
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
        //final String passwd = RandomStringUtils.randomAlphabetic(16);
        String passwd = "PrrukLRXJfwWbMsn";
        log.info("passwd is {}", passwd);
        EventLoopGroup bossGroup = new EpollEventLoopGroup();
        EventLoopGroup workerGroup = new EpollEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(EpollServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ProxyMessageDecoder(NettyConstant.MAX_FRAME_LENGTH,0,NettyConstant.FIELD_LENGTH,passwd.getBytes()))
                                    .addLast(new ProxyMessageEncoder(passwd.getBytes()))
                                    .addLast(new ReadTimeoutHandler(NettyConstant.READ_TIMEOUT))
                                    .addLast(new ProxyServerHandler());
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
