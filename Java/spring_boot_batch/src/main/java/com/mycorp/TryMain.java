package com.mycorp;

import java.util.StringJoiner;

public class TryMain {

    public static void main(String args[]){

        String[] header={"SYMBOL","NAME","LASTSALE","NETCHANGE","PERCENTCHANGE","MARKETCAP","COUNTRY","IPOYEAR","VOLUME","SECTOR","INDUSTRY"};

        String table_name="stock_holdings";
        //String msg = "msg";
        String msg="Symbol,Name,LastSale,NetChange,PercentChange,MarketCap,Country,IPOYear,Volume,Sector,Industry";

        String[] msg_split = msg.split(",");
        StringBuilder msgBuilder= new StringBuilder();

        for (String s:msg_split){
            msgBuilder.append(String.format("\"%s\",", s));
        }
        msgBuilder.deleteCharAt(msgBuilder.length()-1);

        String insert_string = String.format("INSERT INTO %s VALUES (%s)",table_name, msgBuilder.toString());
        System.out.println("insert string: " + insert_string);
    }
}
