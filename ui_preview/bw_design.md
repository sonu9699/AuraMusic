---
name: Monochrome Edition
colors:
  surface: '#0A0A0A'
  surface-dim: '#0A0A0A'
  surface-bright: '#222222'
  surface-container-lowest: '#000000'
  surface-container-low: '#0F0F0F'
  surface-container: '#141414'
  surface-container-high: '#1A1A1A'
  surface-container-highest: '#2A2A2A'
  on-surface: '#FFFFFF'
  on-surface-variant: '#8A8A8F'
  inverse-surface: '#FFFFFF'
  inverse-on-surface: '#000000'
  outline: '#3A3A3C'
  outline-variant: '#1C1C1E'
  surface-tint: '#FFFFFF'
  primary: '#FFFFFF'
  on-primary: '#000000'
  primary-container: '#FFFFFF'
  on-primary-container: '#000000'
  inverse-primary: '#000000'
  secondary: '#8E8E93'
  on-secondary: '#000000'
  secondary-container: '#1C1C1E'
  on-secondary-container: '#FFFFFF'
  tertiary: '#AEAEB2'
  on-tertiary: '#000000'
  tertiary-container: '#2C2C2E'
  on-tertiary-container: '#FFFFFF'
  error: '#FF453A'
  on-error: '#000000'
  error-container: '#FF453A'
  on-error-container: '#000000'
  primary-fixed: '#FFFFFF'
  primary-fixed-dim: '#CCCCCC'
  on-primary-fixed: '#000000'
  on-primary-fixed-variant: '#333333'
  secondary-fixed: '#8E8E93'
  secondary-fixed-dim: '#636366'
  on-secondary-fixed: '#000000'
  on-secondary-fixed-variant: '#1C1C1E'
  tertiary-fixed: '#AEAEB2'
  tertiary-fixed-dim: '#8E8E93'
  on-tertiary-fixed: '#000000'
  on-tertiary-fixed-variant: '#2C2C2E'
  background: '#000000'
  on-background: '#FFFFFF'
  surface-variant: '#1C1C1E'
typography:
  display-lg:
    fontFamily: Sora
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Sora
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Sora
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  body-md:
    fontFamily: Sora
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-sm:
    fontFamily: Space Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.1em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  container-padding: 24px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style
The **Monochrome Edition** design system represents the absolute apex of minimalist luxury and technical precision. Reversing the visual clutter of high-saturated gradients, it adopts a high-contrast editorial look. It targets audiophiles who seek visual calm and uncompromising aesthetics.

Drawing inspiration from high-end print design, classical photography journals, and luxury couture brands like Saint Laurent and Leica, it features deep, pure OLED black (#000000) combined with crisp, bright white (#FFFFFF). Micro-textures are replaced with hairline borders, precise typographic layouts, and structural grid alignments.

## Colors
The palette is built purely on shades of light and shadow, with zero colorful hues.

- **Primary Accent (Pure White):** Used for primary buttons, active indicator states, text highlights, and focus indicators.
- **Background (Pure OLED Black):** Deep, pure black to maximize visual isolation of album artwork.
- **Surfaces (Grayscale Shades):** Grays ranging from deep charcoal (#0A0A0C) to slate (#1C1C1E) represent elevation layers.
- **Borders:** Thin 0.5px to 1px borders in `rgba(255, 255, 255, 0.1)` define boundaries.

## Typography
The typography balances geometric strength with monospaced precision.

- **Sora** provides bold, expressive headers and titles.
- **Inter** ensures crystal-clear readability for lists and body copy.
- **Space Mono** provides the "studio control" look, used for timestamps, bitrates, audio formats, and active system notifications.

## Spacing & Layout
A strict 8px spacing rhythm ensures perfect visual balance. Sections are clearly demarcated by whitespace rather than heavy dividing lines.

## Components
- **Transport Controls:** The Play/Pause button is a solid white circle with a black icon, emitting a clean, subtle white halo.
- **Progress Sliders:** A solid white scrubber knob on a thin white active track.
- **Equalizers & Waveforms:** Styled in high-contrast solid white.
- **Offline Toggles:** Solid white toggle slider.
