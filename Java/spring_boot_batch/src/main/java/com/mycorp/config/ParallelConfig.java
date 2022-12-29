/**
 * Configuration for a job to be run in multi-threads
 *
 */
package com.mycorp.config;

import com.mycorp.step.GenericProcessor;
import com.mycorp.step.GenericReader;
import com.mycorp.step.GenericWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ParallelConfig {


    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    JdbcTemplate mysqlJdbcTemplate;

    @Bean
    public Job importStocksParallel() {
        return jobBuilderFactory.get("importStocksParallel")
                .incrementer(new RunIdIncrementer())
                .flow(readWriteStocksParallel()).end().build();
    }


    @Bean
    public Step readWriteStocksParallel() {
        return stepBuilderFactory.get("readWriterStocksParallel")
                 .<String, String> chunk(100)
                .reader(new GenericReader()).processor(new GenericProcessor())
                .writer(new GenericWriter(mysqlJdbcTemplate))
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        return threadPoolTaskExecutor;
    }

    //
    // SimpleAsyncTaskExecutor:
    // This starts a new thread and executes it asynchronously.
    // It does not reuse the thread
    //
    @Bean
    public TaskExecutor asyncTaskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(5);
        return asyncTaskExecutor;
    }
}
