/* Purpose: Intro loader release, rhythm counter, details rail sync, and feature chapter tick indicators. */

(function (Sur) {
  'use strict';

  var utils = Sur.utils;

  function releaseLoader() {
    var loader = document.getElementById('intro-loader');
    var root = document.documentElement;

    if (!loader || utils.prefersReducedMotion()) {
      if (loader) loader.hidden = true;
      root.classList.add('is-loaded');
      return;
    }

    var released = false;
    function finish() {
      if (released) return;
      released = true;
      root.classList.add('is-loaded');
      loader.classList.add('is-done');
      window.setTimeout(function () {
        loader.hidden = true;
      }, 420);
    }

    var heroImg = document.querySelector('#hero .sur-shot img');
    var fontReady = document.fonts && document.fonts.ready ? document.fonts.ready : Promise.resolve();

    var imgReady = new Promise(function (resolve) {
      if (!heroImg) {
        resolve();
        return;
      }
      if (heroImg.complete) {
        if (typeof heroImg.decode === 'function') {
          heroImg.decode().then(resolve, resolve);
        } else {
          resolve();
        }
        return;
      }
      heroImg.addEventListener('load', resolve, { once: true });
      heroImg.addEventListener('error', resolve, { once: true });
    });

    var hardCap = window.setTimeout(finish, 900);
    Promise.all([fontReady, imgReady]).then(function () {
      window.clearTimeout(hardCap);
      finish();
    });
  }

  function bindSceneWidgets() {
    var rhythmScene = document.getElementById('rhythm');
    var rhythmCounter = document.getElementById('rhythm-counter');
    if (rhythmScene && rhythmCounter) {
      Sur.scroll.onScene(rhythmScene, function (p) {
        var idx = Math.min(4, Math.floor(p * 4) + 1);
        var nextText = '0' + idx + ' / 04';
        if (rhythmCounter.textContent !== nextText) {
          rhythmCounter.textContent = nextText;
        }
      });
    }

    var detailsScene = document.getElementById('details');
    var detailTabs = document.querySelectorAll('.sur-detail-tab');
    if (detailsScene && detailTabs.length) {
      Sur.scroll.onScene(detailsScene, function (p) {
        var activeIdx = Math.min(detailTabs.length - 1, Math.floor(p * detailTabs.length));
        for (var i = 0; i < detailTabs.length; i++) {
          var isActive = i === activeIdx;
          detailTabs[i].classList.toggle('is-active', isActive);
          detailTabs[i].setAttribute('aria-pressed', isActive ? 'true' : 'false');
        }
      });
    }

    var featuresScene = document.getElementById('features');
    var featureTicks = document.querySelectorAll('.sur-feature-tick');
    if (featuresScene && featureTicks.length) {
      Sur.scroll.onScene(featuresScene, function (p) {
        var activeIdx = Math.min(featureTicks.length - 1, Math.floor(p * featureTicks.length));
        for (var i = 0; i < featureTicks.length; i++) {
          featureTicks[i].classList.toggle('is-active', i === activeIdx);
        }
      });
    }
  }

  function init() {
    releaseLoader();
    bindSceneWidgets();
  }

  Sur.animations = {
    init: init
  };
})(window.Sur = window.Sur || {});
