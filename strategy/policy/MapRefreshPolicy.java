/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package batman.strategy.policy;

/**
 *
 * @author pw248348
 */
public enum MapRefreshPolicy {

	FullScanAlways(0, 0),
	OldScanAlways(0, 60),
	UnknownScanAlways(0, -1),
	FullScanModerately(5, 0),
	OldScanModerately(5, 60),
	UnknownScanModerately(5, -1),
	FullScanWhenNotBusy(-1, 0),
	OldScanWhenNotBusy(-1, 60),
	UnknownScanWhenNotBusy(-1, -1),
	FullScanRarely(20, 0),
	OldScanRarely(20, 60),
	UnknownScanRarely(20, -1);

	private MapRefreshPolicy(int scanDelay, int refreshWhenOlderThan) {
		this.scanDelay = scanDelay;
		this.refreshWhenOlderThan = refreshWhenOlderThan;
	}
	public int scanDelay;
	public int refreshWhenOlderThan;
}
