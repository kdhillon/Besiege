Besiege
=======
by Kyle Dhillon

May 2013 - present

Besiege is a single-player real-time strategy and conquest game, written in Java for desktop and Android.
Command a custom army in a fully automated procedurally generated world of rivaling factions and kingdoms.
Win battles and earn glory, conquer cities and castles, and declare yourself ruler of your very own faction.

Map:
Besiege's island map is procedurally generated for each playthrough, using Voronoi graphs, Delaunay triangulation,
and Perlin noise functions, in a Java based off the methods described here:
http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/
The map is populated with over 100 cities, castles and villages, hundreds of armies, and plenty for the player to explore.

AI:
Besiege's AI is fully-automated, meaning if left alone without player participation the world can run smoothly forever.
Rival factions will develop, kingdoms will fall and new ones will arise to take their place.

=======
<pre>
Progress:

Day 1: background map + camera
Day 2: more camera
Day 3: cities and army representation
Day 4: movement and battles
Day 5: player controls and battles (pausing)
Day 6: player controls
Day 7: installed and learned basic LibGDX
Day 8: began translating code from Slick to Libgdx
Day 9: continued translating code
Day 10: finished translating and began basic AI
Day 11: added unit types and got Android functionality
Day 12: added garrisoning and improved running
Day 13: added Patrol army type and waiting
Day 14: added Merchant army type
Day 15: added new textures for everything
Day 16: added farmers and villages
Day 17: added global clock, time, and fast forward
Day 18: added morale and battle icon
Day 19: added army money and raiding parties
Day 20: began making HUD using scene2d UI (minimap failed)
Day 21: worked on HUD
Day 22: finished basic template for HUD
Day 23: continued UI and worked on bottom panel and log
Day 24: continued UI, and worked on character and attributes
Day 25: continued UI, worked on Party and Soldier
Day 26: worked on Weapon and Soldier upgrade tree
Day 27: worked on PartyTypes and UpgradePanel
Day 28: recreated Battle using new soldier and party types
Day 29: worked on PanelBattle and Battle
Day 30: added PanelLocation and rewards for battles
Day 31: began working on interactions with locations
Day 32: worked on army interaction and added los
Day 33: added fog of war, split map, and los/fog toggle
Day 34: added panelhire, Bandits, cleaned up Battle
Day 35: added Faction and crests
Day 36: added Faction crests and basics of diplomacy
Day 37: added garrisons and Siege
Day 38: worked on Siege and expanded player-Location interactions
Day 39: added army autorepairing, worked on Noble
Day 40: worked on Noble
Day 41: added Perlin functionality to Hoten's/Amit's procedural map generation
Day 42: modified Army's target-based navigation to support Paths with multiple nodes
Day 43: added randomly generated map to game
Day 44: worked more on A* pathfinding
Day 45: still working on Pathfinding
Day 46: finally got basic unoptimized A* pathfinding to work with some bugs
Day 47: improved A* pathfinding, optimized visibility graph 10 times faster
Day 48: worked on running, reduced freezing, removed islands from map 
Day 49: removed more freezing, improved graph search, cleaned map edges
Day 50: began profiling for bottlenecks, optimized closestHostileArmy()
Day 51: added spheres of influence, fixed some patrol and farmer bugs
Day 52: fixed freezing and border glitch issues, consolidated distBetween()
Day 53: made village spawns not based on cities, added WebGL (HTML5) functionality
Day 54: deployed to google app engine besiege-game.appspot.com
Day 55: drew out army control flow diagram and restructured Army.act()
Day 56: fixed running bug, fixed farmer bug
Day 57: fixed Noble and Patrol stuck bug, fixed faction borders
Day 58: fixed Merchant bug, added main menu, uploaded to princeton.edu
Day 59: added targetOf, fixed containing polygons bug
Day 60: fixed player offset bug, fixed retreat time = 1 bug,
Day 61: added castle class, cleaned faction location management 
Day 62: fixed one major memory leak bug
Day 63: fixed major bug that would call A* very frequently (armies would still follow garrisoned armies)
Day 64: added texts and crests for city names
Day 65: began working on battlestage
Day 66: added custom weapons to battlestage
Day 67: integrated battlestage into game
Day 68: added additional features to battlestage
Day 69: added different battle environments to battlemap
Day 70: added horses and archers to battlestage
Day 71: added cover, firing animation, shields to battlestage
Day 72: added additional maptypes and formations to battlestage
Day 73: added walls and ladders to battlestage



todo:
now:
make sieges more realistic (make walls not flat, add towers, add roads, catapults)
make battlestage have same EXP, gold, and morale rewards as regular battles.


mostly solved!
big problem: heap is growing very fast with objects, then being garbage collected so all the objects go away. try to prevent this somehow. This isn't soldiers, because they're not created and destroyed. Must be some other problem. Pathfinding maybe. Growing up to using 200mb at max use. should be much lower. Shit ton of memory leaks beacuse garbage collection gets rid of a billion things.


are these actually problems:
implement army repairing based on wealth (?)
allow joining battles (?)
some armies just get "stuck" after battle - find out why (?)
armies can't garrison in their own towns sometimes (?) 
fix glitch where player can't move after winning battle (?)
make bandits not seige (?)



later:

make a realistic world:
nobles are not besieging for some reason. figure out why. also allow besieging of castles.

gameplay:



add resting - in cities and outside - for player and others.
implement "honor" and baronage for player
change "back" button to use a stack
add populations
add partyCap for nobles and player
fix " upgrade for flail not found!" (allow veteran upgrade)
make farmers not spawn immediately!
give names to each region (and village names will be the same), and castle names will be "____ castle" or "____ fortress" 
add city garrison/wealth management
fix "economy"


