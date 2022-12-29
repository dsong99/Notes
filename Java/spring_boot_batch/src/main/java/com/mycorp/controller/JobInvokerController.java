package com.mycorp.controller;
 
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/jobs")
public class JobInvokerController {
 
    @Autowired
    JobLauncher jobLauncher;
 
    @Autowired
    Job importStocks;

    @Autowired
    Job importStocksParallel;

    @Autowired
    Job importStocksPartition;

    @RequestMapping("/importStocks")
    public String importStocks() throws Exception {

        //
        // hardcode some parameters, can be passed in from url
        //
        String fileName = "input/current_holdings.csv";

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("fileName", fileName)
                .toJobParameters();
        jobLauncher.run(importStocks, jobParameters);

        return "Batch job has been invoked";
    }

    /**
     *  load data in multi-threads
     *
     */
    @RequestMapping("/importStocksParallel")
    public String importStocksParallel() throws Exception {
        System.out.println("importStocksParallel...");

        //
        // hardcode some parameters, can be passed in from url
        //
        String fileName = "input/nasdaq_listings.csv";

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("fileName", fileName)
                .toJobParameters();
        jobLauncher.run(importStocksParallel, jobParameters);

        return "Batch job has been invoked";
    }

    /**
     *  load data with multi-thread and input partition
     *
     */
    @RequestMapping("/importStocksPartition")
    public String importStocksPartition() throws Exception {

        //
        // hardcode some parameters, can be passed in from url
        //
        String fileName = "input/nasdaq_listings.csv";

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("fileName", fileName)
                .addLong("skip_line",1L)
                .toJobParameters();
        jobLauncher.run(importStocksPartition, jobParameters);

        return "Batch job has been invoked";
    }
}