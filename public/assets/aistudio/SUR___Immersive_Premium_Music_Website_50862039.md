# SUR — Immersive Premium Music Website

*Product Requirements Document v2.0 · Build spec for Google AI Studio · Static site for GitHub Pages*

## 0. How to read this document

- **Audience:** the AI builder (Google AI Studio) and the person who maintains the result. Every rule is written to be executable and testable.
- **Priority tags:** **P0** = release-blocking, **P1** = should ship, **P2** = polish if time allows.
- **If two rules conflict, this order wins:** §2 Non-negotiables › exact values in §5–§9 › section specs in §10 › any other prose.
- **What v2 fixes in v1:** one canonical section order (v1 listed two different ones); measurable motion timings; a scroll engine where reversibility is guaranteed by design, not patched in; hero type sizes that no longer overflow on 320px phones; a defined mobile fallback for every pinned or horizontal scene; a placeholder, config and no-JS system; a QA script; and a ready-to-paste builder prompt (§20).

## 1. Product definition

- **Name / type:** Sur — an immersive showcase website for the Sur music app. Not a landing page. A visitor should feel they entered an interactive film.
- **Stack:** vanilla HTML5 + CSS3 + JavaScript. No framework, no build step, no backend, no database. **Hosting:** GitHub Pages, safe under a repository subpath.
- **Emotional arc:** Dark → Elegant → Warm → Cinematic → Interactive → Premium → Immersive.
- **Core idea:** do not explain Sur — let the visitor feel it. Few words, huge type, large screenshots, motion that carries meaning.
- **Non-goals:** SaaS template, feature grid, pricing, login, blog, analytics, cookie banner, any server code.
- **Audience:** music-first, design-conscious phone users. Assume most visits are on mobile, so the phone experience is first-class and cinematic, never a shrunken desktop page.
- **Success criteria (measurable):**
  1. Within 1 second of load the visitor sees a dark hero with the SUR wordmark and headline.
  2. Every viewport holds exactly one idea, one dominant visual, minimal supporting text.
  3. Lighthouse (mobile): Performance ≥ 90; Accessibility, Best Practices and SEO ≥ 95.
  4. Zero horizontal overflow from 320px to 2560px, portrait and landscape.
  5. Replacing a screenshot file requires zero code changes.
  6. Scrolling up reverses every major animation smoothly.

## 2. Non-negotiables (P0)

1. Static only: `index.html` + assets. GitHub Pages serves it with no server, backend, database or Node runtime.
2. No horizontal scroll or clipped content at any width from 320px to 2560px.
3. Every major scroll animation is a **pure function of scroll position** (see §8.3), so it reverses automatically when the user scrolls up.
4. Never block native zoom or pinch: no `maximum-scale`, no `user-scalable=no`. No scroll-jacking, no scroll-snap hijack, no custom scroll physics.
5. Animate only `transform`, `opacity`, `filter` (sparingly) and `clip-path`. Never animate width, height, margin, top or left.
6. `prefers-reduced-motion: reduce` is fully supported and the page stays 100% usable.
7. Content, screenshots and CTAs are visible and readable with JavaScript disabled.
8. Relative asset paths only (`assets/images/x.webp`), never `/assets/...`.
9. No fake download or GitHub URLs. All links come from `js/config.js`.
10. No secrets, tokens or keys in source. No runtime dependency except an optional self-hostable font.
11. Orange stays an accent: at most about 8% of any viewport.
12. No bounce or overshoot easing anywhere.

## 3. Technical architecture

- **Scripts:** classic `<script defer>` files loaded in order (config → utils → scroll → animations → navigation → interactions → main), each an IIFE attached to a single `window.Sur` namespace. This works on GitHub Pages and also when `index.html` is opened directly from disk (ES modules do not work over `file://`).
- **CSS:** separate files linked in order: reset, variables, base, typography, layout, components, animations, responsive, accessibility.
- **Browser support:** last two versions of Chrome, Edge, Firefox, Safari; iOS Safari 15.4+. Use `@supports` fallbacks: `height: 100vh; height: 100svh;`.
- **Pages:** one page, hash navigation only (`#hero #experience #player #features #gallery #download`). No router. Anchor links use smooth scrolling (disabled under reduced motion) with `scroll-margin-top: calc(var(--nav-h) + 12px)`.
- **Deploy files:** `.nojekyll` at the root, `README.md` with three-step deploy instructions.

## 4. Project structure

```
SUR-WEBSITE/
├── index.html
├── .nojekyll
├── README.md
├── assets/
│   ├── images/   sur-home.webp  sur-search.webp  sur-player.webp
│   │             sur-lyrics.webp  sur-library.webp  sur-discover.webp
│   │             og-sur.png (1200x630)
│   ├── icons/    favicon.svg
│   └── fonts/    (optional self-hosted Manrope woff2)
├── css/   reset · variables · base · typography · layout · components
│          animations · responsive · accessibility   (.css)
└── js/    config · utils · scroll · animations · navigation
           interactions · main   (.js)
```

