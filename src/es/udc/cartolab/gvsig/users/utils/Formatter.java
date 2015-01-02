package es.udc.cartolab.gvsig.users.utils;

public class Formatter implements IFormatter {

    /**
     * Sometime we want to return " " and others "". This is used to remember it
     */
    private final String EMPTY_STRING = "";

    @Override
    public String toString(Object o) {
	return o == null ? EMPTY_STRING : o.toString();
    }

}
