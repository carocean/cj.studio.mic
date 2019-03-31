package cj.studio.mic.plugin.service;

import java.util.HashMap;
import java.util.Map;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.mic.ultimate.IUserConsoleSession;
@CjService(name="userConsoleSession")
public class UserConsoleSession implements IUserConsoleSession {
	@CjServiceRef(refByName = "mongodb.netos.mic")
	ICube mic;

	@Override
	public void saveCurrentConsoleName(String user, String consoleName) {
		String cjql = String.format(
				"select {'tuple':'*'}.count() from tuple user.current.console.names %s where {'tuple.user':'%s'}",
				HashMap.class.getName(), user);
		IQuery<?> q=mic.count(cjql);
		if(q.count()>0) {
			String whereBson=String.format("{'tuple.user':'%s'}", user);
			mic.deleteDocOne("user.current.console.names", whereBson);
		}
		Map<String, String> map = new HashMap<>();
		map.put("user", user);
		map.put("consoleName", consoleName);
		mic.saveDoc("user.current.console.names", new TupleDocument<>(map));
	}

	@Override
	public String getCurrentConsoleName(String user) {
		String cjql = String.format(
				"select {'tuple':'*'} from tuple user.current.console.names %s where {'tuple.user':'%s'}",
				HashMap.class.getName(), user);
		IQuery<Map<String,String>> q=mic.createQuery(cjql);
		IDocument<Map<String,String>> doc= q.getSingleResult();
		if(doc==null) {
			return "$";
		}
		return doc.tuple().get("consoleName");
	}

}