## 5. Design tokens (`css/variables.css`) — authoritative

```css
:root {
  /* surfaces */
  --sur-bg: #090807;  --sur-bg-2: #0D0A08;
  --sur-surface: #15100D;  --sur-surface-2: #1B1410;  --sur-deep: #050403;
  /* orange (accent only) */
  --sur-orange: #FF8A5B;  --sur-orange-soft: #FFB18D;
  --sur-orange-tint: rgba(255,138,91,.16);  --sur-orange-glow: rgba(255,138,91,.22);
  /* text */
  --sur-white: #FFFFFF;
  --sur-text-2: rgba(255,255,255,.72);
  --sur-text-3: rgba(255,255,255,.46);   /* only for 18px+ or decorative text */
  --sur-label: rgba(255,255,255,.62);    /* micro labels, passes AA */
  --sur-line: rgba(255,255,255,.08);
  /* shape */
  --sur-radius-s: 12px; --sur-radius-m: 20px; --sur-radius-l: 28px; --sur-radius-pill: 999px;
  /* motion */
  --sur-ease: cubic-bezier(.22,1,.36,1);
  --sur-ease-inout: cubic-bezier(.65,0,.35,1);
  --dur-micro: 200ms; --dur-btn: 260ms; --dur-reveal: 700ms; --dur-cine: 1000ms;
  /* layout */
  --gutter: clamp(20px, 5vw, 72px);
  --container: 1360px;
  --nav-h: 64px;
  --safe-top: env(safe-area-inset-top, 0px);
  --safe-bottom: env(safe-area-inset-bottom, 0px);
  --vh-full: 100vh;
}
@supports (height: 100svh) { :root { --vh-full: 100svh; } }
```

Color rules: body background is `--sur-bg` with slow tonal transitions to `--sur-bg-2` and `--sur-deep`. Pure `#000` is allowed only as a gradient end. Text contrast must meet WCAG AA (4.5:1 body, 3:1 large). `--sur-text-3` may not be used for text under 18px.

## 6. Typography system

- **Family:** Manrope (variable, weights 200–800), one family site-wide. Prefer self-hosting `assets/fonts/` with `font-display: swap` and a metric-adjusted system fallback (`size-adjust`, `ascent-override`) so swapping causes no layout jump. If using Google Fonts, add `preconnect` and a fallback stack: `'Manrope', system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif`.
- **Weights:** display 650–700, titles 600, body 400–500, labels 600.
- **Scale (fluid):**
  - `--fs-wordmark` (SUR): `clamp(3rem, 14vw, 11.25rem)`, line-height .88, tracking -0.04em
  - `--fs-hero` (headline): phones `clamp(2.5rem, 12vw, 4.5rem)`; from 768px `clamp(3.5rem, 7.2vw + .5rem, 9.5rem)`; line-height .92, tracking -0.035em
  - `--fs-display` (section statements): phones `clamp(2.25rem, 10vw, 4.5rem)`; from 768px `clamp(3.5rem, 8vw, 8.5rem)`; line-height .95, tracking -0.03em
  - `--fs-giant` (DISCOVER / LISTEN / FEEL / REPEAT): `clamp(2.5rem, 13.5vw, 14rem)`, weight 700, uppercase
  - `--fs-title`: `clamp(1.75rem, 4vw, 3.5rem)`; `--fs-lead`: `clamp(1.0625rem, 1.4vw + .6rem, 1.5rem)`; `--fs-body`: 1rem; `--fs-label`: .75rem (12px)
- **Labels** (`SUR / 01`, `SUR / PLAYER`): 11–13px, uppercase, tracking .12–.18em, weight 600, color `--sur-label`.
- **Overflow-proof rule (P0):** v1 specified a hero of `clamp(64px, 11vw, 180px)`. That makes the line `felt differently.` about 1,450px wide at 1440px and about 600px wide on a 320px phone — it would overflow. v2 sizes type by the **longest line**: longest line width (≈ characters × 0.56em) must be ≤ 100vw − 2×gutter. Hero lines are `Music,` / `felt differently.` from 768px up and `Music,` / `felt` / `differently.` on phones. The builder must verify at 320, 360, 390 and 1440px.
- Use `text-wrap: balance` on headings, `overflow-wrap: break-word` as a last-resort safety net, and explicit `<br>` (hidden or shown per breakpoint) for intended line breaks. Never rely on `white-space: nowrap` for display type.

## 7. Layout, viewport and responsive system

