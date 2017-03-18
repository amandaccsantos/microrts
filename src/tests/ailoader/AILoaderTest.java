package tests.ailoader;

import static org.junit.Assert.*;

import org.junit.Test;

import ai.RandomAI;
import ai.metabot.MetaBot;
import ailoader.AILoader;

public class AILoaderTest {

	@Test
	public void testLoadAI() {
		//test for RandomAI
		assertTrue(AILoader.loadAI("ai.RandomAI") instanceof RandomAI);
		
		//test for MetaBot
		assertTrue(AILoader.loadAI("ai.metabot.MetaBot") instanceof MetaBot);
	}

}
