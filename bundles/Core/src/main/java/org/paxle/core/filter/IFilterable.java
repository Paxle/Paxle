
package org.paxle.core.filter;

public interface IFilterable {
	
	/* =======================================================
	 * General information
	 * ======================================================= */
	public static enum Result {
		Passed,
		Rejected,
		Failure
	}
	
	public Result getResult();
	public boolean isResult(Result result);
	public String getResultText();
	public void setResultText(String description);
	public void setResult(Result result);
	public void setResult(Result result, String description);
}