- **Breakpoints:** phone < 600px · tablet 600–1023px · laptop 1024–1439px · desktop 1440–1919px · ultrawide ≥ 1920px. Add a **short-landscape** query `(max-height: 500px) and (orientation: landscape)` that disables pinning, shrinks display type and shows only headline + screenshot + CTA.
- **Container:** content max-width `--container` (1360px), centered, padding `--gutter`. Hero and cinematic visuals may extend beyond the container, but only inside a clipping stage.
- **Ultrawide:** content stays centered at the container width; extra space becomes ambient glow and negative space. Type size is capped by the `clamp` maximums above.
- **Viewport units:** never a bare `100vh`. Full-screen sections use `min-height: var(--vh-full)` (svh) so mobile address-bar movement causes no jump. Pinned stages use `height: 100svh` with a `100vh` fallback. `100dvh` only for the mobile menu overlay; `100lvh` only for decorative backgrounds that may extend under bars.
- **Overflow rules (P0):** `html, body { max-width: 100%; overflow-x: clip; }`. **Never** use `overflow: hidden` or `auto` on any ancestor of a sticky stage — it silently breaks `position: sticky`. Use `overflow: clip` on stages and wrappers instead. Images: `max-width: 100%; height: auto`. Wide screenshots are clipped by their stage, never by the page.
- **Viewport meta:** `<meta name='viewport' content='width=device-width, initial-scale=1, viewport-fit=cover'>` (no zoom restrictions).
- **Safe areas:** nav top padding adds `--safe-top`; fixed bottom CTAs and the mobile menu add `--safe-bottom`; landscape also respects left/right insets.
- **Foldables / tablets:** fully fluid, no device sniffing. Tablets use an intermediate composition: two-column where it helps, never a stretched desktop layout.
- **Touch:** every target ≥ 44×44px, no hover-dependent function. Desktop gets hover states, and optional magnetic CTA only under `(hover: hover) and (pointer: fine)`.

## 8. Motion system

### 8.1 Principles

Motion is used for **transition, reveal, depth, emphasis and storytelling only**. Nothing loops except the hero screenshot's slow float (±6px, 7s, paused off-screen and under reduced motion). No bounce, no overshoot, no spinners.

### 8.2 Timing

- Micro interaction 150–250ms · button 200–300ms · element reveal 500–800ms · cinematic transition 700–1200ms. Not everything shares one duration.
- Primary easing `--sur-ease` = `cubic-bezier(.22,1,.36,1)`. Scroll-linked values use linear mapping (the scroll itself is the easing).
- Stagger: 100ms steps (`--i` × 100ms), max 4 steps per group.

### 8.3 Scroll engine (`js/scroll.js`) — P0

**Design principle: animation state is a pure function of scroll position.** Nothing is ‘played’, so everything reverses by itself.

- One passive `scroll` listener plus one `requestAnimationFrame` loop that runs only while the page is scrolling (and for 120ms after). It stores `y`, `dir` (+1/−1) and velocity, and sets `document.documentElement.dataset.dir = 'down' | 'up'`.
- A **scene** is any element with `data-scene`. Two types: `pass` (p = 0 when the scene’s top reaches the bottom of the viewport, p = 1 when its bottom leaves the top) and `pin` (a tall track containing a `position: sticky; top: 0; height: 100svh` stage; p = scrolled distance ÷ (track height − viewport height)).
- Per frame, for **active scenes only** (IntersectionObserver with `rootMargin: 20% 0px` marks them), compute p and write `--p` (0–1) on the scene. Sub-timelines use `data-ranges='0-.2, .2-.6, .6-1'`, and JS writes `--p1`, `--p2`, `--p3`, each remapped to 0–1 and clamped. CSS expresses all motion with `calc()`, for example: `transform: translate3d(0, calc(var(--p1) * -8vh), 0) scale(calc(.42 + var(--p2) * .58)); opacity: calc(1 - var(--p3) * 1.4);`
- Optional damped value `--ps` (lerp 0.14) may drive the screenshot only — never text — for a silky feel. Native scrolling is never intercepted.
- **Measure outside the frame loop:** offsets and heights are cached on load, `resize`, `orientationchange`, font load, image load (use `ResizeObserver`). The frame loop reads cached numbers and only writes custom properties — no layout thrash.
- Pause everything when `document.hidden`. Set `will-change: transform` only on active scenes, remove it when inactive.
- **Discrete reveals** (`[data-reveal]`): IntersectionObserver toggles `.is-in` in **both directions** (threshold .25, `rootMargin: -8% 0px`). Hidden state: `opacity: 0; transform: translate3d(0, var(--enter-y), 0)`. `--enter-y` is `+32px` when `html[data-dir='down']` and `-32px` when `up`, so entering and leaving feel mirrored. Blur (≤ 8px) is allowed on display headings only, and is removed on phones.
- **Sharpness rule:** screenshots are laid out at their **final, largest** size and animated with `scale` from smaller values up to 1. Never scale a raster above 1, or UI text will soften.
- **Parallax:** background layers ±10px to ±24px over the scene range, via the same `--p`. Phones: factor ×0.4 or off. Never more than subtle.
- **No-JS and reduced motion:** see §13 and §16.

