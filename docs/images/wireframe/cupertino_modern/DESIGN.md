---
name: Cupertino Modern
colors:
  surface: '#f9f9fe'
  surface-dim: '#d9dade'
  surface-bright: '#f9f9fe'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f8'
  surface-container: '#ededf2'
  surface-container-high: '#e8e8ed'
  surface-container-highest: '#e2e2e7'
  on-surface: '#1a1c1f'
  on-surface-variant: '#414755'
  inverse-surface: '#2e3034'
  inverse-on-surface: '#f0f0f5'
  outline: '#717786'
  outline-variant: '#c1c6d7'
  surface-tint: '#005bc1'
  primary: '#0058bc'
  on-primary: '#ffffff'
  primary-container: '#0070eb'
  on-primary-container: '#fefcff'
  inverse-primary: '#adc6ff'
  secondary: '#5d5e63'
  on-secondary: '#ffffff'
  secondary-container: '#e0dfe4'
  on-secondary-container: '#626267'
  tertiary: '#006b27'
  on-tertiary: '#ffffff'
  tertiary-container: '#008733'
  on-tertiary-container: '#f7fff2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d8e2ff'
  primary-fixed-dim: '#adc6ff'
  on-primary-fixed: '#001a41'
  on-primary-fixed-variant: '#004493'
  secondary-fixed: '#e3e2e7'
  secondary-fixed-dim: '#c6c6cb'
  on-secondary-fixed: '#1a1b1f'
  on-secondary-fixed-variant: '#46464b'
  tertiary-fixed: '#72fe88'
  tertiary-fixed-dim: '#53e16f'
  on-tertiary-fixed: '#002107'
  on-tertiary-fixed-variant: '#00531c'
  background: '#f9f9fe'
  on-background: '#1a1c1f'
  surface-variant: '#e2e2e7'
  system-green: '#34C759'
  system-orange: '#FF9500'
  system-red: '#FF3B30'
  system-gray: '#8E8E93'
  surface-grouped: '#F2F2F7'
  surface-base: '#FFFFFF'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 34px
    fontWeight: '700'
    lineHeight: 41px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 30px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: 0em
  body-lg:
    fontFamily: Inter
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Inter
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0em
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 13px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  margin-mobile: 16px
  margin-desktop: 32px
  gutter: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

The design system is centered on the principles of clarity, deference, and depth. It mimics a premium OS-level experience by prioritizing content through extreme functional minimalism. The brand personality is professional yet approachable, characterized by a "quiet luxury" that feels both high-tech and human-centric.

The aesthetic follows a **Modern Corporate** style with heavy influences from **Glassmorphism**. It relies on high-contrast typography and a rigid adherence to whitespace to create a sense of systematic order. The interface should feel like a native extension of the user's device, utilizing blurred backgrounds and translucent materials to provide spatial context without visual clutter.

## Colors

The palette utilizes the classic iOS spectrum, where color is used purposefully to indicate interactivity or status.

- **Primary (Apple Blue):** Reserved for primary actions, active states, and essential navigational glyphs.
- **Secondary (System Gray):** Used for secondary labels, inactive states, and iconography that should not compete for attention.
- **Semantic Palette:** Success (Green), Warning (Orange), and Error (Red) colors follow the system standard for instant cognitive recognition.
- **Neutral/Surface:** The system uses a tiered background approach. The base canvas is white (`#FFFFFF`), while grouped background sections use a soft gray (`#F2F2F7`) to create logical separation without the need for heavy borders.

## Typography

This design system uses **Inter** as the primary typeface, configured to mirror the optical properties of SF Pro.

The typographic hierarchy is "tight"—meaning font sizes do not vary wildly in standard views, but weight is used aggressively to signal importance.
- **Headlines:** Use Bold (700) and Semi-Bold (600) with slightly negative letter spacing to feel compact and authoritative.
- **Body:** Set to 17px for standard reading to ensure accessibility and a premium "modern" feel.
- **Labels:** Use Medium (500) and Semi-Bold (600) for small-scale UI elements like buttons or tags.

## Layout & Spacing

The layout philosophy follows a **Fluid Grid** with fixed-width constraints on ultra-wide screens.

- **The 8pt System:** All spatial increments are multiples of 8px. This ensures mathematical consistency across different pixel densities.
- **Margins:** Desktop views utilize 32px margins for significant "breathing room," while mobile views collapse to 16px to maximize screen real estate.
- **Rhythm:** Use "Stack" logic for vertical spacing. Elements within a component use `stack-sm` (8px), while distinct components on a page use `stack-lg` (32px).
- **Safe Areas:** On mobile, ensure all interactive elements account for home indicator and status bar offsets to maintain native-app comfort.

## Elevation & Depth

Hierarchy is established through **Glassmorphism** and **Ambient Shadows** rather than high-contrast borders.

1.  **Level 0 (Base):** White or Light Gray canvas.
2.  **Level 1 (Surface):** Cards and containers use a very soft, diffused shadow (`0 2px 10px rgba(0,0,0,0.05)`) with no visible border, creating a floating effect.
3.  **Level 2 (Overlays):** Modals, sidebars, and sticky navigation headers use a `backdrop-filter: blur(25px)` with a semi-transparent white tint (`rgba(255,255,255,0.7)`).
4.  **Level 3 (Popovers):** Active dropdowns or context menus use a slightly more pronounced shadow to separate them from the Level 2 blur.

## Shapes

The system uses high-radius **Rounded** corners to evoke the "Squircle" aesthetic. 

- **Interactive Elements:** Buttons and input fields use a 12px radius.
- **Containers:** Large cards and modals use a 16px to 20px radius. 
- **Icons:** Should reside in square containers with a subtle corner radius (approx 20% of width) for a cohesive look.
- **Avatars:** Circular shapes are used exclusively for user profiles to differentiate "people" from "objects" or "cards."

## Components

### Buttons
- **Primary:** Filled with Primary Blue, white text, 12px corner radius. Bold weight.
- **Secondary:** Filled with `rgba(0, 122, 255, 0.1)` (tinted blue) with Primary Blue text.
- **Action Links:** Text-only buttons using Primary Blue with no underline, following the "Plain Button" pattern.

### Cards
- Surfaces are white with a subtle 1px inner stroke of 5% black. They should feel light and integrated into the background. Use 16px padding for internal content.

### Input Fields
- Standard fields use a subtle gray fill (`#F2F2F7`) to indicate "hollow" state. On focus, they transition to a white background with a soft 2px blue ring or high-contrast border.

### List Items
- Use "Inset Grouped" styling for mobile settings or lists—rounded containers with white backgrounds and gray dividers that don't reach the edge of the container.

### Chips & Badges
- Pill-shaped with a 999px radius. Backgrounds should be light tints of the semantic colors (e.g., light green background for success labels).

### Navigation
- Top navigation should be sticky with a blur effect. Tab bars (mobile) use a 49pt height with thin-stroke icons and labels in `label-sm`.