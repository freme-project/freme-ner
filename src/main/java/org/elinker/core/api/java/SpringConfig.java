package org.elinker.core.api.java;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

	@Bean
	public Config getConfig(){
		return new Config();
	}
	
	@Bean
	public FremeNer getFremeNer(){
		return new FremeNer();
	}
}
