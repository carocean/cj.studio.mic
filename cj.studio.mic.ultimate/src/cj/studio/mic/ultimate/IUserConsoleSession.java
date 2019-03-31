package cj.studio.mic.ultimate;

public interface IUserConsoleSession {
	void saveCurrentConsoleName(String user,String consoleName);
	String getCurrentConsoleName(String user);
}
