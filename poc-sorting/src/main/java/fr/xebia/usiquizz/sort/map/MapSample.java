package fr.xebia.usiquizz.sort.map;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapSample {

	private static final Map<Short, List<Integer>> leaderboard = new TreeMap<Short, List<Integer>>();

	public static void main(String[] args) {
		initLeaderBoard();

	}

	private static short MAX_SCORE = 10000;

	private static void initLeaderBoard() {
		for (short i = 0; i < MAX_SCORE; i++) {
			leaderboard.put(i, new LinkedList<Integer>());
		}
	}

	private static void updateScore(int playerId, short score) {
		leaderboard.get(score).add(playerId);
	}

}
