# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
## Added
- Core: transaction validation
- Core: priority transaction selection
- Core: block capacity
- Database: indexes on tables
- RPC: recalled flag to casted votes

## Changed
- Database moved to H2
- Core: unconfirmed balances calculation improved

## [0.8.0] - 2018-09-07
## Added
- Consensus: delegate public key field to a delegation transaction
- RPC: delegate public key parameter to a delegation transaction request
- Network: node ID as a network identifier
- Core: unconfirmed output balance to a wallet
- RPC: wallet address validation
- RPC: node ID added to active delegate response
- Wallet UI: Send delegate transaction
- Wallet UI: Send vote transaction
- Wallet UI: Transactions list

## Changed
- Network: addresses exploring mechanism
- Network: nodes reconnection mechanism
- Core: synchronization mechanism
- Core: wallet balances calculation improved

## [0.7.0] - 2018-08-31
## Added
- Master nodes initiation as active delegates on chain start
- Configuration file
- Network: Greeting response message contains node's external host
- RPC: Get delegates by wallet address
- Consensus: Added amount field to delegate transaction

## Changed
- Node private key moved from a separate file to a configuration file
- Network: Improved connection to active delegates
- Core: Improved vote transaction validation when a wallet had already voted
- Core: Empty block creation is allowed
- Wallet: Changed update balance logic

## [0.6.1] - 2018-08-27
### Added
- Application properties for docker

## [0.6.0] - 2018-08-24
### Added
- Network: A UID for nodes
- Sync: Up node synchronization
- Sync: Application synchronization status
- Sync: Check application synchronization status and start synchronization if need
- RPC: Get explorer info
- RPC: Get a list of blocks 
- RPC: Get a list of transactions 
- RPC: Main and genesis block endpoints unit tests
- Wallet UI: Create a transaction
- Wallet UI: Past transaction list
- Core: Reward transaction

### Changed
- Core: Genesis and Main blocks are not broadcasted after creation, pending blocks are broadcasted instead
- Core validation: Now it is impossible to create vote transaction on the nonexistent delegate
- Core validation: Now it is impossible to generate a request of vote transaction with the nonexistent vote type
- Core validation: Now it is impossible to create transactions with a negative fee
- Core validation: Improved fee and amount validation for the transfer transaction
- Core: Removed validation from main block service on adding a block
- Core: Removed validation from genesis service on adding a block
- Core: Removed field 'reward' from blocks

## [0.5.0] - 2018-08-10
### Added
- Consensus: Intervals between time slots for synchronization
- Consensus: Prevote stage on block creation
- Consensus: Commit stage on block creation
- Wallet UI: Generate a seed phrase
- Wallet UI: Create a wallet
- Wallet UI: Save wallet data in a file
- Wallet UI: Protect the file with password
- Wallet UI: Access wallet with file
- Wallet UI: Restore wallet from a private key
- Wallet UI: Restore wallet from the seed phrase
- RPC: Total number of nodes connected to the network
- RPC: Get number of current epoch
- RPC: Get when the current epoch started
- RPC: Get number of delegates
- RPC: Get number of an epoch when the block was created
- RPC: Import private key in hex format
- RPC: Get transactions by wallet address

### Changed
- New file structure

## [0.4.0] - 2018-07-27
### Added
- Time slots mechanism
- Delegate transaction
- Vote/Recall vote transaction
- Vote weight based on wallet balance
- Reward transaction
- Transaction sign
- Delegate transaction verification
- Vote/Recall vote verification
- Reward transaction verification
- Transaction fee
- Genesis address defined in properties
- Transactions fees are defined in properties 
- Reward for block creation is defined in properties
- RPC: Add delegate transaction
- RPC: Add vote/recall vote transaction
- RPC: Add transfer transaction
- RPC: Get delegates info
- RPC: Page request filter
- RPC: Application version and timestamp fields to all responses
- TCP: Application version and timestamp fields to all packets
- Database: unconfirmed transactions tables
- Synchronization of blocks and transactions after node up

### Changed
- Serialization mechanism to support kotlin classes
- Transactions structure

## [0.3.0] - 2018-07-13
### Added
- Address mixed-case checksum
- Address validation
- RPC requests: validateAddress
- Vote transaction type
- Voting algorithm mechanism
- Voting storing
- Delegate rating calculation
- Active delegates storing
- Wallet balance calculation
- Database: added Wallet entity
- RPC requests: getWalletBalance
- Sign data
- Verify signed data
- Define epoch duration in properties
- Get current epoch height
- Check if genesis block is needed
- Sharing peers between nodes
- Maintaining necessary connections count
- Connection establishing logic
- Scheduled check of connections
- Reconnection logic
- Generating private and public key pairs on app start and saving in a file if not existing
- Shuffle active delegates
- Transaction Merkle hash calculation
- Block production
- Block validation
- Block, transaction, signature conversion from protobuff to the business object and vice versa
- Accumulation of signatures from nodes
- Block approving
- Block applying by scheduling

### Changed
- Default account info is added to the doGenerateMaster end-point response
- doGenerateMaster end-point renamed to doRestore
- Block are divided by types: GenesisBlock and MainBlock

## [0.2.0] - 2018-06-29
### Added
- Seed phrase generator
- Seed phrase validator
- Generate a master key with seed phrase
- Key derivation algorithm
- Get derivation key of master key by seed phrase and derivation path
- Get a serialized public key value in base58 format from extended key
- Get serialized private key value in base58 format from extended key
- End-point: get a master key by seed phrase
- End-point: get derivation key with an address of master key by seed phrase and derivation path
- Extended private key serialization
- Extended public key serialization
- Export private key
- Import private key
- RPC requests: importKey
- RPC requests: importKey in WIF (Wallet Import Format)
- RPC requests: generateKeys

## [0.1.0] - 2018-06-18
### Added
- Setup Netty server for socket connection
- Setup Netty clients for socket connection
- Setup Netty server for HTTP
- Binary serialization communication protocol
- Binary deserialization communication protocol
- Time synchronization
- End-point chain stability check
- Heartbeat implementation
- Database structure
- Database: setup database connection
- Database: add Base entity
- Database: add Block entity
- Database: add Transaction entity
- Database: setup repository for Block entity
- Database: setup repository for Transaction entity
- Service: define base service interface for Block entity
- Service: define base service interface for Transaction entity
- Service: add base implementation Block entity service
- Service: add base implementation Transaction entity service
- RPC requests: getVersion
- RPC requests: getHardwareInfo
- RPC requests: getTimestamp

[Unreleased]: https://github.com/OpenFuturePlatform/open-chain/compare/master...sprint
[0.8.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.6.1...v0.7.0
[0.6.1]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/OpenFuturePlatform/open-chain/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/OpenFuturePlatform/open-chain/compare/fea19b11de90c89689eff8d2187fd332ee566d19...v0.1.0