### 8.4 Performance rules

- 60 FPS target. Max one rAF loop for the whole site. Passive listeners only. No scroll handler touches the DOM except `style.setProperty` on active scenes.
- Glow is a pre-rendered `radial-gradient`, **not** `filter: blur()` on large layers. Avoid `backdrop-filter` except on the nav and mobile menu.
- Pinned track heights are reduced ×0.8 on phones to shorten scroll distance.

## 9. Core components

- **Navigation (P0):** fixed, floating, pill-like, not a traditional navbar. Desktop: `SUR` left; `EXPERIENCE  PLAYER  FEATURES  DOWNLOAD` right (labels, 12px, tracking .14em). Background `rgba(9,8,7,.72)` + `backdrop-filter: blur(14px)`, hairline `--sur-line`. On scroll > 80px it compacts: height 64→52px, padding reduces, background opacity rises. On scroll **down** it fades to .0–.4 opacity and slides up slightly; on scroll **up** it returns. It never dominates content. Active link = white; others `--sur-text-2`; a small orange dot marks the current section.
- **Mobile menu (P0):** bar shows `SUR` and `MENU`. Opening shows a full-screen overlay (`100dvh`, `--sur-deep` at .96) with four large typographic links (`--fs-title`), staggered reveal, `aria-expanded` on the button, focus moved into the overlay and trapped, `Esc` closes, background content `inert`, body scroll locked, focus returns to the button on close.
- **Primary button:** pill, background `--sur-orange`, text `#090807`, 600 weight, min-height 52px, horizontal padding 28px. Hover: scale 1.02, brightness 1.06, soft shadow. Active: scale .98. Transition 200–260ms `--sur-ease`. Focus-visible: 2px white outline + 3px orange offset ring. **Ghost button** (View on GitHub): transparent, 1px `rgba(255,255,255,.24)` border, white text.
- **Device frame (`.sur-shot`):** wrapper with `aspect-ratio: 9 / 19.5`, radius `--sur-radius-l`, 1px hairline `rgba(255,255,255,.10)`, layered shadow (`0 40px 80px -20px rgba(0,0,0,.7)`, `0 0 0 1px rgba(255,177,141,.10)`), and an orange glow pseudo-element (`radial-gradient`, opacity .14–.22). The `<img>` fills it with `object-fit: contain`, explicit `width`/`height` (1080×2340), `alt`, correct `loading`/`fetchpriority`. Screenshots are never shown as flat rectangles.
- **Placeholder (always present behind every screenshot):** dark `--sur-surface` gradient with a faint `SUR` wordmark and `APP SCREENSHOT` label (11px tracking). If the image fails to load or is missing, JS adds `.is-missing`, hides the `<img>`, and the placeholder remains — never a broken-image icon. The placeholder must look intentional at final quality.
- **Micro label (`.sur-label`):** `SUR / 01`, `SUR / EXPERIENCE`, `SUR / PLAYER`, `SUR / DISCOVER` as specified in §6.
- **Scroll cue (hero only):** a 1px × 48px vertical line with a slow traveling highlight; fades out by p=.15.

## 10. Page narrative — canonical section order

This is the **single source of truth** for order (v1 listed two). Story: INTRO → IDENTITY → DISCOVER → LISTEN → PLAYER → IMMERSION → DETAIL → EXPERIENCE → DOWNLOAD → FINAL BRAND. Each section inherits the previous one’s background tone so there is never a hard cut.

Order: 01 Intro loader · 02 Navigation · 03 Hero `#hero` · 04 Brand statement `#statement` · 05 Interface `#experience` · 06 Discover `#discover` · 07 Listen `#listen` · 08 Rhythm words `#rhythm` · 09 Full player `#player` · 10 Player details `#details` · 11 Immersion `#immersion` · 12 Feature story `#features` · 13 Gallery `#gallery` · 14 Experience statement · 15 Download `#download` · 16 Final brand `#final` · 17 Footer.

Nav links: EXPERIENCE → `#experience`, PLAYER → `#player`, FEATURES → `#features`, DOWNLOAD → `#download`.

**Screenshot map** (each file reused with different crops, so six files carry the whole story): `sur-home` → hero, interface beat 1, gallery · `sur-search` → interface beat 2, gallery · `sur-library` → interface beat 3, gallery · `sur-discover` → discover, feature 01 · `sur-player` → listen (bottom crop), full player, feature 03 · `sur-lyrics` → details, immersion, feature 04.

### 10.1 Intro loader

