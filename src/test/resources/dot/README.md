# dot test resources

Each subfolder contains a hand-written `.dot` file used by `DotReaderTest` and `DotWriterTest`.

| Folder        | Type | Alphabet | Language / Description                                           |
|---------------|------|----------|------------------------------------------------------------------|
| `dfa_simple`  | DFA  | {a, b}   | Strings ending in an odd number of consecutive 'a's (ends in a) |
| `nfa_simple`  | NFA  | {a, b}   | Strings of length 2 where the second symbol is 'a'              |
| `nfa_lambda`  | NFA  | {a, b}   | Strings of length ≥ 1 (accepts after consuming one symbol, with λ-back) |

