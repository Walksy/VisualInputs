<h1>Visual Inputs</h1>

Shows which keys and buttons you pressed, and where in each game tick they landed.

A glass panel sits on the middle left of the screen with one row per tick (T964, T965...). Tick numbers count up to T999 and then start again from T0. Each row is a 0 to 50 ms bar, and every input is marked on it where the game received it, with a label underneath such as `#206 USE 30ms`. The current tick is green and a red playhead shows how far into it you are.

---
### Features:

- Inputs are timed from the game's own input events (GLFW, or SDL on 26.3) the moment they arrive
- Labels name the action the key is bound to (ATTACK, USE, JUMP, SLOT 1...), or the key itself when it isn't bound
- Every input is numbered, so you can tell exactly which press went where
- Coloured by type: left click, right click, other mouse buttons and keyboard
- Optional Flushed Inputs widget above the timeline, listing which clicks each tick actually handled and in what order
- Glass or Minecraft style, with adjustable glass blur
- Holds the last inputs on screen once you stop, so they stay readable
- Row count setting, plus optional release markers
- Move and resize both widgets with WalksyLib's Edit HUD

---
**Credits**

- Mod: Walksy
