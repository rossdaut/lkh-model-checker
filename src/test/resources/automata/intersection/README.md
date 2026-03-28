# intersection/ — fixture files

Each subfolder maps to one test case.

## dfa_dfa/  — intersection(DFA, DFA)

| Folder | L1 | L2 | L1 ∩ L2 |
|--------|----|----|---------|
| `aplus_bplus` | a⁺ | b⁺ | ∅ |
| `even_a_even_b` | even #a | even #b | even #a and even #b |
| `ends_a_ends_b` | (a\|b)*a | (a\|b)*b | ∅ |
| `astar_aplus` | a* | a⁺ | a⁺ |
| `acb_plus_bstar` | (ac\|b⁺)⁺ | b* | b⁺ |

Each folder contains:
- `dfa1.dot` — first DFA operand
- `dfa2.dot` — second DFA operand
- `expected.dot` — result of `intersection(dfa1, dfa2)`

## nfa_nfa/  — intersection(NFA, NFA)

Each case exercises a different non-deterministic feature.

| Folder | NFA feature | L1 | L2 | L1 ∩ L2 |
|--------|-------------|----|----|---------|
| `two_targets` | multiple successors on same symbol | a(a\|b) | (a\|b)b | {ab} |
| `lambda_start` | λ-transition out of initial state | b \| ab | ab* | {ab} |
| `lambda_accept` | λ-transition into final state | a⁺b | a⁺(b\|ε) | a⁺b |
| `mixed_nd` | both NFAs are non-deterministic | (a\|b)*a | a(a\|b)* | strings over {a,b} starting and ending with a |

Non-deterministic feature per operand:

- **`two_targets`** — `nfa1`: `q1` has two transitions on different symbols (`a` and `b`) to the same target. `nfa2`: `p0` has two transitions on different symbols (`a` and `b`) to the same target.
- **`lambda_start`** — `nfa1`: `q0` has a λ-transition to `q1` (so `q0` can reach `q1` without consuming input).
- **`lambda_accept`** — `nfa2`: `p1` has a λ-transition into the final state `p2` (accepts after consuming only a's).
- **`mixed_nd`** — `nfa1`: `q0` has two `a`-transitions (to `q0` and to `q1`), plus a λ-transition from `q1` to `q2` (both final). `nfa2`: `p0` has two `a`-transitions (to `p1` and to `p2`, both looping states).

Each folder contains:
- `nfa1.dot` — first NFA operand
- `nfa2.dot` — second NFA operand
- `expected.dot` — result of `intersection(nfa1, nfa2)` (Integer states)

## set/  — intersection(Set<DFA>)

| Folder | Contents | L |
|--------|----------|---|
| `single` | 1 DFA for a⁺ | a⁺ |
| `two_dfas` | a* ∩ a⁺ | a⁺ |
| `three_dfas` | (ac\|b⁺)⁺ ∩ (a⁺cb)⁺ ∩ acb | acb |
| `four_dfas` | even_a ∩ even_b ∩ ends_a ∩ aplus | strings over {a,b} with even #a, even #b, ending in a, with at least one a |

