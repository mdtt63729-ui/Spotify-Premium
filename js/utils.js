/* Purpose: Shared math, media query, and accessibility helper utilities attached to window.Sur.utils. */

(function (Sur) {
  'use strict';

  var motionQuery = window.matchMedia ? window.matchMedia('(prefers-reduced-motion: reduce)') : null;
  var finePointerQuery = window.matchMedia ? window.matchMedia('(hover: hover) and (pointer: fine)') : null;

  function clamp01(value) {
    if (value <= 0) return 0;
    if (value >= 1) return 1;
    return value;
  }

  function remap(value, start, end) {
    var span = end - start;
    if (span <= 0) return value >= end ? 1 : 0;
    return clamp01((value - start) / span);
  }

  function prefersReducedMotion() {
    return Boolean(motionQuery && motionQuery.matches);
  }

  function isFinePointer() {
    return Boolean(finePointerQuery && finePointerQuery.matches);
  }

  function onReducedMotionChange(callback) {
    if (!motionQuery) return;
    if (typeof motionQuery.addEventListener === 'function') {
      motionQuery.addEventListener('change', callback);
    } else if (typeof motionQuery.addListener === 'function') {
      motionQuery.addListener(callback);
    }
  }

  function setInert(element, isInert) {
    if (!element) return;
    if ('inert' in element) {
      element.inert = Boolean(isInert);
    } else if (isInert) {
      element.setAttribute('aria-hidden', 'true');
    } else {
      element.removeAttribute('aria-hidden');
    }
  }

  Sur.utils = {
    clamp01: clamp01,
    remap: remap,
    prefersReducedMotion: prefersReducedMotion,
    isFinePointer: isFinePointer,
    onReducedMotionChange: onReducedMotionChange,
    setInert: setInert
  };
})(window.Sur = window.Sur || {});
