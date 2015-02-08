package com.mzinck.blockjump.objects;

public class User {
	
	private String name, email;
	private long score, user_id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getUserId() {
		return user_id;
	}

	public void setUserId(long userId) {
		this.user_id = userId;
	}
	
}
