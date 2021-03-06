package ai.metabot.learning.model;

import ai.abstraction.BuildBarracks;
import ai.abstraction.Expand;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;

public enum MetaBotAction {
	WORKER_RUSH(WorkerRush.class.getSimpleName()),
	LIGHT_RUSH(LightRush.class.getSimpleName()),
	RANGED_RUSH(RangedRush.class.getSimpleName()),
	EXPAND(Expand.class.getSimpleName()),
	BUILD_BARRACKS(BuildBarracks.class.getSimpleName())
	;
	
	private final String text;

    /**
     * @param text
     */
    private MetaBotAction(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}


