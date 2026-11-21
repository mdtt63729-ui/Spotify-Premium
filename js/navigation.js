/* Purpose: Floating navigation scroll behavior, active section observer, and accessible mobile menu drawer. */

(function (Sur) {
  'use strict';

  var utils = Sur.utils;

  function init() {
    var navWrap = document.getElementById('sur-nav-wrap');
    var menuBtn = document.getElementById('sur-menu-btn');
    var mobileMenu = document.getElementById('mobile-menu');
    var mobileClose = document.getElementById('sur-mobile-close');
    var mobileLinks = document.querySelectorAll('.sur-mobile-link');
    var desktopLinks = document.querySelectorAll('.sur-nav-link');
    var mainEl = document.getElementById('main');
    var footerEl = document.querySelector('footer');
    var isOpen = false;
    var lastFocused = null;

    if (navWrap) {
      Sur.scroll.onScroll(function (y, dir) {
        navWrap.classList.toggle('is-compact', y > 80);
        if (isOpen) {
          navWrap.classList.remove('is-hidden-down');
          return;
        }
        var shouldHide = y > 160 && dir > 0;
        navWrap.classList.toggle('is-hidden-down', shouldHide);
      });
    }

    // Active section highlighting
    var trackedIds = ['experience', 'player', 'features', 'download'];
    var sectionEls = trackedIds
      .map(function (id) { return document.getElementById(id); })
      .filter(Boolean);

    function setActiveLink(activeId) {
      var allLinks = Array.prototype.slice.call(desktopLinks).concat(Array.prototype.slice.call(mobileLinks));
      for (var i = 0; i < allLinks.length; i++) {
        var href = allLinks[i].getAttribute('href') || '';
        allLinks[i].classList.toggle('is-active', href === '#' + activeId);
      }
    }

    if ('IntersectionObserver' in window && sectionEls.length) {
      var navObserver = new IntersectionObserver(
        function (entries) {
          for (var i = 0; i < entries.length; i++) {
            if (entries[i].isIntersecting) {
              setActiveLink(entries[i].target.id);
            }
          }
        },
        { rootMargin: '-35% 0px -50% 0px', threshold: 0 }
      );
      for (var j = 0; j < sectionEls.length; j++) {
        navObserver.observe(sectionEls[j]);
      }
    }

    // Accessible Mobile Menu
    function getFocusable(container) {
      return Array.prototype.slice.call(
        container.querySelectorAll('a[href], button:not([disabled]), [tabindex]:not([tabindex="-1"])')
      );
    }

    function openMenu() {
      if (!mobileMenu || !menuBtn || isOpen) return;
      isOpen = true;
      lastFocused = document.activeElement;
      mobileMenu.hidden = false;
      window.requestAnimationFrame(function () {
        mobileMenu.classList.add('is-open');
      });
      menuBtn.setAttribute('aria-expanded', 'true');
      document.body.classList.add('is-menu-open');
      utils.setInert(mainEl, true);
      utils.setInert(footerEl, true);
      utils.setInert(navWrap, true);

      var focusables = getFocusable(mobileMenu);
      if (focusables.length) {
        focusables[0].focus();
      }
    }

    function closeMenu() {
      if (!mobileMenu || !menuBtn || !isOpen) return;
      isOpen = false;
      mobileMenu.classList.remove('is-open');
      menuBtn.setAttribute('aria-expanded', 'false');
      document.body.classList.remove('is-menu-open');
      utils.setInert(mainEl, false);
      utils.setInert(footerEl, false);
      utils.setInert(navWrap, false);

      window.setTimeout(function () {
        if (!isOpen) mobileMenu.hidden = true;
      }, 320);

      if (lastFocused && typeof lastFocused.focus === 'function') {
        lastFocused.focus();
      } else {
        menuBtn.focus();
      }
    }

    if (menuBtn) {
      menuBtn.addEventListener('click', function () {
        if (isOpen) closeMenu();
        else openMenu();
      });
    }

    if (mobileClose) {
      mobileClose.addEventListener('click', closeMenu);
    }

    for (var m = 0; m < mobileLinks.length; m++) {
      mobileLinks[m].addEventListener('click', closeMenu);
    }

    document.addEventListener('keydown', function (e) {
      if (!isOpen || !mobileMenu) return;
      if (e.key === 'Escape') {
        e.preventDefault();
        closeMenu();
        return;
      }
      if (e.key === 'Tab') {
        var focusables = getFocusable(mobileMenu);
        if (!focusables.length) return;
        var first = focusables[0];
        var last = focusables[focusables.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    });
  }

  Sur.navigation = {
    init: init
  };
})(window.Sur = window.Sur || {});
