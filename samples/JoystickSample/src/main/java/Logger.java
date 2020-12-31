import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static void initialize() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(lc);
        fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] \\(%c\\) %msg%ex%n");
        fileEncoder.start();
        PatternLayoutEncoder consoleEncoder = new PatternLayoutEncoder();
        consoleEncoder.setContext(lc);
        consoleEncoder.setPattern("%highlight(%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%t] \\(%c\\) %msg%ex%n)");
        consoleEncoder.start();

        //ファイルログローテート
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setContext(lc);
        rollingFileAppender.setFile("log/"+timestamp+"_00.txt");
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setFileNamePattern("log/"+timestamp+"%2i.txt");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(1000);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setContext(lc);
        rollingPolicy.start();
        SizeBasedTriggeringPolicy<ILoggingEvent> sizePolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
        sizePolicy.setMaxFileSize(FileSize.valueOf("5MB"));
        sizePolicy.setContext(lc);
        sizePolicy.start();
        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(sizePolicy);
        rollingFileAppender.setEncoder(fileEncoder);
        rollingFileAppender.start();

        // Console出力
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(lc);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.DEBUG);
        root.addAppender(rollingFileAppender);
        root.addAppender(consoleAppender);
    }
}
