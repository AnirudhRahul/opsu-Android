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
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import fluddokt.ex.DynamoDB.DynamoDB;
import fluddokt.opsu.fake.Color;
import fluddokt.opsu.fake.GameContainer;
import fluddokt.opsu.fake.Graphics;
import fluddokt.opsu.fake.Input;
import fluddokt.opsu.fake.SlickException;
import fluddokt.opsu.fake.TextField;
import fluddokt.opsu.fake.gui.AbstractComponent;
import fluddokt.opsu.fake.gui.GUIContext;
import itdelatrisu.opsu.audio.SoundController;
import itdelatrisu.opsu.audio.SoundEffect;
import itdelatrisu.opsu.options.Options;
import itdelatrisu.opsu.ui.Colors;
import itdelatrisu.opsu.ui.Fonts;
import itdelatrisu.opsu.ui.KineticScrolling;
import itdelatrisu.opsu.ui.MenuButton;
import itdelatrisu.opsu.ui.UI;
import itdelatrisu.opsu.ui.animations.AnimatedValue;
import itdelatrisu.opsu.ui.animations.AnimationEquation;

/*
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.TextField;
*/

/**
 * User selection overlay.
 */
public class UserSelectOverlay extends AbstractComponent {
	/** Listener for events. */
	public interface UserSelectOverlayListener {
		/**
		 * Notification that the overlay was closed.
		 * @param userChanged true if the user was changed
		 */
		void close(boolean userChanged);
	}

	/** Whether this component is active. */
	private boolean active;

	/** Users. */
	private List<UserButton> userButtons = new ArrayList<UserButton>();

	/** The event listener. */
	private final UserSelectOverlayListener listener;

	/** The top-left coordinates. */
	private float x, y;

	/** Dimensions. */
	private int width, height;

	/** The relative offsets of the title. */
	private final int titleY;

	/** The relative offsets of the start of the users section. */
	private final int usersStartX, usersStartY;

	/** Padding between user buttons. */
	private final int usersPaddingY;

	/** Kinetic scrolling. */
	private final KineticScrolling scrolling;

	/** The maximum scroll offset. */
	private int maxScrollOffset;

	/** The current selected button (between a mouse press and release). */
	private UserButton selectedButton;

	/**
	 * The y coordinate of a mouse press, recorded in {@link #mousePressed(int, int, int)}.
	 * If this is -1 directly after a mouse press, then it was not within the overlay.
	 */
	private int mousePressY = -1;

	/** Should all unprocessed events be consumed, and the overlay closed? */
	private boolean consumeAndClose = false;

	/** Global alpha. */
	private float globalAlpha = 1f;

	/** Textfield used for entering new user names. */
	private TextField textField;
    private TextField textFieldPassword;

	/** New user. */
	private User newUser;

	/** New user button. */
	private UserButton newUserButton;

	/** New user icons. */
	private MenuButton[] newUserIcons;

	/** States. */
	private enum State { USER_SELECT, CREATE_USER }

	/** Current state. */
	private State state = State.USER_SELECT;

	/** State change progress. */
	private AnimatedValue stateChangeProgress = new AnimatedValue(500, 0f, 1f, AnimationEquation.LINEAR);

	/** Colors. */
	private static final Color
		COLOR_BG = new Color(Color.black),
		COLOR_WHITE = new Color(Color.white),
		COLOR_GRAY = new Color(Color.lightGray),
		COLOR_RED = new Color(Color.red);

	// game-related variables
	private Input input;
	private int containerWidth, containerHeight;

