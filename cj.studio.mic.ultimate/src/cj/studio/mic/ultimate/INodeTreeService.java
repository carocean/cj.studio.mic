package cj.studio.mic.ultimate;

import java.util.List;

public interface INodeTreeService {
	void addFolder(TFolder folder);

	void updateFolder(String path, TFolder folder);

	void removeFolder(String path);

	TFolder getFolder(String path);

	long getNodeCountOfFolder(String path);

	List<TFolder> listChildFolders(String path);

	void addNode(TNode node);

	void updateNode(String path, TNode node);

	void removeNode(String fullName);

	List<TNode> listNodes(String path);

	TNode getNode(String fullName);

}
