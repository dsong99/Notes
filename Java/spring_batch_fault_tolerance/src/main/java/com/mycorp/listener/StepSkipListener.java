package com.mycorp.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.jdbc.core.JdbcTemplate;

public class StepSkipListener implements SkipListener<String, String> {

    Logger logger = LoggerFactory.getLogger(StepSkipListener.class);
    JdbcTemplate jdbcTemplate;
    String error_table = "SKIPPED_ERRORS";
    int column_limit = 1000;
    String[] columns = {"STEP", "ERROR", "ITEM"};
    String column_names ;

    public StepSkipListener(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
        column_names = String.join("`,`", columns);
        column_names = "`"+column_names+"`";
    }

    private void insertError(String step, String error, String item){

        error =error.replaceAll("\"","\\\\\"");
        item =item.replaceAll("\"","\\\\\"");

        String insertString = String.format("insert into %s (%s) values (\"%s\",\"%s\",\"%s\") ", error_table, column_names,step,error,item);
        System.out.println(insertString);
        this.jdbcTemplate.update(insertString);
    }

    private String truncate(String s, int len){

        if (s.length() > len)
            s = s.substring(0, len - 1);

        return s;
    }

    @Override
    public void onSkipInRead(Throwable throwable) {

        logger.info("A failure on read {} ", throwable.getMessage());

        if(throwable instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) throwable;
            String item = ffpe.getInput();
            item = truncate(item, column_limit);

            String msg = throwable.getMessage();
            msg = truncate(msg,column_limit);

            insertError("reader", msg, item);
        }
    }

    @Override
    public void onSkipInWrite(String item, Throwable throwable) {
        logger.info("A failure on write {} , {}", throwable.getMessage(), item);

        item = truncate(item, column_limit);
        String msg = throwable.getMessage();
        msg = truncate(msg,column_limit);

        insertError("writer", msg,item);

    }


    @Override
    public void onSkipInProcess(String item, Throwable throwable) {
        logger.info("Item {}  was skipped due to the exception  {}", item, throwable.getMessage());
        item = truncate(item, column_limit);
        String msg = throwable.getMessage();
        msg = truncate(msg,column_limit);

        insertError("writer", msg,item);

    }


}