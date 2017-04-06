package tests.rl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.rl.adapters.learners.PersistentMultiAgentQLearningTest;
import tests.rl.adapters.learners.SGQLearningAdapterTest;
import tests.rl.models.aggregate.AggregateJAMTest;
import tests.rl.models.aggregatediff.AggregateDiffStateJAMTest;
import tests.rl.models.aggregatediff.TestAggregateDiffState;
import tests.rl.models.common.TestSimpleWeightedFeatures;
import tests.rl.models.common.TestSimpleWeightedFeaturesTerminal;

@RunWith(Suite.class)
@SuiteClasses({ 
	RLParametersTest.class, 
	SGQLearningAdapterTest.class,
	PersistentMultiAgentQLearningTest.class,
	
	AggregateJAMTest.class,
	TestSimpleWeightedFeatures.class,
	TestSimpleWeightedFeaturesTerminal.class,
	
	TestAggregateDiffState.class,
	AggregateDiffStateJAMTest.class,
})

public class AllTests {

}
