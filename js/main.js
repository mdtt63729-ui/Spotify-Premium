/* Purpose: Application bootstrap reading SUR_CONFIG, hydrating links/metadata, and starting modules safely. */

(function (Sur) {
  'use strict';

  async function latestApk() {
    var cfg = window.SUR_CONFIG || {};
    var apiUrl = cfg.releasesApiUrl || 'https://api.github.com/repos/mdtt63729-ui/Spotify-Premium/releases/latest';
    var res = await fetch(apiUrl, { headers: { Accept: 'application/vnd.github+json' } });
    if (!res.ok) {
      throw new Error('GitHub release lookup failed: ' + res.status);
    }
    var release = await res.json();
    var assets = Array.isArray(release.assets) ? release.assets : [];
    var pick = assets.find(function (a) {
      return (
        a &&
        typeof a.name === 'string' &&
        a.name.endsWith('.apk') &&
        !a.name.includes('universal') &&
        !a.name.includes('vivo')
      );
    });
    if (!pick) {
      pick = assets.find(function (a) {
        return a && typeof a.name === 'string' && a.name.endsWith('.apk');
      });
    }
    if (!pick || !pick.browser_download_url) {
      throw new Error('No matching APK asset found');
    }
    return pick.browser_download_url;
  }

  function hydrateConfig() {
    var cfg = window.SUR_CONFIG || {};
    var downloadBtn = document.getElementById('download-primary');
    var downloadGithub = document.getElementById('download-github');
    var footerGithub = document.getElementById('footer-github');
    var footerYear = document.getElementById('footer-year');

    if (downloadBtn) {
      var initialUrl =
        cfg.downloadUrl && String(cfg.downloadUrl).trim() !== ''
          ? cfg.downloadUrl
          : cfg.releasesFallbackUrl || '';

      if (initialUrl) {
        downloadBtn.setAttribute('href', initialUrl);
        downloadBtn.textContent = 'Get Sur';
        downloadBtn.removeAttribute('aria-disabled');
        downloadBtn.classList.remove('is-disabled');

        if (!cfg.downloadUrl && cfg.releasesApiUrl && typeof window.fetch === 'function') {
          latestApk()
            .then(function (apkUrl) {
              if (apkUrl) {
                downloadBtn.setAttribute('href', apkUrl);
                downloadBtn.setAttribute('download', '');
              }
            })
            .catch(function () {
              /* Fallback href remains cfg.releasesFallbackUrl */
            });
        }
      } else {
        downloadBtn.removeAttribute('href');
        downloadBtn.setAttribute('aria-disabled', 'true');
        downloadBtn.classList.add('is-disabled');
        downloadBtn.textContent = 'Coming soon';
      }
    }

    var hasGithub = Boolean(cfg.githubUrl && String(cfg.githubUrl).trim() !== '');
    if (downloadGithub) {
      if (hasGithub) {
        downloadGithub.setAttribute('href', cfg.githubUrl);
        downloadGithub.hidden = false;
      } else {
        downloadGithub.hidden = true;
      }
    }

    if (footerGithub) {
      if (hasGithub) {
        footerGithub.setAttribute('href', cfg.githubUrl);
        footerGithub.hidden = false;
      } else {
        footerGithub.hidden = true;
      }
    }

    if (footerYear && cfg.year) {
      footerYear.textContent = String(cfg.year);
    }

    if (cfg.siteUrl && String(cfg.siteUrl).trim() !== '') {
      var ogImg = document.querySelector('meta[property="og:image"]');
      if (ogImg) {
        var base = String(cfg.siteUrl).replace(/\/+$/, '');
        ogImg.setAttribute('content', base + '/assets/images/og-sur.png');
      }
    }
  }

  function boot() {
    hydrateConfig();

    try {
      if (Sur.scroll && typeof Sur.scroll.init === 'function') {
        Sur.scroll.init();
      }
      if (Sur.animations && typeof Sur.animations.init === 'function') {
        Sur.animations.init();
      }
      if (Sur.navigation && typeof Sur.navigation.init === 'function') {
        Sur.navigation.init();
      }
      if (Sur.interactions && typeof Sur.interactions.init === 'function') {
        Sur.interactions.init();
      }
    } catch (_err) {
      document.documentElement.classList.add('no-engine');
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})(window.Sur = window.Sur || {});