where should I put these? could put them on corners or centers, if corners, how to make sure they don't share names with a city or castle? 
use completely different names...? ok
Belvoir Castle
Belvoir Fortress
Belvoir Stronghold
Belvoir Ruins

engine:
also figure out why some things happen twice?
fix army following--path doesn't account for borders when chasing--should follow same path as party.
use shape renderer to draw map instead of drawing entire map (large) every time, see if improvements

optimizations:
use kd tree for towns, villages, and centers
improve battle mechanic! (don't just RNG every frame...)

map improvements:
fix visibility graph with penninsulas
add castles
add rivers (just more impassable edges--and check for in between corners)
utilize map areas (farmland = more wealth, mountains make you slow, etc?)

aesthetics:
add rain/lightning!
add cool loading screen 
consolidate battlePanel display using partyPanel's
smooth out camera centering
add minimap (use clipping?)
improve asthetics of map (add rough edges and noise/textures, roads, forests, other details)
test on android
add ruins

castles:
serve as military strongholds (better defense than most towns, more troops, not much economic benefit but important for territorial control. Spawns scouting parties.

final map goals: 
40 cities (or bigger!)
25 castles
10 factions (6 large, 4 small) and bandits
areas bandits spawn, bandit camps kinda like villages?
sphere of influence

inefficiencies:
army detect nearby (improved on day 50)
party check upgrade

adding edge noise: add noise to each Voronoi edge before drawing, making sure still hits endpoints and crosses Delaunay edge. Use Amit's recursive function in NoisyEdges.as
(can do this either just asthetically or actually break down every line into much smaller lines--probably slower pathfinding, etc but more natural)

Level/Tier info:
Tr  Lvl atk def spd
0   1   1   1   1   Farmer
1   3   1   1   2   Vet. Farmer
2   5   1   2   2   Militia
3   7   2   2   2   Vet. Militia
4   10  2   3   2   Tier 4 (spearman etc)
5   12  3   3   2   Tier 5 (vet spearman)
6   15  3   3   3   Tier 6 (pikeman)
7   18  3   4   3   Tier 7 (vet. pikeman)
8   22  4   4   3   Tier 8 (pikemaster)
9   25  4   4   4   Tier 9 (vet pikemaster)

World goal: 
The player inhabits a vast kingdom comprised of many factions of various sizes, each controlling
their own sets of locations including cities, castles and villages. Each faction is trying to maximize
their own influence and wealth by conquering more territory and trading. Factions at war develop bitter
rivalries, factions at peace forge uneasy alliances. Bandits and thieves raid trade routes and farmers, 
and rogue rebel factions occassionally arise. Noblemen command sizable parties to attack other factions
and defend their own, while bandit warlords can rise up to challenge them. 
(think about adding a "party level" as parties win more and more battles?)

AI goal: (requires a lot of 'tuning')
A full automated thriving economy exists, and the gameworld should be able to run without player
interaction for days without imbalance occurring. Powerful empires should fall given enough time,
and new ones should rise up to take their place. Factions, cities, and parties on average should 
have an average wealth that remains generally the same, with some getting very rich and some very poor.

Gameplay goal:
Begin by leading a ragtag band of soldiers, trying to gain wealth, fame, and power. 
As you, their leader, become more respected and capable, your party can grow stronger. By honing your party,
you can try taking on bigger and stronger targets, determining your relation with various factions.
Once strong enough, you can take your shot at capturing a city or town. Then you can create your own faction
and declare yourself as king. Other factions will take notice and may react violently, opening up opportunities
for more expansion or annihilation. You should be able to organize your own merchants, patrols, siege parties,
etc.

army types and purposes:

    Farmers - created at villages, farm to gather wealth for cities and villages
    (Village Patrol) - patrols around villages when they become wealthy enough
    
    Merchants - created at cities, trade between cities to gather wealth for cities
    Patrol - created at cities, patrol around cities when wealthy enough
    
    (Siege Party)
    (Travelers)
    (Pilgrims)
    (Monks)
    (Scouts)
    (Horsemen)
    (Barbarians)
    (Deserters)
    (Shepherds)
     Noblemen - fight each other, raid villages, and besiege cities
        (King) (150+?)
        Archduke
        Prince
        Duke (100+)
        Marquis (80-100)
        Earl (Count)(60-80)
        Baron (40-60)
        (Knights are not noble)
    
    Bandits - created randomly, attack farmers, patrols (raid villages?)
    (Raiders) - created randomly, attack farmers, patrols, and raid villages? 
    
    Gameplay:

control a party on the map. 
Party screen/window contains info about who is in your party, upgrades, etc.
Character screen/window contains player info.

tips:
use object pooling for Armies (allows reuse and efficient memory use)
when drawing background image, disable blending!
or perhaps scene2d makes the most sense for this type of game (actors and stage!)
^definitely makes the most sense (actions, draws all together, etc)

tuning up later:
    check maxSpritesInBatch (int) field of SpriteBatch after running the program and set max to that number
   
ideas:
    make battles 2d fights with rpg maker style characters
        more details: require separate engine, allow player to control units like rts, lay out on grid formation. cool idea
    make visits to cities 2d rpg maker style
    make a plot where you play as both the good guy and the bad king he's trying to defeat (through flashbacks to his rise to power) - focus on the moral dilemmas and parallelisms?
   future games http://en.wikipedia.org/wiki/Yaoguai
   
</pre>
