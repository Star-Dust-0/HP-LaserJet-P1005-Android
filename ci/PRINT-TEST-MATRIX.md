# Print regression coverage

## Automated gates

- Production Java unit tests: booklet selections of 1 through 256 pages, both bindings, padding, content preservation, arbitrary selected-page mapping, manual-duplex composed-sheet split/reversal, front-only and two-pass copies, failure/cancel stop decisions and clean retries.
- Existing SheetComposer tests: fold walk, scale modes, N-up reading order, poster dimensions/coverage/overlap/cut marks, physical paper dimensions.
- Activity emulator suite: actual PDF load, composition, conversion and native dialogs; synthetic USB sink only. Captures each scenario and its log. Includes one-page booklet, copies, single deduplicated page range, both bindings, injected disconnect/permission/transfer errors and retry, cancellation, size/multiple/poster, both manual-duplex edges.
- Existing UI and system PrintService smoke remain separate gates, not implied by the Activity suite.

## Evidence rules

A passing Java decision test does not prove the dialog appeared or the bitmap printed. An emulator screenshot does not prove real USB transfer, firmware behavior, paper feed, correct physical back rotation, or paper exit. Synthetic disconnect/permission/transfer faults prove the app response to those exceptions, not Android USB hardware behavior. Each emulator claim must name the passing SHA/run and inspected screenshots.

## Remaining hardware coverage

Real USB attach/detach during each phase; permission denial/revoke from Android; firmware reload after power loss; real transfer failure; manual reinsert direction/back rotation; long and short edge physical output; paper size/tray mismatch; full booklet fold check on actual printed paper. Test on one sheet before a large print job. Never present stream acceptance as confirmed printed output.

## Known follow-up risks

Print settings are partly captured at pass start but sheet spec/planning still reads Activity fields. Rapid edits/navigation during active printing and Activity recreation need an immutable job-state review. Preview and print renderer concurrency must be checked when the new live preview is integrated. The current hotfix does not silently change paper order, back rotation, firmware or PrintService semantics.
