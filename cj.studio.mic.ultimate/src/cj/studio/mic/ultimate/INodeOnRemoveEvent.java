package cj.studio.mic.ultimate;

import cj.studio.ecm.net.CircuitException;

public interface INodeOnRemoveEvent {

	void onRemoved(String uuid) throws CircuitException;

}
