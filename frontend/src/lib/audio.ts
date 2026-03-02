/**
 * Audio utilities for KDS alert sounds using the Web Audio API.
 *
 * Uses the Web Audio API to generate a brief notification beep — no audio
 * file assets required. Gracefully no-ops when the API is unavailable
 * (e.g. server-side rendering or test environments).
 */

/** Play a short two-tone beep to alert kitchen staff about a new order. */
export function playNewOrderAlert(): void {
  if (typeof window === "undefined" || !window.AudioContext) return;

  try {
    const ctx = new window.AudioContext();
    playTone(ctx, 880, 0, 0.12);
    playTone(ctx, 1100, 0.15, 0.12);
    // Close context after tones finish to avoid leaking AudioContext instances
    setTimeout(() => void ctx.close(), 600);
  } catch {
    // Ignore — browser may block autoplay before user interaction
  }
}

// ── Internal helper ───────────────────────────────────────────────────────────

function playTone(
  ctx: AudioContext,
  frequency: number,
  startOffset: number,
  duration: number
): void {
  const oscillator = ctx.createOscillator();
  const gain = ctx.createGain();

  oscillator.connect(gain);
  gain.connect(ctx.destination);

  oscillator.type = "sine";
  oscillator.frequency.value = frequency;

  gain.gain.setValueAtTime(0.25, ctx.currentTime + startOffset);
  gain.gain.exponentialRampToValueAtTime(
    0.001,
    ctx.currentTime + startOffset + duration
  );

  oscillator.start(ctx.currentTime + startOffset);
  oscillator.stop(ctx.currentTime + startOffset + duration);
}