	/**
	 * Creates the user selection overlay.
	 * @param container the game container
	 * @param listener the event listener
	 */
	public UserSelectOverlay(GameContainer container, UserSelectOverlayListener listener) {
		super(container);
		this.listener = listener;

		this.input = container.getInput();
		this.containerWidth = container.getWidth();
		this.containerHeight = container.getHeight();

		// overlay positions
		this.y = 0;
		this.width = (int) (containerWidth*Options.getMobileUIScale(0.5f) / 3);
		this.height = containerHeight;
		this.x = (containerWidth - width) /2;
		
		// user positions
		this.titleY = Fonts.LARGE.getLineHeight() * 2;
		this.usersStartX = (width - UserButton.getWidth()) / 2;
		this.usersStartY = (int) (titleY + Fonts.XLARGE.getLineHeight() * 1.5f);
		this.usersPaddingY = UserButton.getHeight() / 10;

		// new user
		this.newUser = new User("", UserList.DEFAULT_ICON);
		this.newUserButton = new UserButton(
			(int) (this.x + usersStartX),
			(int) (this.y + usersStartY + Fonts.MEDIUMBOLD.getLineHeight()),
			Color.white
		);
		newUserButton.setUser(newUser);
		newUserButton.setHoverAnimationDuration(400);
		newUserButton.setHoverAnimationEquation(AnimationEquation.LINEAR);

		// new user text field
		this.textField = new TextField(container, Fonts.LARGE, 0, 0, 0, 0);
		textField.setMaxLength(UserList.MAX_USER_NAME_LENGTH);
		container.removeInputListener(textField);
        this.textFieldPassword = new TextField(container, Fonts.LARGE, 0, 0, 0, 0);
        textFieldPassword.setMaxLength(UserList.MAX_USER_NAME_LENGTH);
        container.removeInputListener(textFieldPassword);
		// new user icons
		this.newUserIcons = new MenuButton[UserButton.getIconCount()];
		for (int i = 0; i < newUserIcons.length; i++) {
			newUserIcons[i] = new MenuButton(UserButton.getIconImage(i), 0, 0);
			newUserIcons[i].setHoverFade(0.5f);
		}

		// kinetic scrolling
		this.scrolling = new KineticScrolling();
		scrolling.setAllowOverScroll(true);
	}

	@Override
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() { return (int) x; }

	@Override
	public int getY() { return (int) y; }

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	/** Sets the alpha level of the overlay. */
	public void setAlpha(float alpha) {
		COLOR_BG.a = 0.7f * alpha;
		globalAlpha = alpha;
	}

	/**
	 * Returns true if the coordinates are within the overlay bounds.
	 * @param cx the x coordinate
	 * @param cy the y coordinate
	 */
	public boolean contains(float cx, float cy) {
		return ((cx > x && cx < x + width) && (cy > y && cy < y + height));
	}

	/** Activates the component. */
	public void activate() {
		this.active = true;
		mousePressY = -1;
		globalAlpha = 1f;

		// set initial state
		state = State.USER_SELECT;
		stateChangeProgress.setTime(stateChangeProgress.getDuration());
		prepareUserSelect();
	}

	/** Deactivates the component. */
	public void deactivate() { this.active = false; }

	/**
	 * Whether to consume all unprocessed events, and close the overlay.
	 * @param flag {@code true} to consume all events (default is {@code false})
	 */
	public void setConsumeAndClose(boolean flag) { this.consumeAndClose = flag; }

	@Override
	public void render(GUIContext container, Graphics g) throws SlickException {
		g.setClip((int) x, (int) y, width, height);

		// background
		g.setColor(Color.black);
		g.fillRect(x, y, width, height);
		//textField.render(container, g);
		// render states
		if (!stateChangeProgress.isFinished()) {
			// blend states
			float t = stateChangeProgress.getValue();
			if (state == State.CREATE_USER)
				t = 1f - t;
			renderUserSelect(g, t);
			renderUserCreate(g, 1f - t);
		} else if (state == State.USER_SELECT)
			renderUserSelect(g, globalAlpha);
		else if (state == State.CREATE_USER)
			renderUserCreate(g, globalAlpha);

		g.clearClip();
	}

