package tests.rl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.rl.adapters.learners.PersistentMultiAgentQLearningTest;
import tests.rl.adapters.learners.SGQLearningAdapterTest;
import tests.rl.models.aggregate.AggregateJAMTest;
import tests.rl.models.aggregatediff.AggregateDiffStateJAMTest;
import tests.rl.models.aggregatediff.TestAggregateDiffDomain;
import tests.rl.models.aggregatediff.TestAggregateDiffState;
import tests.rl.models.common.TestSimpleWeightedFeatures;
import tests.rl.models.common.TestSimpleWeightedFeaturesTerminal;
import tests.rl.models.stages.StagesJAMTest;
import tests.rl.planners.TestBackwardInduction;

@RunWith(Suite.class)
@SuiteClasses({ 
	RLParametersTest.class, 
	SGQLearningAdapterTest.class,
	PersistentMultiAgentQLearningTest.class,
	
	//TestAggregateDomain.class, //enumerate yields too many states
	StagesJAMTest.class,
	AggregateJAMTest.class,
	AggregateDiffStateJAMTest.class,
	
	TestAggregateDiffState.class,
	TestAggregateDiffDomain.class,
	
	TestSimpleWeightedFeatures.class,
	TestSimpleWeightedFeaturesTerminal.class,
	
	TestBackwardInduction.class,
	
})

public class AllTests {

}
