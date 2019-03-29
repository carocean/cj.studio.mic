package cj.studio.mic.ultimate;

public class TNode {
	String uuid;
	String title;
	String desc;
	String path;
	String miclient;
	long ctime;

	public TNode() {

	}

	public TNode(String uuid, String title, String desc, String path,String miclient) {
		super();
		this.uuid = uuid;
		this.title = title;
		this.desc = desc;
		this.path = path;
		this.miclient=miclient;
		this.ctime = System.currentTimeMillis();
	}
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getMiclient() {
		return miclient;
	}
	public void setMiclient(String miclient) {
		this.miclient = miclient;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	
	
}