	/** Renders the user selection menu. */
	private void renderUserSelect(Graphics g, float alpha) {
		COLOR_WHITE.a = alpha;

		// title
		String title = "User Select";
		Fonts.XLARGE.drawString(
			x + (width - Fonts.XLARGE.getWidth(title)) / 2,
			(int) (y + titleY - scrolling.getPosition()),
			title, COLOR_WHITE
		);

		// users
		int cx = (int) (x + usersStartX);
		int cy = (int) (y + -scrolling.getPosition() + usersStartY);
		for (UserButton button : userButtons) {
			button.setPosition(cx, cy);
			if (cy < height)
				button.draw(g, alpha);
			cy += UserButton.getHeight() + usersPaddingY;
		}

		// scrollbar
		int scrollbarWidth = 10, scrollbarHeight = 45;
		float scrollbarX = x + width - scrollbarWidth;
		float scrollbarY = y + (scrolling.getPosition() / maxScrollOffset) * (height - scrollbarHeight);
		g.setColor(COLOR_WHITE);
		g.fillRect(scrollbarX, scrollbarY, scrollbarWidth, scrollbarHeight);
	}

	/** Renders the user creation menu. */
	private void renderUserCreate(Graphics g, float alpha) {
		COLOR_WHITE.a = COLOR_RED.a = alpha;
		COLOR_GRAY.a = alpha * 0.8f;

		// title
		String title = "Add User";
		Fonts.XLARGE.drawString(
			x + (width - Fonts.XLARGE.getWidth(title)) / 2,
			(int) (y + titleY),
			title, COLOR_WHITE
		);

		// user button
		int cy = (int) (y + usersStartY);
		String caption = "Click the profile below to create it.";
		Fonts.MEDIUM.drawString(x + (width - Fonts.MEDIUM.getWidth(caption)) / 2, cy, caption, COLOR_WHITE);
		cy += Fonts.MEDIUM.getLineHeight();
		newUserButton.draw(g, alpha);
		cy += UserButton.getHeight() + Fonts.MEDIUMBOLD.getLineHeight();

		// user name
		String nameHeader = "Name";
		String passwordHeader = "Password";
		Fonts.MEDIUMBOLD.drawString(x + (width - Fonts.MEDIUMBOLD.getWidth(nameHeader)) / 2, cy, nameHeader, COLOR_WHITE);
		cy += Fonts.MEDIUMBOLD.getLineHeight();
		Color textColor = COLOR_WHITE;
		String name = newUser.getName();
		String password = newUser.getPassword();
        if (name.isEmpty()) {
            name = "Type a name...";
            textColor = COLOR_GRAY;
        }
        if (password.isEmpty()) {
            password = "Type a password...";
            textColor = COLOR_GRAY;
        }
        if (!UserList.get().isValidUserName(name))
			textColor = COLOR_RED;
		int textWidth = Fonts.LARGE.getWidth(name);
		int textWidthPassword = Fonts.LARGE.getWidth(password);
		int searchTextX = (int) (x + (width - textWidth) / 2);
		textField.setBound((int) x, cy, width, Fonts.LARGE.getLineHeight());
		Fonts.LARGE.drawString(searchTextX, cy, name, textColor);
		g.setColor(textColor);
		g.setLineWidth(2f);
		cy+=Fonts.LARGE.getLineHeight();
		g.drawLine(searchTextX, cy, searchTextX + textWidth, cy);
		Fonts.MEDIUMBOLD.drawString(x + (width - Fonts.MEDIUMBOLD.getWidth(passwordHeader)) / 2, cy, passwordHeader, COLOR_WHITE);
		cy += Fonts.MEDIUMBOLD.getLineHeight();
		textColor=COLOR_WHITE;

		searchTextX = (int) (x + (width - textWidthPassword) / 2);
        textFieldPassword.setBound((int) x, cy, width, Fonts.LARGE.getLineHeight());
        Fonts.LARGE.drawString(searchTextX, cy, password, textColor);
        cy += Fonts.LARGE.getLineHeight();
        g.setColor(textColor);
        g.setLineWidth(2f);
		g.drawLine(searchTextX, cy, searchTextX + textWidthPassword, cy);




		cy += Fonts.MEDIUMBOLD.getLineHeight();

//		// user icons
//		String iconHeader = "Icon";
//		Fonts.MEDIUMBOLD.drawString(x + (width - Fonts.MEDIUMBOLD.getWidth(iconHeader)) / 2, cy, iconHeader, COLOR_WHITE);
//		cy += Fonts.MEDIUMBOLD.getLineHeight() + usersPaddingY;
//		int iconSize = UserButton.getIconSize();
//		int paddingX = iconSize / 4;
//		int maxPerLine = UserButton.getWidth() / (iconSize + paddingX);
//		// start scroll area here
//		g.setClip((int) x, cy, width, height - (int) (cy - y));
//		int scrollOffset = ((newUserIcons.length - 1) / maxPerLine + 1) * (iconSize + usersPaddingY);
//		scrollOffset -= height - cy;
//		scrollOffset = Math.max(scrollOffset, 0);
//		scrolling.setMinMax(0, scrollOffset);
//		cy += -scrolling.getPosition();
//		for (int i = 0; i < newUserIcons.length; i += maxPerLine) {
//			// draw line-by-line
//			int n = Math.min(maxPerLine, newUserIcons.length - i);
//			int cx = (int) (x + usersStartX + (UserButton.getWidth() - iconSize * n - paddingX * (n - 1)) / 2);
//			for (int j = 0; j < n; j++) {
//				MenuButton button = newUserIcons[i + j];
//				button.setX(cx + iconSize / 2);
//				button.setY(cy + iconSize / 2);
//				if (cy < height) {
//					button.getImage().setAlpha((newUser.getIconId() == i + j) ?
//						alpha : alpha * button.getHoverAlpha() * 0.9f
//					);
//					button.getImage().draw(cx, cy);
//				}
//				cx += iconSize + paddingX;
//			}
//			cy += iconSize + usersPaddingY;
//		}
	}

