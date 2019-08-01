package itdelatrisu.opsu.leaderboard;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import fluddokt.ex.DynamoDB.DynamoDB;
import fluddokt.opsu.fake.Log;
import itdelatrisu.opsu.ScoreData;
import itdelatrisu.opsu.beatmap.Beatmap;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.user.User;
import itdelatrisu.opsu.user.UserList;

/**
 * Created by user on 12/25/2018.
 */

//Abstract class for leaderboard mods
public abstract class LeaderboardMode {
    static HashMap<Integer, ArrayList<ScoreData>> myScoresToAdd=new HashMap<>();
    static HashMap<Integer, ArrayList<ScoreData>> cache=new HashMap<>();
    static HashMap<Integer, Future<ArrayList<ScoreData>>> futures=new HashMap<>();
    public String modeName;

    public LeaderboardMode(String in){
        modeName=in;
    }



    public String toString(){return modeName;}


    public void addScore(Beatmap map, ScoreData in){
        if(!cache.containsKey(map.beatmapID)) {
            if(!myScoresToAdd.containsKey(map.beatmapID))
                myScoresToAdd.put(map.beatmapID, new ArrayList<ScoreData>());

            myScoresToAdd.get(map.beatmapID).add(in);

        } else {
            ArrayList<ScoreData> results = cache.get(map.beatmapID);
            results.add(in);
            Collections.sort(results);
            Collections.reverse(results);
            for (int i = 0; i < results.size(); i++) {
                results.get(i).rank = i;
            }
        }
    }

    public boolean isFutureDone(Beatmap in){
        int id = in.beatmapID;

        return  futures.containsKey(id) &&
                futures.get(id)!=null   &&
                futures.get(id).isDone();
    }

    public ArrayList<ScoreData> getFuture(Beatmap in){
        int id = in.beatmapID;
        ArrayList<ScoreData> list = new ArrayList<>();
        if(futures.containsKey(id)&&futures.get(id).isDone()) {
            try{
                ArrayList<ScoreData> futureResult = futures.get(id).get();
                cache.put(id, futureResult);
                for(int i=0;i<futureResult.size();i++)
                    if(filter(futureResult.get(i)))
                        list.add(futureResult.get(i));
            }
            catch (Exception e) {
                e.printStackTrace();
                UI.getNotificationManager().sendNotification(e.toString());
            }
            Log.debug("b4 " + futures);
            futures.remove(id);
            Log.debug("aft " + futures);
        }

        return list;
    }

    public void loadScores(Beatmap in, Future result) {
        int id = in.beatmapID;
        try {
                //Fetch results if we dont already have them
            if(!(cache.containsKey(id)||futures.containsKey(id))) {
                futures.put(id, result);
            }

        } catch (Exception e) {
                UI.getNotificationManager().sendNotification("Failed DB retrieval");
                Log.error(e);
        }

    }

    public HashMap<Integer, ArrayList<ScoreData>> getCache(){
        HashMap<Integer, ArrayList<ScoreData>> result = new HashMap<>();
        User currentUser = UserList.get().getCurrentUser();
        boolean changeMade = false;

        for(int key : cache.keySet()){
            ArrayList<ScoreData> scores = cache.get(key);
            if(myScoresToAdd.containsKey(key)) {
                scores.addAll(myScoresToAdd.remove(key));
                Collections.sort(scores);
                Collections.reverse(scores);
                for(int i=0;i<scores.size();i++)
                    scores.get(i).rank=i;
            }

            ArrayList<ScoreData> filtered = new ArrayList<>();
            for(int i=0;i<scores.size();i++){
                if(filter(scores.get(i)))
                    filtered.add(scores.get(i));
            }

            result.put(key, filtered);

//            if(!currentUser.topped && scores.get(scores.size()-1).playerName.equals(currentUser.getName())){
////                UI.getNotificationManager().sendNotification("Worked "+scores.get(0).title+" "+scores.get(scores.size()-1).playerName);
//
//                HashSet<String> distinct = new HashSet<>();
//                for(ScoreData data: scores)
//                    distinct.add(data.playerName);
//                if(distinct.size()>=10) {
//                    currentUser.topped = true;
//                    currentUser.updateBadges();
//                    changeMade =  true;
//
//                }
//
//            }
////            else
////                UI.getNotificationManager().sendNotification("not Worked "+scores.get(0).title+" "+scores.get(scores.size()-1).playerName);


        }
        if(changeMade)
            DynamoDB.database.addUserToDataBase(currentUser);


//        UI.getNotificationManager().sendNotification("Got cache");

        return result;
    }


    private HashMap<Integer, ArrayList<ScoreData>> deepCopy(Map<Integer, ArrayList<ScoreData>> in){
        HashMap<Integer, ArrayList<ScoreData>> copy=new HashMap<>();
        for(int id:in.keySet())
            copy.put(id,new ArrayList<>(in.get(id)));
        return copy;
    }

    public Callable getCallable(Beatmap in){
        return new retrieveScores(in);
    }

    private class retrieveScores implements Callable<ArrayList<ScoreData>> {
        Beatmap current;
        public retrieveScores(Beatmap in) {
            current = in;
        }
        @Override
        public ArrayList<ScoreData> call() throws SQLException {
            return retrieveMethod(current);

        }
    }

    ArrayList<ScoreData> retrieveMethod(Beatmap in) {
        try {
            return DynamoDB.database.createScoreData(DynamoDB.database.getBeatmapScore(in.beatmapID));


        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    abstract boolean filter(ScoreData in);

    public void reset(){
        cache=new HashMap<>();
        for(int id : futures.keySet()) {
            futures.get(id).cancel(false);
            futures.remove(id);
        }
    }

}
