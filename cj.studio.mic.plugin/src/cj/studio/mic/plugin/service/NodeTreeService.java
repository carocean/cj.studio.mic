package cj.studio.mic.plugin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.mic.ultimate.INodeOnRemoveEvent;
import cj.studio.mic.ultimate.INodeTreeService;
import cj.studio.mic.ultimate.TFolder;
import cj.studio.mic.ultimate.TNode;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;

@CjService(name = "ntService")
public class NodeTreeService implements INodeTreeService {
	@CjServiceRef(refByName = "mongodb.netos.mic")
	ICube mic;

	@Override
	public void addFolder(TFolder folder) {
		if (StringUtil.isEmpty(folder.getPath())) {
			throw new EcmException("缺少路径");
		}
		if (getFolder(folder.getFullName()) != null) {
			throw new EcmException("已存在文件夹：" + folder.getFullName());
		}
		folder.setCtime(System.currentTimeMillis());
		mic.saveDoc("folders", new TupleDocument<>(folder));
	}

	@Override
	public void updateFolder(String path, TFolder folder) {
		if (StringUtil.isEmpty(folder.getPath())) {
			folder.setPath(path);
		}
		if (!path.equals(folder.getPath())) {
			throw new EcmException("不能移动文件夹");
		}
		folder.setCtime(System.currentTimeMillis());
		Document filter = Document
				.parse(String.format("{'tuple.path':'%s','tuple.code':'%s'}", path, folder.getCode()));
		Document update = Document.parse(String.format("{$set:{'tuple':%s}}", new Gson().toJson(folder)));
		mic.updateDocOne("folders", filter, update);
	}

	@Override
	public void removeFolder(String path) {
		if (StringUtil.isEmpty(path) || "/".equals(path)) {
			throw new EcmException("不能移除根");
		}
		if (getFolder(path) == null) {
			throw new EcmException("文件夹不存在：" + path);
		}
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		int pos = path.lastIndexOf("/");
		String code = path.substring(pos + 1, path.length());
		path = path.substring(0, pos);
		if (StringUtil.isEmpty(path)) {
			path = "/";
		}
		mic.deleteDocOne("folders", String.format("{'tuple.path':'%s','tuple.code':'%s'}", path, code));
	}

	@Override
	public TFolder getFolder(String path) {
		if ("/".equals(path)) {
			return null;
		}
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		int pos = path.lastIndexOf("/");
		String code = path.substring(pos + 1, path.length());
		path = path.substring(0, pos);
		if (StringUtil.isEmpty(path)) {
			path = "/";
		}
		String cjql = String.format(
				"select {'tuple':'*'} from tuple folders %s where {'tuple.path':'%s','tuple.code':'%s'}",
				TFolder.class.getName(), path, code);
		IQuery<TFolder> q = mic.createQuery(cjql);
		IDocument<TFolder> doc = q.getSingleResult();
		if (doc == null)
			return null;
		return doc.tuple();
	}

	@Override
	public long getNodeCountOfFolder(String path) {
		String cjql = String.format(
				"select {'tuple':'*'}.count() from tuple nodes %s where {'tuple.path':{$regex:'^%s',$options:'m'}}",
				TNode.class.getName(), path);
		IQuery<TNode> q = mic.createQuery(cjql);
		return q.count();
	}

	@Override
	public List<TFolder> listChildFolders(String path) {
		String cjql = String.format("select {'tuple':'*'} from tuple folders %s where {'tuple.path':'%s'}",
				TFolder.class.getName(), path);
		IQuery<TFolder> q = mic.createQuery(cjql);
		List<IDocument<TFolder>> list = q.getResultList();
		List<TFolder> folders = new ArrayList<>();
		for (IDocument<TFolder> doc : list) {
			folders.add(doc.tuple());
		}
		return folders;
	}

	@Override
	public void addNode(TNode node, INodeOnRemoveEvent event) {
		if (!node.getPath().endsWith("/")) {
			node.setPath(node.getPath() + "/");
		}
		if (StringUtil.isEmpty(node.getPath())) {
			throw new EcmException("缺少路径");
		}
		String fn = node.getPath() + "." + node.getUuid();
		if (getNode(fn) != null) {
			throw new EcmException("已存在文件夹：" + fn);
		}
		TNode exists = getNodeByUUID(node.getUuid());
		if (exists != null) {
			removeNodeByUUID(node.getUuid());
			if (event != null) {
				try {
					event.onRemoved(node.getUuid());
				} catch (CircuitException e) {
					e.printStackTrace();
				}
			}
		}
		node.setCtime(System.currentTimeMillis());
		mic.saveDoc("nodes", new TupleDocument<>(node));
	}