	/**
	 * Updates the overlay.
	 * @param delta the delta interval since the last call
	 */
	public void update(int delta) {
		if (!active)
			return;

		scrolling.update(delta);
		stateChangeProgress.update(delta);
		int mouseX = input.getMouseX(), mouseY = input.getMouseY();

		// user button hover updates
		if (state == State.USER_SELECT || !stateChangeProgress.isFinished()) {
			UserButton hover = getButtonAtPosition(mouseX, mouseY);
			for (UserButton button : userButtons)
				button.hoverUpdate(delta, button == hover);
		}
		if (state == State.CREATE_USER || !stateChangeProgress.isFinished()) {
			newUserButton.hoverUpdate(delta, UserList.get().isValidUserName(newUser.getName()));
			for (int i = 0; i < newUserIcons.length; i++)
				newUserIcons[i].hoverUpdate(delta, mouseX, mouseY);
		}
	}

	@Override
	public void mousePressed(int button, int x, int y) {
		if (!active)
			return;

		if (!contains(x, y)) {
			if (consumeAndClose) {
				consumeEvent();
				listener.close(false);
			}
			return;
		}

		if (state == State.CREATE_USER) {
			textField.setFocus(true);
			textField.resetConsume();
			textField.mousePressed(button, x, y);
			textField.setFocus(false);
			if (textField.isConsumed()) {
				consumeEvent();
				return;
			}
            textFieldPassword.setFocus(true);
            textFieldPassword.resetConsume();
            textFieldPassword.mousePressed(button, x, y);
            textFieldPassword.setFocus(false);
            if (textFieldPassword.isConsumed()) {
                consumeEvent();
                return;
            }
		}
		consumeEvent();

		if (button == Input.MOUSE_MIDDLE_BUTTON)
			return;

		scrolling.pressed();
		mousePressY = y;

		if (state == State.USER_SELECT)
			selectedButton = getButtonAtPosition(x, y);
	}

