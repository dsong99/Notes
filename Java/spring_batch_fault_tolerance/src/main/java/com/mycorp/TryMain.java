package com.mycorp;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.StringJoiner;

public class TryMain {

    public static void main(String args[]){

        String s ="StatementCallback; SQL [INSERT INTO stock_holdings VALUES (\"1\",\"AACG\",\"ATA CREATIVITY GLOBAL AMERICA\",\"$1.54 \",\"0.06\",\"4.05%\",\"48320457\",\"CHINA\",\"2008\",\"8358\",\"CONSUMER DISCRETIONARY\",\"SERVICE TO THE HEALTH INDUSTRY\")]; Duplicate entry '1' for key 'stock_holdings.PRIMARY'; nested exception is java.sql.BatchUpdateException: Duplicate entry '1' for key 'stock_holdings.PRIMARY'";
        System.out.println(s);
        s=s.replaceAll("\"","\\\\\"");
        System.out.println(s);
        String[] columns = {"STEP", "ERROR", "ITEM"};
        s = String.join("\",\"", columns);
        s = "\""+s+"\"";

        System.out.println(String.join(",", s));
    }
}