- Full-screen `--sur-bg` with `SUR` fading in (opacity only, ≤ 400ms). Released as soon as fonts and the hero image are ready, with a hard cap of 900ms. No spinner, no artificial delay. Skipped entirely under reduced motion.

### 10.2 Hero `#hero` — pin track 200svh (phones 170svh)

- **Copy:** label `SUR / 01` · wordmark `SUR` · H1 `Music,` / `felt differently.` · support `A modern music experience` / `designed around the way you listen.` · CTA `Explore Sur` (→ `#experience`). Image: `sur-home`.
- **Desktop/laptop:** text block left (columns 1–7), screenshot right, large, slightly bleeding off the right edge inside the clipped stage, with an orange radial glow behind it. **Tablet:** text top, screenshot below and larger. **Phone:** text top-aligned under the nav, screenshot below at 78–88vw wide, rising from the bottom edge of the stage.
- **Load timeline (ms):** 0 background · 150 wordmark (700ms) · 350 headline lines, 90ms stagger (800ms) · 700 support text · 850 CTA · 950 screenshot rises from `translateY(6vh) scale(.94)` to rest (1100ms). Everything settled by \~2s; headline readable by \~1s.
- **Scroll timeline (`data-ranges='0-.35, .35-1'`):** text `translateY(-8vh)` and opacity → 0 across p1; screenshot scale 1 → 1.12 and `translateY(-4vh)` across p2; glow opacity .22 → .08 and shifts upward; stage background deepens toward `--sur-bg-2`; the next section’s first line is already entering at p=1. Scrolling up reverses it exactly.

### 10.3 Brand statement `#statement` — pin track 260svh

- Mostly empty screen. Label `SUR / IDENTITY`. Layer A: `Not just` / `a player.` Layer B: `An experience.` with only the word `experience.` in `--sur-orange-soft`.
- p 0–.15 A reveals; .15–.45 hold; .45–.65 A exits (`translateY(-6vh)`, opacity → 0, blur ≤ 8px on desktop) while B enters from `translateY(6vh)`; .65–1 hold. Typographic only; no screenshot.

### 10.4 Interface `#experience` — pin track 400svh (3 beats)

- Opening line `Designed` / `around music.` (p 0–.2), then three beats, one screenshot at a time, each with one message and one label:
  1. `Everything` / `starts here.` — `sur-home`, centered, large — label `SUR / HOME`
  2. `Find your sound.` — `sur-search`, screenshot left, text right — label `SUR / SEARCH`
  3. `Your library,` / `always close.` — `sur-library`, screenshot right, text left — label `SUR / LIBRARY`
- Beat change: outgoing screenshot `scale(.92) translateY(-6vh)` and opacity → 0; incoming enters from `translateY(8vh) scale(.94)`. Text follows 80ms behind the screenshot (via an offset range). v1 reused `Stay in the moment.` here; v2 reserves it for §10.10.
- Phones: every beat is single column, screenshot width 86vw capped by `min(86vw, 74svh * 9 / 19.5)`.

### 10.5 Discover `#discover` — pass scene 220svh

- Copy: `Discover` / `something new.` · label `SUR / DISCOVER` · image `sur-discover`.
- Desktop: huge headline behind the screenshot; the phone overlaps at most the lower 25% of the letters so the text stays legible. Screenshot goes `scale(.8) translateY(14vh) opacity .4` → `scale(1) translateY(0) opacity 1` over p 0–.5, then drifts `translateY(-6vh)` by p=1; the glow parallaxes slower. Phones: heading above, screenshot below.

### 10.6 Listen `#listen` — pass scene 200svh

- Copy: `Just` / `listen.` · image `sur-player` cropped to the lower 45% (artwork + controls) using the crop technique in §11.
- Cinematic contrast: background drops to `--sur-bg-2`, a single warm light falls from above-right (gradient, no hard shapes), screenshot rim-lit with a 1px `rgba(255,177,141,.18)` edge. Minimal text, one dominant visual.

### 10.7 Rhythm words `#rhythm` — pin track 400svh

- Words `DISCOVER` → `LISTEN` → `FEEL` → `REPEAT`, one at a time, each filling most of the viewport width (`--fs-giant`). 4 equal slots of 25% p. Each slot: enter (blur 8px → 0, `translateY(4vh)`, opacity 0 → 1) over the first 35%, hold, exit upward over the last 25%. Scrolling up runs the same math backward.
- Counter `01 / 04` (label style) and a 1px orange progress hairline (`scaleX(p)`) below. Orange is used only for that hairline.

### 10.8 Full player `#player` — pin track 360svh

