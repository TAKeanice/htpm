# Constraint guide

## What are constraints

All pattern mining algorithms rely on pruning patterns that will be irrelevant to the user.
HTPM is no different in this regard.
The constraint system allows to apply different criteria to calculate whether patterns are relevant.

## Designing a constraint

Constraints should be anti-monotone. That is, subpatterns of relevant patterns must not be discarded.

There is one exception: The method to filter output patterns can apply any kind of filter.
On the other hand, it does not help the algorithm to prune the search tree.

The constraint methods are called in the following order:
1. generate patterns of length k?
2. join patterns?
3. join occurrence records?
4. occurrences fulfill constraints?
5. patterns fulfill constraints?
6. generated patterns!
7. should output patterns?

Method number 6 can be used by the constraint to save certain patterns that it needs later.

There are some optional bookkeeping methods, which return the number of refused patterns, occurrences or prevented joins.
The constraint is responsible four counting them. They do not serve any algorithmic purpose.

## Overview of implemented constraints

### Regular constraints

1. DefaultHTPMConstraint

    The constraint suggested by the original paper.
    Either this one or the EpisodeMiningConstraint should be used as an effective way of removing infrequent patterns.
    Discards patterns by their support value,
    which is calculated as the ratio of sequences in which the pattern occurs to all sequences in the database.
    This constraint assumes that there are a number of sequences larger than one in the database, 
    otherwise the mining will be exhaustive.
    
2. MaxDurationConstraint
    
    An additional constraint for keeping the duration (in the time units of the sequence database) limited.
    It checks patterns of size 1 or 2 if they are of correct duration.
    Afterwards, by the HTPM way of joining patterns, no overly lengthy occurrences can be built any more.
    
3. EpisodeMiningConstraint
    
    Builds on top of MaxDurationConstraint.
    In contrary to the DefaultHTPMConstraint, support is calculated differently:
    The number of occurrences of a pattern is counted instead of the ratio of supporting and all sequences.
    A pattern is discarded if the number is lower than the provided minimal number of occurrences.
    This constraint can handle databases with only one long sequence.
    It requires a reasonably short duration for the duration constraint, otherwise the mining will be exhaustive.
    
4. PatternSizeConstraint

    Prevents the output of short patterns and prevents mining of patterns over a length limit.
    The length constraints refer to the number of elements of the pattern, NOT the duration.
    
5. CMAP Constraint

    Can be used to improve HTPM performance.
    The CMAP datastructure saves all patterns of length 2. 
    When a pattern join is requested, all potential outcomes of the suffix join are examined:
    If the join of the pattern suffixes cannot build a frequent 2-pattern, the join is be prevented.
    This constraint can help if there are many joinable occurrences of two patterns.
    To work properly, the CMAP constraint must only be applied to search strategies 
    that mine ALL 2-patterns before mining other patterns

### Special constraints

1. ConstraintCollection

    Class used for combining constraints.
    Performance consideration: All constraints are applied in the given order.
    As soon as one constraint returns "false" for a boolean method, further constraints are not considered.
    Thus, Constraints that come first should be the most restrictive with least effort.

2. AcceptAllConstraint
    
    Superclass for constraints that do not want to implement all methods of HTPMConstraint.
    Does not filter anything.
