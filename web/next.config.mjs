import { PHASE_DEVELOPMENT_SERVER } from "next/constants.js";

/**
 * Use different dist dirs for dev and build to avoid cache/artifact races
 * when both commands are run close together.
 */
export default function nextConfig(phase) {
	return {
		distDir: phase === PHASE_DEVELOPMENT_SERVER ? ".next-dev" : ".next",
	};
}