- Copy: `The music` / `takes over.` · label `SUR / PLAYER` · image `sur-player` (full).
- Timeline: p 0–.2 heading in; .15–.85 screenshot `scale(.42)` → `.7` → `1` → dominant (≈ 82–90% of viewport height, top and bottom may crop slightly on desktop); heading fades and rises by p .35; a dark overlay goes 0 → .55 so the background darkens; orange ambient glow grows .12 → .30 (the maximum anywhere on the site). Image stays sharp: laid out at final size, scaled up from small, no blur.
- Phones: final width `min(86vw, 82svh * 9 / 19.5)`.

### 10.9 Player details `#details`

- Five details: **Playback, Lyrics, Queue, Artwork, Controls**, each a separate crop of `sur-player` / `sur-lyrics` (crop technique, §11). Not a grid.
- Desktop/tablet: pin track 500svh; left rail lists the five labels (active = white with a small orange dot, others `--sur-text-3`); one crop is shown at a time and crossfades with a slight `translateY` per 20% slot. Phones: a vertical sequence of five crops, 86vw wide, each with a one-line caption, revealed individually.

### 10.10 Immersion `#immersion` — pass scene 220svh

- Copy: `Stay` / `in the moment.` · image `sur-lyrics` · label `SUR / IMMERSION`.
- Background goes to `--sur-deep`. Screenshot emerges `scale(.86) opacity 0` → `scale(1) opacity 1` slowly (1000ms-class feel mapped to p 0–.6). A low orange atmospheric gradient rises from the bottom, opacity .10 → .22. Heading only; no extra text.

### 10.11 Feature story `#features` — horizontal on desktop

- **Desktop (≥ 1024px, height ≥ 600px, fine pointer or large):** pin track = chapters × 100svh + 100svh. Sticky stage holds a flex rail whose width is `N × 100%` of the stage. `translateX = -p × ((N-1) / N) × 100%` of the rail’s own width. Four chapters, each 25% of the rail:
  - `01` **DISCOVER** — `Find the music` / `that fits the moment.` — `sur-discover`
  - `02` **LISTEN** — `Every track,` / `given room to breathe.` — `sur-search` crop
  - `03` **PLAY** — `Controls that` / `stay out of the way.` — `sur-player`
  - `04` **IMMERSION** — `Lyrics and artwork,` / `always with you.` — `sur-lyrics`
- Within each chapter the reveal order is: number → heading → description → screenshot → (previous chapter transitions out), driven by sub-ranges so it reverses. A four-tick hairline indicator at the bottom shows progress.
- **Tablet / phone:** no horizontal motion. Chapters stack vertically, each min-height `var(--vh-full)`, with the same internal reveal order using `[data-reveal]` stagger. No horizontal overflow, ever.
- Copy must match real app behavior — confirm in §21.

### 10.12 Gallery `#gallery`

- Not a grid. Desktop: one **large** screenshot (`sur-home`, \~38–42% width, center-left), one **floating** smaller screenshot (`sur-library`) overlapping on the right and raised, one **partially visible** screenshot (`sur-search`) cropped by the container edge at lower right. Three depth layers with parallax at ×0.6 / ×1 / ×1.3. Label `SUR / GALLERY`, single line `Every screen, considered.`
- Tablet: two-layer composition. Phones: clean vertical sequence of three 86vw screenshots with a 4vw alternating offset and individual reveals.

### 10.13 Experience statement

- Pass scene 160svh, no screenshot. `Not explained.` / `Felt.` Large white type; `Felt.` may take `--sur-orange-soft`. Ambient glow only.

### 10.14 Download `#download`

- Copy: `Ready` / `to listen?` → then `Experience Sur.` · buttons `Get Sur` (primary) and `View on GitHub` (ghost). Stagger: label 0 · heading 100 · sub 200 · buttons 300ms.
- URLs come from `js/config.js` (§12). If `SUR_DOWNLOAD_URL` is empty, `Get Sur` renders as a disabled-looking button with `aria-disabled='true'`, reads `Coming soon`, and has no `href`. Same for GitHub: if empty, the ghost button is hidden. Desktop-only magnetic hover (≤ 6px translate) on `(hover: hover) and (pointer: fine)`.

### 10.15 Final brand `#final`

- Almost empty. Giant `SUR` (`--fs-wordmark` scaled ×1.4, cap 28rem), under it `Music,` / `felt differently.`. Background slowly darkens to `--sur-deep` (scroll-linked), with a very faint orange glow at the bottom (opacity ≤ .08). Min-height `var(--vh-full)`.

### 10.16 Footer

- Minimal: `SUR` · `Experience`, `Download`, `GitHub`, `Privacy` · `© 2026 Sur`. `Privacy` opens a small `<dialog>` with a three-line statement. Say ‘no cookies, no analytics, no personal data’ **only if true** — if fonts load from Google Fonts, self-host them (§6) so the statement is accurate. Safe-area bottom padding applied.

## 11. Image system

