package itdelatrisu.opsu.leaderboard;

import itdelatrisu.opsu.ScoreData;

/**
 * Created by user on 12/25/2018.
 */

public class TopGlobal extends LeaderboardMode {
    public static TopGlobal mode = new TopGlobal();
    public TopGlobal() {
        super("Top Global");
    }

//    @Override
//    ArrayList<ScoreData> retrieveMethod(Beatmap in, String username) {
//        try {
//            return DynamoDB.database.createScoreData(DynamoDB.database.getBeatmapScore(in.beatmapID));
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    @Override
    boolean filter(ScoreData in) {
        return true;
    }


}
