# Limitless Breathe → Journal integrated Android QA (fork only)

**Never merge or promote this composition branch upstream.** The reviewed components remain independent in [contextual UI #51](https://github.com/kvnloo/noop/pull/51) and [habit Journal #54](https://github.com/kvnloo/noop/pull/54), with [explicit journal copy confirmation #58](https://github.com/kvnloo/noop/pull/58) incorporated into #54. Parent commits: #54 `a90a3f7ba1ead9bddef094d25a48c9545a5132d0`; #51 `1156204e460dc2cf3d4895d4b900c1a97ddd4792`.

The branch overlays #51's 11 disjoint changed files onto #54's tree, retaining both parent histories. Source relative to origin `ryanbr/noop@8e4b0c1` is unchanged outside the existing experimental PRs and this branch's isolated CI/doc additions. This combined build exists to test the complete user path, **not** to claim upstream readiness.

## Qualification
1. Exact-head GitHub workflow `Limitless Android integration QA`: build `assembleFullDebug`, run `testFullDebugUnitTest`, attach a debug APK and its SHA-256 to the same commit's workflow run.
2. Use the APK only after confirming its actual Android package/signing identity; preserve and back up any previous NOOP installation first. Do not claim the app is installed or tested on a real device because CI built an APK.
3. Device walkthrough: Today → Breathe; end before 120 seconds (**no Log journal CTA**); complete 120+ seconds (**CTA** even when R-R data are missing); Journal → optional Two-minute habit (create once) → Tomorrow → explicit Yes, No, or no answer; app reopen and retain user-entered state.
4. Day truthfulness: opening an empty current-day Journal cannot persist yesterday's Yes or No. On the optional **Copy previous answers** action, require explicit consent, no overwrite of today's native/imported answers, no numeric dose copied as Boolean, and correct midnight/year boundary behavior.
5. Exercise disconnected strap, no available Charge, BLE scanning transition, TalkBack/large font/small display, localization, restart and export/backup before pilot evidence is usable.

**Evidence:** CI tests synthetic contract/compilation only; no device, clinical efficacy, or personal outcome claim. #42–44 remain on hold, no new score/database/network/permission.
