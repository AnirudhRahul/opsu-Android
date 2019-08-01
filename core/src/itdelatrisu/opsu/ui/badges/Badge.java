package itdelatrisu.opsu.ui.badges;

import fluddokt.opsu.fake.Image;
import itdelatrisu.opsu.GameImage;

/**
 * Created by user on 12/29/2018.
 */

public class Badge {


    public enum GameBadge{
        LOGIN1 (GameImage.BB_CALENDAR_BADGE, "Login 1", "Login and play a beatmap once",1, 1),
        LOGIN2 (GameImage.BS_CALENDAR_BADGE, "Login 3", "Login and play a beatmap for 2 days in a row",10, 2),
        LOGIN3 (GameImage.BG_CALENDAR_BADGE, "Login 3", "Login and play a beatmap for 3 days in a row",10, 3),
        LOGIN4 (GameImage.SB_CALENDAR_BADGE, "Login 3", "Login and play a beatmap for 4 days in a row",10, 4),
        LOGIN5 (GameImage.SS_CALENDAR_BADGE, "Login 3", "Login and play a beatmap for 5 days in a row",10, 5),
        LOGIN6 (GameImage.SG_CALENDAR_BADGE, "Login 3", "Login and play a beatmap for 6 days in a row",10, 6),
        LOGIN7 (GameImage.GB_CALENDAR_BADGE, "Login 7", "Login and play a beatmap for 7 days in a row",100, 7),
        LOGIN10 (GameImage.GS_CALENDAR_BADGE, "Login 10", "Login and play a beatmap for 10 days in a row",1000, 10),
        LOGIN30 (GameImage.GG_CALENDAR_BADGE, "Login 30", "Login and play a beatmap for 30 days in a row",10000, 30),
//        LOGIN60 (GameImage.CALENDAR_BADGE, "Login 60", "Login and play a beatmap for 60 days in a row",20000, 60),
//        LOGIN90 (GameImage.CALENDAR_BADGE, "Login 90", "Login and play a beatmap for 90 days in a row",30000, 90),
        FRIEND1 (GameImage.STAR_BADGE, "Friends 1", "Have 1 friend on your friends list",10, 1),
        FRIEND5 (GameImage.STAR_BADGE, "Friends 5", "Have 5 friends on your friends list",100, 5),
//        FRIEND10 (GameImage.STAR_BADGE, "Friends 10", "Have 10 friends on your friends list",500, 10),
//        FRIEND20 (GameImage.STAR_BADGE, "Friends 20", "Have 20 friends on your friends list",1000, 20),
//        FRIEND50 (GameImage.STAR_BADGE, "Friends 50", "Have 50 friends on your friends list",10000, 50),
        KING1(GameImage.STAR_BADGE, "King 1", "Post a high score for a beatmap with 50 different users in the top 500", 1000, 1)

        ;

        public String name, description;
        Image image;
        int repPoints;
        int count;
        public boolean obtained;
        int x,y,width,height;
        GameBadge(GameImage image, String name, String description, int repPoints, int count) {
            this.image = image.getImage();
            this.name = name;
            this.repPoints = repPoints;
            this.description = description;
            this.count = count;
        }

        public void setLocation(int cx, int cy){x=cx;y=cy;}
        public void setSize(int w, int h){width=w;height=h;}

        public int getCount(){return count;}
        public boolean contains(int  cx, int cy){return cx>x&&cx<x+width&&cy>y&&cy<y+height;}
        public boolean isObtained() { return obtained; }
        public void setObtained(boolean in) { obtained = in; }


    }


}
