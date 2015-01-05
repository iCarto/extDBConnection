package es.udc.cartolab.gvsig.users.utils;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite.SpatiaLiteDriver;


public class DBSessionSpatiaLiteTest {

    private static DBSession session;
    private final static String sqliteFile = null; // fill me

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	new SpatiaLiteDriver(false, TestProperties.driversPath.replace(
		"/drivers", "/lib/"));
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
	DBSessionSpatiaLite.createConnection(sqliteFile);
	session = DBSession.getCurrentSession();
    }

    @Test
    // regression test
    public void sameFieldIsReturnedFromQuery() throws SQLException {
	String tableName = "geometry_columns";
	String schema = "public";
	String[] fieldNames = {"f_table_name", "f_table_name"};
	String whereClause = null;
	String[] orderBy = null;
	boolean desc = true;
	ResultSet rs = session.getTableAsResultSet(tableName, schema, fieldNames, whereClause, orderBy, desc);
	rs.next();
	assertEquals(2, rs.getMetaData().getColumnCount());
    }

}
