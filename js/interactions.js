/* Purpose: Screenshot placeholder fallback, player detail tab scroll-sync, magnetic CTA hover, and privacy dialog. */

(function (Sur) {
  'use strict';

  var utils = Sur.utils;

  function initImageFallbacks() {
    var mediaFrames = document.querySelectorAll('.sur-shot, .sur-crop');

    for (var i = 0; i < mediaFrames.length; i++) {
      (function (frame) {
        var img = frame.querySelector('img');
        if (!img) {
          frame.classList.add('is-missing');
          return;
        }

        function onLoad() {
          frame.classList.remove('is-missing');
          if (Sur.scroll && typeof Sur.scroll.measure === 'function') {
            Sur.scroll.measure();
          }
        }

        function onError() {
          frame.classList.add('is-missing');
        }

        img.addEventListener('load', onLoad);
        img.addEventListener('error', onError);

        if (img.complete) {
          if (img.naturalWidth === 0) {
            onError();
          } else {
            onLoad();
          }
        }
      })(mediaFrames[i]);
    }
  }

  function initDetailTabs() {
    var detailsScene = document.getElementById('details');
    var tabs = document.querySelectorAll('.sur-detail-tab');
    if (!detailsScene || !tabs.length) return;

    var slotTargets = [0.1, 0.3, 0.5, 0.7, 0.9];
    for (var i = 0; i < tabs.length; i++) {
      (function (btn, idx) {
        btn.addEventListener('click', function () {
          Sur.scroll.scrollToSceneProgress(detailsScene, slotTargets[idx] || 0);
        });
      })(tabs[i], i);
    }
  }

  function initMagneticButtons() {
    if (!utils.isFinePointer() || utils.prefersReducedMotion()) return;
    var buttons = document.querySelectorAll('[data-magnetic]');

    for (var i = 0; i < buttons.length; i++) {
      (function (btn) {
        btn.addEventListener('mousemove', function (e) {
          if (btn.classList.contains('is-disabled') || utils.prefersReducedMotion()) return;
          var rect = btn.getBoundingClientRect();
          var relX = ((e.clientX - rect.left) / rect.width - 0.5) * 12;
          var relY = ((e.clientY - rect.top) / rect.height - 0.5) * 10;
          var max = 6;
          var x = Math.max(-max, Math.min(max, relX));
          var y = Math.max(-max, Math.min(max, relY));
          btn.style.setProperty('--mag-x', x.toFixed(2) + 'px');
          btn.style.setProperty('--mag-y', y.toFixed(2) + 'px');
        });

        btn.addEventListener('mouseleave', function () {
          btn.style.setProperty('--mag-x', '0px');
          btn.style.setProperty('--mag-y', '0px');
        });
      })(buttons[i]);
    }
  }

  function initPrivacyDialog() {
    var openBtn = document.getElementById('privacy-open');
    var dialog = document.getElementById('privacy-dialog');
    var closeBtn = document.getElementById('privacy-close');
    if (!openBtn || !dialog) return;

    openBtn.addEventListener('click', function () {
      if (typeof dialog.showModal === 'function') {
        dialog.showModal();
      } else {
        dialog.setAttribute('open', '');
      }
    });

    if (closeBtn) {
      closeBtn.addEventListener('click', function () {
        if (typeof dialog.close === 'function') {
          dialog.close();
        } else {
          dialog.removeAttribute('open');
        }
      });
    }

    dialog.addEventListener('click', function (e) {
      if (e.target === dialog && typeof dialog.close === 'function') {
        dialog.close();
      }
    });
  }

  function initHeroFocusGuard() {
    var heroCta = document.querySelector('#hero .sur-btn');
    if (!heroCta) return;
    heroCta.addEventListener('focus', function () {
      if ((window.scrollY || 0) > window.innerHeight * 0.45) {
        window.scrollTo({
          top: 0,
          behavior: utils.prefersReducedMotion() ? 'auto' : 'smooth'
        });
      }
    });
  }

  function init() {
    initImageFallbacks();
    initDetailTabs();
    initMagneticButtons();
    initPrivacyDialog();
    initHeroFocusGuard();
  }

  Sur.interactions = {
    init: init
  };
})(window.Sur = window.Sur || {});
