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

package itdelatrisu.opsu;

import java.net.URI;

/**
 * Project-specific constants.
 */
public class OpsuConstants {
	/** Project name. */
	public static final String PROJECT_NAME = "opsu!";

	/** Project name. */
	public static final String VERSION = "v1.15";

	/** Project author. */
	public static final String PROJECT_AUTHOR = "@itdelatrisu + @fluddokt + @anirudhrahul";

	/** Website address. */
	public static final URI WEBSITE_URI = URI.create("https://play.google.com/store/apps/details?id=fluddokt.opsu.android");

	/** Repository address. */
	public static final URI REPOSITORY_URI = URI.create("https://github.com/AnirudhRahul/opsu");

	/** Credits address. */
	public static final URI CREDITS_URI = URI.create("https://github.com/AnirudhRahul/opsu/blob/master/CREDITS.md");

	/** Privacy Policy address. */
	public static final URI PRIVACY_URI = URI.create("https://github.com/AnirudhRahul/opsu-Android/blob/master/privacyPolicy.txt");

	/** Issue reporting address. */
	public static final String ISSUES_URL = "https://play.google.com/store/apps/details?id=fluddokt.opsu.android";


	// This class should not be instantiated.
	private OpsuConstants() {}
}
