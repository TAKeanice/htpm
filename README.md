HTPM-Framework
===================

This is a framework based on the paper "Discovering hybrid temporal patterns from sequences consisting of point-and interval-based events" by SY Wu & YL Chen.
BibTeX:
```
@article{wu2009discovering,
  title={Discovering hybrid temporal patterns from sequences consisting of point-and interval-based events},
  author={Wu, Shin-Yi and Chen, Yen-Liang},
  journal={Data \& Knowledge Engineering},
  volume={68},
  number={11},
  pages={1309--1330},
  year={2009},
  publisher={Elsevier}
}
```

The performance is improved over the original version by search space partitioning and parallelization.
It also features a constraint framework, which enables a much more flexible use and exploring larger search spaces by better pruning.
Additionally, a low-memory depth-first variant of HTPM was developed.
An experimental, even more parallelized but less performant version based on the depth-first variant is part of the package.

Usage
===================
A sample usage can be found in examples/Demo.java.
Also some of the examples used in the paper are coded. You may find them in examples/paper.


Installation
===================
This is a maven project. Install with `mvn install`.
