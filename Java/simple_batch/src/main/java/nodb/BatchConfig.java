package nodb;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

	@Bean
	ResourcelessTransactionManager transactionManager(){
		return new ResourcelessTransactionManager();
	}

	@Bean
	JobRepository jobRepository(){
		try {
			return new MapJobRepositoryFactoryBean(transactionManager()).getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Bean(name = "jobLauncher")
	SimpleJobLauncher jobLauncher(){
		SimpleJobLauncher jobLauncher =  new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository());
		return jobLauncher;
	}

	@Bean
	Job helloWorldJob(){

		JobBuilderFactory factory = new JobBuilderFactory(jobRepository());

		JobBuilder builder = factory.get("helloWorldJob");
		return builder.flow(step1()).end().build();

	}
	@Bean
	public Step step1() {
		StepBuilderFactory stepFactory = new StepBuilderFactory(jobRepository(), transactionManager());
		return stepFactory.get("step1").tasklet(tasklet()).build();
	}

	@Bean
	MyTasklet tasklet(){
		return new MyTasklet();
	}

}
