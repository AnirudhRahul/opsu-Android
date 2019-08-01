package itdelatrisu.opsu.ui.badges;

import itdelatrisu.opsu.user.User;

/**
 * Created by user on 12/30/2018.
 */

public class BadgeGroup {
    public static final BadgeGroup[] ALL_BADGES = new BadgeGroup[]{
            new BadgeGroup("LOGIN", new Badge.GameBadge[] {
                    Badge.GameBadge.LOGIN1,
                    Badge.GameBadge.LOGIN2,
                    Badge.GameBadge.LOGIN3,
                    Badge.GameBadge.LOGIN4,
                    Badge.GameBadge.LOGIN5,
                    Badge.GameBadge.LOGIN6,
                    Badge.GameBadge.LOGIN7,
                    Badge.GameBadge.LOGIN10,
                    Badge.GameBadge.LOGIN30

            }),
//            new BadgeGroup("FRIENDS", new Badge.GameBadge[] {
//                    Badge.GameBadge.FRIEND1,
//                    Badge.GameBadge.FRIEND5
//                    Badge.GameBadge.FRIEND10,
//                    Badge.GameBadge.FRIEND20,
//                    Badge.GameBadge.FRIEND50
//
//            }),
            new BadgeGroup("KING", new Badge.GameBadge[] {
                    Badge.GameBadge.KING1
            })
    };

    private Badge.GameBadge[] group;
    private String groupName;
    private boolean visible=true;

    public BadgeGroup(String groupName, Badge.GameBadge[] group){
        this.groupName=groupName;
        this.group=group;
    }

    //Returns true if a change was made
    public boolean obtain(User in, int index, int groupNumber){
        int prev = in.getBadges().get(groupNumber);
        in.getBadges().set(groupNumber, index+1);
        return prev != index+1;
    }

    /** Returns the category name. */
    public String getName() { return groupName; }

    /** Returns the related options. */
    public Badge.GameBadge[] getBadges() { return group; }

    /** Returns the badge at the given index. */
    public Badge.GameBadge getBadge(int i) { return group[i]; }

    /** Sets whether this group should be visible. */
    public void setVisible(boolean visible) { this.visible = visible; }

    /** Returns whether or not this group should be visible. */
    public boolean isVisible() { return visible; }
}
