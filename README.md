# Atomic Commit Protocols Implementation

This project aims at implementing and evaluating several atomic commit protocols from "[How Fast can a Distributed Transaction Commit?](https://infoscience.epfl.ch/record/225579)".
The reference protocol is the well-known Two Phase Commit (2PC).

## Getting started

### Compiling

This project uses [sbt](http://www.scala-sbt.org/) to compile. Simply type `sbt` to launch a shell, and then `compile` to compile the project.

### Running

Type `run` in a sbt shell.

## Licence

This project is under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## TODO

- [x] Better organization (sub-packages?)
- [x] Parameters for proper configuration
- [x] Indulgent Consensus implementation (Raft leader election?)
- [x] 0NBAC Protocol
- [ ] INBAC Protocol
- [ ] Add == null tests and handle problems (exceptions?)
- [ ] Documentation and comments
