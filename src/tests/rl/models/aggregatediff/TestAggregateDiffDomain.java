package tests.rl.models.aggregatediff;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import rl.models.aggregate.AggregateStateDomain;
import rl.models.aggregatediff.AggregateDifferencesDomain;

public class TestAggregateDiffDomain {

	AggregateDifferencesDomain testedDomain;
	
	@Before
	public void setUp() throws Exception {
		testedDomain = new AggregateDifferencesDomain(
			"src/tests/rl/models/aggregatediff/basesWorkers24x24.xml"
		);
		
	}

	@Test
	public void testEnumerate() {
		// the domain should have 6*3^7 = 13122 states
		assertEquals(13122, testedDomain.enumerate().size());
	}

}
