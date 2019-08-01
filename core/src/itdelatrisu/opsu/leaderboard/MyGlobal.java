package itdelatrisu.opsu.leaderboard;

import itdelatrisu.opsu.ScoreData;
import itdelatrisu.opsu.user.UserList;

/**
 * Created by user on 12/25/2018.
 */

public class MyGlobal extends LeaderboardMode{
    public static MyGlobal mode = new MyGlobal();

    public MyGlobal() {
        super("My Global");
    }


//    @Override
//    ArrayList<ScoreData> retrieveMethod(Beatmap in, String username) {
//        try {
//            ArrayList<ScoreData> result = DynamoDB.database.createScoreData(DynamoDB.database.getBeatmapScore(in.beatmapID));
//            ArrayList<ScoreData> filteredResult = new ArrayList<>();
//            for(int i=0;i<result.size();i++)
//                if(result.get(i).playerName.equals(username)) {
//                    result.get(i).rank=i;
//                    filteredResult.add(result.get(i));
//                }
//            return filteredResult;
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }

    @Override
    boolean filter(ScoreData in) {
        return in.playerName.equals(UserList.get().getCurrentUser().getName());
    }


}
