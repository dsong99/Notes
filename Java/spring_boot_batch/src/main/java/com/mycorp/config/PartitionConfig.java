/**
 * Configuration for a job with multi-thread and partition input
 */
package com.mycorp.config;

import com.mycorp.step.GenericProcessor;
import com.mycorp.step.GenericReader;
import com.mycorp.step.GenericWriter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PartitionConfig {


    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    JdbcTemplate mysqlJdbcTemplate;

    public static int total_lines=0;

    @Bean
    public Job importStocksPartition() {
        return jobBuilderFactory.get("importStocksPartition")
                .incrementer(new RunIdIncrementer()).listener(new JobListener())
                .flow(masterStep()).end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4); // My PC has 4 CPU
        return threadPoolTaskExecutor;
    }

    @Bean
    public InputRangePartitioner partitioner() {
        return new InputRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        //
        // Grid Size:
        // number of data blocks to create to be processed by workers
        // will be passed to partitioner
        //
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep());
        return taskExecutorPartitionHandler;
    }

    @Bean
    public Step slaveStep() {
        //
        // no change on slave step, same as a normal chunk process
        //
        return stepBuilderFactory.get("slaveStep").<String, String>chunk(20)
                .reader(new GenericReader())
                .processor(new GenericProcessor())
                .writer(new GenericWriter(mysqlJdbcTemplate))
                .build();
    }

    @Bean
    public Step masterStep() {
        //
        // master step sets slave name, partitioner, partition handler
        //
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }


    class InputRangePartitioner implements Partitioner {
        @BeforeStep
        public void beforeStep(StepExecution stepExecution) {

        }
        @Override
        public Map<String, ExecutionContext> partition(int gridSize) {


            System.out.println("Partitioner gridSize: " + gridSize);
            int min = 1;
            //int max = 100;
            int max = PartitionConfig.total_lines;
            System.out.println("Partitioner max: " + max);
            int targetSize = (max - min) / gridSize + 1;
            System.out.println("targetSize : " + targetSize);
            Map<String, ExecutionContext> result = new HashMap<>();

            int number = 0;
            int start = min;
            int end = start + targetSize - 1;
            //1 to 500
            // 501 to 1000
            while (start <= max) {
                ExecutionContext value = new ExecutionContext();
                result.put("partition" + number, value);

                if (end >= max) {
                    end = max;
                }
                value.putInt("minValue", start);
                value.putInt("maxValue", end);
                start += targetSize;
                end += targetSize;
                number++;
            }
            System.out.println("partition result:" + result);
            return result;
        }
    }


    public class JobListener extends JobExecutionListenerSupport {

        @Override
        public void beforeJob(JobExecution jobExecution) {
            //
            // determine total lines
            //
            JobParameters jobParameters = jobExecution.getJobParameters();
            String fileName = jobParameters.getString("fileName");
            long skip_line = jobParameters.getLong("skip_line");
            int count=0;

            System.out.println("in beforeJob, job param fileName=" + fileName);
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource(fileName);
            File file = new File(resource.getFile());
            System.out.println(file.getPath());
            System.out.printf("File %s exist:%s%n", fileName, file.exists());
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                for (int i = 0; i < skip_line; i++) {
                    System.out.println("Skip:" + br.readLine());
                }
                while (( br.readLine()) != null){
                    count++;
                }
                System.out.printf("File %s total lines:%d%n", fileName, count);
                jobExecution.getExecutionContext().putInt("total_lines",count);

                //Partitioner cannot get execution context, need to pass it throw a structure.
                PartitionConfig.total_lines=count;

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                System.out.println("BATCH JOB COMPLETED SUCCESSFULLY");
            }
        }

    }

}
