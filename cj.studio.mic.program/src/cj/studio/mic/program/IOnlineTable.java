package cj.studio.mic.program;

import java.util.Set;

public interface IOnlineTable {
	void on(String user,String pipelineName);
	void off(String user);
	String getUserOnPipeline(String user);
	void offPipeline(String inputName);
	Set<String>enumUser();
}
