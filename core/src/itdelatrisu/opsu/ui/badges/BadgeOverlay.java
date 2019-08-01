package itdelatrisu.opsu.ui.badges;

import java.util.List;

import fluddokt.opsu.fake.Color;
import fluddokt.opsu.fake.GameContainer;
import fluddokt.opsu.fake.Graphics;
import fluddokt.opsu.fake.Input;
import fluddokt.opsu.fake.SlickException;
import fluddokt.opsu.fake.gui.AbstractComponent;
import fluddokt.opsu.fake.gui.GUIContext;
import itdelatrisu.opsu.options.Options;
import itdelatrisu.opsu.ui.Colors;
import itdelatrisu.opsu.ui.Fonts;
import itdelatrisu.opsu.ui.KineticScrolling;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.user.User;

/**
 * Created by user on 12/30/2018.
 */

public class BadgeOverlay extends AbstractComponent {
    private GameContainer container;
    private Input input;
    private int containerWidth, containerHeight;
    private int badgeStartY,badgeGroupPadding;
    private int xOffset;
    private List<Integer> obtainedBadges;

    /** The badge that is being hovered */
    private Badge.GameBadge hoveredBadge;

    private int width, height;
    private BadgeGroup[] groups;

    /** The top-left coordinates. */
    private float x, y;

    /** The dimensions of an badge. */
    private int badgeWidth, badgeHeight;

    private int badgesPerRow, badgeSeperationX;

    private User currentUser;

    //Scrolling
    private final KineticScrolling scrolling;
    /** The maximum scroll offset. */
    private int maxScrollOffset;


    public BadgeOverlay(GameContainer container, BadgeGroup[] groups, int width, int height, Input input) {
        super(container);
        this.container = container;
        this.groups = groups;
        this.width = width;
        this.height = height;
        this.badgeHeight = (int) (Options.getMobileUIScale(0.5f) * 150);
        this.badgeWidth = (int) (Options.getMobileUIScale(0.5f) * 150);
        this.badgeStartY = Fonts.MEDIUM.getLineHeight() + Fonts.LARGE.getLineHeight();
        this.xOffset = Fonts.LARGE.getWidth("O");
        width -= xOffset;
        this.badgeGroupPadding = (int) (Fonts.LARGE.getLineHeight() * 1.5f);
        this.badgesPerRow = width / badgeWidth;
        this.input = input;
        if(badgesPerRow>=6)
            badgesPerRow -= 1;

//        this.currentUser=currentUser;



        badgeSeperationX = (width - badgeWidth * badgesPerRow) / badgesPerRow;
        this.scrolling = new KineticScrolling();
        scrolling.setAllowOverScroll(false);
    }

    public void updateBadges(){
        if(currentUser.getBadges()==null){
//            UI.getNotificationManager().sendNotification("Null thang for "+currentUser);
            return;
        }

        obtainedBadges = currentUser.getBadges();

        for(int i=0;i<obtainedBadges.size();i++){
            for(int j=0;j<groups[i].getBadges().length;j++){
                groups[i].getBadge(j).setObtained(false);
            }

            for(int j=0;j<obtainedBadges.get(i);j++){
                groups[i].getBadge(j).setObtained(true);
            }
        }
    }



    public void updateUser(User in){
        currentUser = in;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getY() {
        return (int) y;
    }

    @Override
    public int getX() {
        return (int) x;
    }

    //Colors
    private static final Color
            COLOR_BG = new Color(0,0,0, 100),
            COLOR_WHITE = new Color(Color.white),
            COLOR_PINK = new Color(Colors.PINK_OPTION),
            COLOR_CYAN = new Color(88, 218, 254),
            COLOR_GREY = new Color(55, 55, 57),
            COLOR_BLUE = new Color(Colors.BLUE_BACKGROUND),
            COLOR_COMBOBOX_HOVER = new Color(185, 19, 121),
            COLOR_INDICATOR = new Color(Color.black),
            COLOR_NAV_BG = new Color(COLOR_BG),
            COLOR_NAV_INDICATOR = new Color(COLOR_PINK),
            COLOR_NAV_WHITE = new Color(COLOR_WHITE),
            COLOR_NAV_FILTERED = new Color(37, 37, 37),
            COLOR_NAV_INACTIVE = new Color(153, 153, 153),
            COLOR_NAV_FILTERED_HOVERED = new Color(58, 58, 58);

    @Override
    public void render(GUIContext container, Graphics g) throws SlickException {
        g.setClip((int) x , (int) y, width, height);
        g.setColor(COLOR_BG);
        g.fillRect(x, y, width, height);
        String title = "Badges";
        String subtitle = "Get badges by completing tasks in the game!";
        Fonts.LARGE.drawString(
                x + (width - Fonts.LARGE.getWidth(title)) / 2,
                (int) (y - scrolling.getPosition()),
                title, COLOR_WHITE
        );
        Fonts.MEDIUM.drawString(
                x  + (width - Fonts.MEDIUM.getWidth(subtitle)) / 2,
                (int) (y - scrolling.getPosition()+Fonts.LARGE.getLineHeight()),
                subtitle, COLOR_PINK
        );

        renderBadges(g);

        // scrollbar
        int scrollbarWidth = 10, scrollbarHeight = 45;
        float scrollbarX = x + width - scrollbarWidth;
        float scrollbarY = y + (scrolling.getPosition() / maxScrollOffset) * (height - scrollbarHeight);
        g.setColor(COLOR_WHITE);
        g.fillRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);
        g.clearClip();
//        scrolling.setMinMax(0,1000);
    }

