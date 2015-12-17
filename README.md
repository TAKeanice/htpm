HTPM-Framework
===================

This is a framework based on the paper "Discovering hybrid temporal patterns from sequences consisting of point-and interval-based events" by SY Wu & YL Chen.
BibTeX:
```
@article{wu2007mining,
   title={Mining nonambiguous temporal patterns for interval-based events},
   author={Wu, Shin-Yi and Chen, Yen-Liang},
   journal={Knowledge and Data Engineering, IEEE Transactions on},
   volume={19},
   number={6},
   pages={742--758},
   year={2007},
   publisher={IEEE}
 }
```

It adds some index-structures and modifications to increase the performance.
Also one extension is added called eHTPM which adds one useful constraint.

Usage
===================
A sample usage can be found in examples/Demo.java.
Also some of the examples used in the paper are coded. You may find them in examples/paper.


Installation
===================
This is a maven project. Install with `mvn install`.