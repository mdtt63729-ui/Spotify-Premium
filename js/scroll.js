/* Purpose: Pure-function-of-scroll engine computing --p, --pN, --ps, and bidirectional [data-reveal] states. */

(function (Sur) {
  'use strict';

  var utils = Sur.utils;
  var state = {
    y: window.scrollY || window.pageYOffset || 0,
    lastY: window.scrollY || window.pageYOffset || 0,
    dir: 1,
    velocity: 0,
    vh: window.innerHeight || 800,
    rafId: 0,
    idleTimer: 0,
    lastScrollAt: 0,
    scenes: [],
    listeners: new Map(),
    scrollCallbacks: [],
    revealObserver: null,
    sceneObserver: null,
    resizeObserver: null,
    initialized: false
  };

  function parseRanges(raw) {
    if (!raw) return [];
    return raw
      .split(',')
      .map(function (part) {
        var trimmed = part.trim();
        var dashIdx = trimmed.indexOf('-');
        if (dashIdx === -1) return null;
        var start = parseFloat(trimmed.slice(0, dashIdx));
        var end = parseFloat(trimmed.slice(dashIdx + 1));
        if (Number.isNaN(start) || Number.isNaN(end)) return null;
        return [start, end];
      })
      .filter(Boolean);
  }

  function updateDirection(nextY) {
    var delta = nextY - state.lastY;
    if (Math.abs(delta) > 0.5) {
      state.dir = delta > 0 ? 1 : -1;
      var dirLabel = state.dir > 0 ? 'down' : 'up';
      if (document.documentElement.dataset.dir !== dirLabel) {
        document.documentElement.dataset.dir = dirLabel;
      }
      state.velocity = delta;
    } else {
      state.velocity = 0;
    }
    state.lastY = nextY;
  }

  function measure() {
    var scrollY = window.scrollY || window.pageYOffset || 0;
    state.vh = window.innerHeight || document.documentElement.clientHeight || 800;

    for (var i = 0; i < state.scenes.length; i++) {
      var scene = state.scenes[i];
      var rect = scene.el.getBoundingClientRect();
      scene.top = rect.top + scrollY;
      scene.height = rect.height;
      scene.trackDist = Math.max(1, scene.height - state.vh);
    }

    renderFrame(true);
  }

  function computeProgress(scene, scrollY) {
    if (scene.type === 'pin') {
      return utils.clamp01((scrollY - scene.top) / scene.trackDist);
    }
    var total = scene.height + state.vh;
    if (total <= 0) return 0;
    return utils.clamp01((scrollY + state.vh - scene.top) / total);
  }

  function applyScene(scene, p, ps, forceAll) {
    var style = scene.el.style;
    style.setProperty('--p', p.toFixed(4));
    style.setProperty('--ps', ps.toFixed(4));

    var settling = false;
    for (var j = 0; j < scene.ranges.length; j++) {
      var range = scene.ranges[j];
      var subP = utils.remap(p, range[0], range[1]);
      var prevSub = scene.subPs[j];
      var nextSub;

      if (forceAll || prevSub === undefined) {
        nextSub = subP;
      } else {
        nextSub = prevSub + (subP - prevSub) * 0.12;
        if (Math.abs(subP - nextSub) > 0.0005) {
          settling = true;
        } else {
          nextSub = subP;
        }
      }

      scene.subPs[j] = nextSub;
      style.setProperty('--p' + (j + 1), subP.toFixed(4));
      style.setProperty('--ps' + (j + 1), nextSub.toFixed(4));
    }

    var cb = state.listeners.get(scene.el);
    if (cb) {
      cb(p, ps, scene);
    }

    return settling;
  }

  function renderFrame(forceAll) {
    if (document.hidden || utils.prefersReducedMotion()) {
      state.rafId = 0;
      return;
    }

    var scrollY = window.scrollY || window.pageYOffset || 0;
    state.y = scrollY;
    updateDirection(scrollY);

    var needsAnotherFrame = false;

    for (var i = 0; i < state.scenes.length; i++) {
      var scene = state.scenes[i];
      if (!forceAll && !scene.active) continue;

      var p = computeProgress(scene, scrollY);
      if (forceAll) {
        scene.ps = p;
      } else {
        scene.ps += (p - scene.ps) * 0.12;
        if (Math.abs(p - scene.ps) > 0.0005) {
          needsAnotherFrame = true;
        } else {
          scene.ps = p;
        }
      }

      scene.p = p;
      if (applyScene(scene, p, scene.ps, forceAll)) {
        needsAnotherFrame = true;
      }
    }

    for (var k = 0; k < state.scrollCallbacks.length; k++) {
      state.scrollCallbacks[k](state.y, state.dir, state.velocity);
    }

    var elapsed = performance.now() - state.lastScrollAt;
    if (needsAnotherFrame || elapsed < 140) {
      state.rafId = window.requestAnimationFrame(function () {
        renderFrame(false);
      });
    } else {
      state.rafId = 0;
    }
  }

  function scheduleTick() {
    state.lastScrollAt = performance.now();
    if (!state.rafId && !document.hidden && !utils.prefersReducedMotion()) {
      state.rafId = window.requestAnimationFrame(function () {
        renderFrame(false);
      });
    }
  }

  function setupObservers() {
    var sceneEls = document.querySelectorAll('[data-scene]');
    state.scenes = [];

    for (var i = 0; i < sceneEls.length; i++) {
      var el = sceneEls[i];
      var ranges = parseRanges(el.getAttribute('data-ranges'));
      state.scenes.push({
        el: el,
        type: el.getAttribute('data-scene') === 'pin' ? 'pin' : 'pass',
        ranges: ranges,
        top: 0,
        height: 0,
        trackDist: 1,
        active: i < 2,
        p: 0,
        ps: 0,
        subPs: new Array(ranges.length)
      });
    }

    if ('IntersectionObserver' in window) {
      state.sceneObserver = new IntersectionObserver(
        function (entries) {
          for (var i = 0; i < entries.length; i++) {
            var entry = entries[i];
            for (var j = 0; j < state.scenes.length; j++) {
              if (state.scenes[j].el === entry.target) {
                state.scenes[j].active = entry.isIntersecting;
                entry.target.classList.toggle('is-scene-active', entry.isIntersecting);
                if (entry.isIntersecting) {
                  var p = computeProgress(state.scenes[j], window.scrollY || 0);
                  state.scenes[j].p = p;
                  state.scenes[j].ps = p;
                  applyScene(state.scenes[j], p, p, true);
                }
                break;
              }
            }
          }
        },
        { rootMargin: '20% 0px 20% 0px', threshold: 0 }
      );

      for (var k = 0; k < state.scenes.length; k++) {
        state.sceneObserver.observe(state.scenes[k].el);
      }

      var revealEls = document.querySelectorAll('[data-reveal]');
      state.revealObserver = new IntersectionObserver(
        function (entries) {
          if (utils.prefersReducedMotion()) return;
          for (var i = 0; i < entries.length; i++) {
            entries[i].target.classList.toggle('is-in', entries[i].isIntersecting);
          }
        },
        { threshold: 0.25, rootMargin: '-8% 0px -8% 0px' }
      );

      for (var r = 0; r < revealEls.length; r++) {
        state.revealObserver.observe(revealEls[r]);
      }
    } else {
      for (var s = 0; s < state.scenes.length; s++) {
        state.scenes[s].active = true;
      }
      var fallbackReveals = document.querySelectorAll('[data-reveal]');
      for (var f = 0; f < fallbackReveals.length; f++) {
        fallbackReveals[f].classList.add('is-in');
      }
    }

    if ('ResizeObserver' in window) {
      var resizeQueued = false;
      state.resizeObserver = new ResizeObserver(function () {
        if (resizeQueued) return;
        resizeQueued = true;
        window.requestAnimationFrame(function () {
          resizeQueued = false;
          measure();
        });
      });
      state.resizeObserver.observe(document.body);
    }
  }

  function applyReducedMotionState() {
    if (!utils.prefersReducedMotion()) {
      measure();
      return;
    }
    if (state.rafId) {
      window.cancelAnimationFrame(state.rafId);
      state.rafId = 0;
    }
    var reveals = document.querySelectorAll('[data-reveal]');
    for (var i = 0; i < reveals.length; i++) {
      reveals[i].classList.add('is-in');
    }
  }

  function onScene(el, callback) {
    if (!el || typeof callback !== 'function') return;
    state.listeners.set(el, callback);
  }

  function onScroll(callback) {
    if (typeof callback === 'function') {
      state.scrollCallbacks.push(callback);
    }
  }

  function scrollToSceneProgress(el, targetProgress) {
    for (var i = 0; i < state.scenes.length; i++) {
      var scene = state.scenes[i];
      if (scene.el === el) {
        var destY = scene.top + utils.clamp01(targetProgress) * scene.trackDist;
        window.scrollTo({
          top: destY,
          behavior: utils.prefersReducedMotion() ? 'auto' : 'smooth'
        });
        return;
      }
    }
  }

  function init() {
    if (state.initialized) return;
    state.initialized = true;
    document.documentElement.dataset.dir = 'down';

    setupObservers();
    measure();

    window.addEventListener('scroll', scheduleTick, { passive: true });
    window.addEventListener('resize', measure, { passive: true });
    window.addEventListener('orientationchange', measure, { passive: true });
    window.addEventListener('load', measure, { passive: true });

    document.addEventListener('visibilitychange', function () {
      if (!document.hidden) {
        measure();
      }
    });

    if (document.fonts && document.fonts.ready) {
      document.fonts.ready.then(measure);
    }

    utils.onReducedMotionChange(applyReducedMotionState);
    applyReducedMotionState();
  }

  Sur.scroll = {
    init: init,
    measure: measure,
    onScene: onScene,
    onScroll: onScroll,
    scrollToSceneProgress: scrollToSceneProgress,
    getState: function () {
      return state;
    }
  };
})(window.Sur = window.Sur || {});