	@Override
	public void mouseReleased(int button, int x, int y) {
		if (!active)
			return;

		consumeEvent();

		if (button == Input.MOUSE_MIDDLE_BUTTON)
			return;

		// check if clicked, not dragged
		boolean mouseDragged = (Math.abs(y - mousePressY) >= 5);

		mousePressY = -1;
		scrolling.released();

		if (mouseDragged)
			return;

		if (state == State.USER_SELECT) {
			if (selectedButton != null) {
				SoundController.playSound(SoundEffect.MENUCLICK);
				if (selectedButton.getUser() == null) {
					// new user
					state = State.CREATE_USER;
					stateChangeProgress.setTime(0);
					prepareUserCreate();
				} else {
					// select user
					String name = selectedButton.getUser().getName();
					if (!name.equals(UserList.get().getCurrentUser().getName())) {
						UserList.get().changeUser(name);
						listener.close(true);
					} else
						listener.close(false);
				}
			}
		} else if (state == State.CREATE_USER) {
			// add new user
			if (newUserButton.contains(x, y))
				createNewUser();
			else {
				// change user icons
				for (int i = 0; i < newUserIcons.length; i++) {
					if (newUserIcons[i].contains(x, y)) {
						SoundController.playSound(SoundEffect.MENUCLICK);
						newUser.setIconId(i);
						break;
					}
				}
			}
		}

		selectedButton = null;
	}

	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
		if (!active)
			return;

		consumeEvent();

		int diff = newy - oldy;
		if (diff != 0)
			scrolling.dragged(-diff);
	}

	@Override
	public void mouseWheelMoved(int delta) {
		if (UI.globalMouseWheelMoved(delta, true)) {
			consumeEvent();
			return;
		}

		int mouseX = input.getMouseX(), mouseY = input.getMouseY();
		if (!active)
			return;

		if (!contains(mouseX, mouseY)) {
			if (consumeAndClose) {
				consumeEvent();
				listener.close(false);
			}
			return;
		}

		consumeEvent();

		scrolling.scrollOffset(-delta);
	}

	@Override
	public void keyPressed(int key, char c) {
		if (!active)
			return;

		consumeEvent();

		// esc: close overlay or clear text
//		if (key == Input.KEY_ESCAPE) {
//			if (state == State.CREATE_USER && !textField.getText().isEmpty()) {
//				textField.setText("");
//				newUser.setName("");
//			} else
//				listener.close(false);
//			return;
//		}

		if (UI.globalKeyPressed(key))
			return;
	}
	@Override
	public void keyReleased(int key, char c) {
		if (!active)
			return;

		consumeEvent();

		// key entry
		if (state == State.CREATE_USER) {
			// enter: create user
			if (key == Input.KEY_ENTER) {
				createNewUser();
				return;
			}
		}
	}
	@Override
	public void keyType(char c) {
		if (!active)
			return;

		consumeEvent();

		// key entry
		if (state == State.CREATE_USER) {
            if(textField.isConsumed()){
			textField.setFocus(true);
			textField.keyType(c);
			textField.setFocus(false);
			newUser.setName(textField.getText());}
            else{
                textFieldPassword.setFocus(true);
                textFieldPassword.keyType(c);
                textFieldPassword.setFocus(false);
				newUser.setPassword(textFieldPassword.getText());
            }
			/*
			if (c > 255 && Character.isLetterOrDigit(c)) {
				Fonts.loadGlyphs(Fonts.LARGE, c);
				Fonts.loadGlyphs(Fonts.MEDIUM, c);
			}
			*/
		}
	}

	/**
	 * Returns the button at the given position.
	 * @param cx the x coordinate
	 * @param cy the y coordinate
	 * @return the button, or {@code null} if none
	 */
	private UserButton getButtonAtPosition(int cx, int cy) {
		if (cy < y || cx < x + usersStartX || cx > x + usersStartX + UserButton.getWidth())
			return null;  // out of bounds

		for (UserButton button : userButtons) {
			if (button.contains(cx, cy))
				return button;
		}
		return null;
	}

	/** Creates a new user and switches to it. */
	private void createNewUser() {
		SoundController.playSound(SoundEffect.MENUCLICK);
		String name = newUser.getName();
		String password=newUser.getPassword();
		int icon = newUser.getIconId();
		if (!UserList.get().isValidUserName(name)) {
			String error = name.isEmpty() ? "Enter a name for the user." : "You can't use that name.";
			UI.getNotificationManager().sendBarNotification(error);
			newUserButton.flash();
		}else if(!UserList.get().isValidPassword(password)){
			String error = password.isEmpty() ? "Enter a password for the user." : "You can't use that password.";
			UI.getNotificationManager().sendBarNotification(error);
			newUserButton.flash();
		}
		else {
			AsyncExecutor executor=new AsyncExecutor(1);
			AsyncTask t=new loginTask();
			executor.submit(t);
		}
	}
