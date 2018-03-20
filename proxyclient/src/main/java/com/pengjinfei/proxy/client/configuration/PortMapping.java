package com.pengjinfei.proxy.client.configuration;

import lombok.Data;

/**
 * @author PENGJINFEI533
 * @since 2018-03-20
 */
@Data
public class PortMapping {
	private Integer facadePort;
	private Integer realPort;
	private String realIp;
}
