# Atomic Commit Protocols Implementation

This project aims at implementing and evaluating several atomic commit protocols.
The reference protocol is the well-known Two Phase Commit (2PC).

## Getting started

### Compiling

This project uses [sbt](http://www.scala-sbt.org/) to compile. Simply type `sbt` to launch a shell, and then `compile` to compile the project.

### Running

Type `run` in a sbt shell.

## Licence

This project is under the GNU General Public License v3.0 - see the [LICENSE.md](LICENSE.md) file for details.

## TODO

- [x] Message wrapper class (remove javafx.util.Pair)
- [x] Fix relations between node and event handlers classes (some methods should not be public)
- [ ] System structure (Client app, query processor, storage engine, storage nodes)
- [ ] Add Timeout for receiving messages
- [ ] Parameter configuration
- [ ] Consensus interface
- [ ] Raft leader election implementation for consensus?
