package com.mycorp.step;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.*;
import java.net.URL;

public class GenericReader implements ItemReader<String> {

	BufferedReader br;
	int skip_line;
	int count;

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		//
		// get some job parameters
		//
		JobParameters jobParameters = stepExecution.getJobExecution().getJobParameters();
		String fileName = (String)jobParameters.getString("fileName");
		skip_line = 1;
		count = 0;

		System.out.println("in Reader before step, job param fileName=" + fileName);
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		File file = new File(resource.getFile());
		System.out.println(file.getPath());
		System.out.println(String.format("File %s exist:%s",fileName, file.exists()));
		try {
			br = new BufferedReader(new FileReader(file));
			for(int i=0; i<skip_line;i++){
				System.out.println("Skip:"+br.readLine());
			}


		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//
	// Since we use new operator to create a new reader in job configuration, sync is unnecessary
	// otherwise, try to set bean cope to prototype, in this way, it will create a new bean
	//
	@Override
	public synchronized String read() throws Exception, UnexpectedInputException,
			ParseException, NonTransientResourceException {

		long threadId = Thread.currentThread().getId();

		String sCurrentLine;
		while ((sCurrentLine = br.readLine()) != null){
			count++;

			System.out.println(String.format("Thread %d: reading: %s", threadId,  sCurrentLine));
			return sCurrentLine;
		}

		return null;
	}

}