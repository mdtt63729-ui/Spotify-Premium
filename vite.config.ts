import fs from 'fs';
import path from 'path';
import {defineConfig} from 'vite';

export default defineConfig(() => {
  return {
    base: './',
    plugins: [
      // Copies static assets, classic scripts, stylesheets, and GitHub Pages files
      // into dist/ so Vite builds preserve the exact vanilla static structure.
      {
        name: 'copy-static-sur-site',
        apply: 'build',
        closeBundle(error?: Error): void {
          if (error) {
            return;
          }
          const distDir = path.resolve(__dirname, 'dist');
          if (!fs.existsSync(distDir)) {
            return;
          }
          for (const dir of ['assets', 'css', 'js']) {
            const src = path.resolve(__dirname, dir);
            const dest = path.resolve(distDir, dir);
            if (fs.existsSync(src)) {
              fs.cpSync(src, dest, {recursive: true, force: true});
            }
          }
          for (const file of ['.nojekyll', 'README.md']) {
            const src = path.resolve(__dirname, file);
            const dest = path.resolve(distDir, file);
            if (fs.existsSync(src)) {
              fs.copyFileSync(src, dest);
            }
          }
        },
      },
    ],
    server: {
      hmr: process.env.DISABLE_HMR !== 'true',
      watch: process.env.DISABLE_HMR === 'true' ? null : {},
    },
  };
});
