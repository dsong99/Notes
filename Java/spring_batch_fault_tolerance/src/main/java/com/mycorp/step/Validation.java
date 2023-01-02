package com.mycorp.step;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class Validation implements Tasklet {

    JdbcTemplate jdbcTemplate;
    public Validation(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters();
        //Assume get parameters, hardcoded now

        String table_name="stock_holdings";
        String sql = "select count(*) from " + table_name; // should use time stamp and run id, ignore for now


        int total_read = (int)chunkContext.getStepContext().getJobExecutionContext().get("total_read");
        System.out.printf("total_read %s\n",total_read);
        int total_insert = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.printf("total_insert %s\n",total_insert);

        if(total_read!=total_insert){
            System.out.println("Mark the job to fail");
            chunkContext.getStepContext().getStepExecution().setStatus(BatchStatus.FAILED);

            //
            // WTH, need to raise an  exception to fail the job???
            //
            throw new RuntimeException("total read and write records are not matched");
        }
        return null;
    }
}
