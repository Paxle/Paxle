package ${package}.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class MyFilter implements IFilter {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		try {
			if (command.getResult() != ICommand.Result.Passed) return;
			
			// TODO: do your work here
			
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while processing command with URI '%s'.",
					e.getClass().getName(),
					command.getLocation().toASCIIString()
			),e);
		}			
	}
}
