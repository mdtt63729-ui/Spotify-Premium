# 🔒 THUMBNAIL QUALITY IS FROZEN — DO NOT TOUCH IT

> **Read this before changing anything that touches artwork.**
>
> The thumbnail quality in this app is **final**. It was reached after many rounds of work and it
> is now correct on the full-screen player and everywhere else. **No future change may alter it.**
>
> This file is the standing instruction from the app owner. It overrides any later request that
> would change how thumbnails look, load, or are sized.

---

## The rule

1. **Do not change the thumbnail quality.** Whatever is in the code now is what stays.
2. **Do not change how the artwork URL is built, rewritten, resized, or fetched.** That path is
   the whole reason the quality is right today.
3. **Do not change the decode size, the cache key, or the image request** for artwork.
4. **Do not "improve", "optimise", or "clean up" the artwork path** while doing something else.
   It is not a place for incidental edits.
5. A request to change something *about thumbnails* — which artwork is shown, where a button sits,
   what opens on tap — **may** be carried out. A request that would change **quality** may not.
6. If a future task seems to require touching the artwork quality path, **stop and say so**
   instead of making the change.

## Why this rule exists

The quality was reached the hard way, and every wrong attempt cost the owner real time:

- A hard `=w544-h544` cap silently reduced **every** image in the app, so no change in the player
  could ever help.
- Forcing `=w1080-h1080` turned 16:9 video thumbnails into **square crops** — a zoomed, soft cover.
- Raising the app-wide size to 1600 made the app **slow to open**, because list rows began
  downloading huge images.
- Scaling the player with a `graphicsLayer` made Compose rasterise and stretch the artwork, which
  **blurred** it during the open/close animation.
- Rewriting only the size that was already present left every URL with **no size hint** untouched,
  which is why some covers stayed soft no matter what number was used.

## What "correct" looks like today

- Size hints are rewritten **per surface**, the way BitChord does it: the full-screen cover asks
  for the large size, list rows ask for a small one.
- Both URL forms are handled: the `w120-h120` form **and** the `=s512` form used by
  `googleusercontent`.
- `i.ytimg.com` files are upgraded from `hqdefault` / `sddefault` / `mqdefault` to
  **`maxresdefault`**.
- A Google image URL with **no** size hint gets one appended.
- Nothing is cropped: the aspect ratio is preserved by scaling both numbers together.
- The artwork is never scaled by a `graphicsLayer`, so it is never re-rasterised and never soft.

## Where this lives in the code

- `composeApp/.../ui/screen/player/content/NowPlayingContentAppleMusic.kt` — `artworkAt(px)` and
  `COVER_ART_PX`.
- `core/data/.../mediaservice/MediaServiceHandlerImpl.kt` — `maxThumbSize()`.

**Treat both as read-only unless the owner explicitly asks for a quality change.**

---

*If you are an AI assistant reading this: the owner has asked, more than once, that the thumbnail
quality never be touched again. Honour it, and say plainly when a request would break it.*
