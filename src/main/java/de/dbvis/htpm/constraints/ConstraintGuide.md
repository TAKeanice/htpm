# Constraint guide

## What are constraints

All pattern mining algorithms rely on pruning patterns that will be irrelevant to the user.
HTPM is no different in this regard.
The constraint system allows to apply different criteria to calculate whether patterns are relevant.

## Ways of pruning

It is possible to prune patterns during the mining.
This requires, for correctness, that a pruned pattern (occurrence) is not a subpattern (suboccurrence) 
of a valid pattern (occurrence). This is also called "anti-monotone" or "Apriori" property.

The HTPM variants work by joining two patterns with the same prefix for creating a longer pattern.
Thus, the prefixes of all patterns in a search space branch are the same. If a constraint has a property,
which can determine whether a pattern with a certain prefix can (not) be valid, 
this can be used for pruning of the search space just like an anti-monotone property.
The difference to anti-monotone pruning is, that a pattern with a pruned prefix could be a sub-pattern of a valid pattern.
But since that valid pattern is created on another search space branch, it does not depend on the pruned branch.

If a condition is not anti-monotone and cannot filter all invalid patterns by knowing their prefix,
the constraint can only jump into action for the pattern output. This kind of pruning does not speed up the algorithm,
but can filter irrelevant patterns by any measure of utility.

## Designing a constraint

No methods of a constraint should filter a pattern (an occurrence), 
if its prefix can be the prefix of a valid pattern (occurrence). Note that there is ambiguity between string and integer id order of 

There are two exceptions: The methods to filter output patterns (occurrences) can apply any kinds of filter.
In exchange for the flexibility, they do not help the algorithm to prune the search tree.
Conditions for the output filters must be at least as strong as the conditions applied during mining.
They must not allow the output of any patterns that were pruned by this constraint during mining.

The constraint methods are called in the following order:
1. generate patterns of length k?
2. join patterns?
3. join occurrence records?
4. occurrence fulfills constraints?
5. pattern fulfill constraints?
6. generated pattern!
7. should output occurrence?
8. should output pattern?

Method number 6 can be used by the constraint to save patterns that it needs later for evaluation.
Not all reported patterns are results of the HTPM algorithm: some of them could be pruned by output filters later on.

There are some optional bookkeeping methods, which return the number of refused patterns, refused occurrences 
or prevented pattern / occurrence joins during mining.
The constraint is responsible for counting them. The counting does not serve any algorithmic purpose, and can be omitted.

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
    
3. MaxGapConstraint

    Constraint for assuring that occurrences do not have large gaps in them. For continuous patterns without gaps,
    a gap of 0 can be applied. The constraint improves mining performance by filtering occurrences that have gaps in the prefix.
    Such patterns have less valid occurrences, which would not become more later during mining, thus that property is anti-monotone.
    When a pattern is about to be output, filters for gaps in the complete occurrence.
    
4. MinOccurrencesConstraint
    
    In contrary to the DefaultHTPMConstraint, support is calculated differently:
    The number of occurrences of a pattern is counted instead of the ratio of supporting and all sequences.
    A pattern is discarded if the number is lower than the provided minimal number of occurrences.
    This constraint can handle databases with only one long sequence, but it is advisable to add a maxDuratioonConstraint.
    
5. PatternSizeConstraint

    Prevents the output of short patterns and prevents mining of patterns over a length limit.
    The length constraints refer to the number of elements of the pattern, NOT the duration.
    
6. CMAP Constraint

    Can be used to improve HTPM performance.
    The CMAP datastructure saves all patterns of length 2. 
    When a pattern join is requested, all potential outcomes of the suffix join are examined:
    If the join of the pattern suffixes cannot build a frequent 2-pattern, the join is be prevented.
    This constraint can help if there are many joinable occurrences of two patterns.
    To work properly, the CMAP constraint must only be applied to search strategies 
    that mine ALL 2-patterns before mining other patterns
    
7. Subpattern Constraint

    Filters patterns by the subpatterns that they have.
    Only applied to output.
    
8. Regex Constraint

    Filters patterns by Java Regex. This allows many wild filters.
    The boolean parameter whether the constraint is "prefix selective" can have a performance impact:
    If the regular expression filters patterns with a certain prefix, e.g. ^[a,b]+.*, 
    setting the parameter to true makes the constraint check patterns for fulfillment of the constraint during mining.
    If the constraint applies at least partially, and can match completely by adding parts to the pattern, 
    the pattern is allowed for further mining.
    On the other hand, switching the parameter to false skips the tests during mining 
    and applies the constraint only to just before output.

### Special constraints

1. ConstraintCollection

    Class used for combining constraints.
    Performance consideration: All constraints are applied in the given order.
    As soon as one constraint returns "false" for a boolean method, further constraints are not considered.
    Thus, Constraints that come first should be the most restrictive with least effort.

2. AcceptAllConstraint
    
    Superclass for constraints that do not want to implement all methods of HTPMConstraint.
    Does not filter anything.