private class loginTask implements AsyncTask<Integer> {
	@Override
	public Integer call() throws Exception {
		UserList.get().getUsers().clear();
        String username = newUser.getName();
        String password=newUser.getPassword();
		boolean[] out=new boolean[2];
		out[0]= DynamoDB.database.dataBaseContainsUsername(username);
		out[1]=DynamoDB.database.dataBaseContainsUsernameAndPassword(username,password);
		if(out[0]==true&&out[1]==true) {
			UI.getNotificationManager().sendNotification("Logged in", Colors.GREEN);
			UserList.get().createNewUser(username, password);
			UserList.get().changeUser(username);
		}
		if(out[0]==true&&out[1]==false) {
			UI.getNotificationManager().sendNotification("Wrong Password(Username " + username + " already in use)", Colors.GREEN);
			newUser.setName("");
			newUser.setPassword("");
		}
		if(out[0]==false) {
			UserList.get().createNewUser(username, password);
			DynamoDB.database.addUserToDataBase(username,password);
            UserList.get().changeUser(username);
			UI.getNotificationManager().sendNotification("Account Created", Colors.GREEN);
		}
		listener.close(true);
		return 0 ;
	}
}
	/** Prepares the user selection state. */
	private void prepareUserSelect() {
		selectedButton = null;

		// initialize user buttons
		userButtons.clear();
		UserButton defaultUser = null;
		for (User user : UserList.get().getUsers()) {
			UserButton button = new UserButton(0, 0, Color.white);
			button.setUser(user);
			if (user.getName().equals(UserList.DEFAULT_USER_NAME))
				defaultUser = button;
			else
				userButtons.add(button);
		}
		if (defaultUser != null)
			userButtons.add(defaultUser);  // add default user at the end
		userButtons.add(new UserButton(0, 0, Color.white));  // create new user
		maxScrollOffset = Math.max(0,
			(UserButton.getHeight() + usersPaddingY) * userButtons.size() -
			(int) ((height - usersStartY) * 0.9f));

		scrolling.setPosition(0f);
		scrolling.setAllowOverScroll(true);
		scrolling.setMinMax(0, maxScrollOffset);
	}

	/** Prepares the user creation state. */
	private void prepareUserCreate() {
		newUser.setName("");
		newUser.setPassword("");
		newUser.setIconId(UserList.DEFAULT_ICON);
		textField.setText("");
        textFieldPassword.setText("");
		newUserButton.resetHover();
		for (int i = 0; i < newUserIcons.length; i++)
			newUserIcons[i].resetHover();

		scrolling.setPosition(0f);
		scrolling.setAllowOverScroll(false);
		scrolling.setMinMax(0, 0);  // real max is set in renderUserCreate()
	}

	@Override
	public void setFocus(boolean focus) { /* does not currently use the "focus" concept */ }
}
