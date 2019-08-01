/*
 * opsu! - an open-source osu! client
 * Copyright (C) 2014-2017 Jeffrey Han
 *
 * opsu! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!.  If not, see <http://www.gnu.org/licenses/>.
 */

package itdelatrisu.opsu.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import fluddokt.ex.DynamoDB.DynamoDB;
import itdelatrisu.opsu.ui.badges.BadgeGroup;

/**
 * User profile.
 */
public class User implements Comparable<User> {
	/** Display name. */
	private String name;

	//User's password
	private String password="";

	//Hashed version of the users password
	private String hashedPassword="";

	/** Total score. */
	public long score;

	/** Total accuracy. */
	public double accuracy;

	/** Total number of plays passed. */
	public int playsPassed;

	/** Total number of plays. */
	public int playsTotal;

	/** Current level. */
	public int level;

	/** Next level progress. */
	public double levelProgress;

	/** Profile icon identifier. */
	public int icon;

	//An array representing the badges this account obtained
	private List<Integer> obtainedBadges;

	/** Lis of available Icons*/
	private List<Integer> availableIcon;

	public int tokens = 0, friendLimit = 5, consecutiveLogins = 0;

	public boolean topped = false;

	public long lastLogin = 0;

	private List<String> friendNames = new ArrayList<>();

	private ArrayList<User> friendsList;

	/**
	 * Creates a new user with the given name and icon.
	 * @param name the user's name
	 * @param icon the user's icon
	 */
	public User(String name, int icon) { this(name, 0L, 0.0, 0, 0, icon); }

	/**
	 * Creates a user with existing stats.
	 * @param name the user's name
	 * @param score the user's total score
	 * @param accuracy the user's total accuracy
	 * @param playsPassed the user's total passed play count
	 * @param playsTotal the user's total play count
	 * @param icon the user's icon identifier
	 */
	public User(String name, long score, double accuracy, int playsPassed, int playsTotal, int icon) {
		this.name = name;
		this.score = score;
		this.accuracy = accuracy;
		this.playsPassed = playsPassed;
		this.playsTotal = playsTotal;
		this.icon = icon;
		this.availableIcon=new ArrayList<>();
		availableIcon.add(1);
		availableIcon.add(2);
		calculateLevel();
	}

	/**
	 * Adds stats from a play (passed).
	 * @param score the score for the game
	 * @param accuracy the accuracy for the game
	 */
	public void add(long score, double accuracy) {
		this.score += score;
		this.accuracy = ((this.accuracy * this.playsPassed) + accuracy) / (this.playsPassed + 1);
		this.playsPassed++;
		this.playsTotal++;
		calculateLevel();


//		UserDB userToFind=new UserDB();
//		userToFind.setUsername(this.name);
//		userToFind.setAccuracy(accuracy);
//		userToFind.setScore(score);
//		userToFind.setPlaysPassed(playsPassed);
//		userToFind.setPlaysTotal(playsTotal);
	}



	/**
	 * Adds stats from a play (failed).
	 * @param score the score for the game
	 */
	public void add(long score) {
		this.score += score;
		this.playsTotal++;
		calculateLevel();
	}

	/** Returns the user's name. */
	public String getName() { return name; }

	/** Gets the name's of all the user's friends. */
	public List<String> getFriendNames() { return friendNames; }

	// Gets badges
	public List<Integer> getBadges() { return obtainedBadges; }

	//Returns the user's password
	public String getPassword() { return password; }

	//Returns the user's hashed password
	public String getHashedPassword() { return hashedPassword; }

	/** Returns the user's total score. */
	public long getScore() { return score; }

	/** Returns the user's total accuracy. */
	public double getAccuracy() { return accuracy; }

	/** Returns the user's total passed play count. */
	public int getPassedPlays() { return playsPassed; }

	/** Returns the user's total play count. */
	public int getTotalPlays() { return playsTotal; }

	/** Returns the user's icon identifier. */
	public int getIconId() { return icon; }

	/** Returns the list of possible user icon identifiers. */
	public List<Integer> getAvailableIcons() { return availableIcon; }

	/** Returns the user's level. */
	public int getLevel() { return level; }

	/** Returns the progress to the next level in [0,1). */
	public double getNextLevelProgress() { return levelProgress; }

	/** Calculates the user's current level and next level progress. */
	private void calculateLevel() {
		if (score == 0) {
			this.level = 1;
			this.levelProgress = 0.0;
			return;
		}

		int l;
		for (l = 1; this.score >= getScoreForLevel(l); l++) {}
		l--;
		this.level = l;
		long baseScore = getScoreForLevel(l);
		this.levelProgress = (double) (this.score - baseScore) / (getScoreForLevel(l + 1) - baseScore);
	}

