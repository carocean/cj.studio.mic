package cj.studio.mic.ultimate;

public class TNode {
	String code;
	String name;
	String creator;
	String path;
	long ctime;

	public TNode() {

	}

	public TNode(String code, String path, String name, String creator, long ctime) {
		this();
		this.code = code;
		this.name = name;
		this.path = path;
		this.creator = creator;
		this.ctime = ctime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
}
