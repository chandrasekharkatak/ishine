package com.apmosys.employeeportal.utility;

public class ExceptionLogContext {
	private static final ThreadLocal<StringBuilder> LOG_HOLDER =
            ThreadLocal.withInitial(StringBuilder::new);

    public static void add(String msg) {
        LOG_HOLDER.get().append(msg).append(" || ");
    }

    public static void add(Exception e) {
        LOG_HOLDER.get()
                .append(e.getMessage())
                .append(" || ");
    }
    
    public static String get() {
        return LOG_HOLDER.get().toString();
    }

    public static void clear() {
        LOG_HOLDER.remove();
    }

}
