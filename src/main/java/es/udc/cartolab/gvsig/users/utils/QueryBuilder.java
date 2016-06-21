package es.udc.cartolab.gvsig.users.utils;

public class QueryBuilder {

	public String getOrderByClause(String[] orderBy, boolean desc) {

		if ((orderBy == null) || (orderBy.length == 0)) {
			return "";
		}

		String orderByClause = " ORDER BY ";
		for (String c : orderBy) {
			orderByClause += c + (desc ? " DESC" : " ASC") + ", ";
		}

		return orderByClause.substring(0, orderByClause.length() - 2) + " ";
	}
}
