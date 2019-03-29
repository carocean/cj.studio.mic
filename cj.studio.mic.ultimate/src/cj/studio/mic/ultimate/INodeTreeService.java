package cj.studio.mic.ultimate;

import java.util.List;
import java.util.Map;

public interface INodeTreeService {
	void addFolder(TFolder folder);

	void updateFolder(String path, TFolder folder);

	void removeFolder(String path);

	TFolder getFolder(String path);

	long getNodeCountOfFolder(String path);

	List<TFolder> listChildFolders(String path);

	void addNode(TNode node,INodeOnRemoveEvent event);

	void updateNode(String path, TNode node);

	void removeNode(String fullName);

	List<TNode> listNodes(String path);

	TNode getNode(String fullName);

	void online(TNode n,String inputName);

	Map<String, Object> offline(String inputName);

	boolean isOnline(TNode n);

	Map<String, Object> getOnlineEntry(TNode n);

	TNode getNodeByUUID(String uuid);

	void removeNodeByUUID(String uuid);
}
