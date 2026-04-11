import { promises as fs } from "node:fs";
import path from "node:path";

const MAX_ATTEMPTS = 5;
const RETRY_DELAY_MS = 300;

const root = process.cwd();
const targetSets = {
  dev: [path.join(root, ".next-dev")],
  build: [path.join(root, ".next")],
  all: [path.join(root, ".next"), path.join(root, ".next-dev"), path.join(root, ".turbo")],
};

const mode = process.argv[2] || "all";
const targets = targetSets[mode] || targetSets.all;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function removeWithRetry(targetPath) {
  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt += 1) {
    try {
      await fs.rm(targetPath, { recursive: true, force: true, maxRetries: 0 });
      return true;
    } catch (error) {
      const code = error && typeof error === "object" ? error.code : "UNKNOWN";
      const isRetryable = code === "EPERM" || code === "EBUSY" || code === "ENOTEMPTY";

      if (!isRetryable || attempt === MAX_ATTEMPTS) {
        throw error;
      }

      await sleep(RETRY_DELAY_MS * attempt);
    }
  }

  return false;
}

async function ensureClean(targetPath) {
  try {
    await fs.access(targetPath);
  } catch {
    console.log(`[clean-next-cache] Skipped (missing): ${path.basename(targetPath)}`);
    return;
  }

  await removeWithRetry(targetPath);
  console.log(`[clean-next-cache] Removed: ${path.basename(targetPath)}`);
}

async function main() {
  console.log(`[clean-next-cache] Mode: ${mode}`);
  for (const target of targets) {
    await ensureClean(target);
  }
}

main().catch((error) => {
  console.error("[clean-next-cache] Failed to clean Next.js cache directories.");
  console.error(error);
  process.exitCode = 1;
});
