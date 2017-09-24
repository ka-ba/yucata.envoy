package kaba.yucata.envoy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

/**
 * Created by kaba on 24/09/17.
 */

public class DebugHelper {
    public static String head(String in, int lines)
            throws IllegalArgumentException {
        final BufferedReader reader = new BufferedReader(new StringReader(in));
        final StringBuilder builder = new StringBuilder(lines * 80);
        try {
            for (int i=0; i < lines; i++) {
                final String line = reader.readLine();
                if (line != null)
                    builder.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot head input string"+(in==null?" (is null)":""), e);
        }
        return builder.toString();
    }

    public static String textAndTraceHead(String text,int trace_lines) {
        final StackTraceElement[] trace = new Exception().getStackTrace();
        final StringBuilder builder = new StringBuilder((trace_lines+1) * 80);
        builder.append(text).append('\n');
        for( int i=1; (i<trace.length)&&(i<trace_lines+1); i++ ) // first line would be here => hide
            builder.append(" > ").append(trace[i].toString()).append('\n');
        return builder.toString();
    }
}
