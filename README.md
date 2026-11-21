# SUR — Immersive Music Showcase

Static, framework-free showcase website for the **Sur** music app. Designed for GitHub Pages (safe under repository subpaths) and direct local preview.

## Deploy to GitHub Pages (3 Steps)

1. **Create & push repository:** Create a new GitHub repository and push the contents of this project (`index.html`, `.nojekyll`, `assets/`, `css/`, `js/`, `README.md`) to the `main` branch.
2. **Enable Pages:** Open your repository on GitHub → **Settings** → **Pages** → under *Build and deployment*, set **Source** to **Deploy from a branch**, select `main` and `/ (root)`, then click **Save**.
3. **Configure links & absolute OG URL:** Once deployed, edit `js/config.js` to set your real `downloadUrl`, `githubUrl`, and `siteUrl` (full `https://...` Pages URL so `og:image` resolves cleanly for social crawlers), then push the update.

## Replacing Screenshots (Zero Code Changes)

Drop your real 1080×2400 WebP screenshots (target ≤ 150KB each, hero ≤ 180KB) into `assets/images/` using these exact filenames:

- `sur-home.webp` — Hero, Interface Beat 1, Gallery
- `sur-together.webp` — Interface Beat 2 (Listen Together room)
- `sur-library.webp` — Interface Beat 3 (Library)
- `sur-discover.webp` — Discover, Feature Chapter 01 (Song mixes & Create sheet)
- `sur-settings.webp` — Feature Chapter 02 (Interface & Liquid Glass customization crop)
- `sur-player.webp` — Listen (lower crop), Full Player, Feature Chapter 03, Details (Playback/Artwork/Controls crops)
- `sur-lyrics.webp` — Immersion, Feature Chapter 04, Details (Lyrics crop)
- `sur-queue.webp` — Details (Queue crop)
- `sur-together-server.webp` — Gallery floating layer (Listen Together server settings)
- `sur-account.webp` — Gallery edge layer (Account & Song mixes)
- `og-sur.png` — 1200×630 Open Graph preview image

If an image is missing or fails to load, the `.sur-shot` container automatically displays its dark fallback placeholder.
