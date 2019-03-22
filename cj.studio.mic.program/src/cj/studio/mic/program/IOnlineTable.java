package cj.studio.mic.program;

public interface IOnlineTable {
	void on(String user,String pipelineName);
	void off(String user);
	String getUserOnPipeline(String user);
	void offPipeline(String inputName);
}