- **Specs:** WebP quality ≈ 80, 1080×2340 (9:19.5), target ≤ 150KB each (hero ≤ 180KB). AVIF optional via `<picture>`; PNG only for transparency. The OG image is PNG 1200×630.
- **Loading:** hero image `fetchpriority='high'` plus a `<link rel='preload' as='image'>`; every other screenshot `loading='lazy' decoding='async'`. Width and height attributes on every `<img>` (zero layout shift). Never stretch: `object-fit: contain`.
- **Crop technique (no extra files):** wrapper with `aspect-ratio` and `overflow: clip`; inner `<img>` sized `width: 100%` and shifted with `transform: translateY(var(--crop-y))` or `margin-top` as a percentage. Each crop exposes `--crop-y` and `--crop-h` so it can be tuned in CSS without touching HTML. Optional P2: dedicated files `sur-detail-*.webp` override crops automatically.
- **Replacement:** dropping a new file with the same name into `assets/images/` updates the site. No HTML edits.
- **Known trade-off:** until the real files exist, the browser reports 404s for the missing images. The placeholder hides the failure visually. Ship real screenshots (or tiny same-name placeholders) before the final Lighthouse and console check.
- **Alt text:** descriptive, e.g. `Sur home screen showing a personalised music feed`. Decorative glows and labels use `aria-hidden='true'`.

## 12. Configuration (`js/config.js`)

```js
window.SUR_CONFIG = {
  downloadUrl: '',   // paste the real APK / store link here
  githubUrl: '',     // paste the real repository URL here
  siteUrl: '',       // full https URL, needed for Open Graph absolute links
  year: 2026
};
```

`main.js` reads this once and fills every CTA. Empty value = graceful state from §10.14. Never invent a URL.

## 13. Accessibility (P0)

- Semantic landmarks: `<header>` + `<nav aria-label='Primary'>`, `<main id='main'>`, one `<section aria-labelledby>` per section, `<footer>`. Exactly one `<h1>` (the hero headline); every other section statement is an `<h2>`; chapter titles `<h3>`.
- A ‘Skip to content’ link is the first focusable element. All controls are keyboard operable, have visible focus rings, and logical tab order. The mobile menu follows §9.
- Pinned scenes must not trap keyboard users: all content inside them stays in the DOM and in reading order, so tabbing never lands on hidden or off-screen controls. Off-screen interactive elements get `visibility: hidden` / `inert` when inactive.
- **Reduced motion:** under `@media (prefers-reduced-motion: reduce)` parallax and float are removed, pinned scenes become normal flow (track heights auto, stages static), `[data-reveal]` is simply visible (opacity transition ≤ 150ms), smooth scrolling is off, and the loader is skipped. All information remains.
- Contrast per §5. `lang='en'` on `<html>`. Screenshots in decorative roles use empty `alt` only when adjacent text fully describes them.

## 14. Performance budget (P1)

- CSS total ≤ 60KB, JS total ≤ 40KB (unminified is fine), HTML ≤ 60KB, fonts ≤ 80KB (Latin subset, two weights or one variable file), total first-load transfer ≤ 600KB.
- Pinned/pass section DOM is light: no more than 12 animated elements active at once. Off-screen non-pinned sections may use `content-visibility: auto` with `contain-intrinsic-size`; pinned tracks must not.
- No libraries. No console errors or warnings. No layout shift: CLS < 0.02. LCP element = hero screenshot or headline < 2.0s on a mid-range phone.

## 15. SEO and metadata (P1)

```html
<title>Sur — Music, felt differently.</title>
<meta name='description' content='Experience Sur — a modern music experience designed around the way you listen.'>
<meta name='theme-color' content='#090807'>
<meta property='og:type' content='website'>
<meta property='og:title' content='Sur — Music, felt differently.'>
<meta property='og:description' content='A modern music experience designed around the way you listen.'>
<meta property='og:image' content='assets/images/og-sur.png'>
<meta name='twitter:card' content='summary_large_image'>
<link rel='icon' href='assets/icons/favicon.svg' type='image/svg+xml'>
```

Note: social crawlers need an **absolute** `og:image` URL. `main.js` may rewrite it from `SUR_CONFIG.siteUrl` when set, and the README must tell the owner to set it after the first deploy.

## 16. No-JS and failure fallbacks

- `<html class='no-js'>`; a tiny inline script in `<head>` swaps it to `js` before first paint.
- Under `.no-js` (and `@media (scripting: none)`): all `[data-reveal]` visible, all tracks `height: auto`, stages `position: relative`, horizontal rail stacked vertically, nav links plain anchors, mobile menu shown as a simple inline list, screenshots and CTAs visible.
- If the scroll engine throws, a `try/catch` in `main.js` adds `.no-engine` to `<html>` which applies the same static layout. If an image fails, §9 placeholder. If config URLs are empty, §10.14 states.

## 17. QA matrix and test script

