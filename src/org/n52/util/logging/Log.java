/*
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.util.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class provides functionality for setting up loggers.
 * 
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 */
public class Log {

    public static final int LOG_CHANNEL = 8000;
    public static final int LOG_LEVEL_SEVERE = 1;
    public static final int LOG_LEVEL_WARNING = 2;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_FINE = 4;
    
    private static Properties props = null;

    /**
     * @return a ready to use {@link java.util.logging.Logger}.
     */
    public static Logger setUpLogger(String className)
    {
        try {
            
            // load properties for storing log messages
            if (props == null) {
                props = new Properties();
                props.load(Log.class.getResourceAsStream("/arcGisSos.properties"));
            }


            Logger logger = Logger.getLogger(className);
            
            // setup logging:
            String logFileTxt = props.getProperty("log.file.txt");
            if (logFileTxt != null) {
	            FileHandler fhTxt = new FileHandler();
	            fhTxt.setFormatter(new SimpleFormatter());
	            logger.addHandler(fhTxt);
            }
            else {
                throw new RuntimeException("Property 'log.file.txt' was not defined.");
            }
            
            String logFileHtml = props.getProperty("log.file.html");
            if (logFileHtml != null) {
	            FileHandler fhHtml = new FileHandler(props.getProperty("log.file.html"));
	            fhHtml.setFormatter(new SimpleFormatter());
	            logger.addHandler(fhHtml);
            }
//            else {
//				throw new RuntimeException("Property 'log.file.html' was not defined.");
//			}

            return logger;
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

// This custom formatter formats parts of a log record to a single line
class MyHtmlFormatter extends Formatter {
    // This method is called for every log records
    public String format(LogRecord rec)
    {
        StringBuilder buf = new StringBuilder(1000);
        // Bold any levels >= WARNING
        buf.append("<tr>");
        buf.append("<td>");

        if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
            buf.append("<b>");
            buf.append(rec.getLevel());
            buf.append("</b>");
        } else {
            buf.append(rec.getLevel());
        }
        buf.append("</td>");
        buf.append("<td>");
        buf.append(calcDate(rec.getMillis()));
        buf.append(' ');
        buf.append(formatMessage(rec));
        buf.append('\n');
        buf.append("<td>");
        buf.append("</tr>\n");
        return buf.toString();
    }

    private String calcDate(long millisecs)
    {
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }

    // This method is called just after the handler using this
    // formatter is created
    public String getHead(Handler h)
    {
        return "<HTML>\n<HEAD>\n" + (new Date()) + "\n</HEAD>\n<BODY>\n<PRE>\n" + "<table border>\n  " + "<tr><th>Time</th><th>Log Message</th></tr>\n";
    }

    // This method is called just after the handler using this
    // formatter is closed
    public String getTail(Handler h)
    {
        return "</table>\n  </PRE></BODY>\n</HTML>\n";
    }
}
