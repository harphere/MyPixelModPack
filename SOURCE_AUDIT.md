# Source audit — 3.7.0

Baseline: exact delivered 3.1.0; original rectangle restored from exact 3.0.1 drawable. Battery J geometry class removed. GradientBatteryDrawable adds body-only orientation and a preview scale, preserving upright percentage and the separate bolt. BatteryModule scales actual icon layout dimensions and reads orientation through its existing observer. SettingsProvider adds owner-only setters and per-style clamped sizes, respecting the active override. UI adds orientation and per-style sizing controls. Home diagnostics include size/orientation.

Rectangle defaults to half width/height, circle styles to their previous size. Range 25–100%. Saved style keys, percentage, animation options and provider authorities are retained. No new zygote or System UI hook is introduced. ChromePie, Nav Match, Gboard and all other component implementations match 3.1.0.

Version 3.7.0 / code 39; existing key unchanged. Build/source guards remain; .gitkeep tolerance retained. Workflow removes obsolete BatteryJPaths.java. Tower concept XML/SVG/PNG lives only in docs; no new status-icon hooks are enabled.