	public User mergeUser(User in){
		long newScore=score+in.score;
		int newPlaysPassed=playsPassed+in.playsPassed;
		int newPlaysTotal=playsTotal+in.playsTotal;
		double newAccuracy=(accuracy*playsTotal+in.accuracy*in.playsTotal)/newPlaysTotal;
		HashSet<Integer> icons=new HashSet<>();
		//Add all of the available icons from both users into a set to remove repeats
		for(int e:in.getAvailableIcons())
			icons.add(e);
		for(int e:availableIcon)
			icons.add(e);

		//Store the values in the set in an list
		ArrayList<Integer> newAvailableIcons=new ArrayList<>();
		newAvailableIcons.addAll(icons);
		Collections.sort(newAvailableIcons);

		User newUser=new User(name,newScore,newAccuracy,newPlaysPassed,newPlaysTotal,0);
		newUser.calculateLevel();
		newUser.setAvailableIcons(newAvailableIcons);
		return newUser;
	}
	 public String toString(){return name;}

	/**
	 * Returns the total score needed for a given level:
	 * <ul>
	 * <li><strong>Level <= 100:</strong>
	 * <p>5,000 / 3 * (4n^3 - 3n^2 - n) + 1.25 * 1.8^(n - 60)
	 * <li><strong>Level > 100:</strong>
	 * <p>26,931,190,829 + 100,000,000,000 * (n - 100)
	 * </ul>
	 * @param level the level
	 * @return the total score needed
	 * @see <a href="https://osu.ppy.sh/wiki/Score#Level">https://osu.ppy.sh/wiki/Score#Level</a>
	 */
	private static long getScoreForLevel(int level) {
		if (level <= 1)
			return 1L;
		else if (level <= 100)
			return (long) (5000.0 / 3 * (4 * Math.pow(level, 3) - 3 * Math.pow(level, 2) - level) + 1.25 * Math.pow(1.8, level - 60));
		else
			return 26_931_190_829L + 100_000_000_000L * (level - 100);
	}

	/** Sets the user's name. */
	public void setName(String name) { this.name = name; }

	/** Sets the name's of all the user's friends. */
	public void setFriendNames(List<String> friendNames) { this.friendNames = friendNames; }

	/** Sets the user's name. */
	public void setBadges(List<Integer> obtainedBadges) { this.obtainedBadges = obtainedBadges;}

	/** Sets the user's password. */
	public void setPassword(String password) { this.password = password; hashedPassword=sha256(password);}

	/** Sets the hashed version of the user's password. */
	public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

	/** Sets the user's icon identifier. */
	public void setIconId(int id) { this.icon = id; }

	public void setAvailableIcons(List<Integer> availableIcon) { this.availableIcon = availableIcon; }

	@Override
	public int compareTo(User other) {
		return this.getName().compareToIgnoreCase(other.getName());
	}

	//sha256 implementation
	public String sha256(String a){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(a.getBytes());
        return bytesToHex(hash);
    }
    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public void sendUserToDB(){
		//Update database online
		DynamoDB.database.addUserToDataBase(this);
    }

    public boolean updateBadges(){
		boolean changeMade=false;
		for(int groupNumber=0;groupNumber<BadgeGroup.ALL_BADGES.length;groupNumber++){
			BadgeGroup group=BadgeGroup.ALL_BADGES[groupNumber];
			if(group.getName().equals("FRIENDS")){
				int friendAmount=friendNames.size();
				for(int badgeIndex=group.getBadges().length-1; badgeIndex>=0; badgeIndex--){
					if(friendAmount>=group.getBadge(badgeIndex).getCount()) {
						changeMade= changeMade || group.obtain(this, badgeIndex, groupNumber);
						break;
					}
				}
			}
			else if(group.getName().equals("LOGIN")){
				for(int badgeIndex=group.getBadges().length-1; badgeIndex>=0; badgeIndex--){
					if(consecutiveLogins>=group.getBadge(badgeIndex).getCount()) {
						changeMade = changeMade || group.obtain(this, badgeIndex, groupNumber);
						break;
					}
				}
			}
			else if(group.getName().equals("KING")){
//				for(int badgeIndex=group.getBadges().length-1; badgeIndex>=0; badgeIndex--){
//					if(topped){
//						changeMade = changeMade || group.obtain(this, badgeIndex, groupNumber);
//						break;
//					}
//				}
				if(topped){
					changeMade = changeMade || group.obtain(this, 0, groupNumber);
				}
			}

		}
		return changeMade;
	}

}
