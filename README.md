# Atomic Commit Protocols Implementation

This project aims at implementing and evaluating several atomic commit protocols.
The reference protocol is the well-known Two Phase Commit (2PC).

## Getting started

### Compiling

This project uses [sbt](http://www.scala-sbt.org/) to compile. Simply type `sbt` to launch a shell, and then `compile` to compile the project.

### Running

Type `run` in a sbt shell.

## Licence

This project is under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## TODO

- [ ] Better organization (sub-packages?)
- [ ] Parameters for proper configuration
- [ ] Indulgent Consensus implementation (Raft leader election?)
- [ ] 0NBAC Protocol
- [ ] NBAC Protocol
