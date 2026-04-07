# complete/ — fixture files

Each subfolder contains an `input.dot` (incomplete automaton) and an `expected.dot`
(the automaton after calling `complete("error")`).

| Folder | Type | Alphabet | Language | Notes |
|-------|------|----------|----------|-------|
| `acb_plus/` | DFA | `{a,b,c}` | `(ac\|b⁺)+` | Several states missing transitions; adds sink state `error` |
| `acb/` | DFA | `{a,b,c}` | `{acb}` | Linear chain; almost every pair is missing |
| `zero_one_plus/` | DFA | `{0,1}` | `01⁺` | Three-state DFA; fills dead-end transitions |
| `a/` | NFA | `{a,b}` | `a` | Has duplicate transitions on `a` from state `0` (non-determinism) and a λ-transition `2 → 3`; missing `(0,b)`, `(2,a)`, `(2,b)` |
