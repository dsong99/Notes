package com.mycorp.step;

import java.util.List;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

public class GenericWriter implements ItemWriter<String> {


	JdbcTemplate mysqlJdbcTemplate;

	private String table_name;

	private String[] header;
	private int header_length= 0;

	public GenericWriter(JdbcTemplate mysqlJdbcTemplate){
		this.mysqlJdbcTemplate = mysqlJdbcTemplate;
	}

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		//
		// get some job parameters, hardcoded for demo
		//
		JobParameters jobParameters = stepExecution.getJobExecution().getJobParameters();
		String outfileName = (String) jobParameters.getString("outfileName");
		this.table_name="stock_holdings";
		String[] table_header = {"ID","SYMBOL","NAME","LASTSALE","NETCHANGE","PERCENTCHANGE","MARKETCAP","COUNTRY","IPOYEAR","VOLUME","SECTOR","INDUSTRY"};
		this.header= table_header;
		header_length = this.header.length;
	}

	@Override
	public void write(List<? extends String> messages) throws Exception {
		long threadId = Thread.currentThread().getId();

		for (String msg : messages) {

			String[] msg_split = msg.split(",");

			if(msg_split.length!=this.header_length){
				System.out.println("Error: "+msg);
				continue;
			}
			StringBuilder msgBuilder= new StringBuilder();

			for (String s:msg_split){
				msgBuilder.append(String.format("\"%s\",", s));
			}
			msgBuilder.deleteCharAt(msgBuilder.length()-1);

			String insert_string = String.format("INSERT INTO %s VALUES (%s)",table_name, msgBuilder.toString());

			System.out.println(String.format("Thread %d: insert string: %s", threadId,  insert_string));
			mysqlJdbcTemplate.batchUpdate(insert_string);
		}
	}

}