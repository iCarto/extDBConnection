package es.udc.cartolab.gvsig.users.utils;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;


public class DBSessionPostGISTest {

    private static DBSession session;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
	DBSessionPostGIS.createConnection(TestProperties.server, TestProperties.port, TestProperties.database, TestProperties.schema, TestProperties.username, TestProperties.password);
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
