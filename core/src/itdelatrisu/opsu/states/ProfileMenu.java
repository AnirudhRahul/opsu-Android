package itdelatrisu.opsu.states;

import fluddokt.newdawn.slick.state.transition.EasedFadeOutTransition;
import fluddokt.newdawn.slick.state.transition.FadeInTransition;
import fluddokt.opsu.fake.BasicGameState;
import fluddokt.opsu.fake.Color;
import fluddokt.opsu.fake.GameContainer;
import fluddokt.opsu.fake.Graphics;
import fluddokt.opsu.fake.Image;
import fluddokt.opsu.fake.Input;
import fluddokt.opsu.fake.SlickException;
import fluddokt.opsu.fake.StateBasedGame;
import itdelatrisu.opsu.GameImage;
import itdelatrisu.opsu.Opsu;
import itdelatrisu.opsu.audio.SoundController;
import itdelatrisu.opsu.audio.SoundEffect;
import itdelatrisu.opsu.ui.Colors;
import itdelatrisu.opsu.ui.Fonts;
import itdelatrisu.opsu.ui.MenuButton;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.ui.badges.BadgeGroup;
import itdelatrisu.opsu.ui.badges.BadgeOverlay;
import itdelatrisu.opsu.user.User;
import itdelatrisu.opsu.user.UserList;

/**
 * Created by user on 12/16/2018.
 */

public class ProfileMenu extends BasicGameState {
    private GameContainer container;
    private StateBasedGame game;
    private Input input;
    private final int state;
    private MenuButton userIconButton;
    private int width, height;
    private int usableHeight;
    private int footerY;
    private float xOffset, yOffset;
    private static final int DIVIDER_LINE_WIDTH = 4;
    private User currentUser;
    private BadgeOverlay badgeOverlay;
    @Override
    public int getID() { return state; }

    @Override
    public void init(GameContainer container, StateBasedGame game) throws SlickException {
        this.container = container;
        this.game = game;
        this.input = container.getInput();
        width = container.getWidth();
        height = container.getHeight();
        footerY = height - GameImage.SELECTION_MODS.getImage().getHeight();
        usableHeight = footerY;
        yOffset = usableHeight * 0.02f;
        xOffset = width * 0.0075f;


        badgeOverlay = new BadgeOverlay(container, BadgeGroup.ALL_BADGES, (int)(width-height/2f-5*xOffset), (int)(height*0.8f), input);

    }
    public ProfileMenu(int state) {
        this.state = state;
    }

    @Override
    public void enter(GameContainer container, StateBasedGame game) throws SlickException {
        UI.enter();
        currentUser = UserList.get().getCurrentUser();


        Image userIconImage = GameImage.USER.getImages()[currentUser.getIconId()].getScaledCopy(height/2f, height/2f);

        userIconButton = new MenuButton(userIconImage, xOffset + height/4f, yOffset + height/4f);
        userIconButton.setHoverFade(0.925f);

        badgeOverlay.setLocation((int)(userIconImage.getWidth()+3*xOffset),(int)yOffset);
        badgeOverlay.updateUser(currentUser);
        badgeOverlay.updateBadges();


        UI.getNotificationManager().sendNotification("Name : " + UserList.get().getCurrentUser().getName());
        UI.getNotificationManager().sendNotification("Badges : " + UserList.get().getCurrentUser().getBadges());
        UI.getNotificationManager().sendNotification("Tops : " + UserList.get().getCurrentUser().topped);
        UI.getNotificationManager().sendNotification("Friends list : " + UserList.get().getCurrentUser().getFriendNames());

//        for(BadgeGroup group: BadgeGroup.ALL_BADGES)
//            for (Badge.GameBadge badge: group.getBadges())
//                UI.getNotificationManager().sendNotification(badge.name+" Visible?"+badge.isObtained());

    }

    @Override
    public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
        int mouseX = input.getMouseX(), mouseY = input.getMouseY();

        Image bg = GameImage.SEARCH_BG1.getImage();
        bg.drawCentered(width / 2, height / 2);

        //Draw footer bar
        g.setColor(Colors.BLACK_ALPHA);
        g.fillRect(0, footerY, width, height - footerY);
        g.setColor(Colors.BLUE_DIVIDER);
        g.setLineWidth(DIVIDER_LINE_WIDTH);
        g.drawLine(0, footerY, width, footerY);
        g.resetLineWidth();

        userIconButton.draw();
        float cy = usableHeight*0.01f+yOffset+userIconButton.getImage().getHeight();

        Fonts.LARGE.drawString(
                xOffset,
                cy,
                currentUser.getName(), Color.white
        );

        cy += Fonts.LARGE.getLineHeight();
        cy += usableHeight*0.005f;
        Fonts.MEDIUM.drawString(
                xOffset,
                cy,
                "Score: " + Integer.MAX_VALUE, Color.white
        );

//        g.setColor(Colors.BLACK_ALPHA);
//        g.fillRect(0,0,userIconButton.getImage().getWidth()*1.05f,userIconButton.getImage().getHeight()*1.05f);
        badgeOverlay.render(container, g);
        UI.getBackButton().draw(g);
        UI.draw(g);

    }

    @Override
    public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
        UI.update(delta);
        int mouseX = input.getMouseX(), mouseY = input.getMouseY();
        badgeOverlay.update(delta);
        UI.getBackButton().hoverUpdate(delta, mouseX, mouseY);
        userIconButton.hoverUpdate(delta, mouseX, mouseY);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (UI.getBackButton().contains(x, y)) {
            SoundController.playSound(SoundEffect.MENUBACK);
            ((MainMenu) game.getState(Opsu.STATE_MAINMENU)).reset();
            game.enterState(Opsu.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
            return;
        }
    }


    @Override
    public void mousePressed(int button, int x, int y) {

    }



    @Override
    public void mouseDragged(int oldx, int oldy, int newx, int newy) {

    }

    @Override
    public void keyPressed(int key, char c) {
        if(key==Input.ANDROID_BACK) {
            SoundController.playSound(SoundEffect.MENUBACK);
            ((MainMenu) game.getState(Opsu.STATE_MAINMENU)).reset();
            game.enterState(Opsu.STATE_MAINMENU, new EasedFadeOutTransition(), new FadeInTransition());
            return;
        }
    }

    @Override
    public void leave(GameContainer container, StateBasedGame game) throws SlickException {

    }

}
