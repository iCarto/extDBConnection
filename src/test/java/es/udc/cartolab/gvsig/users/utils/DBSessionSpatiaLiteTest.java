package es.udc.cartolab.gvsig.users.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;

import es.icarto.gvsig.commons.testutils.Drivers;
import es.icarto.gvsig.commons.testutils.TestProperties;
import es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite.NativeDependencies;
import es.udc.cartolab.cit.gvsig.fmap.drivers.jdbc.spatialite.SpatiaLiteDriver;


public class DBSessionSpatiaLiteTest {

    private static DBSession session;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	NativeDependencies.libsPath = TestProperties.driversPath.replace("/drivers", "/lib/");
	Drivers.initgvSIGDrivers(TestProperties.driversPath);
	new SpatiaLiteDriver();

	File db = File.createTempFile("test", ".sqlite");
	db.deleteOnExit();

	DBSessionSpatiaLite.createConnection(db.getAbsolutePath());

	session = DBSession.getCurrentSession();
	initSpatialData();

    }

    private static void initSpatialData() throws SQLException {
	Connection conJbdc = session.getJavaConnection();
	Statement st = conJbdc.createStatement();
	st.executeQuery("SELECT InitSpatialMetaData()");
	conJbdc.commit();
	st.close();
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
