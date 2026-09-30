# Component source audit

| Feature | Source used | Integration changes |
|---|---|---|
| Battery Gradient | BatteryGradient 1.0.10 | Provider authority retargeted; no pack gate inside component; standalone hooks retained. Includes independent Iconify row placement and Tasker receiver. |
| Gboard cursor arrows | GboardCursorKeys 1.0.10 navrow | Standalone source restored without pack gate. Launcher navigation row and Gboard receiver protocol retained. |
| Mobile type icons | MobileTypeFrame 1.4.0 | Provider authority retargeted; resource and binder hooks routed by dispatcher. |
| VoLTE / VoWiFi | VoServiceFrame GH Actions 1.0.2 | Resource imports use combined R class. Style settings use independent `vo_icons_v2` snapshot instead of standalone package file access. |
| OPA Home | PixelOpaHome 1.2.1 | Standalone launcher touch/animation code restored. Its initZygote method only logs and needs no forwarding. |
| Navigation icons | NavDotStyle 1.1.1, legacy port from pack 1.1.1 | Keep direct XC_MethodHook port; `nav_icons_v2` settings snapshot. |
| Nav Bar Status Match | NavBarStatusMatch 1.0.8, legacy port/fix from pack 1.1.1 | Keep direct XC_MethodHook port and lazy Handler; independent `nav_match_v2` settings snapshot; skip SystemUI/self. |
| Network activity | PXNetworkActivityStandalone 1.0.2 alpha3 | Provider authority and preference file retargeted; standalone traffic measurement/view hooks restored. |
| DT2W | PixelDoubleTapWake InfinityX A16 GitHub fixed | Install after Application.attach; suppress first doze tap only after reset scheduling succeeds; rearm and native double-tap behavior retained. |

Provider authorities are all under the combined package. The preference stores used by navigation icons, navigation matching, Vo settings, network display and the master switches are independent. Appearance settings in the standalone packages are not automatically imported.

No module constructor runs from the pack's LSPosed entry constructor. No feature preference is read during zygote specialization. All component instances belong to the target process and are initialized with its class loader.
