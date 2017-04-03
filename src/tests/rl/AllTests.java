package tests.rl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tests.rl.adapters.learners.PersistentMultiAgentQLearningTest;
import tests.rl.adapters.learners.SGQLearningAdapterTest;
import tests.rl.models.common.TestSimpleWeightedFeatures;
import tests.rl.models.common.TestSimpleWeightedFeaturesTerminal;
import tests.rl.models.simplecounting.AggregateJAMTest;

@RunWith(Suite.class)
@SuiteClasses({ 
	RLParametersTest.class, 
	SGQLearningAdapterTest.class,
	PersistentMultiAgentQLearningTest.class,
	AggregateJAMTest.class,
	TestSimpleWeightedFeatures.class,
	TestSimpleWeightedFeaturesTerminal.class,
})

public class AllTests {

}
