package com.neuronrobotics.bowlerstudio.scripting;

public interface IGithubLoginListener {
	public void onLogin(String newUsername);
	public void onLogout(String oldUsername);
}