    private void updateHoverBadge(int mouseX, int mouseY) {
        if (mouseX < x ) {
            hoveredBadge = null;
            return;
        }
        if (mouseY < y ) {
            hoveredBadge = null;
            return;
        }


        hoveredBadge = getBadgeAtPosition(mouseX, mouseY);
    }


    public void update(int delta) {
        int mouseX = input.getMouseX(), mouseY = input.getMouseY();

        int previousScrollingPosition = (int) scrolling.getPosition();
        scrolling.update(delta);
        boolean scrollingMoved = (int) scrolling.getPosition() != previousScrollingPosition;

        updateHoverBadge(mouseX, mouseY);
        // delta updates
        if (hoveredBadge != null)
            UI.updateTooltip(delta, hoveredBadge.description, true);

    }

        /**
         * Renders all options.
         * @param g the graphics context
         */
    private void renderBadges(Graphics g) throws SlickException {
        // render all headers and options
        int cy = (int) (y + -scrolling.getPosition() + badgeStartY);
        int virtualY = 0;
        for (BadgeGroup group : groups) {
            if (!group.isVisible())
                continue;

            // header
            int lineStartY = (int) (cy + Fonts.LARGE.getLineHeight() * 0.6f);
            if (group.getBadges() == null) {
                // section header
                float previousAlpha = COLOR_CYAN.a;
//                if (group != activeGroup)
//                    COLOR_CYAN.a *= 0.2f;
                Fonts.XLARGE.drawString(
                        x + width - Fonts.XLARGE.getWidth(group.getName())
//                                -paddingRight
                        ,
                        (int) (cy + Fonts.XLARGE.getLineHeight() * 0.3f),
                        group.getName(), COLOR_CYAN
                );
                COLOR_CYAN.a = previousAlpha;
            } else {
                // subsection header
                Fonts.MEDIUMBOLD.drawString(x + xOffset , lineStartY, group.getName(), COLOR_WHITE);
            }
            cy += badgeGroupPadding;
            virtualY += badgeGroupPadding;




            int totalBadges = group.getBadges().length;
            int curbadgeIndex = 0;
            int cx = 0;
            for (int j=0; j<=totalBadges / badgesPerRow; j++) {

                for (int i = 0; i<badgesPerRow && curbadgeIndex<totalBadges; i++) {
                    renderBadge(group.getBadge(curbadgeIndex), (int) (xOffset+x+cx), (int) (y+cy));
                    group.getBadge(curbadgeIndex).setSize(badgeWidth, badgeHeight);
                    group.getBadge(curbadgeIndex).setLocation((int) (xOffset+x+cx),(int) (y+cy));
                    cx += badgeSeperationX + badgeWidth;
                    curbadgeIndex++;
                }

                cx = 0;
                cy += badgeHeight + badgeGroupPadding * 0.2f;

            }

            if(totalBadges%badgesPerRow == 0)
                cy -= badgeHeight + badgeGroupPadding * 0.2f;

        }


        scrolling.setMinMax(0, cy);
    }

    @Override
    public void setFocus(boolean focus) {

    }

    //Returns of the height of the item we are drawing
    private void renderBadge(Badge.GameBadge badge, int cx, int cy) throws SlickException {
        badge.image.setAlpha(badge.isObtained()?1f:0.5f);
        badge.image.draw(cx, cy, badgeWidth, badgeHeight);


//        g.drawImage(cx, cy, badgeWidth, badgeHeight);
//        Fonts.DEFAULT.drawString(cx , cy + badgeHeight, badge.name, COLOR_WHITE);

    }

    private Badge.GameBadge getBadgeAtPosition(int cx, int cy) {
        for(BadgeGroup group: groups)
            for(Badge.GameBadge e: group.getBadges()){
                if(e.contains(cx, cy))
                    return e;
        }
        return null;
    }

    /**
     * Returns true if the coordinates are within the overlay bounds.
     * @param cx the x coordinate
     * @param cy the y coordinate
     */
    public boolean contains(float cx, float cy) {
        return ((cx > x && cx < x + width) && (cy > y && cy < y + height));
    }

    @Override
    public void mousePressed(int button, int x, int y) {


        scrolling.pressed();

    }

    @Override
    public void mouseReleased(int button, int x, int y) {
        scrolling.released();
    }

    @Override
    public void mouseDragged(int oldx, int oldy, int newx, int newy) {
        int diff = newy - oldy;
        if (diff != 0)
            scrolling.dragged(-diff);

    }


    @Override
    public void setLocation(int x, int y) {
        this.x=x;
        this.y=y;
    }
}
