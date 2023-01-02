package com.mycorp.config;

import com.mycorp.listener.StepSkipListener;
import com.mycorp.step.Validation;
import javafx.concurrent.Task;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mycorp.listener.JobCompletionListener;
import com.mycorp.step.GenericProcessor;
import com.mycorp.step.GenericReader;
import com.mycorp.step.GenericWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URL;
import java.io.File;

@Configuration
public class BatchConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	JdbcTemplate mysqlJdbcTemplate;

	@Bean
	public Job importStocks() {
		return jobBuilderFactory.get("importStocks")
				.incrementer(new RunIdIncrementer()).listener(listener())
				.flow(readWriteStocks())
				.next(validation())
				.end().build();
	}

	@Bean
	public Step readWriteStocks() {
		return stepBuilderFactory.get("readWriteStocks")
				.<String, String> chunk(1)
				.reader(new GenericReader())
				.processor(new GenericProcessor())
				.writer(new GenericWriter(mysqlJdbcTemplate))
				.faultTolerant()
				.skipLimit(100)
				.listener(skipListener())
				.skip(Throwable.class) //skip all failures
				.build();
	}

	@Bean
	public SkipListener skipListener(){
		return new StepSkipListener(mysqlJdbcTemplate);
	}

	@Bean
	public Step validation() {
		return stepBuilderFactory.get("validation")
				.tasklet(new Validation(mysqlJdbcTemplate))
				.build();
	}

	@Bean
	public JobExecutionListener listener() {
		return new JobCompletionListener();
	}

	@Bean
	StepExecutionListener stepExecutionListener(){
		return new StepExecutionListener() {
			@Override
			public void beforeStep(StepExecution stepExecution) {
				String fileName = (String) stepExecution.getJobExecution().getJobParameters()
						.getString("fileName");
				System.out.println("in STEP listener, job param fileName=" + fileName);
				ClassLoader classLoader = getClass().getClassLoader();
				URL resource = classLoader.getResource(fileName);
				File file = new File(resource.getFile());
				System.out.println(file.getPath());
				System.out.println(String.format("File %s exist:%s",fileName, file.exists()));

			}

			@Override
			public ExitStatus afterStep(StepExecution stepExecution) {
				System.out.println("in STEP listener, jafterStep");
				if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
					return ExitStatus.COMPLETED;
				}
				return ExitStatus.FAILED;
			}
		};
	}


	@Bean
	@StepScope
	public ItemReadListener chunkItemReadListener(final @Value("#{jobParameters['name']}") String name) {

		return new ItemListenerSupport() {
			@Override
			public void beforeRead() {
				System.out.println("in listener, job param name=" + name);
				super.beforeRead();
			}
		};
	}

}
