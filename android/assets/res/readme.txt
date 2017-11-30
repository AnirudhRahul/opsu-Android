opsu!

opsu! is an unofficial open-source client for the rhythm game osu!, written in Java using Slick2D and LWJGL (wrappers around OpenGL and OpenAL).

opsu! runs on Windows, OS X, and Linux. A libGDX port also supports Android devices.

Getting Started

opsu! requires "beatmaps" to run, which contain the songs and gameplay data. These can be downloaded directly through opsu! in the downloads menu, or manually from the osu! website (requires registration) and mirror sites like Bloodcat. Place any manually downloaded beatmaps (in .osz format) in the Import/ directory for opsu! to unpack them automatically.

The beatmap directory can be changed by setting the "BeatmapDirectory" value in the generated configuration file.

First Run

opsu! will parse all beatmaps when launched, which can take a while for the first time. If no beatmaps are found, the game will prompt you to download some to get started.

Game settings can be changed in the options menu, accessed by clicking the "Other Options" button in the song menu. The "Music Offset" value will likely need to be adjusted initially, or whenever hit objects are out of sync with the music.

Directory Structure

The following files and folders will be created by opsu! as needed:

    opsu.cfg: The configuration file. Most (but not all) of the settings can be changed through the options menu.
    opsu.log: The error log. All critical errors displayed in-game are also logged to this file, and other warnings not shown are logged as well.
    Songs/: The beatmap directory. The parser searches all of its subdirectories for .osu files to load.
    Skins/: The skins directory. Each skin must be placed in a folder within this directory. Any game resource (in res/) can be skinned by placing a file with the same name in a skin folder. Skins can be selected in the options menu.
    Replays/: The replay directory. Replays of each completed game are saved as .osr files, and can be viewed at a later time or shared with others.
    Import/: The import directory. All beatmap packs (.osz) and skin packs (.osk) are unpacked to the proper location. All replays (.osr) are moved to the replay directory, and their scores saved to the scores database.


opsu! - an open-source osu! client
Copyright (C) 2014-2017 Jeffrey Han + fluddokt

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.