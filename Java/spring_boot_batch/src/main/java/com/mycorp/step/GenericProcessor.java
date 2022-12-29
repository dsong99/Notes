package com.mycorp.step;

import org.springframework.batch.item.ItemProcessor;

public class GenericProcessor implements ItemProcessor<String, String> {

	@Override
	public String process(String data) throws Exception {
		System.out.println("Processing data:"+data);
		return data.toUpperCase();
	}

}