	@Override
	public void updateNode(String path, TNode node) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		if (StringUtil.isEmpty(node.getPath())) {
			node.setPath(path);
		}
		if (!path.equals(node.getPath())) {
			throw new EcmException("不能移动节点");
		}
		node.setCtime(System.currentTimeMillis());
		Document filter = Document.parse(String.format("{'tuple.path':'%s'}", path));
		Document update = Document.parse(String.format("{$set:%s}", new Gson().toJson(node)));
		mic.updateDocOne("nodes", filter, update);
	}

	@Override
	public void removeNode(String fn) {
		int pos = fn.indexOf(".");
		if (pos < 0) {
			throw new EcmException("不是节点全路径名");
		}
		String path = fn.substring(0, pos);
		if (!path.endsWith("/")) {
			path += "/";
		}
		String code = fn.substring(pos + 1, fn.length());
		mic.deleteDocOne("nodes", String.format("{'tuple.path':'%s','tuple.uuid':'%s'}", path, code));
	}

	@Override
	public void removeNodeByUUID(String uuid) {
		mic.deleteDocOne("nodes", String.format("{'tuple.uuid':'%s'}", uuid));
	}

	@Override
	public List<TNode> listNodes(String path) {
		if (!path.endsWith("/")) {
			path += "/";
		}
		String cjql = String.format("select {'tuple':'*'} from tuple nodes %s where {'tuple.path':'%s'}",
				TNode.class.getName(), path);
		IQuery<TNode> q = mic.createQuery(cjql);
		List<IDocument<TNode>> list = q.getResultList();
		List<TNode> nodes = new ArrayList<>();
		for (IDocument<TNode> doc : list) {
			nodes.add(doc.tuple());
		}
		return nodes;
	}

	@Override
	public TNode getNode(String fn) {
		int pos = fn.indexOf(".");
		if (pos < 0) {
			throw new EcmException("不是节点全路径名");
		}
		String path = fn.substring(0, pos);
		if (!path.endsWith("/")) {
			path += "/";
		}
		String code = fn.substring(pos + 1, fn.length());
		String cjql = String.format(
				"select {'tuple':'*'} from tuple nodes %s where {'tuple.path':'%s','tuple.uuid':'%s'}",
				TNode.class.getName(), path, code);
		IQuery<TNode> q = mic.createQuery(cjql);
		IDocument<TNode> doc = q.getSingleResult();
		if (doc == null)
			return null;
		return doc.tuple();
	}

	@Override
	public TNode getNodeByUUID(String uuid) {
		String cjql = String.format("select {'tuple':'*'} from tuple nodes %s where {'tuple.uuid':'%s'}",
				TNode.class.getName(), uuid);
		IQuery<TNode> q = mic.createQuery(cjql);
		IDocument<TNode> doc = q.getSingleResult();
		if (doc == null)
			return null;
		return doc.tuple();
	}

	@Override
	public void online(TNode n, String channel) {
		Map<String, Object> map = new HashMap<>();
		map.put("path", n.getPath());
		map.put("uuid", n.getUuid());
		map.put("channel", channel);
		map.put("status", "online");
		map.put("ctime", System.currentTimeMillis());
		mic.saveDoc("event.onlines", new TupleDocument<>(map));
	}

	@Override
	public Map<String, Object> offline(String channel) {
		Map<String, Object> online = getOnline(channel);
		if (online == null)
			return null;
		Map<String, Object> map = new HashMap<>();
		map.put("path", online.get("path"));
		map.put("uuid", online.get("uuid"));
		map.put("status", "offline");
		map.put("ctime", System.currentTimeMillis());
		mic.saveDoc("event.onlines", new TupleDocument<>(map));
		return online;
	}

	private Map<String, Object> getOnline(String channel) {
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.ctime':-1}).limit(1).skip(0) from tuple event.onlines %s where {'tuple.channel':'%s'}",
				HashMap.class.getName(), channel);
		IQuery<HashMap<String, Object>> q = mic.createQuery(cjql);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null)
			return null;
		return doc.tuple();
	}

	@Override
	public boolean isOnline(TNode n) {
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.ctime':-1}).limit(1).skip(0) from tuple event.onlines %s where {'tuple.path':'%s','tuple.uuid':'%s'}",
				HashMap.class.getName(), n.getPath(), n.getUuid());
		IQuery<HashMap<String, Object>> q = mic.createQuery(cjql);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null)
			return false;
		return "online".equals(doc.tuple().get("status"));
	}

	@Override
	public Map<String, Object> getOnlineEntry(TNode n) {
		String cjql = String.format(
				"select {'tuple':'*'}.sort({'tuple.ctime':-1}).limit(1).skip(0) from tuple event.onlines %s where {'tuple.path':'%s','tuple.uuid':'%s'}",
				HashMap.class.getName(), n.getPath(), n.getUuid());
		IQuery<HashMap<String, Object>> q = mic.createQuery(cjql);
		IDocument<HashMap<String, Object>> doc = q.getSingleResult();
		if (doc == null)
			return null;
		return doc.tuple();
	}
}
