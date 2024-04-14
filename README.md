[![Discord](https://img.shields.io/discord/1227616718101155912?style=plastic&logo=discord)](https://discord.gg/hg9HgJGkxV)
[![Modrinth](https://img.shields.io/modrinth/dt/Cp13oI7e?style=plastic&logo=modrinth)](https://modrinth.com/mod/eggsentials) I expect to use this but that is not on modrinth just yet
[![Github Releases](https://img.shields.io/github/downloads/HacktheTime/BBsentials/total?style=plastic&logo=github)](https://github.com/HacktheTime/bbsentials/releases)

# eggsentials

This Mod is maintained by Hype_the_Time and is part of the BBsentials Eco System.

Mod Dependencies:

Recent: mod menu, cloth config, fabric api

~~1.8.9: nothing~~ 1.8.9 is not supported at least yet. If you want feel free to make a PR because I do not want to waste my time if Foraging comes out soon anyway.

Currently known Problems:

- There is no 1.8.9 mod
   - Solution: idc about 1.8.9
- Waypoints are sometimes just incredibly bright
    - Im aware that waypoints get brighter the later they are rendered. This is a bug in the rendering code i copied from a different project of mine but which is not originally by myself. Im not great for Rendering Code and stay simple with it usually.
- It breaks on other Servers
  - Solution: Start using Prism Launcher or other muli instances tools to use different instances for your needs. I recommend Prism because it has internal mod update code for modrinth which is ued for newer mod versions by me and most other Mod makers nowadays.
  -  Reason: Make it so It only activates while on Hypixel itself is not too complicated but detecting when you leave Hypixel etc and then not processing is a lot harder. Just use different instances. This means you don't need to change your configs for different playstyles etc either.
- Connecting Problems:
  - The Mod is not connected sometimes
  - Solution: swap servers this should (hopefully) fix the issue. if the server is down the server is down which may cause a bigger performance hit perhaps? If its problematic remove the mod at least temporarily.
  - Reason: The mod is unfortunately not able to detect when she gets disconnected due too the PC going into slkeep which is the reason I needed to add a tracking System which makes it so whenever you swap servers it checks whether the connection is open or was closed without the host informing about the close (disconnect)
  - The mod is not connected even though the Server is up
    - This can happen if the Server is down for longer than originally anticipated. In that case you need to restart MC since the connection was closed by the host "properly" and the reconnect times went past while the Server was still down. 



**Do not expect any kind of support during Bingo.**

The newest branch is usually hypeswap, but that may be unstable

Too other developers. This is part of the BBsentials Code modified for Eggsentials. So this is why some stuff which is
not used is here / the structure is here.
