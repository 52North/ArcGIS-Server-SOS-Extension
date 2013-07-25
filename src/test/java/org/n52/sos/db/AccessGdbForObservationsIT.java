package org.n52.sos.db;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.n52.om.observation.MultiValueObservation;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.sos.AQDObservationEncoder;
import org.n52.sos.db.SubField;
import org.n52.sos.it.EsriTestBase;

public class AccessGdbForObservationsIT extends EsriTestBase {

    static Logger LOGGER = Logger.getLogger(AccessGdbForObservationsIT.class.getName());
    
    @Test
    public void testGetObservationsStringArray()
    {
        String[] observationIdentifiers = { "GB_Observation_59" };

        try {
            gdb.getObservationAccess().getObservations(observationIdentifiers);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    

	@Test
    public void testGetObservations()
    {
        try {
            String[] offerings = null;
            String spatialFilter = "{\"xmin\":-180.0,\"ymin\":-90.0,\"xmax\":180.0,\"ymax\":90.0,\"spatialReference\":{\"wkid\":4326}}";
            String temporalFilter = "after:2013-02-01T01:00:00+0007";
            String where = "value_numeric > 9";
            String[] observedProperties = new String[]{"http://dd.eionet.europa.eu/vocabulary/aq/pollutant/1", "http://dd.eionet.europa.eu/vocabularies/aq/pollutant/7"};
            String[] procedures = null;
            String[] featuresOfInterest = null;
            
            Map<String, MultiValueObservation> idObsList = gdb.getObservationAccess().getObservations(offerings, featuresOfInterest, observedProperties, procedures, spatialFilter, temporalFilter, where);
        
            AQDObservationEncoder encoder = new AQDObservationEncoder();
            String result = encoder.wrapInEnvelope(encoder.encodeObservations(idObsList));
            
            OutputStream out = new FileOutputStream("c:/temp/observations.xml");
            out.write(result.getBytes());
            out.flush();
            out.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCreateTemporalClauseSDE()
    {
        // TEST: equals:yyyy-MM-ddTHH:mm:ss+HH:mm
        String temporalFilter = "equals:2011-12-04T15:45:30+04:00";
        String expectedTemporalClause = SubField.VALUE_DATETIME_END + " = '2011-12-04 11:45:30'";
        String temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: after:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
        temporalFilter = "after:2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " > '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: before:yyyy-MM-ddTHH:mm:ss+HH:mm<br>
        temporalFilter = "before:2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " < '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: during:yyyy-MM-ddTHH:mm:ss+HH:mm,yyyy-MM-dd HH:mm:ss+HH:mm
        temporalFilter = "during:2011-12-04T15:45:30+04:00,2011-12-04T15:45:30+04:00";
        expectedTemporalClause = SubField.VALUE_DATETIME_END + " BETWEEN '2011-12-04 11:45:30' AND '2011-12-04 11:45:30'";
        temporalClause = gdb.getObservationAccess().createTemporalClauseSDE(temporalFilter);
        Assert.assertEquals(expectedTemporalClause, temporalClause);
        
        // TEST: last:milliseconds,+HH:mm
        // cannot geoDBQuerier this, since there is a System.currentTimeMillis() call in the method.
    }
    
    public void testCreatePhenomenonTimeFromDate(){
        try {
            // assuming this is the temporal filter submitted by the client: 
            String temporalFilter = "equals:2011-12-04T15:45:30+04:00";
            
            // assuming this is the corresponding date of an observation coming from our UTC database:
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2011-12-04 11:45:30");
            
            ITimePosition timePos = gdb.getObservationAccess().createTimeFromDate(date, temporalFilter);
            
            Assert.assertEquals("2011-12-04T15:45:30+04:00", timePos.toISO8601Format());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public void testExtractTemporalOperandAfterKeyWord(){
    	Assert.assertEquals("2011-12-04T15:45:30+04:00", gdb.getObservationAccess().extractTemporalOperandAfterKeyWord("equals:2011-12-04T15:45:30+04:00"));
    	Assert.assertEquals("2011-12-04T15:45:30+04:00,2011-12-04T15:50:30+04:00", gdb.getObservationAccess().extractTemporalOperandAfterKeyWord("during:2011-12-04T15:45:30+04:00,2011-12-04T15:50:30+04:00"));
    	Assert.assertEquals("100,+02:00", gdb.getObservationAccess().extractTemporalOperandAfterKeyWord("last:100,+02:00"));
    }
}
