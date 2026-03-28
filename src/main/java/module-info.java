open module banca {
  exports sm.clagenna.banca.javafx to javafx.graphics;
  exports sm.clagenna.banca.sql;
  exports sm.clagenna.banca.dati;
  
  requires java.sql;
  requires transitive java.desktop;
  requires transitive javafx.base;
  requires transitive javafx.fxml;
  requires transitive javafx.web;
  requires transitive javafx.controls;
  requires transitive javafx.graphics;
  
  requires transitive stdc_javafx;
  requires transitive stdc_sql;
  requires transitive stdc_utils;

  requires transitive com.opencsv;

  
  requires lombok;
  requires org.apache.opennlp.tools;
 
}