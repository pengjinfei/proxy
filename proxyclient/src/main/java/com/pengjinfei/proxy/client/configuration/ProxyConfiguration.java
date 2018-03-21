package com.pengjinfei.proxy.client.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@Component
@ConfigurationProperties(prefix = "proxy")
@Data
public class ProxyConfiguration {
	private String  ip;
	private Integer port;
	private String passwd;
	private List<PortMapping> mapping;
}