- **Viewports (portrait and, for phones, landscape):** 320×568 · 360×800 · 375×812 · 390×844 · 412×915 · 768×1024 · 1024×768 · 1280×800 · 1366×768 · 1440×900 · 1920×1080 · 2560×1440.
- **Overflow check (run at every size, expect `true` and an empty list):**

```js
document.documentElement.scrollWidth <= innerWidth;
[...document.querySelectorAll('body *')].filter(e => e.getBoundingClientRect().right > innerWidth + 1);
```

Elements that are intentionally cropped inside a clipping stage may appear in the list; confirm each is clipped by its stage and does not widen the page.

- **Reverse test:** at each pinned scene, scroll down to p=1, scroll back to p=0; state at p=0 must equal the initial state. Repeat with a fast fling and with a slow drag.
- **Throwing tests:** disable JavaScript; enable reduced motion; throttle CPU 4× and network Fast 3G; rotate phone; browser zoom 200%; text size ×1.5.
- **Tools:** Lighthouse mobile, DevTools Performance (no long tasks > 50ms during scroll), keyboard-only pass, VoiceOver or TalkBack spot check.

## 18. Acceptance checklist

**Design:** premium dark identity · warm orange used sparingly · pure white oversized type · strong negative space · one idea per viewport · no card grid anywhere · Material 3 principles (tonal surfaces, pill controls, expressive type, clear hierarchy) without looking like the Android app.

**Screenshots:** easy to replace · visually dominant · never a plain grid · aspect ratio preserved · smooth scale/translate transitions · placeholder fallback · lazy loading with fixed dimensions.

**Animation:** hero load sequence · scroll reveal and exit · **reverse on scroll up for every scene** · screenshot scaling · typography transforms · feature reveal order · full-player cinematic · no bounce · no jank · reduced motion honored.

**Responsive:** 320 to 2560 no overflow · text never clipped · touch targets ≥ 44px · portrait and landscape · tablet intermediate layout · ultrawide centered · horizontal story collapses to vertical below 1024px.

**Technical:** static `index.html` · relative paths · GitHub Pages ready · `.nojekyll` · no secrets · no console errors · SEO metadata · accessibility · optimized images · performance budget met.

## 19. Do-not-use list

Generic SaaS or Bootstrap look · neon gaming style · purple/blue gradients · stacks of glass cards · heavy shadows or borders · icon spam · stock photos or illustrations · cursor follower, giant glowing circle or trails · oversized footer · dashboards, login, backend, database · frameworks · one-time-only scroll animations · bounce · fixed-pixel layouts · absolute asset paths · fake URLs · exposed secrets.

**Final design test for every element:** *does this make Sur more premium, immersive and memorable?* If not, remove or simplify it.

## 20. Builder prompt — paste into Google AI Studio

Use these four passes instead of one giant request; paste the whole PRD each time, then the pass instruction.

- **Pass 1 — structure and static layout.** *Read the attached PRD v2.0 completely. Output a vanilla HTML/CSS/JS project exactly matching §4 (no React, no build tools). Create every file with its path as a heading, each in its own code block. Implement §5–§7 and all sections of §10 as a fully static, fully readable page with no animation yet: correct copy, order, ids, semantic HTML, placeholders, config and metadata. Do not invent URLs.*
- **Pass 2 — motion.** *Add `js/scroll.js` and the animation CSS per §8 and §10 using the pure-function-of-scroll model (`--p`, `--p1..n`, `data-scene`, `data-ranges`). Implement every timeline, the hero load sequence, reveals in both directions, the horizontal story with the vertical fallback, and the mobile menu.*
- **Pass 3 — hardening.** *Apply §13–§16: accessibility, reduced motion, no-JS and no-engine fallbacks, performance budget. Then run through §17 mentally for 320, 390, 768, 1440 and 1920 widths and fix every overflow or clipping risk.*
- **Pass 4 — audit.** *Check the code against §2, §18 and §19. Return a table: requirement, pass/fail, fix applied. Then update README.md with deploy steps (create repo, push, Settings → Pages → deploy from branch) and the screenshot replacement guide.*

**Standing rules for every pass:** plain `<script defer>` files, no CDN libraries, no secrets, relative paths only, comment each file with its purpose, never invent features of Sur that the PRD does not state.

## 21. Inputs only the owner can supply

1. Six real screenshots (1080×2340 WebP, names in §4) and the 1200×630 OG image.
2. `downloadUrl`, `githubUrl`, `siteUrl` for `js/config.js`.
3. Confirmation that the feature copy in §10.4 and §10.11 (search, library, lyrics, queue, artwork) matches what Sur really does.
4. Final wordmark or logo file, if any; otherwise the Manrope `SUR` wordmark is used everywhere (§6).
5. Decision on fonts: self-host Manrope (recommended; keeps the privacy statement honest) or load from Google Fonts.
