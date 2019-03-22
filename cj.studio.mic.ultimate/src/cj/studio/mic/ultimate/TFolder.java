package cj.studio.mic.ultimate;

public class TFolder {
	String code;
	String path;
	String name;
	String creator;
	long ctime;

	public TFolder() {

	}

	public TFolder(String code,String path, String name, String creator, long ctime) {
		this();
		this.code=code;
		this.path = path;
		this.name = name;
		this.creator = creator;
		this.ctime = ctime;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getPath() {
		return path;
	}
	public String getFullName() {
		return String.format("%s.%s", path,code);
	}
	public void setPath(String path) {
		this.path = path;
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
