package tests.rl.models.singleagentstages;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import burlap.mdp.core.state.State;
import rl.models.singleagentstages.SingleAgentStagesDomain;

public class SingleAgentStagesDomainTest {

	SingleAgentStagesDomain testedDomain;
	
	@Before
	public void setUp() throws Exception {
		testedDomain = new SingleAgentStagesDomain(
			"src/tests/rl/models/aggregatediff/basesWorkers24x24.xml"
		);
		
	}

	@Test
	public void testEnumerate() {
		List<? extends State> allStates = testedDomain.enumerate();
		// the domain should have 6 states
		assertEquals(5, allStates.size());
		
		
	}

}
