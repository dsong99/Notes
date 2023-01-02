package com.mycorp.step;

import org.springframework.batch.item.ItemProcessor;

public class GenericProcessor implements ItemProcessor<String, String> {

	@Override
	public String process(String data) throws Exception {
		long threadId = Thread.currentThread().getId();
		System.out.println(String.format("Thread %d: processor %s: Processing data: %s", threadId, this.toString(), data));
		return data.toUpperCase();
	}

}
